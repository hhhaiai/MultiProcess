package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

public class ClazzUtilsTargetTestClass extends AnalsysTest implements View.OnClickListener {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    private int a = 100;
    private static int a2 = 100;
    private static final int a3 = 100;
    private final int a4 = 100;
    private volatile static int a5 = 100;
    private volatile int a6 = 100;


    public static String publicstaticM(String v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    public String publicnotstaticM(String v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    private static String privatestaticM(String v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    private String privatenotstaticM(String v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    public static String publicstaticM() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    public String publicnotstaticM() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    private static String privatestaticM() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    private String privatenotstaticM() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    @Override
    public void onClick(View v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[2];
        String result = "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
    }

    class InnerClass {
        private int a = 1;
        private static final int a3 = 2;
        private final int a4 = 3;
        private volatile int a6 = 4;


        public String publicnotstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }


        private String privatenotstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

        public String publicnotstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }


        private String privatenotstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

    }

    static class StaticInnerClass {
        private int a = 1;
        private static int a2 = 2;
        private static final int a3 = 3;
        private final int a4 = 4;
        private volatile static int a5 = 5;
        private volatile int a6 = 6;

        public static String publicstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

        public String publicnotstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

        private static String privatestaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

        private String privatenotstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

        public static String publicstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

        public String publicnotstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

        private static String privatestaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }

        private String privatenotstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[2];
            return "[" + element.getLineNumber() + "]" + element.getClassName() + "." + element.getMethodName();
        }
    }
}
