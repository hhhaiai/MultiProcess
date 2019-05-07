package com.device;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyServiceB extends Service {
    private String mClassName = this.getClass().getName();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private Thread mThread = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mThread == null) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(1000);
//                            Log.i("servive.test", "。。。。。" + mClassName + "。。。");
                        } catch (InterruptedException e) {
                        }
                    }
                }
            });
            mThread.start();
        }
        return START_STICKY;
    }
}
