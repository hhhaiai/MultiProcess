package com.analysys;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.analysys.utils.EncUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件入口Api类，开发插件性需要实现本类中需要实现的方法。
 */
@PluginApi
public class Plugin1Main {
    public static final String TAG = "PluginHandler2";

    public static final String DATA_LOCATION = "DL";
    public static final String DATA = "DT";
    public static final String TOKEN = "TK";
    public static final String DATA_TYPE = "DTT";

    public static final String DATA_TYPE_UPD = "UPD";
    public static final String DATA_TYPE_ADD = "ADD";
    public static final String DATA_TYPE_DEL = "DEL";

    /**
     * 启动插件，不是长期任务的插件可以不实现。
     */
    @PluginApi
    public static boolean start(Context context) {
        Log.e(TAG, "start");
        return true;
    }

    /**
     * 停止插件，一般设备被拉黑或者特殊情况会调用
     */
    @PluginApi
    public static boolean stop(Context context) {
        Log.e(TAG, "stop");
        return true;
    }

    /**
     * 是否兼容指定的jarVersion版本，不兼容将会被主体删除。
     * 适用于：对某个版本单独开发的插件，当新升级的时候，正常可以不实现直接返回true
     *
     * @return true 兼容 false 不兼容
     */
    @PluginApi
    public static boolean compatible(Context context, String jarVersion) {
        Log.e(TAG, "compatible:" + jarVersion);
        return true;
    }

    /**
     * 获得数据，可以是多组数据，例如ADD、UPD、DEL，必须包含相关的字段。
     *
     * @return 数据包
     */
    @PluginApi
    public static List<Map<String, Object>> getData(Context context) {
        Log.e(TAG, "getData:");
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        //做什么【删除，添加，更新】
        map.put(DATA_TYPE, DATA_TYPE_ADD);
        //目标是谁【已有节点，新节点，新增字段，更新字段】
        map.put(DATA_LOCATION, "DevInfo");
        //数据是什么【对应操作的数据】
        Map<String, Object> data = new HashMap<>();
        data.put("LOOP",getString());
        Pair pair = EncUtils.enc(new JSONObject(data).toString(), 4);
        map.put(DATA, pair.second);
        //暗号【token】
        map.put(TOKEN, pair.first);
        list.add(map);
        return list;
    }

    private static Object getString() {
        return "123";
    }

    /**
     * 通知本插件清理数据，一般发生在JAR上传结束后调用
     */
    @PluginApi
    public static boolean clearData(Context context) {
        Log.e(TAG, "clearData");
        return true;
    }

}
