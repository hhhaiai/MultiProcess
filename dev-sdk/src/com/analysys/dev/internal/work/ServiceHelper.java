package com.analysys.dev.internal.work;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.service.AnalysysJobService;
import com.analysys.dev.utils.PermissionUtils;
import com.analysys.dev.utils.TPUtils;
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
        }else{
            if (EGContext.FLAG_SHOW_NOTIFY && PermissionUtils.checkPermission(mContext,
                    "android.permission.FOREGROUND_SERVICE")){
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
    public void startJobService(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean runJobService = isJobPollServiceOn(context);
            if (!runJobService) {
                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo.Builder builder = new JobInfo.Builder(EGContext.JOB_ID, new ComponentName
                        (context, AnalysysJobService.class.getName()));  //指定哪个JobService执行操作
                builder.setPeriodic(EGContext.JOB_SERVICE_TIME);
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                jobScheduler.schedule(builder.build());
            }

        }

    }
    @TargetApi(21)
    private static boolean isJobPollServiceOn(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context
                .JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        //getAllPendingJobs得到是当前Package对应的已经安排的任务
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) { //获取所有挂起(即尚未执行)的任务
            if (jobInfo.getId() == EGContext.JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }
        return hasBeenScheduled;
    }

    public void startWork(Context context) {
        mContext = EContextHelper.getContext(context);
        start();
    }

    private void start() {
        if (mContext == null) {
            return;
        }
    }

    public void stopWork(final Context context) {
        stop(context);
    }

    private void stop(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    ServiceHelper.getInstance(mContext).startJobService(mContext);
                } catch (Throwable e) {
                }
            } else {
                ComponentName cn = new ComponentName(mContext, AnalysysService.class);
                Intent intent = new Intent();
                intent.setComponent(cn);
                mContext.startService(intent);
            }
        } catch (Throwable e) {
        }
    }

}
