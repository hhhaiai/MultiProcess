package com.eguan.monitor.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;

import com.eguan.monitor.Constants;
import com.eguan.monitor.cache.InnerProcessCacheManager;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.commonutils.SystemUtils;
import com.eguan.monitor.thread.EGQueue;
import com.eguan.monitor.thread.SafeRunnable;

/**
 * 网络类型监听广播
 */
public class NetChangedReciever extends BroadcastReceiver {

    public NetChangedReciever() {
        super();
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    InnerProcessCacheManager.getInstance().dealAppNetworkType(context);

                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.v("inside NetChangedReciever 接收到网络变化");
                    }
                    try {
                        Intent intent1 = new Intent();
                        intent1.setAction(Constants.NT_ACTION);
                        context.sendBroadcast(intent1);
                        SPUtil spUtil = SPUtil.getInstance(context);
                        String netType = spUtil.getNetTypeChange();
                        String cnt = getCurrentNetType(context);
                        spUtil.setNetworkInfo(cnt);

                        if (netType.isEmpty() && !cnt.equals("-1")) {
                            spUtil.setNetTypeChange(System.currentTimeMillis() + "-" + cnt);// 首次缓存当前网络类型,1245451144-4G
                        } else if (!cnt.equals("-1")) {

                            if (netType.contains("|")) {
                                String[] typeList = netType.split("\\|");
                                String lastType = typeList[typeList.length - 1];// 获取上次的网络类型
                                if (cnt.equalsIgnoreCase(lastType.substring(lastType
                                        .indexOf("-") + 1))) {
                                    return;
                                } else {
                                    netType = netType + "|" + System.currentTimeMillis() + "-" + cnt;// 如果有变化，则在字符串最后添加："|"+当前时间戳+当前网络类型
                                    spUtil.setNetTypeChange(netType);// 再次缓存
                                }
                            } else {
                                if (cnt.equalsIgnoreCase(netType.substring(netType.indexOf("-") + 1))) {
                                    return;// 如果网络无变化，则直接返回
                                } else {
                                    netType = netType + "|" + System.currentTimeMillis() + "-" + cnt;// 如果有变化，则在字符串最后添加："|"+当前时间戳+当前网络类型
                                    spUtil.setNetTypeChange(netType);// 再次缓存
                                }
                            }

                        }

                        if (Constants.FLAG_DEBUG_INNER) {
                            EgLog.v("Cache network type::::" + spUtil.getNetTypeChange());
                        }
                    } catch (Throwable e) {
                        if (Constants.FLAG_DEBUG_INNER) {
                            EgLog.e(e);
                        }
                    }
                }
            }
        });

    }


    /**
     * 获取当前网络类型，优先WiFi，其次GPRS
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public String getCurrentNetType(Context context) {
        NetworkInfo wifi = null;
        NetworkInfo gprs = null;
        try {
            ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (SystemUtils.checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                gprs = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            }
        } catch (Exception e) {
        }
        if (wifi != null && wifi.getState() == State.CONNECTED) {
            return "WIFI";
        } else if (gprs != null && gprs.getState() == State.CONNECTED) {
            return getNetGeneration(context);
        }
        return "-1";
    }

    /**
     * 判断GPRS网段下的网络类型
     */
    private String getNetGeneration(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (SystemUtils.checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if (networkInfo != null && networkInfo.isAvailable()) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getNetworkType() + "";
        } else {
            return "-1";
        }
    }

}
