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
import com.analysys.track.utils.FileUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.Utils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class TableOCCount {
    // sv 查询的值selectValue，iv写入的值insertValue
    // private final String sv = "0";
    // private final String iv = "1";

    private final String ZERO = "0";
    private final String ONE = "1";
    Context mContext;

    private static class Holder {
        private static final TableOCCount INSTANCE = new TableOCCount();
    }

    public static TableOCCount getInstance(Context context) {
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
            if (ocInfo == null) {
                return;
            }
            EGContext.isLocked = true;
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            ContentValues cv = getContentValues(ocInfo);
            // ELOG.i(cv+" ：：：：ocInfo "+DBConfig.OCCount.Column.CU);
            cv.put(DBConfig.OCCount.Column.CU, 1);
            db.insert(DBConfig.OCCount.TABLE_NAME, null, cv);
        } catch (Exception e) {
            ELOG.e(e.getMessage() + " ::::::insert()");
        }finally {
            EGContext.isLocked = false;
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * @param ocInfo
     */
    public void insertArray(JSONArray ocInfo) {
        SQLiteDatabase db = null;
        try {
            EGContext.isLocked = true;
            db = DBManager.getInstance(mContext).openDB();
            if (ocInfo != null && ocInfo.length() > 0) {
                db.beginTransaction();
                JSONObject obj = null;
                ContentValues cv = null;
                ELOG.i(ocInfo.length() + "     ：：：：ocInfo size  ");
                for (int i = 0; i < ocInfo.length(); i++) {
                    obj = (JSONObject)ocInfo.get(i);
                    if (obj == null)
                        continue;
                    if (TextUtils.isEmpty(obj.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime))) {
                        cv = getContentValues(obj);
                        // ELOG.i(cv+" ：：：：ocInfo "+DBConfig.OCCount.Column.CU);
                        cv.put(DBConfig.OCCount.Column.CU, 0);
                        db.insert(DBConfig.OCCount.TABLE_NAME, null, cv);
                    } else {
                        cv = getContentValuesForUpdate(obj);
                        db.update(DBConfig.OCCount.TABLE_NAME, cv, DBConfig.OCCount.Column.APN + "=?",
                            new String[] {obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName)});
                    }
                }
                db.setTransactionSuccessful();
            }
        } catch (Throwable e) {
            ELOG.e(e + "  :::::insertArray() has an exception");
        } finally {
            db.endTransaction();
            EGContext.isLocked = false;
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
            EGContext.isLocked = true;
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            String day = SystemUtils.getDay();
            int timeInterval = Base64Utils.getTimeTag(System.currentTimeMillis());
            ContentValues cv = getContentValues(ocInfo);
            ELOG.i(ocInfo + "  ocInfo update()");
            db.update(DBConfig.OCCount.TABLE_NAME, cv,
                DBConfig.OCCount.Column.APN + "=? and " + DBConfig.OCCount.Column.DY + "=? and "
                    + DBConfig.OCCount.Column.TI + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                new String[] {ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName), day,
                    String.valueOf(timeInterval), ZERO});
        } catch (Throwable e) {
            ELOG.e(e);
        }finally {
            EGContext.isLocked = false;
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
                ELOG.i(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime) + "  :::::::::::act`s value...");
                String act = ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime);
                cv.put(DBConfig.OCCount.Column.ACT, act);
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
                ELOG.i(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime) + "  :::::::::::act`s value...");
                String an = Base64Utils.encrypt(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationName), insertTime);
                String act = ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime);
                cv.put(DBConfig.OCCount.Column.APN, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName));
                cv.put(DBConfig.OCCount.Column.AN, an);
                cv.put(DBConfig.OCCount.Column.AOT, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime));
                cv.put(DBConfig.OCCount.Column.ACT, act);
                cv.put(DBConfig.OCCount.Column.DY, SystemUtils.getDay());
                cv.put(DBConfig.OCCount.Column.IT, String.valueOf(insertTime));
                cv.put(DBConfig.OCCount.Column.AVC, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode));
                cv.put(DBConfig.OCCount.Column.NT, ocInfo.optString(DeviceKeyContacts.OCInfo.NetworkType));
                cv.put(DBConfig.OCCount.Column.AT, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationType));
                cv.put(DBConfig.OCCount.Column.CT, ocInfo.optString(DeviceKeyContacts.OCInfo.CollectionType));
                cv.put(DBConfig.OCCount.Column.AST, ocInfo.optString(DeviceKeyContacts.OCInfo.SwitchType));
                cv.put(DBConfig.OCCount.Column.TI, Base64Utils.getTimeTag(insertTime));
                cv.put(DBConfig.OCCount.Column.ST, ZERO);
                cv.put(DBConfig.OCCount.Column.RS, TextUtils.isEmpty(act) ? ONE : ZERO);
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
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            array = new JSONArray();
            Cursor cursor = db.query(DBConfig.OCCount.TABLE_NAME, null, DBConfig.OCCount.Column.RS + "=?",
                new String[] {ONE}, null, null, null);
            JSONObject jsonObject = null;
            while (cursor.moveToNext()) {
                jsonObject = new JSONObject();
                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.IT));
                String appName = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AN));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationPackageName,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.APN)));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationName,
                    Base64Utils.decrypt(appName, Long.valueOf(insertTime)));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AOT)));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AVC)));
                jsonObject.put(DeviceKeyContacts.OCInfo.NetworkType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.NT)));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AT)));
                jsonObject.put(DeviceKeyContacts.OCInfo.CollectionType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.CT)));
                array.put(jsonObject);
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
        return array;
    }

    /**
     * 更新缓存状态,从0到1
     */
    public void updateRunState(JSONArray ocInfo) {
        SQLiteDatabase db = null;
        try {
            EGContext.isLocked = true;
            db = DBManager.getInstance(mContext).openDB();
            if(db == null){
                return;
            }
            db.beginTransaction();
            ContentValues cv;
            List list = SystemUtils.getDiffNO(ocInfo.length() - 1);
            int random;
            for (int i = 0; i < ocInfo.length(); i++) {
                cv = new ContentValues();
                random = (Integer)list.get(i);
                ELOG.i("updateRunState ::::" + random);
                cv.put(DBConfig.OCCount.Column.RS, ONE);
                cv.put(DBConfig.OCCount.Column.ACT, String.valueOf(System.currentTimeMillis() - random));
                String pkgName =
                    new JSONObject(ocInfo.get(i).toString()).optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                db.update(DBConfig.OCCount.TABLE_NAME, cv,
                    DBConfig.OCCount.Column.APN + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                    new String[] {pkgName, ZERO});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e);
        } finally {
            EGContext.isLocked = false;
            db.endTransaction();
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
            EGContext.isLocked = true;
            db = DBManager.getInstance(mContext).openDB();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.OCCount.Column.RS, ZERO);
            cv.put(DBConfig.OCCount.Column.ACT, String.valueOf(System.currentTimeMillis()));
            for (int i = 0; i < ocInfo.length(); i++) {
                obj = (JSONObject)ocInfo.get(i);
                String pkgName = obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                db.execSQL("update e_occ set occ_e = occ_e + 1 where occ_a = '" + pkgName + "'");
                db.update(DBConfig.OCCount.TABLE_NAME, cv,
                        DBConfig.OCCount.Column.APN + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                        new String[] {pkgName, ONE});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e);
        } finally {
            EGContext.isLocked = false;
            db.endTransaction();
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
            String day = SystemUtils.getDay();
            int timeInterval = Base64Utils.getTimeTag(System.currentTimeMillis());
            cursor = db.query(DBConfig.OCCount.TABLE_NAME,
                new String[] {DBConfig.OCCount.Column.APN},
                DBConfig.OCCount.Column.DY + "=? and " + DBConfig.OCCount.Column.TI
                    + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                new String[] {day, String.valueOf(timeInterval), ZERO}, null, null, null);
            list = new ArrayList<String>();
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){//防止空数据导致死循环
                    return list;
                }
                String pkgName =
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.APN));
                if (!TextUtils.isEmpty(pkgName)) {
                    list.add(pkgName);
                }else{
                    blankCount ++;
                }
            }
        } catch (Exception e) {
            ELOG.e(e);
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
        JSONArray ocCountJar = null;
        EGContext.isLocked = true;
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        Cursor cursor = null;
        int blankCount = 0;
        String pkgName = "";
        try {
            if(db == null){
               return ocCountJar;
            }
            ocCountJar = new JSONArray();
            db.beginTransaction();
            cursor = db.query(DBConfig.OCCount.TABLE_NAME, null, null, null, null, null, null);
            String act = "";
            ContentValues cv =null;
            JSONObject jsonObject, etdm;
            while (cursor.moveToNext()) {
                if(blankCount >= EGContext.BLANK_COUNT_MAX){
                    return ocCountJar;
                }
                act =
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.ACT));
                if (TextUtils.isEmpty(act) || "".equals(act))
                    continue;
                jsonObject = new JSONObject();
                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.IT));
                String encryptAn = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AN));
                String an = Base64Utils.decrypt(encryptAn, Long.valueOf(insertTime));
                Utils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationOpenTime,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AOT)),DataController.SWITCH_OF_APPLICATION_OPEN_TIME);
                Utils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationCloseTime, act,DataController.SWITCH_OF_APPLICATION_CLOSE_TIME);
                pkgName = cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.APN));
                if(TextUtils.isEmpty(pkgName)){
                    blankCount++;
                }
                Utils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationPackageName,pkgName,DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
                Utils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationName, an,DataController.SWITCH_OF_APPLICATION_NAME);
                Utils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AVC)),DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
                Utils.pushToJSON(mContext,jsonObject ,DeviceKeyContacts.OCInfo.NetworkType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.NT)),DataController.SWITCH_OF_NETWORK_TYPE);
                etdm = new JSONObject();
                Utils.pushToJSON(mContext,etdm ,DeviceKeyContacts.OCInfo.SwitchType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AST)),DataController.SWITCH_OF_SWITCH_TYPE);
                Utils.pushToJSON(mContext,etdm ,DeviceKeyContacts.OCInfo.ApplicationType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AT)),DataController.SWITCH_OF_APPLICATION_TYPE);
                Utils.pushToJSON(mContext,etdm ,DeviceKeyContacts.OCInfo.CollectionType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.CT)),DataController.SWITCH_OF_COLLECTION_TYPE);
                jsonObject.put(EGContext.EXTRA_DATA, etdm);
                ocCountJar.put(jsonObject);
                cv = new ContentValues();
                cv.put(DBConfig.OCCount.Column.ST, ONE);
                db.update(DBConfig.OCCount.TABLE_NAME, cv,
                 DBConfig.OCCount.Column.ID + "=? ",
                new String[]{cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.ID))});
            }
             db.setTransactionSuccessful();
        } catch (Exception e) {
            ELOG.e(e.getMessage() + "    :::::::exception ");
        } finally {
            if (cursor != null){
                cursor.close();
            }
            db.endTransaction();
            EGContext.isLocked = false;
            DBManager.getInstance(mContext).closeDB();
        }
        return ocCountJar;
    }

    public void delete() {
        try {
            EGContext.isLocked = true;
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null)
                return;
            db.delete(DBConfig.OCCount.TABLE_NAME, DBConfig.OCCount.Column.ST + "=?", new String[] {ONE});
        } catch (Throwable e) {
        } finally {
            EGContext.isLocked = false;
            DBManager.getInstance(mContext).closeDB();
        }
    }
}
