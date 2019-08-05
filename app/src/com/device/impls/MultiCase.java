package com.device.impls;


import android.content.Context;

import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.service.AnalysysAccessibilityService;
import com.analysys.track.service.AnalysysService;
import com.analysys.track.utils.AccessibilityHelper;
import com.analysys.track.utils.AndroidManifestHelper;
import com.device.utils.EL;
import com.device.utils.ProcessUtils;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 多进程case类
 * @Version: 1.0
 * @Create: 2019-07-30 18:23:59
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class MultiCase {

    /**
     * 运行具体的case
     *
     * @param context
     * @param caseNum
     */
    public static void runCase(Context context, int caseNum) {
//        EL.i(ProcessUtils.getCurrentProcessName(context) + "--------->收到case" + caseNum);

        switch (caseNum) {
            case 1:
                runCase1(context);
                break;
            case 2:
                runCase2(context);
                break;
            case 3:
                runCase3(context);
                break;
            case 4:
                runCase4(context);
                break;
            case 5:
                runCase5(context);
                break;
            case 6:
                runCase6(context);
                break;
            case 7:
                runCase7(context);
                break;
            case 8:
                runCase8(context);
                break;
            case 9:
                runCase9(context);
                break;
            default:
                break;
        }
    }

    public static void runCase1(final Context context) {
        EL.i(ProcessUtils.getCurrentProcessName(context) + "----上传测试----");
        UploadImpl.getInstance(context).upload();
    }

    public static void runCase2(final Context context) {
        EL.i(ProcessUtils.getCurrentProcessName(context) + "----处理打开屏幕");
        MessageDispatcher.getInstance(context).processScreenOnOff(true);
        EL.i(ProcessUtils.getCurrentProcessName(context) + "----处理关闭屏幕");
        MessageDispatcher.getInstance(context).processScreenOnOff(false);
    }

    public static void runCase3(final Context context) {
        boolean isS = AndroidManifestHelper.isServiceDefineInManifest(context, AnalysysService.class);
        EL.i(ProcessUtils.getCurrentProcessName(context) + "----声明服务结果:" + isS);
        boolean isA = AccessibilityHelper.isAccessibilitySettingsOn(context, AnalysysAccessibilityService.class);
        EL.i(ProcessUtils.getCurrentProcessName(context) + "----声明辅助功能结果:" + isS);
    }

    public static void runCase4(final Context context) {
    }

    public static void runCase5(final Context context) {
    }

    public static void runCase6(final Context context) {
    }

    public static void runCase7(final Context context) {
    }

    public static void runCase8(final Context context) {
    }

    public static void runCase9(final Context context) {
    }
}
