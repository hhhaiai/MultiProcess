package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.internal.impl.oc.ProcUtils;
import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class TableXXXInfo {
    Context mContext;

    private TableXXXInfo() {
    }

    public static TableXXXInfo getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * 存储数据
     */
    public void insert(JSONObject xxxInfo) {
        SQLiteDatabase db = null;
        try {
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_OC)){
//                return;
//            }
            db = DBManager.getInstance(mContext).openDB();
            if (db == null || xxxInfo == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = getContentValues(xxxInfo);
            db.insert(DBConfig.XXXInfo.TABLE_NAME, null, cv);

        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }

    }

    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValues(JSONObject xxxInfo) {
        ContentValues cv = null;
        String result = null;
        try {
            if (xxxInfo != null) {
                result = String.valueOf(xxxInfo.opt(ProcUtils.RUNNING_RESULT));
                if (!TextUtils.isEmpty(result) && !"null".equalsIgnoreCase(result)) {
                    cv = new ContentValues();
                    cv.put(DBConfig.XXXInfo.Column.TIME,
                            EncryptUtils.encrypt(mContext, String.valueOf(xxxInfo.opt(ProcUtils.RUNNING_TIME))));
//                    cv.put(DBConfig.XXXInfo.Column.TOP, EncryptUtils.encrypt(mContext,String.valueOf(object.opt(ProcParser.RUNNING_TOP))));
//                    cv.put(DBConfig.XXXInfo.Column.PS, EncryptUtils.encrypt(mContext,String.valueOf(object.opt(ProcParser.RUNNING_PS))));
                    // PROC
                    cv.put(DBConfig.XXXInfo.Column.PROC, EncryptUtils.encrypt(mContext, result));
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
        return cv;
    }

    // 连表查询
//    public JSONArray select(){
//        JSONArray  array = null;
//        Cursor cursor = null;
//        int blankCount = 0;
//        try {
//            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
//            if (db == null){
//                return array;
//            }
//            if(!db.isOpen()){
//                db = DBManager.getInstance(mContext).openDB();
//            }
//            array = new JSONArray();
//            cursor = db.query(DBConfig.XXXInfo.TABLE_NAME,
//                    null, null, null,
//                    null, null, null);
//            JSONObject jsonObject = null;
//            String proc = null;
//            while (cursor.moveToNext()) {
//                if(blankCount >= EGContext.BLANK_COUNT_MAX){
//                    return array;
//                }
//                jsonObject = new JSONObject();
//                String id = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.ID));
//                if(TextUtils.isEmpty(id)){
//                    blankCount += 1;
//                }
//                proc = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.PROC)));
//                if(!TextUtils.isEmpty(proc) && !"null".equalsIgnoreCase(proc)){
//                    JsonUtils.pushToJSON(mContext,jsonObject,ProcUtils.RUNNING_RESULT,new JSONArray(proc),DataController.SWITCH_OF_CL_MODULE_PROC);
//                    if(jsonObject == null || jsonObject.length() < 1){
//                        return array;
//                    }else {
//                        String time = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TIME)));
//                        JsonUtils.pushToJSON(mContext,jsonObject,ProcUtils.RUNNING_TIME,time,DataController.SWITCH_OF_RUNNING_TIME);
//                        jsonObject.put(ProcUtils.ID,id);
//                    }
//                }else {
//                    return array;
//                }
//                array.put(new String(Base64.encode(String.valueOf(jsonObject).getBytes(),Base64.DEFAULT)));
//            }
//        } catch (Throwable e) {
//            if(EGContext.FLAG_DEBUG_INNER) {
//                ELOG.e(e);
//            }
//            array = null;
//        }finally {
//            if(cursor != null){
//                cursor.close();
//            }
//            DBManager.getInstance(mContext).closeDB();
//        }
//        return array;
//    }
    public void delete() {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.XXXInfo.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 按时间删除
     *
     * @param idList
     */
    public void deleteByID(List<String> idList) {
        SQLiteDatabase db = null;
        try {
            if (idList == null || idList.size() < 1) {
                return;
            }
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            String id = "";
//            ELOG.e("deleteByID ::: "+idList.size());
            for (int i = 0; i < idList.size(); i++) {
                id = idList.get(i);
                if (TextUtils.isEmpty(id)) {
                    return;
                }
                db.delete(DBConfig.XXXInfo.TABLE_NAME, DBConfig.XXXInfo.Column.ID + "=?", new String[] { id });
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    // 连表查询
    public JSONArray select(long maxLength) {
        JSONArray array = null;
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return array;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            array = new JSONArray();
            cursor = db.query(DBConfig.XXXInfo.TABLE_NAME, null, null, null, null, null, null, "2000");
            JSONObject jsonObject = null;
            String proc = null;
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return array;
                }
                jsonObject = new JSONObject();
                String id = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.ID));
                if (TextUtils.isEmpty(id)) {
                    blankCount += 1;
                }
                proc = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.PROC)));
                if (!TextUtils.isEmpty(proc) && !"null".equalsIgnoreCase(proc)) {
                    JsonUtils.pushToJSON(mContext, jsonObject, ProcUtils.RUNNING_RESULT, new JSONArray(proc),
                            DataController.SWITCH_OF_CL_MODULE_PROC);
                    if (jsonObject == null || jsonObject.length() < 1) {
                        return array;
                    } else {
                        String time = EncryptUtils.decrypt(mContext,
                                cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TIME)));
                        JsonUtils.pushToJSON(mContext, jsonObject, ProcUtils.RUNNING_TIME, time,
                                DataController.SWITCH_OF_RUNNING_TIME);
                        if (countNum / 300 > 0) {
                            countNum = countNum % 300;
                            long size = String.valueOf(array).getBytes().length;
                            if (size >= maxLength * 9 / 10) {
//                                ELOG.e(" size值：："+size+" maxLength = "+maxLength);
                                UploadImpl.isChunkUpload = true;
                                break;
                            } else {
                                UploadImpl.idList.add(id);
                                array.put(new String(
                                        Base64.encode(String.valueOf(jsonObject).getBytes(), Base64.DEFAULT)));
                            }
                        } else {
                            UploadImpl.idList.add(id);
                            array.put(new String(Base64.encode(String.valueOf(jsonObject).getBytes(), Base64.DEFAULT)));
                        }

                    }
                } else {
                    return array;
                }

            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
            array = null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    private static class Holder {
        private static final TableXXXInfo INSTANCE = new TableXXXInfo();
    }
}
