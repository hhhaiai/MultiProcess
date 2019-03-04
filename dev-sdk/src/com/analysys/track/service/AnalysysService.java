package com.analysys.track.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.Reflecer;

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
}
