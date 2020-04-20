package com.device.utils;

import android.app.usage.NetworkStats;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

public class Aha {
    /**
     * 根据包名获取uid
     *
     * @param context     上下文
     * @param packageName 包名
     */
    public static int getUidByPackageName(Context context, String packageName) {
        int uid = -1;
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            uid = packageInfo.applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return uid;
    }


    public static void getUid(Context context, IdCaller caller) {
        PackageManager packageManager = context.getPackageManager();
        final List<PackageInfo> applicationInfoList = packageManager.getInstalledPackages(0);
        EL.i("applicationInfoList:" + applicationInfoList.size());
        NetworkStats networkStats = null;
        for (PackageInfo info : applicationInfoList) {
            try {
                PackageInfo pack = packageManager.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = pack.requestedPermissions;
                if (requestedPermissions == null) {
                    continue;
                }
                int uid = info.applicationInfo.uid;
                String pkgName = info.applicationInfo.packageName;
                String appName = info.applicationInfo.loadLabel(packageManager).toString();
                if (caller != null) {
                    caller.SeeUid(uid, appName, pkgName);
                }

            } catch (Throwable exception) {
                EL.e(exception);
            }
        }
    }

}
