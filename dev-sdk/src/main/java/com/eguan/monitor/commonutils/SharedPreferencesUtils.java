package com.eguan.monitor.commonutils;

import android.content.Context;
import android.content.SharedPreferences;

import com.eguan.monitor.Constants;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * 跨进程操作sharedPreferences的工具类,用来获取通过得到并转化为进程安全的New sharedPreferences对象
 */
public class SharedPreferencesUtils {
    private final static HashMap<String, Object> mSpCache = new HashMap<String, Object>();

    /**
     * 获取SharedPreferences的实例，并将实例缓存，一个进程只有一个具体文件的SharedPreferences实例,
     * 并会将相同名字的系统的SP文件旧数据转换到新实现中
     */
    public static SharedPreferences getSharedPreferences(Context ctx, String fileName) {
        handleReplace(ctx, fileName);
        return getNewSharedPreferences(ctx, fileName);
    }

    private static SharedPreferences getNewSharedPreferences(Context ctx, String fileName) {
        synchronized (mSpCache) {
            if (!mSpCache.containsKey(fileName)) {
                SharedPreferencesNewImpl nsp = new SharedPreferencesNewImpl(getNewSharedPrefsFile(ctx, fileName));
                mSpCache.put(fileName, nsp);
            }

            SharedPreferences ret = (SharedPreferences) mSpCache.get(fileName);
            return ret;
        }
    }

    /**
     * 将文件名/data/data/com.appname/shared_prefs/name.xml转换成/data/data/com.appname/shared_prefs/name.sp
     */
    public static File getNewSharedPrefsFile(Context ctx, String name) {
        File systemFile = getSystemSharedPrefsFile(ctx, name);
        String path = systemFile.getAbsolutePath();
        path = path.substring(0, path.length() - 4) + ".sp";
        return new File(path);
    }

    /**
     * 通过反射方法获取到系统SP文件所在的目录和名称，一般是在/data/data/com.appname/shared_prefs/name.xml
     */
    public static File getSystemSharedPrefsFile(Context ctx, String name) {
        File systemFile = (File) invokeObjectMethod(ctx, "getSharedPrefsFile", new Class[]{String.class},
                new Object[]{name});
        return systemFile;
    }

    /**
     * 在APP将要退出前调用，以保证所有的SP文件数据都及时落地
     */
    public static void onDestroy() {
        synchronized (mSpCache) {
            if (mSpCache.size() > 0) {
                for (Object sp : mSpCache.values()) {
                    ((SharedPreferencesNewImpl) sp).onDestroy();
                }
            }
        }
    }

    /**
     * 检查旧数据是否已经转换成为新数据,如果没有则转换
     */
    private synchronized static void handleReplace(Context ctx, String fileName) {
        if (null == ctx) {
            return;
        }

        SharedPreferences flag = getNewSharedPreferences(ctx, "sp_replace_flag");
        if (!flag.contains(fileName)) {

            SharedPreferences xsp = getNewSharedPreferences(ctx, fileName);
            SharedPreferences.Editor xedit = xsp.edit();
            if (((SharedPreferencesNewImpl) xsp).getModifyID() <= 1) {

                SharedPreferences sp = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE);
                Map<String, ?> map = sp.getAll();
                if (map.size() > 0) {
                    Set<? extends Map.Entry<String, ?>> set = map.entrySet();
                    for (Map.Entry<String, ?> entry : set) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        if (key != null && key.trim().length() > 0 && val != null) {
                            if (val instanceof String) {
                                xedit.putString(key, (String) val);
                            } else if (val instanceof Long) {
                                xedit.putLong(key, (Long) val);
                            } else if (val instanceof Integer) {
                                xedit.putInt(key, (Integer) val);
                            } else if (val instanceof Float) {
                                xedit.putFloat(key, (Float) val);
                            } else if (val instanceof Boolean) {
                                xedit.putBoolean(key, (Boolean) val);
                            }
                        }
                    }
                    xedit.apply();
                }
            }

            flag.edit().putBoolean(fileName, true).apply();
        }
    }

    private static Object invokeObjectMethod(Object o, String methodName, Class[] argsClass, Object[] args) {
        Object returnValue = null;
        try {
            Class<?> c = o.getClass();
            Method method;
            method = c.getMethod(methodName, argsClass);
            method.setAccessible(true);
            returnValue = method.invoke(o, args);
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

        return returnValue;
    }
}
