package com.analysys.track.utils;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ShellUtilsTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void shell() {
    }

    @Test
    public void exec() {
    }

//    @Test
//    public void execCommand() {
//        String s = ShellUtils.execCommand(new String[]{"which su"});
//        Log.i("sanbo", "which su:" + s);
//        Assert.assertNotNull(s);
//
//        s = ShellUtils.execCommand(new String[]{"type su"});
//        Log.i("sanbo", "type su:" + s);
//        Assert.assertNotNull(s);
//
//
//        int pid = android.os.Process.myPid();
//
//        s = ShellUtils.execCommand(new String[]{"cat /proc/" + pid + "/oom_score"});
//        Log.i("sanbo", "cat oom_score:" + s);
//        Assert.assertNotNull(s);
//
//
//        s = ShellUtils.execCommand(new String[]{"getprop"});
//        Log.i("sanbo", "getprop:" + s);
//        Assert.assertNotNull(s);
//    }
}