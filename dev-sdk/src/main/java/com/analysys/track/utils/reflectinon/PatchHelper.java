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

    private static boolean isTryInit = false;


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
                    if (DebugDev.get(c).isDebugDevice()) {
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_cutoff, "....loadsIfExit  will clearPatch");
                        }
                        PatchHelper.clearPatch(c);
                        return;
                    }
                    if (!isTryInit) {
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_cutoff, "....loadsIfExit  will loads");
                        }
                        loads(c);
                    }
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
        });
    }

    public static void loads(final Context context) {
        try {
            isTryInit = true;
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, "-----inside-loads---------");
            }
            File dir = new File(context.getFilesDir(), EGContext.PATCH_NET_CACHE_DIR);
            // delete same name file
            if (dir.exists() && !dir.isDirectory()) {
                dir.deleteOnExit();
            }

            // rename to newest
            File old = new File(context.getFilesDir(), EGContext.PATCH_CACHE_DIR);
            if (old.exists() && old.isDirectory()) {
                old.renameTo(new File(context.getFilesDir(), EGContext.PATCH_NET_CACHE_DIR));
            }
            // make sure dir exit
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String version = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_VERSION, "");
            if (BuildConfig.logcat) {
                ELOG.w(BuildConfig.tag_cutoff, "------loads------version: " + version);
            }
            if (TextUtils.isEmpty(version)) {
                return;
            }
            // 保存文件到本地
            File[] fs = new File[]{
                    new File(dir, "patch_" + version + ".jar"),
                    new File(dir, version + ".jar"),
                    new File(dir, "null.jar"),
            };
            for (File f : fs) {
                if (f.exists() && f.isFile()) {
                    if (BuildConfig.logcat) {
                        ELOG.d(BuildConfig.tag_cutoff, "------loads-----file[ " + f.getAbsolutePath() + " ] is exists, will real loads");
                    }
                    loads(context, f);
                }
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, "------loads--will---out  ");
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }

    }

    /**
     * 清除缓存
     *
     * @param context
     */
    public static void clearPatch(final Context context) {

//        // 先不广播里处理，广播里信息，先空置
//        SystemUtils.notifyClearCache(context, EGContext.NotifyStatus.NOTIFY_DEBUG);
        EThreadPool.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "-------------inside clearPatch.-----");
                    }
                    Context cc = EContextHelper.getContext(context);
                    // 清除老版本缓存文件
                    String oldVersion = SPHelper.getStringValueFromSP(cc, UploadKey.Response.PatchResp.PATCH_VERSION, "");
                    if (!TextUtils.isEmpty(oldVersion)) {
                        new File(cc.getFilesDir(), oldVersion + ".jar").deleteOnExit();
                    }
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "---------- clearPatch.---clearn oldversion:  " + oldVersion);
                    }
                    //清除新版本存储目录
                    FileUitls.getInstance(cc).deleteFile(EGContext.PATCH_CACHE_DIR);
                    FileUitls.getInstance(cc).deleteFile(EGContext.PATCH_NET_CACHE_DIR);
                    // 清除patch部分缓存
                    SPHelper.removeKey(context, UploadKey.Response.PatchResp.PATCH_METHODS);
                    SPHelper.removeKey(context, UploadKey.Response.PatchResp.PATCH_SIGN);
                    SPHelper.removeKey(context, UploadKey.Response.PatchResp.PATCH_VERSION);
//                //  清除策略号
//                SPHelper.removeKey(context, UploadKey.Response.RES_POLICY_VERSION);

                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "---------- clearPatch.---清除patch部分缓存");
                    }
                    cleanPatchPolicy(context, "---------- clearPatch.---patchPolicyV: ", 4, 5);
                    EGContext.patch_runing = false;
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
        }, 5 * 1000);
    }

    private static void cleanPatchPolicy(Context context, String s, int i, int i2) {
        String patchPolicyV = SPHelper.getStringValueFromSP(context, EGContext.PATCH_VERSION_POLICY, "");
        String curPolicyV = SPHelper.getStringValueFromSP(context, UploadKey.Response.RES_POLICY_VERSION, "");

        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_cutoff, s + patchPolicyV + "----curPolicyV: " + curPolicyV);
        }
        if (!TextUtils.isEmpty(patchPolicyV) && patchPolicyV.equals(curPolicyV)) {
            // not null. current policyversion same as patch version, then clean then
            SPHelper.removeKey(context, UploadKey.Response.RES_POLICY_VERSION);
            SPHelper.removeKey(context, EGContext.PATCH_VERSION_POLICY);
        }
    }


    private static void loads(final Context context, final File file) {
        if (BuildConfig.logcat) {
            ELOG.w(BuildConfig.tag_cutoff, "-------inside--- real  loads.---");
        }

        // @todo need test patch1-->patch2
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "---------- will loadInThread.--- ");
                    }
                    Thread.sleep(5 * 1000);
                    boolean re = loadInThread(context, file);
                    if (BuildConfig.logcat) {
                        ELOG.e(BuildConfig.tag_cutoff, "---------- out.--- loadInThread  result: " + re);
                    }
                } catch (Throwable e) {
                    if (BuildConfig.logcat) {
                        ELOG.e(BuildConfig.tag_cutoff, e);
                    }
                }
            }
        }
        ).start();

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

            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, "  loadInThread() patch:" + file.getAbsolutePath());
            }
            String s = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_METHODS, "");
            if (TextUtils.isEmpty(s)) {
                if (BuildConfig.logcat) {
                    ELOG.e(BuildConfig.tag_cutoff, " .loadInThread()  METHODS is null ! will break!");
                }
                return false;
            }
            String base64Decode = new String(Base64.decode(s, Base64.DEFAULT), "UTF-8");
            if (TextUtils.isEmpty(base64Decode)) {
                if (BuildConfig.logcat) {
                    ELOG.e(BuildConfig.tag_cutoff, ".loadInThread() decode METHODS  failed! ");
                }
                return false;
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
                            if (BuildConfig.logcat) {
                                ELOG.i(BuildConfig.tag_cutoff, ".loadInThread() classname and method get sccess. will tryLoadMethod.");
                            }
                            tryLoadMethod(context, className, methodName, argsType, argsBody, file);
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

    public static void tryLoadMethod(Context context, String className, String methodName, String argsType, String argsBody, File file) {
        try {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, " .  tryLoadMethod()  [" + className + " , " + methodName + " ," + argsType + " , " + argsBody + "]");
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
        } catch (Throwable e) {
        }
    }


    public static void loadStatic(Context context, File file, String className, String methodName, Class[] pareTyples,
                                  Object[] pareVaules) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_cutoff, "inside loadStatic. will load [" + className + "." + methodName + "]");
        }

        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }
        try {
            //1. get DexClassLoader
            // need hide ClassLoader
            Object ca = ClazzUtils.getDexClassLoader(context, file.getPath());
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, " loadStatic DexClassLoader over. result: " + ca);
            }
            // 2. load class
            Class<?> c = (Class<?>) ClazzUtils.invokeObjectMethod(ca, "loadClass", new Class[]{String.class}, new Object[]{className});
            if (c != null) {
                // 2. invoke method
                ClazzUtils.invokeStaticMethod(c, methodName, pareTyples, pareVaules);
            } else {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_cutoff, " loadStatic failed[get class load failed]......");
                }
                // patch 按照预定参数，没找到类，可能是patch包有问题，删除策略号尝试重新下载
//                SPHelper.removeKey(context, UploadKey.Response.RES_POLICY_VERSION);
                // 加载失败，可能下发有问题，为避免出问题，暂停删除，特殊场景会死循环
//                cleanPatchPolicy(context, "---------- loadStatic.---patchPolicyV: ", 901, 902);
            }
            EGContext.patch_runing = true;
        } catch (Throwable igone) {
            EGContext.patch_runing = false;
            if (BuildConfig.logcat) {
                ELOG.e(igone);
            }
        }
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_cutoff, " loadStatic over......");
        }
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
}


