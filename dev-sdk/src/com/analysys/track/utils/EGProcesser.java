package com.analysys.track.utils;

import org.json.JSONObject;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.model.SoftwareInfo;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

public class EGProcesser {

    /**
     * 更新易观id和临时id
     */
    public static void setId(String json, Context ctx) {

        try {
            String tmpId = "", egid = "";
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("tmpid")) {
                tmpId = jsonObject.optString("tmpid");

            }
            if (jsonObject.has("egid")) {
                egid = jsonObject.optString("egid");
            }

            if (!TextUtils.isEmpty(tmpId) || !TextUtils.isEmpty(egid)) {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/" + EGContext.EGUANFILE;
                FileUtils.writeFile(egid, tmpId, filePath);
                writeShared(egid, tmpId);
                writeSetting(egid, tmpId);
                // writeDatabase(egid, tmpId);
                // TODO
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
     * 向database中存储数据
     */
    // private void writeDatabase(String egId, String tmpId) {
    //
    // DeviceTableOperation tabOpe = DeviceTableOperation.getInstance(context);
    // if (!TextUtils.isEmpty(egId)) {
    // tabOpe.insertEguanId(egId);
    // }
    // if (!TextUtils.isEmpty(tmpId)) {
    // tabOpe.insertTmpId(tmpId);
    // }
    // }
    /**
     * 向shared中存储数据
     *
     * @param eguanId
     */
    public static void writeShared(String eguanId, String tmpid) {

        if (!TextUtils.isEmpty(eguanId)) {
            SoftwareInfo.getInstance().setEguanID(eguanId);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit()
                .putString(DeviceKeyContacts.DevInfo.EguanID, eguanId).commit();
        }
        if (!TextUtils.isEmpty(tmpid)) {
            SoftwareInfo.getInstance().setTempID(tmpid);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit()
                .putString(DeviceKeyContacts.DevInfo.TempID, tmpid).commit();
        }
    }

}
