# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# R class
-keep class .R
-keep class **.R$* {
    <fields>;
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class * implements com.bumptech.glide.module.LibraryGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES
  public *;
}

# EventBus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# LitePal
-keep class org.litepal.** { *; }
-keep class * extends org.litepal.crud.DataSupport { *; }

# MaterialDrawer
-keep class com.mikepenz.** { *; }
-dontwarn com.mikepenz.**

# XPopup
-keep class com.lxj.xpopup.** { *; }
-dontwarn com.lxj.xpopup.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Okio
-dontwarn okio.**
-keep class okio.** { *; }

# Jackson
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* <methods>;
    @com.fasterxml.jackson.annotation.* <fields>;
}
-dontwarn com.fasterxml.jackson.**

# Jsoup
-keep class org.jsoup.** { *; }

# Guava
-dontwarn com.google.common.**
-keep class com.google.common.** { *; }

# AgentWeb
-keep class com.just.agentweb.** { *; }
-dontwarn com.just.agentweb.**

# Recovery
-keep class com.zxy.android.recovery.** { *; }


# Toasty
-dontwarn com.github.grenderg.**

# EasyPermissions
-dontwarn pub.devrel.easypermissions.**

# SlideBack
-dontwarn com.github.ChenTianSaber.**

# MaterialDialogs
-keep class com.afollestad.materialdialogs.** { *; }
-dontwarn com.afollestad.materialdialogs.**

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable
