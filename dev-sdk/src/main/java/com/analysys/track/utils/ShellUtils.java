package com.analysys.track.utils;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShellUtils {
    /**
     * 执行shell指令
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        java.lang.Process proc = null;
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
        } catch (Throwable e) {
        } finally {
            StreamerUtils.safeClose(br);
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(in);
            StreamerUtils.safeClose(proc);
        }

        return String.valueOf(sb);
    }

    public static String exec(String[] exec) {
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
        } finally {
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(isr);
            StreamerUtils.safeClose(bufferedReader);
            StreamerUtils.safeClose(processBuilder);
            StreamerUtils.safeClose(process);
        }
        return String.valueOf(sb);
    }
}
