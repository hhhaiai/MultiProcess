package com.analysys.track.impl.proc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONException;
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
    private static final String RUNNING_TOP = "top";
    private static final String RUNNING_PS = "ps";
    public static final String RUNNING_TIME = "time";
    private static final String RUNNING_PROC = "proc";
    public static final String RUNNING_RESULT = "result";
    public static final String RUNNING_OC_RESULT = "ocr";
    private static final int DEF_VALUE = -8801;
    public Context mContext = null;

    private ProcUtils() {
    }

    public static ProcUtils getInstance(Context cxt) {
        Holder.Instance.init(cxt);
        return Holder.Instance;
    }

    private void init(Context cxt) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(cxt);
        }
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

            // 1.获取top数据
            String top = ShellUtils.exec(new String[]{RUNNING_TOP, "-n", "1"});
            top = filterData(top);
//            JsonUtils.save(uploadInfo, RUNNING_TOP, top);

            // 2.获取ps数据，方式一
            String ps = ShellUtils.exec(new String[]{RUNNING_PS, "-P", "-p", "-x", "-c"});
            ps = filterData(ps);
            if (!TextUtils.isEmpty(ps)) {
//                JsonUtils.save(uploadInfo, RUNNING_PS, ps);
            } else {
                // 3.获取ps数据，方式二
                ps = ShellUtils.exec(new String[]{RUNNING_PS});
                ps = filterData(ps);
//                if (!TextUtils.isEmpty(ps)) {
//                    JsonUtils.save(uploadInfo, RUNNING_PS, ps);
//                }
            }

            // 4. 确保能生成有效的pid+pkgname
            List<Process> infos = getTopPkg(top, ps);

            // Log.i("PROC", "原始的infos[" + infos.size() + "]: " + infos.toString());

            if (infos != null && infos.size() > 0) {
                //解析Proc和result
                JSONObject jsonObject = processInfos(infos);
//                print("解析后", jsonObject.toString());

                if (jsonObject != null && jsonObject.length() > 0) {
//                    // 解析proc
//                    Object proc = jsonObject.opt("proc");
//                    if (proc != null && proc != "") {
//                        JSONArray procArray = new JSONArray(proc.toString());
//                        uploadInfo.put(RUNNING_PROC, procArray);
//                    }

                    // 解析oc使用的结果
                    Object ocResult = jsonObject.opt(RUNNING_OC_RESULT);
                    if (ocResult != null && ocResult != "") {
                        JSONArray resultArray = new JSONArray(ocResult.toString());
//                        if (resultArray.length() > 0) {
//                            for (int i = 0; i < resultArray.length(); i++) {
//                                String pkg = resultArray.optString(i);
//                                if (!nowPackageNames.contains(pkg)) {
//                                    nowPackageNames.add(pkg);
//                                }
//                            }
//                        }
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
            // L.i(t);
        }
//        print("uploadInfo", uploadInfo.toString());
        return uploadInfo;
    }


    /**
     * 过滤有效数据
     *
     * @param srcData
     * @return
     */
    private String filterData(String srcData) {
        StringBuffer sb = new StringBuffer();
        String[] tts = srcData.split("\n");
        if (tts != null && tts.length > 0) {
            for (int i = 0; i < tts.length; i++) {
                String line = tts[i].trim();
                // 兼容   ps 多参数命令，去除()中的用户信息. TODO 可以考虑使用
//system    17575 780   1589612 30916 4  20    0     0     0     fg  SyS_epoll_ 0000000000 S com.oppo.oppopowermonitor (u:4, s:2)
                if (line.contains("(")) {
                    line = line.substring(0, line.indexOf("("));
                }
                if (!TextUtils.isEmpty(line)) {
                    if (line.contains("u0_") && line.contains(".") && !line.contains(":") && !line.contains("/")) {
                        sb.append(line).append("\n");
                    }
                }
            }
        }
        // 去除最后一个/n
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 解析结果
     *
     * @param infos
     */
    private JSONObject processInfos(List<Process> infos) {
        JSONObject object = new JSONObject();
        Set<String> temp = new HashSet<String>();
        Set<String> ocr = new HashSet<String>();
        //for proc
//        JSONObject procItemJson = null;
//        JSONArray procInfoArray = new JSONArray();
        //for result
        JSONObject resultItemJson = null;
        JSONArray resultArray = new JSONArray();
        PackageManager pm = null;
        Intent intent = null;
        if (mContext != null) {
            pm = mContext.getApplicationContext().getPackageManager();
        }
        try {

            List<Process> ss = new ArrayList<Process>(infos);
            for (int i = 0; i < ss.size(); i++) {
                try {
                    Process info = ss.get(i);
                    // parser every one.
                    int pid = Integer.parseInt(info.getPid());
                    String pkg = info.getName();

                    int oomScore = getOOMScore(pid, pkg);
                    String cpuset = getCpuset(pid, pkg);
                    String cgroup = getCgroup(pid, pkg);
                    String stat = getStat(pid, pkg);
                    String status = getStatus(pid, pkg);

                    String cmdline = getCmdline(pid, pkg);
                    String statm = getStatm(pid, pkg);
                    String current = getCurrent(pid, pkg);
                    String oomAdj = getOOMAdj(pid, pkg);
                    String oomScoreAdj = getOOMScoreAdj(pid, pkg);

                    String wchan = getWchan(pid, pkg);
                    String sched = getSched(pid, pkg);
                    String dev = getDev(pid, pkg);
                    String snmp = getSnmp(pid, pkg);
                    try {
                        if (pm != null) {
                            intent = pm.getLaunchIntentForPackage(pkg);
                        }

                        if (!temp.contains(pkg)) {
                            temp.add(pkg);
//                            procItemJson = new JSONObject();
//                            appendToProc(procItemJson, pid, pkg, procInfoArray, intent, oomScore, cpuset, cgroup, stat, status, cmdline
//                                    , statm, current, oomAdj, oomScoreAdj, sched, wchan, dev, snmp);
                            // add item to result
                            tryAddItemToResult(resultItemJson, resultArray, pid, pkg, intent
                                    , oomScore, cpuset, cgroup, oomAdj);
                        }
                    } catch (Throwable e) {
                        // sb.append("发生异常。。。。。。。");
                    }

                    checkForOcr(ocr, pkg, intent, oomScore, cpuset, cgroup, stat, status, cmdline
                            , statm, current, oomAdj, oomScoreAdj, sched, wchan, dev, snmp);
                } catch (Throwable ttt) {
                    // Log.e("PROC", Log.getStackTraceString(ttt));
                }

            }
//            object.put(RUNNING_PROC, procInfoArray);
            object.put(RUNNING_OC_RESULT, ocr);
            object.put(RUNNING_RESULT, resultArray);
            // Log.i("PROC", "获取后的result===>: " + result.toString());

        } catch (Throwable e) {
            // L.e(e);
        }
        return object;
    }

    private void tryAddItemToResult(JSONObject resultItemJson, JSONArray resultArray, int pid, String pkg, Intent intent, int oomScore, String cpuset, String cgroup, String oomAdj) throws JSONException {
        if (intent == null) {
            return;
        }
        if (oomScore > 120) {
            return;
        }
        resultItemJson = new JSONObject();
        resultItemJson.put("pid", pid);
        resultItemJson.put("oomScore", oomScore);
        JsonUtils.save(resultItemJson, "pkg", pkg);
        JsonUtils.save(resultItemJson, "cpuset", cpuset);
        JsonUtils.save(resultItemJson, "cgroup", cgroup);
        JsonUtils.save(resultItemJson, "oomAdj", oomAdj);
        resultArray.put(resultItemJson);
    }


    private void appendToProc(JSONObject jsonResult, int pid, String pkg, JSONArray resultInfo, Intent intent, int oomScore, String cpuset, String cgroup, String stat, String status, String cmdline, String statm, String current, String oomAdj, String oomScoreAdj, String sched, String wchan, String dev, String snmp) throws JSONException {

        if (oomScore != -1) {
            jsonResult.put("oomScore", oomScore);
        }
        jsonResult.put("pid", pid);
        JsonUtils.save(jsonResult, "pkg", pkg);
        JsonUtils.save(jsonResult, "cpuset", cpuset);
        JsonUtils.save(jsonResult, "cgroup", cgroup);
        JsonUtils.save(jsonResult, "stat", stat);
        JsonUtils.save(jsonResult, "status", status);
        JsonUtils.save(jsonResult, "cmdline", cmdline);
        JsonUtils.save(jsonResult, "statm", statm);
        JsonUtils.save(jsonResult, "current", current);
        JsonUtils.save(jsonResult, "oomAdj", oomAdj);
        JsonUtils.save(jsonResult, "oomScoreAdj", oomScoreAdj);
        JsonUtils.save(jsonResult, "wchan", wchan);
        JsonUtils.save(jsonResult, "sched", sched);
        JsonUtils.save(jsonResult, "dev", dev);
        JsonUtils.save(jsonResult, "snmp", snmp);

        jsonResult.put("canLanch", intent == null ? 0 : 1);// 0不可以，1可以
        resultInfo.put(jsonResult);

    }

    private void checkForOcr(Set<String> ocr, String pkg, Intent intent, int oomScore
            , String cpuset, String cgroup, String stat, String status
            , String cmdline, String statm, String current, String oomAdj, String oomScoreAdj
            , String sched, String wchan, String dev, String snmp) {
        boolean a = isForeGroundByOOMScore(oomScore);
        boolean b = isForeGroundByCpuset(cpuset);
        boolean c = isForeGroundByCgroup(cgroup);
        boolean d = isForeGroundByStat(stat);
        boolean a6 = isForeGroundBystatus(status);
        boolean f = isForeGroundByCmdline(cmdline, pkg);
        boolean g = isForeGroundByStatm(statm);
        boolean h = isForeGroundByCurrent(current);
        boolean a0 = isForeGroundByOOMAdj(oomAdj);
        boolean a1 = isForeGroundByOOMScoreAdj(oomScoreAdj);
        boolean a2 = isForeGroundByWchan(wchan);
        boolean a3 = isForeGroundBySched(sched);
        boolean a4 = isForeGroundByDev(dev);
        boolean a5 = isForeGroundBySnmp(snmp);
        // Log.i("PROC", "条件 " + a
        // + "--" + b
        // + "----" + c
        // + "----" + d
        // + "----" + a6
        // + "----" + f
        // + "----" + g
        // + "----" + h
        // + "----" + a0
        // + "----" + a1
        // + "----" + a2
        // + "----" + a3
        // + "----" + a4
        // + "----" + a5
        // );

        // 只要有符合前台的，暂时可以理解成就在前台
        if (a || b || c || d || a6 || f || g || h || a0 || a1 || a2 || a3 || a4 || a5) {
            // sb.append("有条件符合....");
            try {
                if (!ocr.contains(pkg) && intent != null) {

                    // sb.append("加入成功");
                    // Log.i("PROC", "=====>: " + sb.toString());
                    ocr.add(pkg);
                }
            } catch (Throwable e) {
                // L.e(e);
            }
        } else {
            // sb.append("不符合规则");
            // Log.w("PROC", "=====>: " + sb.toString());

        }

    }

    /***************************************************************************/

    private boolean isForeGroundByCpuset(String cpuset) {
        if (!TextUtils.isEmpty(cpuset) && cpuset.contains("foreground")) {
            return true;
        }
        return false;
    }

    /***************************************************************************/
    /***************************** 直接检查的方法 ***********************************/

    private boolean isForeGroundByOOMScore(int oom_score) {
        if (oom_score < 100) {
            return true;
        }
        return false;
    }

    private boolean isForeGroundByStat(String stat) {
        if (!TextUtils.isEmpty(stat)) {
            String[] ss = stat.split("\\s+");
            try {
                String s = ss[38];
                if (Integer.parseInt(s) != 0) {
                    return true;
                }
            } catch (Throwable e) {
                // L.e(e);
            }
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

    /**
     * 暂时不让生效 路径: /proc/%d/cmdline
     * </p>
     * 意义: 运行的进程名. 僵尸进程没东西.也就意味着这个名字是top解析的
     *
     * @param cmdline
     * @param pkg
     * @return
     */
    private boolean isForeGroundByCmdline(String cmdline, String pkg) {
        // if (!TextUtils.isEmpty(cmdline) && cmdline.contains(pkg) && !cmdline.contains(":") && !cmdline.contains("/"))
        // {
        // if (!TextUtils.isEmpty(cmdline) && cmdline.equals(pkg)) {
        // Log.i("PROC", "CMDLINE: " + cmdline + "==" + pkg);
        // return true;
        // }
        return false;
    }

    /**
     * 未完善规则
     *
     * @param statm
     * @return
     */
    private boolean isForeGroundByStatm(String statm) {
        return false;
    }

    /**
     * 未完善规则
     *
     * @param current
     * @return
     */
    private boolean isForeGroundByCurrent(String current) {
        return false;
    }

    /**
     * 未完善规则
     *
     * @param oomAdj
     * @return
     */
    private boolean isForeGroundByOOMAdj(String oomAdj) {
        return false;
    }

    /**
     * 未完善规则
     *
     * @param oomScoreAdj
     * @return
     */
    private boolean isForeGroundByOOMScoreAdj(String oomScoreAdj) {
        return false;
    }

    /**
     * 未完善规则
     *
     * @param wchan
     * @return
     */
    private boolean isForeGroundByWchan(String wchan) {
        return false;
    }

    /**
     * 未完善规则
     *
     * @param sched
     * @return
     */
    private boolean isForeGroundBySched(String sched) {
        return false;
    }

    /**
     * 未完善规则
     *
     * @param dev
     * @return
     */
    private boolean isForeGroundByDev(String dev) {
        return false;
    }

    /**
     * false
     *
     * @param snmp
     * @return
     */
    private boolean isForeGroundBySnmp(String snmp) {
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
        // L.i("stat [" + pkg + "]-----" + stat);
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

    // cmdline
    private String getCmdline(int pid, String pkg) {
        String cmdline = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/cmdline"});
        // if (isDebug) {
        // L.i("cmdline [" + pkg + "]-----" + cmdline);
        // }
        return cmdline;
    }

    // statm
    private String getStatm(int pid, String pkg) {
        String statm = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/statm"});
        // if (isDebug) {
        // L.i("statm [" + pkg + "]-----" + statm);
        // }
        return statm;
    }

    // oom_adj
    private String getOOMAdj(int pid, String pkg) {
        String oom_adj = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/oom_adj"});
        // if (isDebug) {
        // L.i("oom_adj [" + pkg + "]-----" + oom_adj);
        // }
        return oom_adj;
    }

    // oom_score_adj
    private String getOOMScoreAdj(int pid, String pkg) {
        String oom_score_adj = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/oom_score_adj"});
        // if (isDebug) {
        // L.i("oom_score_adj [" + pkg + "]-----" + oom_score_adj);
        // }
        return oom_score_adj;
    }

    // current
    private String getCurrent(int pid, String pkg) {
        String current = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/attr/current"});
        // if (isDebug) {
        // L.i("current [" + pkg + "]-----" + current);
        // }
        return current;
    }

    // wchan
    private String getWchan(int pid, String pkg) {
        String wchan = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/wchan"});
        // if (isDebug) {
        // L.i("wchan [" + pkg + "]-----" + wchan);
        // }
        return wchan;
    }

    // sched
    private String getSched(int pid, String pkg) {
        String sched = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/sched"});
        // if (isDebug) {
        // L.i("sched [" + pkg + "]-----" + sched);
        // }
        return sched;
    }

    // dev
    private String getDev(int pid, String pkg) {
        String dev = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/net/dev"});
        // if (isDebug) {
        // L.i("dev [" + pkg + "]-----" + dev);
        // }
        return dev;
    }

    // snmp
    private String getSnmp(int pid, String pkg) {
        String snmp = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/net/snmp"});
        // if (isDebug) {
        // L.i("snmp [" + pkg + "]-----" + snmp);
        // }
        return snmp;
    }

    /**
     * 解析置顶 PID+包名.
     *
     * @param top
     * @param ps
     * @return
     */
    private List<Process> getTopPkg(String top, String ps) {
        List<Process> infos = new ArrayList<Process>();

        String sourceData = null;
        if (TextUtils.isEmpty(top) && TextUtils.isEmpty(ps)) {
            forEachProc(infos);
        } else {
            if (!TextUtils.isEmpty(top)) {
                sourceData = top;
            } else if (!TextUtils.isEmpty(ps)) {
                sourceData = ps;
            }
            if (!TextUtils.isEmpty(sourceData)) {
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
                                    infos.add(new Process(String.valueOf(pid), ars[ars.length - 1]));
                                } catch (NumberFormatException e) {
                                }

                            }
                        } catch (Throwable e) {
                        }
                    }
                }
            }
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
    private void forEachProc(List<Process> infos) {
        try {
            File[] files = new File("/proc").listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    int pid;
                    try {
                        pid = Integer.parseInt(file.getName());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    String pkgName = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/cmdline"});
                    if (!TextUtils.isEmpty(pkgName) && pkgName.contains(".") && !pkgName.contains(":")
                            && !pkgName.contains("/")) {
                        infos.add(new Process(String.valueOf(pid), pkgName.trim()));
                    }
                }
            }
        }catch (Throwable t){

        }

    }

    private static class Holder {
        private static ProcUtils Instance = new ProcUtils();
    }

}

