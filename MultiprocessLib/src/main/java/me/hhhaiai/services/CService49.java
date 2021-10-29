package me.hhhaiai.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import me.hhhaiai.mptils.MpLog;
import me.hhhaiai.mptils.MpServiceHelper;
import me.hhhaiai.mptils.MpThreadPool;

public class CService49 extends Service {
    String NAME = CService49.class.getName();

    @Override
    public IBinder onBind(Intent intent) {
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onBind intent:" + intent);
        }
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onRebind intent:" + intent);
        }
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onUnbind intent:" + intent);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onCreate");
        }

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "c49";
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID, "多进程测试[" + NAME + "]", NotificationManager.IMPORTANCE_HIGH);
            // NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            Notification notification =
                    new Notification.Builder(this, CHANNEL_ID)
                            .setContentTitle("")
                            .setContentText("")
                            .build();

            startForeground(49, notification);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (MpServiceHelper.isDebugService) {
            MpLog.d(
                    NAME
                            + ".onStartCommand flags:"
                            + flags
                            + "; startId: "
                            + startId
                            + " ; intent: "
                            + intent);
        }
        MpThreadPool.runOnWorkThread(
                () -> {
                    MpServiceHelper.callback(CService49.this, intent);
                });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onStart startId: " + startId + " ; intent: " + intent);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onLowMemory ");
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onTrimMemory level:" + level);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".attachBaseContext newBase:" + newBase);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (MpServiceHelper.isDebugService) {
            MpLog.d(NAME + ".onDestroy ");
        }
    }
}
