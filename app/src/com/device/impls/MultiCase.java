package com.device.impls;


import android.content.Context;

import com.device.utils.EL;
import com.device.utils.ProcessUtils;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 多进程case类
 * @Version: 1.0
 * @Create: 2019-07-30 18:23:59
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class MultiCase {

    /**
     * 运行具体的case
     *
     * @param context
     * @param caseNum
     */
    public static void runCase(Context context, int caseNum) {
        EL.i(ProcessUtils.getCurrentProcessName(context) + "--------->收到case" + caseNum);
    }
}
