package com.creativemobile.automation.tasks;

import com.android.ddmlib.*;
import com.creativemobile.automation.helpers.Grep;
import com.creativemobile.automation.shell.ActivityMemoryReceiver;
import com.creativemobile.automation.shell.ActivityRunningReceiver;
import com.creativemobile.automation.shell.ShellOutputReceiver;

import java.io.IOException;

public class TestRunnable implements Runnable {
    private IDevice device;
    private String apkDir;
    private String apkFile;
    private String apkPack;
    private String apkActivity;
    private String obbFile;
    private int memoryUsage = 0;
    private String fileName;
    public IShellOutputReceiver  iShellOutputReceiver;

    public TestRunnable (IDevice device, String apkDir, String apkFile, String obbFile, String apkPack, String apkActivity, String fileName)
    {
        this.device = device;
        this.apkDir = apkDir;
        this.apkFile = apkFile;
        this.obbFile = obbFile;
        this.apkActivity = apkActivity;
        this.apkPack = apkPack;
        this.fileName = fileName;
    }
    @Override
    public void run() {

        try {

            String uninstallPackageStatus = device.uninstallPackage(apkPack);
            System.out.println("Uninstall is complete");

            System.out.println("Install: "+ apkDir+apkFile + " on "+device.getSerialNumber());
            String devicelocation =  device.syncPackageToDevice(apkDir+apkFile);
            String installPackageStatus = device.installRemotePackage(devicelocation,true);
            device.removeRemotePackage(devicelocation);
            System.out.println("Install is complete on "+device.getSerialNumber());

            if (!obbFile.equals(""))
            {
                ShellOutputReceiver shellOutputReceiver= new ShellOutputReceiver();
                device.executeShellCommand("echo $EXTERNAL_STORAGE", shellOutputReceiver);
                String externalStoragePath = shellOutputReceiver.toString().replaceAll("\\r\\n.*", "");
                device.executeShellCommand("mkdir -p $EXTERNAL_STORAGE/Android/data/"+apkPack+"/", shellOutputReceiver);

                String obbFileDest = "main.obb";
                try {
                    device.pushFile(apkDir+obbFile, externalStoragePath+"/Android/obb/"+apkPack+"/"+obbFileDest);
                } catch (SyncException e) {
                    System.out.println(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
                }
            }


                iShellOutputReceiver= new ShellOutputReceiver();
                String appStartString = "am start -n "+apkPack+"/"+apkActivity+" -e test all";
                device.executeShellCommand(appStartString, iShellOutputReceiver);

                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Boolean activityisRunning = isActivityRunning(apkPack,device);
                long startTime = System.currentTimeMillis();
                while(activityisRunning)
                {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                    System.out.println("activity is still running on "+device.getSerialNumber());
                    activityisRunning = isActivityRunning(apkPack,device);

                    int activityMemory = getMemoryUsage(device);
                    if (activityMemory > memoryUsage)
                        memoryUsage = activityMemory;
                    System.out.println("memory: "+activityMemory +"Kb on "+device.getSerialNumber());
                    long endTime = System.currentTimeMillis();
                    if (endTime > startTime + 3600000) {
                        System.out.println("Exit on timeout on "+device.getSerialNumber());
                        break;
                    }
                }
                System.out.println("activity finished on " + device.getSerialNumber());

            } catch (ShellCommandUnresponsiveException e) {
                e.printStackTrace();
            } catch (AdbCommandRejectedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            } catch (InstallException e) {
                e.printStackTrace();
            } catch (SyncException e) {
            e.printStackTrace();
        }


            /*System.out.println("Screenshot: image"+device.getSerialNumber()+".png");
            RawImage rawImage = device.getScreenshot();
            // convert raw data to an Image
            BufferedImage image = new BufferedImage(rawImage.width, rawImage.height,
                    BufferedImage.TYPE_INT_ARGB);

            int index = 0;
            int IndexInc = rawImage.bpp >> 3;
            for (int y = 0 ; y < rawImage.height ; y++) {
                for (int x = 0 ; x < rawImage.width ; x++) {
                    int value = rawImage.getARGB(index);
                    index += IndexInc;
                    image.setRGB(x, y, value);
                }
            }

            if (!ImageIO.write(image, "png", new File(apkDir + device.getSerialNumber() + ".png"))) {
                throw new IOException("Failed to find png writer");
            } */


    }

    public int getTotalMemoryUsage()
    {
        return memoryUsage;
    }

    private Boolean isActivityRunning(String activityName, IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        ActivityRunningReceiver activityRunningReceiver= new ActivityRunningReceiver(activityName);
        device.executeShellCommand("dumpsys activity package "+apkPack, activityRunningReceiver);
        boolean isRunning = activityRunningReceiver.isRunning();
        if (isRunning)
        {
            isRunning = !Grep.matchPattern(fileName, "UI TESTS");
        }
        if (isRunning)
        {
            isRunning = !Grep.matchPattern(fileName, "ASensorManager_destroyEventQueue");
        }
        return isRunning;
    }

    private int getMemoryUsage(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        ActivityMemoryReceiver activityMemoryReceiver= new ActivityMemoryReceiver();
        device.executeShellCommand("dumpsys meminfo "+apkPack, activityMemoryReceiver);
        return activityMemoryReceiver.getMemoryUsage();
    }
}
