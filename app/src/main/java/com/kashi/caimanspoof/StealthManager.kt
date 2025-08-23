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
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.*
import java.lang.reflect.Method

/**
 * Advanced stealth and anti-detection manager
 */
class StealthManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: StealthManager? = null
        
        fun getInstance(): StealthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StealthManager().also { INSTANCE = it }
            }
        }
        
        private const val TAG = "PixelSpoof"
        private var isDebugMode = false
        
        // Stealth logging - only logs when debug mode is enabled
        fun stealthLog(message: String) {
            if (isDebugMode) {
                XposedBridge.log("$TAG: $message")
            }
        }
    }
    
    private val hookedMethods = mutableSetOf<String>()
    private val detectionMethods = listOf(
        // Root detection methods
        "com.scottyab.rootbeer.RootBeer.isRooted",
        "com.scottyab.rootbeer.RootBeer.isRootedWithoutBusyBoxCheck",
        
        // Xposed detection
        "de.robv.android.xposed.XposedBridge.log",
        "de.robv.android.xposed.XposedHelpers.findClass",
        
        // System property access
        "android.os.SystemProperties.get",
        "java.lang.System.getProperty",
        
        // Package manager queries
        "android.app.ApplicationPackageManager.getInstalledPackages",
        "android.content.pm.PackageManager.getInstalledApplications",
        
        // Build field access
        "android.os.Build.getRadioVersion"
    )
    
    /**
     * Initialize stealth systems
     */
    fun initializeStealth(lpparam: XC_LoadPackage.LoadPackageParam, debugMode: Boolean = false) {
        isDebugMode = debugMode
        stealthLog("Initializing stealth systems for ${lpparam.packageName}")
        
        try {
            hideXposedPresence(lpparam)
            hideRootAccess(lpparam)
            hideSystemPropertyAccess(lpparam)
            hidePackageManagerQueries(lpparam)
            implementStackTraceCleaning(lpparam)
            stealthLog("Stealth initialization complete")
        } catch (e: Exception) {
            stealthLog("Stealth initialization failed: ${e.message}")
        }
    }
    
    /**
     * Hide Xposed framework presence
     */
    private fun hideXposedPresence(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook XposedBridge.log to prevent detection
            hookMethod(
                "de.robv.android.xposed.XposedBridge",
                "log",
                lpparam.classLoader,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // Silently consume Xposed logs
                        param.result = null
                    }
                }
            )
            
            // Hook XposedHelpers methods
            hookMethod(
                "de.robv.android.xposed.XposedHelpers",
                "findClass",
                lpparam.classLoader,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val className = param.args[0] as? String
                        if (className?.contains("xposed", ignoreCase = true) == true) {
                            param.throwable = ClassNotFoundException("Class not found")
                        }
                    }
                }
            )
            
            stealthLog("Xposed presence hidden")
        } catch (e: Exception) {
            stealthLog("Failed to hide Xposed presence: ${e.message}")
        }
    }
    
    /**
     * Hide root access and detection
     */
    private fun hideRootAccess(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook common root detection libraries
            val rootDetectionMethods = listOf(
                "com.scottyab.rootbeer.RootBeer" to "isRooted",
                "com.scottyab.rootbeer.RootBeer" to "isRootedWithoutBusyBoxCheck",
                "com.jaredrummler.android.device.DeviceName" to "isRooted"
            )
            
            rootDetectionMethods.forEach { (className, methodName) ->
                hookMethod(
                    className,
                    methodName,
                    lpparam.classLoader,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            param.result = false // Always return not rooted
                        }
                    }
                )
            }
            
            // Hook file existence checks for common root files
            hookMethod(
                "java.io.File",
                "exists",
                lpparam.classLoader,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val file = param.thisObject as java.io.File
                        val suspiciousPaths = listOf(
                            "/system/app/Superuser.apk",
                            "/system/xbin/su",
                            "/system/bin/su",
                            "/system/etc/init.d/99SuperSUDaemon",
                            "/dev/com.koushikdutta.superuser.daemon/",
                            "/system/app/SuperSU.apk"
                        )
                        
                        if (suspiciousPaths.any { file.absolutePath.contains(it) }) {
                            param.result = false
                        }
                    }
                }
            )
            
            stealthLog("Root access hidden")
        } catch (e: Exception) {
            stealthLog("Failed to hide root access: ${e.message}")
        }
    }
    
    /**
     * Intercept and modify system property access
     */
    private fun hideSystemPropertyAccess(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook SystemProperties.get
            hookMethod(
                "android.os.SystemProperties",
                "get",
                lpparam.classLoader,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val property = param.args[0] as? String ?: return
                        
                        // Intercept security-sensitive properties
                        when {
                            property.contains("ro.debuggable") -> param.result = "0"
                            property.contains("ro.secure") -> param.result = "1"
                            property.contains("ro.boot.verifiedbootstate") -> param.result = "green"
                            property.contains("ro.boot.flash.locked") -> param.result = "1"
                            property.contains("ro.boot.veritymode") -> param.result = "enforcing"
                            property.contains("ro.boot.warranty_bit") -> param.result = "0"
                        }
                    }
                }
            )
            
            // Hook System.getProperty
            hookMethod(
                "java.lang.System",
                "getProperty",
                lpparam.classLoader,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val property = param.args[0] as? String ?: return
                        
                        if (property.contains("xposed", ignoreCase = true) ||
                            property.contains("lsposed", ignoreCase = true)) {
                            param.result = null
                        }
                    }
                }
            )
            
            stealthLog("System property access intercepted")
        } catch (e: Exception) {
            stealthLog("Failed to intercept system properties: ${e.message}")
        }
    }
    
    /**
     * Hide suspicious packages from package manager queries
     */
    private fun hidePackageManagerQueries(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val suspiciousPackages = setOf(
                "de.robv.android.xposed.installer",
                "org.lsposed.manager",
                "com.topjohnwu.magisk",
                "eu.chainfire.supersu",
                "com.noshufou.android.su",
                "com.koushikdutta.superuser",
                "com.zachspong.temprootremovejb",
                "com.ramdroid.appquarantine"
            )
            
            // Hook getInstalledPackages
            hookMethod(
                "android.app.ApplicationPackageManager",
                "getInstalledPackages",
                lpparam.classLoader,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        @Suppress("UNCHECKED_CAST")
                        val packages = param.result as? MutableList<*>
                        packages?.removeAll { pkg ->
                            val packageName = try {
                                pkg?.javaClass?.getField("packageName")?.get(pkg) as? String
                            } catch (e: Exception) { null }
                            packageName in suspiciousPackages
                        }
                    }
                }
            )
            
            stealthLog("Package manager queries filtered")
        } catch (e: Exception) {
            stealthLog("Failed to filter package queries: ${e.message}")
        }
    }
    
    /**
     * Clean stack traces to remove Xposed signatures
     */
    private fun implementStackTraceCleaning(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            hookMethod(
                "java.lang.Throwable",
                "getStackTrace",
                lpparam.classLoader,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        @Suppress("UNCHECKED_CAST")
                        val stackTrace = param.result as? Array<StackTraceElement>
                        val cleanedTrace = stackTrace?.filter { element ->
                            !element.className.contains("xposed", ignoreCase = true) &&
                            !element.className.contains("lsposed", ignoreCase = true) &&
                            !element.methodName.contains("hook", ignoreCase = true)
                        }?.toTypedArray()
                        
                        if (cleanedTrace != null) {
                            param.result = cleanedTrace
                        }
                    }
                }
            )
            
            stealthLog("Stack trace cleaning implemented")
        } catch (e: Exception) {
            stealthLog("Failed to implement stack trace cleaning: ${e.message}")
        }
    }
    
    /**
     * Safely hook a method with error handling
     */
    private fun hookMethod(
        className: String,
        methodName: String,
        classLoader: ClassLoader,
        hook: XC_MethodHook
    ) {
        try {
            val clazz = XposedHelpers.findClass(className, classLoader)
            val methodKey = "$className.$methodName"
            
            if (methodKey !in hookedMethods) {
                XposedHelpers.findAndHookMethod(clazz, methodName, hook)
                hookedMethods.add(methodKey)
                stealthLog("Hooked: $methodKey")
            }
        } catch (e: Exception) {
            stealthLog("Failed to hook $className.$methodName: ${e.message}")
        }
    }
    
    /**
     * Enable or disable debug mode
     */
    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
        stealthLog("Debug mode ${if (enabled) "enabled" else "disabled"}")
    }
}
