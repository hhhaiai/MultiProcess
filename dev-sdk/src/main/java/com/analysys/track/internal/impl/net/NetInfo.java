package com.analysys.track.internal.impl.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    /*
     * {
     * "time":"12345",
     * "protocol":"IPV4",
     * "pkgname":"info.kfsoft.datamonitor",
     * "appname":"网路流量计",
     * "local_addr":"121.43.208.203:443",
     * "remote_addr":"121.43.208.203:443",
     * "socket_type":"ESTABLISHED"
     * }
     */
    long time;
    String protocol;
    String pkgname;
    String appname;
    String local_addr;
    String remote_addr;
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
    String socket_type;

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("time", time);
            object.put("protocol", protocol);
            object.put("pkgname", pkgname);
            object.put("appname", appname);
            object.put("local_addr", local_addr);
            object.put("remote_addr", remote_addr);
            object.put("socket_type", socket_type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
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
}
