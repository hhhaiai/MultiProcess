package com.analysys.track.internal.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 广播处理类
 * @Version: 1.0
 * @Create: 2019-08-07 17:45:32
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class ReceiverImpl {


    /**
     * 处理接收到的广播
     *
     * @param context
     * @param intent
     */
    public void process(Context context, final Intent intent) {

        if (EGContext.DEBUG_RECEIVER) {
            ELOG.d(BuildConfig.tag_recerver + intent.toString());
        }

        context = context.getApplicationContext();
        long currentTime = System.currentTimeMillis();

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String packageName = getPkgName(intent);
            if (TextUtils.isEmpty(packageName)) {
                return;
            }

            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST,
                    EGContext.TIME_SECOND * 2, currentTime)) {
                if (EGContext.DEBUG_RECEIVER) {
                    ELOG.i(BuildConfig.tag_snap, "安装app:" + packageName);
                }
                AppSnapshotImpl.getInstance(context)
                        .processAppModifyMsg(packageName,
                                Integer.parseInt(EGContext.SNAP_SHOT_INSTALL),
                                EGContext.FILES_SYNC_SNAP_ADD_BROADCAST);

            } else {
//                if (EGContext.DEBUG_RECEIVER) {
//                    ELOG.v(BuildConfig.tag_snap, "安装app:" + packageName + "---->多进程中断");
//                }
                return;
            }


        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            String packageName = getPkgName(intent);

            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST,
                    EGContext.TIME_SECOND * 2, currentTime)) {
                if (EGContext.DEBUG_RECEIVER) {
                    ELOG.i(BuildConfig.tag_snap, "卸载app:" + packageName);
                }
                AppSnapshotImpl.getInstance(context)
                        .processAppModifyMsg(packageName,
                                Integer.parseInt(EGContext.SNAP_SHOT_UNINSTALL),
                                EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST);
            } else {
//                if (EGContext.DEBUG_RECEIVER) {
//                    ELOG.v(BuildConfig.tag_snap, "卸载app:" + packageName + "---->多进程中断");
//                }
                return;
            }

        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            String packageName = getPkgName(intent);

            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST,
                    EGContext.TIME_SECOND * 2, currentTime)) {
                if (EGContext.DEBUG_RECEIVER) {
                    ELOG.i(BuildConfig.tag_snap, "更新app:" + packageName);
                }
                AppSnapshotImpl.getInstance(context)
                        .processAppModifyMsg(packageName,
                                Integer.parseInt(EGContext.SNAP_SHOT_UPDATE),
                                EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST);
            } else {
//                if (EGContext.DEBUG_RECEIVER) {
//                    ELOG.v(BuildConfig.tag_snap, "更新app:" + packageName + "---->多进程中断");
//                }
                return;
            }

        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
//            //  7.x以上版本保持心跳
//            if (Build.VERSION.SDK_INT >= 24) {
//                return;
//            }

            OCImpl.getInstance(context).processOCWhenScreenChange(true);

        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            //  7.x以上版本 不操作
            if (Build.VERSION.SDK_INT >= 24) {
                return;
            }
            OCImpl.getInstance(context).processOCWhenScreenChange(false);
        } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            DeviceImpl.getInstance(context).processBattery(intent);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            MessageDispatcher.getInstance(context).initModule();
        } else if (EGContext.ACTION_MTC_LOCK.equals(intent.getAction())) {
            EGContext.snap_complete = true;
        } else if (EGContext.ACTION_UPDATE_POLICY.equals(intent.getAction())) {
            final Context finalContext = context;
            EThreadPool.post(new Runnable() {
                @Override
                public void run() {
                    PolicyImpl.getInstance(finalContext).updatePolicyForReceiver(intent);
                }
            });
        } else if (EGContext.ACTION_UPDATE_CLEAR.equals(intent.getAction())) {
            final Context finalContext = context;
            EThreadPool.post(new Runnable() {
                @Override
                public void run() {
                    boolean isStopLoop = intent.getBooleanExtra(EGContext.ISSTOP_LOOP, false);
                    boolean isInLoop = intent.getBooleanExtra(EGContext.ISINLOOP, true);
                    stopAndClearData(finalContext, isInLoop, isStopLoop);
                }
            });
        }
    }

    private void stopAndClearData(Context context, boolean isInLoop, boolean isStopLoop) {
        try {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.d(BuildConfig.tag_cutoff, "被动初始化调试设备 清除数据");
            }

            if (isStopLoop) {
                MessageDispatcher.getInstance(context).quit();
            }

            File dexDir = new File(context.getFilesDir(), EGContext.HOTFIX_FILE_DIR);
            if (dexDir.exists() && dexDir.isDirectory()) {
                File[] files = dexDir.listFiles();
                for (File file : files) {
                    if (isInLoop) {
                        file.deleteOnExit();
                    } else {
                        file.delete();
                    }
                }
            }
            File odexdDir = new File(context.getFilesDir(), EGContext.HOTFIX_CACHE_DIR);
            if (odexdDir.exists() && odexdDir.isDirectory()) {
                File[] files = odexdDir.listFiles();
                for (File file : files) {
                    if (isInLoop) {
                        file.deleteOnExit();
                    } else {
                        file.delete();
                    }
                }
            }

            PolicyImpl.getInstance(EContextHelper.getContext()).clear();
            // 清除本地缓存
            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_VERSION, "");
            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_SIGN, "");
            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_METHODS, "");
        } catch (Throwable e) {
        }
    }

    /**
     * 当收到安装、卸载、更新的广播时.会收到dat内容如下: <code>package:com.sollyu.xposed.hook.model</code>
     */
    private String getPkgName(Intent intent) {
        String packageName = "";
        if (intent == null) {
            return packageName;
        }
        String data = intent.getDataString();
        if (!TextUtils.isEmpty(data) && data.startsWith(DATA_APK_STATUS_UPDATE)) {
            packageName = data.replace(DATA_APK_STATUS_UPDATE, "");
        }
        return packageName;
    }

    private static class HOLDER {
        private static ReceiverImpl INSTANCE = new ReceiverImpl();
    }

    private ReceiverImpl() {
    }

    public static ReceiverImpl getInstance() {
        return HOLDER.INSTANCE;
    }

    // 当收到安装、卸载、更新的广播时的data前缀
    private final String DATA_APK_STATUS_UPDATE = "package:";
}
