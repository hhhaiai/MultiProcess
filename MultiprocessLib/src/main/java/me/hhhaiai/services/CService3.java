package me.hhhaiai.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import me.hhhaiai.utils.MpLog;

public class CService3 extends Service {
    String NAME = CService3.class.getName();

    @Override
    public IBinder onBind(Intent intent) {
        MpLog.d(NAME + ".onBind intent:" + intent);
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        MpLog.d(NAME + ".onRebind intent:" + intent);

        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MpLog.d(NAME + ".onUnbind intent:" + intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MpLog.d(NAME + ".onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MpLog.d(NAME + ".onStartCommand flags:" + flags + "; startId: " + startId + " ; intent: " + intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        MpLog.d(NAME + ".onStart startId: " + startId + " ; intent: " + intent);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MpLog.d(NAME + ".onLowMemory ");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        MpLog.d(NAME + ".onTrimMemory level:" + level);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MpLog.d(NAME + ".attachBaseContext newBase:" + newBase);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MpLog.d(NAME + ".onDestroy ");

    }
}