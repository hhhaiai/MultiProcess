package com.device.tripartite.cases;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Process;

import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.impl.usm.USMImpl;
import com.analysys.track.internal.impl.usm.USMUtils;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.service.AnalysysAccessibilityService;
import com.analysys.track.service.AnalysysJobService;
import com.analysys.track.service.AnalysysService;
import com.analysys.track.utils.AccessibilityHelper;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.device.tripartite.cases.models.RefModelA;
import com.device.tripartite.cases.usmcase.USMCase;
import com.device.utils.DemoClazzUtils;
import com.device.utils.EL;

import org.json.JSONArray;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

public class PubCases {
    public static void runCaseTryGetLocation(Context context) {
//        LocationImpl.getInstance(context).tryGetLocationInfo(new ECallBack() {
//            @Override
//            public void onProcessed() {
//                EL.i("操作完毕。。");
//            }
//        });
    }

    public static void runCaseRealGetLocationInfo(Context context) {
//        LocationImpl.getInstance(context).getLocationInfoInThread();
    }

    public static void runCaseCheckManifest(final Context context) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("\n")
                .append("=================== ")
                .append("服务声明检测 ")
                .append(" ===================")
                .append("\n")
                .append("声明 AnalysysService 结果")
                .append(AndroidManifestHelper.isServiceDefineInManifest(context, AnalysysService.class))
                .append("\n")
                .append("声明 AnalysysAccessibilityService 结果")
                .append(AccessibilityHelper.isAccessibilitySettingsOn(context, AnalysysAccessibilityService.class))
                .append("\n")
                .append("声明 AnalysysJobService 结果")
                .append(AndroidManifestHelper.isJobServiceDefineInManifest(context, AnalysysJobService.class))
        ;
        EL.i(sb.toString());
    }


    public static void unCaseRingtone(Context context) {
        log("播放通知提示声音");
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone rt = RingtoneManager.getRingtone(context.getApplicationContext(), uri);
        rt.play();
        rt.stop();
    }
    private static void runCaseUSM1(final Context context) {
        JSONArray arr = USMImpl.getUSMInfo(context, 0, System.currentTimeMillis());
        EL.i("arr: " + arr);
    }

    private static void runCaseUSM2(final Context context) {
        USMCase.simple(context);
    }

    public static void runCaseUSM(final Context context) {
        long now = System.currentTimeMillis();
//        JSONArray arr = USMImpl.getUSMInfo(context, now-48*60*60*1000, now);
        JSONArray arr = USMImpl.getUSMInfo(context, now - 6 * 60 * 60 * 1000, now);
        EL.i("arr: " + arr);

    }

    /**
     * 获取 IUsageStatsManager$Stub$Proxy
     *
     * @param context
     * @return
     */
    public static Object getIUsageStatsManagerStub(Context context) {
        //android.app.usage.IUsageStatsManager$Stub$Proxy
        Object mService = DemoClazzUtils.getObjectFieldObject(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE), "mService");
        if (mService == null) {
            IBinder ibinder = null;
            try {
                Class<?> serviceManager = Class.forName("android.os.ServiceManager");
                Method getService = DemoClazzUtils.getMethod(serviceManager, "getService", String.class);
                ibinder = (IBinder) getService.invoke(null, Context.USAGE_STATS_SERVICE);
            } catch (Throwable e) {
            }
            if (ibinder == null) {
                ibinder = (IBinder) DemoClazzUtils.invokeStaticMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{Context.USAGE_STATS_SERVICE});
            }
            if (ibinder != null) {
                try {
                    Method asInterface = DemoClazzUtils.getMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", IBinder.class);
                    if (asInterface != null) {
                        mService = asInterface.invoke(null, ibinder);
                    }
                } catch (Throwable e) {
                }

                if (mService == null) {
                    mService = DemoClazzUtils.invokeStaticMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{ibinder});
                }
            }
        }
        return mService;
    }

    public static void runCasePatch(Context context) {
        try {

//            loadStatic(mContext, new File("/data/local/tmp/temp_20200108-180351.jar"),
//                  "com.analysys.Ab", "init",
//                    new Class[]{Context.class}, new Object[]{mContext});
            PatchHelper.loadStatic(null,context,
                    new File("/data/local/tmp/temp_20200108-180351.jar"),
                    "com.analysys.Ab", "init",
                    new Class[]{Context.class}, new Object[]{context});
        } catch (Throwable e) {
            EL.e(e);
        }
    }

    public static void runCaseRef1(final Context context) {


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

    public static void runCaseRef2(final Context context) {
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

    public static void runCaseRef3(final Context context) {

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

    public static void runCaseDoubleCardInfo(final Context context) {
        log("测试双卡");
        String imeis = DoubleCardSupport.getInstance().getIMEIS(context);
        EL.i("----测试双卡IMEI: " + imeis);
        String imsis = DoubleCardSupport.getInstance().getIMSIS(context);
        EL.i("----测试双卡IMSI: " + imsis);
    }

    public static void runCaseOC(final Context context) {
        OCImpl.getInstance(context).processOC();
    }

    public static void runCaseGetNetInfoCase(final Context context) {
        log("尝试获取netinfo");
        HashMap<String, NetInfo> info = NetImpl.getInstance(context).getNetInfo();
        EL.i("测试netinfo结果: " + info.toString());
    }

    public static void runCaseRealUpload(final Context context) {
        log("测试发起请求，接收策略");
        UploadImpl.getInstance(context).doUploadImpl();
    }

    public static void runCasePrepareUpload(final Context context) {
        log("测试发起请求，接收策略");
        UploadImpl.getInstance(context).upload();
    }

    public static void runCaseGetUsageStatsService(final Context context) {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = DemoClazzUtils.getMethod(serviceManager, "getService", String.class);
            IBinder rawBinder = (IBinder) getService.invoke(null, Context.USAGE_STATS_SERVICE);
            EL.i("rawBinder: " + rawBinder);
            IBinder binder = (IBinder) DemoClazzUtils.invokeStaticMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{Context.USAGE_STATS_SERVICE});
            EL.i("binder: " + binder);
            Object mService = Class.forName("android.app.usage.IUsageStatsManager$Stub").getMethod("asInterface", IBinder.class).invoke(null, rawBinder);
            EL.i("getMethod mService: " + mService);
            Object mService1 = Class.forName("android.app.usage.IUsageStatsManager$Stub").getDeclaredMethod("asInterface", IBinder.class).invoke(null, binder);
            EL.i("getDeclaredMethod mService1: " + mService1);
            Object mService2 = DemoClazzUtils.invokeStaticMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{binder});
            EL.i("invokeStaticMethod mService2: " + mService2);


            EL.e("反射获取=====>" + USMUtils.getIUsageStatsManagerStub(context));
        } catch (Throwable e) {
            EL.i(e);
        }
    }

    /**
     * 执行shell指令测试
     */
    public static void runCaseShell() {
        log("测试shell方法");
        long pid = Process.myPid();
        String s;
        s = shell("type su");
        EL.d("type su: " + s);
        s = shell("which su");
        EL.i("which su: " + s);
        s = shell("pm list packages");
        EL.d("pm list packages: " + s);
        s = shell(" pm list packages -s");
        EL.i(" pm list packages -s: " + s);
        s = shell("getprop ro.hardware");
        EL.d("getprop ro.hardware: " + s);

        s = shell("cat /proc/self/cmdline");
        EL.i(" cat /proc/self/cmdline: " + s.trim());

        s = shell("cat /proc/" + pid + "/cmdline");
        EL.d("cat /proc/" + pid + "/cmdline: " + s.trim());

        s = shell("ls -l  /proc/self/cmdline");
        EL.i(" ls -l  /proc/self/cmdline: " + s.trim());
        s = shell("ls -lau /proc/" + pid + "/cmdline");
        EL.d("ls -lau /proc/" + pid + "/cmdline: " + s.trim());
    }

    /**
     * 日志打印
     *
     * @param str
     */
    private static void log(String str) {
        EL.i("========================================");
        EL.i("===>" + str + "<===");
        EL.i("========================================");
    }


    /**
     * 执行shell
     *
     * @param s
     * @return
     */
    private static String shell(String s) {
        return (String) DemoClazzUtils.invokeStaticMethod("com.analysys.track.utils.ShellUtils", "shell", new Class[]{String.class}, new Object[]{s});
    }


}
