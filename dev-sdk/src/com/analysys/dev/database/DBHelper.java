package com.analysys.dev.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import com.analysys.dev.internal.utils.LL;

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

    if (mContext == null) {
      mContext = context;
    }
    return Holder.INSTANCES;
  }

  public DBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    createTables();
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    createTables();
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

  public void createTables() {
    SQLiteDatabase db = null;
    try {
      db = getWritableDatabase();

      if (DBUtils.isTableExist(db, DBConfig.TableOCInfo.TABLE_NAME)) {
        db.execSQL(DBConfig.TableOCInfo.CREATE_TABLE);
      }

      if (DBUtils.isTableExist(db, DBConfig.TableOCTimes.TABLE_NAME)) {
        db.execSQL(DBConfig.TableOCTimes.CREATE_TABLE);
      }

      if (DBUtils.isTableExist(db, DBConfig.TableLocation.TABLE_NAME)) {
        db.execSQL(DBConfig.TableLocation.CREATE_TABLE);
      }

      if (DBUtils.isTableExist(db, DBConfig.TableAppSnapshotInfo.TABLE_NAME)) {
        db.execSQL(DBConfig.TableAppSnapshotInfo.CREATE_TABLE);
      }
    } catch (SQLiteDatabaseCorruptException e) {
      DBUtils.deleteDBFile("/data/data/" + mContext.getPackageName() + "/databases/" + DB_NAME);
      createTables();
    } catch (Throwable e) {
      LL.e(e);
    }
  }
}
