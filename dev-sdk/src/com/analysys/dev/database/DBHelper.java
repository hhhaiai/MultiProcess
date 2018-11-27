package com.analysys.dev.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import com.analysys.dev.internal.utils.EContextHelper;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/12 16:32
 * @Author: Wang-X-C
 */
public class DBHelper extends SQLiteOpenHelper {

  private static final String DB_NAME = "e.data";
  private static final int DB_VERSION = 1;
  private static Context mContext = null;

  private static class Holder {
    private static final DBHelper INSTANCES = new DBHelper(mContext);
  }

  public static DBHelper getInstance(Context context) {

    if (context != null) {
      mContext = context;
    } else {
      mContext = EContextHelper.getContext();
    }
    return Holder.INSTANCES;
  }

  public DBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    createTables();
  }

  @Override
  public void onCreate(SQLiteDatabase db) {

    db.execSQL(DBConfig.OC.CREATE_TABLE);
    db.execSQL(DBConfig.OCCount.CREATE_TABLE);
    db.execSQL(DBConfig.Location.CREATE_TABLE);
    db.execSQL(DBConfig.AppSnapshot.CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

  public void createTables() {
    try {
      SQLiteDatabase db = getWritableDatabase();
      db.execSQL(DBConfig.OC.CREATE_TABLE);
      db.execSQL(DBConfig.OCCount.CREATE_TABLE);
      db.execSQL(DBConfig.Location.CREATE_TABLE);
      db.execSQL(DBConfig.AppSnapshot.CREATE_TABLE);
    } catch (SQLiteDatabaseCorruptException e) {
      rebuildDB();
    }
  }

  public void rebuildDB() {
    if (mContext != null) {
      DBUtils.deleteDBFile("/data/data/" + mContext.getPackageName() + "/databases/" + DB_NAME);
      createTables();
    }
  }

  /**
   * 建表
   */
  public void createTable(String createSQL, String tableName) {
    try {
      SQLiteDatabase db = getWritableDatabase();
      if (!DBUtils.isTableExist(db, tableName)) {
        db.execSQL(createSQL);
      }
    } catch (SQLiteDatabaseCorruptException e) {
      rebuildDB();
    }
  }
}
