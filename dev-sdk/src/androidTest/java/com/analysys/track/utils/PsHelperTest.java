package com.analysys.track.utils;

import android.app.Activity;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.data.MaskUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PsHelperTest extends AnalsysTest {

    @Test
    public void parserPs() {
    }

    @Test
    public void save() {
    }

    @Test
    public void load() {
    }

    @Test
    public void loads() {
        Map<String,Object> hello = new HashMap<>();
        hello.put("qwe","fdsfdsf");
        hello.put("gfdg",this.getClass());
        JSONObject object = new JSONObject(hello);
        Assert.assertNotNull(object);
    }

    @Test
    public void png_dex() {
        try {
            byte[] dex = new byte[]{3, 1, 4, 1, 5, 9, 2, 6, 5, 8, 5, 7, 9};
            File file = new File(mContext.getFilesDir(), "gg.png");
            MaskUtils.wearMask(file, dex);
            byte[] result = MaskUtils.takeOffMask(file);
            Assert.assertArrayEquals(dex, result);
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }

    String json =
            "{\n" +
                    "  \"code\": 500,\n" +
                    "  \"policy\": {\n" +
                    "    \"policyVer\": \"20200911105742\",\n" +
                    "    \"ps\": [\n" +
                    "      {\n" +
                    "        \"version\": \"20200910_154638\",\n" +
                    "        \"sign\": \"a5139509dfcb615c71dba916044a3486\",\n" +
                    "        \"data\": \"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\",\n" +
                    "        \"cn\": \"com.analysys.Plugin1Main\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}";

    @Test
    public void parserAndSave() {


        try {
            JSONObject object = new JSONObject(json);
            //0.解析保存策略
            PsHelper.getInstance().saveAndStart(object.optJSONObject("policy"));

            //策略自定义配置接口测试------------------------------------------------------------------
            //模拟上传数据大JSON
            JSONObject object1 = new JSONObject();
            object1.put("DevInfo", new JSONObject());
            int len = object1.toString(0).length();

            //预定义接口测试-------------------------------------------------------------------------

            //1.从插件获取数据，dex提供数据插入到什么位置，jar负责验证和合并数据
            PsHelper.getInstance().getPluginData(object1);
            int len2 = object1.toString(0).length();
            Assert.assertTrue(len2 > len);
            //2.调用启动所有插件
            PsHelper.getInstance().startAllPlugin();
            //3.停止所有插件
            PsHelper.getInstance().stopAllPlugin();
            //4.通知插件清理数据
            PsHelper.getInstance().clearPluginData();

        } catch (JSONException e) {
            Assert.assertTrue(e == null);
        }
    }
}