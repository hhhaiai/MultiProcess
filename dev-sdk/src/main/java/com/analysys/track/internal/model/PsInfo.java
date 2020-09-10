package com.analysys.track.internal.model;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BugReportForTest;

import org.json.JSONObject;

public class PsInfo {

    /**
     * version : 000
     * sign : 0835f7b53e49260b2e98f6d515de8651
     * data : 0835f7b53e49260b2e98f6d515de8651
     * savePath : /sdcard/xxx/
     * mds : [{"mn":"test2","as":"ctx|123456","cg":"ctx|i","cn":"com.test2","type":"0"},{"mn":"test","as":"ctx|HelloWorld","cg":"ctx|s","cn":"com.test","type":"1"}]
     */

    private String version;
    private String sign;
    private String data;
    private String savePath;
    private String cn;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.putOpt("version", version);
            jsonObject.putOpt("sign", sign);
            jsonObject.putOpt("data", data);
            jsonObject.putOpt("savePath", savePath);
            jsonObject.putOpt("cn", savePath);
        } catch (Throwable e) {
            //JSONException
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return jsonObject;
    }

    public static PsInfo fromJson(JSONObject jsonObject) {
        PsInfo bean = new PsInfo();
        try {


            bean.setSavePath((String) jsonObject.opt("savePath"));
            bean.setSign((String) jsonObject.opt("sign"));
            bean.setData((String) jsonObject.opt("data"));
            bean.setVersion((String) jsonObject.opt("version"));
            bean.setCn((String) jsonObject.opt("cn"));
        } catch (Throwable e) {
            //JSONException
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return bean;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PsInfo psInfo = (PsInfo) o;

        if (version != null ? !version.equals(psInfo.version) : psInfo.version != null) {
            return false;
        }
        if (sign != null ? !sign.equals(psInfo.sign) : psInfo.sign != null) {
            return false;
        }
        if (savePath != null ? !savePath.equals(psInfo.savePath) : psInfo.savePath != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (sign != null ? sign.hashCode() : 0);
        result = 31 * result + (savePath != null ? savePath.hashCode() : 0);
        return result;
    }
}
