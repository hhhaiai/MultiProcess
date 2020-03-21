package com.analysys.plugin;

import org.junit.Assert;
import org.junit.Test;

public class AllStrMixTest {

    @Test
    public void decrypt() {

        String str = AllStrMix.decrypt("AAAAAAAAAAAA");
        //由 lysys2020ana base64而来
        Assert.assertEquals("bHlzeXMyMDIwYW5h", str);
    }
}