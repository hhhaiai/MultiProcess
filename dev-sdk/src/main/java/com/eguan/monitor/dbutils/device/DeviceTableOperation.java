package com.eguan.monitor.dbutils.device;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.text.TextUtils;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.TimeUtils;
import com.eguan.monitor.dbutils.EGDBEncryptException;
import com.eguan.monitor.dbutils.EncryptUtils;
import com.eguan.monitor.imp.IUUInfo;
import com.eguan.monitor.imp.OCInfo;
import com.eguan.monitor.imp.OCTimeBean;
import com.eguan.monitor.imp.WBGInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeviceTableOperation {
    static Context mContext;

    private static class Holder {
        private static final DeviceTableOperation INSTANCE = new DeviceTableOperation();
    }

    private DeviceTableOperation() {
    }

    public static DeviceTableOperation getInstance(Context context) {
        if (context == null) {
            EgLog.e("context is null in TableOperation....");
            return null;
        }
        mContext = context;
        return Holder.INSTANCE;
    }

    private final String SP_CONTEXT = "eguan";
    private final String SP_FIRST_LAUNCH = "fl";

    /**
     * <pre>
     *    初始化检查:
     *       1.首次确认是否首次,首次只需要检查内存的加密key可用即可
     *       2.非首次启动,需要先确认内存数据可用,然后确认e_N1001(EGuanID所在库)中的预留字段epa的字段，是否可以解密成"eguan".
     *       3.如果解密失败，则清除表数据.确保后续可以工作
     *       4.工作完成后，将最新的加密"eguan"保存到数据库e_N001中
     *  <pre/>
     */
    public synchronized void initDB() {
        if (mContext == null) {
            return;
        }
        SharedPreferences pref = mContext.getSharedPreferences(Constants.SPUTIL, Context.MODE_PRIVATE);
        if (pref == null) {
            return;
        }
        long firstTime = pref.getLong(SP_FIRST_LAUNCH, -1);
        if (firstTime != -1) {
            //非首次和 EGuan ID表(e_N001)的预留字段epa比较
            Cursor cursor = null;
            try {
                SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
                if (db == null) return;
                DeviceDatabaseHelper.getInstance(mContext).create("e_N101");

                String sql = "select * from e_N101 ";
                cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    String conte = EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("epa")));
                    if (!TextUtils.isEmpty(conte)) {
                        //这次key解不了上次数据
                        if (!SP_CONTEXT.equals(conte)) {
                            clearAllData(db);
                            break;
                        }
                    }
                }
            } catch (Throwable e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                DeviceDBManager.getInstance(mContext).closeDB();
            }

        } else {
            //首次启动
            if (!EncryptUtils.checkEncryptKey(mContext)) {
                EncryptUtils.reInitKey(mContext);
            }
            long time = System.currentTimeMillis();
            pref.edit().putLong(SP_FIRST_LAUNCH, time).commit();
        }
        saveTestTextToDB();
    }

    /**
     * 清除数据库数据
     */
    private void clearAllData(SQLiteDatabase db) {
        try {
            DeviceDatabaseHelper.getInstance(mContext).createIfNotExit();

            String sql = "delete from e_N101 ";
            db.execSQL(sql);
            sql = "delete from e_N102";
            db.execSQL(sql);
            sql = "delete from e_N103";
            db.execSQL(sql);
            sql = "delete from e_N104";
            db.execSQL(sql);
            sql = "delete from e_N105";
            db.execSQL(sql);
            sql = "delete from e_N106";
            db.execSQL(sql);
            sql = "delete from e_N107";
            db.execSQL(sql);
            sql = "delete from e_N108";
            db.execSQL(sql);
            sql = "delete from e_N109";
            db.execSQL(sql);
        } catch (Throwable e) {
        }
    }

    /**
     * 每次启动检查后都将最新加密的"eguan"存入对应DB
     */
    private synchronized void saveTestTextToDB() {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            String insertSql = "insert into e_N101(epa) values (?)";
            db.execSQL(insertSql, new Object[]{EncryptUtils.getCheckID(mContext)});
        } catch (SQLiteDatabaseCorruptException e) {
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * Insert IUUInfo
     *
     * @param insertIUUInfo
     */
    public synchronized void insertIUUInfo(IUUInfo insertIUUInfo) {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N106");
            String sql = "insert into e_N106(aa,ab,ac,ad,ae,aab) values (?,?,?,?,?,?)";
            db.execSQL(sql, new Object[]{
                    EncryptUtils.encrypt(mContext, insertIUUInfo.getApplicationPackageName()),
                    EncryptUtils.encrypt(mContext, insertIUUInfo.getApplicationName()),
                    insertIUUInfo.getApplicationVersionCode(),
                    insertIUUInfo.getActionType(),
                    insertIUUInfo.getActionHappenTime(),
                    System.currentTimeMillis()});
        } catch (EGDBEncryptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }

        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * delete IUUInfo
     *
     * @param listInfo
     */
    public synchronized void deleteIUUInfo(List<IUUInfo> listInfo) {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            for (int i = 0; i < listInfo.size(); i++) {
                String sql = "delete from e_N106 where ae='" + listInfo.get(i).getActionHappenTime() + "'";
                db.execSQL(sql);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * select IUUInfo
     *
     * @return
     */
    public synchronized List<IUUInfo> selectIUUInfo() {
        List<IUUInfo> list = new ArrayList<IUUInfo>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return null;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N106");

            String sql = "select * from e_N106 where aab > " + TimeUtils.getDateBefore(new Date(), 15) + " and ae is not '' order by ae ASC";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                try {
                    IUUInfo info = new IUUInfo();
                    info.setApplicationPackageName(EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("aa"))));
                    info.setApplicationName(EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("ab"))));
                    info.setApplicationVersionCode(cursor.getString(cursor.getColumnIndex("ac")));
                    info.setActionType(cursor.getString(cursor.getColumnIndex("ad")));
                    info.setActionHappenTime(cursor.getString(cursor.getColumnIndex("ae")));
                    list.add(info);
                } catch (EGDBEncryptException e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null)
                cursor.close();
            DeviceDBManager.getInstance(mContext).closeDB();
        }
        return list;

    }

    /**
     * insert Install、UnInstall、Update data
     */
    public synchronized void insertOCInfo(List<OCInfo> insertOCInfo) {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N105");

            for (int i = 0; i < insertOCInfo.size(); i++) {
                try {
                    OCInfo ocInfo = insertOCInfo.get(i);
                    String startTime = ocInfo.getApplicationOpenTime();
                    String endTime = ocInfo.getApplicationCloseTime();
                    if (startTime != null && endTime != null && Long.valueOf(startTime) >= Long.valueOf(endTime)) {
                        continue;
                    }
                    String sql = "insert into e_N105(" +
                            "aa,ab,ac,ad,ae,aab,af,ag,ah,ai) values (?,?,?,?,?,?,?,?,?,?)";
                    db.execSQL(sql, new Object[]{ocInfo.getApplicationOpenTime(),
                            ocInfo.getApplicationCloseTime(),
                            EncryptUtils.encrypt(mContext, ocInfo.getApplicationPackageName()),
                            EncryptUtils.encrypt(mContext, ocInfo.getApplicationName()),
                            ocInfo.getApplicationVersionCode(),
                            System.currentTimeMillis(),
                            ocInfo.getNetwork(),
                            ocInfo.getSwitchType(),
                            ocInfo.getApplicationType(),
                            ocInfo.getCollectionType()});
                } catch (EGDBEncryptException e) {
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }


    /**
     * insert Install、UnInstall、Update data
     */
    public synchronized void insertOneOCInfo(OCInfo ocInfo) {
        try {
            String startTime = ocInfo.getApplicationOpenTime();
            String endTime = ocInfo.getApplicationCloseTime();
            if (startTime != null && endTime != null && Long.valueOf(startTime) >= Long.valueOf(endTime)) {
                return;
            }
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N105");

            String sql = "insert into e_N105(aa,ab,ac,ad," +
                    "ae,aab,af,ag,ah,ai) " +
                    "values (?,?,?,?,?,?,?,?,?,?)";
            db.execSQL(sql, new Object[]{ocInfo.getApplicationOpenTime(),
                    ocInfo.getApplicationCloseTime(),
                    EncryptUtils.encrypt(mContext, ocInfo.getApplicationPackageName()),
                    EncryptUtils.encrypt(mContext, ocInfo.getApplicationName()),
                    ocInfo.getApplicationVersionCode(),
                    System.currentTimeMillis(),
                    ocInfo.getNetwork(),
                    ocInfo.getSwitchType(),
                    ocInfo.getApplicationType(),
                    ocInfo.getCollectionType()});
        } catch (EGDBEncryptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }

    }


    /**
     * delete Install、UnInstall、Update data
     */
    public synchronized void deleteOCInfo(List<OCInfo> listInfo) {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N105");

            for (int j = 0; j < listInfo.size(); j++) {
                String sql = "delete from e_N105 where ab='" + listInfo.get(j).getApplicationCloseTime() + "'";
                db.execSQL(sql);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * select Install、UnInstall、Update data
     */
    public synchronized List<OCInfo> selectOCInfo() {
        List<OCInfo> list = new ArrayList<OCInfo>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return null;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N105");

            String sql = "select * from e_N105 where aab > " + TimeUtils.getDateBefore(new Date(), 15)
                    + " and aa is not '' " + " and ab is not '' order by aa ASC";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {

                try {
                    OCInfo info = new OCInfo();
                    info.setApplicationOpenTime(cursor.getString(cursor.getColumnIndex("aa")));
                    info.setApplicationCloseTime(cursor.getString(cursor.getColumnIndex("ab")));

                    info.setApplicationPackageName(EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("ac"))));
                    info.setApplicationName(EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("ad"))));

                    info.setApplicationVersionCode(cursor.getString(cursor.getColumnIndex("ae")));
                    info.setNetwork(cursor.getString(cursor.getColumnIndex("af")));
                    info.setSwitchType(cursor.getString(cursor.getColumnIndex("ag")));
                    info.setApplicationType(cursor.getString(cursor.getColumnIndex("ah")));
                    info.setCollectionType(cursor.getString(cursor.getColumnIndex("ai")));
                    list.add(info);
                } catch (EGDBEncryptException e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null)
                cursor.close();
            DeviceDBManager.getInstance(mContext).closeDB();
        }
        return list;

    }

    /**
     * 查看OC数据
     *
     * @param CollectionType
     * @return
     */
    public synchronized List<OCInfo> selectOCInfo(String CollectionType) {
        List<OCInfo> list = new ArrayList<OCInfo>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return null;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N105");

            String sql = "select * from e_N105 where ai = '" + CollectionType + "' and aab > " + TimeUtils.getDateBefore(new Date(), 15)
                    + " and aa is not '' " + " and ab is not '' order by aa ASC";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                try {
                    OCInfo info = new OCInfo();
                    info.setApplicationOpenTime(cursor.getString(cursor.getColumnIndex("aa")));
                    info.setApplicationCloseTime(cursor.getString(cursor.getColumnIndex("ab")));

                    info.setApplicationPackageName(EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("ac"))));
                    info.setApplicationName(EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("ad"))));

                    info.setApplicationVersionCode(cursor.getString(cursor.getColumnIndex("ae")));
                    info.setNetwork(cursor.getString(cursor.getColumnIndex("af")));
                    info.setSwitchType(cursor.getString(cursor.getColumnIndex("ag")));
                    info.setApplicationType(cursor.getString(cursor.getColumnIndex("ah")));
                    info.setCollectionType(cursor.getString(cursor.getColumnIndex("ai")));
                    list.add(info);
                } catch (EGDBEncryptException e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null)
                cursor.close();
            DeviceDBManager.getInstance(mContext).closeDB();
        }
        return list;
    }

    /**
     * OCInfo number
     *
     * @return
     */
    public synchronized int DataQuantity() {
        int numb = 0;
        SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
        if (db == null) return numb;
        Cursor cursor = null;
        try {
            DeviceDatabaseHelper.getInstance(mContext).create("e_N105");

            String sql = "select * from e_N105";
            cursor = db.rawQuery(sql, null);
            numb = cursor.getCount();

        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DeviceDBManager.getInstance(mContext).closeDB();
        }
        return numb;
    }

    /**
     * insert baseStaion
     *
     * @param info
     */
    public synchronized void insertWBGInfo(WBGInfo info) {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N108");

            String sql = "insert into e_N108(aa,ab,ac,ad,ae,af,ag,ah) values (?,?,?,?,?,?,?,?)";
            db.execSQL(sql, new Object[]{
                    EncryptUtils.encrypt(mContext, info.getSSID()),
                    EncryptUtils.encrypt(mContext, info.getBSSID()),
                    info.getLevel(),
                    info.getLocationAreaCode(),
                    info.getCellId(),
                    info.getCollectionTime(),
                    info.getGeographyLocation(),
                    info.getIp()});
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    /**
     * select BaseStation
     */
    public synchronized List<WBGInfo> selectWBGInfo() {
        List<WBGInfo> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return list;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N108");

            String sql = "select * from e_N108";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                try {
                    WBGInfo info = new WBGInfo();
                    info.setSSID(EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("aa"))));
                    info.setBSSID(EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("ab"))));
                    info.setLevel(cursor.getString(cursor.getColumnIndex("ac")));
                    info.setLocationAreaCode(cursor.getString(cursor.getColumnIndex("ad")));
                    info.setCellId(cursor.getString(cursor.getColumnIndex("ae")));
                    info.setCollectionTime(cursor.getString(cursor.getColumnIndex("af")));
                    info.setGeographyLocation(cursor.getString(cursor.getColumnIndex("ag")));
                    info.setIp(cursor.getString(cursor.getColumnIndex("ah")));
                    list.add(info);
                } catch (EGDBEncryptException e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }

        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DeviceDBManager.getInstance(mContext).closeDB();
        }
        return list;
    }

    /**
     * delete BaseStation
     */
    public synchronized void deleteWBGInfo() {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N108");

            String sql = "delete from e_N108"; // 清空数据
            db.execSQL(sql);
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }

    }

    public synchronized void updateOcInfo(long closeTime) {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N105");

            String sql = "update e_N105 set ab = " + closeTime + " where ab='';";
            db.execSQL(sql);
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }


    /**
     * eguan id 存储
     *
     * @param id
     */
    public synchronized void insertEguanId(String id) {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            DeviceDatabaseHelper.getInstance(mContext).create("e_N101");

            String deleteSql = "delete from e_N101";
            String insertSql = "insert into e_N101(aa) values (?)";
            db.execSQL(deleteSql);
            db.execSQL(insertSql, new Object[]{EncryptUtils.encrypt(mContext, id)});
            EgLog.e("------ 数据存储成功 ------");
        } catch (EGDBEncryptException e) {
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
        saveTestTextToDB();
    }

    public synchronized void insertTmpId(String id) {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            DeviceDatabaseHelper.getInstance(mContext).create("e_N102");

            String deleteSql = "delete from e_N102";
            String insertSql = "insert into e_N102(aa) values (?)";
            db.execSQL(deleteSql);
            db.execSQL(insertSql, new Object[]{EncryptUtils.encrypt(mContext, id)});
            EgLog.e("------ 数据存储成功 ------");
        } catch (EGDBEncryptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }

    }

    public synchronized String selectEguanId() {
        String eguanId = "";
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) {
                return "";
            }
            DeviceDatabaseHelper.getInstance(mContext).create("e_N101");

            String sql = "select aa from e_N101 ";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                try {
                    eguanId = EncryptUtils.decrypt(mContext, cursor.getString(0));
                } catch (EGDBEncryptException e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DeviceDBManager.getInstance(mContext).closeDB();
        }

        return eguanId;
    }

    public synchronized String selectTmpId() {
        String eguanId = "";
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) {
                return "";
            }
            DeviceDatabaseHelper.getInstance(mContext).create("e_N102");

            String sql = "select aa from e_N102 ";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                try {
                    eguanId = EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndex("aa")));
                } catch (EGDBEncryptException e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DeviceDBManager.getInstance(mContext).closeDB();
        }

        return eguanId;
    }

    public synchronized void deleteDeviceAllInfo() {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N106");
            DeviceDatabaseHelper.getInstance(mContext).create("e_N105");
            DeviceDatabaseHelper.getInstance(mContext).create("e_N108");

            String IUUInfo = "delete from e_N106;";
            String OCInfo = "delete from e_N105;";
            String WBGInfo = "delete from e_N108;";
            String[] sql = {IUUInfo, OCInfo, WBGInfo};
            for (int i = 0; i < sql.length; i++) {
                db.execSQL(sql[i]);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    public synchronized void insertOCTimes(List<OCTimeBean> beans) {
        Cursor cursor = null;
        try {
            if (mContext == null) return;
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N103");
            for (OCTimeBean bean : beans) {
                try {
                    ContentValues cv = new ContentValues();
                    cv.put("aa", EncryptUtils.encrypt(mContext, bean.packageName));
                    cv.put("ab", bean.timeInterval);
                    cv.put("ac", bean.count);
                    cv.put("aab", System.currentTimeMillis());
                    cursor = db.query("e_N103", null, "aa = ? and ab = ?", new String[]{
                            EncryptUtils.encrypt(mContext, bean.packageName), bean.timeInterval}, null, null, null, "1");
                    if (cursor.moveToNext()) {
                        cv.put("ac", cursor.getInt(cursor.getColumnIndexOrThrow("ac")) + 1);
                        db.update("e_N103", cv, "aa = ? and ab = ?",
                                new String[]{EncryptUtils.encrypt(mContext, bean.packageName), bean.timeInterval});
                    } else {
                        db.insert("e_N103", null, cv);
                    }

                } catch (EGDBEncryptException e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    public synchronized void deleteOCTimeTable() {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N103");
            db.delete("e_N103", null, null);
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    public synchronized List<OCTimeBean> selectOCTimes() {
        ArrayList<OCTimeBean> ocTimeBeans = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return ocTimeBeans;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N103");
            cursor = db.query("e_N103", null, null, null, null, null, null);
            OCTimeBean bean;
            while (cursor.moveToNext()) {
                try {
                    bean = new OCTimeBean();
                    bean.packageName = EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndexOrThrow("aa")));
                    bean.timeInterval = cursor.getString(cursor.getColumnIndexOrThrow("ab"));
                    bean.count = cursor.getInt(cursor.getColumnIndexOrThrow("ac"));
                    ocTimeBeans.add(bean);
                } catch (EGDBEncryptException e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DeviceDBManager.getInstance(mContext).closeDB();
            return ocTimeBeans;
        }
    }

    //新增对ProcTemp表的CRUD操作方法
    //新增对packageName的缓存
    private List<String> packageContainer = new ArrayList();

    public synchronized void addProcTemp(String packageName, String openTime) {
        try {
            if (packageContainer.contains(packageName)) {
                return;
            } else {
                packageContainer.add(packageName);
                SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
                if (db == null) return;
                DeviceDatabaseHelper.getInstance(mContext).create("e_N104");

                ContentValues cv = new ContentValues();
                if (packageName == null || openTime == null) return;
                cv.put("aa", EncryptUtils.encrypt(mContext, packageName));
                cv.put("ab", openTime);
                db.insert("e_N104", null, cv);
            }
        } catch (EGDBEncryptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    public synchronized void deleteProTemp() {
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N104");

            db.delete("e_N104", null, null);
            packageContainer.clear();
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    public synchronized void deleteProcTemp(String packageName) {
        try {
            if (packageContainer.contains(packageName)) {
                SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
                if (db == null) return;
                DeviceDatabaseHelper.getInstance(mContext).create("e_N104");

                db.delete("e_N104", "aa=?", new String[]{EncryptUtils.encrypt(mContext, packageName)});
                DeviceDBManager.getInstance(mContext).closeDB();
                packageContainer.remove(packageName);
            } else {
                //不执行
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            DeviceDBManager.getInstance(mContext).closeDB();
        }
    }

    public synchronized Map<String, String> queryProcTemp() {
        Map<String, String> result = new HashMap<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = DeviceDBManager.getInstance(mContext).openDB();
            if (db == null) return null;
            DeviceDatabaseHelper.getInstance(mContext).create("e_N104");

            cursor = db.query("e_N104", null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                try {
                    String packageName = EncryptUtils.decrypt(mContext, cursor.getString(cursor.getColumnIndexOrThrow("aa")));
                    String openTieme = cursor.getString(cursor.getColumnIndexOrThrow("ab"));
                    result.put(packageName, openTieme);
                } catch (EGDBEncryptException e) {
                }
            }
        } catch (SQLiteDatabaseCorruptException e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            if (mContext != null) {
                DeviceDatabaseHelper.getInstance(mContext).rebuildDB();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DeviceDBManager.getInstance(mContext).closeDB();
            return result;
        }
    }


}
