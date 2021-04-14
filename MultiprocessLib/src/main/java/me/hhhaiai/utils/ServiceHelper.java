package me.hhhaiai.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

public class ServiceHelper {


    /******************************开启服务***********************************/
    public static void startService(Context context, List<Class<Service>> clazzs) {
        if (clazzs != null) {
            for (int i = 0; i < clazzs.size(); i++) {
                startService(context, clazzs.get(i));
            }
        }
    }

    public static void startService(Context context, Class<Service> clazz) {
        try {
            context = EContext.getContext(context);
            if (context == null) {
                return;
            }
            if (AndroidManifestHelper.isServiceDefineInManifest(context, clazz)) {
                if (!isServiceWorking(context, clazz.getName())) {
                    if (Build.VERSION.SDK_INT < 26) {
                        startServiceImpl(context, clazz);
                    } else {
                        startForegroundServiceImpl(context, clazz);
                    }
                }
            }
        } catch (Throwable e) {
        }
    }

    private static void startForegroundServiceImpl(Context context, Class<Service> clazz) {
        ComponentName cn = new ComponentName(context, clazz);
        Intent intent = new Intent();
        intent.setComponent(cn);
        Reflect.invokeObjectMethod(context, "startForegroundService", new Class[]{Intent.class}, new Object[]{intent});
    }

    private static void startServiceImpl(Context context, Class<?> clazz) {
        ComponentName cn = new ComponentName(context, clazz);
        Intent intent = new Intent();
        intent.setComponent(cn);
        context.startService(intent);
    }

    /******************************关闭服务***********************************/

    public static void stopService(Context context, Class<Service> clazz) {
        try {
            context = EContext.getContext(context);
            if (context == null) {
                return;
            }
            if (AndroidManifestHelper.isServiceDefineInManifest(context, clazz)) {
                if (isServiceWorking(context, clazz.getName())) {
                    ComponentName cn = new ComponentName(context, clazz);
                    Intent intent = new Intent();
                    intent.setComponent(cn);
                    context.stopService(intent);
                }
            }
        } catch (Throwable e) {
        }
    }

    /******************************开启JobService***********************************/

    @TargetApi(21)
    public static boolean startJobService(Context context, Class<JobService> clazz, int jobId, long intervalMillis) {
        try {
            context = EContext.getContext(context);
            if (context == null) {
                return false;
            }
            if (!AndroidManifestHelper.isJobServiceDefineInManifest(context, clazz)) {
                return false;
            } else {
                boolean runJobService = isJobPollServiceOn(context, jobId);
                if (!runJobService) {
                    try {
                        if (mJobScheduler == null) {
                            mJobScheduler = (JobScheduler) context.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                        }
                        JobInfo.Builder builder = new JobInfo.Builder(jobId,
                                new ComponentName(context, clazz.getName()));
                        builder.setPeriodic(intervalMillis);
                        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                        mJobScheduler.schedule(builder.build());
                    } catch (Throwable e) {
                    }
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /******************************JobService运行状态判断***********************************/

    @TargetApi(21)
    private static boolean isJobPollServiceOn(Context context, int jobId) {
        boolean hasBeenScheduled = false;
        try {

            if (mJobScheduler == null) {
                if (context == null) {
                    return false;
                }
                mJobScheduler = (JobScheduler) context.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            }
            // getAllPendingJobs得到是当前Package对应的已经安排的任务
            for (JobInfo jobInfo : mJobScheduler.getAllPendingJobs()) { // 获取所有挂起(即尚未执行)的任务
                if (jobInfo.getId() == jobId) {
                    hasBeenScheduled = true;
                    break;
                }
            }
        } catch (Throwable e) {
        }

        return hasBeenScheduled;
    }

    /******************************Service运行状态判断***********************************/

    public static boolean isServiceWorking(Context context, Class<Service> serviceClass) {
        return isServiceWorking(context, serviceClass.getName());
    }


    @SuppressWarnings({"deprecation"})
    public static boolean isServiceWorking(Context context, String serviceName) {
        boolean isWork = false;
        try {
            context = EContext.getContext(context);
            // 确定非空
            if (context == null || TextUtils.isEmpty(serviceName)) {
                return isWork;
            }
            if (mActivityManager == null) {
                mActivityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            }
            for (ActivityManager.RunningServiceInfo info : mActivityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (info != null) {
                    if (info.service.getClassName().equals(serviceName)) {
                        isWork = true;
                        break;
                    }
                }

            }

        } catch (Throwable e) {
        }
        return isWork;
    }


    private static ActivityManager mActivityManager = null;
    private static JobScheduler mJobScheduler = null;
}
