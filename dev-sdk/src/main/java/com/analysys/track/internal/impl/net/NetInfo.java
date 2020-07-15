package com.analysys.track.internal.impl.net;

import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 网络信息
 * @Version: 1.0
 * @Create: 2019-10-15 15:48:01
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class NetInfo {

    public String pkgname;
    public String appname;
    public List<ScanningInfo> scanningInfos;
    public boolean isOpen = false;

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb
                .append("appname:").append(appname).append("; ")
                .append("pkgname:").append(pkgname).append("; ")
                .append("scanningInfos:").append(scanningInfos).append("; ")
                .append("isOpen:").append(isOpen)
        ;
        return sb.toString();
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("APN", pkgname);
            object.put("AN", appname);
            JSONArray array = new JSONArray();
            if (scanningInfos != null && scanningInfos.size() > 0) {
                for (ScanningInfo scanningInfo : scanningInfos
                ) {
                    array.put(scanningInfo.toJson(false));
                }
                object.put("SINF", array);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return object;
    }

    public static NetInfo fromJson(JSONObject jsonObject) throws JSONException {
        NetInfo info = new NetInfo();
        info.pkgname = jsonObject.optString("APN");
        info.appname = jsonObject.optString("AN");
        JSONArray array = jsonObject.optJSONArray("SINF");
        if (array != null) {
            info.scanningInfos = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = (JSONObject) array.get(i);
                info.scanningInfos.add(ScanningInfo.fromJson(object));
            }
        }
        return info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NetInfo info = (NetInfo) o;

        return pkgname != null ? pkgname.equals(info.pkgname) : info.pkgname == null;
    }

    @Override
    public int hashCode() {
        return pkgname != null ? pkgname.hashCode() : 0;
    }

    public static class ScanningInfo {
        public String pkgname;
        public String appname;
        public String api_4;
        public JSONArray proc_56;
        public String usm;
        public long time;
        public List<TcpInfo> tcpInfos;

        public static ScanningInfo fromJson(JSONObject object) throws JSONException {
            ScanningInfo scanningInfo = new ScanningInfo();

            scanningInfo.pkgname = object.optString("APN");
            scanningInfo.appname = object.optString("AN");
            scanningInfo.api_4 = object.optString("API4");
            scanningInfo.proc_56 = object.optJSONArray("PROC56");
            scanningInfo.usm = object.optString("USM");
            scanningInfo.time = object.optLong("ST");
            scanningInfo.tcpInfos = new ArrayList<>();
            JSONArray array = object.optJSONArray("CONNS");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    scanningInfo.tcpInfos.add(TcpInfo.fromJson((JSONObject) array.get(i)));
                }
            }

            return scanningInfo;
        }

        /**
         * 实体类转json对象
         *
         * @param hasPA 是否包含 pkgname appname 字段 存的时候包含,上传的时候为了减少数据,不包含
         * @return
         */
        public JSONObject toJson(boolean hasPA) {
            JSONObject object = new JSONObject();
            try {
                if (hasPA) {
                    object.put("APN", pkgname);
                    object.put("AN", appname);
                }
                object.put("ST", time);
                if (tcpInfos == null || tcpInfos.size() == 0) {
                    return object;
                }
                JSONArray array = new JSONArray();
                for (TcpInfo tcpInfo : tcpInfos
                ) {
                    array.put(tcpInfo.toJson());
                }
                object.put("CONNS", array);
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
            return object;
        }
    }

    public static class TcpInfo {
        public String protocol;
        public String local_addr;
        public String remote_addr;
        public String socket_type;

        public static TcpInfo fromJson(JSONObject object) {
            TcpInfo tcpInfo = new TcpInfo();

            tcpInfo.protocol = object.optString("PTL");
            tcpInfo.local_addr = object.optString("CAD");
            tcpInfo.remote_addr = object.optString("RAD");
            tcpInfo.socket_type = object.optString("TP");
            return tcpInfo;
        }

        public JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                if (SPHelper.getBooleanValueFromSP(EContextHelper.getContext(), "NETINFO_PTL", true)) {
                    object.put("PTL", protocol);
                }
                if (SPHelper.getBooleanValueFromSP(EContextHelper.getContext(), "NETINFO_CAD", true)) {
                    object.put("CAD", local_addr);
                }
                if (SPHelper.getBooleanValueFromSP(EContextHelper.getContext(), "NETINFO_RAD", true)) {
                    object.put("RAD", remote_addr);
                }
                if (SPHelper.getBooleanValueFromSP(EContextHelper.getContext(), "NETINFO_TP", true)) {
                    object.put("TP", socket_type);
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
            return object;
        }
    }
}
