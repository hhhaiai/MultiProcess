package com.device.tripartite.cases.usmcase.tools;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.json.JSONArray;

public class IUSMImpl {

    //
//    /**
//     * 一次启动判断一次,能获取就认为一直能获取
//     */
//    public static Boolean USMAvailable;
//    public static final String LAST_UPLOAD_TIME = "USMImpl_ST";
//
//    public static boolean isUSMAvailable(Context context) {
//        try {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                return false;
//            }
//            boolean usmCl = SPHelper.getBooleanValueFromSP(context, RES_POLICY_MODULE_CL_USM, true);
//            if (!usmCl) {
//                //不采集
//                return false;
//            }
//            // 采集 能获取短路 不能获取不短路
//            if (USMAvailable != null) {
//                return USMAvailable;
//            }
//            // 采集 能获取短路 不能获取不短路
//            UsageEvents usageStats = (UsageEvents) IUSMUtils.getUsageEvents(0, System.currentTimeMillis(), context);
//            USMAvailable = usageStats != null;
//            return USMAvailable;
//        } catch (Throwable e) {
//
//        }
//        return false;
//    }

    public static JSONArray getUSMInfo(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return null;
            }
            long end = System.currentTimeMillis();
            long pre_time = end - (60 * 60 * 1000 * 6);
            //SPHelper.setLongValue2SP(context, LAST_UPLOAD_TIME, end);
            return getUSMInfo(context, pre_time, end);
        } catch (Throwable e) {
        }
        return null;
    }

    public static JSONArray getUSMInfo(Context context, long start, long end) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return null;
            }
            if (end - start <= 0) {
                return null;
            }
            if (end - start >= 60 * 60 * 1000 * 24 * 2) {
                start = end - 60 * 60 * 1000 * 24 * 2;
            }

            PackageManager packageManager = context.getPackageManager();
            UsageEvents usageStats = (UsageEvents) IUSMUtils.getUsageEvents(start, end, context);
            if (usageStats != null) {
                JSONArray jsonArray = new JSONArray();
                IUSMInfo openEvent = null;
                UsageEvents.Event lastEvent = null;
                while (usageStats.hasNextEvent()) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    usageStats.getNextEvent(event);

                    if (packageManager.getLaunchIntentForPackage(event.getPackageName()) == null) {
                        continue;
                    }
                    if (openEvent == null) {
                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            openEvent = openUsm(context, packageManager, event);
                        } else {
                            //非前台意味着上一个事件必定有一个打开，则补上开始时间,包名为当前包名
                            openEvent = openUsm(context, packageManager, event, start);
                        }
                    } else {
                        if (!openEvent.getPkgName().equals(event.getPackageName())) {
                            openEvent.setCloseTime(lastEvent.getTimeStamp());

                            //大于3秒的才算做oc,一闪而过的不算
                            if (openEvent.getCloseTime() - openEvent.getOpenTime() >= 1000 * 3) {
                                jsonArray.put(openEvent.toJson());
                            }

                            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                                openEvent = openUsm(context, packageManager, event);
                            }
                        }
                    }
                    lastEvent = event;
                }
                //最后一个，没有关闭则补上关闭
                if (openEvent != null && openEvent.getCloseTime() == 0) {
                    openEvent.setCloseTime(end);
                    //大于3秒的才算做oc,一闪而过的不算
                    if (openEvent.getCloseTime() - openEvent.getOpenTime() >= 1000 * 3) {
                        jsonArray.put(openEvent.toJson());
                    }
                }

                return jsonArray;
            }
        } catch (Throwable e) {
        }
        return null;
    }

    @SuppressLint("NewApi")
    private static IUSMInfo openUsm(Context context, PackageManager packageManager, UsageEvents.Event event) {
        return openUsm(context, packageManager, event, 0);
    }

    @SuppressLint("NewApi")
    private static IUSMInfo openUsm(Context context, PackageManager packageManager, UsageEvents.Event event, long opentime) {
        try {
            if (opentime == 0) {
                opentime = event.getTimeStamp();
            }
            IUSMInfo openEvent = new IUSMInfo(opentime, event.getPackageName());
            openEvent.setCollectionType("5");
//            openEvent.setNetType(NetworkUtils.getNetworkType(context));
//            openEvent.setApplicationType(AppSnapshotImpl.getInstance(context).getAppType(event.getPackageName()));
            openEvent.setSwitchType("1");
            PackageInfo packageInfo = packageManager.getPackageInfo(event.getPackageName(), 0);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            openEvent.setAppName((String) applicationInfo.loadLabel(packageManager));
            openEvent.setVersionCode(packageInfo.versionName + "|" + packageInfo.versionCode);
            return openEvent;
        } catch (Throwable e) {
        }
        return null;
    }
}
