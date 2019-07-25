package com.device;

import android.app.Application;

import com.analysys.track.AnalysysTracker;

public class AnalysysApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 设置打开debug模式，上线请置为false
        AnalysysTracker.setDebugMode(true);
        // 初始化接口:第二个参数填写您在平台申请的appKey,第三个参数填写
        AnalysysTracker.init(this, "7752552892442721d", "WanDouJia");
    }
}
