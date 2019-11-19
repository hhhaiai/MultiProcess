package com.device.activitys;

import org.json.JSONObject;

public class OCInfo {
    long closeTime;
    long openTime;
    String pkgName;
    String appName;
    String versionCode;
    String SwitchType;

    public OCInfo(long openTime, String pkgName) {
        this.openTime = openTime;
        this.pkgName = pkgName;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.putOpt("closeTime", closeTime);
            jsonObject.putOpt("openTime", openTime);
            jsonObject.putOpt("pkgName", pkgName);
            jsonObject.putOpt("appName", appName);
            jsonObject.putOpt("versionCode", versionCode);
            jsonObject.putOpt("SwitchType", SwitchType);
        } catch (Throwable e) {
            //JSONException
        }
        return jsonObject;
    }

    public long getCloseTime() {
        return closeTime;
    }

    public long getOpenTime() {
        return openTime;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getAppName() {
        return appName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getSwitchType() {
        return SwitchType;
    }

    public void setCloseTime(long closeTime) {
        this.closeTime = closeTime;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public void setSwitchType(String switchType) {
        SwitchType = switchType;
    }
}
