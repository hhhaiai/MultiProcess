package com.analysys.dev.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import com.analysys.dev.internal.work.ServiceHelper;

@TargetApi(21)
public class AnalysysJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        ServiceHelper.getInstance(this).startWork(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        ServiceHelper.getInstance(this).stopWork(this);
        return false;
    }

}
