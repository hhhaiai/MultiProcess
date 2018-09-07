package com.eguan.utils;

import java.util.Arrays;

import com.eguan.EDContext;

import android.Manifest.permission;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * @Copyright © 2018 sanbo Inc. All rights reserved.
 * @Description: 辅助功能基础功能类
 * @Version: 1.0
 * @Create: 2018年3月8日 上午11:34:04
 * @Author: sanbo
 */
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
            if (EDContext.FLAG_DEBUG_INNER) {
                L.e("AccessibilityHelper.isAccessibilitySettingsOn  the param is null!");
            }
            return false;
        }
        int accessibilityEnabled = 0;
        /*
         * 1.确保类型是辅助功能
         */
        if (!AndroidManifestHelper.isSubClass(clazz, AccessibilityService.class)) {
            if (EDContext.FLAG_DEBUG_INNER) {
                L.e("请检查传入参数是辅助功能的类!");
            }
            return false;
        }
        /*
         * 2.确认xml中声明该类。声明权限
         */
        if (!AndroidManifestHelper.isServiceDefineInManifest(context, clazz)) {
            if (EDContext.FLAG_DEBUG_INNER) {
                L.e("请在AndroidManifest中声明服务:" + clazz.getCanonicalName());
            }
            return false;
        }
        if (!AndroidManifestHelper.isPermissionDefineInManifest(context, permission.BIND_ACCESSIBILITY_SERVICE)) {
            if (EDContext.FLAG_DEBUG_INNER) {
                L.e("请在AndroidManifest中声明权限: android.permission.BIND_ACCESSIBILITY_SERVICE");
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
                String[] tempSS = settingValue.split(":");
                if (tempSS.length > 0) {
                    if (Arrays.asList(tempSS).contains(service)) {
                        return true;
                    }
                }
            } else {
                if (settingValue.equalsIgnoreCase(service)) {
                    return true;
                }
            }
        } else {
            if (EDContext.FLAG_DEBUG_INNER) {
                L.v("没有开启的辅助功能应用");
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
