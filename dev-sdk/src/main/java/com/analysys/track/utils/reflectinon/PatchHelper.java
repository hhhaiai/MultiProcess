package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.FileUitls;
import com.analysys.track.utils.SystemUtils;
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

    private static int mStatus = -1;
    private static int mK7Status = -1;
    private static boolean isTryInit = false;

    public static int getK2() {
        return mStatus;
    }

    public static int getK7() {
        return mK7Status;
    }

    public static void loadsIfExit(final Context context) {
        SystemUtils.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "inside....loadsIfExit");
                    }
                    Context c = EContextHelper.getContext(context);
                    /**
                     *  if debug, will clear cache
                     */
                    if (DevStatusChecker.getInstance().isDebugDevice(c)) {
                        if (BuildConfig.isNativeDebug) {
                            mK7Status = 101;
                        }
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_cutoff, "....loadsIfExit  will clearPatch");
                        }
                        PatchHelper.clearPatch(c);
                        if (BuildConfig.isNativeDebug) {
                            mK7Status = 102;
                        }
                        return;
                    }
                    if (!isTryInit) {
                        if (BuildConfig.isNativeDebug) {
                            mK7Status = 103;
                        }
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_cutoff, "....loadsIfExit  will loads");
                        }
                        loads(c);
                        if (BuildConfig.isNativeDebug) {
                            mK7Status = 104;
                        }
                    } else {
                        if (BuildConfig.isNativeDebug) {
                            mK7Status = 105;
                        }
                    }
                } catch (Throwable e) {
                    if (BuildConfig.isNativeDebug) {
                        mK7Status = 106;
                    }
                    if (BuildConfig.ENABLE_BUGLY) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
        });
    }

//
//    /**
//     * 确保3秒内执行执行一次
//     */
//    private static void makesureRunOnce(Context context) {
//
//        if (mHandler == null) {
//            mHandler = new MyHandler(context);
//        }
//        if (!mHandler.hasMessages(99)) {
//            mHandler.removeMessages(99);
//        }
//        Message msg = mHandler.obtainMessage();
//        msg.what = 99;
//        mHandler.sendEmptyMessageDelayed(99, 3 * 1000);
//    }
//
//    private static Handler mHandler = null;
//
//    static class MyHandler extends Handler {
//        private Context mContext;
//
//        MyHandler(Context context) {
//            mContext = EContextHelper.getContext(context);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what == 99) {
//                if (!isTryInit) {
//                    loads(mContext);
//                }
//            }
//        }
//    }

    public static void loads(final Context context) {
        try {
            if (BuildConfig.isNativeDebug) {
                mStatus = 0;
            }
            isTryInit = true;
//            Log.e("sanbo", "-----inside-loads---------");
            File dir = new File(context.getFilesDir(), EGContext.PATCH_CACHE_DIR);
            String version = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_VERSION, "");
//            Log.e("sanbo", "------loads------version: " + version);
            // delete same name file
            if (dir.exists() && !dir.isDirectory()) {
                dir.deleteOnExit();
            }
            if (!dir.exists()) {
//                Log.e("sanbo", "------loads------dir[ " + dir.getAbsolutePath() + " ] not exists!");
                dir.mkdirs();
            }
            if (TextUtils.isEmpty(version)) {
                if (BuildConfig.isNativeDebug) {
                    mStatus = 2;
                }
                return;
            }
            // 保存文件到本地
            File file = new File(dir, "patch_" + version + ".jar");
            if (file.exists() && file.isFile()) {
//                Log.i("sanbo", "------loads---patch_---file[ " + file.getAbsolutePath() + " ] is exists!");
                loads(context, file);
            } else {
                //适配旧版本，没加前缀的
                file = new File(dir, version + ".jar");
                if (file.exists() && file.isFile()) {
//                    Log.i("sanbo", "------loads------file[ " + file.getAbsolutePath() + " ] is exists!");
                    loads(context, file);
                }
            }
//            Log.i("sanbo", "------loads--will---out  ");

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
//        Log.i("sanbo", "------loads----out  ");

    }

    /**
     * 清除缓存
     *
     * @param context
     */
    public static void clearPatch(final Context context) {

        SystemUtils.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (BuildConfig.isNativeDebug) {
                        mK7Status = 0;
                    }
                    Context cc = EContextHelper.getContext(context);
                    // 清除老版本缓存文件
                    String oldVersion = SPHelper.getStringValueFromSP(cc, UploadKey.Response.PatchResp.PATCH_VERSION, "");
                    if (!TextUtils.isEmpty(oldVersion)) {
                        new File(cc.getFilesDir(), oldVersion + ".jar").deleteOnExit();
                    }
                    if (BuildConfig.isNativeDebug) {
                        mK7Status = 1;
                    }
                    //清除新版本存储目录
                    FileUitls.getInstance(cc).deleteFile(new File(context.getFilesDir(), EGContext.PATCH_CACHE_DIR));
                    if (BuildConfig.isNativeDebug) {
                        mK7Status = 2;
                    }
                    // 清除patch部分缓存
                    SPHelper.removeKey(context, UploadKey.Response.PatchResp.PATCH_METHODS);
                    SPHelper.removeKey(context, UploadKey.Response.PatchResp.PATCH_SIGN);
                    SPHelper.removeKey(context, UploadKey.Response.PatchResp.PATCH_VERSION);
//                //  清除策略号
//                SPHelper.removeKey(context, UploadKey.Response.RES_POLICY_VERSION);

                    if (BuildConfig.isNativeDebug) {
                        mK7Status = 3;
                    }
                    String patchPolicyV = SPHelper.getStringValueFromSP(context, EGContext.PATCH_VERSION_POLICY, "");
                    String curPolicyV = SPHelper.getStringValueFromSP(context, UploadKey.Response.RES_POLICY_VERSION, "");
                    if (!TextUtils.isEmpty(patchPolicyV) && patchPolicyV.equals(curPolicyV)) {
                        // not null. current policyversion same as patch version, then clean then
                        SPHelper.removeKey(context, UploadKey.Response.RES_POLICY_VERSION);
                        if (BuildConfig.isNativeDebug) {
                            mK7Status = 4;
                        }
                    } else {
                        if (BuildConfig.isNativeDebug) {
                            mK7Status = 5;
                        }
                    }
                    EGContext.patch_runing = false;
                    if (BuildConfig.isNativeDebug) {
                        mK7Status = 6;
                    }
                } catch (Throwable e) {
                    if (BuildConfig.isNativeDebug) {
                        mK7Status = 7;
                    }
                    if (BuildConfig.ENABLE_BUGLY) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
        });

    }

    private static void loads(final Context context, final File file) {
        if (BuildConfig.isNativeDebug) {
            mStatus = 3;
        }
        EThreadPool.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    boolean re = loadInThread(context, file);
//                    if (!re) {
//                        loadStatic(context, file, "com.analysys.Ab", "init", new Class[]{Context.class}, new Object[]{context});
//                    }
                } catch (Throwable e) {
                }
            }
        }, 5 * 1000);
//        EThreadPool.post(new Runnable() {
//            @Override
//            public void run() {
//                loadInThread(context, file);
//            }
//        });
    }

    /**
     * load in thead
     *
     * @param context
     * @param file
     * @return true: call load success
     * false: load failed
     */
    private static boolean loadInThread(Context context, File file) {
        try {
            if (BuildConfig.isNativeDebug) {
                mStatus = 4;
            }

            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.d("loadStatic  loadInThread() patch:" + file.getAbsolutePath());
            }
            String s = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_METHODS, "");
            if (TextUtils.isEmpty(s)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(" loadStatic.loadInThread()  原始字符串是空的，即将停止工作");
                }
                return false;
            }
            String base64Decode = new String(Base64.decode(s, Base64.DEFAULT), "UTF-8");
            if (TextUtils.isEmpty(base64Decode)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("loadStatic.loadInThread() 解析后的字符串为空，即将停止工作");
                }
                return false;
            }
            if (BuildConfig.isNativeDebug) {
                mStatus = 5;
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
                        if (!TextUtils.isEmpty(className) && !TextUtils.isEmpty(methodName)) {
                            if (BuildConfig.isNativeDebug) {
                                mStatus = 6;
                            }
                            if (EGContext.FLAG_DEBUG_INNER) {
                                ELOG.i("loadStatic.loadInThread() 即将开始解析");
                            }
                            tryLoadMethod(context, className, methodName, argsType, argsBody, file);
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

    public static void tryLoadMethod(Context context, String className, String methodName, String argsType, String argsBody, File file) throws IllegalAccessException, ClassNotFoundException, InvocationTargetException {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(String.format(" loadStatic.  tryLoadMethod()  [%s , %s , %s , %s]", className, methodName, argsType, argsBody));
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
        if (BuildConfig.isNativeDebug) {
            mStatus = 7;
        }
        loadStatic(context, file, className, methodName, argsTypeClazzs, argsValues);
    }


    public static void loadStatic(Context context, File file, String className, String methodName, Class[] pareTyples,
                                  Object[] pareVaules) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("inside loadStatic. will load [%s.%s]", className, methodName);
        }

        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            if (BuildConfig.isNativeDebug) {
                mStatus = 8;
            }
            return;
        }
        try {
            //1. get DexClassLoader
            // need hide ClassLoader
            Object ca = ClazzUtils.getDexClassLoader(context, file.getPath());
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(" loadStatic DexClassLoader over. result: " + ca);
            }
            if (BuildConfig.isNativeDebug) {
                mStatus = 9;
            }
            // 2. load class
            Class<?> c = (Class<?>) ClazzUtils.invokeObjectMethod(ca, "loadClass", new Class[]{String.class}, new Object[]{className});
            if (c != null) {
                if (BuildConfig.isNativeDebug) {
                    mStatus = 10;
                }
                // 2. invoke method
                ClazzUtils.invokeStaticMethod(c, methodName, pareTyples, pareVaules);
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(" loadStatic failed[get class load failed]......");
                }
                if (BuildConfig.isNativeDebug) {
                    mStatus = 11;
                }
                // patch 按照预定参数，没找到类，可能是patch包有问题，删除策略号尝试重新下载
                SPHelper.removeKey(context, UploadKey.Response.RES_POLICY_VERSION);

            }
            EGContext.patch_runing = true;
            if (BuildConfig.isNativeDebug) {
                mStatus = 1;
            }
        } catch (Throwable igone) {
            EGContext.patch_runing = false;
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(igone);
            }
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(" loadStatic over......");
        }

    }


}


