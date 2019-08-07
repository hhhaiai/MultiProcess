package com.analysys.track.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.ELOG;

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
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService 。onCreate");
        }
        AnalysysInternal.getInstance(this);
        MessageDispatcher.getInstance(this).initModule();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, Service.START_STICKY, startId);
    }

    @Override
    public void onDestroy() {
        ServiceHelper.getInstance(this).startSelfService();
        super.onDestroy();
    }
}
