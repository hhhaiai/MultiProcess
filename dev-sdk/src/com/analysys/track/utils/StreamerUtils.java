package com.analysys.track.utils;

import java.net.HttpURLConnection;

public class StreamerUtils {

    public static void safeClose(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
            }
        }
    }

    public static void safeClose(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }

    }

    public static void safeClose(Process proc) {
        if (proc != null) {
            try {
                proc.destroy();
                proc = null;
            }catch (Throwable t){
            }

        }

    }

    public static void safeClose(ProcessBuilder pb) {
        if (pb != null) {
            pb.directory();
            pb = null;
        }

    }

}
