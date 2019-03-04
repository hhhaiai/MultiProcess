package com.analysys.track.internal.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.analysys.track.database.TableAppSnapshot;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description:
 * @Version: 1.0
 * @Create: 2018/10/17 14:16
 * @Author: Wang-X-C
 */
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

    // public class Snapshot {
    // public final static String APN = "APN";
    // public final static String AN = "AN";
    // public final static String AVC = "AVC";
    // public final static String AT = "AT";
    // public final static String AHT = "AHT";
    // }

    /**
     * 应用列表
     */
    public void snapshotsInfo() {
        if (isGetSnapshots()) {
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> dbSnapshotsMap =
                        TableAppSnapshot.getInstance(mContext).mSelect();
                    List<JSONObject> currentSnapshotsList = getCurrentSnapshots();
                    if (dbSnapshotsMap != null && !dbSnapshotsMap.isEmpty()) {
                        currentSnapshotsList =
                            getDifference(currentSnapshotsList, dbSnapshotsMap);
                    }
                    TableAppSnapshot.getInstance(mContext)
                        .coverInsert(currentSnapshotsList);
                    SPHelper.getDefault(mContext).edit()
                        .putLong(EGContext.SP_SNAPSHOT_TIME, System.currentTimeMillis())
                        .commit();
                    SPHelper.getDefault(mContext).edit()
                        .putLong(EGContext.SNAPSHOT_LAST_TIME, System.currentTimeMillis())
                        .commit();
                    MessageDispatcher.getInstance(mContext)
                        .snapshotInfo(EGContext.SNAPSHOT_CYCLE);
                }
            });
        }
    }

    /**
     * 判断是否到达获取快照时间
     */
    private boolean isGetSnapshots() {
        Long time = SPHelper.getDefault(mContext).getLong(EGContext.SP_SNAPSHOT_TIME, 0);
        if (time == 0) {
            return true;
        } else {
            if (EGContext.SNAPSHOT_CYCLE <= (System.currentTimeMillis() - time)) {
                return true;
            }
        }
        return false;
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
            if (currentSnapshotsList == null)
                currentSnapshotsList = new ArrayList<JSONObject>();
            for (int i = 0; i < currentSnapshotsList.size(); i++) {
                JSONObject item = currentSnapshotsList.get(i);
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
            list = new ArrayList<JSONObject>();
            for (int i = 0; i < packageInfo.size(); i++) {
                PackageInfo pi = packageInfo.get(i);
                // 过滤掉系统app
                // if ((ApplicationInfo.FLAG_SYSTEM & pi.applicationInfo.flags) != 0) {
                // continue;
                // }
                JSONObject jsonObject = getAppInfo(pi, EGContext.SNAP_SHOT_INSTALL);
                if (jsonObject != null) {
                    list.add(jsonObject);
                }
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
    @SuppressWarnings("deprecation")
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
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
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
                    ELOG.i(type + "   type");
                } catch (Throwable e) {
                    ELOG.e(e);
                }
            }
        });
    }
}
