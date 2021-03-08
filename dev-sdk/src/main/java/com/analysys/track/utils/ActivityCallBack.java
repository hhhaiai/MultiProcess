package com.analysys.track.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.analysys.track.utils.reflectinon.EContextHelper;

public class ActivityCallBack implements Application.ActivityLifecycleCallbacks {
    
    
    private volatile boolean isFront = false;
    private Object lock = new Object();
    
    
    public static ActivityCallBack getInstance() {
        return HLODER.INSTANCE;
    }

//    //初始化时，直接标记是前台. 部分case会导致问题。
//    public Application.ActivityLifecycleCallbacks init() {
//        synchronized (lock) {
//            this.isFront = true;
//        }
//        return HLODER.INSTANCE;
//    }
    
    private static class HLODER {
        private static final ActivityCallBack INSTANCE = new ActivityCallBack();
    }

    private ActivityCallBack() {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        setContext(activity);
    }
    
    
    @Override
    public void onActivityStarted(Activity activity) {
        setContext(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        setContext(activity);
        isFront = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        setContext(activity);
        isFront = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        setContext(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        setContext(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        setContext(activity);
    }
    
    private void setContext(Activity activity) {
        EContextHelper.setContext(activity.getApplicationContext());
    }
    
    public synchronized boolean isAppAliaveInFront() {
        synchronized (lock) {
            return this.isFront;
        }
    }
    
}
