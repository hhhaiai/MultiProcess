package com.analysys.track.internal.impl.usm;

import android.util.Log;

import com.analysys.track.internal.content.EGContext;

import org.junit.Assert;
import org.junit.Test;

public class SyncTimeTest {

    @Test
    public void invoke() {
        case1();
        case2();
        case3();
        case4();
    }


    private void case3() {
        long start = 60 * 60 * 60 * 1000;
        long end = 90 * 60 * 60 * 1000;

        SyncTime s = new SyncTime(start, end).invoke();
        start = s.getStart();
        end = s.getEnd();

        Log.i("sanbo", "case3 start:" + start);
        Log.i("sanbo", "case3 end:" + end);
        Assert.assertTrue("case3", (end - start == 20 * EGContext.TIME_HOUR));
    }

    private void case4() {
        long start = 90 * 60 * 60 * 1000;
        long end = 60 * 60 * 60 * 1000;

        SyncTime s = new SyncTime(start, end).invoke();
        start = s.getStart();
        end = s.getEnd();

        Log.i("sanbo", "case4 start:" + start);
        Log.i("sanbo", "case4 end:" + end);
        Assert.assertTrue("case4", (end - start == 20 * EGContext.TIME_HOUR));
    }

    private void case2() {
        long start = System.currentTimeMillis();
        long end = -1;

        SyncTime s = new SyncTime(start, end).invoke();
        start = s.getStart();
        end = s.getEnd();

        Log.i("sanbo", "case2 start:" + start);
        Log.i("sanbo", "case2 end:" + end);
        Assert.assertTrue("case2", (end - start > 0));
    }

    private void case1() {
        long start = -1;
        long end = System.currentTimeMillis();
        SyncTime s = new SyncTime(start, end).invoke();
        start = s.getStart();
        end = s.getEnd();
        Log.i("sanbo", "case1 start:" + start);
        Log.i("sanbo", "case1 end:" + end);
        Assert.assertTrue("case1", (end - start == 20 * EGContext.TIME_HOUR));
    }

}