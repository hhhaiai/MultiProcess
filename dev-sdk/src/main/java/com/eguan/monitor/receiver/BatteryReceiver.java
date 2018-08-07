package com.eguan.monitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.manager.BatteryInfoManager;
import com.eguan.monitor.thread.EGQueue;
import com.eguan.monitor.thread.SafeRunnable;

public class BatteryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                        int status = intent.getIntExtra("status", 0);
                        int health = intent.getIntExtra("health", 0);
                        int level = intent.getIntExtra("level", 0);
                        int scale = intent.getIntExtra("scale", 0);
                        int plugged = intent.getIntExtra("plugged", 0);
                        String technology = intent.getStringExtra("technology");

                        //电源当前状态
                        BatteryInfoManager.setBs(status + "");
                        //电源健康状态
                        BatteryInfoManager.setBh(health + "");
                        //电源发前电量
                        BatteryInfoManager.setBl(level + "");
                        //电源总电量
                        BatteryInfoManager.setBsl(scale + "");
                        //电源充电状态
                        BatteryInfoManager.setBp(plugged + "");
                        //电源类型
                        BatteryInfoManager.setBt(technology);
                    }
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        });
    }
}
