package com.analysys.dev.internal.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.analysys.dev.database.TableAppSnapshot;
import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.EThreadPool;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.utils.sp.SPHelper;
import com.analysys.dev.internal.work.MessageDispatcher;

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
    private AppSnapshotImpl(){

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

    public class Snapshot {
        public final static String APN = "APN";
        public final static String AN = "AN";
        public final static String AVC = "AVC";
        public final static String AT = "AT";
        public final static String AHT = "AHT";
    }

    /**
     * 应用列表
     */
    public void snapshotsInfo() {
        if (isGetSnapshots()) {
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> dbSnapshotsMap = TableAppSnapshot.getInstance(mContext).mSelect();
                    List<JSONObject> currentSnapshotsList = getCurrentSnapshots();
                    if (!dbSnapshotsMap.isEmpty()) {
                        currentSnapshotsList = getDifference(currentSnapshotsList, dbSnapshotsMap);
                    }
                    TableAppSnapshot.getInstance(mContext).coverInsert(currentSnapshotsList);
                    SPHelper.getDefault(mContext).edit().putLong(EDContext.SP_SNAPSHOT_TIME, System.currentTimeMillis())
                        .commit();
                    MessageDispatcher.getInstance(mContext).snapshotInfo(EDContext.SNAPSHOT_CYCLE);
                }
            });
        }
    }

    /**
     * 判断是否到达获取快照时间
     */
    private boolean isGetSnapshots() {
        Long time = SPHelper.getDefault(mContext).getLong(EDContext.SP_SNAPSHOT_TIME, 0);
        if (time == 0) {
            return true;
        } else {
            if (EDContext.SNAPSHOT_CYCLE <= (System.currentTimeMillis() - time)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 数据库与新获取的当前列表list做对比合并成新的list 存储
     * @param currentSnapshotsList
     * @param dbSnapshotsMap
     */
    private List<JSONObject> getDifference(List<JSONObject> currentSnapshotsList, Map<String, String> dbSnapshotsMap) {
        try {
            for (int i = 0; i < currentSnapshotsList.size(); i++) {
                JSONObject item = currentSnapshotsList.get(i);
                String apn = item.getString(Snapshot.APN);
                if (dbSnapshotsMap.containsKey(apn)) {
                    JSONObject dbitem = new JSONObject(dbSnapshotsMap.get(apn));
                    String avc = item.optString(Snapshot.AVC);
                    String dbAvc = dbitem.optString(Snapshot.AVC);
                    if (!TextUtils.isEmpty(avc) && !avc.equals(dbAvc)) {
                        item.put(Snapshot.AT, "2");
                    }
                    dbSnapshotsMap.remove(apn);
                    continue;
                }
                item.put(Snapshot.AT, "0");
            }
            Set<String> set = dbSnapshotsMap.keySet();
            for (String json : set) {
                JSONObject j = new JSONObject(dbSnapshotsMap.get(json));
                j.put(Snapshot.AT, "1");
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
                JSONObject job = getAppInfo(pi, "-1");
                if (job != null) {
                    list.add(job);
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
        JSONObject job = null;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            job = new JSONObject();
            job.put(Snapshot.APN, pkgInfo.packageName);
            job.put(Snapshot.AN, String.valueOf(pkgInfo.applicationInfo.loadLabel(packageManager)));
            job.put(Snapshot.AVC, pkgInfo.versionName + "|" + pkgInfo.versionCode);
            job.put(Snapshot.AT, tag);
            job.put(Snapshot.AHT, String.valueOf(System.currentTimeMillis()));
        } catch (Throwable e) {
        }
        return job;
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
                        PackageInfo pi = mContext.getPackageManager().getPackageInfo(pkgName, 0);
                        JSONObject job = getAppInfo(pi, "0");
                        if (job != null) {
                            // 判断数据表中是否有该应用的存在，如果有标识此次安装是应用更新所导致
                            boolean isHas = TableAppSnapshot.getInstance(mContext).isHasPkgName(pkgName);
                            if (!isHas) {
                                TableAppSnapshot.getInstance(mContext).insert(job);
                            }
                        }
                    } else if (type == 1) {
                        TableAppSnapshot.getInstance(mContext).update(pkgName, "1");
                    } else if (type == 2) {
                        TableAppSnapshot.getInstance(mContext).update(pkgName, "2");
                    }
                } catch (Throwable e) {
                    ELOG.e(e);
                }
            }
        });
    }
}
