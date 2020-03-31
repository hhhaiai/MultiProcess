package com.device.utils.memot;

import android.content.Context;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2020/3/31 14:51
 * @author: sanbo
 */
public class M2Fmapping {


    /********************* get instance begin **************************/
    public static M2Fmapping getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private M2Fmapping initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final M2Fmapping INSTANCE = new M2Fmapping();
    }

    private M2Fmapping() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/
}
