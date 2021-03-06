package com.device.tripartite.cases.traffic.planc;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

import com.device.utils.Aha;
import com.device.utils.EL;
import com.device.utils.IdCaller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WtfTs {


    public static double getWifiTraffic() {
        double totalRxBytes = TrafficStats.getTotalRxBytes();
        double totalTxBytes = TrafficStats.getTotalTxBytes();
        double mrb = TrafficStats.getMobileRxBytes();
        double mtb = TrafficStats.getMobileTxBytes();
        EL.i("totalRxBytes: " + totalRxBytes);
        EL.i("totalTxBytes: " + totalTxBytes);
        EL.i("mrb: " + mrb);
        EL.i("mtb: " + mtb);
        double rwifi = totalRxBytes - mrb;
        double twifi = totalTxBytes - mtb;
        double totalWifi = rwifi + twifi;

        return totalWifi;
    }

    public static void fk(Context context) {

        Aha.getUid(context, new IdCaller() {
                    @Override
                    public void SeeUid(int uid) {
                    }

                    @Override
                    public void SeeUid(int uid, String app, String pkg) {
                        //
                        planA(uid, app, pkg);
                        planB(uid, app, pkg);

                    }

                    @Override
                    public void SeePid(int pid) {
                        //
                    }
                }
        );


    }

    public static void planB(int uid, String app, String pkg) {
//        /proc/uid_stat/uid/tcp_send
//        /proc/uid_stat/uid/tcp_rcv
//        long rx = getTraffic(uid, "/proc/uid_procstat/" + uid + "/tcp_send");
//        long tx = getTraffic(uid, "/proc/uid_procstat/" + uid + "/tcp_rcv");
        long rx = getTraffic(uid, "/proc/uid_stat/" + uid + "/tcp_send");
        long tx = getTraffic(uid, "/proc/uid_stat/" + uid + "/tcp_rcv");
        if (rx == -1 && tx == -1) {
            EL.v("[PlanB]8888888888" + app + "[" + pkg + "]----->get failed");
        } else {
            EL.i("[PlanB]8888888888" + app + "[" + pkg + "]---上行: " + rx + "----下行:" + tx);
        }
    }

    static long getTraffic(int uid, String path) {
        long sndTraffic = -1; // 上传流量
        sndTraffic = TrafficStats.getUidTxBytes(uid);
        if (sndTraffic != -1) {
            return sndTraffic;
        }
        RandomAccessFile rafRcv = null, rafSnd = null;
        try {
            rafSnd = new RandomAccessFile(path, "r");
            sndTraffic = Long.parseLong(rafSnd.readLine());
            rafRcv.close();
            rafSnd.close();
        } catch (Throwable e) {
//            EL.e(e);
            sndTraffic = -1;
        }
        return sndTraffic;
    }

    private static void planA(int uid, String app, String pkg) {
        long rx = TrafficStats.getUidRxBytes(uid);
        long tx = TrafficStats.getUidTxBytes(uid);
        if (rx == -1 && tx == -1) {
            EL.v("[PlanA]8888888888" + app + "[" + pkg + "]----->get failed");
        } else {
            EL.i("[PlanA]8888888888" + app + "[" + pkg + "]---上行: " + rx + "----下行:" + tx);
        }
    }


}
