package com.eguan.drivermonitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.commonutils.SystemUtils;
import com.eguan.monitor.fangzhou.service.MonitorService;
import com.eguan.monitor.thread.EGQueue;
import com.eguan.monitor.thread.SafeRunnable;


/**
 * @Copyright © 2018 Eguan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2017/10/26 17:19:32
 * @Author: Wang
 */
public class SystemStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                            || intent.getAction().equals(Intent.ACTION_USER_PRESENT)
                            || intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)
                            || intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                        Context appContext = context.getApplicationContext();
                        startService(appContext);
                    }
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        });
    }

    private void startService(final Context context) {
        try {
            boolean running = SystemUtils.isServiceRunning(context, Constants.MONITORSERVICE);
            String tactics = SPUtil.getInstance(context).getDeviceTactics();
            if (!running && SystemUtils.classInspect(Constants.MONITORSERVICE) && !tactics.equals(Constants.TACTICS_STATE)) {
                Intent service = new Intent(context, MonitorService.class);
                service.putExtra(Constants.APP_KEY, SPUtil.getInstance(context).getKey());
                service.putExtra(Constants.APP_CHANNEL, SPUtil.getInstance(context).getChannel());
                context.startService(service);
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }
}