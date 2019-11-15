package com.device.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: USM辅助功能工具类
 * @Version: 1.0
 * @Create: 2019-11-11 16:21:43
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class USMUtils {
    /**
     * 是否有打开辅助功能的设置页面
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
     * 打开辅助功能设置界面
     *
     * @param context
     */
    public static void openUSMSetting(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
        }
    }

    public static List<UsageStats> getUsageStatsByAPI(long beginTime, long endTime, Context context) {
        List<UsageStats> queryUsageStats = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = null;
            usageStatsManager = (UsageStatsManager) context.getApplicationContext()
                    .getSystemService(Context.USAGE_STATS_SERVICE);

            if (usageStatsManager != null) {
                queryUsageStats = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
            }
        }
        return queryUsageStats;
    }

    public static List<UsageStats> getUsageStatsByInvoke(long beginTime, long endTime, Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                Field field = getField(UsageStatsManager.class, "mService");
                if (field == null) {
                    return null;
                }
                boolean override = field.isAccessible();
                field.setAccessible(true);
                Object mService = field.get(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE));
                field.setAccessible(override);

                if (mService == null) {
//                          IBinder iBinder = ServiceManager.getService(USAGE_STATS_SERVICE);
//                          IUsageStatsManager service = IUsageStatsManager.Stub.asInterface(iBinder);

//                          Method method = Class.forName("android.os.ServiceManager").getMethod("getService",String.class);
//                          IBinder iBinder = (IBinder) method.invoke(null, "usagestats");
//                          Object service = Class.forName("android.app.usage.IUsageStatsManager$Stub").getMethod("asInterface",IBinder.class).invoke(null,iBinder);
//                          Class.forName("android.app.usage.IUsageStatsManager").getMethod("queryUsageStats",int.class, long.class, long.class, String.class)
//                          .invoke(service, UsageStatsManager.INTERVAL_BEST, beginTime, endTime, "com.device");
                    return null;
                }
                Method method = getMethod(mService.getClass(), "queryUsageStats", int.class, long.class, long.class, String.class);
                if (method == null) {
                    return null;
                }
                override = method.isAccessible();
                method.setAccessible(true);
                List<String> pkgs = getAppPackageList(context);
                if (pkgs == null) {
                    return null;
                }
                for (int i = 0; i < pkgs.size(); i++) {
                    String opname = pkgs.get(i);
                    Object parceledListSlice = method.invoke(mService, UsageStatsManager.INTERVAL_BEST, beginTime, endTime, opname);
                    if (parceledListSlice == null) {
                        continue;
                    }
                    //android.content.pm.ParceledListSlice.getList()
                    Method getList = getMethod(parceledListSlice.getClass(), "getList");
                    if (getList == null) {
                        continue;
                    }
                    List<UsageStats> o3 = (List<UsageStats>) getList.invoke(parceledListSlice);
                    if (o3 != null || o3.size() > 0) {
                        class RecentUseComparator implements Comparator<UsageStats> {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public int compare(UsageStats lhs, UsageStats rhs) {
                                return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1
                                        : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
                            }
                        }
                        Collections.sort(o3, new RecentUseComparator());
                        return o3;
                    }

                }
                method.setAccessible(override);
            }
        } catch (Throwable igone) {
            igone.printStackTrace();
        }
        return null;
    }

    public static List<String> getAppPackageList(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
        if (packageInfo != null) {
            List<String> strings = new ArrayList<>();
            for (int i = 0; i < packageInfo.size(); i++) {
                strings.add(packageInfo.get(i).packageName);
            }
            return strings;
        }
        return null;
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            try {
                method = clazz.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    public static Field getField(Class clazz, String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (field == null) {
            try {
                field = clazz.getField(fieldName);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return field;
    }

}