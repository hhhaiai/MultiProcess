package com.analysys.track.utils.data;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DexUtilsTest extends AnalsysTest {

    @Test
    public void getDex() {
        byte[] dex = new byte[]{3,1,4,1,5,9,2,6,5,8,5,7,9};
        File file = new File(mContext.getFilesDir(),"gg.png");
        DexUtils.saveDex(file,dex);
        byte[] result = DexUtils.getDex(file);
        Assert.assertTrue(Arrays.equals(dex,result));
    }
}