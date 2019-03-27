package com.analysys.track.impl;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.internal.Content.DeviceKeyContacts;

import com.analysys.track.utils.PermissionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiImpl {
    Context mContext;

    private static class Holder {
        private static final WifiImpl INSTANCE = new WifiImpl();
    }

    public static WifiImpl getInstance(Context context) {
        if (WifiImpl.Holder.INSTANCE.mContext == null) {
            WifiImpl.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }

        return WifiImpl.Holder.INSTANCE;
    }
    /**
     * WiFi信息
     */
    public JSONArray getWifiInfo() {
        JSONArray jar = new JSONArray();
        try {
            if (!PermissionUtils.checkPermission(mContext, Manifest.permission.CHANGE_WIFI_STATE)) {
                return null;
            }
            if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_WIFI_STATE) && PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                WifiManager wm = (WifiManager)mContext.getSystemService(WIFI_SERVICE);
                if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    List<ScanResult> list = wm.getScanResults();
                    wifiSort(list);
                    ScanResult s = null;
                    JSONObject jsonObject = null;
                    for (int i = 0; i < list.size(); i++) {
                        if (i < 5) {
                            s = list.get(i);
                            jsonObject = new JSONObject();
                            JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.WifiInfo.TIME, System.currentTimeMillis(),DataController.SWITCH_OF_SST);
                            JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.WifiInfo.SSID, s.SSID,DataController.SWITCH_OF_SSID);
                            JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.WifiInfo.BSSID, s.BSSID,DataController.SWITCH_OF_BSSID);
                            JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.WifiInfo.Level, s.level,DataController.SWITCH_OF_LEVEL);
                            JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.WifiInfo.Capabilities, s.capabilities,DataController.SWITCH_OF_CAPABILITIES);
                            JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.WifiInfo.Frequency, s.frequency,DataController.SWITCH_OF_FREQUENCY);
                            jar.put(jsonObject);
                        }
                    }
                }
            }
        } catch (Throwable e) {
        }
        return jar;
    }
    /**
     * wifi 列表排序
     */
    private void wifiSort(List<ScanResult> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).level > list.get(j).level) {
                    ScanResult scanResult = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, scanResult);
                }
            }
        }
    }


}
