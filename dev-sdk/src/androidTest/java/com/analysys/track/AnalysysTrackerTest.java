package com.analysys.track;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.SystemUtils;

import org.junit.Assert;
import org.junit.Test;

public class AnalysysTrackerTest {
    Context mContext = InstrumentationRegistry.getContext();

    @Test
    public void testInit() {
        String appkey = "testappkey";
        String channel = "custonchannel";
        AnalysysTracker.init(mContext, appkey, channel);

        try {
            //key在子线程存储进去的，这里等一下
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(SystemUtils.getAppKey(mContext), appkey);
        Assert.assertEquals(SystemUtils.getAppChannel(mContext), channel);
    }

    @Test
    public void setDebugMode() {
        AnalysysTracker.setDebugMode(true);
        Assert.assertTrue(EGContext.FLAG_DEBUG_USER);
        AnalysysTracker.setDebugMode(false);
        Assert.assertFalse(EGContext.FLAG_DEBUG_USER);
    }
}