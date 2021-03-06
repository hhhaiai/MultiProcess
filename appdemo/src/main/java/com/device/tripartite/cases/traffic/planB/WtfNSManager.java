package com.device.tripartite.cases.traffic.planB;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.provider.Settings;

import android.telephony.TelephonyManager;

import com.device.utils.EL;

import java.util.Calendar;
import java.util.List;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 流量获取方式TrafficStats/NetworkStatsManager(6.0开始)
 * @Version: 1.0
 * @Create: 2020/4/20 17:25
 * @author: sanbo
 */
public class WtfNSManager {
    /**
     * 本机使用的 wifi 总流量
     */
    public long getAllBytesWifi() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI,
                    "",
                    0,
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        //这里可以区分发送和接收
        return bucket.getTxBytes() + bucket.getRxBytes();
    }

    /**
     * 本机使用的 mobile 总流量
     */
    public long getAllBytesMobile() {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(mContext, ConnectivityManager.TYPE_MOBILE),
                    0,
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        //这里可以区分发送和接收
        return bucket.getTxBytes() + bucket.getRxBytes();
    }

    @SuppressLint("MissingPermission")
    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }

    /**
     * 获取指定应用 wifi 发送的当天总流量
     *
     * @param packageUid 应用的uid
     * @return
     */
    public long getPackageTxDayBytesWifi(int packageUid) {
        NetworkStats networkStats = null;
        networkStats = networkStatsManager.queryDetailsForUid(
                ConnectivityManager.TYPE_WIFI,
                "",
                getTimesmorning(),
                System.currentTimeMillis(),
                packageUid);
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }

    /**
     * 获取当天的零点时间
     *
     * @return
     */
    public static long getTimesmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (cal.getTimeInMillis());
    }

    //获得本月第一天0点时间
    @SuppressLint("WrongConstant")
    public static int getTimesMonthmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return (int) (cal.getTimeInMillis());
    }

    /**
     * 根据包名获取uid
     *
     * @param context     上下文
     * @param packageName 包名
     */
    public static int getUidByPackageName(Context context, String packageName) {
        int uid = -1;
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            uid = packageInfo.applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return uid;
    }

    public void getStr() {
        PackageManager packageManager = mContext.getPackageManager();
        final List<PackageInfo> applicationInfoList = packageManager.getInstalledPackages(0);
        EL.i("applicationInfoList:" + applicationInfoList.size());
        NetworkStats networkStats = null;
        for (PackageInfo info : applicationInfoList) {
            try {
                PackageInfo pack = packageManager.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS);
                String[] requestedPermissions = pack.requestedPermissions;
                if (requestedPermissions == null) {
                    continue;
                }
                int uid = info.applicationInfo.uid;
                String pkgName = info.applicationInfo.packageName;

                String appName = info.applicationInfo.loadLabel(packageManager).toString();

                if (uid == 1000) {
                    continue;
                }

                for (String str : requestedPermissions) {
                    if (str.equals("android.permission.INTERNET")) {
                        networkStats = networkStatsManager.queryDetailsForUid(
                                ConnectivityManager.TYPE_WIFI,
                                "",
                                getTimesmorning(),
                                System.currentTimeMillis(),
                                uid);
                        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                        networkStats.getNextBucket(bucket);
                        EL.i(appName + "[" + pkgName + "]---->下行【" + bucket.getTxPackets() + "】" + bucket.getTxBytes() + "---->上行【" + bucket.getRxPackets() + "】" + bucket.getRxBytes());
                        break;
                    }
                }
            } catch (Throwable exception) {
//                EL.e(exception);
            }
        }
    }


    // 打开“有权查看使用情况的应用”页面
    private void requestReadNetworkStats() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        mContext.startActivity(intent);
    }

    /********************* get instance begin **************************/
    public static WtfNSManager getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private WtfNSManager initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        if (mContext != null && networkStatsManager == null) {

            if (Build.VERSION.SDK_INT > 22) {
                networkStatsManager = (NetworkStatsManager) mContext.getSystemService(Context.NETWORK_STATS_SERVICE);
            }

        }
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final WtfNSManager INSTANCE = new WtfNSManager();
    }

    private WtfNSManager() {
    }

    private Context mContext = null;
    private NetworkStatsManager networkStatsManager = null;
    /********************* get instance end **************************/
}
