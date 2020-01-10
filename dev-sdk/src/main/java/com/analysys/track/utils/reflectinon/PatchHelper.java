package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 热更使用类
 * @Version: 1.0
 * @Create: 2019-07-27 16:13:28
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class PatchHelper {

    public static void loads(final Context context) {
        try {
            File dir = new File(context.getFilesDir(), EGContext.HOTFIX_CACHE_PATCH_DIR);
            String version = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_VERSION, "");
            if (TextUtils.isEmpty(version)) {
                return;
            }
            // 保存文件到本地
            File file = new File(dir, version + ".jar");
            if (file.exists() && file.isFile()) {
                loads(context, file);
            }
        } catch (Throwable e) {
        }
    }

    public static void loads(final Context context, final File file) {
        EThreadPool.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadInThread(context, file);
            }
        }, 20000);
//        loadInThread(context, file);
    }

    private static boolean loadInThread(Context context, File file) {
        try {

            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.d(BuildConfig.tag_hotfix, "patch:" + file.getAbsolutePath());
            }
            String s = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_METHODS, "");
//            Log.i("sanbo", "原始字符串-------->" + s);
            if (TextUtils.isEmpty(s)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("原始字符串是空的，即将停止工作");
                }
                return true;
            }
            String base64Decode = new String(Base64.decode(s, Base64.DEFAULT), "UTF-8");
            if (TextUtils.isEmpty(base64Decode)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("解析后的字符串为空，即将停止工作");
                }
                return true;
            }
            JSONArray arr = new JSONArray(base64Decode);
            if (arr.length() > 0) {
                String className, methodName, argsType, argsBody;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj != null && obj.length() > 0 && "1".equals(obj.optString("type"))) {
                        className = obj.optString(UploadKey.Response.PatchResp.PATCH_NAME_CLASS, "");
                        methodName = obj.optString(UploadKey.Response.PatchResp.PATCH_NAME_METHOD, "");
                        argsType = obj.optString(UploadKey.Response.PatchResp.PATCH_ARGS_TYPE, "");
                        argsBody = obj.optString(UploadKey.Response.PatchResp.PATCH_ARGS_CONTENT, "");
                        if (TextUtils.isEmpty(className) && TextUtils.isEmpty(methodName)) {
                            return true;
                        } else {
                            tryLoadMethod(context, className, methodName, argsType, argsBody, file);
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return false;
    }

    public static void tryLoadMethod(Context context, String className, String methodName, String argsType, String argsBody, File file) throws IllegalAccessException, ClassNotFoundException, InvocationTargetException {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(BuildConfig.tag_upload, String.format("[%s , %s , %s , %s]", className, methodName, argsType, argsBody));
        }

        Class<?>[] argsTypeClazzs = null;
        Object[] argsValues = null;

        if (argsType != null) {
            String[] temp = argsType.split("\\|");
            for (int i = 0; i < temp.length; i++) {
                if (argsTypeClazzs == null) {
                    argsTypeClazzs = new Class[temp.length];
                }
                String one = temp[i];
                if (!TextUtils.isEmpty(one)) {
                    argsTypeClazzs[i] = Class.forName(one);
                }
            }
        }
        String[] tempBody = null;
        if (argsBody != null) {
            tempBody = argsBody.split("\\|");
        }

        if (argsTypeClazzs != null) {
            argsValues = new Object[argsTypeClazzs.length];
            for (int i = 0; i < argsTypeClazzs.length; i++) {
                Class<?> type = argsTypeClazzs[i];
                if (type == Context.class) {
                    argsValues[i] = context;
                    continue;
                }
                String one = null;
                if (tempBody != null && i < tempBody.length) {
                    one = tempBody[i];
                }
                if (type == int.class) {
                    argsValues[i] = Integer.parseInt(one);
                } else if (type == boolean.class) {
                    argsValues[i] = Boolean.parseBoolean(one);
                } else if (type == String.class) {
                    argsValues[i] = one;
                }
            }
        }
        loadStatic(context, file, className, methodName, argsTypeClazzs, argsValues);
    }


    public static void loadStatic(Context context, File file, String className, String methodName, Class[] pareTyples,
                                  Object[] pareVaules) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("inside loadStatic. will load [%s.%s]", className, methodName);
        }
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }
        try {
            //1. get DexClassLoader
            // need hide ClassLoader
            Object ca = ClazzUtils.getDexClassLoader(context, file.getPath());
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(" loadStatic DexClassLoader over. result: " + ca);
            }
            // 2. load class
            Class<?> c = (Class<?>) ClazzUtils.invokeObjectMethod(ca, "loadClass", new Class[]{String.class}, new Object[]{className});
            if (c != null) {
                // 2. invoke method
                ClazzUtils.invokeStaticMethod(c, methodName, pareTyples, pareVaules);
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(" loadStatic failed[get class load failed]......");
                }

            }

        } catch (Throwable igone) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(igone);
            }
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(" loadStatic over......");
        }
    }


}
