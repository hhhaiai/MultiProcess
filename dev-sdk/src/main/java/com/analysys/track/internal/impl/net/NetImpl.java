package com.analysys.track.internal.impl.net;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.oc.ProcUtils;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 网络信息获取
 * @Version: 1.0
 * @Create: 2019-10-14 16:49:30
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class NetImpl {

    private Context context;
    private static volatile NetImpl instance;

    private static final String[] CMDS = {
            "/proc/net/tcp",
            "/proc/net/tcp6",
            "/proc/net/udp",
            "/proc/net/udp6",
            "/proc/net/raw",
            "/proc/net/raw6",
    };
    private String usm;

    private NetImpl(Context context) {
        this.context = context;
    }

    public static NetImpl getInstance(Context context) {
        if (instance == null) {
            synchronized (NetInfo.class) {
                if (instance == null) {
                    instance = new NetImpl(context);
                }
            }
        }
        return instance;
    }

    public void dumpNet(final ECallBack back) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    tryLockRun();
                    if (back != null) {
                        back.onProcessed();
                    }
                }
            });
        } else {
            tryLockRun();
            if (back != null) {
                back.onProcessed();
            }
        }
    }

    public void tryLockRun() {
        if (!MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_NET, EGContext.TIME_SECOND * 2, System.currentTimeMillis())) {
            //没抢到锁
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("netimpl 没得到锁");
            }
            return;
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("netimpl 得到锁 执行");
        }
        getNetInfo();
        MultiProcessChecker.getInstance().setLockLastModifyTime(context, EGContext.FILES_SYNC_NET, System.currentTimeMillis());
    }


    HashMap<String, NetInfo> pkgs = new HashMap<>();

    private HashMap<String, NetInfo> getCacheInfo() {
        HashMap<String, NetInfo> map = new HashMap<>();
        try {
            JSONArray array = TableProcess.getInstance(context).selectNet(1024 * 1024);
            if (array == null || array.length() == 0) {
                return pkgs;
            }
            array = (JSONArray) array.get(0);
            for (int i = 0; array != null && i < array.length(); i++) {
                JSONObject netInfoObject = (JSONObject) array.get(i);
                NetInfo info = NetInfo.fromJson(netInfoObject);
                map.put(info.pkgname, info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return pkgs;
        }
        return map;
    }

    String api_4;
    JSONObject proc_56;

    public HashMap<String, NetInfo> getNetInfo() {
        try {
            pkgs = getCacheInfo();
            //本次扫描的时间戳
            long time = System.currentTimeMillis();
            //重置打开状态
            Collection<NetInfo> infoCollection1 = pkgs.values();
            for (NetInfo info : infoCollection1) {
                info.isOpen = false;
            }
            api_4 = getApi4(context);
            proc_56 = getProc56(context);
            usm = getUsm(context);
            //扫描
            for (String cmd : CMDS
            ) {
                try {
                    //运行shell获得net信息
                    String result = runShell(cmd);
                    //解析原始信息存到pkgs里面
                    resolve(cmd, result, time);
                } catch (Throwable throwable) {
                    //某一行解析异常
                }
            }
            //最后剩下的就是上一次扫描有 但是这一次扫描没有的应用 加关闭符号
            Collection<NetInfo> infoCollection2 = pkgs.values();
            for (NetInfo info : infoCollection2) {
                //本次扫描到的列表里面有  活着
                if (info.isOpen) {
                    continue;
                }
                // 死了 添加 关闭节点 判断上一个是关闭节点 不新加
                if (info.scanningInfos.get(info.scanningInfos.size() - 1).tcpInfos == null) {
                    //有不操作
                    continue;
                }
                //没有添加关闭节点
                NetInfo.ScanningInfo scanningInfo = new NetInfo.ScanningInfo();
                scanningInfo.time = time;
                info.scanningInfos.add(scanningInfo);
            }

            //存数据库
            saveNetInfoToDb(pkgs);

        } catch (Throwable throwable) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("netimpl error " + throwable.getMessage());
            }
        }
        return pkgs;
    }

    private String getUsm(Context mContext) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 29) {
            class RecentUseComparator implements Comparator<UsageStats> {
                @Override
                public int compare(UsageStats lhs, UsageStats rhs) {
                    return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1
                            : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
                }
            }
            try {
                @SuppressLint("WrongConstant")
                UsageStatsManager usm = (UsageStatsManager) mContext.getApplicationContext()
                        .getSystemService(Context.USAGE_STATS_SERVICE);
                if (usm == null) {
                    return null;
                }
                long ts = System.currentTimeMillis();
                List<UsageStats> usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 10 * 1000, ts);
                if (usageStats == null || usageStats.size() == 0) {
                    return null;
                }
                Collections.sort(usageStats, new RecentUseComparator());
                String usmPkg = usageStats.get(0).getPackageName();
                if (!TextUtils.isEmpty(usmPkg)) {
                    return usmPkg;
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
                }
            }
        }
        return null;
    }

    private void saveNetInfoToDb(HashMap<String, NetInfo> pkgs) {

        JSONArray array = new JSONArray();
        for (NetInfo netInfo : pkgs.values()) {
            array.put(netInfo.toJson());
        }
        if (array.length() > 0) {
            TableProcess.getInstance(context).deleteNet();
        }
        TableProcess.getInstance(context).insertNet(array.toString());
        if (EGContext.FLAG_DEBUG_INNER) {
            try {
                ELOG.i("更新数据:" + array.toString(2));
            } catch (JSONException ignored) {

            }
        }
    }


    private void resolve(String cmd, String result, Long time) throws Throwable {
        if (result == null || "".equals(result)) {
            return;
        }
        ;
        if (pkgs == null) {
            return;
        }
        String[] lines = result.split("\n");

        for (int i = 1; i < lines.length; i++) {
            String[] parameter = lines[i].split("\\s+");
            if (parameter.length < 9) {
                continue;
            }
            PackageManager manager = context.getPackageManager();
            String uid = parameter[8];
            String[] pn = manager.getPackagesForUid(Integer.valueOf(uid));
            for (int j = 0; j < (pn != null ? pn.length : 0); j++) {
                String pkgName = pn[j];
                if (!TextUtils.isEmpty(pkgName)
                        && pkgName.contains(".")
                        && !pkgName.contains(":")
                        && !pkgName.contains("/")
                        && manager.getLaunchIntentForPackage(pkgName) != null) {

                    NetInfo info = pkgs.get(pkgName);
                    if (info == null) {
                        info = new NetInfo();
                        info.pkgname = pkgName;
                        ApplicationInfo info1 = manager.getApplicationInfo(pkgName, 0);
                        info.appname = (String) info1.loadLabel(manager);
                        pkgs.put(pkgName, info);
                    }
                    info.isOpen = true;
                    if (info.scanningInfos == null) {
                        info.scanningInfos = new ArrayList<>();
                    }
                    NetInfo.ScanningInfo scanningInfo = null;
                    for (NetInfo.ScanningInfo scanningInfo1 : info.scanningInfos) {
                        if (scanningInfo1.time == time) {
                            scanningInfo = scanningInfo1;
                            break;
                        }
                    }
                    if (scanningInfo == null) {
                        scanningInfo = new NetInfo.ScanningInfo();
                        scanningInfo.time = time;
                        scanningInfo.api_4 = api_4;
                        scanningInfo.proc_56 = proc_56;
                        scanningInfo.usm = usm;
                        info.scanningInfos.add(scanningInfo);
                    }

                    if (scanningInfo.tcpInfos == null) {
                        scanningInfo.tcpInfos = new ArrayList<>();
                    }
                    NetInfo.TcpInfo tcpInfo = new NetInfo.TcpInfo();
                    scanningInfo.tcpInfos.add(tcpInfo);
                    tcpInfo.local_addr = getIpAddr(parameter[2]);
                    tcpInfo.remote_addr = getIpAddr(parameter[3]);
                    tcpInfo.socket_type = getSocketType(parameter[4]);
                    String[] protocols = cmd.split("/");
                    if (protocols.length > 0) {
                        tcpInfo.protocol = protocols[protocols.length - 1];
                    }
                }
            }
        }
    }

    private JSONObject getProc56(Context context) {
        if ((Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24)) {
            return ProcUtils.getInstance(context).getRunningInfo();
        }
        return null;
    }

    private String getApi4(Context mContext) {
        if (Build.VERSION.SDK_INT >= 21) {
            return null;
        }
        String pkgName = null;
        ActivityManager am = null;
        try {
            if (mContext == null) {
                mContext = EContextHelper.getContext(mContext);
            }
            if (mContext != null) {
                am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks == null || tasks.size() <= 0) {
                    List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
                    for (ActivityManager.RunningAppProcessInfo appProcess : processInfos) {
                        if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            pkgName = appProcess.processName;
                        }
                    }
                } else {
                    // 获取栈顶app的包名
                    pkgName = tasks.get(0).topActivity.getPackageName();
                }
            }


        } catch (Throwable e) {
        }
        return pkgName;
    }


    public String runShell(String cmd) {
        BufferedReader bufferedReader = null;
        InputStreamReader reader = null;
        FileInputStream fileInputStream = null;
        try {
            File file = new File(cmd);
            if (!file.exists() || !file.isFile()) {
                return null;
            }
            fileInputStream = new FileInputStream(file);
            reader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(reader);
            StringBuilder builder = new StringBuilder();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (reader != null) {
                    reader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private String getSocketType(String s) {
        if (s == null) {
            return null;
        }
        if ("00".equals(s)) {
            return "ERROR_STATUS";
        }
        if ("01".equals(s)) {
            return "ESTABLISHED";
        }
        if ("02".equals(s)) {
            return "SYN_SENT";
        }
        if ("03".equals(s)) {
            return "SYN_RECV";
        }
        if ("04".equals(s)) {
            return "FIN_WAIT1";
        }
        if ("05".equals(s)) {
            return "FIN_WAIT2";
        }
        if ("06".equals(s)) {
            return "TIME_WAIT";
        }
        if ("07".equals(s)) {
            return "CLOSE";
        }
        if ("08".equals(s)) {
            return "CLOSE_WAIT";
        }
        if ("09".equals(s)) {
            return "LAST_ACK";
        }
        if ("0A".equals(s)) {
            return "LISTEN";
        }
        if ("0B".equals(s)) {
            return "CLOSING";
        }
        return s;
    }

    private String getIpAddr(String ipx16) {
        if (ipx16.length() > 32) {
            StringBuilder buffer = new StringBuilder();
            buffer
                    .append(ipx16, 0, 4).append(":")
                    .append(ipx16, 4, 8).append(":")
                    .append(ipx16, 8, 12).append(":")
                    .append(ipx16, 12, 16).append(":")
                    .append(ipx16, 16, 20).append(":")
                    .append(ipx16.substring(20, 24).equals("0000") ? "0" : ipx16.substring(20, 24)).append(":")
                    .append(Integer.parseInt(ipx16.substring(30, 32), 16)).append(".")
                    .append(Integer.parseInt(ipx16.substring(28, 30), 16)).append(".")
                    .append(Integer.parseInt(ipx16.substring(26, 28), 16)).append(".")
                    .append(Integer.parseInt(ipx16.substring(24, 26), 16)).append('\\')
                    .append(Integer.parseInt(ipx16.substring(33), 16));
            String ipv6 = buffer.toString();
            if (ipv6.contains("0000:0000:0000:0000")) {
                ipv6 = ipv6.replace("0000:0000:0000:0000", ":");
            }
            return ipv6;
        } else if (ipx16.length() > 8) {
            StringBuilder buffer = new StringBuilder();
            buffer
                    .append(Integer.parseInt(ipx16.substring(6, 8), 16)).append(".")
                    .append(Integer.parseInt(ipx16.substring(4, 6), 16)).append(".")
                    .append(Integer.parseInt(ipx16.substring(2, 4), 16)).append(".")
                    .append(Integer.parseInt(ipx16.substring(0, 2), 16)).append(":")
                    .append(Integer.parseInt(ipx16.substring(9), 16));
            return buffer.toString();
        } else {
            return ipx16;
        }

    }
}
