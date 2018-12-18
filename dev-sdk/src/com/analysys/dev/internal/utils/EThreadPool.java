package com.analysys.dev.internal.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/23 10:53
 * @Author: Wang-X-C
 */
public class EThreadPool {

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void execute(Runnable command) {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }
        executor.execute(command);
    }
}
