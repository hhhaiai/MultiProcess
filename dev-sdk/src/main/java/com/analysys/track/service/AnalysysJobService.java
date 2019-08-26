package com.analysys.track.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;

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
        // 传递Context。防止因为Context缺失导致的调用异常
        AnalysysInternal.getInstance(this);
        MessageDispatcher.getInstance(this).initModule();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        ServiceHelper.getInstance(this).startSelfService();
        return false;
    }

}
