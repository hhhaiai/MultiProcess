-optimizationpasses 5
##打印混淆的详细信息
#-verbose
##声明不压缩输入文件。
#-dontshrink
 # 不进行预校验,Android不需要,可加快混淆速度。
-dontpreverify
#将次选项去除，可以混淆时进行优化
-dontoptimize
# 屏蔽警告
-ignorewarnings
# 混淆时不会产生形形色色的类名(混淆时不使用大小写混合类名)
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库类(不跳过library中的非public的类)
-dontskipnonpubliclibraryclasses
# 指定不去忽略包可见的库类的成员
-dontskipnonpubliclibraryclassmembers
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*
# 避免混淆泛型, 这在JSON实体映射时非常重要
-keepattributes Signature
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.job.JobService
-keep public class * extends android.accessibilityservice.AccessibilityService
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
#################################################################
########################### 三方防混淆 ############################
#################################################################
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#for umeng
-keep class com.umeng.** {*;}
#-keep class com.analysys.** {*;}
-keep class com.bun.miitmdid.core.** {*;}

-keep public class com.device.R$*{
public static final int *;
}

#-keep class com.analysys.track.** {
#  public *;
#}
#-dontwarn com.analysys.track.**
## 测试case反射调用的，不混淆
#-keep class com.device.impls.** {*;}
#-keep class com.analysys.** {*;}
