package com.creativemobile.automation.shell;

import com.android.ddmlib.IShellOutputReceiver;

public class ActivityMemoryReceiver implements IShellOutputReceiver {
    private byte[] bytes;

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

    public int getMemoryUsage()
    {
        String s = new String(bytes);
        String[] array = s.split("\n");
        for (int i=0; i< array.length; i++)
        {
            if (array[i].contains("TOTAL"))
            {
                String[] totalMemoryAr = array[i].split("\\s");
                String totalMemory = "0";
                for (int in=0; in<totalMemoryAr.length; in++)
                {
                    if (totalMemoryAr[in].matches("[0-9]+"))
                    {
                        totalMemory = totalMemoryAr[in];
                        break;
                    }
                }
                if (totalMemory.equals(""))
                    totalMemory = "0";
                int total = Integer.parseInt(totalMemory);
                if (total>0)
                    return total;
            }
        }
        return 0;
    }
}
