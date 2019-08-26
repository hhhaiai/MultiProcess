package com.device.utils;

import android.app.Notification;
import android.app.Service;
import android.os.Build;

public class ServicesProcess {

    public static void onCreate(Service service) {
        if (service == null) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT > 27) {
                if (PermissionH.checkPermission(service, "android.permission.FOREGROUND_SERVICE")) {
                    service.startForeground(1, new Notification());
                }
            } else if (Build.VERSION.SDK_INT > 25 && Build.VERSION.SDK_INT <= 27) {
                service.startForeground(1, new Notification());
            }
        } catch (Throwable e) {
        }
    }

}