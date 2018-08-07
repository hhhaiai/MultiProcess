package com.eguan.monitor.imp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.commonutils.SystemUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用列表信息管理器
 */
public class InstalledAPPInfoManager {


    private static final int APP_MIXSUM = 5;


    /**
     * 获取设备所有应用列表信息(系统与三方应用,包含启动页面的)
     * 1、先通过系统API(getInstalledPackages)获取(size必须>=5)
     * 2、API获取不到通过pm 命令pm list packages 获取
     * 3、如果API与pm方式都获取不到己安装列表的话,则返回一个空集合
     *
     * @param context
     * @return
     */
    public static List<InstalledAppInfo> getAllApps(Context context) {
        List<InstalledAppInfo> appList;
        appList = getAppsFromAPI(context);
        if (appList.size() < APP_MIXSUM) {
            appList = getAppsFromShell(context);
        }
        return appList;
    }

    private static List<InstalledAppInfo> getAppsFromAPI(Context context) {
        List<InstalledAppInfo> appList = new ArrayList<InstalledAppInfo>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            try {
                PackageInfo packageInfo = packages.get(i);
                String packageName = packageInfo.packageName;
                if (!TextUtils.isEmpty(packageName) && pm.getLaunchIntentForPackage(packageName) !=
                        null) {
                    InstalledAppInfo appInfo = new InstalledAppInfo();
                    String appName = packageInfo.applicationInfo.loadLabel(
                            context.getPackageManager()).toString();
                    String versionName = TextUtils.isEmpty(packageInfo.versionName) ? "1.0" :
                            packageInfo.versionName;
                    String versionCode = String.valueOf(packageInfo.versionCode);

                    appInfo.setApplicationPackageName(packageName);
                    appInfo.setApplicationName(appName);
                    appInfo.setApplicationVersionCode(versionName + "|" + versionCode);

                    appList.add(appInfo);
                }
            } catch (Throwable e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            }
        }
        return appList;
    }

    private final static String SHELL_PM_LIST_PACKAGES = "pm list packages";


    private static List<InstalledAppInfo> getAppsFromShell(Context context) {
        PackageManager pm = context.getPackageManager();
        List<InstalledAppInfo> appList = new ArrayList<InstalledAppInfo>();
        InstalledAppInfo info;
        String result = SystemUtils.shell(SHELL_PM_LIST_PACKAGES);
        if (!TextUtils.isEmpty(result) && result.contains("\n")) {
            String[] lines = result.split("\n");
            if (lines.length > 0) {
                for (int i = 0; i < lines.length; i++) {
                    try {
                        String[] split = lines[i].split(":");
                        if (split.length >= 1) {
                            String packageName = split[1];
                            if (!TextUtils.isEmpty(packageName) && pm.getLaunchIntentForPackage
                                    (packageName)
                                    != null) {
                                info = new InstalledAppInfo();
                                info.setApplicationName("");
                                info.setApplicationPackageName(packageName);
                                info.setApplicationVersionCode("");
                                appList.add(info);
                            }
                        }
                    } catch (Throwable e) {
                        if (Constants.FLAG_DEBUG_INNER) {
                            EgLog.e(e);
                        }
                    }
                }
            }
        }
        return appList;
    }

    /**
     * 集合转为JSON串,用于本地缓存
     *
     * @param list
     * @return
     */
    public String getAppInfoToJson(List<InstalledAppInfo> list) {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                InstalledAppInfo appInfo = list.get(i);
                jsonObject.put("APN", appInfo.getApplicationPackageName());
                jsonObject.put("AN", appInfo.getApplicationName());
                jsonObject.put("AVC", appInfo.getApplicationVersionCode());
                jsonObject.put("IN", appInfo.getIsNew());
                arr.put(jsonObject);
            } catch (JSONException e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            }
        }
        return arr.toString();
    }


    /**
     * 将JSON字符串转为List集合
     *
     * @param json
     * @return
     */
    public List<InstalledAppInfo> jsonToList(String json) {
        List<InstalledAppInfo> list = new ArrayList<InstalledAppInfo>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                InstalledAppInfo appInfo = new InstalledAppInfo();
                appInfo.setApplicationPackageName(object.optString("APN"));
                appInfo.setApplicationName(object.optString("AN"));
                appInfo.setApplicationVersionCode(object.optString("AVC"));
                appInfo.setIsNew("IN");
                list.add(appInfo);
            }

        } catch (JSONException e) {
            return list;
        }
        return list;
    }

    /**
     * 获取应用列表上传列表信息
     *
     * @return
     */
    public List<InstalledAppInfo> getPostAppInfoData(Context context) {

        List<InstalledAppInfo> list = null;
        SPUtil spUtil = SPUtil.getInstance(context);
        long time = spUtil.getAppList();
        if (time <= System.currentTimeMillis()) {
            spUtil.setAppList(System.currentTimeMillis() + Constants.GETAPPLIST);
            list = getAllApps(context);
            int size = list.size();
            for (int i = 0; i < size; i++) {
                list.get(i).setIsNew("3");
            }
        }
        return list;

    }

}
