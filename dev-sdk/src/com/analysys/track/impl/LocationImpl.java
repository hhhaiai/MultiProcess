package com.analysys.track.impl;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.impl.proc.AnalysysPhoneStateListener;
import com.analysys.track.internal.Content.DataController;
import com.analysys.track.work.MessageDispatcher;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.SystemUtils;

import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.database.TableLocation;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;

import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.sp.SPHelper;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

public class LocationImpl {

    Context mContext;
    private LocationManager locationManager;
//    PhoneStateListener phoneStateListener = null;
    TelephonyManager mTelephonyManager = null;
//    CellLocation cellLocation = null;
    JSONObject locationJson = null;

    private static class Holder {
        private static final LocationImpl INSTANCE = new LocationImpl();
    }

    public static LocationImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        if (LocationImpl.Holder.INSTANCE.locationManager == null) {
            if (LocationImpl.Holder.INSTANCE.mContext != null) {
                LocationImpl.Holder.INSTANCE.locationManager =
                        (LocationManager) LocationImpl.Holder.INSTANCE.mContext.getApplicationContext()
                                .getSystemService(Context.LOCATION_SERVICE);
            }
        }
        return LocationImpl.Holder.INSTANCE;
    }

    public void location() {
        try {
            if(SystemUtils.isMainThread()){
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        LocationHandle();
                    }
                });
            }else {
                LocationHandle();
            }
        }catch (Throwable t){
        }


    }
    private void LocationHandle(){
        try {
            if(!PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LOCATION,true)){
//                ELOG.i("第一个可能性退出的地方");
                return;
            }
            //么有获取地理位置权限则不做处理
            if(!hasLocationPermission()){
//                ELOG.i("第二个可能性退出的地方location么有新值");
//                return;

            }
            if(mTelephonyManager == null){
                mTelephonyManager = AnalysysPhoneStateListener.getInstance(mContext).getTelephonyManager();
            }
            JSONObject location = getLocation();
//            String log =Log.getStackTraceString(new Exception("采集完毕"));
//            Log.i("xxx.log", SystemUtils.getCurrentProcessName(mContext)+ Thread.currentThread().getName()+log);
            if (location != null) {
                TableLocation.getInstance(mContext).insert(location);
            }
        }catch (Throwable t){
            ELOG.e(t.getMessage()+"  :::LocationHandle");
        }finally {
            MessageDispatcher.getInstance(mContext).locationInfo(EGContext.LOCATION_CYCLE,false);
        }
    }
    private boolean hasLocationPermission() {
        /**
         * Manifest是否声明权限
         */
        if (!AndroidManifestHelper.isPermissionDefineInManifest(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                && !AndroidManifestHelper.isPermissionDefineInManifest(mContext,Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ELOG.i("hasLocationPermission == false ");
            return false;
        }
        //是否可以去获取权限
        if (!PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                && !PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ELOG.e("has no permission");
            return false;
        }
        List<String> pStrings = this.locationManager.getProviders(true);
        String provider;
        if (pStrings.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (pStrings.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
//            ELOG.i("第三个可能性退出的地方");
            return false;
        }
        try {
            Location location = this.locationManager.getLastKnownLocation(provider);
            if(location == null){
                location = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (needSaveLocation(location)) {//距离超过1000米则存储，其他wifi等信息亦有效，存储
//                ELOG.i("  new location ..."+location);
                resetLocaiton(location);
            }else{//距离不超过1000米则无需存储，其他数据也无需获取存储
//                ELOG.i("第四个可能性退出的地方");
                return false;
            }
        }catch (Throwable t){
            ELOG.i(t.getMessage() + "hasLocationPermission has an exception ");
        }
        return true;
    }


    /**
     * 缓存地理位置信息数据
     *
     * @param location
     */
    public void resetLocaiton(Location location) {
        if (location != null) {
            String gl = location.getLongitude() + "-" + location.getLatitude();
            if (TextUtils.isEmpty(gl)) {
                return;
            }
//            ELOG.i(gl+"   ：：：：：：：：：：有效经纬度");
            SPHelper.setStringValue2SP(mContext,EGContext.LAST_LOCATION,gl);
        }
    }

    /**
     * 计算两个坐标之间的距离
     *
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @return
     */
    private double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double EARTH_RADIUS = 6378137.0;
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(
                Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 判断距离是否可以存储信息
     *
     * @param location
     * @return
     */
    private boolean needSaveLocation(Location location) {

        try {
            if (location == null) {
//                ELOG.i("第五个可能性退出的地方");
                return false;
            }
            String lastLocation = SPHelper.getStringValueFromSP(mContext,EGContext.LAST_LOCATION,"");
            if (TextUtils.isEmpty(lastLocation)) {
                return true;
            }

            String[] ary = lastLocation.split("-");
            if (ary.length != 2) {
                return true;
            }
            double longitude1 = Double.parseDouble(ary[1]);
            double latitude1 = Double.parseDouble(ary[0]);
            double distance = getDistance(longitude1, latitude1, location.getLongitude(), location.getLatitude());
            //距离没有变化则不保存
            if (EGContext.MINDISTANCE <= distance) {
                return true;
            }
        } catch (Throwable e) {
            ELOG.e(" needSaveLocation::::; "+ e.getMessage());
        }
//        ELOG.i("第六个可能性退出的地方");
        return false;
    }

    private JSONObject getLocation() {
        try {
            locationJson = new JSONObject();
            try {
                JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.CollectionTime, String.valueOf(System.currentTimeMillis()),DataController.SWITCH_OF_COLLECTION_TIME);
            }catch (Throwable t){
//                ELOG.i("1111111111111111111111111111");
            }
            try {
                String locationInfo = SPHelper.getStringValueFromSP(mContext,EGContext.LAST_LOCATION,"");
                JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.GeographyLocation, locationInfo,DataController.SWITCH_OF_GEOGRAPHY_LOCATION);
            }catch (Throwable t){
//                ELOG.i("22222222222222");
            }

            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_WIFI,true)){
                try {
                    JSONArray wifiInfo = WifiImpl.getInstance(mContext).getWifiInfo();
                    JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.WifiInfo.NAME, wifiInfo,DataController.SWITCH_OF_WIFI_NAME);
                }catch (Throwable t){
//                    ELOG.i("333333333333333");
                }
            }

            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BASE,true)){
                try {
                    JSONArray baseStation = getBaseStationInfo();
                    JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.NAME,baseStation,DataController.SWITCH_OF_BS_NAME);
                }catch (Throwable t){
//                    ELOG.i("4444444444444444444");
                }

            }

        } catch (Throwable e) {
            ELOG.e(e.getMessage()+"  getLocation has an exc.");
        }
        return locationJson;
    }



    /**
     * 基站信息
     * @return
     */
    public JSONArray getBaseStationInfo() {
        JSONArray jsonArray = null;
        JSONObject jsonObject = null;
        try {
            if(mTelephonyManager == null){
                ELOG.i(" mTelephonyManager == null");
                return jsonArray;
            }
            jsonObject = new JSONObject();
            if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ELOG.i(" PermissionUtils.checkPermission is true");
                jsonArray = new JSONArray();
                try {
                    List<NeighboringCellInfo> list = mTelephonyManager.getNeighboringCellInfo();
                    ELOG.i(" List<NeighboringCellInfo>  "+list != null ?list.size():null);
                    if(list != null && list.size()>0) {
                        baseStationSort(list);
                        for (int i = 0; i < list.size(); i++) {
                            if (i < 5) {
                                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, list.get(i).getLac(), DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, list.get(i).getCid(), DataController.SWITCH_OF_CELL_ID);
                                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, list.get(i).getRssi(), DataController.SWITCH_OF_BS_LEVEL);
                                jsonArray.put(jsonObject);
                                ELOG.i("获取当前基站信息1。。。:::::: "+jsonArray);
                            }
                        }
                    }
                }catch (Throwable t){
                    ELOG.e(t.getMessage()+"  getBaseStationInfo has an exc. " );
                }
                try {
                    CellLocation location = mTelephonyManager.getCellLocation();
//                    ELOG.i(" CellLocation  "+location);
                    GsmCellLocation gcl = null;
                    CdmaCellLocation ccl = null;
                    if(location != null){
                        if(location instanceof GsmCellLocation) {
//                            ELOG.i(" location instanceof GsmCellLocation ");
                            gcl = (GsmCellLocation)location;
                            if(gcl != null){
                                ELOG.i("获取当前基站信息:::::: "+gcl.getLac()+ "  vs "+gcl.getCid()+"  vs "+gcl.getPsc());
                                //获取当前基站信息
                                JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, gcl.getLac(),DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, gcl.getCid(),DataController.SWITCH_OF_CELL_ID);
                                JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, gcl.getPsc(),DataController.SWITCH_OF_BS_LEVEL);
                                jsonArray.put(jsonObject);
                                ELOG.i("获取当前基站信息。。:::::: "+jsonArray);
                            }
                        } else{
                            if(mTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA){
                                ccl = (CdmaCellLocation) mTelephonyManager.getCellLocation();
                                if(ccl != null){
                                ELOG.i("获取当前基站信息:::::: "+ccl.getNetworkId()+ "  vs "+ccl.getBaseStationId()+"  vs "+ccl.getBaseStationId());
                                    //获取当前基站信息
                                    JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, ccl.getNetworkId(),DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                    JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, ccl.getBaseStationId(),DataController.SWITCH_OF_CELL_ID);
                                    JsonUtils.pushToJSON(mContext,jsonObject,DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, ccl.getSystemId() ,DataController.SWITCH_OF_BS_LEVEL);
                                    jsonArray.put(jsonObject);
                                ELOG.i("获取当前基站信息。。。:::::: "+jsonArray);
                                }
                            }
                            return jsonArray;
                        }
                    }else {
                        return jsonArray;
                    }
                }catch (Throwable t){
                    ELOG.e(t.getMessage()+"  外层CellLocation catch");
                }
            }
        } catch (Exception e) {
            ELOG.e(e.getMessage()+"   最外层catch");
        }
        return jsonArray;
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
}
