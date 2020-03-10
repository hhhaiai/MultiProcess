package com.analysys.track.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.analysys.track.AnalysysTracker;

import java.util.concurrent.atomic.AtomicInteger;

public class ActivityCallBack implements Application.ActivityLifecycleCallbacks {


    private volatile AtomicInteger num;

    /********************* get instance begin **************************/
    public static ActivityCallBack getInstance() {
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final ActivityCallBack INSTANCE = new ActivityCallBack();
    }

    private ActivityCallBack() {
    }

//    /********************* get instance end **************************/
//    private static volatile ActivityCallBack instance = null;
//
//
//    /********************* get instance end **************************/
//
//    public static ActivityCallBack getInstance() {
//        if (instance == null) {
//            synchronized (ActivityCallBack.class) {
//                if (instance == null) {
//                    instance = new ActivityCallBack();
//                }
//            }
//        }
//        return instance;
//    }
//
//    private ActivityCallBack() {
//    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        EContextHelper.setContext(activity.getApplicationContext());
        AnalysysTracker.setContext(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        try {
            if (num == null) {
                num = new AtomicInteger(0);
            }
            num.incrementAndGet();
        } catch (Throwable e) {
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        EContextHelper.setContext(activity.getApplicationContext());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        EContextHelper.setContext(activity.getApplicationContext());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        try {
            if (num == null) {
                num = new AtomicInteger(0);
            }
            num.decrementAndGet();
        } catch (Throwable e) {
        }

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        EContextHelper.setContext(activity.getApplicationContext());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public boolean isBackGround() {
        //app 初始化的时候,还没来得及回调. 默认是在前台
        if (num == null) {
            return false;
        }
        return ActivityCallBack.getInstance().num.get() == 0;
    }
}
