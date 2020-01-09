package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.impl.oc.ProcUtils;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.data.Base64Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 几种所有的数据操作到一个类。线程安全的单例，避免上行时数据库异常问题。
 * @Version: 1.0
 * @Create: 2019-08-19 15:20:02
 * @author: sanbo
 */
public class TableProcess {


    /********************************************************* xxx ***********************************************************/
    /**
     * 存储数据
     */
    public void insertXXX(JSONObject xxxInfo) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null || xxxInfo == null || xxxInfo.length() < 1) {
                return;
            }

            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = getContentValuesXXX(xxxInfo);
            // 防止因为传递控制导致的写入异常
            if (cv.size() > 1) {
                db.insert(DBConfig.XXXInfo.TABLE_NAME, null, cv);
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }

    }
    /********************************************************* xxx ***********************************************************/
    /**
     * 存储数据
     */
    public void insertNet(String netInfo) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null || netInfo == null || netInfo.length() < 1) {
                return;
            }

            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.NetInfo.Column.TIME, System.currentTimeMillis());
            cv.put(DBConfig.NetInfo.Column.PROC, EncryptUtils.encrypt(mContext, netInfo));
            // 防止因为传递控制导致的写入异常
            if (cv.size() > 1) {
                db.insert(DBConfig.NetInfo.TABLE_NAME, null, cv);
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
    public JSONArray selectNet(long maxLength) {
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
            cursor = db.query(DBConfig.NetInfo.TABLE_NAME, null, null, null, null, null, null, "2000");
            JSONArray jsonArray = null;
            String proc = null;
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return array;
                }

                String id = cursor.getString(cursor.getColumnIndex(DBConfig.NetInfo.Column.ID));
                if (TextUtils.isEmpty(id)) {
                    blankCount += 1;
                }
                proc = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.NetInfo.Column.PROC)));
                if (!TextUtils.isEmpty(proc) && !"null".equalsIgnoreCase(proc)) {
                    jsonArray = new JSONArray(proc);
                    if (jsonArray == null || jsonArray.length() < 1) {
                        return array;
                    } else {

                        if (countNum / 300 > 0) {
                            countNum = countNum % 300;
                            long size = String.valueOf(array).getBytes().length;
                            if (size >= maxLength * 9 / 10) {
//                                ELOG.e(" size值：："+size+" maxLength = "+maxLength);
                                UploadImpl.isChunkUpload = true;
                                break;
                            } else {
                                array.put(jsonArray);
//                                array.put(new String(
//                                        Base64.encode(String.valueOf(jsonArray).getBytes(), Base64.DEFAULT)));
                            }
                        } else {
                            array.put(jsonArray);
                            // array.put(new String(Base64.encode(String.valueOf(jsonArray).getBytes(), Base64.DEFAULT)));
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
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    public void deleteNet() {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.NetInfo.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
    public void deleteScanningInfos() {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.ScanningInfo.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * json数据转成ContentValues
     */
    private ContentValues getContentValuesXXX(JSONObject xxxInfo) {
        ContentValues cv = new ContentValues();
        //样例数据: {"time":1563676428130,"ocr":["com.alipay.hulu","com.device"],"result":[{"pid":4815,"oomScore":41,"pkg":"com.device","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10219\/pid_4815","oomAdj":"0"},{"pid":3644,"oomScore":95,"pkg":"com.alipay.hulu","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10131\/pid_3644","oomAdj":"1"}]}
        if (xxxInfo.has(ProcUtils.RUNNING_RESULT)) {
            String result = xxxInfo.optString(ProcUtils.RUNNING_RESULT);
            if (!TextUtils.isEmpty(result)) {
                // PROC
                cv.put(DBConfig.XXXInfo.Column.PROC, EncryptUtils.encrypt(mContext, result));
                // time 不加密
                if (xxxInfo.has(ProcUtils.RUNNING_TIME)) {
                    long time = xxxInfo.optLong(ProcUtils.RUNNING_TIME);
                    cv.put(DBConfig.XXXInfo.Column.TIME, String.valueOf(time));
                } else {
                    cv.put(DBConfig.XXXInfo.Column.TIME, String.valueOf(System.currentTimeMillis()));
                }
            }
        }
        return cv;
    }

    public void deleteXXX() {
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 按时间删除
     *
     * @param idList
     */
    public void deleteByIDXXX(List<String> idList) {
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
            for (int i = 0; i < idList.size(); i++) {
                String id = idList.get(i);
                if (TextUtils.isEmpty(id)) {
                    return;
                }
                db.delete(DBConfig.XXXInfo.TABLE_NAME, DBConfig.XXXInfo.Column.ID + "=?", new String[]{id});
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    // 连表查询
    public JSONArray selectXXX(long maxLength) {
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
                        // time 不加密
                        String time = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TIME));
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
            array = null;
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }
    /********************************************************* oc ***********************************************************/
    /**
     * 写入数据
     *
     * @param cv
     */
    public void insertOC(ContentValues cv) {

        try {
            if (cv == null || cv.size() < 1) {
                return;
            }
            if (cv.containsKey(DBConfig.OC.Column.ACT) && cv.containsKey(DBConfig.OC.Column.AOT)) {
                // 关闭时间
                String act = cv.getAsString(DBConfig.OC.Column.ACT);
                // 打开时间
                String aot = cv.getAsString(DBConfig.OC.Column.AOT);
                // 关闭时间-开启时间 大于3秒有意义
                if (Long.valueOf(act) - Long.valueOf(aot) < EGContext.MINDISTANCE * 3) {
                    return;
                }
            } else {
                // 没有开始关闭时间，丢弃
                return;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        try {

            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null || cv.size() < 1) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            cv.put(DBConfig.OC.Column.CU, 1);
            long result = db.insert(DBConfig.OC.TABLE_NAME, null, cv);
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_oc, "写入  结果：[" + result + "]。。。。");
            }
        } catch (
                Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_oc, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }


    /**
     * 读取
     */
    public JSONArray selectOC(long maxLength) {
        JSONArray ocJar = new JSONArray();
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        String pkgName = "", act = "";
        JSONObject jsonObject, etdm;
        try {
            if (db == null) {
                return ocJar;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            int id = -1;
            cursor = db.query(DBConfig.OC.TABLE_NAME, null, null, null, null, null, null, "6000");
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return ocJar;
                }

                // ACT不加密
                act = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.ACT));
                if (TextUtils.isEmpty(act) || "".equals(act)) {// closeTime为空，则继续循环，只取closeTime有值的信息
                    continue;
                }
                jsonObject = new JSONObject();

                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationCloseTime, act,
                        DataController.SWITCH_OF_APPLICATION_CLOSE_TIME);


                id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConfig.OC.Column.ID));
                //IT 不加密
                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT));


                //AOT 不加密
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationOpenTime,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AOT)),
                        DataController.SWITCH_OF_APPLICATION_OPEN_TIME);

                // APN 加密
                pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if (TextUtils.isEmpty(pkgName)) {
                    blankCount += 1;
                }
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationPackageName, pkgName,
                        DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);

                // AN 加密
                String an = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AN)));
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationName, an,
                        DataController.SWITCH_OF_APPLICATION_NAME);

                //AVC 加密
                String avc = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AVC)));
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.ApplicationVersionCode, avc,
                        DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
                //NT不加密
                JsonUtils.pushToJSON(mContext, jsonObject, UploadKey.OCInfo.NetworkType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.NT)),
                        DataController.SWITCH_OF_NETWORK_TYPE);

                etdm = new JSONObject();
                // AST 不加密
                JsonUtils.pushToJSON(mContext, etdm, UploadKey.OCInfo.SwitchType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AST)),
                        DataController.SWITCH_OF_SWITCH_TYPE);
                // AT 不加密
                JsonUtils.pushToJSON(mContext, etdm, UploadKey.OCInfo.ApplicationType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AT)),
                        DataController.SWITCH_OF_APPLICATION_TYPE);
                // CT 不加密
                JsonUtils.pushToJSON(mContext, etdm, UploadKey.OCInfo.CollectionType,
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
                        contentValues.put(DBConfig.OC.Column.ST, EGContext.DEFAULT_ONE);
                        db.update(DBConfig.OC.TABLE_NAME, contentValues, DBConfig.OC.Column.ID + "=?",
                                new String[]{String.valueOf(id)});
                    }
                } else {
                    ocJar.put(jsonObject);
                    ContentValues cv = new ContentValues();
                    cv.put(DBConfig.OC.Column.ST, EGContext.DEFAULT_ONE);
                    db.update(DBConfig.OC.TABLE_NAME, cv, DBConfig.OC.Column.ID + "=?",
                            new String[]{String.valueOf(id)});
                }

//                ELOG.e(" size值：："+size+" maxLength = "+maxLength);
            }
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_oc, e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return ocJar;
    }


    public void deleteOC() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.OC.TABLE_NAME, DBConfig.OC.Column.ST + "=?", new String[]{EGContext.DEFAULT_ONE});
//            ELOG.e("删除的行数：：：  "+count);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_oc, e);
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_oc, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
    /********************************************************* location  ***********************************************************/
    /**
     * 将位置信息插入数据库
     *
     * @param locationInfo
     */
    public void insertLocation(JSONObject locationInfo) {
        try {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_loc, " 位置信息即将插入DB .....");
            }
            ContentValues cv = null;
            String locationTime = null;
            long time = -1;
            String encryptLocation = null;
            if (locationInfo != null && locationInfo.length() > 0) {
                locationTime = locationInfo.optString(UploadKey.LocationInfo.CollectionTime);
                if (TextUtils.isEmpty(locationTime)) {
                    locationTime = "0";
                }
                time = Long.parseLong(locationTime);
                encryptLocation = Base64Utils.encrypt(String.valueOf(locationInfo), time);
                if (!TextUtils.isEmpty(encryptLocation)) {
                    cv = new ContentValues();
                    // LI 加密
                    cv.put(DBConfig.Location.Column.LI, EncryptUtils.encrypt(mContext, encryptLocation));
                    cv.put(DBConfig.Location.Column.IT, locationTime);
                    cv.put(DBConfig.Location.Column.ST, EGContext.DEFAULT_ZERO);
                    SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
                    if (db == null) {
                        return;
                    }
                    if (!db.isOpen()) {
                        db = DBManager.getInstance(mContext).openDB();
                    }
                    long result = db.insert(DBConfig.Location.TABLE_NAME, null, cv);
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i(BuildConfig.tag_loc, " 位置信息插入DB 完毕 time[" + locationTime + "]，结果: " + result);
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public JSONArray selectLocation(long maxLength) {
        JSONArray array = null;
        int blankCount = 0, countNum = 0;
        ;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            array = new JSONArray();
            db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return array;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            cursor = db.query(DBConfig.Location.TABLE_NAME, null, null, null, null, null, null, "2000");
            String encryptLocation = "", time = "";
            int id = 0;
            long timeStamp = 0;
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return array;
                }
                id = cursor.getInt(cursor.getColumnIndex(DBConfig.Location.Column.ID));

                time = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.IT));
                if (!TextUtils.isEmpty(time)) {
                    timeStamp = Long.parseLong(time);
                }
                //LI加密
                encryptLocation = cursor.getString(cursor.getColumnIndex(DBConfig.Location.Column.LI));
                String decryptLocation = Base64Utils.decrypt(EncryptUtils.decrypt(mContext, encryptLocation),
                        timeStamp);
                if (!TextUtils.isEmpty(decryptLocation)) {
                    if (countNum / 200 > 0) {
                        countNum = countNum % 200;
                        long size = String.valueOf(array).getBytes().length;
                        if (size >= maxLength) {
//                            ELOG.i(" size值：："+size+" maxLength = "+maxLength);
                            UploadImpl.isChunkUpload = true;
                            break;
                        } else {
                            ContentValues cv = new ContentValues();
                            cv.put(DBConfig.Location.Column.ST, EGContext.DEFAULT_ONE);
                            db.update(DBConfig.Location.TABLE_NAME, cv, DBConfig.Location.Column.ID + "=?",
                                    new String[]{String.valueOf(id)});
                            array.put(new JSONObject(decryptLocation));
                        }
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put(DBConfig.Location.Column.ST, EGContext.DEFAULT_ONE);
                        db.update(DBConfig.Location.TABLE_NAME, cv, DBConfig.Location.Column.ID + "=?",
                                new String[]{String.valueOf(id)});
                        array.put(new JSONObject(decryptLocation));
                    }

                } else {
                    blankCount += 1;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    public void deleteLocation() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }

            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.Location.TABLE_NAME, DBConfig.Location.Column.ST + "=?",
                    new String[]{EGContext.DEFAULT_ONE});
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public void deleteAllLocation() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.Location.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
    /********************************************************* temp id ***********************************************************/
    /**
     * 存储tempid
     *
     * @param tmpId
     */
    public void insertTempId(String tmpId) {
        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            // 如果db对象为空，或者tmpId为空，则return
            if (db == null || TextUtils.isEmpty(tmpId)) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = new ContentValues();
            // TEMPID 加密
            cv.put(DBConfig.IDStorage.Column.TEMPID, EncryptUtils.encrypt(mContext, tmpId));
            db.insert(DBConfig.IDStorage.TABLE_NAME, null, cv);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 读取egid、tmpid
     *
     * @return
     */
    public String selectTempId() {
        String tmpid = "";
        Cursor cursor = null;
        int blankCount = 0;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return tmpid;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            cursor = db.query(DBConfig.IDStorage.TABLE_NAME, null, null, null, null, null, null);
            if (cursor == null) {
                return tmpid;
            }
            while (cursor.moveToNext()) {
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return tmpid;
                }
                // TEMPID 加密
                tmpid = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.IDStorage.Column.TEMPID)));
                if (TextUtils.isEmpty(tmpid)) {
                    blankCount += 1;
                    continue;
                }

            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return tmpid;
    }

    public void deleteTempId() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.IDStorage.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /********************************************************* snap shot ***********************************************************/
    /**
     * 批量插入
     *
     * @param snapshotsList
     */
    public void insertSnapshot(List<JSONObject> snapshotsList) {

        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null || snapshotsList == null || snapshotsList.size() < 1) {
                return;
            }

            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            if (snapshotsList != null && snapshotsList.size() > 0) {
                for (int i = 0; i < snapshotsList.size(); i++) {
                    JSONObject obj = snapshotsList.get(i);
                    if (obj != null && obj.length() > 0) {
                        long result = db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValuesSnapshot(obj));
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.i(BuildConfig.tag_snap, "批量 [" + i + "/" + snapshotsList.size() + "] 写入安装列表, 结果 : " + result);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_snap, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }


    /**
     * 插入单个
     *
     * @param obj
     */
    public void insertSnapshot(JSONObject obj) {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null || obj == null || obj.length() < 1) {
                return;
            }

            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_snap, "。。。obj:" + obj.toString());
            }
            long result = db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValuesSnapshot(obj));
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_snap, "写入安装列表, 结果 : " + result);
            }
            // 写入失败.尝试更改状态
            if (result == -1) {
                updateSnapshot(
                        obj.optString(UploadKey.AppSnapshotInfo.ApplicationPackageName),
                        obj.optString(UploadKey.AppSnapshotInfo.ActionType),
                        ""
                );
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_snap, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * 更新应用标识状态
     *
     * @param pkgName
     * @param appTag
     * @param avc
     */
    public void updateSnapshot(String pkgName, String appTag, String avc) {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = new ContentValues();
            // AT 不加密
            cv.put(DBConfig.AppSnapshot.Column.AT, appTag);
            cv.put(DBConfig.AppSnapshot.Column.AHT, System.currentTimeMillis());
            // AVC 加密
            if (!TextUtils.isEmpty(avc)) {
                cv.put(DBConfig.AppSnapshot.Column.AVC, EncryptUtils.encrypt(mContext, avc));
            }
            // APN 加密
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, DBConfig.AppSnapshot.Column.APN + "= ? ",
                    new String[]{EncryptUtils.encrypt(mContext, pkgName)});

            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_snap, " 更新信息-----> " + appTag);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_snap, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }


    /**
     * 组装加密数据
     *
     * @param snapshot
     * @return
     */
    private ContentValues getContentValuesSnapshot(JSONObject snapshot) {
        ContentValues cv = new ContentValues();

        // APN 加密
        cv.put(DBConfig.AppSnapshot.Column.APN, EncryptUtils.encrypt(mContext,
                snapshot.optString(UploadKey.AppSnapshotInfo.ApplicationPackageName)));
        //AN 加密
        String an = EncryptUtils.encrypt(mContext,
                snapshot.optString(UploadKey.AppSnapshotInfo.ApplicationName));
        cv.put(DBConfig.AppSnapshot.Column.AN, an);
        //AVC 加密
        cv.put(DBConfig.AppSnapshot.Column.AVC, EncryptUtils.encrypt(mContext,
                snapshot.optString(UploadKey.AppSnapshotInfo.ApplicationVersionCode)));
        // AT 不加密
        cv.put(DBConfig.AppSnapshot.Column.AT,
                snapshot.optString(UploadKey.AppSnapshotInfo.ActionType));
        cv.put(DBConfig.AppSnapshot.Column.AHT, snapshot.optString(UploadKey.AppSnapshotInfo.ActionHappenTime));
        return cv;
    }


    /**
     * 从Cursor中解析字段
     *
     * @param cursor
     * @return
     */
    private JSONObject getCursorSnapshot(Cursor cursor) {
        JSONObject jsonObj = null;
        String pkgName = "";
        try {
            jsonObj = new JSONObject();

            //APN 加密
            pkgName = EncryptUtils.decrypt(mContext,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationPackageName, pkgName,
                    DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            //AN 加密
            String an = EncryptUtils.decrypt(mContext,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AN)));
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationName, an,
                    DataController.SWITCH_OF_APPLICATION_NAME);

            //AVC 加密
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationVersionCode,
                    EncryptUtils.decrypt(mContext,
                            cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AVC))),
                    DataController.SWITCH_OF_APPLICATION_VERSION_CODE);

            //AT 不加密
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ActionType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AT)),
                    DataController.SWITCH_OF_ACTION_TYPE);

            //AHT 不加密
            JsonUtils.pushToJSON(mContext, jsonObj, UploadKey.AppSnapshotInfo.ActionHappenTime,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AHT)),
                    DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_snap, e);
            }
        }
        return jsonObj;
    }


    /**
     * 数据查询，格式：{JSONObject}
     */
    public JSONArray selectSnapshot(long maxLength) {
        JSONArray array = new JSONArray();
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        JSONObject jsonObject = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return array;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null, null, null, null, null, null, "4000");
            if (cursor == null) {
                return array;
            }
            while (cursor.moveToNext()) {
                countNum++;
                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
                    return array;
                }
                //APN 加密
                String pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
                if (!TextUtils.isEmpty(pkgName)) {
                    jsonObject = getCursorSnapshot(cursor);
                } else {
                    blankCount += 1;
                    continue;
                }
                if (countNum / 300 > 0) {
                    countNum = countNum % 300;
                    long size = String.valueOf(array).getBytes().length;
                    if (size >= maxLength * 9 / 10) {
//                        ELOG.e(" size值：："+size+" maxLength = "+maxLength);
                        UploadImpl.isChunkUpload = true;
                        break;
                    } else {
                        array.put(jsonObject);
                    }
                } else {
                    array.put(jsonObject);
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_snap, e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }


    public void resetSnapshot() {

        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }


            /**
             * 删除标志已经删除的
             */
            // AT 不加密
            int r = db.delete(DBConfig.AppSnapshot.TABLE_NAME, DBConfig.AppSnapshot.Column.AT + "=?",
                    new String[]{EGContext.SNAP_SHOT_UNINSTALL});
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_snap, " 即将删除delete数据。。。。。======>" + r);
            }


            /**
             * 全部重置标志位
             */
            ContentValues cv = new ContentValues();
            // AT 不加密
            cv.put(DBConfig.AppSnapshot.Column.AT, EGContext.SNAP_SHOT_DEFAULT);
            int result = db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, null, null);
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_snap, " 重置状态-----> " + result);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_snap, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }


    public void deleteAllSnapshot() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_snap, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public void insertScanningInfo(NetInfo.ScanningInfo scanningInfo) {
        try {
            if (scanningInfo == null) {
                return;
            }
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            ContentValues cv = new ContentValues();

            // APN 加密
            cv.put(DBConfig.ScanningInfo.Column.PKG, EncryptUtils.encrypt(mContext, scanningInfo.pkgname));
            cv.put(DBConfig.ScanningInfo.Column.TIME, EncryptUtils.encrypt(mContext, String.valueOf(scanningInfo.time)));
            //AN 加密
            String data = EncryptUtils.encrypt(mContext, scanningInfo.toJson(true).toString());
            cv.put(DBConfig.ScanningInfo.Column.DATA, data);
            if (cv != null && cv.size() > 0) {
                db.insertOrThrow(DBConfig.ScanningInfo.TABLE_NAME, null, cv);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public List<NetInfo.ScanningInfo> selectScanningInfoByPkg(String pkgname, boolean onlyNew) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return null;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            //SELECT * FROM sss where id=( SELECT MAX(id) FROM sss where pkg=123 )

            if (onlyNew) {
                cursor = db.query(DBConfig.ScanningInfo.TABLE_NAME, null,
                        DBConfig.ScanningInfo.Column.ID +
                                "=( SELECT MAX(" + DBConfig.ScanningInfo.Column.ID + ") FROM "
                                + DBConfig.ScanningInfo.TABLE_NAME + " WHERE " +
                                DBConfig.ScanningInfo.Column.PKG + " = \"" + EncryptUtils.encrypt(mContext, pkgname) + "\" )",
                        null,
                        null, null, null);
            } else {
                cursor = db.query(DBConfig.ScanningInfo.TABLE_NAME, null,
                        DBConfig.ScanningInfo.Column.PKG + " = \"" + EncryptUtils.encrypt(mContext, pkgname) + "\"",
                        null,
                        null, null, null);
            }
            List<NetInfo.ScanningInfo> scanningInfos = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(DBConfig.ScanningInfo.Column.DATA));
                data = EncryptUtils.decrypt(mContext, data);
                NetInfo.ScanningInfo info = NetInfo.ScanningInfo.fromJson(new JSONObject(data));
                if (info != null) {
                    scanningInfos.add(info);
                }
            }
            return scanningInfos;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return null;
    }

    public static List<String> waitRemoveScanningInfoIds;

    public void deleteScanningInfosById() {
        if (waitRemoveScanningInfoIds == null) {
            return;
        }
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            for (int i = 0; i < waitRemoveScanningInfoIds.size(); i++) {
                String id = waitRemoveScanningInfoIds.get(i);
                if (TextUtils.isEmpty(id)) {
                    continue;
                }
                db.delete(DBConfig.ScanningInfo.TABLE_NAME, DBConfig.ScanningInfo.Column.ID + " = " + id, null);
            }
            waitRemoveScanningInfoIds.clear();
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public List<NetInfo.ScanningInfo> selectAllScanningInfos(long maxSize) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return null;
            }
            if (!db.isOpen()) {
                db = DBManager.getInstance(mContext).openDB();
            }
            cursor = db.query(DBConfig.ScanningInfo.TABLE_NAME, null,
                    null, null,
                    null, null, null);
            List<NetInfo.ScanningInfo> scanningInfos = new ArrayList<>();
            int currentSize = 0;
            if (waitRemoveScanningInfoIds == null) {
                waitRemoveScanningInfoIds = new ArrayList<>();
            }
            while (cursor.moveToNext()) {
                if (currentSize > maxSize) {
                    UploadImpl.isChunkUpload = true;
                    break;
                }
                String id = cursor.getString(cursor.getColumnIndex(DBConfig.ScanningInfo.Column.ID));
                if (id != null && !"".equals(id)) {
                    waitRemoveScanningInfoIds.add(id);
                }
                String data = cursor.getString(cursor.getColumnIndex(DBConfig.ScanningInfo.Column.DATA));
                data = EncryptUtils.decrypt(mContext, data);
                currentSize += data.length();
                NetInfo.ScanningInfo info = NetInfo.ScanningInfo.fromJson(new JSONObject(data));
                if (info != null) {
                    scanningInfos.add(info);
                }
            }
            return scanningInfos;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return null;
    }

    /********************************************************* 单例和对象 ***********************************************************/
    private static class HOLDER {
        private static TableProcess INSTANCE = new TableProcess();
    }

    private TableProcess() {
    }

    public static TableProcess getInstance(Context context) {
        HOLDER.INSTANCE.init(context);
        return HOLDER.INSTANCE;
    }

    private void init(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext();
        }
    }

    private Context mContext;

}
