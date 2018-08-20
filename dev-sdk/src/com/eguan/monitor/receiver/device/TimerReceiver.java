package com.eguan.monitor.receiver.device;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.eguan.Constants;
import com.eguan.monitor.AccessibilityOCManager;
import com.eguan.monitor.fangzhou.service.EgAccessibilityService;
import com.eguan.imp.AppProcessManager;
import com.eguan.imp.OCInfoManager;
import com.eguan.imp.WBGManager;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.SPHodler;
import com.eguan.utils.commonutils.SystemUtils;
import com.eguan.utils.netutils.DevInfoUpload;
import com.eguan.utils.thread.EGQueue;
import com.eguan.utils.thread.SafeRunnable;

import java.io.File;

/**
 * 定时器，五秒轮询该广播。 1\记录应用打开关闭情况; 2\地理位置获取逻辑控制； 3\数据上传逻辑控制
 */
public class TimerReceiver extends BroadcastReceiver {
    private static final String TAG = "TimerReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    // OC处理
                    process(context);
                    // WiFi信息，基站信息，经纬度存储整合
                    WBGManager.getInstance(context).getWBGInfo();
                    // upload上传
                    DevInfoUpload.getInstance().StartpostData(context);
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        });
    }

    private void process(Context context) {
        try {
            SystemUtils.startJobService(context);

            // 当处于锁屏,屏幕点亮.处理ST为锁屏类型OC数据,保全数组
            if (isKeyguardRestrictedInputMode(context)) {
                OCInfoManager.getInstance(context).filterInsertOCInfo(Constants.CLOSE_SCREEN, false);
                return;
            }
            // root设备或者(设备系统本小于LOLLIPOP且允许了GET_TASKS权限)
            if (isRoot() || (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && isPermitGetTask(context))) {
                // 使用getRunningTask获取OC信息
                OCInfoManager.getInstance(context).getOCInfo();
            } else if (isAccessibilitySettingsOn(context)) {
                // 使用AccessibilityService获取OC信息
                AccessibilityOCManager.getInstance(context).setEnable();
            } else {
                // 系统高于4.4且低于7.0,且并未开启AcessibilityService
                // if (Constants.FLAG_DEBUG_INNER) {
                // EgLog.d("inside TimerReceiver.process. 识别到系统版本属于5.0以上");
                // }
                AppProcessManager.getInstance(context).appProcessInfo();
            }
            SPHodler.getInstance(context).setEndTime(System.currentTimeMillis());
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    private boolean isPermitGetTask(Context mContext) {
        return SystemUtils.checkPermission(mContext, "android.permission.GET_TASKS");
    }

    public boolean isRoot() {
        boolean root = false;
        try {
            root = !((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists()));
        } catch (Exception e) {
        }
        return root;
    }

    /**
     * 是否解锁 锁屏true 开屏false
     *
     * @param context
     * @return
     */
    private boolean isKeyguardRestrictedInputMode(Context context) {
        KeyguardManager manager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean keyString = manager.inKeyguardRestrictedInputMode();

        return keyString;
    }

    /**
     * 此方法用来判断当前应用的辅助功能服务是否开启
     *
     * @param context
     * @return
     */
    public static boolean isAccessibilitySettingsOn(Context context) {
        String accessibilityServiceName = new String(
                context.getPackageName() + "/" + EgAccessibilityService.class.getCanonicalName());
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            EgLog.d(TAG, e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String services = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (services != null) {
                return services.toLowerCase().contains(accessibilityServiceName.toLowerCase());
            }
        }
        return false;
    }
}
