package com.analysys.track.utils;

import android.Manifest.permission;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.track.internal.Content.EGContext;

import java.util.Arrays;


public class AccessibilityHelper {
    /**
     * 检测辅助功能是否开启<br>
     *
     * @param context
     * @param clazz
     * @return boolean
     */
    public static boolean isAccessibilitySettingsOn(Context context, Class<?> clazz) {
        if (context == null || clazz == null) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("AccessibilityHelper.isAccessibilitySettingsOn  the param is null!");
            }
            return false;
        }
        int accessibilityEnabled = 0;
        /*
         * 1.确保类型是辅助功能
         */
        if (!AndroidManifestHelper.isSubClass(clazz, AccessibilityService.class)) {
            // if (EGContext.FLAG_DEBUG_INNER) {
            // L.e("请检查传入参数是辅助功能的类!");
            // }
            return false;
        }
        /*
         * 2.确认xml中声明该类。声明权限
         */
        if (!AndroidManifestHelper.isServiceDefineInManifest(context, clazz)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("please define  service [" + clazz.getCanonicalName() + "]  in AndroidManifest.xml! ");
            }
            return false;
        }
        if (!AndroidManifestHelper.isPermissionDefineInManifest(context, permission.BIND_ACCESSIBILITY_SERVICE)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("please check android.permission.BIND_ACCESSIBILITY_SERVICEAndroid about service["
                        + clazz.getCanonicalName() + "] in AndroidManifest.xml !");
            }
            return false;
        }

        // 数据格式:com.example.bira/com.bira.helper.MyAccessibilityService
        final String service = context.getPackageName() + "/" + clazz.getCanonicalName();
        /*
         * 3.确定有辅助功能服务
         */
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }

        /*
         * 4.确定服务列表中是否勾选
         */
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (TextUtils.isEmpty(settingValue)) {
                return false;
            }
            if (settingValue.contains(":")) {
                String[] tempStringArr = settingValue.split(":");
                if (tempStringArr.length > 0) {
                    if (Arrays.asList(tempStringArr).contains(service)) {
                        return true;
                    }
                }
            } else {
                if (settingValue.equalsIgnoreCase(service)) {
                    return true;
                }
            }
        } else {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("please make sure  accessibility  Enabled!");
            }
        }
        return false;
    }

    /**
     * 打开辅助功能
     *
     * @param context
     */
    public static void openAccessibilityService(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
