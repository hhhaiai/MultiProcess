package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.impl.net.UploadImpl;
import com.analysys.track.internal.Content.DataController;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.Base64Utils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TableOC {

    private final String ZERO = "0";
    private final String ONE = "1";
    Context mContext;

    private TableOC() {
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
            if (db == null || ocInfo == null || ocInfo.length() < 1) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = getContentValues(ocInfo);
            cv.put(DBConfig.OC.Column.CU, 1);// 可有可无，暂时赋值
            db.insert(DBConfig.OC.TABLE_NAME, null, cv);
        } catch (Exception e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * @param ocInfo
     */
    public void insertArray(List<JSONObject> ocInfo) {
        SQLiteDatabase db = null;
        try {
//            if(!DBUtils.isValidData(mContext,EGContext.FILES_SYNC_OC) || ocInfo == null){
//                return;
//            }
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            if (ocInfo != null && ocInfo.size() > 0) {
                db.beginTransaction();
                JSONObject obj = null;
                ContentValues cv = null;
                for (int i = 0; i < ocInfo.size(); i++) {
                    obj = ocInfo.get(i);
                    if (obj == null) {// 为空则过滤
                        continue;
                    }
                    String act = obj.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime);
                    if (!TextUtils.isEmpty(act)) {// 一条新打开的app信息,执行插入操作
                        cv = getContentValues(obj);
                        cv.put(DBConfig.OC.Column.CU, 1);
                        db.insert(DBConfig.OC.TABLE_NAME, null, cv);
                    }
//                    else {//包含closeTime,修改个别字段
//                        cv = getContentValuesForUpdate(obj);
//                        db.update(DBConfig.OC.TABLE_NAME, cv, DBConfig.OC.Column.APN + "=?",
//                            new String[] {obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName)});
//                    }
                }
                db.setTransactionSuccessful();
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }

        } finally {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
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
                String an = Base64Utils.encrypt(ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationName), insertTime);
                cv.put(DBConfig.OC.Column.APN, EncryptUtils.encrypt(mContext,
                        ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName)));
                cv.put(DBConfig.OC.Column.AN, EncryptUtils.encrypt(mContext, an));
                cv.put(DBConfig.OC.Column.AOT,
                        EncryptUtils.encrypt(mContext, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime)));
                cv.put(DBConfig.OC.Column.ACT, EncryptUtils.encrypt(mContext, act));
                cv.put(DBConfig.OC.Column.DY, SystemUtils.getDay());
                cv.put(DBConfig.OC.Column.IT, EncryptUtils.encrypt(mContext, String.valueOf(insertTime)));
                cv.put(DBConfig.OC.Column.AVC, EncryptUtils.encrypt(mContext,
                        ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode)));
                cv.put(DBConfig.OC.Column.NT,
                        EncryptUtils.encrypt(mContext, ocInfo.optString(DeviceKeyContacts.OCInfo.NetworkType)));
                cv.put(DBConfig.OC.Column.AT, ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationType));
                cv.put(DBConfig.OC.Column.CT, ocInfo.optString(DeviceKeyContacts.OCInfo.CollectionType));
                cv.put(DBConfig.OC.Column.AST, ocInfo.optString(DeviceKeyContacts.OCInfo.SwitchType));
                cv.put(DBConfig.OC.Column.TI, Base64Utils.getTimeTag(insertTime));
                cv.put(DBConfig.OC.Column.ST, ZERO);
                cv.put(DBConfig.OC.Column.RS, TextUtils.isEmpty(act) ? ZERO : ONE);// 正在运行为1
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
        return cv;
    }

    /**
     * 更新缓存状态，从1到0
     */
    public void updateStopState(JSONArray ocInfo) {
        SQLiteDatabase db = null;
        JSONObject obj;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null || ocInfo == null || ocInfo.length() < 1) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.OC.Column.RS, ZERO);

            for (int i = 0; i < ocInfo.length(); i++) {
                obj = (JSONObject) ocInfo.get(i);
                String pkgName = EncryptUtils.encrypt(mContext,
                        obj.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName));
                String actTime = obj.optString(DeviceKeyContacts.OCInfo.ApplicationCloseTime);
                String act = EncryptUtils.encrypt(mContext, actTime);
                String switchType = obj.optString(DeviceKeyContacts.OCInfo.SwitchType);
                if (TextUtils.isEmpty(switchType)) {
                    switchType = EGContext.APP_SWITCH;
                }
                cv.put(DBConfig.OC.Column.ACT, act);
                cv.put(DBConfig.OC.Column.ST, switchType);
//                db.execSQL("update e_occ set occ_e = occ_e + 1 where occ_a = '" + pkgName + "'");不做打开关闭次数统计，可忽略
                db.update(DBConfig.OC.TABLE_NAME, cv, DBConfig.OC.Column.APN + "=?", new String[] { pkgName });
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }

        } finally {
            if (db != null && db.inTransaction()) {
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
            if (db == null) {
                return list;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            String day = SystemUtils.getDay();
            int timeInterval = Base64Utils.getTimeTag(System.currentTimeMillis());
            cursor = db.query(DBConfig.OC.TABLE_NAME, new String[] { DBConfig.OC.Column.APN },
                    DBConfig.OC.Column.DY + "=? and " + DBConfig.OC.Column.TI + "=?", // and " + DBConfig.OC.Column.RS +
                    // "=? , ZERO}, null
                    new String[] { day, String.valueOf(timeInterval) }, null, null, null);
            list = new ArrayList<String>();
            while (cursor.moveToNext()) {
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {// 防止空数据导致死循环
                    return list;
                }
                String pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if (!TextUtils.isEmpty(pkgName)) {
                    list.add(pkgName);
                } else {
                    blankCount += 1;
                }
            }
        } catch (Exception e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }

        return list;
    }

    /**
     * 读取
     */
    public JSONArray select(long maxLength) {
        JSONArray ocJar = null;
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        String pkgName = "", act = "";
        JSONObject jsonObject, etdm;
        try {
            if (db == null) {
                return ocJar;
            }
            ocJar = new JSONArray();
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.beginTransaction();
            int id = -1;
            cursor = db.query(DBConfig.OC.TABLE_NAME, null, null, null, null, null, null, "6000");
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return ocJar;
                }
                act = EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.ACT)));
                if (TextUtils.isEmpty(act) || "".equals(act)) {// closeTime为空，则继续循环，只取closeTime有值的信息
                    continue;
                }
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConfig.OC.Column.ID));
                jsonObject = new JSONObject();
                String insertTime = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT)));
                String encryptAn = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AN)));
                String an = Base64Utils.decrypt(encryptAn, Long.valueOf(insertTime));
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationOpenTime,
                        EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AOT))),
                        DataController.SWITCH_OF_APPLICATION_OPEN_TIME);
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationCloseTime, act,
                        DataController.SWITCH_OF_APPLICATION_CLOSE_TIME);
                pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if (TextUtils.isEmpty(pkgName)) {
                    blankCount += 1;
                }
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationPackageName, pkgName,
                        DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationName, an,
                        DataController.SWITCH_OF_APPLICATION_NAME);
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                        EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AVC))),
                        DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.NetworkType,
                        EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.NT))),
                        DataController.SWITCH_OF_NETWORK_TYPE);
                etdm = new JSONObject();
                JsonUtils.pushToJSON(mContext, etdm, DeviceKeyContacts.OCInfo.SwitchType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AST)),
                        DataController.SWITCH_OF_SWITCH_TYPE);
                JsonUtils.pushToJSON(mContext, etdm, DeviceKeyContacts.OCInfo.ApplicationType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AT)),
                        DataController.SWITCH_OF_APPLICATION_TYPE);
                JsonUtils.pushToJSON(mContext, etdm, DeviceKeyContacts.OCInfo.CollectionType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.CT)),
                        DataController.SWITCH_OF_COLLECTION_TYPE);
                jsonObject.put(EGContext.EXTRA_DATA, etdm);
                if (countNum / 800 > 0) {
                    countNum = countNum % 800;
                    long size = String.valueOf(ocJar).getBytes().length;
                    if (size >= maxLength * 9 / 10) {
//                        ELOG.e(" size值：："+size+" maxLength = "+maxLength);
                        UploadImpl.isChunkUpload = true;
                        break;
                    } else {
                        ocJar.put(jsonObject);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DBConfig.OC.Column.ST, ONE);
                        db.update(DBConfig.OC.TABLE_NAME, contentValues, DBConfig.OC.Column.ID + "=?",
                                new String[] { String.valueOf(id) });
                    }
                } else {
                    ocJar.put(jsonObject);
                    ContentValues cv = new ContentValues();
                    cv.put(DBConfig.OC.Column.ST, ONE);
                    db.update(DBConfig.OC.TABLE_NAME, cv, DBConfig.OC.Column.ID + "=?",
                            new String[] { String.valueOf(id) });
                }

//                ELOG.e(" size值：："+size+" maxLength = "+maxLength);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen() && db.inTransaction()) {
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return ocJar;
    }

    /**
     * 读取
     */
    public JSONArray selectAll() {
        JSONArray ocJar = null;
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        Cursor cursor = null;
        int blankCount = 0;
        String pkgName = "", act = "";
        ContentValues cv = null;
        JSONObject jsonObject, etdm;
        try {
            if (db == null) {
                return ocJar;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ocJar = new JSONArray();
            db.beginTransaction();
            cursor = db.query(DBConfig.OC.TABLE_NAME, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return ocJar;
                }
                act = EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.ACT)));
                jsonObject = new JSONObject();
                String insertTime = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT)));
                String encryptAn = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AN)));
                String an = Base64Utils.decrypt(encryptAn, Long.valueOf(insertTime));
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationOpenTime,
                        EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AOT))),
                        DataController.SWITCH_OF_APPLICATION_OPEN_TIME);
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationCloseTime, act,
                        DataController.SWITCH_OF_APPLICATION_CLOSE_TIME);
                pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if (TextUtils.isEmpty(pkgName)) {
                    blankCount += 1;
                }
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationPackageName, pkgName,
                        DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationName, an,
                        DataController.SWITCH_OF_APPLICATION_NAME);
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                        EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AVC))),
                        DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
                JsonUtils.pushToJSON(mContext, jsonObject, DeviceKeyContacts.OCInfo.NetworkType,
                        EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.NT))),
                        DataController.SWITCH_OF_NETWORK_TYPE);
                etdm = new JSONObject();
                JsonUtils.pushToJSON(mContext, etdm, DeviceKeyContacts.OCInfo.SwitchType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AST)),
                        DataController.SWITCH_OF_SWITCH_TYPE);
                JsonUtils.pushToJSON(mContext, etdm, DeviceKeyContacts.OCInfo.ApplicationType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AT)),
                        DataController.SWITCH_OF_APPLICATION_TYPE);
                JsonUtils.pushToJSON(mContext, etdm, DeviceKeyContacts.OCInfo.CollectionType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.CT)),
                        DataController.SWITCH_OF_COLLECTION_TYPE);
                jsonObject.put(EGContext.EXTRA_DATA, etdm);
                ocJar.put(jsonObject);
                cv = new ContentValues();
                cv.put(DBConfig.OC.Column.ST, ONE);
                db.update(DBConfig.OC.TABLE_NAME, cv, DBConfig.OC.Column.ID + "=?",
                        new String[] { String.valueOf(cursor.getInt(cursor.getColumnIndex(DBConfig.OC.Column.ID))) });
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return ocJar;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.OC.TABLE_NAME, DBConfig.OC.Column.ST + "=?", new String[] { ONE });
//            ELOG.e("删除的行数：：：  "+count);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public void deleteAll() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.OC.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private static class Holder {
        private static final TableOC INSTANCE = new TableOC();
    }

}
