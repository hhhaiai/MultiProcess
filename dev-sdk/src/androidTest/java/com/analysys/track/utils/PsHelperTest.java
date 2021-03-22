//package com.analysys.track.utils;
//
//import android.app.Activity;
//
//import com.analysys.track.AnalsysTest;
//import com.analysys.track.utils.data.MaskUtils;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//public class PsHelperTest extends AnalsysTest {
//
//    @Test
//    public void parserPs() {
//    }
//
//    @Test
//    public void save() {
//    }
//
//    @Test
//    public void load() {
//    }
//
//    @Test
//    public void loads() {
//        Map<String,Object> hello = new HashMap<>();
//        hello.put("qwe","fdsfdsf");
//        hello.put("gfdg",this.getClass());
//        JSONObject object = new JSONObject(hello);
//        Assert.assertNotNull(object);
//    }
//
//    @Test
//    public void png_dex() {
//        try {
//            byte[] dex = new byte[]{3, 1, 4, 1, 5, 9, 2, 6, 5, 8, 5, 7, 9};
//            File file = new File(mContext.getFilesDir(), "gg.png");
//            MaskUtils.wearMask(file, dex);
//            byte[] result = MaskUtils.takeOffMask(file);
//            Assert.assertArrayEquals(dex, result);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    String json =
//            "{\n" +
//                    "  \"code\": 500,\n" +
//                    "  \"policy\": {\n" +
//                    "    \"policyVer\": \"20200915172026\",\n" +
//                    "    \"ps\": [\n" +
//                    "      {\n" +
//                    "        \"version\": \"20200915_171827\",\n" +
//                    "        \"sign\": \"fff44ee6320f35eb55fb69fd3e22885e\",\n" +
//                    "        \"data\": \"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\",\n" +
//                    "        \"cn\": \"com.analysys.Plugin1Main\"\n" +
//                    "      }\n" +
//                    "    ]\n" +
//                    "  }\n" +
//                    "}";
//
//    @Test
//    public void parserAndSave() {
//
//
//        try {
//            JSONObject object = new JSONObject(json);
//            //0.解析保存策略
//            PsHelper.getInstance().saveAndStart(object.optJSONObject("policy"));
//
//            //策略自定义配置接口测试------------------------------------------------------------------
//            //模拟上传数据大JSON
//            JSONObject object1 = new JSONObject();
//            object1.put("DevInfo", new JSONObject());
//            int len = object1.toString(0).length();
//
//            //预定义接口测试-------------------------------------------------------------------------
//
//            //1.从插件获取数据，dex提供数据插入到什么位置，jar负责验证和合并数据
//            PsHelper.getInstance().getPluginData(object1);
//            int len2 = object1.toString(0).length();
//            Assert.assertTrue(len2 > len);
//            //2.调用启动所有插件
//            PsHelper.getInstance().startAllPlugin();
//            //3.停止所有插件
//            PsHelper.getInstance().stopAllPlugin();
//            //4.通知插件清理数据
//            PsHelper.getInstance().clearPluginData();
//
//        } catch (JSONException e) {
//            Assert.assertTrue(e == null);
//        }
//    }
//}