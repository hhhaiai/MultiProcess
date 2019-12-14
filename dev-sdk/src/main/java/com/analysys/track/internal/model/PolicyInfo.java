package com.analysys.track.internal.model;

import com.analysys.track.internal.content.EGContext;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * 策略相关信息
 */
public class PolicyInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private static PolicyInfo info = null;
    /**
     * 状态回执
     */
    private String code;
    /**
     * 策略版本
     */
    private String PolicyVer = "";
    /**
     * 服务器延迟上传时间
     */
    private int ServerDelay = EGContext.SERVER_DELAY_DEFAULT;
    /**
     * 上传失败后重试，最大上传次数
     */
    private int FailCount = EGContext.FAIL_COUNT_DEFALUT;
    /**
     * 上传失败后延迟时间
     */
    private long FailTryDelay = EGContext.TIME_MINUTE;
    /**
     * 客户端上传时间间隔
     */
    private long TimerInterval = EGContext.TIME_HOUR * 6;
    /**
     * 动态采集模块
     */
    private JSONArray CtrlList;
    private String Module;
    private String Status;
    private String SecModule;
    private String SecStatus;
    private String DeuFreq;
    private String MinFreq;
    private String MaxFreq;
    private String MaxCount;
    private JSONArray SubControl;
    private String SubModule;
    private String SubStatus;
    private String SubDeuFreq;
    private String SubMinFreq;
    private String SubMaxFreq;
    private String Count;
    private String SecSubModule;
    private String SecSubStatus;

    /**
     * 热更新部分代码
     */
    private String mHotfixData;
    private String mHotfixSign;
    private String mHotfixVersion;
    private String mHotfixMethons;


    private PolicyInfo() {
    }

    public static PolicyInfo getInstance() {
        if (info == null) {
            info = PolicyInfo.Holder.INSTANCE;
        }
        return info;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPolicyVer() {
        return PolicyVer;
    }

    public void setPolicyVer(String policyVer) {
        PolicyVer = policyVer;
    }

    public int getServerDelay() {
        return ServerDelay;
    }

    public void setServerDelay(int serverDelay) {
        ServerDelay = serverDelay;
    }

    public int getFailCount() {
        return FailCount;
    }


    public void setFailCount(int failCount) {
        FailCount = failCount;
    }

    public long getFailTryDelay() {
        return FailTryDelay;
    }

    public void setFailTryDelay(long failTryDelay) {
        FailTryDelay = failTryDelay;
    }

    public long getTimerInterval() {
        return TimerInterval;
    }

    public void setTimerInterval(long timerInterval) {
        TimerInterval = timerInterval;
    }

    public JSONArray getCtrlList() {
        return CtrlList;
    }

    public void setCtrlList(JSONArray ctrlList) {
        CtrlList = ctrlList;
    }


    public String getModule() {
        return Module;
    }

    public void setModule(String module) {
        Module = module;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getSecModule() {
        return SecModule;
    }

    public void setSecModule(String secModule) {
        SecModule = secModule;
    }

    public String getSecStatus() {
        return SecStatus;
    }

    public void setSecStatus(String secStatus) {
        SecStatus = secStatus;
    }

    public String getDeuFreq() {
        return DeuFreq;
    }

    public void setDeuFreq(String deuFreq) {
        DeuFreq = deuFreq;
    }

    public String getMinFreq() {
        return MinFreq;
    }

    public void setMinFreq(String minFreq) {
        MinFreq = minFreq;
    }

    public String getMaxFreq() {
        return MaxFreq;
    }

    public void setMaxFreq(String maxFreq) {
        MaxFreq = maxFreq;
    }

    public String getMaxCount() {
        return MaxCount;
    }

    public void setMaxCount(String maxCount) {
        MaxCount = maxCount;
    }

    public JSONArray getSubControl() {
        return SubControl;
    }

    public void setSubControl(JSONArray subControl) {
        SubControl = subControl;
    }

    public String getSubModule() {
        return SubModule;
    }

    public void setSubModule(String subModule) {
        SubModule = subModule;
    }

    public String getSubStatus() {
        return SubStatus;
    }

    public void setSubStatus(String subStatus) {
        SubStatus = subStatus;
    }

    public String getSubDeuFreq() {
        return SubDeuFreq;
    }

    public void setSubDeuFreq(String subDeuFreq) {
        SubDeuFreq = subDeuFreq;
    }

    public String getSubMinFreq() {
        return SubMinFreq;
    }

    public void setSubMinFreq(String subMinFreq) {
        SubMinFreq = subMinFreq;
    }

    public String getSubMaxFreq() {
        return SubMaxFreq;
    }

    public void setSubMaxFreq(String subMaxFreq) {
        SubMaxFreq = subMaxFreq;
    }

    public String getCount() {
        return Count;
    }

    public void setCount(String count) {
        Count = count;
    }

    public String getSecSubModule() {
        return SecSubModule;
    }

    public void setSecSubModule(String secSubModule) {
        SecSubModule = secSubModule;
    }

    public String getSecSubStatus() {
        return SecSubStatus;
    }

    public void setSecSubStatus(String secSubStatus) {
        SecSubStatus = secSubStatus;
    }


    /**
     * 热更新部分代码.
     */
    public String getHotfixData() {
        return mHotfixData;
    }

    public void setHotfixData(String hotfixData) {
        this.mHotfixData = hotfixData;
    }

    public String getHotfixSign() {
        return mHotfixSign;
    }

    public void setHotfixSign(String hotfixSign) {
        this.mHotfixSign = hotfixSign;
    }

    public String getHotfixVersion() {
        return mHotfixVersion;
    }

    public void setHotfixVersion(String hotfixVersion) {
        this.mHotfixVersion = hotfixVersion;
    }

    public String getHotfixMethons() {
        return mHotfixMethons;
    }

    public void setHotfixMethons(String hotfixMethons) {
        this.mHotfixMethons = hotfixMethons;
    }

    /**
     * 清除内存数据
     */
    public void clearMemoryData() {
        //  清理比较大的内存变量
        setHotfixMethons("");
        setHotfixData("");
    }

    private static class Holder {
        private static final PolicyInfo INSTANCE = new PolicyInfo();
    }
}
