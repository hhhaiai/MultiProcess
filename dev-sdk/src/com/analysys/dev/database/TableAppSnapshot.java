package com.analysys.dev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class TableAppSnapshot {

    Context mContext;

    private static class Holder {
        private static final TableAppSnapshot INSTANCE = new TableAppSnapshot();
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
            if (snapshots.size() > 0) {
                db = DBManager.getInstance(mContext).openDB();
                db.beginTransaction();
                db.execSQL("delete from " + DBConfig.AppSnapshot.TABLE_NAME);
                for (int i = 0; i < snapshots.size(); i++) {
                    JSONObject snapshot = snapshots.get(i);
                    db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshot));
                }
                db.setTransactionSuccessful();
            }
        } catch (Throwable e) {
        } finally {
            db.endTransaction();
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 新增数据，用于安装广播
     */
    public void insert(JSONObject snapshots) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshots));
            db.setTransactionSuccessful();
        } catch (Throwable e) {
        }
        DBManager.getInstance(mContext).closeDB();
    }

    private ContentValues getContentValues(JSONObject snapshot) {
        ContentValues cv = new ContentValues();
        cv.put(DBConfig.AppSnapshot.Column.APN, snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName));
        cv.put(DBConfig.AppSnapshot.Column.AN, snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationName));
        cv.put(DBConfig.AppSnapshot.Column.AVC, snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode));
        cv.put(DBConfig.AppSnapshot.Column.AT, snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ActionType));
        cv.put(DBConfig.AppSnapshot.Column.AHT, snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime));
        return cv;
    }

    /**
     * 数据查询，格式：<pkgName,JSONObject>
     */
    public Map<String, String> mSelect() {
        Map<String, String> map = null;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null, null, null);
            map = new HashMap<String,String>();
            while (cursor.moveToNext()) {
                String apn = cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN));
                map.put(apn, String.valueOf(getCursor(cursor)));
            }
        } catch (Throwable e) {
        }finally {
            if(cursor!= null) cursor.close();
            DBManager.getInstance(mContext).closeDB();
        }
        return map;
    }

    private JSONObject getCursor(Cursor cursor) {
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject();
            jsonObj.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName,
                cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
            jsonObj.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationName,
                cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AN)));
            jsonObj.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode,
                cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AVC)));
            jsonObj.put(DeviceKeyContacts.AppSnapshotInfo.ActionType,
                cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AT)));
            jsonObj.put(DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime,
                cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AHT)));
        } catch (Throwable e) {
        }
        return jsonObj;
    }

    /**
     * 重置应用状态标识
     */
    public void resetAppType() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, DBConfig.AppSnapshot.Column.AT + "=?", new String[] {"1"});
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.AppSnapshot.Column.AT, "-1");
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, DBConfig.AppSnapshot.Column.AT + "!=? ",
                new String[] {"-1"});
        } catch (Throwable e) {
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 更新应用标识状态
     */
    public void update(String pkgName, String appTag) {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.AppSnapshot.Column.AT, appTag);
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, DBConfig.AppSnapshot.Column.APN + "= ? ",
                new String[] {pkgName});
        } catch (Throwable e) {
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public boolean isHasPkgName(String pkgName) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, new String[] {DBConfig.AppSnapshot.Column.APN},
                DBConfig.AppSnapshot.Column.APN + "=?", new String[] {pkgName}, null, null, null);
            if (cursor.getCount() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Throwable e) {
        }finally {
            if(cursor != null) cursor.close();
            DBManager.getInstance(mContext).closeDB();
        }
        return false;
    }

    /**
     * 数据查询，格式：{JSONObject}
     */
    public JSONArray select() {
        JSONArray array = null;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            array = new JSONArray();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                if(cursor == null) continue;
                array.put(getCursor(cursor));
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }finally {
            if(cursor != null) cursor.close();
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }
    public void delete(){
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
}