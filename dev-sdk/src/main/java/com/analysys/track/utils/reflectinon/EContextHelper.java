package com.analysys.track.utils.reflectinon;

import android.app.Application;
import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BuglyUtils;

/**
 * @Copyright © 2019/12/15 analysys Inc. All rights reserved.
 * @Description: 内存持有context对象
 * @Version: 1.0
 * @Create: 2019/12/15 13:35
 * @author: sanbo
 */
public class EContextHelper {


    private static Context mContext = null;

    public static void setContext(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    public static Context getContext(Context context) {
        try {
            if (context != null) {
                mContext = context.getApplicationContext();
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
        }
        return mContext;

    }

//    private static Application getApplication() {
//        try {
//            Class<?> activityThread = Class.forName("android.app.ActivityThread");
//            Object at = activityThread.getMethod("currentActivityThread").invoke(null);
//            Object app = activityThread.getMethod("getApplication").invoke(at);
//            if (app != null) {
//                return (Application) app;
//            }
//        } catch (Exception e) {
//            if (BuildConfig.ENABLE_BUGLY) {
//                BuglyUtils.commitError(e);
//            }
//        }
//        return null;
//    }
}
