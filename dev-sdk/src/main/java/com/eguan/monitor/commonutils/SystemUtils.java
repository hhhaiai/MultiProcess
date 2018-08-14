package com.eguan.monitor.commonutils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import com.eguan.monitor.Constants;
import com.eguan.monitor.fangzhou.service.MonitorJobService;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by chris on 16/11/4.
 */

public class SystemUtils {
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                result = rest == PackageManager.PERMISSION_GRANTED;
            } catch (Exception e) {
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager
                    .PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }

    public static String getAppKey(Context context) {
        String key = "";
        if (context == null) return "";
        SPUtil spUtil = SPUtil.getInstance(context);
        key = spUtil.getKey();
        if (TextUtils.isEmpty(key) || key.length() != 17) {
            try {
                ApplicationInfo appInfo = context.getApplicationContext().getPackageManager()
                        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                key = appInfo.metaData.getString("egAppKey");
                if (!TextUtils.isEmpty(key) && key.length() == 17) {
                    spUtil.setKey(key);
                    return key;
                }
            } catch (Throwable e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            }

        }
        return key;
    }

    public static String getAppChannel(Context context) {
        String channel = "";
        if (context == null) return "";
        SPUtil spUtil = SPUtil.getInstance(context);
        channel = spUtil.getChannel();
        if (TextUtils.isEmpty(channel)) {
            try {
                ApplicationInfo appInfo = context.getApplicationContext()
                        .getPackageManager().getApplicationInfo(context
                                .getPackageName(), PackageManager.GET_META_DATA);
                channel = appInfo.metaData.getString("egChannel");
                if (!TextUtils.isEmpty(channel)) {
                    spUtil.setchannel(channel);
                    return channel;
                }
            } catch (Throwable e) {
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(e);
                }
            }
        }
        return channel;
    }

    public static void startJobService(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean runJobService = isJobPollServiceOn(context);
            if (!runJobService) {
                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context
                        .JOB_SCHEDULER_SERVICE);
                JobInfo.Builder builder = new JobInfo.Builder(Constants.JOB_ID, new ComponentName
                        (context, MonitorJobService.class.getName()));  //指定哪个JobService执行操作
                builder.setPeriodic(Constants.JOB_SERVICE_TIME);
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                jobScheduler.schedule(builder.build());
            }
        }

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isJobPollServiceOn(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context
                .JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        //getAllPendingJobs得到是当前Package对应的已经安排的任务
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == Constants.JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }
        return hasBeenScheduled;
    }


    // 服务是否运行
    public static boolean isServiceRunning(Context context, String serviceName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> lists = am.getRunningServices(Integer.MAX_VALUE);
        //判断是否返回为空集合
        if (lists == null || lists.isEmpty()) return false;
        for (ActivityManager.RunningServiceInfo info : lists) {// 获取运行服务再启动
            if (info.service.getClassName().equals(serviceName)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;

    }

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getApplicationContext().
                getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    @SuppressLint("MissingPermission")
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            return false;
        }
        if (null == cm || null == cm.getActiveNetworkInfo()) {
            return false;
        } else {
            return cm.getActiveNetworkInfo().isAvailable();
        }
    }

    /**
     * 反射机制判断类是否存在
     *
     * @param classPathe
     * @return
     */
    public static boolean classInspect(String classPathe) {
        try {
            Class.forName(classPathe);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public static boolean isWifi(Context context) {
        try {

            if (!checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                return false;
            }
            NetworkInfo networkInfo =
                    ((ConnectivityManager) context.getApplicationContext()
                            .getSystemService(Context.CONNECTIVITY_SERVICE))
                            .getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                }
            }
        } catch (Throwable e) {
            return false;
        }
        return false;
    }

    public static String getTitle(Context mActivity) {
        String title = "";
        if (mActivity instanceof Activity) {
            ActionBar actionBar = ((Activity) mActivity).getActionBar();
            if (actionBar != null) {
                title = actionBar.getTitle().toString();
            }
            if (TextUtils.isEmpty(title)) {
                title = (String) ((Activity) mActivity).getTitle();
            }
        }
        return title;
    }

    private static String EGUAN_CHANNEL_PREFIX = "EGUAN_CHANNEL_";
    private static String UNDERR_LINE = "_";

    /**
     * 仅用作多渠道打包,获取apk文件中的渠道信息
     *
     * @param context
     * @return
     */
    public static String getChannelFromApk(Context context) {

        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        //注意这里：默认放在meta-inf/里， 所以需要再拼接一下
        String channel_pre = "META-INF/" + EGUAN_CHANNEL_PREFIX;
        String channelName = "";
        ZipFile apkZip = null;
        try {
            apkZip = new ZipFile(sourceDir);
            Enumeration<?> entries = apkZip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(channel_pre)) {
                    channelName = entryName;
                    break;
                }
            }
            //假如没有在apk文件中找到相关渠道信息,则返回空串,表示没有调用易观多渠道打包方式
            if (TextUtils.isEmpty(channelName)) {
                return "";
            }
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (apkZip != null) {
                try {
                    apkZip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //Eg的渠道文件以EGUAN_CHANNEL_XXX为例,其XXX为最终的渠道信息
        return channelName.substring(23);
    }

    /**
     * 3.7.9.3加入  add 2018.4.10
     * 作用:执行shell命令
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            proc = Runtime.getRuntime().exec(cmd);
            in = new BufferedInputStream(proc.getInputStream());
            br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable e) {
                }
            }
            if (proc != null) {
                proc.destroy();
            }
        }

        return sb.toString();
    }


    private static final int MAX_KEY_LENGTH = 255;
    private static final int MAX_STRING_VALUE_LENGTH = 255;
    private static final int ARRAY_LIST_MAXSIZE = 100;

    /**
     * 检测传入的参数Map中的数据结构是否匹配规则
     *
     * @param property
     */
    public static void checkProperty(Map<String, Object> property) {
        //先判断是否为空值,如果为null,不做检测
        if (property == null) return;
        if (!property.isEmpty()) {
            for (Iterator<Map.Entry<String, Object>> iterator = property.entrySet().iterator();
                 iterator.hasNext(); ) {

                Map.Entry<String, Object> entry = iterator.next();
                if (!TextUtils.isEmpty(entry.getKey()) && entry.getKey().length() >
                        MAX_KEY_LENGTH) {
                    iterator.remove();
                    EgLog.e("传入自定义属性中 Key=" + entry.getKey().substring(0,
                            10) + "…… 长度超过255,被丢弃 ");
                    continue;
                }
                boolean isNeedRemove = checkValue(entry.getValue());
                if (isNeedRemove) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 检测Map中Value规则
     *
     * @param o
     * @return 如果不匹配, 则需要从Map中删除，返回true;反之,匹配则返回false;
     */
    private static boolean checkValue(Object o) {
        if (o == null) {
            return false;
        }
        try {
            if (o instanceof Boolean ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    (o instanceof String && ((String) o).length() <= MAX_STRING_VALUE_LENGTH) ||
                    (o instanceof List && ((List) o).size() <= ARRAY_LIST_MAXSIZE) ||
                    (o.getClass().isArray() && Array.getLength(o) <= ARRAY_LIST_MAXSIZE)) {
                //正常范围
                return false;
            } else if (o instanceof String && ((String) o).length() > MAX_STRING_VALUE_LENGTH) {
                EgLog.e("传入自定义属性中 value为字符串:" + ((String) o).substring(0, 10)
                        + "…… 长度超过255,被丢弃 ");
                return true;
            } else if (o instanceof List && ((List) o).size() > ARRAY_LIST_MAXSIZE) {
                EgLog.e("传入自定义属性中 value 为List，大小为" + ((List) o).size
                        () +
                        "…… 大小超过100,被丢弃 ");
                return true;
            } else if (o.getClass().isArray() && Array.getLength(o) > ARRAY_LIST_MAXSIZE) {
                EgLog.e("传入自定义属性中 value 为Array，长度为" + Array.getLength
                        (o) +
                        "…… 长度超过100,被丢弃 ");
                return true;
            } else {
                EgLog.e("传入自定义属性中 " +
                        "value类型不满足【String】【Number】【Boolean】【Datetime】【List】,被丢弃");
                return true;
            }

        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        //默认返回true，表示如果异常则直接从map中删除
        return true;
    }

    public static boolean isMainProcess(Context context) {
        try {
            if (context == null) return false;
            return context.getPackageName().equals(getCurrentProcessName(context));
        } catch (Throwable e) {
        }
        return false;

    }

    public static int getCurrentPID() {
        return android.os.Process.myPid();
    }

    public static String getCurrentProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                if (info.pid == pid) {
                    return info.processName;
                }
            }
        } catch (Throwable e) {

        }
        return "";
    }
}
