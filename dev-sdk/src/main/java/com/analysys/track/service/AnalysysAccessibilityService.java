package com.analysys.track.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
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
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysAccessibilityService onCreate");
        }
        super.onCreate();
        mContext = this.getApplicationContext();
    }

    private Context mContext;

    @Override
    protected void onServiceConnected() {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysAccessibilityService onServiceConnected");
        }
        try {
            super.onServiceConnected();
            mContext = this.getApplicationContext();
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
            CharSequence pkgName = event.getPackageName();
            if (TextUtils.isEmpty(pkgName)) {
                return;
            }
            final String pkg = pkgName.toString().trim();
            if (SystemUtils.isMainThread()) {
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        OCImpl.getInstance(mContext).processSignalPkgName(pkg, UploadKey.OCInfo.COLLECTIONTYPE_ACCESSIBILITY);
                    }
                });

            } else {
                OCImpl.getInstance(mContext).processSignalPkgName(pkg, UploadKey.OCInfo.COLLECTIONTYPE_ACCESSIBILITY);
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
