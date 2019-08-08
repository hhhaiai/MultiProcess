package com.device.impls;


import android.content.Context;

import com.analysys.track.internal.impl.DoubleCardSupport;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.net.DataPackaging;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.service.AnalysysAccessibilityService;
import com.analysys.track.service.AnalysysJobService;
import com.analysys.track.service.AnalysysService;
import com.analysys.track.utils.AccessibilityHelper;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.MultiProcessChecker;
import com.device.utils.EL;

import org.json.JSONObject;

import java.lang.reflect.Method;

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

//        EL.d("--- MultiCase  you click  btnCase" + caseNum);
        try {
            Class<?> testCase = MultiCase.class;
            Method runCaseA = testCase.getDeclaredMethod("runCase" + caseNum, Context.class);
            runCaseA.invoke(null, context);
        } catch (Throwable e) {
            EL.v(e);
        }

    }

    /**
     * 多进程上传测试，并发强锁，测试是否可以上传成功
     *
     * @param context
     */
    public static void runCase1(final Context context) {
        EL.i("----上传测试----");
        UploadImpl.getInstance(context).upload();
        EL.i("----上传重试测试----");
        UploadImpl.getInstance(context).reTryAndUpload(false);
    }

    /**
     * 多进程开关屏处理测试
     *
     * @param context
     */
    public static void runCase2(final Context context) {
        EL.i("-----多进程开关屏处理测试----处理打开屏幕");
        MessageDispatcher.getInstance(context).processScreenOnOff(true);
        EL.i("-----多进程开关屏处理测试----处理关闭屏幕");
        MessageDispatcher.getInstance(context).processScreenOnOff(false);
    }

    /**
     * 多进程服务声明探测
     *
     * @param context
     */
    public static void runCase3(final Context context) {
        boolean isS = AndroidManifestHelper.isServiceDefineInManifest(context, AnalysysService.class);
        EL.i("----声明服务结果:" + isS);
        boolean isA = AccessibilityHelper.isAccessibilitySettingsOn(context, AnalysysAccessibilityService.class);
        EL.i("----声明辅助功能结果:" + isS);
        EL.i("----JobService定义测试。。。。");
        boolean isAnalysysJobServiceDef = AndroidManifestHelper.isJobServiceDefineInManifest(context, AnalysysJobService.class);
        EL.i("---JobService 测试结果： " + isAnalysysJobServiceDef);
    }

    /**
     * 测试思想： 同一个文件1秒一次抢锁操作
     *
     * @param context
     */
    public static void runCase4(final Context context) {
        EL.e("----多进程测试。。。。");
        for (int i = 0; i < 100; i++) {
            try {
                long now = System.currentTimeMillis();
                boolean is = MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, "test", 1000, now);
                if (is) {
                    EL.i("----" + (i + 1) + "----多进程测试文件:" + is);
                    Thread.sleep(200);
                    MultiProcessChecker.getInstance().setLockLastModifyTime(context, "test", System.currentTimeMillis());
                } else {
                    EL.d("-----" + (i + 1) + "----多进程测试文件:" + is);
                }
                Thread.sleep(50);
            } catch (Throwable e) {
                EL.i(e);
            }
        }
    }

    public static void runCase5(final Context context) {
        EL.i("----runCase5  LocationImpl 位置信息测试 。。。。");
        LocationImpl.getInstance(context).processLoctionMsg();
    }

    public static void runCase6(final Context context) {
        EL.i("----runCase6  测试获取设备信息 。。。。");
        JSONObject devInfo = DataPackaging.getInstance().getDevInfo(context);
        EL.d("----设备信息: " + devInfo.toString());
    }

    // 多卡获取信息
    public static void runCase7(final Context context) {
        EL.i("=================== 测试多卡获取信息===============");
        EL.i("多卡IMEI： " + DoubleCardSupport.getInstance().getIMEIS(context));
        EL.i("多卡IMSI： " + DoubleCardSupport.getInstance().getIMSIS(context));
    }

    public static void runCase8(final Context context) {
    }

    public static void runCase9(final Context context) {
    }

    public static void runCase10(final Context context) {
    }

    public static void runCase11(final Context context) {
    }

    public static void runCase12(final Context context) {
    }

    public static void runCase13(final Context context) {
    }

    public static void runCase14(final Context context) {
    }

    public static void runCase15(final Context context) {
    }

    public static void runCase16(final Context context) {
    }

    public static void runCase17(final Context context) {
    }

    public static void runCase18(final Context context) {
    }


}
