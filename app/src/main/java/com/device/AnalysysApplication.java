package com.device;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.analysys.track.AnalysysTracker;
import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 自定义的application
 * @Version: 1.0
 * @Create: 2019-07-27 14:03:51
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class AnalysysApplication extends Application {

    @Override
    public void onCreate() {
        //   JLibrary.InitEntry(this);
        // init  bugly
        Bugly.init(getApplicationContext(), "8fea5d1877", false);
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .build());
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectAll()
//                .penaltyLog()
//                .build());
        super.onCreate();
        initAnalysys();
        // MultiProcessWorker.runServices(this);
        //  EL.init(this);
    }

    /**
     * 初始化统计功能
     */
    private void initAnalysys() {

        // 初始化接口:第二个参数填写您在平台申请的appKey,第三个参数填写
        AnalysysTracker.init(this, "iiiii", "WanDouJia");
      //  AnalysysTracker.init(this, "fdfdf", "WanDouJia");
        // 设置打开debug模式，上线请置为false
        AnalysysTracker.setDebugMode(this, false);

        //init umeng
//        if (!getCurrentProcessName().contains(":")) {
//            MobclickAgent.setSessionContinueMillis(10);
//            MobclickAgent.setCatchUncaughtExceptions(true);
//        }
//        UMConfigure.setProcessEvent(true);
//        UMConfigure.setEncryptEnabled(true);
//        UMConfigure.setLogEnabled(true);
//
//
//        UMConfigure.init(this, "5b4c140cf43e4822b3000077", "track-demo-dev", UMConfigure.DEVICE_TYPE_PHONE, "99108ea07f30c2afcafc1c5248576bc5");


    }

    public String getCurrentProcessName() {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == pid) {
                    return info.processName;
                }
            }
        } catch (Throwable e) {
            MobclickAgent.reportError(this, e);
        }
        return "";
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // MultiDex.install(this);
    }
}
