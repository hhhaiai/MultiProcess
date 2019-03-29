package com.analysys.track.impl.proc;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.impl.PrivacyImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.ShellUtils;

import android.content.Context;

public class ProcParser {

    public static final String RUNNING_TOP = "top";
    public static final String RUNNING_PS = "ps";
    public static final String RUNNING_TIME = "time";
    public static final String RUNNING_PROC = "proc";
    public static final String RUNNING_RESULT = "result";

    private static final String PID = "PID";

    public static boolean isDebug = false;

    /**
     * 获取详情
     * 
     * @param context
     * @return
     */
    public static JSONObject getRunningInfo(Context context) {
        JSONObject uploadInfo = null;
        try {
            if (context == null) {
                return null;
            }
            uploadInfo = new JSONObject();
            uploadInfo.put(RUNNING_TIME, System.currentTimeMillis());
            String top = ShellUtils.shell("top -n 1");
            uploadInfo.put(RUNNING_TOP, top);
            String ps = ShellUtils.shell("ps -P -p -x -c");
            uploadInfo.put(RUNNING_PS, ps);
            List<Process> tempValue = PrivacyImpl.getTopPkg(top, ps);
            JSONObject jsonObject = PrivacyImpl.getInfo(tempValue);
            JSONArray proc = new JSONArray(jsonObject.get("proc").toString());
//            Log.i("PROC", "  proc后的结果==>" + proc);
            uploadInfo.put(RUNNING_PROC, proc);
            String result = jsonObject.get("result").toString();
//            Log.i("RESULT", "  result的结果==>" + result);
            uploadInfo.put(RUNNING_RESULT, result);
            ELOG.i("XXXInfo ::::::: "+uploadInfo);
        } catch (Throwable t) {
            ELOG.i(t.getMessage() + "   =============exception");
        }
        return uploadInfo;
    }

    /**
     * 通过PS指令获取对应对应信息
     *
     * @param infos
     * @param context
     */
//    private static List<Process> getPS(List<Process> infos, Context context, String cmd) {
//        try {
//            if (context == null) {
//                return null;
//            }
//            String ps = ShellUtils.shell(cmd);
//            if (!TextUtils.isEmpty(ps)) {
//                String[] pss = ps.split("\n");
//                for (String line : pss) {
//                    // ELOG.i("---->" + line);
//                    if (!TextUtils.isEmpty(line)) {
//                        forEarchTopLine(infos, line);
//                    }
//
//                }
//            }
//        } catch (Throwable e) {
//            ELOG.e(e.getMessage());
//        }
//        return infos;
//    }

    // /**
    // * 获取top结果
    // * <code>adb shell top -n 1| grep "\." | grep -v ":" | grep -v "/"<code/>
    // * 过滤规则:
    // * <p>
    // * 1). 不能包含<code>/</code>
    // * 2). 不能包含<code>:</code>
    // * 3). 包含<code>.</code>
    // * 4). 系统API检测不能启动的
    // * </p>
    // * 三方用户的标志`u0_`过滤去除。
    // *
    // * @param infos
    // * @param context
    // * @return
    // */
    // private static List<Process> getTopPkg(List<Process> infos, Context context) {
    // try {
    // String top = JsonUtils.shell("top -n 1");
    // if (TextUtils.isEmpty(top)) {
    // return null;
    // }
    // if (isDebug) {
    // ELOG.i("top 结果:" + top);
    // }
    // String[] tts = top.split("\n");
    // if (isDebug) {
    // ELOG.i("top 行数: " + tts.length);
    // }
    // if (tts != null && tts.length > 0) {
    // for (int i = 0; i < tts.length; i++) {
    // if (isDebug) {
    // ELOG.i(String.format("top [%d] (%s)", (i + 1), tts[i]));
    // }
    //
    // forEarchTopLine(infos, tts[i]);
    // }
    // }
    // } catch (Throwable e) {
    // }
    // return infos;
    // }

    /**
     * 遍历解析top的单行
     *
     * @param infos
     * @param line
     */
    //TODO ？需要考虑过滤条件么

//    private static void forEarchTopLine(List<Process> infos, String line) {
//        try {
//            if (!TextUtils.isEmpty(line)) {
//                if (line.contains("("))
//                    line = line.substring(0, line.indexOf("("));
//                if (!line.startsWith("User") && !line.startsWith("USER") && !line.startsWith("PID")) {
//                    String[] ars = line.split("\\s+");
//                    if (ars.length > 0) {
//                        String pid = ars[0];
//                        if (TextUtils.isEmpty(pid))
//                            pid = ars[1];
//                        try {
//                            Integer.parseInt(pid);
//                        } catch (Throwable t) {
//                            pid = ars[1];
//                        }
//                        String pkg = ars[ars.length - 1];
//                        Process info = new Process(pid, pkg);
//                        if (!infos.contains(info)) {
//                            infos.add(info);
//                            if (isDebug) {
//                                ELOG.i("top line:" + line);
//                                ELOG.i("top info:" + info.toString());
//                            }
//                        }
//                    }
//                }
//
//            }
//        } catch (Throwable e) {
//        }
//    }

}
