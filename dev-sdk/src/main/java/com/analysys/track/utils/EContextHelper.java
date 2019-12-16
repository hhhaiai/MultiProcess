package com.analysys.track.utils;

import android.content.Context;

public class EContextHelper {
    public static Context context;

    public static Context getContext() {
        return EContextHelper.context;
    }

    public static void setContext(Context context) {
        if (context != null) {
            EContextHelper.context = context.getApplicationContext();
        }
    }
}
