package com.kashi.caimanspoof

import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class SimpleSpoofer : IXposedHookLoadPackage {
    
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Apply Pixel 10 Pro XL spoofing to ALL apps without exclusions
            XposedBridge.log("SimpleSpoofer: Hooking package ${lpparam.packageName}")
            applyPixelSpoofing(lpparam)
        } catch (e: Exception) {
            XposedBridge.log("SimpleSpoofer error: ${e.message}")
        }
    }
    
    private fun applyPixelSpoofing(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hook Build class fields
        hookBuildFields(lpparam)
        
        // Hook SystemProperties.get calls
        hookSystemProperties(lpparam)
    }
    
    private fun hookBuildFields(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val buildClass = XposedHelpers.findClass("android.os.Build", lpparam.classLoader)
            
            // Set all Build.* fields to Pixel 10 Pro XL values
            XposedHelpers.setStaticObjectField(buildClass, "MANUFACTURER", "Google")
            XposedHelpers.setStaticObjectField(buildClass, "BRAND", "google")
            XposedHelpers.setStaticObjectField(buildClass, "MODEL", "Pixel 10 Pro XL")
            XposedHelpers.setStaticObjectField(buildClass, "DEVICE", "mustang")
            XposedHelpers.setStaticObjectField(buildClass, "PRODUCT", "mustang")
            XposedHelpers.setStaticObjectField(buildClass, "BOARD", "mustang")
            XposedHelpers.setStaticObjectField(buildClass, "HARDWARE", "mustang")
            XposedHelpers.setStaticObjectField(buildClass, "FINGERPRINT", "google/mustang/mustang:16/BP41.250725.006/12701944:user/release-keys")
            XposedHelpers.setStaticObjectField(buildClass, "TAGS", "release-keys")
            XposedHelpers.setStaticObjectField(buildClass, "TYPE", "user")
            XposedHelpers.setStaticObjectField(buildClass, "ID", "BP41.250725.006")
            XposedHelpers.setStaticObjectField(buildClass, "DISPLAY", "BP41.250725.006")
            XposedHelpers.setStaticObjectField(buildClass, "HOST", "abfarm-02881")
            XposedHelpers.setStaticObjectField(buildClass, "USER", "android-build")
            
            // Hook Build.VERSION fields
            val versionClass = XposedHelpers.findClass("android.os.Build\$VERSION", lpparam.classLoader)
            XposedHelpers.setStaticObjectField(versionClass, "RELEASE", "16")
            XposedHelpers.setStaticIntField(versionClass, "SDK_INT", 36)
            XposedHelpers.setStaticObjectField(versionClass, "SDK", "36")
            XposedHelpers.setStaticObjectField(versionClass, "INCREMENTAL", "12701944")
            XposedHelpers.setStaticObjectField(versionClass, "SECURITY_PATCH", "2025-08-05")
            XposedHelpers.setStaticObjectField(versionClass, "CODENAME", "REL")
            
            XposedBridge.log("SimpleSpoofer: Build fields set to Pixel 10 Pro XL for ${lpparam.packageName}")
            
        } catch (e: Exception) {
            XposedBridge.log("SimpleSpoofer Build hook error: ${e.message}")
        }
    }
    
    private fun hookSystemProperties(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook single parameter version
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "get",
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as String
                        val spoofedValue = getSystemPropertyValue(key)
                        if (spoofedValue != null) {
                            param.result = spoofedValue
                            XposedBridge.log("SimpleSpoofer: Spoofed $key = $spoofedValue")
                        }
                    }
                }
            )
            
            // Hook two parameter version
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "get",
                String::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as String
                        val spoofedValue = getSystemPropertyValue(key)
                        if (spoofedValue != null) {
                            param.result = spoofedValue
                            XposedBridge.log("SimpleSpoofer: Spoofed $key = $spoofedValue")
                        }
                    }
                }
            )
            
            XposedBridge.log("SimpleSpoofer: SystemProperties hooks installed for ${lpparam.packageName}")
            
        } catch (e: Exception) {
            XposedBridge.log("SimpleSpoofer SystemProperties hook error: ${e.message}")
        }
    }
    
    private fun getSystemPropertyValue(key: String): String? {
        return when (key) {
            "ro.product.manufacturer" -> "Google"
            "ro.product.brand" -> "google"
            "ro.product.model" -> "Pixel 10 Pro XL"
            "ro.product.device" -> "mustang"
            "ro.product.name" -> "mustang"
            "ro.product.board" -> "mustang"
            "ro.build.fingerprint" -> "google/mustang/mustang:16/BP41.250725.006/12701944:user/release-keys"
            "ro.build.id" -> "BP41.250725.006"
            "ro.build.display.id" -> "BP41.250725.006"
            "ro.build.version.release" -> "16"
            "ro.build.version.sdk" -> "36"
            "ro.build.version.incremental" -> "12701944"
            "ro.build.version.security_patch" -> "2025-08-05"
            "ro.vendor.build.security_patch" -> "2025-08-05"
            "ro.build.tags" -> "release-keys"
            "ro.build.type" -> "user"
            "ro.hardware" -> "mustang"
            "ro.board.platform" -> "mustang"
            "ro.product.platform" -> "mustang"
            "ro.soc.model" -> "Tensor G5"
            "ro.soc.manufacturer" -> "Google"
            "ro.product.cpu.abi" -> "arm64-v8a"
            "ro.product.cpu.abilist" -> "arm64-v8a,armeabi-v7a,armeabi"
            "ro.product.cpu.abilist32" -> "armeabi-v7a,armeabi"
            "ro.product.cpu.abilist64" -> "arm64-v8a"
            else -> null
        }
    }
}
