package com.analysys.track.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class JsonUtils {
    /**
     * 过滤掉value为空的数据
     *
     * @param json
     * @param key
     * @param value
     * @param SPDefaultValue
     */
    private static SharedPreferences sp = null;

    public static void pushToJSON(Context mContext, JSONObject json, String key, Object value, boolean SPDefaultValue) {
        try {
            if (sp == null) {
                sp = PolicyImpl.getInstance(mContext).getSP();
            }
            if (value != null
                    && (SPHelper.getBooleanValueFromSP(mContext, key, SPDefaultValue)
                    && sp.getBoolean(key, SPDefaultValue))
                    && !TextUtils.isEmpty(value.toString()) && !"unknown".equalsIgnoreCase(value.toString())) {
                if (!json.has(key)) {
                    json.put(key, value);
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
    }

    public static void save(JSONObject json, String key, String value) {
        try {
            if (json != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                json.put(key, value.trim());
            }
        } catch (Throwable e) {
        }
    }

    public static Set<String> transferStringArray2Set(String data) {
        String[] strArray = null;
        Set<String> nameSet = new HashSet<String>();
        data = data.replace("[", "").replace("]", "");
        strArray = data.split(",");
        if (strArray != null && strArray.length > 0) {
            String key = null;
            for (int i = 0; i < strArray.length; i++) {
                key = strArray[i];
                nameSet.add(key.substring(1, key.length() - 1));
            }
        }
        return nameSet;
    }

//    public static List<JSONObject> jsonArray2JsonObjList(JSONArray array) {
//        List<JSONObject> list = null;
//        try {
//            JSONObject obj = null;
//            if (array != null && array.length() > 0) {
//                list = new ArrayList<JSONObject>();
//                for (int i = 0; i < array.length(); i++) {
//                    obj = (JSONObject) array.get(i);
//                    list.add(obj);
//                }
//            }
//        } catch (Throwable t) {
//        }
//
//        return list;
//    }

}
