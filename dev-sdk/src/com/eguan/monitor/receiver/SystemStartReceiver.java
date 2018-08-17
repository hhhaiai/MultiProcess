package com.eguan.monitor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eguan.Constants;
import com.eguan.monitor.fangzhou.service.MonitorService;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.SPUtil;
import com.eguan.utils.commonutils.SystemUtils;
import com.eguan.utils.thread.EGQueue;
import com.eguan.utils.thread.SafeRunnable;

/**
 * 开机启动广播维护Service存活
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
            if (!running && SystemUtils.classInspect(Constants.MONITORSERVICE)
                    && !tactics.equals(Constants.TACTICS_STATE)) {
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
