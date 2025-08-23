# PixelSpoof ProGuard Rules
# Kotlin & Coroutines
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

# Xposed Framework
-keep class de.robv.android.xposed.** { *; }
-keep class io.github.libxposed.** { *; }

# Main Hook Class (Entry Point)
-keep class com.kashi.caimanspoof.MainHook { *; }
-keep class com.kashi.caimanspoof.MainHook$* { *; }

# Device Profile & Configuration
-keep class com.kashi.caimanspoof.DeviceProfile { *; }
-keep class com.kashi.caimanspoof.ConfigManager { *; }
-keep class com.kashi.caimanspoof.StealthManager { *; }

# OkHttp & Networking
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Android System Properties (Reflection)
-keep class android.os.SystemProperties { *; }
-keep class android.os.Build { *; }
-keep class android.os.Build$* { *; }

# JSON Parsing
-keep class org.json.** { *; }

# Obfuscation & Security
-obfuscationdictionary dictionary.txt
-classobfuscationdictionary dictionary.txt
-packageobfuscationdictionary dictionary.txt

# Remove Logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Anti-Reverse Engineering
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-renamesourcefileattribute SourceFile
-adaptresourcefilenames    **.properties,**.gif,**.jpg,**.png
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF

# String Encryption (Manual Implementation)
-keep class com.kashi.caimanspoof.StringObfuscator { *; }