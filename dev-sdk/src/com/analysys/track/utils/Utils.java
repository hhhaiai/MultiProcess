package com.analysys.track.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.util.HashSet;
import java.util.Set;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;
import com.analysys.track.internal.Content.EGContext;

import com.analysys.track.model.SoftwareInfo;

import android.Manifest;
import android.content.Context;

import android.os.Environment;
import android.provider.Settings;

import android.text.TextUtils;
import org.json.JSONObject;

public class Utils {
    /**
     * 更新易观id和临时id
     */
    public static void setId(String json,Context ctx) {

        try {
            String tmpId = "", egid = "";
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("tmpid")) {
                tmpId =  jsonObject.optString("tmpid");

            }
            if (jsonObject.has("egid")) {
                egid =  jsonObject.optString("egid");
            }

            if (!TextUtils.isEmpty(tmpId) || !TextUtils.isEmpty(egid)) {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/" + EGContext.EGUANFILE;
                FileUtils.writeFile(egid, tmpId ,filePath);
                writeShared(egid, tmpId);
                writeSetting(egid, tmpId);
//                writeDatabase(egid, tmpId);
                //TODO
            }
        } catch (Throwable e) {
        }
    }


    /**
     * 向Setting中存储数据
     *
     * @param egId
     */
    private static void writeSetting(String egId, String tmpId) {

        Context ctx = EContextHelper.getContext(null);
        if (PermissionUtils.checkPermission(ctx, Manifest.permission.WRITE_SETTINGS)) {
            if (!TextUtils.isEmpty(egId)) {
                Settings.System.putString(ctx.getContentResolver(), EGContext.EGIDKEY, egId);
            }
            if (!TextUtils.isEmpty(tmpId)) {
                Settings.System.putString(ctx.getContentResolver(), EGContext.TMPIDKEY, tmpId);
            }
        }
    }
    /**
     * 向shared中存储数据
     *
     * @param eguanId
     */
    public static void writeShared(String eguanId, String tmpid) {

        if (!TextUtils.isEmpty(eguanId)) {
            SoftwareInfo.getInstance().setEguanID(eguanId);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.EguanID,eguanId).commit();
        }
        if (!TextUtils.isEmpty(tmpid)) {
            SoftwareInfo.getInstance().setTempID(tmpid);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.TempID,tmpid).commit();
        }
    }
    /**
     * 过滤掉value为空的数据
     * @param json
     * @param key
     * @param value
     * @param SPDefaultValue
     */
    public static void pushToJSON(Context mContext, JSONObject json, String key, Object value,boolean SPDefaultValue) {
        try {
            if (SPHelper.getDefault(mContext).getBoolean(key ,SPDefaultValue) && !TextUtils.isEmpty(value.toString()) && !"unknown".equalsIgnoreCase(value.toString())) {
                if (!json.has(key)) {
                    json.put(key, value);
                }
            }
        } catch (Throwable e) {
        }
    }

}
