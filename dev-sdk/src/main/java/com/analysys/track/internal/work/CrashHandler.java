package com.analysys.track.internal.work;

/**
 * @Copyright 2019 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-04 13:54:41
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private boolean isEnableCatchThrowable = false;
    private CrashCallBack handler = null;

    private CrashHandler() {
        if (Thread.getDefaultUncaughtExceptionHandler() == this) {
            return;
        }
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static CrashHandler getInstance() {
        return Holder.Instance;
    }

    /**
     * 设置回调
     *
     * @param calback
     * @return
     */
    public CrashHandler setCallback(CrashCallBack calback) {
        if (calback != null) {
            handler = calback;
        }
        return Holder.Instance;
    }

    /**
     * 是否错误采集
     *
     * @param isEnable
     * @return
     */
    public CrashHandler setEnableCatch(boolean isEnable) {
        isEnableCatchThrowable = isEnable;
        return Holder.Instance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            // 增加内部处理
            handleException(ex);
            // 系统处理
            if (mDefaultHandler != null && (mDefaultHandler != Thread.getDefaultUncaughtExceptionHandler())) {
                mDefaultHandler.uncaughtException(thread, ex);
            }
        } catch (Throwable e) {
        }
    }

    private void handleException(Throwable ex) {

        if (handler == null) {
            // 没有注册回调
            return;
        }
        if (isEnableCatchThrowable) {
            handler.onAppCrash(ex);
        } else {
            handler.onAppCrash(null);
        }
    }

    /**
     * 回调函数
     */
    public interface CrashCallBack {
        public abstract void onAppCrash(Throwable e);
    }

    private static class Holder {
        public static CrashHandler Instance = new CrashHandler();
    }
}