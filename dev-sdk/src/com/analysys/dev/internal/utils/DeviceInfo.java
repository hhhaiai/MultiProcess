package com.analysys.dev.internal.utils;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.dev.internal.utils.sp.SPHelper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

import static java.lang.Runtime.getRuntime;

public class DeviceInfo {

    private static final String DEFALT_MAC = "02:00:00:00:00:00";
    private static final String[] FILE_LIST =
            {Base64.encodeToString("/sys/class/net/wlan0/address".getBytes(), Base64.DEFAULT),
                    Base64.encodeToString("/sys/class/net/eth0/address".getBytes(), Base64.DEFAULT),
                    Base64.encodeToString("/sys/devices/virtual/net/wlan0/address".getBytes(), Base64.DEFAULT)};

    /**
     * 读取手机MAC地址
     *
     * @param context
     * @return 返回mac地址
     */

    /**
     * 读取手机mac地址
     *
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        if (context == null) {
            context = EContextHelper.getContext();
        }
        String mac = DEFALT_MAC;
        try {
            if (Build.VERSION.SDK_INT < 23) {
                mac = getMacByAndridAPI(context);
            } else {
                if (isWifiAlive(context)) {
                    mac = getMacByJavaAPI();
                } else {
                    mac = getMacFile();
                }
            }
            if (mac.equals(DEFALT_MAC)) {
                mac = getMacByShell();
            }
        } catch (Throwable e) {
        }

        if (!mac.equals(DEFALT_MAC)) {
            SPHelper.getDefault(context).edit().putString("", "").commit();
        }
        return mac;

    }

    private static String getMacByShell() {
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < FILE_LIST.length; i++) {
                proc = getRuntime().exec("cat " + new String(Base64.decode(FILE_LIST[i], Base64.DEFAULT)));
                in = new BufferedInputStream(proc.getInputStream());
                br = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                if (sb.length() > 0) {
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            LL.e(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {

                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return DEFALT_MAC;
    }

    /**
     * android 9以上没权限获取
     *
     * @return
     * @throws IOException
     */
    private static String getMacFile() throws IOException {
        for (int i = 0; i < FILE_LIST.length; i++) {
            BufferedReader reader = null;
            try {
                File file = new File(new String(Base64.decode(FILE_LIST[i], Base64.DEFAULT)));
                if (file.exists() && file.canRead()) {
                    reader = new BufferedReader(new FileReader(file));
                    String tempString = null;
                    while ((tempString = reader.readLine()) != null) {
                        if (!TextUtils.isEmpty(tempString)) {
                            return tempString;
                        }
                    }
                }
            } catch (IOException e) {
                LL.e(e);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        return DEFALT_MAC;
    }

    /**
     * 需要打开wifi才能获取
     *
     * @return
     * @throws SocketException
     */
    @TargetApi(9)
    private static String getMacByJavaAPI() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface netInterface = interfaces.nextElement();
            if ("wlan0".equals(netInterface.getName()) || "eth0".equals(netInterface.getName())) {
                byte[] addr = netInterface.getHardwareAddress();
                if (addr == null || addr.length == 0) {
                    return null;
                }
                StringBuilder buf = new StringBuilder();
                for (byte b : addr) {
                    buf.append(String.format("%02X:", b));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString().toLowerCase(Locale.getDefault());
            }
        }
        return DEFALT_MAC;
    }

    private static String getMacByAndridAPI(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (PermissionUtils.checkPermission(context, permission.ACCESS_WIFI_STATE)) {
            WifiInfo info = wifi.getConnectionInfo();
            return info.getMacAddress();
        } else {
            return DEFALT_MAC;
        }
    }

    private static boolean isNetworkAlive(Context context) {
        if (PermissionUtils.checkPermission(context, permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null) {
                    return ni.isConnected();
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private static boolean isWifiAlive(Context context) {
        if (PermissionUtils.checkPermission(context, permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifiNetwork != null) {
                    if (wifiNetwork.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
