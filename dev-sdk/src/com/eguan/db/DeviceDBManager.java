package com.eguan.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;

/**
 * Created by chris on 16/11/4.
 */

public class DeviceDBManager {

    private static DeviceDatabaseHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    private DeviceDBManager() {
    }

    private static class Holder {
        private static final DeviceDBManager INSTANCE = new DeviceDBManager();
    }

    public static synchronized DeviceDBManager getInstance(Context context) {
        if (context == null) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e("DBManger参数传入Context为空");
            }
            return null;
        } else {
            mContext = context.getApplicationContext();
        }
        if (dbHelper == null) {
            dbHelper = DeviceDatabaseHelper.getInstance(mContext);
        }
        return Holder.INSTANCE;
    }

    public synchronized SQLiteDatabase openDB() {
        db = dbHelper.getWritableDatabase();
        return db;
    }

    public synchronized void closeDB() {
        try {
            if (db != null) {
                db.close();
            }
        } finally {
            db = null;
        }
    }
}
