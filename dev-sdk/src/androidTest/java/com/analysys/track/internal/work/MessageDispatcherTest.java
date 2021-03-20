package com.analysys.track.internal.work;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

public class MessageDispatcherTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void initModule() {
        MessageDispatcher.getInstance(mContext).initModule();
    }

    @Test
    public void getInstance() {
        MessageDispatcher a = MessageDispatcher.getInstance(mContext);
        MessageDispatcher b = MessageDispatcher.getInstance(mContext);
        Assert.assertEquals(a, b);

    }
}