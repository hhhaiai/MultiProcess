package com.device.tripartite;

import android.content.Context;

import com.device.tripartite.cases.PubCases;


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
    }

    private static void runCaseP5(final Context context) {
    }

    private static void runCaseP6(final Context context) {
    }

    private static void runCaseP7(final Context context) {
    }

    private static void runCaseP8(final Context context) {
    }

    private static void runCaseP9(final Context context) {
    }

    private static void runCaseP10(final Context context) {
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
