package com.analysys.track.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.analysys.track.hotfix.ObjectFactory;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.sp.SPHelper;

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
        boolean hfEnable = SPHelper.getBooleanValueFromSP(this.getApplicationContext(), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST &&hfEnable) {
            return ObjectFactory.invokeMethod(
                    ObjectFactory.make(AnalysysService.class.getName())
                    , AnalysysService.class.getName()
                    , "onBind", intent);

        }
        return null;
    }

    @Override
    public void onCreate() {
        boolean hfEnable = SPHelper.getBooleanValueFromSP(this.getApplicationContext(), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST &&hfEnable) {
            ObjectFactory.invokeMethod(
                    ObjectFactory.make(AnalysysService.class.getName())
                    , AnalysysService.class.getName()
                    , "onCreate");
            return;
        }
        super.onCreate();

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onCreate");
        }
        AnalysysInternal.getInstance(this);
        MessageDispatcher.getInstance(this).initModule();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean hfEnable = SPHelper.getBooleanValueFromSP(this.getApplicationContext(), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST &&hfEnable) {
            return ObjectFactory.invokeMethod(
                    ObjectFactory.make(AnalysysService.class.getName())
                    , AnalysysService.class.getName()
                    , "onStartCommand", intent, flags, startId);

        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onStartCommand");
        }
        return super.onStartCommand(intent, Service.START_STICKY, startId);
    }

    @Override
    public void onDestroy() {
        boolean hfEnable = SPHelper.getBooleanValueFromSP(this.getApplicationContext(), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST &&hfEnable) {
            ObjectFactory.invokeMethod(
                    ObjectFactory.make(AnalysysService.class.getName())
                    , AnalysysService.class.getName()
                    , "onDestroy");
            return;
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onDestroy");
        }
        ServiceHelper.getInstance(this).startSelfService();
        super.onDestroy();
    }
}
