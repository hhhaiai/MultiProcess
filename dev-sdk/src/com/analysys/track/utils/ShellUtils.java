package com.analysys.track.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.text.TextUtils;

public class ShellUtils {

    /**
     * 执行shell指令
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {

        // TODO : 如何防止卡死,需要增加超时机制
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        java.lang.Process proc = null;
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
        } finally {
            Streamer.safeClose(br);
            Streamer.safeClose(in);
            Streamer.safeClose(proc);
        }

        return sb.toString();
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
            Streamer.safeClose(is);
            Streamer.safeClose(isr);
            Streamer.safeClose(bufferedReader);
            Streamer.safeClose(processBuilder);
            Streamer.safeClose(process);
        }
        return sb.toString();
    }
}
