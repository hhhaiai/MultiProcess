package com.analysys;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Map;

/**
 * 每个info节点的时候调用插件来尝试增加内容
 */
public class PluginHandler {
    public static final String TAG = "PluginHandler";

    public static final String RUN_MODE = "RM";
    public static final String COMPATIBILITY_MODE = "CM";
    public static final String ALLOW_DEBUG = "AD";
    public static final String CACHE_DIRECTORY = "CD";

    public static final String DATA_LOCATION = "DL";
    public static final String DATA = "DT";
    public static final String TOKEN = "TK";
    public static final String DATA_TYPE = "DTT";

    public static final String DATA_TYPE_UPD = "UPD";
    public static final String DATA_TYPE_ADD = "ADD";
    public static final String DATA_TYPE_DEL = "DEL";

    private static volatile PluginHandler instance = null;

    private PluginHandler() {
    }

    public static PluginHandler getInstance() {
        if (instance == null) {
            synchronized (PluginHandler.class) {
                if (instance == null) {
                    instance = new PluginHandler();
                }
            }
        }
        return instance;
    }

    public static void init(Context context, String appId) {
        Log.e(TAG, "init:" + appId);
    }

    public boolean start() {
        Log.e(TAG, "start");
        return true;
    }

    public boolean stop() {
        Log.e(TAG, "stop");
        return true;
    }

    /**
     * 是否兼容指定的jarVersion版本，不兼容将会被删除。
     * 适用于：对某个版本单独开发的插件，当新升级的时候
     */
    public boolean compatible(String jarVersion) {
        Log.e(TAG, "compatible:" + jarVersion);
        return true;
    }

    public List<Map<String, Object>> getData() {
        return PluginTestCase.getInstance().getData();
    }


    public boolean clearData() {
        Log.e(TAG, "clearData");
        return true;
    }

}
