package com.analysys.track.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-05 16:58:51
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class AnalysysService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("analysys","hack error");
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("analysys","hack error");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("analysys","hack error");
        return super.onStartCommand(intent, Service.START_STICKY, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("analysys","hack error");
    }
}
