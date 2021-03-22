package com.analysys.track.internal.impl.ftime;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.ELOG;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class LmFileUitlsTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();


    @Test
    public void testGetLastAliveTimeInBaseDir() {
        List<LmFileUitls.AppTime> list = LmFileUitls.getLastAliveTimeInSD(mContext, false);
        ELOG.i("==========testGetLastAliveTimeInBaseDir: %d================", list.size());
        ELOG.i(list.toString());
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void testGetLastAliveTimeInSD() {
        List<LmFileUitls.AppTime> list = LmFileUitls.getLastAliveTimeInSD(mContext, true);
        ELOG.i("=========testGetLastAliveTimeInSD: %d================", list.size());
        ELOG.i(list.toString());
        Assert.assertTrue(list.size() > 0);
    }
}