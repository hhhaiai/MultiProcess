package com.analysys.track.utils.reflectinon;

import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BuglyUtils;

public class RefleUtils {


    /**
     * 是否包含方法
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            if (clazz == null || TextUtils.isEmpty(methodName)) {
                return false;
            }
            if (parameterTypes == null || parameterTypes.length == 0) {
                return clazz.getMethod(methodName) != null;
            } else {
                return clazz.getMethod(methodName, parameterTypes) != null;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return false;
    }


}
