package com.analysys.track.internal.impl.proc;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.impl.DeviceImpl;

import com.analysys.track.internal.impl.PolicyImpl;
import com.analysys.track.internal.impl.SenSorModuleNameImpl;
import com.analysys.track.model.BatteryModuleNameInfo;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.Utils;
import com.analysys.track.utils.sp.SPHelper;

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
            //JSONObject json, String key, String value,String SPKey
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemName, devImpl.getSystemName(),DataController.SWITCH_OF_SYSTEM_NAME);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemVersion, devImpl.getSystemVersion(),DataController.SWITCH_OF_SYSTEM_VERSION);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.DeviceBrand, devImpl.getDeviceBrand(),DataController.SWITCH_OF_DEVICE_BRAND);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.DeviceId, devImpl.getDeviceId(),DataController.SWITCH_OF_DEVICE_ID);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.DeviceModel, devImpl.getDeviceModel(),DataController.SWITCH_OF_DEVICE_MODEL);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.MAC, devImpl.getMac(),DataController.SWITCH_OF_MAC);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SerialNumber, devImpl.getSerialNumber(),DataController.SWITCH_OF_SERIALNUMBER);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Resolution, devImpl.getResolution(),DataController.SWITCH_OF_RESOLUTION);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.DotPerInch, devImpl.getDotPerInch(),DataController.SWITCH_OF_DOTPERINCH);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.MobileOperator, devImpl.getMobileOperator(),DataController.SWITCH_OF_MOBILE_OPERATOR);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.MobileOperatorName, devImpl.getMobileOperatorName(),DataController.SWITCH_OF_MOBILE_OPERATOR_NAME);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.NetworkOperatorCode, devImpl.getNetworkOperatorCode(),DataController.SWITCH_OF_NETWORK_OPERATOR_CODE);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.NetworkOperatorName, devImpl.getNetworkOperatorName(),DataController.SWITCH_OF_NETWORK_OPERATOR_NAME);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Imeis, devImpl.getIMEIS(),DataController.SWITCH_OF_IMEIS);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Imsis, devImpl.getIMSIS(),DataController.SWITCH_OF_IMSIS);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationChannel, devImpl.getApplicationChannel(),DataController.SWITCH_OF_APPLICATION_CHANNEL);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationKey, devImpl.getApplicationKey(),DataController.SWITCH_OF_APPLICATION_KEY);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationName, devImpl.getApplicationName(),DataController.SWITCH_OF_APPLICATION_NAME);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.APILevel, devImpl.getAPILevel(),DataController.SWITCH_OF_APILEVEL);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationPackageName, devImpl.getApplicationPackageName(),DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SDKVersion, devImpl.getSdkVersion(),DataController.SWITCH_OF_SDKVERSION);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationVersionCode, devImpl.getApplicationVersionCode(),DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.AppMD5, devImpl.getAppMD5(),DataController.SWITCH_OF_APP_MD5);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.AppSign, devImpl.getAppSign(),DataController.SWITCH_OF_APP_SIGN);
            Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.TempID, devImpl.getTempID(),DataController.SWITCH_OF_TEMP_ID);

            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV_CHECK,DataController.SWITCH_OF_MODULE_CL_DEV_CHECK)) {
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Simulator, devImpl.isSimulator(),DataController.SWITCH_OF_SIMULATOR);
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Debug, devImpl.getDebug(),DataController.SWITCH_OF_DEBUG);
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Hijack, devImpl.isHijack(),DataController.SWITCH_OF_HIJACK);
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.IsRoot, devImpl.IsRoot(),DataController.SWITCH_OF_IS_ROOT);
            }

            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BLUETOOTH,DataController.SWITCH_OF_MODULE_CL_BLUETOOTH)) {
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.BluetoothMac, devImpl.getBluetoothMac(),DataController.SWITCH_OF_BLUETOOTH_MAC);
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.BluetoothName, devImpl.getBluetoothName(),DataController.SWITCH_OF_BLUETOOTH_NAME);
            }
            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_KEEP_INFO,DataController.SWITCH_OF_MODULE_CL_KEEP_INFO)) {
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemFontSize, devImpl.getSystemFontSize(),DataController.SWITCH_OF_SYSTEM_FONT_SIZE);
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemHour, devImpl.getSystemHour(),DataController.SWITCH_OF_SYSTEM_HOUR);
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemLanguage, devImpl.getSystemLanguage(),DataController.SWITCH_OF_SYSTEM_LANGUAGE);
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemArea, devImpl.getSystemArea(),DataController.SWITCH_OF_SYSTEM_AREA);
                Utils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.TimeZone, devImpl.getTimeZone(),DataController.SWITCH_OF_TIMEZONE);
            }
            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SENSOR,DataController.SWITCH_OF_MODULE_CL_SENSOR)){
                JSONArray senSorArray = SenSorModuleNameImpl.getInstance(mContext).getSensorInfo();
                if(senSorArray != null && senSorArray.length()>0){
                    deviceInfo.put(DeviceKeyContacts.DevInfo.SenSorModuleName,senSorArray);
                }
            }
            JSONObject batteryJson = new JSONObject();

            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BATTERY,DataController.SWITCH_OF_MODULE_CL_BATTERY)) {
                BatteryModuleNameInfo battery = BatteryModuleNameInfo.getInstance();
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryStatus, battery.getBatteryStatus(),DataController.SWITCH_OF_BATTERY_STATUS);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryHealth, battery.getBatteryHealth(),DataController.SWITCH_OF_BATTERY_HEALTH);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryLevel, battery.getBatteryLevel(),DataController.SWITCH_OF_BATTERY_LEVEL);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryScale, battery.getBatteryScale(),DataController.SWITCH_OF_BATTERY_SCALE);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryPlugged, battery.getBatteryPlugged(),DataController.SWITCH_OF_BATTERY_PLUGGED);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryTechnology, battery.getBatteryTechnology(),DataController.SWITCH_OF_BATTERY_TECHNOLOGY);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryTemperature, battery.getBatteryTemperature(),DataController.SWITCH_OF_BATTERY_TEMPERATURE);
            }
            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_MORE_INFO,DataController.SWITCH_OF_MODULE_CL_MORE_INFO)) {
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.CPUModel, devImpl.getCPUModel(),DataController.SWITCH_OF_CPU_MODEL);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildId, devImpl.getBuildId(),DataController.SWITCH_OF_BUILD_ID);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildDisplay, devImpl.getBuildDisplay(),DataController.SWITCH_OF_BUILD_DISPLAY);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildProduct, devImpl.getBuildProduct(),DataController.SWITCH_OF_BUILD_PRODUCT);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildDevice, devImpl.getBuildDevice(),DataController.SWITCH_OF_BUILD_DEVICE);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildBoard, devImpl.getBuildBoard(),DataController.SWITCH_OF_BUILD_BOARD);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildBootloader, devImpl.getBuildBootloader(),DataController.SWITCH_OF_BUILD_BOOT_LOADER);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildHardware, devImpl.getBuildHardware(),DataController.SWITCH_OF_BUILD_HARDWARE);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis, devImpl.getBuildSupportedAbis(),DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis32, devImpl.getBuildSupportedAbis32(),DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_32);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis64, devImpl.getBuildSupportedAbis64(),DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_64);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildType, devImpl.getBuildType(),DataController.SWITCH_OF_BUILD_TYPE);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildTags, devImpl.getBuildTags(),DataController.SWITCH_OF_BUILD_TAGS);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildFingerPrint, devImpl.getBuildFingerPrint(),DataController.SWITCH_OF_BUILD_FINGER_PRINT);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildRadioVersion, devImpl.getBuildRadioVersion(),DataController.SWITCH_OF_BUILD_RADIO_VERSION);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildIncremental, devImpl.getBuildIncremental(),DataController.SWITCH_OF_BUILD_INCREMENTAL);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildBaseOS, devImpl.getBuildBaseOS(),DataController.SWITCH_OF_BUILD_BASE_OS);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSecurityPatch, devImpl.getBuildSecurityPatch(),DataController.SWITCH_OF_BUILD_SECURITY_PATCH);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSdkInt, devImpl.getBuildSdkInt(),DataController.SWITCH_OF_BUILD_SDK_INT);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildPreviewSdkInt, devImpl.getBuildPreviewSdkInt(),DataController.SWITCH_OF_BUILD_PREVIEW_SDK_INT);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildCodename, devImpl.getBuildCodename(),DataController.SWITCH_OF_BUILD_CODE_NAME);
                Utils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.IDFA, devImpl.getIDFA(),DataController.SWITCH_OF_BUILD_IDFA);
//                ELOG.i(batteryJson+"   ::::::::batteryJson batteryJson");
            }
            deviceInfo.put(EGContext.EXTRA_DATA, batteryJson);
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
