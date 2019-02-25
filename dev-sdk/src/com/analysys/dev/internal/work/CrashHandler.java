package com.analysys.dev.internal.work;

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

    private static class Holder {
        public static CrashHandler Instance = new CrashHandler();
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
        // 增加内部处理
        handleException(ex);
        // 系统处理
        if (mDefaultHandler != null
                && (mDefaultHandler != Thread.getDefaultUncaughtExceptionHandler())) {
            mDefaultHandler.uncaughtException(thread, ex);
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
}