package com.device;

import android.app.Application;
import android.os.StrictMode;

import com.analysys.track.AnalysysTracker;
import com.device.impls.MultiProcessWorker;
import com.device.utils.EL;
import com.tencent.bugly.crashreport.CrashReport;


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
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        super.onCreate();
        initAnalysys();
        MultiProcessWorker.runServices(this);
    }

    /**
     * 初始化统计功能
     */
    private void initAnalysys() {
        // 设置打开debug模式，上线请置为false
        AnalysysTracker.setDebugMode(false);
        // 初始化接口:第二个参数填写您在平台申请的appKey,第三个参数填写
        AnalysysTracker.init(this, "7752552892442721d", "WanDouJia");

        try {
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
            strategy.setAppReportDelay(1);   //改为1ms
            CrashReport.setAppPackage(getApplicationContext(), getPackageName());
            CrashReport.setAppChannel(getApplicationContext(), "track-dev");
            //玩安卓demo
            CrashReport.initCrashReport(getApplicationContext(), "869b2916c8", true, strategy);

        } catch (Throwable e) {
            EL.e(e);
        }
    }
}
