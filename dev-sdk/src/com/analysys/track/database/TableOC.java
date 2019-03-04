//package com.analysys.dev.database;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.text.TextUtils;
//
//import DeviceKeyContacts;
//import Base64Utils;
//import ELOG;
//import EContextHelper;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.util.List;
//
//public class TableOC {
//    Context mContext;
//
//    private static class Holder {
//        private static final TableOC INSTANCE = new TableOC();
//    }
//
//    public static TableOC getInstance(Context context) {
//        if (Holder.INSTANCE.mContext == null) {
//            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
//        }
//        return Holder.INSTANCE;
//    }
//
////    public void insert(List<JSONObject> ocInfo) {
////
////        SQLiteDatabase db = null;
////        try {
////            db = DBManager.getInstance(mContext).openDB();
////            db.beginTransaction();
////            long time = System.currentTimeMillis();
////            JSONObject obj;
////            for (int i = 0; i < ocInfo.size(); i++) {
//////                ELOG.i("OC存储内容：" + ocInfo);
////                obj = ocInfo.get(i);
////                String encryptOC = Base64Utils.encrypt(obj.get(DeviceKeyContacts.OCInfo.ApplicationName).toString(), time);
////                if (!TextUtils.isEmpty(encryptOC)) {
////                    ContentValues cv = new ContentValues();
////                    cv.put(DBConfig.OC.Column.OCI, encryptOC);
////                    cv.put(DBConfig.OC.Column.IT, time);
////                    cv.put(DBConfig.OC.Column.ST, 0);
////                    db.insert(DBConfig.OC.TABLE_NAME, null, cv);
////                }
////            }
////            db.setTransactionSuccessful();
////        } catch (Throwable e) {
////            ELOG.e(e);
////        } finally {
////            db.endTransaction();
////        }
////        DBManager.getInstance(mContext).closeDB();
////    }
//
//    public JSONArray select() {
//        JSONArray jar = null;
//        Cursor cursor = null;
//        try {
//            jar = new JSONArray();
//            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
//            cursor = db.query(DBConfig.OC.TABLE_NAME,
//                    new String[]{DBConfig.OC.Column.OCI, DBConfig.OC.Column.IT},
//                    null, null, null,
//                    null, null, null);
//            while (cursor.moveToNext()) {
//                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT));
//                String encryptInfo = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.OCI));
//                String ocInfo = Base64Utils.decrypt(encryptInfo, Long.valueOf(insertTime));
//                if (!TextUtils.isEmpty(ocInfo)) {
//                    jar.put(new JSONObject(ocInfo));
//                }
//            }
//        } catch (Throwable e) {
//            ELOG.e(e);
//        }finally {
//            if(cursor != null) cursor.close();
//        }
//        DBManager.getInstance(mContext).closeDB();
//        return jar;
//    }
//    public void delete() {
//        try {
//            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
//            if(db == null) return;
//            db.delete(DBConfig.OC.TABLE_NAME, DBConfig.OC.Column.ST + "=?", new String[]{"1"});
//        } catch (Throwable e) {
//        }finally {
//            DBManager.getInstance(mContext).closeDB();
//        }
//    }
//}
