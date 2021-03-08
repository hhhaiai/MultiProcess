package com.analysys.track.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Copyright © 2020 analysys Inc. All rights reserved.
 * @Description: 日期
 * @Version: 1.0
 * @Create: Jul 30, 2020 4:21:24 PM
 * @author: sanbo
 */
public class MDate {


    public static final String getToday() {
        return new SimpleDateFormat("YYYY-MM-dd").format(new Date());
    }

    public static final String getNow() {
        return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date());
    }

    public static final int getHour() {
        return Integer.valueOf(new SimpleDateFormat("HH").format(new Date()));
    }

    public static final String formatLongTimeToHms(long interval) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        return formatter.format(interval);
    }

    public static final String formatLongTimeToDate(long interval) {
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return formatter.format(interval);
    }

    public static final int getHourFromTime(long timestamp) {
        return Integer.valueOf(new SimpleDateFormat("HH").format(timestamp));
    }

    public static final int getDaysFromTime(long timestamp) {
        return Integer.valueOf(new SimpleDateFormat("dd").format(timestamp));
    }

    public static final long getDuration(long timeA, long timeB) {
        return Math.abs(timeB - timeA);
    }

}
