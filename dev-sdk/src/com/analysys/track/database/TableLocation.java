package com.analysys.track.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.Base64Utils;

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
            //TODO
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_LOCATION)){
//                return;
//            }
            if (!TextUtils.isEmpty(locationInfo.toString())) {
                long time = Long.parseLong(locationInfo.optString(DeviceKeyContacts.LocationInfo.CollectionTime));
                String encryptLocation = Base64Utils.encrypt(locationInfo.toString(), time);
                if (!TextUtils.isEmpty(encryptLocation)) {
                    ContentValues cv = new ContentValues();
                    cv.put(DBConfig.Location.Column.LI, encryptLocation);
                    cv.put(DBConfig.Location.Column.IT, String.valueOf(time));
                    cv.put(DBConfig.Location.Column.ST, "0");
                    SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
                    if(db == null){
                        return;
                    }
                    db.insert(DBConfig.Location.TABLE_NAME, null, cv);
                }
            }
        } catch (Throwable e) {
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public JSONArray select() {
        JSONArray array = null;
        int blankCount = 0;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            array = new JSONArray();
            db = DBManager.getInstance(mContext).openDB();
            if(db == null){
               return array;
            }
            db.beginTransaction();
            cursor = db.query(DBConfig.Location.TABLE_NAME, null,
                    null, null, null, null, null);
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return array;
                }
                String id = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.ID));
                String encryptLocation = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.LI));
                String time = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.IT));
                ContentValues cv = new ContentValues();
                cv.put(DBConfig.Location.Column.ST, "1");
                db.update(DBConfig.Location.TABLE_NAME, cv, DBConfig.Location.Column.ID + "=?", new String[]{id});
                String decryptLocation = Base64Utils.decrypt(encryptLocation, Long.valueOf(time));
                if(!TextUtils.isEmpty(decryptLocation)){
                    array.put(new JSONObject(decryptLocation));
                } else {
                    blankCount += 1;
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
        }finally {
            if(cursor != null){
                cursor.close();
            }
            if(db != null){
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null) {
                return;
            }
            db.delete(DBConfig.Location.TABLE_NAME, DBConfig.Location.Column.ST + "=?", new String[]{"1"});
        } catch (Throwable e) {
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

}
