package com.analysys.dev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.dev.utils.Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/17 12:10
 * @Author: Wang-X-C
 */
public class TableLocation {

    Context mContext;

    private static class Holder {
        private static final TableLocation INSTANCE = new TableLocation();
    }

    public static TableLocation getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    public void insert(String locationInfo) {
        try {
            if (!TextUtils.isEmpty(locationInfo)) {
                long time = System.currentTimeMillis();
                String encryptLocation = Utils.encrypt(locationInfo, time);
                if (!TextUtils.isEmpty(encryptLocation)) {
                    ContentValues cv = new ContentValues();
                    cv.put(DBConfig.Location.Column.LI, encryptLocation);
                    cv.put(DBConfig.Location.Column.IT, String.valueOf(time));
                    cv.put(DBConfig.Location.Column.ST, "0");
                    SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
                    db.insert(DBConfig.Location.TABLE_NAME, null, cv);
                    DBManager.getInstance(mContext).closeDB();
                }
            }
        } catch (Throwable e) {

        }
    }

    public JSONArray select() {
        JSONArray jar = null;
        try {
            jar = new JSONArray();
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            Cursor cursor = db.query(DBConfig.Location.TABLE_NAME, null,
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.ID));
                String encryptLocation = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.LI));
                String time = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.IT));
                ContentValues cv = new ContentValues();
                cv.put(DBConfig.Location.Column.ST, "1");
                db.update(DBConfig.Location.TABLE_NAME, cv, DBConfig.Location.Column.ID + "=?", new String[]{id});
                String decryptLocation = Utils.decrypt(encryptLocation, Long.valueOf(time));
                jar.put(new JSONObject(decryptLocation));
            }
        } catch (Throwable e) {
        }
        return jar;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            db.delete(DBConfig.Location.TABLE_NAME, DBConfig.Location.Column.ST + "=?", new String[]{"1"});
            DBManager.getInstance(mContext).closeDB();
        } catch (Throwable e) {
        }
    }

}
