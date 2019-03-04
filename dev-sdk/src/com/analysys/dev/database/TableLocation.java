package com.analysys.dev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.utils.Base64Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public void insert(JSONObject locationInfo) {
        try {
            if (!TextUtils.isEmpty(locationInfo.toString())) {
                long time = Long.parseLong(locationInfo.optString(DeviceKeyContacts.LocationInfo.CollectionTime));
                String encryptLocation = Base64Utils.encrypt(locationInfo.toString(), time);
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
                String decryptLocation = Base64Utils.decrypt(encryptLocation, Long.valueOf(time));
                if(!TextUtils.isEmpty(decryptLocation)) jar.put(new JSONObject(decryptLocation));
            }
        } catch (Throwable e) {
        }
        return jar;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null) return;
            db.delete(DBConfig.Location.TABLE_NAME, DBConfig.Location.Column.ST + "=?", new String[]{"1"});
        } catch (Throwable e) {
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

}
