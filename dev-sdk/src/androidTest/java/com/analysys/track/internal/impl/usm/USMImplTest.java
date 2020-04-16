package com.analysys.track.internal.impl.usm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.analysys.track.internal.content.EGContext;

import org.junit.Assert;
import org.junit.Test;

public class USMImplTest {
    Context context;

    @Test
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
    }


    public void testGetUSMInfo() {
    }

    @Test
    public void testTestGetUSMInfo() {
        USMImpl.getUSMInfo(context);
    }

    public void testParserUsageStatsList() {
    }

    public void testGetPackageName() {
    }

    public void testGetTimeStamp() {
    }

    public void testGetEventType() {
    }

    @Test
    public void checkTime() {

        try {
            case1();
            case2();
            case3();
            case4();
        } catch (Throwable e) {
            Log.e("sanbo", Log.getStackTraceString(e));
        }
    }

    private void case3() {
        long start = 60 * 60 * 60 * 1000;
        long end = 90 * 60 * 60 * 1000;

        USMImpl.SyncTime s = new USMImpl.SyncTime(start, end).invoke();
        start = s.getStart();
        end = s.getEnd();

        Log.i("sanbo", "case3 start:" + start);
        Log.i("sanbo", "case3 end:" + end);
        Assert.assertTrue("case3", (end - start == 20 * EGContext.TIME_HOUR));
    }

    private void case4() {
        long start = 90 * 60 * 60 * 1000;
        long end = 60 * 60 * 60 * 1000;

        USMImpl.SyncTime s = new USMImpl.SyncTime(start, end).invoke();
        start = s.getStart();
        end = s.getEnd();

        Log.i("sanbo", "case4 start:" + start);
        Log.i("sanbo", "case4 end:" + end);
        Assert.assertTrue("case4", (end - start == 20 * EGContext.TIME_HOUR));
    }

    private void case2() {
        long start = System.currentTimeMillis();
        long end = -1;

        USMImpl.SyncTime s = new USMImpl.SyncTime(start, end).invoke();
        start = s.getStart();
        end = s.getEnd();

        Log.i("sanbo", "case2 start:" + start);
        Log.i("sanbo", "case2 end:" + end);
        Assert.assertTrue("case2", (end - start == 20 * EGContext.TIME_HOUR));
    }

    private void case1() {
        long start = -1;
        long end = System.currentTimeMillis();
        USMImpl.SyncTime s = new USMImpl.SyncTime(start, end).invoke();
        start = s.getStart();
        end = s.getEnd();
        Log.i("sanbo", "case1 start:" + start);
        Log.i("sanbo", "case1 end:" + end);
        Assert.assertTrue("case1", (end - start == 20 * EGContext.TIME_HOUR));
    }

}