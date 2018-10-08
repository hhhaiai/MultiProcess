package com.analysys.dev.internal.work;

import com.analysys.dev.internal.utils.EContextHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Message;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 服务启动
 * @Version: 1.0
 * @Create: 2018年9月12日 下午2:26:29
 * @Author: sanbo
 */
public class ServiceHelper {
    private Context mContext = null;

    private ServiceHelper() {}

    private static class Holder {
        private static ServiceHelper instance = new ServiceHelper();
    }

    public static ServiceHelper getInstance(Context context) {
        if (Holder.instance.mContext == null) {
            if (context != null) {
                Holder.instance.mContext = context;
            } else {
                Holder.instance.mContext = EContextHelper.getContext();
            }
        }
        return Holder.instance;
    }

    /**
     * 启动服务任务接入
     * 
     * @param delay
     */
    public void startService(int delay) {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_START_SERVICE_SELF;
        MessageDispatcher.getInstance(mContext).sendMessage(msg, delay);
    }

    /**
     * 官方api方式打开服务
     */
    public void startSelfService() {
        // try {
        // ComponentName cn = new ComponentName(mContext, MonitorService.class);
        // Intent intent = new Intent();
        // intent.setComponent(cn);
        // if (Build.VERSION.SDK_INT >= 26) {
        // if (EDContext.FLAG_SHOW_NOTIFY) {
        // // P以上需要设置
        // if (Build.VERSION.SDK_INT >= 28) {
        // if (PermissionUtils.checkPermission(mContext, "android.permission.FOREGROUND_SERVICE")) {
        // Class<?> clazz = Class.forName("android.content.Context");
        // Method startForegroundService = clazz.getMethod("startForegroundService", Intent.class);
        // startForegroundService.invoke(mContext, intent);
        // } else {
        // startWork();
        // }
        // } else {
        // Class<?> clazz = Class.forName("android.content.Context");
        // Method startForegroundService = clazz.getMethod("startForegroundService", Intent.class);
        // startForegroundService.invoke(mContext, intent);
        // }
        // } else {
        // startWork();
        // }
        // } else {
        // mContext.startService(intent);
        // }
        // } catch (Throwable e) {
        // }
    }

    public void startWork() {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_WORK;
        MessageDispatcher.getInstance(mContext).sendMessage(msg, 0);
    }

    /**
     * 通过系统接口启动服务
     * 
     * @param intent
     */
    protected void startServiceByCode(Intent intent) {

    }

    /**
     * 通过shell方式启动服务
     * 
     * @param intent
     */
    protected void startServiceByShell(Intent intent) {

    }
}
