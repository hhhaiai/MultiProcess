package com.eguan;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 协议变量。包含上传变量、策略变量
 * @Version: 1.0
 * @Create: 2018年9月4日 上午10:51:36
 * @Author: sanbo
 */
public class ProtocolInfo {

    public static class DevInfo {

    }

    public static class OCInfo {

    }

    public static class OCTimes {

    }

    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: 安装应用列表信息
     * @Version: 1.0
     * @Create: 2018年9月7日 下午3:31:45
     * @Author: sanbo
     */
    public static class InstalledAppInfo {
        public static final String NAME = "InstalledAppInfo";
        // 应用包名. eg:com.hello
        public static final String ApplicationPackageName = "APN";
        // 应用程序名.eg: QQ
        public static final String ApplicationName = "AN";
        // 应用版本名|应用版本号. eg: 5.2.1|521
        public static final String ApplicationVersionCode = "AVC";
        // 数据使用. 字段值为"3"
        public static final String IsNew = "IN";
    }

    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: 安装、卸载、更新信息
     * @Version: 1.0
     * @Create: 2018年9月7日 下午3:25:32
     * @Author: sanbo
     */
    public static class IUUInfo {
        public static final String NAME = "IUUInfo";
        // 应用包名. eg:com.hello
        public static final String ApplicationPackageName = "APN";
        // 应用程序名.eg: QQ
        public static final String ApplicationName = "AN";
        // 应用版本名|应用版本号. eg: 5.2.1|521
        public static final String ApplicationVersionCode = "AVC";
        // 行为类型. 0:安装 1:卸载 2:更新
        public static final String ActionType = "AT";
        // 行为发生时间.时间戳
        public static final String ActionHappenTime = "AHT";

    }

    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: 热点信息
     * @Version: 1.0
     * @Create: 2018年9月7日 下午3:25:18
     * @Author: sanbo
     */
    public static class WBGInfo {
        public static final String NAME = "WBGInfo";
        // wifi名称
        public static final String SSID = "SSID";
        // 无线路由器的mac地址
        public static final String BSSID = "BSSID";
        // 信号强度
        public static final String Level = "LEVEL";
        // 位置区编码
        public static final String LocationAreaCode = "LAC";
        // 基站编号
        public static final String CellId = "CellId";
        // 采集时间
        public static final String CollectionTime = "CT";
        // 地理位置，由经度和纬度组成，用减号-连接
        public static final String GeographyLocation = "GL";
        // 公网IP
        public static final String ip = "ip";
    }

    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: 字段太大，暂时不要了
     * @Version: 1.0
     * @Create: 2018年9月6日 上午11:28:04
     * @Author: sanbo
     */
    @Deprecated
    public static class PLInfo {
        public static final String NAME = "PLInfo";
        // 进程生命周期，开始-结束|开始-结束
        public static final String ProcessLifecycle = "PL";
    }

    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: 以前sdk传递是个空array
     * @Version: 1.0
     * @Create: 2018年9月6日 上午11:28:12
     * @Author: sanbo
     */
    @Deprecated
    public static class WebInfo {
        public static final String NAME = "WebInfo";
        // 网页地址，如："http://www.baidu.com" . 现在代码中用为WRL
        public static final String Url = "URL";
        // 网页打开时间，转换成时间戳，如："1296035591"
        public static final String WebOpenTime = "WOT";
    }

    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: 网络相应
     * @Version: 1.0
     * @Create: 2018年9月4日 下午2:58:31
     * @Author: sanbo
     */
    public static class Response {
        /**
         * 状态回执
         */
        public static final String RES_CODE = "code";
        /**
         * 策略相关的
         */
        public static final String RES_POLICY = "policy";
        // 策略版本
        public static final String RES_POLICY_VERSION = "policyVer";
        // 服务器延迟上传时间
        public static final String RES_POLICY_SERVERDELAY = "serverDelay";
        // 上传失败次数
        public static final String RES_POLICY_FAILCOUNT = "failCount";
        // 上传失败后延迟时间
        public static final String RES_POLICY_FAILTRYDELAY = "failTryDelay";
        // 客户端上传时间间隔
        public static final String RES_POLICY_TIMERINTERVAL = "timerInterval";
        // 客户端上传时数据条数
        public static final String RES_POLICY_EVENTCOUNT = "eventCount";
        // 是否使用实时策略， 1不使用 0使用
        public static final String RES_POLICY_USERTP = "useRTP";
        // 是否实时上传[非实时分析策略下，是否实时上传] 0不实时上传，1实时上传
        public static final String RES_POLICY_USERTL = "useRTL";
        // 是否采集公网IP
        public static final String RES_POLICY_REMOTEIP = "remoteIp";
        // 是否上传敏感数据, 0,1
        public static final String RES_POLICY_UPLOADSD = "uploadSD";
        // 数据合并间隔
        public static final String RES_POLICY_MERGEINTERVAL = "mergeInterval";
        // 最小使用时长
        public static final String RES_POLICY_MINDURATION = "minDuration";
        // 最长使用时长
        public static final String RES_POLICY_MAXDURATION = "maxDuration";
        // 域名更新次数
        public static final String RES_POLICY_DOMAINUPDATETIMES = "domainUpdateTimes";

        // 拉活服务 相关的
        public static final String RES_POLICY_SERVICEPULL = "servicePull";
        public static final String RES_POLICY_SERVICEPULL_PACKAGENAME = "packageName";
        public static final String RES_POLICY_SERVICEPULL_CLASSNAME = "className";
        public static final String RES_POLICY_SERVICEPULL_ACTION = "action";
        public static final String RES_POLICY_SERVICEPULL_EXTRA = "extra";

        // deeplink拉活
        public static final String RES_POLICY_APPPULL = "appPull";
        public static final String RES_POLICY_APPPULL_PACKAGENAME = "packageName";
        public static final String RES_POLICY_APPPULL_DEEPLINK = "deepLink";
        public static final String RES_POLICY_APPPULL_TITLE = "title";
        public static final String RES_POLICY_APPPULL_CONTENT = "content";
        public static final String RES_POLICY_APPPULL_ICON = "icon";

        // oc打开应用次数限制.暂时没用
        public static final String RES_POLICY_OCRULE = "ocRule";
        public static final String RES_POLICY_OCRULE_MERGEINTERVAL = "mergeInterval";
        public static final String RES_POLICY_OCRULE_MINDURATION = "minDuration";

        // 广告
        public static final String RES_POLICY_AD = "ad";
        public static final String RES_POLICY_AD_ID = "id";
        public static final String RES_POLICY_AD_TITLE = "title";
        public static final String RES_POLICY_AD_CONTENT = "content";
        public static final String RES_POLICY_AD_ICON = "icon";
        public static final String RES_POLICY_AD_URL = "url";

        // 是否同意隐私权限
        public static final String RES_POLICY_UE = "ue";
        // 应用SDK控制 0,1
        public static final String RES_POLICY_UE_AVALID = "aValid";
        // 设备SDK控制 0,1
        public static final String RES_POLICY_UE_DVALID = "dValid";

    }

}
