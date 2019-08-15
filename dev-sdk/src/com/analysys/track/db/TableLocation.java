package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.data.Base64Utils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 定位信息操作
 * @Version: 1.0
 * @Create: 2019-08-15 17:46:46
 * @author: sanbo
 */
public class TableLocation {


    public void insert(JSONObject locationInfo) {
        try {
            if (EGContext.DEBUG_LOCATION) {
                ELOG.i(EGContext.TAG_LOC, " 位置信息即将插入DB .....");
            }
            ContentValues cv = null;
            String locationTime = null;
            long time = -1;
            String encryptLocation = null;
            if (locationInfo != null && locationInfo.length() > 0) {
                locationTime = null;
                locationTime = locationInfo.optString(UploadKey.LocationInfo.CollectionTime);
                time = 0;
                if (!TextUtils.isEmpty(locationTime)) {
                    time = Long.parseLong(locationTime);
                }
                encryptLocation = Base64Utils.encrypt(String.valueOf(locationInfo), time);
                if (!TextUtils.isEmpty(encryptLocation)) {
                    cv = new ContentValues();
                    // LI 加密
                    cv.put(DBConfig.Location.Column.LI, EncryptUtils.encrypt(mContext, encryptLocation));
                    cv.put(DBConfig.Location.Column.IT, locationTime);
                    cv.put(DBConfig.Location.Column.ST, mInsertStatusDefault);
                    SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
                    if (db == null) {
                        return;
                    }
                    long result = db.insert(DBConfig.Location.TABLE_NAME, null, cv);
                    if (EGContext.DEBUG_LOCATION) {
                        ELOG.i(EGContext.TAG_LOC, " 位置信息插入DB 完毕，结果: " + result);
                    }
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    public JSONArray select(long maxLength) {
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
            db.beginTransaction();
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
                            cv.put(DBConfig.Location.Column.ST, mInsertStatusReadOver);
                            db.update(DBConfig.Location.TABLE_NAME, cv, DBConfig.Location.Column.ID + "=?",
                                    new String[]{String.valueOf(id)});
                            array.put(new JSONObject(decryptLocation));
                        }
                    } else {
                        ContentValues cv = new ContentValues();
                        cv.put(DBConfig.Location.Column.ST, mInsertStatusReadOver);
                        db.update(DBConfig.Location.TABLE_NAME, cv, DBConfig.Location.Column.ID + "=?",
                                new String[]{String.valueOf(id)});
                        array.put(new JSONObject(decryptLocation));
                    }

                } else {
                    blankCount += 1;
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
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
        return array;
    }

    public void delete() {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            db.delete(DBConfig.Location.TABLE_NAME, DBConfig.Location.Column.ST + "=?",
                    new String[]{mInsertStatusReadOver});
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
            db.delete(DBConfig.Location.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }

    private static class Holder {
        private static final TableLocation INSTANCE = new TableLocation();
    }


    private TableLocation() {
    }

    public static TableLocation getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    private Context mContext;
    private String mInsertStatusDefault = "0";
    private String mInsertStatusReadOver = "1";
}
