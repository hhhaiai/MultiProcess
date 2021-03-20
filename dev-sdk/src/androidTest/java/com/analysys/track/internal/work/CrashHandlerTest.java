package com.analysys.track.internal.work;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

public class CrashHandlerTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void getInstance() {
        CrashHandler internal = CrashHandler.getInstance();
        CrashHandler internalA = CrashHandler.getInstance();

        Assert.assertEquals(internal, internalA);
    }

    @Test
    public void setCallback() {
        CrashHandler.getInstance().setCallback(new CrashHandler.CrashCallBack() {
            @Override
            public void onAppCrash(Throwable e) {
            }
        });

    }

    @Test
    public void setEnableCatch() {
        CrashHandler.getInstance().setEnableCatch(false);
        CrashHandler.getInstance().setEnableCatch(true);
    }

    @Test
    public void uncaughtException() {
    }
}