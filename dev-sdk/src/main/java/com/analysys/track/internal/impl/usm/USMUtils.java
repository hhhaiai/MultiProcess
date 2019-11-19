package com.analysys.track.internal.impl.usm;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
        }
    }


    public static List<UsageStats> getUsageStats(long beginTime, long endTime, Context context) {
        List<UsageStats> usageStats = null;

        //api
        usageStats = getUsageStatsByAPI(beginTime, endTime, context);
        if (usageStats != null && usageStats.size() > 0) {
            return usageStats;
        }

        //反射传别人的包名
        usageStats = getUsageStatsByInvoke(beginTime, endTime, context);
        if (usageStats != null && usageStats.size() > 0) {
            return usageStats;
        }

        //直接尝试读数据库 有root 手机好使
        usageStats = getUsageStatsByDataBase(beginTime, endTime, context);
        if (usageStats != null && usageStats.size() > 0) {
            return usageStats;
        }
        return usageStats;

    }


    private static List<UsageStats> getUsageStatsByDataBase(long beginTime, long endTime, Context context) {
        File systemDataDir = new File(Environment.getDataDirectory(), "system");
        File mUsageStatsDir = new File(systemDataDir, "usagestats");
        if (mUsageStatsDir.exists() && mUsageStatsDir.isDirectory()) {
            //  UserUsageStatsService(Context context, int userId, File usageStatsDir,StatsUpdatedListener listener)
            try {
                Class clazz = Class.forName("com.android.server.usage.UserUsageStatsService");
                Constructor constructor = clazz.getConstructor(Context.class, int.class, File.class, Class.forName("StatsUpdatedListener"));
                Object userUsageStatsService = constructor.newInstance(context, 0, mUsageStatsDir, null);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<UsageStats> getUsageStatsByAPI(long beginTime, long endTime, Context context) {
        List<UsageStats> queryUsageStats = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getApplicationContext()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                Field field = getField(UsageStatsManager.class, "mService");
                if (field == null) {
                    return null;
                }
                boolean override = field.isAccessible();
                field.setAccessible(true);
                //android.app.usage.IUsageStatsManager$Stub$Proxy
                Object mService = field.get(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE));
                field.setAccessible(override);
                if (mService == null) {
                    Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
                    IBinder iBinder = (IBinder) method.invoke(null, "usagestats");
                    mService = Class.forName("android.app.usage.IUsageStatsManager$Stub").getMethod("asInterface", IBinder.class).invoke(null, iBinder);
                }
                if (mService == null) {
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
                            @Override
                            public int compare(UsageStats lhs, UsageStats rhs) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1
                                            : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
                                }
                                return -1;
                            }
                        }
                        Collections.sort(o3, new RecentUseComparator());
                        return o3;
                    }

                }
                method.setAccessible(override);
            }
        } catch (Throwable igone) {
        }
        return null;
    }

    public static UsageEvents getUsageEvents(long beginTime, long endTime, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageEvents usageEvents;
            usageEvents = getUsageEventsByApi(beginTime, endTime, context);
            if (usageEvents != null && usageEvents.hasNextEvent()) {
                return usageEvents;
            }
            usageEvents = getUsageEventsByInvoke(beginTime, endTime, context);
            if (usageEvents != null && usageEvents.hasNextEvent()) {
                return usageEvents;
            }
        }
        return null;

    }

    private static UsageEvents getUsageEventsByApi(long beginTime, long endTime, Context context) {
        UsageStatsManager usageStatsManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStatsManager = (UsageStatsManager) context.getApplicationContext()
                    .getSystemService(Context.USAGE_STATS_SERVICE);
            return usageStatsManager.queryEvents(beginTime, endTime);
        }
        return null;
    }

    public static UsageEvents getUsageEventsByInvoke(long beginTime, long endTime, Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                Field field = getField(UsageStatsManager.class, "mService");
                if (field == null) {
                    return null;
                }
                boolean override = field.isAccessible();
                field.setAccessible(true);
                //android.app.usage.IUsageStatsManager$Stub$Proxy
                Object mService = field.get(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE));
                field.setAccessible(override);

                if (mService == null) {
                    Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
                    IBinder iBinder = (IBinder) method.invoke(null, "usagestats");
                    mService = Class.forName("android.app.usage.IUsageStatsManager$Stub").getMethod("asInterface", IBinder.class).invoke(null, iBinder);
                }
                if (mService == null) {
                    return null;
                }
                Method method = getMethod(mService.getClass(), "queryEvents", long.class, long.class, String.class);
                if (method == null) {
                    return null;
                }
                override = method.isAccessible();
                method.setAccessible(true);
                List<String> pkgs = getAppPackageList(context);
                if (pkgs == null) {
                    return null;
                }
                UsageEvents usageEvents = null;
                for (int i = 0; i < pkgs.size(); i++) {
                    String opname = pkgs.get(i);
                    usageEvents = (UsageEvents) method.invoke(mService, beginTime, endTime, opname);
                    if (usageEvents != null && usageEvents.hasNextEvent()) {
                        break;
                    }
                }
                method.setAccessible(override);
                return usageEvents;
            }
        } catch (Throwable igone) {
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