package com.analysys.track.internal.work;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

public class ServiceHelperTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

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