package com.analysys.track.utils.sp;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.analysys.track.utils.ELOG;
import com.analysys.track.internal.Content.EGContext;


public class SPHelper {
    private final static HashMap<String, Object> SP_CACHE = new HashMap<String, Object>();
    private static final String DEFAULT_PREFERENCE = "ana_sp_xml";
    private static Editor editor = null;
    private static SharedPreferences res = null;

    private SPHelper() {}

    /**
     * 获取超级SP实例
     *
     * @param context
     * @return
     */
    public static SharedPreferences getDefault(Context context) {
        return getInstance(context, DEFAULT_PREFERENCE);
    }
    private static Editor getEditor(Context ctx){
        if(editor == null){
          editor = getDefault(ctx).edit();
        }
      return editor;
    }

    /**
     * 封装支持老接口,该接口游戏会使用
     *
     * @param context
     * @param name
     * @return
     */
    private static SharedPreferences getInstance(Context context, String name) {
        if (context == null) {
            return null;
        } else {
            if(res == null) res= getSharedPreferences(context, name);
            File f = getSystemSharedPrefsFile(context, name);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                }
            }
            ELOG.v("File[" + f.getAbsolutePath() + "]====>" + f.exists());
            return res;
        }
    }

    /**
     * 获取SharedPreferences的实例，并将实例缓存，一个进程只有一个具体文件的SharedPreferences实例, 并会将相同名字的系统的SP文件旧数据转换到新实现中
     */
    private static SharedPreferences getSharedPreferences(Context ctx, String fileName) {
        handleReplace(ctx, fileName);
        return getNewSharedPreferences(ctx, fileName);
    }

    private static SharedPreferences getNewSharedPreferences(Context ctx, String fileName) {
        synchronized (SP_CACHE) {
            if (!SP_CACHE.containsKey(fileName)) {
                SPImpl nsp = new SPImpl(getNewSharedPrefsFile(ctx, fileName));
                SP_CACHE.put(fileName, nsp);
            }

            SharedPreferences ret = (SharedPreferences)SP_CACHE.get(fileName);
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
    private static File getSystemSharedPrefsFile(Context ctx, String name) {
        File systemFile =
            (File)invokeObjectMethod(ctx, "getSharedPrefsFile", new Class[] {String.class}, new Object[] {name});
        return systemFile;
    }

    /**
     * 在APP将要退出前调用，以保证所有的SP文件数据都及时落地
     */
    public static void onDestroy() {
        synchronized (SP_CACHE) {
            if (SP_CACHE.size() > 0) {
                for (Object sp : SP_CACHE.values()) {
                    ((SPImpl)sp).onDestroy();
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
            if (((SPImpl)xsp).getModifyID() <= 1) {

                SharedPreferences sp = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE);
                Map<String, ?> map = sp.getAll();
                if (map.size() > 0) {
                    Set<? extends Entry<String, ?>> set = map.entrySet();
                    for (Entry<String, ?> entry : set) {
                        String key = entry.getKey();
                        Object val = entry.getValue();
                        if (key != null && key.trim().length() > 0 && val != null) {
                            if (val instanceof String) {
                                xedit.putString(key, (String)val);
                            } else if (val instanceof Long) {
                                xedit.putLong(key, (Long)val);
                            } else if (val instanceof Integer) {
                                xedit.putInt(key, (Integer)val);
                            } else if (val instanceof Float) {
                                xedit.putFloat(key, (Float)val);
                            } else if (val instanceof Boolean) {
                                xedit.putBoolean(key, (Boolean)val);
                            }
                        }
                    }
                    xedit.apply();
                }
            }

            flag.edit().putBoolean(fileName, true).apply();
        }
    }

    private static Object invokeObjectMethod(Object o, String methodName, Class<?>[] argsClass, Object[] args) {
        Object returnValue = null;
        try {
            Class<?> c = o.getClass();
            Method method;
            method = c.getMethod(methodName, argsClass);
            method.setAccessible(true);
            returnValue = method.invoke(o, args);
        } catch (Exception e) {
        }

        return returnValue;
    }
    public static boolean getDebugMode(Context ctx) {
        return getDefault(ctx).getBoolean(EGContext.DEBUG, false);
    }

    public static void setDebugMode(Context ctx, boolean debug) {
        getEditor(ctx).putBoolean(EGContext.DEBUG, debug).commit();
    }
    /**
     * 打开的应用名称
     *
     * @return
     */
    public static String getLastAppName(Context ctx) {
        return getDefault(ctx).getString(EGContext.LASTAPPNAME, "");
    }
    public static void setLastAppName(Context context,String lastAppName) {
        getEditor(context).putString(EGContext.LASTAPPNAME, lastAppName).commit();
    }
    public static String getLastOpenPackgeName(Context ctx) {
        return getDefault(ctx).getString(EGContext.LASTPACKAGENAME, "");
    }
    /**
     * 最后打开的应用包名
     */
    public static void setLastOpenPackgeName(Context ctx,String packageName) {
        getEditor(ctx).putString(EGContext.LASTPACKAGENAME, packageName).commit();
    }
    /**
     * 打开的应用版本号
     *
     * @return
     */
    public static String getLastAppVerison(Context ctx) {
        return getDefault(ctx).getString(EGContext.LASTAPPVERSION, "");
    }
    public static void setLastAppVerison(Context ctx,String lastAppVerison) {
        getEditor(ctx).putString(EGContext.LASTAPPVERSION, lastAppVerison).commit();
    }

    public static void setAppType(Context ctx,String appType) {
        getEditor(ctx).putString(EGContext.APP_TYPE, appType).commit();
    }
    /**
     * 应用的打开时间
     *
     * @return
     */
    public static String getLastOpenTime(Context ctx) {
        return getDefault(ctx).getString(EGContext.LASTOPENTIME, "");
    }
    public static void setLastOpenTime(Context ctx,String lastOpenTime) {
        getEditor(ctx).putString(EGContext.LASTOPENTIME, lastOpenTime).commit();
    }
    /**
     * 进程关闭时间时间
     */
    public static long getEndTime(Context ctx) {
        return getDefault(ctx).getLong(EGContext.ENDTIME, 0);
    }
    public static String getAppType(Context ctx) {
        return getDefault(ctx).getString(EGContext.APP_TYPE, "");
    }
    public static long getMinDuration(Context ctx) {
        return getDefault(ctx).getLong(EGContext.MIN_DURATION_TIME, 0);
    }

    public static void setMinDuration(Context ctx,long info) {
        editor.putLong(EGContext.MIN_DURATION_TIME, info);
        editor.commit();
    }
    public static long getMaxDuration(Context ctx) {
        return getDefault(ctx).getLong(EGContext.MAX_DURATION_TIME, 0);
    }

    public static void setMaxDuration(Context ctx,long info) {
        editor.putLong(EGContext.MAX_DURATION_TIME, info);
        editor.commit();
    }
    /**
     * 进程关闭时间时间
     */
    public static void setEndTime(Context ctx, long time) {
        getDefault(ctx).edit().putLong(EGContext.ENDTIME, time).commit();
    }
}
