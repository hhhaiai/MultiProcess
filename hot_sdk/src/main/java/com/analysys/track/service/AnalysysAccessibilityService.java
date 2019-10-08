package com.analysys.track.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class AnalysysAccessibilityService extends AccessibilityService {
    @Override
    public void onCreate() {
        Log.e("analysys", "hack error");
    }


    @Override
    protected void onServiceConnected() {
        Log.e("analysys", "hack error");
    }

    private void settingAccessibilityInfo() {
        Log.e("analysys", "hack error");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e("analysys", "hack error");
    }

    @Override
    public void onInterrupt() {
        Log.e("analysys", "hack error");
    }
}
