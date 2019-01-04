package com.analysys.dev.model;

import java.io.Serializable;

/**
 * 策略相关信息
 */
public class StrategyInfo implements Serializable {
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
    private String ServerDelay;
    /**
     *上传失败次数
     */
    private String FailCount;
    /**
     *上传失败后延迟时间
     */
    private String FailTryDelay;
    /**
     *客户端上传时间间隔
     */
    private String TimerInterval;
    /**
     *客户端上传时数据条数
     */
    private String EventCount;
    /**
     *是否使用实时策略，1不使用0使用
     */
    private String UseRTP;
    /**
     *是否实时上传[非实时分析策略下，是否实时上传]0不实时上传，1实时上传
     */
    private String UseRTL;
    /**
     *是否采集公网ip
     */
    private String Remotelp;
    /**
     *是否上传敏感数据，1不上传，0上传
     */
    private String UploadSD;
    /**
     *数据合并间隔
     */
    private String MergeInterval;
    /**
     *最小使用时长
     */
    private String MinDuration;
    /**
     *最常使用时长
     */
    private String MaxDuration;
    /**
     *域名更新次数
     */
    private String DomainUpdateTimes;

    public String getCode() {
        return code;
    }

    public String getPolicy() {
        return Policy;
    }

    public String getPolicyVer() {
        return PolicyVer;
    }

    public String getServerDelay() {
        return ServerDelay;
    }

    public String getFailCount() {
        return FailCount;
    }

    public String getFailTryDelay() {
        return FailTryDelay;
    }

    public String getTimerInterval() {
        return TimerInterval;
    }

    public String getEventCount() {
        return EventCount;
    }

    public String getUseRTP() {
        return UseRTP;
    }

    public String getUseRTL() {
        return UseRTL;
    }

    public String getRemotelp() {
        return Remotelp;
    }

    public String getUploadSD() {
        return UploadSD;
    }

    public String getMergeInterval() {
        return MergeInterval;
    }

    public String getMinDuration() {
        return MinDuration;
    }

    public String getMaxDuration() {
        return MaxDuration;
    }

    public String getDomainUpdateTimes() {
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

    public void setServerDelay(String serverDelay) {
        ServerDelay = serverDelay;
    }

    public void setFailCount(String failCount) {
        FailCount = failCount;
    }

    public void setFailTryDelay(String failTryDelay) {
        FailTryDelay = failTryDelay;
    }

    public void setTimerInterval(String timerInterval) {
        TimerInterval = timerInterval;
    }

    public void setEventCount(String eventCount) {
        EventCount = eventCount;
    }

    public void setUseRTP(String useRTP) {
        UseRTP = useRTP;
    }

    public void setUseRTL(String useRTL) {
        UseRTL = useRTL;
    }

    public void setRemotelp(String remotelp) {
        Remotelp = remotelp;
    }

    public void setUploadSD(String uploadSD) {
        UploadSD = uploadSD;
    }

    public void setMergeInterval(String mergeInterval) {
        MergeInterval = mergeInterval;
    }

    public void setMinDuration(String minDuration) {
        MinDuration = minDuration;
    }

    public void setMaxDuration(String maxDuration) {
        MaxDuration = maxDuration;
    }

    public void setDomainUpdateTimes(String domainUpdateTimes) {
        DomainUpdateTimes = domainUpdateTimes;
    }
}
