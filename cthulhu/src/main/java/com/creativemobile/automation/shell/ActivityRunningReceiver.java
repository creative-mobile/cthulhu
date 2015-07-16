package com.creativemobile.automation.shell;

import com.android.ddmlib.IShellOutputReceiver;

public class ActivityRunningReceiver implements IShellOutputReceiver {
    private byte[] bytes;
    private String activityName;

    public ActivityRunningReceiver(String activityName)
    {
        this.activityName = activityName;
    }

    @Override
    public void addOutput(byte[] bytes, int i, int i2) {
        this.bytes = bytes;
    }

    @Override
    public void flush() {

    }

    @Override
    public boolean isCancelled() {

        return false;
    }

    @Override
    public String toString(){
        return new String(bytes);
    }

    public Boolean isRunning()
    {
        String s = new String(bytes);
        String[] array = s.split("\r\n");
        for (int i=0; i< array.length; i++)
        {
            String line = array[i].trim();
            if (line.contains(activityName)) {
                return true;
            }
        }
        return false;
    }
}
