package com.analysys.track.utils;

import java.io.Closeable;
import java.net.HttpURLConnection;

public class Streamer {

    public static void safeClose(Closeable closeable) {
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
            proc.destroy();
            proc = null;
        }

    }

    public static void safeClose(ProcessBuilder pb) {
        if (pb != null) {
            pb.directory();
            pb = null;
        }

    }

}
