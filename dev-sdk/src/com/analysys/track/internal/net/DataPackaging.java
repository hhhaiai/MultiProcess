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
 * 存储、上传数据组装
 */
public class DataPackaging {
    /**
     * 基础信息,公用接口,应用设备都使用 上行字段,DevInfo
     *
     * @return
     */
    public static JSONObject getDevInfo(Context mContext) {
        JSONObject deviceInfo = new JSONObject();
        try {
            if (!PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV,
                    true)) {
                return null;
            }
            DeviceImpl devImpl = DeviceImpl.getInstance(mContext);
            // JSONObject json, String key, String value,String SPKey
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemName, devImpl.getSystemName(),
                    DataController.SWITCH_OF_SYSTEM_NAME);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemVersion,
                    devImpl.getSystemVersion(), DataController.SWITCH_OF_SYSTEM_VERSION);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.DeviceBrand, devImpl.getDeviceBrand(),
                    DataController.SWITCH_OF_DEVICE_BRAND);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.DeviceId, devImpl.getDeviceId(),
                    DataController.SWITCH_OF_DEVICE_ID);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.DeviceModel, devImpl.getDeviceModel(),
                    DataController.SWITCH_OF_DEVICE_MODEL);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.MAC, devImpl.getMac(),
                    DataController.SWITCH_OF_MAC);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SerialNumber,
                    devImpl.getSerialNumber(), DataController.SWITCH_OF_SERIALNUMBER);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Resolution, devImpl.getResolution(),
                    DataController.SWITCH_OF_RESOLUTION);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.DotPerInch, devImpl.getDotPerInch(),
                    DataController.SWITCH_OF_DOTPERINCH);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.MobileOperator,
                    devImpl.getMobileOperator(), DataController.SWITCH_OF_MOBILE_OPERATOR);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.MobileOperatorName,
                    devImpl.getMobileOperatorName(), DataController.SWITCH_OF_MOBILE_OPERATOR_NAME);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.NetworkOperatorCode,
                    devImpl.getNetworkOperatorCode(), DataController.SWITCH_OF_NETWORK_OPERATOR_CODE);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.NetworkOperatorName,
                    devImpl.getNetworkOperatorName(), DataController.SWITCH_OF_NETWORK_OPERATOR_NAME);
            try {
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Imeis,
                        DoubleCardSupport.getIMEIS(mContext), DataController.SWITCH_OF_IMEIS);
            } catch (Throwable t) {
            }
            try {
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Imsis, devImpl.getIMSIS(mContext),
                        DataController.SWITCH_OF_IMSIS);
            } catch (Throwable t) {
            }
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationChannel,
                    SystemUtils.getAppChannel(mContext), DataController.SWITCH_OF_APPLICATION_CHANNEL);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationKey,
                    SystemUtils.getAppKey(mContext), DataController.SWITCH_OF_APPLICATION_KEY);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationName,
                    devImpl.getApplicationName(), DataController.SWITCH_OF_APPLICATION_NAME);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.APILevel, String.valueOf(Build.VERSION.SDK_INT),
                    DataController.SWITCH_OF_APILEVEL);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationPackageName,
                    devImpl.getApplicationPackageName(), DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SDKVersion, devImpl.getSdkVersion(),
                    DataController.SWITCH_OF_SDKVERSION);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.ApplicationVersionCode,
                    devImpl.getApplicationVersionCode(), DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.AppMD5, devImpl.getAppMD5(),
                    DataController.SWITCH_OF_APP_MD5);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.AppSign, devImpl.getAppSign(),
                    DataController.SWITCH_OF_APP_SIGN);
            JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.TempID,
                    EguanIdUtils.getInstance(mContext).getId(), DataController.SWITCH_OF_TEMP_ID);

            if (PolicyImpl.getInstance(mContext).getValueFromSp(
                    DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV_CHECK,
                    DataController.SWITCH_OF_MODULE_CL_DEV_CHECK)) {
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Simulator,
                        DevStatusChecker.getInstance().isSimulator(mContext) ? "1" : "0", DataController.SWITCH_OF_SIMULATOR);
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Debug, DevStatusChecker.getInstance().isSelfDebugApp(mContext) ? "1" : "0",
                        DataController.SWITCH_OF_DEBUG);
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.Hijack, DevStatusChecker.getInstance().isHook(mContext) ? "1" : "0",
                        DataController.SWITCH_OF_HIJACK);
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.IsRoot, SystemUtils.isRooted() ? "1" : "0",
                        DataController.SWITCH_OF_IS_ROOT);
            }

            if (PolicyImpl.getInstance(mContext).getValueFromSp(
                    DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BLUETOOTH,
                    DataController.SWITCH_OF_MODULE_CL_BLUETOOTH)) {
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.BluetoothMac,
                        devImpl.getBluetoothAddress(mContext), DataController.SWITCH_OF_BLUETOOTH_MAC);
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.BluetoothName,
                        devImpl.getBluetoothName(), DataController.SWITCH_OF_BLUETOOTH_NAME);
            }
            if (PolicyImpl.getInstance(mContext).getValueFromSp(
                    DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_KEEP_INFO,
                    DataController.SWITCH_OF_MODULE_CL_KEEP_INFO)) {
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemFontSize,
                        devImpl.getSystemFontSize(), DataController.SWITCH_OF_SYSTEM_FONT_SIZE);
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemHour,
                        devImpl.getSystemHour(), DataController.SWITCH_OF_SYSTEM_HOUR);
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemLanguage,
                        devImpl.getSystemLanguage(), DataController.SWITCH_OF_SYSTEM_LANGUAGE);
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.SystemArea,
                        devImpl.getSystemArea(), DataController.SWITCH_OF_SYSTEM_AREA);
                JsonUtils.pushToJSON(mContext, deviceInfo, DeviceKeyContacts.DevInfo.TimeZone, devImpl.getTimeZone(),
                        DataController.SWITCH_OF_TIMEZONE);
            }
            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SENSOR,
                    DataController.SWITCH_OF_MODULE_CL_SENSOR)) {
                JSONArray senSorArray = SenSorModuleNameImpl.getInstance(mContext).getSensorInfo();
                if (senSorArray != null && senSorArray.length() > 0) {
                    deviceInfo.put(DeviceKeyContacts.DevInfo.SenSorModuleName, senSorArray);
                }
            }
            JSONObject batteryJson = new JSONObject();

            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BATTERY,
                    DataController.SWITCH_OF_MODULE_CL_BATTERY)) {
                BatteryModuleNameInfo battery = BatteryModuleNameInfo.getInstance();
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryStatus,
                        battery.getBatteryStatus(), DataController.SWITCH_OF_BATTERY_STATUS);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryHealth,
                        battery.getBatteryHealth(), DataController.SWITCH_OF_BATTERY_HEALTH);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryLevel,
                        battery.getBatteryLevel(), DataController.SWITCH_OF_BATTERY_LEVEL);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryScale,
                        battery.getBatteryScale(), DataController.SWITCH_OF_BATTERY_SCALE);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryPlugged,
                        battery.getBatteryPlugged(), DataController.SWITCH_OF_BATTERY_PLUGGED);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryTechnology,
                        battery.getBatteryTechnology(), DataController.SWITCH_OF_BATTERY_TECHNOLOGY);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BatteryTemperature,
                        battery.getBatteryTemperature(), DataController.SWITCH_OF_BATTERY_TEMPERATURE);
            }
            if (PolicyImpl.getInstance(mContext).getValueFromSp(
                    DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_MORE_INFO,
                    DataController.SWITCH_OF_MODULE_CL_MORE_INFO)) {
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.CPUModel, String.format("%s:%s",Build.CPU_ABI,Build.CPU_ABI2),
                        DataController.SWITCH_OF_CPU_MODEL);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildId, Build.ID,
                        DataController.SWITCH_OF_BUILD_ID);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildDisplay,
                        Build.DISPLAY, DataController.SWITCH_OF_BUILD_DISPLAY);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildProduct,
                        Build.PRODUCT, DataController.SWITCH_OF_BUILD_PRODUCT);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildDevice,
                        Build.DEVICE, DataController.SWITCH_OF_BUILD_DEVICE);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildBoard,
                        Build.BOARD, DataController.SWITCH_OF_BUILD_BOARD);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildBootloader,
                        Build.BOOTLOADER, DataController.SWITCH_OF_BUILD_BOOT_LOADER);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildHardware,
                        Build.HARDWARE, DataController.SWITCH_OF_BUILD_HARDWARE);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis,
                        devImpl.getBuildSupportedAbis(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis32,
                        devImpl.getBuildSupportedAbis32(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_32);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSupportedAbis64,
                        devImpl.getBuildSupportedAbis64(), DataController.SWITCH_OF_BUILD_SUPPORTED_ABIS_64);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildType, Build.TYPE,
                        DataController.SWITCH_OF_BUILD_TYPE);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildTags, Build.TAGS,
                        DataController.SWITCH_OF_BUILD_TAGS);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildFingerPrint,
                        Build.FINGERPRINT, DataController.SWITCH_OF_BUILD_FINGER_PRINT);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildRadioVersion,
                        Build.getRadioVersion(), DataController.SWITCH_OF_BUILD_RADIO_VERSION);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildIncremental,
                        Build.VERSION.INCREMENTAL, DataController.SWITCH_OF_BUILD_INCREMENTAL);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildBaseOS,
                        Build.VERSION.BASE_OS, DataController.SWITCH_OF_BUILD_BASE_OS);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSecurityPatch,
                        Build.VERSION.SECURITY_PATCH, DataController.SWITCH_OF_BUILD_SECURITY_PATCH);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildSdkInt,
                        String.valueOf(Build.VERSION.SDK_INT), DataController.SWITCH_OF_BUILD_SDK_INT);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildPreviewSdkInt,
                        String.valueOf(Build.VERSION.PREVIEW_SDK_INT), DataController.SWITCH_OF_BUILD_PREVIEW_SDK_INT);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.BuildCodename,
                        Build.VERSION.CODENAME, DataController.SWITCH_OF_BUILD_CODE_NAME);
                JsonUtils.pushToJSON(mContext, batteryJson, DeviceKeyContacts.DevInfo.IDFA, devImpl.getIDFA(),
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
}
