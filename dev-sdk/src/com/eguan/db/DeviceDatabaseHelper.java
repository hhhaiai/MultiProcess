package com.eguan.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;

/**
 * 1-2 OCInfo添加CollectionType字段,修改ST,AT字段长度为2varchar
 * 15-16 增加OCTimeTable(Android SDK 5.0 OC采集次数方案)
 * 增加ProcTemp表(用于保存Proc的临时表)
 */

/**
 * 数据表映射： 表名 EguanId -> e_N101 字段名 eguanid -> aa
 * <p>
 * 表名 TmpId -> e_N102 字段名 TmpId -> aa
 * <p>
 * 表名 OCTimeTable -> e_N103 字段名 _id -> aaa 字段名 PackageName -> aa 字段名
 * TimeInterval -> ab 字段名 Count -> ac 字段名 InsertTime -> aab
 * <p>
 * 表名 ProcTemp -> e_N104 字段名 PACKAGENAME -> aa 字段名 OPENTIME -> ab
 * <p>
 * 表名 OCInfo -> e_N105 字段名 _id -> aaa 字段名 ApplicationOpenTime -> aa 字段名
 * ApplicationCloseTime -> ab 字段名 ApplicationPackageName -> ac 字段名
 * ApplicationName -> ad 字段名 ApplicationVersionCode -> ae 字段名 InsertTime -> aab
 * 字段名 Network -> af 字段名 SwitchType -> ag 字段名 ApplicationType -> ah 字段名
 * CollectionType -> ai
 * <p>
 * 表名 IUUInfo -> e_N106 字段名 _id -> aaa 字段名 ApplicationPackageName -> aa 字段名
 * ApplicationName -> ab 字段名 ApplicationVersionCode -> ac 字段名 ActionType -> ad
 * 字段名 ActionHappenTime -> ae 字段名 InsertTime -> aab
 * <p>
 * 表名 NetworkInfo -> e_N107 字段名 _id -> aaa 字段名 ChangeTime -> aa 字段名 NetworkType
 * -> ab 字段名 InsertTime -> aab
 * <p>
 * 表名 WBGInfo -> e_N108 字段名 _id -> aaa 字段名 SSID -> aa 字段名 BSSID -> ab 字段名 LEVEL
 * -> ac 字段名 LAC -> ad 字段名 CellId -> ae 字段名 CT -> af 字段名 GL -> ag 字段名 ip -> ah
 * <p>
 * 表名 AppUploadInfo -> e_N109 字段名 _id -> aaa 字段名 Day -> aa
 */

public class DeviceDatabaseHelper extends SQLiteOpenHelper {

    //    private static final String DBNAME = "deanag.data";// 数据库名称
//    private static final int version = 1;// 数据库版本
    private static String DROP_TABLE = "DROP TABLE IF EXISTS ";

    private static Context mContext = null;

    private static class Holder {
        private static final DeviceDatabaseHelper INSTANCE = new DeviceDatabaseHelper(mContext);
    }

    /**
     * @param context applicationContext. Not null
     * @return
     */
    public static DeviceDatabaseHelper getInstance(Context context) {
        if (mContext == null) {
            mContext = context.getApplicationContext();
        }
        return Holder.INSTANCE;
    }

    private DeviceDatabaseHelper(Context context) {
        // super(new AnalysyDBContextWrapper(context), DBNAME, null, version);
        super(context, DBContent.NAME_DB, null, DBContent.VERSION_DB);
        mContext = context;
        createIfNotExit();
    }

    public void createIfNotExit() {
        SQLiteDatabase db = null;
        try {

            db = getWritableDatabase();
            if (!DBUtils.isTableExist(DBContent.Table_EguanID.TABLE_NAME, db)) {
                db.execSQL(DBContent.Table_EguanID.TABLE_CREATER);
            }
            if (!DBUtils.isTableExist(DBContent.Table_TmpID.TABLE_NAME, db)) {
                db.execSQL(DBContent.Table_TmpID.TABLE_CREATER);
            }
            if (!DBUtils.isTableExist(DBContent.Table_OCTime.TABLE_NAME, db)) {
                db.execSQL(DBContent.Table_OCTime.TABLE_CREATER);
            }
            if (!DBUtils.isTableExist(DBContent.Table_ProcTemp.TABLE_NAME, db)) {
                db.execSQL(DBContent.Table_ProcTemp.TABLE_CREATER);
            }
            if (!DBUtils.isTableExist(DBContent.Table_OCInfo.TABLE_NAME, db)) {
                db.execSQL(DBContent.Table_OCInfo.TABLE_CREATER);
            }
            if (!DBUtils.isTableExist(DBContent.Table_IUUInfo.TABLE_NAME, db)) {
                db.execSQL(DBContent.Table_IUUInfo.TABLE_CREATER);
            }
//            if (!DBUtils.isTableExist(DBContent.Table_NetworkInfo.TABLE_NAME, db)) {
//                db.execSQL(DBContent.Table_NetworkInfo.TABLE_CREATER);
//            }
            if (!DBUtils.isTableExist(DBContent.Table_WBGInfo.TABLE_NAME, db)) {
                db.execSQL(DBContent.Table_WBGInfo.TABLE_CREATER);
            }
//            if (!DBUtils.isTableExist(DBContent.Table_AppUploadInfo.TABLE_NAME, db)) {
//                db.execSQL(DBContent.Table_AppUploadInfo.TABLE_CREATER);
//            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            rebuildDB();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

    }

    public void create(String tableName) {
        if (TextUtils.isEmpty(tableName)) {
            return;
        }
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            if (db == null)
                return;
            if (tableName.equals(DBContent.Table_EguanID.TABLE_NAME)) {
                if (!DBUtils.isTableExist(DBContent.Table_EguanID.TABLE_NAME, db)) {
                    db.execSQL(DBContent.Table_EguanID.TABLE_CREATER);
                }

            } else if (tableName.equals(DBContent.Table_TmpID.TABLE_NAME)) {
                if (!DBUtils.isTableExist(DBContent.Table_TmpID.TABLE_NAME, db)) {
                    db.execSQL(DBContent.Table_TmpID.TABLE_CREATER);
                }
            } else if (tableName.equals(DBContent.Table_OCTime.TABLE_NAME)) {
                if (!DBUtils.isTableExist(DBContent.Table_OCTime.TABLE_NAME, db)) {
                    db.execSQL(DBContent.Table_OCTime.TABLE_CREATER);
                }

            } else if (tableName.equals(DBContent.Table_ProcTemp.TABLE_NAME)) {
                if (!DBUtils.isTableExist(DBContent.Table_ProcTemp.TABLE_NAME, db)) {
                    db.execSQL(DBContent.Table_ProcTemp.TABLE_CREATER);
                }
            } else if (tableName.equals(DBContent.Table_OCInfo.TABLE_NAME)) {
                if (!DBUtils.isTableExist(DBContent.Table_OCInfo.TABLE_NAME, db)) {
                    db.execSQL(DBContent.Table_OCInfo.TABLE_CREATER);
                }

            } else if (tableName.equals(DBContent.Table_IUUInfo.TABLE_NAME)) {
                if (!DBUtils.isTableExist(DBContent.Table_IUUInfo.TABLE_NAME, db)) {
                    db.execSQL(DBContent.Table_IUUInfo.TABLE_CREATER);
                }

//            } else if (tableName.equals(DBContent.Table_NetworkInfo.TABLE_NAME)) {
//                if (!DBUtils.isTableExist(DBContent.Table_NetworkInfo.TABLE_NAME, db)) {
//                    db.execSQL(DBContent.Table_NetworkInfo.TABLE_CREATER);
//
//                }

            } else if (tableName.equals(DBContent.Table_WBGInfo.TABLE_NAME)) {
                if (!DBUtils.isTableExist(DBContent.Table_WBGInfo.TABLE_NAME, db)) {
                    db.execSQL(DBContent.Table_WBGInfo.TABLE_CREATER);
                }

//            } else if (tableName.equals(DBContent.Table_AppUploadInfo.TABLE_NAME)) {
//                if (!DBUtils.isTableExist(DBContent.Table_AppUploadInfo.TABLE_NAME, db)) {
//                    db.execSQL(DBContent.Table_AppUploadInfo.TABLE_CREATER);
//                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    public void rebuildDB() {
        if (mContext != null) {
            DBUtils.deleteDBFile("/data/data/" + mContext.getPackageName() + "/databases/" + DBContent.NAME_DB);
            createIfNotExit();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DBContent.Table_EguanID.TABLE_CREATER);
            db.execSQL(DBContent.Table_TmpID.TABLE_CREATER);
            db.execSQL(DBContent.Table_OCTime.TABLE_CREATER);
            db.execSQL(DBContent.Table_ProcTemp.TABLE_CREATER);
            db.execSQL(DBContent.Table_OCInfo.TABLE_CREATER);
            db.execSQL(DBContent.Table_IUUInfo.TABLE_CREATER);
//            db.execSQL(DBContent.Table_NetworkInfo.TABLE_CREATER);
            db.execSQL(DBContent.Table_WBGInfo.TABLE_CREATER);
//            db.execSQL(DBContent.Table_AppUploadInfo.TABLE_CREATER);
            // 删除老版本数据库
            deleteDatabase(mContext);
        } catch (SQLiteDatabaseCorruptException e) {
            rebuildDB();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.beginTransaction();
            db.execSQL(DROP_TABLE + DBContent.Table_EguanID.TABLE_NAME);
            db.execSQL(DROP_TABLE + DBContent.Table_TmpID.TABLE_NAME);
            db.execSQL(DROP_TABLE + DBContent.Table_OCTime.TABLE_NAME);
            db.execSQL(DROP_TABLE + DBContent.Table_ProcTemp.TABLE_NAME);
            db.execSQL(DROP_TABLE + DBContent.Table_OCInfo.TABLE_NAME);
            db.execSQL(DROP_TABLE + DBContent.Table_IUUInfo.TABLE_NAME);
//            db.execSQL(DROP_TABLE + DBContent.Table_NetworkInfo.TABLE_NAME);
            db.execSQL(DROP_TABLE + DBContent.Table_WBGInfo.TABLE_NAME);
//            db.execSQL(DROP_TABLE + DBContent.Table_AppUploadInfo.TABLE_NAME);
            db.setTransactionSuccessful();
            db.endTransaction();
            onCreate(db);
        } catch (SQLiteDatabaseCorruptException e) {
            DBUtils.deleteDBFile("/data/data/" + mContext.getPackageName() + "/databases/" + DBContent.NAME_DB);
            createIfNotExit();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    public void deleteDatabase(Context context) {
        if (context != null) {
            context.deleteDatabase(DBContent.NAME_OLD_DB);
        }
    }

}