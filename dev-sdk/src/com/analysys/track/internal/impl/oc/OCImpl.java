package com.analysys.track.internal.impl.oc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.db.TableOC;
import com.analysys.track.db.TableXXXInfo;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.service.AnalysysAccessibilityService;
import com.analysys.track.utils.AccessibilityHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OCImpl {

    public static long mLastAvailableOpenOrCloseTime = -1;
    /**
     * mCache为Android4.x正在运行的app列表的内存变量值
     */
    public static JSONObject mCache = null;
    /**
     * mRunningApps为Android5/6正在运行的app列表的内存变量值
     */
    private static List<JSONObject> mRunningApps = null;
    private static boolean isOCBlockRunning = false;
    String pkgName = null;
    private Context mContext;
    private String mLastPkgName = "";
    private long lastAvailableTime = -1;

    private OCImpl() {
    }

    public static OCImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * OC 信息采集
     */
    public void ocInfo() {
        try {
            long currentTime = System.currentTimeMillis();
            if (Build.VERSION.SDK_INT < 21) {
                MessageDispatcher.getInstance(mContext).ocInfo(EGContext.OC_CYCLE);
                if (MultiProcessChecker.isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_OC, EGContext.OC_CYCLE,
                        currentTime)) {
                    MultiProcessChecker.setLockLastModifyTime(mContext, EGContext.FILES_SYNC_OC, currentTime);
                } else {
                    return;
                }
            } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {
                MessageDispatcher.getInstance(mContext).ocInfo(EGContext.OC_CYCLE_OVER_5);
                if (MultiProcessChecker.isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_OC, EGContext.OC_CYCLE_OVER_5,
                        currentTime)) {
                    MultiProcessChecker.setLockLastModifyTime(mContext, EGContext.FILES_SYNC_OC, currentTime);
                } else {
                    return;
                }
            } else {// 6以上不处理
                return;
            }
            if (!isOCBlockRunning) {
                isOCBlockRunning = true;
            } else {
                return;
            }
            if (SystemUtils.isMainThread()) {
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        processOC();
                    }
                });
            } else {
                processOC();
            }
        } catch (Throwable t) {

        } finally {
            isOCBlockRunning = false;
        }

    }

    /**
     * 真正的OC处理
     */
    public void processOC() {
        try {
//            if(!ProcessManager.getIsCollected()){
//                return;
//            }
            // 亮屏幕工作
            if (SystemUtils.isScreenOn(mContext)) {
//                fillData();
                if (!SystemUtils.isScreenLocked(mContext)) {
                    // 约束OC是否能工作
                    boolean isOCCollectedByPolicy = PolicyImpl.getInstance(mContext)
                            .getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC, true);
                    boolean isXXXCollectedByPolicy = PolicyImpl.getInstance(mContext)
                            .getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_XXX, true);
                    if (!isOCCollectedByPolicy && !isXXXCollectedByPolicy) {
                        return;
                    }
                    JSONObject xxxInfo = null;
                    getInfoByVersion(isOCCollectedByPolicy, isXXXCollectedByPolicy, xxxInfo);
                }
            } else {
                closeOC(false, System.currentTimeMillis());
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
    }

    /**
     * 补数逻辑
     *
     * @param needCalculateTime
     * @param closeTime
     */
    public void closeOC(boolean needCalculateTime, long closeTime) {
        try {
            // String lastAvailableTime = "";
            if (Build.VERSION.SDK_INT < 21) {// 4.x
                if (needCalculateTime && (System.currentTimeMillis() - closeTime < EGContext.OC_CYCLE)) {// 两次时间间隔如果小于5s,则无效
                    if (mCache != null) {
                        mCache.remove(EGContext.LAST_OPEN_TIME);
                        mCache = null;
                    }
                    return;
                } else {// 有效入库
                    if (mCache != null) {
                        mCache.put(EGContext.END_TIME, mLastAvailableOpenOrCloseTime);
                    } else {
                        return;
                    }
                    filterInsertOCInfo(EGContext.CLOSE_SCREEN);
                }
            } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {// 5/6
                if (needCalculateTime && (System.currentTimeMillis() - closeTime < EGContext.OC_CYCLE_OVER_5)) {// 无效
                    return;
                } else {
                    /**
                     * 读取数据库数据入缓存，只读取一次，缓存先改然后与数据库同步
                     */
//                    lastAvailableTime = SPHelper.getStringValueFromSP(mContext,EGContext.LAST_OPEN_TIME, "");
                    fillData();// 批量入库补数
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
    }

    /**
     * 补数逻辑，在屏幕亮屏但是正在锁屏未解锁的时候，对closeTime为空的数据进行补数操作；
     * 在opten时间和现在时间之间随机一个值：差值在5min左右，直接补数，对于小于5min的频繁的开锁屏不做处理，后续依然有补数操作
     * 为了保险起见，如果屏幕亮屏紧随解锁屏，则在正常状态下进行一批的补数操作
     */
    public void fillData() {
        List<JSONObject> fillDataList = null;
        int blankCount = 0;
        try {
            /**
             * 1.如果内存没数据则从db读取一次，然后赋值给内存变量，然后做数据闭合 2.如果有数据就用内存数据做闭合 3.闭合完内存数据同步给db
             */
            if (mRunningApps == null || mRunningApps.size() < 1) {
                return;
            }
            JSONObject json = null;
            fillDataList = new ArrayList<JSONObject>();
            for (int i = 0; i < mRunningApps.size(); i++) {
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    break;
                }
                json = mRunningApps.get(i);
                String openTime = json.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime);
                long randomCloseTime = SystemUtils.getCloseTime(Long.parseLong(openTime));
                if (randomCloseTime == -1) {// 小于轮询周期的频繁的开锁屏不做处理
                    continue;
                }
                String apn = json.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                if (!TextUtils.isEmpty(apn)) {
                    mRunningApps.remove(json);
                    json.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, String.valueOf(randomCloseTime));// 再随机为了防止结束时间一样
                    json.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.CLOSE_SCREEN);
                    fillDataList.add(json);
                } else {
                    blankCount += 1;
                }
            }
            // 一次应用操作闭合，更新OCCunt表，打开次数、应用运行状态
            TableOC.getInstance(mContext).insertArray(fillDataList);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
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
     */
    @SuppressWarnings("deprecation")
    public void getInfoByVersion(boolean isOCCollectedByPolicy, boolean isXXXCollectedByPolicy, JSONObject obj) {
        // 1. 获取info.
        obj = ProcUtils.getInstance(mContext).getRunningInfo();
//        L.info(mContext,"getInfoByVersion--RunningInfo---->" + obj);
        if (obj == null || obj.length() <= 0) {
            return;
        }
        // 2. 解析INFO详情
        Set<String> nameSet = new HashSet<String>();
        long now = System.currentTimeMillis();
        if (obj.has(ProcUtils.RUNNING_OC_RESULT)) {
            nameSet = JsonUtils.transferStringArray2Set(obj.optString(ProcUtils.RUNNING_OC_RESULT));
        }
        if (obj.has(ProcUtils.RUNNING_TIME)) {
            now = obj.optLong(ProcUtils.RUNNING_TIME);
        }
        // 4.x和以下版本判断,逻辑: 有权限申请系统API获取; 无权限直接使用5.x/6.x版本
        if (Build.VERSION.SDK_INT < 21) {// 4.x
            if (PermissionUtils.checkPermission(mContext, Manifest.permission.GET_TASKS)) {
                getRunningTasks();
            } else {// 没权限，用xxxinfo方式收集
                getProcApps(nameSet, now);
            }
        } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {// 5/6
            /**
             * 样例数据.
             * <code>
             *     {"time":1564897235241,"ocr":["com.alipay.hulu","com.google.android.googlequicksearchbox"],"result":[{"pid":3682,"oomScore":105,"pkg":"com.alipay.hulu","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10131\/pid_3682","oomAdj":"1"},{"pid":3538,"oomScore":59,"pkg":"com.google.android.googlequicksearchbox","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10031\/pid_3538","oomAdj":"0"}]}
             * </code>
             */
            // 3. 根据是否采集进行分支判断
            if (isXXXCollectedByPolicy) {
                // 3.1.1 写入XXX
                TableXXXInfo.getInstance(mContext).insert(obj);
//                // 3.1.2 xxx是否收集, 清除RESULT
//                obj.remove(ProcUtils.RUNNING_RESULT);
                // 3.1.3 判断oc是否需要收集
                if (isOCCollectedByPolicy) {
                    ocCollection(nameSet, now);
                }
                // 3.1.4 清除内存数据
//                ocr = null;
                obj = null;
            } else {
                // 3.2.1 xxx不收集
                if (isOCCollectedByPolicy) {
                    ocCollection(nameSet, now);
                }
//                ocr = null;
                obj = null;
            }
        } else {
        }
    }

    /**
     * 辅助功能-->UsageStatsManager-->ocr
     *
     * @param ocr
     * @param now
     */
    private void ocCollection(Set<String> ocr, long now) {
        // 如果辅助功能开启则使用辅助功能
        if (!AccessibilityHelper.isAccessibilitySettingsOn(mContext, AnalysysAccessibilityService.class)) {
            // 如果开了USM则使用USM
            if (SystemUtils.canUseUsageStatsManager(mContext)) {
                processOCByUsageStatsManager(ocr, now);
            } else {
                // 否则使用ocr
                getProcApps(ocr, now);
            }
        }
    }

    /**
     * getRunningTask、辅助功能 OC 信息采集
     */
    public void RunningApps(String pkgName, int collectionType) {
        try {
            this.pkgName = pkgName;
            if (TextUtils.isEmpty(pkgName)) {
                return;
            }
            if (mRunningApps != null && mRunningApps.size() > 0) {
                repeatHandle(mRunningApps);
                if (mRunningApps != null && mRunningApps.size() > 0) {
                    // 完成一次闭合，存储到OC表
                    // 更新缓存表
                    closeState2DB(mRunningApps);
                }
                if (!TextUtils.isEmpty(pkgName)) {
                    updateCache(pkgName, collectionType);
                }
            } else {
                updateCache(pkgName, collectionType);
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
    }

    /**
     * 获取正在运行的应用包名
     */
    @SuppressWarnings("deprecation")
    private void getRunningTasks() {
        String pkgName = "";
        ActivityManager am = null;
        try {
            if (mContext == null) {
                mContext = EContextHelper.getContext(mContext);
            }
            if (mContext != null) {
                am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks == null || tasks.size() <= 0) {
                    getRunningProcess(am);
                    return;
                }
                // 获取栈顶app的包名
                pkgName = tasks.get(0).topActivity.getPackageName();
                processPkgName(pkgName);
            }

        } catch (Throwable e) {
            getRunningProcess(am);
        }
    }

    private void getRunningProcess(ActivityManager am) {
        try {
            List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
            if (processInfos == null || processInfos.size() <= 0) {
                return;
            }
            for (ActivityManager.RunningAppProcessInfo appProcess : processInfos) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    processPkgName(appProcess.processName);
                }
            }
        } catch (Throwable tw) {
        }
    }

    /**
     * 根据包名进行一系列的处理
     *
     * @param packageName
     */
    private void processPkgName(String packageName) {
        try {
            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            JSONObject ocJson = getOCInfo(packageName, EGContext.OC_COLLECTION_TYPE_RUNNING_TASK,
                    System.currentTimeMillis());
            if (!TextUtils.isEmpty(mLastPkgName)) {// 值非空则为立即切换
                if (mCache == null) {
                    mCache = new JSONObject();
                }
                if (!packageName.equals(mLastPkgName)) {
                    mCache.put(EGContext.END_TIME, System.currentTimeMillis());// endTime
                    // 2.入库当前的缓存appTime
                    filterInsertOCInfo(EGContext.APP_SWITCH);
                    // 3.如果打开的包名与缓存的包名不一致，更新新pkgName到sp
                    mLastPkgName = packageName;
                    saveCacheOCInfo(ocJson);
                } else {
                    if (TextUtils.isEmpty(mCache.optString(EGContext.LAST_OPEN_TIME))) {
                        if (lastAvailableTime == -1) {
                            lastAvailableTime = SPHelper.getLongValueFromSP(mContext, EGContext.LAST_AVAILABLE_TIME, 0);
                        }
                        long time = System.currentTimeMillis() - lastAvailableTime;
                        if (time > EGContext.OC_CYCLE) {
                            lastAvailableTime = System.currentTimeMillis();
                            SPHelper.setLongValue2SP(mContext, EGContext.LAST_AVAILABLE_TIME, lastAvailableTime);
                            saveCacheOCInfo(ocJson);
                        } else {
                            SaveData2Sp(packageName, null);
                        }
                    }
                }
            } else {// 值为空则要么第一次打开，要么有时间间隔，需随机取结束时间
                if (mCache == null) {
                    mCache = new JSONObject();
                }
                lastAvailableTime = SPHelper.getLongValueFromSP(mContext, EGContext.LAST_AVAILABLE_TIME, 0);
                // 第一次打开
                if (mCache.optLong(EGContext.LAST_AVAILABLE_TIME) == 0 && lastAvailableTime == 0) {
                    SaveData2Sp(packageName, ocJson);
                } else {// 之前有缓存数据
                    if (System.currentTimeMillis() - lastAvailableTime > EGContext.OC_CYCLE) {
                        SaveData2Sp(packageName, ocJson);
                    } else {
                        SaveData2Sp(packageName, null);
                    }
                }
            }
        } catch (Throwable t) {

        }

    }

    private void SaveData2Sp(String packageName, JSONObject ocJson) {
        mLastPkgName = packageName;
        // 第一次打开存数进内存
        SPHelper.setLongValue2SP(mContext, EGContext.LAST_AVAILABLE_TIME, System.currentTimeMillis());
        saveCacheOCInfo(ocJson);
    }

    private void saveCacheOCInfo(JSONObject ocJson) {
        try {
            if (mCache == null) {
                mCache = new JSONObject();
            }
            if (ocJson == null || ocJson.length() < 1) {
                if (mCache.has(EGContext.LAST_PACKAGE_NAME)) {
                    mCache.remove(EGContext.LAST_PACKAGE_NAME);
                }
                if (mCache.has(EGContext.LAST_OPEN_TIME)) {
                    mCache.remove(EGContext.LAST_OPEN_TIME);
                }
                if (mCache.has(EGContext.LAST_APP_NAME)) {
                    mCache.remove(EGContext.LAST_APP_NAME);
                }
                if (mCache.has(EGContext.LAST_APP_VERSION)) {
                    mCache.remove(EGContext.LAST_APP_VERSION);
                }
                if (mCache.has(EGContext.APP_TYPE)) {
                    mCache.remove(EGContext.APP_TYPE);
                }
                mCache = null;
                return;
            }
            String pkgName = ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
            // pkgName
            mCache.put(EGContext.LAST_PACKAGE_NAME, pkgName);
            // 打开时间
            mCache.put(EGContext.LAST_OPEN_TIME, ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime));
            mCache.put(EGContext.LAST_APP_NAME, ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationName));
            mCache.put(EGContext.LAST_APP_VERSION, ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode));
            mCache.put(EGContext.APP_TYPE, ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationType));
        } catch (Throwable t) {

        }

        String lastOpenTime = SPHelper.getStringValueFromSP(mContext, EGContext.LAST_OPEN_TIME, "");
        if (TextUtils.isEmpty(lastOpenTime)) {
            lastOpenTime = "0";
        }

    }

    /**
     * 从Proc中读取数据
     */
    public void getProcApps(Set<String> nameSet, long time) {
        // 本次proc取到的值
        if (nameSet == null || nameSet.size() <= 0) {
            return;
        }

        try {
            // 第一次轮询，内存没活跃app
            if (mRunningApps == null || mRunningApps.size() < 1) {
                mRunningApps = new ArrayList<JSONObject>();
                for (String name : nameSet) {
                    String pkgName = name.replaceAll(" ", "");
                    if (!TextUtils.isEmpty(pkgName)) {
                        mRunningApps.add(getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_PROC, time));
                    }
                }
            } else {
                // 本次proc取到的值与原来的内存数据去重
                repeatHandle(nameSet, time);
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
    }

    /**
     * 缓存中应用列表与新获取应用列表去重
     */
    @SuppressWarnings({"unused"})
    private void repeatHandle(Set<String> runApps, long time) {
        if (runApps == null || mRunningApps == null) {
            return;
        }
        JSONObject tempMemoryJson = null;
        List<JSONObject> cacheJson = new ArrayList<JSONObject>();
        List<JSONObject> shouldRemoveObject = new ArrayList<JSONObject>();
        try {
            List<Integer> list = SystemUtils.getDiffNO(mRunningApps.size());
            int random;
            String pkgName = "";
            long openTime = -1;
            for (int i = 0; i < mRunningApps.size(); i++) {
                tempMemoryJson = mRunningApps.get(i);
                if (tempMemoryJson == null || tempMemoryJson.length() < 1) {
                    continue;
                }
                random = (Integer) list.get(i);

                // 如果现在列表里没有，则给与结束时间，填写至cacheJson
                pkgName = tempMemoryJson.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName).replaceAll(" ", "");
                openTime = tempMemoryJson.optLong(DeviceKeyContacts.OCInfo.ApplicationOpenTime);
                if (!runApps.contains(pkgName)) {// 现有的不包含，则为闭合数据
                    shouldRemoveObject.add(tempMemoryJson);
                    // 取结束时间，闭合
                    String closeTime = String.valueOf(System.currentTimeMillis() - random);
                    tempMemoryJson.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, closeTime);
                    cacheJson.add(tempMemoryJson);
                } else {
                    // 现有的包含，则为长久存活数据，替换openTime
                    runApps.remove(pkgName);
//                    mRunningApps.add(getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_PROC,openTime));
//                    result.put(pkgName, openTime);
                }
            }
            for (JSONObject obj : shouldRemoveObject) {
                mRunningApps.remove(obj);
            }
            if (cacheJson != null && cacheJson.size() > 0) {
                // 更新缓存表
                closeState2DB(cacheJson);
            }
            if (runApps != null && runApps.size() > 0) {
                // 新增该时段缓存信息
                addCache(runApps, time);
            }
//            result.put("run", runApps);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
//        ELOG.e("xxx.oc", SystemUtils.getCurrentProcessName(mContext)+"最终缓存本次列表mRunningApps：：" + mRunningApps);
    }

    /**
     * 更新缓存表
     */
    private void closeState2DB(List<JSONObject> cacheApps) {
        try {
            if (cacheApps != null && cacheApps.size() > 0) {
                TableOC.getInstance(mContext).insertArray(cacheApps);
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
    }

    /**
     * 新增缓存
     */
    private void addCache(Set<String> runApps, long time) {
        try {
            if (runApps == null || runApps.size() < 1) {
                return;
            }
            if (!runApps.isEmpty()) {
                getOCArray(runApps, time);
//                mRunningApps = oc;
            }
        } catch (Throwable t) {
        }

    }

    /**
     * 根据读取出的包列表，获取应用信息并组成json格式添加到列表
     */
    private void getOCArray(Set<String> runApps, long time) {
//        List<JSONObject> list = null;
        try {
            if (runApps == null || runApps.size() < 1) {
                return;
            }
            List<Integer> randomList = SystemUtils.getDiffNO(runApps.size());
            int random;
            int i = 0;
//            list = new ArrayList<JSONObject>();
            String pkgName = null;
            for (String name : runApps) {
                random = (int) randomList.get(i);
                i += 1;
                pkgName = name.trim();
                if (!TextUtils.isEmpty(pkgName)) {
                    JSONObject ocJson = getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_PROC, time - random);
                    mRunningApps.add(ocJson);
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
//        return list;
    }

    /**
     * 更新缓存，如果该时段有缓存就更新，没有就新增
     */
    private void updateCache(String pkgName, int collectionType) {
        try {
            if (!TextUtils.isEmpty(pkgName)) {
                JSONObject ocJson = getOCInfo(pkgName, collectionType, System.currentTimeMillis());
                mRunningApps.clear();
                mRunningApps.add(ocJson);
            } else {
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }

    }

    /**
     * 去除缓存中的重复，剩余为已经关闭的应用
     */
    private void repeatHandle(List<JSONObject> cacheApps) {
        try {
            JSONObject json = null;
            List<Integer> list = SystemUtils.getDiffNO(cacheApps.size());
            int random;
            for (int i = cacheApps.size() - 1; i >= 0; i--) {
                json = cacheApps.get(i);
                random = (Integer) list.get(i);
                String apn = json.getString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
                    cacheApps.remove(json);
                    pkgName = null;
                    continue;
                }
                json.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime,
                        String.valueOf(System.currentTimeMillis() - random));
                json.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.APP_SWITCH);
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
    }

    /**
     * 根据包名 获取应用信息并组成json格式
     */
    @SuppressWarnings("deprecation")
    private JSONObject getOCInfo(String packageName, int collectionType, long time) {
        JSONObject ocInfo = null;
        try {
            if (!TextUtils.isEmpty(packageName)) {
                PackageManager pm = null;
                ApplicationInfo appInfo = null;
                try {
                    pm = mContext.getPackageManager();
                    appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                } catch (Throwable t) {
                }
                ocInfo = new JSONObject();
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, packageName);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime, time);
                ocInfo.put(DeviceKeyContacts.OCInfo.NetworkType, NetworkUtils.getNetworkType(mContext));
                ocInfo.put(DeviceKeyContacts.OCInfo.CollectionType, collectionType);
                ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.APP_SWITCH);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationType, AppSnapshotImpl.getInstance(mContext).getAppType(packageName));
                try {
                    ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                            pm.getPackageInfo(packageName, 0).versionName + "|"
                                    + pm.getPackageInfo(packageName, 0).versionCode);
                } catch (Throwable t) {
                    // ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode, "");
                }
                try {
                    ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName, String.valueOf(appInfo.loadLabel(pm)));
                } catch (Throwable t) {
//                     ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName, "unknown");
                }

            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return ocInfo;
    }

    public void filterInsertOCInfo(String switchType) {
        String OldPkgName = mCache.optString(EGContext.LAST_PACKAGE_NAME, "");
        String appName = mCache.optString(EGContext.LAST_APP_NAME, "");
        String appVersion = mCache.optString(EGContext.LAST_APP_VERSION, "");
        String openTime = mCache.optString(EGContext.LAST_OPEN_TIME, "");
        Long closeTime = mCache.optLong(EGContext.END_TIME, 0);
        String appType = mCache.optString(EGContext.APP_TYPE, "");
        if (TextUtils.isEmpty(OldPkgName) || TextUtils.isEmpty(appName) || TextUtils.isEmpty(appVersion)
                || TextUtils.isEmpty(openTime)) {
            return;
        }
        JSONObject ocInfo = null;
        if (closeTime > Long.parseLong(openTime)) {
            JSONArray updataData = new JSONArray();
            try {
                ocInfo = new JSONObject();
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, OldPkgName);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime, openTime);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, closeTime);
                ocInfo.put(DeviceKeyContacts.OCInfo.NetworkType, NetworkUtils.getNetworkType(mContext));
                ocInfo.put(DeviceKeyContacts.OCInfo.CollectionType, "1");
                ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType, switchType);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationType, appType);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode, appVersion);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName, appName);
                if (ocInfo != null && !TextUtils.isEmpty(openTime) && 0L != closeTime) {
//                    ProcessManager.saveSP(mContext,ocInfo);
                    JSONArray array = TableOC.getInstance(mContext).selectAll();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = (JSONObject) array.get(i);
                        if (obj.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime).equals(openTime)
                                && obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName).equals(OldPkgName)) {
                            updataData.put(ocInfo);
                        }
                    }
                    if (updataData != null && updataData.length() > 0) {
                        TableOC.getInstance(mContext).updateStopState(updataData);// 有重复数据则更新
                    } else {
                        TableOC.getInstance(mContext).insert(ocInfo);// 保存上一个打开关闭记录信息
                    }
                }
            } catch (Throwable t) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.e(t);
                }
            }
        }
        // 置空
        mCache.remove(EGContext.LAST_PACKAGE_NAME);
        mCache.remove(EGContext.LAST_APP_NAME);
        mCache.remove(EGContext.LAST_APP_VERSION);
        mCache.remove(EGContext.LAST_OPEN_TIME);
        mCache.remove(EGContext.APP_TYPE);
    }

    /**
     * android 5以上，有UsageStatsManager权限可以使用的
     */
    public void processOCByUsageStatsManager(Set<String> ocr, long time) {
        class RecentUseComparator implements Comparator<UsageStats> {
            @Override
            public int compare(UsageStats lhs, UsageStats rhs) {
                return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1
                        : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
            }
        }
        try {
            @SuppressLint("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager) mContext.getApplicationContext()
                    .getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) {
                return;
            }
            long ts = System.currentTimeMillis();
            List<UsageStats> usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 10 * 1000, ts);
            if (usageStats == null || usageStats.size() == 0) {
                return;
            }
            Collections.sort(usageStats, new RecentUseComparator());
            String usmPkg = usageStats.get(0).getPackageName();
            if (!TextUtils.isEmpty(usmPkg)) {
                processPkgName(usmPkg);
            } else {
                getProcApps(ocr, time);
            }
        } catch (Throwable e) {
            getProcApps(ocr, time);
        }
    }

    private static class Holder {
        private static final OCImpl INSTANCE = new OCImpl();
    }

}
