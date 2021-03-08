package com.analysys.track.internal.impl.locations;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.PermissionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: wifi信息获取
 * @Version: 1.0
 * @Create: 2019-08-05 16:18:07
 * @author: sanbo
 */
public class WifiImpl {
    Context mContext;

    private WifiImpl() {
    }

    public static WifiImpl getInstance(Context context) {
        if (WifiImpl.Holder.INSTANCE.mContext == null) {
            WifiImpl.Holder.INSTANCE.mContext = EContextHelper.getContext();
        }

        return WifiImpl.Holder.INSTANCE;
    }

    /**
     * WiFi信息
     */
    public JSONArray getWifiInfo() {
        JSONArray jar = new JSONArray();
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                if (!PermissionUtils.checkPermission(mContext, Manifest.permission.CHANGE_WIFI_STATE)) {
                    return null;
                }
                if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_WIFI_STATE)
                        && PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
                    if (wm != null && wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                        List<ScanResult> list = wm.getScanResults();
                        wifiSort(list);
                        ScanResult s = null;
                        JSONObject jsonObject = null;
                        for (int i = 0; i < list.size(); i++) {
                            if (i < 5) {
                                s = list.get(i);
                                jsonObject = new JSONObject();
                                jsonObject = getWifiInfoObj(jsonObject, s.SSID, s.BSSID, s.level, s.capabilities,
                                        s.frequency);
                                if (jsonObject != null && jsonObject.length() > 0) {
                                    jar.put(jsonObject);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    
        return jar;
    }

    /**
     * wifi 列表排序
     */
    private void wifiSort(List<ScanResult> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).level < list.get(j).level) {
                    ScanResult scanResult = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, scanResult);
                }
            }
        }
    }

    public JSONObject getWifiInfoObj(JSONObject jsonObject, String ssid, String bssid, int level, String capabilities,
                                     int frequency) {
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.WifiInfo.SSID, ssid,
                        DataController.SWITCH_OF_SSID);
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.WifiInfo.BSSID, bssid,
                        DataController.SWITCH_OF_BSSID);
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.WifiInfo.Level, level,
                        DataController.SWITCH_OF_LEVEL);
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.WifiInfo.Capabilities,
                        capabilities, DataController.SWITCH_OF_CAPABILITIES);
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.WifiInfo.Frequency, frequency,
                        DataController.SWITCH_OF_FREQUENCY);
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(t);
                }
            }
        }
        
        return jsonObject;
    }

    private static class Holder {
        private static final WifiImpl INSTANCE = new WifiImpl();
    }

}