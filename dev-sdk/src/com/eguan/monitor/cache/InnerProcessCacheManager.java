package com.eguan.monitor.cache;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.eguan.monitor.commonutils.SystemUtils;

/**
 * Created on 17/2/24.
 * Author : chris
 * Email  : mengqi@analysys.com.cn
 * Detail : 用来管理由子进程monitorServer工具或者服务获取的数据
 */
public class InnerProcessCacheManager {

    private InnerProcessCacheManager() {
    }

    public static InnerProcessCacheManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final InnerProcessCacheManager INSTANCE = new InnerProcessCacheManager();
    }

    public String getGL() {
        return GL;
    }

    public String getNT() {
        return NT;
    }

    private String NT = "-1";
    private String GL = "";
    private String GL_DEFALUT = "0.0-0.0";

    public void updateLocation(String location) {
        if (location == null || "".equals(location) || GL_DEFALUT.equals(location)) return;
        GL = location;
    }

    private void updateNetworkType(String networkType) {
        if (networkType == null || "".equals(networkType)) return;
        NT = networkType;
    }

    public void dealAppNetworkType(Context context) {
        String networkType = "";
        ConnectivityManager conn = (ConnectivityManager) context.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        if (SystemUtils.checkPermission(context, android.Manifest.permission.ACCESS_NETWORK_STATE)) {
            info = conn.getActiveNetworkInfo();

        }
        if (info == null) {
            networkType = "-1";
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI && info.getState() == NetworkInfo.State.CONNECTED) {
            networkType = "WIFI";
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE && info.getState() == NetworkInfo.State.CONNECTED) {
            networkType = ((TelephonyManager) context.getApplicationContext().
                    getSystemService(Context.TELEPHONY_SERVICE)).getNetworkType() + "";
        }
        InnerProcessCacheManager.getInstance().updateNetworkType(networkType);


    }

}
