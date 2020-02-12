package com.device.impls.cases;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.model.BatteryModuleNameInfo;
import com.analysys.track.utils.SystemUtils;
import com.device.utils.EL;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CaseImpls {


    public static void case16Impl(Context mContext) {
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
            String[] result = {
                    "cat /proc/net/tcp",
                    "cat /proc/net/tcp6",
                    "cat /proc/net/udp",
                    "cat /proc/net/udp6",
                    "cat /proc/net/raw",
                    "cat /proc/net/raw6",
            };
            HashSet<NetInfo> pkgs = new HashSet<NetInfo>();
            try {
                for (String cmd : result) {
//                    pkgs.addAll(NetImpl.getInstance(mContext).getNetInfoFromCmd(cmd));
                }
            } catch (Exception e) {
                throwables.add(e);
            }
            JSONArray array = new JSONArray();
            for (NetInfo info : pkgs) {
                array.put(info.toJson());
            }
            String json = array.toString();

            int length = json.length();
            max = Math.max(max, length);
            min = Math.min(min, length);
            abs = (abs + length);
            EL.v("testcasep16", i + "");
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

    public static void caseRongQi(Context context) {
        String pkgName = context.getPackageName();

        EL.i("容器运行检测, 包名：  " + pkgName);

        //1. 安装列表不包含自己,肯定不行
        if (!SystemUtils.hasPackageNameInstalled(context, pkgName)) {
            EL.i("容器运行检测, 安装列表不存在自己安装的app   ------》 容器运行");
            return;
        } else {
            EL.i("容器运行检测, 安装列表包含自己的app");
        }
        // 2. /data/data/pkg/files
        //   /data/user/0/pkg/files
        // 下面代码兼容性文件比较严重，小米双开无法识别
//        String fPath = context.getFilesDir().getAbsolutePath();
//        L.i("file path:" + fPath);
//        if (!fPath.startsWith("/data/data/" + pkgName + "/")
//                && !fPath.startsWith("/data/user/0/" + pkgName + "/")
//        ) {
//            return true;
//        }
        // 3. 遍历文件夹
        try {
            File dir = new File("/data/data/" + pkgName + "/files");
            if (dir.exists()) {
                EL.i("容器运行检测: " + dir.exists() + "----文件个数:" + dir.list().length + "-------->" + Arrays.asList(dir.list()));
            } else {
                EL.i("容器运行检测, files文件夹不存在，创建文件夹");
                dir.mkdirs();
            }
            if (!dir.exists()) {
                EL.i("容器运行检测, files文件夹创建失败    ------》 容器运行 ");
                return;
            }
            File temp = new File(dir, "test");
            if (temp.exists()) {
                EL.i("容器运行检测, test文件存在，删除重新操作.");
                temp.delete();
            }
            EL.i("容器运行检测, test文件不存在，尝试创建");

            boolean result = temp.createNewFile();
            if (!result) {
                EL.i("容器运行检测, test创建失败...   ------》 容器运行");
                return;
            } else {
                EL.i("容器运行检测, test创建成功...");
            }
        } catch (Throwable e) {
            EL.e(e);
        }

        // 4. 通过shell ps获取对应进程信息，理论上只有自己的包名和和子进程的。 必须包含自己包名
//        try {
//            String psInfo = ShellUtils.shell("ps");
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.i("容器运行检测 shell ps: " + psInfo);
//            }
//            if (!TextUtils.isEmpty(psInfo) && !psInfo.contains(pkgName)) {
//                return true;
//            }
//        } catch (Throwable e) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.e(e);
//            }
//        }


//        // 5. pid check /proc/pid/cmdline
//        int pid = android.os.Process.myPid();
//        L.e("pid:" + pid);
        // 6. classloader name check failed
//        L.i("----------->" + getClass().getClassLoader().getClass().getName());
//        L.i("---+++++++-->" + context.getClassLoader().getClass().getName());
    }
}
