package com.eguan.monitor.commonutils;

import android.content.Context;
import android.content.SharedPreferences;

import com.eguan.monitor.Constants;

/**
 * Created on 2017/8/25.
 * Author : chris
 * Email  : mengqi@analysys.com.cn
 * Detail :
 */

public class AppSPUtils {
    private static final String NAME = "app_sp";
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static AppSPUtils instance = null;
    private static final String APP_LAST_OPEN_TIME = "app_last_open_time";


    public static synchronized AppSPUtils getInstance(Context context) {
        if (context == null) {
            return instance;
        }
        if (instance == null) {
            instance = new AppSPUtils();
//            sp = context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
            sp = SharedPreferencesUtils.getSharedPreferences(context, NAME);
            editor = sp.edit();
        }
        return instance;
    }


//    private static final String APP_OC_CACHE = "app_oc_cache";
//    public void setCurrentOC(String jsonString) {
//        editor.putString(APP_OC_CACHE, jsonString);
//        editor.commit();
//    }
//    public void clearCurrentOC() {
//        editor.putString(APP_OC_CACHE, "");
//        editor.commit();
//    }
//    public String getCurrentOC() {
//        return sp.getString(APP_OC_CACHE, "");
//    }
//
//
//    public void setLastOpenTimeForApplication(String l) {
//        editor.putString(APP_LAST_OPEN_TIME, l);
//        editor.commit();
//    }
//
//    public String getLastOpenTimeForApplication() {
//        return sp.getString(APP_LAST_OPEN_TIME, "");
//    }
//
//    public void setUpdateEGUserTag(String info) {
//        editor.putString(Constants.UPDATE_EGUSER, info);
//        editor.commit();
//    }
//
//    public String getUpdateEGUserTag() {
//        return sp.getString(Constants.UPDATE_EGUSER, "");
//    }
//

    /**
     * 存储H5传过来的H5的用户ID
     */
    private static final String HUID = "HUID";
//    public void putHUID(String huid) {
//        editor.putString(HUID, huid);
//        editor.commit();
//    }

    public String getHUID() {
        return sp.getString(HUID, "");
    }

    public void setMergeInterval(long info) {
        editor.putLong(Constants.MERGE_INTERVAL, info);
        editor.commit();
    }

    public void setPolicyVer(String str) {
        editor.putString(Constants.POLICY_VER, str);
        editor.commit();
    }

    public String getPolicyVer() {
        return sp.getString(Constants.POLICY_VER, "0");
    }

//    public void setAppTactics(String info) {
//        editor.putString(Constants.APP_TACTICS, info);
//        editor.commit();
//    }
//
//    public String getAppTactics() {
//        return sp.getString(Constants.APP_TACTICS, "");
//    }
//
//    public int getIFS(Context context) {
//        return sp.getInt("ifs", 1);
//    }
//
//    public void setIFS(Context context, int value) {
//        editor.putInt("ifs", value);
//        editor.commit();
//    }
//
//
//    /**
//     * 宿主应用,当前正在使用的用户的用户ID信息
//     */
//    private static final String USERID = "userId";
//    public static final String USERID_DEFAULT = "";
//    private static final String FAIL_TRY_DELAY_INTERVAL = "failTryDelay";
//    private static final String SERVER_DELAY_INTERVAL = "serverDelay";
//
//    private static final String PROFILE_UPRO = "UPRO";

//    /**
//     * 提供给onProfileSginOn方法使用
//     *
//     * @param id
//     */
//    public void setProfileUserId(String id) {
//
//        editor.putString(USERID, id);
//        editor.apply();
//    }
//
//    /**
//     * 读出来的值为 USERID_DEFAULT 不为空串,是为了保障调用层知道执行过此方法,且收到了确定的值.
//     * 用于与调用层的变量值丢失情况做对比.例如initEguan调用此方法.
//     *
//     * @return userId
//     */
//    public String getProfileUserId() {
//        return sp.getString(USERID, USERID_DEFAULT);
//    }
//
//    public void setFailTryDelay(long interval) {
//        editor.putLong(FAIL_TRY_DELAY_INTERVAL, interval);
//        editor.apply();
//    }
//
//    public long getFailTryDelay() {
//        return sp.getLong(FAIL_TRY_DELAY_INTERVAL, 0);
//    }
//
//    public void setServerDelay(long interval) {
//        editor.putLong(SERVER_DELAY_INTERVAL, interval);
//        editor.apply();
//    }
//
//    public long getServerDelay() {
//        return sp.getLong(SERVER_DELAY_INTERVAL, 0);
//    }
//
//    public String getProfileUPRO() {
//        return sp.getString(PROFILE_UPRO, "");
//    }
//
//    public void setProfileUPRO(String UPRO) {
//        editor.putString(PROFILE_UPRO, UPRO);
//        editor.apply();
//    }

    /**
     * 测试接口
     */
    public boolean getDebugMode() {
        return sp.getBoolean(Constants.DEBUGURL, false);
    }

    public void setDebugMode(boolean debug) {
        editor.putBoolean(Constants.DEBUGURL, debug);
        editor.commit();
    }
//
//    /**
//     * 只需要一个方法，用来自增NotificationID用
//     * @return
//     */
//    public int getADID() {
//        int id = sp.getInt("ADID",0);
//        editor.putInt("ADID",id+1);
//        editor.commit();
//        return id;
//    }

}
