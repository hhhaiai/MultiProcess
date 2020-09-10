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
                    "    \"policyVer\": \"20200910143649\",\n" +
                    "    \"ps\": [\n" +
                    "      {\n" +
                    "        \"version\": \"20200910_143214\",\n" +
                    "        \"sign\": \"be70a929b832b2d930dc7fe00c5150f3\",\n" +
                    "        \"data\": \"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\",\n" +
                    "        \"mds\": [\n" +
                    "          {\n" +
                    "            \"mn\": \"init\",\n" +
                    "            \"as\": \"ctx|hefdsaffdsaf\",\n" +
                    "            \"cg\": \"android.content.Context|java.lang.String\",\n" +
                    "            \"cn\": \"com.analysys.Plugin1Main\",\n" +
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