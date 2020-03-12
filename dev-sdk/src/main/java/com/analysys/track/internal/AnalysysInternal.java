package com.analysys.track.internal;

import android.app.AppOpsManager;
import android.app.Application;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.CrashHandler;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.ActivityCallBack;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.OAIDHelper;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

public class AnalysysInternal {
    private static boolean hasInit = false;

    // 初始化反射模快
    private AnalysysInternal() {
    }

    public static AnalysysInternal getInstance(Context context) {
        try {
            // 初始化日志
            ELOG.init(EContextHelper.getContext(context));
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return Holder.instance;
    }

    public static boolean isInit() {
        return hasInit;
    }

    /**
     * 初始化函数,可能为耗时操作的，判断是否主线程，需要开子线程做
     *
     * @param key
     * @param channel
     * @param initType true 主动初始化 false 被动初始化
     */
    public synchronized void initEguan(final String key, final String channel, final boolean initType) {
        // 单进程内防止重复注册
        if (hasInit) {
            return;
        }
        hasInit = true;
        tryEnableUsm();
        // 防止影响宿主线程中的任务
        EThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    init(key, channel, initType);
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                }

            }
        });
    }

    private void tryEnableUsm() {
        SystemUtils.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT > 18) {
                        Context ctx = EContextHelper.getContext();
                        if (ctx != null) {
                            AppOpsManager appOpsManager = (AppOpsManager) ctx.getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
                            HashMap<String, Integer> map = (HashMap<String, Integer>) ClazzUtils.getObjectFieldObject(appOpsManager, "sOpStrToOp");
                            if (map.containsKey(AppOpsManager.OPSTR_GET_USAGE_STATS)) {
                                int code = map.get(AppOpsManager.OPSTR_GET_USAGE_STATS);
                                int uid = Process.myUid();
                                String pkn = ctx.getPackageName();
                                int mode = AppOpsManager.MODE_ALLOWED;
                                ClazzUtils.invokeObjectMethod(
                                        appOpsManager, "setMode",
                                        new Class[]{int.class, int.class, String.class, int.class}
                                        , new Object[]{code, uid, pkn, mode}
                                );
                            }

                        }

                    }
                } catch (Throwable e) {
                }
            }
        });
    }

    /**
     * key支持参数设置、XML文件设置， 参数设置优先级大于XML设置
     *
     * @param key
     * @param channel
     */
    @SuppressWarnings("deprecation")
    private void init(String key, String channel, boolean initType) {

        try {
            //禁止灰色 api logcat
            ClazzUtils.unseal();

            // 检查是否有Context
            Context ctx = EContextHelper.getContext();
            if (ctx == null) {
                return;
            }

            SPHelper.setBooleanValue2SP(ctx, EGContext.KEY_INIT_TYPE, initType);
            Application application = (Application) ctx;
            application.registerActivityLifecycleCallbacks(ActivityCallBack.getInstance());

            SPHelper.setIntValue2SP(ctx, EGContext.KEY_ACTION_SCREEN_ON_SIZE, EGContext.FLAG_START_COUNT + 1);
            SystemUtils.updateAppkeyAndChannel(ctx, key, channel);// updateSnapshot sp

            // 1. 设置错误回调
            CrashHandler.getInstance().setCallback(null);// 不依赖ctx
            // 2.初始化加密
            EncryptUtils.init(ctx);
            // 3.初始化多进程
            initSupportMultiProcess(ctx);
            // 4. 只能注册一次，不能注册多次
            ReceiverUtils.getInstance().registAllReceiver(ctx);
            // 5. 启动工作机制
            MessageDispatcher.getInstance(ctx).initModule();
            // if has no init time, init
            long initTime = SPHelper.getLongValueFromSP(ctx, EGContext.SP_INIT_TIME, 0);
            if (initTime <= 0) {
                SPHelper.setLongValue2SP(ctx, EGContext.SP_INIT_TIME, System.currentTimeMillis());
            }

            ServiceHelper.getInstance(EContextHelper.getContext()).startSelfService();
            // 6. 根据屏幕调整工作状态
            PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                boolean isScreenOn = pm.isScreenOn();
                // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
                if (!isScreenOn) {
                    ReceiverUtils.getInstance().setWork(false);
                }
            }
            // 7.检查加密模块是否正常，false重新初始化
            if (!EncryptUtils.checkEncryptKey(ctx)) {
                EncryptUtils.reInitKey(ctx);
            }
            Log.i(EGContext.LOGTAG_USER, String.format("[%s] init SDK (%s) success! ", SystemUtils.getCurrentProcessName(EContextHelper.getContext()), EGContext.SDK_VERSION));

            PatchHelper.loadsIfExit(ctx);

            clearOldSpFiles();
            if (Build.VERSION.SDK_INT >= 29) {
                OAIDHelper.tryGetOaidAndSave(ctx);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private void clearOldSpFiles() {
        try {
            // 9. 清除以前的SP和DB
            SPHelper.removeSpFiles(EContextHelper.getContext(), EGContext.SP_NAME);
            File file = SPHelper.getNewSharedPrefsFile(EContextHelper.getContext(), "ana_sp_xml");
            if (file.exists() && file.isFile()) {
                file.delete();
            }

            file = EContextHelper.getContext().getDatabasePath("e.data");
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }

        }
    }


    /**
     * 初始化支持多进程
     *
     * @param cxt
     */
    private void initSupportMultiProcess(Context cxt) {
        try {
            if (cxt == null) {
                return;
            }
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_UPLOAD, EGContext.TIME_SYNC_UPLOAD);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_APPSNAPSHOT, EGContext.TIME_HOUR * 3);
            if (Build.VERSION.SDK_INT < 26) {
                MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SECOND * 5);
            } else {
                MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SYNC_OC_OVER_5);
            }
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_LOCATION, EGContext.TIME_SYNC_LOCATION);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_HOTFIX, EGContext.TIME_SECOND * 5);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SP_WRITER, EGContext.TIME_SYNC_SP);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SCREEN_OFF_BROADCAST, EGContext.TIME_SYNC_BROADCAST);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SCREEN_ON_BROADCAST, EGContext.TIME_SYNC_BROADCAST);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST, EGContext.TIME_SECOND * 5);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_BOOT_BROADCAST, EGContext.TIME_SYNC_DEFAULT);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_BATTERY_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_NET, EGContext.TIME_SECOND * 5);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }
}
