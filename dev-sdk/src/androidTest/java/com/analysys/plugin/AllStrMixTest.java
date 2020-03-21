package com.analysys.plugin;

import org.junit.Assert;
import org.junit.Test;

public class AllStrMixTest {

    @Test
    public void decrypt() {

        String str = AllStrMix.decrypt("AAAAAAAAAAAA");
        Assert.assertEquals("bHlzeXMyMDIwYW5h", str);
    }
}