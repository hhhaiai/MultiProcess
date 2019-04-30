package com.analysys.track.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.impl.proc.ProcUtils;
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
    private TableXXXInfo(){}
    private static class Holder {
        private static final TableXXXInfo INSTANCE = new TableXXXInfo();
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
            if (db == null || xxxInfo == null){
                return;
            }
            ContentValues cv = getContentValues(xxxInfo);
            db.insert(DBConfig.XXXInfo.TABLE_NAME, null, cv);

        } catch (Throwable e) {
            ELOG.e(e.getMessage()+"  insert XXX");
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }

    }
    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValues(JSONObject xxxInfo) {
        ContentValues cv = null;
        String result = null;
        try{
            if (xxxInfo != null) {
                result = String.valueOf(xxxInfo.opt(ProcUtils.RUNNING_RESULT));
                if(!TextUtils.isEmpty(result) && !"null".equalsIgnoreCase(result) ){
                    cv = new ContentValues();
                    cv.put(DBConfig.XXXInfo.Column.TIME, EncryptUtils.encrypt(mContext,String.valueOf(xxxInfo.opt(ProcUtils.RUNNING_TIME))));
//                    cv.put(DBConfig.XXXInfo.Column.TOP, EncryptUtils.encrypt(mContext,String.valueOf(object.opt(ProcParser.RUNNING_TOP))));
//                    cv.put(DBConfig.XXXInfo.Column.PS, EncryptUtils.encrypt(mContext,String.valueOf(object.opt(ProcParser.RUNNING_PS))));
                    //PROC
                    cv.put(DBConfig.XXXInfo.Column.PROC, EncryptUtils.encrypt(mContext,result));
                }
            }
        }catch (Throwable t){
            ELOG.e(t.getMessage()+  "getContentValues() ...");
        }
        return cv;
    }
    //连表查询
    public JSONArray select(){
        JSONArray  array = null;
        Cursor cursor = null;
        int blankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null){
                return array;
            }
            array = new JSONArray();
            cursor = db.query(DBConfig.XXXInfo.TABLE_NAME,
                    null, null, null,
                    null, null, null);
            JSONObject jsonObject = null;
            String proc = null;
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return array;
                }
                jsonObject = new JSONObject();
                String time = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TIME)));
                if(TextUtils.isEmpty(time)){
                    blankCount += 1;
                }
                JsonUtils.pushToJSON(mContext,jsonObject,ProcUtils.RUNNING_TIME,time,DataController.SWITCH_OF_RUNNING_TIME);
                proc = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.PROC)));
                if(!TextUtils.isEmpty(proc) && !"null".equalsIgnoreCase(proc)){
                    JsonUtils.pushToJSON(mContext,jsonObject,ProcUtils.RUNNING_RESULT,new JSONArray(proc),DataController.SWITCH_OF_CL_MODULE_PROC);
                }
                if(jsonObject == null || jsonObject.length() < 1){
                    return array;
                }
                array.put(new String(Base64.encode(String.valueOf(jsonObject).getBytes(),Base64.DEFAULT)));
//                array.put(jsonObject);
//                ELOG.i("array :::::::::" +array);
            }
        } catch (Throwable e) {
            ELOG.e(e+"  TableXXXInfo select() has an exception... ");
            array = null;
        }finally {
            if(cursor != null){
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }
    public void delete() {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return;
            }
            db.delete(DBConfig.XXXInfo.TABLE_NAME, null, null);
        } catch (Throwable e) {
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 按时间删除
     * @param timeList
     */
    public void deleteByTime(List<String> timeList) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null){
                return;
            }
            String time = "";
            for(int i = 0;i < timeList.size();i++){
                time = timeList.get(i);
                if(TextUtils.isEmpty(time)){
                    return;
                }
                db.delete(DBConfig.XXXInfo.TABLE_NAME, DBConfig.XXXInfo.Column.TIME + "=?", new String[] {EncryptUtils.encrypt(mContext,time)});
            }
        } catch (Throwable e) {
            ELOG.e(e.getMessage()+"  :::::deleteByTime ");
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
}

