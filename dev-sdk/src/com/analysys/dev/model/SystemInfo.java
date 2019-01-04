package com.analysys.dev.model;

import java.io.Serializable;

/**
 * 系统阶段保持信息，默认不上传，根据服务器控制来上传
 */
public class SystemInfo  implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 系统字体大小，如“17”
     */
    private String SystemFontSize ;
    /**
     * 系统小时制，如“12”
     */
    private String SystemHour;
    /**
     * 系统语言，如“zh-Hans-CN”
     */
    private String SystemLanguage;
    /**
     * 设备所在地，如“CN”
     */
    private String SystemArea;
    /**
     * 当前时区，如“GMT+8”
     */
    private String TimeZone;

    public String getSystemFontSize() {
        return SystemFontSize;
    }

    public String getSystemHour() {
        return SystemHour;
    }

    public String getSystemLanguage() {
        return SystemLanguage;
    }

    public String getSystemArea() {
        return SystemArea;
    }

    public String getTimeZone() {
        return TimeZone;
    }

    public void setSystemFontSize(String systemFontSize) {
        SystemFontSize = systemFontSize;
    }

    public void setSystemHour(String systemHour) {
        SystemHour = systemHour;
    }

    public void setSystemLanguage(String systemLanguage) {
        SystemLanguage = systemLanguage;
    }

    public void setSystemArea(String systemArea) {
        SystemArea = systemArea;
    }

    public void setTimeZone(String timeZone) {
        TimeZone = timeZone;
    }
}
