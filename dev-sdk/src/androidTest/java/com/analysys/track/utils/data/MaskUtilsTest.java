package com.analysys.track.utils.data;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class MaskUtilsTest extends AnalsysTest {

    @Test
    public void getDex() {
        byte[] dex = new byte[]{3,1,4,1,5,9,2,6,5,8,5,7,9};
        File file = new File(mContext.getFilesDir(),"gg.png");
        MaskUtils.saveDex(file,dex);
        byte[] result = MaskUtils.getDex(file);
        Assert.assertTrue(Arrays.equals(dex,result));
    }
}