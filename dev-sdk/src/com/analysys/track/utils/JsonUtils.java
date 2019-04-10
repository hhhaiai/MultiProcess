package com.analysys.track.utils;

import com.analysys.track.impl.PolicyImpl;
import com.analysys.track.utils.sp.SPHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import org.json.JSONObject;

public class JsonUtils {
    /**
     * 过滤掉value为空的数据
     * @param json
     * @param key
     * @param value
     * @param SPDefaultValue
     */
    private static SharedPreferences sp = null;
    public static void pushToJSON(Context mContext, JSONObject json, String key, Object value,boolean SPDefaultValue) {
        try {
            if(sp == null){
                sp =  PolicyImpl.getInstance(mContext).getSP();
            }
            if (value != null && (sp.getBoolean(key,SPDefaultValue) || SPHelper.getBooleanValueFromSP(mContext,key ,SPDefaultValue))&& !TextUtils.isEmpty(value.toString()) && !"unknown".equalsIgnoreCase(value.toString())) {
                if (!json.has(key)) {
                    json.put(key, value);
                }
            }
        } catch (Throwable e) {
            ELOG.e("pushToJSON has an exception... =" + e.getMessage());
        }
    }

}
