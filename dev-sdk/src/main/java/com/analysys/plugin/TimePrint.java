package com.analysys.plugin;

import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 方法耗时时间打印
 * @Version: 1.0
 * @Create: 2019-11-13 11:26:43
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class TimePrint {
    private static HashMap<String, Long> map = new HashMap<>();

    public static void start(String name) {
        Log.d("TimePrint", name + ": -->");
        map.put(name, SystemClock.currentThreadTimeMillis());
    }

    public static void end(String name) {
        long time = map.remove(name);
        time = SystemClock.currentThreadTimeMillis() - time;
        Log.d("TimePrint", name + ": <-- " + "time:" + time);
    }
}
