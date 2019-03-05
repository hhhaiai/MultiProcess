package com.analysys.track.internal.Content;

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
    // public static boolean FLAG_SHOW_NOTIFY = true;

    /**
     * SDK版本
     */
    public static final String SDK_VERSION = "4.3.0|20190225";

    public static final String LOGTAG_DEBUG = "xxx";

    /**
     * xml 中声明的 appid、channel
     */
    public static final String XML_METADATA_APPKEY = "ANALYSYS_APPKEY";
    public static final String XML_METADATA_CHANNEL = "ANALYSYS_CHANNEL";

    public static final String SERVICE_NAME = "AnalysysService";

    // 应用列表获取周期时间,30min
    public static final int SNAPSHOT_CYCLE = 30 * 60 * 1000;
    public static final String SNAPSHOT_LAST_TIME = "SNAP_SHOT";
    // 位置获取周期时间
    public static final int LOCATION_CYCLE = 30 * 60 * 1000;
    public static final String LOCATION_LAST_TIME = "LOCATION";
    // 应用打开关闭获取周期时间
    public static final int OC_CYCLE = 5 * 1000;
    public static final String OC_LAST_TIME = "OC";
    // 5.0以上30s
    public static final int OC_CYCLE_OVER_5 = 30 * 1000;
    public static final String OC_LAST_TIME_OVER_5 = "OC_OVER_5";

    // 应用打开关闭获取周期时间
//    public static final int UPLOAD_CYCLE = 6 * 60 * 60 * 1000;
    public static final String UPLOAD_LAST_TIME = "UPLOAD";
    // 心跳检查
    public static final int CHECK_HEARTBEAT_CYCLE = 15 * 1000;
    public static final String HEARTBEAT_LAST_TIME = "HEART_BETA";

    // delay时常
    public static final int DELAY_TIME_CHECK = 20 * 1000;

    public static final String SP_APP_KEY = "appKey";
    public static final String SP_APP_CHANNEL = "appChannel";

    public static final String SP_SNAPSHOT_TIME = "getSnapshotTime";

    public static String APP_KEY_VALUE = "";
    public static String APP_CHANNEL_VALUE = "";
    public static String EGUAN_CHANNEL_PREFIX = "EGUAN_CHANNEL_";
    public static final String SP_LOCATION = "location";

    public static final String SP_LOCATION_TIME = "getLocationTime";
    public static final String SP_MAC_ADDRESS = "MACAddress";

    public static final String SP_DAEMON_TIME = "getDaemonTime";

    public static final String SP_APP_IDFA = "appIDFA";
    public static final String SWITCH_TYPE_DEFAULT = "1";

    // 蓝牙信息，默认不上传，需要根据服务器控制
    public static boolean SWITCH_OF_BLUETOOTH = true;
    // 蓝牙信息
    public static String BLUETOOTH_SWITCH = "BLUETOOTH";
    // 蓝牙
    public static final String BLUETOOTH = "0";
    // 电量信息，默认不上传，需要根据服务器控制
    public static boolean SWITCH_OF_BATTERY = true;
    // 电量信息
    public static String BATTERY_SWITCH = "BATTERY";
    // 电量
    public static final String BATTERY = "1";
    // 传感器，默认不上传，可控制上传
    public static boolean SWITCH_OF_SENSOR = true;
    // 传感器开关
    public static String SENSOR_SWITCH = "SENSOR";
    // 传感器
    public static final String SENSOR = "2";
    // 系统阶段保持信息，默认可不上传，根据服务器控制来上传
    public static boolean SWITCH_OF_SYSTEM_INFO = true;
    // 系统阶段保持信息
    public static String SYSTEM_INFO_SWITCH = "SYSTEM_INFO";
    // 系统阶段保持信息
    public static final String SYSTEM_INFO = "3";
    // 更加详细的设备详情信息，默认可不上传，可用于确定设备信息
    public static boolean SWITCH_OF_DEV_FURTHER_DETAIL = true;
    // 更加详细的设备详情信息
    public static String DEV_FURTHER_DETAIL_SWITCH = "DEV_FURTHER_DETAIL";
    // 更加详细的设备详情信息
    public static final String DEV_FURTHER_DETAIL = "4";
    // 防作弊相关信息开关，默认不上传，可控制上传
    public static boolean SWITCH_OF_PREVENT_CHEATING = true;
    // 防作弊相关信息开关
    public static String PREVENT_CHEATING_SWITCH = "PREVENT_CHEATING";
    // 防作弊相关信息开关
    public static final String PREVENT_CHEATING = "5";
    // TOP，默认上传，需要根据服务器控制
    public static boolean SWITCH_OF_TOP = true;
    // TOP信息
    public static String TOP_SWITCH = "TOP";
    // TOP信息
    public static final String TOP = "6";
    // PS，默认上传，需要根据服务器控制
    public static boolean SWITCH_OF_PS = true;
    // PS信息
    public static String PS_SWITCH = "PS";
    // PS信息
    public static final String PS = "7";
    // PROC，默认上传，可用于确定设备信息
    public static boolean SWITCH_OF_PROC = true;
    // PROC信息
    public static String PROC_SWITCH = "PROC";
    // PROC信息
    public static final String PROC = "8";

    public static int OC_COLLECTION_TYPE_RUNNING_TASK = 1;// getRunningTask
    public static int OC_COLLECTION_TYPE_PROC = 2;// 读取proc
    public static int OC_COLLECTION_TYPE_AUX = 3;// 辅助功能
    public static int OC_COLLECTION_TYPE_SYSTEM = 4;// 系统统计

    public static String SNAP_SHOT_INSTALL = "0";
    public static String SNAP_SHOT_UNINSTALL = "1";
    public static String SNAP_SHOT_UPDATE = "2";

    public static String NETWORK_TYPE_2G = "2G";
    public static String NETWORK_TYPE_3G = "3G";
    public static String NETWORK_TYPE_4G = "4G";
    public static String NETWORK_TYPE_WIFI = "WIFI";
    public static String NETWORK_TYPE_NO_NET = "无网络";

    public static final String SP_NAME = "eg_policy";
    public static final String LASTLOCATION = "LastLocation";
    public static final String POLICY_VER_DEFALUT = "";
    public static final long SERVER_DELAY_DEFAULT = 0L;
    public static final int FAIL_COUNT_DEFALUT = 5;
    public static final long FAIL_TRY_DELAY_DEFALUT = 60 * 60 * 1000;
    public static final int TIMER_INTERVAL_DEFALUT = 5 * 1000;
    public static final int TIMER_INTERVAL_DEFALUT_60 = 60 * 1000;
    public static final int EVENT_COUNT_DEFALUT = 10;
    public static final int USER_RTP_DEFALUT = 1;
    public static final int USER_RTL_DEFAULT = 1;
    public static final int UPLOAD_SD_DEFALUT = 1;
    public static final int REMOTE_IP = 0;
    public static final int MERGE_INTERVAL = 30 * 60 * 60 * 1000;// TODO 需确认
    public static final int MIN_DURATION = 60 * 1000;// TODO 需确认
    public static final int MAX_DURATION = 5 * 60 * 1000;// TODO 需确认
    public static final int DOMAIN_UPDATE_TIMES = 1;// TODO 需确认
    public static final long PERMIT_FOR_FAIL_TIME_DEFALUT = 0;
    private static final int PERMIT_FOR_SERVER_TIME_DEFALUT = 0;
    public static final long MINDISTANCE = 1000;// 地理位置信息获取距离/米

    public static String APP_URL = null;
    public static String DEVIER_URL = null;

    // /**
    // * 运营统计分析与用户画像内部版本与发版日期 及整体版本号
    // */
    // public static final String SDK_VERSION = "3.7.9.3|20181228";
    public final static String URL_SCHEME = "http://";
    /**
     * 实时上传端口
     */
    public final static String RT_PORT = ":8099";
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
     * 非实时上传是,使用的域名池,以ait开始的为应用上传接口;以urd开始的为设备上传接口
     */
    public final static String[] NORMAL_UPLOAD_URL =
            {"ait103.analysys.cn", "urd103.analysys.cn", // 0
                    "ait240.analysys.cn", "urd240.analysys.cn", // 1
                    "ait183.analysys.cn", "urd183.analysys.cn", // 2
                    "ait409.analysys.cn", "urd409.analysys.cn", // 3
                    "ait203.analysys.cn", "urd203.analysys.cn", // 4
                    "ait490.analysys.cn", "urd490.analysys.cn", // 5
                    "ait609.analysys.cn", "urd609.analysys.cn", // 6
                    "ait301.analysys.cn", "urd301.analysys.cn", // 7
                    "ait405.analysys.cn", "urd405.analysys.cn", // 8
                    "ait025.analysys.cn", "urd025.analysys.cn", // 9
                    "ait339.analysys.cn", "urd339.analysys.cn"// 头部应用 用作测试

                    // TODO ait不用
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
    public static String RT_APP_URL = "";
    public static String RT_DEVIER_URL = "";
    public static String NORMAL_APP_URL = "";
    public static String NORMAL_DEVIER_URL = "";

    public static String PERMIT_FOR_FAIL_TIME = "policy_fail_time";

    public static String POLICY_SERVICE_PULL_VER = "servicePullVer";
    public static String USERKEY = "keyValue";
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
    public static final String POLICYVER = "policyVer";
    public static final String PRO = "PRO";

    // HTTP Status-Code 413: Request Entity Too Large
    public static final String HTTP_DATA_OVERLOAD = "413";
    // 200 SUCCESS
    public static final String HTTP_SUCCESS = "200";
    // 500 RETRY
    public static final String HTTP_RETRY = "500";
    // int default 值
    public static final int DEFAULT = 0;

    // OCINFO
    public static final String LASTAPPNAME = "lastAppName";
    public static final String LASTPACKAGENAME = "lastPackageName";
    public static final String ENDTIME = "ProcessEndTime";
    public static final String LASTAPPVERSION = "lastAppVersion";
    public static final String LASTOPENTIME = "lastOpenTime";
    public static final String MIN_DURATION_TIME = "minDuration";
    public static final String MAX_DURATION_TIME = "maxDuration";
    public static final String APP_TYPE = "applicationType";
    public static final int SHORT_TIME = 5 * 1000;// 计时器时间间隔毫秒数
    public static final long LONGEST_TIME = 5 * 60 * 60 * 1000;
    public static final String CLOSE_SCREEN = "2";
    public static final String APP_SWITCH = "1";
    public static final String SERVCICE_RESTART = "3";

    /**
     * @Copyright © 2019 analysys Inc. All rights reserved.
     * @Description: 日志详情类
     * @Version: 1.0
     * @Create: Mar 4, 2019 9:52:15 PM
     * @Author: sanbo
     */
    public class LOGINFO {
        public static final String LOG_NOT_APPKEY = "please check you appkey!";
    }

    /************************************************************************************/
    /*********************************** 多进程同步 *****************************************/
    /************************************************************************************/
    // SDK发送同步文件,首次SDK初始化时创建
    public static final String FILES_SYNC_UPLOAD = "SNET.TAG";
    // SDK发送同步文件两次间隔时间，同时只有一个进程工作,默认6个小时，两次间隔5小时58分
    public static final long TIME_SYNC_UPLOAD = 5 * 60 * 1000 + 58 * 1000;
    // SDK应用列表更新间隔,同时只有一个进程工作,首次SDK初始化时创建,涉及广播，5秒监听就行
    public static final String FILES_SYNC_APPSNAPSHOT = "SAP.TAG";
    public static final String FILES_SYNC_OC = "OCS.TAG";
    // OC 5+同步时间,同时只有一个进程工作
    public static final long TIME_SYNC_OC_ABOVE_FIVE = 25 * 1000;
    // 位置信息,通进程只有一个工作,两次间隔29分钟
    public static final String FILES_SYNC_LOCATION = "LCT.TAG";
    public static final long TIME_SYNC_OC_LOCATION = 29 * 60 * 1000;
    // 数据库写入，多进程只有一个写入,两次间隔5秒
    public static final String FILES_SYNC_DB_WRITER = "WDB.TAG";

    // 默认同步写入时间，5秒内能写入一次。 间隔范围: APP列表(多进程广播方面)、 OC 5.x以下、DB写入
    public static final long TIME_SYNC_DEFAULT = 5 * 1000;

}
