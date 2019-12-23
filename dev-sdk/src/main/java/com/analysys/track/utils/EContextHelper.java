package com.analysys.track.utils;

import android.content.Context;

public class EContextHelper {
    private static Context mContext;

    public static Context getContext() {
//        if (context == null) {
//            try {
//                Object at = ClazzUtils.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", null, null);
//                context = (Context) ClazzUtils.invokeObjectMethod(at, "getApplication");
//            } catch (Throwable e) {
//            }
//        }
        return mContext;
    }

    public static void setContext(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }
}
