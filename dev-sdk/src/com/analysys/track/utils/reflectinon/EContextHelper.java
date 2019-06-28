package com.analysys.track.utils.reflectinon;

import android.app.Application;
import android.content.Context;


public class EContextHelper {

    public static Context getContext(Context context) {
        try {
            if (context != null) {
                return context.getApplicationContext();
            }
        } catch (Throwable t) {
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
        }
        return null;
    }
}
