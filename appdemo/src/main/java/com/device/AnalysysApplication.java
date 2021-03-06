package com.device;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.device.services.MultiProcessFramework;
import com.device.tripartite.Abu;
import com.device.utils.EL;


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

        // 初始化多进程和安卓系统的严格模式检测
        initMultiProcessIfDebug(this.getApplicationContext());
        // 初始化bugly
        Abu.initBugly(this.getApplicationContext());
        // 统计初始化: 含友盟统计和易观审计SDK
        Abu.initAnalysys(this.getApplicationContext());
        // 日志工具类初始化
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 低版本OAID获取必须调用
//        JLibrary.InitEntry(base);
    }
}
