package com.analysys.track.utils.reflectinon;

import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.os.Build.VERSION.SDK_INT;

public class ClazzUtils {
    private static Method forName;
    private static Method getDeclaredMethod;
    private static Method getMethod;
    private static Method getField;
    private static Method getDeclaredField;

    public static boolean rawReflex = false;

    static {
        if (SDK_INT >= Build.VERSION_CODES.P && SDK_INT <= 29) {// android  9 10 版本
            try {
                forName = Class.class.getDeclaredMethod("forName", String.class);
                getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
                getMethod = Class.class.getDeclaredMethod("getMethod", String.class, Class[].class);
                getDeclaredField = Class.class.getDeclaredMethod("getDeclaredField", String.class);
                getField = Class.class.getDeclaredMethod("getField", String.class);
                rawReflex = true;
            } catch (Throwable e) {
                rawReflex = false;
            }
        }
    }

    /**
     * 设置豁免所有hide api
     */
    public static void unseal() {
        try {
            Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
            Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
            Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
            Object sVmRuntime = getRuntime.invoke(null);
            setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});
        } catch (Throwable e) {
        }
    }

    public static Method getMethod(String clazzName, String methodName, Class<?>... parameterTypes) {
        return getMethod(getClass(clazzName), methodName, parameterTypes);
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        Method method = null;
        try {
            if (getDeclaredMethod != null) {
                method = (Method) getDeclaredMethod.invoke(clazz, methodName, parameterTypes);
            }
        } catch (Throwable e) {
        }
        if (method != null) {
            method.setAccessible(true);
            return method;
        }
        try {
            if (getMethod != null) {
                method = (Method) getMethod.invoke(clazz, methodName, parameterTypes);
            }
        } catch (Throwable e) {
        }
        if (method != null) {
            method.setAccessible(true);
            return method;
        }
        try {
            method = clazz.getDeclaredMethod(methodName,parameterTypes);
        } catch (Throwable e) {
        }
        if (method != null) {
            method.setAccessible(true);
            return method;
        }
        try {
            method = clazz.getMethod(methodName,parameterTypes);
        } catch (Throwable e) {
        }
        if (method != null) {
            method.setAccessible(true);
            return method;
        } else {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(new Exception(clazz.getName() + methodName + "not found !"));
            }
        }
        return method;
    }

    public static Field getField(Class clazz, String fieldName) {
        if (clazz == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }
        Field field = null;
        try {
            if (getDeclaredField != null) {
                field = (Field) getDeclaredField.invoke(clazz, fieldName);
            }
        } catch (Throwable e) {
        }
        if (field != null) {
            field.setAccessible(true);
            return field;
        }
        try {
            if (getField != null) {
                field = (Field) getField.invoke(clazz, fieldName);
            }
        } catch (Throwable e) {
        }
        if (field != null) {
            field.setAccessible(true);
            return field;
        }
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (Throwable e) {
        }
        if (field != null) {
            field.setAccessible(true);
            return field;
        }
        try {
            field = clazz.getField(fieldName);
        } catch (Throwable e) {
        }
        if (field != null) {
            field.setAccessible(true);
            return field;
        } else {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(new Exception(clazz.getName() + fieldName + "not found !"));
            }
        }
        return null;
    }

    public static Class getClass(String name) {
        if (TextUtils.isEmpty(name)) {
            return Object.class;
        }
        Class result = null;
        if (forName != null) {
            try {
                result = (Class) forName.invoke(null, name);
            } catch (Throwable e) {

            }
        }
        if (result != null) {
            return result;
        }
        try {
            return Class.forName(name);
        } catch (Throwable e) {

        }
        return Object.class;
    }

    public static Object invokeObjectMethod(Object o, String methodName) {
        return invokeObjectMethod(o, methodName, (Class<?>[]) null, null);
    }

    public static Object invokeObjectMethod(Object o, String methodName, Class<?>[] argsClass, Object[] args) {
        if (o == null || methodName == null) {
            return null;
        }
        Object returnValue = null;
        try {
            Class<?> c = o.getClass();
            Method method;
            method = getMethod(c, methodName, argsClass);
            returnValue = method.invoke(o, args);
        } catch (Throwable e) {
        }

        return returnValue;
    }

    public static Object invokeObjectMethod(Object o, String methodName, String[] argsClassNames, Object[] args) {
        if (o == null || methodName == null) {
            return null;
        }
        if (argsClassNames != null) {
            Class[] argsClass = new Class[argsClassNames.length];
            for (int i = 0; i < argsClassNames.length; i++) {
                try {
                    argsClass[i] = Class.forName(argsClassNames[i]);
                } catch (Throwable e) {
                }
            }
            return invokeObjectMethod(o, methodName, argsClass, args);
        }
        return null;
    }


    public static Object getObjectFieldObject(Object o, String fieldName) {
        if (o == null || fieldName == null) {
            return null;
        }
        Field field = getField(o.getClass(), fieldName);
        try {
            return field.get(o);
        } catch (Throwable e) {

        }
        return null;
    }

    public static void setObjectFieldObject(Object o, String fieldName, Object value) {
        if (o == null || fieldName == null) {
            return;
        }
        Field field = getField(o.getClass(), fieldName);
        try {
            field.set(o, value);
        } catch (Throwable e) {

        }
    }

    public static Object getStaticFieldObject(Class clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return null;
        }
        Field field = getField(clazz.getClass(), fieldName);
        try {
            return field.get(null);
        } catch (Throwable e) {

        }
        return null;
    }

    public static Object invokeStaticMethod(String clazzName, String methodName, Class<?>[] argsClass, Object[] args) {
        return invokeStaticMethod(getClass(clazzName), methodName, argsClass, args);
    }

    public static Object invokeStaticMethod(Class clazz, String methodName, Class<?>[] argsClass, Object[] args) {
        if (clazz == null || methodName == null) {
            return null;
        }
        Object returnValue = null;
        try {
            Method method;
            method = getMethod(clazz, methodName, argsClass);
            returnValue = method.invoke(null, args);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }

        return returnValue;
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

    /**
     * 获取构造函数
     *
     * @param clazzName
     * @param types
     * @param values
     * @return
     */
    public static Object newInstance(String clazzName, Class[] types, Object[] values) {

        try {
            Constructor ctor = getDeclaredConstructor(getClass(clazzName), types);
            if (ctor != null) {
                ctor.setAccessible(true);
                return ctor.newInstance(values);
            }
        } catch (Throwable igone) {
        }
        return null;
    }

    public static Object newInstance(String clazzName) {
        return newInstance(clazzName, null, null);
    }

    private static Constructor getDeclaredConstructor(Class<?> clazz, Class[] types) {
        try {
            if (types == null) {
                return clazz.getDeclaredConstructor();
            }
            return clazz.getDeclaredConstructor(types);
        } catch (NoSuchMethodException e) {
            try {
                if (types == null) {
                    return clazz.getConstructor();
                }
                return clazz.getConstructor(types);
            } catch (NoSuchMethodException igone) {
            }
        }
        return null;
    }
}
