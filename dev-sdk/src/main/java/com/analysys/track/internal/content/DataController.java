package com.analysys.track.internal.content;

/**
 * @Copyright 2019 sanbo Inc. All rights reserved.
 * @Description: 服务器策略控制.那部分不能上传那部分能上传
 * @Version: 1.0
 * @Create: 2019-08-04 17:36:20
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class DataController {
    /*
     * 模块的采集控制
     */
    // 电量采集.默认不上传，可控制上传
    public static boolean SWITCH_OF_MODULE_CL_BATTERY = true;
    // 传感器采集控制
    public static boolean SWITCH_OF_MODULE_CL_SENSOR = false;
    // 防作弊相关信息控制.默认上传,可控制不上传. 0不上传
    public static boolean SWITCH_OF_MODULE_CL_DEV_CHECK = true;
    // 系统阶段保持信息控制,比如语言、时区、小时制等. 默认不上传，可控制不上传
    public static boolean SWITCH_OF_MODULE_CL_KEEP_INFO = true;
    // 更多设备信息控制，手机中一些系统信息.默认不上传，可控制上传
    public static boolean SWITCH_OF_MODULE_CL_MORE_INFO = true;

    /*
     * 控制ProcessInfo模块的采集
     */
    public static boolean SWITCH_OF_CL_MODULE_PROC = true;
//    public static boolean SWITCH_OF_CL_MODULE_PS = true;
//    public static boolean SWITCH_OF_CL_MODULE_TOP = true;
//    public static boolean SWITCH_OF_CL_MODULE_RESULT = true;

    public static boolean SWITCH_OF_SYSTEM_NAME = true;
    public static boolean SWITCH_OF_SYSTEM_VERSION = true;
    public static boolean SWITCH_OF_DEVICE_BRAND = true;
    public static boolean SWITCH_OF_DEVICE_ID = true;
    public static boolean SWITCH_OF_OAID = true;
    public static boolean SWITCH_OF_POLICYVER = true;
    public static boolean SWITCH_OF_DEVICE_MODEL = true;
    public static boolean SWITCH_OF_SERIALNUMBER = true;
    public static boolean SWITCH_OF_RESOLUTION = true;
    public static boolean SWITCH_OF_DOTPERINCH = true;
    public static boolean SWITCH_OF_MOBILE_OPERATOR = true;
    public static boolean SWITCH_OF_MOBILE_OPERATOR_NAME = true;
    public static boolean SWITCH_OF_NETWORK_OPERATOR_CODE = true;
    public static boolean SWITCH_OF_NETWORK_OPERATOR_NAME = true;
    public static boolean SWITCH_OF_IMEIS = true;
    public static boolean SWITCH_OF_IMSIS = true;
    public static boolean SWITCH_OF_APPLICATION_CHANNEL = true;
    public static boolean SWITCH_OF_APPLICATION_KEY = true;
    public static boolean SWITCH_OF_APPLICATION_NAME = true;
    public static boolean SWITCH_OF_APILEVEL = true;
    public static boolean SWITCH_OF_APPLICATION_PACKAGE_NAME = true;
    public static boolean SWITCH_OF_SDKVERSION = true;
    public static boolean SWITCH_OF_APPLICATION_VERSION_CODE = true;
    public static boolean SWITCH_OF_NETWORK_TYPE = true;
    public static boolean SWITCH_OF_SWITCH_TYPE = true;
    public static boolean SWITCH_OF_APPLICATION_TYPE = true;
    public static boolean SWITCH_OF_COLLECTION_TYPE = true;
    public static boolean SWITCH_OF_RUNNING_TIME = true;

    public static boolean SWITCH_OF_APP_MD5 = true;
    public static boolean SWITCH_OF_APP_SIGN = true;
    //    public static boolean SWITCH_OF_EGUAN_ID = true;
    public static boolean SWITCH_OF_TEMP_ID = true;
    public static boolean SWITCH_OF_SIMULATOR = true;
    public static boolean SWITCH_OF_DEBUG = true;
    public static boolean SWITCH_OF_HIJACK = true;
    public static boolean SWITCH_OF_IS_ROOT = true;
    public static boolean SWITCH_OF_BATTERY_STATUS = true;
    public static boolean SWITCH_OF_BATTERY_HEALTH = true;
    public static boolean SWITCH_OF_BATTERY_LEVEL = true;
    public static boolean SWITCH_OF_BATTERY_SCALE = true;
    public static boolean SWITCH_OF_BATTERY_PLUGGED = true;
    public static boolean SWITCH_OF_BATTERY_TECHNOLOGY = true;
    public static boolean SWITCH_OF_BATTERY_TEMPERATURE = true;
    public static boolean SWITCH_OF_SENSOR_NAME = true;
    public static boolean SWITCH_OF_SENSOR_VERSION = true;
    public static boolean SWITCH_OF_SENSOR_MANUFACTURER = true;
    public static boolean SWITCH_OF_SENSOR_ID = true;
    public static boolean SWITCH_OF_SENSOR_WAKEUPSENSOR = true;
    public static boolean SWITCH_OF_SENSOR_POWER = true;
    public static boolean SWITCH_OF_SYSTEM_FONT_SIZE = true;
    public static boolean SWITCH_OF_SYSTEM_HOUR = true;
    public static boolean SWITCH_OF_SYSTEM_LANGUAGE = true;
    public static boolean SWITCH_OF_SYSTEM_AREA = true;
    public static boolean SWITCH_OF_TIMEZONE = true;
    //add targetSdkVersion in v4.4.0.2   by sanbo
    public static boolean SWITCH_OF_TARGETSDKVERSION = true;
    public static boolean SWITCH_OF_CPU_MODEL = true;
    public static boolean SWITCH_OF_BUILD_ID = true;
    public static boolean SWITCH_OF_BUILD_DISPLAY = true;
    public static boolean SWITCH_OF_BUILD_PRODUCT = true;
    public static boolean SWITCH_OF_BUILD_DEVICE = true;
    public static boolean SWITCH_OF_BUILD_BOARD = true;
    public static boolean SWITCH_OF_BUILD_BOOT_LOADER = true;
    public static boolean SWITCH_OF_BUILD_HARDWARE = true;
    public static boolean SWITCH_OF_BUILD_SUPPORTED_ABIS = true;
    public static boolean SWITCH_OF_BUILD_SUPPORTED_ABIS_32 = true;
    public static boolean SWITCH_OF_BUILD_SUPPORTED_ABIS_64 = true;
    public static boolean SWITCH_OF_BUILD_TYPE = true;
    public static boolean SWITCH_OF_BUILD_TAGS = true;
    public static boolean SWITCH_OF_BUILD_FINGER_PRINT = true;
    public static boolean SWITCH_OF_BUILD_RADIO_VERSION = true;
    public static boolean SWITCH_OF_BUILD_INCREMENTAL = true;
    public static boolean SWITCH_OF_BUILD_BASE_OS = true;
    public static boolean SWITCH_OF_BUILD_SECURITY_PATCH = true;
    public static boolean SWITCH_OF_BUILD_SDK_INT = true;
    public static boolean SWITCH_OF_BUILD_PREVIEW_SDK_INT = true;
    public static boolean SWITCH_OF_BUILD_CODE_NAME = true;
    public static boolean SWITCH_OF_BUILD_IDFA = true;
    public static boolean SWITCH_OF_BUILD_UA = true;
    public static boolean SWITCH_OF_APPLICATION_OPEN_TIME = true;
    public static boolean SWITCH_OF_APPLICATION_CLOSE_TIME = true;
    public static boolean SWITCH_OF_ACTION_TYPE = true;
    public static boolean SWITCH_OF_ACTION_HAPPEN_TIME = true;
    public static boolean SWITCH_OF_COLLECTION_TIME = true;
    public static boolean SWITCH_OF_GEOGRAPHY_LOCATION = true;
    public static boolean SWITCH_OF_WIFI_NAME = true;
    public static boolean SWITCH_OF_SSID = true;
    public static boolean SWITCH_OF_BSSID = true;
    public static boolean SWITCH_OF_LEVEL = true;// wifi信号强度
    public static boolean SWITCH_OF_CAPABILITIES = true;
    public static boolean SWITCH_OF_FREQUENCY = true;
    public static boolean SWITCH_OF_BS_NAME = true;
    public static boolean SWITCH_OF_LOCATION_AREA_CODE = true;
    public static boolean SWITCH_OF_CELL_ID = true;
    public static boolean SWITCH_OF_BS_LEVEL = true;// 基站信号强度
    public static boolean SWITCH_OF_BS_PCI = true;// PCI
    public static boolean SWITCH_OF_BS_RSRP = true;// RSRP
    public static boolean SWITCH_OF_BS_ECIO = true;// ECIO
    public static boolean SWITCH_OF_BS_RSRQ = true;// RSRQ
//    public static boolean SWITCH_OF_BS_CID_LIST = true;//cid
//    public static boolean SWITCH_OF_BS_LAC_LIST = true;//lac
//    public static boolean SWITCH_OF_BS_RSRP_LIST = true;//lac
//    public static boolean SWITCH_OF_BS_ECIO_LIST = true;//lac

}
