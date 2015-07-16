package com.creativemobile.automation;

import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class N2OLogCatListener implements LogCatListener {
    private List<LogCatMessage> logCatMessages;
    private String logcat = "";
    private Boolean passed = false;
    private String logFileName;
    private String logFilter;

    public N2OLogCatListener(String logFileName, String logFilter)
    {
        this.logFileName = logFileName;
        this.logFilter = logFilter;
    }

    @Override
    public void log(List<LogCatMessage> logCatMessages) {
          this.logCatMessages = logCatMessages;
        for (LogCatMessage msg : logCatMessages) {
            /* Possible data
            System.out.print(msg.getTime());
            System.out.print(msg.getPid());
            System.out.print(msg.getLogLevel());
            System.out.print(msg.getAppName());
            System.out.print(msg.getTag());
            System.out.print(msg.getTid());
            System.out.println(msg.getMessage());*/

            if (msg.getTag().equalsIgnoreCase(logFilter))
            {
                String message = msg.getTime()+ " " +msg.getTag()+ " " +msg.getLogLevel() + " " +msg.getMessage()+ " [" +msg.getTid()+ " " +msg.getPid()+"]";

                try
                {
                    FileWriter fw = new FileWriter(logFileName,true); //the true will append the new data
                    fw.write(message+"\n");//appends the string to the file
                    fw.close();
                }
                catch(IOException ioe)
                {
                    System.err.println("IOException: " + ioe.getMessage());
                }
                System.out.println(message);
            }
            if (msg.getMessage().contains("UI TESTS PASSED"))
                passed = true;
        }
    }

    public List<LogCatMessage> getLogCatMessages ()
    {
        return logCatMessages;
    }

    public String getMessages(){
          return logcat;
    }

    public Boolean isPassed()
    {
        return passed;
    }
}
