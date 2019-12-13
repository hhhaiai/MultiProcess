package com.analysys.track.utils;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

import static com.analysys.track.utils.CutOffUtils.FLAG_BACKSTAGE;
import static com.analysys.track.utils.CutOffUtils.FLAG_BOOT_TIME;
import static com.analysys.track.utils.CutOffUtils.FLAG_DEBUG;
import static com.analysys.track.utils.CutOffUtils.FLAG_LOW_DEV;
import static com.analysys.track.utils.CutOffUtils.FLAG_OLD_INSTALL;
import static com.analysys.track.utils.CutOffUtils.FLAG_PASSIVE_INIT;
import static com.analysys.track.utils.CutOffUtils.FLAG_SCORE_10;
import static com.analysys.track.utils.CutOffUtils.FLAG_SCORE_6;

public class CutOffUtilsTest extends AnalsysTest {

    @Test
    public void cutOff() {

    }

    @Test
    public void cutOff1() {
        boolean FLAG_BACKSTAGEa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_BACKSTAGE);
        boolean FLAG_BOOT_TIMEa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_BOOT_TIME);
        boolean FLAG_DEBUGa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_DEBUG);
        boolean FLAG_LOW_DEVa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_LOW_DEV);
        boolean FLAG_NEW_INSTALLa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_OLD_INSTALL);
        boolean FLAG_PASSIVE_INITa = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_PASSIVE_INIT);
        boolean FLAG_SCORE_6a = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_SCORE_6);
        boolean FLAG_SCORE_10a = CutOffUtils.getInstance().cutOff(mContext, "hello", FLAG_SCORE_10);
        Assert.assertTrue(FLAG_DEBUGa);
    }
}