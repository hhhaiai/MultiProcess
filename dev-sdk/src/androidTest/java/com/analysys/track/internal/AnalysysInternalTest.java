package com.analysys.track.internal;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.SystemUtils;

import org.junit.Assert;
import org.junit.Test;

public class AnalysysInternalTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void getInstance() {
        AnalysysInternal internal = AnalysysInternal.getInstance(mContext);
        AnalysysInternal internalA = AnalysysInternal.getInstance(null);

        Assert.assertEquals(internal, internalA);
    }

    @Test
    public void initEguan() {
        String appkey = "testappkey";
        String channel = "testchannel";
        AnalysysInternal.getInstance(mContext).initEguan(appkey, channel, true);

        Assert.assertEquals(SystemUtils.getAppKey(mContext), appkey);
        Assert.assertEquals(SystemUtils.getAppChannel(mContext), channel);
    }
}