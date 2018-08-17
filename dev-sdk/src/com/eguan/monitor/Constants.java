package com.eguan.monitor;


public class Constants {

    //    public static String RT_APP_URL = "";
    //    public static String NORMAL_APP_URL = "";
    public static String NORMAL_DEVIER_URL = "";
    public static String RT_DEVIER_URL = "";

    /**
     * 测试情况下，设置为true; 正常出包， 设为false。
     */
    public static boolean FLAG_DEBUG_INNER = true;

    /**
     * 运营统计分析与用户画像内部版本与发版日期 及整体版本号
     */
    public static final String SDK_VERSION = "3.7.9.3|20180709";

    public final static String URL_SCHEME = "http://";

    /**
     * 实时上传端口
     */
    public final static String RT_PORT = ":8099";
    /**
     * 测试回传接口.Debug模式
     */
    public final static String TEST_CALLBACK_PORT = ":10031";
    /**
     * 非实时上传端口
     */
    public final static String ORI_PORT = ":8089";

    /**
     * 实时域名
     */
    public static final String RT_DOMAIN_NAME = "rt101.analysys.cn";


//    public static final String USERTP_URL = URL_SCHEME + RT_DOMAIN_NAME + RT_PORT;
    /**
     * 测试域名
     */
    public static final String TEST_CALLBACK_DOMAIN_NAME = "apptest.analysys.cn";

    /**
     * 非实时上传是,使用的域名池,以ait开始的为应用上传接口;以urd开始的为设备上传接口
     */
    public final static String[] NORMAL_UPLOAD_URL = {
            "urd103.analysys.cn",// 0
            "urd240.analysys.cn",// 1
            "urd183.analysys.cn",// 2
            "urd409.analysys.cn",// 3
            "urd203.analysys.cn",// 4
            "urd490.analysys.cn",// 5
            "urd609.analysys.cn",// 6
            "urd301.analysys.cn",// 7
            "urd405.analysys.cn",// 8
            "urd025.analysys.cn",// 9
            "urd339.analysys.cn"// 头部应用 用作测试
    };
//    public final static String[] NORMAL_UPLOAD_URL = {"ait103.analysys.cn",
//            "urd103.analysys.cn",// 0
//            "ait240.analysys.cn", "urd240.analysys.cn",// 1
//            "ait183.analysys.cn", "urd183.analysys.cn",// 2
//            "ait409.analysys.cn", "urd409.analysys.cn",// 3
//            "ait203.analysys.cn", "urd203.analysys.cn",// 4
//            "ait490.analysys.cn", "urd490.analysys.cn",// 5
//            "ait609.analysys.cn", "urd609.analysys.cn",// 6
//            "ait301.analysys.cn", "urd301.analysys.cn",// 7
//            "ait405.analysys.cn", "urd405.analysys.cn",// 8
//            "ait025.analysys.cn", "urd025.analysys.cn",// 9
//            "ait339.analysys.cn", "urd339.analysys.cn"// 头部应用 用作测试
//    };

    /**
     * 3.7.6.3
     * 正常模式下, 也使用实时模式
     */
    public final static String[] RT_UPLOAD_URL = {
            "rtait103.analysys.cn", "rturd103.analysys.cn",
            "rtait240.analysys.cn", "rturd240.analysys.cn",
            "rtait183.analysys.cn", "rturd183.analysys.cn",
            "rtait409.analysys.cn", "rturd409.analysys.cn",
            "rtait203.analysys.cn", "rturd203.analysys.cn",
            "rtait490.analysys.cn", "rturd490.analysys.cn",
            "rtait609.analysys.cn", "rturd609.analysys.cn",
            "rtait301.analysys.cn", "rturd301.analysys.cn",
            "rtait405.analysys.cn", "rturd405.analysys.cn",
            "rtait025.analysys.cn", "rturd025.analysys.cn",
            "rtait339.analysys.cn", "rturd339.analysys.cn"
    };

    /**
     * 实时计算接口
     */
    public static final String RT_URL = URL_SCHEME + RT_DOMAIN_NAME + RT_PORT;

    /**
     * 测试回传接口
     */
    public static final String TEST_CALLBACK_URL = URL_SCHEME + TEST_CALLBACK_DOMAIN_NAME + TEST_CALLBACK_PORT;
//    public static final String TEST_URL = URL_SCHEME + TEST_CALLBACK_DOMAIN_NAME + TEST_CALLBACK_PORT;
    /**
     * 非实时计算接口
     * 不需要了,正常接口,参考使用<p>APP_URL</p><p>DEVIER_URL</p>
     */
    public static String DEVIER_URL = null;


    /*------------------------------------ 综合版SDK-----------------------------------*/

    /**
     * 使用广播的方式获取IP,GL,NT信息
     */
    public static final String GL_ACTION = "com.eguan.monitor.ACTION_GL";
    public static final String NT_ACTION = "com.eguan.monitor.ACTION_NT";


    /**
     * 系统名称
     */
    public static final String SN = "Android";


    /* ---Manifest位置信息--- */
    public static String LI = "getLocationInfo";
    /* 数据加密KEY */
    public static final String DEVICE_UPLOAD_KEY = "facility2";
    //    public static final String APP_UPLOAD_KEY = "app3";
    //    public static final String NOTIFICATION_ACTION = "com.android.notificationReceiver";
    /*--------------------设备监测SDK 2.0.1常量字段------------------*/
    public static final String REQUEST_STATE = "request_state";
    /* alarm broadcast */
    public static final String ACTION_ALARM_TIMER = "com.android.eguan.drivermonitor";
    public static final String SPUTIL = "sputil";
    public static final String APP_KEY = "monitor_app_key";
    public static final String APP_CHANNEL = "monitor_app_channel";
    public static final String LASTOPENTIME = "lastOpenTime";
    public static final String LASTAPPNAME = "lastAppName";
    public static final String LASTAPPVERSION = "lastAppVersion";
    public static final String APPJSON = "appJson";
    public static final String LASTPACKAGENAME = "lastPackageName";
    public static final String NETTYPE = "netType";
    public static final String LASTQUESTTIME = "lastQuestTime";
    public static final String LONGTIME = "longTime";
    public static final String DIVINFOJSON = "divInfoJson";
    public static final String ALLAPPFORUNINSTALL = "allAppForUninstall";
    public static final String ENCODE = "utf-8";
    public static final String MONITORSERVICE = "com.eguan.monitor.fangzhou.service.MonitorService";// 监测的目标服务名
    public static final String USERCHANNEL = "channelValue";
    public static final String USERKEY = "keyValue";
    public static final int JOB_ID = 2071111;
    public static final String FAILEDNUMBER = "uploadFailedNumber";
    public static final String FAILEDTIME = "uploadFailedTime";
    public static final String RETRYTIME = "RetryIntervalTime";
    public static final String LOCATIONINFO = "LocationManifest";
    public static final String DEBUGURL = "debugUrl";
    public static final String INTERVALTIME = "TimerIntervalTime";
    public static final String APPLIST = "getAppListTime";
    public static final String APPPROCESS = "AppProcess";
    public static final String APPPROCESSTIME = "getAppProcessTime";
    public static final String WBG_INFO = "WBGInfo";
    public static final String DATATIME = "ProcessLifecycle";
    public static final String STARTTIME = "ProcessStartTime";
    public static final String ENDTIME = "ProcessEndTime";
    public static final String GETGPSTIME = "LocationInfoTime";
    public static final String LASTLOCATION = "LastLocation";
    public static final String EGUANID = "EguanIdKey";
    public static final String TMPID = "TmpIdKey";
    public static final String NETWORK = "NetworkState";
    public static final String NETWORK_INFO = "NetworkInfo";
    public static final String DEVICE_TACTICS = "DeviceTactics";
    public static final String TACTICS_STATE = "1";
    public static final String NET_IP_TAG = "policy";
    public static final String POLICY_VER = "policyVer";
    public static final String MERGE_INTERVAL = "mergeInterval";
    public static final String MIN_DURATION = "minDuration";
    public static final String MAX_DURATION = "maxDuration";
    public static final String APP_TYPE = "applicationType";
    public static final String APP_UPDATE = "AppUpdate";

//    public static final String APP_TACTICS = "AppTactics";
//    public static final String UPDATE_EGUSER = "UpdateEGUserTag";
//    public static final String DEVICE_DOMAIN_TAG = "DeviceDomainTag";
//    public static final String FIRST_START = "firstStart";
//    public static final String APP = "app";
//    public static final String DEVICE = "dev";
//    public static final String FILE_NAME = "e.g";
//    public static final String DATASERVICE = "com.eguan.monitor.fangzhou.service.DataService";// 数据服务名

    /**
     * ST字段 ：存放OC切换原因 1（正常使用） 2（开关屏幕切换）3（服务重启）
     */
    public static final String APP_SWITCH = "1";
    public static final String CLOSE_SCREEN = "2";
    public static final String SERVCICE_RESTART = "3";

    //    public final static long INTERVAL_TIME = 30 * 1000;
    //    public static final long HEBDOMAD = 7 * 24 * 60 * 60 * 1000;
    public static final long RETRY_TIME = 60 * 60 * 1000;// 上传重试时间间隔
    public static final int DATA_NUMBER = 20;// 上传条数
    public static final int SHORT_TIME = 5 * 1000;// 计时器时间间隔毫秒数
    public static final long LONG_INVALIED_TIME = 6 * 60 * 60 * 1000;// 上传数据时间间隔
    public static final long MINDISTANCE = 1000;// 地理位置信息获取距离/米
    public static final long GETAPPLIST = 24 * 60 * 60 * 1000;// 应用列表获取时间差/24小时
    public static final String SYSTEMNAME = "Android";
    public static final long SPAN_TIME = 30 * 1000;
    public static final long GIT_DATA_TIME_INTERVAL = 30 * 60 * 1000;//获取数据的时间间隔
    public static final long JOB_SERVICE_TIME = 10 * 1000;//jobservice判断时间间隔
    public static final long LONGEST_TIME = 5 * 60 * 60 * 1000;
    public static final long TIME_INTERVAL = 10 * 1000;
    public static final String QIFAN_ENVIRONMENT_HEADER = "PRO";
    public static final String QIFAN_TEST_ENVIRONMENT = "QF37";
    public static String APP_KEY_VALUE = "";
    public static String APP_CHANNEL_VALUE = "";


    //默认使用debug模式
    static {
        if (FLAG_DEBUG_INNER) {
            DEVIER_URL = TEST_CALLBACK_URL;
        } else {
            DEVIER_URL = RT_DEVIER_URL;
        }
    }

    public static void setNormalUploadUrl() {
        int sum = 0;
        for (int i = 0; i < APP_KEY_VALUE.length(); i++) {
            sum = sum + APP_KEY_VALUE.charAt(i);
        }
        int index = sum % 10;
        //update
//        NORMAL_DEVIER_URL = URL_SCHEME + NORMAL_UPLOAD_URL[index * 2 + 1] + ORI_PORT;
        NORMAL_DEVIER_URL = URL_SCHEME + NORMAL_UPLOAD_URL[index * 2] + ORI_PORT;
    }

    public static void setRTLUploadUrl() {
        int sum = 0;
        for (int i = 0; i < APP_KEY_VALUE.length(); i++) {
            sum = sum + APP_KEY_VALUE.charAt(i);
        }
        int index = sum % 10;
//        RT_APP_URL = URL_SCHEME + RT_UPLOAD_URL[index * 2] + ORI_PORT;
        RT_DEVIER_URL = URL_SCHEME + RT_UPLOAD_URL[index * 2 + 1] + ORI_PORT;
    }

    public static void changeUrlNormal(boolean debug) {
        if (debug) {
            DEVIER_URL = TEST_CALLBACK_URL;
        } else {
            DEVIER_URL = RT_DEVIER_URL;
        }
    }
}
