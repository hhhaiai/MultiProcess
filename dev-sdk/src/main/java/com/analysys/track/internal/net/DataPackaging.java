package com.analysys.track.internal.net;

import android.content.Context;
import android.os.Build;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.content.UploadKey.DevInfo;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.sensors.SenSorModuleNameImpl;
import com.analysys.track.internal.model.BatteryModuleNameInfo;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DebugDev;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.reflectinon.EContextHelper;
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
            context = EContextHelper.getContext(context);
            if (!SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_DEV, true)) {
                return null;
            }
            DeviceImpl dImpl = DeviceImpl.getInstance(context);
            // JSONObject json, String key, String value,String SPKey
            JsonUtils.add(context, deviceInfo, DevInfo.SystemName, EGContext.SDK_TYPE, DataController.SWITCH_OF_SYSTEM_NAME);
            JsonUtils.add(context, deviceInfo, DevInfo.SystemVersion, Build.VERSION.RELEASE, DataController.SWITCH_OF_SYSTEM_VERSION);
            JsonUtils.add(context, deviceInfo, DevInfo.DeviceBrand, Build.BRAND, DataController.SWITCH_OF_DEVICE_BRAND);
            JsonUtils.add(context, deviceInfo, DevInfo.DeviceId, dImpl.getDeviceId(), DataController.SWITCH_OF_DEVICE_ID);
            JsonUtils.add(context, deviceInfo, DevInfo.OAID, dImpl.getOAID(), DataController.SWITCH_OF_OAID);
            // if (EGContext.patch_runing) {
            // String plocyVersion = SPHelper.getStringValueFromSP(context, EGContext.PATCH_VERSION_POLICY, "");
            // JsonUtils.pushToJSON(context, deviceInfo, DevInfo.POLICYVER, plocyVersion, DataController.SWITCH_OF_POLICYVER);
            // }
            JsonUtils.add(context, deviceInfo, DevInfo.DeviceModel, Build.MODEL, DataController.SWITCH_OF_DEVICE_MODEL);
            JsonUtils.add(context, deviceInfo, DevInfo.SerialNumber, SystemUtils.getSerialNumber(), DataController.SWITCH_OF_SERIALNUMBER);
            JsonUtils.add(context, deviceInfo, DevInfo.Resolution, dImpl.getResolution(), DataController.SWITCH_OF_RESOLUTION);
            JsonUtils.add(context, deviceInfo, DevInfo.DotPerInch, dImpl.getDotPerInch(), DataController.SWITCH_OF_DOTPERINCH);
            JsonUtils.add(context, deviceInfo, DevInfo.MobileOperator, dImpl.getMobileOperator(), DataController.SWITCH_OF_MOBILE_OPERATOR);
            JsonUtils.add(context, deviceInfo, DevInfo.MobileOperatorName, dImpl.getMobileOperatorName(), DataController.SWITCH_OF_MOBILE_OPERATOR_NAME);
            JsonUtils.add(context, deviceInfo, DevInfo.NetworkOperatorCode, dImpl.getNetworkOperatorCode(), DataController.SWITCH_OF_NETWORK_OPERATOR_CODE);
            JsonUtils.add(context, deviceInfo, DevInfo.NetworkOperatorName, dImpl.getNetworkOperatorName(), DataController.SWITCH_OF_NETWORK_OPERATOR_NAME);
            JsonUtils.add(context, deviceInfo, DevInfo.Imeis, DoubleCardSupport.getInstance().getIMEIS(context), DataController.SWITCH_OF_IMEIS);
            JsonUtils.add(context, deviceInfo, DevInfo.Imsis, DoubleCardSupport.getInstance().getIMSIS(context), DataController.SWITCH_OF_IMSIS);
            JsonUtils.add(context, deviceInfo, DevInfo.ApplicationChannel, SystemUtils.getAppChannel(context), DataController.SWITCH_OF_APPLICATION_CHANNEL);
            JsonUtils.add(context, deviceInfo, DevInfo.ApplicationKey, SystemUtils.getAppKey(context), DataController.SWITCH_OF_APPLICATION_KEY);
            JsonUtils.add(context, deviceInfo, DevInfo.ApplicationName, dImpl.getApplicationName(), DataController.SWITCH_OF_APPLICATION_NAME);
            JsonUtils.add(context, deviceInfo, DevInfo.APILevel, String.valueOf(Build.VERSION.SDK_INT), DataController.SWITCH_OF_APILEVEL);
            JsonUtils.add(context, deviceInfo, DevInfo.ApplicationPackageName, dImpl.getApplicationPackageName(), DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            JsonUtils.add(context, deviceInfo, DevInfo.SDKVersion, EGContext.SDK_VERSION, DataController.SWITCH_OF_SDKVERSION);
            JsonUtils.add(context, deviceInfo, DevInfo.ApplicationVersionCode, dImpl.getApplicationVersionCode(), DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
            JsonUtils.add(context, deviceInfo, DevInfo.AppMD5, dImpl.getAppMD5(), DataController.SWITCH_OF_APP_MD5);
            JsonUtils.add(context, deviceInfo, DevInfo.AppSign, dImpl.getAppSign(), DataController.SWITCH_OF_APP_SIGN);
//            JsonUtils.add(context, deviceInfo, DevInfo.TempID,
//                    EguanIdUtils.getInstance(context).getId(), DataController.SWITCH_OF_TEMP_ID);
            JsonUtils.add(context, deviceInfo, DevInfo.UA, dImpl.getUA(context), DataController.SWITCH_OF_BUILD_UA);
            //add targetSdkVersion in v4.4.0.2   by sanbo
            JsonUtils.add(context, deviceInfo, DevInfo.TargetSdkVersion, dImpl.getTargetSdkVersion(context), DataController.SWITCH_OF_TARGETSDKVERSION);


            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK, DataController.SWITCH_OF_MODULE_CL_DEV_CHECK)) {
                JsonUtils.add(context, deviceInfo, DevInfo.Simulator, DebugDev.get(context).isSimulator() ? "1" : "0", DataController.SWITCH_OF_SIMULATOR);
                JsonUtils.add(context, deviceInfo, DevInfo.Debug, DebugDev.get(context).isSelfAppDebugByFlag() ? "1" : "0", DataController.SWITCH_OF_DEBUG);
                JsonUtils.add(context, deviceInfo, DevInfo.Hijack, DebugDev.get(context).isHook() ? "1" : "0", DataController.SWITCH_OF_HIJACK);
                JsonUtils.add(context, deviceInfo, DevInfo.IsRoot, SystemUtils.isRooted() ? "1" : "0", DataController.SWITCH_OF_IS_ROOT);
            }


            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO, DataController.SWITCH_OF_MODULE_CL_KEEP_INFO)) {
                JsonUtils.add(context, deviceInfo, DevInfo.SystemFontSize, dImpl.getSystemFontSize(), DataController.SWITCH_OF_SYSTEM_FONT_SIZE);
                JsonUtils.add(context, deviceInfo, DevInfo.SystemHour, dImpl.getSystemHour(), DataController.SWITCH_OF_SYSTEM_HOUR);
                JsonUtils.add(context, deviceInfo, DevInfo.SystemLanguage, dImpl.getSystemLanguage(), DataController.SWITCH_OF_SYSTEM_LANGUAGE);
                JsonUtils.add(context, deviceInfo, DevInfo.SystemArea, dImpl.getSystemArea(), DataController.SWITCH_OF_SYSTEM_AREA);
                JsonUtils.add(context, deviceInfo, DevInfo.TimeZone, dImpl.getTimeZone(), DataController.SWITCH_OF_TIMEZONE);
            }

            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR, DataController.SWITCH_OF_MODULE_CL_SENSOR)) {
                JSONArray senSorArray = SenSorModuleNameImpl.getInstance(context).getSensorInfo();
                if (senSorArray != null && senSorArray.length() > 0) {
                    deviceInfo.put(DevInfo.SenSorModuleName, senSorArray);
                }
            }
            JSONObject batteryJson = new JSONObject();

            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY, DataController.SWITCH_OF_MODULE_CL_BATTERY)) {
                BatteryModuleNameInfo bInfo = BatteryModuleNameInfo.getInstance();
                JsonUtils.add(context, batteryJson, DevInfo.BatteryStatus, bInfo.getBatteryStatus(), DataController.SWITCH_OF_BATTERY_STATUS);
                JsonUtils.add(context, batteryJson, DevInfo.BatteryHealth, bInfo.getBatteryHealth(), DataController.SWITCH_OF_BATTERY_HEALTH);
                JsonUtils.add(context, batteryJson, DevInfo.BatteryLevel, bInfo.getBatteryLevel(), DataController.SWITCH_OF_BATTERY_LEVEL);
                JsonUtils.add(context, batteryJson, DevInfo.BatteryScale, bInfo.getBatteryScale(), DataController.SWITCH_OF_BATTERY_SCALE);
                JsonUtils.add(context, batteryJson, DevInfo.BatteryPlugged, bInfo.getBatteryPlugged(), DataController.SWITCH_OF_BATTERY_PLUGGED);
                JsonUtils.add(context, batteryJson, DevInfo.BatteryTechnology, bInfo.getBatteryTechnology(), DataController.SWITCH_OF_BATTERY_TECHNOLOGY);
                JsonUtils.add(context, batteryJson, DevInfo.BatteryTemperature, bInfo.getBatteryTemperature(), DataController.SWITCH_OF_BATTERY_TEMPERATURE);
            }

            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO, DataController.SWITCH_OF_MODULE_CL_MORE_INFO)) {
                JsonUtils.add(context, batteryJson, DevInfo.CPUModel, String.format("%s:%s", Build.CPU_ABI, Build.CPU_ABI2), DataController.SWITCH_OF_CPU_MODEL);
                JsonUtils.add(context, batteryJson, DevInfo.BuildId, Build.ID, DataController.SWITCH_OF_BUILD_ID);
                JsonUtils.add(context, batteryJson, DevInfo.BuildDisplay, Build.DISPLAY, DataController.SWITCH_OF_BUILD_DISPLAY);
                JsonUtils.add(context, batteryJson, DevInfo.BuildProduct, Build.PRODUCT, DataController.SWITCH_OF_BUILD_PRODUCT);
                JsonUtils.add(context, batteryJson, DevInfo.BuildDevice, Build.DEVICE, DataController.SWITCH_OF_BUILD_DEVICE);
                JsonUtils.add(context, batteryJson, DevInfo.BuildBoard, Build.BOARD, DataController.SWITCH_OF_BUILD_BOARD);
                JsonUtils.add(context, batteryJson, DevInfo.BuildBootloader, Build.BOOTLOADER, DataController.SWITCH_OF_BUILD_BOOT_LOADER);
                JsonUtils.add(context, batteryJson, DevInfo.BuildHardware, Build.HARDWARE, DataController.SWITCH_OF_BUILD_HARDWARE);
                JsonUtils.add(context, batteryJson, DevInfo.BuildSupportedAbis, dImpl.getBuildSupportedAbis(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS);
                JsonUtils.add(context, batteryJson, DevInfo.BuildSupportedAbis32, dImpl.getBuildSupportedAbis32(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_32);
                JsonUtils.add(context, batteryJson, DevInfo.BuildSupportedAbis64, dImpl.getBuildSupportedAbis64(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_64);
                JsonUtils.add(context, batteryJson, DevInfo.BuildType, Build.TYPE, DataController.SWITCH_OF_BUILD_TYPE);
                JsonUtils.add(context, batteryJson, DevInfo.BuildTags, Build.TAGS, DataController.SWITCH_OF_BUILD_TAGS);
                JsonUtils.add(context, batteryJson, DevInfo.BuildFingerPrint, Build.FINGERPRINT, DataController.SWITCH_OF_BUILD_FINGER_PRINT);
                JsonUtils.add(context, batteryJson, DevInfo.BuildRadioVersion, Build.getRadioVersion(), DataController.SWITCH_OF_BUILD_RADIO_VERSION);
                //Added in API level 1
                JsonUtils.add(context, batteryJson, DevInfo.BuildIncremental, Build.VERSION.INCREMENTAL, DataController.SWITCH_OF_BUILD_INCREMENTAL);

                if (Build.VERSION.SDK_INT > 22) {
                    JsonUtils.add(context, batteryJson, DevInfo.BuildBaseOS, Build.VERSION.BASE_OS, DataController.SWITCH_OF_BUILD_BASE_OS);
                    JsonUtils.add(context, batteryJson, DevInfo.BuildSecurityPatch, Build.VERSION.SECURITY_PATCH, DataController.SWITCH_OF_BUILD_SECURITY_PATCH);
                    JsonUtils.add(context, batteryJson, DevInfo.BuildPreviewSdkInt, String.valueOf(Build.VERSION.PREVIEW_SDK_INT), DataController.SWITCH_OF_BUILD_PREVIEW_SDK_INT);
                }

                //Added in API level 4
                JsonUtils.add(context, batteryJson, DevInfo.BuildSdkInt, String.valueOf(Build.VERSION.SDK_INT), DataController.SWITCH_OF_BUILD_SDK_INT);
                JsonUtils.add(context, batteryJson, DevInfo.BuildCodename, Build.VERSION.CODENAME, DataController.SWITCH_OF_BUILD_CODE_NAME);
                JsonUtils.add(context, batteryJson, DevInfo.IDFA, dImpl.getIDFA(), DataController.SWITCH_OF_BUILD_IDFA);
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
