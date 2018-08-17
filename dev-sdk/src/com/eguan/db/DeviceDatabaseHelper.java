package com.eguan.db;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

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

    private static final String DBNAME = "deanag.data";// 数据库名称
    private static final int version = 1;// 数据库版本
    private static String DROP_TABLE = "DROP TABLE IF EXISTS ";

    private static final String CREATE_TABLE_e_N101 = "create table if not exists e_N101(" + "aa varchar(50),"
            + "epa text," + "epb text," + "epc text)";
    private static final String CREATE_TABLE_e_N102 = "create table if not exists e_N102(" + "aa varchar(50) not null,"
            + "epa text," + "epb text," + "epc text)";
    private static final String CREATE_TABLE_e_N103 = "create table if not exists e_N103("
            + "aaa Integer Primary Key Autoincrement , " + "aa varchar(200) not null, " + "ab varchar(2) not null, "
            + "ac Integer not null, " + "aab varchar(50) not null," + "epa text," + "epb text," + "epc text);";

    private static final String CREATE_TABLE_e_N104 = "create  table if not exists e_N104("
            + "aaa Integer Primary Key Autoincrement," + "aa varchar(50) not null," + "ab varchar(50) not null )";

    private static final String CREATE_TABLE_e_N105 = "create table if not exists e_N105("
            + "aaa Integer Primary Key Autoincrement," + "aa varchar(50) not null," + "ab varchar(50) not null,"
            + "ac varchar(200) not null," + "ad varchar(200) not null," + "ae varchar(50) not null,"
            + "aab varchar(50) not null," + "af varchar(50)," + "ag varchar(10)," + "ah varchar(10),"
            + "ai varchar(10)," + "epa text," + "epb text," + "epc text)";

    private static final String CREATE_TABLE_e_N106 = "create table if not exists e_N106("
            + "aaa Integer Primary Key Autoincrement, " + "aa varchar(200) not null, " + "ab varchar(200) not null, "
            + "ac varchar(50) not null, " + "ad varchar(50) not null, " + "ae varchar(50) not null,"
            + "aab varchar(50) not null," + "epa text," + "epb text," + "epc text);";

    private static final String CREATE_TABLE_e_N107 = "create table if not exists e_N107("
            + "aaa Integer Primary Key Autoincrement, " + "aa varchar(50) not null, " + "ab varchar(50) not null,"
            + "aab varchar(50) not null," + "epa text," + "epb text," + "epc text);";

    private static final String CREATE_TABLE_e_N108 = "create table if not exists e_N108("
            + "aaa Integer Primary Key Autoincrement, " + "aa varchar(50), " + "ab varchar(50), " + "ac varchar(50), "
            + "ad varchar(50), " + "ae varchar(50), " + "af varchar(50), " + "ag varchar(50)," + "ah varchar(50),"
            + "epa text," + "epb text," + "epc text);";

    private static final String CREATE_TABLE_e_N109 = "create table if not exists e_N109("
            + "aaa Integer Primary Key Autoincrement, " + "aa varchar(20) not null," + "epa text," + "epb text,"
            + "epc text);";
    private static Context mContext = null;

    private static class Holder {
        private static final DeviceDatabaseHelper INSTANCE = new DeviceDatabaseHelper(mContext);
    }

    /**
     * @param context
     *            applicationContext. Not null
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
        super(context, DBNAME, null, version);
        mContext = context;
        createIfNotExit();
    }

    public void createIfNotExit() {
        SQLiteDatabase db = null;
        try {

            db = getWritableDatabase();
            if (!DBUtils.isTableExist("e_N101", db)) {
                db.execSQL(CREATE_TABLE_e_N101);
            }
            if (!DBUtils.isTableExist("e_N102", db)) {
                db.execSQL(CREATE_TABLE_e_N102);
            }
            if (!DBUtils.isTableExist("e_N103", db)) {
                db.execSQL(CREATE_TABLE_e_N103);
            }
            if (!DBUtils.isTableExist("e_N104", db)) {
                db.execSQL(CREATE_TABLE_e_N104);
            }
            if (!DBUtils.isTableExist("e_N105", db)) {
                db.execSQL(CREATE_TABLE_e_N105);
            }
            if (!DBUtils.isTableExist("e_N106", db)) {
                db.execSQL(CREATE_TABLE_e_N106);
            }
            if (!DBUtils.isTableExist("e_N107", db)) {
                db.execSQL(CREATE_TABLE_e_N107);
            }
            if (!DBUtils.isTableExist("e_N108", db)) {
                db.execSQL(CREATE_TABLE_e_N108);
            }
            if (!DBUtils.isTableExist("e_N109", db)) {
                db.execSQL(CREATE_TABLE_e_N109);
            }
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
            if (tableName.equals("e_N101")) {
                if (!DBUtils.isTableExist("e_N101", db)) {
                    db.execSQL(CREATE_TABLE_e_N101);
                }

            } else if (tableName.equals("e_N102")) {
                if (!DBUtils.isTableExist("e_N102", db)) {
                    db.execSQL(CREATE_TABLE_e_N102);
                }
            } else if (tableName.equals("e_N103")) {
                if (!DBUtils.isTableExist("e_N103", db)) {
                    db.execSQL(CREATE_TABLE_e_N103);
                }

            } else if (tableName.equals("e_N104")) {
                if (!DBUtils.isTableExist("e_N104", db)) {
                    db.execSQL(CREATE_TABLE_e_N104);
                }
            } else if (tableName.equals("e_N105")) {
                if (!DBUtils.isTableExist("e_N105", db)) {
                    db.execSQL(CREATE_TABLE_e_N105);
                }

            } else if (tableName.equals("e_N106")) {
                if (!DBUtils.isTableExist("e_N106", db)) {
                    db.execSQL(CREATE_TABLE_e_N106);
                }

            } else if (tableName.equals("e_N107")) {
                if (!DBUtils.isTableExist("e_N107", db)) {
                    db.execSQL(CREATE_TABLE_e_N107);

                }

            } else if (tableName.equals("e_N108")) {
                if (!DBUtils.isTableExist("e_N108", db)) {
                    db.execSQL(CREATE_TABLE_e_N108);
                }

            } else if (tableName.equals("e_N109")) {
                if (!DBUtils.isTableExist("e_N109", db)) {
                    db.execSQL(CREATE_TABLE_e_N109);
                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

    }

    public void rebuildDB() {
        if (mContext != null) {
            DBUtils.deleteDBFile("/data/data/" + mContext.getPackageName() + "/databases/" + DBNAME);
            createIfNotExit();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_e_N101);
            db.execSQL(CREATE_TABLE_e_N102);
            db.execSQL(CREATE_TABLE_e_N103);
            db.execSQL(CREATE_TABLE_e_N104);
            db.execSQL(CREATE_TABLE_e_N105);
            db.execSQL(CREATE_TABLE_e_N106);
            db.execSQL(CREATE_TABLE_e_N107);
            db.execSQL(CREATE_TABLE_e_N108);
            db.execSQL(CREATE_TABLE_e_N109);
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
            db.execSQL(DROP_TABLE + "e_N101");
            db.execSQL(DROP_TABLE + "e_N102");
            db.execSQL(DROP_TABLE + "e_N103");
            db.execSQL(DROP_TABLE + "e_N104");
            db.execSQL(DROP_TABLE + "e_N105");
            db.execSQL(DROP_TABLE + "e_N106");
            db.execSQL(DROP_TABLE + "e_N107");
            db.execSQL(DROP_TABLE + "e_N108");
            db.execSQL(DROP_TABLE + "e_N109");
            db.setTransactionSuccessful();
            db.endTransaction();
            onCreate(db);
        } catch (SQLiteDatabaseCorruptException e) {
            DBUtils.deleteDBFile("/data/data/" + mContext.getPackageName() + "/databases/" + DBNAME);
            createIfNotExit();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    public void deleteDatabase(Context context) {
        if (context != null) {
            context.deleteDatabase("eguan.db");
        }
    }

}