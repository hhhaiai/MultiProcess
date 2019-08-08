package com.analysys.track.internal.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MultiProcessChecker;

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
    public void process(Context context, Intent intent) {

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.d(" receiver intent: " + intent.toString());
        }

        context = context.getApplicationContext();
        long currentTime = System.currentTimeMillis();

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            String packageName = getPkgName(intent);
            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST,
                    EGContext.TIME_SYNC_DEFAULT, currentTime)) {
                MessageDispatcher.getInstance(context).appChangeReceiver(packageName,
                        Integer.parseInt(EGContext.SNAP_SHOT_INSTALL), currentTime);

            } else {
                return;
            }


        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            String packageName = getPkgName(intent);

            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST,
                    EGContext.TIME_SYNC_DEFAULT, currentTime)) {

                MessageDispatcher.getInstance(context).appChangeReceiver(packageName,
                        Integer.parseInt(EGContext.SNAP_SHOT_UNINSTALL), currentTime);
            } else {
                return;
            }

        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            String packageName = getPkgName(intent);

            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST,
                    EGContext.TIME_SYNC_DEFAULT, currentTime)) {
                MessageDispatcher.getInstance(context).appChangeReceiver(packageName,
                        Integer.parseInt(EGContext.SNAP_SHOT_UPDATE), currentTime);
            } else {
                return;
            }

        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            //  7.x以上版本保持心跳
            if (Build.VERSION.SDK_INT >= 24) {
                MessageDispatcher.getInstance(context).sendMessage();
                return;
            }
            // 设置开锁屏的flag 用于补数逻辑
            EGContext.SCREEN_ON = true;
            MessageDispatcher.getInstance(context).screenStatusHandle(true);
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            //  7.x以上版本 不操作
            if (Build.VERSION.SDK_INT >= 24) {
                return;
            }
            EGContext.SCREEN_ON = false;
            MessageDispatcher.getInstance(context).screenStatusHandle(false);
        } else if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_BATTERY_BROADCAST,
                    EGContext.TIME_SYNC_DEFAULT, currentTime)) {
                DeviceImpl.getInstance(context).processBattery(intent);
            } else {
                return;
            }
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_BOOT_BROADCAST,
//                    EGContext.TIME_SYNC_DEFAULT, currentTime)) {
//                MultiProcessChecker.getInstance().setLockLastModifyTime(context, EGContext.FILES_SYNC_BOOT_BROADCAST, currentTime);
//            } else {
//                return;
//            }
            MessageDispatcher.getInstance(context).startService();
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
