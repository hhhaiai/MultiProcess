package com.analysys.track.utils;

import android.database.Cursor;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;

import java.io.Closeable;
import java.net.HttpURLConnection;
import java.nio.channels.FileLock;


/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 流关闭
 * @Version: 1.0
 * @Create: 2020/3/11 11:00
 * @author: sanbo
 */
public class StreamerUtils {
    /**
     * java 对象关闭器.
     *
     * @param os 可关闭的对象，如I/O类，HttpURLConnection 等
     */
    public static void safeClose(Object... os) {
        if (os != null && os.length > 0) {
            for (Object o : os) {
                if (o != null) {
                    try {
                        if (o instanceof HttpURLConnection) {
                            ((HttpURLConnection) o).disconnect();
                        } else if (o instanceof Closeable) {
                            ((Closeable) o).close();
                        } else if (o instanceof FileLock) {
                            ((FileLock) o).release();
                        } else if (o instanceof Closeable) {
                            ((Closeable) o).close();
                        } else if (o instanceof Cursor) {
                            ((Cursor) o).close();
                        }
                        ClazzUtils.invokeObjectMethod(o, "close");
                    } catch (Throwable e) {
                        if (BuildConfig.ENABLE_BUG_REPORT) {
                            BugReportForTest.commitError(e);
                        }
                    }
                }
            }
        }
    }
//    public static void safeClose(FileLock closeable) {
//        if (closeable != null) {
//            try {
////                if (Build.VERSION.SDK_INT > 18) {
////                    closeable.close();
////                } else {
////                    closeable.release();
////                }
//                //每个版本都有relase
//                closeable.release();
//            } catch (Throwable e) {
//                if (BuildConfig.ENABLE_BUG_REPORT) {
//                    BugReportForTest.commitError(e);
//                }
//            }
//        }
//    }
//
//
//    // 低版本类名不了 java.lang.AutoCloseable,而是com.android.tools.layoutlib.java.AutoCloseable
//    //  使用 safeClose(Object)来关闭
//    public static void safeClose(AutoCloseable closeable) {
//        if (closeable != null) {
//            try {
//                if (Build.VERSION.SDK_INT > 18) {
//                    closeable.close();
//                } else {
//                    ClazzUtils.invokeObjectMethod(closeable, "close");
//                }
//            } catch (Throwable e) {
//                if (BuildConfig.ENABLE_BUG_REPORT) {
//                    BugReportForTest.commitError(e);
//                }
//            }
//        }
//    }
//
//    // 继承自AutoCloseable
//    public static void safeClose(Closeable closeable) {
//        if (closeable != null) {
//            try {
//                closeable.close();
//            } catch (Throwable e) {
//                if (BuildConfig.ENABLE_BUG_REPORT) {
//                    BugReportForTest.commitError(e);
//                }
//            }
//        }
//    }
//
//    public static void safeClose(Cursor closeable) {
//        if (closeable != null) {
//            try {
//                closeable.close();
//            } catch (Throwable e) {
//                if (BuildConfig.ENABLE_BUG_REPORT) {
//                    BugReportForTest.commitError(e);
//                }
//            }
//        }
//    }
//
//
//    public static void safeClose(HttpURLConnection connection) {
//        try {
//            if (connection != null) {
//                connection.disconnect();
//                connection = null;
//            }
//        } catch (Throwable e) {
//        }
//    }
//
//    public static void safeClose(ZipFile closeable) {
//        if (closeable != null) {
//            try {
//                closeable.close();
//            } catch (Throwable e) {
//                if (BuildConfig.ENABLE_BUG_REPORT) {
//                    BugReportForTest.commitError(e);
//                }
//            }
//        }
//    }
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
//    }

}
