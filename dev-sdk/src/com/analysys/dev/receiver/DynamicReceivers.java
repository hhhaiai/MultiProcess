package com.analysys.dev.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 动态注册的广播
 * @Version: 1.0
 * @Create: 2018年10月8日 下午5:54:14
 * @Author: sanbo
 */
public class DynamicReceivers {

    Context mContext;
    IuuBroadcastReceiver iuuBroadcastReceiver = null;
    NetChangedReceiver netChangedReceiver = null;
    ScreenReceiver screenReceiver = null;
    public DynamicReceivers(Context context){
        this.mContext = context;
        registerIUUReceiver();
        registerNetChangedReceiver();
        registerScreenReceiver();
    }

    /**
     * 注册应用安装/卸载/更新广播
     */
    private void registerIUUReceiver(){
        iuuBroadcastReceiver = new IuuBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        mContext.registerReceiver(iuuBroadcastReceiver,intentFilter);
    }

    /**
     * 注册网络变化广播
     */
    private void registerNetChangedReceiver(){
        netChangedReceiver = new NetChangedReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mContext.registerReceiver(netChangedReceiver,intentFilter);
    }

    /**
     * 注册屏幕开关广播
     */
    private void registerScreenReceiver(){
        screenReceiver = new ScreenReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction((Intent.ACTION_SCREEN_ON));
        intentFilter.addAction((Intent.ACTION_SCREEN_OFF));
        mContext.registerReceiver(screenReceiver,intentFilter);
    }




    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: TODO
     * @Version: 1.0
     * @Create: 2018年9月6日 上午11:02:24
     * @Author: sanbo
     */
    public class IuuBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }

    }
    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: TODO
     * @Version: 1.0
     * @Create: 2018年9月6日 上午11:02:05
     * @Author: sanbo
     */
    public class NetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }

    }
    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: TODO
     * @Version: 1.0
     * @Create: 2018年9月6日 上午11:02:32
     * @Author: sanbo
     */
    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }

    }
}
