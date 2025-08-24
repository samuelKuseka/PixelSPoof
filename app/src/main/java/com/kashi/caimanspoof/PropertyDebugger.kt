package com.kashi.caimanspoof

import android.content.Context
import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Debug utility to test property spoofing
 */
class PropertyDebugger {
    
    companion object {
        fun testPropertyAccess(lpparam: XC_LoadPackage.LoadPackageParam) {
            try {
                StealthManager.stealthLog("🔍 TESTING PROPERTY ACCESS...")
                
                // Test SystemProperties directly
                val systemPropertiesClass = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader)
                
                // Try to get some common properties
                val testProperties = listOf(
                    "ro.product.model",
                    "ro.product.brand", 
                    "ro.product.manufacturer",
                    "ro.product.device",
                    "ro.build.fingerprint",
                    "ro.build.id"
                )
                
                testProperties.forEach { prop ->
                    try {
                        val value = XposedHelpers.callStaticMethod(
                            systemPropertiesClass,
                            "get",
                            prop
                        ) as String
                        StealthManager.stealthLog("🧪 TEST: $prop = $value")
                    } catch (e: Exception) {
                        StealthManager.stealthLog("🧪 TEST FAILED: $prop - ${e.message}")
                    }
                }
                
                // Test Build class access
                StealthManager.stealthLog("🧪 Testing Build class...")
                StealthManager.stealthLog("🧪 Build.MODEL = ${Build.MODEL}")
                StealthManager.stealthLog("🧪 Build.BRAND = ${Build.BRAND}")
                StealthManager.stealthLog("🧪 Build.MANUFACTURER = ${Build.MANUFACTURER}")
                StealthManager.stealthLog("🧪 Build.DEVICE = ${Build.DEVICE}")
                StealthManager.stealthLog("🧪 Build.FINGERPRINT = ${Build.FINGERPRINT}")
                
            } catch (e: Exception) {
                StealthManager.stealthLog("🧪 Property test failed: ${e.message}")
            }
        }
        
        /**
         * Hook application context creation to run tests
         */
        fun setupTestHooks(lpparam: XC_LoadPackage.LoadPackageParam) {
            try {
                // Hook Application onCreate to run our tests
                XposedHelpers.findAndHookMethod(
                    "android.app.Application",
                    lpparam.classLoader,
                    "onCreate",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            StealthManager.stealthLog("🧪 Application started, running property tests...")
                            testPropertyAccess(lpparam)
                        }
                    }
                )
                
                StealthManager.stealthLog("✅ Debug hooks installed")
                
            } catch (e: Exception) {
                StealthManager.stealthLog("❌ Debug hook setup failed: ${e.message}")
            }
        }
    }
}
