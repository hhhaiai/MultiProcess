package com.device.impls;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BuglyUtils;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 测试DEX相关
 * @Version: 1.0
 * @Create: 2019-12-14 11:18
 * @author: sanbo
 */
public class Test {

    public static void init(Context c, String key) {
        Log.i("sanbo", String.format("[%s]===========收到初始化[%s]=============", getCurrentProcessName(c), key));
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("sanbo", String.format("[%s] %s", getCurrentProcessName(c), Log.getStackTraceString(e)));
            }
            Log.i("sanbo", String.format("[%s] init(%d): %s ", getCurrentProcessName(c), i, "哦吼, 调用成功了。。。"));
        }
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

}
