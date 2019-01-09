package com.analysys.dev.internal.work;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.utils.PermissionUtils;
import com.analysys.dev.utils.Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.receiver.DynamicReceivers;
import com.analysys.dev.service.AnalysysService;

import java.lang.reflect.Method;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 服务启动
 * @Version: 1.0
 * @Create: 2018年9月12日 下午2:26:29
 * @Author: sanbo
 */
public class ServiceHelper {
    Context mContext;
    private static DynamicReceivers dynamicReceivers = null;

    private ServiceHelper() {

    }

    private static class Holder {
        private static ServiceHelper INSTANCE = new ServiceHelper();
    }

    public static ServiceHelper getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * 注册动态广播
     */
    public void registerReceiver() {
        if (dynamicReceivers == null) {
            dynamicReceivers = new DynamicReceivers();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            mContext.registerReceiver(dynamicReceivers, intentFilter);
        }
    }

    /**
     * 官方api方式打开服务
     */
    protected void startSelfService() {
        if (isStartService()) {
            boolean isWork = Utils.isServiceWork(mContext, EGContext.SERVICE_NAME);
            if (!isWork) {
                try {
                    ComponentName cn = new ComponentName(mContext, AnalysysService.class);
                    Intent intent = new Intent();
                    intent.setComponent(cn);
                    if (Build.VERSION.SDK_INT >= 26) {
                        Class<?> clazz = Class.forName("android.content.Context");
                        Method startForegroundService = clazz.getMethod("startForegroundService", Intent.class);
                        startForegroundService.invoke(mContext, intent);
                    } else {
                        mContext.startService(intent);
                    }
                } catch (Throwable e) {
                }
            }
        } else {
            MessageDispatcher.getInstance(mContext).initModule();
        }
    }

    /**
     * 判断是否可以启动服务
     */
    private boolean isStartService() {
        if (Build.VERSION.SDK_INT < 26) {
            return true;
        }
        if (EGContext.FLAG_SHOW_NOTIFY) {
            if (Build.VERSION.SDK_INT >= 28) {
                if (PermissionUtils.checkPermission(mContext,
                        "android.permission.FOREGROUND_SERVICE")) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过系统接口启动服务，用作拉活使用
     */
    protected void startServiceByCode(Intent intent) {

    }

    /**
     * 通过shell方式启动服务，用作拉活使用
     */
    protected void startServiceByShell(Intent intent) {

    }
}
