package com.analysys.track.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.analysys.track.BuildConfig;
import com.analysys.track.impl.CusHotTransform;
import com.analysys.track.internal.AnalysysInternal;
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
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysService.class.getName(), "onBind")) {
                return (IBinder) CusHotTransform.getInstance(this).transform(true, AnalysysService.class.getName(), "onBind", intent);
            }
            AnalysysInternal.getInstance(this).aliave();
        } catch (Throwable e) {
        }
        return null;
    }

    @Override
    public void onCreate() {
        try {
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysService.class.getName(), "onCreate")) {
                CusHotTransform.getInstance(this).transform(true, AnalysysService.class.getName(), "onCreate");
                return;
            }
            super.onCreate();

            if (BuildConfig.logcat) {
                ELOG.i("AnalysysService onCreate");
            }
//            AnalysysInternal.getInstance(EContextHelper.getContext());
//            MessageDispatcher.getInstance(EContextHelper.getContext()).initModule();
            AnalysysInternal.getInstance(this).aliave();
        } catch (Throwable e) {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysService.class.getName(), "onStartCommand")) {
                Integer i = (Integer) CusHotTransform.getInstance(this).transform(true, AnalysysService.class.getName(), "onStartCommand", intent, flags, startId);
                if (i != null) {
                    return i;
                } else {
                    return Service.START_STICKY;
                }
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysService onStartCommand");
            }
            AnalysysInternal.getInstance(this).aliave();
        } catch (Throwable e) {
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysService.class.getName(), "onDestroy")) {
                CusHotTransform.getInstance(this).transform(true, AnalysysService.class.getName(), "onDestroy");
                return;
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysService onDestroy");
            }
//            ServiceHelper.getInstance(EContextHelper.getContext()).startSelfService();
            AnalysysInternal.getInstance(this).aliave();
        } catch (Throwable e) {
        }
        super.onDestroy();
    }
}
