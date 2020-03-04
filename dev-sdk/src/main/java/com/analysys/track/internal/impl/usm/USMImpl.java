package com.analysys.track.internal.impl.usm;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.analysys.track.internal.content.UploadKey.Response.RES_POLICY_MODULE_CL_USM;

public class USMImpl {
    public static final String LAST_UPLOAD_TIME = "USMImpl_ST";

    public static JSONArray getUSMInfo(Context context) {
        try {
            // 低版本不采集
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return null;
            }
            //不采集
            boolean usmCl = SPHelper.getBooleanValueFromSP(context, RES_POLICY_MODULE_CL_USM, true);
            if (!usmCl) {
                return null;
            }
            long lastUploadTime = SPHelper.getLongValueFromSP(context, LAST_UPLOAD_TIME, -1);
            long end = System.currentTimeMillis();
            if (lastUploadTime == -1) {
                lastUploadTime = end - (EGContext.TIME_HOUR * 48);
            }
            //SPHelper.setLongValue2SP(context, LAST_UPLOAD_TIME, end);
            return getUSMInfo(context, lastUploadTime, end);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    public static JSONArray getUSMInfo(Context context, long start, long end) {
        JSONArray arr = new JSONArray();
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return arr;
            }
            // 时间校验.
            if (end - start <= 0) {
                end = start - 12 * EGContext.TIME_HOUR;
            }
            if (end - start >= EGContext.TIME_HOUR * 24 * 2) {
                start = end - EGContext.TIME_HOUR * 24 * 2;
            }
            context = EContextHelper.getContext(context);

            // 1. ue方式获取
            Object usageEvents = USMUtils.getUsageEvents(start, end, context);

            if (usageEvents != null) {
                getArrayFromUsageEvents(context, usageEvents, arr);
            }
            if (arr.length() == 0) {
                // 2. us方式获取
                //  List<UsageStats> usList = new ArrayList<UsageStats>();
                //  后续如数据量增加，可考虑更细力度的取时间，更精确，暂时一次获取
                List<UsageStats> usageStatsList = USMUtils.getUsageStats(context, start, end);
                if (usageStatsList.size() > 0) {
                    parserUsageStatsList(context, usageStatsList, arr);
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    /**
     * 解析 dao
     *
     * @param context
     * @param usageStatsList
     * @param arr
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void parserUsageStatsList(Context context, List<UsageStats> usageStatsList, JSONArray arr) {
        if (usageStatsList != null && usageStatsList.size() > 0) {
            PackageManager packageManager = context.getPackageManager();

            for (UsageStats us : usageStatsList) {
                try {
                    long lastUsedTime = us.getLastTimeUsed();
                    long durTime = us.getTotalTimeInForeground();
                    if (lastUsedTime > 0 && durTime > 0) {
                        String pkgName = us.getPackageName();
                        long openTime = lastUsedTime - durTime;

                        USMInfo openEvent = new USMInfo(openTime, pkgName);
                        openEvent.setCollectionType("5");
                        openEvent.setNetType(NetworkUtils.getNetworkType(context));
                        openEvent.setApplicationType(AppSnapshotImpl.getInstance(context).getAppType(pkgName));
                        openEvent.setSwitchType("1");
                        PackageInfo packageInfo = packageManager.getPackageInfo(pkgName, 0);
                        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                        openEvent.setAppName((String) applicationInfo.loadLabel(packageManager));
                        openEvent.setVersionCode(packageInfo.versionName + "|" + packageInfo.versionCode);
                        openEvent.setCloseTime(lastUsedTime);
                        arr.put(openEvent.toJson());
                    }
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUGLY) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
        }
    }

    /**
     * 通过UE 解析json arrsy
     *
     * @param context
     * @param usageEvents
     * @param jsonArray
     * @return
     */
    private static JSONArray getArrayFromUsageEvents(Context context, Object usageEvents, JSONArray jsonArray) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return jsonArray;
        }
        USMInfo openEvent = null;
        Object lastEvent = null;
        boolean hasNextEvent = false;
        while (true) {
            hasNextEvent = (boolean) ClazzUtils.invokeObjectMethod(usageEvents, "hasNextEvent");
            if (!hasNextEvent) {
                break;
            }
            /**
             * 获取Event
             */
            Object event = ClazzUtils.newInstance("android.app.usage.UsageEvents$Event");
            ClazzUtils.invokeObjectMethod(usageEvents, "getNextEvent", new String[]{"android.app.usage.UsageEvents$Event"}
                    , new Object[]{event});

            if (event == null || !SystemUtils.hasLaunchIntentForPackage(packageManager, getPackageName(event))) {
                continue;
            }
            /**
             * 闭合数据
             */
            if (openEvent == null) {
                // 首个
                if (getEventType(event) == UsageEvents.Event.MOVE_TO_FOREGROUND
                        || getEventType(event) == UsageEvents.Event.ACTIVITY_RESUMED) {
                    openEvent = openUsm(context, packageManager, event);
                }
            } else {
                // 闭合非连续
                if (!openEvent.getPkgName().equals(getPackageName(event))) {
                    openEvent.setCloseTime(getTimeStamp(lastEvent));

                    //大于3秒的才算做oc,一闪而过的不算
                    if (openEvent.getCloseTime() - openEvent.getOpenTime() >= EGContext.MINDISTANCE * 3) {
                        jsonArray.put(openEvent.toJson());
                    }

                    if (getEventType(event) == UsageEvents.Event.MOVE_TO_FOREGROUND
                            || getEventType(event) == UsageEvents.Event.ACTIVITY_RESUMED
                    ) {
                        openEvent = openUsm(context, packageManager, event);
                    }
                }
            }
            lastEvent = event;
        }
        return jsonArray;
    }

    public static String getPackageName(Object object) {
        return (String) ClazzUtils.invokeObjectMethod(object, "getPackageName");
    }

    public static long getTimeStamp(Object object) {
        return (long) ClazzUtils.invokeObjectMethod(object, "getTimeStamp");
    }

    public static int getEventType(Object object) {
        return (int) ClazzUtils.invokeObjectMethod(object, "getEventType");
    }

    @SuppressLint("NewApi")
    private static USMInfo openUsm(Context context, PackageManager packageManager, Object event) {
        try {
            USMInfo openEvent = new USMInfo(getTimeStamp(event), getPackageName(event));
            openEvent.setCollectionType("5");
            openEvent.setNetType(NetworkUtils.getNetworkType(context));
            openEvent.setApplicationType(AppSnapshotImpl.getInstance(context).getAppType(getPackageName(event)));
            openEvent.setSwitchType("1");
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(event), 0);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            openEvent.setAppName((String) applicationInfo.loadLabel(packageManager));
            openEvent.setVersionCode(packageInfo.versionName + "|" + packageInfo.versionCode);
            return openEvent;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }
}
