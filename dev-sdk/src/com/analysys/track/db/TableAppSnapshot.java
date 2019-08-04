package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableAppSnapshot {

    Context mContext;

    private TableAppSnapshot() {
    }

    public static TableAppSnapshot getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * 覆盖新增数据，多条
     */
    public void coverInsert(List<JSONObject> snapshots) {
        SQLiteDatabase db = null;
        try {
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_APPSNAPSHOT)){
//                return;
//            }
            db = DBManager.getInstance(mContext).openDB();
            db.beginTransaction();
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
            JSONObject snapshot = null;
            for (int i = 0; i < snapshots.size(); i++) {
                snapshot = null;
                snapshot = (JSONObject) snapshots.get(i);
                db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshot));
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
        } finally {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 新增数据，用于安装广播
     */
    public void insert(JSONObject snapshots) {
        SQLiteDatabase db = null;
        try {
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_APPSNAPSHOT)){
//                return;
//            }
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshots));
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private ContentValues getContentValues(JSONObject snapshot) {
        ContentValues cv = new ContentValues();
        String an = EncryptUtils.encrypt(mContext,
                snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationName));
//        ELOG.i(an+ " getContentValues  an");
        cv.put(DBConfig.AppSnapshot.Column.APN, EncryptUtils.encrypt(mContext,
                snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName)));
        cv.put(DBConfig.AppSnapshot.Column.AN, an);
        cv.put(DBConfig.AppSnapshot.Column.AVC, EncryptUtils.encrypt(mContext,
                snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode)));
        cv.put(DBConfig.AppSnapshot.Column.AT,
                EncryptUtils.encrypt(mContext, snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ActionType)));
        cv.put(DBConfig.AppSnapshot.Column.AHT, snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime));
        return cv;
    }

    /**
     * 数据查询，格式：<pkgName,JSONObject>
     */
    public Map<String, String> snapShotSelect() {
        Map<String, String> map = null;
        Cursor cursor = null;
        int blankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return map;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null, null, null);
            map = new HashMap<String, String>();
            while (cursor.moveToNext()) {
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return map;
                }
                String apn = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
                if (!TextUtils.isEmpty(apn)) {
                    map.put(apn, String.valueOf(getCursor(cursor)));
                } else {
                    blankCount += 1;
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return map;
    }

    private JSONObject getCursor(Cursor cursor) {
        JSONObject jsonObj = null;
        String pkgName = "";
        try {
            jsonObj = new JSONObject();
            String an = EncryptUtils.decrypt(mContext,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AN)));
            pkgName = EncryptUtils.decrypt(mContext,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
            JsonUtils.pushToJSON(mContext, jsonObj, DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName, pkgName,
                    DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            JsonUtils.pushToJSON(mContext, jsonObj, DeviceKeyContacts.AppSnapshotInfo.ApplicationName, an,
                    DataController.SWITCH_OF_APPLICATION_NAME);
            JsonUtils.pushToJSON(mContext, jsonObj, DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode,
                    EncryptUtils.decrypt(mContext,
                            cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AVC))),
                    DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
            JsonUtils.pushToJSON(mContext, jsonObj, DeviceKeyContacts.AppSnapshotInfo.ActionType,
                    EncryptUtils.decrypt(mContext,
                            cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AT))),
                    DataController.SWITCH_OF_ACTION_TYPE);
            JsonUtils.pushToJSON(mContext, jsonObj, DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AHT)),
                    DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return jsonObj;
    }

    /**
     * 更新应用标识状态
     */
    public void update(String pkgName, String appTag, long time) {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.AppSnapshot.Column.AT, EncryptUtils.encrypt(mContext, appTag));
            cv.put(DBConfig.AppSnapshot.Column.AHT, time);
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, DBConfig.AppSnapshot.Column.APN + "= ? ",
                    new String[] { EncryptUtils.encrypt(mContext, pkgName) });
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }

        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public boolean isHasPkgName(String pkgName) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return false;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, DBConfig.AppSnapshot.Column.APN + "=?",
                    new String[] { EncryptUtils.encrypt(mContext, pkgName) }, null, null, null);
//            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME,
//                new String[] {DBConfig.AppSnapshot.Column.APN},
//                DBConfig.AppSnapshot.Column.APN + "=?", new String[] {pkgName}, null,
//                null, null);
            if (cursor.getCount() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return false;
    }

    /**
     * 数据查询，格式：{JSONObject}
     */
    public JSONArray select(long maxLength) {
        JSONArray array = null;
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        JSONObject jsonObject = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return array;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            array = new JSONArray();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null, null, null, "4000");
            if (cursor == null) {
                return array;
            }
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return array;
                }
                String pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
                if (!TextUtils.isEmpty(pkgName)) {
                    jsonObject = getCursor(cursor);
                } else {
                    blankCount += 1;
                    continue;
                }
                if (countNum / 300 > 0) {
                    countNum = countNum % 300;
                    long size = String.valueOf(array).getBytes().length;
                    if (size >= maxLength * 9 / 10) {
//                        ELOG.e(" size值：："+size+" maxLength = "+maxLength);
                        UploadImpl.isChunkUpload = true;
                        break;
                    } else {
                        array.put(jsonObject);
                    }
                } else {
                    array.put(jsonObject);
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, DBConfig.AppSnapshot.Column.AT + "=?",
                    new String[] { EncryptUtils.encrypt(mContext, EGContext.SNAP_SHOT_UNINSTALL) });
//            ELOG.e("AppSnapshot 删除行数：：："+co);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 更新应用标识状态
     */
    public void update() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.AppSnapshot.Column.AT, EncryptUtils.encrypt(mContext, EGContext.SNAP_SHOT_INSTALL));
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, null, null);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
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
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private static class Holder {
        private static final TableAppSnapshot INSTANCE = new TableAppSnapshot();
    }
}