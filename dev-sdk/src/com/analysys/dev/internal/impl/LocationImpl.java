package com.analysys.dev.internal.impl;

import static android.content.Context.WIFI_SERVICE;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.dev.database.DBConfig;
import com.analysys.dev.database.TableLocation;
import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.EThreadPool;
import com.analysys.dev.utils.PermissionUtils;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.utils.sp.SPHelper;
import com.analysys.dev.internal.work.MessageDispatcher;

import android.Manifest;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/15 15:01
 * @Author: Wang-X-C
 */
public class LocationImpl {

    Context mContext;

    private static class Holder {
        private static final LocationImpl INSTANCE = new LocationImpl();
    }

    public static LocationImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }

        return Holder.INSTANCE;
    }

    public void location() {
        EThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (!isGetLocation()) {
                    return;
                }
                JSONObject location = getLocation();
                if (location != null) {
                    TableLocation.getInstance(mContext).insert(String.valueOf(location));
                    SPHelper.getDefault(mContext).edit().putLong(EGContext.SP_LOCATION_TIME, System.currentTimeMillis())
                        .commit();
                }
                MessageDispatcher.getInstance(mContext).locationInfo(EGContext.LOCATION_CYCLE);
            }
        });
    }

    private boolean isGetLocation() {
        long time = SPHelper.getDefault(mContext).getLong(EGContext.SP_LOCATION_TIME, 0);
        if (time == 0) {
            return true;
        } else {
            if (System.currentTimeMillis() - time >= EGContext.LOCATION_CYCLE) {
                return true;
            } else {
                return false;
            }
        }
    }
    private Location getLocationInfo(){
        //TODO 获取6.0以上的location对象信息并返回
        if (Build.VERSION.SDK_INT > 22) {
            if (!PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    && !PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ELOG.e("has no permission");
                return null;
            }
        }
        LocationManager lm =
                (LocationManager)mContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (lm == null) {
            return null;
        }
        ELOG.i("是否包含GPS: " + lm.isProviderEnabled(LocationManager.GPS_PROVIDER));

        // lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        // location = locationManager .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // if (location != null) {
        // //支持
        // }


        //监听地理位置变化，地理位置变化时，能够重置location
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location loc) {
                if (loc != null) {
                    //TODO
//         location = loc;
//         showLocation(location);
                }
            }
        };

        ELOG.i("是否包含网络: " + lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        // lm.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        // location = lm .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        // if (location != null) {
        // //支持
        // }
        // // 谷歌网站可以请求对应地域
        // url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
        // url.append(loc.getLatitude()).append(",");
        // url.append(loc.getLongitude());

        // 特殊的位置提供
        Location loc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (loc == null) {
            ELOG.e("getLastKnownLocation is null!");
            return null;
        }
//        ELOG.i("getLatitude:" + loc.getLatitude());
//        ELOG.i("getLongitude:" + loc.getLongitude());
//        ELOG.i("getSpeed:" + loc.getSpeed());
//        ELOG.i("getTime:" + loc.getTime());

        ELOG.i("===================");
        // 查找到服务信息
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
        String provider = lm.getBestProvider(criteria, true); // 获取GPS信息
        ELOG.i("provider: " + provider);
        Location location = lm.getLastKnownLocation(provider); // 通过GPS获取位置
        if (location == null) {
            ELOG.e("获取异常  location is null! ");
            return null;
        }
//        ELOG.i("===getLatitude===>" + location.getLatitude());
//        ELOG.i("===getLongitude===>" + location.getLongitude());
        return location;
    }
    private String getGeographyLocation(){
        try{
            Location l = getLocationInfo();
            return l.getLatitude()+"-"+l.getLongitude();
        }catch (Throwable t){
            return getCoordinate();
        }
    }
    private JSONObject getLocation() {
        JSONObject locationJson = null;
        try {
            locationJson = new JSONObject();
            locationJson.put(DeviceKeyContacts.LocationInfo.CollectionTime, String.valueOf(System.currentTimeMillis()));

            String locationInfo = getGeographyLocation();
//            int location = SPHelper.getDefault(mContext).getInt(EGContext.SP_LOCATION, 1);
            if (!TextUtils.isEmpty(locationInfo)
//                    && location == 1
                    ) {
                locationJson.put(DeviceKeyContacts.LocationInfo.GeographyLocation, locationInfo);
            }

            JSONArray wifiInfo = WifiImpl.getInstance(mContext).getWifiInfo();
//            int wifi = SPHelper.getDefault(mContext).getInt(EGContext.SP_WIFI, 1);
            if (wifiInfo != null && wifiInfo.length() != 0
//                    && wifi == 1
                    ) {
                locationJson.put(DeviceKeyContacts.LocationInfo.WifiInfo.NAME, wifiInfo);
            }

            JSONArray baseStation = getBaseStationInfo();
//            int base = SPHelper.getDefault(mContext).getInt(EGContext.SP_BASE_STATION, 1);
            if (baseStation != null && baseStation.length() != 0
//                    && base == 1
                    ) {
                locationJson.put(DeviceKeyContacts.LocationInfo.BaseStationInfo.NAME, baseStation);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return locationJson;
    }

    /**
     * 经纬度坐标
     */
    private String getCoordinate() {
        return "2.00000" + "-" + "6.233232323";
    }



       /**
     * 基站信息
     */
    public JSONArray getBaseStationInfo() {
        JSONArray jar = new JSONArray();
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                 List<NeighboringCellInfo> list = mTelephonyManager.getNeighboringCellInfo();
                 baseStationSort(list);
                JSONObject locationJson = null;
                 for (int i = 0; i < list.size(); i++) {
                     if (i < 5) {//TODO 只要小于5个的？
                         locationJson = new JSONObject();
                         locationJson.put(DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, list.get(i).getLac());
                         locationJson.put(DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, list.get(i).getCid());
                         locationJson.put(DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, list.get(i).getRssi());
                         jar.put(locationJson);
                     }
                 }
                GsmCellLocation location = (GsmCellLocation)mTelephonyManager.getCellLocation();
                locationJson = new JSONObject();
                locationJson.put(DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, location.getLac());
                locationJson.put(DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, location.getCid());
                locationJson.put(DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, location.getPsc());
                jar.put(locationJson);
            }
        } catch (Exception e) {
        }
        return jar;
    }

    /**
     * 基站列表排序
     */
    public void baseStationSort(List<NeighboringCellInfo> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).getRssi() < list.get(j).getRssi()) {
                    NeighboringCellInfo cellInfo = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, cellInfo);
                }
            }
        }
    }

    /**
     * 基站测试
     *
     * @return
     */

//    public JSONArray getBaseStation() {
//        try {
//            if (Build.VERSION.SDK_INT > 22) {
//                if (!PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
//                    && !PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    LL.e("has no permission");
//                    return new JSONArray();
//                }
//
//            }
//            TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
//
//            LL.i("===================基站信息===============================");
//
//            // 1. 基站信息
//            CellLocation cellLocation = tm.getCellLocation();
//            if (cellLocation instanceof GsmCellLocation) {
//                GsmCellLocation gsmCellLocation = (GsmCellLocation)cellLocation;
//                if (gsmCellLocation != null) {
//                    LL.i("GsmCellLocation.getLac:" + gsmCellLocation.getLac());
//                    LL.i("GsmCellLocation.getCid:" + gsmCellLocation.getCid());
//                    LL.i("GsmCellLocation.getPsc:" + gsmCellLocation.getPsc());
//                }
//            } else if (cellLocation instanceof CdmaCellLocation) {
//                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation)cellLocation;
//                if (cdmaCellLocation != null) {
//                    LL.i("CdmaCellLocation.getSystemId:" + cdmaCellLocation.getSystemId());
//                    LL.i("CdmaCellLocation.getNetworkId:" + cdmaCellLocation.getNetworkId());
//                    LL.i("CdmaCellLocation.getBaseStationId:" + cdmaCellLocation.getBaseStationId());
//                    LL.i("CdmaCellLocation.getBaseStationLatitude:" + cdmaCellLocation.getBaseStationLatitude());
//                    LL.i("CdmaCellLocation.getBaseStationLongitude:" + cdmaCellLocation.getBaseStationLongitude());
//                }
//            }
//
//            LL.i("===================附近小区信息================================");
//            // 2. 附近小区信息
//            List<CellInfo> allCellInfo = tm.getAllCellInfo();
//            if (allCellInfo != null) {
//                LL.i("=====>" + allCellInfo.size() + "<=====");
//                for (CellInfo info : allCellInfo) {
//                    LL.i("CellInfo.isRegistered: " + info.isRegistered());
//                    LL.i("CellInfo.ts: " + info.getTimeStamp());
//                    if (info instanceof CellInfoGsm) {
//                        LL.i("-----GSM------");
//
//                        CellInfoGsm cellInfoGsm = (CellInfoGsm)info;
//                        CellIdentityGsm cellIdentity = cellInfoGsm.getCellIdentity();
//
////                        LL.i("CellInfoGsm.getCellConnectionStatus:" + cellInfoGsm.getCellConnectionStatus());
//                        LL.i("CellInfoGsm.getTimeStamp:" + cellInfoGsm.getTimeStamp());
//                        LL.i("CellIdentityGsm.getLac:" + cellIdentity.getLac());
//                        LL.i("CellIdentityGsm.getCid:" + cellIdentity.getCid());
//                        LL.i("CellIdentityGsm.getMcc:" + cellIdentity.getMcc());
//                        LL.i("CellIdentityGsm.getMnc:" + cellIdentity.getMnc());
//                        LL.i("CellIdentityGsm.getArfcn:" + cellIdentity.getArfcn());
////                        LL.i("CellIdentityGsm.getMobileNetworkOperator:" + cellIdentity.getMobileNetworkOperator());
//                        LL.i("CellIdentityGsm.getPsc:" + cellIdentity.getPsc());
//                        LL.i("CellIdentityGsm.getBsic:" + cellIdentity.getBsic());
////                        LL.i("CellIdentityGsm.getOperatorAlphaLong:" + cellIdentity.getOperatorAlphaLong());
////                        LL.i("CellIdentityGsm.getOperatorAlphaShort:" + cellIdentity.getOperatorAlphaShort());
//
//                    } else if (info instanceof CellInfoCdma) {
//                        LL.i("-----CDMA------");
//                        CellInfoCdma cellInfoCdma = (CellInfoCdma)info;
//                        CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
////                        LL.i("CellInfoCdma.getCellConnectionStatus:" + cellInfoCdma.getCellConnectionStatus());
//                        LL.i("CellInfoCdma.getTimeStamp:" + cellInfoCdma.getTimeStamp());
//
//                        LL.i("CellIdentityCdma.getLatitude:" + cellIdentity.getLatitude());
//                        LL.i("CellIdentityCdma.getLongitude:" + cellIdentity.getLongitude());
//                        LL.i("CellIdentityCdma.getSystemId:" + cellIdentity.getSystemId());
//                        LL.i("CellIdentityCdma.getNetworkId:" + cellIdentity.getNetworkId());
//                        LL.i("CellIdentityCdma.getBasestationId:" + cellIdentity.getBasestationId());
//
//                        CellSignalStrength cellSignalStrength = cellInfoCdma.getCellSignalStrength();
//                        LL.i("CellSignalStrength.getDbm:" + cellSignalStrength.getDbm());
//                        LL.i("CellSignalStrength.getAsuLevel:" + cellSignalStrength.getAsuLevel());
//                        LL.i("CellSignalStrength.getLevel:" + cellSignalStrength.getLevel());
//
//                    } else if (info instanceof CellInfoLte) {
//                        LL.i("-----LTE------");
//                        CellInfoLte cellInfoLte = (CellInfoLte)info;
//                        CellIdentityLte cellIdentity = cellInfoLte.getCellIdentity();
//
//                        LL.i("CellInfoLte.getCellConnectionStatus:"
//                            + Reflecer.hook(cellInfoLte, "getCellConnectionStatus"));
//                        // LL.i("CellInfoLte.getCellConnectionStatus:" + cellInfoLte.getCellConnectionStatus());
//                        LL.i("CellInfoLte.getTimeStamp:" + cellInfoLte.getTimeStamp());
//
//                        LL.i("CellIdentityLte.getTac:" + cellIdentity.getTac());
//                        LL.i("CellIdentityLte.getCi:" + cellIdentity.getCi());
//                        LL.i("CellIdentityLte.getEarfcn:" + cellIdentity.getEarfcn());
//                        // LL.i("CellIdentityLte.getBandwidth:" + cellIdentity.getBandwidth());
//                        LL.i("CellIdentityLte.getBandwidth:" + Reflecer.hook(cellIdentity, "getBandwidth"));
//                        LL.i("CellIdentityLte.getPci:" + cellIdentity.getPci());
//                        LL.i("CellIdentityLte.getMnc:" + cellIdentity.getMnc());
//                        LL.i("CellIdentityLte.getMcc:" + cellIdentity.getMcc());
//
//                        CellSignalStrengthLte csl = cellInfoLte.getCellSignalStrength();
//
//                        LL.i("CellSignalStrengthLte.getAsuLevel:" + csl.getAsuLevel());
//                        // LL.i("CellSignalStrengthLte.getCqi:" + csl.getCqi());
//                        LL.i("CellSignalStrengthLte.getRsrq:" + Reflecer.hook(csl, "getCqi"));
//                        LL.i("CellSignalStrengthLte.getDbm:" + csl.getDbm());
//                        LL.i("CellSignalStrengthLte.getLevel:" + csl.getLevel());
//                        // LL.i("CellSignalStrengthLte.getRsrp:" + csl.getRsrp());
//                        LL.i("CellSignalStrengthLte.getRsrp:" + Reflecer.hook(csl, "getRsrp"));
//                        // LL.i("CellSignalStrengthLte.getRsrq:" + csl.getRsrq());
//                        LL.i("CellSignalStrengthLte.getRsrq:" + Reflecer.hook(csl, "getRsrq"));
//                        // LL.i("CellSignalStrengthLte.getRssnr:" + csl.getRssnr());
//                        LL.i("CellSignalStrengthLte.getRssnr:" + Reflecer.hook(csl, "getRssnr"));
//                        // LL.i("CellSignalStrengthLte.getTimingAdvance:" + csl.getTimingAdvance());
//                        LL.i("CellSignalStrengthLte.getTimingAdvance:" + Reflecer.hook(csl, "getTimingAdvance"));
//
//                    } else if (info instanceof CellInfoWcdma) {
//                        LL.i("-----WCDMA------");
//                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma)info;
//                        CellIdentityWcdma cellIdentity = cellInfoWcdma.getCellIdentity();
//
////                        LL.i("CellInfoWcdma.getCellConnectionStatus:" + cellInfoWcdma.getCellConnectionStatus());
//                        LL.i("CellInfoWcdma.getTimeStamp:" + cellInfoWcdma.getTimeStamp());
//
//                        LL.i("CellIdentityWcdma.getCid:" + cellIdentity.getCid());
//                        LL.i("CellIdentityWcdma.getMnc:" + cellIdentity.getMnc());
//                        LL.i("CellIdentityWcdma.getMcc:" + cellIdentity.getMcc());
//                        LL.i("CellIdentityWcdma.getLac:" + cellIdentity.getLac());
//                        LL.i("CellIdentityWcdma.getPsc:" + cellIdentity.getPsc());
//                        LL.i("CellIdentityWcdma.getUarfcn:" + cellIdentity.getUarfcn());
////                        LL.i("CellIdentityWcdma.getOperatorAlphaLong:" + cellIdentity.getOperatorAlphaLong());
////                        LL.i("CellIdentityWcdma.getOperatorAlphaShort:" + cellIdentity.getOperatorAlphaShort());
//
//                        CellSignalStrengthWcdma csw = cellInfoWcdma.getCellSignalStrength();
//                        LL.i("CellSignalStrengthWcdma.getAsuLevel:" + csw.getAsuLevel());
//                        LL.i("CellSignalStrengthWcdma.getDbm:" + csw.getDbm());
//                        LL.i("CellSignalStrengthWcdma.getLevel:" + csw.getLevel());
//                    } else {
//                        LL.i("----其他------");
////                        LL.i("CellInfo.getCellConnectionStatus:" + info.getCellConnectionStatus());
//                        LL.i("CellInfo.getTimeStamp:" + info.getTimeStamp());
//                    }
//                }
//            }
//
//            LL.i("===================其他附近信息===============================");
//            // 3. 附近小区获取
//            List<NeighboringCellInfo> neighboringCellInfo = tm.getNeighboringCellInfo();
//            if (neighboringCellInfo != null) {
//                for (NeighboringCellInfo nci : neighboringCellInfo) {
//                    LL.i("NeighboringCellInfo.getLac:" + nci.getLac());
//                    LL.i("NeighboringCellInfo.getCid:" + nci.getCid());
//                    LL.i("NeighboringCellInfo.getPsc:" + nci.getPsc());
//                    LL.i("NeighboringCellInfo.getNetworkType:" + nci.getNetworkType());
//                    LL.i("NeighboringCellInfo.getRssi:" + nci.getRssi());
//                }
//            }
//        } catch (Throwable e) {
//            LL.e(e);
//        }
//        return new JSONArray();
//    }

    /**
     * 获取GPS信息
     */
//    public Location getGPSInfo() {
//
//        if (Build.VERSION.SDK_INT > 22) {
//            if (!PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
//                && !PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                ELOG.e("has no permission");
//                return null;
//            }
//        }
//        LocationManager lm =
//            (LocationManager)mContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//
//        if (lm == null) {
//            return null;
//        }
//        ELOG.i("是否包含GPS: " + lm.isProviderEnabled(LocationManager.GPS_PROVIDER));
//
//        // lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
//        // location = locationManager .getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        // if (location != null) {
//        // //支持
//        // }
//
//
//         //监听地理位置变化，地理位置变化时，能够重置location
//         LocationListener locationListener = new LocationListener() {
//         @Override
//         public void onStatusChanged(String provider, int status, Bundle extras) {
//         }
//         @Override
//         public void onProviderEnabled(String provider) {
//         }
//
//         @Override
//         public void onProviderDisabled(String provider) {
//
//         }
//
//         @Override
//         public void onLocationChanged(Location loc) {
//         if (loc != null) {
////         location = loc;
////         showLocation(location);
//         }
//         }
//         };
//
//        ELOG.i("是否包含网络: " + lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
//        // lm.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
//        // location = lm .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        // if (location != null) {
//        // //支持
//        // }
//        // // 谷歌网站可以请求对应地域
//        // url.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
//        // url.append(loc.getLatitude()).append(",");
//        // url.append(loc.getLongitude());
//
//        // 特殊的位置提供
//        Location loc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//        if (loc == null) {
//            ELOG.e("getLastKnownLocation is null!");
//            return null;
//        }
////        ELOG.i("getLatitude:" + loc.getLatitude());
////        ELOG.i("getLongitude:" + loc.getLongitude());
////        ELOG.i("getSpeed:" + loc.getSpeed());
////        ELOG.i("getTime:" + loc.getTime());
//
//        ELOG.i("===================");
//        // 查找到服务信息
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(true);
//        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
//        String provider = lm.getBestProvider(criteria, true); // 获取GPS信息
//        ELOG.i("provider: " + provider);
//        Location location = lm.getLastKnownLocation(provider); // 通过GPS获取位置
//        if (location == null) {
//            ELOG.e("获取异常  location is null! ");
//            return null;
//        }
////        ELOG.i("===getLatitude===>" + location.getLatitude());
////        ELOG.i("===getLongitude===>" + location.getLongitude());
//        return location;
//    }

}
