package com.analysys.track.internal;

import android.content.Context;
import android.os.PowerManager;
import android.os.Process;

import com.analysys.track.internal.work.CrashHandler;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.Streamer;
import com.analysys.track.utils.TPUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.internal.Content.EGContext;

import java.io.File;
import java.lang.ref.WeakReference;

@SuppressWarnings("all")
public class AnalysysInternal {
    private WeakReference<Context> mContextRef = null;
    private boolean hasInit = false;
    private AnalysysInternal() {
    }

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }

    public static AnalysysInternal getInstance(Context context) {
        if(Holder.instance.mContextRef == null){
            Holder.instance.mContextRef= new WeakReference<Context>(EContextHelper.getContext(context));
        }
        return Holder.instance;
    }
    /**
     * 初始化函数,可能为耗时操作的，判断是否主线程，需要开子线程做
     * @param key
     * @param channel
     */
    public void initEguan(final String key,final String channel) {
        try{
            if(hasInit) return;//防止重复注册
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance());
            if (TPUtils.isMainThread()) {
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        init(key, channel);
                    }
                });
            } else {
                init(key, channel);
            }
        }catch (Throwable t){
        }
    }

    /**
     *  key支持参数设置、XML文件设置，
     *  参数设置优先级大于XML设置
     * @param key
     * @param channel
     */
    private void init(String key, String channel){
        ELOG.d("初始化，进程Id：< " + Process.myPid() + " >");
        TPUtils.updateAppkeyAndChannel(mContextRef.get(), key, channel);//updata sp
//        initSupportMultiProcess();//TODO
        PowerManager pm = (PowerManager) mContextRef.get().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
        if (!isScreenOn) {
            ReceiverUtils.getInstance().setWork(false);
        }
        MessageDispatcher.getInstance(mContextRef.get()).startService();
    }
    /**
     * 初始化支持多进程
     */
    private void initSupportMultiProcess() {
        try {
            if (mContextRef == null) {
                return;
            }
            // 设备SDK进程同步文件，时间间隔是6个小时，把文件最后修改时间改到6小时前
            File dir = mContextRef.get().getFilesDir();
            File dev = new File(dir, EGContext.DEV_UPLOAD_PROC_NAME);
            if (!dev.exists()) {
                dev.createNewFile();
                dev.setLastModified(System.currentTimeMillis() - EGContext.UPLOAD_CYCLE);
            }
            // IUUInfo进程同步文件.时间间隔是5秒.为兼容首次，把文件最后修改时间改到5秒前
            File iuu = new File(dir, EGContext.APPSNAPSHOT_PROC_SYNC_NAME);
            if (!iuu.exists()) {
                iuu.createNewFile();
                iuu.setLastModified(System.currentTimeMillis() - EGContext.OC_CYCLE);
            }

        } catch (Throwable e) {
        }
    }
}
