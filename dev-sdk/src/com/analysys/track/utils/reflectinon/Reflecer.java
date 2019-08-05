package com.analysys.track.utils.reflectinon;

import android.os.Build;

import com.analysys.track.internal.impl.DoubleCardSupport;

import java.lang.reflect.Field;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 反射类
 * @Version: 1.0
 * @Create: 2019-08-05 16:44:52
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class Reflecer {
    /**
     * make sure hook getApplication success
     */
    public static void aliveMContext(String name) {
        try {
            Class<?> reflectionHelperClz = Class.forName(name);
            Class<?> classClz = Class.class;
            Field classLoaderField = classClz.getDeclaredField("classLoader");
            classLoaderField.setAccessible(true);
            classLoaderField.set(reflectionHelperClz, null);
        } catch (Throwable e) {
        }
    }

    /**
     * init for hooker
     */
    public static void init() {
        if (Build.VERSION.SDK_INT > 27) {
            aliveMContext(EContextHelper.class.getName());
            aliveMContext(DoubleCardSupport.class.getName());
            aliveMContext(PatchHelper.class.getName());
        }
    }

}
