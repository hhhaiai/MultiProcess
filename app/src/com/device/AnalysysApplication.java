package com.device;

import android.app.Application;
import com.analysys.dev.EguanMonitorAgent;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/10 14:24
 * @Author: Wang-X-C
 */
public class AnalysysApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EguanMonitorAgent.getInstance().initEguan(this, "aaaaaa", "WanDouJia", true);
    }
}
