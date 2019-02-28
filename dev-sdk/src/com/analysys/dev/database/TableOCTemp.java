package com.analysys.dev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.utils.Base64Utils;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableOCTemp {
    Context mContext;

    private static class Holder {
        private static final TableOCTemp INSTANCE = new TableOCTemp();
    }

    public static TableOCTemp getInstance(Context context) {
        if (TableOCTemp.Holder.INSTANCE.mContext == null) {
            TableOCTemp.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return TableOCTemp.Holder.INSTANCE;
    }
    public synchronized Map<String, String> queryProcTemp() {
        Map<String, String> result = new HashMap<String, String>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return null;
            }
            cursor = db.query(DBConfig.OCTemp.TABLE_NAME, null,
                    null, null,
                    null, null, null);
            while (cursor.moveToNext()) {
                try {
                    String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OCTemp.Column.IT));
                    String encryptAn = cursor.getString(cursor.getColumnIndex(DBConfig.OCTemp.Column.APN));
                    String packageName = Base64Utils.decrypt(encryptAn, Long.valueOf(insertTime));
                    String openTime = cursor.getString(cursor.getColumnIndexOrThrow(DBConfig.OCTemp.Column.AOT));
                    result.put(packageName, openTime);
                } catch (Throwable e) {
                }
            }

        }catch (Throwable e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return result;
    }


    /**
     * 存储数据
     */
    public void insert(JSONArray ocInfo) {
        try {
            if (ocInfo == null) {
                return;
            }
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            JSONObject obj = new JSONObject();
            for(int i = 0;i <ocInfo.length();i++){
                obj = (JSONObject)ocInfo.get(i);
                ContentValues cv = getContentValues(obj);
//            ELOG.i(cv+"     ：：：：ocInfo  "+DBConfig.OCCount.Column.CU);
                cv.put(DBConfig.OCCount.Column.CU, 0);
                db.insert(DBConfig.OCCount.TABLE_NAME, null, cv);
            }

        } catch (Exception e) {
            ELOG.e(e.getMessage()+" ::::::insert()");
        }
        DBManager.getInstance(mContext).closeDB();
    }
    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValues(JSONObject ocInfo) {
        ContentValues cv = null;
        try {
            if (ocInfo != null) {
                cv = new ContentValues();
                long insertTime = System.currentTimeMillis();
                String an = Base64Utils.encrypt(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName), insertTime);
                cv.put(DBConfig.OCCount.Column.APN, an);
                cv.put(DBConfig.OCCount.Column.AOT, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime));
                cv.put(DBConfig.OCCount.Column.IT, insertTime);
            }
        }catch (Throwable t){
            ELOG.e(t.getMessage()+"   ::::getContentValues");
        }
        return cv;
    }
    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null) return;
            db.delete(DBConfig.OCTemp.TABLE_NAME, null, null);
        } catch (Throwable e) {
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }



}
