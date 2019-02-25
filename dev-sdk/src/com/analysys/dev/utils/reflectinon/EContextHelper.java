package com.analysys.dev.utils.reflectinon;

import android.app.Application;
import android.content.Context;


public class EContextHelper {

    public static Context getContext(Context context) {
        if (context != null) {
            return context.getApplicationContext();
        } else {
            return getApplication();
        }
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
            e.printStackTrace();
        }
        return null;
    }
}
