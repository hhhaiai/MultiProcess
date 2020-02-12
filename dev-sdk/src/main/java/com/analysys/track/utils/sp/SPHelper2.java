package com.analysys.track.utils.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


public class SPHelper2 {

    private static final String DEFAULT_SP = "ana_sp_xml_v3";

    public static SharedPreferences getDefault(Context context) {
        return context.getSharedPreferences(DEFAULT_SP, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context ctx) {
        return getDefault(ctx).edit();
    }

    /**
     * 存int类型数据入sp
     *
     * @param ctx
     * @param key
     * @param value
     */
    public static void setIntValue2SP(Context ctx, String key, int value) {
        getEditor(ctx).putInt(key, value).commit();
    }

    /**
     * 从sp取值
     *
     * @param ctx
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getIntValueFromSP(Context ctx, String key, int defaultValue) {
        return getDefault(ctx).getInt(key, defaultValue);
    }

    /**
     * @param ctx
     * @param key
     * @param value
     */
    public static void setStringValue2SP(Context ctx, String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        getEditor(ctx).putString(key, value).commit();
    }

    public static void setStringValue2SPCommit(Context ctx, String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        getEditor(ctx).putString(key, value).commit();
    }

    public static void setBooleanValue2SPCommit(Context ctx, String key, boolean value) {
        getEditor(ctx).putBoolean(key, value).commit();
    }

    /**
     * @param ctx
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getStringValueFromSP(Context ctx, String key, String defaultValue) {
        return getDefault(ctx).getString(key, defaultValue);
    }

    /**
     * @param ctx
     * @param key
     * @param value
     */
    public static void setBooleanValue2SP(Context ctx, String key, boolean value) {
        getEditor(ctx).putBoolean(key, value).commit();
    }

    /**
     * @param ctx
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBooleanValueFromSP(Context ctx, String key, boolean defaultValue) {
        return getDefault(ctx).getBoolean(key, defaultValue);
    }

    /**
     * @param ctx
     * @param key
     * @param value
     */
    public static void setLongValue2SP(Context ctx, String key, long value) {
        getEditor(ctx).putLong(key, value).commit();
    }

    public static void removeKey(Context ctx, String key) {
        getEditor(ctx).remove(key).commit();
    }

    /**
     * @param ctx
     * @param key
     * @param defaultValue
     * @return
     */
    public static long getLongValueFromSP(Context ctx, String key, long defaultValue) {
        return getDefault(ctx).getLong(key, defaultValue);
    }
}
