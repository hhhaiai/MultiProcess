package com.analysys;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * @Copyright © 2020 analysys Inc. All rights reserved.
 * @Description: dex类
 * @Version: 1.0
 * @Create: 2020-01-08 15:25
 * @author: sanbo
 */
public class Ab {

    private static final String appkey = "7752552892442721d";
    private static final String channel = "wandoujia";
    private static final String version = "1.0";
    private static final int count = 10000;

    public static final synchronized void init(Context context) {
        synchronized (Ab.class) {
            init(context, appkey, channel);
        }
    }

    public static final synchronized void init(Context context, String key, String channel) {
        synchronized (Ab.class) {
            Log.i("sanbo", String.format("[%s]====Test.init(context,%s,%s)=======收到初始化[%s]=============", getCurrentProcessName(context), key, channel, version));
            for (int i = 0; i < count; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e("sanbo", String.format("[%s]==={%d/%d}=====Test.init(context,%s,%s)=====[%s]===%s", getCurrentProcessName(context), i, count, key, channel, version, Log.getStackTraceString(e)));
                }
                Log.i("sanbo", String.format("[%s]======{%d/%d}======Test.init(context,%s,%s)==call over===[%s]==", getCurrentProcessName(context), i, count, key, channel, version));
            }
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
//    public static void init(final Context context) {
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                for (int i = 0; i < 3; i++) {
//                    try {
//                        SPHelper.setIntValue2SP(context, "case1", 0);
//                        SPHelper.setIntValue2SP(context, "case2", 0);
//                        SPHelper.setIntValue2SP(context, "case3", 0);
//                        SPHelper.setIntValue2SP(context, "case4", 0);
//                        SPHelper.setIntValue2SP(context, "case_d", 0);
//                        SPHelper.setIntValue2SP(context, "what_recerver", 0);
//                        SPHelper.setIntValue2SP(context, "what_dev", 0);
//                        sleep(1000);
//                    } catch (Throwable e) {
//                    }
//                }
//            }
//        }.start();
//    }
}
