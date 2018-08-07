package com.eguan.monitor.thread;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Copyright © 2015 sanbo Inc. All rights reserved.
 * @Description: 单线程的线程池.
 * @Version: 1.0
 * @Create: 2015年9月2日 下午11:59:53
 * @Author: sanbo
 */
public class EGQueue {
    // 任务队列,为了最后的清理数据
    private static List<WeakReference<ScheduledFuture<?>>> queue = new ArrayList<WeakReference<ScheduledFuture<?>>>();
    /**
     * 事件或者其他耗时操作的存放
     */
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static long MAX_WAIT_SECONDS = 5;

    /**
     * 琐碎文件操作的线程池
     */
    public static void execute(Runnable command) {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(command);
    }


    public static void waitForAsyncTask() {
        try {
            for (WeakReference<ScheduledFuture<?>> reference : queue) {
                ScheduledFuture<?> f = reference.get();
                if (f != null) {
                    f.cancel(false);
                }
            }
            queue.clear();

            if (!executor.isShutdown())
                executor.shutdown();
            if (!executorUpload.isShutdown())
                executorUpload.shutdown();

            executor.awaitTermination(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            executorUpload.awaitTermination(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ignore) {
        }
    }

    /**
     * 上行数据相关的放该线程池
     */
    private static ScheduledExecutorService executorUpload = Executors.newSingleThreadScheduledExecutor();

    /**
     * 耗时服务线程池.处理网络,缓存和文件操作
     */
    public synchronized static void post(Runnable command) {

        if (executorUpload.isShutdown()) {
            executorUpload = Executors.newSingleThreadScheduledExecutor();
        }
        executorUpload.execute(command);
    }

    //上行时候调用
    public static Future sumbit(Callable task) {
        if (executorUpload.isShutdown()) {
            executorUpload = Executors.newSingleThreadScheduledExecutor();
        }
        return executorUpload.submit(task);
    }

    public synchronized static void postDelayed(Runnable command, long delay) {
        if (executorUpload.isShutdown()) {
            executorUpload = Executors.newSingleThreadScheduledExecutor();
        }

        queue.add(new WeakReference<ScheduledFuture<?>>(executorUpload.schedule(command, delay, TimeUnit.MILLISECONDS)));
    }

    public synchronized static void postSync(Runnable command) {
        if (executorUpload.isShutdown()) {
            executorUpload = Executors.newSingleThreadScheduledExecutor();
        }

        Future<?> f = executorUpload.submit(command);
        try {
            f.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }
}
