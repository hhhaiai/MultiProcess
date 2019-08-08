package com.analysys.track.internal.net;

import android.content.Context;
import android.os.Build;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.impl.DevStatusChecker;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.DoubleCardSupport;
import com.analysys.track.internal.impl.SenSorModuleNameImpl;
import com.analysys.track.internal.model.BatteryModuleNameInfo;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EguanIdUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.SystemUtils;

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
            if (!PolicyImpl.getInstance(context).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV,
                    true)) {
                return null;
            }
            DeviceImpl devImpl = DeviceImpl.getInstance(context);
            // JSONObject json, String key, String value,String SPKey
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.SystemName, EGContext.SDK_TYPE,
                    DataController.SWITCH_OF_SYSTEM_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.SystemVersion,
                    Build.VERSION.RELEASE, DataController.SWITCH_OF_SYSTEM_VERSION);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.DeviceBrand, Build.BRAND,
                    DataController.SWITCH_OF_DEVICE_BRAND);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.DeviceId, devImpl.getDeviceId(),
                    DataController.SWITCH_OF_DEVICE_ID);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.DeviceModel, Build.MODEL,
                    DataController.SWITCH_OF_DEVICE_MODEL);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.MAC, devImpl.getMac(),
                    DataController.SWITCH_OF_MAC);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.SerialNumber,
                    devImpl.getSerialNumber(), DataController.SWITCH_OF_SERIALNUMBER);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.Resolution, devImpl.getResolution(),
                    DataController.SWITCH_OF_RESOLUTION);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.DotPerInch, devImpl.getDotPerInch(),
                    DataController.SWITCH_OF_DOTPERINCH);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.MobileOperator,
                    devImpl.getMobileOperator(), DataController.SWITCH_OF_MOBILE_OPERATOR);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.MobileOperatorName,
                    devImpl.getMobileOperatorName(), DataController.SWITCH_OF_MOBILE_OPERATOR_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.NetworkOperatorCode,
                    devImpl.getNetworkOperatorCode(), DataController.SWITCH_OF_NETWORK_OPERATOR_CODE);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.NetworkOperatorName,
                    devImpl.getNetworkOperatorName(), DataController.SWITCH_OF_NETWORK_OPERATOR_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.Imeis,
                    DoubleCardSupport.getInstance().getIMEIS(context), DataController.SWITCH_OF_IMEIS);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.Imsis, DoubleCardSupport.getInstance().getIMSIS(context),
                    DataController.SWITCH_OF_IMSIS);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationChannel,
                    SystemUtils.getAppChannel(context), DataController.SWITCH_OF_APPLICATION_CHANNEL);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationKey,
                    SystemUtils.getAppKey(context), DataController.SWITCH_OF_APPLICATION_KEY);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationName,
                    devImpl.getApplicationName(), DataController.SWITCH_OF_APPLICATION_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.APILevel, String.valueOf(Build.VERSION.SDK_INT),
                    DataController.SWITCH_OF_APILEVEL);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationPackageName,
                    devImpl.getApplicationPackageName(), DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.SDKVersion, EGContext.SDK_VERSION,
                    DataController.SWITCH_OF_SDKVERSION);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationVersionCode,
                    devImpl.getApplicationVersionCode(), DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.AppMD5, devImpl.getAppMD5(),
                    DataController.SWITCH_OF_APP_MD5);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.AppSign, devImpl.getAppSign(),
                    DataController.SWITCH_OF_APP_SIGN);
            JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.TempID,
                    EguanIdUtils.getInstance(context).getId(), DataController.SWITCH_OF_TEMP_ID);

            if (PolicyImpl.getInstance(context).getValueFromSp(
                    DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV_CHECK,
                    DataController.SWITCH_OF_MODULE_CL_DEV_CHECK)) {
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.Simulator,
                        DevStatusChecker.getInstance().isSimulator(context) ? "1" : "0", DataController.SWITCH_OF_SIMULATOR);
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.Debug, DevStatusChecker.getInstance().isSelfDebugApp(context) ? "1" : "0",
                        DataController.SWITCH_OF_DEBUG);
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.Hijack, DevStatusChecker.getInstance().isHook(context) ? "1" : "0",
                        DataController.SWITCH_OF_HIJACK);
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.IsRoot, SystemUtils.isRooted() ? "1" : "0",
                        DataController.SWITCH_OF_IS_ROOT);
            }

            if (PolicyImpl.getInstance(context).getValueFromSp(
                    DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BLUETOOTH,
                    DataController.SWITCH_OF_MODULE_CL_BLUETOOTH)) {
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.BluetoothMac,
                        devImpl.getBluetoothAddress(context), DataController.SWITCH_OF_BLUETOOTH_MAC);
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.BluetoothName,
                        devImpl.getBluetoothName(), DataController.SWITCH_OF_BLUETOOTH_NAME);
            }
            if (PolicyImpl.getInstance(context).getValueFromSp(
                    DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_KEEP_INFO,
                    DataController.SWITCH_OF_MODULE_CL_KEEP_INFO)) {
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.SystemFontSize,
                        devImpl.getSystemFontSize(), DataController.SWITCH_OF_SYSTEM_FONT_SIZE);
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.SystemHour,
                        devImpl.getSystemHour(), DataController.SWITCH_OF_SYSTEM_HOUR);
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.SystemLanguage,
                        devImpl.getSystemLanguage(), DataController.SWITCH_OF_SYSTEM_LANGUAGE);
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.SystemArea,
                        devImpl.getSystemArea(), DataController.SWITCH_OF_SYSTEM_AREA);
                JsonUtils.pushToJSON(context, deviceInfo, DeviceKeyContacts.DevInfo.TimeZone, devImpl.getTimeZone(),
                        DataController.SWITCH_OF_TIMEZONE);
            }
            if (PolicyImpl.getInstance(context).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SENSOR,
                    DataController.SWITCH_OF_MODULE_CL_SENSOR)) {
                JSONArray senSorArray = SenSorModuleNameImpl.getInstance(context).getSensorInfo();
                if (senSorArray != null && senSorArray.length() > 0) {
                    deviceInfo.put(DeviceKeyContacts.DevInfo.SenSorModuleName, senSorArray);
                }
            }
            JSONObject batteryJson = new JSONObject();

            if (PolicyImpl.getInstance(context).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BATTERY,
                    DataController.SWITCH_OF_MODULE_CL_BATTERY)) {
                BatteryModuleNameInfo battery = BatteryModuleNameInfo.getInstance();
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BatteryStatus,
                        battery.getBatteryStatus(), DataController.SWITCH_OF_BATTERY_STATUS);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BatteryHealth,
                        battery.getBatteryHealth(), DataController.SWITCH_OF_BATTERY_HEALTH);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BatteryLevel,
                        battery.getBatteryLevel(), DataController.SWITCH_OF_BATTERY_LEVEL);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BatteryScale,
                        battery.getBatteryScale(), DataController.SWITCH_OF_BATTERY_SCALE);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BatteryPlugged,
                        battery.getBatteryPlugged(), DataController.SWITCH_OF_BATTERY_PLUGGED);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BatteryTechnology,
                        battery.getBatteryTechnology(), DataController.SWITCH_OF_BATTERY_TECHNOLOGY);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BatteryTemperature,
                        battery.getBatteryTemperature(), DataController.SWITCH_OF_BATTERY_TEMPERATURE);
            }
            if (PolicyImpl.getInstance(context).getValueFromSp(
                    DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_MORE_INFO,
                    DataController.SWITCH_OF_MODULE_CL_MORE_INFO)) {
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.CPUModel, String.format("%s:%s", Build.CPU_ABI, Build.CPU_ABI2),
                        DataController.SWITCH_OF_CPU_MODEL);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildId, Build.ID,
                        DataController.SWITCH_OF_BUILD_ID);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildDisplay,
                        Build.DISPLAY, DataController.SWITCH_OF_BUILD_DISPLAY);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildProduct,
                        Build.PRODUCT, DataController.SWITCH_OF_BUILD_PRODUCT);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildDevice,
                        Build.DEVICE, DataController.SWITCH_OF_BUILD_DEVICE);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildBoard,
                        Build.BOARD, DataController.SWITCH_OF_BUILD_BOARD);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildBootloader,
                        Build.BOOTLOADER, DataController.SWITCH_OF_BUILD_BOOT_LOADER);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildHardware,
                        Build.HARDWARE, DataController.SWITCH_OF_BUILD_HARDWARE);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis,
                        devImpl.getBuildSupportedAbis(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis32,
                        devImpl.getBuildSupportedAbis32(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_32);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis64,
                        devImpl.getBuildSupportedAbis64(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_64);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildType, Build.TYPE,
                        DataController.SWITCH_OF_BUILD_TYPE);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildTags, Build.TAGS,
                        DataController.SWITCH_OF_BUILD_TAGS);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildFingerPrint,
                        Build.FINGERPRINT, DataController.SWITCH_OF_BUILD_FINGER_PRINT);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildRadioVersion,
                        Build.getRadioVersion(), DataController.SWITCH_OF_BUILD_RADIO_VERSION);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildIncremental,
                        Build.VERSION.INCREMENTAL, DataController.SWITCH_OF_BUILD_INCREMENTAL);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildBaseOS,
                        Build.VERSION.BASE_OS, DataController.SWITCH_OF_BUILD_BASE_OS);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildSecurityPatch,
                        Build.VERSION.SECURITY_PATCH, DataController.SWITCH_OF_BUILD_SECURITY_PATCH);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildSdkInt,
                        String.valueOf(Build.VERSION.SDK_INT), DataController.SWITCH_OF_BUILD_SDK_INT);
                if (Build.VERSION.SDK_INT > 22) {
                    JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildPreviewSdkInt,
                            String.valueOf(Build.VERSION.PREVIEW_SDK_INT), DataController.SWITCH_OF_BUILD_PREVIEW_SDK_INT);
                }
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.BuildCodename,
                        Build.VERSION.CODENAME, DataController.SWITCH_OF_BUILD_CODE_NAME);
                JsonUtils.pushToJSON(context, batteryJson, DeviceKeyContacts.DevInfo.IDFA, devImpl.getIDFA(),
                        DataController.SWITCH_OF_BUILD_IDFA);
            }
            deviceInfo.put(EGContext.EXTRA_DATA, batteryJson);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
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
