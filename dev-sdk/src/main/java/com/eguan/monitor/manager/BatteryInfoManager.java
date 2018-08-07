package com.eguan.monitor.manager;

/**
 * Created on 17/5/22.
 * Author : chris
 * Email  : mengqi@analysys.com.cn
 * Detail :电源相关:
 * BS=BatteryStatus电源状态;
 * BH=BatteryHealth电源健康情况;
 * BL=BatteryLevel电源当前电量;
 * BSL=BatteryScale电源总电量;
 * BP=BatteryPlugged电源连接插座;
 * BT=BatteryTechnology电源类型
 */

public class BatteryInfoManager {

    /**
     * values for "status" field in the ACTION_BATTERY_CHANGED Intent
     * public static final int BATTERY_STATUS_UNKNOWN = 1;
     * public static final int BATTERY_STATUS_CHARGING = 2;
     * public static final int BATTERY_STATUS_DISCHARGING = 3;
     * public static final int BATTERY_STATUS_NOT_CHARGING = 4;
     * public static final int BATTERY_STATUS_FULL = 5;
     */
    private static String bs = "";
    /**
     * values for "health" field in the ACTION_BATTERY_CHANGED Intent
     * public static final int BATTERY_HEALTH_UNKNOWN = 1;
     * public static final int BATTERY_HEALTH_GOOD = 2;
     * public static final int BATTERY_HEALTH_OVERHEAT = 3;
     * public static final int BATTERY_HEALTH_DEAD = 4;
     * public static final int BATTERY_HEALTH_OVER_VOLTAGE = 5;
     * public static final int BATTERY_HEALTH_UNSPECIFIED_FAILURE = 6;
     * public static final int BATTERY_HEALTH_COLD = 7;
     */
    private static String bh = "";
    /**
     * the current battery level, from 0 to {@link #bsl}
     */
    private static String bl = "";
    /**
     * the maximum battery level
     */
    private static String bsl = "";
    /**
     * Power source is an AC charger.
     * public static final int BATTERY_PLUGGED_AC = 1;
     * Power source is a USB port.
     * public static final int BATTERY_PLUGGED_USB = 2;
     * Power source is wireless.
     * public static final int BATTERY_PLUGGED_WIRELESS = 4;
     */
    private static String bp = "";
    /**
     * String describing the technology of the current battery
     */
    private static String bt = "";

    public static String getBs() {
        return bs;
    }

    public static void setBs(String bs) {
        BatteryInfoManager.bs = bs;
    }

    public static String getBh() {
        return bh;
    }

    public static void setBh(String bh) {
        BatteryInfoManager.bh = bh;
    }

    public static String getBl() {
        return bl;
    }

    public static void setBl(String bl) {
        BatteryInfoManager.bl = bl;
    }

    public static String getBsl() {
        return bsl;
    }

    public static void setBsl(String bsl) {
        BatteryInfoManager.bsl = bsl;
    }

    public static String getBp() {
        return bp;
    }

    public static void setBp(String bp) {
        BatteryInfoManager.bp = bp;
    }

    public static String getBt() {
        return bt;
    }

    public static void setBt(String bt) {
        BatteryInfoManager.bt = bt;
    }
}
