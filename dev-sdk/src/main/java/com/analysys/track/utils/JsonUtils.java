package com.analysys.track.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;

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

    public static void add(Context mContext, JSONObject json, String key, Object value, boolean SPDefaultValue) {
        try {
            if (value != null
//                    && SPHelper.getBooleanValueFromSP(mContext, key, SPDefaultValue)
                    && !TextUtils.isEmpty(value.toString())
                    && !Build.UNKNOWN.equalsIgnoreCase(value.toString())) {
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

    public static void add(JSONObject json, String key, Object value) {
        try {
            if (!TextUtils.isEmpty(key)
                    && value != null
                    && !TextUtils.isEmpty(value.toString())
                    && !Build.UNKNOWN.equalsIgnoreCase(value.toString())) {
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
        try {
            // data = data.replace("[", "") .replace("]", "");
            // 替换必须转义字符
            data = data.replaceAll("\\[", "").replaceAll("\\]", "");
            strArray = data.split(",");
            if (strArray != null && strArray.length > 0) {
                for (int i = 0; i < strArray.length; i++) {
                    String key = strArray[i];
                    nameSet.add(key.substring(1, key.length() - 1));
                }
            }
        } catch (Throwable e) {
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

    /**
     * merge two arrs to one array.
     *
     * @param arr1
     * @param arr2
     * @return
     */
    public static JSONArray mergeArrays(JSONArray arr1, JSONArray arr2) {
        JSONArray arr = new JSONArray();
        if (arr1 != null && arr1.length() > 0) {
            for (int i = 0; i < arr1.length(); i++) {
                String temp = arr1.optString(i, "");
                if (!TextUtils.isEmpty(temp)) {
                    arr1.put(temp);
                }
            }
        }
        if (arr2 != null && arr2.length() > 0) {
            for (int i = 0; i < arr2.length(); i++) {
                String temp = arr2.optString(i, "");
                if (!TextUtils.isEmpty(temp)) {
                    arr2.put(temp);
                }
            }
        }
        return arr;
    }

    /**
     * merge two arrs to one array.
     *
     * @param desk
     * @param subSrc
     * @return
     */
    public static JSONArray addAll(JSONArray desk, JSONArray subSrc) {
        if (desk == null) {
            desk = new JSONArray();
        }
        if (subSrc == null) {
            return desk;
        }
        for (int i = 0; i < subSrc.length(); i++) {
            Object o = subSrc.opt(i);
            if (o != null) {
                desk.put(o);
            }
        }
        return desk;
    }

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
