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
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

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
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                return null;
            }
            // 时间校验.
            if (end - start <= 0) {
                end = start - 12 * EGContext.TIME_HOUR;
            }
            if (end - start >= EGContext.TIME_HOUR * 24 * 2) {
                start = end - EGContext.TIME_HOUR * 24 * 2;
            }

            PackageManager packageManager = EContextHelper.getContext().getPackageManager();
            if (packageManager == null) {
                return null;
            }
            Object usageEvents = USMUtils.getUsageEvents(start, end, context);
            if (usageEvents != null) {
                JSONArray jsonArray = new JSONArray();
                USMInfo openEvent = null;
                Object lastEvent = null;
                boolean hasNextEvent = false;
                while (true) {
                    hasNextEvent = (boolean) ClazzUtils.invokeObjectMethod(usageEvents, "hasNextEvent");
                    if (!hasNextEvent) {
                        break;
                    }
                    Object event = ClazzUtils.newInstance("android.app.usage.UsageEvents$Event");

                    ClazzUtils.invokeObjectMethod(usageEvents, "getNextEvent", new String[]{"android.app.usage.UsageEvents$Event"}
                            , new Object[]{event});

                    if (event == null || !SystemUtils.hasLaunchIntentForPackage(packageManager, getPackageName(event))) {
                        continue;
                    }
                    if (openEvent == null) {
                        if (getEventType(event) == UsageEvents.Event.MOVE_TO_FOREGROUND
                                || getEventType(event) == UsageEvents.Event.ACTIVITY_RESUMED) {
                            openEvent = openUsm(context, packageManager, event);
                        }
                    } else {
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
            } else {
                //US

            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
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
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }
}
