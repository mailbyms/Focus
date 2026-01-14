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
  **[] $VALUES;
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
-keep class * extends org.litepal.crud.LitePalSupport { *; }
# 保留所有数据模型类，防止混淆导致 LitePal 反射失败
-keep class com.ihewro.focus.bean.** { *; }

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

# Activity Result API
-keep class androidx.activity.result.** { *; }
-keep interface androidx.activity.result.** { *; }

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# BaseRecyclerViewAdapterHelper (BaseAdapter)
-keep class com.chad.library.adapter.base.** { *; }
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers class * extends com.chad.library.adapter.base.BaseViewHolder {
    <init>(android.view.View);
}
-keep class * implements com.chad.library.adapter.base.BaseMultiItemEntity

# 保留项目中的自定义适配器和 ViewHolder
-keep class com.ihewro.focus.adapter.** { *; }
-keep class com.ihewro.focus.helper.** { *; }
-keepclassmembers class com.ihewro.focus.adapter.** {
    public <methods>;
    protected <methods>;
}
-keepclassmembers class com.ihewro.focus.helper.** {
    <init>(android.view.View);
}
