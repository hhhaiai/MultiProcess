package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MultiProcessChecker;

public class AnalysysReceiver extends BroadcastReceiver {
    // 上次结束时间
    public static long mLastCloseTime = 0;
    public static boolean isScreenOnOffBroadCastHandled = false;
    private static boolean isSnapShotAddBroadCastHandled = false;
    private static boolean isSnapShotDeleteBroadCastHandled = false;
    private static boolean isSnapShotUpdateBroadCastHandled = false;
    private static boolean isBatteryBroadCastHandled = false;
    private static boolean isBootBroadCastHandled = false;
    Context mContext;
    String intentPackageAdd = "android.intent.action.intentPackageAdd";
    String intentPackageRemove = "android.intent.action.intentPackageRemove";
    String intentPackageReplace = "android.intent.action.intentPackageReplace";
    String intentScreenOn = "android.intent.action.intentScreenOn";
    String intentScreenOff = "android.intent.action.intentScreenOff";
    // String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    String intentBatteryChanged = "android.intent.action.intentBatteryChanged";
    String intentBootCompleted = "android.intent.action.intentBootCompleted";

    public static AnalysysReceiver getInstance() {
        return AnalysysReceiver.Holder.INSTANCE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null) {
                return;
            }
            String data = intent.getDataString();
            String packageName = "";
            if (!TextUtils.isEmpty(data)) {
                packageName = data.substring(8);
            }
            mContext = context.getApplicationContext();
            long currentTime = System.currentTimeMillis();
            if (intentPackageAdd.equals(intent.getAction())) {
                try {
                    if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST,
                            EGContext.TIME_SYNC_DEFAULT, currentTime)) {
                        MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST, currentTime);
                    } else {
                        return;
                    }
                    if (!isSnapShotAddBroadCastHandled) {
                        isSnapShotAddBroadCastHandled = true;
                    } else {
                        return;
                    }
                    MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName,
                            Integer.parseInt(EGContext.SNAP_SHOT_INSTALL), currentTime);
                } catch (Throwable t) {
                } finally {
                    isSnapShotAddBroadCastHandled = false;
                }

            }
            if (intentPackageRemove.equals(intent.getAction())) {
                try {
                    if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST,
                            EGContext.TIME_SYNC_DEFAULT, currentTime)) {
                        MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST,
                                currentTime);
                    } else {
                        return;
                    }
                    if (!isSnapShotDeleteBroadCastHandled) {
                        isSnapShotDeleteBroadCastHandled = true;
                    } else {
                        return;
                    }
                    MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName,
                            Integer.parseInt(EGContext.SNAP_SHOT_UNINSTALL), currentTime);
                } catch (Throwable t) {
                } finally {
                    isSnapShotDeleteBroadCastHandled = false;
                }
            }
            if (intentPackageReplace.equals(intent.getAction())) {
                try {
                    if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST,
                            EGContext.TIME_SYNC_DEFAULT, currentTime)) {
                        MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST,
                                currentTime);
                    } else {
                        return;
                    }
                    if (!isSnapShotUpdateBroadCastHandled) {
                        isSnapShotUpdateBroadCastHandled = true;
                    } else {
                        return;
                    }
                    MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName,
                            Integer.parseInt(EGContext.SNAP_SHOT_UPDATE), currentTime);
                } catch (Throwable t) {
                } finally {
                    isSnapShotUpdateBroadCastHandled = false;
                }
            }
            if (intentScreenOn.equals(intent.getAction())) {
                if (Build.VERSION.SDK_INT >= 24) {
                    MessageDispatcher.getInstance(mContext).sendMessages();
                    return;
                }
                // 设置开锁屏的flag 用于补数逻辑
                EGContext.SCREEN_ON = true;
                MessageDispatcher.getInstance(mContext).screenStatusHandle(true);
            }
            if (intentScreenOff.equals(intent.getAction())) {
                if (Build.VERSION.SDK_INT >= 24) {
                    return;
                }
                EGContext.SCREEN_ON = false;
                MessageDispatcher.getInstance(mContext).screenStatusHandle(false);
            }
            if (intentBatteryChanged.equals(intent.getAction())) {
                try {
                    if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_BATTERY_BROADCAST,
                            EGContext.TIME_SYNC_DEFAULT, currentTime)) {
                        MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_BATTERY_BROADCAST, currentTime);
                    } else {
                        return;
                    }
                    if (!isBatteryBroadCastHandled) {
                        isBatteryBroadCastHandled = true;
                    } else {
                        return;
                    }
                    DeviceImpl.getInstance(mContext).processBattery(intent);
                } catch (Throwable t) {
                } finally {
                    isBatteryBroadCastHandled = false;
                }
            }
            if (intentBootCompleted.equals(intent.getAction())) {
                try {
                    if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_BOOT_BROADCAST,
                            EGContext.TIME_SYNC_DEFAULT, currentTime)) {
                        MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_BOOT_BROADCAST, currentTime);
                    } else {
                        return;
                    }
                    if (!isBootBroadCastHandled) {
                        isBootBroadCastHandled = true;
                    } else {
                        return;
                    }
                    MessageDispatcher.getInstance(mContext).startService();
                } catch (Throwable t) {
                } finally {
                    isBootBroadCastHandled = false;
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
    }

    private static class Holder {
        private static final AnalysysReceiver INSTANCE = new AnalysysReceiver();
    }

}
