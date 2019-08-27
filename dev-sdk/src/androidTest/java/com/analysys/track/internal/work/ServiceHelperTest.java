package com.analysys.track.internal.work;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

public class ServiceHelperTest {
    Context mContext = InstrumentationRegistry.getContext();

    @Test
    public void startSelfService() {
        ServiceHelper.getInstance(mContext).startSelfService();
    }

    @Test
    public void getInstance() {
        ServiceHelper a = ServiceHelper.getInstance(mContext);
        ServiceHelper b = ServiceHelper.getInstance(mContext);
        Assert.assertEquals(a, b);
    }
}