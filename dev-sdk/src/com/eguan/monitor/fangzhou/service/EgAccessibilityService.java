package com.eguan.monitor.fangzhou.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.eguan.Constants;
import com.eguan.monitor.AccessibilityOCManager;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.thread.EGQueue;
import com.eguan.utils.thread.SafeRunnable;

/**
 * Created on 2017/10/16. Author : chris Email : mengqi@analysys.com.cn Detail :
 */

public class EgAccessibilityService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        settingAccessibilityInfo();
    }

    private void settingAccessibilityInfo() {
        AccessibilityServiceInfo mAccessibilityServiceInfo = new AccessibilityServiceInfo();
        // 响应事件的类型，这里是窗口发生改变时
        mAccessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        // 反馈给用户的类型，这里是通用类型
        mAccessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        // 设置flag
        mAccessibilityServiceInfo.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        // TODO:设置描述
        setServiceInfo(mAccessibilityServiceInfo);
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {

                CharSequence pkgName = event.getPackageName();
                if (!TextUtils.isEmpty(pkgName)) {
                    AccessibilityOCManager.getInstance(EgAccessibilityService.this)
                            .setAccessibilityOC(pkgName.toString());
                } else {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.v(event.toString());
                    }
                }
            }
        });
    }

    @Override
    public void onInterrupt() {

    }
}
