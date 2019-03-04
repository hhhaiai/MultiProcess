package com.analysys.track.database;

import com.analysys.track.utils.reflectinon.EContextHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

    private static Context mContext = null;
    private static DBHelper dbHelper = null;
    private SQLiteDatabase db = null;

    public DBManager() {}

    private static class Holder {
        private static final DBManager INSTANCE = new DBManager();
    }

    public static synchronized DBManager getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        if (dbHelper == null) {
            dbHelper = DBHelper.getInstance(mContext);
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
