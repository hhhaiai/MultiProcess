package com.eguan.imp;

import java.io.Serializable;

/**
 * 应用安装、卸载、更新实体类
 * 
 * @author Machenike
 *
 */
public class IUUInfo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * 应用包名，如：“com.qzone”
     */
    private String applicationPackageName;
    /**
     * 应用程序名，如：“QQ空间”
     */
    private String applicationName;
    /**
     * 应用版本|应用版本号，如“5.4.1|89”
     */
    private String applicationVersionCode;
    /**
     * 行为类型， 0代表安装；1代表卸载；2代表更新
     */
    private String actionType;
    /**
     * 行为发生时间，转换成时间戳，如：“1296035800“
     */
    private String actionHappenTime;

    public String getApplicationPackageName() {
        return applicationPackageName;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        this.applicationPackageName = applicationPackageName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersionCode() {
        return applicationVersionCode;
    }

    public void setApplicationVersionCode(String applicationVersionCode) {
        this.applicationVersionCode = applicationVersionCode;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionHappenTime() {
        return actionHappenTime;
    }

    public void setActionHappenTime(String actionHappenTime) {
        this.actionHappenTime = actionHappenTime;
    }

    @Override
    public String toString() {
        return "IUUInfo [applicationPackageName=" + applicationPackageName + ", applicationName=" + applicationName
                + ", applicationVersionCode=" + applicationVersionCode + ", actionType=" + actionType
                + ", actionHappenTime=" + actionHappenTime + "]";
    }

}
