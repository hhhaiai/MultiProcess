package com.analysys.dev.model;

import org.json.JSONObject;

public class ResponseCtrlInfo {
    private static final long serialVersionUID = 1L;
    private String Module;
    private String Status;
    private String DefaultFreq;
    private String MinFreq;
    private String MaxFreq;
    private JSONObject SubControl;
    private String SubModule;
    private String SubStatus;
    private String SubDefaultFreq;
    private String SubMinFreq;
    private String SubMaxFreq;
    private String Count;

    private String Mode;
    private String Command;
    private String Match;
    private String Result;
    private String StayModule;
    private String StayName;
    private static class Holder {
        private static final ResponseCtrlInfo INSTANCE = new ResponseCtrlInfo();
    }
    public static ResponseCtrlInfo getInstance() {
        return Holder.INSTANCE;
    }
    public String getModule() {
        return Module;
    }

    public String getStatus() {
        return Status;
    }

    public String getDefaultFreq() {
        return DefaultFreq;
    }

    public String getMinFreq() {
        return MinFreq;
    }

    public String getMaxFreq() {
        return MaxFreq;
    }

    public JSONObject getSubControl() {
        return SubControl;
    }

    public String getSubModule() {
        return SubModule;
    }

    public String getSubStatus() {
        return SubStatus;
    }

    public String getSubDefaultFreq() {
        return SubDefaultFreq;
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

    public String getMode() {
        return Mode;
    }

    public String getCommand() {
        return Command;
    }

    public String getMatch() {
        return Match;
    }

    public String getResult() {
        return Result;
    }

    public String getStayModule() {
        return StayModule;
    }

    public String getStayName() {
        return StayName;
    }

    public void setModule(String module) {
        Module = module;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public void setDefaultFreq(String defaultFreq) {
        DefaultFreq = defaultFreq;
    }

    public void setMinFreq(String minFreq) {
        MinFreq = minFreq;
    }

    public void setMaxFreq(String maxFreq) {
        MaxFreq = maxFreq;
    }

    public void setSubControl(JSONObject subControl) {
        SubControl = subControl;
    }

    public void setSubModule(String subModule) {
        SubModule = subModule;
    }

    public void setSubStatus(String subStatus) {
        SubStatus = subStatus;
    }

    public void setSubDefaultFreq(String subDefaultFreq) {
        SubDefaultFreq = subDefaultFreq;
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

    public void setMode(String mode) {
        Mode = mode;
    }

    public void setCommand(String command) {
        Command = command;
    }

    public void setMatch(String match) {
        Match = match;
    }

    public void setResult(String result) {
        Result = result;
    }

    public void setStayModule(String stayModule) {
        StayModule = stayModule;
    }

    public void setStayName(String stayName) {
        StayName = stayName;
    }
}
