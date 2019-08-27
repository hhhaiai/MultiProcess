package com.analysys.track.internal.work;

import org.junit.Assert;
import org.junit.Test;

public class CrashHandlerTest {

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