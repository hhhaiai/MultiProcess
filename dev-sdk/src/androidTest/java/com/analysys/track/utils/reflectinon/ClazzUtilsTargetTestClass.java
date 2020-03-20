package com.analysys.track.utils.reflectinon;

import android.view.View;

import java.text.SimpleDateFormat;

public class ClazzUtilsTargetTestClass extends SimpleDateFormat implements View.OnClickListener {
    private int a = 100;
    private static int a2 = 100;
    private static final int a3 = 100;
    private final int a4 = 100;
    private volatile static int a5 = 100;
    private volatile int a6 = 100;


    public static String publicstaticM(String v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        return element.getClassName() + element.getMethodName() + element.getLineNumber();
    }

    public String publicnotstaticM(String v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        return element.getClassName() + element.getMethodName() + element.getLineNumber();
    }

    private static String privatestaticM(String v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        return element.getClassName() + element.getMethodName() + element.getLineNumber();
    }

    private String privatenotstaticM(String v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        return element.getClassName() + element.getMethodName() + element.getLineNumber();
    }

    public static String publicstaticM() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        return element.getClassName() + element.getMethodName() + element.getLineNumber();
    }

    public String publicnotstaticM() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        return element.getClassName() + element.getMethodName() + element.getLineNumber();
    }

    private static String privatestaticM() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        return element.getClassName() + element.getMethodName() + element.getLineNumber();
    }

    private String privatenotstaticM() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
        return element.getClassName() + element.getMethodName() + element.getLineNumber();
    }

    @Override
    public void onClick(View v) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[1];
    }

    class InnerClass {
        private int a = 1;
        private static final int a3 = 2;
        private final int a4 = 3;
        private volatile int a6 = 4;


        public String publicnotstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }


        private String privatenotstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }

        public String publicnotstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }


        private String privatenotstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
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
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }

        public String publicnotstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }

        private static String privatestaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }

        private String privatenotstaticM(String v) {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }

        public static String publicstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }

        public String publicnotstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }

        private static String privatestaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }

        private String privatenotstaticM() {
            StackTraceElement element = Thread.currentThread().getStackTrace()[1];
            return element.getClassName() + element.getMethodName() + element.getLineNumber();
        }
    }
}
