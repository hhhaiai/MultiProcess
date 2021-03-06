package com.device.tripartite.cases.traffic.planA;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;

import com.device.utils.EL;

import java.util.ArrayList;
import java.util.List;

public class A {

    /**
     * 返回所有的有互联网访问权限的应用程序的流量信息。
     * TrafficInfo 为一个Bean 模型类。使用的时候可以自定义一个、。
     *
     * @return
     */
    public static List<TrafficInfo> getTrafficInfo(Context context) {
        //获取到配置权限信息的应用程序
        PackageManager pms = context.getPackageManager();
        ;
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packinfos = pms
                .getInstalledPackages(PackageManager.GET_PERMISSIONS);
        //存放具有Internet权限信息的应用
        List<TrafficInfo> trafficInfos = new ArrayList<TrafficInfo>();
        for (PackageInfo packinfo : packinfos) {
            //获取该应用的所有权限信息
            String[] permissions = packinfo.requestedPermissions;
            if (permissions != null && permissions.length > 0) {
                for (String permission : permissions) {
                    //筛选出具有Internet权限的应用程序
                    if ("android.permission.INTERNET".equals(permission)) {
                        //用于封装具有Internet权限的应用程序信息
                        TrafficInfo trafficInfo = new TrafficInfo();
                        //封装应用信息
                        trafficInfo.setPackname(packinfo.packageName);
                        trafficInfo.setIcon(packinfo.applicationInfo.loadIcon(pm));
                        trafficInfo.setAppname(packinfo.applicationInfo.loadLabel(pm).toString());
                        //获取到应用的uid（user id）
                        int uid = packinfo.applicationInfo.uid;
                        //TrafficStats对象通过应用的uid来获取应用的下载、上传流量信息


                        //发送的 上传的流量byte
                        trafficInfo.setRx(TrafficStats.getUidRxBytes(uid));
                        //下载的流量 byte
                        trafficInfo.setTx(TrafficStats.getUidTxBytes(uid));
                        EL.i(trafficInfo.toString());
                        trafficInfos.add(trafficInfo);
                        trafficInfo = null;
                        break;
                    }
                }
            }
        }
        return trafficInfos;
    }
}
