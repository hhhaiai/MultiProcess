package com.analysys.track.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.ELOG;

import java.io.File;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "ev2.data";
    private static final int DB_VERSION = 6;
    private static Context mContext = null;

    private DBHelper(Context context) {
        super(EContextHelper.getContext(context), DB_NAME, null, DB_VERSION);
    }

    private static volatile DBHelper instance;

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    if (mContext == null) {
                        mContext = EContextHelper.getContext(mContext);
                    }
                    instance = new DBHelper(mContext);
                }
            }
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db == null) {
            return;
        }
        recreateTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.logcat) {
            ELOG.e("触发升级逻辑");
        }
        //数据库版本机密算法变动，简单粗暴直接删除老的库，重新构建新库
        delDbFile(db);
        recreateTables(db);
    }

    public void recreateTables(SQLiteDatabase db) {
        for (int i = 0; i < 5; i++) {
            try {
                if (db == null) {
                    return;
                }
                if (!DBUtils.isTableExist(db, DBConfig.OC.CREATE_TABLE)) {
                    db.execSQL(DBConfig.OC.CREATE_TABLE);
                }
                if (!DBUtils.isTableExist(db, DBConfig.Location.CREATE_TABLE)) {
                    db.execSQL(DBConfig.Location.CREATE_TABLE);
                }
                if (!DBUtils.isTableExist(db, DBConfig.AppSnapshot.CREATE_TABLE)) {
                    db.execSQL(DBConfig.AppSnapshot.CREATE_TABLE);
                }
                if (!DBUtils.isTableExist(db, DBConfig.ScanningInfo.CREATE_TABLE)) {
                    db.execSQL(DBConfig.ScanningInfo.CREATE_TABLE);
                }
                if (!DBUtils.isTableExist(db, DBConfig.XXXInfo.CREATE_TABLE)) {
                    db.execSQL(DBConfig.XXXInfo.CREATE_TABLE);
                }
                if (!DBUtils.isTableExist(db, DBConfig.NetInfo.CREATE_TABLE)) {
                    db.execSQL(DBConfig.NetInfo.CREATE_TABLE);
                }
                if (!DBUtils.isTableExist(db, DBConfig.IDStorage.CREATE_TABLE)) {
                    db.execSQL(DBConfig.IDStorage.CREATE_TABLE);
                }
            } catch (SQLiteDatabaseCorruptException e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
                delDbFile(db);
            }
        }
    }

    public void delDbFile(SQLiteDatabase db) {
        try {
            mContext=EContextHelper.getContext(mContext);
            if (mContext != null) {
                File f = mContext.getDatabasePath(DB_NAME);
                if (f.exists()) {
                    f.delete();
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

}
