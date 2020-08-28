package com.analysys.track.internal.impl.net;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
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

    private HashMap<String, NetInfo> pkgs = new HashMap<>();

    public void dumpNet(final ECallBack back) {

        try {
            if (!BuildConfig.ENABLE_NETINFO) {
                return;
            }
            if (!MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_NET, EGContext.TIME_SECOND * 2, System.currentTimeMillis())) {
                //没抢到锁
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_netinfo, "netimpl 没得到锁");
                }
                if (back != null) {
                    back.onProcessed();
                }
                return;
            }
            SystemUtils.runOnWorkThread(new Runnable() {
                @Override
                public void run() {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_netinfo, "netimpl 得到锁 执行");
                    }
                    getNetInfo();
                    MultiProcessChecker.getInstance().setLockLastModifyTime(context, EGContext.FILES_SYNC_NET, System.currentTimeMillis());
                    if (back != null) {
                        back.onProcessed();
                    }
                }
            });
        } catch (Throwable e) {
        }

    }

    private HashMap<String, NetInfo> getCacheInfo() {
        HashMap<String, NetInfo> map = new HashMap<>();
        try {
            JSONArray array = TableProcess.getInstance(context).selectNet(1024 * 1024);
            if (array == null || array.length() == 0) {
                return map;
            }
            array = (JSONArray) array.get(0);
            for (int i = 0; array != null && i < array.length(); i++) {
                try {
                    String pkg_name = (String) array.get(i);
                    if (!TextUtils.isEmpty(pkg_name) && pkg_name.contains("_")) {
                        String[] values = pkg_name.split("_");
                        if (values == null || values.length < 2) {
                            continue;
                        }
                        NetInfo info = new NetInfo();
                        info.pkgname = values[0];
                        info.appname = values[1];
                        map.put(info.pkgname, info);
                    }
                } catch (Throwable e) {
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_netinfo, e);
            }
        }
        return map;
    }


    public HashMap<String, NetInfo> getNetInfo() {
        try {
//            if (SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_NET, false)) {
//                //控制短路 不工作
//                return null;
//                //否则工作
//            }
            if (!BuildConfig.ENABLE_NETINFO) {
                return null;
            }
            //android 10 以上不工作
            if(Build.VERSION.SDK_INT>=29){
                return null;
            }
            //net不允许采集,不工作,默认允许true
            if (!SPHelper.getBooleanValueFromSP(context, UploadKey.Response.RES_POLICY_MODULE_CL_NET, true)) {
                return null;
            }
            pkgs = getCacheInfo();

            //重置打开状态
            Collection<NetInfo> infoCollection1 = pkgs.values();
            for (NetInfo info : infoCollection1) {
                info.isOpen = false;
            }
            //本次扫描的时间戳
            long time = System.currentTimeMillis();
            //扫描
            for (String cmd : CMDS) {
                try {

                    String result = SystemUtils.getContent(cmd);
                    if (!TextUtils.isEmpty(result)) {
                        //解析原始信息存到pkgs里面
                        resolve(cmd, result, time);
                    }
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(BuildConfig.tag_netinfo, e);
                    }
                }
            }

            //添加关闭节点 4.3.1.0 注释，理由：数据分析不需要此信息
            //addCloseTag(time);

            //存数据库
            savePkgToDb(pkgs);
            saveScanningInfos(pkgs);

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_netinfo, e);
            }
        }
        return pkgs;
    }

    private void addCloseTag(long time) {
        //最后剩下的就是上一次扫描有 但是这一次扫描没有的应用 加关闭符号
        Collection<NetInfo> infoCollection2 = pkgs.values();
        for (NetInfo info : infoCollection2) {
            //本次扫描到的列表里面有  活着
            if (info.isOpen) {
                continue;
            }

            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_netinfo, info.appname + "[死了,判断关闭节点]");
            }

            List<NetInfo.ScanningInfo> scanningInfos = TableProcess.getInstance(context).selectScanningInfoByPkg(info.pkgname, true);
            // 死了 添加 关闭节点 判断上一个是关闭节点 不新加
            if (scanningInfos != null && scanningInfos.size() > 0) {
                List<NetInfo.TcpInfo> tcpInfos = scanningInfos.get(0).tcpInfos;
                if (tcpInfos == null || tcpInfos.isEmpty()) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_netinfo, info.appname + "[死了][有关闭节点-不操作]");
                    }
                    //有不操作
                    continue;
                }
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_netinfo, info.appname + "[死了][无关闭节点-添加关闭节点]");
            }
            //没有添加关闭节点
            NetInfo.ScanningInfo scanningInfo = new NetInfo.ScanningInfo();
            scanningInfo.time = time;
            scanningInfo.pkgname = info.pkgname;
            scanningInfo.appname = info.appname;
            if (info.scanningInfos == null) {
                info.scanningInfos = new ArrayList<>();
            }
            info.scanningInfos.add(scanningInfo);
        }
    }

    private void saveScanningInfos(HashMap<String, NetInfo> pkgs) {
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_netinfo, "[存ScanningInfo列表][开始]");
        }
        for (String string : pkgs.keySet()) {
            List<NetInfo.ScanningInfo> scanningInfos = pkgs.get(string).scanningInfos;
            if (scanningInfos != null) {
                for (int i = 0; i < scanningInfos.size(); i++) {
                    NetInfo.ScanningInfo info = scanningInfos.get(i);
                    //必须有扫描到的tcp连接才存，否则不存，4.3.1.0 版本后由于不需要关闭节点了，因此防护空的也存储。
                    if (info != null && !info.tcpInfos.isEmpty()) {
                        TableProcess.getInstance(context).insertScanningInfo(scanningInfos.get(i));
                    }
                }
            }
        }
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_netinfo, "[存ScanningInfo列表][结束]");
        }
    }


    private void savePkgToDb(HashMap<String, NetInfo> pkgs) {

        if (BuildConfig.logcat) {
            ELOG.d(BuildConfig.tag_netinfo, "[存App列表][" + pkgs.keySet().size() + "]");
        }
        JSONArray array = new JSONArray();
        for (NetInfo netInfo : pkgs.values()) {
            array.put(netInfo.pkgname + "_" + netInfo.appname);
        }
        if (array.length() > 0) {
            TableProcess.getInstance(context).deleteNet();
        }
        TableProcess.getInstance(context).insertNet(array.toString());
        if (BuildConfig.logcat) {
            try {
                ELOG.i(BuildConfig.tag_netinfo, "[存App列表]" + array.toString(2));
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_netinfo, e);
                }
            }
        }
    }


    private void resolve(String cmd, String result, Long time) throws Exception {
        if (TextUtils.isEmpty(result)) {
            return;
        }
        if (pkgs == null) {
            return;
        }
        String[] lines = result.split("\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (TextUtils.isEmpty(line)) {
                continue;
            }
            String[] parameter = line.split("\\s+");
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
                        && SystemUtils.hasLaunchIntentForPackage(manager, pkgName)) {

                    NetInfo info = pkgs.get(pkgName);
                    if (info == null) {
                        info = new NetInfo();
                        info.pkgname = pkgName;
                        pkgs.put(pkgName, info);
                    }
                    if (info.appname == null) {
                        try {
                            ApplicationInfo info1 = manager.getApplicationInfo(pkgName, 0);
                            if (info1 != null) {
                                info.appname = (String) info1.loadLabel(manager);
                            }
                        } catch (Throwable e) {
                        }
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
                        scanningInfo.pkgname = info.pkgname;
                        scanningInfo.appname = info.appname;
                        info.scanningInfos.add(scanningInfo);
                    }

                    if (scanningInfo.tcpInfos == null) {
                        scanningInfo.tcpInfos = new ArrayList<>();
                    }
                    String socketType = getSocketType(parameter[4]);
                    if (socketType != null && theTypeOfAttention(socketType)) {
                        NetInfo.TcpInfo tcpInfo = new NetInfo.TcpInfo();
                        scanningInfo.tcpInfos.add(tcpInfo);
                        tcpInfo.local_addr = getIpAddr(parameter[2]);
                        tcpInfo.remote_addr = getIpAddr(parameter[3]);
                        tcpInfo.socket_type = getSocketType(parameter[4]);
                        if (cmd.contains("/")) {
                            String[] protocols = cmd.split("/");
                            if (protocols.length > 0) {
                                tcpInfo.protocol = protocols[protocols.length - 1];
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean theTypeOfAttention(String socket_type) {
        switch (socket_type) {
            case "1"://ESTABLISHED
            case "2"://SYN_SENT
            case "3"://SYN_RECV
            case "10"://LISTEN
                return true;
            default:
                return false;
        }
    }

    private String getSocketType(String s) {
        if (s == null || "".equals(s)) {
            return null;
        }
        if (s.contains(":") || s.contains(".")) {
            return null;
        }
        try {
            int linuxcode = Integer.valueOf(s, 16);
            return String.valueOf(linuxcode);
        } catch (Throwable e) {
            return null;
        }
    }

    private String getIpAddr(String ipx16) {
        try {
            if (ipx16.length() > 32) {
                StringBuilder buffer = new StringBuilder();
                buffer
                        .append(ipx16, 0, 4).append(":")
                        .append(ipx16, 4, 8).append(":")
                        .append(ipx16, 8, 12).append(":")
                        .append(ipx16, 12, 16).append(":")
                        .append(ipx16, 16, 20).append(":")
                        .append("0000".equals(ipx16.substring(20, 24)) ? "0" : ipx16.substring(20, 24)).append(":")
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
        } catch (Throwable e) {
        }
        return ipx16;
    }
}
