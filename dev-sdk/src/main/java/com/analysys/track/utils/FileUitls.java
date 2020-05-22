package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * @Copyright @ 2019/12/12 sanbo Inc. All rights reserved.
 * @Description: file utils
 * @Version: 1.0
 * @Create: 2019/12/12 22:49
 * @author: sanbo
 */
public class FileUitls {


    /**
     * File's create time
     *
     * @param file
     * @return
     */
    public long getCreateTime(File file) {
        return getCreateTime(file.getAbsolutePath());
    }


    /**
     * 保存内容到制定路径文件中
     *
     * @param info     保存的内容
     * @param filePath 完整的文件路径
     */
    public void saveToFile(String info, String filePath) {
        saveToFile(info, new File(filePath), false);
    }

    /**
     * 保存内容到制定路径文件中
     *
     * @param info
     * @param file
     * @param isAppend
     */
    public void saveToFile(String info, File file, boolean isAppend) {
        FileOutputStream outputStream = null;
        BufferedWriter writer = null;
        try {
            if (TextUtils.isEmpty(info)) {
                return;
            }

            if (!file.exists()) {
                file.createNewFile();
                file.setReadable(true);
                file.setWritable(true);
                file.setExecutable(true);
            }
            if (!file.exists()) {
                return;
            }
            outputStream = new FileOutputStream(file, isAppend);
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(info);
            writer.flush();
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(outputStream);
            StreamerUtils.safeClose(writer);
        }
    }

    /**
     * 读取文件中字符串
     *
     * @param filePath 完整的文件路径
     * @return
     */
    public String readStringFromFile(String filePath) {
        return readStringFromFile(new File(filePath));
    }

    /**
     * 读取文件中的字符串
     *
     * @param file
     * @return
     */
    public String readStringFromFile(File file) {

        FileInputStream outputStream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            if (!file.exists()) {
                return "";
            }
            outputStream = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(outputStream));
            while (true) {
                String str = reader.readLine();
                if (str == null) {
                    break;
                }
                builder.append(str).append("\n");
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(reader);
            StreamerUtils.safeClose(outputStream);
        }
        return builder.toString();
    }


    /**
     * File's create time
     *
     * @param path
     * @return
     */
    public long getCreateTime(String path) {

        String shellResult = ShellUtils.exec(new String[]{"ls", "-lau", path});
        if (!TextUtils.isEmpty(shellResult)) {
            String[] arr = shellResult.split("\\s+");
            if (arr.length >= 3) {
                String date = arr[arr.length - 3];
                String time = arr[arr.length - 2];
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                try {
                    Date d = sdf.parse(date + " " + time);
                    return d.getTime();
                } catch (Throwable e) {
                }
            }

        }
        return 0;
    }

    public void rename(File oldFile, File newFile, boolean isDir) {
        // 1. remove new file
        if (isDir) {
            // makesure dir
            if (newFile.exists() && !newFile.isDirectory()) {
                newFile.deleteOnExit();
            }
        } else {
            if (newFile.exists()) {
                newFile.deleteOnExit();
            }
        }
        // 2. rename
        if (oldFile.exists()) {
            oldFile.renameTo(oldFile);
        }
    }

    /********************* get instance begin **************************/
    public static FileUitls getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private FileUitls initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = EContextHelper.getContext(context);
        }
        return HLODER.INSTANCE;
    }

    public String getString(String data, byte[] bs) throws UnsupportedEncodingException, JSONException {
        String json = new String(xor(Arrays.copyOf(bs, bs.length), EGContext.STRING_FOG_KEY), "utf-8");
        return SystemUtils.getString(data, json);
    }

    private byte[] xor(byte[] data, String key) {
        int len = data.length;
        int lenKey = key.length();
        int i = 0;
        int j = 0;
        while (i < len) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] ^ key.charAt(j));
            i++;
            j++;
        }
        return data;
    }
    
    public void deleteFileAtFilesDir(String path) {
        try {
            if (!TextUtils.isEmpty(path)) {
                deleteFile(new File(mContext.getFilesDir(), path));
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    public void deleteFile(File file) {
        try {
            if (file == null || !file.exists()) {
                return;
            }
            if (file.isFile()) {
                file.delete();
            } else {
                for (File tm : file.listFiles()) {
                    deleteFile(tm);
                }
            }
            file.delete();
        } catch (Throwable e) {
        }
    }

    private static class HLODER {
        private static final FileUitls INSTANCE = new FileUitls();
    }

    private FileUitls() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/
}
