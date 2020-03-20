package com.analysys.track.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;

import java.lang.reflect.Method;


/**
 * @Copyright 2019 sanbo Inc. All rights reserved.
 * @Description: 权限检查
 * @Version: 1.0
 * @Create: 2019-08-04 17:27:00
 * @author: sanbo
 */
public class PermissionUtils {
    /**
     * 检查权限 权限申请被拒绝检测返回false，权限申请通过检测返回true
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                int rest = (Integer) ClazzUtils.invokeObjectMethod(context, "checkSelfPermission", new Class[]{String.class}, new Object[]{permission});
                result = rest == PackageManager.PERMISSION_GRANTED;
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
                result = false;
            }
        } else {
//            PackageManager pm = context.getPackageManager();
//            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            result = true;
//            }
        }
        return result;
    }
}
