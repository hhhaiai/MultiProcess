package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;

public class EContextHelper {
    private static Context mContext;

    public static Context getContext() {
        if (!BuildConfig.IS_HOST && mContext == null) {
            try {
                Object at = ClazzUtils.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread", null, null);
                mContext = (Context) ClazzUtils.invokeObjectMethod(at, "getApplication");
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
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
                    BuglyUtils.commitError(e);
                }
            }
        }
    }
}
