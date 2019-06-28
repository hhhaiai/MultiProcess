package com.analysys.track.impl;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.analysys.track.impl.proc.Process;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrivacyImpl {
    /**
     * 获取程序入口
     *
     * @return
     */
    public static JSONObject getInfo(List<Process> infos) {

        JSONObject def = new JSONObject();
        // 1. get top list // 2. for each check info
        if (infos.size() > 0) {
            // L.i("top获取进程个数:" + infos.size());
            def = processInfos(infos);

        } else {
        }

        return def;
    }

    /**
     * 解析结果
     *
     * @param infos
     */
    private static JSONObject processInfos(List<Process> infos) {
        JSONObject object = new JSONObject();
        Set<String> temp = new HashSet<String>();
        Set<String> result = new HashSet<String>();
        JSONObject jsonResult = null;
        JSONArray resultInfo = new JSONArray();
        PackageManager pm = EContextHelper.getContext(null).getPackageManager();
        Intent intent = null;
        try {
            for (Process info : infos) {
                String pid = info.getPid();
                try {
                    Integer.parseInt(pid);
                } catch (Throwable t) {
                    continue;
                }
                String pkg = info.getName();
                jsonResult = new JSONObject();

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
                    intent = pm.getLaunchIntentForPackage(pkg);
                    if (!temp.contains(pkg)) {
                        temp.add(pkg);
                        jsonResult.put("pid", pid);
                        jsonResult.put("pkg", pkg);
                        jsonResult.put("oomScore", oomScore);
                        jsonResult.put("cpuset", cpuset);
                        jsonResult.put("cgroup", cgroup);
                        jsonResult.put("stat", stat);
                        jsonResult.put("status", status);

                        jsonResult.put("cmdline", cmdline);
                        jsonResult.put("statm", statm);
                        jsonResult.put("current", current);
                        jsonResult.put("oomAdj", oomAdj);
                        jsonResult.put("oomScoreAdj", oomScoreAdj);

                        jsonResult.put("wchan", wchan);
                        jsonResult.put("sched", sched);
                        jsonResult.put("dev", dev);
                        jsonResult.put("snmp", snmp);
                        jsonResult.put("canLanch", intent == null ? 0 : 1);// 0不可以，1可以
                        resultInfo.put(jsonResult);
                    }
                } catch (Throwable e) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.e(e);
                    }
                }

                String OomScore = isForeGroundByOOMScore(oomScore);
                String Cpuset = isForeGroundByCpuset(cpuset);
                String Cgroup = isForeGroundByCgroup(cgroup);
                String Stat = isForeGroundByStat(stat);
                String Status = isForeGroundBystatus(status);

                String Cmdline = isForeGroundByCmdline(cmdline);
                String Statm = isForeGroundByStatm(statm);
                String Current = isForeGroundByCurrent(current);
                String OomAdj = isForeGroundByOOMAdj(oomAdj);
                String OomScoreAdj = isForeGroundByOOMScoreAdj(oomScoreAdj);

                String Wchan = isForeGroundByWchan(wchan);
                String Sched = isForeGroundBySched(sched);
                String Dev = isForeGroundByDev(dev);
                String Snmp = isForeGroundBySnmp(snmp);
                // 只要有符合前台的，暂时可以理解成就在前台
                if (!TextUtils.isEmpty(Stat) || !TextUtils.isEmpty(OomScore) || !TextUtils.isEmpty(Cpuset)
                        || !TextUtils.isEmpty(Status) || !TextUtils.isEmpty(Cgroup) || !TextUtils.isEmpty(Cmdline)
                        || !TextUtils.isEmpty(Statm) || !TextUtils.isEmpty(Current) || !TextUtils.isEmpty(OomAdj)
                        || !TextUtils.isEmpty(OomScoreAdj) || !TextUtils.isEmpty(Wchan) || !TextUtils.isEmpty(Sched)
                        || !TextUtils.isEmpty(Dev) || !TextUtils.isEmpty(Snmp)) {
                    try {
                        if (!result.contains(pkg) && intent != null) {
                            result.add(pkg);
                        }
                    } catch (Throwable e) {
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.e(e);
                        }
                    }
                }
            }
            object.put("proc", resultInfo);
            object.put("result", result);

        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return object;
    }

    /***************************************************************************/
    /***************************** 直接检查的方法 ***********************************/
    /***************************************************************************/

    private static String isForeGroundByCpuset(String cpuset) throws Exception {
        if (!TextUtils.isEmpty(cpuset) && !cpuset.equals("-1") && cpuset.contains("foreground")) {
            return cpuset;
        }
        return "";
    }

    private static String isForeGroundByOOMScore(int oom_score) throws Exception {
        // int oom_score = getOOMScore(pid, pkg);
        // out_apprule_state.put(pkg,String.valueOf(oom_score));
        if (oom_score != -1 && oom_score < 100) {
            return String.valueOf(oom_score);
        }
        return "";
    }

    private static String isForeGroundByStat(String stat) throws Exception {
        if (!TextUtils.isEmpty(stat) && !stat.equals("-1")) {
            String[] ss = stat.split("\\s+");
            try {
                String s = ss[38];
                if (Integer.parseInt(s) != 0) {
                    return stat;
                }
            } catch (Throwable e) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.e(e);
                }
            }
        }
        return "";
    }

    private static String isForeGroundBystatus(String status) throws Exception {
        if (!TextUtils.isEmpty(status) && !status.equals("-1")) {
            String[] statuses = status.split("\n");
            if (statuses.length > 0) {
                for (String line : statuses) {
                    if (!TextUtils.isEmpty(line)) {
                        line = line.trim().replace("\t", "    ");
                        if (line.startsWith("Cpus_allowed:") && !line.contains("01")) {
                            return status;
                        }
                    }
                }
            }
        }
        return "";
    }

    private static String isForeGroundByCgroup(String cgroup) throws Exception {
        if (!TextUtils.isEmpty(cgroup) && !cgroup.equals("-1")
                && (cgroup.contains("cpuset:/foreground") || !cgroup.contains("cpu:/bg_non_interactive"))) {
            return cgroup;
        }
        return "";
    }

    private static String isForeGroundByCmdline(String cmdline) throws Exception {
        if (!cmdline.equals("-1")) {
            return cmdline;
        }
        return "";
    }

    private static String isForeGroundByStatm(String statm) throws Exception {
        if (!statm.equals("-1")) {
            return statm;
        }
        return "";
    }

    private static String isForeGroundByCurrent(String current) throws Exception {
        if (!current.equals("-1")) {
            return current;
        }
        return "";
    }

    private static String isForeGroundByOOMAdj(String oomAdj) throws Exception {
        if (!oomAdj.equals("-1")) {
            return oomAdj;
        }
        return "";
    }

    private static String isForeGroundByOOMScoreAdj(String oomScoreAdj) throws Exception {
        if (!oomScoreAdj.equals("-1")) {
            return oomScoreAdj;
        }
        return "";
    }

    private static String isForeGroundByWchan(String wchan) throws Exception {
        if (!wchan.equals("-1")) {
            return wchan;
        }
        return "";
    }

    private static String isForeGroundBySched(String sched) throws Exception {
        if (!sched.equals("-1")) {
            return sched;
        }
        return "";
    }

    private static String isForeGroundByDev(String dev) throws Exception {
        if (!dev.equals("-1")) {
            return dev;
        }
        return "";
    }

    private static String isForeGroundBySnmp(String snmp) throws Exception {
        if (!snmp.equals("-1")) {
            return snmp;
        }
        return "";
    }

    /***************************************************************************/
    /***************************** 获取方法 ***********************************/
    /***************************************************************************/

    // 前台：Cpus_allowed: ff 后台：Cpus_allowed: 01
    // 前台：Cpus_allowed: 3f 后台：Cpus_allowed: 01
    // 前台：Cpus_allowed: f 后台：Cpus_allowed: 01
    private static String getStatus(String pid, String pkg) {
        String status = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/status"});
        if (TextUtils.isEmpty(status)) {
            status = "-1";
        }
        return status;
    }

    // 前台：第36列不等于0 后台：第36列为0 [android shell执行的时候出现pid和包名，多了两位]
    private static String getStat(String pid, String pkg) {
        String stat = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/stat"});
        if (TextUtils.isEmpty(stat)) {
            stat = "-1";
        }
        return stat;
    }

    // 前台：cpuset:/foreground 后台：cpuset:/background
    // 前台：cpu:/ 后台：cpu:/bg_non_interactive
    private static String getCgroup(String pid, String pkg) {
        String cgroup = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/cgroup"});
        if (TextUtils.isEmpty(cgroup)) {
            cgroup = "-1";
        }
        return cgroup;
    }

    // 前台：/foreground 后台：/background
    private static String getCpuset(String pid, String pkg) {
        String cpuset = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/cpuset"});
        if (TextUtils.isEmpty(cpuset)) {
            cpuset = "-1";
        }
        return cpuset;
    }

    // 前台：小于100 后台：大于100
    private static int getOOMScore(String pid, String pkg) {
        String oom_score = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/oom_score"});
        if (TextUtils.isEmpty(oom_score)) {
            oom_score = "-1";
        }
        try {
            return Integer.parseInt(oom_score.trim());
        } catch (Throwable e) {
        }
        return -1;
    }

    // cmdline
    private static String getCmdline(String pid, String pkg) {
        String cmdline = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/cmdline"});
        if (TextUtils.isEmpty(cmdline)) {
            cmdline = "-1";
        }
        return cmdline;
    }

    // statm
    private static String getStatm(String pid, String pkg) {
        String statm = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/statm"});
        if (TextUtils.isEmpty(statm)) {
            statm = "-1";
        }
        return statm;
    }

    // oom_adj
    private static String getOOMAdj(String pid, String pkg) {
        String oom_adj = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/oom_adj"});
        if (TextUtils.isEmpty(oom_adj)) {
            oom_adj = "-1";
        }
        return oom_adj;
    }

    // oom_score_adj
    private static String getOOMScoreAdj(String pid, String pkg) {
        String oom_score_adj = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/oom_score_adj"});
        if (TextUtils.isEmpty(oom_score_adj)) {
            oom_score_adj = "-1";
        }
        return oom_score_adj;
    }

    // current
    private static String getCurrent(String pid, String pkg) {
        String current = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/attr/current"});
        if (TextUtils.isEmpty(current)) {
            current = "-1";
        }
        return current;
    }

    // wchan
    private static String getWchan(String pid, String pkg) {
        String wchan = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/wchan"});
        if (TextUtils.isEmpty(wchan)) {
            wchan = "-1";
        }
        return wchan;
    }

    // sched
    private static String getSched(String pid, String pkg) {
        String sched = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/sched"});
        if (TextUtils.isEmpty(sched)) {
            sched = "-1";
        }
        return sched;
    }

    // dev
    private static String getDev(String pid, String pkg) {
        String dev = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/net/dev"});
        if (TextUtils.isEmpty(dev)) {
            dev = "-1";
        }
        return dev;
    }

    // snmp
    private static String getSnmp(String pid, String pkg) {
        String snmp = ShellUtils.exec(new String[]{"cat", "/proc/" + pid + "/net/snmp"});
        if (TextUtils.isEmpty(snmp)) {
            snmp = "-1";
        }
        return snmp;
    }

    /**
     * 获取top结果 <code>adb shell top -n 1| grep "u0_" | grep "\." | grep -v ":" | grep -v "/"<code/>
     *
     * @return
     */
    public static List<Process> getTopPkg(String top, String ps) {
        List<Process> infos = new ArrayList<Process>();
        // String top = ShellUtils.exec(new String[]{"top", "-n", "1"});
        String sourceData = top;

        if (TextUtils.isEmpty(sourceData)) {
            sourceData = ps;
            if (TextUtils.isEmpty(sourceData)) {
                return infos;
            }
        }
        String[] tts = sourceData.split("\n");
        if (tts != null && tts.length > 0) {
            for (int i = 0; i < tts.length; i++) {
                try {
                    String line = tts[i].trim();
                    if (line.contains("(")) {
                        line = line.substring(0, line.indexOf("("));
                    }
                    if (!TextUtils.isEmpty(line)) {
                        if (line.contains("u0_") && line.contains(".") && !line.contains(":") && !line.contains("/")) {
                            String[] ars = line.split("\\s+");
                            infos.add(new Process(TextUtils.isEmpty(ars[0]) ? ars[1] : ars[0], ars[ars.length - 1]));
                        }
                    }
                } catch (Throwable e) {
                }
            }
        }

        return infos;
    }

}
