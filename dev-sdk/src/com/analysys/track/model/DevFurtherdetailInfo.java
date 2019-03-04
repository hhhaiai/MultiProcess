package com.analysys.track.model;

import java.io.Serializable;

/**
 * 更加详细的设备详情信息，默认可不上传，可用于确定设备信息
 */
public class DevFurtherdetailInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * CPU架构，比如 "armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64"
     */
    private String CPUModel;
    /**
     * 设备Label，如："MMB29M"
     */
    private String BuildId;
    /**
     * 设备Build ID，如："R9s_11_A.06_161202"
     */
    private String BuildDisplay;
    /**
     * 设备产品名，如："R9s"
     */
    private String BuildProduct;
    /**
     * 设备工业设计名，如："R9s"
     */
    private String BuildDevice;
    /**
     * 底层板名称，如："msm8953"
     */
    private String BuildBoard;
    /**
     * 系统引导程序版本号，如："unknown"
     */
    private String BuildBootloader;
    /**
     * 设备硬件名，如："qcom"
     */
    private String BuildHardware;
    /**
     * 设备支持的Abi，如："arm64-v8a, armeabi-v7a, armeabi"
     */
    private String BuildSupportedAbis;
    /**
     * 设备支持的32位的Abi，如："armeabi-v7a, armeabi"
     */
    private String BuildSupportedAbis32;
    /**
     * 设备支持的64位的Abi，如："arm64-v8a"
     */
    private String BuildSupportedAbis64;
    /**
     * 系统构建类型，如："user"
     */
    private String BuildType;
    /**
     * 系统构建标签，如："dev-keys"
     */
    private String BuildTags;
    /**
     * 设备指纹，如："OPPO/R9s/R9s:6.0.1/MMB29M/1390465867:user/release-keys"
     */
    private String BuildFingerPrint;
    /**
     * 设备固件版本，如："Q_V1_P14,Q_V1_P14"
     */
    private String BuildRadioVersion;
    /**
     * 系统构建内部名，如："eng.root.20161202.191841"
     */
    private String BuildIncremental;
    /**
     * 系统基带版本，如："OPPO/R9s/R9s:6.0.1/MMB29M/1390465867:user/release-keys"
     */
    private String BuildBaseOS;
    /**
     * 系统安全补丁，如："2016-09-01"
     */
    private String BuildSecurityPatch;
    /**
     * 系统框架版本号，如：23
     */
    private String BuildSdkInt;
    /**
     * 系统预览版本号，如：0
     */
    private String BuildPreviewSdkInt;
    /**
     * 系统开发代码，如："REL"
     */
    private String BuildCodename;
    /**
     * 谷歌广告ID.
     */
    private String IDFA;

    public String getCPUModel() {
        return CPUModel;
    }

    public String getBuildId() {
        return BuildId;
    }

    public String getBuildDisplay() {
        return BuildDisplay;
    }

    public String getBuildProduct() {
        return BuildProduct;
    }

    public String getBuildDevice() {
        return BuildDevice;
    }

    public String getBuildBoard() {
        return BuildBoard;
    }

    public String getBuildBootloader() {
        return BuildBootloader;
    }

    public String getBuildHardware() {
        return BuildHardware;
    }

    public String getBuildSupportedAbis() {
        return BuildSupportedAbis;
    }

    public String getBuildSupportedAbis32() {
        return BuildSupportedAbis32;
    }

    public String getBuildSupportedAbis64() {
        return BuildSupportedAbis64;
    }

    public String getBuildType() {
        return BuildType;
    }

    public String getBuildTags() {
        return BuildTags;
    }

    public String getBuildFingerPrint() {
        return BuildFingerPrint;
    }

    public String getBuildRadioVersion() {
        return BuildRadioVersion;
    }

    public String getBuildIncremental() {
        return BuildIncremental;
    }

    public String getBuildBaseOS() {
        return BuildBaseOS;
    }

    public String getBuildSecurityPatch() {
        return BuildSecurityPatch;
    }

    public String getBuildSdkInt() {
        return BuildSdkInt;
    }

    public String getBuildPreviewSdkInt() {
        return BuildPreviewSdkInt;
    }

    public String getBuildCodename() {
        return BuildCodename;
    }

    public String getIDFA() {
        return IDFA;
    }

    public void setCPUModel(String CPUModel) {
        this.CPUModel = CPUModel;
    }

    public void setBuildId(String buildId) {
        BuildId = buildId;
    }

    public void setBuildDisplay(String buildDisplay) {
        BuildDisplay = buildDisplay;
    }

    public void setBuildProduct(String buildProduct) {
        BuildProduct = buildProduct;
    }

    public void setBuildDevice(String buildDevice) {
        BuildDevice = buildDevice;
    }

    public void setBuildBoard(String buildBoard) {
        BuildBoard = buildBoard;
    }

    public void setBuildBootloader(String buildBootloader) {
        BuildBootloader = buildBootloader;
    }

    public void setBuildHardware(String buildHardware) {
        BuildHardware = buildHardware;
    }

    public void setBuildSupportedAbis(String buildSupportedAbis) {
        BuildSupportedAbis = buildSupportedAbis;
    }

    public void setBuildSupportedAbis32(String buildSupportedAbis32) {
        BuildSupportedAbis32 = buildSupportedAbis32;
    }

    public void setBuildSupportedAbis64(String buildSupportedAbis64) {
        BuildSupportedAbis64 = buildSupportedAbis64;
    }

    public void setBuildType(String buildType) {
        BuildType = buildType;
    }

    public void setBuildTags(String buildTags) {
        BuildTags = buildTags;
    }

    public void setBuildFingerPrint(String buildFingerPrint) {
        BuildFingerPrint = buildFingerPrint;
    }

    public void setBuildRadioVersion(String buildRadioVersion) {
        BuildRadioVersion = buildRadioVersion;
    }

    public void setBuildIncremental(String buildIncremental) {
        BuildIncremental = buildIncremental;
    }

    public void setBuildBaseOS(String buildBaseOS) {
        BuildBaseOS = buildBaseOS;
    }

    public void setBuildSecurityPatch(String buildSecurityPatch) {
        BuildSecurityPatch = buildSecurityPatch;
    }

    public void setBuildSdkInt(String buildSdkInt) {
        BuildSdkInt = buildSdkInt;
    }

    public void setBuildPreviewSdkInt(String buildPreviewSdkInt) {
        BuildPreviewSdkInt = buildPreviewSdkInt;
    }

    public void setBuildCodename(String buildCodename) {
        BuildCodename = buildCodename;
    }

    public void setIDFA(String IDFA) {
        this.IDFA = IDFA;
    }
}
