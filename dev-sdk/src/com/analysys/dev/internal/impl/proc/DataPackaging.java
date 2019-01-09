package com.analysys.dev.internal.impl.proc;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.impl.DeviceImpl;
import com.analysys.dev.internal.impl.OCImpl;
import com.analysys.dev.model.BatteryModuleNameInfo;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * 存储、上传数据组装
 */
public class DataPackaging {
    /**
     * 基础信息,公用接口,应用设备都使用
     *  上行字段,DevInfo
     * @return
     */
    public static JSONObject getDevInfo(Context mContext) {

        JSONObject deviceInfo = new JSONObject();
        try {
            DeviceImpl devImpl = DeviceImpl.getInstance(mContext);

            deviceInfo.put(DeviceKeyContacts.DevInfo.SystemName, devImpl.getSystemName());
            deviceInfo.put(DeviceKeyContacts.DevInfo.SystemVersion, devImpl.getSystemVersion());
            deviceInfo.put(DeviceKeyContacts.DevInfo.DeviceBrand, devImpl.getDeviceBrand());
            deviceInfo.put(DeviceKeyContacts.DevInfo.DeviceId, devImpl.getDeviceId());
            deviceInfo.put(DeviceKeyContacts.DevInfo.DeviceModel, devImpl.getDeviceModel());
            deviceInfo.put(DeviceKeyContacts.DevInfo.MAC, devImpl.getMac());
            deviceInfo.put(DeviceKeyContacts.DevInfo.SerialNumber, devImpl.getSerialNumber());
            deviceInfo.put(DeviceKeyContacts.DevInfo.Resolution, devImpl.getResolution());//系统分辨率
            deviceInfo.put(DeviceKeyContacts.DevInfo.DotPerInch, devImpl.getDotPerInch());

            deviceInfo.put(DeviceKeyContacts.DevInfo.MobileOperator, devImpl.getMobileOperator());
            deviceInfo.put(DeviceKeyContacts.DevInfo.MobileOperatorName, devImpl.getMobileOperatorName());
            deviceInfo.put(DeviceKeyContacts.DevInfo.NetworkOperatorCode, devImpl.getNetworkOperatorCode());
            deviceInfo.put(DeviceKeyContacts.DevInfo.NetworkOperatorName, devImpl.getNetworkOperatorName());
            deviceInfo.put(DeviceKeyContacts.DevInfo.Imeis, devImpl.getIMEIS());//获取IMEI,一个或者两个?是否有三卡的?
            deviceInfo.put(DeviceKeyContacts.DevInfo.Imsis, devImpl.getIMSIS());
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationChannel, devImpl.getApplicationChannel());
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationKey, devImpl.getApplicationKey());
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationName, devImpl.getApplicationName());
            deviceInfo.put(DeviceKeyContacts.DevInfo.APILevel, devImpl.getAPILevel());
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationPackageName, devImpl.getApplicationPackageName());
            deviceInfo.put(DeviceKeyContacts.DevInfo.SDKVersion, devImpl.getSdkVersion());
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationVersionCode, devImpl.getApplicationVersionCode());
            deviceInfo.put(DeviceKeyContacts.DevInfo.AppMD5, devImpl.getAppMD5());
            deviceInfo.put(DeviceKeyContacts.DevInfo.AppSign, devImpl.getAppSign());
            deviceInfo.put(DeviceKeyContacts.DevInfo.TempID, devImpl.getTempID());
            if (EGContext.SWITCH_OF_PREVENT_CHEATING) {
                deviceInfo.put(DeviceKeyContacts.DevInfo.Simulator, devImpl.isSimulator());
                deviceInfo.put(DeviceKeyContacts.DevInfo.Debug, devImpl.getDebug());
                deviceInfo.put(DeviceKeyContacts.DevInfo.Hijack, devImpl.isHijack());//TODO:需要方法实现,0表示没有被劫持,1表示被劫持
                deviceInfo.put(DeviceKeyContacts.DevInfo.IsRoot, devImpl.IsRoot());
            }
            if (EGContext.SWITCH_OF_BLUETOOTH) {
                deviceInfo.put(DeviceKeyContacts.DevInfo.BluetoothMac, devImpl.getBluetoothMac());
                deviceInfo.put(DeviceKeyContacts.DevInfo.BluetoothName, devImpl.getBluetoothName());
            }
            if (EGContext.SWITCH_OF_SYSTEM_INFO) {
                deviceInfo.put(DeviceKeyContacts.DevInfo.SystemFontSize, devImpl.getSystemFontSize());
                deviceInfo.put(DeviceKeyContacts.DevInfo.SystemHour, devImpl.getSystemHour());
                deviceInfo.put(DeviceKeyContacts.DevInfo.SystemLanguage, devImpl.getSystemLanguage());
                deviceInfo.put(DeviceKeyContacts.DevInfo.SystemArea, devImpl.getSystemArea());
                deviceInfo.put(DeviceKeyContacts.DevInfo.TimeZone, devImpl.getTimeZone());
            }
            JSONObject batteryJson = new JSONObject();
            if (EGContext.SWITCH_OF_BATTERY) {
                BatteryModuleNameInfo battery = BatteryModuleNameInfo.getInstance();
                if (!TextUtils.isEmpty(battery.getBatteryStatus()))
                    batteryJson.put(DeviceKeyContacts.DevInfo.BatteryStatus, battery.getBatteryStatus());
                if (!TextUtils.isEmpty(battery.getBatteryHealth()))
                    batteryJson.put(DeviceKeyContacts.DevInfo.BatteryHealth, battery.getBatteryHealth());
                if (!TextUtils.isEmpty(battery.getBatteryLevel()))
                    batteryJson.put(DeviceKeyContacts.DevInfo.BatteryLevel, battery.getBatteryLevel());
                if (!TextUtils.isEmpty(battery.getBatteryScale()))
                    batteryJson.put(DeviceKeyContacts.DevInfo.BatteryScale, battery.getBatteryScale());
                if (!TextUtils.isEmpty(battery.getBatteryPlugged()))
                    batteryJson.put(DeviceKeyContacts.DevInfo.BatteryPlugged, battery.getBatteryPlugged());
                if (!TextUtils.isEmpty(battery.getBatteryTechnology()))
                    batteryJson.put(DeviceKeyContacts.DevInfo.BatteryTechnology, battery.getBatteryTechnology());
                if (!TextUtils.isEmpty(battery.getBatteryTemperature()))
                    batteryJson.put(DeviceKeyContacts.DevInfo.BatteryTemperature, battery.getBatteryTemperature());
            }
            if (EGContext.SWITCH_OF_DEV_FURTHER_DETAIL) {
                batteryJson.put(DeviceKeyContacts.DevInfo.CPUModel, devImpl.getCPUModel());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildId, devImpl.getBuildId());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildDisplay, devImpl.getBuildDisplay());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildProduct, devImpl.getBuildProduct());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildDevice, devImpl.getBuildDevice());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildBoard, devImpl.getBuildBoard());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildBootloader, devImpl.getBuildBootloader());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildHardware, devImpl.getBuildHardware());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSupportedAbis, devImpl.getBuildSupportedAbis());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSupportedAbis32, devImpl.getBuildSupportedAbis32());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSupportedAbis64, devImpl.getBuildSupportedAbis64());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildType, devImpl.getBuildType());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildTags, devImpl.getBuildTags());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildFingerPrint, devImpl.getBuildFingerPrint());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildRadioVersion, devImpl.getBuildRadioVersion());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildIncremental, devImpl.getBuildIncremental());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildBaseOS, devImpl.getBuildBaseOS());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSecurityPatch, devImpl.getBuildSecurityPatch());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSdkInt, devImpl.getBuildSdkInt());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildPreviewSdkInt, devImpl.getBuildPreviewSdkInt());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildCodename, devImpl.getBuildCodename());
                batteryJson.put(DeviceKeyContacts.DevInfo.IDFA, devImpl.getIDFA());
            }
            deviceInfo.put("ETDM", batteryJson.toString());
        }catch (Throwable t){

        }
        return deviceInfo;
    }

    /**
     * OCCount
     * @return
     */
    public static JSONArray getOCCount(){
        //暂时这版不要
        return null;
    }

    /**
     * Processinfo
     * @return
     */
    public static JSONObject getProcessinfo(){
        //这版暂时预留接口
        return null;
    }

    /**
     * AppSnapshotInfo
     * @return
     */
    public static JSONArray getAppSnapshotInfo(){

        return null;
    }

    /**
     * LocationInfo
     * @return
     */
    public static JSONArray getLocationInfo(){
        return null;
    }
}
