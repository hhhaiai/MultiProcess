package com.miqt.costtime;

import com.analysys.plugin.StringFog;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StringFogTest {
    StringFog.StringFogImpl stringFog;

    @Before
    public void before() {
        stringFog = new StringFog.StringFogImpl();
    }

    @Test
    public void test1() {
        String[] keys = new String[]{
                "中())(*&^%$#$%^文",
                "aaabbb",
                "aa \nbb cc ",
                ".,/,/.,!@#$%^&*())(*&^%$#$%^&*()",
                "=====",
                "-123",
                "miqt",
                "\n",
                "\\",
        };
        String[] textCase = new String[]{
                "中文())(*&^%$#$%^",
                "aaabbb",
                "aa bb cc ",
                ".,/,/.,!@#$%^&*())(*&^\n%$#$%^&*()",
                "=====",
                "-123",
                "\n",
                "\\",
        };
        for (String key : keys) {
            for (String text : textCase) {
                Assert.assertEquals(text, stringFog.decrypt(stringFog.encrypt(text, key), key));
            }
        }


    }


    @Test
    public void g() {
        String g = StringFog_G.FOG.g();
        Assert.assertNotNull(g);
    }


}