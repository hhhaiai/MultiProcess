package com.analysys.dev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.analysys.dev.internal.impl.AppSnapshotImpl;
import com.analysys.dev.internal.utils.EContextHelper;
import com.analysys.dev.internal.utils.LL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/17 15:02
 * @Author: Wang-X-C
 */
public class TableAppSnapshot {

  Context mContext;

  private static class Holder {
    private static final TableAppSnapshot INSTANCE = new TableAppSnapshot();
  }

  public static TableAppSnapshot getInstance(Context context) {
    if (Holder.INSTANCE.mContext == null) {
      if (context != null) {
        Holder.INSTANCE.mContext = context;
      } else {
        Holder.INSTANCE.mContext = EContextHelper.getContext();
      }
    }

    return Holder.INSTANCE;
  }

  /**
   * 覆盖新增数据，多条
   */
  public void coverInsert(List<JSONObject> snapshots) {
    SQLiteDatabase db = null;
    try {
      if (snapshots.size() > 0) {
        db = DBManager.getInstance(mContext).openDB();
        db.beginTransaction();
        db.execSQL("delete from " + DBConfig.AppSnapshot.TABLE_NAME);
        for (int i = 0; i < snapshots.size(); i++) {
          JSONObject snapshot = snapshots.get(i);
          db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshot));
        }
        db.setTransactionSuccessful();
      }
    } catch (Throwable e) {
    } finally {
      db.endTransaction();
    }
    DBManager.getInstance(mContext).closeDB();
  }

  /**
   * 新增数据，用于安装广播
   */
  public void insert(JSONObject snapshots) {
    SQLiteDatabase db = null;
    try {
      db = DBManager.getInstance(mContext).openDB();
      db.insert(DBConfig.AppSnapshot.TABLE_NAME, null, getContentValues(snapshots));
      db.setTransactionSuccessful();
    } catch (Throwable e) {
    }
    DBManager.getInstance(mContext).closeDB();
  }

  private ContentValues getContentValues(JSONObject snapshot) {
    ContentValues cv = new ContentValues();
    cv.put(DBConfig.AppSnapshot.Column.APN, snapshot.optString(AppSnapshotImpl.Snapshot.APN));
    cv.put(DBConfig.AppSnapshot.Column.AN, snapshot.optString(AppSnapshotImpl.Snapshot.AN));
    cv.put(DBConfig.AppSnapshot.Column.AVC, snapshot.optString(AppSnapshotImpl.Snapshot.AVC));
    cv.put(DBConfig.AppSnapshot.Column.AT, snapshot.optString(AppSnapshotImpl.Snapshot.AT));
    cv.put(DBConfig.AppSnapshot.Column.AHT, snapshot.optString(AppSnapshotImpl.Snapshot.AHT));
    return cv;
  }

  /**
   * 数据查询，格式：<pkgName,JSONObject>
   */
  public Map<String, String> mSelect() {
    Map<String, String> map = null;
    try {
      SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
      Cursor cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null,
          null, null, null, null, null);
      map = new HashMap<>();
      while (cursor.moveToNext()) {
        String apn = cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN));
        map.put(apn, String.valueOf(getCursor(cursor)));
      }
    } catch (Throwable e) {
    }
    return map;
  }

  private JSONObject getCursor(Cursor cursor) {
    JSONObject job = null;
    try {
      job = new JSONObject();
      job.put(AppSnapshotImpl.Snapshot.APN, cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.APN)));
      job.put(AppSnapshotImpl.Snapshot.AN, cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AN)));
      job.put(AppSnapshotImpl.Snapshot.AVC, cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AVC)));
      job.put(AppSnapshotImpl.Snapshot.AT, cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AT)));
      job.put(AppSnapshotImpl.Snapshot.AHT, cursor.getString(cursor.getColumnIndex(DBConfig.AppSnapshot.Column.AHT)));
    } catch (Throwable e) {
    }
    return job;
  }

  /**
   * 重置应用状态标识
   */
  public void resetAppType() {
    try {
      SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
      db.delete(DBConfig.AppSnapshot.TABLE_NAME,
          DBConfig.AppSnapshot.Column.AT + "=?", new String[] { "1" });
      ContentValues cv = new ContentValues();
      cv.put(DBConfig.AppSnapshot.Column.AT, "-1");
      db.update(DBConfig.AppSnapshot.TABLE_NAME, cv,
          DBConfig.AppSnapshot.Column.AT + "!=? ", new String[] { "-1" });
      DBManager.getInstance(mContext).closeDB();
    } catch (Throwable e) {
    }
  }

  /**
   * 更新应用标识状态
   */
  public void update(String pkgName, String appTag) {
    try {
      SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
      ContentValues cv = new ContentValues();
      cv.put(DBConfig.AppSnapshot.Column.AT, appTag);
      db.update(DBConfig.AppSnapshot.TABLE_NAME, cv, DBConfig.AppSnapshot.Column.APN + "= ? ",
          new String[] { pkgName });
      DBManager.getInstance(mContext).closeDB();
    } catch (Throwable e) {
    }
  }

  public boolean isHasPkgName(String pkgName) {
    try {
      SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
      Cursor cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, new String[] { DBConfig.AppSnapshot.Column.APN },
          DBConfig.AppSnapshot.Column.APN + "=?", new String[] { pkgName }, null, null, null);
      if (cursor.getCount()== 0) {
        return false;
      } else {
        return true;
      }
    } catch (Throwable e) {
    }
    return false;
  }

  /**
   * 数据查询，格式：{JSONObject}
   */
  public JSONArray select() {
    JSONArray jar = null;
    try {
      SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
      jar = new JSONArray();
      Cursor cursor = db.query(DBConfig.AppSnapshot.TABLE_NAME, null,
          null, null, null, null, null);
      while (cursor.moveToNext()) {
        jar.put(getCursor(cursor));
      }
    } catch (Throwable e) {
      LL.e(e);
    }
    return jar;
  }
}