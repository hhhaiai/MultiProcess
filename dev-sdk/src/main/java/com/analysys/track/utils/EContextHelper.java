package com.analysys.track.utils;

import android.app.Application;
import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;

public class EContextHelper {
    private static Context mContext;

    public static Context getContext() {
//        if (!BuildConfig.IS_HOST && mContext == null) {
        if (mContext == null) {
            try {
//                // 此处逻辑内有调用context的方法，会导致死循环
//                if (EGContext.FLAG_DEBUG_INNER) {
//                    ELOG.e(BuildConfig.tag_hotfix, "context 出现为空，错误请检查");
//                }
//                Object at = ClazzUtils.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", null, null);
//                mContext = (Context) ClazzUtils.invokeObjectMethod(at, "getApplication");
//                if (mContext == null) {
//                    if (EGContext.FLAG_DEBUG_INNER) {
//                        ELOG.e(BuildConfig.tag_hotfix, "context 反射获取后为空，错误请检查");
//                    }
//                }

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
