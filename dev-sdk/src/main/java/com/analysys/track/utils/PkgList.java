package com.analysys.track.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 安装列表获取
 * @Version: 1.0
 * @Create: 2020/3/4 16:04
 * @author: sanbo
 */
public class PkgList {


    public static Set<String> getAppPackageList(Context context) {
        Set<String> appSet = new HashSet<>();
        try {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
            if (packageInfo.size() > 0) {
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
                            if (split.length > 1) {
                                String packageName = split[1];
                                appSet.add(packageName);
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
        if (appSet.size() == 0) {
            appSet.add(context.getPackageName());
        }
        return appSet;
    }
}
