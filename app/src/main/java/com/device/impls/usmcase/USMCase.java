package com.device.impls.usmcase;

import android.app.usage.ConfigurationStats;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.device.impls.usmcase.tools.IUSMImpl;
import com.device.utils.EL;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * USM case
 */
public class USMCase {
    public static void run(Context context) {
        EL.i("===>" + IUSMImpl.getUSMInfo(context));
    }

    public static void simple(Context context) {

//        getDataByUsageEvents(context);
        getDataByUsageStats(context);
//        getDataByConfigurationStats(context);
//        getDataByA(context);
    }

    private static void getDataByA(Context context) {
        EL.i("===================USM测试[MAP]=====================");

        long beginTime = 0;
        long endTime = System.currentTimeMillis();
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            EL.i("【MAP API获取】获取USM：" + usm);
            Map<String, UsageStats> m = usm.queryAndAggregateUsageStats(beginTime, endTime);
            EL.i("【MAP API获取】获取AndAggregateUsageStats：" + m);
            for (String key : m.keySet()) {
                UsageStats us = m.get(key);
                if (packageManager.getLaunchIntentForPackage(us.getPackageName()) != null) {
                    EL.i("【MAP API获取】" + key + "-----(" + m.get(key).getPackageName() + ")  LastTimeStamp：" + m.get(key).getLastTimeStamp() + "  LastTimeUsed：" + m.get(key).getLastTimeUsed());
                } else {
                    EL.v("【MAP API获取】" + key + "-----(" + m.get(key).getPackageName() + ")  LastTimeStamp：" + m.get(key).getLastTimeStamp() + "  LastTimeUsed：" + m.get(key).getLastTimeUsed());
                }
            }
        }
    }

    private static void getDataByConfigurationStats(Context context) {

        EL.i("===================USM测试[Configurations]=====================");

        long beginTime = 0;
        long endTime = System.currentTimeMillis();
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            EL.i("【CS API获取】获取USM：" + usm);

            List<ConfigurationStats> css = usm.queryConfigurations(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
            if (css != null && css.size() > 0) {
                EL.i("【CS API获取】获取css：" + css.size());
                for (ConfigurationStats cs : css) {
                    EL.i("【CS API获取】(" + cs.getConfiguration() + ")" + cs.toString());
                }
            }
        }
    }

    private static void getDataByUsageStats(Context context) {

        EL.i("===================USM测试[UsageStats]=====================");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PackageManager packageManager = context.getPackageManager();

            long beginTime = 0;
            long endTime = System.currentTimeMillis();
            EL.w("-US 获取使用方式1： 系统API");

            /**
             * 方式一 api获取
             */
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            EL.i("【US API获取】获取USM：" + usm);
            List<UsageStats> uss = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
            if (uss != null && uss.size() > 0) {
                EL.i("【US API获取】获取queryUsageStats：" + uss.size());
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
                Collections.sort(uss, new RecentUseComparator());
            }
            for (UsageStats us : uss) {
                if (packageManager.getLaunchIntentForPackage(us.getPackageName()) != null) {
//                    EL.i("【US API获取】(" + us.getPackageName() + ")  LastTimeStamp：" + us.getLastTimeStamp() + "  LastTimeUsed：" + us.getLastTimeUsed());
                    EL.i("US [" + us.getPackageName() + "]  " + us.getLastTimeUsed()
//                            +" ; " + us.getTotalTimeVisible()
                                    + " ; " + us.getLastTimeStamp()
//                                    + " ; " + us.getLastTimeForegroundServiceUsed()
                                    + " ; " + us.getFirstTimeStamp()
//                                    + " ; " + us.getTotalTimeForegroundServiceUsed()
                                    + " ; " + us.getTotalTimeInForeground()
                    );
                } else {
                    EL.v("【US API获取】(" + us.getPackageName() + ")  LastTimeStamp：" + us.getLastTimeStamp() + "  LastTimeUsed：" + us.getLastTimeUsed());
                }
            }
        }

    }


    public static void getDataByUsageEvents(Context context) {
        EL.i("===================USM测试[UsageEvents]=====================");
        long beginTime = 0;
        long endTime = System.currentTimeMillis();

        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm != null) {
                UsageEvents usageEvents = usm.queryEvents(beginTime, endTime);
                EL.w("UE获取使用方式一：系统API");
                /**
                 * 方式一: API获取
                 */
                try {
                    if (usageEvents != null) {
                        EL.i("【UE API获取】UsageEvents 成功~");
                        EL.i("【UE API获取】 UsageEvents hasNext:" + usageEvents.hasNextEvent());
                        List<UsageStats> list = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
                        EL.i("【UE API获取】 UsageStatsManager.queryUsageStats:" + list.size());

                        while (usageEvents.hasNextEvent()) {
                            UsageEvents.Event event = new UsageEvents.Event();
                            usageEvents.getNextEvent(event);

                            // 有启动界面的彩色展示，否则灰色展示
                            if (packageManager.getLaunchIntentForPackage(event.getPackageName()) != null) {
                                // 1. ACTIVITY_RESUMED/MOVE_TO_FOREGROUND
                                // 2.  ACTIVITY_PAUSED/MOVE_TO_BACKGROUND
                                if (event.getEventType() == 1) {
                                    EL.i("【UE API获取】（ " + event.getPackageName() + " ）TimeStamp： " + event.getTimeStamp() + "  切入前台");
                                } else if (event.getEventType() == 2) {
                                    EL.d("【UE API获取】（ " + event.getPackageName() + " ）TimeStamp： " + event.getTimeStamp() + "  切入后台");
                                } else if (event.getEventType() == 5) {
                                    EL.d("【UE API获取】（ " + event.getPackageName() + " ）TimeStamp： " + event.getTimeStamp() + "  旋转屏幕。。");
                                } else {
                                    EL.e("【UE API获取】（ " + event.getPackageName() + " ）TimeStamp： " + event.getTimeStamp() + "  EventType：" + event.getEventType());
                                }
                            } else {
                                if (event.getEventType() == 1) {
                                    EL.v("【UE API获取】（ " + event.getPackageName() + " ）TimeStamp： " + event.getTimeStamp() + "  切入前台");
                                } else if (event.getEventType() == 2) {
                                    EL.v("【UE API获取】（ " + event.getPackageName() + " ）TimeStamp： " + event.getTimeStamp() + "  切入后台");
                                } else if (event.getEventType() == 5) {
                                    EL.v("【UE API获取】（ " + event.getPackageName() + " ）TimeStamp： " + event.getTimeStamp() + "  旋转屏幕。。");
                                } else {
                                    EL.v("【UE API获取】（ " + event.getPackageName() + " ）TimeStamp： " + event.getTimeStamp() + "  EventType：" + event.getEventType());
                                }
                            }

                        }
                    }
                } catch (Throwable e) {
                    EL.e(e);
                }
            }
        }
    }
}
