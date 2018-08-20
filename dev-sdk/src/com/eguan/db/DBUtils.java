package com.eguan.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;

import java.io.File;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 数据相关的工具类
 * @Version: 1.0
 * @Create: 18/2/3 14:42
 * @Author: sanbo
 */
public class DBUtils {

    /**
     * 数据库是否存在该表
     *
     * @param tabName
     * @param db
     * @return
     */
    public static boolean isTableExist(String tabName, SQLiteDatabase db) {
        boolean result = false;
        if (tabName == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public static void deleteDBFile(String filePath) {
        File result = new File(filePath);
        if (result != null) {
            if (result.exists()) {
                result.delete();
            }
        }
    }
}
