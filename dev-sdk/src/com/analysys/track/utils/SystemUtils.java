package com.analysys.track.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.text.TextUtils;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 系统工具方法: 屏幕状态判断/是否授权/用户权限判断/应用版本获取/日期获取/随机数获取
 * @Version: 1.0
 * @Create: Mar 6, 2019 6:35:34 PM
 * @Author: sanbo
 */
public class SystemUtils {
    /**
     * 生成n个不同的随机数，且随机数区间为[0,10)
     * 
     * @param n
     * @return
     */
    public static ArrayList getDiffNO(int n) {
        // 生成 [0-n) 个不重复的随机数
        // list 用来保存这些随机数
        ArrayList list = new ArrayList();
        Random rand = new Random(30);
        boolean[] bool = new boolean[n];
        int num = 0;
        for (int i = 0; i < n; i++) {
            do {
                // 如果产生的数相同继续循环
                num = rand.nextInt(n);
            } while (bool[num]);
            bool[num] = true;
            list.add(num);
        }
        return list;
    }

    /**
     * 获取日期
     */
    public static String getDay() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        return time;
    }

    /**
     * 是否点亮屏幕
     *
     * @param context
     * @return true: 屏幕点亮 false: 屏幕熄灭
     */
    @SuppressWarnings("deprecation")
    public static boolean isScreenOn(Context context) {
        PowerManager powerManager =
            (PowerManager)context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        // 锁屏true 开屏false
        return powerManager.isScreenOn();
    }

    /**
     * 是否锁屏
     *
     * @param context
     * @return true:锁屏,有输入密码解锁或者锁屏壁纸页面 false: 进入系统中的任何页面
     */
    public static boolean isScreenLocked(Context context) {

        KeyguardManager manager =
            (KeyguardManager)context.getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        // 锁屏true 开屏false
        boolean inKeyguardRestrictedInputMode = manager.inKeyguardRestrictedInputMode();
        return inKeyguardRestrictedInputMode;
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

        return false;
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
            ApplicationInfo applicationInfo =
                context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager)context.getApplicationContext().getSystemService("appops");
            int mode = appOpsManager.checkOpNoThrow(op, applicationInfo.uid, applicationInfo.packageName);
            // return mode == AppOpsManager.MODE_ALLOWED;
            return mode == 0;
        } catch (Throwable e) {
        }
        return false;
    }

    public static String getApplicationName(Context ctx, String packageName) {
        String appName = "";
        try {
            PackageManager pm = ctx.getPackageManager();
            appName = (String)pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
        }
        return appName;
    }

    public static String getApplicationVersion(Context ctx, String packageName) {
        String appVer = "";
        PackageManager pm = ctx.getPackageManager();
        try {
            appVer =
                pm.getPackageInfo(packageName, 0).versionName + "|" + pm.getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return appVer;
    }
}
