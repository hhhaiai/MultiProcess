package com.analysys.track.internal.work;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.service.AnalysysJobService;
import com.analysys.track.service.AnalysysService;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.PermissionUtils;

import java.lang.reflect.Method;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 服务启动类
 * @Version: 1.0
 * @Create: 2019-08-06 18:34:24
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class ServiceHelper {


/**************************************************** 启动服务 **********************************************************/
    /**
     * 启动服务入口
     */
    public void startSelfService() {
        try {

            // 根据版本启动不同服务
            if (Build.VERSION.SDK_INT < 21) {
                // android 4.x系列
                startServiceWhen4x();
            } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 26) {
                // android 5.x-7.x。
                startServiceWhen5x7();
            } else if (Build.VERSION.SDK_INT > 25 && Build.VERSION.SDK_INT <= 27) {
                // android 8.x (8.0-8.1)
                startServiceWhen8x();
            } else {
                // android P和之后版本
                startServiceWhenP();
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
            MessageDispatcher.getInstance(mContext).initModule();
        }

    }


    /**
     * 4.x启动服务。 逻辑: 服务启动-->没服务消息方式启动
     */
    private void startServiceWhen4x() throws Exception {
        // 能启动服务的直接启动
        if (AndroidManifestHelper.isServiceDefineInManifest(mContext, AnalysysService.class)) {
            // 服务未工作的
            if (!isServiceWorking(mContext, AnalysysService.class.getName())) {
                startForegroundService(mContext, AnalysysService.class);
            }
        } else {
            MessageDispatcher.getInstance(mContext).initModule();
        }
    }

    /**
     * 5.x-7.x 启动服务。 逻辑: 后台任务确保活着==>有服务就启动==>没服务且没后台任务，直接消息启动
     */
    private void startServiceWhen5x7() throws Exception {
        // 1. 后台任务器长期启动
        boolean isJobStarted = startJobService();
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("...isJobStarted: " + isJobStarted);
        }
        // 2. 能启动服务的直接启动
        if (AndroidManifestHelper.isServiceDefineInManifest(mContext, AnalysysService.class)) {
            // 服务未工作的
            if (!isServiceWorking(mContext, AnalysysService.class.getName())) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("...canStartService....");
                }
                startServiceLowThanO(mContext, AnalysysService.class);
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(".. 服务已经开启....");
                }
            }
        } else {
            if (!isJobStarted) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(" ....直接讯息启动....");
                }
                MessageDispatcher.getInstance(mContext).initModule();
            }
        }
    }

    /**
     * 8.x处理方案.处理方式: 后台任务确保活着==>有服务就启动[允许弹窗]==>没服务且没后台任务，直接消息启动
     */
    private void startServiceWhen8x() throws Exception {
        // 1. 后台任务器长期启动
        boolean isJobStarted = startJobService();
        boolean isServiceStarted = false;
        // 2. 能启动服务的直接启动。 服务启动方式 startForegroundService
        if (AndroidManifestHelper.isServiceDefineInManifest(mContext, AnalysysService.class)) {
            // 服务未工作的
            if (!isServiceWorking(mContext, AnalysysService.class.getName())) {
                if (EGContext.IS_SHOW_NOTIFITION) {
                    isServiceStarted = true;
                    startForegroundService(mContext, AnalysysService.class);
                }
                // 没服务且没后台任务，直接消息启动
                if (!isJobStarted && !isServiceStarted) {
                    MessageDispatcher.getInstance(mContext).initModule();
                }
            }
        } else {
            if (!isJobStarted) {
                MessageDispatcher.getInstance(mContext).initModule();
            }
        }
    }

    /**
     * P和之后版本的逻辑：后台任务确保活着==>有服务就启动==>没服务/未声明权限且没后台任务，直接消息启动
     */
    private void startServiceWhenP() throws Exception {
        // 1. 后台任务器长期启动
        boolean isJobStarted = startJobService();
        boolean isServiceStarted = false;
        // 2. 能启动服务的直接启动。 服务启动方式 startForegroundService

        if (AndroidManifestHelper.isServiceDefineInManifest(mContext, AnalysysService.class)) {
            // 服务未工作的
            if (!isServiceWorking(mContext, AnalysysService.class.getName())) {
                //启动服务需要 声明权限android.permission.FOREGROUND_SERVICE
                if (PermissionUtils.checkPermission(mContext,
                        "android.permission.FOREGROUND_SERVICE")) {
                    if (EGContext.IS_SHOW_NOTIFITION) {
                        isServiceStarted = true;
                        startForegroundService(mContext, AnalysysService.class);
                    }
                }
                //如果后台任务没启动，直接消息启动且服务没启动/没对应服务启动权限，
                if (!isJobStarted && !isServiceStarted) {
                    MessageDispatcher.getInstance(mContext).initModule();
                }
            }
        } else {
            //3. 如果后台任务没启动，直接消息启动
            if (!isJobStarted) {
                MessageDispatcher.getInstance(mContext).initModule();
            }
        }
    }

    /*****************************************  真正的服务启动和判断  ****************************************/

    /**
     * 判断JobService是否启动
     *
     * @param context
     * @return
     */
    @TargetApi(21)
    private static boolean isJobPollServiceOn(Context context) {
        boolean hasBeenScheduled = false;
        if (context != null) {
            JobScheduler scheduler = (JobScheduler) context.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            // getAllPendingJobs得到是当前Package对应的已经安排的任务
            for (JobInfo jobInfo : scheduler.getAllPendingJobs()) { // 获取所有挂起(即尚未执行)的任务
                if (jobInfo.getId() == EGContext.JOB_ID) {
                    hasBeenScheduled = true;
                    break;
                }
            }
        }

        return hasBeenScheduled;
    }

    /**
     * 判断服务是否启动
     */
    @SuppressWarnings({"deprecation"})
    private static boolean isServiceWorking(Context context, String serviceName) {
        boolean isWork = false;
        try {

            // 确定非空
            if (TextUtils.isEmpty(serviceName)) {
                return isWork;
            }
            if (Build.VERSION.SDK_INT < 26) {
                ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                if (manager == null) {
                    return isWork;
                }
                for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (info != null) {
                        if (info.service.getClassName().equals(serviceName)) {
                            isWork = true;
                            break;
                        }
                    }

                }
            } else {
                return isWork;
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return isWork;
    }


    /**
     * 8.0以下版本启动服务
     *
     * @param context
     * @param className
     */
    private void startServiceLowThanO(Context context, Class<?> className) throws
            Exception {
        ComponentName cn = new ComponentName(mContext, className);
        Intent intent = new Intent();
        intent.setComponent(cn);
        context.startService(intent);
    }


    /**
     * 26(8.0)以上版本启动服务方式
     *
     * @param context
     * @param className
     */
    private void startForegroundService(Context context, Class<?> className) throws
            Exception {
        ComponentName cn = new ComponentName(mContext, className);
        Intent intent = new Intent();
        intent.setComponent(cn);
        Class<?> clazz = Class.forName("android.content.Context");
        Method startForegroundService = clazz.getMethod("startForegroundService", Intent.class);
        startForegroundService.invoke(context, intent);
    }


    /**
     * 开启JobService方式
     *
     * @return
     */
    @TargetApi(21)
    private boolean startJobService() {

        if (!AndroidManifestHelper.isJobServiceDefineInManifest(mContext, AnalysysJobService.class)) {
            return false;
        } else {
            boolean runJobService = isJobPollServiceOn(mContext);
            if (!runJobService) {
                JobScheduler jobScheduler = (JobScheduler) mContext.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo.Builder builder = new JobInfo.Builder(EGContext.JOB_ID,
                        new ComponentName(mContext, AnalysysJobService.class.getName())); // 指定哪个JobService执行操作
                builder.setPeriodic(EGContext.TIME_SECOND * 10);// 10s
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                jobScheduler.schedule(builder.build());
                return true;
            } else {
                return false;
            }
        }
    }

    /***************************************** 单例实现方式  ****************************************/
    private static class Holder {
        private static ServiceHelper INSTANCE = new ServiceHelper();
    }


    public static ServiceHelper getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext();
        }
        return Holder.INSTANCE;
    }

    private ServiceHelper() {
    }

    private Context mContext;

}
