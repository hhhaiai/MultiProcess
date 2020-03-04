package com.analysys.track.internal.impl.oc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.DBConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.usm.USMImpl;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.service.AnalysysAccessibilityService;
import com.analysys.track.utils.AccessibilityHelper;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.data.Base64Utils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Copyright 2019 sanbo Inc. All rights reserved.
 * @Description: OC处理逻辑
 * @Version: 1.0
 * @Create: 2019-08-10 22:42:03
 * @author: sanbo
 */
public class OCImpl {


    /**
     * 处理OC采集信息
     */
    public void processOCMsg(final ECallBack callback) {
        try {
            // 根据OC间隔时间进行任务调整
            long durTime = getOCDurTime();
            if (durTime > 0) {
                // 多进程锁定文件工作。
                if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_OC, EGContext.TIME_SECOND * 2, System.currentTimeMillis())) {
                    SystemUtils.runOnWorkThread(new Runnable() {
                        @Override
                        public void run() {
                            processOC();
                            //oc开始处理重置标记位.
                            MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_OC, System.currentTimeMillis());
                            if (callback != null) {
                                callback.onProcessed();
                            }
                        }
                    });
                } else {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.v(BuildConfig.tag_oc, "多进程并发。停止处理 。。。");
                    }
                    return;
                }
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.v(BuildConfig.tag_oc, " 6.0以上版本. 停止处理");
                }
                // 6.0以上版本
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_oc,t);
            }
        }


    }


    /**
     * 真正的OC处理
     */
    public void processOC() {
        try {
            if (SystemUtils.isScreenOn(mContext)) {
//                fillData();
                if (!SystemUtils.isScreenLocked(mContext)) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.d(BuildConfig.tag_oc, " 屏幕不亮 且不锁屏，开始检测");
                    }
                    // 约束OC是否能工作
//                    boolean isAllowOC = PolicyImpl.getInstance(mContext)
//                            .getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_OC, true);
//                    boolean isAllowXXX = PolicyImpl.getInstance(mContext)
//                            .getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_XXX, true);
                    boolean isAllowOC = SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC, true);
                    boolean isAllowXXX = SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_XXX, true);
                    if (!isAllowOC && !isAllowXXX) {
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.d(BuildConfig.tag_oc, " 屏幕不亮 且不锁屏，不需要采集OC和XXX，即将停止工作");
                        }
                        return;
                    }
                    if (
//                            USMImpl.isUSMAvailable(mContext) &&
                            SPHelper.getBooleanValueFromSP(mContext,
                                    UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_OC, false)) {
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.d(BuildConfig.tag_oc, "辅助功能可用 并 被短路 , 停止OC工作");
                        }
                        return;
                    }
                    getInfoByVersion(isAllowOC, isAllowXXX);
                } else {
                    // 亮屏，未解锁也将内存数据缓存
                    processScreenOff();
                }
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.d(BuildConfig.tag_oc, " 屏幕不亮，停止处理。。保存数据");
                }
                processScreenOff();
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_oc,t);
            }
        }
    }

    /**
     * <pre>
     * OC获取，按照版本区别处理:
     *      1. 5.x以下系统API获取
     *      2. 5.x/6.x使用top/ps+proc来完成，数据同步给XXXInfo
     *      3. 7.x+获取活着服务.
     * </pre>
     *
     * @param isAllowOC
     * @param isAllowXXX
     */
    @SuppressWarnings("deprecation")
    public void getInfoByVersion(boolean isAllowOC, boolean isAllowXXX) {

        // 1. 声明xxx
        JSONObject xxx = null;

        // xxx demo数据
        //{"time":1563676428130,"ocr":["com.alipay.hulu","com.device"],"result":[{"pid":4815,"oomScore":41,"pkg":"com.device","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10219\/pid_4815","oomAdj":"0"},{"pid":3644,"oomScore":95,"pkg":"com.alipay.hulu","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10131\/pid_3644","oomAdj":"1"}]}

        // oc 允许采集
        if (isAllowOC) {
            // 有辅助功能停止其他方式处理
            if (!AccessibilityHelper.isAccessibilitySettingsOn(mContext, AnalysysAccessibilityService.class)) {
                // 获取XXX
                xxx = ProcUtils.getInstance(mContext).getRunningInfo();
                // 最新的列表
                JSONArray aliveList = null;
                if (xxx != null && xxx.has(ProcUtils.RUNNING_OC_RESULT)) {
                    aliveList = xxx.optJSONArray(ProcUtils.RUNNING_OC_RESULT);
                }

                if (Build.VERSION.SDK_INT < 21) {
                    if (PermissionUtils.checkPermission(mContext, Manifest.permission.GET_TASKS)) {
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.i(BuildConfig.tag_oc, " 4.x版本  有GET_TASKS权限。。。");
                        }
                        getRunningTasksByapiWhen4x(aliveList);
                    } else {
                        // 没权限，用xxxinfo方式收集
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.e(BuildConfig.tag_oc, " 4.x版本  没有GET_TASKS权限。。。");
                        }
                        getAliveAppByProc(aliveList);
                    }
                } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {// 5 6

                    // 如果开了USM则使用USM
                    if (SystemUtils.canUseUsageStatsManager(mContext)) {
                        processOCByUsageStatsManager(aliveList);
                    } else {
                        getAliveAppByProc(aliveList);
                    }
                } else if (Build.VERSION.SDK_INT < 26) { // 7
//                    // 如果开了USM则使用USM
//                    if (SystemUtils.canUseUsageStatsManager(mContext)) {
//                        processOCByUsageStatsManager(aliveList);
//                    } else {
//                        getRuningService();
//                    }
                }

            }

        }
        // xxx允许采集进行处理
        if (isAllowXXX) {
            parserXXXAndSave(xxx);
        }
    }


    private void getRuningService() {
        try {
            ActivityManager myManager = (ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);
            ArrayList<ActivityManager.RunningServiceInfo> runningService = null;
            if (myManager != null) {
                runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                        .getRunningServices(30);
            }
            PackageManager pm = mContext.getPackageManager();
            HashSet<String> pkgs = new HashSet<>();
            if (runningService != null) {
                for (int i = 0; i < runningService.size(); i++) {
                    //分割报名和进程名,样例:com.device:h
                    String name = runningService.get(i).process;
                    String[] split = name.split(":");
                    if (split != null || split.length > 0) {
                        String pkgName = split[0];
                        if (!TextUtils.isEmpty(pkgName)
                                && pkgName.contains(".")
                                && !pkgName.contains(":")
                                && !pkgName.contains("/")
                                && SystemUtils.hasLaunchIntentForPackage(pm, pkgName)) {
                            pkgs.add(pkgName);
                        }
                    }
                }
            }
            getAliveAppByProc(new JSONArray(pkgs));
        } catch (Throwable e) {

        }
    }

    /**
     * 根据XXX内容处理相应信息。样例数据 [com.device, com.alipay.hulu]
     *
     * @param aliveList
     */
    public void getAliveAppByProc(JSONArray aliveList) {

        // 1. 若无有效数据，停止处理

        if (aliveList == null || aliveList.length() < 1) {
            return;
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(BuildConfig.tag_oc, "\n内存里的列表数据[" + mOpenedPkgNameList.size() + "]");
            ELOG.i(BuildConfig.tag_oc, "\n内存里的MAP数据[" + mOpenedPkgNameAndInfoMap.size() + "]");
            ELOG.i(BuildConfig.tag_oc, "\n本次处理的数据[" + aliveList.length() + "]");
        }

        /**
         *  2. 双列表对比，生成需要处理的列表
         */

        //新增加列表: 新的有，老的没有
        List<String> needOpenList = new ArrayList<String>();
        // 需要闭合列表: 老的有.新的没有
        List<String> needCloseList = new ArrayList<String>();

        // 以新的列表为主遍历，老列表中不包含为条件，生成新增列表
        // 如内存列表: 1,2,3, 本次，3，4，5,应该生成==》needOpenList[4,5]
        for (int i = 0; i < aliveList.length(); i++) {
            String pkgName = aliveList.optString(i);
            if (!TextUtils.isEmpty(pkgName) && !mOpenedPkgNameList.contains(pkgName)) {
                if (!needOpenList.contains(pkgName)) {
                    needOpenList.add(pkgName);
                }
            }
        }

        // 以老列表为主遍历，新列表中不包含为条件，生成需要闭合列表
        // 如内存列表: 1,2,3, 本次，3，4，5,应该生成==》needCloseList[1,2]
        if (mOpenedPkgNameList.size() > 0) {
            List<String> converyList = JsonUtils.converyJSONArrayToList(aliveList);
            for (int i = 0; i < mOpenedPkgNameList.size(); i++) {
                String pkgName = mOpenedPkgNameList.get(i);
                if (!TextUtils.isEmpty(pkgName) && !converyList.contains(pkgName)) {
                    if (!needCloseList.contains(pkgName)) {
                        needCloseList.add(pkgName);
                    }
                }
            }
        }

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(BuildConfig.tag_oc, "-----needOpenList:" + needOpenList.toString());
            ELOG.i(BuildConfig.tag_oc, "-----needCloseList:" + needCloseList.toString());
        }


        /**
         * 3. 根据新增、保存列表进行处理
         */
        // 处理闭合列表
        if (needCloseList.size() > 0) {
            ContentValues info = null;
            // 先闭合数据
            for (String closePkg : needCloseList) {
                if (!TextUtils.isEmpty(closePkg)) {
                    info = null;
                    // 获取内存里的数据
                    info = mOpenedPkgNameAndInfoMap.get(closePkg);
                    if (info != null && info.size() > 0) {
                        // 补充上闭合时间 ACT不加密
                        info.put(DBConfig.OC.Column.ACT, String.valueOf(System.currentTimeMillis()));
                        info.put(DBConfig.OC.Column.RS, "1");
                        // 保存上一个打开关闭记录信息
                        TableProcess.getInstance(mContext).insertOC(info);
                    }
                }

            }
            // 再删除数据，直接删除会导致不可预知的问题。
            for (String closePkg : needCloseList) {
                if (!TextUtils.isEmpty(closePkg)) {
                    mOpenedPkgNameList.remove(closePkg);
                    mOpenedPkgNameAndInfoMap.remove(closePkg);
                }
            }
            // 清除缓存变量
            info = null;
            needCloseList.clear();
            needCloseList = null;
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(BuildConfig.tag_oc, "\n 闭合数据后，列表[" + mOpenedPkgNameList.size() + "]");
            ELOG.i(BuildConfig.tag_oc, "\n 闭合数据后，MAP[" + mOpenedPkgNameAndInfoMap.size() + "]");
        }

        // 增加新增加的数据
        if (needOpenList.size() > 0) {
            for (String openPkg : needOpenList) {
                if (!TextUtils.isEmpty(openPkg)) {
                    cacheDataToMemory(openPkg, UploadKey.OCInfo.COLLECTIONTYPE_PROC);
                }
            }
            needOpenList.clear();
            needOpenList = null;
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(BuildConfig.tag_oc, "\n 新增数据完毕，列表[" + mOpenedPkgNameList.size() + "]");
            ELOG.i(BuildConfig.tag_oc, "\n 新增数据完毕，MAP[" + mOpenedPkgNameAndInfoMap.size() + "]");
        }
    }


    /**
     * 解析XXX并且存储.处理逻辑: xxx允许采集， 且5.x以后版本，去除OCR数据，存储到数据库
     *
     * @param xxx
     */
    private void parserXXXAndSave(JSONObject xxx) {
        // 且5.x以后版本，去除OCR数据，存储到数据库
        if (Build.VERSION.SDK_INT > 20) {
            if (xxx == null) {
                xxx = ProcUtils.getInstance(mContext).getRunningInfo();
            }
            if (xxx != null && xxx.has(ProcUtils.RUNNING_OC_RESULT)) {
                xxx.remove(ProcUtils.RUNNING_OC_RESULT);
                TableProcess.getInstance(mContext).insertXXX(xxx);
            }
        }
    }


    /**
     * 获取正在运行的应用包名
     *
     * @param aliveList
     */
    @SuppressWarnings("deprecation")
    private void getRunningTasksByapiWhen4x(JSONArray aliveList) {
        String pkgName = "";
        ActivityManager am = null;
        try {
            if (mContext == null) {
                mContext = EContextHelper.getContext();
            }
            if (mContext != null) {
                am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks == null || tasks.size() <= 0) {
                    List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
                    for (ActivityManager.RunningAppProcessInfo appProcess : processInfos) {
                        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            pkgName = appProcess.processName;
                        }
                    }
                } else {
                    // 获取栈顶app的包名
                    pkgName = tasks.get(0).topActivity.getPackageName();
                }
            }

            if (!TextUtils.isEmpty(pkgName)) {
                processSignalPkgName(pkgName, UploadKey.OCInfo.COLLECTIONTYPE_RUNNINGTASK);
            } else {
                getAliveAppByProc(aliveList);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_oc,e);
            }
            getAliveAppByProc(aliveList);
        }
    }

    /**
     * 处理单个应用名字
     *
     * @param packageName
     * @param ct          采集方式 link{UploadKey.OCInfo.COLLECTIONTYPE_RUNNINGTASK/COLLECTIONTYPE_USAGESTATSMANAGER}
     */
    public void processSignalPkgName(String packageName, String ct) {
        try {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_oc, " 处理包名： " + packageName);
            }
            if (TextUtils.isEmpty(packageName)) {
                return;
            }

            // 包含。说明上次已经打开。keeping
            if (mOpenedPkgNameList.contains(packageName)) {
                // do nothing
            } else {
                ContentValues info = null;
                // 不包含。说明有变动，需要对应处理
                if (mOpenedPkgNameList.size() < 1) {
                    //内存没有，说明首次.
                    // 1. 清除内存
                    info = null;
                    mOpenedPkgNameList.clear();
                    mOpenedPkgNameAndInfoMap.clear();
                    // 2. 保存现在的数据
                    cacheDataToMemory(packageName, ct);
                } else {
                    // 闭合上一个, 存储
                    for (String pkg : mOpenedPkgNameList) {
                        info = null;
                        if (mOpenedPkgNameAndInfoMap.containsKey(pkg)) {
                            // 获取内存里的数据
                            info = mOpenedPkgNameAndInfoMap.get(pkg);
                            if (info != null && info.size() > 0) {
                                // 补充上闭合时间 ACT不加密
                                info.put(DBConfig.OC.Column.ACT, String.valueOf(System.currentTimeMillis()));
                                info.put(DBConfig.OC.Column.RS, "1");
                                // 保存上一个打开关闭记录信息
                                TableProcess.getInstance(mContext).insertOC(info);
                            }
                        }
                    }
                    info = null;
                    mOpenedPkgNameList.clear();
                    mOpenedPkgNameAndInfoMap.clear();
                    // 存储当前的
                    cacheDataToMemory(packageName, ct);
                }
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_oc,t);
            }

        }

    }

    /**
     * 将单个包名的应用缓存到内存中
     *
     * @param packageName
     * @param ct          采集来源类型，1-getRunningTask，2-读取proc，3-辅助功能，4-系统统计
     */
    public void cacheDataToMemory(String packageName, String ct) {
        ContentValues info = getInfoWhenFound(packageName, ct, UploadKey.OCInfo.SWITCHTYPE_GENERAL);
        if (info != null && info.size() > 0) {
            mOpenedPkgNameList.add(packageName);
            mOpenedPkgNameAndInfoMap.put(packageName, info);
        }
    }

    /**
     * 获取APP详情. 打开APP时获取详情
     *
     * @param packageName
     * @param ct          采集来源类型，1-getRunningTask，2-读取proc，3-辅助功能，4-系统统计
     * @param switchType  OC 切换的类型，1-正常使用，2-开关屏幕切换，3-服务重启
     * @return
     */
    private ContentValues getInfoWhenFound(String packageName, String ct, String switchType) {
        ContentValues info = new ContentValues();
        try {
            long insertTime = System.currentTimeMillis();
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            PackageInfo ii = pm.getPackageInfo(packageName, 0);

            //APN 加密
            info.put(DBConfig.OC.Column.APN, EncryptUtils.encrypt(mContext, packageName));


            // AOT 不加密
            info.put(DBConfig.OC.Column.AOT, String.valueOf(System.currentTimeMillis()));
            //NT不加密
            info.put(DBConfig.OC.Column.NT, NetworkUtils.getNetworkType(mContext));
            // 采集来源类型，1-getRunningTask，2-读取proc，3-辅助功能，4-系统统计
            info.put(DBConfig.OC.Column.CT, ct);
            //AVC 加密
            String avc = EncryptUtils.encrypt(mContext, ii.versionName + "|" + ii.versionCode);
            info.put(DBConfig.OC.Column.AVC, avc);
            info.put(DBConfig.OC.Column.DY, SystemUtils.getDay());
            // IT不加密
            info.put(DBConfig.OC.Column.IT, String.valueOf(insertTime));
            //  OC 切换的类型，1-正常使用，2-开关屏幕切换，3-服务重启
            info.put(DBConfig.OC.Column.AST, switchType);
            info.put(DBConfig.OC.Column.AT, AppSnapshotImpl.getInstance(mContext).getAppType(packageName));
            info.put(DBConfig.OC.Column.TI, Base64Utils.getTimeTag(insertTime));
            info.put(DBConfig.OC.Column.ST, "0");
            info.put(DBConfig.OC.Column.RS, "0");
//            info.put(DBConfig.OC.Column.ACT,  closeTime);

            //存在异常的case
            if (appInfo != null) {
                //AN 加密
                CharSequence cs = appInfo.loadLabel(pm);
                if (cs != null) {
                    info.put(DBConfig.OC.Column.AN, EncryptUtils.encrypt(mContext, String.valueOf(cs)));
                }
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_oc,t);
            }
        }
        return info;
    }


    /**
     * android 5以上，有UsageStatsManager权限可以使用的
     *
     * @param aliveList
     */
    @SuppressLint("WrongConstant")
    public void processOCByUsageStatsManager(JSONArray aliveList) {
        class RecentUseComparator implements Comparator<UsageStats> {
            @Override
            public int compare(UsageStats lhs, UsageStats rhs) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return Long.compare(rhs.getLastTimeUsed(), lhs.getLastTimeUsed());
                }
                return 0;
            }
        }
        try {
            UsageStatsManager usm = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                usm = (UsageStatsManager) mContext.getApplicationContext()
                        .getSystemService(Context.USAGE_STATS_SERVICE);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    usm = (UsageStatsManager) mContext.getApplicationContext()
                            .getSystemService("usagestats");
                }
            }
            if (usm == null) {
                getAliveAppByProc(aliveList);
                return;
            }
            long ts = System.currentTimeMillis();
            List<UsageStats> usageStats = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 10 * 1000, ts);
            }
            if (usageStats == null || usageStats.size() == 0) {
                getAliveAppByProc(aliveList);
                return;
            }
            Collections.sort(usageStats, new RecentUseComparator());
            String usmPkg = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                usmPkg = usageStats.get(0).getPackageName();
            }
            if (!TextUtils.isEmpty(usmPkg)) {
                processSignalPkgName(usmPkg, UploadKey.OCInfo.COLLECTIONTYPE_USAGESTATSMANAGER);
            } else {
                getAliveAppByProc(aliveList);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(BuildConfig.tag_oc,e);
            }
            getAliveAppByProc(aliveList);
        }
    }


    /************************************** OC间隔时间获取 ********************************************/

    /**
     * 获取OC轮训时间
     *
     * @return
     */
    public long getOCDurTime() {
        if (Build.VERSION.SDK_INT < 21) {
            return EGContext.TIME_SECOND * 5;
            //5 6 7
        } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 26) {
            return EGContext.TIME_SECOND * 30;
        } else {// 7以上不处理
            return -1;
        }
    }


    /************************************** 开关屏处理 ********************************************/
    /**
     * 处理关闭屏幕、打开屏幕. 多进程+多线程支持。 多进程1秒处理一次
     *
     * @param isScreenOn
     */
    public void processOCWhenScreenChange(boolean isScreenOn) {
        if (isScreenOn) {
            // 亮屏幕 不进行相应的操作。后台的时候 队列还一直在运行中。

        } else {
            // 灭屏幕处理
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext,
                    EGContext.FILES_SYNC_SCREEN_OFF_BROADCAST,
                    EGContext.TIME_SECOND, System.currentTimeMillis())) {
                SystemUtils.runOnWorkThread(new Runnable() {
                    @Override
                    public void run() {
                        processScreenOff();
                        MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SCREEN_OFF_BROADCAST, System.currentTimeMillis());
                    }
                });
            } else {
                // 多进程抢占
                return;
            }
        }
    }


    /**
     * 关闭屏幕处理，内存里所有数据都保存到DB
     */
    public void processScreenOff() {

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(BuildConfig.tag_oc, "\n 收到关闭屏幕广播，列表[" + mOpenedPkgNameList.size() + "]");
        }

        if (mOpenedPkgNameList.size() > 0) {
            // 闭合上一个, 存储
            for (int i = 0; i < mOpenedPkgNameList.size(); i++) {
                String pkg = mOpenedPkgNameList.get(i);
                if (!TextUtils.isEmpty(pkg) && mOpenedPkgNameAndInfoMap.containsKey(pkg)) {
                    // 获取内存里的数据
                    ContentValues info = mOpenedPkgNameAndInfoMap.get(pkg);
                    if (info != null && info.size() > 0) {
                        // 补充上闭合时间 ACT不加密
                        info.put(DBConfig.OC.Column.ACT, String.valueOf(System.currentTimeMillis()));
                        // 设置数据产生时机
                        info.put(DBConfig.OC.Column.AST, UploadKey.OCInfo.SWITCHTYPE_CLOSE_SCREEN);
                        info.put(DBConfig.OC.Column.RS, "1");
                        // 保存上一个打开关闭记录信息
                        TableProcess.getInstance(mContext).insertOC(info);
                    }
                    info = null;
                }
            }
        }
        mOpenedPkgNameList.clear();
        mOpenedPkgNameAndInfoMap.clear();
    }

    /************************************** 单例和对象声明 ********************************************/

    private static class Holder {
        private static final OCImpl INSTANCE = new OCImpl();
    }

    private OCImpl() {
    }

    public static OCImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext();
        }
        return Holder.INSTANCE;
    }

    private Context mContext;
    // 内存中的上次打开列表
    private List<String> mOpenedPkgNameList = new ArrayList<String>();
    // 内存里存在上次打开APP名字和app详情。
    private Map<String, ContentValues> mOpenedPkgNameAndInfoMap = new HashMap<String, ContentValues>();

}
