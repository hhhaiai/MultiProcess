package com.analysys.track.utils;

import android.text.TextUtils;

import com.analysys.track.BuildConfig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ShellUtils {
    /**
     * 执行shell指令
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {
        return execCommand(new String[]{cmd});
    }

    public static String exec(String[] exec) {
        StringBuffer sb = new StringBuffer();
        for (String s : exec) {
            sb.append(s).append(" ");
        }
        return execCommand(new String[]{sb.toString()});

    }


    /**
     * 支持多个语句的shell
     *
     * @param commands
     * @return
     */
    private static String execCommand(String[] commands) {
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

            process.waitFor();

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
        return resultSb.toString();
    }
}
