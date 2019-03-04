package com.analysys.track.utils;


public class HiJack {
    //检测xposed相关文件

    /**
     *检测XposedBridge.jar这个文件和de.robv.android.xposed.XposedBridge
     * XposedBridge.jar存放在framework里面，de.robv.android.xposed.XposedBridge是开发xposed框架使用的主要接口。
     * 在这个方法里读取了maps这个文件，在linux内核中，这个文件存储了进程映射了的内存区域和访问权限。在这里我们可以看到这个进程加载了那些文件。
     * 如果进程加载了xposed相关的so库或者jar则表示xposed框架已注入
     * @return
     */
//    public static boolean byCheckXposeFile(){
//        try{
//            Set set = new HashSet();
//            // 读取maps文件信息
//            BufferedReader localBufferedReader =
//                    new BufferedReader(new FileReader("/proc/" + android.os.Process.myPid() + "/maps"));
//            // 遍历查询关键词
//            if(localBufferedReader != null){
//                String readline;
//                //TODO cat一下 拿最新文件
//                while ((readline = localBufferedReader.readLine()) != null ){
//                    if ((readline.endsWith(".so")) || (readline.endsWith(".jar"))) {
//                        set.add(readline.substring(readline.lastIndexOf(" ") + 1));
//                    }
//                }
//                localBufferedReader.close();
//                while (set.iterator().hasNext()){
//                    if(((String)set.iterator().next()).toLowerCase().contains("xposed")){
//                        return true;
//                    }
//                }
//            }
//        }catch (Exception e) {
//            ELOG.i(e.getMessage()+"  byCheckXposeFile");
//            return false;
//        }
//        return false;
//    }
    //尝试加载xposed的类,如果能加载则表示已经安装了
    public static boolean byLoadXposedClass(){
        try{
            Object localObject = ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedHelpers").newInstance();
            // 如果加载类失败 则表示当前环境没有xposed
            return localObject != null;
        }catch (Throwable localThrowable) {
            return false;
        }
    }

}
