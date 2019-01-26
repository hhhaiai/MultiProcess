package com.device;

import android.app.Application;
import com.analysys.dev.EguanMonitorAgent;


public class AnalysysApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EguanMonitorAgent.getInstance().initEguan(this, "7752552892442721d", "WanDouJia", true);
        EguanMonitorAgent.getInstance().setDebugMode(this,true);
    }
}
