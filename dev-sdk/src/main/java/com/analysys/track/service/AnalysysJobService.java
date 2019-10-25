package com.analysys.track.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import com.analysys.track.BuildConfig;
import com.analysys.track.hotfix.HotFixImpl;
import com.analysys.track.hotfix.HotFixTransformCancel;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.EContextHelper;

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
            Boolean aBoolean = HotFixImpl.transform(
                    HotFixImpl.make(AnalysysJobService.class.getName())
                    , AnalysysJobService.class.getName()
                    , "onStartJob", params);
            if (aBoolean != null) {
                return aBoolean;
            }
        } catch (HotFixTransformCancel e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            e.printStackTrace();
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysJobService onStartJob");
        }
        // 传递Context。防止因为Context缺失导致的调用异常
        AnalysysInternal.getInstance(null);
        MessageDispatcher.getInstance(null).initModule();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        try {
            Boolean aBoolean = HotFixImpl.transform(
                    HotFixImpl.make(AnalysysJobService.class.getName())
                    , AnalysysJobService.class.getName()
                    , "onStopJob", params);
            if (aBoolean != null) {
                return aBoolean;
            }
        } catch (HotFixTransformCancel e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            e.printStackTrace();
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysJobService onStopJob");
        }
        ServiceHelper.getInstance(EContextHelper.getContext(null)).startSelfService();
        return false;
    }

}
