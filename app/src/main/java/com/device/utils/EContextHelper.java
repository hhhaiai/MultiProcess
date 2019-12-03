package com.device.utils;

import android.app.Application;
import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BuglyUtils;

public class EContextHelper {

    public static Context getContext(Context context) {
        try {
            if (context != null) {
                return context.getApplicationContext();
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
        }
        return getApplication();

    }

    private static Application getApplication() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object at = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(at);
            if (app != null) {
                return (Application) app;
            }
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return null;
    }
}
