package com.analysys.track.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.Utils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

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
                EGContext.isLocked = true;
                db = DBManager.getInstance(mContext).openDB();
                db.beginTransaction();
                db.execSQL("delete from " + DBConfig.AppSnapshot.TABLE_NAME);
                for (int i = 0; i < snapshots.size(); i++) {
                    JSONObject snapshot = snapshots.get(i);
                    db.insert(DBConfig.AppSnapshot.TABLE_NAME, null,
                        getContentValues(snapshot));
                }
                db.setTransactionSuccessful();
            }
        } catch (Throwable e) {
        } finally {
            db.endTransaction();
            EGContext.isLocked = false;
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 新增数据，用于安装广播
     */
    public void insert(JSONObject snapshots) {
        SQLiteDatabase db = null;
        try {
            EGContext.isLocked = true;
            db = DBManager.getInstance(mContext).openDB();
            db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshots));
            db.setTransactionSuccessful();
        } catch (Throwable e) {
        }finally {
            EGContext.isLocked = false;
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private ContentValues getContentValues(JSONObject snapshot) {
        ContentValues cv = new ContentValues();
        cv.put(DBConfig.AppSnapshot.Column.APN,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName));
        cv.put(DBConfig.AppSnapshot.Column.AN,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationName));
        cv.put(DBConfig.AppSnapshot.Column.AVC,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode));
        cv.put(DBConfig.AppSnapshot.Column.AT,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ActionType));
        cv.put(DBConfig.AppSnapshot.Column.AHT,
            snapshot.optString(DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime));
        return cv;
    }

    /**
     * 数据查询，格式：<pkgName,JSONObject>
     */
    public Map<String, String> mSelect() {
        Map<String, String> map = null;
        Cursor cursor = null;
        int blankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null,
                null, null);
            map = new HashMap<String, String>();
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return map;
                }
                String apn = cursor
                    .getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN));
                map.put(apn, String.valueOf(getCursor(cursor , blankCount)));
            }
        } catch (Throwable e) {
        } finally {
            if (cursor != null)
                cursor.close();
            DBManager.getInstance(mContext).closeDB();
        }
        return map;
    }

    private JSONObject getCursor(Cursor cursor,int blankCount) {
        JSONObject jsonObj = null;
        String pkgName = "";
        try {
            jsonObj = new JSONObject();
            pkgName = cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN));
            if(TextUtils.isEmpty(pkgName)){
                blankCount++;
            }
            Utils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName,pkgName,DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            Utils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ApplicationName,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AN)),DataController.SWITCH_OF_APPLICATION_NAME);
            Utils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ApplicationVersionCode,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AVC)),DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
            Utils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ActionType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AT)),DataController.SWITCH_OF_ACTION_TYPE);
            Utils.pushToJSON(mContext,jsonObj,DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AHT)),DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
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
            db.delete(DBConfig.AppSnapshot.TABLE_NAME,
                DBConfig.AppSnapshot.Column.AT + "=?", new String[] {"1"});
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.AppSnapshot.Column.AT, "-1");
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv,
                DBConfig.AppSnapshot.Column.AT + "!=? ", new String[] {"-1"});
        } catch (Throwable e) {
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 更新应用标识状态
     */
    public void update(String pkgName, String appTag) {
        try {
            EGContext.isLocked = true;
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.AppSnapshot.Column.AT, appTag);
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv,
                DBConfig.AppSnapshot.Column.APN + "= ? ", new String[] {pkgName});
        } catch (Throwable e) {
        } finally {
            EGContext.isLocked = false;
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public boolean isHasPkgName(String pkgName) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME,
                new String[] {DBConfig.AppSnapshot.Column.APN},
                DBConfig.AppSnapshot.Column.APN + "=?", new String[] {pkgName}, null,
                null, null);
            if (cursor.getCount() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (Throwable e) {
        } finally {
            if (cursor != null)
                cursor.close();
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
        int blankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            array = new JSONArray();
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null,
                null, null);
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return array;
                }
                if (cursor == null){
                    blankCount++;
                    continue;
                }
                array.put(getCursor(cursor,blankCount));
            }
        } catch (Throwable e) {
            ELOG.e(e);
        } finally {
            if (cursor != null){
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    public void delete() {
        try {
            EGContext.isLocked = true;
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            EGContext.isLocked = false;
            DBManager.getInstance(mContext).closeDB();
        }
    }
}