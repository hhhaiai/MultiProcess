package cn.analysys.casedemo.utils;

import android.widget.Toast;

import com.cslib.CaseHelper;
import com.cslib.cuscase.ECase;
import com.cslib.defcase.ETestCase;
import com.cslib.utils.L;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 调用demo和case的入口
 * @Version: 1.0
 * @Create: 2021/03/70 18:06:39
 * @author: sanbo
 */
public class Woo {
    /**
     * case中的toast
     *
     * @param info
     */
    private static void toastFromCase(String info) {
        Toast.makeText(CaseHelper.getCaseContext(), info, Toast.LENGTH_SHORT).show();
    }

    /**
     * 只打印来自case的需求打印
     *
     * @param info
     */
    public static void logFormCase(String info) {
        try {
            StackTraceElement[] stackElement = Thread.currentThread().getStackTrace();
            for (int i = 0; i < stackElement.length; i++) {
                StackTraceElement ele = stackElement[i];

                if ("logFormCase".equals(ele.getMethodName()) && (i + 1) < stackElement.length) {
                    ele = stackElement[i + 1];
                    String clsName = ele.getClassName();

                    if (SDKHelper.isSubClass(Class.forName(clsName), ETestCase.class) || SDKHelper.isSubClass(Class.forName(clsName), ECase.class)) {
                        L.i(info);
                        toastFromCase(info);
                    }
                }
            }
        } catch (Throwable e) {
            L.e(e);
        }


    }


}
