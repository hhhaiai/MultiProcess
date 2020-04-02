package com.analysys.track.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import com.analysys.track.AnalysysTracker;
import com.analysys.track.BuildConfig;
import com.analysys.track.impl.CusHotTransform;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-08 10:46:43
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
@TargetApi(21)
public class AnalysysJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        try {
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysJobService.class.getName(), "onStartJob")) {
                Boolean b = (Boolean) CusHotTransform.getInstance(this).transform(true, AnalysysJobService.class.getName(), "onStartJob", params);
                if (b != null) {
                    return b;
                }
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysJobService onStartJob");
            }
            // 传递Context。防止因为Context缺失导致的调用异常
            MessageDispatcher.getInstance(null).initModule();
        } catch (Throwable e) {
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        try {
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysJobService.class.getName(), "onStopJob")) {
                Boolean b = (Boolean) CusHotTransform.getInstance(this).transform(true, AnalysysJobService.class.getName(), "onStopJob", params);
                if (b != null) {
                    return b;
                }
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysJobService onStopJob");
            }
            ServiceHelper.getInstance(EContextHelper.getContext()).startSelfService();
        } catch (Throwable e) {
        }
        return false;
    }

}
