package com.eguan.monitor.receiver.app;

import com.eguan.Constants;
import com.eguan.monitor.InnerProcessCacheManager;
import com.eguan.utils.commonutils.EgLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            InnerProcessCacheManager.getInstance().dealAppNetworkType(context);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }
}
