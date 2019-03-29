package com.analysys.track.database;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.Base64Utils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class TableOC {

    private final String ZERO = "0";
    private final String ONE = "1";
    Context mContext;

    private static class Holder {
        private static final TableOC INSTANCE = new TableOC();
    }

    public static TableOC getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * 存储数据
     */
    public void insert(JSONObject ocInfo) {
        try {
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_OC) || ocInfo == null){
//                return;
//            }
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null || ocInfo == null || ocInfo.length()<1){
                return;
            }
            ContentValues cv = getContentValues(ocInfo);
            cv.put(DBConfig.OC.Column.CU, 1);//可有可无，暂时赋值
            db.insert(DBConfig.OC.TABLE_NAME, null, cv);
        } catch (Exception e) {
            ELOG.e(e.getMessage() + " ::::::insert()");
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * @param ocInfo
     */
    public void insertArray(JSONArray ocInfo) {
        SQLiteDatabase db = null;
        try {
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_OC) || ocInfo == null){
//                return;
//            }
            db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return;
            }
            if (ocInfo != null && ocInfo.length() > 0) {
                db.beginTransaction();
                JSONObject obj = null;
                ContentValues cv = null;
                ELOG.i(ocInfo.length() + "     ：：：：ocInfo size  ");
                for (int i = 0; i < ocInfo.length(); i++) {
                    obj = (JSONObject)ocInfo.get(i);
                    if (obj == null){//为空则过滤
                        continue;
                    }
                    if (TextUtils.isEmpty(obj.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime))) {//一条新打开的app信息,执行插入操作
                        cv = getContentValues(obj);
                        // ELOG.i(cv+" ：：：：ocInfo "+DBConfig.OC.Column.CU);
                        cv.put(DBConfig.OC.Column.CU, 1);
                        db.insert(DBConfig.OC.TABLE_NAME, null, cv);
                    } else {//包含closeTime,修改个别字段
                        cv = getContentValuesForUpdate(obj);
                        db.update(DBConfig.OC.TABLE_NAME, cv, DBConfig.OC.Column.APN + "=?",
                            new String[] {obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName)});
                    }
                }
                db.setTransactionSuccessful();
            }
        } catch (Throwable e) {
            ELOG.e(e + "  :::::insertArray() has an exception");
        } finally {
            if(db != null){
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 该时段有应用操作记录，更新记录状态做缓存
     */
    public void update(JSONObject ocInfo) {
        try {
            if (ocInfo == null) {
                return;
            }
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return;
            }
            String day = SystemUtils.getDay();
            int timeInterval = Base64Utils.getTimeTag(System.currentTimeMillis());
            ContentValues cv = getContentValues(ocInfo);
//            ELOG.i(ocInfo + "  ocInfo update()");
            db.update(DBConfig.OC.TABLE_NAME, cv,
                DBConfig.OC.Column.APN + "=? and " + DBConfig.OC.Column.DY + "=? and "
                    + DBConfig.OC.Column.TI + "=? and " + DBConfig.OC.Column.RS + "=?",
                new String[] {EncryptUtils.encrypt(mContext,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName)), day,
                    String.valueOf(timeInterval), ZERO});
        } catch (Throwable e) {
            ELOG.e(e+ "update() ..");
        }finally {
            DBManager.getInstance(mContext).closeDB();
        }

    }

    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValuesForUpdate(JSONObject ocInfo) {
        ContentValues cv = null;
        try {
            if (ocInfo != null) {
                cv = new ContentValues();
                String act = EncryptUtils.encrypt(mContext,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime));
                cv.put(DBConfig.OC.Column.ACT, act);
            }
        } catch (Throwable t) {
            ELOG.e(t.getMessage() + "   ::::getContentValuesForUpdate");
        }
        return cv;
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
                String act = ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime);
                ELOG.i(act + "  :::::::::::act`s value...");
                String an = Base64Utils.encrypt(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationName), insertTime);
                cv.put(DBConfig.OC.Column.APN, EncryptUtils.encrypt(mContext,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName)));
                cv.put(DBConfig.OC.Column.AN, EncryptUtils.encrypt(mContext,an));
                cv.put(DBConfig.OC.Column.AOT, EncryptUtils.encrypt(mContext,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime)));
                cv.put(DBConfig.OC.Column.ACT, EncryptUtils.encrypt(mContext,act));
                cv.put(DBConfig.OC.Column.DY, SystemUtils.getDay());
                cv.put(DBConfig.OC.Column.IT, EncryptUtils.encrypt(mContext,String.valueOf(insertTime)));
                cv.put(DBConfig.OC.Column.AVC, EncryptUtils.encrypt(mContext,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode)));
                cv.put(DBConfig.OC.Column.NT, EncryptUtils.encrypt(mContext,ocInfo.optString(DeviceKeyContacts.OCInfo.NetworkType)));
                cv.put(DBConfig.OC.Column.AT, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationType));
                cv.put(DBConfig.OC.Column.CT, ocInfo.optString(DeviceKeyContacts.OCInfo.CollectionType));
                cv.put(DBConfig.OC.Column.AST, ocInfo.optString(DeviceKeyContacts.OCInfo.SwitchType));
                cv.put(DBConfig.OC.Column.TI, Base64Utils.getTimeTag(insertTime));
                cv.put(DBConfig.OC.Column.ST, ZERO);
                cv.put(DBConfig.OC.Column.RS, TextUtils.isEmpty(act) ? ZERO:ONE );//正在运行为1
            }
        } catch (Throwable t) {
            ELOG.e(t.getMessage() + "   ::::getContentValues");
        }
        return cv;
    }

    /**
     * 查询 正在运行的应用记录
     */
    public JSONArray selectRunning() {
        JSONArray array = null;
        int blankCount = 0;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null){
               return null;
            }
            array = new JSONArray();
            cursor = db.query(DBConfig.OC.TABLE_NAME, null, DBConfig.OC.Column.ACT + "=?",
                new String[] {""}, null, null, null);//closeTime为空的信息
            JSONObject jsonObject = null;
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){//防止空数据导致死循环
                    return array;
                }
                jsonObject = new JSONObject();
                String insertTime = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT)));
                String appName = Base64Utils.decrypt(EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AN))), Long.parseLong(insertTime));
                String apn = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if(TextUtils.isEmpty(apn)){
                    blankCount += 1;
                }
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, apn);
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationName, appName);
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime,
                        EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AOT))));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                        EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AVC))));
                jsonObject.put(DeviceKeyContacts.OCInfo.NetworkType,
                        EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.NT))));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AT)));
                jsonObject.put(DeviceKeyContacts.OCInfo.CollectionType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.CT)));
                array.put(jsonObject);
            }
        } catch (Throwable e) {
            ELOG.e(e+"  selectRunning ..");
        }finally {
            if(cursor != null){
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    /**
     * 更新缓存状态,从0到1
     */
    public void updateRunState(JSONArray ocInfo) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if(db == null || ocInfo == null || ocInfo.length()<1){
                return;
            }
            db.beginTransaction();
            ContentValues cv;
            List list = SystemUtils.getDiffNO(ocInfo.length());
            int random;
            for (int i = 0; i < ocInfo.length(); i++) {
                cv = new ContentValues();
                random = (Integer)list.get(i);
                cv.put(DBConfig.OC.Column.RS, ONE);
                cv.put(DBConfig.OC.Column.ACT, EncryptUtils.encrypt(mContext,String.valueOf(System.currentTimeMillis() - random)));
                String pkgName =
                    EncryptUtils.encrypt(mContext,new JSONObject(ocInfo.get(i).toString()).optString(DeviceKeyContacts.OCInfo.ApplicationPackageName));
                db.update(DBConfig.OC.TABLE_NAME, cv,
                    DBConfig.OC.Column.APN + "=? and " + DBConfig.OC.Column.RS + "=?",
                    new String[] {pkgName, ZERO});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e+"  updateRunState ");
        } finally {
            if(db != null){
                db.endTransaction();
            }
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 更新缓存状态，从1到0
     */
    public void updateStopState(JSONArray ocInfo) {
        SQLiteDatabase db = null;
        JSONObject obj;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if(db == null || ocInfo == null || ocInfo.length()<1){
                return;
            }
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.OC.Column.RS, ZERO);

            for (int i = 0; i < ocInfo.length(); i++) {
                obj = (JSONObject)ocInfo.get(i);
                String pkgName = EncryptUtils.encrypt(mContext,obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName));
                String act = EncryptUtils.encrypt(mContext,obj.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime));
                String switchType = obj.optString(DeviceKeyContacts.OCInfo.SwitchType);
                if(TextUtils.isEmpty(switchType)){
                    switchType = EGContext.APP_SWITCH;
                }
                cv.put(DBConfig.OC.Column.ACT, act);
                cv.put(DBConfig.OC.Column.ST,switchType);
//                db.execSQL("update e_occ set occ_e = occ_e + 1 where occ_a = '" + pkgName + "'");不做打开关闭次数统计，可忽略
                db.update(DBConfig.OC.TABLE_NAME, cv,
                        DBConfig.OC.Column.APN + "=?",
                        new String[] {pkgName});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e+"  updateStopState ..");
        } finally {
            if(db != null){
                db.endTransaction();
            }
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 判断当前时段，是否存在该应用的操作记录
     */
    public List<String> getIntervalApps() {
        List<String> list = null;
        int blankCount = 0;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return list;
            }
            String day = SystemUtils.getDay();
            int timeInterval = Base64Utils.getTimeTag(System.currentTimeMillis());
            cursor = db.query(DBConfig.OC.TABLE_NAME,
                new String[] {DBConfig.OC.Column.APN},
                DBConfig.OC.Column.DY + "=? and " + DBConfig.OC.Column.TI
                    + "=?",// and " + DBConfig.OC.Column.RS + "=? , ZERO}, null
                new String[] {day, String.valueOf(timeInterval)}, null, null,null);
            list = new ArrayList<String>();
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){//防止空数据导致死循环
                    return list;
                }
                String pkgName =
                        EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if (!TextUtils.isEmpty(pkgName)) {
                    list.add(pkgName);
                }else{
                    blankCount +=1;
                }
            }
        } catch (Exception e) {
            ELOG.e(e+" getIntervalApps ");
        }finally {
            if (cursor != null){
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }

        return list;
    }

    /**
     * 读取
     */
    public JSONArray select() {
        JSONArray ocJar = null;
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        Cursor cursor = null;
        int blankCount = 0;
        String pkgName = "",act = "";
        ContentValues cv =null;
        JSONObject jsonObject, etdm;
        try {
            if(db == null){
               return ocJar;
            }
            ocJar = new JSONArray();
            db.beginTransaction();
            cursor = db.query(DBConfig.OC.TABLE_NAME, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return ocJar;
                }
                act = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.ACT)));
                if (TextUtils.isEmpty(act) || "".equals(act)){//closeTime为空，则继续循环，只取closeTime有值的信息
                    continue;
                }
                jsonObject = new JSONObject();
                String insertTime = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT)));
                String encryptAn = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AN)));
                String an = Base64Utils.decrypt(encryptAn, Long.valueOf(insertTime));
                JsonUtils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationOpenTime,
                        EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AOT))),DataController.SWITCH_OF_APPLICATION_OPEN_TIME);
                JsonUtils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationCloseTime, act,DataController.SWITCH_OF_APPLICATION_CLOSE_TIME);
                pkgName = EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if(TextUtils.isEmpty(pkgName)){
                    blankCount +=1;
                }
                JsonUtils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationPackageName,pkgName,DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
                JsonUtils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationName, an,DataController.SWITCH_OF_APPLICATION_NAME);
                JsonUtils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                        EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AVC))),DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
                JsonUtils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.NetworkType,
                        EncryptUtils.decrypt(mContext,cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.NT))),DataController.SWITCH_OF_NETWORK_TYPE);
                etdm = new JSONObject();
                JsonUtils.pushToJSON(mContext,etdm ,DeviceKeyContacts.OCInfo.SwitchType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AST)),DataController.SWITCH_OF_SWITCH_TYPE);
                JsonUtils.pushToJSON(mContext,etdm ,DeviceKeyContacts.OCInfo.ApplicationType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AT)),DataController.SWITCH_OF_APPLICATION_TYPE);
                JsonUtils.pushToJSON(mContext,etdm ,DeviceKeyContacts.OCInfo.CollectionType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.CT)),DataController.SWITCH_OF_COLLECTION_TYPE);
                jsonObject.put(EGContext.EXTRA_DATA, etdm);
                ocJar.put(jsonObject);
                cv = new ContentValues();
                cv.put(DBConfig.OC.Column.ST, ONE);
                db.update(DBConfig.OC.TABLE_NAME, cv,
                 DBConfig.OC.Column.ID + "=? ",
                new String[]{cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.ID))});
            }
             db.setTransactionSuccessful();
        } catch (Exception e) {
            ELOG.e(e.getMessage() + "    :::::::exception ");
        } finally {
            if (cursor != null){
                cursor.close();
            }
            if(db != null){
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return ocJar;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null)
                return;
            db.delete(DBConfig.OC.TABLE_NAME, DBConfig.OC.Column.ST + "=?", new String[] {ONE});
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

}
