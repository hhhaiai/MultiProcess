package com.analysys.track.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @Copyright 2019 sanbo Inc. All rights reserved.
 * @Description: 权限检查
 * @Version: 1.0
 * @Create: 2019-08-04 17:27:00
 * @author: sanbo
 */
public class PermissionUtils {

    /**
     * 申请权限
     *
     * @param activity
     * @param permissionList
     * @param requestCode
     */
    @TargetApi(23)
    public static void reqPermission(Activity activity, String[] permissionList, int requestCode) {

        if (activity == null || permissionList == null || permissionList.length <= 0) {
            return;
        }
        List<String> reqs = new CopyOnWriteArrayList<String>();
        for (String permission : permissionList) {
            if (!checkPermission(activity, permission)) {
                reqs.add(permission);
            }
        }
        if (reqs.size() > 0) {
//            reqs.stream().toArray(String[]::new);
            activity.requestPermissions(reqs.toArray(new String[reqs.size()]), requestCode);
        }
    }

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
            context = EContextHelper.getContext(context);
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            }
            if (context != null) {
                if (!AndroidManifestHelper.isPermissionDefineInManifest(context, permission)) {
                    return false;
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    int rest = (Integer) ClazzUtils.g().invokeObjectMethod(context, "checkSelfPermission", new Class[]{String.class}, new Object[]{permission});
                    result = (rest == PackageManager.PERMISSION_GRANTED);
                } else {
                    result = true;
                }
            }
        } catch (Throwable igone) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(igone);
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
        try {
            context = EContextHelper.getContext(context);
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
        } catch (Throwable igone) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(igone);
            }
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
