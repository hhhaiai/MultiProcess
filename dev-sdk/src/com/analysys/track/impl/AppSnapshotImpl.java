package com.analysys.track.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.database.TableAppSnapshot;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
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
        }
    }
    private void getSnapShotInfo(){
        Map<String, String> dbSnapshotsMap =
                TableAppSnapshot.getInstance(mContext).snapShotSelect();
        JSONArray currentSnapshotsList = getCurrentSnapshots();
        if (dbSnapshotsMap != null && !dbSnapshotsMap.isEmpty()) {
            currentSnapshotsList =
                    getDifference(currentSnapshotsList, dbSnapshotsMap);
        }
        TableAppSnapshot.getInstance(mContext)
                .coverInsert(currentSnapshotsList);
        MessageDispatcher.getInstance(mContext)
                .snapshotInfo(EGContext.SNAPSHOT_CYCLE,false);
    }

    /**
     * 数据库与新获取的当前列表list做对比合并成新的list 存储
     * 
     * @param currentSnapshotsList
     * @param dbSnapshotsMap
     */
    private JSONArray getDifference(JSONArray currentSnapshotsList,
        Map<String, String> dbSnapshotsMap) {
        try {
            if (currentSnapshotsList == null){
                currentSnapshotsList = new JSONArray();
            }
            for (int i = 0; i < currentSnapshotsList.length(); i++) {
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
                currentSnapshotsList.put(j);
            }
        } catch (Throwable e) {
            return currentSnapshotsList;
        }
        return currentSnapshotsList;
    }

    /**
     * 获取应用列表快照
     */
    private JSONArray getCurrentSnapshots() {
        JSONArray list = null;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
            list = new JSONArray();
            for (int i = 0; i < packageInfo.size(); i++) {
                PackageInfo pi = packageInfo.get(i);
                JSONObject jsonObject = getAppInfo(pi, EGContext.SNAP_SHOT_INSTALL);
                if (jsonObject != null) {
                    list.put(jsonObject);
                }
            }
            //如果上面的方法不能获取，改用shell命令
            if(list == null || list.length()<1){
                list = SystemUtils.getAppsFromShell(mContext,EGContext.SNAP_SHOT_INSTALL);
            }
        } catch (Exception e) {
        }
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
            appInfo = new JSONObject();
            appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName,
                pkgInfo.packageName);
            appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationName,
                String.valueOf(pkgInfo.applicationInfo.loadLabel(packageManager)));
            appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode,
                pkgInfo.versionName + "|" + pkgInfo.versionCode);
            appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ActionType, tag);
            appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime,
                String.valueOf(System.currentTimeMillis()));
        } catch (Throwable e) {
        }
        return appInfo;
    }

    /**
     * 处理应用安装卸载更新广播改变状态
     */
    public void changeActionType(final String pkgName, final int type) {
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
                            } else if (type == 1) {
                                TableAppSnapshot.getInstance(mContext).update(pkgName,
                                        EGContext.SNAP_SHOT_UNINSTALL);
                            } else if (type == 2) {
                                TableAppSnapshot.getInstance(mContext).update(pkgName,
                                        EGContext.SNAP_SHOT_UPDATE);
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
                    } else if (type == 1) {
                        TableAppSnapshot.getInstance(mContext).update(pkgName,
                                EGContext.SNAP_SHOT_UNINSTALL);
                    } else if (type == 2) {
                        TableAppSnapshot.getInstance(mContext).update(pkgName,
                                EGContext.SNAP_SHOT_UPDATE);
                    }
                } catch (Throwable e) {
                    ELOG.e(e);
                }
            }

        }catch (Throwable t){

        }

    }

}
