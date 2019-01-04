package com.analysys.dev.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Looper;

import com.analysys.dev.utils.reflectinon.EContextHelper;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description 线程、进程判断(Thread/Process)
 * @Version 1.0
 * @Create 2018/12/18 16:51
 * @Author sanbo
 */
public class TPUtils {

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static boolean isMainThreadA() {
        return Looper.getMainLooper() == Looper.myLooper();
    }


    public static boolean isMainThreadB() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }

    public static boolean isMainProcess(Context context) {
        try {
            context = EContextHelper.getContext(context);
            if (context != null) {
                return context.getPackageName().equals(getCurrentProcessName(context));
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static String getCurrentProcessName(Context context) {
        try {
            context = EContextHelper.getContext(context);
            if (context != null) {
                int pid = android.os.Process.myPid();
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                    if (info.pid == pid) {
                        return info.processName;
                    }
                }
            }
        } catch (Throwable e) {
        }
        return "";
    }
}
