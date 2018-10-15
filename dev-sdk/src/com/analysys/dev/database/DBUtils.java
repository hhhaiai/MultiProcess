package com.analysys.dev.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.io.File;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/13 14:49
 * @Author: Wang-X-C
 */
public class DBUtils {

  /**
   * 数据库是否存在该表
   */
  public static boolean isTableExist(SQLiteDatabase db, String tableName) {
    boolean result = false;
    Cursor cursor = null;
    try {
      final String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tableName + "' ";
      cursor = db.rawQuery(sql, null);
      if (cursor.moveToNext()) {
        int count = cursor.getInt(0);
        if (count > 0) {
          result = true;
        }
      }
    } catch (Throwable e) {
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return result;
  }

  public static void deleteDBFile(String filePath) {
    if (!TextUtils.isEmpty(filePath)) {
      File result = new File(filePath);
      if (result != null) {
        if (result.exists()) {
          result.delete();
        }
      }
    }
  }
}
