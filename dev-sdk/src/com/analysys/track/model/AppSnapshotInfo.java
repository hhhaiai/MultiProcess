package com.analysys.track.model;

import java.io.Serializable;

/**
 * app安装/卸载/更新详情
 */
public class AppSnapshotInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 宿主应用包名,如：“com.qzone”
     */
    private String ApplicationPackageName;
    /**
     * 宿主应用名称
     */
    private String ApplicationName;
    /**
     * 样本应用版本号, 如：“1.3.4”
     */
    private String applicationVersionCode;
    /**
     * 行为类型: -1未变动(可不上传);0安装;1卸载;2更新
     */
    private String ActionType;
    /**
     * 行为发生时间，转换成时间戳，如：“1296035800“
     */
    private String actionHappenTime;

    public String getApplicationPackageName() {
        return ApplicationPackageName;
    }

    public String getApplicationName() {
        return ApplicationName;
    }

    public String getApplicationVersionCode() {
        return applicationVersionCode;
    }

    public String getActionType() {
        return ActionType;
    }

    public String getActionHappenTime() {
        return actionHappenTime;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        ApplicationPackageName = applicationPackageName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public void setApplicationVersionCode(String applicationVersionCode) {
        this.applicationVersionCode = applicationVersionCode;
    }

    public void setActionType(String actionType) {
        ActionType = actionType;
    }

    public void setActionHappenTime(String actionHappenTime) {
        this.actionHappenTime = actionHappenTime;
    }
}
