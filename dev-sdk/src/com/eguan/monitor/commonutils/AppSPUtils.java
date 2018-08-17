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


    public static synchronized AppSPUtils getInstance(Context context) {
        if (context == null) {
            return instance;
        }
        if (instance == null) {
            instance = new AppSPUtils();
            sp = SharedPreferencesUtils.getSharedPreferences(context, NAME);
            editor = sp.edit();
        }
        return instance;
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

}
