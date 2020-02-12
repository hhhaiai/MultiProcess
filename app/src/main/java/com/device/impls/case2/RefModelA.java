package com.device.impls.case2;

import com.device.utils.EL;

public class RefModelA {

    private static String mName = "默认值";
    private static int sAge = -1;

    private RefModelA() {
    }

    public RefModelA(String name, int age) {
        EL.i("正经的初始化 name: " + name + " ; age: " + age);
        this.mName = name;
        this.sAge = age;
    }

    public void sayHi(String sth) {
        EL.i(mName + "[" + sAge + "] 公开方法 sayHi: " + sth);
    }

    private void sayFuck(String sth) {
        EL.i(mName + "[" + sAge + "]  私有方法 sayFuck: " + sth);
    }

    private static void sayFuckStatic(String sth) {
        EL.i(mName + "[" + sAge + "] 私有方法 static sayFuckStatic: " + sth);
    }
}
