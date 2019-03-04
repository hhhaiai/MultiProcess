package com.analysys.track.model;

import java.io.Serializable;

/**
 * 基站信息
 */
public class BaseStationInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * [锆云]位置区编码
     */
    private String LocationAreaCode;
    /**
     * [锆云]基站编号
     */
    private String CellId;
    /**
     * 信号强度
     */
    private String Level;

    public String getLocationAreaCode() {
        return LocationAreaCode;
    }

    public String getCellId() {
        return CellId;
    }

    public String getLevel() {
        return Level;
    }

    public void setLocationAreaCode(String locationAreaCode) {
        LocationAreaCode = locationAreaCode;
    }

    public void setCellId(String cellId) {
        CellId = cellId;
    }

    public void setLevel(String level) {
        Level = level;
    }
}
