package com.analysys.dev.internal.utils;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.analysys.dev.BuildConfig;
import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.sp.SPHelper;
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
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Locale;

import static java.lang.Runtime.getRuntime;

public class DeviceInfo {

  /**
   * 系统名称
   */
  public static String getSystemName() {
    return "Android";
  }

  /**
   * 系统版本
   */
  public static String getSystemVersion() {
    return Build.VERSION.RELEASE;
  }

  /**
   * 设备品牌
   */
  public static String getDeviceBrand() {
    return Build.BRAND;
  }

  /**
   * 设备Id 由IMEI-IMSI-AndroidId组成
   */
  public static String getDeviceId(Context context) {
    String deviceId = null;
    if (context != null) {
      String imei = null, imsi = null;
      if (PermissionUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        imei = tm.getDeviceId();
        imsi = tm.getSubscriberId();
      }
      String androidId = android.provider.Settings.System.getString(
          context.getContentResolver(), Settings.Secure.ANDROID_ID);
      deviceId = (TextUtils.isEmpty(imei) ? "null" : imei)
          + "-" + (TextUtils.isEmpty(imsi) ? "null" : imsi)
          + "-" + (TextUtils.isEmpty(androidId) ? "null" : androidId);
    }
    return deviceId;
  }

  /**
   * 设备型号
   */
  public static String getDeviceModel() {
    return Build.MODEL;
  }

  private static final String DEFALT_MAC = "02:00:00:00:00:00";
  private static final String[] FILE_LIST =
      {
          Base64.encodeToString("/sys/class/net/wlan0/address".getBytes(), Base64.DEFAULT),
          Base64.encodeToString("/sys/class/net/eth0/address".getBytes(), Base64.DEFAULT),
          Base64.encodeToString("/sys/devices/virtual/net/wlan0/address".getBytes(), Base64.DEFAULT)
      };

  /**
   * MAC 地址
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

  /**
   * 设备序列号,SerialNumber
   */
  public static String getSerialNumber(Context context) {
    String serialNumber = null;
    try {

        if (PermissionUtils.checkPermission(context,permission.READ_PHONE_STATE)){
          TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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
  public static String getResolution(Context context) {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    return displayMetrics.widthPixels + "-" + displayMetrics.heightPixels;
  }

  /**
   * 运营商名称（中文）,如:中国联通
   */
  public static String getMobileOperator(Context context) {
    String operatorName = null;
    try {
      TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      operatorName = tm.getSimOperator();
    } catch (Throwable e) {
    }
    return operatorName;
  }

  /**
   * 运行商名称（英文）如:CHINA MOBILE
   */
  public static String getMobileOperatorName(Context context) {
    String operatorName = null;
    try {
      TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      operatorName = tm.getSimOperatorName();
    } catch (Throwable e) {
    }
    return operatorName;
  }

  /**
   * 运营商编码
   */
  public static String getNetworkOperatorCode(Context context) {
    String operatorCode = null;
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    operatorCode = tm.getNetworkOperator();
    return operatorCode;
  }

  /**
   * 运营商编码
   */
  public static String getNetworkOperatorName(Context context) {
    String operatorCode = null;
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    operatorCode = tm.getNetworkOperatorName();
    return operatorCode;
  }

  /**
   * 多卡IMEI
   */
  public static String getImeis(Context context) {
    try {
      TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      Class clazz = tm.getClass();
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
  public static String getImsis(Context context) {
    TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    Class<?> telephonyClass;
    try {
      telephonyClass = Class.forName(telephony.getClass().getName());
      Method m2 = telephonyClass.getMethod("getSubscriberId", new Class[] { int.class });
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

  /**
   * 推广渠道
   */
  public static String getApplicationChannel() {
    return null;
  }

  /**
   * 样本应用key
   */
  public static String getApplicationKey() {
    return null;
  }

  /**
   * 应用名称
   */
  public static String getApplicationName(Context context) {
    String applicationName = null;
    try {
      PackageManager packageManager = context.getApplicationContext().getPackageManager();
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
      applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
    } catch (Throwable e) {
    }
    return applicationName;
  }

  /**
   * API等级
   */
  public static String getAPILevel() {
    return String.valueOf(Build.VERSION.SDK_INT);
  }

  /**
   * 应用包名
   */
  public static String getApplicationPackageName(Context context) {
    return BuildConfig.APPLICATION_ID;
  }

  /**
   * SDK版本
   */
  public static String getSdkVersion() {
    return EDContext.SDK_VERSION;
  }

  /**
   * 应用版本名称|版本号
   */
  public static String getApplicationVersionCode() {
    return BuildConfig.VERSION_NAME + "|" + BuildConfig.VERSION_CODE;
  }

  /**
   * 获取对应mContext应用的认证指文
   */
  public static String getAppMD5(Context context) {
    try {
      Signature sig = getSignature(context);
      String md5Fingerprint = doFingerprint(sig.toByteArray());
      return md5Fingerprint;
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * App签名MD5值
   */
  private static String doFingerprint(byte[] certificateBytes) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(certificateBytes);
      byte[] bytes = md.digest();
      return byteArrayToString(bytes);
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * App签名信息
   */

  public static String getAppSign(Context context) {
    try {
      Signature sig = getSignature(context);
      byte[] cert = sig.toByteArray();
      MessageDigest md = MessageDigest.getInstance("SHA1");
      byte[] bytes = md.digest(cert);
      return byteArrayToString(bytes);
    } catch (Throwable e) {
      return null;
    }
  }

  private static Signature getSignature(Context context) {
    try {
      PackageManager pm = context.getPackageManager();
      PackageInfo packageInfo = pm.getPackageInfo(
          context.getPackageName(), PackageManager.GET_SIGNATURES);
      Signature sig = packageInfo.signatures[0];
      return sig;
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * byte数组转String
   */
  private static String byteArrayToString(byte[] bytes) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < bytes.length; i++) {
      if (i != 0) {
        sb.append(":");
      }
      String hex = Integer.toHexString(
          bytes[i] & 0xff).toUpperCase(Locale.US);
      if (hex.length() == 1) {
        sb.append("0");
      }
      sb.append(hex);
    }
    return sb.toString();
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
