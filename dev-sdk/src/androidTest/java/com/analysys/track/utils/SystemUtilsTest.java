package com.analysys.track.utils;

import android.os.Handler;
import android.os.Looper;

import org.junit.Test;


import static org.junit.Assert.*;

public class SystemUtilsTest {

    @Test
    public void getDay() {
    }

    @Test
    public void getTime() {
    }

    @Test
    public void hasPackageNameInstalled() {
    }

    @Test
    public void isApkDebugable() {
    }

    @Test
    public void getProp() {
    }

    @Test
    public void getDate() {
    }

    @Test
    public void isScreenOn() {
    }

    @Test
    public void isRooted() {
    }

    @Test
    public void isScreenLocked() {
    }

    @Test
    public void canUseUsageStatsManager() {
    }

    @Test
    public void intervalTime() {
    }

    @Test
    public void getContentFromFile() {
    }

    @Test
    public void getContentFromFile1() {
    }

    @Test
    public void updateAppkeyAndChannel() {
    }

    @Test
    public void getChannelFromApk() {
    }

    @Test
    public void getAppKey() {
    }

    @Test
    public void getAppChannel() {
    }

    @Test
    public void getCloseTime() {
    }

    @Test(timeout = 120)
    public void isMainThread() throws InterruptedException {
        assertFalse(SystemUtils.isMainThread());

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                assertTrue(SystemUtils.isMainThread());
            }
        });

        Thread.sleep(100);
    }

    @Test
    public void getCurrentProcessName() {
    }
}