package com.analysys.dev.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.utils.Utils;
import com.analysys.dev.internal.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/30 16:06
 * @Author: Wang-X-C
 */
public class TableOC {
    Context mContext;

    private static class Holder {
        private static final TableOC INSTANCE = new TableOC();
    }

    public static TableOC getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    public void insert(List<JSONObject> ocInfo) {

        SQLiteDatabase db = null;
        try {
            db = DBManager.getInstance(mContext).openDB();
            db.beginTransaction();
            long time = System.currentTimeMillis();
            for (int i = 0; i < ocInfo.size(); i++) {
                LL.i("OC存储内容：" + ocInfo);
                String encryptOC = Utils.encrypt(String.valueOf(ocInfo.get(i)), time);
                if (!TextUtils.isEmpty(encryptOC)) {
                    ContentValues cv = new ContentValues();
                    cv.put(DBConfig.OC.Column.OCI, encryptOC);
                    cv.put(DBConfig.OC.Column.IT, time);
                    cv.put(DBConfig.OC.Column.ST, "0");
                    db.insert(DBConfig.OC.TABLE_NAME, null, cv);
                }
            }
            db.setTransactionSuccessful();
        } catch (Throwable e) {
            LL.e(e);
        } finally {
            db.endTransaction();
        }
        DBManager.getInstance(mContext).closeDB();
    }

    public JSONArray select() {
        JSONArray jar = null;
        try {
            jar = new JSONArray();
            SQLiteDatabase db = DBManager.getInstance(mContext).openDB();
            Cursor cursor = db.query(DBConfig.OC.TABLE_NAME,
                    new String[]{DBConfig.OC.Column.OCI, DBConfig.OC.Column.IT},
                    null, null, null,
                    null, null, null);
            while (cursor.moveToNext()) {
                String insertTime = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.IT));
                String encryptInfo = cursor.getString(cursor.getColumnIndex(DBConfig.OC.Column.OCI));
                String ocInfo = Utils.decrypt(encryptInfo, Long.valueOf(insertTime));
                if (!TextUtils.isEmpty(ocInfo)) {
                    jar.put(new JSONObject(ocInfo));
                }
            }
        } catch (Throwable e) {
            LL.e(e);
        }
        DBManager.getInstance(mContext).closeDB();
        return jar;
    }
}
