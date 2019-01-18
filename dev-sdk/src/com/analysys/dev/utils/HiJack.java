package com.analysys.dev.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HiJack {
    //检测xposed相关文件

    /**
     *检测XposedBridge.jar这个文件和de.robv.android.xposed.XposedBridge
     * XposedBridge.jar存放在framework里面，de.robv.android.xposed.XposedBridge是开发xposed框架使用的主要接口。
     * 在这个方法里读取了maps这个文件，在linux内核中，这个文件存储了进程映射了的内存区域和访问权限。在这里我们可以看到这个进程加载了那些文件。
     * 如果进程加载了xposed相关的so库或者jar则表示xposed框架已注入
     * @return
     */
    public static boolean byCheckXposeFile(){
        try
        {
            Object localObject = new HashSet();
            // 读取maps文件信息
            BufferedReader localBufferedReader =
                    new BufferedReader(new FileReader("/proc/" + android.os.Process.myPid() + "/maps"));
            // 遍历查询关键词 反编译出来的代码可能不太准确
            for (;;)
            {
                String str = localBufferedReader.readLine();
                if (str == null) {
                    break;
                }
                if ((str.endsWith(".so")) || (str.endsWith(".jar"))) {
                    ((Set)localObject).add(str.substring(str.lastIndexOf(" ") + 1));
                }
            }
            localBufferedReader.close();
            localObject = ((Set)localObject).iterator();
            while (((Iterator)localObject).hasNext())
            {
                boolean bool = ((String)((Iterator)localObject).next()).toLowerCase().contains("xposed");
                if (bool) {
                    return true;
                }
            }
        }catch (Exception e) {

        }
        return false;
    }
    //尝试加载xposed的类,如果能加载则表示已经安装了
    public static boolean byLoadXposedClass(){
        try{
            Object localObject = ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedHelpers").newInstance();
            // 如果加载类失败 则表示当前环境没有xposed
            if (localObject != null){
               return true;
            }
            return false;
        }catch (Throwable localThrowable) {
            return false;
        }
    }

}
