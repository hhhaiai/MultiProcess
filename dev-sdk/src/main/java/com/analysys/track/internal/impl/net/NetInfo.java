package com.analysys.track.internal.impl.net;

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


    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("pkgname", pkgname);
            object.put("appname", appname);
            JSONArray array = new JSONArray();
            if (scanningInfos != null && scanningInfos.size() > 0) {
                for (ScanningInfo scanningInfo : scanningInfos
                ) {
                    array.put(scanningInfo.toJson());
                }
                object.put("scanningInfos", array);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static NetInfo fromJson(JSONObject jsonObject) throws JSONException {
        NetInfo info = new NetInfo();
        info.pkgname = jsonObject.optString("pkgname");
        info.appname = jsonObject.optString("appname");
        JSONArray array = jsonObject.optJSONArray("scanningInfos");
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
        public JSONObject proc_56;
        public String usm;
        public long time;
        public List<TcpInfo> tcpInfos;

        public static ScanningInfo fromJson(JSONObject object) throws JSONException {
            ScanningInfo scanningInfo = new ScanningInfo();

            scanningInfo.pkgname = object.optString("pkgname");
            scanningInfo.appname = object.optString("appname");
            scanningInfo.api_4 = object.optString("api_4");
            scanningInfo.proc_56 = object.optJSONObject("proc_56");
            scanningInfo.usm = object.optString("usm");
            scanningInfo.time = object.optLong("time");
            scanningInfo.tcpInfos = new ArrayList<>();
            JSONArray array = object.optJSONArray("tcpInfos");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    scanningInfo.tcpInfos.add(TcpInfo.fromJson((JSONObject) array.get(i)));
                }
            }

            return scanningInfo;
        }

        public JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                object.put("pkgname", pkgname);
                object.put("appname", appname);
                object.put("time", time);
                object.put("usm", usm);
                object.put("api_4", api_4);
                object.put("proc_56", proc_56);
                if (tcpInfos == null) {
                    return object;
                }
                JSONArray array = new JSONArray();
                for (TcpInfo tcpInfo : tcpInfos
                ) {
                    array.put(tcpInfo.toJson());
                }
                object.put("tcpInfos", array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }
    }

    public static class TcpInfo {
        public String protocol;
        public String local_addr;
        public String remote_addr;
        /**
         * 00  "ERROR_STATUS",
         * 01  "TCP_ESTABLISHED",
         * 02  "TCP_SYN_SENT",
         * 03  "TCP_SYN_RECV",
         * 04  "TCP_FIN_WAIT1",
         * 05  "TCP_FIN_WAIT2",
         * 06  "TCP_TIME_WAIT",
         * 07  "TCP_CLOSE",
         * 08  "TCP_CLOSE_WAIT",
         * 09  "TCP_LAST_ACK",
         * 0A  "TCP_LISTEN",
         * 0B  "TCP_CLOSING",
         */
        public String socket_type;

        public static TcpInfo fromJson(JSONObject object) {
            TcpInfo tcpInfo = new TcpInfo();

            tcpInfo.protocol = object.optString("protocol");
            tcpInfo.local_addr = object.optString("local_addr");
            tcpInfo.remote_addr = object.optString("remote_addr");
            tcpInfo.socket_type = object.optString("socket_type");
            return tcpInfo;
        }

        public JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {

                object.put("protocol", protocol);
                object.put("local_addr", local_addr);
                object.put("remote_addr", remote_addr);
                object.put("socket_type", socket_type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }
    }
}
