package com.analysys;

import android.util.Pair;

import org.junit.Assert;
import org.junit.Test;

public class EncUtilsTest {

    @Test
    public void enc() {
        String[] str = new String[]{"", "hgfdhfdgh", "q", "fdskafhdsakfhsafhkdsafh", "\n"};
        for (int i = 0; i < str.length; i++) {
            Pair pair = EncUtils.enc(str[i], 10000);
            String data = EncUtils.dec(pair);
            Assert.assertEquals(str[i], data);
        }

    }
}