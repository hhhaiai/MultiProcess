package com.analysys.dev.model;

import java.io.Serializable;

/**
 * OC信息：包含来源和取值时网络状态
 */
public class OCInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 应用打开时间，转换成时间戳，如：“1296035591”
     */
    private String ApplicationOpenTime;
    /**
     * 应用关闭时间，转换成时间戳，如：“1296035599”
     */
    private String ApplicationCloseTime;
    /**
     * 应用包名，如：“com.qzone”
     */
    private String ApplicationPackageName;
    /**
     * 应用程序名，如：“QQ空间”
     */
    private String ApplicationName;
    /**
     * 应用版本|应用版本号，如“5.4.1|89”
     */
    private String ApplicationVersionCode;

    /**
     * 网络类型，选项WIFI/2G/3G/4G/无网络
     *
     * @return
     */
    private String NetworkType;
    /**
     * OC切换类型，1-正常使用，2-开关屏幕切换，3-服务重启
     */
    private String SwitchType;
    /**
     * 应用类型，SA-系统应用,OA-第三方应用
     */
    private String ApplicationType;
    /**
     * 采集来源类型，1-getRunningTask,2-读取proc,3-辅助功能，4-系统统计
     */
    private String CollectionType;

    public String getApplicationOpenTime() {
        return ApplicationOpenTime;
    }

    public String getApplicationCloseTime() {
        return ApplicationCloseTime;
    }

    public String getApplicationPackageName() {
        return ApplicationPackageName;
    }

    public String getApplicationName() {
        return ApplicationName;
    }

    public String getApplicationVersionCode() {
        return ApplicationVersionCode;
    }

    public String getNetworkType() {
        return NetworkType;
    }

    public String getSwitchType() {
        return SwitchType;
    }

    public String getApplicationType() {
        return ApplicationType;
    }

    public String getCollectionType() {
        return CollectionType;
    }

    public void setApplicationOpenTime(String applicationOpenTime) {
        ApplicationOpenTime = applicationOpenTime;
    }

    public void setApplicationCloseTime(String applicationCloseTime) {
        ApplicationCloseTime = applicationCloseTime;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        ApplicationPackageName = applicationPackageName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public void setApplicationVersionCode(String applicationVersionCode) {
        ApplicationVersionCode = applicationVersionCode;
    }

    public void setNetworkType(String networkType) {
        NetworkType = networkType;
    }

    public void setSwitchType(String switchType) {
        SwitchType = switchType;
    }

    public void setApplicationType(String applicationType) {
        ApplicationType = applicationType;
    }

    public void setCollectionType(String collectionType) {
        CollectionType = collectionType;
    }
}
