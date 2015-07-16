package com.creativemobile.automation.tasks;

import com.android.ddmlib.IDevice;

import java.util.Map;

public class TestItem {
    public Thread threadLog;
    public Thread threadTest;
    public LogRunnable logRunnable;
    public TestRunnable testRunnable;
    public IDevice device;
    public Boolean isFinished = false;
    public Map<String, String> properties;
}
