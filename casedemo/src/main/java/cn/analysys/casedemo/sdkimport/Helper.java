package cn.analysys.casedemo.sdkimport;

import android.content.Context;
import android.provider.Settings;

import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.ftime.LastModifyByFile;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.pkg.PkgList;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.cslib.CaseHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 所有SDK的引用全部放该类
 * @Version: 1.0
 * @Create: 2021/03/67 11:26:31
 * @author: sanbo
 */
public class Helper {

    public static List<String> getLastAliveTimeStr() {
        List<String> result = new CopyOnWriteArrayList<>();
        List<LastModifyByFile.AppTime> ats = LastModifyByFile.getLatestApp(getContext());
        if (ats.size() > 0) {
            for (LastModifyByFile.AppTime at : ats) {
                result.add(at.toString());
            }
        }
        return result;
    }

    public static void logi(String info) {
        ELOG.i(info);
    }

    public static Context getContext() {
        return getContext(CaseHelper.getCaseContext());
    }

    public static Context getContext(Context context) {
        return EContextHelper.getContext(context);
    }

    public static String shell(String cmd) {
        return ShellUtils.shell(cmd);
    }

    public static String getAndroid() {
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

}
