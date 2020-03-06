package com.analysys.track.internal.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.PatchHelper;

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
    public void process(final Context context, final Intent intent) {

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.d(BuildConfig.tag_recerver + intent);
        }
        if (intent == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String packageName = getPkgName(intent);
            if (TextUtils.isEmpty(packageName)) {
                return;
            }

            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST,
                    EGContext.TIME_SECOND * 2, currentTime)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(BuildConfig.tag_snap, "安装app:" + packageName);
                }
                AppSnapshotImpl.getInstance(context)
                        .processAppModifyMsg(packageName,
                                Integer.parseInt(EGContext.SNAP_SHOT_INSTALL),
                                EGContext.FILES_SYNC_SNAP_ADD_BROADCAST);

            } else {
//                if (EGContext.FLAG_DEBUG_INNER) {
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
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(BuildConfig.tag_snap, "卸载app:" + packageName);
                }
                AppSnapshotImpl.getInstance(context)
                        .processAppModifyMsg(packageName,
                                Integer.parseInt(EGContext.SNAP_SHOT_UNINSTALL),
                                EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST);
            } else {
//                if (EGContext.FLAG_DEBUG_INNER) {
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
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(BuildConfig.tag_snap, "更新app:" + packageName);
                }
                AppSnapshotImpl.getInstance(context)
                        .processAppModifyMsg(packageName,
                                Integer.parseInt(EGContext.SNAP_SHOT_UPDATE),
                                EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST);
            } else {
//                if (EGContext.FLAG_DEBUG_INNER) {
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
            SystemUtils.runOnWorkThread(new Runnable() {
                @Override
                public void run() {
                    DeviceImpl.getInstance(context).processBattery(intent);
                }
            });
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            MessageDispatcher.getInstance(context).initModule();
        } else if (EGContext.ACTION_MTC_LOCK.equals(intent.getAction())) {
            EGContext.snap_complete = true;
        } else if (EGContext.ACTION_UPDATE_POLICY.equals(intent.getAction())) {
            PolicyImpl.getInstance(EContextHelper.getContext(context)).updatePolicyForReceiver(intent);
        } else if (EGContext.ACTION_UPDATE_CLEAR.equals(intent.getAction())) {
            boolean isStopLoop = intent.getBooleanExtra(EGContext.ISSTOP_LOOP, false);
            boolean isInLoop = intent.getBooleanExtra(EGContext.ISINLOOP, true);
            stopAndClearData(EContextHelper.getContext(context), isInLoop, isStopLoop);
        }
    }

    private void stopAndClearData(Context context, boolean isInLoop, boolean isStopLoop) {
        try {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.d(BuildConfig.tag_cutoff, "[清除数据]");
            }

            PatchHelper.clearPatch(context);
//            if (isStopLoop) {
//                if (EGContext.FLAG_DEBUG_INNER) {
//                    ELOG.d(BuildConfig.tag_cutoff, "[停止轮训]");
//                }
//                MessageDispatcher.getInstance(context).quit();
//            }
//            // 热修复保证能下发修复成功，宿主包不删除，由热修复包考虑删除
//            File hotfixDir = new File(context.getFilesDir(), EGContext.HOTFIX_CACHE_HOTFIX_DIR);
//            FileUitls.getInstance(context).deleteFile(hotfixDir);
//            //patch 维持原样
//            File patchDir = new File(context.getFilesDir(), EGContext.PATCH_CACHE_DIR);
//            FileUitls.getInstance(context).deleteFile(patchDir);
//            PolicyImpl.getInstance(EContextHelper.getContext()).clear();
//            // 清除本地缓存
//            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_VERSION, "");
//            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_SIGN, "");
//            SPHelper.setStringValue2SP(EContextHelper.getContext(), UploadKey.Response.PatchResp.PATCH_METHODS, "");


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
