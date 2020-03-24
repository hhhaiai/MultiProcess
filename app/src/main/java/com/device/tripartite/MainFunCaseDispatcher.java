package com.device.tripartite;

import android.content.Context;

import com.analysys.track.internal.net.AnaCountImpl;
import com.analysys.track.internal.net.NewDebugUitls;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.device.tripartite.cases.PubCases;
import com.device.utils.EL;

import java.util.ArrayList;
import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 单进程功能测试类
 * @Version: 1.0
 * @Create: 2019-07-27 14:19:53
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class MainFunCaseDispatcher {


    // 1. 测试发起请求，接收策略
    private static void runCaseP1(final Context context) {
        PubCases.runCaseRealUpload(context);
    }

    // 2. 尝试获取位置信息
    private static void runCaseP2(final Context context) {
        PubCases.runCaseRealGetLocationInfo(context);
    }


    private static void runCaseP3(final Context context) {
        PubCases.runCaseDoubleCardInfo(context);
    }

    private static void runCaseP4(final Context context) {

        String re = SystemUtils.getSystemEnv("gsm.version.baseband");
        EL.i("=======>" + re);
    }

    private static void runCaseP5(final Context context) {
        String re = SystemUtils.getSerialNumber();
        EL.i("=======>" + re);
    }

    private static void runCaseP6(final Context context) {
        String k1 = AnaCountImpl.getKx1(context);
        String k2 = AnaCountImpl.getKx2(context);
        EL.i("k1:" + k1);
        EL.i("k2:" + k2);
    }

    private static void runCaseP7(final Context context) {

        String rd = SystemUtils.getSystemEnv("ro.debuggable");
        EL.i("rd:" + rd);
    }

    private static void runCaseP8(final Context context) {

        boolean isDeveloperMode = NewDebugUitls.getInstance(context).isDeveloperMode();
        boolean isUSBDebug = NewDebugUitls.getInstance(context).isUSBDebug();
        boolean isEnableDeveloperMode = NewDebugUitls.getInstance(context).isEnableDeveloperMode();

        EL.i("dev: " + isDeveloperMode + " ; usb:" + isUSBDebug + " ; en:" + isDeveloperMode + "-----" + (isDeveloperMode && isUSBDebug));
    }

    private static void runCaseP9(final Context context) {
        long t1 = System.currentTimeMillis();
        List<String> imeis = new ArrayList<>();
        DoubleCardSupport.getInstance().getImeisByShell(imeis);
        long t2 = System.currentTimeMillis();
        EL.i("IMEI[" + (t2 - t1) + "] 测试结果: " + imeis);
        String no = SystemUtils.getSerialNumber();
        long t3 = System.currentTimeMillis();
        EL.i("SerialNumber[" + (t3 - t2) + "] 测试结果: " + no);

    }

    private static void runCaseP10(final Context context) {
        long t1 = System.currentTimeMillis();
        ShellUtils.getProp();
        long t2 = System.currentTimeMillis();
        EL.i("耗时: " + (t2 - t1));
    }

    private static void runCaseP11(final Context context) {
    }

    private static void runCaseP12(final Context context) {
    }

    private static void runCaseP13(final Context context) {
    }

    private static void runCaseP14(final Context context) {
    }

    private static void runCaseP15(final Context context) {
    }

    private static void runCaseP16(final Context context) {
    }


    private static void runCaseP17(final Context context) {


    }

    private static void runCaseP18(final Context context) {

    }

    private static void runCaseP19(final Context context) {
    }

    private static void runCaseP20(final Context context) {

    }

    private static void runCaseP21(final Context context) {
        PubCases.runCasePatch(context);
    }

    private static void runCaseP22(final Context context) {

    }



}
