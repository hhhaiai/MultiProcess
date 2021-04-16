package cn.analysys.casedemo.cases.logics;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.testcaselib.defcase.ETestCase;
import me.hhhaiai.testcaselib.utils.L;

public class StoreCase extends ETestCase {

    public StoreCase() {
        super("[存储]内外置SDCard");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        new Thread(() -> {
            try {
                gotoWork();
            } catch (Throwable e) {
                L.e();
            }
        }).start();
        return true;
    }

    private void gotoWork() {
        StringBuffer sb = new StringBuffer();
//            File sdFile= Environment.getExternalStorageDirectory();
//            File[] files=sdFile.getParentFile().listFiles();
//            for(File file:files) {
//                if (file.getAbsolutePath().equals(sdFile.getAbsolutePath())) {//外置
//                    sb.append("外置:").append(sdFile.getAbsolutePath()).append("\n");
//                } else if (file.getAbsolutePath().contains("sdcard")) {//得到内置sdcard
//                    sb.append("内置:").append(file.getAbsolutePath()).append("\n");
//                }
//            }

        sb.append("==================SD卡信息获取================\n");
        sb.append("内部存储路径:").append(Environment.getDataDirectory().toString()).append("\n");
        sb.append("反射获取SDCard路径:").append(getStoragePath(SDKHelper.getContext(), false)).append("\n");
        sb.append("Shell获取SDCard路径:").append(getExtSDCardPaths()).append("\n");
        sb.append("ExternalStorageDirectory:").append(Environment.getExternalStorageDirectory().getPath()).append("\n");
        sb.append("DataDirectory:").append(Environment.getDataDirectory().getPath()).append("\n");
        sb.append("SD是否挂载:").append(isStorageMounted(SDKHelper.getContext())).append("\n");
        sb.append("SDCARD剩余存储空间:").append(getAvailableExternalMemorySize()).append("\n");
        sb.append("SDCARD总的存储空间:").append(getTotalExternalMemorySize()).append("\n");
        sb.append("手机内部剩余存储空间:").append(getAvailableInternalMemorySize()).append("\n");
        sb.append("手机内部总的存储空间:").append(getTotalInternalMemorySize()).append("\n");
//            sb.append("SD列表:").append(getSDCardList()).append("\n");
        Woo.logFormCase(sb.toString());
        getSDCardList();
    }

    private void getSDCardList() {
        printDir("/sdcard/Android/");
        printDir("/sdcard/Android/media/");
        printDir("/sdcard/Android/data/");
        printDir("/sdcard/Android/obj/");
        printDir(Environment.getExternalStorageDirectory());
    }

    private void printDir(String path) {
        printDir(new File(path));
    }

    private void printDir(File f) {
        StringBuffer sb = new StringBuffer();
        String path = f.getPath();
        sb.append("============[" + path + "]访问情况================\n")
                .append("[").append(path).append("] exists: ").append(f.exists()).append("\n")
                .append("[").append(path).append("] canRead: ").append(f.canRead()).append("\n")
                .append("[").append(path).append("] canExecute: ").append(f.canExecute()).append("\n")
                .append("[").append(path).append("] canWrite: ").append(f.canWrite()).append("\n")
                .append("[").append(path).append("] getFreeSpace: ").append(f.getFreeSpace()).append("\n")
                .append("[").append(path).append("] list: ").append(f.list()).append("\n")
                .append("[").append(path).append("] listFiles: ").append(f.listFiles()).append("\n")
        ;
        L.i(sb.toString());
    }


    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部总的存储空间
     *
     * @return
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * 获取内置SD卡路径
     *
     * @return
     */
    public String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取外置SD卡路径
     *
     * @return
     */
    public List<String> getExtSDCardPaths() {
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        if (extFileStatus.endsWith(Environment.MEDIA_UNMOUNTED)
                && extFile.exists() && extFile.isDirectory()
                && extFile.canWrite()) {
            paths.add(extFile.getAbsolutePath());
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                if ((!line.contains("fat") && !line.contains("fuse") && !line
                        .contains("storage"))
                        || line.contains("secure")
                        || line.contains("asec")
                        || line.contains("firmware")
                        || line.contains("shell")
                        || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
                        || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
                        || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile
                        .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                paths.add(mountPath);
            }
        } catch (Throwable e) {
            L.e(e);
        }
        return paths;
    }

    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param mContext    上下文
     * @param is_removale 是否可移除，false返回内部存储路径，true返回外置SD卡路径
     * @return
     */
    private static String getStoragePath(Context mContext, boolean is_removale) {
        String path = "";
        //使用getSystemService(String)检索一个StorageManager用于访问系统存储功能。
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            //StorageVolume是在Android7.0（api24）里面才添加的类
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);

            for (int i = 0; i < Array.getLength(result); i++) {
                Object storageVolumeElement = Array.get(result, i);
                path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            L.e(e);
        }
        return path;
    }

    /**
     * 判断外置sd卡是否挂载
     */
    public static boolean isStorageMounted(Context mContext) {
        boolean isMounted = false;
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Method getState = storageVolumeClazz.getMethod("getState");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                String state = (String) getState.invoke(storageVolumeElement);
                if (removable && state.equals(Environment.MEDIA_MOUNTED)) {
                    isMounted = removable;
                    break;
                }

            }
        } catch (Exception e) {
            L.e(e);
        }
        return isMounted;
    }


    /**
     * 获取SDCARD剩余存储空间
     *
     * @return
     */
    public static long getAvailableExternalMemorySize() {
        // File path = Environment.getExternalStorageDirectory();
        // StatFs stat = new StatFs(path.getPath());
        StatFs stat = new StatFs(getStoragePath(SDKHelper.getContext(), true));//path.getPath()
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    /**
     * 获取SDCARD总的存储空间
     *
     * @return
     */
    public static long getTotalExternalMemorySize() {
        // File path = Environment.getExternalStorageDirectory();
        // StatFs stat = new StatFs(path.getPath());
        StatFs stat = new StatFs(getStoragePath(SDKHelper.getContext(), true));//path.getPath()
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }


    /**
     * 单位换算
     *
     * @param size      单位为B
     * @param isInteger 是否返回取整的单位
     * @return 转换后的单位
     */
    public static String formatFileSize(long size, boolean isInteger) {
        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString = "0M";
        if (size < 1024 && size > 0) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else if (size < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
        } else {
            fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "G";
        }
        return fileSizeString;
    }

    private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");
}

