package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: JSON工具类
 * @Version: 1.0
 * @Create: 2019-08-05 16:33:36
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class JsonUtils {

    public static void pushToJSON(Context mContext, JSONObject json, String key, Object value, boolean SPDefaultValue) {
        try {
            if (value != null
                    && SPHelper.getBooleanValueFromSP(mContext, key, SPDefaultValue)
                    && !TextUtils.isEmpty(value.toString())
                    && !"unknown".equalsIgnoreCase(value.toString())) {
                if (!json.has(key)) {
                    json.put(key, value);
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }



    public static Set<String> transferStringArray2Set(String data) {
        String[] strArray = null;
        Set<String> nameSet = new HashSet<String>();
        data = data.replace("[", "")
                .replace("]", "");
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

    /**
     * 类型转换。装换结果为不重复的list
     *
     * @param arr
     * @return
     */
    public static List<String> converyJSONArrayToList(JSONArray arr) {
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < arr.length(); i++) {
            String a = arr.optString(i);
            // 非空不包含
            if (!TextUtils.isEmpty(a) && !result.contains(a)) {
                result.add(a);
            }
        }

        return result;

    }
//
//    public static void save(JSONObject json, String key, String value) {
//        try {
//            if (json != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
//                json.put(key, value.trim());
//            }
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(e);
//            }
//        }
//    }
//
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
