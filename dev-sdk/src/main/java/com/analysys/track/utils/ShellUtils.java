package com.analysys.track.utils;

import android.text.TextUtils;

import com.analysys.track.BuildConfig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * @Copyright © 2020/2/12 analysys Inc. All rights reserved.
 * @Description: shell工具类
 * @Version: 1.0
 * @Create: 2020/2/12 15:23
 * @author: sanbo
 */
public class ShellUtils {

    /**
     * 执行拼接shell
     *
     * @param exec new String[]{"ls", "-l", "xx"}
     * @return
     */
    public static String exec(String[] exec) {
        StringBuffer sb = new StringBuffer();
        for (String s : exec) {
            sb.append(s).append(" ");
        }
        return shell(sb.toString());
    }

    /**
     * 执行shell指令
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return "";
        }
        return getResultString(cmd);
    }

    private static String getResultString(String cmd) {
        String result = "";
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder sb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        try {
            proc = Runtime.getRuntime().exec("sh");
            pos = proc.getOutputStream();
            os = new DataOutputStream(pos);

            // donnot use os.writeBytes(commmand), avoid chinese charset error
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.flush();
            //exitValue
            os.writeBytes("exit\n");
            os.flush();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            if (sb.length() > 0) {
                return sb.substring(0, sb.length() - 1);
            }
            result = String.valueOf(sb);
            if (!TextUtils.isEmpty(result)) {
                result = result.trim();
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(pos);
            StreamerUtils.safeClose(ii);
            StreamerUtils.safeClose(br);
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(in);
            StreamerUtils.safeClose(os);
        }

        return result;
    }


    public static List<String> getResultArrays(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        InputStream ii = null;
        StringBuilder sb = new StringBuilder();
        DataOutputStream os = null;
        OutputStream pos = null;
        try {
            proc = Runtime.getRuntime().exec("sh");
            pos = proc.getOutputStream();
            os = new DataOutputStream(pos);
            // donnot use os.writeBytes(commmand), avoid chinese charset error
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            ii = proc.getInputStream();
            in = new BufferedInputStream(ii);
            is = new InputStreamReader(in);
            br = new BufferedReader(is);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!TextUtils.isEmpty(line) && !result.contains(line.trim())) {
                    result.add(line.trim());
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(pos);
            StreamerUtils.safeClose(ii);
            StreamerUtils.safeClose(br);
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(in);
            StreamerUtils.safeClose(os);
        }
        return result;
    }

//    /**
//     * 支持多个语句的shell
//     *
//     * @param commands 每一个元素都是语句shell.示例 new String[]{"type su"}.
//     * @return
//     */
//    public static String execCommand(String[] commands) {
//        if (commands == null || commands.length == 0) {
//            return "";
//        }
//
//        Process process = null;
//        BufferedReader successResult = null;
//        InputStreamReader reader = null;
//        InputStream is = null;
//        DataOutputStream os = null;
////        BufferedReader errorResult = null;
//        StringBuilder resultSb = new StringBuilder();
//        try {
//            process = Runtime.getRuntime().exec("sh");
//            os = new DataOutputStream(process.getOutputStream());
//            for (String command : commands) {
//                if (command == null) {
//                    continue;
//                }
//
//                // donnot use os.writeBytes(commmand), avoid chinese charset
//                // error
//                os.write(command.getBytes());
//                os.writeBytes("\n");
//                os.flush();
//            }
//            os.writeBytes("exit\n");
//            os.flush();
//
////            // top等循环打印指令，可能导致死等
////            process.waitFor();
//
//            is = process.getInputStream();
//            reader = new InputStreamReader(is);
//            successResult = new BufferedReader(reader);
//            String s;
//            while ((s = successResult.readLine()) != null) {
//                resultSb.append(s).append("\n");
//            }
////            // shell执行错误
////            if (resultSb.length() <= 0) {
////                // failed
////                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
////                while ((s = errorResult.readLine()) != null) {
////                    resultSb.append(s);
////                }
////            }
//            if (resultSb.length() > 0) {
//                String sss = resultSb.toString();
//                return sss.substring(0, s.length() - 1);
//            }
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(e);
//            }
//        } finally {
//
//            StreamerUtils.safeClose(os);
//            StreamerUtils.safeClose(is);
//            StreamerUtils.safeClose(reader);
//            StreamerUtils.safeClose(successResult);
////            if (process != null) {
////                process.destroy();
////            }
//        }
//        if (resultSb.length() > 0) {
////        L.w("执行[ " + Arrays.asList(commands) + " ], 结果: " + resultSb.toString());
//            return resultSb.toString();
//        } else {
//            return "";
//        }
//    }
//
//
//    private static String backOldMethod(String[] exec) {
//        StringBuilder sb = new StringBuilder();
//        Process process = null;
//        ProcessBuilder processBuilder = new ProcessBuilder(exec);
//        BufferedReader bufferedReader = null;
//        InputStreamReader isr = null;
//        InputStream is = null;
//        try {
//            process = processBuilder.start();
//            is = process.getInputStream();
//            isr = new InputStreamReader(is);
//            bufferedReader = new BufferedReader(isr);
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                sb.append(line).append("\n");
//            }
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(e);
//            }
//        } finally {
//            StreamerUtils.safeClose(is);
//            StreamerUtils.safeClose(isr);
//            StreamerUtils.safeClose(bufferedReader);
////            StreamerUtils.safeClose(processBuilder);
////            if (process != null) {
////                process.destroy();
////            }
//        }
//
//        return String.valueOf(sb);
//    }
}
