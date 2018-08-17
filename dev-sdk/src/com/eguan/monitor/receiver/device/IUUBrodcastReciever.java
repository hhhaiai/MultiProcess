package com.eguan.monitor.receiver.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.dbutils.device.DeviceTableOperation;
import com.eguan.monitor.imp.IUUInfo;
import com.eguan.monitor.imp.InstalledAPPInfoManager;
import com.eguan.monitor.imp.InstalledAppInfo;
import com.eguan.monitor.thread.EGQueue;
import com.eguan.monitor.thread.SafeRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 应用安装、卸载、更新广播接收器，获取应用相关信息，并做入库缓存处理
 */
public class IUUBrodcastReciever extends BroadcastReceiver {

    private String flag = "";// 标记：0表示新装，1表示卸载，2表示更新
    private PackageManager pm;

    boolean boo = true;
    List<IUUInfo> lexList = new ArrayList<IUUInfo>();
    boolean aBoolean = true;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                if (android.os.Build.BRAND.equals("LeEco") && android.os.Build.MODEL.equals("LEX720")
                        && android.os.Build.VERSION.RELEASE.equals("6.0.1") && android.os.Build.VERSION.SDK_INT == 23) {
                    aBoolean = false;
                }

                process(context, intent);
                LeEcoCustomized(context);

            }
        });
    }

    private void LeEcoCustomized(final Context context) {

        if (aBoolean)
            return;
        if (!boo)
            return;
        boo = false;
        try {
            Thread.sleep(1000);
            filteringData(context);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    private void filteringData(Context context) {
        if (lexList.size() >= 2) {
            Collections.sort(lexList, new Comparator<IUUInfo>() {

                @Override
                public int compare(IUUInfo o1, IUUInfo o2) {
                    if (Long.parseLong(o1.getActionHappenTime()) < Long.parseLong(o2.getActionHappenTime())) {
                        return -1;
                    }
                    return 1;
                }
            });
        }

        if (lexList.size() == 1) {
            saveIuuinfo(context, lexList.get(0));
        } else if (lexList.size() == 2) {
            if (lexList.get(0).getActionType().equals("1") && lexList.get(1).getActionType().equals("0")) {
                saveIuuinfo(context, lexList.get(1));
            }
        } else if (lexList.size() == 3) {
            if (lexList.get(0).getActionType().equals("0") && lexList.get(1).getActionType().equals("1")
                    && lexList.get(2).getActionType().equals("2")) {
                saveIuuinfo(context, lexList.get(2));
            } else if (lexList.get(0).getActionType().equals("1") && lexList.get(1).getActionType().equals("0")
                    && lexList.get(2).getActionType().equals("2")) {
                List<InstalledAppInfo> list4 = InstalledAPPInfoManager.getAllApps(context);
                boolean bool = true;
                for (int i = 0; i < list4.size(); i++) {
                    if (list4.get(i).getApplicationPackageName().equals(lexList.get(2).getApplicationPackageName())) {
                        saveIuuinfo(context, lexList.get(2));
                        bool = false;
                        break;
                    }
                }
                if (bool) {
                    saveIuuinfo(context, lexList.get(0));
                }
            }
        }
        lexList.clear();
        boo = true;
    }

    private void process(Context context, Intent intent) {
        try {
            pm = context.getPackageManager();
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                long time = System.currentTimeMillis();
                flag = "0";
                IUUInfo info = new IUUInfo();
                String packageName = intent.getDataString().substring(8);// 新安装包名
                info.setApplicationPackageName(packageName);
                try {
                    info.setApplicationName(getProgramNameByPackageName(context, packageName));
                    if (pm.getPackageInfo(packageName, 0).versionName != null) {
                        info.setApplicationVersionCode(pm.getPackageInfo(packageName, 0).versionName
                                + pm.getPackageInfo(packageName, 0).versionCode);// 应用程序版本号
                    } else {
                        info.setApplicationVersionCode("");
                    }
                } catch (Exception e) {
                    info.setApplicationVersionCode("");
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
                info.setActionType(flag);
                info.setActionHappenTime(String.valueOf(time));// 程序安装时间
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.d(info.toString());
                }
                /** ---------------------保存新装应用信息------------------ **/
                lexList.add(info);
                if (aBoolean) {
                    saveIuuinfo(context, info);
                }
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                long time = System.currentTimeMillis();
                flag = "1";
                IUUInfo info = null;
                String packageName = intent.getDataString().substring(8);// 卸载程序包名
                InstalledAPPInfoManager manager = new InstalledAPPInfoManager();
                SPUtil spUtil = SPUtil.getInstance(context);
                List<InstalledAppInfo> list = manager.jsonToList(spUtil.getAllAppForUninstall());
                for (int i = 0; i < list.size(); i++) {
                    if (packageName.equalsIgnoreCase(list.get(i).getApplicationPackageName())) {
                        info = new IUUInfo();
                        info.setApplicationPackageName(packageName);
                        info.setApplicationName(list.get(i).getApplicationName());
                        info.setApplicationVersionCode(list.get(i).getApplicationVersionCode());
                        info.setActionType(flag);
                        info.setActionHappenTime(String.valueOf(time));// 程序卸载时间
                        break;
                    }
                }
                if (info == null) {
                    return;
                }
                EgLog.e("UninstallInfo ：" + info.toString());
                /** ---------------------保存被卸载的应用信息------------------ **/
                lexList.add(info);
                if (aBoolean) {
                    saveIuuinfo(context, info);
                }

            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                long time = System.currentTimeMillis();
                flag = "2";
                IUUInfo info = new IUUInfo();
                String packageName = intent.getDataString().substring(8);// 更新程序包名
                info.setApplicationPackageName(packageName);
                try {
                    info.setApplicationName(getProgramNameByPackageName(context, packageName));
                    if (pm.getPackageInfo(packageName, 0).versionName != null) {

                        info.setApplicationVersionCode(pm.getPackageInfo(packageName, 0).versionName
                                + pm.getPackageInfo(packageName, 0).versionCode);// 应用程序版本号
                    } else {
                        info.setApplicationVersionCode("");
                    }
                } catch (Exception e) {
                    info.setApplicationVersionCode("");
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
                info.setActionType(flag);
                info.setActionHappenTime(String.valueOf(time));// 程序更新时间
                EgLog.e("updateInfo：" + info.toString());
                /** ---------------------保存被更新的应用信息------------------ **/

                lexList.add(info);
                if (aBoolean) {
                    saveIuuinfo(context, info);
                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    /**
     * 保存变化的应用信息至数据库
     */
    private void saveIuuinfo(final Context context, final IUUInfo info) {
        try {
            SPUtil spUtil = SPUtil.getInstance(context);
            InstalledAPPInfoManager manager = new InstalledAPPInfoManager();
            List<InstalledAppInfo> list = InstalledAPPInfoManager.getAllApps(context);
            spUtil.setAllAppForUninstall(manager.getAppInfoToJson(list));
            DeviceTableOperation tOperation = DeviceTableOperation.getInstance(context);
            tOperation.insertIUUInfo(info);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    /**
     * 根据包名,获取应用程序名
     *
     * @param context
     * @param packageName
     * @return
     */
    public String getProgramNameByPackageName(Context context, String packageName) {
        String name = "";
        try {
            name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (Exception e) {
            return name;
        }
        return name;
    }

}
