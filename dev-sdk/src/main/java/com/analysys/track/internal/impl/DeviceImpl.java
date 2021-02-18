package com.analysys.track.internal.impl;

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
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.model.BatteryModuleNameInfo;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.OAIDHelper;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.sp.SPHelper;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 设备信息获取类
 * @Version: 1.0
 * @Create: 2019-08-07 14:04:02
 * @author: sanbo
 */
public class DeviceImpl {

    private static final String UNKNOW = "";
    public List<String> minEffectiveValue = new ArrayList<String>();
    private Context mContext;

    private DeviceImpl() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 17; i++) {
            sb.append("0");
            if (!minEffectiveValue.contains(sb.toString())) {
                minEffectiveValue.add(sb.toString());
            }
        }

    }

    public static DeviceImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }


    /**
     * 设备Id 由IMEI-IMSI-AndroidId组成
     */
    @SuppressWarnings("deprecation")
    public String getDeviceId() {
        String deviceId = "", imei = "", imsi = "";
        try {
            if (mContext != null) {
                if (BuildConfig.ENABLE_IMEI) {
                    if (PermissionUtils.checkPermission(mContext, Manifest.permission.READ_PHONE_STATE)) {
                        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                        imei = tm.getDeviceId();
                        imsi = tm.getSubscriberId();
                    }
                } else {
                    List<String> imeis = DoubleCardSupport.getInstance().getImeiArray(mContext);
                    if (imeis.size() > 0) {
                        imei = imeis.get(0);
                    }
                    List<String> imsis = DoubleCardSupport.getInstance().getImsisArrays(mContext);
                    if (imsis.size() > 0) {
                        imsi = imsis.get(0);
                    }
                }

            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
        try {
            String androidId = Settings.System.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            deviceId = (TextUtils.isEmpty(imei) ? "null" : imei) + "-" + (TextUtils.isEmpty(imsi) ? "null" : imsi)
                    + "-" + (TextUtils.isEmpty(androidId) ? "null" : androidId);
        } catch (Throwable e) {
        }

        return deviceId;
    }

    public String getOAID() {
        if (Build.VERSION.SDK_INT >= 29) {
            String oaid = SPHelper.getStringValueFromSP(mContext, OAIDHelper.OAID, "");
            if (!TextUtils.isEmpty(oaid)) {
                return oaid;
            }
        }
        return null;
    }


    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics;
        try {
            displayMetrics = mContext.getApplicationContext().getResources().getDisplayMetrics();
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
            res = "";
        }
        return res;
    }

    // 运营商信息

    public String getDotPerInch() {
        String dpi = "";
        try {
            dpi = String.valueOf(getDisplayMetrics().densityDpi);
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
            dpi = "";
        }
        return dpi;
    }

    /**
     * 运营商名称（中文）,如:中国联通
     */
    public String getMobileOperator() {
        String operatorName = "";
        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            operatorName = tm.getSimOperator();
            if (operatorName.isEmpty()) {
                int mcc = mContext.getResources().getConfiguration().mcc;
                if (mcc == 0) {
                    return operatorName;
                }
                int mnc = mContext.getResources().getConfiguration().mnc;
                if (mnc != Configuration.MNC_ZERO) {
                    operatorName = operatorName + mnc;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
            operatorCode = "";
        }
        if (minEffectiveValue.contains(operatorCode)) {
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
            operatorCode = "";
        }
        return operatorCode;
    }


    /**
     * 应用名称
     */
    public String getApplicationName() {
        try {
            PackageManager packageManager = mContext.getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mContext.getPackageName(), 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return UNKNOW;
    }


    /**
     * 应用包名
     */
    public String getApplicationPackageName() {
        try {
            return mContext.getPackageName();
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return UNKNOW;
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
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return UNKNOW;
    }

    @SuppressWarnings("deprecation")
    private Signature getSignature() {
        try {
            PackageManager pm = mContext.getPackageManager();
            if (pm != null) {
                PackageInfo packageInfo = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
                return packageInfo.signatures[0];
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }


    public void processBattery(final Intent intent) {
        try {
            int status = intent.getIntExtra("status", 0);
            int health = intent.getIntExtra("health", 0);
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 0);
            int plugged = intent.getIntExtra("plugged", 0);
            String technology = intent.getStringExtra("technology");
            int temperature = intent.getIntExtra("temperature", 0);

            // 检查设备是否在调试状态
            if (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL) {
                if (plugged == BatteryManager.BATTERY_PLUGGED_USB) {
                    //连接usb且在调试状态
                    EGContext.STATUS_USB_DEBUG = true;
                } else if (plugged == BatteryManager.BATTERY_PLUGGED_AC) {
                    //连接usb在充电
                    EGContext.STATUS_USB_DEBUG = false;
                } else {
                    EGContext.STATUS_USB_DEBUG = false;
                }
            } else {
                //未连接usb
                EGContext.STATUS_USB_DEBUG = false;
            }

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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        //MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_BATTERY_BROADCAST, System.currentTimeMillis());
    }

    // 电池相关信息BatteryModuleNameImpl

    /**
     * 系统字体大小
     *
     * @return
     */
    public String getSystemFontSize() {
        try {
            Configuration mCurConfig = mContext.getResources().getConfiguration();
            return mCurConfig.fontScale + "";
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
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
        String result = "";
        try {
            TimeZone tz = TimeZone.getDefault();
            result = tz.getDisplayName(false, TimeZone.SHORT);
        } catch (Throwable e) {
        }
        return result;
    }

//    private void appendNumber(StringBuilder builder, int count, int value) {
//
//        String string = String.valueOf(value);
//        for (int i = 0; i < count - string.length(); i++) {
//            builder.append('0');
//        }
//        builder.append(string);
//    }


    public String getBuildSupportedAbis() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return stringArrayToString(Build.SUPPORTED_ABIS);
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
        return "";
    }

    public String getBuildSupportedAbis32() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return stringArrayToString(Build.SUPPORTED_32_BIT_ABIS);
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
        return "";
    }

    public String getBuildSupportedAbis64() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return stringArrayToString(Build.SUPPORTED_64_BIT_ABIS);
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
        return "";
    }


    public String getIDFA() {
        String idfa = "";
        try {
            idfa = SPHelper.getStringValueFromSP(mContext, EGContext.SP_APP_IDFA, "");
            if (!idfa.isEmpty()) {
                return idfa;
            }
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);// 阻塞调用，需放在子线程处理
                        if (adInfo != null) {
                            SPHelper.setStringValue2SP(mContext, EGContext.SP_APP_IDFA, adInfo.getId());
                        }

                    } catch (Throwable e) {
                    }
                }
            });
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
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
                if (!TextUtils.isEmpty(stringArray[i])) {
                    sb.append(stringArray[i]).append(",");
                }
            }
            if (sb.length() > 0) {
                result = String.valueOf(sb);
                result = result.substring(0, result.length() - 1);
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
            return null;
        }
        return result;
    }

    public String getUA() {
        String userAgent = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(EContextHelper.getContext());
            } catch (Throwable e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static class Holder {
        private static final DeviceImpl INSTANCE = new DeviceImpl();
    }
}
