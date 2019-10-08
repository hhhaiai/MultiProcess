package com.analysys.hotfix.utils;

import android.os.Handler;
import android.os.HandlerThread;

public class ThreadPool {
    private final Handler mHandler;
    private static ThreadPool ins;
    private final HandlerThread thread;

    private ThreadPool() {
        thread = new HandlerThread("hotfix_module");
        thread.start();
        mHandler = new Handler(thread.getLooper());
    }

    public static Handler get() {
        if (ins == null) {
            synchronized (ThreadPool.class) {
                if (ins == null) {
                    ins = new ThreadPool();
                }
            }
        }
        return ins.mHandler;
    }
}
