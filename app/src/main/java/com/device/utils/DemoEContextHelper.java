package com.device.utils;

import android.app.Application;
import android.content.Context;

public class DemoEContextHelper {
    private static Context mContext;

    public static Context getContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return getContext();
    }

    public static Context getContext() {

        try {
            if (mContext == null) {
                Application app = null;
                Object at = DemoClazzUtils.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", null, null);
                app = (Application) DemoClazzUtils.invokeObjectMethod(at, "getApplication");
                if (app != null) {
                    mContext = app.getApplicationContext();
                }
                if (mContext == null) {
                    app = (Application) DemoClazzUtils.invokeStaticMethod("android.app.AppGlobals", "getInitialApplication", null, null);
                    if (app != null) {
                        mContext = app.getApplicationContext();
                    }
                }
            }
        } catch (Throwable e) {
        }

        return mContext;
    }

    public static void setContext(Context context) {
        try {
            if (context != null && mContext == null) {
                mContext = context.getApplicationContext();
            }
        } catch (Throwable e) {
        }
    }
}
