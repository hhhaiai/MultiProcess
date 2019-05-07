package com.analysys.track.database;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.reflectinon.EContextHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

public class DBManager {

    private static Context mContext = null;
    private static DBHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private AtomicInteger mOpenWriteCounter = new AtomicInteger();

    public DBManager() {}

    private static class Holder {
        private static final DBManager INSTANCE = new DBManager();
    }

    public static synchronized DBManager getInstance(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
        if (dbHelper == null) {
            dbHelper = DBHelper.getInstance(mContext);
        }
        return Holder.INSTANCE;
    }

    public synchronized SQLiteDatabase openDB() {
        try {
            if (mOpenWriteCounter.incrementAndGet() == 1) {
                // Opening new database
                db = dbHelper.getWritableDatabase();
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
        return db;
    }

    public synchronized void closeDB() {
        try {
            if (mOpenWriteCounter.decrementAndGet() == 0) {
                // Closing database
                db.close();
            }

        } catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }

        }
//        finally {
//            db = null;
//        }
    }
}
