package com.eguan.monitor.commonutils;

import com.eguan.monitor.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    /**
     * 时间戳转换
     *
     * @param currrentMillons
     * @return
     */
    public static String longToTime(long currrentMillons) {
        try {
            Date date = new Date(currrentMillons);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            return format.format(date) + ":";
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return "";
    }

    public static String longToDay(long currentMillons) {
        try {
            Date date = new Date(currentMillons);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return format.format(date);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return "";
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        return str;
    }

    /**
     * 获取上传间隔时间
     *
     * @return
     */
    public static long intervalTime() {
        long time = ((int) (Math.random() * 10)) * 60 * 1000 + Constants.RETRY_TIME;
        EgLog.i("---返回上传间隔时间---" + time / 60 / 1000);
        return time;
    }

    /*********************以下为应用监测SDK使用到的方法**********************/

    /**
     * get时间戳
     *
     * @return
     */
    public static long getTimestamp() {

        return System.currentTimeMillis();
    }

    /**
     * get 年月日时分秒
     *
     * @return
     */
    public static String getTimeInfo() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 为获取当前系统时间
        return df.format(new Date());
    }

    /**
     * 获取从即刻起，到过去几天的时间戳点
     *
     * @param d
     * @param day
     * @return 从现在到参数之前的时间戳
     */
    public static String getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        Date date = now.getTime();
        return String.valueOf(date.getTime());
    }

}
