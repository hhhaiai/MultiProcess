package com.analysys.track.utils;

import com.analysys.track.BuildConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Copyright 2020 analysys Inc. All rights reserved.
 * @Description: 用于专门读写 /data/local/tmp 中配置文件的工具类
 * @Version: 1.0
 * @Create: 2020-02-03 13:56:28
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class DataTmpUtils {
    private File mFile;
    private Map<String, Object> mValue;

    private static volatile DataTmpUtils instance = null;

    private DataTmpUtils(File file) {
        this.mFile = file;

        mValue = new HashMap<>();
        doFile2Map();
    }

    public static DataTmpUtils getInstance(String filePath) {
        if (instance == null) {
            synchronized (DataTmpUtils.class) {
                if (instance == null) {
                    instance = new DataTmpUtils(new File(filePath));
                }
            }
        }
        return instance;
    }


    public String getString(String key, String defValue) {
        if (!mValue.containsKey(key)) {
            return defValue;
        }
        return (String) mValue.get(key);
    }


    public int getInt(String key, int defValue) {
        if (!mValue.containsKey(key)) {
            return defValue;
        }
        return (int) mValue.get(key);
    }


    public long getLong(String key, long defValue) {
        if (!mValue.containsKey(key)) {
            return defValue;
        }
        return (long) mValue.get(key);
    }


    public boolean getBoolean(String key, boolean defValue) {
        if (!mValue.containsKey(key)) {
            return defValue;
        }
        return (boolean) mValue.get(key);
    }

    public void putBoolean(String key, boolean value) {
        this.mValue.put(key, value);
        doMap2File();
    }

    public void putLong(String key, long value) {
        this.mValue.put(key, value);
        doMap2File();
    }

    public void putInt(String key, int value) {
        this.mValue.put(key, value);
        doMap2File();
    }

    public void putString(String key, String value) {
        this.mValue.put(key, value);
        doMap2File();
    }

    public void remove(String key) {
        this.mValue.remove(key);
        doMap2File();
    }


    public boolean contains(String key) {
        if (!mValue.containsKey(key)) {
            return false;
        }
        return true;
    }


    private void doFile2Map() {
        if (mFile.exists() && mFile.isFile() && mFile.length() > 0) {
            FileInputStream outputStream = null;
            BufferedReader reader = null;
            try {
                outputStream = new FileInputStream(mFile);
                reader = new BufferedReader(new InputStreamReader(outputStream));
                StringBuilder builder = new StringBuilder();
                while (true) {
                    String str = reader.readLine();
                    if (str == null) {
                        break;
                    }
                    builder.append(str).append("\n");
                }

                JSONObject object = new JSONObject(builder.toString());
                mValue.clear();
                for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                    String k = it.next();
                    mValue.put(k, object.get(k));
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
                }
            } finally {
                StreamerUtils.safeClose(reader);
                StreamerUtils.safeClose(outputStream);
            }
        }
    }

    private void doMap2File() {
        try {
            if (!mFile.exists()) {
                File patentDir = mFile.getParentFile();
                if (patentDir != null && !patentDir.exists()) {
                    patentDir.mkdirs();
                }
                mFile.createNewFile();
            }
        } catch (Throwable e) {
        }

        FileOutputStream outputStream = null;
        BufferedWriter writer = null;
        try {

            JSONObject object = new JSONObject(mValue);
            outputStream = new FileOutputStream(mFile, false);
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(object.toString());
            writer.flush();
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(outputStream);
            StreamerUtils.safeClose(writer);
        }
    }
}

