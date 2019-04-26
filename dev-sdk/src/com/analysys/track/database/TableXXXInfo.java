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

import java.util.ArrayList;
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
//            db.beginTransaction();
            ContentValues cv = getContentValues(xxxInfo);
//            ContentValues contentValues;
//            for (ContentValues cv:listCv) {
//                ELOG.i("每一个cv::::::  "+cv);
//                List<ContentValues> procCV = TablePROC.getInstance(mContext).getContentValues(cv.get(DBConfig.XXXInfo.Column.TIME).toString(), new JSONArray(EncryptUtils.decrypt(mContext,cv.get(DBConfig.XXXInfo.Column.PROC).toString())));
//                cv.put(DBConfig.XXXInfo.Column.PROC, EncryptUtils.encrypt(mContext,"0"));
            db.insert(DBConfig.XXXInfo.TABLE_NAME, null, cv);

//                for(int i = 0;i<procCV.size();i++){
//                    contentValues = procCV.get(i);
////                    ELOG.i("每一个子cv::::::  "+contentValues);
//                    db.insert(DBConfig.PROCInfo.TABLE_NAME,null,contentValues);
//                }
//            }
//            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e.getMessage()+"  insert XXX");
        }finally {
//            if(db != null){
//                db.endTransaction();
//            }
            DBManager.getInstance(mContext).closeDB();
        }

    }
    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValues(JSONObject xxxInfo) {
        ContentValues cv = null;
//        JSONObject object = new JSONObject();
        try{
            if (xxxInfo != null) {
//                list = new ArrayList<ContentValues>();
//                for(int i = 0; i< xxxInfo.length();i++){
//                    object = (JSONObject) xxxInfo.get(i);
                    cv = new ContentValues();
//                    ELOG.i(xxxInfo.toString()+"     xxxInfo  ");
                    cv.put(DBConfig.XXXInfo.Column.TIME, EncryptUtils.encrypt(mContext,String.valueOf(xxxInfo.opt(ProcUtils.RUNNING_TIME))));
//                    cv.put(DBConfig.XXXInfo.Column.TOP, EncryptUtils.encrypt(mContext,String.valueOf(object.opt(ProcParser.RUNNING_TOP))));
//                    cv.put(DBConfig.XXXInfo.Column.PS, EncryptUtils.encrypt(mContext,String.valueOf(object.opt(ProcParser.RUNNING_PS))));
                    //PROC
                    cv.put(DBConfig.XXXInfo.Column.PROC, EncryptUtils.encrypt(mContext,String.valueOf(xxxInfo.opt(ProcUtils.RUNNING_RESULT))));
                   //OCR
//                    cv.put(DBConfig.XXXInfo.Column.RESULT, EncryptUtils.encrypt(mContext,String.valueOf(xxxInfo.opt(ProcUtils.RUNNING_OC_RESULT))));
//                }
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
//        int subBlankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null){
                return array;
            }
//            procArray = new JSONArray();
            array = new JSONArray();
            cursor = db.query(DBConfig.XXXInfo.TABLE_NAME,
                    null, null, null,
                    null, null, null);
            JSONObject jsonObject = null;
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
                JsonUtils.pushToJSON(mContext,jsonObject,ProcUtils.RUNNING_RESULT,new JSONObject(EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.PROC)))),DataController.SWITCH_OF_CL_MODULE_PROC);
//                JsonUtils.pushToJSON(mContext,jsonObject,ProcUtils.RUNNING_OC_RESULT,EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.RESULT))),DataController.SWITCH_OF_CL_MODULE_RESULT);

//                curProc = db.query(DBConfig.PROCInfo.TABLE_NAME, new String[] {DBConfig.PROCInfo.Column.CONTENT},
//                        DBConfig.PROCInfo.Column.PARENT_ID_TIME + "=?", new String[] {EncryptUtils.encrypt(mContext,time)}, null, null, null);
//                while (curProc.moveToNext()) {
//                    if(subBlankCount >= EGContext.BLANK_COUNT_MAX){
//                        return array;
//                    }
//                    String content = EncryptUtils.decrypt(mContext,curProc.getString(curProc.getColumnIndex(DBConfig.PROCInfo.Column.CONTENT)));
//                    if(!TextUtils.isEmpty(content)){
//                        procArray.put(new JSONObject(new String(Base64.decode(content.getBytes(),Base64.DEFAULT))));
//                    }else {
//                        subBlankCount += 1;
//                    }
//
//                }
//                JsonUtils.pushToJSON(mContext,jsonObject,ProcParser.RUNNING_PROC,procArray,DataController.SWITCH_OF_CL_MODULE_PROC);
//                JsonUtils.pushToJSON(mContext,jsonObject,ProcParser.RUNNING_RESULT,EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.RESULT))),DataController.SWITCH_OF_CL_MODULE_RESULT);
                array.put(new String(Base64.encode(String.valueOf(jsonObject).getBytes(),Base64.DEFAULT)));
//                array.put(jsonObject);
//                ELOG.i("array :::::::::" +array);
            }
        } catch (Exception e) {
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
            db.beginTransaction();
            db.delete(DBConfig.XXXInfo.TABLE_NAME, null, null);
//            db.delete(DBConfig.PROCInfo.TABLE_NAME,null,null);
            db.setTransactionSuccessful();
        } catch (Throwable e) {
        }finally {
            if(db != null && db.inTransaction()){
                db.endTransaction();
            }
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
//            db.beginTransaction();
            String time = "";
            for(int i = 0;i < timeList.size();i++){
                time = timeList.get(i);
                if(TextUtils.isEmpty(time)){
                    return;
                }
                db.delete(DBConfig.XXXInfo.TABLE_NAME, DBConfig.XXXInfo.Column.TIME + "=?", new String[] {EncryptUtils.encrypt(mContext,time)});
//                db.delete(DBConfig.PROCInfo.TABLE_NAME, DBConfig.PROCInfo.Column.PARENT_ID_TIME + "=?", new String[] {EncryptUtils.encrypt(mContext,time)});
            }
//            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e.getMessage()+"  :::::deleteByTime ");
        } finally {
//            if(db != null){
//                db.endTransaction();
//            }
            DBManager.getInstance(mContext).closeDB();
        }
    }
}

