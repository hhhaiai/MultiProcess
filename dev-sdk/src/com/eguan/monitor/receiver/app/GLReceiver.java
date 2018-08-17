package com.eguan.monitor.receiver.app;

import com.eguan.Constants;
import com.eguan.monitor.InnerProcessCacheManager;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.thread.EGQueue;
import com.eguan.utils.thread.SafeRunnable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
