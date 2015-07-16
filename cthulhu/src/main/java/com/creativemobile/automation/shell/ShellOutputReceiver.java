package com.creativemobile.automation.shell;

import com.android.ddmlib.IShellOutputReceiver;

public class ShellOutputReceiver implements IShellOutputReceiver {
    private byte[] bytes = null;
    private int checkRunning = 0;
    private String grepPattern = "";
    private String result = "";

    public ShellOutputReceiver()
    {

    }

    public ShellOutputReceiver(String grepPattern)
    {
        this.grepPattern = grepPattern;
    }

    @Override
    public void addOutput(byte[] bytes, int i1, int i2) {
        String result = "";
        if (bytes!=null)
            result = new String(bytes);
        if (!grepPattern.equals(""))
        {
            String[] array = result.split("\r\n");
            for (int i=0; i< array.length; i++)
            {
                if (array[i].contains(grepPattern))
                {
                    this.result = array[i];
                    break;
                }
            }
        }

    }

    @Override
    public void flush() {

    }

    @Override
    public boolean isCancelled() {
        if (bytes!=null)
        {
            if (checkRunning == bytes.length)
                return true;
            checkRunning = bytes.length;
        }
        return false;
    }

    @Override
    public String toString(){

        return result;
    }

    public Boolean isRunning()
    {
        if (checkRunning == bytes.length)
            return false;
        checkRunning = bytes.length;
        return true;
    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
