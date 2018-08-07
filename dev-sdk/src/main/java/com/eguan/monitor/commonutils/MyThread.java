package com.eguan.monitor.commonutils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.eguan.monitor.Constants;

public class MyThread extends Thread {

    Context context;
    private Handler handler;
    public static MyThread instance = null;

    public static MyThread getInstance(Context context) {
        if (instance == null) {
            synchronized (MyThread.class) {
                if (instance == null) {
                    instance = new MyThread(context);
                }
            }
        }
        return instance;
    }

    public MyThread(Context context) {
        this.context = context;
        handler = new Handler(context.getMainLooper());

    }

    @Override
    public void run() {
        super.run();
        if (handler != null)
            handler.postDelayed(this, 5000);
        Intent intent = new Intent(Constants.ACTION_ALARM_TIMER);
        if (context != null) {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
            localBroadcastManager.sendBroadcast(intent);
        }

    }

    public void startThread() {
        if (handler != null)
            handler.post(this);
    }

    public void stopThread() {
        if (handler != null)
            handler.removeCallbacks(this);
    }
}
