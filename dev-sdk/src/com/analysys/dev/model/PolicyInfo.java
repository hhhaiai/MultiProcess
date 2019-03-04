package com.analysys.dev.model;

import com.analysys.dev.internal.Content.EGContext;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * 策略相关信息
 */
public class PolicyInfo implements Serializable {
    private static class Holder {
        private static final PolicyInfo INSTANCE = new PolicyInfo();
    }
    public static PolicyInfo getInstance() {
        return PolicyInfo.Holder.INSTANCE;
    }
    private static final long serialVersionUID = 1L;
    /**
     * 状态回执
     */
    private String code;
    /**
     * 基础发送，策略相关的
     */
    private String Policy;
    /**
     * 策略版本
     */
    private String PolicyVer;
    /**
     * 服务器延迟上传时间
     */
    private long ServerDelay = EGContext.DEFAULT;
    /**
     *上传失败次数
     */
    private int FailCount = EGContext.DEFAULT;
    /**
     *上传失败后延迟时间
     */
    private long FailTryDelay = EGContext.DEFAULT;
    /**
     *客户端上传时间间隔
     */
    private long TimerInterval = EGContext.DEFAULT;
    /**
     *客户端上传时数据条数
     */
    private int EventCount = EGContext.DEFAULT;
    /**
     *是否使用实时策略，1不使用0使用
     */
    private int UseRTP = EGContext.DEFAULT;
    /**
     *是否实时上传[非实时分析策略下，是否实时上传]0不实时上传，1实时上传
     */
    private int UseRTL = EGContext.DEFAULT;
    /**
     *是否采集公网ip
     */
    private int Remotelp = EGContext.DEFAULT;
    /**
     *是否上传敏感数据，1不上传，0上传
     */
    private int UploadSD = EGContext.DEFAULT;
    /**
     *数据合并间隔
     */
    private long MergeInterval = EGContext.DEFAULT;
    /**
     *最小使用时长
     */
    private long MinDuration = EGContext.DEFAULT;
    /**
     *最常使用时长
     */
    private long MaxDuration = EGContext.DEFAULT;
    /**
     *域名更新次数
     */
    private int DomainUpdateTimes = EGContext.DEFAULT;

    /**
     * 动态采集模块
     */
    private JSONArray CtrlList ;

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

    public String getCode() {
        return code;
    }

    public String getPolicy() {
        return Policy;
    }

    public String getPolicyVer() {
        return PolicyVer;
    }

    public long getServerDelay() {
        return ServerDelay;
    }

    public int getFailCount() {
        return FailCount;
    }

    public long getFailTryDelay() {
        return FailTryDelay;
    }

    public long getTimerInterval() {
        return TimerInterval;
    }

    public int getEventCount() {
        return EventCount;
    }

    public int isUseRTP() {
        return UseRTP;
    }

    public int isUseRTL() {
        return UseRTL;
    }

    public int getRemotelp() {
        return Remotelp;
    }

    public int getUploadSD() {
        return UploadSD;
    }

    public long getMergeInterval() {
        return MergeInterval;
    }

    public long getMinDuration() {
        return MinDuration;
    }

    public long getMaxDuration() {
        return MaxDuration;
    }

    public int getDomainUpdateTimes() {
        return DomainUpdateTimes;
    }

    public int getUseRTP() {
        return UseRTP;
    }

    public int getUseRTL() {
        return UseRTL;
    }

    public JSONArray getCtrlList() {
        return CtrlList;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setPolicy(String policy) {
        Policy = policy;
    }

    public void setPolicyVer(String policyVer) {
        PolicyVer = policyVer;
    }

    public void setServerDelay(long serverDelay) {
        ServerDelay = serverDelay;
    }

    public void setFailCount(int failCount) {
        FailCount = failCount;
    }

    public void setFailTryDelay(long failTryDelay) {
        FailTryDelay = failTryDelay;
    }

    public void setTimerInterval(long timerInterval) {
        TimerInterval = timerInterval;
    }

    public void setEventCount(int eventCount) {
        EventCount = eventCount;
    }

    public void setUseRTP(int useRTP) {
        UseRTP = useRTP;
    }

    public void setUseRTL(int useRTL) {
        UseRTL = useRTL;
    }

    public void setRemotelp(int remotelp) {
        Remotelp = remotelp;
    }

    public void setUploadSD(int uploadSD) {
        UploadSD = uploadSD;
    }

    public void setMergeInterval(long mergeInterval) {
        MergeInterval = mergeInterval;
    }

    public void setMinDuration(long minDuration) {
        MinDuration = minDuration;
    }

    public void setMaxDuration(long maxDuration) {
        MaxDuration = maxDuration;
    }

    public void setDomainUpdateTimes(int domainUpdateTimes) {
        DomainUpdateTimes = domainUpdateTimes;
    }

    public void setCtrlList(JSONArray ctrlList) {
        CtrlList = ctrlList;
    }

    public String getModule() {
        return Module;
    }

    public String getStatus() {
        return Status;
    }

    public String getSecModule() {
        return SecModule;
    }

    public String getSecStatus() {
        return SecStatus;
    }

    public String getDeuFreq() {
        return DeuFreq;
    }

    public String getMinFreq() {
        return MinFreq;
    }

    public String getMaxFreq() {
        return MaxFreq;
    }

    public String getMaxCount() {
        return MaxCount;
    }

    public JSONArray getSubControl() {
        return SubControl;
    }

    public String getSubModule() {
        return SubModule;
    }

    public String getSubStatus() {
        return SubStatus;
    }

    public String getSubDeuFreq() {
        return SubDeuFreq;
    }

    public String getSubMinFreq() {
        return SubMinFreq;
    }

    public String getSubMaxFreq() {
        return SubMaxFreq;
    }

    public String getCount() {
        return Count;
    }

    public String getSecSubModule() {
        return SecSubModule;
    }

    public String getSecSubStatus() {
        return SecSubStatus;
    }

    public void setModule(String module) {
        Module = module;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public void setSecModule(String secModule) {
        SecModule = secModule;
    }

    public void setSecStatus(String secStatus) {
        SecStatus = secStatus;
    }

    public void setDeuFreq(String deuFreq) {
        DeuFreq = deuFreq;
    }

    public void setMinFreq(String minFreq) {
        MinFreq = minFreq;
    }

    public void setMaxFreq(String maxFreq) {
        MaxFreq = maxFreq;
    }

    public void setMaxCount(String maxCount) {
        MaxCount = maxCount;
    }

    public void setSubControl(JSONArray subControl) {
        SubControl = subControl;
    }

    public void setSubModule(String subModule) {
        SubModule = subModule;
    }

    public void setSubStatus(String subStatus) {
        SubStatus = subStatus;
    }

    public void setSubDeuFreq(String subDeuFreq) {
        SubDeuFreq = subDeuFreq;
    }

    public void setSubMinFreq(String subMinFreq) {
        SubMinFreq = subMinFreq;
    }

    public void setSubMaxFreq(String subMaxFreq) {
        SubMaxFreq = subMaxFreq;
    }

    public void setCount(String count) {
        Count = count;
    }

    public void setSecSubModule(String secSubModule) {
        SecSubModule = secSubModule;
    }

    public void setSecSubStatus(String secSubStatus) {
        SecSubStatus = secSubStatus;
    }
}
