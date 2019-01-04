package com.analysys.dev.model;

import java.io.Serializable;

/**
 * 软件相关信息，宿主信息，每次上传
 */
public class SoftwareInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 宿主应用名称
     */
    private String ApplicationName;
    /**
     * API等级
     */
    private String ApiLevel;
    /**
     * 宿主应用包名,如：“com.qzone”
     */
    private String ApplicationPackageName;
    /**
     * SDK版本，如：“150208”
     */
    private String SdkVersion;
    /**
     * 应用版本名|应用版本号, 如：“5.2.1|521”
     */
    private String ApplicationVersionCode;
    /**
     * App签名MD5值
     */
    private String AppMD5;
    /**
     * App签名信息
     */
    private String AppSign;
    /**
     * 临时ID，易观自己生成的用来识别设备的ID,38位字符
     */
    private String TempID;

    public String getApplicationName() {
        return ApplicationName;
    }

    public String getApiLevel() {
        return ApiLevel;
    }

    public String getApplicationPackageName() {
        return ApplicationPackageName;
    }

    public String getSdkVersion() {
        return SdkVersion;
    }

    public String getApplicationVersionCode() {
        return ApplicationVersionCode;
    }

    public String getAppMD5() {
        return AppMD5;
    }

    public String getAppSign() {
        return AppSign;
    }

    public String getTempID() {
        return TempID;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public void setApiLevel(String apiLevel) {
        ApiLevel = apiLevel;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        ApplicationPackageName = applicationPackageName;
    }

    public void setSdkVersion(String sdkVersion) {
        SdkVersion = sdkVersion;
    }

    public void setApplicationVersionCode(String applicationVersionCode) {
        ApplicationVersionCode = applicationVersionCode;
    }

    public void setAppMD5(String appMD5) {
        AppMD5 = appMD5;
    }

    public void setAppSign(String appSign) {
        AppSign = appSign;
    }

    public void setTempID(String tempID) {
        TempID = tempID;
    }
}
