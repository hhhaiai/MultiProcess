package com.analysys.track.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

import com.analysys.track.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;

public class AnalysysService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ELOG.d("服务启动 进程ID：< " + Process.myPid() + " >");
        MessageDispatcher.getInstance(this).initModule();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
