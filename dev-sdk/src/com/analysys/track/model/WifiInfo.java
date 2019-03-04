package com.analysys.track.model;

import java.io.Serializable;

/**
 * wifi信息
 */
public class WifiInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * wifi名称
     */
    private String SSID;
    /**
     * 无线路由器的mac的地址
     * */
    private String BSSID;
    /**
     * 信号强度
     */
    private String Level;
    /**
     * wifi加密方式
     */
    private String Capabilities;
    /**
     * 以MHz为单位的接入频率值
     */
    private String Frequency;

    public String getSSID() {
        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getLevel() {
        return Level;
    }

    public String getCapabilities() {
        return Capabilities;
    }

    public String getFrequency() {
        return Frequency;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public void setLevel(String level) {
        Level = level;
    }

    public void setCapabilities(String capabilities) {
        Capabilities = capabilities;
    }

    public void setFrequency(String frequency) {
        Frequency = frequency;
    }
}
