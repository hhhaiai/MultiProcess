package com.analysys.track.internal;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.FileUtils;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.reflectinon.Reflecer;
import com.analysys.track.work.CrashHandler;
import com.analysys.track.work.MessageDispatcher;

import java.lang.ref.WeakReference;

public class AnalysysInternal {
    private static boolean hasInit = false;
    private WeakReference<Context> mContextRef = null;

    private AnalysysInternal() {
        Reflecer.init();// 必须调用-----
    }

    public static void setInitFalse() {
        hasInit = false;
    }

    public static AnalysysInternal getInstance(Context context) {
        if (Holder.instance.mContextRef == null) {
            Holder.instance.mContextRef = new WeakReference<Context>(EContextHelper.getContext(context));
        }
        return Holder.instance;
    }

    /**
     * 初始化函数,可能为耗时操作的，判断是否主线程，需要开子线程做
     *
     * @param key
     * @param channel
     */
    public void initEguan(final String key, final String channel) {
        try {
            //单进程内防止重复注册
            if (hasInit) {
                return;
            }
            //防止影响宿主线程中的任务
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    init(key, channel);
                }
            });
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
    }

    /**
     * key支持参数设置、XML文件设置，
     * 参数设置优先级大于XML设置
     *
     * @param key
     * @param channel
     */
    private void init(String key, String channel) {
        hasInit = true;
        // 0.首先检查是否有Context
        Context ctx = EContextHelper.getContext(mContextRef == null ? null : mContextRef.get());
        if (ctx == null) {
            return;
        }
//        if (mContextRef == null) {
//            mContextRef = new WeakReference<Context>(ctx);
//        }
        SystemUtils.updateAppkeyAndChannel(ctx, key, channel);//update sp

        // 1. 设置错误回调
        CrashHandler.getInstance().setCallback(null);//不依赖ctx
        //2.初始化加密
        EncryptUtils.init(ctx);
        // 3.初始化多进程
        initSupportMultiProcess(ctx);
        // 4. 启动工作机制
        MessageDispatcher.getInstance(ctx).startService();
        // 4. 根据屏幕调整工作状态
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            boolean isScreenOn = pm.isScreenOn();
            // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
            if (!isScreenOn) {
                ReceiverUtils.getInstance().setWork(false);
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
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_UPLOAD, EGContext.TIME_SYNC_UPLOAD);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_APPSNAPSHOT, EGContext.SNAPSHOT_CYCLE);
            if (Build.VERSION.SDK_INT < 21) {
                FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SYNC_DEFAULT);
            } else {
                FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SYNC_OC_OVER_5);
            }
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_LOCATION, EGContext.TIME_SYNC_LOCATION);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_SP_WRITER, EGContext.TIME_SYNC_SP);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_SCREEN_OFF_BROADCAST, EGContext.TIME_SYNC_BROADCAST);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_SCREEN_ON_BROADCAST, EGContext.TIME_SYNC_BROADCAST);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST, EGContext.TIME_SYNC_DEFAULT);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST, EGContext.TIME_SYNC_DEFAULT);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST, EGContext.TIME_SYNC_DEFAULT);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_BOOT_BROADCAST, EGContext.TIME_SYNC_DEFAULT);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_BATTERY_BROADCAST, EGContext.TIME_SYNC_DEFAULT);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
    }

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }
}
