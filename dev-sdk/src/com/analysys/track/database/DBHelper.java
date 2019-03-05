package com.analysys.track.database;

import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.FileUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "e.data";
    private static final int DB_VERSION = 1;
    private static Context mContext = null;

    public static DBHelper getInstance(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
        return new DBHelper(mContext);
    }
    public DBHelper(Context context) {
        super(EContextHelper.getContext(context), DB_NAME, null, DB_VERSION);
        recreateTables();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // db.execSQL(DBConfig.OC.CREATE_TABLE);
        db.execSQL(DBConfig.OCCount.CREATE_TABLE);
        db.execSQL(DBConfig.Location.CREATE_TABLE);
        db.execSQL(DBConfig.AppSnapshot.CREATE_TABLE);
        db.execSQL(DBConfig.XXXInfo.CREATE_TABLE);
        db.execSQL(DBConfig.PROCInfo.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public void recreateTables() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            if (db == null)
                return;
            // if(!DBUtils.isTableExist(db ,DBConfig.OC.CREATE_TABLE)){
            // db.execSQL(DBConfig.OC.CREATE_TABLE);
            // }
            if (DBUtils.isTableExist(db, DBConfig.OCCount.CREATE_TABLE)) {
                db.execSQL(DBConfig.OCCount.CREATE_TABLE);
            }
            if (DBUtils.isTableExist(db, DBConfig.Location.CREATE_TABLE)) {
                db.execSQL(DBConfig.Location.CREATE_TABLE);
            }
            if (DBUtils.isTableExist(db, DBConfig.AppSnapshot.CREATE_TABLE)) {
                db.execSQL(DBConfig.AppSnapshot.CREATE_TABLE);
            }
            if (DBUtils.isTableExist(db, DBConfig.XXXInfo.CREATE_TABLE)) {
                db.execSQL(DBConfig.XXXInfo.CREATE_TABLE);
            }
            if (DBUtils.isTableExist(db, DBConfig.PROCInfo.CREATE_TABLE)) {
                db.execSQL(DBConfig.PROCInfo.CREATE_TABLE);
            }

        } catch (SQLiteDatabaseCorruptException e) {
            rebuildDB();
        } finally {
            db.close();
        }
    }

    public void rebuildDB() {
        if (mContext != null) {
            FileUtils.deleteFile(
                "/data/data/" + mContext.getPackageName() + "/databases/" + DB_NAME);
            recreateTables();
        }
    }

    /**
     * 建表
     */
    public void createTable(String createSQL, String tableName) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            if (!DBUtils.isTableExist(db, tableName)) {
                db.execSQL(createSQL);
            }
        } catch (Throwable t) {

        } finally {
            db.close();
        }
    }
}
