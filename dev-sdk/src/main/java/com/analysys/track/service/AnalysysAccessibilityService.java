package com.analysys.track.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.track.AnalysysTracker;
import com.analysys.track.BuildConfig;
import com.analysys.track.hotfix.HotFixTransform;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.SystemUtils;


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
        AnalysysTracker.setContext(this);
        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(
                        HotFixTransform.make(AnalysysAccessibilityService.class.getName())
                        , AnalysysAccessibilityService.class.getName()
                        , "onCreate");
                return;
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
                }
            }
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysAccessibilityService onCreate");
        }
        super.onCreate();
        mContext = EContextHelper.getContext();
    }

    private Context mContext;

    @Override
    protected void onServiceConnected() {
        AnalysysTracker.setContext(this);

        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(
                        HotFixTransform.make(AnalysysAccessibilityService.class.getName())
                        , AnalysysAccessibilityService.class.getName()
                        , "onServiceConnected");
                return;
            } catch (Throwable e) {
            }
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysAccessibilityService onServiceConnected");
        }
        try {
            super.onServiceConnected();
            mContext = EContextHelper.getContext();
            settingAccessibilityInfo();
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
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
        AnalysysTracker.setContext(this);
        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(
                        HotFixTransform.make(AnalysysAccessibilityService.class.getName())
                        , AnalysysAccessibilityService.class.getName()
                        , "onAccessibilityEvent", event);
                return;
            } catch (Throwable e) {
            }
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
        }
    }

    @Override
    public void onInterrupt() {
        AnalysysTracker.setContext(this);
        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(
                        HotFixTransform.make(AnalysysAccessibilityService.class.getName())
                        , AnalysysAccessibilityService.class.getName()
                        , "onInterrupt");
                return;
            } catch (Throwable e) {
            }
        }
    }
}
