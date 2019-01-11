package com.analysys.dev.model;

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
    private long ServerDelay;
    /**
     *上传失败次数
     */
    private int FailCount;
    /**
     *上传失败后延迟时间
     */
    private long FailTryDelay;
    /**
     *客户端上传时间间隔
     */
    private long TimerInterval;
    /**
     *客户端上传时数据条数
     */
    private int EventCount;
    /**
     *是否使用实时策略，1不使用0使用
     */
    private int UseRTP;
    /**
     *是否实时上传[非实时分析策略下，是否实时上传]0不实时上传，1实时上传
     */
    private int UseRTL;
    /**
     *是否采集公网ip
     */
    private int Remotelp;
    /**
     *是否上传敏感数据，1不上传，0上传
     */
    private int UploadSD;
    /**
     *数据合并间隔
     */
    private long MergeInterval;
    /**
     *最小使用时长
     */
    private long MinDuration;
    /**
     *最常使用时长
     */
    private long MaxDuration;
    /**
     *域名更新次数
     */
    private int DomainUpdateTimes;


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
}
