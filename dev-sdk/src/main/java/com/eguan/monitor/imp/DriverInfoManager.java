package com.eguan.monitor.imp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.FileUitls;
import com.eguan.monitor.commonutils.SignaturesUtils;
import com.eguan.monitor.commonutils.SystemUtils;
import com.eguan.monitor.simulator.SimulatorUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 设备信息管理器
 */
public class DriverInfoManager {

    private Context context;
    private TelephonyManager tm;

    public DriverInfoManager(Context context) {
        this.context = context;
        tm = (TelephonyManager) context.getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void setDriverInfo() {

        DriverInfo driverInfo = DriverInfo.getInstance();
        /*---------------------------获取系统名称------------------------------*/
        try {
            driverInfo.setSystemName(Constants.SN);
        } catch (Throwable e) {
            driverInfo.setSystemName("");
        }
        /*---------------------------获取设备信息ID------------------------------*/
        try {
            driverInfo.setDeviceId(getDiviceID(3));
        } catch (Throwable e) {
            driverInfo.setDeviceId("null-null-null");
        }
        /*---------------------------获取MAC地址------------------------------*/
        try {
            driverInfo.setMACAddress(getMacAddress());
        } catch (Throwable e) {
            driverInfo.setMACAddress("");
        }
        /*---------------------------获取序列号------------------------------*/
        try {
            driverInfo.setSerialNumber(getSerialNumber());
        } catch (Throwable e) {
            driverInfo.setSerialNumber("");
        }
        /*------------------------------获取品牌名------------------------------*/
        try {
            driverInfo.setDeviceBrand(Build.BRAND);
        } catch (Throwable e) {
            driverInfo.setDeviceBrand("");
        }
        /*------------------------------获取设备型号------------------------------*/
        try {
            driverInfo.setDeviceModel(Build.MODEL);
        } catch (Throwable e) {
            driverInfo.setDeviceModel("");
        }
        /*------------------------------获取系统版本号-------------------------------*/
        try {
            driverInfo.setSystemVersion(Build.VERSION.RELEASE);
        } catch (Throwable e) {
            driverInfo.setSystemVersion("");
        }
        /*------------------------------是否已root-------------------------------*/
        try {
            driverInfo.setIsJailbreak(isRoot());
        } catch (Throwable e) {
            driverInfo.setIsJailbreak("0");
        }
        /*------------------------------样本应用版本号-------------------------------*/
        try {
            driverInfo.setApplicationVersionCode(getAppVersion());
        } catch (Throwable e) {
            driverInfo.setApplicationVersionCode("");
        }
        /*------------------------------样本应用包名-------------------------------*/
        try {
            driverInfo.setApplicationPackageName(context.getPackageName());
        } catch (Throwable e) {
            driverInfo.setApplicationPackageName("");
        }
        /*--------------------------样本应用名称-------------------------------*/
        try {
            driverInfo.setApplicationName(getApplicationName(context));
        } catch (Throwable e) {
            driverInfo.setApplicationName("");
        }
        /*--------------------------样本应用key，由易观分配-------------------------------*/
        try {
            driverInfo.setApplicationKey(SystemUtils.getAppKey(context));
        } catch (Throwable e) {
            driverInfo.setApplicationKey("");
        }
        /*------------------------------样本应用推广渠道-------------------------------*/
        try {
            driverInfo.setApplicationChannel(SystemUtils.getAppChannel(context));
        } catch (Throwable e) {
            driverInfo.setApplicationChannel("");
        }
        /*------------------------------样本应用UserId-------------------------------*/
        try {
            driverInfo.setApplicationUserId("");
        } catch (Throwable e) {
            driverInfo.setApplicationUserId("");
        }
        /*--------------------------获取API等级-------------------------------*/
        try {
            driverInfo.setApiLevel(String.valueOf(Build.VERSION.SDK_INT));
        } catch (Throwable e) {
            driverInfo.setApiLevel("");
        }
        /*------------------------获取运营商-------------------------------------*/
        try {
            driverInfo.setMobileOperator(getMobileOperator());
        } catch (Throwable e) {
            driverInfo.setMobileOperator("");
        }
        /*---------------------------获取手机号码-------------------------------------------*/
        // driverInfo.setPhoneNum(tm.getLine1Number());//取消获取手机号
        driverInfo.setPhoneNum("");
        /*--------------------------3.7.0.0--------------------------------*/
        try {
            driverInfo.setResolution(getResolution());
        } catch (Throwable e) {
            driverInfo.setResolution("");
        }
        try {
            driverInfo.setSystemFontSize(getSystemFontSize());
        } catch (Throwable e) {
            driverInfo.setSystemFontSize("");
        }
        try {
            driverInfo.setTimeZone(getTimeZone());
        } catch (Throwable e) {
            driverInfo.setTimeZone("");
        }
        try {
            driverInfo.setSystemArea(getSystemArea());
        } catch (Throwable e) {
            driverInfo.setSystemArea("");
        }
        try {
            driverInfo.setSystemLanguage(getSystemLanguage());
        } catch (Throwable e) {
            driverInfo.setSystemLanguage("");
        }
        try {
            driverInfo.setSystemHour(getSystemHour());
        } catch (Throwable e) {
            driverInfo.setSystemHour("");
        }
        try {
            driverInfo.setBluetoothMAC(getBluetoochMac(context));
        } catch (Throwable e) {
            driverInfo.setBluetoothMAC("");
        }
        driverInfo.setDebug(0);

        try {
            boolean isSimulator = isSimulator(context);
            driverInfo.setSimulator(isSimulator ? 1 : 0);
            if (isSimulator) {
                driverInfo.setSimulatorDescription(getSimulatorDescription(context));
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

        driverInfo.setHjack(0);//设备OS是否被劫持
        try {
            driverInfo.setIMEIS(getIMEIS(context));//获取多卡信息
        } catch (Throwable e) {
            driverInfo.setIMEIS("");
        }
        try {
            driverInfo.setMobileOperatorName(getMobileOperatorName());
        } catch (Throwable e) {
            driverInfo.setMobileOperatorName("");
        }
        try {
            driverInfo.setNetworkOperatorName(getNetworkOperatorName());
        } catch (Throwable e) {
            driverInfo.setNetworkOperatorName("");
        }
        try {
            driverInfo.setNetworkOperatorCode(getNetworkOperatorCode());
        } catch (Throwable e) {
            driverInfo.setNetworkOperatorCode("");
        }
        //对未捉取的数据调协初始值
        driverInfo.setApplicationUserId("");
        try {
            driverInfo.setIMSI(getDiviceID(0));
        } catch (Throwable e) {
            driverInfo.setIMSI("");
        }
        try {
            driverInfo.setAndroidID(getDiviceID(1));
        } catch (Throwable e) {
            driverInfo.setAndroidID("");
        }
        try {
            driverInfo.setAppMD5(getAppMD5());
        } catch (Throwable e) {
            driverInfo.setAppMD5("");
        }
        try {
            driverInfo.setAppSign(getAppSign());
        } catch (Throwable e) {
            driverInfo.setAppSign("");
        }

        try {
            driverInfo.setBI(Build.ID);
        } catch (Throwable t) {
            driverInfo.setBI("");
        }
        try {
            driverInfo.setBD(Build.DISPLAY);
        } catch (Throwable t) {
            driverInfo.setBD("");
        }
        try {
            driverInfo.setBPT(Build.PRODUCT);
        } catch (Throwable t) {
            driverInfo.setBPT("");
        }
        try {
            driverInfo.setBDE(Build.DEVICE);
        } catch (Throwable t) {
            driverInfo.setBDE("");
        }
        try {
            driverInfo.setBB(Build.BOARD);
        } catch (Throwable t) {
            driverInfo.setBB("");
        }
        try {
            driverInfo.setBBL(Build.BOOTLOADER);
        } catch (Throwable t) {
            driverInfo.setBBL("");
        }
        try {
            driverInfo.setBHW(Build.HARDWARE);
        } catch (Throwable t) {
            driverInfo.setBHW("");
        }

        if (Build.VERSION.SDK_INT >= 21) {
            try {
                driverInfo.setBSA(Arrays.toString(Build.SUPPORTED_ABIS));
            } catch (Throwable t) {
                driverInfo.setBSA("");
            }
            try {
                driverInfo.setBSAS(Arrays.toString(Build.SUPPORTED_32_BIT_ABIS));
            } catch (Throwable t) {
                driverInfo.setBSAS("");
            }
            try {
                driverInfo.setBSASS(Arrays.toString(Build.SUPPORTED_64_BIT_ABIS));
            } catch (Throwable t) {
                driverInfo.setBSASS("");
            }
        }

        try {
            driverInfo.setBTE(Build.TYPE);
        } catch (Throwable t) {
            driverInfo.setBTE("");
        }
        try {
            driverInfo.setBTS(Build.TAGS);
        } catch (Throwable t) {
            driverInfo.setBTS("");
        }
        try {
            driverInfo.setBFP(Build.FINGERPRINT);
        } catch (Throwable t) {
            driverInfo.setBFP("");
        }
        if (Build.VERSION.SDK_INT >= 14) {
            try {
                driverInfo.setBRV(Build.getRadioVersion());
            } catch (Throwable t) {
                driverInfo.setBRV("");
            }
        }
        try {
            driverInfo.setBIR(Build.VERSION.INCREMENTAL);
        } catch (Throwable t) {
            driverInfo.setBIR("");
        }
        try {
            driverInfo.setBBO(Build.VERSION.BASE_OS);

        } catch (Throwable t) {
            driverInfo.setBBO("");
        }
        try {
            driverInfo.setBSP(Build.VERSION.SECURITY_PATCH);
        } catch (Throwable t) {
            driverInfo.setBSP("");
        }
        try {
            driverInfo.setBSI(Build.VERSION.SDK_INT + "");
        } catch (Throwable t) {
            driverInfo.setBSI("");
        }
        try {
            driverInfo.setBPSI(Build.VERSION.PREVIEW_SDK_INT + "");
        } catch (Throwable e) {
            driverInfo.setBPSI("");
        }
        try {
            driverInfo.setBC(Build.VERSION.CODENAME);
        } catch (Throwable t) {
            driverInfo.setBC("");
        }

    }

    /**
     * @param context
     * @return 返回false:表示不是simulator;true:表示是simulator;
     */
    public boolean isSimulator(Context context) {
        //检查设备的设备ID与常见的模拟器ID是否相同,如果相同,则为模拟器
        //检查设备的IMSI号与常见的模拟器IMSI是否相同,如果相同,则为模拟器
        //检查设备的板载,品牌,工业设计,硬件等信息是否匹配模拟器的信息,如果相同,则为模拟器
        //检查设备的手机号,是否与常见的模拟器加载的手机号相同,如果相同,则为模拟器
        //检查设备是否有模拟器特有的pipe目录,如果有,则为模拟器
        //同上,检查设备是否有模拟器特有的QEmu目录,如果有,则为模拟器
        //同上,检查设备是否有模拟器特有对应的QEmu设备对应的目录,如果有则为模拟器
        //通过读取proc/net/tcp查看adb是否对应模拟器,如果对应,则为模拟器
        //检查设备上是否有模拟器目录,如果有,则为模拟器
        //                || SimulatorUtils.hasQEmuProps(mContext)             //检查设备上否有模拟器相关的属性,如果有,且超过5个,则表示为模拟器
        //检查设备上的网络连接状态是否为eth0,如果是,则为模拟器
        //                || SimulatorUtils.checkEmulatorByCpuInfo())         //通过cpu的类型来判断是否为模拟器,如果满足,其中一种类型,则为模拟器
        return SimulatorUtils.hasKnownDeviceId(context)                //检查设备的设备ID与常见的模拟器ID是否相同,如果相同,则为模拟器
                || SimulatorUtils.hasKnownImsi(context)             //检查设备的IMSI号与常见的模拟器IMSI是否相同,如果相同,则为模拟器
                || SimulatorUtils.hasEmulatorBuild(context)         //检查设备的板载,品牌,工业设计,硬件等信息是否匹配模拟器的信息,如果相同,则为模拟器
                || SimulatorUtils.hasKnownPhoneNumber(context)      //检查设备的手机号,是否与常见的模拟器加载的手机号相同,如果相同,则为模拟器
                || SimulatorUtils.hasPipes()                        //检查设备是否有模拟器特有的pipe目录,如果有,则为模拟器
                || SimulatorUtils.hasQEmuFiles()                    //同上,检查设备是否有模拟器特有的QEmu目录,如果有,则为模拟器
                || SimulatorUtils.hasQEmuDrivers()                  //同上,检查设备是否有模拟器特有对应的QEmu设备对应的目录,如果有则为模拟器
                || SimulatorUtils.hasEmulatorAdb()                  //通过读取proc/net/tcp查看adb是否对应模拟器,如果对应,则为模拟器
                || SimulatorUtils.hasGenyFiles()                    //检查设备上是否有模拟器目录,如果有,则为模拟器
//                || SimulatorUtils.hasQEmuProps(mContext)             //检查设备上否有模拟器相关的属性,如果有,且超过5个,则表示为模拟器
                || SimulatorUtils.hasEmulatorWifi();
    }

    private String getSimulatorDescription(Context context) {
        String fileContent = FileUitls.getContentFromFile("/system/build.prop");
        String res = "未知";
        if (fileContent.contains("microvirtd")) {
            res = "逍遥模拟器";
        } else if (fileContent.contains("genyd")) {
            res = "Genymotion模拟器";
        } else if (fileContent.contains("ttvmd")) {
            res = "天天模拟器";
        } else if (fileContent.contains("都是Droid4X")) {
            res = "海马玩模拟器";
        }
        return res;

    }

    private String getSystemHour() {
        ContentResolver cv = context.getContentResolver();
        String timeFormat = Settings.System.getString(cv, Settings.System.TIME_12_24);
        return timeFormat;
    }

    private String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    private String getSystemArea() {
        return Locale.getDefault().getCountry();
    }

    private String getTimeZone() {
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
        return builder.toString();
    }

    private static void appendNumber(StringBuilder builder, int count, int value) {
        String string = Integer.toString(value);
        for (int i = 0; i < count - string.length(); i++) {
            builder.append('0');
        }
        builder.append(string);
    }

    private String getSystemFontSize() {
        try {
            Configuration mCurConfig = null;
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Object obj = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
            Method method = obj.getClass().getMethod("getConfiguration");
            mCurConfig = (Configuration) method.invoke(obj);
            return mCurConfig.fontScale + "";
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            return "0";
        }
    }

    private String getResolution() {
        try {
            // 方法1 Android获得屏幕的宽和高
            WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            int mScreenWidth = dm.widthPixels;
            int mScreenHeigh = dm.heightPixels;
            return mScreenWidth + "-" + mScreenHeigh;

        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            return "";
        }
    }


    private String getMobileOperatorName() {
        String result = "";
        try {
            result = tm.getSimOperatorName();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            return result;
        }
        return result;
    }

    private String getNetworkOperatorName() {
        String result = "";
        try {
            result = tm.getNetworkOperatorName();
        } catch (Throwable e) {
            /**
             * Availability: Only when user is registered to a network. Result may be
             * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
             * on a CDMA network).
             */
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return result;
    }

    private String getNetworkOperatorCode() {
        String result = "";
        try {
            result = tm.getNetworkOperator();
        } catch (Throwable e) {
            /**
             * Availability: Only when user is registered to a network. Result may be
             * unreliable on CDMA networks (use {@link #getPhoneType()} to determine if
             * on a CDMA network).
             */
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return result;
    }

    /**
     * @param context
     * @return
     */
    private String getIMEIS(Context context) {
        String imei_one = "", imei_two = "", value = "";
        if (SystemUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            imei_one = tm.getDeviceId();
        }
        String imei_one_flowSlot = getOperatorBySlot(context, 0);
        if (imei_one_flowSlot != null) {
            imei_one = imei_one_flowSlot;
        }
        imei_two = getOperatorBySlot(context, 1);

        if (!imei_one.isEmpty() && !imei_two.isEmpty()) {

            String[] strs = {imei_one, imei_two};
            Arrays.sort(strs);
            StringBuffer sb = new StringBuffer();
            for (String str : strs) {
                sb.append(str);
                sb.append("|");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            value = sb.toString();
        } else if (!imei_one.isEmpty() && imei_two.isEmpty()) {
            value = imei_one;
        } else if (imei_one.isEmpty() && !imei_two.isEmpty()) {
            value = imei_two;
        }

        return value;
    }

    //反射方法  TelephonyManager.getDeviceId(int)  19 /20   21开始有
    private static String getOperatorBySlot(Context context, int slotID) {
        String inumeric = "";
        if (Build.VERSION.SDK_INT > 20 && SystemUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
                Class<?>[] parameter = new Class[1];
                parameter[0] = int.class;
                Method getSimID = telephonyClass.getMethod("getDeviceId", parameter);
                Object[] obParameter = new Object[1];
                obParameter[0] = slotID;
                Object ob_phone = getSimID.invoke(telephony, obParameter);
                if (ob_phone != null) {
                    inumeric = ob_phone.toString();
                }
            } catch (Throwable e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
                return inumeric;

            }
        }
        return inumeric;
    }


    /**
     * 需要权限:Manifest.permission.BLUETOOTH
     *
     * @param context
     * @return
     */
    private String getBluetoochMac(Context context) {
        String address = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            address = BluetoothAdapter.getDefaultAdapter().getAddress();
            if (address == null || address.equals("") || address.equals("02:00:00:00:00:00")) {
                address = Secure.getString(context.getContentResolver(), "bluetooth_address");
            }
        } else {
            try {
                address = getBtAddressViaReflection();
            } catch (Exception e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            }
        }

        return address;
    }

    private String getBtAddressViaReflection() throws Exception {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Field mService = reflectField("mService", bluetoothAdapter.getClass());
        mService.setAccessible(true);
        Object bluetoothManagerService = mService.get(bluetoothAdapter);
        if (bluetoothManagerService == null) {
            return "";
        }
        Method method = reflectMethod("getAddress", new Class<?>[0],
                bluetoothManagerService.getClass());
        method.setAccessible(true);
        Object address = method.invoke(bluetoothManagerService);
        if (address != null && address instanceof String) {
            return (String) address;
        } else {
            return "";
        }
    }

    private Field reflectField(final String fieldName, final Class clazz) {
        for (final Field f : reflectAllFields(clazz)) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        return null;
    }

    private List<Field> reflectAllFields(Class clazz) {
        Class<?> c = clazz;
        final List<Field> list = new ArrayList<Field>();
        while (c != null) {
            list.addAll(Arrays.asList(c.getDeclaredFields()));
            for (Class<?> interf : c.getInterfaces()) {
                list.addAll(Arrays.asList(interf.getFields()));
            }
            c = c.getSuperclass();
        }
        return list;
    }

    public Method reflectMethod(final String methodName, final Class<?>[] argumentTypes,
                                final Class clazz) {
        final ClassArrayMatcher matcher = new ClassArrayMatcher(argumentTypes);

        Method match = null;
        for (final Method method : reflectAllMethods(clazz)) {
            if (method.getName().equals(methodName)) {
                final MatchType matchType = matcher.match(method.getParameterTypes());
                if (MatchType.PERFECT.equals(matchType)) {
                    match = method;
                    break;
                } else if (MatchType.MATCH.equals(matchType)) {
                    match = method;
                }
            }
        }
        return match;
    }

    private List<Method> reflectAllMethods(Class clazz) {
        Class<?> c = clazz;
        final List<Method> list = new ArrayList<Method>();
        while (c != null) {
            list.addAll(Arrays.asList(c.getDeclaredMethods()));
            c = c.getSuperclass();
        }
        return list;
    }

    /**
     * 设备是否root
     *
     * @return
     **/
    private String isRoot() {
        String root = "0";
        try {
            if ((!new File("/system/bin/su").exists())
                    && (!new File("/system/xbin/su").exists())) {
                root = "0";
            } else {
                root = "1";
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return root;
    }

    /**
     * 获取设备信息ID
     * tag=0,返回IMSI，tag=1返回AndroidID,其他返回deviceId
     * 要产出的值必定是 IMEI-IMSI-ANDROID_ID 或者为 null-null-null 用null代替DeviceID组成项的字符串
     *
     * @return
     */
    private String getDiviceID(int tag) {

        String deviceId = null;
        if (context != null) {
            String IMEI = "null";
            String IMSI = "null";
            if (SystemUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager)
                        context.getSystemService(Context.TELEPHONY_SERVICE);
                IMEI = telephonyManager.getDeviceId();
                IMSI = telephonyManager.getSubscriberId();
            }
            String androidId = Settings.System.getString(
                    context.getContentResolver(), Secure.ANDROID_ID);
            deviceId = (TextUtils.isEmpty(IMEI) ? "null" : IMEI) + "-" + (TextUtils.isEmpty(IMSI) ? "null" : IMSI)
                    + "-" + (TextUtils.isEmpty(androidId) ? "null" : androidId);
            if (tag == 0) {
                return IMSI;
            } else if (tag == 1) {
                return androidId;
            }
        }
        return deviceId;
    }

    /**
     * 获取运营商信息
     */
    public String getMobileOperator() {
        String imsi = null;
        try {
            imsi = tm.getSimOperator();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return imsi;
    }

    /**
     * 获取APP应用版本号
     *
     * @return
     */
    public String getAppVersion() {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
        } catch (Throwable e) {
            return "0.0.0|0";
        }
        return packInfo.versionName + "|" + packInfo.versionCode;
    }

    /**
     * get app name
     *
     * @param context
     * @return
     */
    public String getApplicationName(Context context) {
        String applicationName = null;
        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            applicationName = (String) packageManager
                    .getApplicationLabel(applicationInfo);
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

        return applicationName;
    }

    private String getMacAddress() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return getMacBySystemInterface(context);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            return getMacByFileAndJavaAPI();
        } else {
            return getMacByJavaAPI();
        }
    }

    private String getMacBySystemInterface(Context context) {
        if (context == null) return "00:00:00:00:00:00";
        try {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (SystemUtils.checkPermission(context, Manifest.permission.ACCESS_WIFI_STATE)) {
                WifiInfo info = wifi.getConnectionInfo();
                return info.getMacAddress();
            } else {
                EgLog.i("Could not get mac address.[no permission android.permission.ACCESS_WIFI_STATE");
                return "00:00:00:00:00:00";
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            return "00:00:00:00:00:00";
        }
    }

    private String getMacByFileAndJavaAPI() {
        String mac = getMacShell();
        return !TextUtils.isEmpty(mac) ? mac : getMacByJavaAPI();
    }

    private String getMacByJavaAPI() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netInterface = interfaces.nextElement();
                if ("wlan0".equals(netInterface.getName()) || "eth0".equals(netInterface.getName())) {
                    byte[] addr = netInterface.getHardwareAddress();
                    if (addr == null || addr.length == 0) {
                        continue;
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
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return "00:00:00:00:00:00";
    }

    private String getMacShell() {
        try {
            String[] urls = new String[]{"/sys/class/net/wlan0/address", "/sys/class/net/eth0/address",
                    "/sys/devices/virtual/net/wlan0/address"};
            String mc;
            for (int i = 0; i < urls.length; i++) {
                try {
                    mc = reaMac(urls[i]);
                    if (mc != null) {
                        return mc;
                    }
                } catch (Throwable e) {
                    EgLog.e("open file  Failed", e);
                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

        return null;
    }

    private String reaMac(String url) {
        String macInfo = null;
        try {
            FileReader fstream = new FileReader(url);
            BufferedReader in = null;
            if (fstream != null) {
                try {
                    in = new BufferedReader(fstream, 1024);
                    macInfo = in.readLine();
                } finally {
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (Throwable e) {

                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable e) {

                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return macInfo;
    }

    private String getSerialNumber() {
        return Build.SERIAL;
    }


    private String getAppMD5() {
        String md5 = SignaturesUtils.getCertificateWithMd5(context);
        return md5;
    }

    private String getAppSign() {
        String sign = SignaturesUtils.getSingInfo(context);
        return sign;
    }

}
