package com.miqt.costtime;

import android.os.SystemClock;
import android.util.Log;

import java.util.HashMap;

public class TimePrint {
    static HashMap<String, Long> map = new HashMap();

    public static void start(String name) {
        Log.v("TimePrint", name + ": -->");
        map.put(name, SystemClock.currentThreadTimeMillis());
    }

    public static void end(String name) {
        long time = map.remove(name);
        time = SystemClock.currentThreadTimeMillis() - time;
        Log.v("TimePrint", name + ": <-- " + "time:" + time);
    }
}
