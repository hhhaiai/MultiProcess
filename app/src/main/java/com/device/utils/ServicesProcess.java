package com.device.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

public class ServicesProcess {

    private static final String CHANNEL_ID = "APPService";
    private static final CharSequence CHANNEL_NAME = "测试用";

    public static void onCreate(Service service) {
        if (service == null) {
            return;
        }

        try {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                if (DemoPermissionH.checkPermission(service, "android.permission.FOREGROUND_SERVICE")) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH);
                    NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.createNotificationChannel(channel);
                    Notification.Builder builder = new Notification.Builder(service.getApplicationContext(), CHANNEL_ID);
                    Notification notification = builder.setContentText("友好提示").setContentTitle("提示").build();
                    service.startForeground(1, notification);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}