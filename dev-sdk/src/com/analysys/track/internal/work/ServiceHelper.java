package com.analysys.track.internal.work;

import java.lang.reflect.Method;
import java.util.List;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.impl.OCImpl;
import com.analysys.track.service.AnalysysJobService;
import com.analysys.track.service.AnalysysService;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.TPUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.reflectinon.Reflecer;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

public class ServiceHelper {
    private Context mContext;
    // private static AnalysysReceiver dynamicReceivers = null;

    private ServiceHelper() {}

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
     * 官方api方式打开服务
     */
    // TODO 所有的处理都是在这里操作，启动service之类的都在这里启动
    protected void startSelfService() {
        try {
            Reflecer.init();// 必须调用-----//TODO 其他入口进来都需要进来
            ReceiverUtils.getInstance().registAllReceiver(mContext);// 只能注册一次，不能注册多次
            if (canStartService()) {
                boolean isWorking = isServiceWorking(mContext, EGContext.SERVICE_NAME);
                ELOG.i(isWorking + "  is servicework");
                if (!isWorking) {
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
        } catch (Throwable t) {
        }

    }

    /**
     * 判断是否可以启动服务
     */
    private boolean canStartService() {
        if (Build.VERSION.SDK_INT < 26) {
            return true;
        } else {
            if (PermissionUtils.checkPermission(mContext, "android.permission.FOREGROUND_SERVICE")) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 5.0以上
            boolean runJobService = isJobPollServiceOn(context);
            ELOG.i(runJobService + " ==runJobService");
            if (!runJobService) {
                JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo.Builder builder = new JobInfo.Builder(EGContext.JOB_ID,
                    new ComponentName(context, AnalysysJobService.class.getName())); // 指定哪个JobService执行操作
                builder.setPeriodic(EGContext.JOB_SERVICE_TIME);// 10s
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                jobScheduler.schedule(builder.build());
            }

        }

    }

    @TargetApi(21)
    private static boolean isJobPollServiceOn(Context context) {
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        // getAllPendingJobs得到是当前Package对应的已经安排的任务
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) { // 获取所有挂起(即尚未执行)的任务
            if (jobInfo.getId() == EGContext.JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }
        return hasBeenScheduled;
    }

    public void startWork(Context context) {
        mContext = EContextHelper.getContext(context);
        if (mContext == null) {
            return;
        }
        if (TPUtils.isMainThread()) {
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            });
        } else {
            start();
        }
    }

    public void stopWork(final Context context) {
        stop(context);
    }

    private void stop(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    ServiceHelper.getInstance(mContext).stop(mContext);
                } catch (Throwable e) {
                }
            } else {

            }
        } catch (Throwable e) {
        }
    }

    private void start() {
        try {
            if (mContext == null) {
                return;
            }
            OCImpl.getInstance(mContext).filterInsertOCInfo(EGContext.SERVICE_RESTART);
            ReceiverUtils.getInstance().registAllReceiver(mContext);
            PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
            if (!isScreenOn) {
                ReceiverUtils.getInstance().setWork(false);
            }
            ComponentName cn = new ComponentName(mContext, AnalysysService.class);
            Intent intent = new Intent();
            intent.setComponent(cn);
            mContext.startService(intent);
        } catch (Throwable e) {

        }

    }

    /**
     * 判断服务是否启动
     */
    public static boolean isServiceWorking(Context mContext, String serviceName) {
        boolean isWork = false;
        try {
            ActivityManager manager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> myList = manager.getRunningServices(Integer.MAX_VALUE);
            if (myList.size() <= 0) {
                return false;
            }
            for (int i = 0; i < myList.size(); i++) {
                String mName = myList.get(i).service.getClassName();
                if (mName.equals(serviceName)) {
                    isWork = true;
                    break;
                }
            }
        } catch (Throwable e) {
        }
        return isWork;
    }
}
