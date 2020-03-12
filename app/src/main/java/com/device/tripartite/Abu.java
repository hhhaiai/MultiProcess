package com.device.tripartite;


import android.content.Context;
import android.content.pm.PackageManager;
import android.os.StrictMode;

import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.sp.SPHelper;
import com.device.BuildConfig;
import com.device.impls.MultiProcessFramework;
import com.device.utils.AssetsHelper;
import com.device.utils.DemoClazzUtils;
import com.device.utils.EL;
import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 所有的三方SDK的调用
 * @Version: 1.0
 * @Create: 2020/3/12 17:22
 * @author: sanbo
 */
public class Abu {

    /**
     * 初始化bugly。 track-sdk-demo
     *
     * @param context
     */
    public static void initBugly(Context context) {
        Bugly.init(context, "8b5379e3bc", false);
    }

    /**
     * 初始化统计
     *
     * @param context
     */
    public static void initAnalysys(Context context) {
        initEg(context);
        initUmeng(context);
    }

    /**
     * 调试打开多进程和严格模式
     *
     * @param context
     */
    public static void initMultiProcessIfDebug(Context context) {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            MultiProcessFramework.runServices(context);
        }
    }

    public static void onResume(Context ctx, String pn) {
        MobclickAgent.onResume(ctx);
        MobclickAgent.onPageStart(pn);
    }


    public static void onPause(Context ctx, String pn) {
        MobclickAgent.onPause(ctx);
        MobclickAgent.onPageEnd(pn);
    }

    public static void onEvent(Context ctx, String eventName) {
        MobclickAgent.onEvent(ctx, eventName);
    }

    public static JSONArray getUSMInfo(Context ctx, long begin, long end) {
        return (JSONArray) DemoClazzUtils.invokeStaticMethod("com.analysys.track.internal.impl.usm.USMImpl", "getUSMInfo",
                new Class[]{Context.class, long.class, long.class}, new Object[]{ctx, begin, end});
    }

    // 初始化接口:第二个参数填写您在平台申请的appKey,第三个参数填写
    private static void initEg(Context context) {

        DemoClazzUtils.invokeStaticMethod("com.analysys.track.AnalysysTracker", "init",
                new Class[]{Context.class, String.class, String.class}, new Object[]{context, "7773661000888540d", "WanDouJia"});
    }

    //init umeng
    private static void initUmeng(Context context) {

        MobclickAgent.setSessionContinueMillis(10);
        MobclickAgent.setCatchUncaughtExceptions(true);
        UMConfigure.setProcessEvent(true);
        UMConfigure.setEncryptEnabled(true);
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(context, "5b4c140cf43e4822b3000077", "track-demo-dev", UMConfigure.DEVICE_TYPE_PHONE, "99108ea07f30c2afcafc1c5248576bc5");

    }


    public static void case1(Context context) {
        try {
            EL.i("=================== 测试发起请求，接收策略===============");

            SPHelper.setIntValue2SP(context, EGContext.FAILEDNUMBER, 0);
            PolicyImpl.getInstance(context).clear();
            UploadImpl.getInstance(context).doUploadImpl();
        } catch (Throwable e) {
            EL.i(e);
        }
    }

    public static void case2(Context context) {
        try {
//            EL.i("=================== 测试接收并处理策略===============");
//            PolicTestY.testSavePolicy(context);
            boolean isDe = DevStatusChecker.getInstance().isDebugDevice(context);
            EL.i("debug：" + isDe);
        } catch (Throwable e) {
            EL.i(e);
        }
    }

    public static void case3(Context context) {
        EL.i("=================== OC测试 ===============");
        OCImpl.getInstance(context).processOC();
    }

    public static void case4(Context context) {
        EL.i("=================== 安装列表调试状态获取测试 ===============");

        List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();
        EL.i("列表:" + list);
    }

    public static void case5(Context context) {
        EL.i("=================== 保存文件到本地,忽略调试设备状态直接加载 ===============");
        try {
            JSONObject obj = new JSONObject(AssetsHelper.getFromAssetsToString(context, "policy_body.txt"));
            JSONObject patch = obj.optJSONObject("patch");
            String version = patch.optString("version");
            String data = patch.optString("data");
            EL.i("testParserPolicyA------version: " + version);
            EL.i("testParserPolicyA------data: " + data);
            PolicyImpl.getInstance(context).saveFileAndLoad(version, data);
        } catch (Throwable e) {
            EL.e(e);
        }
    }

    public static void case6(Context context) {
        EL.i("=================== 根据手机APP情况，随机抽5个进行OC逻辑验证 ===============");

        // 获取安装列表
        List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();

        //获取有界面的安装列表
        PackageManager pm = context.getPackageManager();
        List<String> ll = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {

            JSONObject o = list.get(i);

            if (o != null && o.has(EGContext.TEXT_DEBUG_APP)) {
                String pkg = o.optString(EGContext.TEXT_DEBUG_APP);
                if (SystemUtils.hasLaunchIntentForPackage(pm, pkg) && !ll.contains(pkg)) {
                    ll.add(pkg);
                }
            }
        }


        //获取前5个，然后三个作为老列表，2个作为新列表进行测试
        if (ll.size() > 5) {
            //proc方式获取
            OCImpl.getInstance(context).cacheDataToMemory(ll.get(0), "2");
            OCImpl.getInstance(context).cacheDataToMemory(ll.get(1), "2");
            OCImpl.getInstance(context).cacheDataToMemory(ll.get(2), "2");

            JSONArray arr = new JSONArray();
            arr.put(ll.get(2));
            arr.put(ll.get(3));
            arr.put(ll.get(4));
            // 进行新旧对比，内部打印日志和详情
            OCImpl.getInstance(context).getAliveAppByProc(arr);
        } else {
            EL.e("应用列表还没有5个。。无法正常测试");
        }

    }

    public static void case7(Context context) {
        EL.i("=================== OC逻辑验证 ===============");
        OCImpl.getInstance(context).processOC();
    }

    public static void case8(Context context) {
        EL.i("=================== 插入OC数据到数据库测试 ===============");
        // 获取安装列表
        List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();

        //获取有界面的安装列表
        PackageManager pm = context.getPackageManager();
        List<String> ll = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {

            JSONObject o = list.get(i);

            if (o != null && o.has(EGContext.TEXT_DEBUG_APP)) {
                String pkg = o.optString(EGContext.TEXT_DEBUG_APP);
                if (SystemUtils.hasLaunchIntentForPackage(pm, pkg) && !ll.contains(pkg)) {
                    ll.add(pkg);
                }
            }
        }

        if (ll.size() == 0) {
            throw new RuntimeException("安装列表获取是空");
        }
        // 2.放内存里
        for (int i = 0; i < ll.size(); i++) {
            OCImpl.getInstance(context).cacheDataToMemory(ll.get(i), "2");
        }
        //写入库
        OCImpl.getInstance(context).processScreenOff();

        EL.i("=================== 从数据库取出OC数据 ===============");

        JSONArray oc = TableProcess.getInstance(context).selectOC(EGContext.LEN_MAX_UPDATE_SIZE);

        if (oc != null) {
            EL.i("获取OC数据:" + oc.toString());
        }
    }

    public static void case9(Context context) {
        EL.i("----忽略进程直接发起网络请求-----");
        UploadImpl.getInstance(context).doUploadImpl();
    }

    public static void case10(Context context) {
        EL.i("----【安装列表】检查并更新数据库数据-----");
        AppSnapshotImpl.getInstance(context).getSnapShotInfo();
    }

    public static void case11(Context context) {
        EL.i("----【安装列表】数据库-----");
        JSONArray ins = TableProcess.getInstance(context).selectSnapshot(EGContext.LEN_MAX_UPDATE_SIZE);
        EL.i(ins);
    }

    public static void case12(Context context) {
        EL.i("----【定位信息】直接获取。。。忽略多进程-----");
        LocationImpl.getInstance(context).getLocationInfoInThread();
    }

    public static void case13(Context context) {
        EL.i("----测试加密数据-----");
        UploadImpl.getInstance(context).messageEncrypt("测试加密数据");
    }

    public static void case14(Context context) {
        EL.i("----测试双卡-----");
        String imeis = DoubleCardSupport.getInstance().getIMEIS(context);
        EL.i("----测试双卡IMEI: " + imeis);
        String imsis = DoubleCardSupport.getInstance().getIMSIS(context);
        EL.i("----测试双卡IMSI: " + imsis);
    }
}
