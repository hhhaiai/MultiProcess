package cn.com.analysys.dex;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 测试DEX相关
 * @Version: 1.0
 * @Create: 2019-12-14 11:18
 * @author: sanbo
 */
public class Test {

    private static final String version = "1.0";
    private static final int count = 10000;

    public static void init(Context c, String key) {
        Log.i("sanbo", String.format("[%s]====Test.init(context,%s)=======收到初始化[%s]=============", getCurrentProcessName(c), key, version));
        for (int i = 0; i < count; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("sanbo", String.format("[%s]==={%d/%d}=====Test.init(context,%s)=====[%s]===%s", getCurrentProcessName(c), i, count, key, version, Log.getStackTraceString(e)));
            }
            Log.i("sanbo", String.format("[%s]======{%d/%d}======Test.init(context,%s)==call over===[%s]==", getCurrentProcessName(c), i, count, key, version));
        }
    }

    public static void init(Context c) {
        Log.i("sanbo", String.format("[%s]====Test.init(context)=======收到初始化[%s]=============", getCurrentProcessName(c), version));
        for (int i = 0; i < count; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e("sanbo", String.format("[%s]==={%d/%d}=====Test.init(context)=====[%s]===%s", getCurrentProcessName(c), i, count, version, Log.getStackTraceString(e)));
            }
            Log.i("sanbo", String.format("[%s]======{%d/%d}======Test.init(context)==call over===[%s]==", getCurrentProcessName(c), i, count, version));
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
