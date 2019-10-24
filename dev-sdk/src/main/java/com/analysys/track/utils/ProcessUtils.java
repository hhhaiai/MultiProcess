package com.analysys.track.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Looper;

import com.analysys.track.BuildConfig;

public class ProcessUtils {


    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static String getCurrentProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == pid) {
                    return info.processName;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return "";
    }

}
