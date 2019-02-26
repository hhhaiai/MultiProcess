package com.analysys.dev.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.text.TextUtils;

import com.analysys.dev.database.DBConfig;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.utils.sp.SPHelper;

import java.io.File;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description 线程、进程判断(Thread/Process)
 * @Version 1.0
 * @Create 2018/12/18 16:51
 * @Author sanbo
 */
public class TPUtils {

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static boolean isMainThreadA() {
        return Looper.getMainLooper() == Looper.myLooper();
    }


    public static boolean isMainThreadB() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }

    public static boolean isMainProcess(Context context) {
        try {
            context = EContextHelper.getContext(context);
            if (context != null) {
                return context.getPackageName().equals(getCurrentProcessName(context));
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static String getCurrentProcessName(Context context) {
        try {
            context = EContextHelper.getContext(context);
            if (context != null) {
                int pid = android.os.Process.myPid();
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                    if (info.pid == pid) {
                        return info.processName;
                    }
                }
            }
        } catch (Throwable e) {
        }
        return "";
    }
    /**
     * 获取Appkey. 优先级：内存==>SP==>XML
     *
     * @param context
     * @return
     */
    public static String getAppKey(Context context) {
        Context cxt = EContextHelper.getContext(context);
        String appkey = EGContext.APP_KEY_VALUE;
        if (!TextUtils.isEmpty(appkey)) {
            return appkey;
        }
        if (cxt == null) {
            return "";
        }
        appkey = SPHelper.getDefault(context).getString(EGContext.SP_APP_KEY,"");
        if (!TextUtils.isEmpty(appkey)) {
            return appkey;
        }
        try {
            ApplicationInfo appInfo = cxt.getApplicationContext().getPackageManager()
                    .getApplicationInfo(cxt.getPackageName(), PackageManager.GET_META_DATA);
            appkey = appInfo.metaData.getString(EGContext.XML_METADATA_APPKEY);
            if (!TextUtils.isEmpty(appkey)) {
                return appkey;
            }
        } catch (Throwable e) {
        }
        return appkey;
    }

    /**
     * 渠道优先级: xml>内存>SP
     *
     * @param context
     * @return
     */
    public static String getAppChannel(Context context) {
        String channel = "";
        try {
            Context cxt = EContextHelper.getContext(context);
            if (cxt == null) {
                return channel;
            }
            ApplicationInfo appInfo = context.getApplicationContext().getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            channel = appInfo.metaData.getString(EGContext.XML_METADATA_CHANNEL);
            if (!TextUtils.isEmpty(channel)) {
                return channel;
            }
        } catch (Throwable e) {
        }
        if (!TextUtils.isEmpty(EGContext.APP_CHANNEL_VALUE)) {
            return EGContext.APP_CHANNEL_VALUE;
        }
        channel = SPHelper.getDefault(context).getString(EGContext.SP_APP_CHANNEL,"");
        if (!TextUtils.isEmpty(channel)) {
            return channel;
        }
        return channel;
    }

    /**
     * 判断是否需要上传数据
     *
     * @param context
     */
    //TODO
//    public synchronized void tryPostDataToServer(Context context) {
//        try {
//            if (!NetworkUtils.isNetworkAlive(context)) {
//                return;
//            }
//            DevSPUtils spUtil = DevSPUtils.getInstance(context);
//            if (spUtil.getRequestState() != 0) {
//                if (Config.EG_DEBUG) {
//                    ELog.e(PubConfigInfo.DEVICE_TAG, "not ready status.");
//                }
//                return;
//            }
//            // Log.i("sanbo",SystemUtils.getCurrentProcessName(context) + "----[" + android.os.Process.myTid() + "]---[" +
//            // Thread.currentThread().getName() + "]---" + "请求状态====>" + spUtil.getRequestState());
//            File dir = context.getFilesDir();
//            File f = new File(dir, PubConfigInfo.DEV_UPLOAD_PROC_NAME);
//            long now = System.currentTimeMillis();
//            if (f.exists()) {
//                long time = f.lastModified();
//                long dur = now - time;
//                if (Math.abs(dur) <= PubConfigInfo.LONG_INVALIED_TIME) {
//                    return;
//                } else {
//                }
//            } else {
//                f.createNewFile();
//                f.setLastModified(now);
//            }
//
//            boolean isDurOK = (now - spUtil.getLastQuestTime()) > PubConfigInfo.LONG_INVALIED_TIME;
//            // Log.i("sanbo","---------即将发送数据isDurOK："+isDurOK);
//            if (isDurOK) {
//                f.setLastModified(now);
//                if (spUtil.getFailedNumb() == 0) {
//                    upload(context);
//                    if (Config.EG_DEBUG) {
//                        ELog.d(PubConfigInfo.DEVICE_TAG, " upload.");
//                    }
//                } else if (spUtil.getFailedNumb() == 1
//                        && System.currentTimeMillis() - spUtil.getFailedTime() > spUtil.getRetryTime()) {
//                    upload(context);
//                    if (Config.EG_DEBUG) {
//                        ELog.d(PubConfigInfo.DEVICE_TAG, "retry upload ");
//                    }
//
//                } else if (spUtil.getFailedNumb() == 2
//                        && System.currentTimeMillis() - spUtil.getFailedTime() > spUtil.getRetryTime()) {
//                    upload(context);
//                    if (Config.EG_DEBUG) {
//                        ELog.d(PubConfigInfo.DEVICE_TAG, "retry upload...");
//
//                    }
//
//                } else if (spUtil.getFailedNumb() == 3) {
//                    spUtil.setFailedNumb(0);
//                    spUtil.setLastQuestTime(System.currentTimeMillis());
//                    spUtil.setFailedTime(0);
//                } else if (spUtil.getFailedNumb() == -1) {
//                    return;
//                }
//            }
//        } catch (Throwable e) {
//        }
//    }

}
