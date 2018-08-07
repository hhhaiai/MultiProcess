package com.eguan.monitor.receiver.app;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eguan.monitor.Constants;
import com.eguan.monitor.cache.InnerProcessCacheManager;
import com.eguan.monitor.commonutils.EgLog;

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

	