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

import java.util.HashSet;
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
                // 基于弹框考虑,尽量少使用该api接口
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
            public void onProcessLine(String line) {
                parseLine(line);
//                EThreadPool.runOnWorkThread(new Runnable() {
//                    @Override
//                    public void run() {
//                parseLine(line);
//                    }
//                });
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

    private static HashSet<String> catchPackage = new HashSet<>();

    /**
     * getLaunchIntentForPackage 这个方法某些设备比较耗时 引起波动, 在这里缓存一下
     *
     * @param manager
     * @param packageName
     * @return
     */
    public static boolean hasLaunchIntentForPackage(PackageManager manager, String packageName) {
        try {
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            if (catchPackage.contains(packageName)) {
                return true;
            }
            if (manager == null) {
                Context c = EContextHelper.getContext();
                if (c != null) {
                    manager = c.getPackageManager();
                }
            }
            if (manager == null) {
                return false;
            }
            if (manager.getLaunchIntentForPackage(packageName) != null) {
                catchPackage.add(packageName);
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 确定应用是否安装
     *
     * @param packageName
     * @return
     */
    public boolean isInstall(String packageName) {
        try {
            if (apps == null || apps.size() < 5) {
                getAppPackageList();
            }

            boolean result = apps.contains(packageName);
            if (!result) {
                try {
                    PackageManager pm = EContextHelper.getContext(mContext).getPackageManager();
                    pm.getPackageInfo(packageName, 0);
                    result = true;
                } catch (Throwable e) {
                    result = false;
                }
            }
            return result;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

    /**
     * @param context
     * @param packageName
     * @return
     */
    public String getInstalledMarket(Context context, String packageName) {
        try {
            PackageManager packageManager = EContextHelper.getContext(context).getPackageManager();
            return packageManager.getInstallerPackageName(packageName);
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return "";
    }

    /********************* get instance begin **************************/
    public static PkgList getInstance(Context context) {
        HLODER.INSTANCE.mContext = EContextHelper.getContext(context);
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
