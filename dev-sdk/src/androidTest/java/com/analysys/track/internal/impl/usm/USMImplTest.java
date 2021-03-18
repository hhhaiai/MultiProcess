package com.analysys.track.internal.impl.usm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class USMImplTest {
    Context context;

    @Test
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testGetUSMInfo() {
        long end = System.currentTimeMillis();
        long mid = end - TimeUnit.HOURS.toMillis(3);
        long start = end - TimeUnit.HOURS.toMillis(6);
        JSONArray jsonArray1 = USMImpl.getUSMInfo(context, start, end);

        JSONArray jsonArray2 = USMImpl.getUSMInfo(context, start, mid);
        JSONArray jsonArray3 = USMImpl.getUSMInfo(context, mid, end);

        if (jsonArray1 == null || jsonArray2 == null || jsonArray3 == null) {
            return;
        }
        Assert.assertTrue(jsonArray1.length() <= (jsonArray2.length() + jsonArray3.length()));
    }

    @Test
    public void testTestGetUSMInfo() {
        long start = System.currentTimeMillis();
        JSONArray usm = USMImpl.getUSMInfo(context);
        long end = System.currentTimeMillis();
        ELOG.i("耗时：" + (end - start) + "----[" + usm.length() + "]-----" + usm.toString());
    }

    public void testParserUsageStatsList() {
    }

    public void testGetPackageName() {
    }

    public void testGetTimeStamp() {
    }

    public void testGetEventType() {

    }

    public static String stampToDate(long lt) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(lt);
        return simpleDateFormat.format(date);
    }

    @Test
    public void moreRun() {
        long now = System.currentTimeMillis();
        long lsa = now - 20 * EGContext.TIME_HOUR;
        long dur = 3 * EGContext.TIME_HOUR;
        Log.e("sanbo", "--------------------开始-----------------");
        while (true) {
            if (lsa + dur >= now) {
                Log.i("sanbo", String.format("尾声了。。起时间:[%s], 止时间[%s]", stampToDate(lsa), stampToDate(now)));
                break;
            } else {
                Log.i("sanbo", String.format("中间。。起时间:[%s], 止时间[%s]", stampToDate(lsa), stampToDate(lsa + dur)));
                lsa = lsa + dur;
            }
        }
        Log.e("sanbo", "--------------------结束-----------------");

    }

    @Test
    public void compareOneAndMore() {
        for (int i = 0; i < 100; i++) {
            Log.i("sanbo", "--------------------" + i + "/" + 100 + "-----------------");
            realWork();
        }

    }

    private void realWork() {
        long s1 = System.currentTimeMillis();
        long lastRequestTime = s1 - 20 * EGContext.TIME_HOUR;
        USMImpl.getUSMInfo(context, lastRequestTime, s1);
        long e1 = System.currentTimeMillis();
        Log.e("sanbo", "单次获取20小时耗时:" + (e1 - s1));


        long tstart = System.currentTimeMillis();

        long now = s1;
        long lsa = lastRequestTime;
        long dur = 3 * EGContext.TIME_HOUR;
//        Log.e("sanbo", "--------------------开始-----------------");
        while (true) {
            if (lsa + dur >= now) {
//                Log.i("sanbo", String.format("尾声了。。起时间:[%s], 止时间[%s]", stampToDate(lsa), stampToDate(now)));
                USMImpl.getUSMInfo(context, lsa, now);
                break;
            } else {
//                Log.i("sanbo", String.format("中间。。起时间:[%s], 止时间[%s]", stampToDate(lsa), stampToDate(lsa + dur)));
                USMImpl.getUSMInfo(context, lsa, lsa + dur);
                lsa = lsa + dur;
            }
        }
//        Log.e("sanbo", "--------------------结束-----------------");


        long tsend = System.currentTimeMillis();
        Log.e("sanbo", "多次获取20小时耗时:" + (tsend - tstart));
        Assert.assertTrue("获取USM耗时异常：", tsend - tstart <= 10000);
    }


    @Test
    public void testZeroDurRequest() {
        try {
            //has a bug. app get last request time failed.
            long lastReqTime = SPHelper.getLongValueFromSP(context, EGContext.LASTQUESTTIME, 0);
            if (lastReqTime == 0) {
                Log.i("sanbo", "未成功发送，没有上次发送时间");
            } else {
                Log.i("sanbo", "上次发送时间: " + stampToDate(lastReqTime));
            }
            long defTime = (long) ClazzUtils.getStaticFieldValue(BuildConfig.class, "TIME_USM_SPLIT");
            Log.i("sanbo", "采集的间隔:" + defTime);
            Log.i("sanbo", "-----------模拟首次请求------------");
            if (lastReqTime != 0) {
                SPHelper.setLongValue2SP(context, EGContext.LASTQUESTTIME, 0);
            }
            Log.i("sanbo", "----case1: 时间间隔 0---------------");
            try {
                if (defTime != 0) {
                    ClazzUtils.setStaticFieldValue(BuildConfig.class, "TIME_USM_SPLIT", 0);
                }
            } catch (Exception e) {
                Log.e("sanbo", Log.getStackTraceString(e));
            }
            long a = System.currentTimeMillis();
            JSONArray info = USMImpl.getUSMInfo(context);
            long b = System.currentTimeMillis();
            Log.i("sanbo", "首次请求结果: " + info);
            Log.w("sanbo", "时间间隔 0,耗时: " + (b - a));
            if (lastReqTime != 0) {
                SPHelper.setLongValue2SP(context, EGContext.LASTQUESTTIME, lastReqTime);
            }
            try {
                if (defTime != 0) {
                    ClazzUtils.setStaticFieldValue(BuildConfig.class, "TIME_USM_SPLIT", defTime);
                }
            } catch (Exception e) {
                Log.e("sanbo", Log.getStackTraceString(e));
            }
        } catch (Throwable e) {
            Log.e("sanbo", Log.getStackTraceString(e));
        }
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
        Assert.assertTrue("case2", (end - start == 20 * EGContext.TIME_HOUR));
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