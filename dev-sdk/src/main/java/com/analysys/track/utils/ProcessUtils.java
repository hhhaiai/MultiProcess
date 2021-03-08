package com.analysys.track.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Looper;

import com.analysys.track.BuildConfig;

import java.util.List;

public class ProcessUtils {


    /**
     * 是否是主线程
     *
     * @return
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * 获取当前进程的名称
     *
     * @param context
     * @return
     */
    public static String getCurrentProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
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
    public static boolean isMainProcess(Context context) {
        try {
            if (context == null) {
                return false;
            }
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApps = null;
            if (activityManager != null) {
                runningApps = activityManager.getRunningAppProcesses();
            }
            if (runningApps == null) {
                return false;
            }
            String process = "";
            for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
                if (proInfo.pid == android.os.Process.myPid()) {
                    if (proInfo.processName != null) {
                        process = proInfo.processName;
                    }
                }
            }
            String mainProcessName = null;
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            if (applicationInfo != null) {
                mainProcessName = context.getApplicationInfo().processName;
            }
            if (mainProcessName == null) {
                mainProcessName = context.getPackageName();
            }
            return mainProcessName.equals(process);
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

}
