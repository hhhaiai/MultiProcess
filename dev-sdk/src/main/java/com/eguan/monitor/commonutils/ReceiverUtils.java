package com.eguan.monitor.commonutils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.eguan.monitor.Constants;
import com.eguan.monitor.receiver.BatteryReceiver;
import com.eguan.monitor.receiver.NetChangedReciever;
import com.eguan.monitor.receiver.device.IUUBrodcastReciever;
import com.eguan.monitor.receiver.device.ScreenReceiver;
import com.eguan.monitor.receiver.device.TimerReceiver;

public class ReceiverUtils {
    private IUUBrodcastReciever iuuReceiver;// 安装卸载广播
    private NetChangedReciever npl_receiver;// 网络变化广播
    private TimerReceiver tReceiver;
    private ScreenReceiver mScreenReceiver;//锁屏广播
    private IntentFilter intentFilter;
    private LocalBroadcastManager localBroadcastManager;
    private BatteryReceiver mBatteryReceiver; //电池changed广播

    // --------------地理位置信息--------------
    private ReceiverUtils() {
    }

    public static ReceiverUtils getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final ReceiverUtils INSTANCE = new ReceiverUtils();
    }

    public void unRegistAllReceiver(Context context, boolean isFromService) {

        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.d("inside ReceiverUtils.unRegistAllReceiver. 即将注销广播接收器");
        }

        try {
//			GlobalTimer.getInstance(context).stopHeart();
            MyThread.getInstance(context).stopThread();
            /*-------------注销IUU广播-------------*/
            if (iuuReceiver != null) {
                context.unregisterReceiver(iuuReceiver);
            }
            /*-------------注销网络监听广播-------------*/
            if (npl_receiver != null) {
                context.unregisterReceiver(npl_receiver);
            }
            /*-------------取消闹铃-------------*/
            if (tReceiver != null && localBroadcastManager != null) {
                localBroadcastManager.unregisterReceiver(tReceiver);
            }
            /*--------------取消屏幕监听广播------*/
            if (isFromService && mScreenReceiver != null) {
                context.unregisterReceiver(mScreenReceiver);
            }

            /*-------------取消LBS监听——————————————————*/
//            String str = SPUtil.getInstance(context).getLocation();
//			if (str != null && str.equalsIgnoreCase("yes")) {
//				LocationChangeManager.getInstance(context).unRegistLocation();
//			}

            unregisterBatteryReceiver(context);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    public void registAllReceiver(Context context) {

        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.v("inside ReceiverUtils.registAllReceiver. 即将注册广播接收器");
        }
        try {
//			GlobalTimer.getInstance(context).startAlarm();

            /*--------------- 注册网络变化广播 -----------------------*/
            regiestNetTypeReceiver(context);
            /*---------------- 注册闹钟广播-------------------------*/
            registAlarmer(context);
            MyThread.getInstance(context).startThread();
            /*---------------- 注册应用监听卸载广播 --------------------*/
            regiestIuuReceiver(context);
            /*------------------- 注册地理位置变化监听器---------------------*/
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//				if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//					registerLBS(context);
//                }
//			}else {
//				registerLBS(context);
//			}
            /**
             * 注册监听电源信息的Receiver
             */
            registerBatteryReceiver(context);

        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    private void registerBatteryReceiver(Context context) {
        mBatteryReceiver = new BatteryReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(mBatteryReceiver, filter);
    }

    private void unregisterBatteryReceiver(Context context) {
        if (mBatteryReceiver != null) {
            context.unregisterReceiver(mBatteryReceiver);
        }
    }


    /**
     * 注册闹钟广播
     */
    private void registAlarmer(Context context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_ALARM_TIMER);
        tReceiver = new TimerReceiver();
        localBroadcastManager.registerReceiver(tReceiver, intentFilter);
    }

    /**
     * 注册安装卸载广播接收器
     */
    private void regiestIuuReceiver(Context context) {
        iuuReceiver = new IUUBrodcastReciever();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        context.registerReceiver(iuuReceiver, intentFilter);
    }

    /**
     * 注册网络变化监听广播接收器
     */
    private void regiestNetTypeReceiver(Context context) {
        npl_receiver = new NetChangedReciever();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        context.registerReceiver(npl_receiver, intentFilter);
    }

    /**
     * 注册锁屏广播
     */
    public void registerScreenReceiver(Context context) {

        mScreenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.setPriority(Integer.MAX_VALUE);
        context.registerReceiver(mScreenReceiver, filter);

    }

}
