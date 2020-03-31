package com.analysys.track.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.analysys.track.AnalysysTracker;
import com.analysys.track.BuildConfig;
import com.analysys.track.impl.HotFixTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.EContextHelper;
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
        try {
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix) {
                try {
                    IBinder iBinder = HotFixTransform.transform(
                            true
                            , AnalysysService.class.getName()
                            , "onBind", intent);
                    if (iBinder != null) {
                        return iBinder;
                    }
                } catch (Throwable e) {

                }
            }
        } catch (Throwable e) {
        }
        return null;
    }

    @Override
    public void onCreate() {
        try {
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix) {
                try {
                    HotFixTransform.transform(
                            true
                            , AnalysysService.class.getName()
                            , "onCreate");
                    return;
                } catch (Throwable e) {

                }
            }
            super.onCreate();

            if (BuildConfig.logcat) {
                ELOG.i("AnalysysService onCreate");
            }
            AnalysysInternal.getInstance(EContextHelper.getContext());
            MessageDispatcher.getInstance(EContextHelper.getContext()).initModule();
        } catch (Throwable e) {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix) {
                try {
                    Integer o = HotFixTransform.transform(
                            true
                            , AnalysysService.class.getName()
                            , "onStartCommand", intent, flags, startId);
                    if (o != null) {
                        return o;
                    }
                    return Service.START_STICKY;
                } catch (Throwable e) {

                }
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysService onStartCommand");
            }
        } catch (Throwable e) {
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix) {
                try {
                    HotFixTransform.transform(
                            true
                            , AnalysysService.class.getName()
                            , "onDestroy");
                    return;
                } catch (Throwable e) {

                }
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysService onDestroy");
            }
            ServiceHelper.getInstance(EContextHelper.getContext()).startSelfService();
        } catch (Throwable e) {
        }
        super.onDestroy();
    }
}
