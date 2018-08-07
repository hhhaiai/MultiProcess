package com.eguan.monitor.receiver.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eguan.monitor.Constants;
import com.eguan.monitor.cache.InnerProcessCacheManager;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.thread.EGQueue;
import com.eguan.monitor.thread.SafeRunnable;

/**
 * Created on 17/2/27.
 * Author : chris
 * Email  : mengqi@analysys.com.cn
 * Detail :
 */

public class GLReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, final Intent intent) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    InnerProcessCacheManager.getInstance().updateLocation(intent.getStringExtra("GL"));
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        });
    }
}
