package com.analysys.track.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.database.TableOC;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.impl.proc.ProcessManager;
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

public class OCImpl {

    private Context mContext;
    private long mProcessTime = 0L;

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
    private void processOC() {
        try {
            if(!PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC,true)){
                return;
            }
            // 亮屏幕工作
            if (SystemUtils.isScreenOn(mContext)) {
                fillData();
                if (!SystemUtils.isScreenLocked(mContext)) {
                    if (!AccessibilityHelper.isAccessibilitySettingsOn(mContext, AnalysysAccessibilityService.class)) {
                        getInfoByVersion();
                    }
                }
            }else{//TODO 黑屏补充数据，待验证
                String openApp = SPHelper.getStringValueFromSP(mContext,EGContext.LASTAPPNAME, "");
                if (!TextUtils.isEmpty(openApp)) {
                    // 补充时间
                    String lastOpenTime = SPHelper.getStringValueFromSP(mContext,EGContext.LASTOPENTIME, "");
                    if(TextUtils.isEmpty(lastOpenTime)){
                        lastOpenTime = "0";
                    }
                    long randomCloseTime = SystemUtils.calculateCloseTime(Long.parseLong(lastOpenTime));
                    SPHelper.setLongValue2SP(mContext,EGContext.ENDTIME,randomCloseTime);
                    filterInsertOCInfo(EGContext.CLOSE_SCREEN);
                }
            }
        } catch (Throwable t) {
            ELOG.i(t.getMessage()+"  processOCprocessOC");
        }finally {
            MessageDispatcher.getInstance(mContext).ocInfo(EGContext.OC_CYCLE, false);
        }
    }

    /**
     * 补数逻辑，在屏幕亮屏但是正在锁屏未解锁的时候，对closeTime为空的数据进行补数操作；
     * 在opten时间和现在时间之间随机一个值：差值在5min左右，直接补数，对于小于5min的频繁的开锁屏不做处理，后续依然有补数操作
     * 为了保险起见，如果屏幕亮屏紧随解锁屏，则在正常状态下进行一批的补数操作
     *
     */
    private void fillData(){
        //TODO 按逻辑注释
        JSONArray fillDataArray = null;
        int blankCount = 0;
        try {
            JSONArray runningApps = TableOC.getInstance(mContext).selectRunning();
            JSONObject json = null;
            List list = SystemUtils.getDiffNO(runningApps.length());
            int random;
            for (int i = 0;i < runningApps.length(); i++) {
                if(blankCount >= 5){
                    break;
                }
                json = (JSONObject)runningApps.get(i);
                random = (Integer)list.get(i);
                String openTime = json.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime);
                long randomCloseTime = SystemUtils.calculateCloseTime(Long.parseLong(openTime));
                if(randomCloseTime == -1){//小于5min的频繁的开锁屏不做处理
                    continue;
                }
                String apn = json.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                ELOG.i(apn + " -------apn" + "    random ::::::" + random);
                if (!TextUtils.isEmpty(apn)) {
                    json.put(DeviceKeyContacts.OCInfo.ApplicationPackageName,
                            apn);
                    json.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime,
                            String.valueOf(randomCloseTime- random));//再随机为了防止结束时间一样
                    json.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.CLOSE_SCREEN);
                    fillDataArray.put(json);
                }else {
                    blankCount += 1;
                }
            }
            // 一次应用操作闭合，更新OCCunt表，打开次数、应用运行状态
            TableOC.getInstance(mContext).updateStopState(fillDataArray);
        }catch (Throwable t){

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
    public void getInfoByVersion() {
        // 4.x和以下版本判断,逻辑: 有权限申请系统API获取; 无权限直接使用5.x/6.x版本
        if (Build.VERSION.SDK_INT < 21) {
            if (PermissionUtils.checkPermission(mContext, Manifest.permission.GET_TASKS)) {
                getRunningTasks();
            } else {
                getProcApps();
            }
        } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {
            // 确定5.0和以上版本UsageStatsManager启用5秒处理一次
            if (SystemUtils.canUseUsageStatsManager(mContext)) {
                 ELOG.i("开启了。UsageStatsManager功能");
                processOCByUsageStatsManager();
            } else {
                // 确定5.0和以上版本proc判断30秒处理一次
                if (isDurLThanThri()) {
                    getProcApps();
                }
            }
        } else {
            // TODO 7.0以上待调研
        }
    }

    /**
     * getRunningTask、辅助功能 OC 信息采集
     */
    public void RunningApps(String pkgName, int collectionType) {
        try {
            this.pkgName = pkgName;
//            ELOG.i(pkgName+"   pkgNamepkgNamepkgNamepkgNamepkgName");
            if(TextUtils.isEmpty(pkgName)){
                return;
            }
            JSONArray cacheApps = TableOC.getInstance(mContext).selectRunning();
            ELOG.i("cacheApps::::::   " + cacheApps);
            if (cacheApps != null && cacheApps.length() > 0) {
                removeRepeat(cacheApps);
                if (cacheApps != null && cacheApps.length() > 0) {
                    // 完成一次闭合，存储到OC表
//                    TableOCTemp.getInstance(mContext).insert(cacheApps);
                    ELOG.i("RunningApps:::::::" + cacheApps);
                    TableOC.getInstance(mContext).insertArray(cacheApps);
                    // 一次应用操作闭合，更新OCCunt表，打开次数、应用运行状态
                    TableOC.getInstance(mContext).updateStopState(cacheApps);
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
                ELOG.i("  processInfos is null 。。。");
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
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        String lastPkgName = SPHelper.getStringValueFromSP(mContext,EGContext.LASTPACKAGENAME,"");
        JSONObject ocJson = getOCInfo(packageName, EGContext.OC_COLLECTION_TYPE_RUNNING_TASK);
        // 是否首次打开
        if (TextUtils.isEmpty(lastPkgName)) {
            ProcessManager.saveSP(mContext,ocJson);
        } else {
            String lastOpenTime = SPHelper.getStringValueFromSP(mContext,EGContext.LASTOPENTIME, "");
            if(TextUtils.isEmpty(lastOpenTime)){
                lastOpenTime = "0";
            }
            long randomCloseTime = SystemUtils.calculateCloseTime(Long.parseLong(lastOpenTime));
//            ELOG.i(randomCloseTime+"  = randomCloseTime");
            if(randomCloseTime == -1){
                return;
            }
            //1.非首次打开，之前有pkgName缓存,设置当前时间往前推一点为结束时间
            SPHelper.setLongValue2SP(mContext,EGContext.ENDTIME,randomCloseTime);
            //2.入库当前的缓存appTime
            filterInsertOCInfo(EGContext.APP_SWITCH);
            // 3.如果打开的包名与缓存的包名不一致，更新新pkgName到sp
            if (!packageName.equals(lastPkgName)) {
                 ELOG.i("=======切换包名。即将保存"+packageName+"  OLD "+lastPkgName);
                ProcessManager.saveSP(mContext,ocJson);
            }
        }
    }


    /**
     * 从Proc中读取数据
     */
    private void getProcApps() {
        JSONArray cacheApps = TableOC.getInstance(mContext).selectRunning();//结束时间为空，则为正在运行的app
        ELOG.i(cacheApps + "   :::::::: cacheApps");
        Set<String> nameSet = ProcessManager.getRunningForegroundApps(mContext);
        if(nameSet == null){
            ELOG.i("XXXINFO没取到数据");
            return;
        }
        try {
            if (cacheApps == null || cacheApps.length() < 1) {
                JSONArray ocArray = new JSONArray();
                for (String name:nameSet) {
                    String pkgName = name.replaceAll(" ","");
                    if (!TextUtils.isEmpty(pkgName)) {
                        ocArray.put(getOCInfo(pkgName.replaceAll(" ", ""), EGContext.OC_COLLECTION_TYPE_PROC));
                    }
                }
                ELOG.i("getProcApps 280:::::" + ocArray);
                TableOC.getInstance(mContext).insertArray(ocArray);
            } else {
                // 去重
                JSONObject res = removeRepeat(cacheApps, nameSet);
                if (res != null && res.length() > 0) {
                    try {
                        cacheApps = new JSONArray(String.valueOf(res.get("cache")));
                        ELOG.i(cacheApps + "   ::::::::: cacheApps:::::");
                    } catch (Throwable t) {
                        ELOG.i("   ::::::::: cacheApps 异常:::::");
                        cacheApps = null;
                    }
                }
                if (cacheApps != null && cacheApps.length() > 0) {
                    // 更新缓存表
                    updateCacheState(cacheApps);
                }
                try {
                    nameSet = (Set<String>) res.get("run");
                } catch (Throwable t) {
                    nameSet = null;
                }
                if (nameSet != null && nameSet.size() > 0) {
                    // 新增该时段缓存信息
                    addCache(nameSet);
//                    ELOG.i("RUN   :" + nameSet);
                }
            }
        } catch (Throwable t) {
            ELOG.i("getProcApps has an exception :::" + t.getMessage());
        }
    }

    /**
     * 缓存中应用列表与新获取应用列表去重
     */
    private JSONObject removeRepeat(JSONArray cacheApps, Set<String> runApps) {

        JSONObject ocInfo = null, result = new JSONObject();
        try {
            List list = SystemUtils.getDiffNO(cacheApps.length());
            int random;
            List<JSONObject> oc = new ArrayList();
            String apn;
            for (int i = 0; i < cacheApps.length() - 1; i++) {
                random = (Integer)list.get(i);
                ocInfo = (JSONObject)cacheApps.get(i);
                if (ocInfo == null || ocInfo.length() < 1){
                    continue;
                }
                // ELOG.i(i+"ocInfoocInfoocInfo :::: "+ocInfo);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime,
                    String.valueOf(System.currentTimeMillis() - random));
                oc.add(ocInfo);
                apn = ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName).replaceAll(" ", "");
                for (String name: runApps) {
                    // ELOG.i(runApps.size()+" :::runApps.size() "+j);
                    String pkgName = name.replaceAll(" ", "");
                    // ELOG.i(pkgName +"::::::::pkgName ::"+apn+":::::"+ apn.equals(pkgName));
                    if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
                        oc.remove(oc.size() - 1);
                        runApps.remove(name);
                        break;
                    }
                }
                // ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType, EGContext.SWITCH_TYPE_DEFAULT);
            }
            // ELOG.i(oc+" :::::: oc");
            if (cacheApps != null && cacheApps.length() > 0) {
                cacheApps = new JSONArray(oc);
                result.put("cache", cacheApps);
                // ELOG.i(cacheApps+" :::::: cacheApps");
            }
            if (runApps != null && runApps.size() > 0) {
                result.put("run", runApps);
                // ELOG.i(runApps+" :::::: runApps");
            }
        } catch (Throwable t) {
            ELOG.e(t.getMessage() + "tttttttttttttttttttt");
        }
        return result;
    }

    /**
     * 更新缓存表
     */
    private void updateCacheState(JSONArray cacheApps) {
        try {
            if (cacheApps != null && cacheApps.length() > 0) {
                // 缓存数据列表与新获取数据列表去重，缓存列表剩余为已经关闭的应用，需要转存储到OC表，并更新运行状态为0
                JSONArray ocList = new JSONArray();
                JSONObject oc = null;
                for (int i = 0; i < cacheApps.length(); i++) {
                    oc = (JSONObject)cacheApps.get(i);
//                    int numb = oc.optInt(DeviceKeyContacts.OCInfo.CU) + 1;
                    String apn = oc.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                    String act = oc.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime);
//                    oc.remove(DeviceKeyContacts.OCInfo.CU);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, apn);
                    jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, act);
//                    jsonObject.put(DeviceKeyContacts.OCInfo.CU, numb);忽略
                    ocList.put(jsonObject);
                }
                TableOC.getInstance(mContext).updateStopState(ocList);
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
            List oc = new ArrayList();
            if (!runApps.isEmpty()) {
                // 缓存数据列表与新获取数据列表去重，新获取列表剩余未新打开的应用，需要缓存到OCCount中，
                List<String> ocInfo = TableOC.getInstance(mContext).getIntervalApps();
                JSONArray runList = getOCArray(runApps);

                JSONArray updateOCInfo = new JSONArray();
                // 将新增列表拆开，该时段有应用打开记录的修改更新记录，该时段没有应用打开记录的新增记录
                for (int i = runList.length() - 1; i >= 0; i--) {
                    oc.add(runList.get(i));
                    String pkgName = new JSONObject(String.valueOf(runList.get(i)))
                        .optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                    if (!TextUtils.isEmpty(pkgName) && ocInfo.contains(pkgName)) {
                        updateOCInfo.put(runList.get(i));
                        // runList.remove(i);
                        oc.remove(oc.size() - 1);
                    }
                }
                runList = new JSONArray(oc);
                if (updateOCInfo != null && updateOCInfo.length() > 0) {
                    // 更新该时段有记录的应用信息，则更新缓存表中的运行状态为1
                    TableOC.getInstance(mContext).updateRunState(updateOCInfo);
                }
                if (runList != null && runList.length() > 0) {
                    // 新增该时段没有记录的应用信息
                    ELOG.i("addCache:::::" + runList);
                    TableOC.getInstance(mContext).insertArray(runList);
                }
            }
        } catch (Throwable t) {

        }

    }

    /**
     * 根据读取出的包列表，获取应用信息并组成json格式添加到列表
     */
    private JSONArray getOCArray(Set<String> runApps) {
        JSONArray list = null;
        try {
            list = new JSONArray();
            for (String name:runApps) {
                String pkgName = name.trim();
                if (!TextUtils.isEmpty(pkgName)) {
                    JSONObject ocJson = getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_PROC);
                    list.put(ocJson);
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
                // 根据包名和时段查询，判断当前时段是否已经启动过，如果有就更新，如果没有就新建
                List<String> ocInfo = TableOC.getInstance(mContext).getIntervalApps();
                JSONObject ocJson = getOCInfo(pkgName, EGContext.OC_COLLECTION_TYPE_RUNNING_TASK);
//                ELOG.i(pkgName + " ==pkgName  &  ocJson =="+ocJson);
                if (ocInfo.contains(pkgName)) {
                    // 该时段存在数据,使用已有记录的数据 更新开始时间结束时间
                    TableOC.getInstance(mContext).update(ocJson);
                } else {
                    // 该时段没有数据，存储该时段的记录
                    ProcessManager.saveSP(mContext,ocJson);
                    TableOC.getInstance(mContext).insert(ocJson);
                }
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
    private void removeRepeat(JSONArray cacheApps) {
        try {
            JSONObject json = null;
            List list = SystemUtils.getDiffNO(cacheApps.length());
            int random;
            for (int i = cacheApps.length() - 1; i >= 0; i--) {
                json = (JSONObject)cacheApps.get(i);
                random = (Integer)list.get(i);

                String apn = json.getString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
//                ELOG.i(apn + " ********** apn" + "    random ::::::" + random);
                if (!TextUtils.isEmpty(apn) && apn.equals(pkgName)) {
                    cacheApps.remove(i);
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
                    // ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName, "unknown");
                }

            }
        } catch (Throwable e) {
            ELOG.e(e + "    ::::::getOCInfo has an exception");
        }
        return ocInfo;
    }

    public void filterInsertOCInfo(String switchType) {
        String OldPkgName = SPHelper.getStringValueFromSP(mContext,EGContext.LASTPACKAGENAME,"");
        String appName = SPHelper.getStringValueFromSP(mContext,EGContext.LASTAPPNAME, "");
        String appVersion = SPHelper.getStringValueFromSP(mContext,EGContext.LASTAPPVERSION,"");
        String openTime = SPHelper.getStringValueFromSP(mContext,EGContext.LASTOPENTIME, "");
        Long closeTime = SPHelper.getLongValueFromSP(mContext,EGContext.ENDTIME, 0);
        String appType = SPHelper.getStringValueFromSP(mContext,EGContext.APP_TYPE, "");
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
//        SPHelper.setLastOpenPackgeName(mContext, "");
//        SPHelper.setLastOpenTime(mContext, "");
//        SPHelper.setLastAppName(mContext, "");
//        SPHelper.setLastAppVerison(mContext, "");
//        SPHelper.setAppType(mContext,"");
    }

    /**
     * android 5/6需要间隔大于30秒
     *
     * @return
     */
    private boolean isDurLThanThri() {
        long now = System.currentTimeMillis();
        if (mProcessTime == 0 || (now - mProcessTime) >= EGContext.OC_CYCLE_OVER_5) {
            mProcessTime = now;
            return true;
        }
        return false;
    }

    /**
     * android 5以上，有UsageStatsManager权限可以使用的
     */
    public void processOCByUsageStatsManager() {
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
                getProcApps();
            }
        } catch (Throwable e) {
            getProcApps();
        }
    }

}
