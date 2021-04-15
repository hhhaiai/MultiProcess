package me.hhhaiai.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import me.hhhaiai.utils.EThreadPool;
import me.hhhaiai.utils.MpLog;
import me.hhhaiai.utils.ServiceHelper;

public class CService30 extends Service {
    String NAME = CService30.class.getName();

    @Override
    public IBinder onBind(Intent intent) {
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onBind intent:" + intent);
        }
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onRebind intent:" + intent);
        }
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onUnbind intent:" + intent);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onCreate");
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onStartCommand flags:" + flags + "; startId: " + startId + " ; intent: " + intent);
        }
        EThreadPool.runOnWorkThread(() -> {
            ServiceHelper.callback(NAME, intent);
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onStart startId: " + startId + " ; intent: " + intent);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onLowMemory ");
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onTrimMemory level:" + level);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".attachBaseContext newBase:" + newBase);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onDestroy ");
        }
    }
}
