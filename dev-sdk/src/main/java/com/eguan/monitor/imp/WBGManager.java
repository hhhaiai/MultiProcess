package com.eguan.monitor.imp;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.commonutils.SystemUtils;
import com.eguan.monitor.dbutils.device.DeviceTableOperation;

import java.util.List;


public class WBGManager {

    Context context;
    WBGInfo wbgInfo;
    private static WBGManager instance = null;

    private WBGManager(Context context) {
        this.context = context;
        wbgInfo = new WBGInfo();
    }

    public static WBGManager getInstance(Context context) {
        if (instance == null) {
            synchronized (WBGManager.class) {
                if (instance == null) {
                    instance = new WBGManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 将WiFi信息，基站信息，经纬度存储整合
     */
    public void getWBGInfo() {
        if (getDataTime()) {
            EgLog.e("-- 获取WBG数据 --");
            LocationChangeManager.getInstance(context).getLocationInfo();
            wbgInfo.setCollectionTime(System.currentTimeMillis() + "");
            getBaseStationInfo();
            getWifiInfo();
            String location = SPUtil.getInstance(context).getLastLocation();
            wbgInfo.setGeographyLocation(location);
            saveWBGInfo();
        }
    }

    private boolean getDataTime() {
        long nowTime = System.currentTimeMillis();
        SPUtil sputil = SPUtil.getInstance(context);
        long oldeTime = sputil.getWBGInfoTime();
        if (Constants.GIT_DATA_TIME_INTERVAL <= nowTime - oldeTime) {
            sputil.setWBGInfoTime(nowTime);
            return true;
        }
        return false;
    }

    /**
     * 获取基站信息
     */
    private void getBaseStationInfo() {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (TelephonyManager.SIM_STATE_ABSENT != tm.getSimState()) {
                TelephonyManager mTelNet = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (mTelNet == null) {
                    cleanWBGInfo();
                    return;
                }
                GsmCellLocation location = null;
                if (SystemUtils.checkPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        && SystemUtils.checkPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                    location = (GsmCellLocation) mTelNet.getCellLocation();
                if (location == null) {
                    cleanWBGInfo();
                    return;
                }
                int cid = location.getCid();
                int lac = location.getLac();
                if (cid <= 0 || lac <= 0) {
                    cleanWBGInfo();
                    return;
                }

                wbgInfo.setCellId(location.getCid() + "");
                wbgInfo.setLocationAreaCode(location.getLac() + "");


                EgLog.e("location.getCid()::::::" + location.getCid());

            } else {
                cleanWBGInfo();
                EgLog.e("没有发现SIM卡");
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    private void cleanWBGInfo() {
//        wbgInfo.setCollectionTime("");
        wbgInfo.setCellId("");
        wbgInfo.setLocationAreaCode("");
    }

    /**
     * 获取WiFi信息
     */
    private void getWifiInfo() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo(); // 当前WIFI连接信息
            wifiInfo.getMacAddress();
            List<ScanResult> list = wifiManager.getScanResults();// 搜索到的设备列表
            int listSize = list.size();
            if (listSize == 0) {
                if (SystemUtils.isWifi(context)) {
                    wbgInfo.setSSID(wifiInfo.getSSID().replace("\"", ""));
                    wbgInfo.setBSSID(wifiInfo.getBSSID());
                    wbgInfo.setLevel(wifiInfo.getRssi() + "");
                } else {
                    wbgInfo.setSSID("");
                    wbgInfo.setBSSID("");
                    wbgInfo.setLevel("");
                }
            } else {
                if (listSize > 3) {
                    listSize = 3;
                }
                String SSID = null, BSSID = null, level = null;
                for (int i = 0; i < listSize; i++) {
                    if (i == 0) {
                        SSID = list.get(i).SSID;
                        BSSID = list.get(i).BSSID;
                        level = list.get(i).level + "";
                    } else {
                        SSID += "|" + list.get(i).SSID;
                        BSSID += "|" + list.get(i).BSSID;
                        level += "|" + list.get(i).level;
                    }
                    wbgInfo.setSSID(SSID);
                    wbgInfo.setBSSID(BSSID);
                    wbgInfo.setLevel(level);
                }
            }
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    /**
     * 将WiFi信息，基站信息，经纬度存储至数据库
     */
    private void saveWBGInfo() {
        if (wbgInfo != null) {
            EgLog.e("" + wbgInfo.getBSSID() + ",CellId:" + wbgInfo.getCellId() + ",GL:" + wbgInfo.getGeographyLocation());

            try {
                String tag = SPUtil.getInstance(context).getNetIpTag();

                if (tag.equals("") || tag.equals("0")) {
//                    wbgInfo.setIp(getNetIp());
                    //update by sanbo
                    wbgInfo.setIp("");
                } else {
                    wbgInfo.setIp("");
                }
                DeviceTableOperation.getInstance(context).insertWBGInfo(wbgInfo);
            } catch (Throwable e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            }
        }
    }
}
