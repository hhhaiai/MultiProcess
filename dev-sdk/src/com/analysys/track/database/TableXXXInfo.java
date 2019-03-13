package com.analysys.track.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.impl.PolicyImpl;
import com.analysys.track.internal.impl.proc.ProcParser;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.Utils;
import com.analysys.track.utils.reflectinon.EContextHelper;

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
            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_LOCATION)){
                return;
            }
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
            DBManager.getInstance(mContext).closeDB();
        }

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
                list = new ArrayList<ContentValues>();
                for(int i = 0; i< xxxInfo.length();i++){
                    object = (JSONObject) xxxInfo.get(i);
                    cv = new ContentValues();
//                    ELOG.i(xxxInfo.toString()+"     xxxInfo  ");
                    cv.put(DBConfig.XXXInfo.Column.TIME, object.opt(ProcParser.RUNNING_TIME).toString());
                    cv.put(DBConfig.XXXInfo.Column.TOP, new String(Base64.encode(object.opt(ProcParser.RUNNING_TOP).toString().getBytes(),Base64.DEFAULT)));
                    cv.put(DBConfig.XXXInfo.Column.PS, new String(Base64.encode(object.opt(ProcParser.RUNNING_PS).toString().getBytes(),Base64.DEFAULT)));
                    cv.put(DBConfig.XXXInfo.Column.PROC, new String(Base64.encode(object.opt(ProcParser.RUNNING_PROC).toString().getBytes(),Base64.DEFAULT)));
                    cv.put(DBConfig.XXXInfo.Column.RESULT, new String(Base64.encode(object.opt(ProcParser.RUNNING_RESULT).toString().getBytes(),Base64.DEFAULT)));
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
        int blankCount = 0;
        try {
            procArray = new JSONArray();
            array = new JSONArray();
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            cursor = db.query(DBConfig.XXXInfo.TABLE_NAME,
                    null, null, null,
                    null, null, null);
            JSONObject jsonObject = null;
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return array;
                }
                jsonObject = new JSONObject();
                String time = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TIME));
                if(TextUtils.isEmpty(time)){
                    blankCount++;
                }
                Utils.pushToJSON(mContext,jsonObject,ProcParser.RUNNING_TIME,time,DataController.SWITCH_OF_RUNNING_TIME);
                Utils.pushToJSON(mContext,jsonObject,ProcParser.RUNNING_TOP,new String(Base64.decode(cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TOP)).getBytes(),Base64.DEFAULT)),DataController.SWITCH_OF_CL_MODULE_TOP);
                Utils.pushToJSON(mContext,jsonObject,ProcParser.RUNNING_PS,new String(Base64.decode(cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.PS)).getBytes(),Base64.DEFAULT)),DataController.SWITCH_OF_CL_MODULE_PS);

                curProc = db.query(DBConfig.PROCInfo.TABLE_NAME, new String[] {DBConfig.PROCInfo.Column.CONTENT},
                        DBConfig.PROCInfo.Column.PARENT_ID_TIME + "=?", new String[] {time}, null, null, null);
                while (curProc.moveToNext()) {
//                    ELOG.i("content :::::::::::::::::::::     "+curProc.getColumnIndex(DBConfig.PROCInfo.Column.CONTENT));
                    procArray.put(new JSONObject(curProc.getString(curProc.getColumnIndex(DBConfig.PROCInfo.Column.CONTENT))));
                }
                Utils.pushToJSON(mContext,jsonObject,ProcParser.RUNNING_PROC,new String(Base64.decode(procArray.toString().getBytes(),Base64.DEFAULT)),DataController.SWITCH_OF_CL_MODULE_PROC);
                Utils.pushToJSON(mContext,jsonObject,ProcParser.RUNNING_RESULT,new String(Base64.decode(cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.RESULT)).getBytes(),Base64.DEFAULT)),DataController.SWITCH_OF_CL_MODULE_RESULT);
                array.put(Base64.encode(jsonObject.toString().getBytes(),Base64.DEFAULT));
            }
        } catch (Exception e) {
            ELOG.e(e.getMessage()+"  TableXXXInfo select() has an exception... ");
            array = null;
        }finally {
            if(curProc != null){
                curProc.close();
            }
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

    /**
     * 按时间删除
     * @param timeList
     */
    public void deleteByTime(List<String> timeList) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null)
                return;
            db.beginTransaction();
            for(int i = 0;i < timeList.size();i++){
                db.delete(DBConfig.XXXInfo.TABLE_NAME, DBConfig.XXXInfo.Column.TIME + "=?", new String[] {timeList.get(i)});
                db.delete(DBConfig.PROCInfo.TABLE_NAME, DBConfig.PROCInfo.Column.PARENT_ID_TIME + "=?", new String[] {timeList.get(i)});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
        } finally {
            if(db != null){
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
    }
}

