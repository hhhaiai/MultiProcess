package com.eguan.monitor.imp;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;

import java.io.Serializable;
import java.net.URLEncoder;

/**
 * 设备信息单例类
 */
public class DriverInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private DriverInfo() {
    }

    public static DriverInfo getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder{
        private static final DriverInfo INSTANCE = new DriverInfo();
    }

    /**
     * 系统名称
     */
    private String SystemName;
    /**
     * 设备Id，由IMEI、IMSI、ANDROID_ID三部分组成，各部分用减号-连接，如：“860157010327119-1ds5412-
     * b42e03d9cc43ff4c”
     */
    private String deviceId;
    /**
     * MAC
     */
    private String MACAddress;
    /**
     * SerialNumber
     */
    private String serialNumber;
    /**
     * 设备品牌，如：“联想”、“魅族”
     */

    private String deviceBrand;
    /**
     * 设备型号，如：“Lenovo S760”
     */
    private String deviceModel;
    /**
     * 系统版本，如：“2.3.4”
     */
    private String systemVersion;
    /**
     * 是否root，1表示root/0表示没root
     */
    private String IsJailbreak;
    /**
     * 样本应用版本号, 如：“1.3.4”
     */
    private String applicationVersionCode;
    /**
     * 宿主应用包名,如：“com.qzone”
     */
    private String ApplicationPackageName;
    /**
     * 宿主应用名称
     */
    private String ApplicationName;
    /**
     * 样本应用key，由易观为样本应用分配的key
     */
    private String applicationKey;
    /**
     * 样本应用自己的推广渠道，如：“360”、“QQ管家”
     */
    private String applicationChannel;
    /**
     * 使用样本应用的用户Id
     */
    private String applicationUserId;
    /**
     * SDK版本，如：“150208”
     */
    private String sdkVersion;
    /**
     * API等级
     */
    private String apiLevel;
    /**
     * 运营商，如“联通”、“移动”
     */
    private String mobileOperator;
    /**
     * 手机号码
     */
    private String phoneNum;

    /**
     * 移动运营商名字
     */
    private String MobileOperatorName;

    /**
     * 接入运营商的编码
     */
    private String NetworkOperatorCode;

    /**
     * 接入运营商的名字
     */
    private String NetworkOperatorName;

    /**
     * 蓝牙MAC地址
     */
    private String BluetoothMAC;

    /**
     * 多卡IMEI
     */
    private String IMEIS;

    /**
     * 分辨率
     */
    private String Resolution;

    /**
     * 系统字体大小
     */
    private String SystemFontSize;

    /**
     * 当前时区
     */
    private String TimeZone;

    /**
     * 设备所在地
     */
    private String SystemArea;

    /**
     * 系统语言
     */
    private String SystemLanguage;

    /**
     * 系统小时制
     */
    private String SystemHour;

    /**
     * 判断设备本身,APP,以及工作环境是否是被调试状态
     */
    private int Debug;

    /**
     * 判断设备是否被劫持
     */
    private int Hjack;

    /**
     * 设备是否为模拟器 1:表示是模拟器;0:表示不是模拟器
     */
    private int Simulator;

    /**
     * 模拟器描述,如果是模拟器才进行描述
     */
    private String SimulatorDescription;

    /**
     * App签名MD5值
     */
    private String AppMD5;
    /**
     * App签名信息
     */
    private String AppSign;
    /**
     * IMSI
     */
    private String IMSI;
    /**
     * AndroidId
     */
    private String AndroidID;

    private String HUID;

    private String BI;
    private String BD;
    private String BPT;
    private String BDE;
    private String BB;
    private String BBL;
    private String BHW;
    private String BSA;
    private String BSAS;
    private String BSASS;
    private String BTE;
    private String BTS;
    private String BFP;
    private String BRV;
    private String BIR;
    private String BBO;
    private String BSP;
    private String BSI;
    private String BPSI;
    private String BC;

    public String getBI() {
        return BI;
    }

    public void setBI(String BI) {
        this.BI = BI;
    }

    public String getBD() {
        return BD;
    }

    public void setBD(String BD) {
        this.BD = BD;
    }

    public String getBPT() {
        return BPT;
    }

    public void setBPT(String BPT) {
        this.BPT = BPT;
    }

    public String getBDE() {
        return BDE;
    }

    public void setBDE(String BDE) {
        this.BDE = BDE;
    }

    public String getBB() {
        return BB;
    }

    public void setBB(String BB) {
        this.BB = BB;
    }

    public String getBBL() {
        return BBL;
    }

    public void setBBL(String BBL) {
        this.BBL = BBL;
    }

    public String getBHW() {
        return BHW;
    }

    public void setBHW(String BHW) {
        this.BHW = BHW;
    }

    public String getBSA() {
        return BSA;
    }

    public void setBSA(String BSA) {
        this.BSA = BSA;
    }

    public String getBSAS() {
        return BSAS;
    }

    public void setBSAS(String BSAS) {
        this.BSAS = BSAS;
    }

    public String getBSASS() {
        return BSASS;
    }

    public void setBSASS(String BSASS) {
        this.BSASS = BSASS;
    }

    public String getBTE() {
        return BTE;
    }

    public void setBTE(String BTE) {
        this.BTE = BTE;
    }

    public String getBTS() {
        return BTS;
    }

    public void setBTS(String BTS) {
        this.BTS = BTS;
    }

    public String getBFP() {
        return BFP;
    }

    public void setBFP(String BFP) {
        this.BFP = BFP;
    }

    public String getBRV() {
        return BRV;
    }

    public void setBRV(String BRV) {
        this.BRV = BRV;
    }

    public String getBIR() {
        return BIR;
    }

    public void setBIR(String BIR) {
        this.BIR = BIR;
    }

    public String getBBO() {
        return BBO;
    }

    public void setBBO(String BBO) {
        this.BBO = BBO;
    }

    public String getBSP() {
        return BSP;
    }

    public void setBSP(String BSP) {
        this.BSP = BSP;
    }

    public String getBSI() {
        return BSI;
    }

    public void setBSI(String BSI) {
        this.BSI = BSI;
    }

    public String getBPSI() {
        return BPSI;
    }

    public void setBPSI(String BPSI) {
        this.BPSI = BPSI;
    }

    public String getBC() {
        return BC;
    }

    public void setBC(String BC) {
        this.BC = BC;
    }





    public String getHUID() {
        return HUID;
    }

    public void setHUID(String HUID) {
        this.HUID = HUID;
    }

    public String getSimulatorDescription() {
        return SimulatorDescription;
    }

    public void setSimulatorDescription(String simulatorDescription) {
        SimulatorDescription = simulatorDescription;
    }

    public String getIMSI() {
        return IMSI;
    }

    public void setIMSI(String IMSI) {
        this.IMSI = IMSI;
    }

    public String getAndroidID() {
        return AndroidID;
    }

    public void setAndroidID(String androidID) {
        AndroidID = androidID;
    }

    public String getMobileOperatorName() {
        return MobileOperatorName;
    }

    public void setMobileOperatorName(String mobileOperatorName) {
        MobileOperatorName = mobileOperatorName;
    }

    public String getNetworkOperatorCode() {
        return NetworkOperatorCode;
    }

    public void setNetworkOperatorCode(String networkOperatorCode) {
        NetworkOperatorCode = networkOperatorCode;
    }

    public String getNetworkOperatorName() {
        return NetworkOperatorName;
    }

    public void setNetworkOperatorName(String networkOperatorName) {
        NetworkOperatorName = networkOperatorName;
    }

    public String getBluetoothMAC() {
        return BluetoothMAC;
    }

    public void setBluetoothMAC(String bluetoothMAC) {
        BluetoothMAC = bluetoothMAC;
    }

    public String getIMEIS() {
        return IMEIS;
    }

    public void setIMEIS(String IMEIS) {
        this.IMEIS = IMEIS;
    }

    public String getResolution() {
        return Resolution;
    }

    public void setResolution(String resolution) {
        Resolution = resolution;
    }

    public String getSystemFontSize() {
        return SystemFontSize;
    }

    public void setSystemFontSize(String systemFontSize) {
        SystemFontSize = systemFontSize;
    }

    public String getTimeZone() {
        return TimeZone;
    }

    public void setTimeZone(String timeZone) {
        TimeZone = timeZone;
    }

    public String getSystemArea() {
        return SystemArea;
    }

    public void setSystemArea(String systemArea) {
        SystemArea = systemArea;
    }

    public String getSystemLanguage() {
        return SystemLanguage;
    }

    public void setSystemLanguage(String systemLanguage) {
        SystemLanguage = systemLanguage;
    }

    public String getSystemHour() {
        return SystemHour;
    }

    public void setSystemHour(String systemHour) {
        SystemHour = systemHour;
    }

    public int getDebug() {
        return Debug;
    }

    public void setDebug(int debug) {
        Debug = debug;
    }

    public int getHjack() {
        return Hjack;
    }

    public void setHjack(int hjack) {
        Hjack = hjack;
    }

    public int getSimulator() {
        return Simulator;
    }

    public void setSimulator(int simulator) {
        Simulator = simulator;
    }

    public String getSystemName() {
        return SystemName;
    }

    public void setSystemName(String systemName) {
        SystemName = systemName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String mSerialNumber) {
        serialNumber = mSerialNumber;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String mACAddress) {
        MACAddress = mACAddress;
    }

    public String getDeviceBrand() {
        try {
            return URLEncoder.encode(deviceBrand, Constants.ENCODE);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return "";
    }

    public void setDeviceBrand(String deviceBrand) {
        this.deviceBrand = deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getIsJailbreak() {
        return IsJailbreak;
    }

    public void setIsJailbreak(String isJailbreak) {
        IsJailbreak = isJailbreak;
    }

    public String getApplicationPackageName() {
        return ApplicationPackageName;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        ApplicationPackageName = applicationPackageName;
    }

    public String getApplicationName() {
        return ApplicationName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public String getApplicationVersionCode() {
        return applicationVersionCode;
    }

    public void setApplicationVersionCode(String applicationVersion) {
        this.applicationVersionCode = applicationVersion;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String getApplicationChannel() {
        return applicationChannel;
    }

    public void setApplicationChannel(String applicationChannel) {
        this.applicationChannel = applicationChannel;
    }

    public String getApplicationUserId() {
        return applicationUserId;
    }

    public void setApplicationUserId(String applicationUserId) {
        this.applicationUserId = applicationUserId;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
    }

    public String getMobileOperator() {
        return mobileOperator;
    }

    public void setMobileOperator(String mobileOperator) {
        this.mobileOperator = mobileOperator;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getAppMD5() {
        return AppMD5;
    }

    public void setAppMD5(String appMD5) {
        AppMD5 = appMD5;
    }

    public String getAppSign() {
        return AppSign;
    }

    public void setAppSign(String appSign) {
        AppSign = appSign;
    }

    @Override
    public String toString() {
        return "DriverInfo [deviceId=" + deviceId + ", deviceBrand="
                + deviceBrand + ", deviceModel=" + deviceModel
                + ", systemVersion=" + systemVersion + ", applicationVersion="
                + applicationVersionCode + ", ApplicationPackageName="
                + ApplicationPackageName + ", ApplicationName="
                + ApplicationName + ", applicationKey=" + applicationKey
                + ", applicationChannel=" + applicationChannel
                + ", applicationUserId=" + applicationUserId + ", sdkVersion="
                + sdkVersion + ", apiLevel=" + apiLevel + ", mobileOperator="
                + mobileOperator + ", phoneNum=" + phoneNum + "]";
    }

}
