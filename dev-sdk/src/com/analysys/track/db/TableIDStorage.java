package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

public class TableIDStorage {


    /**
     * 存储eguanid tempid
     *
     * @param tmpId
     */
    public void insert(String tmpId) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            // 如果db对象为空，或者tmpId为空，则return
            if (db == null || TextUtils.isEmpty(tmpId)) {
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.IDStorage.Column.TEMPID, EncryptUtils.encrypt(mContext, tmpId));
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.insert(DBConfig.IDStorage.TABLE_NAME, null, cv);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 读取egid、tmpid
     *
     * @return
     */
    public String select() {
        String tmpid = "";
        Cursor cursor = null;
        int blankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return tmpid;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            cursor = db.query(DBConfig.IDStorage.TABLE_NAME, null, null, null, null, null, null);
            if (cursor == null) {
                return tmpid;
            }
            while (cursor.moveToNext()) {
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return tmpid;
                }
                tmpid = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.IDStorage.Column.TEMPID)));
                if (TextUtils.isEmpty(tmpid)) {
                    blankCount += 1;
                    continue;
                }

            }
        } catch (Throwable e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return tmpid;
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
            db.delete(DBConfig.IDStorage.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private static class Holder {
        private static final TableIDStorage INSTANCE = new TableIDStorage();
    }

    private Context mContext;

    private TableIDStorage() {
    }

    public static TableIDStorage getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }
}