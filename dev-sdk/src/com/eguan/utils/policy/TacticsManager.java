package com.eguan.utils.policy;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eguan.Constants;
import com.eguan.db.DBPorcesser;
import com.eguan.monitor.fangzhou.service.MonitorService;
import com.eguan.utils.commonutils.AppSPUtils;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.ReceiverUtils;
import com.eguan.utils.commonutils.SPHodler;

import org.json.JSONObject;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 下发的策略管理
 * @Version: 1.0
 * @Create: 2017/4/25 15:17:58
 * @Author: Wang
 */
public class TacticsManager {

    private static TacticsManager instance;
    private Context context;
    SPHodler spUtil;

    public static TacticsManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TacticsManager.class) {
                if (instance == null) {
                    instance = new TacticsManager(context);
                }
            }
        }
        return instance;
    }

    public TacticsManager(Context context) {
        this.context = context;
        spUtil = SPHodler.getInstance(context);
    }

    public void devTacticsProcess(JSONObject json) {
        String devValue = "";
        try {
            if (json.has("ue")) {
                JSONObject jobt = new JSONObject(json.getString("ue"));
                if (jobt.has("dValid")) {
                    devValue = jobt.getString("dValid");
                    if (!TextUtils.isEmpty(devValue) && devValue.equals(Constants.TACTICS_STATE)) {
                        spUtil.setDeviceTactics(devValue);
                        ReceiverUtils.getInstance().unRegistAllReceiver(context, true);
                        deleteDevData(context);
                        context.stopService(new Intent(context, MonitorService.class));
                        System.exit(0);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
            }
            policy(json);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

    }

    private void policy(JSONObject json) {
        try {
            if (json.has("policy")) {
                JSONObject jobt = new JSONObject(json.getString("policy"));
                if (jobt.has("remoteIp")) {
                    String remoteIp = jobt.getString("remoteIp");
                    spUtil.setNetIpTag(remoteIp);
                }
                if (jobt.has("policyVer")) {
                    String policyVer = jobt.getString("policyVer");
                    if (!TextUtils.isEmpty(policyVer)) {
                        AppSPUtils.getInstance(context).setPolicyVer(policyVer);
                    }
                }
                // 数据合并间隔，默认是10秒
                if (jobt.has("mergeInterval")) {
                    long mergeInterval = jobt.getLong("mergeInterval");
                    if (mergeInterval != 0 && mergeInterval != 10) {
                        AppSPUtils.getInstance(context).setMergeInterval(mergeInterval);
                    }
                }
                // 最小使用时长，默认是5秒
                if (jobt.has("minDuration")) {
                    long minDuration = jobt.getLong("minDuration");
                    if (minDuration != 0 && minDuration != 5) {
                        spUtil.setMinDuration(minDuration);
                    }
                }
                // 最长使用时长，默认是18000秒
                if (jobt.has("maxDuration")) {
                    long maxDuration = jobt.getLong("maxDuration");
                    if (maxDuration != 0 && maxDuration != 18000) {
                        spUtil.setMaxDuration(maxDuration);
                    }
                }
                if (jobt.has("mergeInterval")) {
                    String mergeInterval = jobt.getString("mergeInterval");
                    if (!TextUtils.isEmpty(mergeInterval)) {
                        spUtil.setMergeInterval(Long.parseLong(mergeInterval) * 1000);

                    }
                }
                if (jobt.has("minDuration")) {
                    String minDuration = jobt.getString("minDuration");
                    if (!TextUtils.isEmpty(minDuration)) {
                        spUtil.setMinDuration(Long.parseLong(minDuration) * 1000);
                    }
                }

            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    private void deleteDevData(final Context context) {

        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.v("deleteDevData -------- 清空设备信息 --------");
        }
        DBPorcesser.getInstance(context).deleteDeviceAllInfo();
    }

}
