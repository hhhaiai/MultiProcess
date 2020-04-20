package com.analysys.track.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.track.BuildConfig;
import com.analysys.track.impl.CusHotTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.ELOG;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 辅助功能采集OC
 * @Version: 1.0
 * @Create: 2019-08-05 16:58:10
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class AnalysysAccessibilityService extends AccessibilityService {
    @Override
    public void onCreate() {
        try {
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysAccessibilityService.class.getName(), "onCreate")) {
                CusHotTransform.getInstance(this).transform(true, AnalysysAccessibilityService.class.getName(), "onCreate");
                return;
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysAccessibilityService onCreate");
            }
        } catch (Throwable e) {
        }
        super.onCreate();

    }



    @Override
    protected void onServiceConnected() {
        try {
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysAccessibilityService.class.getName(), "onServiceConnected")) {
                CusHotTransform.getInstance(this).transform(true, AnalysysAccessibilityService.class.getName(), "onServiceConnected");
                return;
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysAccessibilityService onServiceConnected");
            }
            try {
                super.onServiceConnected();
                settingAccessibilityInfo();
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(t);
                }
            }
        } catch (Throwable e) {
        }
    }

    private void settingAccessibilityInfo() {
        AccessibilityServiceInfo mAccessibilityServiceInfo = new AccessibilityServiceInfo();
        // 响应事件的类型，这里是窗口发生改变时
        mAccessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        // 反馈给用户的类型，这里是通用类型
        mAccessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        // 设置flag
        mAccessibilityServiceInfo.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(mAccessibilityServiceInfo);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysAccessibilityService.class.getName(), "onAccessibilityEvent")) {
                CusHotTransform.getInstance(this).transform(true, AnalysysAccessibilityService.class.getName(), "onAccessibilityEvent", event);
                return;
            }
        } catch (Throwable e) {
        }

        AnalysysInternal.getInstance(this).accessibilityEvent(event);
    }

    @Override
    public void onInterrupt() {
        try {
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(this).isCanWork(AnalysysAccessibilityService.class.getName(), "onInterrupt")) {
                CusHotTransform.getInstance(this).transform(true, AnalysysAccessibilityService.class.getName(), "onInterrupt");
                return;
            }
        } catch (Throwable e) {
        }
    }
}
