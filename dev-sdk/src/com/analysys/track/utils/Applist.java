package com.analysys.track.utils;

import java.util.HashSet;
import java.util.Set;

import com.analysys.track.utils.reflectinon.EContextHelper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 安装列表工具类
 * @Version: 1.0
 * @Create: Mar 6, 2019 5:49:18 PM
 * @Author: sanbo
 */
public class Applist {

    private Applist() {}

    private static class Holder {
        private static Applist Instance = new Applist();
    }

    public static Applist getInstance(Context cxt) {
        Holder.Instance.init(cxt);
        return Holder.Instance;
    }

    /**
     * 获取APP类型
     * 
     * @param pkg
     * @return
     */
    public String getAppType(String pkg) {
        return Applist.getInstance(mContext).isSystemApps(pkg) ? "SA" : "OA";
    }

    /**
     * 是否为系统应用:
     * <p>
     * 1. shell获取到三方列表判断
     * <p>
     * 2. 获取异常的使用其他方式判断
     * 
     * @param pkg
     * @return
     */
    private boolean isSystemApps(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        // 1. 没有获取应用列表则获取
        if (!isGetAppList) {
            getPkgList(mSystemAppSet, APP_LIST_SYSTEM);
            isGetAppList = true;
        }
        // 2. 根据列表内容判断
        if (mSystemAppSet.size() > 0) {
            if (mSystemAppSet.contains(pkg)) {
                return true;
            } else {
                return false;
            }
        } else {
            try {
                // 3. 使用系统方法判断
                mContext = EContextHelper.getContext(mContext);
                if (mContext == null) {
                    return false;
                }
                PackageManager pm = mContext.getPackageManager();
                if (pm == null) {
                    return false;
                }
                PackageInfo pInfo = pm.getPackageInfo(pkg, 0);
                if ((pInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 1) {
                    return true;
                }
            } catch (Throwable e) {
            }

        }
        return false;
    }

    /**
     * 获取安装列表
     * 
     * @param appSet
     * @param shell
     * @return
     */
    public static Set<String> getPkgList(Set<String> appSet, String shell) {
        // Set<String> set = new HashSet<String>();
        String result = ShellUtils.shell(shell);
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
        return appSet;
    }

    private void init(Context cxt) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(cxt);
        }
    }

    private Context mContext = null;
    // private final String SHELL_PM_LIST_PACKAGES = "pm list packages";//all
    private final String APP_LIST_SYSTEM = "pm list packages -s";// system
    // private final String APP_LIST_USER = "pm list packages -3";// third party
    // 获取系统应用列表
    private final Set<String> mSystemAppSet = new HashSet<String>();
    // 已经获取应用列表
    private volatile boolean isGetAppList = false;

}
