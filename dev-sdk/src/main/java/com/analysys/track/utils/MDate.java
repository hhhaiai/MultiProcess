package com.analysys.track.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Copyright © 2020 analysys Inc. All rights reserved.
 * @Description: 日期
 * @Version: 1.0
 * @Create: Jul 30, 2020 4:21:24 PM
 * @author: sanbo
 */
public class MDate {


    public static String getTime() {
        return new SimpleDateFormat("HH:mm:sss").format(new Date(System.currentTimeMillis()));
    }

    public static String getTime(long time) {
        return new SimpleDateFormat("HH:mm:sss").format(new Date(time));
    }

    public static final String getToday() {
        return new SimpleDateFormat("YYYY-MM-dd").format(new Date(System.currentTimeMillis()));
    }

    public static final String getNow() {
        return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
    }

    public static final int getHour() {
        return Integer.valueOf(new SimpleDateFormat("HH").format(new Date(System.currentTimeMillis())));
    }

    public static final String convertLongTimeToHms(long interval) {
        return  new SimpleDateFormat("HH:mm:ss.SSS").format(interval);
    }

    public static final String getDateFromTimestamp(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return formatter.format(timestamp);
    }

    public static final int getHourFromTimestamp(long timestamp) {
        return Integer.valueOf(new SimpleDateFormat("HH").format(timestamp));
    }

    public static final int getDaysFromTimestamp(long timestamp) {
        return Integer.valueOf(new SimpleDateFormat("dd").format(timestamp));
    }

    public static final long getDuration(long timeA, long timeB) {
        return Math.abs(timeB - timeA);
    }

}
