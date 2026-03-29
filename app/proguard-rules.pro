# ProGuard rules for CryptoPulse

# Retain Parcelize annotations if used by dependencies
-keepattributes *Annotation*
-dontwarn kotlinx.parcelize.**
-keep class kotlinx.parcelize.Parcelize { *; }

# AWS Amplify
-dontwarn com.amplifyframework.**
-keep class com.amplifyframework.** { *; }

# Hilt
-keep class dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * implements dagger.hilt.EntryPoint { *; }
-keep @dagger.hilt.EntryPoint class * { *; }

# Retrofit / OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Android Security Crypto (EncryptedSharedPreferences)
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Keep all models/entities that are serialized by Gson
-keep class com.cryptopulse.trader.data.model.** { *; }
