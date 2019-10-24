package com.analysys.track.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.analysys.track.hotfix.HotFixTransformCancel;
import com.analysys.track.hotfix.HotFixImpl;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.EContextHelper;
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
        try {
            IBinder iBinder = HotFixImpl.transform(
                    HotFixImpl.make(AnalysysService.class.getName())
                    , AnalysysService.class.getName()
                    , "onBind", intent);
            if (iBinder != null) {
                return iBinder;
            }
        } catch (HotFixTransformCancel e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onCreate() {
        try {
            HotFixImpl.transform(
                    HotFixImpl.make(AnalysysService.class.getName())
                    , AnalysysService.class.getName()
                    , "onCreate");
            return;
        } catch (HotFixTransformCancel e) {
            e.printStackTrace();
        }
        super.onCreate();

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onCreate");
        }
        AnalysysInternal.getInstance(EContextHelper.getContext(null));
        MessageDispatcher.getInstance(EContextHelper.getContext(null)).initModule();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            return HotFixImpl.transform(
                    HotFixImpl.make(AnalysysService.class.getName())
                    , AnalysysService.class.getName()
                    , "onStartCommand", intent, flags, startId);
        } catch (HotFixTransformCancel e) {
            e.printStackTrace();
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onStartCommand");
        }
        if (EGContext.IS_HOST) {
            return super.onStartCommand(intent, Service.START_STICKY, startId);
        } else {
            return Service.START_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        try {
            HotFixImpl.transform(
                    HotFixImpl.make(AnalysysService.class.getName())
                    , AnalysysService.class.getName()
                    , "onDestroy");
            return;
        } catch (HotFixTransformCancel e) {
            e.printStackTrace();
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysService onDestroy");
        }
        ServiceHelper.getInstance(EContextHelper.getContext(null)).startSelfService();
        super.onDestroy();
    }
}
