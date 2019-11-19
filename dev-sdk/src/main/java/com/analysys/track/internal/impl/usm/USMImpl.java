package com.analysys.track.internal.impl.usm;

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
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

public class USMImpl {

    public static final String LAST_UPLOAD_TIME = "USMImpl_ST";

    public static boolean isUSMAvailable(Context context) {
        try {
            UsageEvents usageStats = USMUtils.getUsageEvents(0, System.currentTimeMillis(), context);
            return usageStats != null;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }

        }
        return false;
    }

    public static JSONArray getUSMInfo(Context context) {
        try {
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            UsageEvents usageStats = USMUtils.getUsageEvents(start, end, context);
            if (usageStats != null) {
                JSONArray jsonArray = new JSONArray();
                USMInfo openEvent = null;
                while (usageStats.hasNextEvent()) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    usageStats.getNextEvent(event);
                    if (packageManager.getLaunchIntentForPackage(event.getPackageName()) == null) {
                        continue;
                    }
                    if (openEvent == null) {
                        openEvent = new USMInfo(event.getTimeStamp(), event.getPackageName());
                        openEvent.setCollectionType("4");
                        openEvent.setNetType(NetworkUtils.getNetworkType(context));
                        openEvent.setApplicationType(AppSnapshotImpl.getInstance(context)
                                .getAppType(event.getPackageName()));
                        openEvent.setSwitchType("1");
                        PackageInfo packageInfo = null;
                        try {
                            packageInfo = packageManager.getPackageInfo(event.getPackageName(), 0);
                            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                            openEvent.setAppName((String) applicationInfo.loadLabel(packageManager));
                            openEvent.setVersionCode(packageInfo.versionName + "|" + packageInfo.versionCode);
                        } catch (Throwable e) {
                            if (BuildConfig.ENABLE_BUGLY) {
                                BuglyUtils.commitError(e);
                            }
                        }
                    } else {
                        if (!openEvent.getPkgName().equals(event.getPackageName())) {
                            openEvent.setCloseTime(event.getTimeStamp());
                            jsonArray.put(openEvent.toJson());
                            openEvent = null;
                        }
                    }
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
}
