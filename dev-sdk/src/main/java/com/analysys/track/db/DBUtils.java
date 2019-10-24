package com.analysys.track.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BuglyUtils;

public class DBUtils {

    /**
     * 数据库是否存在该表
     */
    public static boolean isTableExist(SQLiteDatabase db, String tableName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            final String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tableName
                    + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
}
