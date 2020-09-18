package com.analysys;

import android.content.Context;
import android.util.Pair;

import com.analysys.utils.EncUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright 2020 analysys Inc. All rights reserved.
 * @Description: 插件主类提供相关Api接口
 * @Version: 1.0
 * @Create: 2020-09-15 17:52:52
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
@PluginApi
public class PluginApiTemplate {
    //-----每个数据包必含要素-------
    public static final String DATA_LOCATION = "DL";
    public static final String DATA = "DT";
    public static final String TOKEN = "TK";
    public static final String DATA_TYPE = "DTT";
    //-----每个数据包必含要素-------

    //-----data type-----
    public static final String DATA_TYPE_UPD = "UPD";
    public static final String DATA_TYPE_ADD = "ADD";
    public static final String DATA_TYPE_DEL = "DEL";
    //-----data type-----

    /**
     * 启动插件，不是长期任务的插件可以不实现。
     */
    @PluginApi
    public static boolean start(Context context) {
        return true;
    }

    /**
     * 停止插件，一般设备被拉黑或者特殊情况会调用
     */
    @PluginApi
    public static boolean stop(Context context) {
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
        return true;
    }

    /**
     * 获得数据，可以是多组数据，例如ADD、UPD、DEL，必须包含相关的字段。
     *
     * @return 数据包
     */
    @PluginApi
    public static List<Map<String, Object>> getData(Context context) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        //做什么【删除，添加，更新】
        map.put(DATA_TYPE, DATA_TYPE_ADD);
        //目标是谁【已有节点，新节点，新增字段，更新字段】
        map.put(DATA_LOCATION, "DevInfo");
        //数据是什么【对应操作的数据】
        Map<String, Object> data = new HashMap<>();
        data.put("d2", "h1");
        data.put("d1", "h2");
        Pair pair = EncUtils.enc(new JSONObject(data).toString(), 4);
        map.put(DATA, pair.second);
        //暗号【token】
        map.put(TOKEN, pair.first);
        list.add(map);
        return list;
    }

    /**
     * 通知本插件清理数据，一般发生在JAR上传结束后调用
     */
    @PluginApi
    public static boolean clearData(Context context) {
        return true;
    }
}
