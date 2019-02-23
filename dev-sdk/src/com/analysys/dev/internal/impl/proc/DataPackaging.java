package com.analysys.dev.internal.impl.proc;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.impl.DeviceImpl;

import com.analysys.dev.internal.impl.PolicyImpl;
import com.analysys.dev.internal.impl.SenSorModuleNameImpl;
import com.analysys.dev.model.BatteryModuleNameInfo;
import com.analysys.dev.utils.ELOG;

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
            if(!TextUtils.isEmpty(devImpl.getSystemName()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.SystemName, devImpl.getSystemName());
            if(!TextUtils.isEmpty(devImpl.getSystemVersion()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.SystemVersion, devImpl.getSystemVersion());
            if(!TextUtils.isEmpty(devImpl.getDeviceBrand()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.DeviceBrand, devImpl.getDeviceBrand());
            if(!TextUtils.isEmpty(devImpl.getDeviceId()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.DeviceId, devImpl.getDeviceId());
            if(!TextUtils.isEmpty(devImpl.getDeviceModel()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.DeviceModel, devImpl.getDeviceModel());
            if(!TextUtils.isEmpty(devImpl.getMac()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.MAC, devImpl.getMac());
            if(!TextUtils.isEmpty(devImpl.getSerialNumber()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.SerialNumber, devImpl.getSerialNumber());
            if(!TextUtils.isEmpty(devImpl.getResolution()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.Resolution, devImpl.getResolution());//系统分辨率
            if(!TextUtils.isEmpty(devImpl.getDotPerInch()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.DotPerInch, devImpl.getDotPerInch());
            if(!TextUtils.isEmpty(devImpl.getMobileOperator()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.MobileOperator, devImpl.getMobileOperator());
            if(!TextUtils.isEmpty(devImpl.getMobileOperatorName()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.MobileOperatorName, devImpl.getMobileOperatorName());
            if(!TextUtils.isEmpty(devImpl.getNetworkOperatorCode()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.NetworkOperatorCode, devImpl.getNetworkOperatorCode());
            if(!TextUtils.isEmpty(devImpl.getNetworkOperatorName()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.NetworkOperatorName, devImpl.getNetworkOperatorName());
            if(!TextUtils.isEmpty(devImpl.getIMEIS()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.Imeis, devImpl.getIMEIS());//获取IMEI,一个或者两个?是否有三卡的?
            if(!TextUtils.isEmpty(devImpl.getIMSIS()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.Imsis, devImpl.getIMSIS());
            if(!TextUtils.isEmpty(devImpl.getApplicationChannel()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationChannel, devImpl.getApplicationChannel());
            if(!TextUtils.isEmpty(devImpl.getApplicationKey()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationKey, devImpl.getApplicationKey());
            if(!TextUtils.isEmpty(devImpl.getApplicationName()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationName, devImpl.getApplicationName());
            if(!TextUtils.isEmpty(devImpl.getAPILevel()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.APILevel, devImpl.getAPILevel());
            if(!TextUtils.isEmpty(devImpl.getApplicationPackageName()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationPackageName, devImpl.getApplicationPackageName());
            if(!TextUtils.isEmpty(devImpl.getSdkVersion()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.SDKVersion, devImpl.getSdkVersion());
            if(!TextUtils.isEmpty(devImpl.getApplicationVersionCode()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.ApplicationVersionCode, devImpl.getApplicationVersionCode());
            if(!TextUtils.isEmpty(devImpl.getAppMD5()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.AppMD5, devImpl.getAppMD5());
            if(!TextUtils.isEmpty(devImpl.getAppSign()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.AppSign, devImpl.getAppSign());
            if(!TextUtils.isEmpty(devImpl.getTempID()))
            deviceInfo.put(DeviceKeyContacts.DevInfo.TempID, devImpl.getTempID());
//            ELOG.i("deviceInfo ::::::"+deviceInfo);

            if (PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.PREVENT_CHEATING_SWITCH,EGContext.SWITCH_OF_PREVENT_CHEATING)) {
                if(!TextUtils.isEmpty(devImpl.isSimulator()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.Simulator, devImpl.isSimulator());
                if(!TextUtils.isEmpty(devImpl.getDebug()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.Debug, devImpl.getDebug());
                if(!TextUtils.isEmpty(devImpl.isHijack()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.Hijack, devImpl.isHijack());//0表示没有被劫持,1表示被劫持
                if(!TextUtils.isEmpty(devImpl.IsRoot()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.IsRoot, devImpl.IsRoot());
            }

            if (PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.BLUETOOTH_SWITCH,EGContext.SWITCH_OF_BLUETOOTH)) {
                if(!TextUtils.isEmpty(devImpl.getBluetoothMac()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.BluetoothMac, devImpl.getBluetoothMac());
                if(!TextUtils.isEmpty(devImpl.getBluetoothName()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.BluetoothName, devImpl.getBluetoothName());
            }
            if (PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.SYSTEM_INFO_SWITCH,EGContext.SWITCH_OF_SYSTEM_INFO)) {
                if(!TextUtils.isEmpty(devImpl.getSystemFontSize()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.SystemFontSize, devImpl.getSystemFontSize());
                if(!TextUtils.isEmpty(devImpl.getSystemHour()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.SystemHour, devImpl.getSystemHour());
                if(!TextUtils.isEmpty(devImpl.getSystemLanguage()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.SystemLanguage, devImpl.getSystemLanguage());
                if(!TextUtils.isEmpty(devImpl.getSystemArea()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.SystemArea, devImpl.getSystemArea());
                if(!TextUtils.isEmpty(devImpl.getTimeZone()))
                deviceInfo.put(DeviceKeyContacts.DevInfo.TimeZone, devImpl.getTimeZone());
            }
            if(PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.SENSOR_SWITCH,EGContext.SWITCH_OF_SENSOR)){
                JSONArray senSorArray = SenSorModuleNameImpl.getInstance(mContext).getSensorInfo();
                if(senSorArray != null && senSorArray.length()>0){
                    deviceInfo.put(DeviceKeyContacts.DevInfo.SenSorModuleName,senSorArray);
                }
            }
            JSONObject batteryJson = new JSONObject();

            if (PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.BATTERY_SWITCH,EGContext.SWITCH_OF_BATTERY)) {
                BatteryModuleNameInfo battery = BatteryModuleNameInfo.getInstance();
                if(TextUtils.isEmpty(battery.getBatteryStatus()) && TextUtils.isEmpty(battery.getBatteryHealth()) &&
                        TextUtils.isEmpty(battery.getBatteryTechnology())){
                    String batteryInfo = PolicyImpl.getInstance(mContext).getSP().getString(DeviceKeyContacts.DevInfo.BatteryModuleName,"");
                    if(!TextUtils.isEmpty(batteryInfo)){
                        batteryJson = new JSONObject(batteryInfo);
                    }
                }else{
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
            }
            if (PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.DEV_FURTHER_DETAIL_SWITCH,EGContext.SWITCH_OF_DEV_FURTHER_DETAIL)) {
                if(!TextUtils.isEmpty(devImpl.getCPUModel()))
                batteryJson.put(DeviceKeyContacts.DevInfo.CPUModel, devImpl.getCPUModel());
                if(!TextUtils.isEmpty(devImpl.getBuildId()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildId, devImpl.getBuildId());
                if(!TextUtils.isEmpty(devImpl.getBuildDisplay()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildDisplay, devImpl.getBuildDisplay());
                if(!TextUtils.isEmpty(devImpl.getBuildProduct()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildProduct, devImpl.getBuildProduct());
                if(!TextUtils.isEmpty(devImpl.getBuildDevice()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildDevice, devImpl.getBuildDevice());
                if(!TextUtils.isEmpty(devImpl.getBuildBoard()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildBoard, devImpl.getBuildBoard());
                if(!TextUtils.isEmpty(devImpl.getBuildBootloader()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildBootloader, devImpl.getBuildBootloader());
                if(!TextUtils.isEmpty(devImpl.getBuildHardware()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildHardware, devImpl.getBuildHardware());
                if(!TextUtils.isEmpty(devImpl.getBuildSupportedAbis()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSupportedAbis, devImpl.getBuildSupportedAbis());
                if(!TextUtils.isEmpty(devImpl.getBuildSupportedAbis32()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSupportedAbis32, devImpl.getBuildSupportedAbis32());
                if(!TextUtils.isEmpty(devImpl.getBuildSupportedAbis64()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSupportedAbis64, devImpl.getBuildSupportedAbis64());
                if(!TextUtils.isEmpty(devImpl.getBuildType()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildType, devImpl.getBuildType());
                if(!TextUtils.isEmpty(devImpl.getBuildTags()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildTags, devImpl.getBuildTags());
                if(!TextUtils.isEmpty(devImpl.getBuildFingerPrint()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildFingerPrint, devImpl.getBuildFingerPrint());
                if(!TextUtils.isEmpty(devImpl.getBuildRadioVersion()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildRadioVersion, devImpl.getBuildRadioVersion());
                if(!TextUtils.isEmpty(devImpl.getBuildIncremental()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildIncremental, devImpl.getBuildIncremental());
                if(!TextUtils.isEmpty(devImpl.getBuildBaseOS()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildBaseOS, devImpl.getBuildBaseOS());
                if(!TextUtils.isEmpty(devImpl.getBuildSecurityPatch()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSecurityPatch, devImpl.getBuildSecurityPatch());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildSdkInt, devImpl.getBuildSdkInt());
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildPreviewSdkInt, devImpl.getBuildPreviewSdkInt());
                if(!TextUtils.isEmpty(devImpl.getBuildCodename()))
                batteryJson.put(DeviceKeyContacts.DevInfo.BuildCodename, devImpl.getBuildCodename());
                if(!TextUtils.isEmpty(devImpl.getIDFA()))
                batteryJson.put(DeviceKeyContacts.DevInfo.IDFA, devImpl.getIDFA());
                ELOG.i(batteryJson+"   ::::::::batteryJson batteryJson");
            }
            deviceInfo.put("ETDM", batteryJson);
        }catch (Throwable t){
            ELOG.e(t.getMessage()+" datapackaging has an exception.....");
        }
        return deviceInfo;
    }

    /**
     * OCCount
     * @return
     */
    public static JSONArray getOCInfo(Context ctx){
//         OCImpl.getInstance(ctx).getInfoByVersion();
         //TODO
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
