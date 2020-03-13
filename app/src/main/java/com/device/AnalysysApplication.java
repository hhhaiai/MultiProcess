package com.device;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.analysys.track.AnalysysTracker;
import com.device.impls.MultiProcessFramework;
import com.device.tripartite.Abu;
import com.device.utils.EL;
import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;


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
        super.onCreate();

        initMultiProcessIfDebug(this.getApplicationContext());
        Abu.initBugly(this.getApplicationContext());
        Abu.initAnalysys(this.getApplicationContext());

        EL.init(this.getApplicationContext());
    }

    /**
     * 调试打开多进程和严格模式
     *
     * @param context
     */
    private void initMultiProcessIfDebug(Context context) {
        if (BuildConfig.USE_MULTI_TEST) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            MultiProcessFramework.runServices(context);
        }
    }
}
