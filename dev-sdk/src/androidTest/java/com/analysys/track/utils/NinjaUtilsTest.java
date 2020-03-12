//package com.analysys.track.utils;
//
//import com.analysys.track.AnalsysTest;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//public class NinjaUtilsTest extends AnalsysTest {
//
//    @Test(timeout = 500 * 10000000)
//    public void newInstall() throws InterruptedException {
//        Assert.assertEquals(NinjaUtils.newInstall(mContext, Integer.MAX_VALUE), true);
//        Thread.sleep(200);
//        Assert.assertEquals(NinjaUtils.newInstall(mContext, 100), false);
//    }
//
//    @Test(timeout = 500)
//    public void bootTimeMore() {
//        Assert.assertEquals(NinjaUtils.bootTimeMore(1000), true);
//    }
//
//    @Test(timeout = 500)
//    public void isLowDev() {
//        Assert.assertEquals(NinjaUtils.isLowDev(mContext), NinjaUtils.isLowDev(mContext));
//    }
//}