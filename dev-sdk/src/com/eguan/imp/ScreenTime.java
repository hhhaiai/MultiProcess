package com.eguan.imp;

import java.util.ArrayList;
import java.util.List;

public class ScreenTime {
    private static List<Long> onOffTime = new ArrayList<Long>();

    private static boolean status = false; // false-表示关屏，true-表示开屏

    public static List<Long> getOnOffTime() {
        return onOffTime;
    }

    public static void addOnOffTime(boolean st) {
        if (st == status) {
            onOffTime.add(System.currentTimeMillis());
            status = !status;
        }
    }

    public static void clearOnOffTime() {
        status = false;
        onOffTime.clear();
    }

}
