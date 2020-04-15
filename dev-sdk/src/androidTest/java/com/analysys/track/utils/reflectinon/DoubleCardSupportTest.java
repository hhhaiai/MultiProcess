package com.analysys.track.utils.reflectinon;

import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class DoubleCardSupportTest {

    @Test
    public void getIMEIS() {
        long begin = System.currentTimeMillis();
        String imeis = DoubleCardSupport.getInstance().getIMEIS(EContextHelper.getContext());
        long end = System.currentTimeMillis();
        ELOG.i("imei获取耗时:" + (end - begin));
        Assert.assertNotNull(imeis);
    }

    @Test
    public void getIMSIS() {

    }

    @Test
    public void getInstance() {
    }
}