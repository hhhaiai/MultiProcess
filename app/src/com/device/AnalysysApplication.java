package com.device;

import android.app.Application;
import android.os.StrictMode;

import com.analysys.track.AnalysysTracker;
//import com.analysys.track.utils.ELOG;


public class AnalysysApplication extends Application {

    private boolean DEV_MODE = true;
    @Override
    public void onCreate() {
//        if (DEV_MODE) {
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectCustomSlowCalls() //API等级11，使用StrictMode.noteSlowCode
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()   // or .detectAll() for all detectable problems
//                    .penaltyDialog() //弹出违规提示对话框
//                    .penaltyLog() //在Logcat 中打印违规异常信息
//                    .penaltyFlashScreen() //API等级11
//                    .build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects() //API等级11
//                    .penaltyLog()
//                    .penaltyDeath()
//                    .build());
//        }

        super.onCreate();
        AnalysysTracker.init(this, "7752552892442721d", "WanDouJia");
        AnalysysTracker.setDebugMode(this,true);
    }
}
