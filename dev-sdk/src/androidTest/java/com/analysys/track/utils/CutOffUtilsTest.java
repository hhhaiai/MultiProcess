package com.analysys.track.utils;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

import static com.analysys.track.utils.CutOffUtils.FLAG_BACKSTAGE;
import static com.analysys.track.utils.CutOffUtils.FLAG_BOOT_TIME;
import static com.analysys.track.utils.CutOffUtils.FLAG_DEBUG;
import static com.analysys.track.utils.CutOffUtils.FLAG_LOW_DEV;
import static com.analysys.track.utils.CutOffUtils.FLAG_NEW_INSTALL;
import static com.analysys.track.utils.CutOffUtils.FLAG_PASSIVE_INIT;
import static com.analysys.track.utils.CutOffUtils.FLAG_SCORE_10;
import static com.analysys.track.utils.CutOffUtils.FLAG_SCORE_6;

public class CutOffUtilsTest extends AnalsysTest {

    @Test
    public void cutOff() {
        CutOffUtils.getInstance().cutOff(mContext, "hello", "1111 1111");
        CutOffUtils.getInstance().cutOff(mContext, "hello", "1111 tret");
        CutOffUtils.getInstance().cutOff(mContext, "hello", "wrweqr 1111");
        CutOffUtils.getInstance().cutOff(mContext, "hello", "00000000");
        CutOffUtils.getInstance().cutOff(mContext, "hello", "00000 1111");
        CutOffUtils.getInstance().cutOff(mContext, "hello", "111100 111000001");
        CutOffUtils.getInstance().cutOff(mContext, "hello", "1111 0");
        CutOffUtils.getInstance().cutOff(mContext, "hello", "000");
    }

    @Test
    public void cutOff1() {
        boolean FLAG_BACKSTAGEa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_BACKSTAGE);
        boolean FLAG_BOOT_TIMEa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_BOOT_TIME);
        boolean FLAG_DEBUGa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_DEBUG);
        boolean FLAG_LOW_DEVa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_LOW_DEV);
        boolean FLAG_NEW_INSTALLa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_NEW_INSTALL);
        boolean FLAG_PASSIVE_INITa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_PASSIVE_INIT);
        boolean FLAG_SCORE_6a = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_SCORE_6);
        boolean FLAG_SCORE_10a = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_SCORE_10);
        Assert.assertTrue(FLAG_DEBUGa);
    }
}