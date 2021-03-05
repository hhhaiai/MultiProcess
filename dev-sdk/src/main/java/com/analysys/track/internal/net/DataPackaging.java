package com.analysys.track.internal.net;

import android.content.Context;
import android.os.Build;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.sensors.SenSorModuleNameImpl;
import com.analysys.track.internal.model.BatteryModuleNameInfo;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DebugDev;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 存储、上传数据组装
 * @Version: 1.0
 * @Create: 2019-08-07 17:39:48
 * @author: ly
 */
public class DataPackaging {

    /**
     * 基础信息,公用接口,应用设备都使用 上行字段,DevInfo
     *
     * @param context
     * @return
     */
    public JSONObject getDevInfo(Context context) {
        JSONObject deviceInfo = new JSONObject();
        try {
//            if (!PolicyImpl.getInstance(context).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_DEV,
//                    true)) {
            if (!SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_DEV, true)) {

                return null;
            }
            DeviceImpl devImpl = DeviceImpl.getInstance(context);
            // JSONObject json, String key, String value,String SPKey
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.SystemName, EGContext.SDK_TYPE,
                    DataController.SWITCH_OF_SYSTEM_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.SystemVersion,
                    Build.VERSION.RELEASE, DataController.SWITCH_OF_SYSTEM_VERSION);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.DeviceBrand, Build.BRAND,
                    DataController.SWITCH_OF_DEVICE_BRAND);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.DeviceId, devImpl.getDeviceId(),
                    DataController.SWITCH_OF_DEVICE_ID);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.OAID, devImpl.getOAID(),
                    DataController.SWITCH_OF_OAID);
            // if (EGContext.patch_runing) {
                // String plocyVersion = SPHelper.getStringValueFromSP(context, EGContext.PATCH_VERSION_POLICY, "");
                // JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.POLICYVER, plocyVersion, DataController.SWITCH_OF_POLICYVER);
            // }
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.DeviceModel, Build.MODEL,
                    DataController.SWITCH_OF_DEVICE_MODEL);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.SerialNumber,
                    SystemUtils.getSerialNumber(), DataController.SWITCH_OF_SERIALNUMBER);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.Resolution, devImpl.getResolution(), DataController.SWITCH_OF_RESOLUTION);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.DotPerInch, devImpl.getDotPerInch(), DataController.SWITCH_OF_DOTPERINCH);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.MobileOperator,
                    devImpl.getMobileOperator(), DataController.SWITCH_OF_MOBILE_OPERATOR);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.MobileOperatorName,
                    devImpl.getMobileOperatorName(), DataController.SWITCH_OF_MOBILE_OPERATOR_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.NetworkOperatorCode,
                    devImpl.getNetworkOperatorCode(), DataController.SWITCH_OF_NETWORK_OPERATOR_CODE);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.NetworkOperatorName,
                    devImpl.getNetworkOperatorName(), DataController.SWITCH_OF_NETWORK_OPERATOR_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.Imeis,
                    DoubleCardSupport.getInstance().getIMEIS(context), DataController.SWITCH_OF_IMEIS);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.Imsis, DoubleCardSupport.getInstance().getIMSIS(context), DataController.SWITCH_OF_IMSIS);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.ApplicationChannel,
                    SystemUtils.getAppChannel(context), DataController.SWITCH_OF_APPLICATION_CHANNEL);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.ApplicationKey,
                    SystemUtils.getAppKey(context), DataController.SWITCH_OF_APPLICATION_KEY);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.ApplicationName,
                    devImpl.getApplicationName(), DataController.SWITCH_OF_APPLICATION_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.APILevel, String.valueOf(Build.VERSION.SDK_INT), DataController.SWITCH_OF_APILEVEL);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.ApplicationPackageName,
                    devImpl.getApplicationPackageName(), DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.SDKVersion, EGContext.SDK_VERSION,
                    DataController.SWITCH_OF_SDKVERSION);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.ApplicationVersionCode,
                    devImpl.getApplicationVersionCode(), DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.AppMD5, devImpl.getAppMD5(),
                    DataController.SWITCH_OF_APP_MD5);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.AppSign, devImpl.getAppSign(),
                    DataController.SWITCH_OF_APP_SIGN);
//            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.TempID,
//                    EguanIdUtils.getInstance(context).getId(), DataController.SWITCH_OF_TEMP_ID);
            JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.UA, devImpl.getUA(),
                    DataController.SWITCH_OF_BUILD_UA);


//            if (PolicyImpl.getInstance(context).getValueFromSp(
//                    UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK,
//                    DataController.SWITCH_OF_MODULE_CL_DEV_CHECK)) {
            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK, DataController.SWITCH_OF_MODULE_CL_DEV_CHECK)) {
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.Simulator,
                        DebugDev.get(context).isSimulator() ? "1" : "0", DataController.SWITCH_OF_SIMULATOR);
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.Debug, DebugDev.get(context).isSelfAppDebugByFlag() ? "1" : "0",
                        DataController.SWITCH_OF_DEBUG);
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.Hijack, DebugDev.get(context).isHook() ? "1" : "0",
                        DataController.SWITCH_OF_HIJACK);
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.IsRoot, SystemUtils.isRooted() ? "1" : "0",
                        DataController.SWITCH_OF_IS_ROOT);
            }

//            if (PolicyImpl.getInstance(context).getValueFromSp(
//                    UploadKey.Response.RES_POLICY_MODULE_CL_BLUETOOTH,
//                    DataController.SWITCH_OF_MODULE_CL_BLUETOOTH)) {

//            if (PolicyImpl.getInstance(context).getValueFromSp(
//                    UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO,
//                    DataController.SWITCH_OF_MODULE_CL_KEEP_INFO)) {

            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO, DataController.SWITCH_OF_MODULE_CL_KEEP_INFO)) {
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.SystemFontSize,
                        devImpl.getSystemFontSize(), DataController.SWITCH_OF_SYSTEM_FONT_SIZE);
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.SystemHour,
                        devImpl.getSystemHour(), DataController.SWITCH_OF_SYSTEM_HOUR);
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.SystemLanguage,
                        devImpl.getSystemLanguage(), DataController.SWITCH_OF_SYSTEM_LANGUAGE);
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.SystemArea,
                        devImpl.getSystemArea(), DataController.SWITCH_OF_SYSTEM_AREA);
                JsonUtils.pushToJSON(context, deviceInfo, UploadKey.DevInfo.TimeZone, devImpl.getTimeZone(),
                        DataController.SWITCH_OF_TIMEZONE);
            }
//            if (PolicyImpl.getInstance(context).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR,
//                    DataController.SWITCH_OF_MODULE_CL_SENSOR)) {

            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR, DataController.SWITCH_OF_MODULE_CL_SENSOR)) {
                JSONArray senSorArray = SenSorModuleNameImpl.getInstance(context).getSensorInfo();
                if (senSorArray != null && senSorArray.length() > 0) {
                    deviceInfo.put(UploadKey.DevInfo.SenSorModuleName, senSorArray);
                }
            }
            JSONObject batteryJson = new JSONObject();

//            if (PolicyImpl.getInstance(context).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY,
//                    DataController.SWITCH_OF_MODULE_CL_BATTERY)) {

            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY, DataController.SWITCH_OF_MODULE_CL_BATTERY)) {
                BatteryModuleNameInfo battery = BatteryModuleNameInfo.getInstance();
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BatteryStatus,
                        battery.getBatteryStatus(), DataController.SWITCH_OF_BATTERY_STATUS);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BatteryHealth,
                        battery.getBatteryHealth(), DataController.SWITCH_OF_BATTERY_HEALTH);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BatteryLevel,
                        battery.getBatteryLevel(), DataController.SWITCH_OF_BATTERY_LEVEL);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BatteryScale,
                        battery.getBatteryScale(), DataController.SWITCH_OF_BATTERY_SCALE);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BatteryPlugged,
                        battery.getBatteryPlugged(), DataController.SWITCH_OF_BATTERY_PLUGGED);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BatteryTechnology,
                        battery.getBatteryTechnology(), DataController.SWITCH_OF_BATTERY_TECHNOLOGY);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BatteryTemperature,
                        battery.getBatteryTemperature(), DataController.SWITCH_OF_BATTERY_TEMPERATURE);
            }
//            if (PolicyImpl.getInstance(context).getValueFromSp(
//                    UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO,
//                    DataController.SWITCH_OF_MODULE_CL_MORE_INFO)) {

            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO, DataController.SWITCH_OF_MODULE_CL_MORE_INFO)) {
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.CPUModel, String.format("%s:%s", Build.CPU_ABI, Build.CPU_ABI2),
                        DataController.SWITCH_OF_CPU_MODEL);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildId, Build.ID,
                        DataController.SWITCH_OF_BUILD_ID);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildDisplay,
                        Build.DISPLAY, DataController.SWITCH_OF_BUILD_DISPLAY);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildProduct,
                        Build.PRODUCT, DataController.SWITCH_OF_BUILD_PRODUCT);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildDevice,
                        Build.DEVICE, DataController.SWITCH_OF_BUILD_DEVICE);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildBoard,
                        Build.BOARD, DataController.SWITCH_OF_BUILD_BOARD);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildBootloader,
                        Build.BOOTLOADER, DataController.SWITCH_OF_BUILD_BOOT_LOADER);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildHardware,
                        Build.HARDWARE, DataController.SWITCH_OF_BUILD_HARDWARE);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildSupportedAbis,
                        devImpl.getBuildSupportedAbis(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildSupportedAbis32,
                        devImpl.getBuildSupportedAbis32(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_32);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildSupportedAbis64,
                        devImpl.getBuildSupportedAbis64(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_64);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildType, Build.TYPE,
                        DataController.SWITCH_OF_BUILD_TYPE);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildTags, Build.TAGS,
                        DataController.SWITCH_OF_BUILD_TAGS);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildFingerPrint,
                        Build.FINGERPRINT, DataController.SWITCH_OF_BUILD_FINGER_PRINT);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildRadioVersion,
                        Build.getRadioVersion(), DataController.SWITCH_OF_BUILD_RADIO_VERSION);
                //Added in API level 1
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildIncremental,
                        Build.VERSION.INCREMENTAL, DataController.SWITCH_OF_BUILD_INCREMENTAL);

                if (Build.VERSION.SDK_INT > 22) {
                    JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildBaseOS,
                            Build.VERSION.BASE_OS, DataController.SWITCH_OF_BUILD_BASE_OS);
                    JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildSecurityPatch,
                            Build.VERSION.SECURITY_PATCH, DataController.SWITCH_OF_BUILD_SECURITY_PATCH);
                    JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildPreviewSdkInt,
                            String.valueOf(Build.VERSION.PREVIEW_SDK_INT), DataController.SWITCH_OF_BUILD_PREVIEW_SDK_INT);
                }

                //Added in API level 4
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildSdkInt,
                        String.valueOf(Build.VERSION.SDK_INT), DataController.SWITCH_OF_BUILD_SDK_INT);
                //Added in API level 4
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.BuildCodename,
                        Build.VERSION.CODENAME, DataController.SWITCH_OF_BUILD_CODE_NAME);
                JsonUtils.pushToJSON(context, batteryJson, UploadKey.DevInfo.IDFA, devImpl.getIDFA(),
                        DataController.SWITCH_OF_BUILD_IDFA);
            }
            deviceInfo.put(EGContext.EXTRA_DATA, batteryJson);
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
        return deviceInfo;
    }

    private static class HOLDER {
        private static DataPackaging INSTANCE = new DataPackaging();
    }

    private DataPackaging() {
    }

    public static DataPackaging getInstance() {
        return HOLDER.INSTANCE;
    }

}
