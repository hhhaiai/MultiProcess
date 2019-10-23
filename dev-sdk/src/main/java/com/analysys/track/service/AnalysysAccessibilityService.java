package com.analysys.track.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.track.hotfix.HotFixException;
import com.analysys.track.hotfix.HotFixImpl;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;


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
        boolean hfEnable = SPHelper.getBooleanValueFromSP(EContextHelper.getContext(null), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST&&!EGContext.DEX_ERROR &&hfEnable) {
            try {
                HotFixImpl.invokeMethod(
                        HotFixImpl.make(AnalysysAccessibilityService.class.getName())
                        , AnalysysAccessibilityService.class.getName()
                        , "onCreate");
                return;
            } catch (Throwable e) {

            }

        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysAccessibilityService onCreate");
        }
        super.onCreate();
        mContext = EContextHelper.getContext(null);
    }

    private Context mContext;

    @Override
    protected void onServiceConnected() {
        boolean hfEnable = SPHelper.getBooleanValueFromSP(EContextHelper.getContext(null), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST&&!EGContext.DEX_ERROR &&hfEnable) {
            try {
                HotFixImpl.invokeMethod(
                        HotFixImpl.make(AnalysysAccessibilityService.class.getName())
                        , AnalysysAccessibilityService.class.getName()
                        , "onServiceConnected");
                return;
            } catch (HotFixException e) {
            }

        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysAccessibilityService onServiceConnected");
        }
        try {
            super.onServiceConnected();
            mContext = EContextHelper.getContext(null);
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
        boolean hfEnable = SPHelper.getBooleanValueFromSP(EContextHelper.getContext(null), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST&&!EGContext.DEX_ERROR &&hfEnable) {
            try {
                HotFixImpl.invokeMethod(
                        HotFixImpl.make(AnalysysAccessibilityService.class.getName())
                        , AnalysysAccessibilityService.class.getName()
                        , "onAccessibilityEvent",event);
                return;
            } catch (HotFixException e) {
            }
        }
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
        boolean hfEnable = SPHelper.getBooleanValueFromSP(EContextHelper.getContext(null), EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST&&!EGContext.DEX_ERROR &&hfEnable) {
            try {
                HotFixImpl.invokeMethod(
                        HotFixImpl.make(AnalysysAccessibilityService.class.getName())
                        , AnalysysAccessibilityService.class.getName()
                        , "onInterrupt");
                return;
            } catch (HotFixException e) {
                e.printStackTrace();
            }
        }
    }
}
