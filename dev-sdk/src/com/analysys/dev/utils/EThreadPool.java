package com.analysys.dev.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/23 10:53
 * @Author: Wang-X-C
 */
public class EThreadPool {

    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static ScheduledExecutorService executorUpload = Executors.newSingleThreadScheduledExecutor();
    // 任务队列,为了最后的清理数据
    private static List<WeakReference<ScheduledFuture<?>>> queue = new ArrayList<WeakReference<ScheduledFuture<?>>>();
    /**
     * 上行数据相关的放该线程池
     */

    public static void execute(Runnable command) {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        executor.execute(command);
    }
    public synchronized static void postDelayed(Runnable command, long delay) {
        if (executorUpload.isShutdown()) {
            executorUpload = Executors.newSingleThreadScheduledExecutor();
        }

        queue.add(new WeakReference<ScheduledFuture<?>>(executorUpload.schedule(command, delay, TimeUnit.MILLISECONDS)));
    }
}
