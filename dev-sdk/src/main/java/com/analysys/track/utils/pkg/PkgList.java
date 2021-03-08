package com.analysys.track.utils.pkg;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.work.ISayHello;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 安装列表获取
 * @Version: 1.0
 * @Create: 2020/3/4 16:04
 * @author: sanbo
 */
public class PkgList {

    public synchronized List<String> getAppPackageList() {


        if (apps != null) {
            return apps;
        }
        apps = new CopyOnWriteArrayList<String>();
        try {
            ShellUtils.getArrays("pm list packages", new ISayHello() {
                @Override
                public void onProcessLine(final String line) {
                    SystemUtils.runOnWorkThread(new Runnable() {
                        @Override
                        public void run() {
                            parseLine(apps, line);
                        }
                    });
                }
            }, false);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        if (apps.size() < 5) {
            try {
                PackageManager packageManager = mContext.getPackageManager();
                if (packageManager != null) {
                    List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
                    if (packageInfos.size() > 0) {
                        for (final PackageInfo info : packageInfos) {
                            addToMemory(info);
                        }
                    }
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
        if (!apps.contains(mContext.getPackageName())) {
            apps.add(mContext.getPackageName());
        }
        return apps;
    }

    private void addToMemory(PackageInfo info) {
        try {
            String pkg = info.packageName;
            if (!TextUtils.isEmpty(pkg) && !apps.contains(pkg)) {
                apps.add(pkg);
            }
        } catch (Throwable e) {
        }
    }


    private void parseLine(List<String> apps, String line) {
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

    public void add(String pkgName) {
        if (!TextUtils.isEmpty(pkgName) && apps != null && !apps.contains(pkgName)) {
            apps.add(pkgName);
        }
    }

    public void del(String pkgName) {
        if (!TextUtils.isEmpty(pkgName) && apps != null && apps.contains(pkgName)) {
            apps.remove(pkgName);
        }
    }
    /********************* get instance begin **************************/
    public static PkgList getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private PkgList initContext(Context context) {
        mContext = EContextHelper.getContext(context);
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final PkgList INSTANCE = new PkgList();
    }

    private PkgList() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/
    //若不考虑内存占用，可使用CopyOnWriteArrayList(线程安全)
//    private static List<String> apps = new CopyOnWriteArrayList<String>();
    private List<String> apps = null;
}
