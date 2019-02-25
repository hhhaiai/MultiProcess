package com.analysys.dev.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.impl.DeviceImpl;
import com.analysys.dev.internal.impl.WifiImpl;
import com.analysys.dev.internal.impl.proc.ProcessManager;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.ReceiverUtils;
import com.analysys.dev.utils.reflectinon.Reflecer;
import com.analysys.dev.internal.work.MessageDispatcher;

public class DynamicReceivers extends BroadcastReceiver {
    String SCREEN_ON = "android.intent.action.SCREEN_ON";
    String SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    String BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
    String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    Context mContext;
    public static DynamicReceivers getInstance() {
        return Holder.INSTANCE;
    }
    private static class Holder {
        private static final DynamicReceivers INSTANCE = new DynamicReceivers();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Reflecer.init();
        mContext = context.getApplicationContext();
        if (CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            ELOG.d("接收网络变化广播");
            WifiImpl.getInstance(mContext).getWifiInfo();
            MessageDispatcher.getInstance(mContext).startService(0);
        }
        if (SCREEN_ON.equals(intent.getAction())) {
            ELOG.e("接收开启屏幕广播");
            MessageDispatcher.getInstance(mContext).startService(0);
        }
        if (SCREEN_OFF.equals(intent.getAction())) {
            ProcessManager.setIsCollected(false);
            ReceiverUtils.getInstance().unRegistAllReceiver(mContext);
            MessageDispatcher.getInstance(mContext).killWorker();
            ELOG.e("接收关闭屏幕广播");
        }
        if (BATTERY_CHANGED.equals(intent.getAction())) {
            if(EGContext.SWITCH_OF_BATTERY) DeviceImpl.getInstance(mContext).processBattery(intent);
            ELOG.e("电池变化广播");
        }
        if (BOOT_COMPLETED.equals(intent.getAction())) {
            ELOG.e("接收到开机广播");
            MessageDispatcher.getInstance(mContext).startService(0);
        }

    }
}
