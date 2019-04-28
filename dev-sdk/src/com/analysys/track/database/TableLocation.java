package com.analysys.track.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.Base64Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class TableLocation {

    Context mContext;
    String INSERT_STATUS_DEFAULT = "0";
    String INSERT_STATUS_READ_OVER = "1";

    private TableLocation(){}
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
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_LOCATION)){
//                return;
//            }
            ELOG.i("insert","================="+System.currentTimeMillis());
            ELOG.i("insert","================="+locationInfo);
            ContentValues cv = null;
            String locationTime = null;
            long time = -1;
            String encryptLocation = null;
            if (locationInfo != null && locationInfo.length() > 0) {
                locationTime = null;
                locationTime = locationInfo.optString(DeviceKeyContacts.LocationInfo.CollectionTime);
                time = 0;
                if(!TextUtils.isEmpty(locationTime)){
                   time = Long.parseLong(locationTime);
                }
                encryptLocation = Base64Utils.encrypt(String.valueOf(locationInfo), time);
                if (!TextUtils.isEmpty(encryptLocation)) {
                    cv = new ContentValues();
                    cv.put(DBConfig.Location.Column.LI, EncryptUtils.encrypt(mContext,encryptLocation));
                    cv.put(DBConfig.Location.Column.IT, locationTime);
                    cv.put(DBConfig.Location.Column.ST, INSERT_STATUS_DEFAULT);
                    SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
                    if(db == null){
                        return;
                    }
                    db.insert(DBConfig.Location.TABLE_NAME, null, cv);
                }
            }
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e.getMessage());
            }
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
            String id = "",encryptLocation = "",time = "";
            long timeStamp = 0;
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return array;
                }
                id = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.ID));
                encryptLocation = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.LI));
                time = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.IT));
                ContentValues cv = new ContentValues();
                cv.put(DBConfig.Location.Column.ST, INSERT_STATUS_READ_OVER);
                db.update(DBConfig.Location.TABLE_NAME, cv, DBConfig.Location.Column.ID + "=?", new String[]{id});
                if(!TextUtils.isEmpty(time)){
                    timeStamp = Long.parseLong(time);
                }
                String decryptLocation = Base64Utils.decrypt(EncryptUtils.decrypt(mContext,encryptLocation), timeStamp);
                if(!TextUtils.isEmpty(decryptLocation)){
                    array.put(new JSONObject(decryptLocation));
                } else {
                    blankCount += 1;
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e.getMessage());
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
            if(db != null && db.inTransaction()){
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
//            String sql = "delete from " + DBConfig.Location.TABLE_NAME + " where "+DBConfig.Location.Column.ID +
//                    "!=(select max(" + DBConfig.Location.Column.ID + ") from " + DBConfig.Location.TABLE_NAME + "" +
//                    " where " + DBConfig.Location.Column.ST + "=1) and " + DBConfig.Location.Column.ST + "=1";
//            ELOG.i("sql ::::::::  "+sql);
//            db.execSQL(sql);
            db.delete(DBConfig.Location.TABLE_NAME, DBConfig.Location.Column.ST + "=?", new String[]{INSERT_STATUS_READ_OVER});
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e.getMessage());
            }
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

}
