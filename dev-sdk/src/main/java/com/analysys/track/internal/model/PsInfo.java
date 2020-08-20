package com.analysys.track.internal.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private List<MdsBean> mds;

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

    public List<MdsBean> getMds() {
        return mds;
    }

    public void setMds(List<MdsBean> mds) {
        this.mds = mds;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.putOpt("version", version);
            jsonObject.putOpt("sign", sign);
            jsonObject.putOpt("data", data);
            jsonObject.putOpt("savePath", savePath);
            if (mds != null) {
                JSONArray array = new JSONArray();
                for (int i = 0; i < mds.size(); i++) {
                    MdsBean mdsBean = mds.get(i);
                    if (mdsBean == null) {
                        continue;
                    }
                    array.put(mdsBean.toJson());
                }
                jsonObject.put("mds", array);
            }
        } catch (Throwable e) {
            //JSONException
        }
        return jsonObject;
    }

    public static PsInfo fromJson(JSONObject jsonObject) {
        PsInfo bean = new PsInfo();
        try {
            JSONArray jsonArray = jsonObject.optJSONArray("mds");
            if (jsonArray != null) {
                List<MdsBean> mdsBeans = new ArrayList<>(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    mdsBeans.add(MdsBean.fromJson(object));
                }
                bean.setMds(mdsBeans);
            }

            bean.setSavePath((String) jsonObject.opt("savePath"));
            bean.setSign((String) jsonObject.opt("sign"));
            bean.setData((String) jsonObject.opt("data"));
            bean.setVersion((String) jsonObject.opt("version"));
        } catch (Throwable e) {
            //JSONException
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
        return mds != null ? mds.equals(psInfo.mds) : psInfo.mds == null;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (sign != null ? sign.hashCode() : 0);
        result = 31 * result + (savePath != null ? savePath.hashCode() : 0);
        result = 31 * result + (mds != null ? mds.hashCode() : 0);
        return result;
    }

    public static class MdsBean {
        /**
         * mn : test2
         * as : ctx|123456
         * cg : ctx|i
         * cn : com.test2
         * type : 0
         */

        private String mn;
        private String as;
        private String cg;
        private String cn;
        private String type;

        public String getMn() {
            return mn;
        }

        public void setMn(String mn) {
            this.mn = mn;
        }

        public String getAs() {
            return as;
        }

        public void setAs(String as) {
            this.as = as;
        }

        public String getCg() {
            return cg;
        }

        public void setCg(String cg) {
            this.cg = cg;
        }

        public String getCn() {
            return cn;
        }

        public void setCn(String cn) {
            this.cn = cn;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public JSONObject toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt("mn", mn);
                jsonObject.putOpt("as", as);
                jsonObject.putOpt("cg", cg);
                jsonObject.putOpt("cn", cn);
                jsonObject.putOpt("type", type);
            } catch (Throwable e) {
                //JSONException
            }
            return jsonObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MdsBean mdsBean = (MdsBean) o;

            if (mn != null ? !mn.equals(mdsBean.mn) : mdsBean.mn != null) return false;
            if (as != null ? !as.equals(mdsBean.as) : mdsBean.as != null) return false;
            if (cg != null ? !cg.equals(mdsBean.cg) : mdsBean.cg != null) return false;
            if (cn != null ? !cn.equals(mdsBean.cn) : mdsBean.cn != null) return false;
            return type != null ? type.equals(mdsBean.type) : mdsBean.type == null;
        }

        @Override
        public int hashCode() {
            int result = mn != null ? mn.hashCode() : 0;
            result = 31 * result + (as != null ? as.hashCode() : 0);
            result = 31 * result + (cg != null ? cg.hashCode() : 0);
            result = 31 * result + (cn != null ? cn.hashCode() : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }

        public static MdsBean fromJson(JSONObject jsonObject) {
            MdsBean bean = new MdsBean();
            try {
                bean.setMn((String) jsonObject.opt("mn"));
                bean.setAs((String) jsonObject.opt("as"));
                bean.setCg((String) jsonObject.opt("cg"));
                bean.setCn((String) jsonObject.opt("cn"));
                bean.setType((String) jsonObject.opt("type"));
            } catch (Throwable e) {
                //JSONException
            }
            return bean;
        }

    }
}
