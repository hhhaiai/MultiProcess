package com.device.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;

import java.util.List;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 方便跳转到 设置辅助功能 页面
 * @Version: 1.0
 * @Create: 2019-11-11 16:21:43
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class USMUtils {
    /**
     * 是否有打开辅助功能的选项
     *
     * @param context
     * @return
     */
    public static boolean isOption(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PackageManager packageManager = context.getApplicationContext()
                    .getPackageManager();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }
        return false;
    }
    /**
     * 是否打开了辅助功能
     *
     * @param context
     * @return
     */
    public static boolean isSwitch(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = null;
            usageStatsManager = (UsageStatsManager) context.getApplicationContext()
                    .getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = null;
            if (usageStatsManager != null) {
                queryUsageStats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_BEST, 0, ts);
            }
            if (queryUsageStats != null && !queryUsageStats.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void openUSMSetting(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
        }
    }
}