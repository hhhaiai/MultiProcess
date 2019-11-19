package com.analysys.track.internal.impl.usm;

import org.json.JSONObject;

public class USMInfo {
    private long closeTime;
    private long openTime;
    private String pkgName;
    private String appName;
    private String versionCode;
    private String netType;
    private final ExtendedMap extendedMap = new ExtendedMap();

    public USMInfo(long openTime, String pkgName) {
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
            jsonObject.putOpt("ACT", closeTime);
            jsonObject.putOpt("AOT", openTime);
            jsonObject.putOpt("APN", pkgName);
            jsonObject.putOpt("AN", appName);
            jsonObject.putOpt("AVC", versionCode);
            jsonObject.putOpt("NT", netType);
            jsonObject.putOpt("ETDM", extendedMap.toJson());
        } catch (Throwable e) {
            //JSONException
        }
        return jsonObject;
    }

    private class ExtendedMap {
        private String collectionType;
        private String applicationType;
        private String switchType;

        @Override
        public String toString() {
            return toJson().toString();
        }

        public JSONObject toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt("CT", collectionType);
                jsonObject.putOpt("AT", applicationType);
                jsonObject.putOpt("ST", switchType);
            } catch (Throwable e) {
                //JSONException
            }
            return jsonObject;
        }


    }

    public String getNetType() {
        return netType;
    }

    public void setNetType(String netType) {
        this.netType = netType;
    }

    public String getCollectionType() {
        return extendedMap.collectionType;
    }

    public void setCollectionType(String collectionType) {
        extendedMap.collectionType = collectionType;
    }

    public String getApplicationType() {
        return extendedMap.applicationType;
    }

    public void setApplicationType(String applicationType) {
        extendedMap.applicationType = applicationType;
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
        return extendedMap.switchType;
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
        extendedMap.switchType = switchType;
    }
}
