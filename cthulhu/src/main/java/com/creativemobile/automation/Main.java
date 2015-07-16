package com.creativemobile.automation;

import com.Ostermiller.util.CmdLn;
import com.Ostermiller.util.CmdLnOption;
import com.Ostermiller.util.CmdLnResult;
import com.android.ddmlib.*;
import com.creativemobile.automation.helpers.LogFormatter;
import com.creativemobile.automation.shell.ShellOutputReceiver;
import com.creativemobile.automation.tasks.LogRunnable;
import com.creativemobile.automation.tasks.TestItem;
import com.creativemobile.automation.tasks.TestRunnable;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static IShellOutputReceiver  iShellOutputReceiver;
    private static String apkFile = "";
    private static String apkPack = "";
    private static String apkActivity = "";
    private static String obbFile = "";
    private static String apkDir = "C:\\projects\\";
    private static String apkExe = "adb";
    private static String logFilter = "unity";
    private static String logcat;

    private enum EnumOptions {
        HELP(new CmdLnOption("help",'h')),
        APKFILE(new CmdLnOption("apkfile",'a').setRequiredArgument().setDescription("apk file to use")),
        APKFILTER(new CmdLnOption("apkfilter",'f').setRequiredArgument().setDescription("log filter to use")),
        APKPACK(new CmdLnOption("apkPack",'p').setRequiredArgument().setDescription("apk packages to use")),
        APKACTIVITY(new CmdLnOption("apkActivity",'c').setRequiredArgument().setDescription("apk activity to use")),
        OBBFILE(new CmdLnOption("obbfile",'o').setOptionalArgument().setDescription("obb file to use")),
        APKDIR(new CmdLnOption("apkdir",'d').setOptionalArgument().setDescription("directory where to download files")),
        APKEXE(new CmdLnOption("apkexe",'x').setOptionalArgument().setDescription("adb file location"));
        private CmdLnOption option;
        private EnumOptions(CmdLnOption option){
            option.setUserObject(this);
            this.option = option;
        }
        private CmdLnOption getCmdLineOption(){
            return option;
        }
    }

    public static void main(String[] args) throws IOException {
        CmdLn cmdLn = new CmdLn(args).setDescription("app for automated installation of apk and obb files");
        for (EnumOptions option: EnumOptions.values()){
            cmdLn.addOption(option.getCmdLineOption());
        }
        int delay = 0;
        for(CmdLnResult result: cmdLn.getResults()){
            switch((EnumOptions)result.getOption().getUserObject()){
                case HELP:{
                    cmdLn.printHelp();
                    System.exit(0);
                    break;
                }
                case APKFILE:{
                    apkFile = result.getArgument();
                    break;
                }
                case APKFILTER:{
                    logFilter = result.getArgument();
                    break;
                }
                case APKPACK:{
                    apkPack = result.getArgument();
                    break;
                }
                case APKACTIVITY:{
                    apkActivity = result.getArgument();
                    break;
                }
                case OBBFILE:{
                    obbFile = result.getArgument();
                    break;
                }
                case APKDIR:{
                    apkDir = result.getArgument();
                    break;
                }
                case APKEXE:{
                    apkExe = result.getArgument();
                    break;
                }
            }
        }

        AndroidDebugBridge.initIfNeeded(false);
       AndroidDebugBridge adb = AndroidDebugBridge.createBridge(apkExe, true);
        if (adb == null) {
            System.err.println("Invalid ADB location.");
            System.exit(1);
        }


        IDevice[] iDevices = adb.getDevices();
        final List<IDevice> deviceList = new ArrayList<IDevice>();

       AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {

            @Override
            public void deviceChanged(IDevice device, int arg1) {
                // not implement
            }

            @Override
            public void deviceConnected(IDevice device) {

                System.out.println(String.format("%s connected", device.getSerialNumber()));

                deviceList.add(device);

            }

            @Override
            public void deviceDisconnected(IDevice device) {
                System.out.println(String.format("%s disconnected", device.getSerialNumber()));

            }

        });

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int deviceCount = deviceList.size();
        while (deviceCount < deviceList.size())
        {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deviceCount = deviceList.size();
        }
        System.out.println("devices: "+deviceList.size());

        List<TestItem> testItemList = new ArrayList<TestItem>();
        for (IDevice device: deviceList)
        {
            TestItem testItem = new TestItem();
            testItem.device = device;
            Map<String, String> properties = null;
            int battery = 0;
            try {
                properties = getDeviceInformation(device);
                if (device!=null)
                    battery = device.getBatteryLevel();
            } catch (TimeoutException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (AdbCommandRejectedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ShellCommandUnresponsiveException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            // check battery level is higher than 15%
            System.out.println(String.format("Battery = %s ", battery));
            if (battery > 15) {

                String fileName = apkDir + "apkinstall_" + testItem.device.getSerialNumber() + ".log";
                PrintWriter writer = new PrintWriter(fileName, "UTF-8");
                if (properties != null) {
                    Iterator it = properties.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry) it.next();
                        System.out.println(String.format("Property %s = %s", pairs.getKey(), pairs.getValue()));
                        writer.println(String.format("Property %s = %s", pairs.getKey(), pairs.getValue()));
                    }
                }
                writer.close();
                testItem.properties = properties;


                LogRunnable task = new LogRunnable(device, fileName, logFilter);
                //executor.execute(task);
                Thread worker = new Thread(task);
                // We can set the name of the thread
                worker.setName(device.getSerialNumber());
                // Start the thread, never call method run() direct
                worker.start();

                testItem.logRunnable = task;
                testItem.threadLog = worker;

                TestRunnable taskTest = new TestRunnable(device, apkDir, apkFile, obbFile, apkPack, apkActivity, fileName);
                //executor.execute(task);
                Thread workerTest = new Thread(taskTest);
                // We can set the name of the thread
                workerTest.setName(device.getSerialNumber());
                // Start the thread, never call method run() direct
                workerTest.start();
                testItem.testRunnable = taskTest;
                testItem.threadTest = workerTest;

                testItemList.add(testItem);
            }

        }
        Logger LOGGER = Logger.getLogger(Main.class
                .getName());
        FileHandler fh;
        try {
            // This block configure the LOGGER with handler and formatter
            fh = new FileHandler(apkDir+"memory_usage.log", true);

            LOGGER.addHandler(fh);
            Formatter formatter = new LogFormatter();
            fh.setFormatter(formatter);

            LOGGER.setLevel(Level.ALL);
            // the following statement is used to log any messages
            LOGGER.info("Memory usage log");

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //PrintWriter outMemory = new PrintWriter(apkDir+"memory_usage.log");
        //outMemory.println("test");

        int running = 0;
        do {
            running = 0;
            for (TestItem testItem: testItemList) {
                Thread thread = testItem.threadTest;
                if (thread.isAlive()) {
                    running++;
                }else{
                    if (testItem.threadLog.isAlive()){
                        //add information about memory usage
                        int totalMemoryUsage = testItem.testRunnable.getTotalMemoryUsage();
                        if (testItem.properties!=null)
                        {
                            Iterator it = testItem.properties.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = (Map.Entry)it.next();
                                //System.out.println(String.format("Property %s = %s", pairs.getKey(), pairs.getValue()));
                                LOGGER.info(String.format("Device %s = %s", pairs.getKey(), pairs.getValue()));
                            }
                        }
                        LOGGER.info(totalMemoryUsage+"kB "+apkFile);
                        //outMemory.println(testItem.device.getSerialNumber()+" "+totalMemoryUsage+" "+apkFile);
                        String status = "FAILED";
                        if (testItem.logRunnable.isPassed())
                            status = "PASSED";


                        //System.out.println(task.getMessages());
                        System.out.println("test are finished on "+ testItem.device.getSerialNumber());
                        testItem.logRunnable.stop();
                        //testItemList.remove(testItem);
                    }
                }
        }
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            System.out.println("We have " + running + " running threads. ");
        } while (running > 0);



        System.exit(0);
        //System.out.println("Press enter to exit.");
        //System.in.read();
    }

    private static Map<String, String> getDeviceInformation(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        Map<String, String> resultMap = new HashMap<String, String>();

        ShellOutputReceiver shellOutputReceiver= new ShellOutputReceiver("MemTotal");
        device.executeShellCommand("cat /proc/meminfo", shellOutputReceiver);
        String memory = shellOutputReceiver.toString().replaceAll( "MemTotal:[\\s]*(.*)", "$1" ).replaceAll("\\r\\n.*", "");
        resultMap.put("memory", memory);

        shellOutputReceiver= new ShellOutputReceiver("Processor");
        device.executeShellCommand("cat /proc/cpuinfo", shellOutputReceiver);
        String cpu = shellOutputReceiver.toString().replaceAll( "Processor[\\s]*:[\\s]*(.*)\\r\\n.*", "$1" ).replaceAll("\\r\\n.*", "");
        resultMap.put("cpu", cpu);

        shellOutputReceiver= new ShellOutputReceiver("product.manufacturer");
        device.executeShellCommand("cat /system/build.prop", shellOutputReceiver);
        String model = shellOutputReceiver.toString().replaceAll(".*manufacturer=(.*)", "$1");
        shellOutputReceiver= new ShellOutputReceiver("product.model");
        device.executeShellCommand("cat /system/build.prop", shellOutputReceiver);
        model += " "+shellOutputReceiver.toString().replaceAll(".*model=(.*)", "$1");
        resultMap.put("model", model);

        shellOutputReceiver= new ShellOutputReceiver("GLES:");
        device.executeShellCommand("dumpsys", shellOutputReceiver);
        String gpuInfo = shellOutputReceiver.toString();
        resultMap.put("gpuInfo",gpuInfo);

        resultMap.put("serial", device.getSerialNumber());

        return  resultMap;

    }


}
