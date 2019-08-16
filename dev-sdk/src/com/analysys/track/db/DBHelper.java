package com.analysys.track.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;

import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.EContextHelper;

import java.io.File;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "e.data";
    private static final int DB_VERSION = 3;
    private static Context mContext = null;

    public DBHelper(Context context) {
        super(EContextHelper.getContext(context), DB_NAME, null, DB_VERSION);
        recreateTables(getWritableDatabase());
    }

    public static DBHelper getInstance(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
        return new DBHelper(mContext);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db == null) {
            return;
        }
        db.execSQL(DBConfig.OC.CREATE_TABLE);
        db.execSQL(DBConfig.Location.CREATE_TABLE);
        db.execSQL(DBConfig.AppSnapshot.CREATE_TABLE);
        db.execSQL(DBConfig.XXXInfo.CREATE_TABLE);
//        db.execSQL(DBConfig.PROCInfo.CREATE_TABLE);
        db.execSQL(DBConfig.IDStorage.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ELOG.e("触发升级逻辑");
        //数据库版本机密算法变动，简单粗暴直接删除老的库，重新构建新库
        rebuildDB(db);
    }

    public void recreateTables(SQLiteDatabase db) {
        try {

            if (db == null) {
                db = getWritableDatabase();
                if (db == null) {
                    return;
                }
            }
            if (DBUtils.isTableExist(db, DBConfig.OC.CREATE_TABLE)) {
                db.execSQL(DBConfig.OC.CREATE_TABLE);
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
            if (DBUtils.isTableExist(db, DBConfig.IDStorage.CREATE_TABLE)) {
                db.execSQL(DBConfig.IDStorage.CREATE_TABLE);
            }

        } catch (SQLiteDatabaseCorruptException e) {
            rebuildDB(db);
//        } finally {
//            db.close();
        }
    }

    public void rebuildDB(SQLiteDatabase db) {
        try {
            if (mContext != null) {
//            MultiProcessChecker.deleteFile("/data/data/" + mContext.getPackageName() + "/databases/" + DB_NAME);
                File f = mContext.getDatabasePath(DB_NAME);
                if (f.exists()) {
                    f.delete();
                }
                recreateTables(db);
            }
        } catch (Throwable e) {
        }
    }

}
