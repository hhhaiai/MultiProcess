package com.analysys.dev.model;

import java.io.Serializable;

/**
 * 电量信息，默认不上传，需要根据服务器控制
 */
public class BatteryModuleNameInfo implements Serializable {

    private static class Holder {
        private static final BatteryModuleNameInfo INSTANCE = new BatteryModuleNameInfo();
    }
    public static BatteryModuleNameInfo getInstance() {
        return BatteryModuleNameInfo.Holder.INSTANCE;
    }
    private static final long serialVersionUID = 1L;
    /**
     * BS电源状态，下面有Android和iOS的传值与对应电源状态的码表
     */
    private String BatteryStatus;
    /**
     * BH电源健康状态情况
     */
    private String BatteryHealth;
    /**
     * BL电源当前电量，0-100的值
     */
    private String BatteryLevel;
    /**
     * BSL电源总电量，0-100的值
     */
    private String BatteryScale;
    /**
     * BP电源连接插座
     */
    private String BatteryPlugged;
    /**
     * BT电源类型,比如"Li-ion"
     */
    private String BatteryTechnology;
    /**
     * BTP电池温度，如270
     */
    private String BatteryTemperature;

    public String getBatteryStatus() {
        return BatteryStatus;
    }

    public String getBatteryHealth() {
        return BatteryHealth;
    }

    public String getBatteryLevel() {
        return BatteryLevel;
    }

    public String getBatteryScale() {
        return BatteryScale;
    }

    public String getBatteryPlugged() {
        return BatteryPlugged;
    }

    public String getBatteryTechnology() {
        return BatteryTechnology;
    }

    public String getBatteryTemperature() {
        return BatteryTemperature;
    }

    public void setBatteryStatus(String batteryStatus) {
        BatteryStatus = batteryStatus;
    }

    public void setBatteryHealth(String batteryHealth) {
        BatteryHealth = batteryHealth;
    }

    public void setBatteryLevel(String batteryLevel) {
        BatteryLevel = batteryLevel;
    }

    public void setBatteryScale(String batteryScale) {
        BatteryScale = batteryScale;
    }

    public void setBatteryPlugged(String batteryPlugged) {
        BatteryPlugged = batteryPlugged;
    }

    public void setBatteryTechnology(String batteryTechnology) {
        BatteryTechnology = batteryTechnology;
    }

    public void setBatteryTemperature(String batteryTemperature) {
        BatteryTemperature = batteryTemperature;
    }
}
