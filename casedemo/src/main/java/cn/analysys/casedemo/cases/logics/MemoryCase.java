package cn.analysys.casedemo.cases.logics;

import android.app.ActivityManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.testcaselib.defcase.ETestCase;
import me.hhhaiai.testcaselib.utils.L;

public class MemoryCase extends ETestCase {

    public MemoryCase() {
        super("内存使用状况");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        new Thread(() -> {
            try {
                gotoWork();
            } catch (Throwable e) {
                L.e();
            }
        }).start();
        return true;
    }

    private void gotoWork() {
        StringBuffer sb = new StringBuffer();

        sb.append("==================内存信息获取================\n");
        sb.append("系统总内存(字节):")
                .append(getTotalMemorySize(SDKHelper.getContext()))
                .append("\n");
        sb.append("当前可用内存(字节):")
                .append(getAvailableMemory(SDKHelper.getContext()))
                .append("\n");
        Woo.logFormCase(sb.toString());
    }


    /**
     * 获取系统总内存
     *
     * @param context 可传入应用程序上下文。
     * @return 总内存大单位为B。
     */
    public static long getTotalMemorySize(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
        } catch (Exception e) {
            L.e(e);
        }
        return 0;
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

}

