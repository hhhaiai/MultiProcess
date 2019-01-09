package com.analysys.dev.internal.Content;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 变量持有类
 * @Version: 1.0
 * @Create: 2018年9月3日 下午2:40:40
 * @Author: sanbo
 */
public class EGContext {

    /**
     * EGuan 内部调试控制. 主要用于控制堆栈打印、错误打印、内部提示信息打印
     */
    public static final boolean FLAG_DEBUG_INNER = true;
    /**
     * 用户debug控制
     */
    public static boolean FLAG_DEBUG_USER = true;
    /**
     * 是否展示广告通知。授权后，服务高版本可以切换成前台服务
     */
    public static boolean FLAG_SHOW_NOTIFY = true;

    /**
     * SDK版本
     */
    public static final String SDK_VERSION = "4.0.1";

    public static final String LOGTAG_DEBUG = "xxx";

    /**
     * xml 中声明的 appid、channel
     */
    public static final String XML_METADATA_APPKEY = "ANALYSYS_APPKEY";
    public static final String XML_METADATA_CHANNEL = "ANALYSYS_CHANNEL";

    public static final String SERVICE_NAME = "com.analysys.dev.service.AnalysysService";

    // 应用列表获取周期时间
    public static final int SNAPSHOT_CYCLE = 30 * 60 * 1000;
    // 位置获取周期时间
    public static final int LOCATION_CYCLE = 5 * 60 * 1000;
    // 应用打开关闭获取周期时间
    public static final int OC_CYCLE = 5 * 1000;
    // 应用打开关闭获取周期时间
    public static final int UPLOAD_CYCLE = 6 * 60 * 60 * 1000;
    // 心跳检查
    public static final int CHECK_HEARTBEAT_CYCLE = 15 * 1000;

    public static final String SP_APP_KEY = "appKey";
    public static final String SP_APP_CHANNEL = "appKey";

    public static final String SP_SNAPSHOT_TIME = "getSnapshotTime";

    public static final String SP_WIFI = "wifi";
    public static final String SP_WIFI_DETAIL = "wifiDetail";

    public static final String SP_BASE_STATION = "baseStation";
    public static final String SP_LOCATION = "location";

    public static final String SP_LOCATION_TIME = "getLocationTime";
    public static final String SP_MAC_ADDRESS = "MACAddress";

    public class LOGINFO {
        public static final String LOG_NOT_APPKEY = "please check you appkey!";
    }
    public static final String APPSNAPSHOT_PROC_SYNC_NAME = "install.txt";
    public static final String SP_APP_IDFA = "appIDFA";


    //防作弊相关信息开关，默认不上传，可控制上传
    public static boolean SWITCH_OF_PREVENT_CHEATING = false;
    //蓝牙信息，默认不上传，需要根据服务器控制
    public static boolean SWITCH_OF_BLUETOOTH = false;
    //电量信息，默认不上传，需要根据服务器控制
    public static boolean SWITCH_OF_BATTERY = false;
    //更加详细的设备详情信息，默认可不上传，可用于确定设备信息
    public static boolean SWITCH_OF_DEV_FURTHER_DETAIL= false;
    //系统阶段保持信息，默认可不上传，根据服务器控制来上传
    public static boolean SWITCH_OF_SYSTEM_INFO= false;
}
