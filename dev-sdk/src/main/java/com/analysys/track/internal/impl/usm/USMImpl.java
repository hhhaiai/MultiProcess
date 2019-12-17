package com.analysys.track.internal.impl.usm;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

import static com.analysys.track.internal.content.UploadKey.Response.RES_POLICY_MODULE_CL_USM;

public class USMImpl {

    /**
     * 一次启动判断一次,能获取就认为一直能获取
     */
    public static Boolean USMAvailable;
    public static final String LAST_UPLOAD_TIME = "USMImpl_ST";

    public static boolean isUSMAvailable(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return false;
            }
            boolean usmCl = SPHelper.getBooleanValueFromSP(context, RES_POLICY_MODULE_CL_USM, true);
            if (!usmCl) {
                //不采集
                return false;
            }
            // 采集 能获取短路 不能获取不短路
            if (USMAvailable != null) {
                return USMAvailable;
            }
            // 采集 能获取短路 不能获取不短路
            Object usageStats = USMUtils.getUsageEvents(0, System.currentTimeMillis(), context);
            USMAvailable = usageStats != null;
            return USMAvailable;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }

        }
        return false;
    }

    public static JSONArray getUSMInfo(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return null;
            }
            long pre_time = SPHelper.getLongValueFromSP(context, LAST_UPLOAD_TIME, -1);
            long end = System.currentTimeMillis();
            if (pre_time == -1) {
                pre_time = end - (EGContext.TIME_HOUR * 6);
            }
            //SPHelper.setLongValue2SP(context, LAST_UPLOAD_TIME, end);
            return getUSMInfo(context, pre_time, end);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
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
            if (end - start >= EGContext.TIME_HOUR * 24 * 2) {
                start = end - EGContext.TIME_HOUR * 24 * 2;
            }

            PackageManager packageManager = context.getPackageManager();
            Object usageStats = USMUtils.getUsageEvents(start, end, context);
            if (usageStats != null) {
                JSONArray jsonArray = new JSONArray();
                USMInfo openEvent = null;
                Object lastEvent = null;
                boolean hasNext = false;
                while (true) {
                    boolean b = (boolean) ClazzUtils.invokeObjectMethod(usageStats, "hasNextEvent");
                    if (!b) {
                        break;
                    }
                    Object event = ClazzUtils.newInstance("android.app.usage.UsageEvents$Event");

                    ClazzUtils.invokeObjectMethod(usageStats, "getNextEvent", new String[]{"android.app.usage.UsageEvents$Event"}
                            , new Object[]{event});

                    if (packageManager.getLaunchIntentForPackage(getPackageName(event)) == null) {
                        continue;
                    }
                    if (openEvent == null) {

                        if (getEventType(event) == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            openEvent = openUsm(context, packageManager, event);
                        }
                    } else {
                        if (!openEvent.getPkgName().equals(getPackageName(event))) {
                            openEvent.setCloseTime(getTimeStamp(lastEvent));

                            //大于3秒的才算做oc,一闪而过的不算
                            if (openEvent.getCloseTime() - openEvent.getOpenTime() >= EGContext.MINDISTANCE * 3) {
                                jsonArray.put(openEvent.toJson());
                            }

                            if (getEventType(event) == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                                openEvent = openUsm(context, packageManager, event);
                            }
                        }
                    }
                    lastEvent = event;
                }
                return jsonArray;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return null;
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
                BuglyUtils.commitError(e);
            }
        }
        return null;
    }
}
