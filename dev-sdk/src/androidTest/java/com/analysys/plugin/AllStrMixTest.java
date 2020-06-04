package com.analysys.plugin;

import org.junit.Assert;
import org.junit.Test;

public class AllStrMixTest {

    @Test
    public void decrypt() {
        String enc = AllStrMix.encrypt("AAAAAAAAAAAA");
        String str = AllStrMix.decrypt(enc);
        Assert.assertEquals("AAAAAAAAAAAA", str);
    }
}