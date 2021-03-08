package com.analysys.track.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;

public class EContextHelper {
    private static Context mContext;

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
                ClazzUtils cz = ClazzUtils.g();
                Application app = null;
                if (cz != null) {
                    Object at = cz.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");
                    app = (Application) cz.invokeObjectMethod(at, "getApplication");
                    if (app != null) {
                        mContext = app.getApplicationContext();
                    }
                    if (mContext == null) {
                        app = (Application) cz.invokeStaticMethod("android.app.AppGlobals", "getInitialApplication");
                        if (app != null) {
                            mContext = app.getApplicationContext();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.logcat) {
                Log.e("analysys", Log.getStackTraceString(e));
            }
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
