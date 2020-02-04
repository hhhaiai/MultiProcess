package com.analysys.track.utils;

import org.junit.Assert;
import org.junit.Test;

public class DataTmpUtilsTest {

    String path = "/data/local/tmp/kvs";

    @Test
    public void getString() {
        DataTmpUtils.getInstance(path).putString("s1", "nihao");
        DataTmpUtils.getInstance(path).putString("s2", "123");
        DataTmpUtils.getInstance(path).putString("s3", "false");
        DataTmpUtils.getInstance(path).putString(null, null);
        DataTmpUtils.getInstance(path).putString("", "\n");

        Assert.assertEquals(DataTmpUtils.getInstance(path).getString("s1", ""), "nihao");
        Assert.assertEquals(DataTmpUtils.getInstance(path).getString("s2", ""), "123");
        Assert.assertEquals(DataTmpUtils.getInstance(path).getString("s3", ""), "false");
        Assert.assertEquals(DataTmpUtils.getInstance(path).getString(null, ""), null);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getString("", ""), "\n");

        DataTmpUtils.getInstance(path).putString("s1", "q");
        DataTmpUtils.getInstance(path).putString("s2", "w");
        DataTmpUtils.getInstance(path).putString("s3", "e");

        Assert.assertEquals(DataTmpUtils.getInstance(path).getString("s1", ""), "q");
        Assert.assertEquals(DataTmpUtils.getInstance(path).getString("s2", ""), "w");
        Assert.assertEquals(DataTmpUtils.getInstance(path).getString("s3", ""), "e");
        Assert.assertEquals(DataTmpUtils.getInstance(path).getString("s4", "s4"), "s4");
    }

    @Test
    public void getInt() {
        DataTmpUtils.getInstance(path).putInt("i1", 1);
        DataTmpUtils.getInstance(path).putInt("i2", 2);
        DataTmpUtils.getInstance(path).putInt("i3", 3);
        DataTmpUtils.getInstance(path).putInt(null, 4);
        DataTmpUtils.getInstance(path).putInt("", 12);

        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("i1", -1), 1);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("i2", -1), 2);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("i3", -1), 3);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt(null, -1), 4);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("", -1), 12);

        DataTmpUtils.getInstance(path).putInt("i1", 11);
        DataTmpUtils.getInstance(path).putInt("i2", 22);
        DataTmpUtils.getInstance(path).putInt("i3", 33);

        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("i1",-1), 11);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("i2",-1), 22);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("i3",-1), 33);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("i4",-1), -1);


    }

    @Test
    public void getLong() {
        DataTmpUtils.getInstance(path).putLong("l1", Long.MAX_VALUE);
        DataTmpUtils.getInstance(path).putLong("l2", Long.MIN_VALUE);
        DataTmpUtils.getInstance(path).putLong("l3", 3L);
        DataTmpUtils.getInstance(path).putLong(null, 4L);
        DataTmpUtils.getInstance(path).putLong("", 12L);

        Assert.assertEquals(DataTmpUtils.getInstance(path).getLong("l1", -1), Long.MAX_VALUE);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getLong("l2", -1), Long.MIN_VALUE);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getLong("l3", -1), 3);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getLong(null, -1), 4);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getLong("", -1), 12);

        DataTmpUtils.getInstance(path).putInt("l1", 11);
        DataTmpUtils.getInstance(path).putInt("l2", 22);
        DataTmpUtils.getInstance(path).putInt("l3", 33);

        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("l1",-1), 11);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("l2",-1), 22);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("l3",-1), 33);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getInt("l4",-1), -1);
    }

    @Test
    public void getBoolean() {
        DataTmpUtils.getInstance(path).putBoolean("booleannn", true);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getBoolean("booleannn",false), true);
        Assert.assertEquals(DataTmpUtils.getInstance(path).getBoolean("ddd",false), false);
    }

    @Test
    public void contains() {

        DataTmpUtils.getInstance(path).putBoolean("cccc", true);
        Assert.assertEquals(DataTmpUtils.getInstance(path).contains("cccc"), true);
        DataTmpUtils.getInstance(path).remove("cccc");
        Assert.assertEquals(DataTmpUtils.getInstance(path).contains("cccc"), false);
        Assert.assertEquals(DataTmpUtils.getInstance(path).contains("ccc1"), false);
    }
}