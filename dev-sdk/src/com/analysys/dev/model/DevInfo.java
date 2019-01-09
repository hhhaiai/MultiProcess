package com.analysys.dev.model;
import java.io.Serializable;
/**
 * 设备相关信息,每次上传
 */
public class DevInfo implements Serializable{
    private static class Holder {
        private static final DevInfo INSTANCE = new DevInfo();
    }
    public static DevInfo getInstance() {
        return Holder.INSTANCE;
    }
    private static final long serialVersionUID = 1L;
    /**
     * 系统名称
     */
    private String SystemName;
    /**
     * 系统版本，如：“2.3.4”
     */
    private String SystemVersion;
    /**
     * 设备品牌，如：“联想”、“魅族”
     */

    private String DeviceBrand;
    /**
     * 设备Id，由IMEI、IMSI、ANDROID_ID三部分组成，各部分用减号-连接，如：“860157010327119-1ds5412-
     * b42e03d9cc43ff4c”
     */
    private String DeviceId;
    /**
     * 设备型号，如：“Lenovo S760”
     */
    private String DeviceModel;
    /**
     * MAC
     */
    private String MACAddress;
    /**
     * SerialNumber,设备序列号
     */
    private String SerialNumber;
    /**
     * 分辨率
     */
    private String Resolution;
    /**
     * 屏幕密度
     */
    private String DotPerInch;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getSystemName() {
        return SystemName;
    }

    public String getSystemVersion() {
        return SystemVersion;
    }

    public String getDeviceBrand() {
        return DeviceBrand;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public String getDeviceModel() {
        return DeviceModel;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public String getResolution() {
        return Resolution;
    }

    public String getDotPerInch() {
        return DotPerInch;
    }

    public void setSystemName(String systemName) {
        SystemName = systemName;
    }

    public void setSystemVersion(String systemVersion) {
        SystemVersion = systemVersion;
    }

    public void setDeviceBrand(String deviceBrand) {
        DeviceBrand = deviceBrand;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    public void setDeviceModel(String deviceModel) {
        DeviceModel = deviceModel;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = serialNumber;
    }

    public void setResolution(String resolution) {
        Resolution = resolution;
    }

    public void setDotPerInch(String dotPerInch) {
        DotPerInch = dotPerInch;
    }
}
