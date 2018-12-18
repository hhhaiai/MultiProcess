package com.analysys.dev.internal.impl;

import static java.lang.Runtime.getRuntime;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Locale;

import org.json.JSONObject;

import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.utils.PermissionUtils;
import com.analysys.dev.internal.utils.reflectinon.EContextHelper;
import com.analysys.dev.internal.utils.simulator.SimulatorUtils;
import com.analysys.dev.internal.utils.sp.SPHelper;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;

public class DeviceImpl {

    private Context mContext;

    final String ZERO = "0";
    final String ONE = "1";

    private static class Holder {
        private static final DeviceImpl INSTANCE = new DeviceImpl();
    }

    public static DeviceImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    public JSONObject getDeviceInfo() {

        return null;
    }

    /**
     * 设备硬件信息
     */
    public class DeviceHardware {

        /**
         * 系统名称
         */
        public String getSystemName() {
            return "Android";
        }

        /**
         * 系统版本
         */
        public String getSystemVersion() {
            return Build.VERSION.RELEASE;
        }

        /**
         * 设备品牌
         */
        public String getDeviceBrand() {
            return Build.BRAND;
        }

        /**
         * 设备Id 由IMEI-IMSI-AndroidId组成
         */
        @SuppressWarnings("deprecation")
        public String getDeviceId() {
            String deviceId = null;
            if (mContext != null) {
                String imei = null, imsi = null;
                if (PermissionUtils.checkPermission(mContext, Manifest.permission.READ_PHONE_STATE)) {
                    TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
                    imei = tm.getDeviceId();
                    imsi = tm.getSubscriberId();
                }
                String androidId = android.provider.Settings.System.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
                deviceId = (TextUtils.isEmpty(imei) ? "null" : imei) + "-" + (TextUtils.isEmpty(imsi) ? "null" : imsi)
                    + "-" + (TextUtils.isEmpty(androidId) ? "null" : androidId);
            }
            return deviceId;
        }

        /**
         * 设备型号
         */
        public String getDeviceModel() {
            return Build.MODEL;
        }

        private final String DEFALT_MAC = "02:00:00:00:00:00";
        private final String[] FILE_LIST =
            {Base64.encodeToString("/sys/class/net/wlan0/address".getBytes(), Base64.DEFAULT),
                Base64.encodeToString("/sys/class/net/eth0/address".getBytes(), Base64.DEFAULT),
                Base64.encodeToString("/sys/devices/virtual/net/wlan0/address".getBytes(), Base64.DEFAULT)};

        /**
         * MAC 地址
         */
        public String getMac() {
            if (mContext == null) {
                return null;
            }
            String mac = DEFALT_MAC;
            try {
                if (Build.VERSION.SDK_INT < 23) {
                    mac = getMacByAndridAPI();
                } else {
                    if (isWifiAlive()) {
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
                SPHelper.getDefault(mContext).edit().putString("", "").commit();
            }
            return mac;
        }

        /**
         * MAC 地址
         */
        private String getMacByAndridAPI() {
            WifiManager wifi = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
            if (PermissionUtils.checkPermission(mContext, permission.ACCESS_WIFI_STATE)) {
                WifiInfo info = wifi.getConnectionInfo();
                return info.getMacAddress();
            } else {
                return DEFALT_MAC;
            }
        }

        @SuppressWarnings("deprecation")
        private boolean isWifiAlive() {
            if (PermissionUtils.checkPermission(mContext, permission.ACCESS_NETWORK_STATE)) {
                ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

        /**
         * 需要打开wifi才能获取
         *
         * @throws SocketException
         */
        @TargetApi(9)
        private String getMacByJavaAPI() throws SocketException {
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

        /**
         * android 9以上没权限获取
         *
         * @throws IOException
         */
        private String getMacFile() throws IOException {
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

        private String getMacByShell() {
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
         * 设备序列号,SerialNumber
         */
        public String getSerialNumber() {
            String serialNumber = null;
            try {

                if (PermissionUtils.checkPermission(mContext, permission.READ_PHONE_STATE)) {
                    TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
                    String simSerialNum = tm.getSimSerialNumber();
                    return simSerialNum;
                }
            } catch (Throwable e) {
            }
            return serialNumber;
        }

        /**
         * 分辨率
         */
        public String getResolution() {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            return displayMetrics.widthPixels + "-" + displayMetrics.heightPixels;
        }
    }

    /**
     * 运营商信息
     */
    public class MobileOperator {
        /**
         * 运营商名称（中文）,如:中国联通
         */
        public String getMobileOperator() {
            String operatorName = null;
            try {
                TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
                operatorName = tm.getSimOperator();
            } catch (Throwable e) {
            }
            return operatorName;
        }

        /**
         * 运行商名称（英文）如:CHINA MOBILE
         */
        public String getMobileOperatorName() {
            String operatorName = null;
            try {
                TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
                operatorName = tm.getSimOperatorName();
            } catch (Throwable e) {
            }
            return operatorName;
        }

        /**
         * 运营商编码
         */
        public String getNetworkOperatorCode() {
            String operatorCode = null;
            TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            operatorCode = tm.getNetworkOperator();
            return operatorCode;
        }

        /**
         * 运营商编码
         */
        public String getNetworkOperatorName() {
            String operatorCode = null;
            TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            operatorCode = tm.getNetworkOperatorName();
            return operatorCode;
        }

        /**
         * 多卡IMEI
         */
        public String getIMEI() {
            try {
                TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
                Class<?> clazz = tm.getClass();
                Method getImei = clazz.getDeclaredMethod("getImei", int.class);
                Object imei1 = getImei.invoke(tm, 0);
                Object imei2 = getImei.invoke(tm, 1);
                if (imei1 != null && imei2 != null) {
                    return imei1 + "|" + imei2;
                } else if (imei1 == null && imei2 == null) {
                    return "";
                } else {
                    if (imei1 == null) {
                        return String.valueOf(imei2);
                    } else {
                        return String.valueOf(imei1);
                    }
                }
            } catch (Throwable e) {
            }
            return "";
        }

        /**
         * 多卡IMSI
         */
        public String getIMSI() {
            TelephonyManager telephony = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            Class<?> telephonyClass;
            try {
                telephonyClass = Class.forName(telephony.getClass().getName());
                Method m2 = telephonyClass.getMethod("getSubscriberId", new Class[] {int.class});
                Object imsi1 = m2.invoke(telephony, 0);
                Object imsi2 = m2.invoke(telephony, 1);
                if (imsi1 != null && imsi2 != null) {
                    return imsi1 + "|" + imsi2;
                } else if (imsi1 == null && imsi2 == null) {
                    return "";
                } else {
                    if (imsi1 == null) {
                        return String.valueOf(imsi2);
                    } else {
                        return String.valueOf(imsi1);
                    }
                }
            } catch (Throwable e) {
            }
            return "";
        }
    }

    /**
     * 样本应用key
     */
    public String getApplicationKey() {
        return SPHelper.getDefault(mContext).getString(EDContext.SP_APP_KEY, "");
    }

    /**
     * 推广渠道
     */
    public String getApplicationChannel() {
        return SPHelper.getDefault(mContext).getString(EDContext.SP_APP_CHANNEL, "");
    }

    /**
     * 应用信息
     */
    public class Aplication {

        private static final String UNKNOW = "";

        /**
         * 应用名称
         */
        public String getApplicationName() {
            try {
                PackageManager packageManager = mContext.getApplicationContext().getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
                return (String)packageManager.getApplicationLabel(applicationInfo);
            } catch (Throwable e) {
            }
            return UNKNOW;
        }

        /**
         * API等级
         */
        public String getAPILevel() {
            return String.valueOf(Build.VERSION.SDK_INT);
        }

        /**
         * 应用包名
         */
        public String getApplicationPackageName() {
            try {
                return mContext.getPackageName();
            } catch (Throwable e) {
            }
            return UNKNOW;
        }

        /**
         * SDK版本
         */
        public String getSdkVersion() {
            return EDContext.SDK_VERSION;
        }

        /**
         * 应用版本名称|版本号
         */
        @SuppressWarnings("deprecation")
        public String getApplicationVersionCode() {
            try {
                PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                int versionCode = pInfo.versionCode;
                String versionName = pInfo.versionName;
                return versionName + "|" + versionCode;
            } catch (NameNotFoundException e) {
            }
            return UNKNOW;
        }

        /**
         * 获取对应mContext应用的认证指文
         */
        public String getAppMD5() {
            try {
                Signature sig = getSignature();
                String md5Fingerprint = doFingerprint(sig.toByteArray());
                return md5Fingerprint;
            } catch (Throwable e) {
            }
            return UNKNOW;
        }

        /**
         * App签名MD5值
         */
        public String doFingerprint(byte[] certificateBytes) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(certificateBytes);
                byte[] bytes = md.digest();
                return byteArrayToString(bytes);
            } catch (Throwable e) {
            }
            return UNKNOW;
        }

        /**
         * App签名信息
         */

        public String getAppSign() {
            try {
                Signature sig = getSignature();
                byte[] cert = sig.toByteArray();
                MessageDigest md = MessageDigest.getInstance("SHA1");
                byte[] bytes = md.digest(cert);
                return byteArrayToString(bytes);
            } catch (Throwable e) {
            }
            return UNKNOW;
        }

        @SuppressWarnings("deprecation")
        private Signature getSignature() {
            try {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
                Signature sig = packageInfo.signatures[0];
                return sig;
            } catch (Throwable e) {
            }
            return null;
        }
    }

    /**
     * 获取eguan id
     */
    public String getEguanID() {

        return null;
    }

    /**
     * 获取临时id
     */
    public String getTempID() {
        return null;
    }

    /**
     * 防止刷量作弊信息
     */
    public class PreventCheat {

        /**
         * 判断是否是模拟器，"0”= 不是模拟器“1”= 是模拟器
         */
        public String isSimulator() {
            // 检查设备的设备ID与常见的模拟器ID是否相同,如果相同,则为模拟器
            if (SimulatorUtils.hasKnownDeviceId(mContext)
                // 检查设备的IMSI号与常见的模拟器IMSI是否相同,如果相同,则为模拟器
                || SimulatorUtils.hasKnownImsi(mContext)
                // 检查设备的板载,品牌,工业设计,硬件等信息是否匹配模拟器的信息,如果相同,则为模拟器
                || SimulatorUtils.hasEmulatorBuild(mContext)
                // 检查设备的手机号,是否与常见的模拟器加载的手机号相同,如果相同,则为模拟器
                || SimulatorUtils.hasKnownPhoneNumber(mContext)
                // 检查设备是否有模拟器特有的pipe目录,如果有,则为模拟器
                || SimulatorUtils.hasPipes()
                // 同上,检查设备是否有模拟器特有的QEmu目录,如果有,则为模拟器
                || SimulatorUtils.hasQEmuFiles()
                // 同上,检查设备是否有模拟器特有对应的QEmu设备对应的目录,如果有则为模拟器
                || SimulatorUtils.hasQEmuDrivers()
                // 通过读取proc/net/tcp查看adb是否对应模拟器,如果对应,则为模拟器
                || SimulatorUtils.hasEmulatorAdb()
                // 检查设备上是否有模拟器目录,如果有,则为模拟器
                || SimulatorUtils.hasGenyFiles()
                // 检查设备上否有模拟器相关的属性,如果有,且超过5个,则表示为模拟器
                || SimulatorUtils.hasQEmuProps(mContext)
                // 检查设备上的网络连接状态是否为eth0,如果是,则为模拟器
                || SimulatorUtils.hasEmulatorWifi()
                // 通过cpu的类型来判断是否为模拟器,如果满足,其中一种类型,则为模拟器
                || SimulatorUtils.checkEmulatorByCpuInfo()) {
                return ONE;
            }
            return ZERO;
        }

        /**
         * 判断设备本身、APP、以及工作环境是否是被调试状态，“0”= 不在调试状态“1”= 在调试状态
         */
        public String getDebug() {
            return null;
        }

        /**
         * 判断设备的OS是否被劫持，"0”= 没有被劫持“1”= 被劫持
         */
        public String isHijack() {
            return null;
        }

        /**
         * 是否root，值为1表示获取root权限；值为0表示没获取root权限
         */
        public String IsJailbreak() {
            try {
                if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                    return ZERO;
                }
            } catch (Throwable e) {
            }
            return ONE;
        }
    }

    /**
     * 蓝牙信息
     */
    public class Bluetooth {
        /**
         * 蓝牙MAC，如“6c:5c:14:25:be:ba”
         */
        public String getBluetoothMac() {
            return null;
        }

        /**
         * 蓝牙信息
         */
        public String getBluetoothName() {
            return null;
        }
    }

    /**
     * 电池相关信息
     */
    public class Battery {
        /**
         * 电源状态，下面有iOS和Android的传值与对应电源状态的码表
         */
        public String getBatteryStatus() {

            return null;
        }

        /**
         * @return
         */
        public String getBatteryHealth() {

            return null;
        }

        /**
         * @return
         */
        public String getBatteryLevel() {

            return null;
        }

        /**
         * @return
         */
        public String getBatteryScale() {

            return null;
        }

        /**
         * @return
         */
        public String getBatteryPlugged() {

            return null;
        }

        /**
         * @return
         */
        public String getBatteryTechnology() {

            return null;
        }
    }

    /**
     * byte数组转String
     */
    private String byteArrayToString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                sb.append(":");
            }
            String hex = Integer.toHexString(bytes[i] & 0xff).toUpperCase(Locale.US);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public boolean isNetworkAlive() {
        if (PermissionUtils.checkPermission(mContext, permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null) {
                    return ni.isConnected();
                }
            }
        }
        return false;
    }
}
