package com.analysys.track.internal.Content;

public class EGContext {

    /**
     * EGuan 内部调试控制. 主要用于控制堆栈打印、错误打印、内部提示信息打印
     */
    public static final boolean FLAG_DEBUG_INNER = true;
    /**
     * 用户debug控制
     */
    public static boolean FLAG_DEBUG_USER = false;
    /**
     * SDK版本
     */
    public static final String SDK_VERSION = "4.3.0|20190225";

    public static final String LOGTAG_DEBUG = "xxx";
    public static final String USER_TAG_DEBUG = "analysys";
    /**
     * xml 中声明的 appid、channel
     */
    public static final String XML_METADATA_APPKEY = "ANALYSYS_APPKEY";
    public static final String XML_METADATA_CHANNEL = "ANALYSYS_CHANNEL";

    public static final String SERVICE_NAME = "AnalysysService";

    // 应用列表获取周期时间,3个小时
    public static final int SNAPSHOT_CYCLE = 3 * 60 * 60 * 1000;
    public static final String SP_SNAPSHOT_CYCLE = "SP_SNAPSHOT_CYCLE";
    // 位置获取周期时间,30min
    public static final int LOCATION_CYCLE = 30 * 60* 1000;
    public static final String SP_LOCATION_CYCLE = "SP_LOCATION_CYCLE";
    // 应用打开关闭获取周期时间
    public static final int OC_CYCLE = 5 * 1000;
    public static final String SP_OC_CYCLE = "SP_OC_CYCLE";
    // 5.0以上30s
    public static final int OC_CYCLE_OVER_5 = 30 * 1000;

    // 应用打开关闭获取周期时间
    public static final int UPLOAD_CYCLE = 6 * 60 * 60 * 1000;
    // 心跳检查
    public static final int CHECK_HEARTBEAT_CYCLE = 5 * 1000;
    public static final String HEARTBEAT_LAST_TIME = "HEART_BETA";
    // 发送失败后重复发送的心跳检查
    public static final int CHECK_RETRY_CYCLE = 5 * 1000;

    public static final String SP_APP_KEY = "appKey";
    public static final String SP_APP_CHANNEL = "appChannel";

    public static String APP_KEY_VALUE = "";
    public static String APP_CHANNEL_VALUE = "";
    public static String EGUAN_CHANNEL_PREFIX = "EGUAN_CHANNEL_";

//    public static final String SP_LOCATION_TIME = "getLocationTime";
    public static final String SP_MAC_ADDRESS = "MACAddress";


    public static final String SP_APP_IDFA = "appIDFA";

    // 蓝牙
    public static final String BLUETOOTH = "0";
    // 电量
    public static final String BATTERY = "1";
    // 传感器
    public static final String SENSOR = "2";
    // 系统阶段保持信息
    public static final String SYSTEM_INFO = "3";
    // 更加详细的设备详情信息
    public static final String DEV_FURTHER_DETAIL = "4";
    // 防作弊相关信息开关
    public static final String PREVENT_CHEATING = "5";
    // TIME信息
    public static final String XXX_TIME = "6";
    // OCR信息
    public static final String OCR = "7";
    // PROC信息
    public static final String PROC = "8";

    public static final int OC_COLLECTION_TYPE_RUNNING_TASK = 1;// getRunningTask
    public static final int OC_COLLECTION_TYPE_PROC = 2;// 读取proc
    public static final int OC_COLLECTION_TYPE_AUX = 3;// 辅助功能
    public static final int OC_COLLECTION_TYPE_SYSTEM = 4;// 系统统计

    public static final String SNAP_SHOT_INSTALL = "0";
    public static final String SNAP_SHOT_UNINSTALL = "1";
    public static final String SNAP_SHOT_UPDATE = "2";

    public static final String NETWORK_TYPE_2G = "2G";
    public static final String NETWORK_TYPE_3G = "3G";
    public static final String NETWORK_TYPE_4G = "4G";
    public static final String NETWORK_TYPE_WIFI = "WIFI";
    public static final String NETWORK_TYPE_NO_NET = "无网络";

    public static final String SP_NAME = "eg_policy";
    public static final String LAST_LOCATION = "last_location";
    public static final String TMPID = "tmp_id";
    public static final int SERVER_DELAY_DEFAULT = 0;
    public static final int FAIL_COUNT_DEFALUT = 3;//上传重试次数，默认3次
    public static final long FAIL_TRY_DELAY_DEFALUT = 60000;// 上传重试时间间隔默认60-70s
    public static final int TIMER_INTERVAL_DEFALUT = 5 * 1000;

    public static final long MINDISTANCE = 1000;// 地理位置信息获取距离/米

    public static String APP_URL = "";

    public final static String URL_SCHEME = "http://";
    /**
     * 实时上传端口
     */
    public final static String RT_PORT = ":8099";
    /**
     * 非实时上传端口
     */
    public final static String ORI_PORT = ":8089";
    /**
     * 测试回传接口.Debug模式
     */
    public final static String TEST_CALLBACK_PORT = ":8089";
    /**
     * 实时域名
     */
    public static final String RT_DOMAIN_NAME = "rt101.analysys.cn";
    public static final String USERTP_URL = URL_SCHEME + RT_DOMAIN_NAME + RT_PORT;
    /**
     * 测试域名
     */
    // public static final String TEST_CALLBACK_DOMAIN_NAME = "192.168.220.167";
    public static final String TEST_CALLBACK_DOMAIN_NAME = "192.168.8.150";
    /**
     * 非实时上传是,使用的域名池,以urd开始的为设备上传接口
     */
    public final static String[] NORMAL_UPLOAD_URL =
            { "urd103.analysys.cn", // 0
              "urd240.analysys.cn", // 1
               "urd183.analysys.cn", // 2
               "urd409.analysys.cn", // 3
               "urd203.analysys.cn", // 4
               "urd490.analysys.cn", // 5
               "urd609.analysys.cn", // 6
               "urd301.analysys.cn", // 7
               "urd405.analysys.cn", // 8
               "urd025.analysys.cn", // 9
               "urd339.analysys.cn"// 头部应用 用作测试
            };
    /**
     * 实时计算接口
     */
    public static final String RT_URL = URL_SCHEME + RT_DOMAIN_NAME + RT_PORT;
    /**
     * 测试回传接口
     */
    public static final String TEST_CALLBACK_URL =
            URL_SCHEME + TEST_CALLBACK_DOMAIN_NAME + TEST_CALLBACK_PORT;
    public static final String TEST_URL =
            URL_SCHEME + TEST_CALLBACK_DOMAIN_NAME + TEST_CALLBACK_PORT;
    public static String NORMAL_APP_URL = EGContext.URL_SCHEME + EGContext.NORMAL_UPLOAD_URL[0] + EGContext.ORI_PORT;
    public static final String APP_URL_SP = "app_url_sp";

    public static final String ORIGINKEY_STRING = "analysys";
    public static final String EGUANFILE = "eg.a";
    public static final String EGIDKEY = "egid";
    public static final String TMPIDKEY = "tmpid";

    // 用于jobservice
    public static final int JOB_ID = 2071111;
    // jobservice判断时间间隔
    public static final long JOB_SERVICE_TIME = 10 * 1000;

    public static final int TIME_OUT_TIME = 30 * 1000; // 设置为30秒
    public static final String SDKV = "SDKV";
    public static final String DEBUG = "DEBUG";
    public static final String APPKEY = "AK";
    public static final String TIME = "TIME";
    public static final String  POLICYVER= "policyVer";
    public static final String  PRO= "PRO";
    public static final String  PRO_KEY_WORDS= "QF4";

    // HTTP Status-Code 413: Request Entity Too Large
    public static final String HTTP_DATA_OVERLOAD = "413";
    // 200 SUCCESS
    public static final String HTTP_SUCCESS = "200";
    // 500 RETRY
    public static final String HTTP_RETRY = "500";
    // int default 值
    public static final int DEFAULT = 0;

    // OCINFO
    public static final String LAST_APP_NAME = "lastAppName";
    public static final String LAST_PACKAGE_NAME = "lastPackageName";
    public static final String END_TIME = "ProcessEndTime";
    public static final String LAST_APP_VERSION = "lastAppVersion";
    public static final String LAST_OPEN_TIME = "lastOpenTime";
    public static final String LAST_AVAILABLE_TIME = "lastAvailableTime";
//    public static final String MIN_DURATION_TIME = "minDuration";
//    public static final String MAX_DURATION_TIME = "maxDuration";
    public static final String APP_TYPE = "applicationType";
    public static final int SHORT_TIME = 5 * 1000;// 计时器时间间隔毫秒数
    public static final long LONGEST_TIME = 5 * 60 * 60 * 1000;
    public static final long DEFAULT_SPACE_TIME = 30 * 1000;//默认开关屏时间间隔在30s以上，才算一次有效的时间闭合事件
    public static final String CLOSE_SCREEN = "2";
    public static final String APP_SWITCH = "1";
    public static final String SERVICE_RESTART = "3";
    public static final String NORMAL = "0";
    public static final String THREAD_NAME = "com.eguan";
//    public static long HEARTBEAT_LAST_TIME_STMP = -1;
//    public static long SNAPSHOT_LAST_TIME_STMP = -1;
//    public static long LOCATION_LAST_TIME_STMP = -1;
//    public static long OC_LAST_TIME_STMP = -1;
//    public static long UPLOAD_LAST_TIME_STMP = -1;
    public static final String UPLOAD_KEY_WORDS = "facility4";
    public static final String EXTRA_DATA = "ETDM";
    public static final int BLANK_COUNT_MAX = 10;


    /************************************************************************************/
    /*********************************** 多进程同步 *****************************************/
    /************************************************************************************/
    // SDK发送同步文件,首次SDK初始化时创建
    public static final String FILES_SYNC_UPLOAD = "SNET.TAG";
    // SDK发送同步文件两次间隔时间，同时只有一个进程工作,默认6个小时，两次间隔5小时58分
    public static final long TIME_SYNC_UPLOAD = 6 * 60 * 60 * 1000;
    // SDK应用列表更新间隔,同时只有一个进程工作,首次SDK初始化时创建,涉及广播，5秒监听就行
    public static final String FILES_SYNC_APPSNAPSHOT = "SAP.TAG";
    public static final String FILES_SYNC_OC = "OCS.TAG";
    // OC 5+同步时间,同时只有一个进程工作
    public static final long TIME_SYNC_OC_OVER_5 = 30 * 1000;
    // 位置信息,通进程只有一个工作,两次间隔29分钟
    public static final String FILES_SYNC_LOCATION = "LCT.TAG";
    public static final long TIME_SYNC_LOCATION = 30 * 60 * 1000;
    // 数据库写入，多进程只有一个写入,两次间隔5秒
    public static final String FILES_SYNC_SP_WRITER = "SP.TAG";
    public static final long TIME_SYNC_SP = 5 * 1000;//最少间隔5s查询一次
    // 默认同步写入时间，5秒内能写入一次。 间隔范围: APP列表(多进程广播方面)、 OC 5.x以下、DB写入
    public static final long TIME_SYNC_DEFAULT = 5 * 1000;
    public static final String PERMISSION_TIME = "LOCATION_PERMISSION";
    public static final String PERMISSION_COUNT = "LOCATION_COUNT";
    public static final long LEN_MAX_UPDATE_SIZE = 1 * 1024 * 1024;
    public static final String INTERVALTIME = "TimerIntervalTime";
    public static final String LASTQUESTTIME = "lastQuestTime";
    public static final String RETRYTIME = "RetryIntervalTime";//重试间隔时间
    public static final String FAILEDNUMBER = "uploadFailedNumber";//本地已经重试并失败，次数
    public static final String FAILEDTIME = "uploadFailedTime";
    public static final String REQUEST_STATE = "request_state";
    // 设备内SDK发送 进程同步文件。首次SDK初始化时创建
    public static final String DEV_UPLOAD_PROC_NAME = "tmp";
    public static final int sPrepare = 0;
    public static final int sBeginResuest = 1;

    //上传模块
    public static final String MODULE_OC = "M_OC";
    public static final String MODULE_SNAPSHOT = "M_SNAP";
    public static final String MODULE_LOCATION = "M_LOC";
    public static final String MODULE_WIFI = "M_WIFI";
    public static final String MODULE_BASE = "M_BASE";
    public static final String MODULE_DEV = "M_DEV";
    public static final String MODULE_XXX = "M_XXX";
    /**
     * 控制android8以后是否后台启动服务。提示通知
     */
    public static boolean IS_SHOW_NOTIFITION = false;
    public static boolean SCREEN_ON = true;
    public static final String SPUTIL = "sptrack";

}
