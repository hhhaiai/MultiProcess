package com.analysys.track.internal.impl.locations;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.ActivityCallBack;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.MDate;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 位置信息获取
 * @Version: 1.0
 * @Create: 2019-08-05 16:17:41
 * @author: ly
 */
public class LocationImpl {


    /**
     * 处理位置信息
     *
     * @param callback
     */
    public void tryGetLocationInfo(final ECallBack callback) {
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                // 模快不工作，没有必要轮训
                if (!SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, true)) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_loc, "不允许采集位置停止处理");
                    }
                    return;
                }

                long now = System.currentTimeMillis();
                long durByPolicy = SPHelper.getIntValueFromSP(mContext, EGContext.SP_LOCATION_CYCLE, EGContext.TIME_MINUTE * 30);
                // 3秒内只能处理一次
                if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_LOCATION, EGContext.TIME_SECOND * 3, now)) {
                    long time = SPHelper.getLongValueFromSP(mContext, EGContext.SP_APP_LOCATION, 0);
                    long dur = now - time;

                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_loc, "间隔时间: " + dur + "----------durByPolicy---->" + durByPolicy);
                    }
                    //大于固定时间才可以工作
                    if (dur > durByPolicy || time == 0) {
                        SPHelper.setLongValue2SP(mContext, EGContext.SP_APP_LOCATION, now);
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_loc, "时间满足，即将开始处理。。。");
                        }
                        SystemUtils.runOnWorkThread(new Runnable() {
                            @Override
                            public void run() {
                                getLocationInfoInThread();
                                MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_LOCATION, System.currentTimeMillis());
                                if (callback != null) {
                                    callback.onProcessed();
                                }
                            }
                        });
                    } else {
                        if (BuildConfig.logcat) {
                            ELOG.d(BuildConfig.tag_loc, "时间不到...等待处理时间，继续循环");
                        }
                        //多进程解锁
                        MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_LOCATION, time);
//                    MessageDispatcher.getInstance(mContext).postSnap(dur);
                        if (callback != null) {
                            callback.onProcessed();
                        }
                    }

                } else {

                    if (callback != null) {
                        callback.onProcessed();
                    }

                    if (BuildConfig.logcat) {
                        ELOG.d(BuildConfig.tag_loc, "多进程并发，停止处理");
                    }
                    return;
                }

            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, t);
                }
            }
        }
    }


    public void getLocationInfoInThread() {
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                if (BuildConfig.logcat) {
                    ELOG.d(BuildConfig.tag_loc, "位置信息获取 开始处理。。。。");
                }
                if (!canworkForXiaomi()) {
                    return;
                }
                // 没有获取地理位置权限则不做处理
                if (!isWillWork()) {
                    if (BuildConfig.logcat) {
                        ELOG.d(BuildConfig.tag_loc, "位置信息获取  停止工作。。。。");
                    }
                    return;
                }
                if (mTelephonyManager == null) {
                    mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                }
                JSONObject location = getLocation();
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_loc, "获取 Loction结束，结果 location:" + location.length());
                }
                if (location == null || location.length() < 1) {
                    return;
                }
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_loc, "Loction检测 GL:" + location.has(UploadKey.LocationInfo.GeographyLocation)
                            + "  ; WifiInfo:" + location.has(UploadKey.LocationInfo.WifiInfo.NAME)
                            + "  ;BaseStationInfo:" + location.has(UploadKey.LocationInfo.BaseStationInfo.NAME)

                    );
                }
                if (location.has(UploadKey.LocationInfo.GeographyLocation)
                        || location.has(UploadKey.LocationInfo.WifiInfo.NAME)
                        || location.has(UploadKey.LocationInfo.BaseStationInfo.NAME)) {
                    TableProcess.getInstance(mContext).insertLocation(location);
                } else {
                    if (BuildConfig.logcat) {
                        ELOG.e(BuildConfig.tag_loc, "不管什么原因失败了。。。。。。。。。");
                    }
                }
            } catch (Throwable t) {
                if (BuildConfig.logcat) {
                    ELOG.e(BuildConfig.tag_loc, t);
                }
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, t);
                }
            }
        }

    }

    /**
     * 修复小米手机后台定位(miui11版本开始监控的)
     *
     * @return true:可以工作
     * false:不可以工作
     */
    public boolean canworkForXiaomi() {
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                // 1. ro.miui.ui.version.name==>Vx
                if (!Build.MANUFACTURER.equalsIgnoreCase("xiaomi")
//                        // 概率会失败
//                        || !SystemUtils.containsKeyInProp("ro.miui.ui.version.name")
                ) {
                    if (BuildConfig.logcat) {
                        ELOG.e(BuildConfig.tag_loc, "非小米设备");
                    }
                    return true;
                }
                // 2. get miui version
                String version = SystemUtils.getSystemEnv("ro.miui.ui.version.name");
                if (BuildConfig.logcat) {
                    ELOG.e(BuildConfig.tag_loc, "miui version： " + version);
                }
                if (TextUtils.isEmpty(version)) {
                    return true;
                }
                // 2. 过滤低版本
                if (version.length() > 0) {
                    try {
                        int num = Integer.valueOf(version.substring(1));
                        if (num < 11) {
                            if (BuildConfig.logcat) {
                                ELOG.e(BuildConfig.tag_loc, "miui 11以下不拦截 ");
                            }
                            return true;
                        }
                    } catch (Throwable e) {
                    }
                }
                // 3. 判断后台
                if (ActivityCallBack.getInstance().isAppAliaveInFront()) {
                    return false;
                }

            } catch (Throwable e) {
            }
        }

        return false;
    }


    /**
     * 允许工作： 声明权限、允许申请权限、移动距离长度大于1000米
     *
     * @return
     */
    public boolean isWillWork() {
        if (BuildConfig.ENABLE_LOCATIONINFO) {


            /**
             * 1. Manifest未声明权限，退出
             */
            if (!AndroidManifestHelper.isPermissionDefineInManifest(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    && !AndroidManifestHelper.isPermissionDefineInManifest(mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
//            if (BuildConfig.logcat) {
//                ELOG.d(BuildConfig.tag_loc, "XML没有声明权限。。。。");
//            }
                return false;
            }

            // 2. 没权限再进行判断。是否申请超过五次
            if (!PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    && !PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (!makesureRequestPermissionLessThanFive(mContext)) {
//                if (BuildConfig.logcat) {
//                    ELOG.d(BuildConfig.tag_loc, "没有授权、授权申请次数多余5次。。。。");
//                }
                    return false;
                }
            }

            // 3. 距离不超过1000米
            List<String> pStrings = mLocationManager.getProviders(true);
//        if (BuildConfig.logcat) {
//            ELOG.i(BuildConfig.tag_loc, "获取provider: " + pStrings.toString());
//        }
            // 获取渠道失败。
            if (pStrings == null || pStrings.size() < 1) {
                return true;
            }
            try {
                Location location = null;
                for (String provider : pStrings) {
                    //RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
                    location = mLocationManager.getLastKnownLocation(provider);
//                if (BuildConfig.logcat) {
//                    ELOG.i(BuildConfig.tag_loc, "获取渠道: " + provider + "========>" + location);
//                }
                    if (location != null) {
                        break;
                    }
                }
                if (location == null) {
                    return true;
                }
                if (needSaveLocation(location)) {
                    // 距离超过1000米则存储，其他wifi等信息亦有效，存储
                    resetLocaiton(location);
                } else {
                    // 距离不超过1000米则无需存储，其他数据也无需获取存储
                    if (BuildConfig.logcat) {
                        ELOG.w(BuildConfig.tag_loc, "距离不超过1000米。。。。");
                    }
                    return false;
                }
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, t);
                }
            }
        }
        return true;
    }

    /**
     * 确保每天权限只申请五次
     *
     * @param context
     * @return
     */
    private boolean makesureRequestPermissionLessThanFive(Context context) {
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                if (context == null) {
                    return false;
                }
                String day = MDate.getToday();
                String spDay = SPHelper.getStringValueFromSP(context, EGContext.PERMISSION_TIME, "-1");
                if (permissionAskCount == 0) {
                    permissionAskCount = SPHelper.getIntValueFromSP(context, EGContext.PERMISSION_COUNT, 0);
                }
                // 如果是当天，则累加，并将当前count存sp；否则，则置零，重新累加。即，一天只能有5次申请授权
                if (spDay.equals(day)) {
                    if (permissionAskCount > 5) {
                        return false;
                    } else {
                        permissionAskCount += 1;
                        SPHelper.setIntValue2SP(context, EGContext.PERMISSION_COUNT, permissionAskCount);
                    }

                } else {
                    permissionAskCount += 1;
                    SPHelper.setStringValue2SP(context, EGContext.PERMISSION_TIME, day);
                    SPHelper.setIntValue2SP(context, EGContext.PERMISSION_COUNT, permissionAskCount);
                }
                return true;
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, t);
                }
            }
        }
        return false;
    }

    /**
     * 缓存地理位置信息数据
     *
     * @param location
     */
    public void resetLocaiton(Location location) {
        if (BuildConfig.ENABLE_LOCATIONINFO) {

            if (location != null) {
                String gl = location.getLongitude() + "-" + location.getLatitude();
                if (TextUtils.isEmpty(gl)) {
                    return;
                }
                SPHelper.setStringValue2SP(mContext, EGContext.LAST_LOCATION, gl);
            }
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
        double s = 2 * Math.asin(Math
                .sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
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
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                String lastLocation = SPHelper.getStringValueFromSP(mContext, EGContext.LAST_LOCATION, "");
                if (TextUtils.isEmpty(lastLocation)) {
//                if (BuildConfig.logcat) {
//                    ELOG.d(BuildConfig.tag_loc, "距离检测。SP未缓存。");
//                }
                    return true;
                }

                if (lastLocation.contains("-")) {
                    String[] ary = lastLocation.split("-");
                    if (ary.length != 2) {
//                    if (BuildConfig.logcat) {
//                        ELOG.d(BuildConfig.tag_loc, "缓存有值。");
//                    }
                        return true;
                    }
                    double longitude1 = Double.parseDouble(ary[1]);
                    double latitude1 = Double.parseDouble(ary[0]);
                    double distance = getDistance(longitude1, latitude1, location.getLongitude(), location.getLatitude());
                    // 距离没有变化则不保存
                    if (EGContext.MINDISTANCE <= distance) {
//                    if (BuildConfig.logcat) {
//                        ELOG.d(BuildConfig.tag_loc, "有变化。");
//                    }
                        return true;
                    }
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, e);
                }
            }
        }
        return false;
    }

    public JSONObject getLocation() {
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                locationJson = new JSONObject();
                try {
                    JsonUtils.pushToJSON(mContext, locationJson, UploadKey.LocationInfo.CollectionTime,
                            String.valueOf(System.currentTimeMillis()), DataController.SWITCH_OF_COLLECTION_TIME);
                } catch (Throwable t) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(BuildConfig.tag_loc, t);
                    }
                }
                try {
                    String locationInfo = SPHelper.getStringValueFromSP(mContext, EGContext.LAST_LOCATION, "");
                    JsonUtils.pushToJSON(mContext, locationJson, UploadKey.LocationInfo.GeographyLocation,
                            locationInfo, DataController.SWITCH_OF_GEOGRAPHY_LOCATION);
                } catch (Throwable t) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(BuildConfig.tag_loc, t);
                    }
                }

//            if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_WIFI, true)) {
                if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_WIFI,
                        true)) {
                    try {
                        JSONArray wifiInfo = WifiImpl.getInstance(mContext).getWifiInfo();
                        if (wifiInfo != null && wifiInfo.length() > 0) {
                            JsonUtils.pushToJSON(mContext, locationJson, UploadKey.LocationInfo.WifiInfo.NAME,
                                    wifiInfo, DataController.SWITCH_OF_WIFI_NAME);
                        }
                    } catch (Throwable t) {
                        if (BuildConfig.ENABLE_BUG_REPORT) {
                            BugReportForTest.commitError(BuildConfig.tag_loc, t);
                        }
                    }
                }

//            if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_BASE,
//                    true)) {
                if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BASE,
                        true)) {
                    try {
                        JSONArray baseStation = getBaseStationInfo();
                        if (baseStation != null && baseStation.length() > 0) {
                            JsonUtils.pushToJSON(mContext, locationJson,
                                    UploadKey.LocationInfo.BaseStationInfo.NAME, baseStation,
                                    DataController.SWITCH_OF_BS_NAME);
                        }
                    } catch (Throwable t) {
                        if (BuildConfig.ENABLE_BUG_REPORT) {
                            BugReportForTest.commitError(BuildConfig.tag_loc, t);
                        }
                    }

                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, e);
                }
            }
        }
        return locationJson;
    }

    /**
     * 基站信息 1.判断权限 2.周围基站最多前五 3.GSM or CDMA 基站信息
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public JSONArray getBaseStationInfo() {
        JSONArray jsonArray = null;
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            JSONObject jsonObject = null, tempJsonObj = null;
            Set<String> cid = new HashSet<String>();
//        List<Integer> cidList = new ArrayList<Integer>();
//        List<Integer> lacList = new ArrayList<Integer>();
            try {
                if (mTelephonyManager == null) {
                    mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                }
                if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    jsonArray = new JSONArray();
                    Map<String, JSONObject> tempGsmMap = new HashMap<String, JSONObject>(),
                            tempCdmaMap = new HashMap<String, JSONObject>();
                    List<JSONObject> gsmList = new ArrayList<JSONObject>(), cdmaList = new ArrayList<JSONObject>();
                    try {
                        JSONObject obj = null;
                        // https://blog.csdn.net/itfootball/article/details/25421015
                        List<CellInfo> infos = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            infos = mTelephonyManager.getAllCellInfo();

                            if (infos != null && infos.size() > 0) {
                                int tempCid = -1;// cid
                                int tempLac = -1;// lac
                                int strength = -1;// 信号强度
                                String key = null;
                                for (CellInfo info : infos) {
                                    tempCid = -1;
                                    tempLac = -1;
                                    strength = -1;
                                    if (info instanceof CellInfoCdma) {
                                        try {
                                            CellInfoCdma cdma = (CellInfoCdma) info;
                                            tempCid = cdma.getCellIdentity().getBasestationId();
                                            tempLac = cdma.getCellIdentity().getNetworkId();
                                            key = tempCid + "|" + tempLac;
                                            if (tempCid > 0 && tempLac > 0 && (!cid.contains(key))) {
                                                cid.add(key);
                                                obj = new JSONObject();
                                                strength = cdma.getCellSignalStrength().getDbm();
                                                obj = getBaseStationInfoObj(obj, tempLac, tempCid, strength, "0", 0,
                                                        cdma.getCellSignalStrength().getCdmaEcio(), 0);
                                                tempCdmaMap.put(strength + "|" + key, obj);
                                                tempJsonObj = new JSONObject();
                                                tempJsonObj.put("stren", strength);
                                                tempJsonObj.put("mapKey", strength + "|" + key);
                                                cdmaList.add(tempJsonObj);
                                            }
                                        } catch (Throwable e) {
                                        }
                                    } else if (info instanceof CellInfoGsm) {
                                        try {
                                            CellInfoGsm gsm = (CellInfoGsm) info;
                                            tempCid = gsm.getCellIdentity().getCid();
                                            tempLac = gsm.getCellIdentity().getLac();
                                            key = tempCid + "|" + tempLac;
                                            if (tempCid > 0 && tempLac > 0 && (!cid.contains(key))) {
                                                cid.add(key);
                                                obj = new JSONObject();
                                                strength = gsm.getCellSignalStrength().getDbm();
                                                obj = getBaseStationInfoObj(obj, tempLac, tempCid, strength,
                                                        UploadKey.LocationInfo.BaseStationInfo.PSC + "&"
                                                                + gsm.getCellIdentity().getPsc(),
                                                        0, 0, 0);
                                                tempGsmMap.put(strength + "|" + key, obj);
                                                tempJsonObj = new JSONObject();
                                                tempJsonObj.put("stren", strength);
                                                tempJsonObj.put("mapKey", strength + "|" + key);
                                                gsmList.add(tempJsonObj);
                                            }
                                        } catch (Throwable e) {
                                        }
                                    } else if (info instanceof CellInfoLte) {
                                        try {
                                            CellInfoLte lte = (CellInfoLte) info;
                                            tempCid = lte.getCellIdentity().getPci();
                                            tempLac = lte.getCellIdentity().getTac();
                                            key = tempCid + "|" + tempLac;
                                            if (tempCid > 0 && tempLac > 0 && (!cid.contains(key))) {

                                                cid.add(key);
                                                obj = new JSONObject();
                                                strength = lte.getCellSignalStrength().getDbm();
                                                String lteString = lte.getCellSignalStrength().toString();
                                                obj = getBaseStationInfoObj(obj, tempLac, tempCid, strength,
                                                        UploadKey.LocationInfo.BaseStationInfo.PCI + "&"
                                                                + lte.getCellIdentity().getPci(),
                                                        strength, 0,
                                                        Integer.parseInt(lteString.substring(lteString.indexOf("rsrq=") + 5,
                                                                lteString.indexOf(" rssnr="))));
                                                tempCdmaMap.put(strength + "|" + key, obj);
                                                tempJsonObj = new JSONObject();
                                                tempJsonObj.put("stren", strength);
                                                tempJsonObj.put("mapKey", strength + "|" + key);
                                                cdmaList.add(tempJsonObj);

                                            }
                                        } catch (Throwable e) {
                                        }
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                        try {
                                            if (info instanceof CellInfoWcdma) {
                                                CellInfoWcdma wcdma = (CellInfoWcdma) info;
                                                tempCid = wcdma.getCellIdentity().getCid();
                                                tempLac = wcdma.getCellIdentity().getLac();
                                                key = tempCid + "|" + tempLac;
                                                if (tempCid > 0 && tempLac > 0 && (!cid.contains(key))) {
                                                    cid.add(key);
                                                    obj = new JSONObject();
                                                    strength = wcdma.getCellSignalStrength().getDbm();
                                                    obj = getBaseStationInfoObj(obj, tempLac, tempCid, strength,
                                                            UploadKey.LocationInfo.BaseStationInfo.PSC + "&"
                                                                    + wcdma.getCellIdentity().getPsc(),
                                                            0, 0, 0);
                                                    tempCdmaMap.put(strength + "|" + key, obj);
                                                    tempJsonObj = new JSONObject();
                                                    tempJsonObj.put("stren", strength);
                                                    tempJsonObj.put("mapKey", strength + "|" + key);
                                                    cdmaList.add(tempJsonObj);
                                                }
                                            }
                                        } catch (Throwable e) {
                                        }
                                    }

                                }
                            }
                        }
                    } catch (Throwable t) {
                        if (BuildConfig.ENABLE_BUG_REPORT) {
                            BugReportForTest.commitError(BuildConfig.tag_loc, t);
                        }
                    }
                    if (gsmList != null && tempGsmMap != null) {
                        listFilter(gsmList, jsonArray, tempGsmMap);
                    }
                    if (cdmaList != null && tempCdmaMap != null) {
                        listFilter(cdmaList, jsonArray, tempCdmaMap);
                    }
                    return jsonArray;
                }
            } catch (Exception e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, e);
                }
            }
        }
        return jsonArray;
    }

    private void listFilter(List<JSONObject> list, JSONArray jsonArray, Map<String, JSONObject> map) {
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                int count = 0;
                if (list != null && list.size() > 0) {
                    // 降序排列
                    baseStationSortByStren(list);
                    String tempKey = null;
                    for (int k = 0; k < list.size(); k++) {
                        if (count < 5) {
                            count = count + 1;
                            tempKey = list.get(k).optString("mapKey");
                            JSONObject obj = map.get(tempKey);
                            if (obj != null && obj.length() > 0) {
                                jsonArray.put(obj);
                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, t);
                }
            }
        }
    }


    /**
     * 基站列表排序
     */
    public void baseStationSortByStren(List<JSONObject> list) {
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            JSONObject obj = null;
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(i).optInt("stren", -1) < list.get(j).optInt("stren", -1)) {
                        obj = list.get(i);
                        list.set(i, list.get(j));
                        list.set(j, obj);
                    }
                }
            }
        }
    }

    public JSONObject getBaseStationInfoObj(JSONObject jsonObject, int lac, int cid, int stren, String psc, int rsrp,
                                            int ecio, int rsrq) {
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            try {
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.BaseStationInfo.LocationAreaCode,
                        lac, DataController.SWITCH_OF_LOCATION_AREA_CODE);
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.BaseStationInfo.CellId, cid,
                        DataController.SWITCH_OF_CELL_ID);
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.BaseStationInfo.Level, stren,
                        DataController.SWITCH_OF_BS_LEVEL);
                if (!TextUtils.isEmpty(psc) && !"0".equals(psc)) {
                    String key = psc.substring(0, psc.indexOf("&"));
                    String value = psc.substring(psc.lastIndexOf("&") + 1, psc.length());
                    JsonUtils.pushToJSON(mContext, jsonObject, key, value, DataController.SWITCH_OF_BS_PCI);
                }
                if (rsrp != 0) {
                    JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.BaseStationInfo.RSRP, rsrp,
                            DataController.SWITCH_OF_BS_RSRP);
                }
                if (ecio != 0) {
                    JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.BaseStationInfo.ECIO, ecio,
                            DataController.SWITCH_OF_BS_ECIO);
                }
                // 仅当DYNAMIC_NETWORK_TYPE字段为4G时需要填写
                if (rsrq != 0 && NetworkUtils.getNetworkType(mContext) == EGContext.NETWORK_TYPE_4G) {
                    JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.LocationInfo.BaseStationInfo.RSRQ, rsrq,
                            DataController.SWITCH_OF_BS_RSRQ);
                }
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_loc, t);
                }
            }
        }
        return jsonObject;
    }

    /*********************************************** 单例和变量 *****************************************************/
    private static class Holder {
        private static final LocationImpl INSTANCE = new LocationImpl();
    }


    private LocationImpl() {
    }

    public static LocationImpl getInstance(Context context) {

        Holder.INSTANCE.init(context);
        return LocationImpl.Holder.INSTANCE;
    }

    private void init(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
        if (mLocationManager == null && mContext != null) {
            mLocationManager = (LocationManager) mContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private static int permissionAskCount = 0;
    Context mContext;
    TelephonyManager mTelephonyManager = null;
    JSONObject locationJson = null;
    private LocationManager mLocationManager;

//    /**
//     * 基站列表排序
//     */
//    public void baseStationSort(List<NeighboringCellInfo> list) {
//        for (int i = 0; i < list.size() - 1; i++) {
//            for (int j = i + 1; j < list.size(); j++) {
//                if (list.get(i).getCid() == list.get(j).getCid()) {
//                    list.remove(j);
//                    continue;
//                }
//                if (list.get(i).getRssi() < list.get(j).getRssi()) {
//                    NeighboringCellInfo cellInfo = list.get(i);
//                    list.set(i, list.get(j));
//                    list.set(j, cellInfo);
//                }
//            }
//        }
//    }
}
