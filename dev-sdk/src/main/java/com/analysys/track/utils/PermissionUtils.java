package com.analysys.track.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;


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
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    context = EContextHelper.getContext(context);
                    //  if (context instanceof Application) {
                    //     context = ((Application) context).getBaseContext();
                    // }
                    //   //这样写应该也可以
                    // if (context instanceof Application) {
                    //  context = ((Application) context).getApplicationContext();
                    //  }
                    if (context instanceof ContextWrapper) {
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    int rest = (Integer) ClazzUtils.g().invokeObjectMethod(context, "checkSelfPermission", new Class[]{String.class}, new Object[]{permission});
                    result = rest == PackageManager.PERMISSION_GRANTED;
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                    result = false;
                }
            } else {
                // PackageManager pm = context.getPackageManager();
                //     if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
                //
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return result;
    }


    /**
     * 是否可以使用UsageStatsManager。 判断思路: 0. xml中是否声明权限 1. 是否授权
     *
     * @param context
     * @return
     */
    @TargetApi(21)
    public static boolean canUseUsageStatsManager(Context context) {
        if (context == null) {
            return false;
        }
        if (!AndroidManifestHelper.isPermissionDefineInManifest(context, "android.permission.PACKAGE_USAGE_STATS")) {
            return false;
        }
        // AppOpsManager.OPSTR_GET_USAGE_STATS 对应页面是 "有权查看使用情况的应用"
        if (!hasPermission(context, AppOpsManager.OPSTR_GET_USAGE_STATS)) {
            return false;
        }

        return true;
    }

    /**
     * 是否授权
     *
     * @param context
     * @param op
     * @return
     */
    @SuppressLint("WrongConstant")
    private static boolean hasPermission(Context context, String op) {
        try {
            if (context == null || TextUtils.isEmpty(op)) {
                return false;
            }
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    0);
            AppOpsManager appOpsManager = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                appOpsManager = (AppOpsManager) context.getApplicationContext().getSystemService("appops");
                if (appOpsManager != null) {
                    int mode = appOpsManager.checkOpNoThrow(op, applicationInfo.uid, applicationInfo.packageName);
                    // return mode == AppOpsManager.MODE_ALLOWED;
                    return mode == 0;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }
}
