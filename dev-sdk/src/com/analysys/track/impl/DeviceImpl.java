package com.analysys.track.impl;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.analysys.track.impl.proc.DoubleCardSupport;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.model.BatteryModuleNameInfo;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.FileUtils;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import static java.lang.Runtime.getRuntime;

public class DeviceImpl {

    private Context mContext;

    private final String ZERO = "0";
    private final String ONE = "1";
    private final String[] IMSIS = {"00000000000000", "00000000", "000000000000000", "00000"};

    private DeviceImpl() {
    }

    private static class Holder {
        private static final DeviceImpl INSTANCE = new DeviceImpl();
    }

    public static DeviceImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

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
        String version = "";
        try {
            version = Build.VERSION.RELEASE;
        } catch (Throwable t) {
            version = "";
        }
        return version;
    }

    /**
     * 设备品牌
     */
    public String getDeviceBrand() {
        String brand = "";
        try {
            brand = Build.BRAND;
        } catch (Throwable t) {
            brand = "";
        }
        return brand;
    }

    /**
     * 设备Id 由IMEI-IMSI-AndroidId组成
     */
    public String getDeviceId() {
        String deviceId = "";
        try {
            if (mContext != null) {
                String imei = "", imsi = "";
                if (PermissionUtils.checkPermission(mContext, Manifest.permission.READ_PHONE_STATE)) {
                    TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                    imei = tm.getDeviceId();
                    imsi = tm.getSubscriberId();
                }
                String androidId = android.provider.Settings.System.getString(mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                deviceId = (TextUtils.isEmpty(imei) ? "null" : imei) + "-" + (TextUtils.isEmpty(imsi) ? "null" : imsi)
                        + "-" + (TextUtils.isEmpty(androidId) ? "null" : androidId);
            }
        } catch (Throwable t) {
            deviceId = "";
        }

        return deviceId;
    }

    /**
     * 设备型号
     */
    public String getDeviceModel() {
        String model = "";
        try {
            model = Build.MODEL;
        } catch (Throwable t) {
            model = "";
        }
        return model;
    }

    private final String DEFALT_MAC = "";
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
                if (NetworkUtils.isWifiAlive(mContext)) {
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
            SPHelper.setStringValue2SP(mContext, EGContext.SP_MAC_ADDRESS, mac);
        }
        return mac;
    }

    /**
     * MAC 地址
     */
    private String getMacByAndridAPI() {
        String macAddress = "";
        try {
            WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            if (PermissionUtils.checkPermission(mContext, permission.ACCESS_WIFI_STATE)) {
                WifiInfo info = wifi.getConnectionInfo();
                macAddress = info.getMacAddress();

            } else {
                macAddress = DEFALT_MAC;
            }
        } catch (Throwable t) {
            macAddress = DEFALT_MAC;
        }
        return macAddress;
    }

    /**
     * 需要打开wifi才能获取
     *
     * @throws SocketException
     */
    @TargetApi(9)
    private String getMacByJavaAPI() throws SocketException {
        String mac = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netInterface = interfaces.nextElement();
                if ("wlan0".equals(netInterface.getName()) || "eth0".equals(netInterface.getName())) {
                    byte[] addr = netInterface.getHardwareAddress();
                    if (addr == null || addr.length == 0) {
                        return "";
                    }
                    StringBuilder buf = new StringBuilder();
                    for (byte b : addr) {
                        buf.append(String.format("%02X:", b));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    mac = String.valueOf(buf).toLowerCase(Locale.getDefault());
                }
            }
        } catch (Throwable t) {
            mac = DEFALT_MAC;
        }

        return mac;
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
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.e(e);
                }
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
        StringBuilder sb;
        try {
            sb = new StringBuilder();
            for (int i = 0; i < FILE_LIST.length; i++) {
                proc = getRuntime().exec("cat " + new String(Base64.decode(FILE_LIST[i], Base64.DEFAULT)));
                in = new BufferedInputStream(proc.getInputStream());
                br = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                if (sb.length() > 0) {
                    return String.valueOf(sb);
                }
            }
        } catch (Exception e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {

                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return DEFALT_MAC;
    }

    /**
     * 设备序列号,SerialNumber
     */
    public String getSerialNumber() {
        String result = "";
        try {
            if (Build.VERSION.SDK_INT > 26) {

                Class<?> clazz = Class.forName("android.os");
                Method method = clazz.getMethod("getSerial");
                result = (String) method.invoke(null);
            } else {
                result = Build.SERIAL;
            }
        } catch (Throwable e) {
            result = "";
        }
        return result;
    }

    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics;
        try {
            displayMetrics = mContext.getApplicationContext().getResources().getDisplayMetrics();
        } catch (Throwable t) {
            displayMetrics = null;
        }
        return displayMetrics;
    }

    /**
     * 分辨率
     */
    public String getResolution() {
        String res = "";
        try {
            res = getDisplayMetrics().widthPixels + "-" + getDisplayMetrics().heightPixels;
        } catch (Throwable t) {
            res = "";
        }
        return res;
    }

    public String getDotPerInch() {
        String dpi = "";
        try {
            dpi = String.valueOf(getDisplayMetrics().densityDpi);
        } catch (Throwable t) {
            dpi = "";
        }
        return dpi;
    }

    // 运营商信息

    /**
     * 运营商名称（中文）,如:中国联通
     */
    public String getMobileOperator() {
        String operatorName = "";
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            operatorName = tm.getSimOperator();
        } catch (Throwable e) {
        }
        return operatorName;
    }

    /**
     * 运行商名称（英文）如:CHINA MOBILE
     */
    public String getMobileOperatorName() {
        String operatorName = "";
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            operatorName = tm.getSimOperatorName();
        } catch (Throwable e) {
            operatorName = "";
        }
        return operatorName;
    }

    /**
     * 运营商编码
     */
    public String getNetworkOperatorCode() {
        String operatorCode = "";
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            operatorCode = tm.getNetworkOperator();

        } catch (Throwable t) {
            operatorCode = "";
        }
        if ("00000".equals(operatorCode)) {
            operatorCode = "";
        }
        return operatorCode;
    }

    /**
     * 接入运营商名字
     */
    public String getNetworkOperatorName() {
        String operatorCode = "";
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            operatorCode = tm.getNetworkOperatorName();
        } catch (Throwable t) {
            operatorCode = "";
        }
        return operatorCode;
    }

    /**
     * 获取IMEI,一个或者两个.需要优化完善.未兼容三卡
     */
    public String getIMEIS(Context context) {
        try {
            List<String> imeis = DoubleCardSupport.getIMEIS(context);
            if (imeis != null && imeis.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (String ime : imeis) {
                    // 防止电信MEID为空。 典型Lg
                    if (!TextUtils.isEmpty(ime)) {
                        ime = ime.replaceAll(" ", "");
                        if (!TextUtils.isEmpty(ime) && !defaultImsis().contains(ime)) {
                            sb.append(ime).append("|");
                        }
                    }
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                return String.valueOf(sb);
            }
        } catch (Throwable e) {
        }
        return "";
    }

    /**
     * 多卡IMSI
     */
    public String getIMSIS(Context context) {
        try {
            List<String> imsis = DoubleCardSupport.getIMSIS(context);
            if (imsis != null && imsis.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (String ims : imsis) {
                    if (!TextUtils.isEmpty(ims)) {
                        ims = ims.replaceAll(" ", "");
                        if (!TextUtils.isEmpty(ims) && !defaultImsis().contains(ims)) {
                            sb.append(ims).append("|");
                        }
                    }
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                return String.valueOf(sb);
            }
        } catch (Throwable e) {
        }
        return "";
    }
    // SettingInfoImpl

    /**
     * 推广渠道
     */
    public String getApplicationChannel() {
        return SystemUtils.getAppChannel(mContext);
    }

    private Set<String> defaultImsis() {
        Set<String> imsis = new HashSet<String>();
        for (String imsi : IMSIS) {
            imsis.add(imsi);
        }
        return imsis;
    }

    /**
     * 样本应用key
     */
    public String getApplicationKey() {
        return SystemUtils.getAppKey(mContext);
    }

    // 应用信息SoftwareInfoImpl
    private static final String UNKNOW = "";

    /**
     * 应用名称
     */
    public String getApplicationName() {
        try {
            PackageManager packageManager = mContext.getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
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
        return EGContext.SDK_VERSION;
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
        return "";
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
        return "";
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

    /**
     * 判断设备本身、APP、以及工作环境是否是被调试状态，“0”= 不在调试状态“1”= 在调试状态
     */
    public String getDebug() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            ApplicationInfo appinfo = packageInfo.applicationInfo;
            if (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                return ZERO;
            }
        } catch (Exception e) {
            return ONE;
        }
        return ONE;
    }

    /**
     * 判断设备的OS是否被劫持，"0”= 没有被劫持“1”= 被劫持
     */
    public String isHijack() {
        // 是否装xpose等
        return SystemUtils.byLoadXposedClass() == true ? "1" : "0";
    }

    /**
     * 是否root，值为1表示获取root权限；值为0表示没获取root权限
     */
    public String IsRoot() {
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                return ZERO;
            }
        } catch (Throwable e) {
            return ZERO;
        }
        return ONE;
    }

    // 蓝牙信息BluetoothModuleNameImpl

    /**
     * 蓝牙MAC，如“6c:5c:14:25:be:ba”
     */
    public String getBluetoothMac() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT < 23) {
            return adapter.getAddress();
        } else {
            String macSerial = "";
            try {
                Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
                InputStreamReader ir = new InputStreamReader(pp.getInputStream());
                LineNumberReader input = new LineNumberReader(ir);
                String str = "";
                while ((str = input.readLine()) != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            } catch (Exception ex) {
                return "02:00:00:00:00:00";
            }
            if ("".equals(macSerial)) {
                try {
                    return FileUtils.loadFileAsString("/sys/class/net/eth0/address").toUpperCase(Locale.getDefault()).substring(0, 17);
                } catch (Exception e) {
                    return "";
                }
            }
            return macSerial;
        }
    }

    /**
     * 蓝牙信息
     */
    public String getBluetoothName() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            return adapter.getName();
        } catch (Throwable t) {
            return "";
        }
    }

    // 电池相关信息BatteryModuleNameImpl

    public void processBattery(final Intent intent) {
        try {
            int status = intent.getIntExtra("status", 0);
            int health = intent.getIntExtra("health", 0);
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 0);
            int plugged = intent.getIntExtra("plugged", 0);
            String technology = intent.getStringExtra("technology");
            int temperature = intent.getIntExtra("temperature", 0);
            BatteryModuleNameInfo info = BatteryModuleNameInfo.getInstance();
            info.setBatteryStatus(String.valueOf(status));
            // 电源健康状态
            info.setBatteryHealth(String.valueOf(health));
            // 电源发前电量
            info.setBatteryLevel(String.valueOf(level));
            // 电源总电量
            info.setBatteryScale(String.valueOf(scale));
            // 电源充电状态
            info.setBatteryPlugged(String.valueOf(plugged));
            // 电源类型
            info.setBatteryTechnology(technology);
            // 电池温度
            info.setBatteryTemperature(String.valueOf(temperature));
        } catch (Throwable e) {

        }
    }

    /**
     * 系统字体大小
     *
     * @return
     */
    public String getSystemFontSize() {
        try {
            Configuration mCurConfig = null;
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Object obj = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
            Method method = obj.getClass().getMethod("getConfiguration");
            mCurConfig = (Configuration) method.invoke(obj);
            return mCurConfig.fontScale + "";
        } catch (Throwable e) {
            return "0";
        }
    }

    public String getSystemHour() {
        ContentResolver cv = mContext.getContentResolver();
        String timeFormat = android.provider.Settings.System.getString(cv, Settings.System.TIME_12_24);
        return timeFormat;
    }

    public String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public String getSystemArea() {
        return Locale.getDefault().getCountry();
    }

    public String getTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        int offsetMinutes = tz.getRawOffset() / 60000;
        char sign = '+';
        if (offsetMinutes < 0) {
            sign = '-';
            offsetMinutes = -offsetMinutes;
        }
        StringBuilder builder = new StringBuilder(9);
        builder.append("GMT");
        builder.append(sign);
        appendNumber(builder, 2, offsetMinutes / 60);
        builder.append(':');
        appendNumber(builder, 2, offsetMinutes % 60);
        return String.valueOf(builder);
    }

    private void appendNumber(StringBuilder builder, int count, int value) {
        String string = Integer.toString(value);
        for (int i = 0; i < count - string.length(); i++) {
            builder.append('0');
        }
        builder.append(string);
    }

    // DevFurtherdetailImpl
    public String getCPUModel() {
        return Build.CPU_ABI + ":" + Build.CPU_ABI2;
    }

    public String getBuildId() {
        return Build.ID;
    }

    public String getBuildDisplay() {
        return Build.DISPLAY;
    }

    public String getBuildProduct() {
        return Build.PRODUCT;
    }

    public String getBuildDevice() {
        return Build.DEVICE;
    }

    public String getBuildBoard() {
        return Build.BOARD;
    }

    public String getBuildBootloader() {
        return Build.BOOTLOADER;
    }

    public String getBuildHardware() {
        return Build.HARDWARE;
    }

    public String getBuildSupportedAbis() {
        try {
            return stringArrayToString(Build.SUPPORTED_ABIS);
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildSupportedAbis32() {
        try {
            return stringArrayToString(Build.SUPPORTED_32_BIT_ABIS);
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildSupportedAbis64() {
        try {
            return stringArrayToString(Build.SUPPORTED_64_BIT_ABIS);
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildType() {
        try {
            return Build.TYPE;
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildTags() {
        try {
            return Build.TAGS;
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildFingerPrint() {
        try {
            return Build.FINGERPRINT;
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildRadioVersion() {
        try {
            return Build.getRadioVersion();
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildIncremental() {
        try {
            return Build.VERSION.INCREMENTAL;
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildBaseOS() {
        try {
            return Build.VERSION.BASE_OS;
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildSecurityPatch() {
        try {
            return Build.VERSION.SECURITY_PATCH;
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildSdkInt() {
        try {
            return String.valueOf(Build.VERSION.SDK_INT);
        } catch (Throwable t) {
            return "";
        }

    }

    public String getBuildPreviewSdkInt() {
        String value = "";
        try {
            value = String.valueOf(Build.VERSION.PREVIEW_SDK_INT);
        } catch (Throwable t) {
            value = "";
        }
        return value;
    }

    public String getBuildCodename() {
        String codeName = "";
        try {
            codeName = Build.VERSION.CODENAME;
        } catch (Throwable t) {
            codeName = "";
        }
        return codeName;
    }

    public String getIDFA() {
        String idfa = "";
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);// 阻塞调用，需放在子线程处理
                        String advertisingId = adInfo.getId();
                        SPHelper.setStringValue2SP(mContext, EGContext.SP_APP_IDFA, advertisingId);
                    } catch (Exception e) {
                    }
                }
            }).start();
            idfa = SPHelper.getStringValueFromSP(mContext, EGContext.SP_APP_IDFA, "");
            if (!idfa.isEmpty()) {
                return idfa;
            }
        } catch (Throwable t) {
        }

        return idfa;
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
        return String.valueOf(sb);
    }

    private String stringArrayToString(String[] stringArray) {
        StringBuilder sb = null;
        String result = "";
        try {
            sb = new StringBuilder();
            for (int i = 0; i < stringArray.length; i++) {
                sb.append(stringArray[i]);
                sb.append(",");
            }
            result = String.valueOf(sb);
            result = result.substring(0, result.length() - 1);
        } catch (Throwable t) {
            return null;
        }
        return result;
    }
}
