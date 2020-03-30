package com.analysys.track.internal.impl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.PkgList;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 安装列表获取
 * @Version: 1.0
 * @Create: 2019-08-06 19:13:40
 * @author: ly
 */
public class AppSnapshotImpl {

    /**
     * 应用列表
     *
     * @param callBack
     */
    public void snapshotsInfo(final ECallBack callBack) {
        try {

            // 策略不允许安装列表模快工作, 则停止工作。
            if (!SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, true)) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_snap, "动态调整关闭采集安装列表。。。");
                }
                if (callBack != null) {
                    callBack.onProcessed();
                }
                return;
            }
            long now = System.currentTimeMillis();
            // 获取下发的间隔时间
//            long durByPolicy = PolicyImpl.getInstance(mContext).getSP().getLong(EGContext.SP_SNAPSHOT_CYCLE, EGContext.TIME_HOUR * 3);
            long durByPolicy = SPHelper.getIntValueFromSP(mContext, EGContext.SP_SNAPSHOT_CYCLE, EGContext.TIME_HOUR * 3);

            // 3秒内只能操作一次
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_APPSNAPSHOT, EGContext.TIME_SECOND * 3, now)) {

                long time = SPHelper.getLongValueFromSP(mContext, EGContext.SP_APP_SNAP, 0);
                long dur = now - time;
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_snap, "通过多进程验证。  time： " + time + " ----间隔：" + dur);
                }

                //大于三个小时才可以工作
                if (dur > durByPolicy || time == 0) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_snap, " 大于3小时可以开始工作 ");
                    }
                    SPHelper.setLongValue2SP(mContext, EGContext.SP_APP_SNAP, now);
                    SystemUtils.runOnWorkThread(new Runnable() {
                        @Override
                        public void run() {
                            getSnapShotInfo();
                            MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_APPSNAPSHOT, System.currentTimeMillis());
                            if (callBack != null) {
                                callBack.onProcessed();
                            }

                        }
                    });
                } else {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_snap, " 小于3小时");
                    }
                    //多进程解锁
                    MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_LOCATION, time);
                    if (callBack != null) {
                        callBack.onProcessed();
                    }
                }

            } else {
                if (BuildConfig.logcat) {
                    ELOG.d(BuildConfig.tag_snap, "多进程并发，停止操作。。。。");
                }
                if (callBack != null) {
                    callBack.onProcessed();
                }
                return;
            }

        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_snap, t);
            }
        }
    }
//    // 获取下次应该工作的时间
//    public long getDurTime() {
//        // 获取时间间隔
//        long durByPolicy = SPHelper.getIntValueFromSP(mContext, EGContext.SP_SNAPSHOT_CYCLE, EGContext.TIME_HOUR * 3);
//        // 获取上次运行时间
//        long time = SPHelper.getLongValueFromSP(mContext, EGContext.SP_APP_SNAP, 0);
//        // 获取上次运行到现在的时间间隔
//        long dur = System.currentTimeMillis() - time;
//        return durByPolicy - dur;
//    }

    public void getSnapShotInfo() {
        try {
            // 1. 获取现在的安装列表。所有标志位都是default(3)
            List<JSONObject> memoryData = getCurrentSnapshots();
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_snap, " 获取安装列表: " + memoryData.size());
            }

            if (memoryData.size() < 1) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_snap, " 获取安装列表失败。。 ");
                }
                return;
            }

            // 2. 获取DB数据
            JSONArray dbData = TableProcess.getInstance(mContext).selectSnapshot(EGContext.LEN_MAX_UPDATE_SIZE);
            if (dbData.length() < 5) {
                //DB没存数据,存入. 兼容场景首次、不允许采集->允许采集
                TableProcess.getInstance(mContext).insertSnapshot(memoryData);
            } else {
                // 双列表对比，处理
                checkDiff(dbData, memoryData);
            }
            memoryData.clear();
            dbData = null;
            memoryData = null;
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_snap, t);
            }
        }
    }

    //    private final String SHELL_PM_LIST_PACKAGES = "pm list packages";// all
//    private final String APP_LIST_SYSTEM = "pm list packages -s";// system
//    // private final String APP_LIST_USER = "pm list packages -3";// third party
//    // 获取系统应用列表
//    private final Set<String> mSystemAppSet = new HashSet<String>();
    //    private boolean isSnapShotBlockRunning = false;
    private Context mContext;

    public static AppSnapshotImpl getInstance(Context context) {
        return Holder.INSTANCE.initContext(context);
    }

    /**
     * 内存列表和数据库安装列表对比
     *
     * @param dbData
     * @param memoryData
     */
    private void checkDiff(JSONArray dbData, List<JSONObject> memoryData) {
        /**
         *  阶段一、双列表对比，生成需要处理的列表
         *
         *  // APN
         *  UploadKey.AppSnapshotInfo.ApplicationPackageName
         *  //AN
         *  UploadKey.AppSnapshotInfo.ApplicationName
         *  //AVC
         *  UploadKey.AppSnapshotInfo.ApplicationVersionCode
         *  // AT
         *  UploadKey.AppSnapshotInfo.ActionType
         *  //ATh
         *  UploadKey.AppSnapshotInfo.ActionHappenTime
         *
         */

        // 1. dbData生成MAP
        Map<String, JSONObject> dbMap = new HashMap<String, JSONObject>();
        //先单节点遍历生成，内存map
        for (int i = 0; i < dbData.length(); i++) {
            JSONObject dbJson = dbData.optJSONObject(i);
            if (dbJson == null || dbJson.length() < 1) {
                continue;
            }
            String apn = dbJson.optString(UploadKey.AppSnapshotInfo.ApplicationPackageName);
            dbMap.put(apn, dbJson);
        }
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_snap, " DB存储数据:" + dbMap.size());
        }
        // 2. memoryData数据生成map
        Map<String, JSONObject> memMap = new HashMap<String, JSONObject>();
        for (int j = 0; j < memoryData.size(); j++) {
            JSONObject memJson = memoryData.get(j);
            //最小粒径控制，单次无效。则跳过节点循环
            if (memJson == null || memJson.length() < 1) {
                continue;
            }
            String apn = memJson.optString(UploadKey.AppSnapshotInfo.ApplicationPackageName);
            memMap.put(apn, memJson);
        }
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_snap, " 内存存储数据:" + memMap.size());
        }


        /**
         * 阶段二、根据对比，处理对应数据。
         * 新增[内存有，DB没有]
         * 删除[DB有.内存没有]
         * 更改[版本号不一致,使用新的-适应内存数据遍历]
         */
        PackageManager pm = mContext.getPackageManager();
        for (int i = 0; i < dbData.length(); i++) {
            try {
                JSONObject dbJson = dbData.optJSONObject(i);
                if (dbJson == null || dbJson.length() < 1) {
                    continue;
                }
                String apn = dbJson.optString(UploadKey.AppSnapshotInfo.ApplicationPackageName);

                // 内存没有。DB有 -->删除列表
                if (!memMap.containsKey(apn)) {
                    try {
                        PackageInfo pi = pm.getPackageInfo(apn, 0);
                        String avc = pi.versionName + "|" + pi.versionCode;
                        TableProcess.getInstance(mContext).updateSnapshot(apn, EGContext.SNAP_SHOT_UNINSTALL, avc);
                    } catch (Throwable e) {
                    }
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_snap, e);
                }
            }
        }
        for (int j = 0; j < memoryData.size(); j++) {
            try {
                JSONObject memJson = memoryData.get(j);
                //最小粒径控制，单次无效。则跳过节点循环
                if (memJson == null || memJson.length() < 1) {
                    continue;
                }
                String memApn = memJson.optString(UploadKey.AppSnapshotInfo.ApplicationPackageName);
                // 内存有，DB没有--> 插入
                if (!dbMap.containsKey(memApn)) {
                    try {
                        PackageInfo pi = pm.getPackageInfo(memApn, 0);
                        if (SystemUtils.hasLaunchIntentForPackage(pm, memApn)) {
                            JSONObject info = getAppInfo(pi, pm, EGContext.SNAP_SHOT_INSTALL);
                            if (info != null && info.length() > 0) {
                                TableProcess.getInstance(mContext).insertSnapshot(info);
                            }
                        }
                    } catch (Throwable e) {
                    }
                } else {
                    String memAvc = memJson.optString(UploadKey.AppSnapshotInfo.ApplicationVersionCode);
                    JSONObject dbJson = dbMap.get(memApn);
                    String dbAvc = dbJson.optString(UploadKey.AppSnapshotInfo.ApplicationVersionCode);
                    //版本不一致
                    if (!memAvc.equals(dbAvc)) {
                        TableProcess.getInstance(mContext).updateSnapshot(memApn, EGContext.SNAP_SHOT_UPDATE, memAvc);
                    }
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_snap, e);
                }
            }
        }

        /**
         * 阶段三、清除数据
         */
        memMap.clear();
        dbMap.clear();
        memMap = null;
        dbMap = null;

    }

//    /**
//     * shell方式获取文件名字，然后解析出app详情
//     *
//     * @param mContext
//     * @param tag
//     * @param appList
//     * @return
//     */
//    private List<JSONObject> getAppInfosFromShell(Context mContext, String tag, List<JSONObject> appList) {
//        try {
//            JSONObject appInfo;
//            if (appList == null) {
//                appList = new ArrayList<JSONObject>();
//            }
//            Set<String> result = new HashSet<>();
//            PackageManager pm = mContext.getPackageManager();
//            result = getPkgNamesByShell(result, SHELL_PM_LIST_PACKAGES);
//            PackageInfo pi = null;
//            for (String pkgName : result) {
//                try {
//                    if (!TextUtils.isEmpty(pkgName) && SystemUtils.hasLaunchIntentForPackage(pm, pkgName)) {
//                        pi = mContext.getPackageManager().getPackageInfo(pkgName, 0);
//                        appInfo = AppSnapshotImpl.getInstance(mContext).getAppInfo(pi, pm, tag);
//                        if (!appList.contains(appInfo)) {
//                            appList.add(appInfo);
//                        }
//
//                    }
//                } catch (Throwable e) {
//                }
//            }
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(BuildConfig.tag_snap, e);
//            }
//        }
//        return appList;
//    }
//
//    /**
//     * 通过shell获取安装列表
//     *
//     * @param appSet
//     * @param shell
//     * @return
//     */
//    private Set<String> getPkgNamesByShell(Set<String> appSet, String shell) {
//        // Set<String> set = new HashSet<String>();
//        String result = ShellUtils.shell(shell);
//        if (!TextUtils.isEmpty(result) && result.contains("\n")) {
//            String[] lines = result.split("\n");
//            if (lines.length > 0) {
//                String line = null;
//                for (int i = 0; i < lines.length; i++) {
//                    try {
//                        line = lines[i];
//                        // 单行条件: 非空&&有点&&有冒号
//                        if (!TextUtils.isEmpty(line) && line.contains(".") && line.contains(":")) {
//                            // 分割. 样例数据:<code>package:com.android.launcher3</code>
//                            String[] ss = line.split(":");
//                            if (ss != null && ss.length > 1) {
//                                String packageName = ss[1];
//                                appSet.add(packageName);
//                            }
//                        }
//                    } catch (Throwable e) {
//                    }
//                }
//            }
//        }
//        return appSet;
//    }


    public String getAppType(String pkg) {
        return isSystemApps(pkg) ? UploadKey.OCInfo.APPLICATIONTYPE_SYSTEM_APP : UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP;
    }

    /**
     * 获取应用列表快照
     */
    private List<JSONObject> getCurrentSnapshots() {
        List<JSONObject> list = new ArrayList<JSONObject>();

        List<String> applist = PkgList.getInstance(mContext).getAppPackageList();
        if (applist != null && applist.size() > 0) {
            PackageManager pm = mContext.getApplicationContext().getPackageManager();
            for (String pkgName : applist) {
                try {
                    if (!TextUtils.isEmpty(pkgName) && SystemUtils.hasLaunchIntentForPackage(pm, pkgName)) {
                        PackageInfo pi = mContext.getPackageManager().getPackageInfo(pkgName, 0);
                        JSONObject appInfo = getAppInfo(pi, pm, EGContext.SNAP_SHOT_DEFAULT);
                        if (appInfo != null && !list.contains(appInfo)) {
                            list.add(appInfo);
                        }
                    }
                } catch (Throwable e) {
                }
            }
        }

        return list;
    }

//    /**
//     * 获取安装列表和对应的调试状态
//     *
//     * @return
//     */
//    private List<JSONObject> getAppDebugStatus() {
//        List<JSONObject> list = new ArrayList<JSONObject>();
//        try {
//            List<String> applist = PkgList.getAppPackageList(mContext);
//            if (applist != null && applist.size() > 0) {
//                for (String pkg : applist) {
//                    if (!TextUtils.isEmpty(pkg)) {
//                        JSONObject appInfo = new JSONObject();
//                        appInfo.put(EGContext.TEXT_DEBUG_APP, pkg);
//                        appInfo.put(EGContext.TEXT_DEBUG_STATUS, SystemUtils.isApkDebugable(mContext, pkg));
//                        if (!list.contains(appInfo)) {
//                            list.add(appInfo);
//                        }
//                    }
//                }
//            }
//
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(BuildConfig.tag_snap, e);
//            }
//        }
//        return list;
//    }

    /**************************************  处理广播消息 ******************************************************/

    /**
     * 处理消息中的应用安装、卸载、更新广播
     *
     * @param pkgName
     * @param type
     * @param lockFileName
     */
    public void processAppModifyMsg(final String pkgName, final int type, final String lockFileName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
        if (BuildConfig.logcat) {
            ELOG.d(BuildConfig.tag_snap, " 处理广播接收到的信息 包:" + pkgName + "----type: " + type);
        }
        // 数据库操作修改包名和类型
        SystemUtils.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                realProcessInThread(type, pkgName, lockFileName);
            }
        });
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
    public boolean isSystemApps(String pkg) {
            try {
                // 1. 使用系统方法判断
                mContext = EContextHelper.getContext();
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
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_snap, e);
                }
            }


        return false;
    }

    /**************************************  单例和变量 ******************************************************/

    private AppSnapshotImpl() {
    }


    private static class Holder {
        private static final AppSnapshotImpl INSTANCE = new AppSnapshotImpl();
    }

    /**
     * 获取APP详情
     *
     * @param pkgInfo
     * @param packageManager
     * @param tag
     * @return
     */
    @SuppressWarnings("deprecation")
    private JSONObject getAppInfo(PackageInfo pkgInfo, PackageManager packageManager, String tag) {
        JSONObject appInfo = new JSONObject();

        try {
            String pkg = pkgInfo.packageName;
//            if (!TextUtils.isEmpty(pkg) && pkg.contains(".") && SystemUtils.hasLaunchIntentForPackage(packageManager, pkg)) {
            if (!TextUtils.isEmpty(pkg) && pkg.contains(".")) {
                JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ApplicationPackageName,
                        pkgInfo.packageName, DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
                try {
                    JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ApplicationName,
                            String.valueOf(pkgInfo.applicationInfo.loadLabel(packageManager)),
                            DataController.SWITCH_OF_APPLICATION_NAME);
                } catch (Throwable e) {
                }
                JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ApplicationVersionCode,
                        pkgInfo.versionName + "|" + pkgInfo.versionCode, DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
                JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ActionType, tag,
                        DataController.SWITCH_OF_ACTION_TYPE);
                JsonUtils.pushToJSON(mContext, appInfo, UploadKey.AppSnapshotInfo.ActionHappenTime,
                        String.valueOf(System.currentTimeMillis()), DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
            }
        } catch (Throwable e) {
        }

        return appInfo;
    }

    private void realProcessInThread(int type, String pkgName, String lockFileName) {
        try {
            PackageManager pm = mContext.getPackageManager();
            if (type == 0) {
                PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                // SNAP_SHOT_INSTALL 解锁
                if (pi != null && SystemUtils.hasLaunchIntentForPackage(pm, pkgName)) {
                    JSONObject obj = getAppInfo(pi, pm, EGContext.SNAP_SHOT_INSTALL);
                    if (obj != null && obj.length() > 0) {
                        TableProcess.getInstance(mContext).insertSnapshot(obj);
                    }
                }
            } else if (type == 1) {

                if (BuildConfig.logcat) {
                    ELOG.d(BuildConfig.tag_snap, " 真正处理卸载...." + pkgName);
                }
                // 卸载时候，不能获取版本，会出现解析版本异常
                TableProcess.getInstance(mContext).updateSnapshot(pkgName, EGContext.SNAP_SHOT_UNINSTALL, "");
                // SNAP_SHOT_UNINSTALL 解锁
            } else if (type == 2) {
                PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                String avc = pi.versionName + "|" + pi.versionCode;
                if (BuildConfig.logcat) {
                    ELOG.d(BuildConfig.tag_snap, " 真正处理更新...." + pkgName + "----- " + avc);
                }
                TableProcess.getInstance(mContext).updateSnapshot(pkgName, EGContext.SNAP_SHOT_UPDATE, avc);
                // SNAP_SHOT_UPDATE 解锁
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_snap, e);
            }
        }
        if (!TextUtils.isEmpty(lockFileName)) {
            MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, lockFileName, System.currentTimeMillis());
        }
    }

    private AppSnapshotImpl initContext(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }


}
