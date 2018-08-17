package com.eguan.monitor;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.eguan.Constants;
import com.eguan.db.DeviceTableOperation;
import com.eguan.monitor.imp.DriverInfo;
import com.eguan.monitor.imp.DriverInfoManager;
import com.eguan.monitor.imp.IUUInfo;
import com.eguan.monitor.imp.InstalledAPPInfoManager;
import com.eguan.monitor.imp.InstalledAppInfo;
import com.eguan.monitor.imp.OCInfo;
import com.eguan.monitor.imp.OCTimeBean;
import com.eguan.monitor.imp.WBGInfo;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.EguanIdUtils;
import com.eguan.utils.commonutils.SPUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceJsonFactory {

    /**
     * 获取设备信息
     *
     * @return
     */

    public static JSONObject getDeviceJson(Context mContext) {

        JSONObject deviceInfo = new JSONObject();
        try {
            DriverInfoManager driverInfoManager = new DriverInfoManager(mContext);

            driverInfoManager.setDriverInfo();
            DriverInfo driverInfo = DriverInfo.getInstance();

            if (!TextUtils.isEmpty(driverInfo.getSystemName())) {
                deviceInfo.put("SN", driverInfo.getSystemName());
            }
            if (!TextUtils.isEmpty(driverInfo.getDeviceId())) {
                deviceInfo.put("DI", driverInfo.getDeviceId());
            }

            if (!TextUtils.isEmpty(driverInfo.getMACAddress())) {
                deviceInfo.put("MAC", driverInfo.getMACAddress());
            }

            if (!TextUtils.isEmpty(driverInfo.getIMSI()) && !driverInfo.getIMSI().equals("null")) {
                deviceInfo.put("IMSI", driverInfo.getIMSI());
            }

            if (!TextUtils.isEmpty(driverInfo.getAndroidID())) {
                deviceInfo.put("AID", driverInfo.getAndroidID());
            }

            if (!TextUtils.isEmpty(driverInfo.getSerialNumber())) {
                deviceInfo.put("SNR", driverInfo.getSerialNumber());
            }

            if (!TextUtils.isEmpty(driverInfo.getDeviceBrand())) {
                deviceInfo.put("DB", driverInfo.getDeviceBrand());
            }

            if (!TextUtils.isEmpty(driverInfo.getDeviceModel())) {
                deviceInfo.put("DM", driverInfo.getDeviceModel());
            }

            if (!TextUtils.isEmpty(driverInfo.getSystemVersion())) {
                deviceInfo.put("SV", driverInfo.getSystemVersion());
            }

            if (!TextUtils.isEmpty(driverInfo.getIsJailbreak())) {
                deviceInfo.put("IJ", driverInfo.getIsJailbreak());
            }

            if (!TextUtils.isEmpty(driverInfo.getApplicationPackageName())) {
                deviceInfo.put("APN", driverInfo.getApplicationPackageName());
            }

            if (!TextUtils.isEmpty(driverInfo.getApplicationName())) {
                deviceInfo.put("AN", driverInfo.getApplicationName());
            }

            if (!TextUtils.isEmpty(driverInfo.getApplicationChannel())) {
                deviceInfo.put("AC", driverInfo.getApplicationChannel());
            }

            if (!TextUtils.isEmpty(driverInfo.getApplicationKey())) {
                deviceInfo.put("AK", driverInfo.getApplicationKey());
            }

            if (!TextUtils.isEmpty(driverInfo.getApplicationUserId())) {
                deviceInfo.put("AUI", driverInfo.getApplicationUserId());
            }

            if (!TextUtils.isEmpty(Constants.SDK_VERSION)) {
                deviceInfo.put("SDKV", Constants.SDK_VERSION);
            }

            if (!TextUtils.isEmpty(driverInfo.getApplicationVersionCode())) {
                deviceInfo.put("AV", driverInfo.getApplicationVersionCode());
            }

            if (!TextUtils.isEmpty(driverInfo.getApiLevel())) {
                deviceInfo.put("APIL", driverInfo.getApiLevel());
            }

            if (!TextUtils.isEmpty(driverInfo.getMobileOperator())) {
                deviceInfo.put("MO", driverInfo.getMobileOperator());
            }

            if (!TextUtils.isEmpty(driverInfo.getPhoneNum())) {
                deviceInfo.put("PN", driverInfo.getPhoneNum());
            }

            if (!TextUtils.isEmpty(Constants.SYSTEMNAME)) {
                deviceInfo.put("SN", Constants.SYSTEMNAME);
            }
            deviceInfo.put("DBG", SPUtil.getInstance(mContext).getDebugMode() ? "1" : "0");
            deviceInfo.put("HJK", "0");// TODO:需要方法实现,0表示没有被劫持,1表示被劫持
            deviceInfo.put("SIR", "0");// TODO:需要方法实现,0表示非模拟器,1表示模拟器
            List<String> list = EguanIdUtils.getInstance(mContext).getId();
            if (list.size() == 2) {
                if (!TextUtils.isEmpty(list.get(0))) {
                    deviceInfo.put("TMPID", list.get(0));
                }
                if (!TextUtils.isEmpty(list.get(1))) {
                    deviceInfo.put("EGID", list.get(1));
                }
            }
            if (!TextUtils.isEmpty(driverInfo.getIMEIS())) {
                deviceInfo.put("IMS", driverInfo.getIMEIS());// 获取IMEI,一个或者两个?是否有三卡的?
            }

            if (!TextUtils.isEmpty(driverInfo.getBluetoothMAC())) {
                deviceInfo.put("BMAC", driverInfo.getBluetoothMAC());// bluetoochMac
            }

            if (!TextUtils.isEmpty(driverInfo.getResolution())) {
                deviceInfo.put("RES", driverInfo.getResolution());// 系统分辨率
            }

            if (!TextUtils.isEmpty(driverInfo.getSystemFontSize())) {
                deviceInfo.put("SFS", driverInfo.getSystemFontSize());// 系统字体大小
            }

            if (!TextUtils.isEmpty(driverInfo.getTimeZone())) {
                deviceInfo.put("TZ", driverInfo.getTimeZone());// 系统时区
            }

            if (!TextUtils.isEmpty(driverInfo.getSystemArea())) {
                deviceInfo.put("SA", driverInfo.getSystemArea());// 设备所在地
            }

            if (!TextUtils.isEmpty(driverInfo.getSystemLanguage())) {
                deviceInfo.put("SL", driverInfo.getSystemLanguage());// 系统语言
            }

            if (!TextUtils.isEmpty(driverInfo.getSystemHour())) {
                deviceInfo.put("SH", driverInfo.getSystemHour());// 系统小时制
            }

            if (!TextUtils.isEmpty(driverInfo.getMobileOperatorName())) {
                deviceInfo.put("MPN", driverInfo.getMobileOperatorName());
            }

            if (!TextUtils.isEmpty(driverInfo.getNetworkOperatorCode())) {
                deviceInfo.put("NOC", driverInfo.getNetworkOperatorCode());
            }

            if (!TextUtils.isEmpty(driverInfo.getNetworkOperatorName())) {
                deviceInfo.put("NON", driverInfo.getNetworkOperatorName());
            }

            // v3.7.5新增字段
            int simulator = driverInfo.getSimulator();
            deviceInfo.put("SIR", simulator + ""); // 是否为模拟器,0代表非模拟器;1代表模拟器

            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.d("getDeviceJson 设备信息:" + deviceInfo);
            }
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

        return deviceInfo;
    }

    /**
     * 应用安装卸载信息
     *
     * @return
     */
    public static JSONArray getIUUInfo(Context context) {

        JSONArray iuuJsonArray = new JSONArray();
        try {
            List<IUUInfo> list = DeviceTableOperation.getInstance(context).selectIUUInfo();
            list = dataFiltering(list);
            for (int i = 0; i < list.size(); i++) {
                String AN = list.get(i).getApplicationName();
                String AVC = list.get(i).getApplicationVersionCode();
                if (TextUtils.isEmpty(AN) || TextUtils.isEmpty(AVC)) {
                    continue;
                }
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("APN", list.get(i).getApplicationPackageName());
                jsonObject2.put("AN", AN);
                jsonObject2.put("AVC", AVC);
                jsonObject2.put("AT", list.get(i).getActionType());
                jsonObject2.put("AHT", list.get(i).getActionHappenTime());

                iuuJsonArray.put(jsonObject2);

            }
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return iuuJsonArray;
    }

    private static List<IUUInfo> dataFiltering(List<IUUInfo> list) {

        if (Build.BRAND.equals("LeEco") && Build.MODEL.equals("LEX720") && Build.VERSION.RELEASE.equals("6.0.1")
                && Build.VERSION.SDK_INT == 23) {

            return list;
        }

        List<IUUInfo> list2 = new ArrayList<IUUInfo>();
        for (int i = 0; i < list.size(); i++) {
            String pkgName1 = list.get(i).getApplicationPackageName();
            if (list.get(i).getActionType().equals("2")) {
                if (i >= 1) {
                    if (pkgName1.equals(list.get(i - 1).getApplicationPackageName())) {
                        if (Long.parseLong(list.get(i).getActionHappenTime())
                                - Long.parseLong(list.get(i - 1).getActionHappenTime()) < 5000) {
                            list2.add(list.get(i - 1));
                        }
                    }
                }
                if (i >= 2) {
                    if (pkgName1.equals(list.get(i - 2).getApplicationPackageName())) {
                        if (Long.parseLong(list.get(i).getActionHappenTime())
                                - Long.parseLong(list.get(i - 2).getActionHappenTime()) < 5000) {
                            list2.add(list.get(i - 2));
                        }
                    }
                }
                if (i <= list.size() - 2) {
                    if (pkgName1.equals(list.get(i + 1).getApplicationPackageName())) {
                        if (Long.parseLong(list.get(i + 1).getActionHappenTime())
                                - Long.parseLong(list.get(i).getActionHappenTime()) < 5000) {
                            list2.add(list.get(i + 1));
                        }
                    }
                }
                if (i <= list.size() - 3) {
                    if (pkgName1.equals(list.get(i + 2).getApplicationPackageName())) {
                        if (Long.parseLong(list.get(i + 2).getActionHappenTime())
                                - Long.parseLong(list.get(i).getActionHappenTime()) < 5000) {
                            list2.add(list.get(i + 2));
                        }
                    }
                }
            }
        }
        list.removeAll(list2);
        return list;
    }

    /**
     * 应用打开关闭信息
     *
     * @return
     */
    public static JSONArray getOCInfo(Context context) {
        List<OCInfo> getRunningTaskLists;
        JSONArray sumJSONArray = new JSONArray();
        try {
            List<OCInfo> list = DeviceTableOperation.getInstance(context).selectOCInfo("1");
            if (list.size() > 1) {
                getRunningTaskLists = dataMerge(context, dataIntersection(list));
            } else {
                getRunningTaskLists = list;
            }

            List<OCInfo> procList = DeviceTableOperation.getInstance(context).selectOCInfo("2");
            List<OCInfo> accessiblityList = DeviceTableOperation.getInstance(context).selectOCInfo("3");
            transfer(context, getRunningTaskLists, sumJSONArray);
            transfer(context, procList, sumJSONArray);
            transfer(context, accessiblityList, sumJSONArray);

        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return sumJSONArray;

    }

    private static void transfer(Context context, List<OCInfo> listOCInfo, JSONArray sumJSONArray)
            throws JSONException {
        if (listOCInfo.size() <= 0)
            return;
        for (int i = 0; i < listOCInfo.size(); i++) {
            String openTime = listOCInfo.get(i).getApplicationOpenTime();
            String closeTime = listOCInfo.get(i).getApplicationCloseTime();
            String version = listOCInfo.get(i).getApplicationVersionCode();
            String pkgName = listOCInfo.get(i).getApplicationPackageName();
            String appName = listOCInfo.get(i).getApplicationName();
            String appType = listOCInfo.get(i).getApplicationType();
            String switchType = listOCInfo.get(i).getSwitchType();
            String collectionType = listOCInfo.get(i).getCollectionType();
            String netType = listOCInfo.get(i).getNetwork();

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("AOT", openTime);
            jsonObject2.put("ACT", closeTime);
            jsonObject2.put("AVC", version);
            jsonObject2.put("APN", pkgName);
            jsonObject2.put("AN", appName);
            jsonObject2.put("NT", netType);

            JSONObject jobt = new JSONObject();
            if (!empty(switchType)) {
                jobt.put("ST", switchType);
            }
            if (!empty(appType)) {
                jobt.put("AT", appType);
            }
            if (!empty(collectionType)) {
                jobt.put("CT", collectionType);
            }
            if (jobt.length() > 0) {
                jsonObject2.put("ETDM", jobt);
            }
            sumJSONArray.put(jsonObject2);
        }
    }

    private static boolean empty(String value) {
        return value == null || value.equals("");
    }

    private static List<OCInfo> dataIntersection(List<OCInfo> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            long close_time = Long.parseLong(list.get(i).getApplicationCloseTime());
            long open_Time = Long.parseLong(list.get(i + 1).getApplicationOpenTime());
            if (close_time >= open_Time) {
                list.get(i).setApplicationCloseTime(open_Time + "");
            }
        }
        return list;
    }

    private static List<OCInfo> dataMerge(Context context, List<OCInfo> list) {

        long mergeInterval = SPUtil.getInstance(context).getMergeInterval();
        if (mergeInterval == 0) {
            mergeInterval = Constants.TIME_INTERVAL;
        } else {
            mergeInterval = mergeInterval * 1000;
        }
        for (int i = 0; i < list.size(); i++) {
            String pkgName1 = list.get(i).getApplicationPackageName();
            for (int j = i + 1; j < list.size(); j++) {
                String pkgName2 = list.get(j).getApplicationPackageName();
                if (pkgName1.equals(pkgName2)) {
                    long closeTime = Long.parseLong(list.get(i).getApplicationCloseTime());
                    long openTime = Long.parseLong(list.get(j).getApplicationOpenTime());
                    if ((openTime - closeTime) <= mergeInterval) {
                        list.get(j).setApplicationOpenTime(list.get(i).getApplicationOpenTime());
                        for (int k = 0; k < j - i; k++) {
                            list.remove(i);
                        }
                        dataMerge(context, list);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 应用安装列表
     *
     * @return
     */
    public static JSONArray getInstalledAPPInfos(Context mContext) {
        JSONArray installInfoArray = new JSONArray();
        try {

            InstalledAPPInfoManager appInfoManager = new InstalledAPPInfoManager();
            List<InstalledAppInfo> appInfos = appInfoManager.getPostAppInfoData(mContext);
            if (appInfos != null && appInfos.size() > 0) {
                JSONObject installInfo = null;
                for (InstalledAppInfo info : appInfos) {
                    installInfo = new JSONObject();
                    installInfo.put("APN", info.getApplicationPackageName());
                    installInfo.put("AN", info.getApplicationName());
                    installInfo.put("AVC", info.getApplicationVersionCode());
                    installInfo.put("IN", info.getIsNew());
                    installInfoArray.put(installInfo);
                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return installInfoArray;
    }

    /**
     * 基站信息
     *
     * @return
     */
    public static JSONArray getBaseStationInfos(Context mContext) {
        JSONArray wbgJsonArray = new JSONArray();
        List<WBGInfo> WBGInfos = DeviceTableOperation.getInstance(mContext).selectWBGInfo();

        try {
            for (WBGInfo info : WBGInfos) {
                String ssid, bssid, level, locationAreaCode, callId, geographyLocation, ip;
                String collectionTime;
                JSONObject wbgJsonObject = new JSONObject();
                ssid = info.getSSID();
                bssid = info.getBSSID();
                level = info.getLevel();
                locationAreaCode = info.getLocationAreaCode();
                callId = info.getCellId();
                collectionTime = info.getCollectionTime();
                geographyLocation = info.getGeographyLocation();
                ip = info.getIp();
                if (!TextUtils.isEmpty(ssid)) {
                    wbgJsonObject.put("SSID", ssid);
                }
                if (!TextUtils.isEmpty(bssid)) {
                    wbgJsonObject.put("BSSID", bssid);
                }
                if (!TextUtils.isEmpty(level)) {
                    wbgJsonObject.put("LEVEL", level);
                }
                if (!TextUtils.isEmpty(locationAreaCode)) {
                    wbgJsonObject.put("LAC", locationAreaCode);
                }
                if (!TextUtils.isEmpty(callId)) {
                    wbgJsonObject.put("CellId", callId);
                }
                if (!TextUtils.isEmpty(collectionTime)) {
                    wbgJsonObject.put("CT", collectionTime);
                }
                if (!TextUtils.isEmpty(geographyLocation)) {
                    wbgJsonObject.put("GL", geographyLocation);
                }
                if (!TextUtils.isEmpty(ip)) {
                    wbgJsonObject.put("ip", ip);
                }
                if (wbgJsonObject.length() > 0) {
                    wbgJsonArray.put(wbgJsonObject);
                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        EgLog.v("基站信息--：" + wbgJsonArray);
        return wbgJsonArray;
    }

    public static JSONArray getOCTimesJson(Context mContext) {
        JSONArray result = new JSONArray();
        try {
            List<OCTimeBean> beans = DeviceTableOperation.getInstance(mContext).selectOCTimes();
            JSONObject jo;
            for (OCTimeBean bean : beans) {
                jo = new JSONObject();
                jo.put("PN", bean.packageName);
                jo.put("CU", bean.count + "");
                jo.put("TI", bean.timeInterval);
                result.put(jo);
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return result;
    }
}
