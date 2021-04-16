package cn.analysys.casedemo.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.analysys.track.db.DBConfig;
import com.analysys.track.db.DBManager;
import com.analysys.track.db.DBUtils;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.ftime.LmFileImpl;
import com.analysys.track.internal.impl.ftime.LmFileUitls;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MDate;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.ProcessUtils;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.pkg.PkgList;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import me.hhhaiai.testcaselib.CaseHelper;
import me.hhhaiai.testcaselib.utils.L;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 所有SDK的引用全部放该类
 * @Version: 1.0
 * @Create: 2021/03/67 11:26:31
 * @author: sanbo
 */
public class SDKHelper {

    @TargetApi(23)
    public static void reqPermission(@NonNull Activity activity, @NonNull String[] permissionList, int requestCode) {
        PermissionUtils.reqPermission(activity, permissionList, requestCode);
    }

    public static ConcurrentHashMap<String, Long> getFileAndCacheTime() {
        ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
        List<LmFileUitls.AppTime> ats = LmFileUitls.getLastAliveTimeByPkgName(getContext(), false);
        if (ats.size() > 0) {
            for (LmFileUitls.AppTime at : ats) {
                String pkg = at.getPackageName();
                long time = at.getLastActiveTime();
                map.put(pkg, time);
            }
        }
        return map;
    }

    public static ConcurrentHashMap<String, Long> getSDDirTime(boolean isClearMemoryData) {
        if (isClearMemoryData) {
            SDKHelper.clearMemoryPkgList();
        }
        ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
        List<LmFileUitls.AppTime> ats = LmFileUitls.getLastAliveTimeByPkgName(getContext(), true);
        if (ats.size() > 0) {
            for (LmFileUitls.AppTime at : ats) {
                String pkg = at.getPackageName();
                long time = at.getLastActiveTime();
                map.put(pkg, time);
            }
        }
        return map;
    }

    public static void runOnWorkThread(Runnable runnable) {
        EThreadPool.runOnWorkThread(runnable);
    }

    /**
     * 获取末次活跃的列表
     *
     * @return
     */
    public static List<String> getLastAliveTimeStr() {
        List<String> result = new CopyOnWriteArrayList<String>();
        List<LmFileUitls.AppTime> ats = LmFileUitls.getLastAliveTimeByPkgName(getContext(), false);
        ;
        if (ats.size() > 0) {
            for (LmFileUitls.AppTime at : ats) {
                result.add(at.toString());
            }
        }
        return result;
    }


    /**
     * 获取可用的context
     *
     * @return
     */
    public static Context getContext() {
        return getContext(CaseHelper.getCaseContext());
    }

    public static Context getContext(Context context) {
        return EContextHelper.getContext(context);
    }

    /**
     * 调用系统shell
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {
        return ShellUtils.shell(cmd);
    }

    /**
     * 获取安卓ID
     *
     * @return
     */
    public static String getAndroidID() {
        return DeviceImpl.getInstance(getContext()).getValueFromSettingSystem(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    /**
     * 获取安装app数量
     *
     * @return
     */
    public static int getInstallAppSize() {
        return PkgList.getInstance(getContext()).getMemoryDataSize();
    }

    public static void getInstallAppSizeByApi() {
        PkgList.getInstance(getContext()).getByApi();
    }

    public static void getInstallAppSizeByShell() {
        PkgList.getInstance(getContext()).getByShell();
    }

    public static void getInstallAppSizeByUid() {
        PkgList.getInstance(getContext()).getByUid();
    }

    public static List<String> getPkgList() {
        return PkgList.getInstance(getContext()).getAppPackageList();
    }

    public static void clearMemoryPkgList() {
        PkgList.getInstance(getContext()).clearMemoryData();
    }

    /**
     * 判断是否两个类是否是有祖、父类关系
     *
     * @param subClass
     * @param fatherClass
     * @return
     */
    public static boolean isSubClass(Class<?> subClass, Class<?> fatherClass) {
        return AndroidManifestHelper.isSubClass(subClass, fatherClass);
    }

    public static void logi(String info) {
        //dev-sdk build.gradle中必须设置release=false&logcat=true
        ELOG.i(info);
    }

    public static void prepareDB() {
        DBManager.getInstance(getContext()).openDB();
        DBManager.getInstance(getContext()).closeDB();
    }

    public static boolean checkAppsnapshotDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.AppSnapshot.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }

        return false;
    }

    public static boolean checkLocationDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.Location.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean checkFinfoDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.FInfo.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

//    public static boolean checkXxxDB() {
//        try {
//            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.XXXInfo.TABLE_NAME)) {
//                return true;
//            }
//        } catch (Throwable e) {
//        }
//        return false;
//    }

    public static boolean checkScanDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.ScanningInfo.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean checkOCDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.OC.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean checkNetDB() {
        try {
            if (DBUtils.isTableExist(DBManager.getInstance(getContext()).openDB(), DBConfig.NetInfo.TABLE_NAME)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static void realGetFlt() {
        LmFileImpl.getInstance(getContext()).realGetFlt();
    }

    public static void tryGetFileTime() {
        LmFileImpl.getInstance(getContext()).tryGetFileTime(new ECallBack() {
            @Override
            public void onProcessed() {
                L.d("来源于回调函数: [" + SDKHelper.getProcessName() + "] 进程处理完毕");
            }
        });
    }

    public static Map<String, Long> getMemDataForTest() {
        return LmFileImpl.getInstance(getContext()).getMemDataForTest();
    }

    public static JSONObject getJson(PackageManager pm, String pkg, Long value) {
        return LmFileImpl.getInstance(getContext()).getAppInfo(pm, pkg, value);
    }


    public static long getLmfByPkg(String pkg) {
        String path = "/sdcard/Android/data/" + pkg;
        return LmFileUitls.getDirsRealActiveTime(new File(path), true);
    }

    public static boolean isToday(long lastActiveTime) {
        return MDate.isToday(lastActiveTime);
    }

    public static String getDateFromTimestamp(long lastActiveTime) {
        return MDate.getDateFromTimestamp(lastActiveTime);
    }

    public static String getToday() {
        return MDate.getToday();
    }

    public static String convertLongTimeToHms(long time) {
        return MDate.convertLongTimeToHms(time);
    }

    /**
     * 发送消息调用单独模快
     *
     * @param what
     */
    public static void postMsgToDispatcher(int what) {
        MessageDispatcher.getInstance(getContext()).postDelay(what, 0);
    }

    public static void postLastModifyTimeToDispatcher() {
        int what = (int) ClazzUtils.getStaticFieldValue(MessageDispatcher.class, "MSG_INFO_LASTFILETIME");
        postMsgToDispatcher(what);
    }

    public static String getMoreImeis() {
        return DoubleCardSupport.getInstance().getIMEIS(getContext());
    }

    public static String getMoreImsis() {
        return DoubleCardSupport.getInstance().getIMSIS(getContext());
    }

    public static String getProcessName() {
        return ProcessUtils.getCurrentProcessName(getContext());
    }
}
