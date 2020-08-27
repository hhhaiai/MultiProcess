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


    @Test
    public void parserAndSave() {
        String json = "{\n" +
                "  \"code\": 500,\n" +
                "  \"policy\": {\n" +
                "    \"policyVer\": \"20200826143702\",\n" +
                "    \"ps\": [\n" +
                "      {\n" +
                "        \"version\": \"fdsaf\",\n" +
                "        \"sign\": \"c75071448fa3a940d0d1b27d15cd118c\",\n" +
                "        \"data\": \"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bm5vdGF0aW9uL01lbWJlckNsYXNzZXM7AB1MZGFsdmlrL2Fubm90YXRpb24vU2lnbmF0dXJlOwARTGphdmEvbGFuZy9DbGFzczsAEkxqYXZhL2xhbmcvT2JqZWN0OwASTGphdmEvbGFuZy9TdHJpbmc7ABlMamF2YS9sYW5nL1N0cmluZ0J1aWxkZXI7ABVMamF2YS9sYW5nL1Rocm93YWJsZTsAGkxqYXZhL2xhbmcvcmVmbGVjdC9NZXRob2Q7ABVMamF2YS91dGlsL0FycmF5TGlzdDsAD0xqYXZhL3V0aWwvTGlzdAAQTGphdmEvdXRpbC9MaXN0OwAVTG9yZy9qc29uL0pTT05PYmplY3Q7AAFWAAJWTAADVkxMAARWTExMAAFaAAJaTAADWkxMABJbTGphdmEvbGFuZy9DbGFzczsAE1tMamF2YS9sYW5nL09iamVjdDsAE1tMamF2YS9sYW5nL1N0cmluZzsAAWEAC2FjY2Vzc0ZsYWdzAANhZGQAGmFuZHJvaWQuYXBwLkFjdGl2aXR5VGhyZWFkABZhbmRyb2lkLmFwcC5BcHBHbG9iYWxzAAZhcHBlbmQABmFwcGtleQAJY2FsbGJhY2tzAAdjaGFubmVsAAVjb3VudAASY3VycmVudEFwcGxpY2F0aW9uAAFlAAZlcXVhbHMAB2Zvck5hbWUAA2dldAAKZ2V0Q29udGV4dAAHZ2V0SW1laQAVZ2V0SW5pdGlhbEFwcGxpY2F0aW9uAAtnZXRJbnN0YW5jZQAJZ2V0TWV0aG9kAARpbml0AAhpbnN0YW5jZQAGaW52b2tlAARtYWluAARuYW1lAApwbHVnaW5pbWllAA9wcmludFN0YWNrVHJhY2UAB3B1Ymxpc2gAA3B1dAAEc2l6ZQAJc3Vic2NyaWJlAAh0b1N0cmluZwAFdmFsdWUAB3ZlcnNpb24ACXdhbmRvdWppYQADeHh4AAEABw4AAwIAAAc7GS5paQABAAcOAAEABzsBFhBPARUQAnkdYAABAQAHDqU8ZgAEAwAAAAcOiAEaEAIR4AEaDwJqaAABAQAHDgABAAcOPAABAAcOSzxLeR85AAECAAAHLIcAAQEABywAAggBTxgHAgoCMCQJBkcXLwIJAU8aBgIKAjAECEceAgsBTxwBGAYCDAFPHAQXIhcEFxQXBwQXAxdRBAMXAgAAAAEIgQgAAAABAQGAgATECQIB3AkEAAUAABoBGgEaARoDgYAErAoBCsQKAZmACPgLAZmACNwMAQnMDgEBAwIFSgQCCYiABOwOAYKABIAPAQmoDwwBgBABAcwQAAARAAAAAAAAAAEAAAAAAAAAAQAAAFMAAABwAAAAAgAAABsAAAC8AQAAAwAAABMAAAAoAgAABAAAAAYAAAAMAwAABQAAABwAAAA8AwAABgAAAAQAAAAcBAAAAxAAAAQAAACcBAAAASAAAAwAAADEBAAABiAAAAMAAABwCAAAARAAAAwAAACoCAAAAiAAAFMAAAAKCQAAAyAAAAsAAABpDQAABCAAAAYAAADRDQAABSAAAAEAAAAFDgAAACAAAAQAAAAODgAAABAAAAEAAABsDgAA\",\n" +
                "        \"mds\": [\n" +
                "          {\n" +
                "            \"mn\": \"init\",\n" +
                "            \"as\": \"ctx|bbb|bbb\",\n" +
                "            \"cg\": \"android.content.Context|java.lang.String|java.lang.String\",\n" +
                "            \"cn\": \"com.analysys.Ab\",\n" +
                "            \"type\": \"1\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"mn\": \"init\",\n" +
                "            \"as\": \"ctx|aaa|aaa\",\n" +
                "            \"cg\": \"android.content.Context|java.lang.String|java.lang.String\",\n" +
                "            \"cn\": \"com.analysys.Ab\",\n" +
                "            \"type\": \"1\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        try {
            JSONObject object = new JSONObject(json);
            PsHelper.getInstance().parserAndSave(object.optJSONObject("policy"));
            PsHelper.getInstance().loadsFromCache();
            PsHelper.getInstance().loadsFromCache();

            JSONObject object1 = new JSONObject();
            int len = object1.length();
            PsHelper.getInstance().publish(object1, "getImei");
            PsHelper.getInstance().publish(object1, "getImei");
            int len2 = object1.length();
            Assert.assertTrue(len2 > len);
            PsHelper.getInstance().loadsFromCache();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}