package com.analysys.dev.internal.impl;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.utils.PermissionUtils;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONException;
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
            if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_WIFI_STATE)) {
                WifiManager wm = (WifiManager)mContext.getSystemService(WIFI_SERVICE);
                int wifiDetail = SPHelper.getDefault(mContext).getInt(EDContext.SP_WIFI_DETAIL, 0);
                if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    List<ScanResult> list = wm.getScanResults();
                    wifiSort(list);
                    for (int i = 0; i < list.size(); i++) {
                        if (i < 5) {
                            ScanResult s = list.get(i);
                            JSONObject job = new JSONObject();
                            job.put("SSID", s.SSID);
                            job.put("BSSID", s.BSSID);
                            job.put("LEVEL", s.level);
                            if (wifiDetail == 1) {
                                job.put("CBT", s.capabilities);
                                job.put("FQC", s.frequency);
                            }
                            jar.put(job);
                        }
                    }
                }
            }
        } catch (JSONException e) {
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
