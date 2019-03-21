package com.analysys.track.utils;

import com.analysys.track.impl.PolicyImpl;
import com.analysys.track.utils.sp.SPHelper;
import android.content.Context;
import android.text.TextUtils;
import org.json.JSONObject;

public class JsonUtils {
//    /**
//     * 更新易观id和临时id
//     */
//    public static void setId(String json,Context ctx) {
//
//        try {
//            String tmpId = "", egid = "";
//            JSONObject jsonObject = new JSONObject(json);
//            if (jsonObject.has("tmpid")) {
//                tmpId =  jsonObject.optString("tmpid");
//
//            }
//            if (jsonObject.has("egid")) {
//                egid =  jsonObject.optString("egid");
//            }
//
//            if (!TextUtils.isEmpty(tmpId) || !TextUtils.isEmpty(egid)) {
//                String filePath = Environment.getExternalStorageDirectory().toString() + "/" + EGContext.EGUANFILE;
//                FileUtils.writeFile(egid, tmpId ,filePath);
//                writeShared(egid, tmpId);
//                writeSetting(egid, tmpId);
////                writeDatabase(egid, tmpId);
//            }
//        } catch (Throwable e) {
//        }
//    }
//
//
//    /**
//     * 向Setting中存储数据
//     *
//     * @param egId
//     */
//    private static void writeSetting(String egId, String tmpId) {
//
//        Context ctx = EContextHelper.getContext(null);
//        if (PermissionUtils.checkPermission(ctx, Manifest.permission.WRITE_SETTINGS)) {
//            if (!TextUtils.isEmpty(egId)) {
//                Settings.System.putString(ctx.getContentResolver(), EGContext.EGIDKEY, egId);
//            }
//            if (!TextUtils.isEmpty(tmpId)) {
//                Settings.System.putString(ctx.getContentResolver(), EGContext.TMPIDKEY, tmpId);
//            }
//        }
//    }
//    /**
//     * 向shared中存储数据
//     *
//     * @param eguanId
//     */
//    public static void writeShared(String eguanId, String tmpid) {
//
//        if (!TextUtils.isEmpty(eguanId)) {
//            SoftwareInfo.getInstance().setEguanID(eguanId);
//            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.EguanID,eguanId).commit();
//        }
//        if (!TextUtils.isEmpty(tmpid)) {
//            SoftwareInfo.getInstance().setTempID(tmpid);
//            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.TempID,tmpid).commit();
//        }
//    }
    /**
     * 过滤掉value为空的数据
     * @param json
     * @param key
     * @param value
     * @param SPDefaultValue
     */
    public static void pushToJSON(Context mContext, JSONObject json, String key, Object value,boolean SPDefaultValue) {
        try {
//            ELOG.i("come in pushToJSON ");
//            ELOG.i("1 pushToJSON "+key +"  =====   " + value);
            //(PolicyImpl.getInstance(mContext).getSP().getBoolean(key,SPDefaultValue) || SPHelper.getDefault(mContext).getBoolean(key ,SPDefaultValue))
            if ((PolicyImpl.getInstance(mContext).getSP().getBoolean(key,SPDefaultValue) || SPHelper.getDefault(mContext).getBoolean(key ,SPDefaultValue))&& !TextUtils.isEmpty(value.toString()) && !"unknown".equalsIgnoreCase(value.toString())) {
//                ELOG.i("2 pushToJSON "+key+"  =====   " +value);
                if (!json.has(key)) {
//                    ELOG.i("3 pushToJSON "+key+"  =====   " +value);
                    json.put(key, value);
                }
            }
        } catch (Throwable e) {
            ELOG.e("pushToJSON has an exception... =" + e.getMessage());
        }
    }

}
