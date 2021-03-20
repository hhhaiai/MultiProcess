package com.analysys.track.utils.data;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class MaskUtilsTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void getDex() {
        byte[] dex = new byte[]{3,1,4,1,5,9,2,6,5,8,5,7,9};
        File file = new File(mContext.getFilesDir(),"gg.png");
        MaskUtils.wearMask(file,dex);
        byte[] result = MaskUtils.takeOffMask(file);
        Assert.assertTrue(Arrays.equals(dex,result));
    }
}