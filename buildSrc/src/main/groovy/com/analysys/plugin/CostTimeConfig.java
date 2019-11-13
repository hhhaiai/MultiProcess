package com.analysys.plugin;

public class CostTimeConfig {
    boolean enable = true;
    boolean costAll = false;
    int timeout = 10;
    boolean enableLog = true;

    public void enable(boolean enable) {
        this.enable = enable;
    }

    public void costAll(boolean costAll) {
        this.costAll = costAll;
    }

    public void timeout(int timeout) {
        this.timeout = timeout;
    }

    public void enableLog(boolean enableLog) {
        this.enableLog = enableLog;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"enable\":")
                .append(enable);
        sb.append(",\"costAll\":")
                .append(costAll);
        sb.append(",\"timeout\":")
                .append(timeout);
        sb.append(",\"enableLog\":")
                .append(enableLog);
        sb.append('}');
        return sb.toString();
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isCostAll() {
        return costAll;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isEnableLog() {
        return enableLog;
    }
}
