package com.eguan.utils.netutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.eguan.monitor.imp.DriverInfo;
import com.eguan.monitor.imp.DriverInfoManager;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by Wang on 2017/2/20.
 */
public class HeadInfo {

    private HeadInfo() {
    }

    public static HeadInfo getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final HeadInfo INSTANCE = new HeadInfo();
    }

    /**
     * F1,F2参数获取
     *
     * @return
     */
    public List<String> getDevInfo(Context mContext) {

        List<String> list = new ArrayList<String>();
        DriverInfoManager driverInfoManager = new DriverInfoManager(mContext);
        driverInfoManager.setDriverInfo();
        DriverInfo driverInfo = DriverInfo.getInstance();
        String mac, deviceId, imei = null, numb, androidId = null, imsi = null;
        mac = driverInfo.getMACAddress();
        numb = driverInfo.getSerialNumber();

        deviceId = driverInfo.getDeviceId();

        if (deviceId != null && !deviceId.equals("") && deviceId.indexOf("-") != -1) {
            String[] dev = deviceId.split("-");
            if (!TextUtils.isEmpty(dev[0]) && !dev[0].equals("null")) {
                imei = dev[0];
            }
            if (!TextUtils.isEmpty(dev[1]) && !dev[1].equals("null")) {
                imsi = dev[1];
            }
            if (!TextUtils.isEmpty(dev[2]) && !dev[2].equals("null")) {
                androidId = dev[2];
            }
        }

        if (!TextUtils.isEmpty(mac) && !mac.equals("null")) {

            list.add("1=" + mac);
        }
        if (!TextUtils.isEmpty(imei) && !imei.equals("null")) {
            list.add("2=" + imei);
            if (list.size() == 2)
                return list;
        }
        if (!TextUtils.isEmpty(numb) && !numb.equals("null")) {

            list.add("4=" + numb);
            if (list.size() == 2)
                return list;
        }
        if (!TextUtils.isEmpty(androidId) && !androidId.equals("null")) {

            list.add("3=" + androidId);
            if (list.size() == 2)
                return list;
        }
        if (!TextUtils.isEmpty(imsi) && !imsi.equals("null")) {
            list.add("5=" + imsi);
            if (list.size() == 2)
                return list;
        }
        if (list.size() == 0) {
            long f1 = createData(15);
            long f2 = f1 + 20120302;
            list.add("0=" + f1);
            list.add("0=" + f2);

        } else if (list.size() == 1) {

            list.add("0=" + createData(15));
        }
        return list;
    }

    /**
     * 随机数获取
     *
     * @param length
     * @return
     */
    private long createData(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(rand.nextInt(10));
        }
        return Long.parseLong(sb.toString());
    }
}
