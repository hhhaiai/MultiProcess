package com.analysys.track.model;

import com.analysys.track.internal.Content.EGContext;

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
    private long FailTryDelay = EGContext.FAIL_TRY_DELAY_DEFALUT;
    /**
     * 客户端上传时间间隔
     */
    private long TimerInterval = EGContext.UPLOAD_CYCLE;
    /**
     * 动态采集模块
     */
    private JSONArray CtrlList;
    private String Module;
    private String Status;
    /**
     *是否使用实时策略，1不使用0使用
     */
//    private int UseRTP = EGContext.DEFAULT;
    /**
     *是否实时上传[非实时分析策略下，是否实时上传]0不实时上传，1实时上传
     */
//    private int UseRTL = EGContext.DEFAULT;
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

//    public int isUseRTP() {
//        return UseRTP;
//    }

//    public int isUseRTL() {
//        return UseRTL;
//    }

//    public int getUseRTP() {
//        return UseRTP;
//    }

//    public int getUseRTL() {
//        return UseRTL;
//    }

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

//    public void setUseRTP(int useRTP) {
//        UseRTP = useRTP;
//    }

//    public void setUseRTL(int useRTL) {
//        UseRTL = useRTL;
//    }

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

    private static class Holder {
        private static final PolicyInfo INSTANCE = new PolicyInfo();
    }
}
