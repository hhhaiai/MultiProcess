package com.eguan.imp;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.eguan.Constants;
import com.eguan.db.DBPorcesser;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.SPHodler;
import com.eguan.utils.commonutils.SystemUtils;

import java.util.ArrayList;
import java.util.List;

public class OCInfoManager {

    SPHodler spUtil = null;
    Context context;
    private static OCInfoManager instance = null;

    private OCInfoManager(Context context) {
        this.context = context;
        spUtil = SPHodler.getInstance(context);
    }

    public static synchronized OCInfoManager getInstance(Context context) {
        if (instance == null) {
            instance = new OCInfoManager(context);
        }
        return instance;
    }

    @SuppressWarnings("deprecation")
    public void getOCInfo() {
        if (!SystemUtils.checkPermission(context, "android.permission.GET_TASKS")) {
            return;
        }

        PackageManager pm = null;
        String packageName = "";
        try {
            ActivityManager actm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            pm = context.getPackageManager();
            List<RunningTaskInfo> tasks = actm.getRunningTasks(1);// 获取正在运行的任务列表
            if (tasks.size() <= 0) {
                return;
            }
            packageName = tasks.get(0).topActivity.getPackageName();// 得到当前正在运行的任务包名
            // PackageInfo pInfo = pm.getPackageInfo(packageName, 0);

            String lastPkgName = spUtil.getLastOpenPackgeName();

            if (TextUtils.isEmpty(packageName))
                return;
            // 是否首次打开
            if (lastPkgName.equals("")) {
                // 如果打开的不是系统应用或者浏览器，做首次存储
                insertShared(pm, packageName);
                return;

            } else if (!lastPkgName.equals("")) { // 是否首次打开

                // 如果打开的应用不是浏览器或系统应用，并且与上次缓存包名相同，跳出判断
                if (packageName.equals(lastPkgName)) {
                    return;
                } else if (!packageName.equals(lastPkgName)) {
                    // 如果打开的包名与缓存的包名不一致，存储数据并将包名做缓存
                    filterInsertOCInfo(Constants.APP_SWITCH, false);
                    insertShared(pm, packageName);
                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    /**
     * 缓存数据
     *
     * @param pm
     * @param pkgName
     */
    private void insertShared(PackageManager pm, String pkgName) {

        String appName = "", versionName = "", versionCode = "", appType = "OA";
        try {

            if (pm == null || TextUtils.isEmpty(pkgName))
                return;

            PackageInfo pInfo = pm.getPackageInfo(pkgName, 0);
            if ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    || pkgName.equals("com.android.browser")) {
                appType = "SA";
            }

            appName = pm.getApplicationLabel(pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)) + "";
            versionName = pm.getPackageInfo(pkgName, 0).versionName;
            versionCode = String.valueOf(pm.getPackageInfo(pkgName, 0).versionCode);

        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        String nowTime = System.currentTimeMillis() + "";
        spUtil.setLastOpenPackgeName(pkgName);
        spUtil.setLastOpenTime(nowTime);
        spUtil.setLastAppName(appName);
        spUtil.setLastAppVerison(
                versionName == null || versionName.equals("null") ? "1.0" : versionName + "|" + versionCode);
        spUtil.setAppType(appType);
    }

    public void filterInsertOCInfo(String switchType, boolean createThread) {

        String OldPkgName = spUtil.getLastOpenPackgeName();
        String appName = spUtil.getLastAppName();
        String appVersion = spUtil.getLastAppVerison();
        String openTime = spUtil.getLastOpenTime();
        Long closeTime = spUtil.getEndTime();
        String appType = spUtil.getAppType();
        if (OldPkgName.equals("") || appName.equals("") || appVersion.equals("") || openTime.equals("")) {
            return;
        }
        long time = closeTime - Long.parseLong(openTime);
        long minDuration = spUtil.getMinDuration();
        long maxDuration = spUtil.getMaxDuration();

        if (minDuration <= 0) {
            minDuration = Constants.SHORT_TIME;
        }
        if (maxDuration <= 0) {
            maxDuration = Constants.LONGEST_TIME;
        }

        if (minDuration <= time && time <= maxDuration) {

            List<OCInfo> list = new ArrayList<OCInfo>();
            OCInfo ocInfo = new OCInfo();
            ocInfo.setApplicationPackageName(OldPkgName);
            ocInfo.setApplicationName(appName);
            ocInfo.setApplicationVersionCode(appVersion);
            ocInfo.setApplicationOpenTime(openTime);
            ocInfo.setApplicationCloseTime(closeTime + "");
            ocInfo.setSwitchType(switchType);
            ocInfo.setApplicationType(appType);
            ocInfo.setCollectionType("1");
            list.add(ocInfo);
            insertForDataBase(list, createThread);// 保存上一个打开关闭记录信息
        }
        spUtil.setLastOpenPackgeName("");
        spUtil.setLastOpenTime("");
        spUtil.setLastAppName("");
        spUtil.setLastAppVerison("");
    }

    /**
     * 保存应用打开关闭入库,只有一条记录同时又打开和关闭时间，才进行入库缓存操作
     *
     * @param ocInfo
     */
    private void insertForDataBase(final List<OCInfo> ocInfo, boolean createThread) {

        if (ocInfo != null && !ocInfo.get(0).getApplicationOpenTime().equals("")
                && !ocInfo.get(0).getApplicationCloseTime().equals("")) {

            if (createThread) {
                insertForDataBase(ocInfo);
            } else {
                insertForDataBase(ocInfo);
            }
        }
    }

    private void insertForDataBase(List<OCInfo> ocInfo) {
        try {
            DBPorcesser tOperation = DBPorcesser.getInstance(context);
            tOperation.insertOCInfo(ocInfo);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }
}
