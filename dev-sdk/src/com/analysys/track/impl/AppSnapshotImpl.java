package com.analysys.track.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.analysys.track.database.TableAppSnapshot;
import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.FileUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.SystemUtils;

import com.analysys.track.utils.reflectinon.EContextHelper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

public class AppSnapshotImpl {
    private static boolean isSnapShotBlockRunning = false;
    Context mContext;
    private AppSnapshotImpl() {

    }

    private static class Holder {
        private static final AppSnapshotImpl INSTANCE = new AppSnapshotImpl();
    }

    public static AppSnapshotImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * 应用列表
     */
    public void snapshotsInfo() {
        try {
            long currentTime = System.currentTimeMillis();
//            long snapCollectCycle = PolicyImpl.getInstance(mContext).getSP().getLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL,EGContext.UPLOAD_CYCLE);
            MessageDispatcher.getInstance(mContext).snapshotInfo(EGContext.SNAPSHOT_CYCLE);
            if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_APPSNAPSHOT,EGContext.SNAPSHOT_CYCLE,currentTime)){
                FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_APPSNAPSHOT,currentTime);
            }else {
                ELOG.i("snapshotsInfo不符合采集轮询周期return，进程Id：< " + SystemUtils.getCurrentProcessName(mContext) + " >");
                return;
            }
            if(!isSnapShotBlockRunning){
                isSnapShotBlockRunning = true;
            }else {
                return;
            }
            if(SystemUtils.isMainThread()){
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        getSnapShotInfo();
                    }
                });
            }else {
                getSnapShotInfo();
            }
        }catch (Throwable t){
        }finally {
            isSnapShotBlockRunning = false;
        }
    }
    private void getSnapShotInfo(){
        try {
            ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"进入getSnapShotInfo获取.....");
            if(!PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SNAPSHOT,true)){
                return;
            }
            Map<String, String> dbSnapshotsMap =
                    TableAppSnapshot.getInstance(mContext).snapShotSelect();
            List<JSONObject> currentSnapshotsList = getCurrentSnapshots();
            if (dbSnapshotsMap != null && !dbSnapshotsMap.isEmpty()) {
                //对比处理当前快照和db数据
                currentSnapshotsList =
                        getDifference(currentSnapshotsList, dbSnapshotsMap);
            }
            TableAppSnapshot.getInstance(mContext)
                    .coverInsert(currentSnapshotsList);
        }catch (Throwable t){
            ELOG.e("snapshotInfo",t.getMessage());
        }
    }

    /**
     * 数据库与新获取的当前列表list做对比合并成新的list 存储
     * 
     * @param currentSnapshotsList
     * @param dbSnapshotsMap
     */
    private List<JSONObject> getDifference(List<JSONObject> currentSnapshotsList,
        Map<String, String> dbSnapshotsMap) {
        try {
            if (currentSnapshotsList == null){
                currentSnapshotsList = new ArrayList<JSONObject>();
            }
            for (int i = 0; i < currentSnapshotsList.size(); i++) {
                JSONObject item = (JSONObject) currentSnapshotsList.get(i);
                String apn = item
                    .getString(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName);
                if (dbSnapshotsMap.containsKey(apn)) {
                    JSONObject dbitem = new JSONObject(dbSnapshotsMap.get(apn));
                    String avc = item.optString(
                        DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode);
                    String dbAvc = dbitem.optString(
                        DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode);
                    if (!TextUtils.isEmpty(avc) && !avc.equals(dbAvc)) {
                        item.put(DeviceKeyContacts.AppSnapshotInfo.ActionType,
                            EGContext.SNAP_SHOT_UPDATE);
                    }
                    dbSnapshotsMap.remove(apn);
                    continue;
                }
                item.put(DeviceKeyContacts.AppSnapshotInfo.ActionType,
                    EGContext.SNAP_SHOT_INSTALL);
            }
            Set<String> set = dbSnapshotsMap.keySet();
            for (String json : set) {
                JSONObject j = new JSONObject(dbSnapshotsMap.get(json));
                j.put(DeviceKeyContacts.AppSnapshotInfo.ActionType,
                    EGContext.SNAP_SHOT_UNINSTALL);
                currentSnapshotsList.add(j);
            }
        } catch (Throwable e) {
            return currentSnapshotsList;
        }
        return currentSnapshotsList;
    }

    /**
     * 获取应用列表快照
     */
    private List<JSONObject> getCurrentSnapshots() {
        List<JSONObject> list = null;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
            if(packageInfo != null && packageInfo.size() > 0){
                list = new ArrayList<JSONObject>();
                JSONObject jsonObject = null;
                PackageInfo pi = null;
                for (int i = 0; i < packageInfo.size(); i++) {
                    try {
                        pi = packageInfo.get(i);
                        if(pi != null){
                            jsonObject = null;
                            jsonObject = getAppInfo(pi, EGContext.SNAP_SHOT_INSTALL);
                            if (jsonObject != null) {
                                list.add(jsonObject);
                            }
                        }
                    }catch (Throwable t){
                    }
                }
                if(list.size() < 5){
                    list = SystemUtils.getAppsFromShell(mContext,EGContext.SNAP_SHOT_INSTALL,list);
                }
            }else {
                //如果上面的方法不能获取，改用shell命令
                if(list == null){
                    list = new ArrayList<JSONObject>();
                    if(list.size()<5){
                        list = SystemUtils.getAppsFromShell(mContext,EGContext.SNAP_SHOT_INSTALL,list);
                    }
                }
            }

        } catch (Exception e) {
            ELOG.e("xxx",e.getMessage());
        }
//        ELOG.i("xxx","list:::::"+list);
        return list;
    }

    /**
     * 单个应用json信息
     * 
     * @param pkgInfo
     * @param tag
     * @return
     */
    private JSONObject getAppInfo(PackageInfo pkgInfo, String tag) {
        JSONObject appInfo = null;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            appInfo = getAppInfo(appInfo, pkgInfo,packageManager,tag);
        } catch (Throwable e) {
            ELOG.e("xxx",e.getMessage()+" has an excption");
        }
//        ELOG.i("xxx","appInfo === "+appInfo);
        return appInfo;
    }
    public JSONObject getAppInfo(JSONObject appInfo,PackageInfo pkgInfo, PackageManager packageManager,String tag) throws JSONException {
        appInfo = new JSONObject();
        JsonUtils.pushToJSON(mContext, appInfo, DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName, pkgInfo.packageName,DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
        JsonUtils.pushToJSON(mContext, appInfo, DeviceKeyContacts.AppSnapshotInfo.ApplicationName, String.valueOf(pkgInfo.applicationInfo.loadLabel(packageManager)),DataController.SWITCH_OF_APPLICATION_NAME);
        JsonUtils.pushToJSON(mContext, appInfo, DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode, pkgInfo.versionName + "|" + pkgInfo.versionCode,DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
        JsonUtils.pushToJSON(mContext, appInfo, DeviceKeyContacts.AppSnapshotInfo.ActionType, tag,DataController.SWITCH_OF_ACTION_TYPE);
        JsonUtils.pushToJSON(mContext, appInfo, DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime, String.valueOf(System.currentTimeMillis()),DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
//        appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName,
//                pkgInfo.packageName);
//        appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationName,
//                String.valueOf(pkgInfo.applicationInfo.loadLabel(packageManager)));
//        appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode,
//                pkgInfo.versionName + "|" + pkgInfo.versionCode);
//        appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ActionType, tag);
//        appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime,
//                String.valueOf(System.currentTimeMillis()));
        return appInfo;
    }

    /**
     * 处理应用安装卸载更新广播改变状态
     */
    public void changeActionType(final String pkgName, final int type,final long time) {
        try {
            if (TextUtils.isEmpty(pkgName)) {
                return;
            }
            if(SystemUtils.isMainThread()){
                // 数据库操作修改包名和类型
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (type == 0) {
                                PackageInfo pi =
                                        mContext.getPackageManager().getPackageInfo(pkgName, 0);
                                if(pi != null){
                                    JSONObject jsonObject =
                                            getAppInfo(pi, EGContext.SNAP_SHOT_INSTALL);
                                    if (jsonObject != null) {
                                        // 判断数据表中是否有该应用的存在，如果有标识此次安装是应用更新所导致
                                        boolean isHas = TableAppSnapshot.getInstance(mContext)
                                                .isHasPkgName(pkgName);
                                        if (!isHas) {
                                            TableAppSnapshot.getInstance(mContext).insert(jsonObject);
                                        }
                                    }
                                }
                            } else if (type == 1) {
                                TableAppSnapshot.getInstance(mContext).update(pkgName,
                                        EGContext.SNAP_SHOT_UNINSTALL,time);
                            } else if (type == 2) {
                                TableAppSnapshot.getInstance(mContext).update(pkgName,
                                        EGContext.SNAP_SHOT_UPDATE,time);
                            }
                        } catch (Throwable e) {
                            ELOG.e(e);
                        }
                    }
                });
            }else{
                try {
                    if (type == 0) {
                        PackageInfo pi =
                                mContext.getPackageManager().getPackageInfo(pkgName, 0);
                        if(pi != null){
                            JSONObject jsonObject =
                                    getAppInfo(pi, EGContext.SNAP_SHOT_INSTALL);
                            if (jsonObject != null) {
                                // 判断数据表中是否有该应用的存在，如果有标识此次安装是应用更新所导致
                                boolean isHas = TableAppSnapshot.getInstance(mContext)
                                        .isHasPkgName(pkgName);
                                if (!isHas) {
                                    TableAppSnapshot.getInstance(mContext).insert(jsonObject);
                                }
                            }
                        }
                    } else if (type == 1) {
                        TableAppSnapshot.getInstance(mContext).update(pkgName,
                                EGContext.SNAP_SHOT_UNINSTALL,time);
                    } else if (type == 2) {
                        TableAppSnapshot.getInstance(mContext).update(pkgName,
                                EGContext.SNAP_SHOT_UPDATE,time);
                    }
                } catch (Throwable e) {
                    ELOG.e(e);
                }
            }

        }catch (Throwable t){

        }

    }

}
