package com.analysys.dev.internal.utils.reflectinon;

import android.os.Build;

import java.lang.reflect.Field;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description TODO
 * @Version 1.0
 * @Create 2018/12/18 16:17
 * @Author sanbo
 */
public class Reflecer {
    /**
     * make sure hook getApplication success
     */
    public static void aliveMContext() {
        try {
            Class<?> reflectionHelperClz = Class.forName(EContextHelper.class.getName());
            Class<?> classClz = Class.class;
            Field classLoaderField = classClz.getDeclaredField("classLoader");
            classLoaderField.setAccessible(true);
            classLoaderField.set(reflectionHelperClz, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * init for hooker
     */
    public static void init() {
        if (Build.VERSION.SDK_INT > 27) {
            aliveMContext();
        }
    }
}
