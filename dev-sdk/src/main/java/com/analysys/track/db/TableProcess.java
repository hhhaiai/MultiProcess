package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.data.Base64Utils;
import com.analysys.track.utils.data.EncryptUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public void insertNet(String netInfo) {
        try {
            if (TextUtils.isEmpty(netInfo)) {
                return;
            }
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            ContentValues cv = new ContentValues();
            cv.put(DBConfig.NetInfo.Column.TIME, System.currentTimeMillis());
            cv.put(DBConfig.NetInfo.Column.PROC, EncryptUtils.encrypt(mContext, netInfo));
            // 防止因为传递控制导致的写入异常
            if (cv.size() > 1) {
                db.insert(DBConfig.NetInfo.TABLE_NAME, null, cv);
            }

        } catch (Throwable e) {
            if (BuildConfig.logcat) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }

    }

    // 连表查询
    public JSONArray selectNet(long maxLength) {
        JSONArray array = new JSONArray();
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return array;
            }
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
                            long size = String.valueOf(array).getBytes("UTF-8").length;
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
            if (BuildConfig.logcat) {
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
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.NetInfo.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public void deleteScanningInfos() {
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.ScanningInfo.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }
///********************************************************* xxx ***********************************************************/
//    /**
//     * 存储数据
//     */
//    public void insertXXX(JSONObject xxxInfo) {
//        try {
//            if (xxxInfo == null || xxxInfo.length() < 1) {
//                return;
//            }
//            SQLiteDatabase db = prepareGetDB();
//            if (db == null) {
//                return;
//            }
//            ContentValues cv = getContentValuesXXX(xxxInfo);
//            // 防止因为传递控制导致的写入异常
//            if (cv.size() > 1) {
//                db.insert(DBConfig.XXXInfo.TABLE_NAME, null, cv);
//            }
//
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(e);
//            }
//        } finally {
//            DBManager.getInstance(mContext).closeDB();
//        }
//
//    }
//    /**
//     * json数据转成ContentValues
//     */
//    private ContentValues getContentValuesXXX(JSONObject xxxInfo) {
//        ContentValues cv = new ContentValues();
//        //样例数据: {"time":1563676428130,"ocr":["com.alipay.hulu","com.device"],"result":[{"pid":4815,"oomScore":41,"pkg":"com.device","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10219\/pid_4815","oomAdj":"0"},{"pid":3644,"oomScore":95,"pkg":"com.alipay.hulu","cpuset":"\/foreground","cgroup":"3:cpuset:\/foreground\n2:cpu:\/\n1:cpuacct:\/uid_10131\/pid_3644","oomAdj":"1"}]}
//        if (xxxInfo.has(ProcUtils.RUNNING_RESULT)) {
//            String result = xxxInfo.optString(ProcUtils.RUNNING_RESULT);
//            if (!TextUtils.isEmpty(result)) {
//                // PROC
//                cv.put(DBConfig.XXXInfo.Column.PROC, EncryptUtils.encrypt(mContext, result));
//                // time 不加密
//                if (xxxInfo.has(ProcUtils.RUNNING_TIME)) {
//                    long time = xxxInfo.optLong(ProcUtils.RUNNING_TIME);
//                    cv.put(DBConfig.XXXInfo.Column.TIME, String.valueOf(time));
//                } else {
//                    cv.put(DBConfig.XXXInfo.Column.TIME, String.valueOf(System.currentTimeMillis()));
//                }
//            }
//        }
//        return cv;
//    }
//
//    public void deleteXXX() {
//        try {
//            SQLiteDatabase db = prepareGetDB();
//            if (db == null) {
//                return;
//            }
//            db.delete(DBConfig.XXXInfo.TABLE_NAME, null, null);
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(e);
//            }
//        } finally {
//            DBManager.getInstance(mContext).closeDB();
//        }
//    }
//
//    /**
//     * 按时间删除
//     *
//     * @param idList
//     */
//    public void deleteByIDXXX(List<String> idList) {
//        try {
//            if (idList == null || idList.size() < 1) {
//                return;
//            }
//            SQLiteDatabase db = prepareGetDB();
//            if (db == null) {
//                return;
//            }
//            for (int i = 0; i < idList.size(); i++) {
//                String id = idList.get(i);
//                if (TextUtils.isEmpty(id)) {
//                    return;
//                }
//                db.delete(DBConfig.XXXInfo.TABLE_NAME, DBConfig.XXXInfo.Column.ID + "=?", new String[]{id});
//            }
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(e);
//            }
//        } finally {
//            DBManager.getInstance(mContext).closeDB();
//        }
//    }
//
//    // 连表查询
//    public JSONArray selectXXX(long maxLength) {
//        JSONArray array = null;
//        Cursor cursor = null;
//        int blankCount = 0, countNum = 0;
//        try {
//            SQLiteDatabase db = prepareGetDB();
//            if (db == null) {
//                return array;
//            }
//            array = new JSONArray();
//            cursor = db.query(DBConfig.XXXInfo.TABLE_NAME, null, null, null, null, null, null, "2000");
//            JSONObject jsonObject = null;
//            String proc = null;
//            while (cursor.moveToNext()) {
//                countNum++;
//                if (blankCount >= EGContext.BLANK_COUNT_MAX) {
//                    return array;
//                }
//                jsonObject = new JSONObject();
//                String id = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.ID));
//                if (TextUtils.isEmpty(id)) {
//                    blankCount += 1;
//                }
//                proc = EncryptUtils.decrypt(mContext,
//                        cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.PROC)));
//                if (!TextUtils.isEmpty(proc) && !"null".equalsIgnoreCase(proc)) {
//                    JsonUtils.add(mContext, jsonObject, ProcUtils.RUNNING_RESULT, new JSONArray(proc),
//                            DataController.SWITCH_OF_CL_MODULE_PROC);
//                    if (jsonObject == null || jsonObject.length() < 1) {
//                        return array;
//                    } else {
//                        // time 不加密
//                        String time = cursor.getString(cursor.getColumnIndex(DBConfig.XXXInfo.Column.TIME));
//                        JsonUtils.add(mContext, jsonObject, ProcUtils.RUNNING_TIME, time,
//                                DataController.SWITCH_OF_RUNNING_TIME);
//                        if (countNum / 300 > 0) {
//                            countNum = countNum % 300;
//                            long size = String.valueOf(array).getBytes("UTF-8").length;
//                            if (size >= maxLength * 9 / 10) {
////                                ELOG.e(" size值：："+size+" maxLength = "+maxLength);
//                                UploadImpl.isChunkUpload = true;
//                                break;
//                            } else {
//                                UploadImpl.idList.add(id);
//                                array.put(new String(
//                                        Base64.encode(String.valueOf(jsonObject).getBytes(), Base64.DEFAULT)));
//                            }
//                        } else {
//                            UploadImpl.idList.add(id);
//                            array.put(new String(Base64.encode(String.valueOf(jsonObject).getBytes(), Base64.DEFAULT)));
//                        }
//
//                    }
//                } else {
//                    return array;
//                }
//
//            }
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(e);
//            }
//            array = null;
//        } finally {
//            StreamerUtils.safeClose(cursor);
//            DBManager.getInstance(mContext).closeDB();
//        }
//        return array;
//    }
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
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
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
            cv.put(DBConfig.OC.Column.CU, 1);
            long result = db.insert(DBConfig.OC.TABLE_NAME, null, cv);
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_oc, "写入  结果：[" + result + "]。。。。");
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        String pkgName = "", act = "";
        JSONObject jsonObject, etdm;
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return ocJar;
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

                JsonUtils.add(mContext, jsonObject, UploadKey.OCInfo.ApplicationCloseTime, act,
                        DataController.SWITCH_OF_APPLICATION_CLOSE_TIME);


                id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConfig.OC.Column.ID));
                //IT 不加密
                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT));


                //AOT 不加密
                JsonUtils.add(mContext, jsonObject, UploadKey.OCInfo.ApplicationOpenTime,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AOT)),
                        DataController.SWITCH_OF_APPLICATION_OPEN_TIME);

                // APN 加密
                pkgName = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.APN)));
                if (TextUtils.isEmpty(pkgName)) {
                    blankCount += 1;
                }
                JsonUtils.add(mContext, jsonObject, UploadKey.OCInfo.ApplicationPackageName, pkgName,
                        DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);

                // AN 加密
                String an = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AN)));
                JsonUtils.add(mContext, jsonObject, UploadKey.OCInfo.ApplicationName, an,
                        DataController.SWITCH_OF_APPLICATION_NAME);

                //AVC 加密
                String avc = EncryptUtils.decrypt(mContext,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AVC)));
                JsonUtils.add(mContext, jsonObject, UploadKey.OCInfo.ApplicationVersionCode, avc,
                        DataController.SWITCH_OF_APPLICATION_VERSION_CODE);
                //NT不加密
                JsonUtils.add(mContext, jsonObject, UploadKey.OCInfo.NetworkType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.NT)),
                        DataController.SWITCH_OF_NETWORK_TYPE);

                etdm = new JSONObject();
                // AST 不加密
                JsonUtils.add(mContext, etdm, UploadKey.OCInfo.SwitchType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AST)),
                        DataController.SWITCH_OF_SWITCH_TYPE);
                // AT 不加密
                JsonUtils.add(mContext, etdm, UploadKey.OCInfo.ApplicationType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.AT)),
                        DataController.SWITCH_OF_APPLICATION_TYPE);
                // CT 不加密
                JsonUtils.add(mContext, etdm, UploadKey.OCInfo.CollectionType,
                        cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.CT)),
                        DataController.SWITCH_OF_COLLECTION_TYPE);
                jsonObject.put(EGContext.EXTRA_DATA, etdm);
                if (countNum / 800 > 0) {
                    countNum = countNum % 800;
                    long size = String.valueOf(ocJar).getBytes("UTF-8").length;
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
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_oc, e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return ocJar;
    }


    /**
     * 删除ocinfo
     *
     * @param isAllClear 是否删除所有的OCINFO
     */
    public void deleteOC(boolean isAllClear) {
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            if (isAllClear) {
                db.delete(DBConfig.OC.TABLE_NAME, null, null);
            } else {
                db.delete(DBConfig.OC.TABLE_NAME, DBConfig.OC.Column.ST + "=?", new String[]{EGContext.DEFAULT_ONE});
            }
//            ELOG.e("删除的行数：：：  "+count);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_loc, " 位置信息即将插入DB insertLocation().....");
            }
            if (locationInfo == null || locationInfo.length() < 1) {
                return;
            }
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
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
                    long result = db.insert(DBConfig.Location.TABLE_NAME, null, cv);
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_loc, " 位置信息插入DB 完毕 time[" + locationTime + "]，结果: " + result);
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public JSONArray selectLocation(long maxLength) {
        JSONArray array = new JSONArray();
        int blankCount = 0, countNum = 0;
        Cursor cursor = null;
        try {

            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_loc, " 查询位置信息 selectLocation().....");
            }
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return array;
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
                        long size = String.valueOf(array).getBytes("UTF-8").length;
                        if (size >= maxLength * 9 / 10) {
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }

    public void deleteLocation() {
        try {

            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_loc, " 清除状态为1的位置信息  deleteLocation().....");
            }
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.Location.TABLE_NAME, DBConfig.Location.Column.ST + "=?",
                    new String[]{EGContext.DEFAULT_ONE});
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public void deleteAllLocation() {
        try {

            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_loc, " 清除所有的位置信息 deleteLocation().....");
            }
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.Location.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            if (snapshotsList == null || snapshotsList.size() < 1) {
                return;
            }
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }

            if (snapshotsList != null && snapshotsList.size() > 0) {
                for (int i = 0; i < snapshotsList.size(); i++) {
                    JSONObject obj = snapshotsList.get(i);
                    if (obj != null && obj.length() > 0) {
                        long result = db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValuesSnapshot(obj));
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_snap, "批量 [" + i + "/" + snapshotsList.size() + "] 写入安装列表, 结果 : " + result);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            if (obj == null || obj.length() < 1) {
                return;
            }
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }

            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_snap, "。。。obj:" + obj.toString());
            }
            long result = db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValuesSnapshot(obj));
            if (BuildConfig.logcat) {
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
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

            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_snap, " 更新信息-----> " + appTag);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            JsonUtils.add(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationPackageName, pkgName,
                    DataController.SWITCH_OF_APPLICATION_PACKAGE_NAME);
            //AN 加密
            String an = EncryptUtils.decrypt(mContext,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AN)));
            JsonUtils.add(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationName, an,
                    DataController.SWITCH_OF_APPLICATION_NAME);

            //AVC 加密
            JsonUtils.add(mContext, jsonObj, UploadKey.AppSnapshotInfo.ApplicationVersionCode,
                    EncryptUtils.decrypt(mContext,
                            cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AVC))),
                    DataController.SWITCH_OF_APPLICATION_VERSION_CODE);

            //AT 不加密
            JsonUtils.add(mContext, jsonObj, UploadKey.AppSnapshotInfo.ActionType,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AT)),
                    DataController.SWITCH_OF_ACTION_TYPE);

            //AHT 不加密
            JsonUtils.add(mContext, jsonObj, UploadKey.AppSnapshotInfo.ActionHappenTime,
                    cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AHT)),
                    DataController.SWITCH_OF_ACTION_HAPPEN_TIME);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return array;
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
                    long size = String.valueOf(array).getBytes("UTF-8").length;
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }


    public void resetSnapshot() {
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            /**
             * 删除标志已经删除的
             */
            // AT 不加密
            int r = db.delete(DBConfig.AppSnapshot.TABLE_NAME, DBConfig.AppSnapshot.Column.AT + "=?",
                    new String[]{EGContext.SNAP_SHOT_UNINSTALL});
            if (BuildConfig.logcat) {
                ELOG.e(BuildConfig.tag_snap, " 即将删除delete数据。。。。。======>" + r);
            }

            /**
             * 全部重置标志位
             */
            ContentValues cv = new ContentValues();
            // AT 不加密
            cv.put(DBConfig.AppSnapshot.Column.AT, EGContext.SNAP_SHOT_DEFAULT);
            int result = db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, null, null);
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_snap, " 重置状态-----> " + result);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }


    public void deleteAllSnapshot() {
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public List<NetInfo.ScanningInfo> selectScanningInfoByPkg(String pkgname, boolean onlyNew) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return null;
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public List<NetInfo.ScanningInfo> selectAllScanningInfos(long maxSize) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return null;
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return null;
    }
    /********************************************************* Finfo  ***********************************************************/
    /**
     * 保存临时数据到DB,方便下次直接加载
     *
     * @param data
     */
    public void flushMemFInfo(Map<String, Long> data) {

        for (Map.Entry<String, Long> e : data.entrySet()) {

            // 取值
            String info = e.getKey();
            //  加密
            info = EncryptUtils.encrypt(mContext, info);

            if (!TextUtils.isEmpty(info)) {
                ContentValues cv = new ContentValues();
                // 存储
                cv.put(DBConfig.FInfo.Column.PKG, info);
                cv.put(DBConfig.FInfo.Column.LAST_TIME, e.getValue());
                cv.put(DBConfig.FInfo.Column.TYPE, DBConfig.FInfo.DefType.TYPE_ACTIVE);
                insertLmf(cv, true);
            }
        }
    }

    /**
     * 保存即将上传数据
     *
     * @param data
     */
    public void flushUploadFInfo(List<JSONObject> data) {

        if (data == null || data.size() < 1) {
            return;
        }
        for (JSONObject item : data) {
            ContentValues cv = new ContentValues();
            //  取值-加密
            String info = EncryptUtils.encrypt(mContext, item.toString());
            if (!TextUtils.isEmpty(info)) {
                // 存储
                cv.put(DBConfig.FInfo.Column.UPDATE_JSON, info);
                cv.put(DBConfig.FInfo.Column.TYPE, DBConfig.FInfo.DefType.TYPE_PREPARE_UPLOAD);
            }
            insertLmf(cv, false);
        }

    }


    /**
     * 写入数据
     *
     * @param cv
     * @param isNeedUpdateByPkg
     */
    private void insertLmf(ContentValues cv, boolean isNeedUpdateByPkg) {
        Cursor cursor = null;
        try {
            if (cv == null || cv.size() < 1) {
                return;
            }
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            long result = -99;
            if (!isNeedUpdateByPkg) {
                result = db.insert(DBConfig.FInfo.TABLE_NAME, null, cv);
            } else {
                /**
                 * 根据数据库中数据是否存在,决定写入还是修改
                 */
                String text = cv.getAsString(DBConfig.FInfo.Column.PKG);
                //1. 查询DB数据
                cursor = db.query(DBConfig.FInfo.TABLE_NAME, null,
                        DBConfig.FInfo.Column.PKG + " = \"" + text + "\"",
                        null,
                        null, null, null);

                // 2. 没有，写入
                if (cursor != null && cursor.getCount() == 0) {
                    result = db.insert(DBConfig.FInfo.TABLE_NAME, null, cv);
                } else {
                    //3. DB表有该数据，查询末次活跃时间
                    while (cursor.moveToNext()) {
                        long time = cursor.getLong(cursor.getColumnIndex(DBConfig.FInfo.Column.LAST_TIME));
                        //4. DB表末次活跃时间和即将存入的不一致，存入，一致放弃操作
                        if (cv.getAsLong(DBConfig.FInfo.Column.LAST_TIME) != time) {
//                            if (BuildConfig.logcat) {
//                                ELOG.i(BuildConfig.tag_finfo, "Finfo 时间不一致，即将写入");
//                            }
                            String updateTime = "UPDATE %s SET %s=%d, %s=%d where %s=\"%s\"";
                            db.execSQL(String.format(updateTime
                                    , DBConfig.FInfo.TABLE_NAME
                                    , DBConfig.FInfo.Column.LAST_TIME, cv.getAsLong(DBConfig.FInfo.Column.LAST_TIME)
                                    , DBConfig.FInfo.Column.TYPE, DBConfig.FInfo.DefType.TYPE_ACTIVE
                                    , DBConfig.FInfo.Column.PKG, text
                            ));
                            //TODO 该方式更新有异常，会多一条数据
//                            db.update(DBConfig.FInfo.TABLE_NAME,cv,null,null);
                        } else {
//                            if (BuildConfig.logcat) {
//                                ELOG.i(BuildConfig.tag_finfo, "Finfo 时间一致，不写入");
//                            }
                        }
                    }
                }
            }
            if (BuildConfig.logcat && result < 0) {
                ELOG.i(BuildConfig.tag_finfo, "Finfo 数据库写入完毕。 Result[" + result + "]  写入数据:" + cv);
            }
        } catch (Throwable e) {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_finfo, e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
    }


    /**
     * 加载到内存中
     */
    public Map<String, Long> loadMemFinfo() {
        Map<String, Long> pkgAndActivieTime = new ConcurrentHashMap<String, Long>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return pkgAndActivieTime;
            }
            cursor = db.query(DBConfig.FInfo.TABLE_NAME, null,
                    DBConfig.FInfo.Column.TYPE + " = " + DBConfig.FInfo.DefType.TYPE_ACTIVE,
                    null,
                    null, null, null);
            if (cursor == null) {
                return pkgAndActivieTime;
            }
            while (cursor.moveToNext()) {
                // 取值
                String pkg = cursor.getString(cursor.getColumnIndex(DBConfig.FInfo.Column.PKG));
                //  解密
                pkg = EncryptUtils.decrypt(mContext, pkg);
                // 有效性检查
                if (!TextUtils.isEmpty(pkg)) {
                    long lastTime = cursor.getLong(cursor.getColumnIndex(DBConfig.FInfo.Column.LAST_TIME));
                    // 加载
                    pkgAndActivieTime.put(pkg, lastTime);
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_finfo, e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return pkgAndActivieTime;
    }

    /**
     * 读取
     */
    public JSONArray selectFinfo(long maxLength) {
        JSONArray result = new JSONArray();
        Cursor cursor = null;
        long countNum = 0L;
        try {

            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return result;
            }

            cursor = db.query(DBConfig.FInfo.TABLE_NAME, null,
                    DBConfig.FInfo.Column.TYPE + " = " + DBConfig.FInfo.DefType.TYPE_PREPARE_UPLOAD,
                    null,
                    null, null, null);
            if (cursor == null) {
                return result;
            }
            List<Integer> ids = new CopyOnWriteArrayList<Integer>();
            while (cursor.moveToNext()) {
                try {
                    //取值
                    String json = cursor.getString(cursor.getColumnIndex(DBConfig.FInfo.Column.UPDATE_JSON));
                    //  解密
                    json = EncryptUtils.decrypt(mContext, json);
                    // 测是否超大
                    if (!TextUtils.isEmpty(json)) {
                        countNum += json.getBytes("UTF-8").length;
                        //是否超体积
                        if (countNum <= maxLength * 9 / 10) {
                            result.put(new JSONObject(json));
                            int id = cursor.getInt(cursor.getColumnIndex(DBConfig.FInfo.Column.ID));
                            ids.add(id);
                        } else {
                            UploadImpl.isChunkUpload = true;
                        }
                    }
                } catch (JSONException e) {
                    //单次异常不处理
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_finfo, e);
                    }
                }
            }
            if (ids.size() > 0) {
                updateFinfoStatus(db, ids, DBConfig.FInfo.DefType.TYPE_ALREADY_UPLOADED);
            }
        } catch (Throwable e) {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_finfo, e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return result;
    }

    private void updateFinfoStatus(SQLiteDatabase db, List<Integer> ids, int status) {
        for (int id : ids) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBConfig.FInfo.Column.TYPE, status);
            db.update(DBConfig.FInfo.TABLE_NAME, contentValues, DBConfig.FInfo.Column.ID + "=?",
                    new String[]{String.valueOf(id)});
        }
    }


    /**
     * 删除Finfo表数据
     *
     * @param isAllDel 是否全部删除  true:清该表数据  false:清除该表已经上传的数据
     */
    public void deleteFinfo(boolean isAllDel) {
        try {
            SQLiteDatabase db = prepareGetDB();
            if (db == null) {
                return;
            }
            if (isAllDel) {
                db.delete(DBConfig.FInfo.TABLE_NAME, null, null);
            } else {
                db.delete(DBConfig.FInfo.TABLE_NAME, DBConfig.FInfo.Column.TYPE + "=?", new String[]{String.valueOf(DBConfig.FInfo.DefType.TYPE_ALREADY_UPLOADED)});
            }

        } catch (Throwable e) {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_finfo, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    /********************************************************* 工具方法 ***********************************************************/

    private SQLiteDatabase prepareGetDB() {
        SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
        if (!db.isOpen()) {
            return DBManager.getInstance(mContext).openDB();
        }
        return db;
    }

    /********************************************************* 单例和对象 ***********************************************************/
    private static class HOLDER {
        private static TableProcess INSTANCE = new TableProcess();
    }

    private TableProcess() {
    }

    public static TableProcess getInstance(Context context) {
        HOLDER.INSTANCE.mContext = EContextHelper.getContext(context);
        return HOLDER.INSTANCE;
    }

    private Context mContext;

}
