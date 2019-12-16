package com.analysys.track.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.analysys.track.AnalysysTracker;

import java.util.concurrent.atomic.AtomicInteger;

public class ActivityCallBack implements Application.ActivityLifecycleCallbacks {

    private static volatile ActivityCallBack instance = null;


    public static ActivityCallBack getInstance() {
        if (instance == null) {
            synchronized (ActivityCallBack.class) {
                if (instance == null) {
                    instance = new ActivityCallBack();
                }
            }
        }
        return instance;
    }

    AtomicInteger num;

    private ActivityCallBack() {
        num = new AtomicInteger(0);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        AnalysysTracker.setContext(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        num.incrementAndGet();
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        num.decrementAndGet();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public boolean isBackGround() {
        return ActivityCallBack.getInstance().num.get() == 0;
    }
}
