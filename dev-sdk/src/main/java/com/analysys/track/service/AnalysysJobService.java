package com.analysys.track.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

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
 * @Create: 2019-08-08 10:46:43
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
@TargetApi(21)
public class AnalysysJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        boolean hfEnable = SPHelper.getBooleanValueFromSP(this.getApplicationContext(), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST &&hfEnable) {
            return ObjectFactory.invokeMethod(
                    ObjectFactory.make(AnalysysJobService.class.getName())
                    , AnalysysJobService.class.getName()
                    , "onStartJob",params);

        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysJobService onStartJob");
        }
        // 传递Context。防止因为Context缺失导致的调用异常
        AnalysysInternal.getInstance(this);
        MessageDispatcher.getInstance(this).initModule();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        boolean hfEnable = SPHelper.getBooleanValueFromSP(this.getApplicationContext(), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST &&hfEnable) {
            return ObjectFactory.invokeMethod(
                    ObjectFactory.make(AnalysysJobService.class.getName())
                    , AnalysysJobService.class.getName()
                    , "onStopJob",params);

        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysJobService onStopJob");
        }
        ServiceHelper.getInstance(this).startSelfService();
        return false;
    }

}
