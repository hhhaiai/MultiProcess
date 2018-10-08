package com.analysys.dev.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

@TargetApi(21)
public class AnalysysJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
