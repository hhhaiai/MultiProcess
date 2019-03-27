package com.analysys.track.internal.Content;

public class DeviceKeyContacts {
  /**
   * 设备相关信息
   */
  public static class DevInfo {
    public static final String NAME = "DevInfo";
    /*
     * 设备硬件信息f
     */
    // 系统名称，如：“Android“、”iPhone OS“
    public static final String SystemName = "SN";
    // 系统版本，如：”2.3.4”
    public static final String SystemVersion = "SV";
    // 设备品牌，如：“联想”、“魅族”。（苹果设备的品牌名都为apple）
    public static final String DeviceBrand = "DB";
    // 设备Id，由IMEI-IMSI-AndroidId组成，如：863363036038592-460019031508084-144379774dc1c0b8
    public static final String DeviceId = "DI";
    // 设备型号 , 如：“Lenovo S760”
    public static final String DeviceModel = "DM";
    // 设备MAC地址，如：“6c:5c:14:25:be:ba”
    public static final String MAC = "MAC";
    // 设备序列号，如：”c83eccca”
    public static final String SerialNumber = "SNR";
    // 分辨率，如“480-800”
    public static final String Resolution = "RES";
    //屏幕密度，如360
    public static final String DotPerInch = "DPI";
    /*
     * 运营商相关的
     */
    // 运营商，如“联通”、“移动”
    public static final String MobileOperator = "MO";
    // sim卡运营商名字，如："CHINA MOBILE"
    public static final String MobileOperatorName = "MPN";
    // 接入运营商编码，如："60202"
    public static final String NetworkOperatorCode = "NOC";
    // 接入运营商名字，如："vodafone"
    public static final String NetworkOperatorName = "NON";
    // 多卡IMEI，采集手机上的多个IMEI值，如“86015700327|86098790529”
    public static final String Imeis = "IMEIS";
    // 手机IMSI号，如：“460019031508084”
    public static final String Imsis = "IMSIS";
    /*
     * 配置相关信息
     */
    // 推广渠道
    public static final String ApplicationChannel = "AC";
    // 样本应用key，由易观为样本应用分配的key
    public static final String ApplicationKey = "AK";
    /*
     * 软件相关信息
     */
    // 应用程序名，如：“QQ空间”
    public static final String ApplicationName = "AN";
    // [STG字段APILevel] API等级（仅供安卓使用）
    public static final String APILevel = "APIL";
    // 应用包名，如：“com.eguan.sdkdemo”
    public static final String ApplicationPackageName = "APN";
    // SDK版本，如：“3.7.0|170215”
    public static final String SDKVersion = "SDKV";
    // [以前在其他列表里]应用版本名|应用版本号. eg: 5.2.1|521
    public static final String ApplicationVersionCode = "AVC";
    // App签名MD5值，比如 "CF:95:50:8C:E8:8A:62:D7:6A:D0:CF:88:5E:B9:D1:32"
    public static final String AppMD5 = "AM";
    // App签名信息，比如 "CE:B5:52:9D:F2:B2:2C:AF:BD:A2:97:9C:02:2F:FF:0F:C8:C4:A4:83"
    public static final String AppSign = "AS";
    /*
     * 内部ID，有则上传
     */
    // 易观ID，易观自己根据设备数据生成的标识设备的ID，如："123456789"
    public static final String EguanID = "EGID";
    // 临时ID，易观自己生成的用来识别设备的ID，38位字符
    public static final String TempID = "TMPID";
    /*
     * 防作弊相关信息，默认上传，可控制不上传
     */
    // 判断是否是模拟器，"0”= 不是模拟器“1”= 是模拟器
    public static final String Simulator = "SIR";
    // 判断设备本身、APP、以及工作环境是否是被调试状态，“0”= 不在调试状态“1”= 在调试状态
    public static final String Debug = "DBG";
    // 判断设备的OS是否被劫持，"0”= 没有被劫持“1”= 被劫持//原SDK有字段，没有实现
    public static final String Hijack = "HJK";
    // 是否root，值为1表示获取root权限；值为0表示没获取root权限
    public static final String IsRoot = "IR";
    /*
     * 蓝牙信息,默认不上传,需要根据服务器控制
     */
    // 蓝牙模块名字
    public static final String BluetoothModuleName = "BTTMN";
    // 蓝牙MAC，如“6c:5c:14:25:be:ba”
    public static final String BluetoothMac = "BMAC";
    // 蓝牙信息
    public static final String BluetoothName = "BName";
    /*
     *  电量信息,默认不上传,需要根据服务器控制
     */
    // 电量模块名字
    public static final String BatteryModuleName = "BYMN";
    // 电源状态，下面有iOS和Android的传值与对应电源状态的码表
    public static final String BatteryStatus = "BS";
    // 电源健康情况，下面有Android的传值与对应的电源健康情况码表
    public static final String BatteryHealth = "BH";
    // 电源当前电量，0-100的值
    public static final String BatteryLevel = "BL";
    // 电源总电量，0-100的值
    public static final String BatteryScale = "BSL";
    // 电源连接插座，下面有Android的传值与对应的连接插座的码表
    public static final String BatteryPlugged = "BP";
    // 电源类型，比如"Li-ion"
    public static final String BatteryTechnology = "BT";
    // 电池温度，如：270
    public static final String BatteryTemperature = "BTP";
    /*
     * 传感器,默认不上传
     */
    // 传感器模块名字
    public static final String SenSorModuleName = "SSMN";
    // 传感器名字
    public static final String SenSorName = "SSN";
    // 传感器版本
    public static final String SenSorVersion = "SSV";
    // 传感器厂商
    public static final String SenSorManufacturer = "SSM";
    // 传感器id，同一个应用程序传感器ID将是唯一的，除非该设备是恢复出厂设置
    public static final String SenSorId = "SSI";
    // 当传感器是唤醒状态返回true
    public static final String SenSorWakeUpSensor = "SSWUS";
    // 传感器耗电量
    public static final String SenSorPower = "SSP";
    /*
     * 系统阶段保持信息，默认不上传.根据服务器控制来上传
     */
    // 系统字体大小，如“17”
    public static final String SystemFontSize = "SFS";
    // 系统小时制，如“12”
    public static final String SystemHour = "SH";
    // 系统语言，如“zh-Hans-CN”
    public static final String SystemLanguage = "SL";
    // 设备所在地，如“CN”
    public static final String SystemArea = "SA";
    // 当前时区，如“GMT+8”
    public static final String TimeZone = "TZ";
    /*
     * 更加详细的设备详情信息,默认可不上传.可用于确定设备信息
     */
    // CPU架构，比如 "armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
    public static final String CPUModel = "CM";
    // 设备Label，如："MMB29M"
    public static final String BuildId = "BI";
    // 设备Build ID，如："R9s_11_A.06_161202"
    public static final String BuildDisplay = "BD";
    // 设备产品名，如："R9s"
    public static final String BuildProduct = "BPT";
    // 设备工业设计名，如："R9s"
    public static final String BuildDevice = "BDE";
    // 底层板名称，如："msm8953"
    public static final String BuildBoard = "BB";
    // 系统引导程序版本号，如："unknown"
    public static final String BuildBootloader = "BBL";
    // 设备硬件名，如："qcom"
    public static final String BuildHardware = "BHW";
    // 设备支持的Abi，如："arm64-v8a, armeabi-v7a, armeabi"
    public static final String BuildSupportedAbis = "BSA";
    // 设备支持的32位的Abi，如："armeabi-v7a, armeabi"
    public static final String BuildSupportedAbis32 = "BSAS";
    // 设备支持的64位的Abi，如："arm64-v8a"
    public static final String BuildSupportedAbis64 = "BSASS";
    // 系统构建类型，如："user"
    public static final String BuildType = "BTE";
    // 系统构建标签，如："dev-keys"
    public static final String BuildTags = "BTS";
    // 设备指纹，如："OPPO/R9s/R9s:6.0.1/MMB29M/1390465867:user/release-keys"
    public static final String BuildFingerPrint = "BFP";
    // 设备固件版本，如："Q_V1_P14,Q_V1_P14"
    public static final String BuildRadioVersion = "BRV";
    // 系统构建内部名，如："eng.root.20161202.191841"
    public static final String BuildIncremental = "BIR";
    // 系统基带版本，如："OPPO/R9s/R9s:6.0.1/MMB29M/1390465867:user/release-keys"
    public static final String BuildBaseOS = "BBO";
    // 系统安全补丁，如："2016-09-01"
    public static final String BuildSecurityPatch = "BSP";
    // 系统框架版本号，如：23
    public static final String BuildSdkInt = "BSI";
    // 系统预览版本号，如：0
    public static final String BuildPreviewSdkInt = "BPSI";
    // 系统开发代码，如："REL"
    public static final String BuildCodename = "BC";
    // 谷歌广告ID.
    public static final String IDFA = "IDFA";

  }

  /**
   * @Copyright © 2018 Analysys Inc. All rights reserved.
   * @Description: OC信息：包含来源和取值时网络状态
   * @Version: 1.0
   * @Create: 2018年10月8日 下午12:01:42
   * @Author: sanbo
   */
  public static class OCInfo {
    public static final String NAME = "OCInfo";
    // 应用打开时间，转换成时间戳，如：“1296035591”
    public static final String ApplicationOpenTime = "AOT";
    // 应用关闭时间，转换成时间戳，如：“1296035599”
    public static final String ApplicationCloseTime = "ACT";
    // 应用包名，如：“com.qzone”
    public static final String ApplicationPackageName = "APN";
    // 应用程序名，如：“QQ空间”
    public static final String ApplicationName = "AN";
    // 应用版本名|应用版本号，如“5.4.1|89”
    public static final String ApplicationVersionCode = "AVC";
    // 网络类型， 选项: WIFI/2G/3G/4G/无网络
    public static final String NetworkType = "NT";
    // OC 切换的类型，1-正常使用，2-开关屏幕切换，3-服务重启
    public static final String SwitchType = "ST";
    // 应用类型，SA-系统应用，OA-第三方应用
    public static final String ApplicationType = "AT";
    // 采集来源类型，1-getRunningTask，2-读取proc，3-辅助功能，4-系统统计
    public static final String CollectionType = "CT";
    // 应用打开关闭次数
    public static final String CU = "CU";
    // 快照次数所属的时段，1表示0～6小时，2表示6～12小时，3表示12～18小时，4表示18～24小时
    public static final String TI = "TI";
    // 发生日期
    public static final String DY = "DY";
  }

  /**
   * @Copyright © 2018 Analysys Inc. All rights reserved.
   * @Description: 进程信息, 几种方式获取
   * @Version: 1.0
   * @Create: 2018年10月8日 下午1:38:24
   * @Author: sanbo
   */
  public static class ProcessInfo {
    public static final String NAME = "ProcessImpl";
    // proc方式读取的数据
    public static final String POC = "PROC";
    // shell 执行top指令获取的数据
    public static final String TOP = "TOP";
    // shell 执行ps指令获取的数据
    public static final String PS = "PS";
  }

  /**
   * @Copyright © 2018 Analysys Inc. All rights reserved.
   * @Description: app安装/卸载/更新详情.
   * @Version: 1.0
   * @Create: 2018年10月8日 上午11:49:34
   * @Author: sanbo
   */
  public static class AppSnapshotInfo {
    public static final String NAME = "AppSnapshotImpl";
    // 应用包名. eg:com.hello
    public static final String ApplicationPackageName = "APN";
    // 应用程序名.eg: QQ
    public static final String ApplicationName = "AN";
    // 应用版本名|应用版本号. eg: 5.2.1|521
    public static final String ApplicationVersionCode = "AVC";
    // 行为类型. -1:未变动(可不上传) 0:安装 1:卸载 2:更新
    public static final String ActionType = "AT";
    // 行为发生时间.时间戳 后端表: ods.ods_dev_app_installed_d [锆云]
    public static final String ActionHappenTime = "AHT";
  }

  /**
   * @Copyright © 2018 Analysys Inc. All rights reserved.
   * @Description: 定位信息
   * @Version: 1.0
   * @Create: 2018年10月8日 上午11:49:10
   * @Author: sanbo
   */
  public static class LocationInfo {
    public static final String NAME = "LocationImpl";
    //时间
    public static final String CollectionTime = "CT";
    // 地理位置，由经度和纬度组成，用减号-连接
    public static final String GeographyLocation = "GL";

    // WiFi信息
    public static class WifiInfo {
      public static final String NAME = "WifiInfo";
      // wifi名称
      public static final String SSID = "SSID";
      // 无线路由器的mac地址
      public static final String BSSID = "BSSID";
      // 信号强度
      public static final String Level = "LEVEL";
      // WiFi加密方式
      public static final String Capabilities = "CBT";
      // 以MHz为单位的接入频率值
      public static final String Frequency = "FQC";
      public static final String TIME = "WT";

    }

    // 基站信息
    public static class BaseStationInfo {
      public static final String NAME = "BaseStationInfo";
      // ods.ods_dev_user_geo_lbs_d [锆云] 位置区编码
      public static final String LocationAreaCode = "LAC";
      // ods.ods_dev_user_geo_lbs_d [锆云] 基站编号
      public static final String CellId = "CI";
      // 信号强度
      public static final String Level = "LV";
      //时间
      public static final String BSTIME = "BSTime";

    }
  }

  /**
   * @Copyright © 2018 Analysys Inc. All rights reserved.
   * @Description: 策略相关信息
   * @Version: 1.0
   * @Create: 2018年9月4日 下午2:58:31
   * @Author: sanbo
   */
  public static class Response {
    /**
     * 状态回执
     */
    public static final String RES_CODE = "code";
    /*
     * 基础发送.策略相关的
     */
    public static final String RES_POLICY = "policy";
    // 策略版本
    public static final String RES_POLICY_VERSION = "policyVer";
    // 策略--失败
    public static final String RES_POLICY_FAIL = "fail";
    // 服务器延迟上传时间，只用于第一次上传时延时
    public static final String RES_POLICY_SERVER_DELAY = "serverDelay";
    // 上传最大失败次数
    public static final String RES_POLICY_FAIL_COUNT = "failCount";
    // 上传失败后延迟时间
    public static final String RES_POLICY_FAIL_TRY_DELAY = "failTryDelay";
    // 客户端上传时间间隔
    public static final String RES_POLICY_TIMER_INTERVAL = "timerInterval";
    // 是否使用实时策略， 1不使用 0使用
    public static final String RES_POLICY_USE_RTP = "useRTP";
    // 是否实时上传[非实时分析策略下，是否实时上传] 0不实时上传，1实时上传
    public static final String RES_POLICY_USE_RTL = "useRTL";

    //动态采集模块
    public static final String RES_POLICY_CTRL_LIST = "ctrlList";


    //某个Info名称，伴随SDK更新
    public static final String RES_POLICY_CTRL_MODULE = "module";
    //状态
    public static final String RES_POLICY_CTRL_STATUS = "status";
    //默认
    public static final String RES_POLICY_CTRL_DEUFREQ = "deuFreq";
    //最小采集频率
    public static final String RES_POLICY_CTRL_MIN_FREQ = "minFreq";
    //最大采集频率
    public static final String RES_POLICY_CTRL_MAX_FREQ = "maxFreq";
    //
    public static final String RES_POLICY_CTRL_MAX_COUNT = "maxCount";
    //不要的字段
    public static final String RES_POLICY_CTRL_UNWANTED = "exclude";
    //
    public static final String RES_POLICY_CTRL_SUB_CONTROL = "subControl";
    //子模块名称(需要SDK内置):0(蓝牙),1(电量),2(语言、时区、输入法),3(VPN),4(账号)，5(传感器)
    public static final String RES_POLICY_CTRL_SUB_MODULE = "submodule";
    //
    public static final String RES_POLICY_CTRL_SUB_STATUS = "sub_status";
    //
    public static final String RES_POLICY_CTRL_SUB_DEUFREQ = "sub_deuFreq";
    //
    public static final String RES_POLICY_CTRL_SUB_MIN_FREQ = "sub_minFreq";
    //
    public static final String RES_POLICY_CTRL_SUB_MAX_FREQ = "sub_maxFreq";
    //
    public static final String RES_POLICY_CTRL_COUNT= "count";


    /*
     * 控制ProcessInfo模块的采集
     */
    public static final String RES_POLICY_CL = "pi_cl";
    public static final String RES_POLICY_CL_MODULE_PROC = "pi_proc";
    public static final String RES_POLICY_CL_MODULE_PS = "pi_ps";
    public static final String RES_POLICY_CL_MODULE_TOP = "pi_top";
    // 控制策略. 1,0
    public static final String RES_POLICY_CL_PROCESSINFO = "use_process_info";
    // 获取策略频率
    public static final String RES_POLICY_CL_GET_DUR = "process_info_get_dur";
    // 上传频率
    public static final String RES_POLICY_CL_UP_DUR = "process_info_up_dur";
    
    /*
     * 模块的采集控制
     */
    public static final String RES_POLICY_MODULE_CL = "module_cl";
    // 蓝牙采集.默认不上传，可控制上传
    public static final String RES_POLICY_MODULE_CL_BLUETOOTH = "module_cl_bluetooth";
    // 电量采集.默认不上传，可控制上传
    public static final String RES_POLICY_MODULE_CL_BATTERY = "module_cl_battery";
    // 传感器控制
    public static final String RES_POLICY_MODULE_CL_SENSOR = "module_cl_sensor";
    // 防作弊相关信息控制.默认上传,可控制不上传. 0不上传
    public static final String RES_POLICY_MODULE_CL_DEV_CHECK = "module_cl_dev";
    // 系统阶段保持信息控制,比如语言、时区、小时制等. 默认不上传，可控制不上传
    public static final String RES_POLICY_MODULE_CL_KEEP_INFO = "module_cl_keep_info";
    // 更多设备信息控制，手机中一些系统信息.默认不上传，可控制上传
    public static final String RES_POLICY_MODULE_CL_MORE_INFO = "module_cl_more_info";
    /**
     * 模块整体控制
     */
    //OC
    public static final String RES_POLICY_MODULE_CL_OC = "module_cl_oc";
    //SNAPSHOT
    public static final String RES_POLICY_MODULE_CL_SNAPSHOT = "module_cl_snapshot";
    //LOCATION
    public static final String RES_POLICY_MODULE_CL_LOCATION = "module_cl_location";
    //WIFI
    public static final String RES_POLICY_MODULE_CL_WIFI = "module_cl_wifi";
    //BASE_STATION
    public static final String RES_POLICY_MODULE_CL_BASE = "module_cl_base";
    //BASE_STATION
    public static final String RES_POLICY_MODULE_CL_DEV = "module_cl_dev";
  }
}
