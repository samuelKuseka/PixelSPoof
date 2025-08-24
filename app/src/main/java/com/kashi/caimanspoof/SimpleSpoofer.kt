package com.kashi.caimanspoof

import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class SimpleSpoofer : IXposedHookLoadPackage {
    
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Skip problematic system apps that may crash with aggressive spoofing
            if (shouldSkipPackage(lpparam.packageName)) {
                XposedBridge.log("SimpleSpoofer: Skipping problematic package ${lpparam.packageName}")
                return
            }
            
            // Apply Pixel 10 Pro XL spoofing to compatible apps
            XposedBridge.log("SimpleSpoofer: Hooking package ${lpparam.packageName}")
            applyPixelSpoofing(lpparam)
        } catch (e: Exception) {
            XposedBridge.log("SimpleSpoofer error: ${e.message}")
        }
    }
    
    private fun shouldSkipPackage(packageName: String): Boolean {
        // Skip only specific problematic apps that crash with spoofing
        val problematicApps = listOf(
            "org.codeaurora.snapcam",           // Snapcam (causes crashes)
            "com.miui.screenrecorder",          // Screen recorder (crashes)
            "com.android.soundrecorder",        // Voice recorder (crashes)
            "com.xiaomi.screenrecorder",        // Xiaomi recorder variants
            "com.miui.camera",                  // MIUI camera
            "com.android.systemui",             // System UI (critical)
            "android"                           // System server (critical)
        )
        
        // Exact match only to avoid false positives
        return problematicApps.contains(packageName)
    }
    
    private fun applyPixelSpoofing(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hook Build class fields
        hookBuildFields(lpparam)
        
        // Hook SystemProperties.get calls
        hookSystemProperties(lpparam)
        
        // Hook PackageManager.hasSystemFeature - THE KEY METHOD for Pixel exclusives
        hookPackageManagerFeatures(lpparam)
    }
    
    private fun hookBuildFields(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val buildClass = XposedHelpers.findClass("android.os.Build", lpparam.classLoader)
            
            // Set all Build.* fields to Pixel 10 Pro XL values
            XposedHelpers.setStaticObjectField(buildClass, "MANUFACTURER", "Google")
            XposedHelpers.setStaticObjectField(buildClass, "BRAND", "google")
            XposedHelpers.setStaticObjectField(buildClass, "MODEL", "Pixel 10 Pro XL")
            XposedHelpers.setStaticObjectField(buildClass, "DEVICE", "mustang")
            XposedHelpers.setStaticObjectField(buildClass, "PRODUCT", "mustang_beta")
            XposedHelpers.setStaticObjectField(buildClass, "BOARD", "mustang")
            XposedHelpers.setStaticObjectField(buildClass, "HARDWARE", "mustang")
            XposedHelpers.setStaticObjectField(buildClass, "BOOTLOADER", "mustang-1.0-12701944")
            XposedHelpers.setStaticObjectField(buildClass, "SERIAL", "33001FDD40019Y")
            XposedHelpers.setStaticObjectField(buildClass, "FINGERPRINT", "google/mustang_beta/mustang:16/BP41.250725.006/12701944:user/release-keys")
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
            XposedHelpers.setStaticObjectField(versionClass, "CODENAME", "REL")
            // Add Pixel-specific version fields
            XposedHelpers.setStaticObjectField(versionClass, "BASE_OS", "")
            XposedHelpers.setStaticIntField(versionClass, "PREVIEW_SDK_INT", 0)
            // Set security patch AFTER other fields to ensure it doesn't get overwritten
            XposedHelpers.setStaticObjectField(versionClass, "SECURITY_PATCH", "2025-08-05")
            
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
                        // Log SoC-related requests for debugging
                        if (key.contains("soc") || key.contains("chip") || key.contains("cpu")) {
                            XposedBridge.log("SimpleSpoofer: SoC-related request: $key = ${param.result}")
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
            
            // Add additional hook specifically for security patch to prevent it being overridden
            try {
                XposedHelpers.findAndHookMethod(
                    "android.os.SystemProperties",
                    lpparam.classLoader,
                    "get",
                    String::class.java,
                    String::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val key = param.args[0] as String
                            if (key == "ro.build.version.security_patch" || key == "ro.vendor.build.security_patch") {
                                param.result = "2025-08-05"
                                XposedBridge.log("SimpleSpoofer: FORCED security patch = 2025-08-05")
                            } else if (key == "ro.build.description") {
                                param.result = "mustang_beta-user 16 BP41.250725.006 12701944 release-keys"
                                XposedBridge.log("SimpleSpoofer: FORCED description = mustang_beta-user")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                XposedBridge.log("SimpleSpoofer: Error adding security patch force hook: ${e.message}")
            }
            
        } catch (e: Exception) {
            XposedBridge.log("SimpleSpoofer SystemProperties hook error: ${e.message}")
        }
    }
    
    private fun getSystemPropertyValue(key: String): String? {
        // Don't spoof security-critical properties that might affect device certification
        val securityProperties = listOf(
            "ro.boot.vbmeta.device_state",
            "ro.boot.verifiedbootstate",
            "ro.boot.veritymode", 
            "ro.boot.flash.locked",
            "ro.debuggable",
            "ro.secure",
            "ro.adb.secure",
            "ro.build.selinux",
            "ro.boot.selinux"
        )
        
        if (securityProperties.contains(key)) {
            return null // Don't modify security properties
        }
        
        return when (key) {
            "ro.product.manufacturer" -> "Google"
            "ro.product.brand" -> "google"
            "ro.product.model" -> "Pixel 10 Pro XL"
            "ro.product.device" -> "mustang"
            "ro.product.name" -> "mustang_beta"
            "ro.product.board" -> "mustang"
            "ro.build.fingerprint" -> "google/mustang_beta/mustang:16/BP41.250725.006/12701944:user/release-keys"
            "ro.build.id" -> "BP41.250725.006"
            "ro.build.display.id" -> "BP41.250725.006"
            "ro.build.version.release" -> "16"
            "ro.build.version.sdk" -> "36"
            "ro.build.version.incremental" -> "12701944"
            "ro.build.version.security_patch" -> "2025-08-05"
            "ro.vendor.build.security_patch" -> "2025-08-05"
            "ro.product.first_api_level" -> "36"
            "ro.build.tags" -> "release-keys"
            "ro.build.type" -> "user"
            "ro.build.description" -> "mustang_beta-user 16 BP41.250725.006 12701944 release-keys"
            "ro.hardware" -> "mustang"
            "ro.board.platform" -> "mustang"
            "ro.product.platform" -> "mustang"
            "ro.bootloader" -> "mustang-1.0-12701944"
            "ro.bootmode" -> "normal"
            "ro.boot.bootloader" -> "mustang-1.0-12701944"
            "ro.serialno" -> "33001FDD40019Y"
            "ro.boot.serialno" -> "33001FDD40019Y"
            "ro.soc.model" -> "Tensor G5"
            "ro.soc.manufacturer" -> "Google"
            "ro.boot.hardware" -> "mustang"
            "ro.boot.hardware.sku" -> "mustang"
            "ro.product.hardware.sku" -> "mustang"
            "ro.boot.cpuid" -> "0x80481100"  // Tensor G5 CPU ID
            "persist.radio.chipset_id" -> "1"
            "ro.vendor.soc.manufacturer" -> "Google"
            "ro.vendor.soc.model" -> "Tensor G5"
            "ro.system.soc.manufacturer" -> "Google"
            "ro.system.soc.model" -> "Tensor G5"
            "ro.chipname" -> "Tensor G5"
            "ro.board.chipset" -> "Tensor G5"
            "ro.soc.platform" -> "mustang"
            "ro.pixel.device" -> "true"
            "ro.config.enable_pixel_features" -> "true"
            "ro.hardware.chipset" -> "tensor"
            "ro.config.pixel_2017" -> "true"
            "ro.config.pixel_2018" -> "true"
            "ro.config.pixel_2019" -> "true"
            "ro.config.pixel_2020" -> "true"
            "ro.config.pixel_2021" -> "true"
            "ro.config.pixel_2022" -> "true"
            "ro.config.pixel_2023" -> "true"
            "ro.config.pixel_2024" -> "true"
            "ro.config.pixel_2025" -> "true"
            "ro.opa.eligible_device" -> "true"
            "ro.com.google.gmsversion" -> "16_202508"
            "ro.config.ringtone" -> "Pixel.ogg"
            "ro.config.notification_sound" -> "Chime.ogg"
            "ro.config.alarm_alert" -> "Flow.ogg"
            "ro.google.camera.hal" -> "true"
            "ro.camera.req.fmq.size" -> "268435456"
            "ro.hardware.camera" -> "google"
            "ro.hardware.fingerprint" -> "goodix"
            "ro.build.characteristics" -> "nosdcard"
            "persist.vendor.camera.privapp.list" -> "com.google.camera"
            "ro.config.face_unlock_service" -> "true"
            "ro.vendor.camera.extensions.package" -> "com.google.camera"
            "ro.vendor.camera.extensions.service" -> "com.google.camera.extensions.service.PixelExtensions"
            "ro.config.adaptive_brightness" -> "true"
            "ro.config.auto_brightness_type" -> "1"
            "ro.telephony.call_ring.multiple" -> "false"
            "ro.config.vc_call_vol_steps" -> "7"
            "ro.config.media_vol_steps" -> "25"
            "persist.vendor.dpmhalservice.enable" -> "1"
            "ro.config.always_on_display" -> "true"
            "ro.config.ambient_display" -> "true"
            "persist.vendor.radio.enable_voicecall" -> "1"
            "ro.config.carrier_enabled" -> "true"
            "ro.config.low_ram" -> "false"
            "ro.vendor.qti.va_aosp.support" -> "1"
            "ro.vendor.use_data_netmgrd" -> "true"
            "ro.config.enable_quicksettings" -> "true"
            "ro.config.enable_emergency_call" -> "true"
            "ro.config.google_battery_saver" -> "true"
            "ro.config.google_assistant" -> "true"
            "ro.config.google_camera" -> "true"
            "ro.config.google_photos_backup" -> "unlimited"
            "ro.config.aicore_enabled" -> "true"
            "ro.config.live_translate" -> "true"
            "ro.config.call_screen" -> "true"
            "ro.config.voice_translate" -> "true"
            "ro.config.smart_compose" -> "true"
            "ro.config.magic_cue" -> "true"
            "ro.config.ai_camera_coach" -> "true"
            "ro.build.expect.bootloader" -> "mustang-1.0-12701944"
            "ro.build.expect.baseband" -> "g5150-105671-250725-B-12701944"
            "persist.vendor.aicore.enabled" -> "1"
            "ro.vendor.aicore.version" -> "16.0"
            "ro.system.aicore.enabled" -> "true"
            "ro.product.cpu.abi" -> "arm64-v8a"
            "ro.product.cpu.abilist" -> "arm64-v8a,armeabi-v7a,armeabi"
            "ro.product.cpu.abilist32" -> "armeabi-v7a,armeabi"
            "ro.product.cpu.abilist64" -> "arm64-v8a"
            else -> null
        }
    }
    
    private fun hookPackageManagerFeatures(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook the key method that Google apps use to detect Pixel features
            XposedHelpers.findAndHookMethod(
                "android.app.ApplicationPackageManager",
                lpparam.classLoader,
                "hasSystemFeature",
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val featureName = param.args[0] as String
                        if (isPixelSystemFeature(featureName)) {
                            param.result = true
                            XposedBridge.log("SimpleSpoofer: Enabled Pixel feature: $featureName")
                        }
                    }
                }
            )
            
            XposedBridge.log("SimpleSpoofer: PackageManager.hasSystemFeature hook installed for ${lpparam.packageName}")
        } catch (e: Exception) {
            XposedBridge.log("SimpleSpoofer PackageManager hook error: ${e.message}")
        }
    }
    
    private fun isPixelSystemFeature(featureName: String): Boolean {
        return when (featureName) {
            // Core Pixel features that Google apps check
            "com.google.android.feature.PIXEL_EXPERIENCE" -> true
            "com.google.android.feature.TURBO_PRELOADING" -> true
            // AICore and AI features - CRUCIAL for Pixel AI exclusives
            "com.google.android.feature.AICORE" -> true
            "com.google.android.feature.LIVE_TRANSLATE" -> true
            "com.google.android.feature.CALL_SCREEN" -> true
            "com.google.android.feature.VOICE_TRANSLATE" -> true
            "com.google.android.feature.SMART_COMPOSE" -> true
            "com.google.android.feature.MAGIC_CUE" -> true
            "com.google.android.feature.AI_CAMERA_COACH" -> true
            "android.software.device_admin" -> true
            "android.software.managed_users" -> true
            "android.software.verified_boot" -> true
            "android.hardware.camera.front" -> true
            "android.hardware.camera.autofocus" -> true
            "android.hardware.camera.flash" -> true
            "android.software.picture_in_picture" -> true
            "android.software.activities_on_secondary_displays" -> true
            "android.software.backup" -> true
            "android.software.secure_lock_screen" -> true
            else -> false
        }
    }
}
