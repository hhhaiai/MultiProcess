package com.analysys.track.internal.content;

import com.analysys.track.BuildConfig;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 内部变量存储
 * @Version: 1.0
 * @Create: 2019-08-05 14:54:55
 * @author: ly
 */
public class EGContext {


    /**
     * SDK版本
     */
    public static final String SDK_VERSION = BuildConfig.SDK_VERSION;
    public static final String SDK_TYPE = "Android";
    public static final String LOGTAG_USER = "analysys";
    public static String LOGTAG_INNER;
    public static boolean FLAG_DEBUG_USER = false;

    /**
     * 广播相关
     */
    public static final String ACTION_MTC_LOCK = "com.analysys.sdk.action_snap";
    public static final String ACTION_UPDATE_POLICY = "com.analysys.sdk.action_policy";
    public static final String POLICY = "POLICY";
    public static final String PNAME = "PNAME";

    public static volatile boolean snap_complete = false;
    /**
     * xml 中声明的 appid、channel
     */
    public static final String XML_METADATA_APPKEY = "ANALYSYS_APPKEY";
    public static final String XML_METADATA_CHANNEL = "ANALYSYS_CHANNEL";

    //获取间隔时间
    public static final String SP_SNAPSHOT_CYCLE = "SP_SNAPSHOT_CYCLE";
    public static final String SP_LOCATION_CYCLE = "SP_LOCATION_CYCLE";
    public static final String SP_OC_CYCLE = "SP_OC_CYCLE";

    public static final String SP_APP_SNAP = "S_SNAP_TIME";
    public static final String SP_APP_LOCATION = "S_LOC_TIME";

    public static final int TIME_SECOND = 1000;
    public static final int TIME_MINUTE = 60 * 1000;
    public static final int TIME_HOUR = 60 * 60 * 1000;


    public static final String SP_APP_KEY = "appKey";
    public static final String SP_APP_CHANNEL = "appChannel";

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
    public static final String SNAP_SHOT_INSTALL = "0";
    public static final String SNAP_SHOT_UNINSTALL = "1";
    public static final String SNAP_SHOT_UPDATE = "2";
    public static final String SNAP_SHOT_DEFAULT = "3";

    public static final String SP_NAME = "eg_policy";
    public static final String LAST_LOCATION = "last_location";
    public static final String TMPID = "tmp_id";
    public static final int SERVER_DELAY_DEFAULT = 0;
    // 上传重试次数，默认3次
    public static final int FAIL_COUNT_DEFALUT = 3;
    // 地理位置信息获取距离/米
    public static final long MINDISTANCE = 1000;
    public final static String URL_SCHEME = "http://";


    public static final String NETWORK_TYPE_2G = "2G";
    public static final String NETWORK_TYPE_3G = "3G";
    public static final String NETWORK_TYPE_4G = "4G";
    public static final String NETWORK_TYPE_5G = "5G";
    public static final String NETWORK_TYPE_WIFI = "WIFI";
    public static final String NETWORK_TYPE_NO_NET = "无网络";
    /**
     * 非实时上传端口
     */
    public final static String ORI_PORT = ":8089";
    /**
     * 测试回传接口.Debug模式
     */
    public final static String TEST_CALLBACK_PORT = ":10031";
    /**
     * 测试域名
     */
    public static final String TEST_CALLBACK_DOMAIN_NAME = "apptest.analysys.cn";
    /**
     * 非实时上传是,使用的域名池,以urd开始的为设备上传接口
     */
    public final static String[] NORMAL_UPLOAD_URL = {"urd103.analysys.cn", // 0
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
    public static final String TEST_URL = URL_SCHEME + TEST_CALLBACK_DOMAIN_NAME + TEST_CALLBACK_PORT;
    public static final String ORIGINKEY_STRING = "analysys";
    public static final String EGUANFILE = "eg.a";


    public static final String DEFAULT_ZERO = "0";
    public static final String DEFAULT_ONE = "1";

    public static final String TMPIDKEY = "tmpid";
    // 用于jobservice
    public static final int JOB_ID = 2071112;
    public static final String SDKV = "SDKV";
    public static final String DEBUG = "DEBUG";
    public static final String APPKEY = "AK";
    public static final String TIME = "TIME";
    public static final String POLICYVER = "policyVer";

    //  Negix 超过最大返回。网络状态，非消息内容
    public static final String HTTP_STATUS_413 = "413";
    // 200 请求成功。 返回值内部状态
    public static final String HTTP_STATUS_200 = "200";
    // 500 策略。返回值内部状态
    public static final String HTTP_STATUS_500 = "500";

    public static final int SHORT_TIME = 5 * 1000;// 计时器时间间隔毫秒数
    public static final long DEFAULT_SPACE_TIME = 30 * 1000;// 默认开关屏时间间隔在30s以上，才算一次有效的时间闭合事件
    public static final String THREAD_NAME = "com.eguan";
    public static final String UPLOAD_KEY_WORDS = "facility4";
    public static final String EXTRA_DATA = "ETDM";
    public static final int BLANK_COUNT_MAX = 10;

    /************************************************************************************/
    // SDK发送同步文件,首次SDK初始化时创建
    public static final String FILES_SYNC_UPLOAD = "SNET.TAG";
    // SDK发送同步文件两次间隔时间，同时只有一个进程工作,默认6个小时，两次间隔5小时58分
    public static final long TIME_SYNC_UPLOAD = 6 * 60 * 60 * 1000;
    // SDK应用列表更新间隔,同时只有一个进程工作,首次SDK初始化时创建,涉及广播，5秒监听就行
    public static final String FILES_SYNC_APPSNAPSHOT = "SAP.TAG";
    public static final String FILES_SYNC_OC = "OCS.TAG";
    public static final String FILES_SYNC_NET = "NETS.TAG";
    // OC 5+同步时间,同时只有一个进程工作
    public static final long TIME_SYNC_OC_OVER_5 = 30 * 1000;


    /************************************************************************************/
    /***********************************
     * 多进程同步
     *****************************************/
    public static final long TIME_SYNC_LOCATION = 30 * 60 * 1000;

    public static final String FILES_SYNC_SCREEN_OFF_BROADCAST = "T-OFF";
    //    public static final String FILES_SYNC_SCREEN_ON_BROADCAST = "T-ON";
    public static final String FILES_SYNC_SNAP_ADD_BROADCAST = "T-SADD";
    public static final String FILES_SYNC_SNAP_DELETE_BROADCAST = "T-SDEL";
    public static final String FILES_SYNC_SNAP_UPDATE_BROADCAST = "T-SUPDATE";
    public static final String FILES_SYNC_BATTERY_BROADCAST = "T-BATTERY";

    // 多进程同步. 同步版本号
    public static final String MULTIPROCESS_SP = "T-SP";

    // 位置信息,通进程只有一个工作,两次间隔29分钟
    public static final String FILES_SYNC_LOCATION = "T-LCT";
    // 最少间隔2s查询一次
    public static final long TIME_SYNC_BROADCAST = 2 * 1000;
    // 广播多进程处理，一次只能有一个进程在处理
    public static final String PERMISSION_TIME = "LOCATION_PERMISSION";
    public static final String PERMISSION_COUNT = "LOCATION_COUNT";
    public static final long LEN_MAX_UPDATE_SIZE = 1 * 1024 * 1024;
    public static final String INTERVALTIME = "TimerIntervalTime";
    public static final String LASTQUESTTIME = "lastQuestTime";
    public static final String RETRYTIME = "RetryIntervalTime";// 重试间隔时间
    public static final String FAILEDNUMBER = "uploadFailedNumber";// 本地已经重试并失败，次数
    public static final String FAILEDTIME = "uploadFailedTime";

    // 设备内SDK发送 进程同步文件。首次SDK初始化时创建
    public static final String MULTI_FILE_UPLOAD_RETRY = "M_TMP";
    public static final String MULTI_FILE_UPLOAD = "M_UP";

    // 上传模块
    public static final String MODULE_OC = "M_OC";
    public static final String MODULE_SNAPSHOT = "M_SNAP";
    public static final String MODULE_LOCATION = "M_LOC";
    public static final String MODULE_WIFI = "M_WIFI";
    public static final String MODULE_BASE = "M_BASE";
    public static final String MODULE_DEV = "M_DEV";
    public static final String MODULE_XXX = "M_XXX";
    public static final String SPUTIL = "sptrack";
    /**
     * 判断是否debug App列表
     */
    public static final String TEXT_DEBUG_APP = "packageName";
    public static final String TEXT_DEBUG_STATUS = "debugable";

    public static String VALUE_APPKEY = "";
    public static String VALUE_APP_CHANNEL = "";
    public static String EGUAN_CHANNEL_PREFIX = "EGUAN_CHANNEL_";
    public static String APP_URL = "";
    public static String NORMAL_APP_URL = EGContext.URL_SCHEME + EGContext.NORMAL_UPLOAD_URL[0] + EGContext.ORI_PORT;
    /**
     * 控制android8以后是否后台启动服务。提示通知
     */
    public static final boolean IS_SHOW_NOTIFITION = false;
    /**
     * 是否USB调试状态
     */
    public static boolean STATUS_USB_DEBUG = false;


    /********************************************日志控制************************************************/
    /**
     * EGuan 内部调试系列tag.主要用于控制堆栈打印、错误打印、内部提示信息打印
     */
    // 策略的总控。关闭后所有的日志都不能打印
    public static final boolean FLAG_DEBUG_INNER = true;

    // 上传模快日志控制
    public static final boolean DEBUG_UPLOAD = false;
    public static String TAG_UPLOAD;
    // OC模快日志控制
    public static final boolean DEBUG_OC = false;
    public static String TAG_OC;
    // 安装列表部分日志控制
    public static final boolean DEBUG_SNAP = false;
    public static String TAG_SNAP;
    // 广播日志控制
    public static final boolean DEBUG_RECEIVER = false;
    public static String TAG_RECEIVER;
    // 位置日志打印
    public static final boolean DEBUG_LOCATION = false;
    public static String TAG_LOC;
    // 执行上传URL控制
    public static final boolean DEBUG_URL = false;

    static {
        //调整。解决编译时到处可见打印字段问题。
        if (FLAG_DEBUG_INNER) {
            LOGTAG_INNER = "sanbo";
            TAG_RECEIVER = LOGTAG_INNER + ".rece";
            TAG_SNAP = LOGTAG_INNER + ".snap";
            TAG_OC = LOGTAG_INNER + ".oc";
            TAG_LOC = LOGTAG_INNER + ".loc";
            TAG_UPLOAD = LOGTAG_INNER + ".upload";
        }
    }
}
