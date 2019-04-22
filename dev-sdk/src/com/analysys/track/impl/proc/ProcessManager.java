//package com.analysys.track.impl.proc;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import com.analysys.track.database.TableXXXInfo;
//import com.analysys.track.internal.Content.DeviceKeyContacts;
//import com.analysys.track.internal.Content.EGContext;
//import com.analysys.track.utils.ELOG;
//
//import com.analysys.track.utils.sp.SPHelper;
//
//import android.content.Context;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class ProcessManager {
//    // 是否开启工作
//    private volatile static boolean isCollected = true;
//
//    public static void setIsCollected(boolean isCollected) {
//        ProcessManager.isCollected = isCollected;
//    }
////    public static boolean getIsCollected(){
////        return isCollected;
////    }
//
////    public static Set<String> getRunningForegroundApps(Context ctx,JSONObject obj) {
////        Set<String> nameSet = null;
////        JSONArray uploadArray = null;
////        if (isCollected) {
////            ELOG.i("start===>" + isCollected);
////            try {
////                uploadArray = new JSONArray();
////
////                uploadArray.put(obj);
////
////                TableXXXInfo.getInstance(ctx).insert(uploadArray);
////                //获取正在运行的nameList
////                String[] strArray = null;
////                nameSet = new HashSet<String>();
////                JSONObject jsonObject = null;
////                for (int i = 0; i < uploadArray.length(); i++) {
////                    jsonObject = (JSONObject)uploadArray.get(i);
////                    String res = String.valueOf(jsonObject.get(ProcUtils.RUNNING_OC_RESULT)).replace("[", "").replace("]", "");
////                    strArray = res.split(",");
////                }
////                for (int i = 0; i < strArray.length; i++) {
////                    nameSet.add(strArray[i]);
////                }
////
////            } catch (Exception e) {
////                ELOG.i(e.getMessage() + "   getRunningForegroundApps()");
////            }
////        }
////        return nameSet;
////    }
//
////    public static void saveSP(Context ctx, JSONObject ocInfo){
////        String pkgName = ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationPackageName);
////        ELOG.i("重新写进去sp里的pkgName = "+pkgName);
////        SPHelper.setStringValue2SP(ctx,EGContext.LAST_PACKAGE_NAME,pkgName);
////        SPHelper.setStringValue2SP(ctx,EGContext.LAST_OPEN_TIME,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationOpenTime));
////        SPHelper.setStringValue2SP(ctx,EGContext.LAST_APP_NAME,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationName));
////        SPHelper.setStringValue2SP(ctx,EGContext.LAST_APP_VERSION,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationVersionCode));
////        SPHelper.setStringValue2SP(ctx,EGContext.APP_TYPE,ocInfo.optString(DeviceKeyContacts.OCInfo.ApplicationType));
////    }
//
//
//}
