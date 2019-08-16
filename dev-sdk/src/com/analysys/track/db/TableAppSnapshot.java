package com.analysys.track.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.track.internal.content.DataController;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: APP SNAPSHOT 操作
 * @Version: 1.0
 * @Create: 2019-08-12 14:25:43
 * @author: LY
 */
public class TableAppSnapshot {


    /**
     * 批量插入
     *
     * @param snapshotsList
     */
    public void insert(List<JSONObject> snapshotsList) {

        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }

            if (snapshotsList != null && snapshotsList.size() > 0) {

                for (int i = 0; i < snapshotsList.size(); i++) {
                    JSONObject obj = snapshotsList.get(i);
                    if (obj != null && obj.length() > 0) {
                        long result = db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(obj));
                        if (EGContext.DEBUG_SNAP) {
                            ELOG.d(EGContext.TAG_SNAP, "批量 [" + i + "/" + snapshotsList.size() + "] 写入安装列表,结果 : " + result);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (EGContext.DEBUG_SNAP) {
                ELOG.e(EGContext.TAG_SNAP, e);
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
    public void insert(JSONObject obj) {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            if (EGContext.DEBUG_SNAP) {
                ELOG.i(EGContext.TAG_SNAP, "。。。obj:" + obj.toString());
            }
            if (obj != null && obj.length() > 0) {
                long result = db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(obj));
                if (EGContext.DEBUG_SNAP) {
                    ELOG.d(EGContext.TAG_SNAP, "写入安装列表,结果 : " + result);
                }
            }

        } catch (Throwable e) {
            if (EGContext.DEBUG_SNAP) {
                ELOG.e(EGContext.TAG_SNAP, e);
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
    public void update(String pkgName, String appTag, String avc) {
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            ContentValues cv = new ContentValues();
            // AT 不加密
            cv.put(DBConfig.AppSnapshot.Column.AT, appTag);
            cv.put(DBConfig.AppSnapshot.Column.AHT, System.currentTimeMillis());
            // AVC 加密
            cv.put(DBConfig.AppSnapshot.Column.AVC, EncryptUtils.encrypt(mContext, avc));
            // APN 加密
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, DBConfig.AppSnapshot.Column.APN + "= ? ",
                    new String[]{EncryptUtils.encrypt(mContext, pkgName)});

            if (EGContext.DEBUG_SNAP) {
                ELOG.d(EGContext.TAG_SNAP, " 更新信息: " + cv.toString());
            }
        } catch (Throwable e) {
            if (EGContext.DEBUG_SNAP) {
                ELOG.e(EGContext.TAG_SNAP, e);
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
    private ContentValues getContentValues(JSONObject snapshot) {
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
    private JSONObject getCursor(Cursor cursor) {
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
            if (EGContext.DEBUG_SNAP) {
                ELOG.e(EGContext.TAG_SNAP, e);
            }
        }
        return jsonObj;
    }


    /**
     * 数据查询，格式：{JSONObject}
     */
    public JSONArray select(long maxLength) {
        JSONArray array = new JSONArray();
        Cursor cursor = null;
        int blankCount = 0, countNum = 0;
        JSONObject jsonObject = null;
        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
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
                    jsonObject = getCursor(cursor);
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
            if (EGContext.DEBUG_SNAP) {
                ELOG.e(EGContext.TAG_SNAP, e);
            }
        } finally {
            StreamerUtils.safeClose(cursor);
            DBManager.getInstance(mContext).closeDB();
        }
        return array;
    }


    public void reset() {

        try {
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            /**
             * 删除标志已经删除的
             */
            // AT 不加密
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, DBConfig.AppSnapshot.Column.AT + "=?",
                    new String[]{EGContext.SNAP_SHOT_UNINSTALL});

            /**
             * 全部重置标志位
             */
            ContentValues cv = new ContentValues();
            // AT 不加密
            cv.put(DBConfig.AppSnapshot.Column.AT, EGContext.SNAP_SHOT_DEFAULT);
            db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, null, null);
        } catch (Throwable e) {
            if (EGContext.DEBUG_SNAP) {
                ELOG.e(EGContext.TAG_SNAP, e);
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
            db.delete(DBConfig.AppSnapshot.TABLE_NAME, null, null);
        } catch (Throwable e) {
            if (EGContext.DEBUG_SNAP) {
                ELOG.e(EGContext.TAG_SNAP, e);
            }
        } finally {
            DBManager.getInstance(mContext).closeDB();
        }
    }


    private static class Holder {
        private static final TableAppSnapshot INSTANCE = new TableAppSnapshot();
    }

    public static TableAppSnapshot getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    private TableAppSnapshot() {
    }

    private Context mContext;

}