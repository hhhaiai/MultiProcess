package com.analysys.track.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.database.TableOC;
import com.analysys.track.database.TableXXXInfo;
import com.analysys.track.impl.proc.ProcUtils;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.work.MessageDispatcher;
import com.analysys.track.service.AnalysysAccessibilityService;
import com.analysys.track.utils.AccessibilityHelper;

import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.SystemUtils;

import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

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
import android.util.Log;

public class OCImpl {

    private Context mContext;
//    private long mProcessTime = 0L;
    private String mLastPkgName = "";
    public static long mLastAvailableOpenOrCloseTime = -1;
    /**
     * mRunningApps为Android5/6正在运行的app列表的内存变量值
     */
    private static List<JSONObject> mRunningApps = null;
    /**
     * mCache为Android4.x正在运行的app列表的内存变量值
     */
    public static JSONObject mCache = null;
    private static class Holder {
        private static final OCImpl INSTANCE = new OCImpl();
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

        }

    }

    /**
     * 真正的OC处理
     */
    public void processOC() {
        try {
//            if(!ProcessManager.getIsCollected()){
//                ELOG.i("屏幕关闭，不收集");
//                return;
//            }
            // 亮屏幕工作
            if (SystemUtils.isScreenOn(mContext)) {
//                fillData();
                if (!SystemUtils.isScreenLocked(mContext)) {
                    ELOG.i("xxx.oc","屏幕开启，正常收集");
                    boolean isOCCollected = PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC,true);
                    boolean isXXXCollected = PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_XXX,true);
                    if(!isOCCollected && !isXXXCollected){
                        return;
                    }
                    JSONObject xxxInfo = null;
//                    if (!AccessibilityHelper.isAccessibilitySettingsOn(mContext, AnalysysAccessibilityService.class)) {
                    getInfoByVersion(isOCCollected, isXXXCollected, xxxInfo);
//                    }
                }
            }else{
                closeOC();
            }
        } catch (Throwable t) {
            ELOG.i("xxx.oc", Log.getStackTraceString(t));
        }finally {
            MessageDispatcher.getInstance(mContext).ocInfo(EGContext.OC_CYCLE, false);
        }
    }

    /**
     * 补数逻辑
     */
    private void closeOC(){
        if(Build.VERSION.SDK_INT < 21){
            SPHelper.setLongValue2SP(mContext,EGContext.END_TIME, System.currentTimeMillis());
            OCImpl.getInstance(mContext).filterInsertOCInfo(EGContext.CLOSE_SCREEN);
        }else if(Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24){
            //TODO 黑屏补充数据，待验证
            String openApp = SPHelper.getStringValueFromSP(mContext,EGContext.LAST_APP_NAME, "");
            if (!TextUtils.isEmpty(openApp)) {
                // 补充时间
                String lastOpenTime = SPHelper.getStringValueFromSP(mContext,EGContext.LAST_OPEN_TIME, "");
                if(TextUtils.isEmpty(lastOpenTime)){
                    lastOpenTime = "0";
                }
                long randomCloseTime = SystemUtils.calculateCloseTime(Long.parseLong(lastOpenTime));
                SPHelper.setLongValue2SP(mContext,EGContext.END_TIME,randomCloseTime);
                ELOG.i("黑屏补充数据系列");
                filterInsertOCInfo(EGContext.CLOSE_SCREEN);
                fillData();
            }
        }
    }
    /**
     * 补数逻辑，在屏幕亮屏但是正在锁屏未解锁的时候，对closeTime为空的数据进行补数操作；
     * 在opten时间和现在时间之间随机一个值：差值在5min左右，直接补数，对于小于5min的频繁的开锁屏不做处理，后续依然有补数操作
     * 为了保险起见，如果屏幕亮屏紧随解锁屏，则在正常状态下进行一批的补数操作
     *
     */
    public void fillData(){
        ELOG.i("ocInfo 5&6系统进入补数逻辑...");
        List<JSONObject> fillDataList = null;
        int blankCount = 0;
        try {
            /**
             * 1.如果内存没数据则从db读取一次，然后赋值给内存变量，然后做数据闭合
             * 2.如果有数据就用内存数据做闭合
             * 3.闭合完内存数据同步给db
             */
            if(mRunningApps == null || mRunningApps.size() < 1){
                return;
            }
            JSONObject json = null;
            fillDataList = new ArrayList<JSONObject>();
            for (int i = 0; i < mRunningApps.size(); i++) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    break;
                }
                json = mRunningApps.get(i);
                String openTime = json.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime);
                long randomCloseTime = SystemUtils.calculateCloseTime(Long.parseLong(openTime));
                if(randomCloseTime == -1){//小于轮询周期的频繁的开锁屏不做处理
                    continue;
                }
                String apn = json.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                if (!TextUtils.isEmpty(apn)) {
                    mRunningApps.remove(json);
                    json.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime,
                            String.valueOf(randomCloseTime));//再随机为了防止结束时间一样
                    json.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.CLOSE_SCREEN);
                    fillDataList.add(json);
                }else {
                    blankCount += 1;
                }
            }
            // 一次应用操作闭合，更新OCCunt表，打开次数、应用运行状态
            TableOC.getInstance(mContext).insertArray(fillDataList);
        }catch (Throwable t){
            ELOG.i(t.getMessage()+"fillData");
        }

    }
    String pkgName = null;

    /**
     * <pre>
     * OC获取，按照版本区别处理:
     *      1. 5.x以下系统API获取
     *      2. 5.x/6.x使用top/ps+proc来完成，数据同步给XXXInfo
     *      3. 7.x+获取活着服务.
     * </pre>
     */
    @SuppressWarnings("deprecation")
    public void getInfoByVersion(boolean isOCCollected,boolean isXXXCollected,JSONObject obj) {
        // 1. 获取info
        obj = ProcUtils.getInstance(mContext).getRunningInfo();
        if (obj == null || obj.length() <= 0) {
            return;
        }
        // 2. 解析INFO详情
        String ocr = null;
        String[] strArray = null;
        Set<String> nameSet = new HashSet<String>();
        long now = System.currentTimeMillis();
        if (obj.has(ProcUtils.RUNNING_OC_RESULT)) {
            ocr = obj.optString(ProcUtils.RUNNING_OC_RESULT).replace("[","").replace("]","");
            strArray = ocr.split(",");
            if(strArray != null && strArray.length > 0){
                for (int i = 0; i < strArray.length; i++) {
                    String pkgName = strArray[i];
                    nameSet.add(pkgName.substring(1,pkgName.length()-1));
                }
            }
        }
        if (obj.has(ProcUtils.RUNNING_TIME)) {
            now = obj.optLong(ProcUtils.RUNNING_TIME);
        }
        // 4.x和以下版本判断,逻辑: 有权限申请系统API获取; 无权限直接使用5.x/6.x版本
        if (Build.VERSION.SDK_INT < 21) {//4.x
            if (PermissionUtils.checkPermission(mContext, Manifest.permission.GET_TASKS)) {
                getRunningTasks();
            } else {//没权限，用xxxinfo方式收集
                getProcApps(nameSet);
            }
        } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {//5/6
            /**
             * 样例数据
             * <code>    {"time":1557237920076,"ocr":["com.oppo.market","com.android.mms","com.oppo.usercenter","com.nearme.gamecenter","com.coloros.gallery3d","cn.analysys.demo"],"result":[{"pid":17910,"oomScore":17,"pkg":"cn.analysys.demo","cgroup":"2:cpu:\/\n1:cpuacct:\/uid_10509\/pid_17910","oomAdj":"0"},{"pid":16205,"oomScore":66,"pkg":"com.coloros.gallery3d","cgroup":"2:cpu:\/\n1:cpuacct:\/uid_10011\/pid_16205"},{"pid":16263,"oomScore":71,"pkg":"com.android.mms","cgroup":"2:cpu:\/\n1:cpuacct:\/uid_10015\/pid_16263"}]}</code>
             */
            //3. 根据是否采集进行分支判断
            if (isXXXCollected) {
                // 3.1.1 写入XXX
                TableXXXInfo.getInstance(mContext).insert(obj);
//                // 3.1.2 xxx是否收集, 清除RESULT
//                obj.remove(ProcUtils.RUNNING_RESULT);
                // 3.1.3 判断oc是否需要收集
                if (isOCCollected) {
                    ocCollection(nameSet, now);
                }
                // 3.1.4 清除内存数据
                ocr = null;
                obj = null;
            } else {
                //3.2.1 xxx不收集
                if (isOCCollected) {
                    ocCollection(nameSet, now);
                }
                ocr = null;
                obj = null;
            }
        } else {
            // TODO 7.0以上待调研
        }
    }

    /**
     * 辅助功能-->UsageStatsManager-->ocr
     *
     * @param ocr
     * @param now
     */
    private void ocCollection(Set<String> ocr, long now) {
        if(!AccessibilityHelper.isAccessibilitySettingsOn(mContext, AnalysysAccessibilityService.class)){//如果辅助功能开启则使用辅助功能
            if(SystemUtils.canUseUsageStatsManager(mContext)){//如果开了USM则使用USM
                ELOG.i("开启了。UsageStatsManager功能");
                processOCByUsageStatsManager(ocr);
            }else {//否则使用ocr
                getProcApps(ocr);
            }
        }
    }
    /**
     * getRunningTask、辅助功能 OC 信息采集
     */
    public void RunningApps(String pkgName, int collectionType) {
        try {
            this.pkgName = pkgName;
            if(TextUtils.isEmpty(pkgName)){
                return;
            }
            if (mRunningApps != null && mRunningApps.size() > 0) {
                removeRepeat(mRunningApps);
                if (mRunningApps != null && mRunningApps.size() > 0) {
                    // 完成一次闭合，存储到OC表
                    // 更新缓存表
                    closeState2DB(mRunningApps);
                }
                if (!TextUtils.isEmpty(pkgName)) {
                    updateCache(pkgName ,collectionType);
                }
            } else {
                ELOG.i("go into updateCache...");
                updateCache(pkgName ,collectionType);
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
    }

    /**
     * 获取正在运行的应用包名
     */
    private void getRunningTasks() {
        String pkgName = "";
        ActivityManager am = null;
        try {
            if(mContext == null){
                mContext = EContextHelper.getContext(mContext);
            }
            if (mContext != null) {
                am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks == null || tasks.size() <= 0) {
                    ELOG.i("OC获取RunningTaskInfo为空");
                    getRunningProcess(am);
                    return;
                }
                //获取栈顶app的包名
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
                ELOG.i("OC获取RunningAppProcessInfo为空");
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
    private long lastAvailableTime = -1;
    /**
     * 根据包名进行一系列的处理
     *
     * @param packageName
     */
    private void processPkgName(String packageName) {
        try {
            ELOG.i("根据包名做一系列处理：获取到的包名：："+packageName);
            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            JSONObject ocJson = getOCInfo(packageName, EGContext.OC_COLLECTION_TYPE_RUNNING_TASK);
            if(!TextUtils.isEmpty(mLastPkgName)){//值非空则为立即切换
                if(mCache == null){
                    mCache = new JSONObject();
                }
                if(TextUtils.isEmpty(mCache.optString(EGContext.LAST_OPEN_TIME))){
                    if(lastAvailableTime == -1){
                        lastAvailableTime = SPHelper.getLongValueFromSP(mContext,EGContext.LAST_AVAILABLE_TIME,0);
                    }
                    long time = System.currentTimeMillis()- lastAvailableTime;
                    if(time > EGContext.OC_CYCLE){
                        lastAvailableTime = System.currentTimeMillis();
                        SPHelper.setLongValue2SP(mContext,EGContext.LAST_AVAILABLE_TIME,lastAvailableTime);
                        saveCacheOCInfo(ocJson);
                    }
                }
                if (!packageName.equals(mLastPkgName)) {
                    ELOG.i("=======切换包名。即将保存"+packageName+"  OLD "+mLastPkgName);
                    mCache.put(EGContext.END_TIME, System.currentTimeMillis());//endTime
                    //2.入库当前的缓存appTime
                    filterInsertOCInfo(EGContext.APP_SWITCH);
                    // 3.如果打开的包名与缓存的包名不一致，更新新pkgName到sp
                    mLastPkgName = packageName;
                    saveCacheOCInfo(ocJson);
                }
            }else {//值为空则要么第一次打开，要么有时间间隔，需随机取结束时间
                if(mCache == null){
                    mCache = new JSONObject();
                }
                mLastPkgName = packageName;
                //第一次打开存数进内存
                saveCacheOCInfo(ocJson);
            }

//            // 是否首次打开
//            if (TextUtils.isEmpty(mLastPkgName)) {
//            } else {
//                //如果当前sp没值或者被清理过了，则记录当前oc部分信息到sp
//                String lastOpenTime = SPHelper.getStringValueFromSP(mContext,EGContext.LAST_OPEN_TIME, "");
//                ELOG.i("OC新的轮询，lastOpenTime == "+lastOpenTime);
//                if(TextUtils.isEmpty(lastOpenTime) && (System.currentTimeMillis() - mLastAvailableOpenOrCloseTime > EGContext.OC_CYCLE)){
//                    ELOG.i("OC新的轮询，如果当前sp中，oc没值则进去当前应用信息");
//                    mLastAvailableOpenOrCloseTime = System.currentTimeMillis();
//                    mLastPkgName = packageName;
//                    ProcessManager.saveSP(mContext,ocJson);
//                }
//
//            }
        }catch (Throwable t){

        }

    }

    private void saveCacheOCInfo(JSONObject ocJson){
        try {
            if(mCache == null){
                mCache = new JSONObject();
            }
            String pkgName = ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
            ELOG.i("重新写进去sp里的pkgName = "+pkgName);
            mCache.put(EGContext.LAST_PACKAGE_NAME,pkgName);//pkgName
            mCache.put(EGContext.LAST_OPEN_TIME,ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime));//打开时间
            mCache.put(EGContext.LAST_APP_NAME,ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationName));
            mCache.put(EGContext.LAST_APP_VERSION,ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode));
            mCache.put(EGContext.APP_TYPE,ocJson.optString(DeviceKeyContacts.OCInfo.ApplicationType));
//            String lastOpenTime = mCache.optString(EGContext.LAST_OPEN_TIME,"");
//            if(!TextUtils.isEmpty(lastOpenTime)){
//                long randomCloseTime = SystemUtils.calculateCloseTime(Long.parseLong(lastOpenTime));
//                ELOG.i("补数逻辑：得到的randomCloseTime"+randomCloseTime);
//                if(randomCloseTime == -1){//不足轮询时间间隔，无效数据
//                    mCache = null;
//                    return;
//                }
//                //1.非首次打开，之前有pkgName缓存,设置当前时间往前推一点为结束时间
//                mCache.put(EGContext.LAST_OPEN_TIME,lastOpenTime);
//            }else {//没有开始时间则无需闭合
//                return;
//            }
        }catch (Throwable t){

        }

        String lastOpenTime = SPHelper.getStringValueFromSP(mContext,EGContext.LAST_OPEN_TIME, "");
        if(TextUtils.isEmpty(lastOpenTime)){
            lastOpenTime = "0";
        }

    }
    /**
     * 从Proc中读取数据
     */
    private void getProcApps(Set<String> nameSet) {
//        JSONArray cacheApps = TableOC.getInstance(mContext).selectRunning();//结束时间为空，则为正在运行的app
        //本次proc取到的值
        if(nameSet == null|| nameSet.size()<=0){
            ELOG.i("本次proc没获取到数据");
            return;
        }

        try {
            ELOG.i("xxx.oc","mRunningApps:"+(mRunningApps== null ?" null ":mRunningApps.toString()));
            ELOG.i("xxx.oc","ocr:"+nameSet.toString());
            //第一次轮询，内存没活跃app
            if(mRunningApps == null || mRunningApps.size()< 1){
                mRunningApps = new ArrayList<JSONObject>();
                for (String name:nameSet) {
                    String pkgName = name.replaceAll(" ","");
                    ELOG.i("pkgName = "+pkgName);
                    if (!TextUtils.isEmpty(pkgName)) {
                        mRunningApps.add(getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_PROC));
                    }
                }
            }else {
                // 本次proc取到的值与原来的内存数据去重
                JSONObject res = removeRepeat(mRunningApps, nameSet);
                if (res != null && res.length() > 0) {
                    try {
                        mRunningApps = (ArrayList<JSONObject>)(res.get("cache"));
                        ELOG.i(mRunningApps + "   ::::::::: cacheApps:::::");
                    } catch (Throwable t) {
                        ELOG.i("   ::::::::: cacheApps 异常:::::");
//                        cacheApps = null;
                    }
                }
                if (mRunningApps != null && mRunningApps.size() > 0) {
                    // 更新缓存表
                    closeState2DB(mRunningApps);
                }
                try {
                    nameSet = (Set<String>) res.get("run");
                } catch (Throwable t) {
                    nameSet = null;
                }
                if (nameSet != null && nameSet.size() > 0) {
                    // 新增该时段缓存信息
                    addCache(nameSet);
                }
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.i("xxx.oc",Log.getStackTraceString(t));
            }
        }
        ELOG.i("xxx.oc","结束时： :"+mRunningApps.toString());
    }

    /**
     * 缓存中应用列表与新获取应用列表去重
     */
    private JSONObject removeRepeat(List<JSONObject> cacheApps, Set<String> runApps) {
        ELOG.i("xxx.oc",cacheApps+" :::::: 上次app");
        ELOG.i("xxx.oc",runApps+" :::::: 本次app");

        JSONObject ocInfo = null, result = new JSONObject();
        try {
            List list = SystemUtils.getDiffNO(cacheApps.size());
            int random;
            String apn = "";
            for (int i = 0; i < cacheApps.size(); i++) {
                random = (Integer)list.get(i);
                ocInfo = cacheApps.get(i);
//                cacheApps.remove(ocInfo);
                if (ocInfo == null || ocInfo.length() < 1){
                    continue;
                }
                String act = String.valueOf(System.currentTimeMillis() - random);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime,act);
                ELOG.i("pkgName :" + ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName)+ "::act:::: "+act);
//                cacheApps.add(ocInfo);
                //内存里的pkgName
                apn = ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName).replaceAll(" ", "");
                //遍历当前获取到的，如果取到的值里不包含则close掉，包含则为一直存活的app,从当前set里去除
                for (String name: runApps) {
                    String pkgName = name.replaceAll(" ", "");
                    if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
//                        cacheApps.remove(cacheApps.size() - 1);
                        cacheApps.remove(ocInfo);
//                        runApps.remove(name);
                        break;
                    }
                }
                 ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.APP_SWITCH);
            }
             ELOG.i(ocInfo+" :::::: oc");
            if (cacheApps != null && cacheApps.size() > 0) {
                result.put("cache", cacheApps);
                 ELOG.i(cacheApps+" :::::: cacheApps");
            }
            if (runApps != null && runApps.size() > 0) {
                result.put("run", runApps);
                 ELOG.i(runApps+" :::::: runApps");
            }
        } catch (Throwable t) {
            ELOG.e(t.getMessage() + "tttttttttttttttttttt");
        }
        return result;
    }

    /**
     * 更新缓存表
     */
    private void closeState2DB(List<JSONObject> cacheApps) {
        try {
            if (cacheApps != null && cacheApps.size() > 0) {
                ELOG.i("xxx.oc","closeState2DB:"+cacheApps.toString());
                TableOC.getInstance(mContext).insertArray(cacheApps);
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
    }

    /**
     * 新增缓存
     */
    private void addCache(Set<String> runApps) {
        try {
            List<JSONObject> oc = new ArrayList<JSONObject>();
            if (!runApps.isEmpty()) {
                oc = getOCArray(runApps);
                mRunningApps = oc;
            }
        } catch (Throwable t) {
        }

    }

    /**
     * 根据读取出的包列表，获取应用信息并组成json格式添加到列表
     */
    private List<JSONObject> getOCArray(Set<String> runApps) {
        List<JSONObject> list = null;
        try {
            list = new ArrayList<JSONObject>();
            for (String name:runApps) {
                String pkgName = name.trim();
                if (!TextUtils.isEmpty(pkgName)) {
                    JSONObject ocJson = getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_PROC);
                    list.add(ocJson);
                }
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
        return list;
    }

    /**
     * 更新缓存，如果该时段有缓存就更新，没有就新增
     */
    private void updateCache(String pkgName,int collectionType) {
        try {
            if (!TextUtils.isEmpty(pkgName)) {
                JSONObject ocJson = getOCInfo(pkgName, collectionType);
                mRunningApps.clear();
                mRunningApps.add(ocJson);
            }else {
                ELOG.i(" updateCache  else  pkgName == null");
            }
        }catch (Throwable t){
            ELOG.e(t.getMessage()+"......updateCache ");
        }

    }

    /**
     * 去除缓存中的重复，剩余为已经关闭的应用
     */
    private void removeRepeat(List<JSONObject> cacheApps) {
        try {
            JSONObject json = null;
            List list = SystemUtils.getDiffNO(cacheApps.size());
            int random;
            for (int i = cacheApps.size() - 1; i >= 0; i--) {
                json =cacheApps.get(i);
                random = (Integer)list.get(i);
                String apn = json.getString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
                    cacheApps.remove(json);
                    ELOG.i(" -------remove repeat ");
                    pkgName = null;
                    continue;
                }
                json.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime,
                    String.valueOf(System.currentTimeMillis() - random));
                json.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.APP_SWITCH);
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
    }

    /**
     * 根据包名 获取应用信息并组成json格式
     */
    private JSONObject getOCInfo(String packageName, int collectionType) {
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
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime, String.valueOf(System.currentTimeMillis()));
                ocInfo.put(DeviceKeyContacts.OCInfo.NetworkType, NetworkUtils.getNetworkType(mContext));
                ocInfo.put(DeviceKeyContacts.OCInfo.CollectionType, collectionType);
                ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.APP_SWITCH);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationType,
                    SystemUtils.getAppType(mContext ,packageName));
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
            ELOG.e(e + "    ::::::getOCInfo has an exception");
        }
        return ocInfo;
    }

    public void filterInsertOCInfo(String switchType) {
        String OldPkgName = mCache.optString(EGContext.LAST_PACKAGE_NAME,"");
        String appName =mCache.optString(EGContext.LAST_APP_NAME, "");
        String appVersion =mCache.optString(EGContext.LAST_APP_VERSION,"");
        String openTime =mCache.optString(EGContext.LAST_OPEN_TIME, "");
        Long closeTime = mCache.optLong(EGContext.END_TIME, 0);
        ELOG.i("oc读到的sp里close时间:::"+closeTime);
        String appType =mCache.optString(EGContext.APP_TYPE, "");
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
                ELOG.i("put进去数据库的close时间:::"+closeTime);
                ocInfo.put(DeviceKeyContacts.OCInfo.NetworkType, NetworkUtils.getNetworkType(mContext));
                ocInfo.put(DeviceKeyContacts.OCInfo.CollectionType, "1");
                ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType, switchType);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationType, appType);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode, appVersion);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName, appName);
                if (ocInfo != null && !"".equals(openTime) && !"".equals(closeTime)) {
//                    ProcessManager.saveSP(mContext,ocInfo);
                    JSONArray array = TableOC.getInstance(mContext).selectAll();
                    for(int i = 0;i < array.length(); i++){
                        JSONObject obj = (JSONObject)array.get(i);
                        if(obj.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime).equals(openTime) &&
                                obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName).equals(OldPkgName)  ){
                            updataData.put(ocInfo);
                        }
                    }
                    if(updataData != null && updataData.length() >0){
                        TableOC.getInstance(mContext).updateStopState(updataData);// 有重复数据则更新
                    }else {
                        TableOC.getInstance(mContext).insert(ocInfo);// 保存上一个打开关闭记录信息
                    }
                }
            } catch (Throwable t) {
                ELOG.e(t.getMessage()+"  filterInsertOCInfo has an exc");
            }
        }
        //置空
        mCache.remove(EGContext.LAST_PACKAGE_NAME);
        mCache.remove(EGContext.LAST_APP_NAME);
        mCache.remove(EGContext.LAST_APP_VERSION);
        mCache.remove(EGContext.LAST_OPEN_TIME);
        mCache.remove(EGContext.APP_TYPE);
    }

    /**
     * android 5/6需要间隔大于30秒
     *
     * @return
     */
//    private boolean isDurLThanThri() {
//        long now = System.currentTimeMillis();
//        if (mProcessTime == 0 || (now - mProcessTime) >= EGContext.OC_CYCLE_OVER_5) {
//            mProcessTime = now;
//            return true;
//        }
//        return false;
//    }

    /**
     * android 5以上，有UsageStatsManager权限可以使用的
     */
    public void processOCByUsageStatsManager(Set<String> ocr) {
        class RecentUseComparator implements Comparator<UsageStats> {
            @Override
            public int compare(UsageStats lhs, UsageStats rhs) {
                return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1
                    : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
            }
        }
        try {
            @SuppressLint("WrongConstant")
            UsageStatsManager usm = (UsageStatsManager)mContext.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) {
                return;
            }
            long ts = System.currentTimeMillis();
            List<UsageStats> usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 10 * 1000 , ts);
            if (usageStats == null || usageStats.size() == 0) {
                return;
            }
            Collections.sort(usageStats, new RecentUseComparator());
            String usmPkg = usageStats.get(0).getPackageName();
//            ELOG.i("usmPkg :::: "+ usmPkg);
            if (!TextUtils.isEmpty(usmPkg)) {
                processPkgName(usmPkg);
            } else {
                getProcApps(ocr);
            }
        } catch (Throwable e) {
            getProcApps(ocr);
        }
    }

}
