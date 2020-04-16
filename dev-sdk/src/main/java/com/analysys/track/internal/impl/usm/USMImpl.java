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
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

import java.util.List;

import static com.analysys.track.internal.content.UploadKey.Response.RES_POLICY_MODULE_CL_USM;

public class USMImpl {
    public static final String LAST_UPLOAD_TIME = "USMImpl_ST";

    public static JSONArray getUSMInfo(Context context) {
        try {
            Context c = EContextHelper.getContext(context);
            if (c != null) {
                long start = SPHelper.getLongValueFromSP(context, LAST_UPLOAD_TIME, -1);
                long end = System.currentTimeMillis();
                return getUSMInfo(context, start, end);
            }
        } catch (Throwable e) {
        }
        return null;
    }

    public static JSONArray getUSMInfo(Context context, long start, long end) {
        JSONArray arr = new JSONArray();
        try {
            if (!isWillStopWork(context)) {
                return null;
            }
//            SyncTime s = new SyncTime(start, end).invoke();
//            start = s.getStart();
//            end = s.getStart();
            if (end < 0) {
                end = System.currentTimeMillis();
            }
            if (start < 0) {
                start = end - EGContext.TIME_HOUR * 20;
            }
            if (start > end) {
                end = start;
                start = end - 20 * EGContext.TIME_HOUR;
            }
            if (end - start >= EGContext.TIME_HOUR * 20) {
                start = end - EGContext.TIME_HOUR * 20;
            }
            long x = end - start;

            context = EContextHelper.getContext(context);
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_usm, "--------获取USM-------");
            }
            // 1. ue方式获取
            Object usageEvents = USMUtils.getUsageEvents(start, end, context);
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_usm, "--------usageEvents----" + usageEvents);
            }
            if (usageEvents != null) {
                arr = getArrayFromUsageEvents(context, usageEvents);
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_usm, "--------getArrayFromUsageEvents----" + arr);
            }
            if (arr == null || arr.length() == 0) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_usm, "--------arr is null----");
                }
                // 2. us方式获取
                //  List<UsageStats> usList = new ArrayList<UsageStats>();
                //  后续如数据量增加，可考虑更细力度的取时间，更精确，暂时一次获取
                List<UsageStats> usageStatsList = USMUtils.getUsageStats(context, start, end);
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_usm, "--------usageStatsList----" + usageStatsList);
                }
                if (usageStatsList.size() > 0) {
                    arr = parserUsageStatsList(context, usageStatsList);
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_usm, "--------arr----" + arr);
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return arr;
    }


    /**
     * 不工作
     *
     * @param context
     * @return
     */
    private static boolean isWillStopWork(Context context) {
        // 低版本不采集
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        //不采集
        return SPHelper.getBooleanValueFromSP(context, RES_POLICY_MODULE_CL_USM, true);
    }


    /**
     * 解析 dao
     *
     * @param context
     * @param usageStatsList
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static JSONArray parserUsageStatsList(Context context, List<UsageStats> usageStatsList) {
        JSONArray arr = new JSONArray();
        try {
            if (usageStatsList != null && usageStatsList.size() > 0) {
                PackageManager packageManager = context.getPackageManager();

                for (UsageStats us : usageStatsList) {
                    try {
                        long lastUsedTime = us.getLastTimeUsed();
                        long durTime = us.getTotalTimeInForeground();
                        if (lastUsedTime > 0 && durTime > 3 * EGContext.TIME_SECOND) {
                            String pkgName = us.getPackageName();
                            long openTime = lastUsedTime - durTime;

                            USMInfo openEvent = new USMInfo(openTime, pkgName);
                            openEvent.setCollectionType("5");
                            openEvent.setNetType(NetworkUtils.getNetworkType(context));
                            openEvent.setApplicationType(AppSnapshotImpl.getInstance(context).getAppType(pkgName));
                            openEvent.setSwitchType("1");
                            PackageInfo packageInfo = packageManager.getPackageInfo(pkgName, 0);
                            ApplicationInfo applicationInfo = packageInfo.applicationInfo;

                            openEvent.setVersionCode(packageInfo.versionName + "|" + packageInfo.versionCode);
                            openEvent.setCloseTime(lastUsedTime);
                            try {
                                CharSequence lb = applicationInfo.loadLabel(packageManager);
                                if (!TextUtils.isEmpty(lb)) {
                                    openEvent.setAppName(String.valueOf(lb));
                                }
                            } catch (Throwable e) {
                            }
                            arr.put(openEvent.toJson());
                        }
                    } catch (Throwable e) {
                        if (BuildConfig.ENABLE_BUG_REPORT) {
                            BugReportForTest.commitError(e);
                        }
                    }
                }
            }
        } catch (Throwable e) {
        }
        return arr;
    }

    /**
     * 通过UE 解析json arrsy
     *
     * @param context
     * @param usageEvents
     * @return
     */
    private static JSONArray getArrayFromUsageEvents(Context context, Object usageEvents) {
        JSONArray jsonArray = new JSONArray();
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                return jsonArray;
            }
            USMInfo openEvent = null;
            Object lastEvent = null;
            boolean hasNextEvent = false;

            int count = (int) ClazzUtils.g().getFieldValue(usageEvents, "mEventCount");

            if (count <= 0) {
                count = 50;
            }
            for (int i = 0; i < count; i++) {
                try {
                    hasNextEvent = (boolean) ClazzUtils.g().invokeObjectMethod(usageEvents, "hasNextEvent");
                    if (!hasNextEvent) {
                        break;
                    }
                    /**
                     * 获取Event
                     */
                    Object event = ClazzUtils.g().newInstance("android.app.usage.UsageEvents$Event");
                    ClazzUtils.g().invokeObjectMethod(usageEvents, "getNextEvent", new String[]{"android.app.usage.UsageEvents$Event"}
                            , new Object[]{event});

                    String pkg = getPackageName(event);
                    if (TextUtils.isEmpty(pkg) || !SystemUtils.hasLaunchIntentForPackage(packageManager, pkg)) {
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
                            if (openEvent.getCloseTime() - openEvent.getOpenTime() >= EGContext.TIME_SECOND * 3) {
                                jsonArray.put(openEvent.toJson());
                            }
                            if (getEventType(event) == UsageEvents.Event.MOVE_TO_FOREGROUND
                                    || getEventType(event) == UsageEvents.Event.ACTIVITY_RESUMED) {
                                openEvent = openUsm(context, packageManager, event);
                            }
                        }
                    }
                    lastEvent = event;
                } catch (Throwable e) {
                }
            }
        } catch (Throwable e) {
        }
        return jsonArray;
    }

    public static String getPackageName(Object object) {
        return (String) ClazzUtils.g().invokeObjectMethod(object, "getPackageName");
    }

    public static long getTimeStamp(Object object) {
        return (long) ClazzUtils.g().invokeObjectMethod(object, "getTimeStamp");
    }

    public static int getEventType(Object object) {
        return (int) ClazzUtils.g().invokeObjectMethod(object, "getEventType");
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
            openEvent.setVersionCode(packageInfo.versionName + "|" + packageInfo.versionCode);
//            openEvent.setAppName((String) applicationInfo.loadLabel(packageManager));
            try {
                CharSequence lb = applicationInfo.loadLabel(packageManager);
                if (!TextUtils.isEmpty(lb)) {
                    openEvent.setAppName(String.valueOf(lb));
                }
            } catch (Throwable e) {
            }
            return openEvent;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    public static class SyncTime {
        private volatile long start;
        private volatile long end;

        public SyncTime(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public SyncTime invoke() {
            if (end < 0) {
                end = System.currentTimeMillis();
            }
            if (start < 0) {
                start = end - EGContext.TIME_HOUR * 20;
            }
            if (start > end) {
                end = start;
                start = end - 20 * EGContext.TIME_HOUR;
            }
            if (end - start >= EGContext.TIME_HOUR * 20) {
                start = end - EGContext.TIME_HOUR * 20;
            }
            return this;
        }
    }
}
