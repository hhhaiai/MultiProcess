package com.eguan.imp;

import java.util.List;

import com.eguan.Constants;
import com.eguan.db.DBPorcesser;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.SPHodler;
import com.eguan.utils.commonutils.SystemUtils;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

public class WBGManager {

    private Context mContext;
    private WBGInfo mWbgInfo;

    private static class Holder {
        private static final WBGManager INSTANCE = new WBGManager();
    }

    public static WBGManager getInstance(Context context) {

        if (context != null) {
            Holder.INSTANCE.mContext = context.getApplicationContext();
        }
        return WBGManager.Holder.INSTANCE;
    }

    private WBGManager() {
        mWbgInfo = new WBGInfo();
    }

    /**
     * 将WiFi信息，基站信息，经纬度存储整合
     */
    public void getWBGInfo() {
        // 30分钟获取一次。
        if (getDataTime()) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.v("-- 获取WBG数据 --");
            }
            LocationChangeManager.getInstance(mContext).getLocationInfo();
            mWbgInfo.setCollectionTime(String.valueOf(System.currentTimeMillis()));
            getBaseStationInfo();
            getWifiInfo();
            String location = SPHodler.getInstance(mContext).getLastLocation();
            mWbgInfo.setGeographyLocation(location);
            saveWBGInfo();
        }
    }

    private boolean getDataTime() {
        long nowTime = System.currentTimeMillis();
        SPHodler sputil = SPHodler.getInstance(mContext);
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
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (TelephonyManager.SIM_STATE_ABSENT != tm.getSimState()) {
                TelephonyManager mTelNet = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (mTelNet == null) {
                    cleanWBGInfo();
                    return;
                }
                GsmCellLocation location = null;
                if (SystemUtils.checkPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        && SystemUtils.checkPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION))
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

                cid = location.getCid();
                mWbgInfo.setCellId(String.valueOf(location.getCid()));
                mWbgInfo.setLocationAreaCode(String.valueOf(location.getLac()));

                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.v("getBaseStationInfo  Cid:" + String.valueOf(location.getCid()) + "; LocationAreaCode: "
                            + String.valueOf(location.getLac()));
                }

            } else {
                cleanWBGInfo();
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.w("没有发现SIM卡");
                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    private void cleanWBGInfo() {
        // mWbgInfo.setCollectionTime("");
        mWbgInfo.setCellId("");
        mWbgInfo.setLocationAreaCode("");
        // sCid = -1;
        // sLac = -1;
    }

    /**
     * 获取WiFi信息
     */
    private void getWifiInfo() {
        try {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo(); // 当前WIFI连接信息

            List<ScanResult> list = null;
            if (SystemUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    || SystemUtils.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {

                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.v(" WBGInfo getWifiInfo 有权限。");
                }
                list = wifiManager.getScanResults();// 搜索到的设备列表

                int listSize = list.size();
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
                    mWbgInfo.setSSID(SSID);
                    mWbgInfo.setBSSID(BSSID);
                    mWbgInfo.setLevel(level);
                }
            } else {
                if (SystemUtils.isWifi(mContext)) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.v(" WBGInfo getWifiInfo 没权限。 wifi");
                    }
                    mWbgInfo.setSSID(wifiInfo.getSSID().replace("\"", ""));
                    mWbgInfo.setBSSID(wifiInfo.getBSSID());
                    mWbgInfo.setLevel(wifiInfo.getRssi() + "");
                } else {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.v(" WBGInfo getWifiInfo 没权限。 wifi");
                    }
                    mWbgInfo.setSSID("");
                    mWbgInfo.setBSSID("");
                    mWbgInfo.setLevel("");
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
        if (mWbgInfo != null) {

            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.v("saveWBGInfo==> BSSID:" + mWbgInfo.getBSSID() + " ,CellId:" + mWbgInfo.getCellId() + " ,GL:"
                        + mWbgInfo.getGeographyLocation());
            }
            try {
                String tag = SPHodler.getInstance(mContext).getNetIpTag();

                if (tag.equals("") || tag.equals("0")) {
                    // mWbgInfo.setIp(getNetIp());
                    // update by sanbo
                    mWbgInfo.setIp("");
                } else {
                    mWbgInfo.setIp("");
                }
                DBPorcesser.getInstance(mContext).insertWBGInfo(mWbgInfo);
            } catch (Throwable e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            }
        }
    }
}
