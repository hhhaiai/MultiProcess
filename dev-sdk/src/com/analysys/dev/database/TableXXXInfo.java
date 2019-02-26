package com.analysys.dev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.impl.PolicyImpl;
import com.analysys.dev.internal.impl.proc.ProcParser;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TableXXXInfo {
    Context mContext;
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
    public void insert(JSONArray xxxInfo) {
        SQLiteDatabase db = null;
        try {
             db = DBManager.getInstance(mContext).openDB();
            if (xxxInfo == null) {
                return;
            }

            db.beginTransaction();
            List<ContentValues> listCv = getContentValues(xxxInfo);
            for (ContentValues cv:listCv) {
//                ELOG.i("每一个cv::::::  "+cv);
                List<ContentValues> procCV = TablePROC.getInstance(mContext).getContentValues(cv.get(DBConfig.XXXInfo.Column.TIME).toString(), new JSONArray(cv.get(DBConfig.XXXInfo.Column.PROC).toString()));
                cv.put(DBConfig.XXXInfo.Column.PROC, "00000000");
                db.insert(DBConfig.XXXInfo.TABLE_NAME, null, cv);

                ContentValues contentValues;
//                ELOG.i("procCV.size()::::::  "+procCV.size());
                for(int i = 0;i<procCV.size();i++){
//                    contentValues = new ContentValues();
                    contentValues = procCV.get(i);
//                    ELOG.i("每一个子cv::::::  "+contentValues);
                    db.insert(DBConfig.PROCInfo.TABLE_NAME,null,contentValues);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            ELOG.e(e+"  insert XXX");
        }finally {
            db.endTransaction();
        }
        DBManager.getInstance(mContext).closeDB();
    }
    /**
     * json数据转成ContentValues
     */
    private List<ContentValues> getContentValues(JSONArray xxxInfo) {
        List<ContentValues> list = null;
        ContentValues cv = null;
        JSONObject object = new JSONObject();
        try{
            if (xxxInfo != null) {
                list = new ArrayList<>();
                for(int i = 0; i< xxxInfo.length();i++){
                    object = (JSONObject) xxxInfo.get(i);
                    cv = new ContentValues();
//                    ELOG.i(xxxInfo.toString()+"     xxxInfo  ");
                    cv.put(DBConfig.XXXInfo.Column.TIME, object.get("time").toString());
                    cv.put(DBConfig.XXXInfo.Column.TOP, object.get("top").toString());
                    cv.put(DBConfig.XXXInfo.Column.PS, object.get("ps").toString());
                    cv.put(DBConfig.XXXInfo.Column.PROC, object.get("proc").toString());
                    cv.put(DBConfig.XXXInfo.Column.RESULT, object.get(ProcParser.RUNNING_RESULT).toString());
                    list.add(cv);
                }

            }
        }catch (Throwable t){
        }
        return list;
    }
    //连表查询
    public JSONArray select(){
        JSONArray procArray, array;
        Cursor cursor = null,curProc = null;
        try {
            procArray = new JSONArray();
            array = new JSONArray();
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            cursor = db.query(DBConfig.XXXInfo.TABLE_NAME,
                    null, null, null,
                    null, null, null);
            JSONObject jsonObject = null;
            while (cursor.moveToNext()) {
                jsonObject = new JSONObject();
                String time = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TIME));
                jsonObject.put(ProcParser.RUNNING_TIME,time);
                if(PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.TOP_SWITCH,EGContext.SWITCH_OF_TOP))
                jsonObject.put(ProcParser.RUNNING_TOP,new String(Base64.encode(cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TOP)).getBytes(),Base64.DEFAULT)));
                if(PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.PS_SWITCH,EGContext.SWITCH_OF_PS))
                jsonObject.put(ProcParser.RUNNING_PS,new String(Base64.encode(cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.PS)).getBytes(),Base64.DEFAULT)));
//                curProc1 = db.query(DBConfig.PROCInfo.TABLE_NAME,null,null,null,null,null,null);
//                ELOG.i(curProc1+"    :::::::::::curProc1   "+ curProc1.getCount());
                curProc = db.query(DBConfig.PROCInfo.TABLE_NAME, new String[] {DBConfig.PROCInfo.Column.CONTENT},
                        DBConfig.PROCInfo.Column.PARENT_ID_TIME + "=?", new String[] {time}, null, null, null);
                while (curProc.moveToNext()) {
//                    ELOG.i("content :::::::::::::::::::::     "+curProc.getColumnIndex(DBConfig.PROCInfo.Column.CONTENT));
                    procArray.put(new JSONObject(curProc.getString(curProc.getColumnIndex(DBConfig.PROCInfo.Column.CONTENT))));
                }
                if(PolicyImpl.getInstance(mContext).getValueFromSp(EGContext.PROC_SWITCH,EGContext.SWITCH_OF_PROC))
                jsonObject.put(ProcParser.RUNNING_PROC,new String(Base64.encode(procArray.toString().getBytes(),Base64.DEFAULT)));
                jsonObject.put(ProcParser.RUNNING_RESULT,cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.RESULT)));
                array.put(jsonObject);
            }
        } catch (Exception e) {
            ELOG.e(e.getMessage()+"  TableXXXInfo select() has an exception... ");
            array = null;
        }finally {
            if(curProc != null) curProc.close();
            if(cursor != null) cursor.close();
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }
    public void delete() {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if(db == null) return;
            db.beginTransaction();
            db.delete(DBConfig.XXXInfo.TABLE_NAME, null, null);
            db.delete(DBConfig.PROCInfo.TABLE_NAME,null,null);
            db.setTransactionSuccessful();
        } catch (Throwable e) {
        }finally {
            db.endTransaction();
            DBManager.getInstance(mContext).closeDB();
        }
    }
}

