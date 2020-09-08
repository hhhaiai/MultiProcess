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
                    "    \"policyVer\": \"20200908104423\",\n" +
                    "    \"ps\": [\n" +
                    "      {\n" +
                    "        \"version\": \"20200908_104332\",\n" +
                    "        \"sign\": \"1fd6350a4d61efc38f77cbeabfc74a30\",\n" +
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