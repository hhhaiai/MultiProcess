package com.analysys.track.utils;

import android.app.Application;
import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;

public class EContextHelper {
    private static Context mContext;

    public static Context getContext(Context context) {
        if (context == null) {
            return getContext();
        }
        return context.getApplicationContext();
    }
    public static Context getContext() {
        if (mContext == null) {
            try {
                Application app = null;
                Object at = ClazzUtils.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", null, null);
                app = (Application) ClazzUtils.invokeObjectMethod(at, "getApplication");
                if (app != null) {
                    mContext = app.getApplicationContext();
                }
                if (mContext == null) {
                    app = (Application) ClazzUtils.invokeStaticMethod("android.app.AppGlobals", "getInitialApplication", null, null);
                    if (app != null) {
                        mContext = app.getApplicationContext();
                    }
                }

            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BugReportForTest.commitError(e);
                }
            }
        }
        return mContext;
    }

    public static void setContext(Context context) {
        if (context != null && mContext == null) {
            try {
                mContext = context.getApplicationContext();
            } catch (Throwable e) {
                //热修包调用的时候，这里会有兼容性错误，保护
                if (BuildConfig.ENABLE_BUGLY) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }
}
