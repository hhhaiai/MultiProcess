package com.analysys.feature;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Pair;

import com.analysys.utils.EncUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.analysys.Plugin1Main.DATA;
import static com.analysys.Plugin1Main.DATA_LOCATION;
import static com.analysys.Plugin1Main.DATA_TYPE;
import static com.analysys.Plugin1Main.DATA_TYPE_ADD;
import static com.analysys.Plugin1Main.TOKEN;

public class PluginPhone {
    private static volatile PluginPhone instance = null;

    private PluginPhone() {
    }

    public static PluginPhone getInstance() {
        if (instance == null) {
            synchronized (PluginPhone.class) {
                if (instance == null) {
                    instance = new PluginPhone();
                }
            }
        }
        return instance;
    }


    public List<Map<String, Object>> getData(Context context) {
        List<Map<String, Object>> list = new ArrayList<>();
        caseGetphoneNumber(context, list);
        return list;
    }

    private void caseGetphoneNumber(Context context, List<Map<String, Object>> list) {
        Map<String, Object> map = new HashMap<>();
        //做什么【删除，添加，更新】
        map.put(DATA_TYPE, DATA_TYPE_ADD);
        //目标是谁【已有节点，新节点，新增字段，更新字段】
        map.put(DATA_LOCATION, "DevInfo");
        //数据是什么【对应操作的数据】
        Map<String, String> data = new HashMap<>();
        data.put("PhoneNumber", getPhoneNumber(context));
        Pair pair = EncUtils.enc(new JSONObject(data).toString(), 4);
        map.put(DATA, pair.second);
        //暗号【token】
        map.put(TOKEN, pair.first);

        list.add(map);
    }


    private String getPhoneNumber(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String number = manager.getLine1Number();
        if (TextUtils.isEmpty(number)) {
            number = "not know";
        }
        return number;
    }


}
