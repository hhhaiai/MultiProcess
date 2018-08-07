//package com.eguan.monitor.manager;
//
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//
//import com.eguan.monitor.commonutils.SPUtil;
//
///**
// * Created on 17/2/24.
// * Author : chris
// * Email  : mengqi@analysys.com.cn
// * Detail : Application Update Info
// */
//
//public class AUInfoManager {
//
//    private static final String SETTING_KEY = "APP_VERSION_CODE";
//
//    /**
//     * 向Setting中存储数据
//     *
//     * @param appVersionCode
//     */
//    private static void writeVersionCode(Context mContext, int appVersionCode) {
//        SPUtil.getInstance(mContext).setAppUpdate(appVersionCode);
//    }
//
//    /**
//     * 从Setting中读取数据
//     *
//     * @return
//     */
//    private static int readVersionCode(Context mContext) {
//        return SPUtil.getInstance(mContext).getAppUpdate();
//    }
//
//    public static boolean isUpdateAppVersion(Context mContext){
//        int oldVersionCode = readVersionCode(mContext);
//        PackageManager packageManager = mContext.getPackageManager();
//        PackageInfo packInfo = null;
//        try {
//            packInfo = packageManager.getPackageInfo(mContext.getPackageName(),0);
//            writeVersionCode(mContext,packInfo.versionCode);
//            if (oldVersionCode == 0) {
//                return false;
//            }
//            if (packInfo.versionCode > oldVersionCode){
//                return true;
//            }
//        } catch (Throwable e) {
//            return false;
//        }
//        return false;
//    }
//
//}
