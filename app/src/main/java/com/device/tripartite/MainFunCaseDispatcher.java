package com.device.tripartite;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.impl.usm.USMImpl;
import com.analysys.track.internal.work.ISayHello;
import com.analysys.track.utils.PkgList;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.DebugDev;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.sp.SPHelper;
import com.device.tripartite.cases.PubCases;
import com.device.tripartite.cases.traffic.planA.A;
import com.device.tripartite.cases.traffic.planc.WtfTs;
import com.device.utils.EL;
import com.device.tripartite.cases.traffic.planB.WtfNSManager;
import com.device.utils.memot.M2Fmapping;

import org.json.JSONArray;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


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
//        long t1 = System.currentTimeMillis();
//        String k1 = AnaCountImpl.getKx1(context);
//        long t2 = System.currentTimeMillis();
//        EL.i("k1 耗时[" + (t2 - t1) + "]    内容:" + k1);
//        String k2 = AnaCountImpl.getKx2(context);
//        long t3 = System.currentTimeMillis();
//        EL.i("k2 耗时[" + (t3 - t2) + "]  内容:" + k2);
    }

    private static void runCaseP7(final Context context) {

        for (int i = 0; i < 1000; i++) {
            long t1 = System.currentTimeMillis();
            List<String> apps = PkgList.getInstance(context).getAppPackageList();
            long t2 = System.currentTimeMillis();

            EL.i(" 获取列表 耗时[" + (t2 - t1) + "]  内容:" + apps.size() + "-------" + apps.toString().length());
        }


    }

    private static void parseLine(List<String> apps, String line) {
        // 单行条件: 非空&&有点&&有冒号
        if (!TextUtils.isEmpty(line) && line.contains(".") && line.contains(":")) {
            // 分割. 样例数据:<code>package:com.android.launcher3</code>
            String[] ss = line.split(":");
            if (ss.length > 1) {
                String packageName = ss[1];
                if (!TextUtils.isEmpty(packageName) && !apps.contains(packageName)) {
                    apps.add(packageName);
                }
            }
        }
    }

    private static void runCaseP8(final Context context) {


        for (int i = 0; i < 10000; i++) {
            long t1 = System.currentTimeMillis();
            HashMap<String, NetInfo> m = NetImpl.getInstance(context).getNetInfo();
            long t2 = System.currentTimeMillis();
//            boolean s = DebugDevice.getInstance(context).isDebugDevice();
//            long t3 = System.currentTimeMillis();
//            EL.i("[NetInfo]大小: " + m.toString().length() + " ; 耗时: " + (t2 - t1)
//                    + " ;[DebugMode] 大小: " + s.length() + " ;耗时: " + (t3 - t2)
//            );
        }

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

        ShellUtils.getArrays("top", new ISayHello() {
            @Override
            public void onProcessLine(final String line) {

                EL.i("===>" + line);
            }
        }, false);
    }

    private static void runCaseP12(final Context context) {

        List<String> pks = PkgList.getInstance(context).getAppPackageList();

        for (int i = 0; i < 100000; i++) {
            long start = System.currentTimeMillis();
            boolean isA = M2Fmapping.getInstance(context).save(pks.toString());
//            EL.i("save: " + isA);
            String re = M2Fmapping.getInstance(context).load();
//            EL.i("load: " + v.length + "-----" + new String(v).trim());
            long end = System.currentTimeMillis();
            EL.i("写入[" + pks.toString().length() + "], 耗用时间[" + (end - start) + "] 读取byte大小: " + re.length());
        }
    }

    private static void runCaseP13(final Context context) {
        String imeis = DoubleCardSupport.getInstance().getIMEIS(context);
        EL.i("imeis:" + imeis);
    }

    private static void runCaseP14(final Context context) {

        for (int i = 0; i < 10000000; i++) {
            long start = System.currentTimeMillis();
            boolean result = DebugDev.get(context).isDebugDevice();
            long end = System.currentTimeMillis();
            EL.i("result:" + result + "---------" + (end - start));
        }
    }

    private static void runCaseP15(final Context context) {
//        for (int i = 0; i < 1000; i++) {
//            LocationImpl.getInstance(context).getLocationInfoInThread();
//        }
    }

    private static void runCaseP16(final Context context) {
        String s = SystemUtils.getContentFromFile("/proc/net/tcp6");
        EL.i("s:" + s);
    }


    private static void runCaseP17(final Context context) {


        try {
            FileChannel fc = new RandomAccessFile(new File("/proc/net/tcp6"), "r").getChannel();
            EL.i("fc: " + fc.size());
            long size = fc.size() - 10;

            MappedByteBuffer mBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, 1024);
            if (mBuffer != null) {
                mBuffer.position(0);
            }
            byte[] result = new byte[(int) size];
            if (mBuffer.remaining() > 0) {
                mBuffer.get(result, 0, mBuffer.remaining());
            }
            if (result.length > 0) {
                EL.i(new String(result, "UTF-8"));
            }
        } catch (Throwable e) {
            EL.i(e);
        }
    }

    private static void runCaseP18(final Context context) {
        A.getTrafficInfo(context);
    }


    private static void runCaseP19(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WtfNSManager.getInstance(context).getStr();
        }
    }


    private static void runCaseP20(final Context context) {
        WtfTs.getWifiTraffic();
    }

    private static void runCaseP21(final Context context) {
        WtfTs.fk(context);
//        PubCases.runCasePatch(context);
    }

    private static void runCaseP22(final Context context) {
    
        for (int i = 0; i < 100; i++) {
            Log.i("sanbo", "--------------------" + i + "/" + 100 + "-----------------");
            realWork(context);
        }
    }
    
    private static void realWork(final Context context) {
        long s1 = System.currentTimeMillis();
        long lastRequestTime = s1 - 20 * EGContext.TIME_HOUR;
        USMImpl.getUSMInfo(context, lastRequestTime, s1);
        long e1 = System.currentTimeMillis();
        Log.e("sanbo", "单次获取20小时耗时:" + (e1 - s1));
        
        long dur = 3 * EGContext.TIME_HOUR;
        more(context, s1, lastRequestTime, dur);
        dur = 1 * EGContext.TIME_HOUR;
        more(context, s1, lastRequestTime, dur);
        dur = 30 * EGContext.TIME_MINUTE;
        more(context, s1, lastRequestTime, dur);
    }
    
    private static void more(Context context, long s1, long lastRequestTime, long dur) {
        long tstart = System.currentTimeMillis();
        long now = s1;
        long lsa = lastRequestTime;

//        Log.e("sanbo", "--------------------开始-----------------");
        while (true) {
            if (lsa + dur >= now) {
//                Log.i("sanbo", String.format("尾声了。。起时间:[%s], 止时间[%s]", stampToDate(lsa), stampToDate(now)));
                USMImpl.getUSMInfo(context, lsa, now);
                break;
            } else {
//                Log.i("sanbo", String.format("中间。。起时间:[%s], 止时间[%s]", stampToDate(lsa), stampToDate(lsa + dur)));
                USMImpl.getUSMInfo(context, lsa, lsa + dur);
                lsa = lsa + dur;
            }
        }
//        Log.e("sanbo", "--------------------结束-----------------");
        
        long tsend = System.currentTimeMillis();
        Log.e("sanbo", "多次获取20小时 [" + dur + "] 耗时:" + (tsend - tstart));
    }
    
    private static void runCaseP23(final Context context) {
        JSONArray arr = USMImpl.getUSMInfo(context, 0, System.currentTimeMillis());
        EL.i(arr);
    }
    
    private static void runCaseP24(final Context context) {
        long lastReqTime = SPHelper.getLongValueFromSP(context, EGContext.LASTQUESTTIME, 0);
        EL.i("lastReqTime: " + lastReqTime);
    }
    
    private static void runCaseP25(final Context context) {
        JSONArray arr = USMImpl.getUSMInfo(context);
        EL.i(arr);
    }


}
