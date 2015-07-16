package com.creativemobile.automation.tasks;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.LogCatReceiverTask;
import com.creativemobile.automation.N2OLogCatListener;

public class LogRunnable implements Runnable {
    LogCatReceiverTask lcrt;
    N2OLogCatListener lcl;
    IDevice device;
    String logFileName;
    String logFilter;

    public LogRunnable (IDevice device, String logFileName, String logFilter)
    {
        this.device = device;
        this.logFileName = logFileName;
        this.logFilter = logFilter;
    }

    @Override
    public void run() {

        lcrt=new LogCatReceiverTask(device);

        lcl= new N2OLogCatListener(logFileName, logFilter);
        lcrt.addLogCatListener(lcl);
        lcrt.run();
    }

    public void stop(){
        lcrt.stop();
    }

    public String getMessages(){
        return lcl.getMessages();
    }

    public Boolean isPassed(){
        return lcl.isPassed();
    }
}
