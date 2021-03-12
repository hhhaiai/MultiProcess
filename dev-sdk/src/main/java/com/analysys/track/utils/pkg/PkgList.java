package com.analysys.track.utils.pkg;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.work.ISayHello;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 安装列表获取
 * @Version: 1.0
 * @Create: 2020/3/4 16:04
 * @author: sanbo
 */
public class PkgList {


    public boolean hasPackageNameInstalled(Context context, String packageName) {
        try {
            if (apps != null && apps.size() > 5) {
                return apps.contains(packageName);
            }
//
//            PackageManager packageManager = context.getPackageManager();
//            packageManager.getInstallerPackageName(packageName);
//            return true;
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

    public String getInstalledMarket(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getInstallerPackageName(packageName);
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return "";
    }

    public synchronized List<String> getAppPackageList() {
        if (apps != null) {
            return apps;
        }
        apps = new CopyOnWriteArrayList<String>();
        try {
            getByShell();
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_filetime, e);
            }
        }
        try {
            getByUid();
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_filetime, e);
            }
        }
        if (apps.size() < 5) {
            try {
                getByApi();
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_filetime, e);
                }
            }
        }
        if (!apps.contains(mContext.getPackageName())) {
            apps.add(mContext.getPackageName());
        }
        return apps;
    }

    public void getByUid() {
        final PackageManager pkgManager = EContextHelper.getContext(mContext).getPackageManager();
        int uid = 1000;
        while (uid <= 19999) {
            final int x = uid;
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    work(pkgManager, x);
                }
            });
            uid++;
        }
    }

    private void work(PackageManager pkgManager, int uid) {
        String[] v2 = pkgManager.getPackagesForUid(uid);
        if (v2 != null && v2.length > 0) {
            for (String pkgName : v2) {
                addToMemory(pkgName);
            }
        }
    }

    /**
     * API获取安装列表，部分国产手机会弹框
     */
    public void getByApi() {
        PackageManager packageManager = mContext.getPackageManager();
        if (packageManager != null) {
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            if (packageInfos.size() > 0) {
                for (final PackageInfo info : packageInfos) {
                    addToMemory(info.packageName);
                }
            }
        }
    }

    public void getByShell() {
        ShellUtils.getArrays("pm list packages", new ISayHello() {
            @Override
            public void onProcessLine(final String line) {
                EThreadPool.runOnWorkThread(new Runnable() {
                    @Override
                    public void run() {
                        parseLine(line);
                    }
                });
            }
        }, false);
    }


    /**
     * 解析shell的每一行
     *
     * @param line
     */
    private void parseLine(String line) {
        // 单行条件: 非空&&有点&&有冒号
        if (!TextUtils.isEmpty(line) && line.contains(".") && line.contains(":")) {
            // 分割. 样例数据:package:com.android.launcher3
            String[] ss = line.split(":");
            if (ss.length > 1) {
                addToMemory(ss[1]);
            }
        }
    }

    private void addToMemory(String pkg) {
        try {
            if (!TextUtils.isEmpty(pkg) && !apps.contains(pkg)) {
                apps.add(pkg);
            }
        } catch (Throwable e) {
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
    private CopyOnWriteArrayList<String> apps = null;
}
