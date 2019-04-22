# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-optimizationpasses 5               # 代码的压缩级别 0 - 7(指定代码进行迭代优化的次数，在Android里面默认是5)
#-dontusemixedcaseclassnames         # 混淆时不会产生形形色色的类名(混淆时不使用大小写混合类名)
#-dontskipnonpubliclibraryclasses    # 指定不去忽略非公共的库类(不跳过library中的非public的类)
#-dontskipnonpubliclibraryclassmembers # 指定不去忽略包可见的库类的成员
##默认已包含
##-dontoptimize      # 不进行优化
##-dontpreverify     # 不进行预校验
#-ignorewarnings     # 屏蔽警告
## 指定混淆采用的算法,谷歌推荐的算法，一般不做更改
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes *Annotation*        # 保护代码中的Annotation不被混淆
#-keepattributes Signature           # 避免混淆泛型, 用于JSON实体映射
#-keepattributes SourceFile,LineNumberTable  # 抛出异常时保留代码行号
#-allowaccessmodification            # 优化时允许访问并修改有修饰符的类和类的成员，可以提高优化步骤的结果
#-repackageclasses ''                # 把执行后的类重新放在某一个目录下，后跟一个目录名
#-verbose

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.multidex.MultiDexApplication
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
#-keep public class * extends android.support.v4.**
#-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**
-keep public class * extends android.graphics.drawable.Drawable{*;}
#-keep class android.support.** {*;}
# 接入Google原生的一些服务时使用
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
# 不混淆任何包含native方法的类的类名以及native方法名,和native有关的自定义的类的参数可能会被混淆,但是会在jni初始化
# 时报错,需要自己手动将和native方法有关的自定义类也添加到不混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
# 在layout 中写的onclick方法android:onclick="onClick"，不进行混淆
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# 不混淆任何一个View中的setXxx()和getXxx()方法,属性动画需要有相应的setter和getter的方法实现.混淆了无法工作
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
   public <init>(android.content.Context);
   public <init>(android.content.Context, android.util.AttributeSet);
   public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
# 不混淆Parcelable实现类中的CREATOR字段
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
# 指定了继承Serizalizable的类的如下成员不被移除混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# 保留R下的资源
-keepclassmembers class **.R$* {
    public static <fields>;
}
# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
#-keepclassmembers class * {
#    void *(**On*Event);
#    void *(**On*Listener);
#}
#webView处理
#-keepclassmembers class com.example.Webview {
#   public *;
#}
#-keepclassmembers class * extends android.webkit.WebViewClient {
#    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
#    public boolean *(android.webkit.WebView, java.lang.String);
#}
#-keepclassmembers class * extends android.webkit.WebViewClient {
#    public void *(android.webkit.WebView, jav.lang.String);
#}
#在app中与HTML5的JavaScript的交互进行特殊处理,确保js要调用的原生方法不能够被混淆：
#-keepclassmembers class com.example.JSInterface {
#    <methods>;
#}
# 删除代码中Log相关的代码
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
# 保持测试相关的代码
#-dontnote junit.framework.**
#-dontnote junit.runner.**
#-dontwarn android.test.**
#-dontwarn android.support.test.**
#-dontwarn org.junit.**