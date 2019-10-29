package com.analysys.track.internal.impl.net;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.text.TextUtils;

import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MultiProcessChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

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

    public Set<NetInfo> getNetInfo() {
        String[] cmds = {
                "/proc/net/tcp",
                "/proc/net/tcp6",
                "/proc/net/udp",
                "/proc/net/udp6",
                "/proc/net/raw",
                "/proc/net/raw6",
        };
        HashSet<NetInfo> pkgs = new HashSet<NetInfo>();
        try {
            for (String cmd : cmds
            ) {
                try {
                    //运行shell获得net信息
                    String result = runShell(cmd);
                    //解析原始信息存到pkgs里面
                    resolve(cmd, pkgs, result);
                } catch (Throwable throwable) {
                    //某一行解析异常
                }
            }


            saveNetInfoToDb(pkgs);

        } catch (Throwable throwable) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("netimpl error " + throwable.getMessage());
            }
        }
        return pkgs;
    }

    HashSet<NetInfo> lastNetInfo = new HashSet<>();

    private void saveNetInfoToDb(HashSet<NetInfo> pkgs) {
        //todo lastNetInfo 多进程同步

        if (lastNetInfo.isEmpty()) {
            lastNetInfo.addAll(pkgs);
            return;
        }
        for (NetInfo info :
                lastNetInfo) {
            if (!pkgs.contains(info)) {
                if (lastNetInfo.remove(info)) {
                    closeAndSave(info);
                }
            }
        }

        lastNetInfo.addAll(pkgs);

        // pkgs.clear();

    }

    private void closeAndSave(NetInfo info) {
        //闭合本次的info
        info.setClose_time(System.currentTimeMillis());
        JSONObject object = info.toJson();
        TableProcess.getInstance(context).insertNet(object);
        if (EGContext.FLAG_DEBUG_INNER) {
            try {
                ELOG.i("闭合数据:" + object.toString(2));
            } catch (JSONException ignored) {

            }
        }
    }


    private void resolve(String cmd, HashSet<NetInfo> pkgs, String result) throws Throwable {
        if (result == null || "".equals(result)) {
            return;
        }
        if (pkgs == null) {
            return;
        }
        String[] lines = result.split("\n");
        for (int i = 1; i < lines.length; i++) {
            boolean isApp = false;
            String[] parameter = lines[i].split("\\s+");
            if (parameter.length < 9) {
                continue;
            }
            NetInfo info = new NetInfo();
            info.time = System.currentTimeMillis();
            info.local_addr = getIpAddr(parameter[2]);
            info.remote_addr = getIpAddr(parameter[3]);
            info.socket_type = getSocketType(parameter[4]);
            String[] protocols = cmd.split("/");
            if (protocols.length > 0) {
                info.protocol = protocols[protocols.length - 1];
            }
            String uid = parameter[8];

            PackageManager manager = context.getPackageManager();
            String[] pn = manager.getPackagesForUid(Integer.valueOf(uid));
            for (int j = 0; j < (pn != null ? pn.length : 0); j++) {
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

            if (isApp) {
                pkgs.add(info);
            }
        }
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

    public void processWhenScreenChange(boolean open) {
        //开
        if (!open) {
            for (NetInfo info : lastNetInfo
            ) {
                closeAndSave(info);
            }
            lastNetInfo.clear();
        } else {
//            dumpNet(null);
        }
    }
}
