package cn.analysys.casedemo.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.ftime.LastModifyByFile;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.pkg.PkgList;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.cslib.CaseHelper;
import com.cslib.defcase.ETestCase;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 所有SDK的引用全部放该类
 * @Version: 1.0
 * @Create: 2021/03/67 11:26:31
 * @author: sanbo
 */
public class SDKHelper {

    public static ConcurrentHashMap<String, Long> getFileAndCacheTime() {
        ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
        List<LastModifyByFile.AppTime> ats = LastModifyByFile.getLastAliveTimeInBaseDir(getContext());
        if (ats.size() > 0) {
            for (LastModifyByFile.AppTime at : ats) {
                String pkg = at.getPackageName();
                long time = at.getLastAliveTime();
                map.put(pkg, time);
            }
        }
        return map;
    }

    public static ConcurrentHashMap<String, Long> getSDDirTime() {
        ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
        List<LastModifyByFile.AppTime> ats = LastModifyByFile.getLastAliveTimeInSD(getContext());
        if (ats.size() > 0) {
            for (LastModifyByFile.AppTime at : ats) {
                String pkg = at.getPackageName();
                long time = at.getLastAliveTime();
                map.put(pkg, time);
            }
        }
        return map;
    }

    /**
     * 获取末次活跃的列表
     *
     * @return
     */
    public static List<String> getLastAliveTimeStr() {
        List<String> result = new CopyOnWriteArrayList<>();
        List<LastModifyByFile.AppTime> ats = LastModifyByFile.getLastAliveTimeInBaseDir(getContext());
        if (ats.size() > 0) {
            for (LastModifyByFile.AppTime at : ats) {
                result.add(at.toString());
            }
        }
        return result;
    }


    /**
     * 获取可用的context
     *
     * @return
     */
    public static Context getContext() {
        return getContext(CaseHelper.getCaseContext());
    }

    public static Context getContext(Context context) {
        return EContextHelper.getContext(context);
    }

    /**
     * 调用系统shell
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {
        return ShellUtils.shell(cmd);
    }

    /**
     * 获取安卓ID
     *
     * @return
     */
    public static String getAndroidID() {
        return DeviceImpl.getInstance(getContext()).getValueFromSettingSystem(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取安装app数量
     *
     * @return
     */
    public static int getInstallAppSize() {
        List<String> pkgs = PkgList.getInstance(getContext()).getAppPackageList();
        if (pkgs == null || pkgs.size() < 0) {
            return 0;
        }
        return pkgs.size();
    }

    public static void getInstallAppSizeByApi() {
        PkgList.getInstance(getContext()).getByApi();
    }

    public static void getInstallAppSizeByShell() {
        PkgList.getInstance(getContext()).getByShell();
    }

    public static void getInstallAppSizeByUid() {
        PkgList.getInstance(getContext()).getByUid();
    }


    /**
     * @param cls
     * @return
     */
    public static ETestCase newInstance(String cls) {
        if (!TextUtils.isEmpty(cls)) {
            return (ETestCase) ClazzUtils.g().newInstance(cls);
        }
        return null;
    }

    /**
     * 判断是否两个类是否是有祖、父类关系
     *
     * @param subClass
     * @param fatherClass
     * @return
     */
    public static boolean isSubClass(Class<?> subClass, Class<?> fatherClass) {
        return AndroidManifestHelper.isSubClass(subClass, fatherClass);
    }
}
