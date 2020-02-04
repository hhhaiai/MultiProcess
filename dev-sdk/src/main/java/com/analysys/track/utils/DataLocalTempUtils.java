package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class DataLocalTempUtils {

    public String getString(String key, String defValue) {
        try {
            if (initJson()) {
                if (mJson.has(key)) {
                    return mJson.getString(key);
                }
            }
        } catch (JSONException e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return defValue;
    }

    public void putString(String key, String value) {
        putObject(key, value, "putString");
    }


    public boolean getBoolean(String key, boolean defValue) {
        try {
            if (initJson()) {
                if (mJson.has(key)) {
                    return mJson.getBoolean(key);
                }
            }
        } catch (JSONException e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return defValue;
    }

    public void putBoolean(String key, boolean value) {
        putObject(key, value, "putBoolean");
    }

    public long getLong(String key, long defValue) {
        try {
            if (initJson()) {
                if (mJson.has(key)) {
                    return mJson.getLong(key);
                }
            }
        } catch (JSONException e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return defValue;
    }

    public void putLong(String key, long value) {
        putObject(key, value, "putLong");
    }

    public int getInt(String key, int defValue) {
        try {
            if (initJson()) {
                if (mJson.has(key)) {
                    return mJson.getInt(key);
                }
            }
        } catch (JSONException e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return defValue;
    }

    public void putInt(String key, int value) {
        putObject(key, value, "putLong");
    }

    public boolean contains(String key) {
        if (initJson()) {
            if (mJson.has(key)) {
                return true;
            }
        }
        return false;
    }

    public void remove(String key) {
        if (initJson()) {
            if (mJson.has(key)) {
                mJson.remove(key);
                flash();
            }
        }
    }

    /**
     * 存放文件到JSON
     *
     * @param key
     * @param value
     * @param methodName
     */
    public void putObject(String key, Object value, String methodName) {
        try {
            if (masureFileExists()) {
                if (mJson == null) {
                    mJson = new JSONObject();
                }
                mJson.put(key, value);
                flash();
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.e(methodName + "(" + key + ", " + value + ") Failed. file not exits. please check!");
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }


    /**
     * 更新到文件中
     */
    private void flash() {
        if (mJson != null) {
            FileUitls.getInstance(mContext).saveToFile(mJson.toString(), mFile);
        }
    }


    /**
     * 加载文件,存在一个弊端，若动态push文件到/data/local/tmp/可能存在不能立即读取
     *
     * @return
     * @throws JSONException
     */
    private boolean initJson() {
        try {
            //文件存在再处理
            if (mFile.exists()) {
                // 内存未加載才生效
                if (mJson == null) {
                    String info = FileUitls.getInstance(mContext).readStringFromFile(mFile);
                    if (!TextUtils.isEmpty(info)) {
                        mJson = new JSONObject(info);
                        if (mJson.length() > 0) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return false;
    }

    /**
     * 确保文件存在
     *
     * @return
     */
    private boolean masureFileExists() {
        try {
            //文件存在再处理
            if (!mFile.exists()) {
                mFile.createNewFile();
                mFile.setReadable(true);
                mFile.setWritable(true);
                mFile.setExecutable(true);
            }

            return mFile.exists();
        } catch (Throwable e) {
//            ShellUtils.exec(new String[]{"echo\"\">","/data/local/tmp/kvs"})
//            ShellUtils.exec(new String[]{"touch","/data/local/tmp/kvs"})
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return false;
    }

    /********************* get instance begin **************************/
    public static DataLocalTempUtils getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private DataLocalTempUtils initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final DataLocalTempUtils INSTANCE = new DataLocalTempUtils();
    }

    private DataLocalTempUtils() {
    }

    private Context mContext = null;
    private File mFile = new File("/data/local/tmp/kvs");
    private JSONObject mJson = null;
    /********************* get instance end **************************/
}
