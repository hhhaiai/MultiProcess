package com.analysys.track.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EThreadPool {

    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static ScheduledExecutorService executorUpload = Executors.newSingleThreadScheduledExecutor();
    // 任务队列,为了最后的清理数据
    private static List<WeakReference<ScheduledFuture<?>>> queue = new ArrayList<WeakReference<ScheduledFuture<?>>>();
    private static ScheduledExecutorService ocPools = Executors.newSingleThreadScheduledExecutor();
    private static List<Future<?>> ocTasks = new ArrayList<Future<?>>();
    private static long MAX_WAIT_SECONDS = 5;

    /**
     * 上行数据相关的放该线程池
     */

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

            if (!executor.isShutdown()) {
                executor.shutdown();
            }
            if (!executorUpload.isShutdown()) {
                executorUpload.shutdown();
            }

            executor.awaitTermination(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            executorUpload.awaitTermination(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ignore) {
        }
    }

    /**
     * 耗时服务线程池.处理网络,缓存和文件操作
     */
    public synchronized static void post(Runnable command) {

        if (executorUpload.isShutdown()) {
            executorUpload = Executors.newSingleThreadScheduledExecutor();
        }
        executorUpload.execute(command);
    }

    public synchronized static void postDelayed(Runnable command, long delay) {
        if (executorUpload.isShutdown()) {
            executorUpload = Executors.newSingleThreadScheduledExecutor();
        }

        queue.add(
                new WeakReference<ScheduledFuture<?>>(executorUpload.schedule(command, delay, TimeUnit.MILLISECONDS)));
    }

    public synchronized static void postSync(Runnable command) {
        if (executorUpload.isShutdown()) {
            executorUpload = Executors.newSingleThreadScheduledExecutor();
        }

        Future<?> f = executorUpload.submit(command);
        try {
            f.get(5, TimeUnit.SECONDS);
        } catch (Exception ignore) {
        }
    }

    // OC使用
    public static void sumbit(Runnable command) {
        if (ocPools.isShutdown()) {
            ocPools = Executors.newSingleThreadScheduledExecutor();
        }
        if (ocTasks.size() < 10) {
            ocTasks.add(ocPools.submit(command));
        }
    }
}
