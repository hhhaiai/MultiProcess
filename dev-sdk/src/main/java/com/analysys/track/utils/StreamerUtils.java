package com.analysys.track.utils;

import android.database.Cursor;
import android.os.Build;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.nio.channels.FileLock;
import java.util.zip.ZipFile;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 流关闭
 * @Version: 1.0
 * @Create: 2020/3/11 11:00
 * @author: sanbo
 */
public class StreamerUtils {


    public static void safeClose(FileLock closeable) {
        if (closeable != null) {
            try {
//                if (Build.VERSION.SDK_INT > 18) {
//                    closeable.close();
//                } else {
//                    closeable.release();
//                }
                //每个版本都有relase
                closeable.release();
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }


    public static void safeClose(Object closeable) {
        if (closeable != null) {
            try {
//                closeable.close();
                ClazzUtils.invokeObjectMethod(closeable, "close");
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }

    // 低版本类名不了 java.lang.AutoCloseable,而是com.android.tools.layoutlib.java.AutoCloseable
    //  使用 safeClose(Object)来关闭
    public static void safeClose(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                if (Build.VERSION.SDK_INT > 18) {
                    closeable.close();
                } else {
                    ClazzUtils.invokeObjectMethod(closeable, "close");
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }

    // 继承自AutoCloseable
    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }

    public static void safeClose(Cursor closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }

    public static void safeClose(ZipFile closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }

    public static void safeClose(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }

    }


//    public static void safeClose(Process proc) {
//        if (proc != null) {
//            try {
//                proc.exitValue();
//            } catch (Throwable t) {
//                if (BuildConfig.ENABLE_BUGLY) {
//                    BuglyUtils.commitError(t);
//                }
////                proc.destroy();
//            }
//            proc = null;
//
//        }
//
//    }


}
