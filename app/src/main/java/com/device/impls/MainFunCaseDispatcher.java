package com.device.impls;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.impl.usm.USMImpl;
import com.analysys.track.internal.impl.usm.USMUtils;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;
import com.device.impls.case2.RefModelA;
import com.device.impls.cases.CaseImpls;
import com.device.impls.usmcase.USMCase;
import com.device.utils.AssetsHelper;
import com.device.utils.EContextHelper;
import com.device.utils.EL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    /**
     * 接收到方法
     *
     * @param context
     * @param x
     */
    public static void runCase(final Context context, final String x) {
        try {
            EL.d("--- you click  btnCase" + x);
            Method runCaseA = MainFunCaseDispatcher.class.getDeclaredMethod("runCaseP" + x, Context.class);
            runCaseA.invoke(null, context);
        } catch (Throwable e) {
            EL.e(e);
        }

    }


    // 1. 测试发起请求，接收策略
    private static void runCaseP1(final Context context) {
        try {
            EL.i("=================== 测试发起请求，接收策略===============");

            SPHelper.setIntValue2SP(context, EGContext.FAILEDNUMBER, 0);
            PolicyImpl.getInstance(context).clear();
            UploadImpl.getInstance(context).doUploadImpl();
        } catch (Throwable e) {
            EL.i(e);
        }

    }

    // 2. 测试接收并处理策略
    private static void runCaseP2(final Context context) {
        try {
//            EL.i("=================== 测试接收并处理策略===============");
//            PolicTestY.testSavePolicy(context);

            boolean isDe = DevStatusChecker.getInstance().isDebugDevice(context);
            EL.i("debug：" + isDe);
        } catch (Throwable e) {
            EL.i(e);
        }
    }

    // 3. OC测试
    private static void runCaseP3(final Context context) {
        EL.i("=================== OC测试 ===============");
        OCImpl.getInstance(context).processOC();
    }

    // 4.安装列表调试状态获取测试
    private static void runCaseP4(final Context context) {
        EL.i("=================== 安装列表调试状态获取测试 ===============");

        List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();
        EL.i("列表:" + list);
    }

    // 5. 测试保存文件到本地,忽略调试设备状态加载
    private static void runCaseP5(final Context context) {
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

    // 6. 根据手机APP情况，随机抽5个进行OC逻辑验证
    private static void runCaseP6(final Context context) {
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

    // 7. OC逻辑验证
    private static void runCaseP7(final Context context) {
        EL.i("=================== OC逻辑验证 ===============");
        OCImpl.getInstance(context).processOC();
    }

    //8. OC部分数据入库测试
    private static void runCaseP8(final Context context) {
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

    // 9. 忽略进程直接发起网络请求
    private static void runCaseP9(final Context context) {
        EL.i("----忽略进程直接发起网络请求-----");
        UploadImpl.getInstance(context).doUploadImpl();
    }

    // 10.【安装列表】检查并更新数据库数据
    private static void runCaseP10(final Context context) {
        EL.i("----【安装列表】检查并更新数据库数据-----");
        AppSnapshotImpl.getInstance(context).getSnapShotInfo();

    }


    // 11. 【安装列表】查询数据库
    private static void runCaseP11(final Context context) {
        EL.i("----【安装列表】数据库-----");
        JSONArray ins = TableProcess.getInstance(context).selectSnapshot(EGContext.LEN_MAX_UPDATE_SIZE);
        EL.i(ins);
    }

    // 12.【定位信息】直接获取。。。忽略多进程
    private static void runCaseP12(final Context context) {
        EL.i("----【定位信息】直接获取。。。忽略多进程-----");
        LocationImpl.getInstance(context).getLocationInfoInThread();
    }

    // 13. 测试加密数据
    private static void runCaseP13(final Context context) {
        EL.i("----测试加密数据-----");
        UploadImpl.getInstance(context).messageEncrypt("测试加密数据");
    }

    // 14.测试双卡
    private static void runCaseP14(final Context context) {
        EL.i("----测试双卡-----");
        String imeis = DoubleCardSupport.getInstance().getIMEIS(context);
        EL.i("----测试双卡IMEI: " + imeis);
        String imsis = DoubleCardSupport.getInstance().getIMSIS(context);
        EL.i("----测试双卡IMSI: " + imsis);
    }

    private static void runCaseP15(final Context mContext) {
        FileObserver fileObserver = new FileObserver("/proc/net/tcp", FileObserver.ALL_EVENTS) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.v(path, event + "");
            }
        };
        fileObserver.startWatching();
    }

    private static void runCaseP16(final Context mContext) {
        CaseImpls.case16Impl(mContext);
    }


    private static void runCaseP17(final Context mContext) {

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(mContext.getApplicationContext(), uri);
        rt.stop();

        try {
            FileInputStream inputStream = new FileInputStream(mContext.getCacheDir().getAbsoluteFile() + "/netimpl.log");
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder builder = new StringBuilder();
            while (true) {
                String str = bufferedReader.readLine();
                if (str != null) {
                    builder.append(str).append("\n");
                } else {
                    break;
                }
            }
            EL.i(builder.toString());
            bufferedReader.close();
        } catch (Exception e) {
            EL.e(e);
        }
    }

    private static void runCaseP18(final Context mContext) {
        NetImpl.getInstance(mContext).getNetInfo();
    }

    private static void runCaseP19(final Context mContext) {
        EL.i("----测试灰名单-----");
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            List<NeighboringCellInfo> list = (List<NeighboringCellInfo>) ClazzUtils.invokeObjectMethod(mTelephonyManager, "getNeighboringCellInfo");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DeviceImpl.getInstance(mContext).getBluetoothAddress(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DevStatusChecker.getInstance().isDebugRom();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            USMUtils.getUsageEventsByInvoke(0, System.currentTimeMillis(), mContext);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            EContextHelper.getContext(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runCaseP20(final Context mContext) {
        TelephonyManager mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mTelephonyManager.getAllCellInfo();
        }
    }

    private static void runCaseP21(final Context mContext) {
        try {

//            loadStatic(mContext, new File("/data/local/tmp/temp_20200108-180351.jar"),
//                  "com.analysys.Ab", "init",
//                    new Class[]{Context.class}, new Object[]{mContext});
            PatchHelper.loadStatic(mContext,
                    new File("/data/local/tmp/temp_20200108-180351.jar"),
                    "com.analysys.Ab", "init",
                    new Class[]{Context.class}, new Object[]{mContext});
        } catch (Throwable e) {
            EL.e(e);
        }
    }

    private static void runCaseP22(final Context context) {
        CaseImpls.caseRongQi(context);

    }

    private static void runCaseP23(final Context context) {


        try {
            EL.i("===========================测试反射======================");
            EL.w("1.反射调用,初始化空构造");
            Constructor ctor = RefModelA.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object clazzObj = ctor.newInstance();

            EL.w("2.反射公用非静态方法");
            Method sayHi = RefModelA.class.getDeclaredMethod("sayHi", String.class);
            sayHi.setAccessible(true);
            sayHi.invoke(clazzObj, "八戒，你站住！");

            EL.w("3.反射私有非静态方法");
            Method sayFuck = RefModelA.class.getDeclaredMethod("sayFuck", String.class);
            sayFuck.setAccessible(true);
            sayFuck.invoke(clazzObj, " 三藏！ 你给我等着！");

            EL.w("4.反射私有静态方法");
            Method sayFuckStatic = RefModelA.class.getDeclaredMethod("sayFuckStatic", String.class);
            sayFuckStatic.setAccessible(true);
            sayFuckStatic.invoke(null, " 你是猴子请来的救兵么！");


            EL.e("--------------正经调用---------------------");
            RefModelA a = new RefModelA("小明", 10);
            a.sayHi("我十岁了！");
        } catch (Throwable e) {
            EL.e(e);
        }


    }

    private static void runCaseP24(final Context context) {


        try {
            EL.i("===========================测试反射======================");
            EL.w("1.反射调用,初始化空构造");
            Constructor a = RefModelA.class.getDeclaredConstructor();
            Object RefModelAObj = a.newInstance();

            Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            getDeclaredMethod.setAccessible(true);
            Method invoke = Method.class.getDeclaredMethod("invoke", Object.class, Object[].class);
            invoke.setAccessible(true);

            EL.w("2.反射公用非静态方法");

            Method sayHi = (Method) getDeclaredMethod.invoke(RefModelA.class, "sayHi", new Class[]{String.class});
            sayHi.setAccessible(true);
            sayHi.invoke(RefModelAObj, new Object[]{"八戒，你站住！"});


            EL.w("3.反射私有非静态方法");


            Method sayFuck = (Method) getDeclaredMethod.invoke(RefModelA.class, "sayFuck", new Class[]{String.class});
            sayFuck.setAccessible(true);
            sayFuck.invoke(RefModelAObj, new Object[]{" 三藏！ 你给我等着！"});

            EL.w("4.反射私有静态方法");

            Method sayFuckStatic = (Method) getDeclaredMethod.invoke(RefModelA.class, "sayFuckStatic", new Class[]{String.class});
            sayFuckStatic.setAccessible(true);
            sayFuckStatic.invoke(RefModelAObj, new Object[]{" 你是猴子请来的救兵么！"});

//
            EL.e("--------------正经调用---------------------");
            RefModelA ra = new RefModelA("小明", 10);
            ra.sayHi("我十岁了！");
        } catch (Throwable e) {
            EL.e(e);
        }
    }

    private static void runCaseP25(final Context context) {

        try {
            try {
                EL.i("===========================测试反射======================");
                Method getMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

                EL.w("1.反射调用,初始化空构造");

                Constructor ctor = RefModelA.class.getDeclaredConstructor();
                ctor.setAccessible(true);
                Object obj = ctor.newInstance();

                EL.w("2.反射公用非静态方法");
                Method method1 = (Method) getMethod.invoke(RefModelA.class, "sayHi", new Class[]{String.class});
                method1.setAccessible(true);
                method1.invoke(new RefModelA("小红", 11), "hello");

                EL.w("3.反射私有非静态方法");
                Method method2 = (Method) getMethod.invoke(RefModelA.class, "sayFuck", new Class[]{String.class});
                method2.setAccessible(true);
                method2.invoke(new RefModelA("小刚", 22), "hello");

                EL.w("4.反射私有静态方法");
                Method method3 = (Method) getMethod.invoke(RefModelA.class, "sayFuckStatic", new Class[]{String.class});
                method3.setAccessible(true);
                method3.invoke(null, "hello");

                EL.e("--------------正经调用---------------------");
                RefModelA a = new RefModelA("小明", 10);
                a.sayHi("我十岁了！");


            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            EL.e(e);
        }

    }

    private static void runCaseP26(final Context context) {
        String res = ShellUtils.shell("type su");
        EL.i("shell type su..:" + res);

        res = ShellUtils.shell("which su");
        EL.i("shell which su..:" + res);


        res = ShellUtils.exec(new String[]{"which", " su"});
        EL.i("exec which su..:" + res);

        res = ShellUtils.exec(new String[]{"type", " su"});
        EL.i("exec type su..:" + res);

        res = ShellUtils.exec(new String[]{"getprop"});
        EL.i("exec getprop:" + res);

    }

    private static void runCaseP27(final Context context) {
        OCImpl.getInstance(context).processOC();
        EL.i("ss");

    }


    private static void runCaseP28(final Context context) {
//        JSONArray arr = new JSONArray();
//
//        List<UsageStats> usageStatsList = USMUtils.getUsageStats(context, 0, System.currentTimeMillis());
//        if (usageStatsList.size() > 0) {
//            USMImpl.parserUsageStatsList(context, usageStatsList, arr);
//        }
//        EL.i("=====>" + arr.toString());

//        Object s1 = getIUsageStatsManagerStub(context);
//        EL.i("s1=========+> " + s1);
//        try {
//            testGetService(context);
//        } catch (Throwable e) {
//            EL.e(e);
//        }
        long now = System.currentTimeMillis();
//        JSONArray arr = USMImpl.getUSMInfo(context, now-48*60*60*1000, now);
        JSONArray arr = USMImpl.getUSMInfo(context, now - 6 * 60 * 60 * 1000, now);
        EL.i("arr: " + arr);

//        USMCase.run(context);
    }


    /**
     * 获取 IUsageStatsManager$Stub$Proxy
     *
     * @param context
     * @return
     */
    public static Object getIUsageStatsManagerStub(Context context) {
        //android.app.usage.IUsageStatsManager$Stub$Proxy
        Object mService = ClazzUtils.getObjectFieldObject(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE), "mService");
        if (mService == null) {
            IBinder ibinder = null;
            try {
                Class<?> serviceManager = Class.forName("android.os.ServiceManager");
                Method getService = ClazzUtils.getMethod(serviceManager, "getService", String.class);
                ibinder = (IBinder) getService.invoke(null, Context.USAGE_STATS_SERVICE);
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_snap, e);
                }
            }
            if (ibinder == null) {
                ibinder = (IBinder) ClazzUtils.invokeStaticMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{Context.USAGE_STATS_SERVICE});
            }
            if (ibinder != null) {
                try {
                    Method asInterface = ClazzUtils.getMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", IBinder.class);
                    if (asInterface != null) {
                        mService = asInterface.invoke(null, ibinder);
                    }
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(BuildConfig.tag_snap, e);
                    }
                }

                if (mService == null) {
                    mService = ClazzUtils.invokeStaticMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{ibinder});
                }
            }
        }
        return mService;
    }

    private static void testGetService(final Context context) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = ClazzUtils.getMethod(serviceManager, "getService", String.class);
            IBinder rawBinder = (IBinder) getService.invoke(null, Context.USAGE_STATS_SERVICE);
            EL.i("rawBinder: " + rawBinder);
            IBinder binder = (IBinder) ClazzUtils.invokeStaticMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{Context.USAGE_STATS_SERVICE});
            EL.i("binder: " + binder);
            Object mService = Class.forName("android.app.usage.IUsageStatsManager$Stub").getMethod("asInterface", IBinder.class).invoke(null, rawBinder);
            EL.i("getMethod mService: " + mService);
            Object mService1 = Class.forName("android.app.usage.IUsageStatsManager$Stub").getDeclaredMethod("asInterface", IBinder.class).invoke(null, binder);
            EL.i("getDeclaredMethod mService1: " + mService1);
            Object mService2 = ClazzUtils.invokeStaticMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{binder});
            EL.i("invokeStaticMethod mService2: " + mService2);


            EL.e("反射获取=====>" + USMUtils.getIUsageStatsManagerStub(context));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static void runCaseP29(final Context context) {
        JSONArray arr = USMImpl.getUSMInfo(context, 0, System.currentTimeMillis());
        EL.i("arr: " + arr);
    }
    private static void runCaseP30(final Context context) {
        USMCase.simple(context);
    }


}
