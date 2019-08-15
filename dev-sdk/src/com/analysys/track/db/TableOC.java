package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

public class TableOC {


    /**
     * 写入数据
     *
     * @param cv
     */
    public void insert(ContentValues cv) {

        try {
            if (cv == null && cv.size() < 1) {
                return;
            }
            if (cv.containsKey(DBConfig.OC.Column.ACT) && cv.containsKey(DBConfig.OC.Column.AOT)) {
                // 关闭时间
                String act = cv.getAsString(DBConfig.OC.Column.ACT);
                // 打开时间
                String aot = cv.getAsString(DBConfig.OC.Column.AOT);
                // 关闭时间-开启时间 大于3秒有意义
                if (Long.valueOf(act) - Long.valueOf(aot) < EGContext.MINDISTANCE * 3) {
                    return;
                }
            } else {
                // 没有开始关闭时间，丢弃
                return;
            }
        } catch (Throwable e) {
        }
        try {

            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null || cv.size() < 1) {
                return;
            }
            cv.put(DBConfig.OC.Column.CU, 1);
            long result = db.insert(DBConfig.OC.TABLE_NAME, null, cv);
            if (EGContext.DEBUG_OC) {
                ELOG.i("sanbo.oc", "写入  结果：[" + result + "]。。。。\n写入详情" + cv.toString());
            }
        } catch (
                Throwable e) {
            if (EGContext.DEBUG_OC) {
                ELOG.i("sanbo.oc", e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }


    /**
     * 读取
     */
    public JSONArray select(long maxLength) {
        JSONArray ocJar = null;
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        String pkgName = "", act = "";
        JSONObject jsonObject, etdm;
        try {
            if (db == null) {
                return ocJar;
            }
            ocJar = new JSONArray();
//            if (!db.isOpen()) {
//                db = DBManager.getInstance(mContext).openDB();
//            }
            db.beginTransaction();
            int id = -1;
            cursor = db.query(DBConfig.OC.TABLE_NAME, null, null, null, null, null, null, "6000");
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return ocJar;
                }

                // ACT不加密
                act = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.ACT));
                if (TextUtils.isEmpty(act) || "".equals(act)) {// closeTime为空，则继续循环，只取closeTime有值的信息
                    continue;
                }
                jsonObject = new JSONObject();

                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationCloseTime, act,
                        DataController.SWITCH_OF_APPLICATION_CLOSE_TIME);


                id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConfig.OC.Column.ID));
                //IT 不加密
                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT));


                //AOT 不加密
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationOpenTime,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AOT)),
                        DataController.SWITCH_OF_APPLICATION_OPEN_TIME);

                // APN 加密
                pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if (TextUtils.isEmpty(pkgName)) {
                    blankCount += 1;
                }
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationPackageName, pkgName,
                        DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);

                // AN 加密
                String an = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AN)));
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationName, an,
                        DataController.SWITCH_OF_APPLICATION_NAME);

                //AVC 加密
                String avc = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AVC)));
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationVersionCode, avc,
                        DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
                //NT不加密
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.NetworkType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.NT)),
                        DataController.SWITCH_OF_NETWORK_TYPE);

                etdm = new JSONObject();
                // AST 不加密
                JsonUtils.pushToJSON(mContext, etdm, UploadKey.OCInfo.SwitchType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AST)),
                        DataController.SWITCH_OF_SWITCH_TYPE);
                // AT 不加密
                JsonUtils.pushToJSON(mContext, etdm, UploadKey.OCInfo.ApplicationType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AT)),
                        DataController.SWITCH_OF_APPLICATION_TYPE);
                // CT 不加密
                JsonUtils.pushToJSON(mContext, etdm, UploadKey.OCInfo.CollectionType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.CT)),
                        DataController.SWITCH_OF_COLLECTION_TYPE);
                jsonObject.put(EGContext.EXTRA_DATA, etdm);
                if (countNum / 800 > 0) {
                    countNum = countNum % 800;
                    long size = String.valueOf(ocJar).getBytes().length;
                    if (size >= maxLength * 9 / 10) {
//                        ELOG.e(" size值：："+size+" maxLength = "+maxLength);
                        UploadImpl.isChunkUpload = true;
                        break;
                    } else {
                        ocJar.put(jsonObject);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DBConfig.OC.Column.ST, ONE);
                        db.update(DBConfig.OC.TABLE_NAME, contentValues, DBConfig.OC.Column.ID + "=?",
                                new String[]{String.valueOf(id)});
                    }
                } else {
                    ocJar.put(jsonObject);
                    ContentValues cv = new ContentValues();
                    cv.put(DBConfig.OC.Column.ST, ONE);
                    db.update(DBConfig.OC.TABLE_NAME, cv, DBConfig.OC.Column.ID + "=?",
                            new String[]{String.valueOf(id)});
                }

//                ELOG.e(" size值：："+size+" maxLength = "+maxLength);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            if (EGContext.DEBUG_OC) {
                ELOG.e("sanbo.oc", e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            if (db != null && db.isOpen() && db.inTransaction()) {
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return ocJar;
    }


    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
//            if (!db.isOpen()) {
//                db = DBManager.getInstance(mContext).openDB();
//            }
            db.delete(DBConfig.OC.TABLE_NAME, DBConfig.OC.Column.ST + "=?", new String[]{ONE});
//            ELOG.e("删除的行数：：：  "+count);
        } catch (Throwable e) {
            if (EGContext.DEBUG_OC) {
                ELOG.e("sanbo.oc", e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public void deleteAll() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
//            if (!db.isOpen()) {
//                db = DBManager.getInstance(mContext).openDB();
//            }
            db.delete(DBConfig.OC.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (EGContext.DEBUG_OC) {
                ELOG.e("sanbo.oc", e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private static class Holder {
        private static final TableOC INSTANCE = new TableOC();
    }


    private TableOC() {
    }

    public static TableOC getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    private final String ONE = "1";
    private Context mContext;
}
