package com.analysys.dev.internal.utils.reflectinon;

import android.app.Application;
import android.content.Context;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 部分变量的获取方法
 * @Version: 1.0
 * @Create: 2018年8月30日 上午11:49:23
 * @Author: sanbo
 */
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
