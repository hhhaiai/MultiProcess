package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.reflectinon.EContextHelper;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;


/**
 * @Copyright 2019 sanbo Inc. All rights reserved.
 * @Description: 多进程保护
 * @Version: 1.0
 * @Create: 2019-08-04 17:26:07
 * @author: sanbo
 */
public class MultiProcessChecker {

    /**
     * 创建锁文件
     *
     * @param cxt
     * @param fileName 锁文件名称
     * @param time     锁使用间隔，为了不影响首次使用,时间前移一秒
     * @returnc
     */
    public static boolean createLockFile(Context cxt, String fileName, long time) {
        try {
            cxt = EContextHelper.getContext(cxt);
            if (cxt == null) {
                return false;
            }
            File dev = new File(cxt.getFilesDir(), fileName);
            if (!dev.exists()) {
                dev.createNewFile();
                dev.setExecutable(true);
                dev.setWritable(true);
                dev.setReadable(true);
                dev.setLastModified(System.currentTimeMillis() - (time + 1000));
            }
            if (dev.exists()) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    /**
     * 获取锁文件的最后修改时间
     *
     * @param cxt
     * @param fileName
     * @return
     */
    public static long getLockFileLastModifyTime(Context cxt, String fileName) {
        try {
            cxt = EContextHelper.getContext(cxt);
            if (cxt != null) {
                File dev = new File(cxt.getFilesDir(), fileName);
                if (!dev.exists()) {
                    createLockFile(cxt, fileName, 0);
                }
                return dev.lastModified();
            }

        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return -1;
    }

    /**
     * 设置锁文件的修改时间
     *
     * @param cxt
     * @param fileName
     * @param time
     * @return
     */
    public static boolean setLockLastModifyTime(Context cxt, String fileName, long time) {
        try {
            cxt = EContextHelper.getContext(cxt);
            if (cxt != null) {
                File dev = new File(cxt.getFilesDir(), fileName);
                if (!dev.exists()) {
                    dev.createNewFile();
                    dev.setExecutable(true);
                    dev.setWritable(true);
                    dev.setReadable(true);
                }
                dev.setLastModified(time);
                if (dev.lastModified() == time) {
                    if (mFilenameAndLocks.containsKey(fileName)) {
                        Locks locks = mFilenameAndLocks.get(fileName);
                        if (locks != null) {
                            locks.safeClose();
                        }
                    }
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private static Map<String, Locks> mFilenameAndLocks = new HashMap<String, Locks>();


    /**
     * 根据锁文件时间，判断是否达到触发时间
     *
     * @param cxt  上下文
     * @param lock 文件名
     * @param time 轮询间隔
     * @param now  本次时间
     * @return
     */
    public static boolean isNeedWorkByLockFile(Context cxt, String lock, long time, long now) {
        try {
            cxt = EContextHelper.getContext(cxt);
            if (cxt == null) {
                return false;
            }

            long lastModifyTime = getLockFileLastModifyTime(cxt, lock);
            if (Math.abs(lastModifyTime - now) > time) {
                // 文件同步
                File f = new File(cxt.getFilesDir(), lock);
                RandomAccessFile randomFile = null;
                FileChannel fileChannel = null;
                FileLock fl = null;
                try {
                    // 持有锁
                    if (mFilenameAndLocks.containsKey(lock)) {
                        return true;
                    } else {
                        randomFile = new RandomAccessFile(f, "rw");
                        fileChannel = randomFile.getChannel();
                        fl = fileChannel.tryLock();
                        if (fl != null) {
                            Locks locks = new Locks();
                            locks.setFileChannel(fileChannel);
                            locks.setLock(fl);
                            locks.setRandomFile(randomFile);
                            mFilenameAndLocks.put(lock, locks);
                            return true;
                        } else {
                            return false;
                        }
                    }


                } catch (Throwable e) {
                }
            } else {
                return false;
            }

        } catch (Throwable t) {
        }
        return false;
    }


    /**
     * @Copyright © 2019 sanbo Inc. All rights reserved.
     * @Description: 同步文件锁
     * @Version: 1.0
     * @Create: 2019-08-05 18:43:31
     * @author: sanbo
     * @mail: xueyongfu@analysys.com.cn
     */
    static class Locks {
        private FileLock mLock = null;
        private RandomAccessFile mRandomFile = null;
        private FileChannel mFileChannel = null;

        public Locks() {
        }

        public void setLock(FileLock lock) {
            this.mLock = lock;
        }

        public void setRandomFile(RandomAccessFile randomFile) {
            this.mRandomFile = randomFile;
        }

        public void setFileChannel(FileChannel fileChannel) {
            this.mFileChannel = fileChannel;
        }


        public void safeClose() {
            StreamerUtils.safeClose(mLock);
            StreamerUtils.safeClose(mRandomFile);
            StreamerUtils.safeClose(mFileChannel);
        }
    }
}
