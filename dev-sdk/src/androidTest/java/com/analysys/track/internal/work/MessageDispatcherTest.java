package com.analysys.track.internal.work;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

public class MessageDispatcherTest {
    Context mContext = InstrumentationRegistry.getContext();

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