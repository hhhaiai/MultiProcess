package com.analysys.track.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

public class DataTmpUtilsTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    String path = "/data/local/tmp/kvs";

    @Test
    public void getString() {
        DataLocalTempUtils.getInstance(mContext).putString("s1", "nihao");
        DataLocalTempUtils.getInstance(mContext).putString("s2", "123");
        DataLocalTempUtils.getInstance(mContext).putString("s3", "false");
        DataLocalTempUtils.getInstance(mContext).putString("", "\n");

        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getString("s1", ""), "nihao");
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getString("s2", ""), "123");
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getString("s3", ""), "false");
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getString("", ""), "\n");

        DataLocalTempUtils.getInstance(mContext).putString("s1", "q");
        DataLocalTempUtils.getInstance(mContext).putString("s2", "w");
        DataLocalTempUtils.getInstance(mContext).putString("s3", "e");

        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getString("s1", ""), "q");
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getString("s2", ""), "w");
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getString("s3", ""), "e");
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getString("s4", "s4"), "s4");
    }

    @Test
    public void getInt() {
        DataLocalTempUtils.getInstance(mContext).putInt("i1", 1);
        DataLocalTempUtils.getInstance(mContext).putInt("i2", 2);
        DataLocalTempUtils.getInstance(mContext).putInt("i3", 3);
        DataLocalTempUtils.getInstance(mContext).putInt("", 12);

        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("i1", -1), 1);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("i2", -1), 2);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("i3", -1), 3);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("", -1), 12);

        DataLocalTempUtils.getInstance(mContext).putInt("i1", 11);
        DataLocalTempUtils.getInstance(mContext).putInt("i2", 22);
        DataLocalTempUtils.getInstance(mContext).putInt("i3", 33);

        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("i1",-1), 11);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("i2",-1), 22);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("i3",-1), 33);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("i4",-1), -1);


    }

    @Test
    public void getLong() {
        DataLocalTempUtils.getInstance(mContext).putLong("l1", Long.MAX_VALUE);
        DataLocalTempUtils.getInstance(mContext).putLong("l2", Long.MIN_VALUE);
        DataLocalTempUtils.getInstance(mContext).putLong("l3", 3L);
        DataLocalTempUtils.getInstance(mContext).putLong("", 12L);

        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getLong("l1", -1), Long.MAX_VALUE);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getLong("l2", -1), Long.MIN_VALUE);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getLong("l3", -1), 3);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getLong("", -1), 12);

        DataLocalTempUtils.getInstance(mContext).putInt("l1", 11);
        DataLocalTempUtils.getInstance(mContext).putInt("l2", 22);
        DataLocalTempUtils.getInstance(mContext).putInt("l3", 33);

        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("l1",-1), 11);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("l2",-1), 22);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("l3",-1), 33);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getInt("l4",-1), -1);
    }

    @Test
    public void getBoolean() {
        DataLocalTempUtils.getInstance(mContext).putBoolean("booleannn", true);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getBoolean("booleannn",false), true);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).getBoolean("ddd",false), false);
    }

    @Test
    public void contains() {

        DataLocalTempUtils.getInstance(mContext).putBoolean("cccc", true);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).contains("cccc"), true);
        DataLocalTempUtils.getInstance(mContext).remove("cccc");
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).contains("cccc"), false);
        Assert.assertEquals(DataLocalTempUtils.getInstance(mContext).contains("ccc1"), false);
    }
}