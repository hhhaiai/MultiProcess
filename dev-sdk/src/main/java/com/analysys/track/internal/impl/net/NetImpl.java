package com.analysys.track.internal.impl.net;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.text.TextUtils;

import com.analysys.track.db.DBHelper;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.ShellUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

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
                    getNetInfo();
                    if (back != null) {
                        back.onProcessed();
                    }
                }
            });
        } else {
            getNetInfo();
            if (back != null) {
                back.onProcessed();
            }
        }
    }

    public HashSet<NetInfo> getNetInfo() {

        if (!MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_NET, EGContext.TIME_SECOND * 2, System.currentTimeMillis())) {
            //没抢到锁
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("netimpl 没得到锁");
            }
            return null;
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("netimpl 得到锁 执行");
        }
        String result[] = {
                "cat /proc/net/tcp",
                "cat /proc/net/tcp6",
                "cat /proc/net/udp",
                "cat /proc/net/udp6",
                "cat /proc/net/raw",
                "cat /proc/net/raw6",
        };
        HashSet<NetInfo> pkgs = new HashSet<NetInfo>();
        try {
            for (String cmd : result
            ) {
                pkgs.addAll(getNetInfoFromCmd(cmd));
            }
        } catch (Exception e) {

        }

        JSONArray array = new JSONArray();
        for (NetInfo info :
                pkgs) {
            array.put(info.toJson());
        }

        if (EGContext.FLAG_DEBUG_INNER) {
            try {
                ELOG.i("netimpl  执行 完毕:" + array.toString(2));
            } catch (JSONException e) {
            }
        }

        TableProcess.getInstance(context).insertNet(array);

        MultiProcessChecker.getInstance().setLockLastModifyTime(context, EGContext.FILES_SYNC_NET, System.currentTimeMillis());
        return pkgs;
    }


    /**
     * 只支持输入
     * cat /proc/net/tcp
     * cat /proc/net/tcp6
     * cat /proc/net/udp
     * cat /proc/net/udp6
     * cat /proc/net/raw
     * cat /proc/net/raw6
     *
     * @return
     */
    public HashSet<NetInfo> getNetInfoFromCmd(String cmd) {
        HashSet<NetInfo> pkgs = new HashSet<>();
        try {
            String result = ShellUtils.shell(cmd.concat(" \n"));
            String[] lines = new String[0];
            if (result != null) {
                lines = result.split("\n");
            }
            for (int i = 1; i < lines.length; i++) {
                boolean isApp = false;
                String[] parameter = lines[i].split("\\s+");
                NetInfo info = new NetInfo();
                info.time = System.currentTimeMillis();
                info.local_addr = cmd.contains("6") ? ipv6(parameter[2]) : ipv4(parameter[2]);
                info.remote_addr = cmd.contains("6") ? ipv6(parameter[3]) : ipv4(parameter[3]);
                info.socket_type = getST(parameter[4]);
                String[] protocols = cmd.split("/");
                if (protocols.length > 0) {
                    info.protocol = protocols[protocols.length - 1];
                }
                String uid = parameter[8];

                try {
                    PackageManager manager = context.getPackageManager();
                    String[] pn = manager.getPackagesForUid(Integer.valueOf(uid));
                    for (int j = 0; j < pn.length; j++) {
                        String pkgName = pn[j];
                        if (!TextUtils.isEmpty(pkgName)
                                && pkgName.contains(".")
                                && !pkgName.contains(":")
                                && !pkgName.contains("/")
                                && manager.getLaunchIntentForPackage(pkgName) != null) {
                            isApp = true;
                            ApplicationInfo info1 = manager.getApplicationInfo(pkgName, 0);
                            info.pkgname = pkgName;
                            info.appname = (String) info1.loadLabel(manager);
                        }
                    }
                } catch (Throwable ignored) {
                }

                if (isApp) {
                    pkgs.add(info);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return pkgs;
    }

    /**
     * 16进制 ipv6 地址
     *
     * @param ipv6_16
     * @return
     */
    public String ipv6(String ipv6_16) {
        if (ipv6_16.length() < 32) {
            return ipv6_16;
        }
        StringBuffer buffer = new StringBuffer();
        buffer
                .append(ipv6_16, 0, 4).append(":")
                .append(ipv6_16, 4, 8).append(":")
                .append(ipv6_16, 8, 12).append(":")
                .append(ipv6_16, 12, 16).append(":")
                .append(ipv6_16, 16, 20).append(":")
                .append(ipv6_16, 20, 24).append(":")
                .append(Integer.parseInt(ipv6_16.substring(30, 32), 16)).append(".")
                .append(Integer.parseInt(ipv6_16.substring(28, 30), 16)).append(".")
                .append(Integer.parseInt(ipv6_16.substring(26, 28), 16)).append(".")
                .append(Integer.parseInt(ipv6_16.substring(24, 26), 16)).append(":")
                .append(Integer.parseInt(ipv6_16.substring(33), 16));
        return buffer.toString();
    }

    private String getST(String s) {
        if (s.equals("00")) {
            return "ERROR_STATUS";
        }
        if (s.equals("01")) {
            return "ESTABLISHED";
        }
        if (s.equals("02")) {
            return "SYN_SENT";
        }
        if (s.equals("03")) {
            return "SYN_RECV";
        }
        if (s.equals("04")) {
            return "FIN_WAIT1";
        }
        if (s.equals("05")) {
            return "FIN_WAIT2";
        }
        if (s.equals("06")) {
            return "TIME_WAIT";
        }
        if (s.equals("07")) {
            return "CLOSE";
        }
        if (s.equals("08")) {
            return "CLOSE_WAIT";
        }
        if (s.equals("09")) {
            return "LAST_ACK";
        }
        if (s.equals("0A")) {
            return "LISTEN";
        }
        if (s.equals("0B")) {
            return "CLOSING";
        }
        return s;
    }

    private String ipv4(String ipv4_16) {
        if (ipv4_16.length() < 10) {
            return ipv4_16;
        }
        StringBuffer buffer = new StringBuffer();
        buffer
                .append(Integer.parseInt(ipv4_16.substring(6, 8), 16)).append(".")
                .append(Integer.parseInt(ipv4_16.substring(4, 6), 16)).append(".")
                .append(Integer.parseInt(ipv4_16.substring(2, 4), 16)).append(".")
                .append(Integer.parseInt(ipv4_16.substring(0, 2), 16)).append(":")
                .append(Integer.parseInt(ipv4_16.substring(9), 16));
        return buffer.toString();
    }
}
