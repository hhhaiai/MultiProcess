package com.analysys.dev.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import com.analysys.dev.receiver.AnalysysReceiver;
import com.analysys.dev.receiver.DynamicReceivers;


/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 动态广播注册器
 * @Version: 1.0
 * @Create: 2018/12/27 11:46:48
 * @Author: sanbo
 */
public class ReceiverUtils {

    private DynamicReceivers mReceiver = null;
    private AnalysysReceiver aReceiver = null;
    private boolean sWorkStatus = false;
    private ReceiverUtils() {
    }

    public static ReceiverUtils getInstance() {
        return Holder.INSTANCE;
    }

    public void registAllReceiver(Context context) {
        try {
            // L.i("[%s]----registAllReceiver...begin....", SystemUtils.getCurrentProcessName(mContext));
            setWork(true);//TODO 跟下一行重复，改到receiver里
            if (mReceiver == null) {
                mReceiver = DynamicReceivers.getInstance();
                aReceiver = AnalysysReceiver.getInstance();
                // net work
                IntentFilter intentFilter = new IntentFilter();
                if (Build.VERSION.SDK_INT < 24) {
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                }
                context.registerReceiver(mReceiver, intentFilter);

                // 解锁,更新网络
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_USER_PRESENT);
                context.registerReceiver(mReceiver, intentFilter);

                // battery
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                context.registerReceiver(mReceiver, intentFilter);

                // IUUinfo
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
                intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
                intentFilter.addDataScheme("package");
                context.registerReceiver(aReceiver, intentFilter);

                // 关屏、锁屏
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter.setPriority(Integer.MAX_VALUE);
                context.registerReceiver(mReceiver, intentFilter);
                // L.i("[%s]----registAllReceiver...over....", SystemUtils.getCurrentProcessName(mContext));
            }
        } catch (Throwable e) {
        }
    }

    public void unRegistAllReceiver(Context context) {
        try {
            setWork(false);
        } catch (Throwable e) {
        }
    }

    private static class Holder {
        private static final ReceiverUtils INSTANCE = new ReceiverUtils();
    }
    /**
     * @param isWorking
     */
    public void setWork(boolean isWorking) {
        sWorkStatus = isWorking;
    }
}
