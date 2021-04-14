package com.device.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.device.utils.ServicesProcess;

public class MyServiceN extends Service {

    private String mClassName = this.getClass().getName();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ServicesProcess.onCreate(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        EL.i("sanbo.proctest", "。。。。。" + mClassName + "。。。");
        MultiProcessFramework.processCommand(this.getApplicationContext(), intent);
        return START_STICKY;
    }
}