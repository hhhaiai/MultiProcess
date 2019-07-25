package com.analysys.track.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.track.impl.OCImpl;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.SystemUtils;

public class AnalysysAccessibilityService extends AccessibilityService {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        try {
            super.onServiceConnected();
            settingAccessibilityInfo();
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
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
        try {
            final String pkgName = String.valueOf(event.getPackageName()).replaceAll(" ", "");
            if (SystemUtils.isMainThread()) {
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        OCImpl.getInstance(AnalysysAccessibilityService.this).RunningApps(pkgName,
                                EGContext.OC_COLLECTION_TYPE_AUX);
                    }
                });

            } else {
                OCImpl.getInstance(this).RunningApps(pkgName, EGContext.OC_COLLECTION_TYPE_AUX);
            }

        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }
}
