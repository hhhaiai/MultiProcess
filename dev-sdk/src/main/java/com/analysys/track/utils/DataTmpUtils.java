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
    private File file;
    private boolean available;
    private Map<String, Object> value;

    private static volatile DataTmpUtils instance = null;

    private DataTmpUtils(File file) {
        this.file = file;

        available = file.exists() && file.isFile() && file.canRead() && file.length() > 0;

        if (available) {
            value = new HashMap<>();
            doFile2Map();
        }
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
        if (!available) {
            return defValue;
        }
        return (String) value.get(key);
    }


    public int getInt(String key, int defValue) {
        if (!available) {
            return defValue;
        }
        return (int) value.get(key);
    }


    public long getLong(String key, long defValue) {
        if (!available) {
            return defValue;
        }
        return (long) value.get(key);
    }


    public boolean getBoolean(String key, boolean defValue) {
        if (!available) {
            return defValue;
        }
        return (boolean) value.get(key);
    }


    public boolean contains(String key) {
        if (!available) {
            return false;
        }
        return false;
    }


    private void doFile2Map() {
        if (!available) {
            return;
        }
        FileInputStream outputStream = null;
        BufferedReader reader = null;
        try {
            outputStream = new FileInputStream(file);
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
            value.clear();
            for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                String k = it.next();
                value.put(k, object.get(k));
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

    private void doMap2File() {
        if (!available) {
            return;
        }
        FileOutputStream outputStream = null;
        BufferedWriter writer = null;
        try {
            JSONObject object = new JSONObject(value);
            outputStream = new FileOutputStream(file);
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

