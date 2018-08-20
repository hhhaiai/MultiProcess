package com.eguan.monitor;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.eguan.Constants;
import com.eguan.db.DBPorcesser;
import com.eguan.imp.OCInfo;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.SPHodler;

/**
 * @Copyright © 2018 Analysy Inc. All rights reserved.
 * @Description: 辅助类获取OCInfo
 * @Version: 1.0
 * @Create: 2018年8月17日 下午4:02:13
 * @Author:
 */
public class AccessibilityOCManager {

    private Context mContext;
    private volatile static AccessibilityOCManager instance;
    // 设置AccessibilityService是否工作
    private boolean enable = false;

    // private boolean isFirstRecord = true;

    private static final String COLLECTION_TYPE = "3";
    // private static final String TAG = "AccessibilityOCManager";

    private static final String ST_NORMAL = "1";
    private static final String ST_KEYGUARD = "2";
    private static final String ST_REBOOT = "3";

    private static final String AT_SYSTEM = "SA";
    private static final String AT_OTHER = "OA";

    private static final String KEYGUARD_PACKAGENAME_DEFAULT = "KEYGUARD_PACKAGENAME";

    private String prePackageName;
    private OCInfo ocInfo = new OCInfo();
    private String preTime;

    private AccessibilityOCManager(Context mContext) {
        this.mContext = mContext;
    }

    public static AccessibilityOCManager getInstance(Context mContext) {
        if (instance == null) {
            synchronized (AccessibilityOCManager.class) {
                if (instance == null) {
                    instance = new AccessibilityOCManager(mContext);
                }
            }
        }
        return instance;
    }

    public void setEnable() {
        enable = true;
    }

    public void setDisable() {
        enable = false;
    }

    private boolean isKeyGuard = false;

    public void setAccessibilityOC(String packageName) {
        if (!enable)
            return;
        if (empty(packageName))
            return;
        isKeyGuard = isKeyguardRestrictedInputMode(mContext);
        if (empty(prePackageName)) {
            if (isKeyGuard) {
                prePackageName = KEYGUARD_PACKAGENAME_DEFAULT;
            } else {
                prePackageName = packageName;
            }
            preTime = System.currentTimeMillis() + "";
            if (!isKeyGuard) {
                SPHodler.getInstance(mContext).setAccessibilityOCPackageName(packageName);
                SPHodler.getInstance(mContext).setAccessibilityOCStartTime(preTime);
                SPHodler.getInstance(mContext).setNetworkState(InnerProcessCacheManager.getInstance().getNT());
            }
            return;
        }
        if (prePackageName.equals(packageName)) {
            return;
        } else {
            String sa = ST_NORMAL;
            if (isKeyGuard) {
                sa = ST_KEYGUARD;
            } else {
                SPHodler.getInstance(mContext).setAccessibilityOCPackageName(packageName);
                SPHodler.getInstance(mContext).setAccessibilityOCStartTime(System.currentTimeMillis() + "");
                SPHodler.getInstance(mContext).setNetworkState(InnerProcessCacheManager.getInstance().getNT());
            }
            if (!prePackageName.equals(KEYGUARD_PACKAGENAME_DEFAULT)) {
                saveOC(prePackageName, sa);
            }
            if (isKeyGuard) {
                prePackageName = KEYGUARD_PACKAGENAME_DEFAULT;
            } else {
                prePackageName = packageName;
            }
            preTime = System.currentTimeMillis() + "";

        }

    }

    private boolean empty(String name) {
        return name == null || name.equals("");
    }

    private void saveOC(String packageName, String st) {
        ocInfo.setApplicationPackageName(packageName);
        ocInfo.setApplicationOpenTime(preTime);
        ocInfo.setApplicationCloseTime(System.currentTimeMillis() + "");
        ocInfo.setApplicationName(getApplicationName(packageName));
        ocInfo.setCollectionType(COLLECTION_TYPE);
        ocInfo.setSwitchType(st);
        ocInfo.setNetwork(InnerProcessCacheManager.getInstance().getNT());
        ocInfo.setApplicationVersionCode(getApplicationVersion(packageName));
        ocInfo.setApplicationType(isSystemApplication(packageName) ? AT_SYSTEM : AT_OTHER);
        DBPorcesser.getInstance(mContext).insertOneOCInfo(ocInfo);
        SPHodler.getInstance(mContext).setAccessibilityOCPackageName("");
        SPHodler.getInstance(mContext).setAccessibilityOCStartTime("");
        SPHodler.getInstance(mContext).setNetworkState("-1");
    }

    /**
     * 判断是否为系统应用
     *
     * @param packageName
     * @return true 系统应用; false 非系统应用
     */
    private boolean isSystemApplication(String packageName) {
        boolean boo = false;
        try {
            if (empty(packageName))
                return false;
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);
            if ((pInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 1) {
                boo = true;
            }
        } catch (Exception e) {
            boo = true;
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return boo;
    }

    private String getApplicationName(String packageName) {
        String appName = "";
        try {
            PackageManager pm = mContext.getPackageManager();
            appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return appName;
    }

    private String getApplicationVersion(String packageName) {
        String appVer = "";
        PackageManager pm = mContext.getPackageManager();
        try {
            appVer = pm.getPackageInfo(packageName, 0).versionName + "|"
                    + pm.getPackageInfo(packageName, 0).versionCode;
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return appVer;
    }

    public void updateServiceBootOCInfo() {
        String packageName = SPHodler.getInstance(mContext).getAccessibilityOCPackageName();
        if (!empty(packageName)) {
            final OCInfo ocInfo = new OCInfo();
            String netType = SPHodler.getInstance(mContext).getNetworkState();
            String startTime = SPHodler.getInstance(mContext).getAccessibilityOCStartTime();
            ocInfo.setSwitchType(ST_REBOOT);
            ocInfo.setApplicationType(isSystemApplication(packageName) ? AT_SYSTEM : AT_SYSTEM);
            ocInfo.setApplicationName(getApplicationName(packageName));
            ocInfo.setApplicationVersionCode(getApplicationVersion(packageName));
            ocInfo.setApplicationOpenTime(startTime);
            ocInfo.setApplicationCloseTime(SPHodler.getInstance(mContext).getEndTime() + "");
            ocInfo.setNetwork(netType);
            ocInfo.setCollectionType(COLLECTION_TYPE);
            ocInfo.setApplicationPackageName(packageName);
            DBPorcesser.getInstance(mContext).insertOneOCInfo(ocInfo);
        }
    }

    /**
     * 是否解锁 锁屏true 开屏false
     *
     * @param context
     * @return
     */
    private boolean isKeyguardRestrictedInputMode(Context context) {
        KeyguardManager manager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean result = manager.inKeyguardRestrictedInputMode();
        return result;
    }
}
