package com.analysys.track.impl;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import com.analysys.track.database.TableLocation;
import com.analysys.track.impl.proc.AnalysysPhoneStateListener;
import com.analysys.track.impl.proc.DoubleCardSupport;
import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.FileUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;
import com.analysys.track.work.MessageDispatcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocationImpl {

    Context mContext;
    private LocationManager locationManager;
    TelephonyManager mTelephonyManager = null;
    JSONObject locationJson = null;
    private static boolean isLocationBlockRunning = false;
    private LocationImpl() {
    }

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
            long currentTime = System.currentTimeMillis();
            MessageDispatcher.getInstance(mContext).locationInfo(EGContext.LOCATION_CYCLE);
            if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_LOCATION,EGContext.LOCATION_CYCLE,currentTime)){
                FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_LOCATION,currentTime);
            }else {
                return;
            }
            if(!isLocationBlockRunning){
                isLocationBlockRunning = true;
            }else {
                return;
            }
            if (SystemUtils.isMainThread()) {
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        LocationHandle();
                    }
                });
            } else {
                LocationHandle();
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }
        }finally {
            isLocationBlockRunning = false;
        }
    }

    private void LocationHandle() {
        try {
            if (!PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LOCATION, true)) {
                return;
            }
            //么有获取地理位置权限则不做处理
            if (!hasLocationPermission()) {
//                return;

            }
            if (mTelephonyManager == null) {
                mTelephonyManager = AnalysysPhoneStateListener.getInstance(mContext).getTelephonyManager();
            }
            JSONObject location = getLocation();
            if (location != null) {
                TableLocation.getInstance(mContext).insert(location);
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }
        }
    }

    private boolean hasLocationPermission() {
        /**
         * Manifest是否声明权限
         */
        if (!AndroidManifestHelper.isPermissionDefineInManifest(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                && !AndroidManifestHelper.isPermissionDefineInManifest(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return false;
        }
        //是否可以去获取权限
        if(canCheckPermission(mContext)){
            if (!PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    && !PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                return false;
            }
        }else {
            return false;
        }
        List<String> pStrings = this.locationManager.getProviders(true);
        String provider;
        if (pStrings.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (pStrings.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            return false;
        }
        try {
            Location location = this.locationManager.getLastKnownLocation(provider);
            if (location == null) {
                location = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (needSaveLocation(location)) {//距离超过1000米则存储，其他wifi等信息亦有效，存储
                resetLocaiton(location);
            } else {//距离不超过1000米则无需存储，其他数据也无需获取存储
                return false;
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }
        }
        return true;
    }
    private static int permissionAskCount = 0;
    private boolean canCheckPermission(Context context){
        try {
            if(context == null){
                return false;
            }
            String day = SystemUtils.getDay();
            String spDay = SPHelper.getStringValueFromSP(context,EGContext.PERMISSION_TIME,"-1");
            if(permissionAskCount == 0){
                permissionAskCount = SPHelper.getIntValueFromSP(context,EGContext.PERMISSION_COUNT,0);
            }
            if(spDay.equals(day) && permissionAskCount > 5){
                return false;
            }
            //如果是当天，则累加，并将当前count存sp；否则，则置零，重新累加。即，一天只能有5次申请授权
            if(spDay.equals(day)){
                permissionAskCount += 1;
                SPHelper.setIntValue2SP(context,EGContext.PERMISSION_COUNT,permissionAskCount);
            }else{
                permissionAskCount += 1;
                SPHelper.setStringValue2SP(context,EGContext.PERMISSION_TIME,day);
                SPHelper.setIntValue2SP(context,EGContext.PERMISSION_COUNT,permissionAskCount);
            }
            return true;
        }catch (Throwable t){
        }
        return false;
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
            SPHelper.setStringValue2SP(mContext, EGContext.LAST_LOCATION, gl);
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
                return false;
            }
            String lastLocation = SPHelper.getStringValueFromSP(mContext, EGContext.LAST_LOCATION, "");
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
        }
        return false;
    }

    private JSONObject getLocation() {
        try {
            locationJson = new JSONObject();
            try {
                JsonUtils.pushToJSON(mContext, locationJson, DeviceKeyContacts.LocationInfo.CollectionTime, String.valueOf(System.currentTimeMillis()), DataController.SWITCH_OF_COLLECTION_TIME);
            } catch (Throwable t) {
            }
            try {
                String locationInfo = SPHelper.getStringValueFromSP(mContext, EGContext.LAST_LOCATION, "");
                JsonUtils.pushToJSON(mContext, locationJson, DeviceKeyContacts.LocationInfo.GeographyLocation, locationInfo, DataController.SWITCH_OF_GEOGRAPHY_LOCATION);
            } catch (Throwable t) {
            }

            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_WIFI, true)) {
                try {
                    JSONArray wifiInfo = WifiImpl.getInstance(mContext).getWifiInfo();
                    JsonUtils.pushToJSON(mContext, locationJson, DeviceKeyContacts.LocationInfo.WifiInfo.NAME, wifiInfo, DataController.SWITCH_OF_WIFI_NAME);
                } catch (Throwable t) {
                }
            }

            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BASE, true)) {
                try {
                    JSONArray baseStation = getBaseStationInfo();
                    JsonUtils.pushToJSON(mContext, locationJson, DeviceKeyContacts.LocationInfo.BaseStationInfo.NAME, baseStation, DataController.SWITCH_OF_BS_NAME);
                } catch (Throwable t) {
                }

            }

        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e);
            }
        }
        return locationJson;
    }


    /**
     * 基站信息
     * 1.判断权限
     * 2.周围基站最多前五
     * 3.GSM or CDMA 基站信息
     *
     * @return
     */
    public JSONArray getBaseStationInfo() {
        JSONArray jsonArray = null;
        JSONObject jsonObject = null,tempJsonObj = null;
        Set<String> cid = new HashSet<String>();
        try {
            if (mTelephonyManager == null) {
                return jsonArray;
            }
            if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                jsonArray = new JSONArray();
                try {
                    if(DoubleCardSupport.hasMethod(mTelephonyManager.getClass().getName(),"getNeighboringCellInfo",List.class)){
                        List<NeighboringCellInfo> list = mTelephonyManager.getNeighboringCellInfo();
                        if (list != null && list.size() > 0) {
                            baseStationSort(list);
                            int tempCid = -1, tempLac = -1;
                            String key = null;
                            for (int i = 0; i < list.size(); i++) {
                                if (cid.size() < 5) {
                                    NeighboringCellInfo info = list.get(i);
                                    tempCid = info.getCid();
                                    tempLac = info.getLac();
                                    key = tempCid + "|" + tempLac;
                                    if (tempCid > 0 && tempLac > 0 && !cid.contains(key)) {
                                        cid.add(key);
                                        jsonObject = new JSONObject();
                                        JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, tempLac, DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                        JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, tempCid, DataController.SWITCH_OF_CELL_ID);
                                        JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, info.getRssi(), DataController.SWITCH_OF_BS_LEVEL);
                                        jsonArray.put(jsonObject);
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    if(EGContext.FLAG_DEBUG_INNER){
                        ELOG.e(t);
                    }
                }
                Map<String, JSONObject> tempGsmMap = new HashMap<String, JSONObject>(), tempCdmaMap = new HashMap<String, JSONObject>();
                List<JSONObject> gsmList = new ArrayList<JSONObject>(), cdmaList = new ArrayList<JSONObject>();
                try {
                    CellLocation location = mTelephonyManager.getCellLocation();
                    GsmCellLocation gcl = null;
                    CdmaCellLocation ccl = null;
                    if (location != null) {
                        if (location instanceof GsmCellLocation) {
                            gcl = (GsmCellLocation) location;
                            jsonObject = new JSONObject();
                            if (gcl != null) {
                                //获取当前基站信息
                                int tempCid = gcl.getCid(), tempLac = gcl.getLac();
                                String key = tempCid + "|" + tempLac;
                                if (tempCid > 0 && tempLac > 0 && !cid.contains(key)) {
                                    cid.add(key);
                                    int stren = gcl.getPsc();
                                    JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, tempLac, DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                    JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, tempCid, DataController.SWITCH_OF_CELL_ID);
                                    JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, stren, DataController.SWITCH_OF_BS_LEVEL);

                                    tempGsmMap.put(stren+"|"+key, jsonObject);
                                    tempJsonObj = new JSONObject();
                                    tempJsonObj.put("stren",stren);
                                    tempJsonObj.put("mapKey",stren+"|"+key);
                                    gsmList.add(tempJsonObj);
                                }
                            }
                        } else if (location instanceof CdmaCellLocation) {
                            jsonObject = new JSONObject();
                            ccl = (CdmaCellLocation) mTelephonyManager.getCellLocation();
                            if (ccl != null) {
                                int tempCid = ccl.getBaseStationId(), tempLac = ccl.getNetworkId();
                                String key = tempCid + "|" + tempLac;

                                if (tempCid > 0 && tempLac > 0 && !cid.contains(key)) {
                                    cid.add(key);
                                    //获取当前基站信息
                                    int stren = ccl.getSystemId();
                                    JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, tempLac, DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                    JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, tempCid, DataController.SWITCH_OF_CELL_ID);
                                    JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, stren, DataController.SWITCH_OF_BS_LEVEL);
                                    tempCdmaMap.put(stren+"|"+key, jsonObject);
                                    tempJsonObj = new JSONObject();
                                    tempJsonObj.put("stren",stren);
                                    tempJsonObj.put("mapKey",stren+"|"+key);
                                    cdmaList.add(tempJsonObj);
                                }
                            }
                        }
                    }

                } catch (Throwable t) {
                    if(EGContext.FLAG_DEBUG_INNER){
                        ELOG.e(t);
                    }
                }
                //方案B
                try {
                    JSONObject obj = null;
                    //https://blog.csdn.net/itfootball/article/details/25421015
                    List<CellInfo> infos = mTelephonyManager.getAllCellInfo();
                    if (infos != null && infos.size() > 0) {
                        int tempCid = -1;//cid
                        int tempLac = -1;//lac
                        int strength = -1;//信号强度
                        String key = null;
                        for (CellInfo info : infos) {
                            tempCid = -1;
                            tempLac = -1;
                            strength = -1;
                            if (info instanceof CellInfoCdma) {
                                CellInfoCdma cdma = (CellInfoCdma) info;
                                tempCid = cdma.getCellIdentity().getBasestationId();
                                tempLac = cdma.getCellIdentity().getNetworkId();
                                key = tempCid + "|" + tempLac;

                                if (tempCid > 0 && tempLac > 0 && (!cid.contains(key))) {
                                    cid.add(key);
                                    obj = new JSONObject();
                                    strength = cdma.getCellSignalStrength().getDbm();
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, tempLac, DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, tempCid, DataController.SWITCH_OF_CELL_ID);
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, strength, DataController.SWITCH_OF_BS_LEVEL);
                                    tempCdmaMap.put(strength+"|"+key, obj);
                                    tempJsonObj = new JSONObject();
                                    tempJsonObj.put("stren",strength);
                                    tempJsonObj.put("mapKey",strength+"|"+key);
                                    cdmaList.add(tempJsonObj);
                                }
                            } else if (info instanceof CellInfoGsm) {
                                CellInfoGsm gsm = (CellInfoGsm) info;
                                tempCid = gsm.getCellIdentity().getCid();
                                tempLac = gsm.getCellIdentity().getLac();
                                key = tempCid + "|" + tempLac;
                                if (tempCid > 0 && tempLac > 0 && (!cid.contains(key))) {
                                    cid.add(key);
                                    obj = new JSONObject();
                                    strength = gsm.getCellSignalStrength().getDbm();
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, tempLac, DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, tempCid, DataController.SWITCH_OF_CELL_ID);
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, strength, DataController.SWITCH_OF_BS_LEVEL);
                                    tempGsmMap.put(strength+"|"+key, obj);
                                    tempJsonObj = new JSONObject();
                                    tempJsonObj.put("stren",strength);
                                    tempJsonObj.put("mapKey",strength+"|"+key);
                                    gsmList.add(tempJsonObj);
                                }
                            } else if (info instanceof CellInfoLte) {
                                CellInfoLte lte = (CellInfoLte) info;
                                tempCid = lte.getCellIdentity().getPci();
                                tempLac = lte.getCellIdentity().getTac();
                                key = tempCid + "|" + tempLac;
                                if (tempCid > 0 && tempLac > 0 && (!cid.contains(key))) {
                                    cid.add(key);
                                    obj = new JSONObject();
                                    strength = lte.getCellSignalStrength().getDbm();
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, tempLac, DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, tempCid, DataController.SWITCH_OF_CELL_ID);
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, strength, DataController.SWITCH_OF_BS_LEVEL);
                                    tempCdmaMap.put(strength+"|"+key, obj);
                                    tempJsonObj = new JSONObject();
                                    tempJsonObj.put("stren",strength);
                                    tempJsonObj.put("mapKey",strength+"|"+key);
                                    cdmaList.add(tempJsonObj);
                                }
                            } else if (info instanceof CellInfoWcdma) {
                                CellInfoWcdma wcdma = (CellInfoWcdma) info;
                                tempCid = wcdma.getCellIdentity().getCid();
                                tempLac = wcdma.getCellIdentity().getLac();
                                key = tempCid + "|" + tempLac;
                                if (tempCid > 0 && tempLac > 0 && (!cid.contains(key))) {
                                    cid.add(key);
                                    obj = new JSONObject();
                                    strength = wcdma.getCellSignalStrength().getDbm();
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, tempLac, DataController.SWITCH_OF_LOCATION_AREA_CODE);
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, tempCid, DataController.SWITCH_OF_CELL_ID);
                                    JsonUtils.pushToJSON(mContext, obj, DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, strength, DataController.SWITCH_OF_BS_LEVEL);
                                    tempCdmaMap.put(strength+"|"+key, obj);
                                    tempJsonObj = new JSONObject();
                                    tempJsonObj.put("stren",strength);
                                    tempJsonObj.put("mapKey",strength+"|"+key);
                                    cdmaList.add(tempJsonObj);
                                }
                            }

                        }
                    }
                } catch (Throwable t) {
                    if(EGContext.FLAG_DEBUG_INNER){
                        ELOG.e(t);
                    }
                }
                if (gsmList != null && tempGsmMap != null) {
                    jsonArray = listFilter(gsmList, jsonArray, tempGsmMap);
                }
                if (cdmaList != null && tempCdmaMap != null) {
                    jsonArray = listFilter(cdmaList, jsonArray, tempCdmaMap);
                }
                return jsonArray;
            }
        } catch (Exception e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e);
            }
        }
        return jsonArray;
    }


    private JSONArray listFilter(List<JSONObject> list, JSONArray jsonArray, Map<String, JSONObject> map) {
        try {
            int count = 0;
            if (list != null && list.size() > 0) {
                //降序排列
                baseStationSortByStren(list);
                for (int k = 0; k < list.size(); k++) {
                    if (count < 5) {
                        count = count + 1;
                        JSONObject obj = map.get(list.get(k).optString("mapKey"));
                        jsonArray.put(obj);
                    } else {
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }
        }

        return jsonArray;
    }

    /**
     * 基站列表排序
     */
    public void baseStationSort(List<NeighboringCellInfo> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).getCid() == list.get(j).getCid()) {
                    list.remove(j);
                    continue;
                }
                if (list.get(i).getRssi() < list.get(j).getRssi()) {
                    NeighboringCellInfo cellInfo = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, cellInfo);
                }
            }
        }
    }
    /**
     * 基站列表排序
     */
    public void baseStationSortByStren(List<JSONObject> list) {
        JSONObject obj = null;
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).optInt("stren",-1) < list.get(j).optInt("stren",-1)) {
                    obj = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, obj);
                }
            }
        }
    }
}
