package com.analysys.track.utils;

import com.analysys.track.internal.content.EGContext;

import org.junit.Assert;
import org.junit.Test;

public class PolicyEncryptTest {

    @Test
    public void encode() {
        PolicyEncrypt policyEncrypt = PolicyEncrypt.getInstance();
        String[] keys = new String[]{
                "中())(*&^%$#$%^文",
                "aaabbb",
                "aa \nbb cc ",
                ".,/,/.,!@#$%^&*())(*&^%$#$%^&*()",
                "=====",
                "-123",
                "miqt",
                "null",
                "\n",
                "",
                "\\",
        };
        String[] textCase = new String[]{
                "中文())(*&^%$#$%^",
                "aaabbb",
                "aa bb cc ",
                ".,/,/.,!@#$%^&*())(*&^\n%$#$%^&*()",
                "=====",
                "-123",
                "",
                "\n",
                "null",
                "\\",
        };
        for (String key : keys) {
            for (String text : textCase) {
                String encode = policyEncrypt.encode(text, key, EGContext.SDKV, "-1",
                        "20191206_Analysys");
                Assert.assertEquals(text, policyEncrypt.decode(encode, key, EGContext.SDKV, "-1",
                        "20191206_Analysys"));
            }
        }
    }

    @Test
    public void decode() {
    }
}