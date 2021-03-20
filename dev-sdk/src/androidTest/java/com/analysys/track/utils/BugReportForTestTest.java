package com.analysys.track.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

public class BugReportForTestTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void commitError() {
        int flag = 0;
        int case1 = 1 << 0;
        int case2 = 1 << 1;
        int case3 = 1 << 2;
        int case4 = 1 << 31;

        //加标记
        flag |= case1;
        flag |= case2;
        flag |= case3;
        flag |= case4;
        //验证标记
        Assert.assertTrue((flag & case1) != 0);
        Assert.assertTrue((flag & case2) != 0);
        Assert.assertTrue((flag & case3) != 0);
        Assert.assertTrue((flag & case4) != 0);
        //删除标记
        flag &= ~case1;
        flag &= ~case2;
        flag &= ~case3;
        flag &= ~case4;
        //验证标记
        Assert.assertFalse((flag & case1) != 0);
        Assert.assertFalse((flag & case2) != 0);
        Assert.assertFalse((flag & case3) != 0);
        Assert.assertFalse((flag & case4) != 0);
    }
}