package com.device.impls;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.model.BatteryModuleNameInfo;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.reflectinon.DoubleCardSupport;
import com.device.utils.AssetsHelper;
import com.device.utils.EL;
import com.device.utils.MyLooper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
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
        FileObserver fileObserver = new FileObserver("/proc/net/tcp", FileObserver.ALL_EVENTS) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.v(path, event + "");
            }
        };
        fileObserver.startWatching();
    }

    private static void runCaseP16(final Context mContext) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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


                long abs = 0;
                int max = 0, min = Integer.MAX_VALUE;
                long time = System.currentTimeMillis();
                for (int i = 0; i < test_size; i++) {
                    String result[] = {
                            "cat /proc/net/tcp",
                            "cat /proc/net/tcp6",
                            "cat /proc/net/udp",
                            "cat /proc/net/udp6",
                            "cat /proc/net/raw",
                            "cat /proc/net/raw6",
                    };
                    HashSet<NetInfo> pkgs = new HashSet<NetInfo>();
                    try {
                        for (String cmd : result
                        ) {
                            // pkgs.addAll(NetImpl.getInstance(mContext).getNetInfoFromCmd(cmd));
                        }
                    } catch (Exception e) {
                        throwables.add(e);
                    }
                    JSONArray array = new JSONArray();
                    for (NetInfo info :
                            pkgs) {
                        array.put(info.toJson());
                    }
                    String json = array.toString();

                    int length = json.length();
                    max = Math.max(max, length);
                    min = Math.min(min, length);
                    abs = (abs + length);
                    Log.v("testcasep16", i + "");
                }
                abs = abs / test_size;
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
                        .append(freeMemory).append("\n")
                        .append("平均:最大:最小:")
                        .append(abs).append("\n")
                        .append(max).append("\n")
                        .append(min).append("\n");

                for (Throwable throwable : throwables
                ) {
                    builder.append(throwable.getMessage()).append("\n");
                }

                try {
                    FileOutputStream outputStream = new FileOutputStream(mContext.getCacheDir().getAbsoluteFile() + "/netimpl.log");
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

                while (true) {
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone rt = RingtoneManager.getRingtone(mContext.getApplicationContext(), uri);
                    rt.play();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runCaseP18(final Context mContext) {
        NetImpl.getInstance(mContext).getNetInfo();
    }

    private static void runCaseP19(final Context mContext) {
    }

    private static void runCaseP20(final Context mContext) {
    }

    private static void runCaseP21(final Context mContext) {
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
