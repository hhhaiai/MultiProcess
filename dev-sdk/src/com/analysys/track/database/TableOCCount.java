package com.analysys.track.database;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.utils.Base64Utils;
import com.analysys.track.utils.ELOG;
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
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            ContentValues cv = getContentValues(ocInfo);
            // ELOG.i(cv+" ：：：：ocInfo "+DBConfig.OCCount.Column.CU);
            cv.put(DBConfig.OCCount.Column.CU, 0);
            db.insert(DBConfig.OCCount.TABLE_NAME, null, cv);
        } catch (Exception e) {
            ELOG.e(e.getMessage() + " ::::::insert()");
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * @param ocInfo
     */
    public void insertArray(JSONArray ocInfo) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (ocInfo != null && ocInfo.length() > 0) {
                db.beginTransaction();
                JSONObject obj;
                ContentValues cv;
                ELOG.i(ocInfo.length() + "     ：：：：ocInfo size  ");
                for (int i = 0; i < ocInfo.length(); i++) {
                    obj = (JSONObject)ocInfo.get(i);
                    if (obj == null)
                        continue;
                    if (TextUtils.isEmpty(
                        obj.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime))) {
                        cv = getContentValues(obj);
                        // ELOG.i(cv+" ：：：：ocInfo "+DBConfig.OCCount.Column.CU);
                        cv.put(DBConfig.OCCount.Column.CU, 0);
                        db.insert(DBConfig.OCCount.TABLE_NAME, null, cv);
                    } else {
                        cv = getContentValuesForUpdate(obj);
                        db.update(DBConfig.OCCount.TABLE_NAME, cv,
                            DBConfig.OCCount.Column.APN + "=?",
                            new String[] {obj.optString(
                                DeviceKeyContacts.OCInfo.ApplicationPackageName)});
                    }
                }
                db.setTransactionSuccessful();
            }
        } catch (Throwable e) {
            ELOG.e(e + "  :::::insertArray() has an exception");
        } finally {
            db.endTransaction();
        }
        DBManager.getInstance(mContext).closeDB();
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
            String day = Utils.getDay();
            int timeInterval = Base64Utils.getTimeTag(System.currentTimeMillis());
            ContentValues cv = getContentValues(ocInfo);
            ELOG.i(ocInfo + "  ocInfo update()");
            db.update(DBConfig.OCCount.TABLE_NAME, cv,
                DBConfig.OCCount.Column.APN + "=? and " + DBConfig.OCCount.Column.DY
                    + "=? and " + DBConfig.OCCount.Column.TI + "=? and "
                    + DBConfig.OCCount.Column.RS + "=?",
                new String[] {
                    ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName),
                    day, String.valueOf(timeInterval), ZERO});
        } catch (Throwable e) {
            ELOG.e(e);
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValuesForUpdate(JSONObject ocInfo) {
        ContentValues cv = null;
        try {
            if (ocInfo != null) {
                cv = new ContentValues();
                ELOG.i(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime)
                    + "  :::::::::::act`s value...");
                String act =
                    ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime);
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
                ELOG.i(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime)
                    + "  :::::::::::act`s value...");
                String an = Base64Utils.encrypt(
                    ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationName),
                    insertTime);
                String act =
                    ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime);
                cv.put(DBConfig.OCCount.Column.APN,
                    ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName));
                cv.put(DBConfig.OCCount.Column.AN, an);
                cv.put(DBConfig.OCCount.Column.AOT,
                    ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime));
                cv.put(DBConfig.OCCount.Column.ACT, act);
                cv.put(DBConfig.OCCount.Column.DY, Utils.getDay());
                cv.put(DBConfig.OCCount.Column.IT, String.valueOf(insertTime));
                cv.put(DBConfig.OCCount.Column.AVC,
                    ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode));
                cv.put(DBConfig.OCCount.Column.NT,
                    ocInfo.optString(DeviceKeyContacts.OCInfo.NetworkType));
                cv.put(DBConfig.OCCount.Column.AT,
                    ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationType));
                cv.put(DBConfig.OCCount.Column.CT,
                    ocInfo.optString(DeviceKeyContacts.OCInfo.CollectionType));
                cv.put(DBConfig.OCCount.Column.AST,
                    ocInfo.optString(DeviceKeyContacts.OCInfo.SwitchType));
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
            Cursor cursor = db.query(DBConfig.OCCount.TABLE_NAME, null,
                DBConfig.OCCount.Column.RS + "=?", new String[] {ONE}, null, null, null);
            JSONObject jsonObject = null;
            while (cursor.moveToNext()) {
                jsonObject = new JSONObject();
                String insertTime =
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.IT));
                String appName =
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AN));
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
            db = DBManager.getInstance(mContext).openDB();
            db.beginTransaction();
            ContentValues cv;
            List list = Utils.getDiffNO(ocInfo.length() - 1);
            int random;
            for (int i = 0; i < ocInfo.length(); i++) {
                cv = new ContentValues();
                random = (Integer)list.get(i);
                ELOG.i("updateRunState ::::" + random);
                cv.put(DBConfig.OCCount.Column.RS, ONE);
                cv.put(DBConfig.OCCount.Column.ACT,
                    String.valueOf(System.currentTimeMillis() - random));
                String pkgName = new JSONObject(ocInfo.get(i).toString())
                    .optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                db.update(
                    DBConfig.OCCount.TABLE_NAME, cv, DBConfig.OCCount.Column.APN
                        + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                    new String[] {pkgName, ZERO});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e);
        } finally {
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
            db = DBManager.getInstance(mContext).openDB();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.OCCount.Column.RS, ZERO);
            cv.put(DBConfig.OCCount.Column.ACT,
                String.valueOf(System.currentTimeMillis()));
            for (int i = 0; i < ocInfo.length(); i++) {
                obj = (JSONObject)ocInfo.get(i);
                String pkgName =
                    obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
                db.execSQL(
                    "update e_occ set occ_e = occ_e + 1 where occ_a = '" + pkgName + "'");
                db.update(
                    DBConfig.OCCount.TABLE_NAME, cv, DBConfig.OCCount.Column.APN
                        + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                    new String[] {pkgName, ONE});
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            ELOG.e(e);
        } finally {
            db.endTransaction();
        }
        DBManager.getInstance(mContext).closeDB();
    }

    /**
     * 判断当前时段，是否存在该应用的操作记录
     */
    public List<String> getIntervalApps() {
        List<String> list = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            String day = Utils.getDay();
            int timeInterval = Base64Utils.getTimeTag(System.currentTimeMillis());
            Cursor cursor = db.query(DBConfig.OCCount.TABLE_NAME,
                new String[] {DBConfig.OCCount.Column.APN},
                DBConfig.OCCount.Column.DY + "=? and " + DBConfig.OCCount.Column.TI
                    + "=? and " + DBConfig.OCCount.Column.RS + "=?",
                new String[] {day, String.valueOf(timeInterval), ZERO}, null, null, null);
            list = new ArrayList<String>();
            while (cursor.moveToNext()) {
                String pkgName =
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.APN));
                if (!TextUtils.isEmpty(pkgName)) {
                    list.add(pkgName);
                }
            }
        } catch (Exception e) {
            ELOG.e(e);
        }
        DBManager.getInstance(mContext).closeDB();
        return list;
    }

    /**
     * 读取
     */
    public JSONArray select() {
        JSONArray ocCountJar = null;
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        Cursor cursor = null;
        try {
            ocCountJar = new JSONArray();
            // db.beginTransaction();
            cursor =
                db.query(DBConfig.OCCount.TABLE_NAME, null, null, null, null, null, null);
            String act = "";
            // ContentValues cv ;
            JSONObject jsonObject, etdm;
            while (cursor.moveToNext()) {
                act =
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.ACT));
                if (TextUtils.isEmpty(act) || "".equals(act))
                    continue;
                jsonObject = new JSONObject();
                String insertTime =
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.IT));
                String encryptAn =
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AN));
                String an = Base64Utils.decrypt(encryptAn, Long.valueOf(insertTime));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AOT)));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, act);
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationPackageName,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.APN)));
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationName, an);
                jsonObject.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AVC)));
                jsonObject.put(DeviceKeyContacts.OCInfo.NetworkType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.NT)));
                etdm = new JSONObject();
                etdm.put(DeviceKeyContacts.OCInfo.SwitchType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AST)));
                etdm.put(DeviceKeyContacts.OCInfo.ApplicationType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.AT)));
                etdm.put(DeviceKeyContacts.OCInfo.CollectionType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.CT)));
                jsonObject.put("ETDM", etdm);
                ocCountJar.put(jsonObject);
                // cv = new ContentValues();
                // cv.put(DBConfig.OCCount.Column.RS, ONE);

                // db.update(DBConfig.OCCount.TABLE_NAME, cv,
                // DBConfig.OCCount.Column.ID + "=? ",
                // new String[]{cursor.getString(cursor.getColumnIndex(DBConfig.OCCount.Column.ID))});
            }
            // db.setTransactionSuccessful();
        } catch (Exception e) {
            ELOG.e(e.getMessage() + "    :::::::exception ");
        } finally {
            if (cursor != null)
                cursor.close();
            // db.endTransaction();
            DBManager.getInstance(mContext).closeDB();
        }
        return ocCountJar;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null)
                return;
            db.delete(DBConfig.OCCount.TABLE_NAME, DBConfig.OCCount.Column.ST + "=?",
                new String[] {"1"});
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
}
