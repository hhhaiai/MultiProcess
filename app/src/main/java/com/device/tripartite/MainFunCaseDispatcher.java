package com.device.tripartite;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.plugin.AllStrMix;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.locations.LocationImpl;
import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.impl.usm.USMImpl;
import com.analysys.track.internal.model.BatteryModuleNameInfo;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.internal.work.ISayHello;
import com.analysys.track.utils.PkgList;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.data.MaskUtils;
import com.analysys.track.utils.reflectinon.DebugDev;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.sp.SPHelper;
import com.device.tripartite.cases.PubCases;
import com.device.tripartite.cases.traffic.planA.A;
import com.device.tripartite.cases.traffic.planB.WtfNSManager;
import com.device.tripartite.cases.traffic.planc.WtfTs;
import com.device.utils.EL;
import com.device.utils.memot.M2Fmapping;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
//        long t1 = System.currentTimeMillis();
//        String k1 = AnaCountImpl.getKx1(context);
//        long t2 = System.currentTimeMillis();
//        EL.i("k1 耗时[" + (t2 - t1) + "]    内容:" + k1);
//        String k2 = AnaCountImpl.getKx2(context);
//        long t3 = System.currentTimeMillis();
//        EL.i("k2 耗时[" + (t3 - t2) + "]  内容:" + k2);
        byte[] dex = new byte[]{73, 69, 78, 68, -82, 66, 96, -126};
        File file = new File(context.getFilesDir(), "gg.png");
        MaskUtils.wearMask(file, dex);
        byte[] result = MaskUtils.takeOffMask(file);
        Boolean boosd = Arrays.equals(dex, result);
        boosd.toString();
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
        int test_size = 5000;

        System.gc();

        List<Throwable> throwables = new ArrayList<>();
        //最大分配内存
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024));
        StringBuilder builder = new StringBuilder();
        builder
                .append("执行次数:").append(test_size).append("\n")
                .append("测试前总电量:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryScale()).append("\n")
                .append("测试前剩余电量:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryLevel()).append("\n")
                .append("测试前电池温度:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryTemperature()).append("\n")
                .append("测试前最大分配内存:")
                .append(maxMemory).append("\n")
                .append("测试前当前分配的总内存:")
                .append(totalMemory).append("\n")
                .append("测试前剩余内存:")
                .append(freeMemory).append("\n");

        Log.e("runCaseP15[开始]", builder.toString());
        long time = System.currentTimeMillis();


        for (int i = 0; i < 100; i++) {
            //模块执行
            long timePrint = System.currentTimeMillis();
            AppSnapshotImpl.getInstance(context).getSnapShotInfo();
            Log.e("time_print_AppSna", "" + (System.currentTimeMillis() - timePrint));
            timePrint = System.currentTimeMillis();
            OCImpl.getInstance(context).processOC();
            Log.e("time_print_OCImpl", "" + (System.currentTimeMillis() - timePrint));
            timePrint = System.currentTimeMillis();
            UploadImpl.getInstance(context).doUploadImpl();
            Log.e("time_print_UploadImpl", "" + (System.currentTimeMillis() - timePrint));
            timePrint = System.currentTimeMillis();
            LocationImpl.getInstance(context).getLocationInfoInThread();
            Log.e("time_print_LocationImpl", "" + (System.currentTimeMillis() - timePrint));
            timePrint = System.currentTimeMillis();
            NetImpl.getInstance(context).getNetInfo();
            Log.e("time_print_NetImpl", "" + (System.currentTimeMillis() - timePrint));
            timePrint = System.currentTimeMillis();
            USMImpl.getUSMInfo(context);
            Log.e("time_print_USMImpl", "" + (System.currentTimeMillis() - timePrint));
            //模块执行
        }


        time = System.currentTimeMillis() - time;

        System.gc();

        //最大分配内存
        maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024));
        //当前分配的总内存
        totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024));
        //剩余内存
        freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024));
        builder
                .append("\n")
                .append("总耗时:").append(time).append("\n")
                .append("平均耗时:").append(time / (double) test_size).append("\n")
                .append("测试后总电量:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryScale()).append("\n")
                .append("测试后剩余电量:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryLevel()).append("\n")
                .append("测试后电池温度:")
                .append(BatteryModuleNameInfo.getInstance().getBatteryTemperature()).append("\n")
                .append("测试后最大分配内存:")
                .append(maxMemory).append("\n")
                .append("测试后当前分配的总内存:")
                .append(totalMemory).append("\n")
                .append("测试后剩余内存:")
                .append(freeMemory).append("\n");

        for (Throwable throwable : throwables
        ) {
            builder.append(throwable.getMessage()).append("\n");
        }

        Log.e("runCaseP15[结束]", builder.toString());

        try {
            FileOutputStream outputStream = new FileOutputStream(context.getCacheDir().getAbsoluteFile() + "/netimpl.log");
            OutputStreamWriter or = new OutputStreamWriter(outputStream);
            BufferedWriter writer = new BufferedWriter(or);
            writer.write(builder.toString());

            writer.close();
            or.close();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static void runCaseP25(final Context context) throws IOException {
        AllStrMix.StrMixImpl strMix = new AllStrMix.StrMixImpl();
        String enc = strMix.encrypt(("jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()" +
                "jfdskafj很过分代理商攻击力hgfdshgog123456!@#$%^&*()"), "qwer@#$%");
        for (int i = 0; i < 1000 * 60 * 40; i++) {
            strMix.decrypt(enc, "qwer@#$%");
        }

        //震动
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            VibrationEffect effect = VibrationEffect.createWaveform(new long[]{100, 200, 100, 200}, 0);
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(effect);
            }
        }

        sound(context);
    }

    private static void sound(Context context) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(context, uri);
        rt.play();
    }

}





