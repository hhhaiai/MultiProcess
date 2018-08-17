package com.eguan.monitor.dbutils;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by chris on 16/9/29.
 */

public class DBContext extends ContextWrapper {

    public DBContext(Context context) {
        super(context);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openDatabase(getDatabasePath(name).getAbsolutePath(), factory,
                SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.OPEN_READWRITE
                        | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return SQLiteDatabase.openDatabase(getDatabasePath(name).getAbsolutePath(), factory,
                SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.OPEN_READWRITE
                        | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }
}
