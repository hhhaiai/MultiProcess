package com.analysys.track.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 保证同一时间间隔内只操作一次数据库，防止重复操作
     *
     * @param ctx
     * @param fileName
     * @return
     */
//  public static boolean isValidData(Context ctx,String fileName){
//    boolean isValid = false;
//    long time = System.currentTimeMillis();
//    if(EGContext.FILES_SYNC_APPSNAPSHOT.equals(fileName)){
//      if(time - MultiProcessChecker.getLockFileLastModifyTime(ctx,fileName) > EGContext.SNAPSHOT_CYCLE){
//          isValid = true;
//      }
//    }else if(EGContext.FILES_SYNC_LOCATION.equals(fileName)){
//      if(time - MultiProcessChecker.getLockFileLastModifyTime(ctx,fileName) > EGContext.LOCATION_CYCLE){
//        isValid = true;
//      }
//    }else if(EGContext.FILES_SYNC_OC.equals(fileName)){
//      if((time - MultiProcessChecker.getLockFileLastModifyTime(ctx,fileName) > EGContext.LOCATION_CYCLE)||SystemUtils.isScreenLocked(ctx)){
//        isValid = true;
//      }
//    }
//  return isValid;
//  }

}
