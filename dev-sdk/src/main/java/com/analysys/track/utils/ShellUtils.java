package com.analysys.track.utils;

import android.text.TextUtils;

import com.analysys.track.BuildConfig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * @Copyright © 2020/2/12 analysys Inc. All rights reserved.
 * @Description: shell工具类
 * @Version: 1.0
 * @Create: 2020/2/12 15:23
 * @author: sanbo
 */
public class ShellUtils {
    /**
     * 执行shell指令
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {

        return backShellOldMethod(cmd);
//        return execCommand(new String[]{cmd});
    }


    public static String exec(String[] exec) {

        return backOldMethod(exec);

//        StringBuffer sb = new StringBuffer();
//        for (String s : exec) {
//            sb.append(s).append(" ");
//        }
//        return execCommand(new String[]{sb.toString()});
    }


    /**
     * 支持多个语句的shell
     *
     * @param commands 每一个元素都是语句shell.示例 new String[]{"type su"}.
     * @return
     */
    public static String execCommand(String[] commands) {
        if (commands == null || commands.length == 0) {
            return "";
        }

        Process process = null;
        BufferedReader successResult = null;
        InputStreamReader reader = null;
        InputStream is = null;
        DataOutputStream os = null;
//        BufferedReader errorResult = null;
        StringBuilder resultSb = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes("\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();

//            // top等循环打印指令，可能导致死等
//            process.waitFor();

            is = process.getInputStream();
            reader = new InputStreamReader(is);
            successResult = new BufferedReader(reader);
            String s;
            while ((s = successResult.readLine()) != null) {
                resultSb.append(s);
            }
//            // shell执行错误
//            if (resultSb.length() <= 0) {
//                // failed
//                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//                while ((s = errorResult.readLine()) != null) {
//                    resultSb.append(s);
//                }
//            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {

            StreamerUtils.safeClose(os);
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(reader);
            StreamerUtils.safeClose(successResult);
            if (process != null) {
                process.destroy();
            }
        }
//        L.w("执行[ " + Arrays.asList(commands) + " ], 结果: " + resultSb.toString());
        return resultSb.toString();
    }



    private static String backShellOldMethod(String cmd) {
        String result = "";
        if (TextUtils.isEmpty(cmd)) {
            return result;
        }
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        InputStreamReader is = null;
        StringBuilder sb = new StringBuilder();
        try {
            proc = Runtime.getRuntime().exec(cmd);
            in = new BufferedInputStream(proc.getInputStream());
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
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(br);
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(in);
            if (proc != null) {
                proc.destroy();
            }
        }
        return result;
    }
    private static String backOldMethod(String[] exec) {
        StringBuilder sb = new StringBuilder();
        Process process = null;
        ProcessBuilder processBuilder = new ProcessBuilder(exec);
        BufferedReader bufferedReader = null;
        InputStreamReader isr = null;
        InputStream is = null;
        try {
            process = processBuilder.start();
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(isr);
            StreamerUtils.safeClose(bufferedReader);
            StreamerUtils.safeClose(processBuilder);
            if (process != null) {
                process.destroy();
            }
        }

        return  String.valueOf(sb);
    }
}
