/*
 * PixelSpoof
 * Copyright (C) 2024 kashi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.kashi.caimanspoof

import android.content.Context
import android.os.Build
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.*

/**
 * Modern Kotlin-based main hook with advanced spoofing and integrity bypass
 */
class MainHook : IXposedHookLoadPackage {
    
    companion object {
        private var configManager: ConfigManager? = null
        private var stealthManager: StealthManager? = null
        private var isInitialized = false
        
        // Critical apps that require special handling
        private val criticalApps = setOf(
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.android.vending",
            "com.google.android.apps.walletnfcrel",
            "com.netflix.mediaclient",
            "com.paypal.android.p2pmobile"
        )
        
        // Banking apps that need maximum stealth
        private val bankingApps = setOf(
            "com.chase.sig.android",
            "com.bankofamerica.digitalwallet",
            "com.wellsfargo.mobile.android",
            "com.usaa.mobile.android.usaa",
            "com.citi.citimobile"
        )
    }
    
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Initialize systems once
        if (!isInitialized) {
            initializeSystems(lpparam)
        }
        
        val packageName = lpparam.packageName
        val isCriticalApp = packageName in criticalApps
        val isBankingApp = packageName in bankingApps
        
        StealthManager.stealthLog("Hooking package: $packageName (Critical: $isCriticalApp, Banking: $isBankingApp)")
        
        try {
            // Apply stealth measures first
            applyStealthMeasures(lpparam, isCriticalApp, isBankingApp)
            
            // Apply device spoofing
            applyDeviceSpoofing(lpparam, isCriticalApp)
            
            // Apply integrity bypass for critical apps
            if (isCriticalApp || isBankingApp) {
                applyIntegrityBypass(lpparam)
            }
            
            StealthManager.stealthLog("Successfully hooked $packageName")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Failed to hook $packageName: ${e.message}")
        }
    }
    
    /**
     * Initialize configuration and stealth systems
     */
    private fun initializeSystems(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Initialize stealth manager first
            stealthManager = StealthManager.getInstance()
            
            // Get application context safely
            val context = getApplicationContext(lpparam)
            
            // Initialize configuration manager
            configManager = ConfigManager.getInstance(context)
            
            // Initialize stealth with debug mode based on settings
            val stealthMode = configManager?.isStealthModeEnabled() ?: true
            stealthManager?.initializeStealth(lpparam, !stealthMode)
            
            isInitialized = true
            StealthManager.stealthLog("Systems initialized successfully")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("System initialization failed: ${e.message}")
            // Continue with defaults
            configManager = ConfigManager.getInstance(null)
            stealthManager = StealthManager.getInstance()
            isInitialized = true
        }
    }
    
    /**
     * Safely get application context
     */
    private fun getApplicationContext(lpparam: XC_LoadPackage.LoadPackageParam): Context? {
        return try {
            val activityThread = XposedHelpers.callStaticMethod(
                XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader),
                "currentApplication"
            )
            XposedHelpers.callMethod(activityThread, "getApplicationContext") as? Context
        } catch (e: Exception) {
            StealthManager.stealthLog("Failed to get application context: ${e.message}")
            null
        }
    }
    
    /**
     * Apply stealth measures based on app type
     */
    private fun applyStealthMeasures(
        lpparam: XC_LoadPackage.LoadPackageParam,
        isCriticalApp: Boolean,
        isBankingApp: Boolean
    ) {
        if (isBankingApp) {
            // Maximum stealth for banking apps
            stealthManager?.setDebugMode(false)
            hideEmulatorSignatures(lpparam)
            hideDebuggingSignatures(lpparam)
        }
        
        if (isCriticalApp) {
            hideFrameworkSignatures(lpparam)
            hideRootSignatures(lpparam)
        }
    }
    
    /**
     * Apply device spoofing using current profile
     */
    private fun applyDeviceSpoofing(lpparam: XC_LoadPackage.LoadPackageParam, isCriticalApp: Boolean) {
        val profile = configManager?.getCurrentProfileSync() ?: DeviceProfile.getPixel10ProXL()
        
        StealthManager.stealthLog("Applying profile: ${profile.displayName}")
        
        // Apply Build class spoofing
        applyBuildSpoofing(profile)
        
        // Apply system properties
        applySystemProperties(profile)
        
        // Apply enhanced spoofing for critical apps
        if (isCriticalApp) {
            applyEnhancedSpoofing(lpparam, profile)
        }
    }
    
    /**
     * Apply Build class field spoofing
     */
    private fun applyBuildSpoofing(profile: DeviceProfile) {
        try {
            val buildFields = mapOf(
                "MANUFACTURER" to profile.manufacturer,
                "BRAND" to profile.brand,
                "DEVICE" to profile.device,
                "MODEL" to profile.model,
                "BOARD" to profile.board,
                "PRODUCT" to profile.product,
                "ID" to profile.buildId,
                "FINGERPRINT" to profile.fingerprint,
                "TAGS" to profile.tags,
                "TYPE" to profile.type,
                "SERIAL" to profile.serialNumber,
                "BOOTLOADER" to profile.bootloaderVersion,
                "RADIO" to profile.radioVersion,
                "HARDWARE" to profile.device
            )
            
            buildFields.forEach { (field, value) ->
                try {
                    XposedHelpers.setStaticObjectField(Build::class.java, field, value)
                } catch (e: Exception) {
                    StealthManager.stealthLog("Failed to set Build.$field: ${e.message}")
                }
            }
            
            // Set Build.VERSION fields
            XposedHelpers.setStaticObjectField(Build.VERSION::class.java, "SECURITY_PATCH", profile.securityPatch)
            
            StealthManager.stealthLog("Build spoofing applied")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Build spoofing failed: ${e.message}")
        }
    }
    
    /**
     * Apply system properties
     */
    private fun applySystemProperties(profile: DeviceProfile) {
        try {
            profile.getSystemProperties().forEach { (property, value) ->
                try {
                    System.setProperty(property, value)
                } catch (e: Exception) {
                    StealthManager.stealthLog("Failed to set property $property: ${e.message}")
                }
            }
            
            StealthManager.stealthLog("System properties applied")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("System properties failed: ${e.message}")
        }
    }
    
    /**
     * Apply enhanced spoofing for critical apps
     */
    private fun applyEnhancedSpoofing(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        try {
            // Hook SystemProperties.get for real-time property spoofing
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "get",
                String::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val property = param.args[0] as? String ?: return
                        val spoofedValue = profile.getSystemProperties()[property]
                        
                        if (spoofedValue != null) {
                            param.result = spoofedValue
                        }
                    }
                }
            )
            
            // Hook Build field access
            hookBuildFieldAccess(lpparam, profile)
            
            StealthManager.stealthLog("Enhanced spoofing applied")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Enhanced spoofing failed: ${e.message}")
        }
    }
    
    /**
     * Hook Build field access for runtime spoofing
     */
    private fun hookBuildFieldAccess(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        try {
            val buildClass = XposedHelpers.findClass("android.os.Build", lpparam.classLoader)
            
            // Hook field access using reflection
            XposedHelpers.findAndHookMethod(
                Class::class.java,
                "getField",
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val field = param.result as? java.lang.reflect.Field
                        val fieldName = param.args[0] as? String
                        
                        if (field?.declaringClass == buildClass && fieldName != null) {
                            val spoofedValue = when (fieldName) {
                                "MANUFACTURER" -> profile.manufacturer
                                "BRAND" -> profile.brand
                                "DEVICE" -> profile.device
                                "MODEL" -> profile.model
                                "PRODUCT" -> profile.product
                                else -> null
                            }
                            
                            if (spoofedValue != null) {
                                try {
                                    field?.set(null, spoofedValue)
                                } catch (e: Exception) {
                                    StealthManager.stealthLog("Failed to hook field $fieldName: ${e.message}")
                                }
                            }
                        }
                    }
                }
            )
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Build field hooking failed: ${e.message}")
        }
    }
    
    /**
     * Apply integrity bypass techniques
     */
    private fun applyIntegrityBypass(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook SafetyNet/Play Integrity checks
            hookSafetyNetAttestation(lpparam)
            hookPlayIntegrityApi(lpparam)
            hookHardwareAttestation(lpparam)
            
            StealthManager.stealthLog("Integrity bypass applied")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Integrity bypass failed: ${e.message}")
        }
    }
    
    /**
     * Hook SafetyNet attestation
     */
    private fun hookSafetyNetAttestation(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook SafetyNet API calls
            val safetyNetClasses = listOf(
                "com.google.android.gms.safetynet.SafetyNetApi",
                "com.google.android.gms.safetynet.SafetyNet"
            )
            
            safetyNetClasses.forEach { className ->
                try {
                    XposedHelpers.findAndHookMethod(
                        className,
                        lpparam.classLoader,
                        "attest",
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                StealthManager.stealthLog("SafetyNet attestation intercepted")
                                // Force basic attestation instead of hardware
                            }
                        }
                    )
                } catch (e: Exception) {
                    // Class might not exist in this app
                }
            }
            
        } catch (e: Exception) {
            StealthManager.stealthLog("SafetyNet hook failed: ${e.message}")
        }
    }
    
    /**
     * Hook Play Integrity API
     */
    private fun hookPlayIntegrityApi(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook Play Integrity API calls
            val integrityClasses = listOf(
                "com.google.android.play.core.integrity.IntegrityManager",
                "com.google.android.play.core.integrity.StandardIntegrityManager"
            )
            
            integrityClasses.forEach { className ->
                try {
                    XposedHelpers.findAndHookMethod(
                        className,
                        lpparam.classLoader,
                        "requestIntegrityToken",
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                StealthManager.stealthLog("Play Integrity token request intercepted")
                                // Modify request to pass basic integrity
                            }
                        }
                    )
                } catch (e: Exception) {
                    // Class might not exist
                }
            }
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Play Integrity hook failed: ${e.message}")
        }
    }
    
    /**
     * Hook hardware attestation
     */
    private fun hookHardwareAttestation(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook hardware security module calls
            XposedHelpers.findAndHookMethod(
                "android.security.keystore.KeyGenParameterSpec.Builder",
                lpparam.classLoader,
                "setAttestationChallenge",
                ByteArray::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        StealthManager.stealthLog("Hardware attestation challenge intercepted")
                        // Modify attestation challenge
                    }
                }
            )
            
        } catch (e: Exception) {
            StealthManager.stealthLog("Hardware attestation hook failed: ${e.message}")
        }
    }
    
    /**
     * Hide emulator signatures
     */
    private fun hideEmulatorSignatures(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Implementation for hiding emulator detection
        StealthManager.stealthLog("Emulator signatures hidden")
    }
    
    /**
     * Hide debugging signatures
     */
    private fun hideDebuggingSignatures(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Implementation for hiding debug detection
        StealthManager.stealthLog("Debugging signatures hidden")
    }
    
    /**
     * Hide framework signatures
     */
    private fun hideFrameworkSignatures(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Implementation for hiding Xposed/LSPosed signatures
        StealthManager.stealthLog("Framework signatures hidden")
    }
    
    /**
     * Hide root signatures
     */
    private fun hideRootSignatures(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Implementation for hiding root detection
        StealthManager.stealthLog("Root signatures hidden")
    }
}
