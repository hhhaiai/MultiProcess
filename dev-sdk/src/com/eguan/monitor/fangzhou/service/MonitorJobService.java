package com.eguan.monitor.fangzhou.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.SystemUtils;
import com.eguan.utils.thread.EGQueue;
import com.eguan.utils.thread.SafeRunnable;

/**
 * Created by Wang on 2017/6/9.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MonitorJobService extends JobService {
    @Override
    public boolean onStartJob(final JobParameters params) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    boolean running = SystemUtils.isServiceWork(MonitorJobService.this, Constants.MONITORSERVICE);
                    if (!running) {
                        Intent intent = new Intent(MonitorJobService.this, MonitorService.class);
                        startService(intent);
                    }
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        });
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
