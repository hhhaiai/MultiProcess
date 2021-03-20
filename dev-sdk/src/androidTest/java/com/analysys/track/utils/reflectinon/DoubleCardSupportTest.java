package com.analysys.track.utils.reflectinon;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.ELOG;

import org.junit.Assert;
import org.junit.Test;

public class DoubleCardSupportTest extends AnalsysTest {

    private Context context;

    @Test
    public void init() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
    @Test
    public void getIMEIS() {
        long begin = System.currentTimeMillis();
        String imeis = DoubleCardSupport.getInstance().getIMEIS(context);
        long end = System.currentTimeMillis();
        ELOG.i("imei获取耗时:" + (end - begin));
        Assert.assertNotNull("imei获取", imeis);
    }

    @Test(timeout = 500)
    public void getIMSIS() {
        long begin = System.currentTimeMillis();
        String imsis = DoubleCardSupport.getInstance().getIMSIS(context);
        long end = System.currentTimeMillis();
        ELOG.i("imsi获取耗时:" + (end - begin));
        Assert.assertNotNull("imsi获取", imsis);
    }

    @Test
    public void getInstance() {
        Assert.assertNotNull("单例获取非空", DoubleCardSupport.getInstance());
        Assert.assertTrue("对比单例获取结果", DoubleCardSupport.getInstance() == DoubleCardSupport.getInstance());
    }
}