package com.analysys.track.utils;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.analysys.track.internal.Content.EGContext;

public class NetworkUtils {
    private static ConnectivityManager connManager = null;

    private static ConnectivityManager getConnectivityManager(Context ctx) {
        if (connManager == null) {
            connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return connManager;
    }

    /**
     * 获取当前的网络状态
     */
    public static String getNetworkType(Context context) {
        String netType = EGContext.NETWORK_TYPE_NO_NET;

        NetworkInfo networkInfo = getConnectivityManager(context).getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = EGContext.NETWORK_TYPE_WIFI;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {

            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.isNetworkRoaming()) {
                netType = EGContext.NETWORK_TYPE_2G;
            } else {
                switch (nSubType) {
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = EGContext.NETWORK_TYPE_4G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        netType = EGContext.NETWORK_TYPE_3G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        netType = EGContext.NETWORK_TYPE_2G;
                        break;
                    default:
                        netType = EGContext.NETWORK_TYPE_2G;
                        break;
                }
            }

        } else {
            netType = EGContext.NETWORK_TYPE_NO_NET;
        }
        return netType;
    }

    public static boolean isWifiAlive(Context mContext) {
        if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_NETWORK_STATE)) {
            if (getConnectivityManager(mContext) != null) {
                NetworkInfo wifiNetwork = getConnectivityManager(mContext).getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifiNetwork != null) {
                    if (wifiNetwork.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isNetworkAlive(Context mContext) {
        if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_NETWORK_STATE)) {
            if (getConnectivityManager(mContext) != null) {
                NetworkInfo ni = getConnectivityManager(mContext).getActiveNetworkInfo();
                if (ni != null) {
                    return ni.isConnected();
                }
            }
        }
        return false;
    }
}
