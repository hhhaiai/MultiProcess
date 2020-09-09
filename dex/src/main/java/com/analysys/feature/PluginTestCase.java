package com.analysys.feature;

import android.util.Log;
import android.util.Pair;

import com.analysys.utils.EncUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.analysys.PluginHandler.DATA;
import static com.analysys.PluginHandler.DATA_LOCATION;
import static com.analysys.PluginHandler.DATA_TYPE;
import static com.analysys.PluginHandler.DATA_TYPE_ADD;
import static com.analysys.PluginHandler.DATA_TYPE_DEL;
import static com.analysys.PluginHandler.DATA_TYPE_UPD;
import static com.analysys.PluginHandler.TOKEN;

public class PluginTestCase {

    public static final String TAG = "PluginHandler";

    private static volatile PluginTestCase instance = null;

    private PluginTestCase() {
    }

    public static PluginTestCase getInstance() {
        if (instance == null) {
            synchronized (PluginTestCase.class) {
                if (instance == null) {
                    instance = new PluginTestCase();
                }
            }
        }
        return instance;
    }

    public List<Map<String, Object>> getData() {
        Log.e(TAG, "getData");
        List<Map<String, Object>> datas = new ArrayList<>();
        try {
            //ADD
            //已有节点添加字段              - 新增字段
            caseAddImei(datas);
            //已有节点字段是已经存在的字段    - 无法添加
            caseAddAN(datas);
            //添加新节点                    - 可以添加
            caseAddTestInfo(datas);
            //添加新节点是JAR已有节点         - 合并字段
            caseAddDevInfo(datas);

            //DEL
            //case 删除某个节点的一个字段      - 可以删除
            caseDelDevInfoAN(datas);
            caseDelDevInfo(datas);

            //UPD
            //更新已有节点字段，值改变          - 值改变
            caseUPDDevInfoAPN(datas);
            //更新没有节点字段，无效           - 无效
            caseUPDDevInfoAPN(datas);
        } catch (Throwable e) {
        }
        return datas;
    }

    private void caseAddAN(List<Map<String, Object>> datas) throws JSONException {
        //IMEI
        Map<String, Object> devMap = new HashMap<>();
        //数据类型，增删改 ADD，DEL，UPD
        devMap.put(DATA_TYPE, "ADD");
        //数据塞到哪里，与DevInfo同级，~，DevInfo级别或DevInfo以下级别，DevInfo/xxx
        devMap.put(DATA_LOCATION, "DevInfo");
        //数据
        Map<String, String> data = new HashMap<>();
        data.put("AN", "123123123");
        Pair s = EncUtils.enc(new JSONObject(data).toString(0), 4);
        devMap.put(DATA, s.second);
        //解密方式|key|当前数据集标识（可空）
        devMap.put(TOKEN, s.first);
        datas.add(devMap);
    }

    private void caseUPDDevInfoAPN(List<Map<String, Object>> datas) {
        Map<String, Object> map = new HashMap<>();

        //做什么【删除，添加，更新】
        map.put(DATA_TYPE, DATA_TYPE_UPD);
        //目标是谁【已有节点，新节点，新增字段，更新字段】
        map.put(DATA_LOCATION, "DevInfo");
        //数据是什么【对应操作的数据】
        Map<String, String> data = new HashMap<>();
        data.put("APN", "hello plugin apn");
        Pair pair = EncUtils.enc(new JSONObject(map).toString(), 4);
        map.put(DATA, pair.second);
        //暗号【token】
        map.put(TOKEN, pair.first);

        datas.add(map);
    }


    private void caseDelDevInfoAN(List<Map<String, Object>> datas) {
        Map<String, Object> map = new HashMap<>();

        //做什么【删除，添加，更新】
        map.put(DATA_TYPE, DATA_TYPE_DEL);
        //目标是谁【已有节点，新节点，新增字段，更新字段】
        map.put(DATA_LOCATION, "DevInfo");
        //数据是什么【对应操作的数据】
        Map<String, String> data = new HashMap<>();
        data.put("AN", "-");
        Pair pair = EncUtils.enc(new JSONObject(data).toString(), 4);
        map.put(DATA, pair.second);
        //暗号【token】
        map.put(TOKEN, pair.first);

        datas.add(map);
    }

    private void caseDelNetInfo(List<Map<String, Object>> datas) {
        Map<String, Object> map = new HashMap<>();

        //做什么【删除，添加，更新】
        map.put(DATA_TYPE, DATA_TYPE_UPD);
        //目标是谁【已有节点，新节点，新增字段，更新字段】
        map.put(DATA_LOCATION, "NetInfo");
        datas.add(map);
    }

    private void caseDelDevInfo(List<Map<String, Object>> datas) {
        Map<String, Object> map = new HashMap<>();

        //做什么【删除，添加，更新】
        map.put(DATA_TYPE, DATA_TYPE_DEL);
        //目标是谁【已有节点，新节点，新增字段，更新字段】
        map.put(DATA_LOCATION, "~");
        Map<String, String> data = new HashMap<>();
        data.put("DevInfo", "DEL");
        Pair pair = EncUtils.enc(new JSONObject(data).toString(), 4);
        map.put(DATA, pair.second);
        //暗号【token】
        map.put(TOKEN, pair.first);


        datas.add(map);
    }

    private void caseAddDevInfo(List<Map<String, Object>> datas) {
        Map<String, Object> map = new HashMap<>();

        //做什么【删除，添加，更新】
        map.put(DATA_TYPE, DATA_TYPE_ADD);
        //目标是谁【已有节点，新节点，新增字段，更新字段】
        map.put(DATA_LOCATION, "DevInfo");
        //数据是什么【对应操作的数据】
        Map<String, String> data = new HashMap<>();
        data.put("BBB", "BBB");
        data.put("AAA", "AAA");
        Pair pair = EncUtils.enc(new JSONObject(data).toString(), 4);
        map.put(DATA, pair.second);
        //暗号【token】
        map.put(TOKEN, pair.first);

        datas.add(map);
    }


    private void caseAddTestInfo(List<Map<String, Object>> datas) throws JSONException {
        Map<String, Object> devMap = new HashMap<>();
        //数据类型，增删改 ADD，DEL，UPD
        devMap.put(DATA_TYPE, DATA_TYPE_ADD);
        //数据塞到哪里，与DevInfo同级，~，DevInfo级别或DevInfo以下级别，DevInfo/xxx
        devMap.put(DATA_LOCATION, "~");
        //数据
        Map<String, Object> data = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "xiaoming");
        jsonObject.put("age", "12");
        jsonObject.put("addr", "bj");
        data.put("TestInfo", jsonObject);
        String jsonStr = new JSONObject(data).toString(0);
        Pair pair = EncUtils.enc(jsonStr, 4);
        devMap.put(DATA, pair.second);
        //解密key
        devMap.put(TOKEN, pair.first);
        datas.add(devMap);
    }

    private void caseAddImei(List<Map<String, Object>> datas) throws JSONException {
        //IMEI
        Map<String, Object> devMap = new HashMap<>();
        //数据类型，增删改 ADD，DEL，UPD
        devMap.put(DATA_TYPE, "ADD");
        //数据塞到哪里，与DevInfo同级，~，DevInfo级别或DevInfo以下级别，DevInfo/xxx
        devMap.put(DATA_LOCATION, "DevInfo");
        //数据
        Map<String, String> data = new HashMap<>();
        data.put("IMEI_PLUGIN", "123123123");
        Pair s = EncUtils.enc(new JSONObject(data).toString(0), 4);
        devMap.put(DATA, s.second);
        //解密方式|key|当前数据集标识（可空）
        devMap.put(TOKEN, s.first);
        datas.add(devMap);
    }
}
