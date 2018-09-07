package com.eguan.utils;

import android.app.Application;
import android.content.Context;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 部分变量的获取方法
 * @Version: 1.0
 * @Create: 2018年8月30日 上午11:49:23
 * @Author: sanbo
 */
public class EGetHelper {

    public static Context getContext() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object at = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(at);
            if (app != null) {
                Application a = (Application) app;
                if (a != null) {
                    return a.getApplicationContext();
                }
            }
        } catch (Throwable e) {
        }
        return null;
    }
}
