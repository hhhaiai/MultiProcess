package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.utils.reflectinon.ClazzUtils;

public class EContextHelper {
    public static Context context;

    public static Context getContext() {
        if (context == null) {
            try {
                Object at = ClazzUtils.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", null, null);
                context = (Context) ClazzUtils.invokeObjectMethod(at, "getApplication");
            } catch (Throwable e) {
            }
        }
        return EContextHelper.context;
    }

    public static void setContext(Context context) {
        if (context != null) {
            EContextHelper.context = context.getApplicationContext();
        }
    }
}
