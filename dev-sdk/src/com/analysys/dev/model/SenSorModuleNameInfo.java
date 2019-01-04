package com.analysys.dev.model;

import java.io.Serializable;

/**
 * 传感器，默认不上传
 */
public class SenSorModuleNameInfo  implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 传感器名字
     */
    private String SenSorName;
    /**
     * 传感器版本
     */
    private int SenSorVersion ;
    /**
     * 传感器厂商
     */
    private String SenSorManufacturer;
    /**
     * 传感器id，同一个应用程序传感器ID将是唯一的，除非该设备是恢复出厂设置
     */
    public int SenSorId ;
    /**
     * 当传感器是唤醒状态返回true
     */
    private boolean SenSorWakeUpSensor;
    /**
     * 传感器耗电量
     */
    private float SenSorPower;

    private String getSenSorName() {
        return SenSorName;
    }

    public int getSenSorVersion() {
        return SenSorVersion;
    }

    public String getSenSorManufacturer() {
        return SenSorManufacturer;
    }

    public int getSenSorId() {
        return SenSorId;
    }

    public boolean getSenSorWakeUpSensor() {
        return SenSorWakeUpSensor;
    }

    public float getSenSorPower() {
        return SenSorPower;
    }

    public void setSenSorName(String senSorName) {
        SenSorName = senSorName;
    }

    public void setSenSorVersion(int senSorVersion) {
        SenSorVersion = senSorVersion;
    }

    public void setSenSorManufacturer(String senSorManufacturer) {
        SenSorManufacturer = senSorManufacturer;
    }

    public void setSenSorId(int senSorId) {
        SenSorId = senSorId;
    }

    public void setSenSorWakeUpSensor(boolean senSorWakeUpSensor) {
        SenSorWakeUpSensor = senSorWakeUpSensor;
    }

    public void setSenSorPower(float senSorPower) {
        SenSorPower = senSorPower;
    }
}
