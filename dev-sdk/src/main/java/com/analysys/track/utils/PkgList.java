package com.analysys.track.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 安装列表获取
 * @Version: 1.0
 * @Create: 2020/3/4 16:04
 * @author: sanbo
 */
public class PkgList {


    public static List<String> getAppPackageList(Context context) {
        //若不考虑内存占用，可使用CopyOnWriteArrayList(线程安全)
        List<String> apps = new ArrayList<String>();
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
                if (packageInfo.size() > 0) {
                    for (int i = 0; i < packageInfo.size(); i++) {
                        try {
                            String pkg = packageInfo.get(i).packageName;
                            if (!TextUtils.isEmpty(pkg) && !apps.contains(pkg)) {
                                apps.add(pkg);
                            }
                        } catch (Throwable e) {
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }

        try {
            List<String> arr = ShellUtils.getResultArrays("pm list packages");
            if (arr != null) {
                for (String line : arr) {
                    // 单行条件: 非空&&有点&&有冒号
                    if (!TextUtils.isEmpty(line) && line.contains(".") && line.contains(":")) {
                        // 分割. 样例数据:<code>package:com.android.launcher3</code>
                        String[] ss = line.split(":");
                        if (ss.length > 1) {
                            String packageName = ss[1];
                            if (!TextUtils.isEmpty(packageName) && !apps.contains(packageName)) {
                                apps.add(packageName);
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }

        if (apps.size() == 0) {
            apps.add(context.getPackageName());
        }
        return apps;
    }
}
