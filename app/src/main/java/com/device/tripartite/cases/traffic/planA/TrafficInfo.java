package com.device.tripartite.cases.traffic.planA;

import android.graphics.drawable.Drawable;

class TrafficInfo {
    private String pkgName = null;
    private String appname = null;
    private long uidTxBytes = 0L;
    private long uidRxBytes = 0L;

    public void setPackname(String packageName) {
        pkgName = packageName;
    }

    public void setAppname(String a) {
        appname = a;
    }

    public void setRx(long rx) {
        uidTxBytes = rx;
    }

    public void setTx(long tx) {
        uidTxBytes = tx;
    }

    @Override
    public String toString() {
        return new StringBuffer().append(appname).append("[").append(pkgName).append("]")
                .append(", 上行: ").append(uidRxBytes)
                .append(", 下行: ").append(uidTxBytes)
                .toString();
    }

    public void setIcon(Drawable loadIcon) {
    }
}
