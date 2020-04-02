package com.analysys.track.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.track.AnalysysTracker;
import com.analysys.track.BuildConfig;
import com.analysys.track.impl.CusHotTransform;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.receiver.AnalysysReceiver;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;


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
        processOnCreate();
        super.onCreate();

    }

    private void processOnCreate() {
        try {
            //禁止灰色 api logcat
            ClazzUtils.unseal();
            AnalysysTracker.setContext(this);
            mContext = EContextHelper.getContext(this.getApplicationContext());

            if (BuildConfig.enableHotFix && CusHotTransform.isCanWork(AnalysysAccessibilityService.class.getName(), "onCreate")) {
                CusHotTransform.transform(true, AnalysysAccessibilityService.class.getName(), "onCreate");
                return;
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysAccessibilityService onCreate");
            }
        } catch (Throwable e) {
        }
    }


    @Override
    protected void onServiceConnected() {
        try {
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix && CusHotTransform.isCanWork(AnalysysAccessibilityService.class.getName(), "onServiceConnected")) {
                CusHotTransform.transform(true, AnalysysAccessibilityService.class.getName(), "onServiceConnected");
                return;
            }
            if (BuildConfig.logcat) {
                ELOG.i("AnalysysAccessibilityService onServiceConnected");
            }
            try {
                super.onServiceConnected();
                mContext = EContextHelper.getContext();
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
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix && CusHotTransform.isCanWork(AnalysysAccessibilityService.class.getName(), "onAccessibilityEvent")) {
                CusHotTransform.transform(true, AnalysysAccessibilityService.class.getName(), "onAccessibilityEvent", event);
                return;
            }
            try {
                CharSequence pkgName = event.getPackageName();
                if (TextUtils.isEmpty(pkgName)) {
                    return;
                }
                final String pkg = pkgName.toString().trim();
                SystemUtils.runOnWorkThread(new Runnable() {
                    @Override
                    public void run() {
                        OCImpl.getInstance(mContext).processSignalPkgName(pkg, UploadKey.OCInfo.COLLECTIONTYPE_ACCESSIBILITY);
                    }
                });
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(t);
                }
            }
        } catch (Throwable e) {
        }
    }

    @Override
    public void onInterrupt() {
        try {
            AnalysysTracker.setContext(this);
            if (BuildConfig.enableHotFix && CusHotTransform.isCanWork(AnalysysAccessibilityService.class.getName(), "onInterrupt")) {
                CusHotTransform.transform(true, AnalysysAccessibilityService.class.getName(), "onInterrupt");
                return;
            }
        } catch (Throwable e) {
        }
    }

    private Context mContext;
}
