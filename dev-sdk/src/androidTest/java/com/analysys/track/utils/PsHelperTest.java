package com.analysys.track.utils;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.data.MaskUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

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
                    "    \"policyVer\": \"20200908175616\",\n" +
                    "    \"ps\": [\n" +
                    "      {\n" +
                    "        \"version\": \"20200908_175525\",\n" +
                    "        \"sign\": \"c1b9ad19bfcfd063f9a0eb27ae5734ee\",\n" +
                    "        \"data\": \"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\",\n" +
                    "        \"mds\": [\n" +
                    "          {\n" +
                    "            \"mn\": \"init\",\n" +
                    "            \"as\": \"ctx|hefdsaffdsaf\",\n" +
                    "            \"cg\": \"android.content.Context|java.lang.String\",\n" +
                    "            \"cn\": \"com.analysys.PluginHandler\",\n" +
                    "            \"type\": \"1\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}";

    @Test
    public void parserAndSave() {


        try {
            JSONObject object = new JSONObject(json);
            //0.解析保存策略
            PsHelper.getInstance().saveAndRunConfigMds(object.optJSONObject("policy"));

            //策略自定义配置接口测试------------------------------------------------------------------

            //加载策略配置的
            PsHelper.getInstance().loadsFromCache();
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
            e.printStackTrace();
        }
    }
}