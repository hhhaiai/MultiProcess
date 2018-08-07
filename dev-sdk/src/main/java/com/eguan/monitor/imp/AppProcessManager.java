package com.eguan.monitor.imp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.dbutils.device.DeviceTableOperation;
import com.eguan.monitor.procutils.AndroidAppProcess;
import com.eguan.monitor.procutils.AndroidProcessManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppProcessManager {


    private static AppProcessManager instance = null;
    private static SPUtil spUtil = null;
    private Context context;

    private static int receiver_count = 0;

    public static void resetCounter() {
        receiver_count = 0;
    }

    /**
     * 仅用来打印
     */
    private List<OCInfo> ocCache = new ArrayList<>();

    private List<String> prePackageNames = new ArrayList<>();
    private List<String> nowPackageNames = new ArrayList<>();
    private String startTime = null;
    private String endTime = null;

    public AppProcessManager(Context context) {
        this.context = context.getApplicationContext();
        spUtil = SPUtil.getInstance(context);
    }

    public static AppProcessManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AppProcessManager.class) {
                if (instance == null) {
                    instance = new AppProcessManager(context);
                }
            }
        }
        return instance;
    }

    public void appProcessInfo() {
        if (!ifRunning()) {
            return;
        }
        dealAppProcess();
    }


    /**
     *
     */
    private OCInfo info;
    private List<OCInfo> ocList = new ArrayList<>();

    private void dealAppProcess() {
        //每次进入循环，对isSaveForScreenOff赋值
        isSaveForScreenOff = false;
        List<AndroidAppProcess> apps = AndroidProcessManager.getRunningForegroundApps(context);
        nowPackageNames = getPackageNameFromPAndroidAppProcess(apps);
        //此方法位置不可移动,在后续会对nowPackageName做处理,需要nowPacakgeName后立即处理
        dealOCTime();
        if (prePackageNames.isEmpty()) {
            //为第一次循环,把packagename集合赋值给prePackageName;
            prePackageNames = nowPackageNames;
            String startTime = System.currentTimeMillis() + "";
            for (String packageName : nowPackageNames) {
                DeviceTableOperation.getInstance(context).addProcTemp(packageName,
                        startTime);
            }
            //得到最初的OCInfo集合,并赋初值,有开始没有结束时间
            ocList = getOCInfoList(prePackageNames);
        } else {
            //第二次进来.对ocList进行检查,如果新得到的packageNames集合中不存在ocList中有的packageName,表示app停掉,需要赋值结束时间,并保存进cache.
            //如果上次的包名集合中去除本次包名集合成功,遍历上次包名剩余的包名,进行时间关闭保存操作
            //在操作前对prePackageName做一次深拷备
            List<String> preCopy = new ArrayList<>(prePackageNames);
            preCopy.removeAll(nowPackageNames);

            if (!preCopy.isEmpty()) {
                endTime = System.currentTimeMillis() + "";
                for (String packageName : preCopy) {
                    for (OCInfo info : ocList) {
                        if (info.getApplicationPackageName().equals(packageName)) {
                            info.setApplicationCloseTime(endTime);
                            //将此条数据保存至cache中.
                            ocCache.add(info);
                            //把该条info从ProcTemp表中清除
                            deleteProcTemp(packageName);
                            //将info存储进OCInfo表中
                            insertOCInfo(info);
                            //保存了记录,将此条记录从ocList中去除
                            ocList.remove(info);
                            break;
                        }
                    }
                }
            }


            //处理本次packageNames集合中与上次packageNames集合对比新增的包名,对其进行初始化操作,赋值开始时间
            List<String> nowCopy = new ArrayList<>(nowPackageNames);
            nowCopy.removeAll(prePackageNames);
            final String startTime = System.currentTimeMillis() + "";
            if (!nowCopy.isEmpty()) {
                for (final String packageName : nowCopy) {
                    OCInfo info = new OCInfo();
                    info.setApplicationPackageName(packageName);
                    info.setApplicationName(getApplicationName(packageName));
                    info.setApplicationVersionCode(getApplicationVersion(packageName));
                    info.setApplicationOpenTime(startTime);
                    info.setNetwork(spUtil.getNetworkInfo());
                    info.setSwitchType("1");
                    info.setApplicationType(ifSystemApplication(packageName) ? "SA" : "OA");
                    info.setCollectionType("2");
                    //将新加入的进程(只有开始时间)加入到ocList.
                    ocList.add(info);
                    //将新增的数据同步到数据库表ProcTemp中
                    DeviceTableOperation.getInstance(context).addProcTemp(packageName,
                            startTime);
                }

            }
            //处理完之后,仍然需要将nowPackageNames集合赋值给prePackageNames集合
            prePackageNames = nowPackageNames;

        }
    }

    private void dealOCTime() {
        if (nowPackageNames.size() == 0) return;
        final ArrayList<OCTimeBean> ocTimeBeans = new ArrayList<>();
        for (String packageName : nowPackageNames) {
            OCTimeBean bean = new OCTimeBean(packageName, computerTimeInterval(), 1);
            ocTimeBeans.add(bean);
        }
        DeviceTableOperation.getInstance(context).insertOCTimes(ocTimeBeans);
    }

    private String computerTimeInterval() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return String.valueOf(hour / 6 + 1);
    }

    private void deleteProcTemp(final String packageName) {
        DeviceTableOperation.getInstance(context).deleteProcTemp(
                packageName);
    }

    private void insertOCInfo(final OCInfo info) {
        DeviceTableOperation.getInstance(context).insertOneOCInfo(info);
    }

    private List<OCInfo> getOCInfoList(List<String> names) {
        startTime = System.currentTimeMillis() + "";
        //去除重复元素,List转Set;
        Set<String> nameSets = new HashSet<>(names);
        List<OCInfo> result = new ArrayList<>();
        OCInfo info;
        for (String packageName : nameSets) {
            info = new OCInfo();
            info.setApplicationPackageName(packageName);
            info.setApplicationName(getApplicationName(packageName));
            info.setApplicationVersionCode(getApplicationVersion(packageName));
            info.setApplicationOpenTime(startTime);
            info.setNetwork(spUtil.getNetworkInfo());
            info.setCollectionType("2");
            info.setSwitchType("1");
            info.setApplicationType(ifSystemApplication(packageName) ? "SA" : "OA");
            result.add(info);
        }
        return result;
    }

    private List<String> getPackageNameFromPAndroidAppProcess(List<AndroidAppProcess> list) {
        List<String> result = new ArrayList<>(list.size());
        for (AndroidAppProcess ap : list) {
            String pn = ap.getPackageName();
            result.add(pn);
        }
        return result;
    }


    /**
     * 判断是否为系统应用
     *
     * @param packageName
     * @return true  系统应用; false  非系统应用
     */
    private boolean ifSystemApplication(String packageName) {
        boolean boo = false;
        try {
            if (packageName == null || packageName.equals("")) {
                return false;
            }
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);
            if ((pInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 1) {
                boo = true;
            }

        } catch (Throwable e) {
            boo = true;
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return boo;
    }


    /**
     * start time
     *
     * @return
     */
    private boolean ifRunning() {
        receiver_count++;
        return receiver_count % 6 == 1;
    }

    /**
     * 屏幕关闭数据处理
     */
    public void saveDatabase() {
        spUtil.setAppProcess("");
        try {
            DeviceTableOperation.getInstance(context).updateOcInfo(System.currentTimeMillis());
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }


    private String getApplicationName(String packageName) {
        String appName = "";
        try {
            PackageManager pm = context.getPackageManager();
            appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return appName;
    }

    private String getApplicationVersion(String packageName) {
        String appVer = "";
        PackageManager pm = context.getPackageManager();
        try {
            appVer = pm.getPackageInfo(packageName, 0).versionName +
                    "|" + pm.getPackageInfo(packageName, 0).versionCode;
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return appVer;
    }


    public void dealScreenOff() {
        if (!isSaveForScreenOff) {
            isSaveForScreenOff = true;
        } else {
            //确保在每个l轮循之间，只有一次的锁屏saveDB。
            return;
        }
        //保存ProcTemp表中数据至OCInfo表中
        Map<String, String> map = DeviceTableOperation.getInstance(context).queryProcTemp();
        ocList = getOCInfoProcTemp(map, 0);
        DeviceTableOperation.getInstance(context).insertOCInfo(ocList);
        //TODO:需要对ocList清空
        ocList.clear();
        ocCache.clear();
        //清除ProcTemp表
        DeviceTableOperation.getInstance(context).deleteProTemp();
        prePackageNames.clear();
    }

    public synchronized void dealRestartService() {
        //保存ProcTemp表中数据至OCInfo表中
        Map<String, String> map = DeviceTableOperation.getInstance(context).queryProcTemp();
        ocList = getOCInfoProcTemp(map, 1);
        DeviceTableOperation.getInstance(context).insertOCInfo(ocList);
        //TODO:需要对ocList清空
        ocList.clear();
        ocCache.clear();
        //清除ProcTemp表
        DeviceTableOperation.getInstance(context).deleteProTemp();
        prePackageNames.clear();
    }

    /**
     * @param map
     * @param type 0 表示锁屏，1表示服务重启
     * @return
     */
    private List<OCInfo> getOCInfoProcTemp(Map<String, String> map, int type) {
        String endTime = "";
        if (type == 0) {
            endTime = System.currentTimeMillis() + "";
        } else if (type == 1) {
            endTime = SPUtil.getInstance(context).getEndTime() + "";
        }
        List<OCInfo> result = new ArrayList<>();
        OCInfo info;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                String startTime = entry.getValue();
                info = new OCInfo();
                info.setApplicationPackageName(entry.getKey());
                info.setApplicationName(getApplicationName(entry.getKey()));
                info.setApplicationVersionCode(getApplicationVersion(entry.getKey()));
                info.setApplicationOpenTime(startTime);
                info.setApplicationCloseTime(endTime);
                info.setNetwork(spUtil.getNetworkInfo());
                info.setCollectionType("2");
                info.setApplicationType(ifSystemApplication(entry.getKey()) ? "SA" : "OA");
                info.setSwitchType(type == 0 ? "2" : "3");
                //bugfix:上传OC中,关闭时间与开始时间相同,与关闭时间小于开始时间的问题
                if (startTime != null && endTime != null && Long.valueOf(endTime) > Long.valueOf(startTime)) {
                    result.add(info);
                }
            } catch (Throwable e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            }

        }
        return result;
    }

    private boolean isSaveForScreenOff = false;
}