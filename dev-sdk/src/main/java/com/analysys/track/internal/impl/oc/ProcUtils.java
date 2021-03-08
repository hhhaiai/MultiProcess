package com.analysys.track.internal.impl.oc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SystemUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 方舟进程管理
 * @Version: 1.0
 * @Create: Mar 6, 2019 8:26:31 PM
 * @Author: sanbo
 */
public class ProcUtils {
    public static final String RUNNING_TIME = "time";
    public static final String RUNNING_RESULT = "result";
    public static final String RUNNING_OC_RESULT = "ocr";
    private static final String RUNNING_TOP = "top";
    private static final String RUNNING_PS = "ps";
    private static final int DEF_VALUE = -8801;
    public Context mContext = null;

    private ProcUtils() {
    }

    public static ProcUtils getInstance(Context cxt) {
        return Holder.Instance.init(cxt);
    }

    private ProcUtils init(Context cxt) {
        if (mContext == null) {
            mContext = EContextHelper.getContext();
        }
        return Holder.Instance;
    }

    /**
     * 获取详情.
     *
     * @return
     */
    public JSONObject getRunningInfo() {

        JSONObject uploadInfo = null;
        try {
            uploadInfo = new JSONObject();
            uploadInfo.put(RUNNING_TIME, System.currentTimeMillis());

            // 1. get source data
            List<ProcessInfo> infos = getSouceProcessInfo();
            if (infos != null && infos.size() > 0) {
                // 解析Proc和result
                JSONObject jsonObject = processInfos(infos);

                if (jsonObject != null && jsonObject.length() > 0) {

                    // 解析oc使用的结果
                    Object ocResult = jsonObject.opt(RUNNING_OC_RESULT);
                    if (ocResult != null && ocResult != "") {
                        JSONArray resultArray = new JSONArray(ocResult.toString());
                        if (resultArray.length() > 0) {
                            uploadInfo.put(RUNNING_OC_RESULT, resultArray);
                        }
                    }

                    // 解析result
                    Object result = jsonObject.opt(RUNNING_RESULT);
                    if (result != null && result != "") {
                        JSONArray resultArray = new JSONArray(result.toString());
                        if (resultArray.length() > 0) {
                            uploadInfo.put(RUNNING_RESULT, resultArray);
                        }
                    }

                }
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(t);
            }
        }
        return uploadInfo;
    }


    /**
     * 解析结果
     *
     * @param infos
     */
    private JSONObject processInfos(List<ProcessInfo> infos) {
        JSONObject object = new JSONObject();
        try {
            Set<String> temp = new HashSet<String>();
            Set<String> ocr = new HashSet<String>();
            JSONArray resultArray = new JSONArray();
            PackageManager pm = null;
            if (mContext != null) {
                pm = mContext.getApplicationContext().getPackageManager();
            }
            List<ProcessInfo> ss = new ArrayList<ProcessInfo>(infos);
            for (int i = 0; i < ss.size(); i++) {
                try {
                    ProcessInfo info = ss.get(i);
                    // parser every one.
                    int pid = info.getPid();
                    String pkg = info.getPkgName();


                    if (SystemUtils.hasLaunchIntentForPackage(pm, pkg)) {
                        int oomScore = getOOMScore(pid, pkg);
                        String cpuset = getCpuset(pid, pkg);
                        String cgroup = getCgroup(pid, pkg);
                        String stat = getStat(pid, pkg);
                        String status = getStatus(pid, pkg);

                        String oomAdj = getOOMAdj(pid, pkg);


                        if (!temp.contains(pkg)) {
                            temp.add(pkg);
                            // add item to result
                            tryAddItemToResult(resultArray, pid, pkg, oomScore, cpuset, cgroup, oomAdj);
                        }
                        checkForOcr(ocr, pkg, oomScore, cpuset, cgroup, stat, status);
                    }


                } catch (Throwable ttt) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(ttt);
                    }
                }

            }
            object.put(RUNNING_OC_RESULT, ocr);
            object.put(RUNNING_RESULT, resultArray);

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
            // L.e(e);
        }
        return object;
    }

    private void tryAddItemToResult(JSONArray resultArray, int pid, String pkg,
                                    int oomScore, String cpuset, String cgroup, String oomAdj) {
        try {
            if (oomScore > 120) {
                return;
            }
            JSONObject resultItemJson = new JSONObject();
            resultItemJson.putOpt("pid", pid);
            resultItemJson.putOpt("oomScore", oomScore);
            resultItemJson.putOpt("pkg", pkg);
            resultItemJson.putOpt("cpuset", cpuset);
            resultItemJson.putOpt("cgroup", cgroup);
            resultItemJson.putOpt("oomAdj", oomAdj);
            if (resultItemJson.length() > 0) {
                resultArray.put(resultItemJson);
            }
        } catch (Throwable e) {
        }
    }


    private void checkForOcr(Set<String> ocr, String pkg, int oomScore, String cpuset, String cgroup,
                             String stat, String status) {
        try {
            boolean a = isForeGroundByOomScore(oomScore);
            boolean b = isForeGroundByCpuset(cpuset);
            boolean c = isForeGroundByCgroup(cgroup);
            boolean d = isForeGroundByStat(stat);
            boolean a6 = isForeGroundBystatus(status);

            // 只要有符合前台的，暂时可以理解成就在前台
            if (a || b || c || d || a6) {
                try {
                    if (!ocr.contains(pkg)) {
                        ocr.add(pkg);
                    }
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
        } catch (Throwable e) {
        }

    }

    /***************************************************************************/

    private boolean isForeGroundByCpuset(String cpuset) {
        return !TextUtils.isEmpty(cpuset) && cpuset.contains("foreground");
    }

    private boolean isForeGroundByOomScore(int oomScore) {
        return oomScore < 100;
    }

    private boolean isForeGroundByStat(String stat) {

        try {
            if (!TextUtils.isEmpty(stat)) {
                String[] ss = stat.split("\\s+");
                String s = ss[38];
                if (Integer.parseInt(s) != 0) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }

        return false;
    }

    private boolean isForeGroundBystatus(String status) {
        if (!TextUtils.isEmpty(status)) {
            String[] statuses = status.split("\n");
            if (statuses.length > 0) {
                for (String line : statuses) {
                    if (!TextUtils.isEmpty(line)) {
                        line = line.trim().replace("\t", "    ");
                        if (line.startsWith("Cpus_allowed:") && !line.contains("01")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isForeGroundByCgroup(String cgroup) {
        if (!TextUtils.isEmpty(cgroup)
                && (cgroup.contains("cpuset:/foreground") || !cgroup.contains("cpu:/bg_non_interactive"))) {
            return true;
        }
        return false;
    }


    /***************************************************************************/

    // 前台：Cpus_allowed: ff 后台：Cpus_allowed: 01
    // 前台：Cpus_allowed: 3f 后台：Cpus_allowed: 01
    // 前台：Cpus_allowed: f 后台：Cpus_allowed: 01
    private String getStatus(int pid, String pkg) {
        String status = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/status"});
        // if (isDebug) {
        // L.i("status [" + pkg + "]-----" + status);
        // }
        return status;
    }

    /***************************************************************************/
    /***************************** 获取方法 ***********************************/

    // 前台：第36列不等于0 后台：第36列为0 [android shell执行的时候出现pid和包名，多了两位]
    private String getStat(int pid, String pkg) {
        String stat = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/stat"});
        // if (isDebug) {
        // L.i("stat [" + pkg + "]-----" + stat);
        // }
        return stat;
    }

    // 前台：cpuset:/foreground 后台：cpuset:/background
    // 前台：cpu:/ 后台：cpu:/bg_non_interactive
    private String getCgroup(int pid, String pkg) {
        String cgroup = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/cgroup"});
        // if (isDebug) {
        // L.i("cgroup [" + pkg + "]-----" + cgroup);
        // }
        return cgroup;
    }

    // 前台：/foreground 后台：/background
    private String getCpuset(int pid, String pkg) {
        String cpuset = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/cpuset"});
        // if (isDebug) {
        // L.i("cpuset [" + pkg + "] -----" + cpuset);
        // }
        return cpuset;
    }

    // 前台：小于100 后台：大于100
    private int getOOMScore(int pid, String pkg) {
        String oom_score = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/oom_score"});
        // if (isDebug) {
        // L.i("oom_score [" + pkg + "]-----" + oom_score);
        // }
        try {
            return Integer.parseInt(oom_score.trim());
        } catch (Throwable e) {
        }
        return DEF_VALUE;
    }


    // oom_adj
    private String getOOMAdj(int pid, String pkg) {
        String oom_adj = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/oom_adj"});
        // if (isDebug) {
        // L.i("oom_adj [" + pkg + "]-----" + oom_adj);
        // }
        return oom_adj;
    }

    /**
     * 解析置顶 PID+包名.
     *
     * @return
     */
    private List<ProcessInfo> getSouceProcessInfo() {
        List<ProcessInfo> infos = new ArrayList<ProcessInfo>();

        try {
            String sourceData = null;
            // top-->ps with args-->ps --> files
            sourceData = ShellUtils.exec(new String[]{RUNNING_TOP, "-n", "1"});
            if (TextUtils.isEmpty(sourceData)) {
                sourceData = ShellUtils.exec(new String[]{RUNNING_PS, "-P", "-p", "-x", "-c"});
                if (TextUtils.isEmpty(sourceData)) {
                    sourceData = ShellUtils.exec(new String[]{RUNNING_PS});
                }
            }

            if (TextUtils.isEmpty(sourceData)) {
                forEachProc(infos);
            } else {
                String[] tts = sourceData.split("\n");
                if (tts != null && tts.length > 0) {
                    for (int i = 0; i < tts.length; i++) {
                        try {
                            String line = tts[i].trim();
                            if (!TextUtils.isEmpty(line)) {
                                String[] ars = line.split("\\s+");
                                int pid = -1;
                                try {
                                    String pp = TextUtils.isEmpty(ars[0]) ? ars[1] : ars[0];
                                    pid = Integer.parseInt(pp);
                                    String pkgName = ars[ars.length - 1];
                                    //增加过滤规则
                                    if (!TextUtils.isEmpty(pkgName) && pkgName.contains(".") && !pkgName.contains(":")
                                            && !pkgName.contains("/")) {
                                        infos.add(new ProcessInfo(pid, pkgName));
                                    }

                                } catch (Throwable e) {
                                }
                            }
                        } catch (Throwable e) {
                            if (BuildConfig.ENABLE_BUG_REPORT) {
                                BugReportForTest.commitError(e);
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
        }

        return infos;
    }

    /**
     * 遍历文件夹获取PID. 进程名字条件:
     * </p>
     * 1. 必须包含`.` 2. 不能包含`:`和`/`
     *
     * @param infos
     */
    private void forEachProc(List<ProcessInfo> infos) {
        try {
            File[] files = new File("/proc").listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    int pid;
                    try {
                        pid = Integer.parseInt(file.getName());
                    } catch (Throwable e) {
                        continue;
                    }
                    String pkgName = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/cmdline"});
                    if (!TextUtils.isEmpty(pkgName) && pkgName.contains(".") && !pkgName.contains(":")
                            && !pkgName.contains("/")) {
                        infos.add(new ProcessInfo(pid, pkgName.trim()));
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }


    private static class Holder {
        private static ProcUtils Instance = new ProcUtils();
    }
}
