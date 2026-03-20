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

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name.
-renamesourcefileattribute SourceFile

# Keep data binding classes
-keep class com.sgdigitalposter.app.databinding.** { *; }

# Keep model/data classes (prevent field name obfuscation for Gson/Firebase)
# NOTE: The actual model classes are in the 'items' package, not 'models' or 'pojo'
-keep class com.iqueen.brandpeak.items.** { *; }
-keep class com.sgdigitalposter.app.items.** { *; }

# Keep API response/status classes
-keep class com.iqueen.brandpeak.api.** { *; }
-keep class com.sgdigitalposter.app.api.** { *; }

# Keep Room database and converters
-keep class com.iqueen.brandpeak.database.** { *; }
-keep class com.sgdigitalposter.app.database.** { *; }

# Keep ViewModel classes
-keep class com.iqueen.brandpeak.viewmodel.** { *; }
-keep class com.sgdigitalposter.app.viewmodel.** { *; }

# Keep repository classes
-keep class com.iqueen.brandpeak.repository.** { *; }
-keep class com.sgdigitalposter.app.repository.** { *; }

# Keep classes with @SerializedName annotations (safety net)
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Gson serialized/deserialized classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# CRITICAL: Keep TypeToken generic signatures (R8 strips them causing crashes)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Keep Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit call adapter and converter factories
-keep class * extends retrofit2.CallAdapter$Factory { *; }
-keep class * extends retrofit2.Converter$Factory { *; }

# CRITICAL: Keep generic type info for LiveData + Retrofit integration
# Without this, R8 strips ParameterizedType info and causes ClassCastException
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep LiveData call adapter classes
-keep class com.sgdigitalposter.app.api.common.** { *; }
-keep class com.iqueen.brandpeak.api.common.** { *; }

# Keep ApiService interface methods with full generic signatures
# CRITICAL: -keep (not -keepnames) to prevent obfuscation of return types
-keep interface com.sgdigitalposter.app.api.ApiService { *; }
-keep interface com.iqueen.brandpeak.api.ApiService { *; }

# Keep ApiResponse generic type information
-keep class com.sgdigitalposter.app.api.ApiResponse { *; }
-keep class com.iqueen.brandpeak.api.ApiResponse { *; }

# CRITICAL: Keep LiveData class name so Retrofit can resolve ParameterizedType
-keep class androidx.lifecycle.LiveData { *; }
-keep class androidx.lifecycle.MutableLiveData { *; }
-keep class androidx.lifecycle.MediatorLiveData { *; }

# Preserve generic signatures for Retrofit and LiveData (prevents ClassCastException)
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep OneSignal
-keep class com.onesignal.** { *; }

# Keep uCrop
-keep class com.yalantis.ucrop.** { *; }

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep WebView JS interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Stripe - ignore missing card scan classes
-dontwarn com.stripe.android.stripecardscan.**
-dontwarn com.stripe.android.ui.core.**

# Suppress all missing class warnings (libraries with optional dependencies)
-dontwarn javax.annotation.**
-dontwarn kotlin.reflect.jvm.internal.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**