package com.analysys.track.internal.model;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class PsInfoTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void toJson() {
        String json = "{\n" +
                "    \"version\": \"000\",\n" +
                "    \"sign\": \"0835f7b53e49260b2e98f6d515de8651\",\n" +
                "    \"savePath\": \"3sb/H9AA\",\n" +
                "    \"mds\": [\n" +
                "        {\n" +
                "            \"mn\": \"test2\",\n" +
                "            \"as\": \"ctx|123456\",\n" +
                "            \"cg\": \"ctx|i\",\n" +
                "            \"cn\": \"com.test2\",\n" +
                "            \"type\": \"0\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"mn\": \"test\",\n" +
                "            \"as\": \"ctx|HelloWorld\",\n" +
                "            \"cg\": \"ctx|s\",\n" +
                "            \"cn\": \"com.test\",\n" +
                "            \"type\": \"1\"\n" +
                "        }\n" +
                "    ]}";

        try {
            PsInfo psInfo1 = PsInfo.fromJson(new JSONObject(json));
            PsInfo psInfo2 = PsInfo.fromJson( psInfo1.toJson());
            Assert.assertEquals(psInfo1,psInfo2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fromJson() {
    }
}