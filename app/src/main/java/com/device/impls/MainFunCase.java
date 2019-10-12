package com.device.impls;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.device.utils.AssetsHelper;
import com.device.utils.EL;
import com.device.utils.MyLooper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class MainFunCase {

    /**
     * 接收到方法
     *
     * @param context
     * @param x
     */
    public static void runCase(Context context, String x) {
//        EL.d("--- you click  btnCase" + x);
        try {
            Class<?> testCase = MainFunCase.class;
            Method runCaseA = testCase.getDeclaredMethod("runCaseP" + x, Context.class);
            runCaseA.invoke(null, context);
        } catch (Throwable e) {
            EL.v(e);
        }

    }


    // 1. 测试发起请求，接收策略
    private static void runCaseP1(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EL.i("=================== 测试发起请求，接收策略===============");
                    PolicyImpl.getInstance(context).clear();
                    UploadImpl.getInstance(context).doUploadImpl();

                } catch (Throwable e) {
                    EL.i(e);
                }
            }
        });
    }

    // 2. 测试接收并处理策略
    private static void runCaseP2(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EL.i("=================== 测试接收并处理策略===============");
                    testSavePolicy(context);
                } catch (Throwable e) {
                    EL.i(e);
                }
            }
        });
    }

    // 3. OC测试
    private static void runCaseP3(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("=================== OC测试 ===============");
                OCImpl.getInstance(context).processOC();
            }
        });
    }

    // 4.安装列表调试状态获取测试
    private static void runCaseP4(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("=================== 安装列表调试状态获取测试 ===============");

                List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();
                EL.i("列表:" + list);
            }
        });
    }

    // 5. 测试保存文件到本地,忽略调试设备状态加载
    private static void runCaseP5(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    // 6. 根据手机APP情况，随机抽5个进行OC逻辑验证
    private static void runCaseP6(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
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
                        if (pm.getLaunchIntentForPackage(pkg) != null && !ll.contains(pkg)) {
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
        });
    }

    // 7. OC逻辑验证
    private static void runCaseP7(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("=================== OC逻辑验证 ===============");
                OCImpl.getInstance(context).processOC();
            }
        });
    }

    //8. OC部分数据入库测试
    private static void runCaseP8(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
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
                        if (pm.getLaunchIntentForPackage(pkg) != null && !ll.contains(pkg)) {
                            ll.add(pkg);
                        }
                    }
                }

                // 2.放内存里
                OCImpl.getInstance(context).cacheDataToMemory(ll.get(0), "2");
                OCImpl.getInstance(context).cacheDataToMemory(ll.get(1), "2");
                OCImpl.getInstance(context).cacheDataToMemory(ll.get(2), "2");
                //写入库
                OCImpl.getInstance(context).processScreenOff();

                EL.i("=================== 从数据库取出OC数据 ===============");

                JSONArray oc = TableProcess.getInstance(context).selectOC(EGContext.LEN_MAX_UPDATE_SIZE);

                if (oc != null) {
                    EL.i("获取OC数据:" + oc.toString());
                }

            }
        });
    }

    // 9. 忽略进程直接发起网络请求
    private static void runCaseP9(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("----忽略进程直接发起网络请求-----");
                UploadImpl.getInstance(context).doUploadImpl();
            }
        });
    }

    // 10.【安装列表】检查并更新数据库数据
    private static void runCaseP10(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("----【安装列表】检查并更新数据库数据-----");
                AppSnapshotImpl.getInstance(context).getSnapShotInfo();
            }
        });

    }


    // 11. 【安装列表】查询数据库
    private static void runCaseP11(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("----【安装列表】数据库-----");
                JSONArray ins = TableProcess.getInstance(context).selectSnapshot(EGContext.LEN_MAX_UPDATE_SIZE);
                EL.i(ins);
            }
        });
    }

    // 12.【定位信息】直接获取。。。忽略多进程
    private static void runCaseP12(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("----【定位信息】直接获取。。。忽略多进程-----");
                LocationImpl.getInstance(context).getLocationInfoInThread();
            }
        });
    }

    // 13. 测试加密数据
    private static void runCaseP13(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("----测试加密数据-----");
                UploadImpl.getInstance(context).messageEncrypt("测试加密数据");
            }
        });
    }

    // 14.测试双卡
    private static void runCaseP14(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                EL.i("----测试双卡-----");
                String imeis = DoubleCardSupport.getInstance().getIMEIS(context);
                EL.i("----测试双卡IMEI: " + imeis);
                String imsis = DoubleCardSupport.getInstance().getIMSIS(context);
                EL.i("----测试双卡IMSI: " + imsis);
            }
        });
    }

    private static void runCaseP15(final Context mContext) {
        try {
            File innerF = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/test.jar");
            File re = new File(mContext.getFilesDir().getAbsolutePath() + "/test.jar");
            try {
                re.getParentFile().mkdirs();
                FileInputStream fileInputStream = new FileInputStream(innerF);
                FileOutputStream outputStream = new FileOutputStream(re);

                byte[] b = new byte[1024];
                int len = -1;
                while ((len = fileInputStream.read(b)) != -1) {
                    outputStream.write(b);
                }
                fileInputStream.close();
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            innerF = re;
            String classname = "AD";
            String methodName = "getString";
            Class[] pt = new Class[]{String.class};
            Object[] pv = new Object[]{"123123"};
            //测试加载内部路径jar
            classname = "AD";
            methodName = "getString";
            pt = new Class[]{String.class};
            pv = new Object[]{"123123"};
            PatchHelper.loadStatic(mContext, innerF, classname, methodName, pt, pv);
            //测试加载静态内部类的static方法
            classname = "AD$Inner";
            methodName = "getString";
            pt = new Class[]{String.class};
            pv = new Object[]{"123123"};
            PatchHelper.loadStatic(mContext, innerF, classname, methodName, pt, pv);
            //测试反射ActivityThread
            classname = "AD$Inner";
            methodName = "closeAndroidPDialog";
            pt = new Class[]{};
            pv = new Object[]{};
            PatchHelper.loadStatic(mContext, innerF, classname, methodName, pt, pv);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /********************************** 功能实现区 ************************************/

    /**
     * 测试解析策略 部分内容
     *
     * @param context
     * @throws JSONException
     */
    private static void testSavePolicy(Context context) throws JSONException {
        PolicyImpl.getInstance(context).clear();
        JSONObject obj = new JSONObject(AssetsHelper.getFromAssetsToString(context, "policy_body.txt"));
        PolicyImpl.getInstance(context).saveRespParams(obj);
    }


}
