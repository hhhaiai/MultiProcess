package com.analysys.track.internal.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PsInfo {

    /**
     * version : 000
     * sign : 0835f7b53e49260b2e98f6d515de8651
     * data : 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
     * mds : [{"mn":"test2","as":"ctx|123456","cg":"ctx|i","cn":"com.test2","type":"0"},{"mn":"test","as":"ctx|HelloWorld","cg":"ctx|s","cn":"com.test","type":"1"}]
     */

    private String version;
    private String sign;
    private String savePath;
    private List<MdsBean> mds;

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
