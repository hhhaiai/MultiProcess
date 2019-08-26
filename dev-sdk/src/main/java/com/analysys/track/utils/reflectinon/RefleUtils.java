package com.analysys.track.utils.reflectinon;

import android.text.TextUtils;

public class RefleUtils {
    /**
     * 是否包含方法
     *
     * @param className
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static boolean hasMethod(String className, String methodName, Class<?>... parameterTypes) {
        try {
            if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
                return false;
            }
            return hasMethod(Class.forName(className), methodName, parameterTypes);
        } catch (Throwable e) {
        }
        return false;
    }

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
            if (clazz != null) {
                return clazz.getMethod(methodName, parameterTypes) != null;
            }
        } catch (Throwable e) {
        }
        return false;
    }


}
