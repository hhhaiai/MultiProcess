package com.analysys.track.model;

import java.io.Serializable;

/**
 * 蓝牙信息，默认不上传，需要根据服务器控制
 */
public class BluetoothModuleNameInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 蓝牙MAC地址
     */
    private String BluetoothMac;
    /**
     * 蓝牙信息
     */
    private String BlustoothName;

    public String getBluetoothMac() {
        return BluetoothMac;
    }

    public String getBlustoothName() {
        return BlustoothName;
    }

    public void setBluetoothMac(String bluetoothMac) {
        BluetoothMac = bluetoothMac;
    }

    public void setBlustoothName(String blustoothName) {
        BlustoothName = blustoothName;
    }
}
