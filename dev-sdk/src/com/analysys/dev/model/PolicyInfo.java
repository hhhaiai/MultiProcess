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
}
