package com.analysys.track.internal.impl.usm;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PackageManager packageManager = context.getApplicationContext()
                        .getPackageManager();
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                return list.size() > 0;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 打开辅助功能设置界面
     *
     * @param context
     */
    public static void openUSMSetting(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                context.startActivity(intent);
            }
        } catch (Throwable e) {
        }
    }


    public static Object getUsageEvents(long beginTime, long endTime, Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Object usageEvents;
                usageEvents = getUsageEventsByApi(beginTime, endTime, context);
                boolean b = (boolean) ClazzUtils.invokeObjectMethod(usageEvents, "hasNextEvent");
                if (usageEvents != null && b) {
                    return usageEvents;
                }
                usageEvents = getUsageEventsByInvoke(beginTime, endTime, context);
                b = (boolean) ClazzUtils.invokeObjectMethod(usageEvents, "hasNextEvent");
                if (usageEvents != null && b) {
                    return usageEvents;
                }
            }
        } catch (Throwable e) {
        }
        return null;

    }

    private static Object getUsageEventsByApi(long beginTime, long endTime, Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return ClazzUtils.invokeObjectMethod(context.getApplicationContext()
                                .getSystemService(Context.USAGE_STATS_SERVICE), "queryEvents",
                        new Class[]{long.class, long.class}, new Object[]{beginTime, endTime});
            }
        } catch (Throwable e) {
        }
        return null;
    }

    public static Object getUsageEventsByInvoke(long beginTime, long endTime, Context context) {
        try {
            if (endTime <= beginTime) {
                return null;
            }
            if (context == null) {
                return null;
            }
            if (Build.VERSION.SDK_INT > 29) {
                //未来 android 11 防止
                return null;
            }
            if (!ClazzUtils.rawReflex && (context.getApplicationInfo().targetSdkVersion >= 28 || Build.VERSION.SDK_INT >= 28)) {
                return null;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Object mService = ClazzUtils.getObjectFieldObject(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE), "mService");
                if (mService == null) {
                    return null;
                }
                Set<String> pkgs = getAppPackageList(context);
                if (pkgs == null) {
                    return null;
                }
                Object usageEvents = null;
                for (String opname : pkgs) {
                    try {
                        usageEvents = ClazzUtils.invokeObjectMethod(mService, "queryEvents", new Class[]{long.class, long.class, String.class}, new Object[]{beginTime, endTime, opname});
                        if (usageEvents == null) {
                            continue;
                        }
                        boolean b = (boolean) ClazzUtils.invokeObjectMethod(usageEvents, "hasNextEvent");
                        if (b) {
                            break;
                        }
                    } catch (Throwable e) {
                    }
                }
                return usageEvents;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return null;
    }


    public static Set<String> getAppPackageList(Context context) {
        Set<String> appSet = new HashSet<>();
        try {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
            if (packageInfo != null) {
                for (int i = 0; i < packageInfo.size(); i++) {
                    appSet.add(packageInfo.get(i).packageName);
                }
            }

            String result = ShellUtils.shell("pm list packages");
            if (!TextUtils.isEmpty(result) && result.contains("\n")) {
                String[] lines = result.split("\n");
                if (lines.length > 0) {
                    String line = null;
                    for (int i = 0; i < lines.length; i++) {
                        line = lines[i];
                        // 单行条件: 非空&&有点&&有冒号
                        if (!TextUtils.isEmpty(line) && line.contains(".") && line.contains(":")) {
                            // 分割. 样例数据:<code>package:com.android.launcher3</code>
                            String[] split = line.split(":");
                            if (split != null && split.length > 1) {
                                String packageName = split[1];
                                appSet.add(packageName);
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return appSet;
    }


}