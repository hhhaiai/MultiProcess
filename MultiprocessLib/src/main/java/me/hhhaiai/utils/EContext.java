package me.hhhaiai.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class EContext {
    private static Context mContext = null;

    public static Context getContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return getContextImpl();
    }

    public static Context getContext() {
        return getContextImpl();
    }

    private static Context getContextImpl() {
        try {
            if (mContext == null) {
                Object at = Reflect.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");
                Application app = (Application) Reflect.invokeObjectMethod(at, "getApplication");
                if (app != null) {
                    mContext = app.getApplicationContext();
                }
                if (mContext == null) {
                    app = (Application) Reflect.invokeStaticMethod("android.app.AppGlobals", "getInitialApplication");
                    if (app != null) {
                        mContext = app.getApplicationContext();
                    }
                }
            }
        } catch (Throwable igone) {
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
