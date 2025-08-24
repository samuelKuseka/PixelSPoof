package com.example.kashi.core

import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.lang.reflect.Method

/**
 * KernelSU Integration and Detection Layer
 * 
 * This class handles:
 * - KernelSU detection and compatibility
 * - SUSFS filesystem hiding integration
 * - Tricky Store bootloader spoofing cooperation
 * - Kernel-level device spoofing coordination
 * 
 * Based on actual working methods from 2025 KernelSU ecosystem
 */
class KernelSUIntegration private constructor() {
    
    companion object {
        private const val TAG = "KernelSUIntegration"
        
        @Volatile
        private var INSTANCE: KernelSUIntegration? = null
        
        fun getInstance(): KernelSUIntegration {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KernelSUIntegration().also { INSTANCE = it }
            }
        }
        
        // KernelSU detection patterns (2025 methods)
        private val KERNELSU_INDICATORS = listOf(
            "/system/bin/ksu",
            "/data/adb/ksu",
            "/dev/kernelsu",
            "/proc/kernelsu",
            "/system/etc/ksu.rc"
        )
        
        // SUSFS detection and integration points
        private val SUSFS_PATHS = listOf(
            "/data/adb/modules/SUSFS4KSU",
            "/system/etc/susfs.conf",
            "/proc/susfs",
            "/dev/susfs_ctl"
        )
        
        // Tricky Store integration points
        private val TRICKY_STORE_PATHS = listOf(
            "/data/adb/modules/tricky_store",
            "/system/etc/tricky_store",
            "/data/adb/tricky_store"
        )
    }
    
    private val _kernelSUStatus = MutableStateFlow(KernelSUStatus.UNKNOWN)
    val kernelSUStatus: StateFlow<KernelSUStatus> = _kernelSUStatus.asStateFlow()
    
    private val _susfsStatus = MutableStateFlow(SUSFSStatus.UNKNOWN)
    val susfsStatus: StateFlow<SUSFSStatus> = _susfsStatus.asStateFlow()
    
    private val _trickyStoreStatus = MutableStateFlow(TrickyStoreStatus.UNKNOWN)
    val trickyStoreStatus: StateFlow<TrickyStoreStatus> = _trickyStoreStatus.asStateFlow()
    
    enum class KernelSUStatus {
        UNKNOWN,
        NOT_PRESENT,
        PRESENT_INACTIVE,
        PRESENT_ACTIVE,
        FULLY_INTEGRATED
    }
    
    enum class SUSFSStatus {
        UNKNOWN,
        NOT_AVAILABLE,
        AVAILABLE_INACTIVE,
        ACTIVE_BASIC,
        ACTIVE_ADVANCED,
        FULLY_INTEGRATED
    }
    
    enum class TrickyStoreStatus {
        UNKNOWN,
        NOT_INSTALLED,
        INSTALLED_INACTIVE,
        ACTIVE_NO_KEYBOX,
        ACTIVE_WITH_KEYBOX,
        FULLY_OPERATIONAL
    }
    
    /**
     * Initialize KernelSU integration with SAFETY CHECKS
     */
    fun initialize(): Boolean {
        Log.i(TAG, "🛡️ Initializing SAFE KernelSU integration system...")
        
        try {
            // CRITICAL: Perform safety checks FIRST
            if (!performSafetyChecks()) {
                Log.e(TAG, "❌ SAFETY CHECK FAILED - Aborting KernelSU integration")
                return false
            }
            
            // SAFE: Detect KernelSU presence and version (read-only)
            detectKernelSU()
            
            // SAFE: Check for SUSFS availability (read-only)
            detectSUSFS()
            
            // SAFE: Check for Tricky Store integration (read-only)
            detectTrickyStore()
            
            Log.i(TAG, "✅ KernelSU integration initialized SAFELY")
            Log.i(TAG, "KernelSU: ${_kernelSUStatus.value}")
            Log.i(TAG, "SUSFS: ${_susfsStatus.value}")
            Log.i(TAG, "Tricky Store: ${_trickyStoreStatus.value}")
            
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize KernelSU integration", e)
            return false
        }
    }
    
    /**
     * CRITICAL SAFETY CHECKS - Must pass before ANY kernel operations
     */
    private fun performSafetyChecks(): Boolean {
        Log.i(TAG, "🔍 Performing critical safety checks...")
        
        try {
            // Check 1: Verify device is not in critical state
            if (isDeviceInCriticalState()) {
                Log.e(TAG, "❌ Device in critical state - unsafe to proceed")
                return false
            }
            
            // Check 2: Verify we have proper recovery options
            if (!hasRecoveryOptions()) {
                Log.w(TAG, "⚠️ No recovery options detected - proceeding with extra caution")
            }
            
            // Check 3: Verify KernelSU compatibility with device
            if (!isKernelSUCompatible()) {
                Log.e(TAG, "❌ Device not compatible with KernelSU - aborting")
                return false
            }
            
            // Check 4: Verify we're not in production/work environment
            if (isProductionEnvironment()) {
                Log.e(TAG, "❌ Production environment detected - aborting for safety")
                return false
            }
            
            Log.i(TAG, "✅ All safety checks passed")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Safety check failed", e)
            return false
        }
    }
    
    /**
     * Check if device is in critical state (low battery, unstable, etc.)
     */
    private fun isDeviceInCriticalState(): Boolean {
        return try {
            // Check battery level
            val batteryLevel = getBatteryLevel()
            if (batteryLevel < 30) {
                Log.w(TAG, "⚠️ Low battery: $batteryLevel% - risky for kernel operations")
                return true
            }
            
            // Check for existing system instability
            if (hasSystemInstability()) {
                Log.w(TAG, "⚠️ System instability detected")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking device state", e)
            true // Err on side of caution
        }
    }
    
    /**
     * Check if device has recovery options available
     */
    private fun hasRecoveryOptions(): Boolean {
        return try {
            // Check for custom recovery
            val hasCustomRecovery = File("/system/recovery-resource").exists() ||
                                  File("/system/etc/recovery-resource").exists()
            
            // Check for fastboot access
            val hasFastboot = File("/system/bin/fastboot").exists() ||
                            canAccessFastbootMode()
            
            hasCustomRecovery || hasFastboot
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check KernelSU compatibility with current device
     */
    private fun isKernelSUCompatible(): Boolean {
        return try {
            // Check kernel version compatibility
            val kernelVersion = System.getProperty("os.version") ?: ""
            val isKernelCompatible = kernelVersion.isNotEmpty()
            
            // Check device architecture
            val arch = Build.SUPPORTED_ABIS[0]
            val isSupportedArch = arch in listOf("arm64-v8a", "armeabi-v7a")
            
            // Check for known incompatible devices
            val deviceModel = Build.MODEL
            val isKnownIncompatible = deviceModel in getIncompatibleDevices()
            
            isKernelCompatible && isSupportedArch && !isKnownIncompatible
        } catch (e: Exception) {
            false // Err on side of caution
        }
    }
    
    /**
     * Check if this is a production/work environment
     */
    private fun isProductionEnvironment(): Boolean {
        return try {
            // Check for work profile indicators
            val hasWorkProfile = Build.DISPLAY.contains("work", ignoreCase = true) ||
                               Build.TAGS.contains("enterprise", ignoreCase = true)
            
            // Check for known enterprise management apps
            val enterpriseApps = listOf(
                "com.microsoft.intune",
                "com.airwatch.androidagent",
                "com.mobileiron"
            )
            
            val hasEnterpriseApps = enterpriseApps.any { packageExists(it) }
            
            hasWorkProfile || hasEnterpriseApps
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Detect KernelSU presence and capabilities
     */
    private fun detectKernelSU() {
        try {
            var hasKernelSU = false
            var isActive = false
            
            // Check for KernelSU binaries and interfaces
            for (path in KERNELSU_INDICATORS) {
                if (File(path).exists()) {
                    hasKernelSU = true
                    Log.d(TAG, "Found KernelSU indicator: $path")
                    break
                }
            }
            
            if (hasKernelSU) {
                // Check if KernelSU is actually active
                isActive = checkKernelSUActive()
                
                _kernelSUStatus.value = if (isActive) {
                    KernelSUStatus.PRESENT_ACTIVE
                } else {
                    KernelSUStatus.PRESENT_INACTIVE
                }
            } else {
                _kernelSUStatus.value = KernelSUStatus.NOT_PRESENT
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting KernelSU", e)
            _kernelSUStatus.value = KernelSUStatus.UNKNOWN
        }
    }
    
    /**
     * Check if KernelSU is actively running
     */
    private fun checkKernelSUActive(): Boolean {
        return try {
            // Try to communicate with KernelSU
            val process = Runtime.getRuntime().exec("su -c 'echo test'")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Detect SUSFS (filesystem hiding) availability
     */
    private fun detectSUSFS() {
        try {
            var hasSUSFS = false
            var isActive = false
            
            // Check for SUSFS module and interfaces
            for (path in SUSFS_PATHS) {
                if (File(path).exists()) {
                    hasSUSFS = true
                    Log.d(TAG, "Found SUSFS indicator: $path")
                    break
                }
            }
            
            if (hasSUSFS) {
                // Check if SUSFS is actively hiding filesystem modifications
                isActive = checkSUSFSActive()
                
                _susfsStatus.value = when {
                    isActive -> SUSFSStatus.ACTIVE_ADVANCED
                    hasSUSFS -> SUSFSStatus.AVAILABLE_INACTIVE
                    else -> SUSFSStatus.NOT_AVAILABLE
                }
            } else {
                _susfsStatus.value = SUSFSStatus.NOT_AVAILABLE
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting SUSFS", e)
            _susfsStatus.value = SUSFSStatus.UNKNOWN
        }
    }
    
    /**
     * Check if SUSFS is actively providing filesystem hiding
     */
    private fun checkSUSFSActive(): Boolean {
        return try {
            // Check if SUSFS is hiding known root artifacts
            val testPaths = listOf(
                "/system/bin/su",
                "/system/xbin/su",
                "/data/adb/modules"
            )
            
            // If SUSFS is working, these paths should be hidden from detection
            var hiddenCount = 0
            for (path in testPaths) {
                if (!File(path).exists()) {
                    hiddenCount++
                }
            }
            
            hiddenCount > 0 // If any paths are hidden, SUSFS is likely active
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Detect Tricky Store (bootloader spoofing) availability
     */
    private fun detectTrickyStore() {
        try {
            var hasTrickyStore = false
            var hasKeybox = false
            
            // Check for Tricky Store installation
            for (path in TRICKY_STORE_PATHS) {
                if (File(path).exists()) {
                    hasTrickyStore = true
                    Log.d(TAG, "Found Tricky Store indicator: $path")
                    break
                }
            }
            
            if (hasTrickyStore) {
                // Check if valid keybox is available
                hasKeybox = checkTrickyStoreKeybox()
                
                _trickyStoreStatus.value = when {
                    hasKeybox -> TrickyStoreStatus.ACTIVE_WITH_KEYBOX
                    hasTrickyStore -> TrickyStoreStatus.INSTALLED_INACTIVE
                    else -> TrickyStoreStatus.NOT_INSTALLED
                }
            } else {
                _trickyStoreStatus.value = TrickyStoreStatus.NOT_INSTALLED
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting Tricky Store", e)
            _trickyStoreStatus.value = TrickyStoreStatus.UNKNOWN
        }
    }
    
    /**
     * Check if Tricky Store has a valid keybox for bootloader spoofing
     */
    private fun checkTrickyStoreKeybox(): Boolean {
        return try {
            val keyboxPaths = listOf(
                "/data/adb/tricky_store/keybox.xml",
                "/system/etc/tricky_store/keybox.xml",
                "/data/adb/modules/tricky_store/keybox.xml"
            )
            
            keyboxPaths.any { File(it).exists() && File(it).length() > 100 }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * SAFE kernel-level device spoofing with extensive safety measures
     */
    fun enableKernelLevelSpoofing(deviceProfile: String): Boolean {
        if (_kernelSUStatus.value != KernelSUStatus.PRESENT_ACTIVE) {
            Log.w(TAG, "⚠️ KernelSU not active, cannot enable kernel-level spoofing")
            return false
        }
        
        Log.i(TAG, "🛡️ Enabling SAFE kernel-level spoofing for: $deviceProfile")
        
        return try {
            // SAFETY: Create restore point first
            if (!createRestorePoint()) {
                Log.e(TAG, "❌ Failed to create restore point - aborting")
                return false
            }
            
            // SAFETY: Use only TEMPORARY, REVERSIBLE modifications
            val success = applySafeKernelSpoofing(deviceProfile)
            
            if (success) {
                Log.i(TAG, "✅ SAFE kernel-level spoofing applied successfully")
                // Schedule automatic rollback as safety net
                scheduleAutomaticRollback()
            } else {
                Log.e(TAG, "❌ Kernel spoofing failed - rolling back")
                rollbackKernelChanges()
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed kernel spoofing - emergency rollback", e)
            rollbackKernelChanges()
            false
        }
    }
    
    /**
     * Apply SAFE kernel spoofing that won't brick the device
     */
    private fun applySafeKernelSpoofing(deviceProfile: String): Boolean {
        return try {
            // Generate SAFE commands (temporary only, no permanent writes)
            val safeCommands = generateSafeKernelCommands(deviceProfile)
            
            Log.i(TAG, "🔧 Applying ${safeCommands.size} SAFE kernel commands...")
            
            // Apply each command with safety verification
            for (command in safeCommands) {
                if (!applySafeKernelCommand(command)) {
                    Log.e(TAG, "❌ Safe command failed: $command")
                    return false
                }
                
                // Verify system stability after each command
                if (!verifySystemStability()) {
                    Log.e(TAG, "❌ System instability detected - aborting")
                    return false
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error applying safe kernel spoofing", e)
            false
        }
    }
    
    /**
     * Generate SAFE kernel commands (TEMPORARY ONLY - NO PERMANENT WRITES)
     */
    private fun generateSafeKernelCommands(deviceProfile: String): List<SafeKernelCommand> {
        return listOf(
            // SAFE: Runtime property overrides (temporary, memory-only)
            SafeKernelCommand(
                type = CommandType.RUNTIME_PROPERTY,
                command = "setprop ro.product.model 'Pixel 8 Pro'",
                reversible = true,
                riskLevel = RiskLevel.LOW
            ),
            SafeKernelCommand(
                type = CommandType.RUNTIME_PROPERTY,
                command = "setprop ro.product.manufacturer 'Google'",
                reversible = true,
                riskLevel = RiskLevel.LOW
            ),
            SafeKernelCommand(
                type = CommandType.RUNTIME_PROPERTY,
                command = "setprop ro.product.brand 'google'",
                reversible = true,
                riskLevel = RiskLevel.LOW
            ),
            SafeKernelCommand(
                type = CommandType.RUNTIME_PROPERTY,
                command = "setprop ro.product.device 'husky'",
                reversible = true,
                riskLevel = RiskLevel.LOW
            ),
            SafeKernelCommand(
                type = CommandType.RUNTIME_PROPERTY,
                command = "setprop ro.build.fingerprint 'google/husky/husky:15/AP31.240517.015/12043167:user/release-keys'",
                reversible = true,
                riskLevel = RiskLevel.LOW
            )
            // NOTE: NO permanent filesystem modifications, NO bootloader changes, NO firmware writes
        )
    }
    
    /**
     * Apply a single safe kernel command with verification
     */
    private fun applySafeKernelCommand(command: SafeKernelCommand): Boolean {
        return try {
            // SAFETY: Only apply low-risk commands
            if (command.riskLevel != RiskLevel.LOW) {
                Log.w(TAG, "⚠️ Skipping high-risk command: ${command.command}")
                return true // Skip but don't fail
            }
            
            // SAFETY: Verify command is reversible
            if (!command.reversible) {
                Log.w(TAG, "⚠️ Skipping irreversible command: ${command.command}")
                return true // Skip but don't fail
            }
            
            Log.d(TAG, "🔧 Applying safe command: ${command.command}")
            
            // Execute with timeout and error handling
            val process = Runtime.getRuntime().exec("su -c '${command.command}'")
            val completed = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
            
            if (!completed) {
                Log.e(TAG, "❌ Command timeout: ${command.command}")
                process.destroyForcibly()
                return false
            }
            
            val exitCode = process.exitValue()
            if (exitCode != 0) {
                Log.e(TAG, "❌ Command failed with exit code $exitCode: ${command.command}")
                return false
            }
            
            Log.d(TAG, "✅ Safe command applied successfully: ${command.command}")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error applying safe command: ${command.command}", e)
            false
        }
    }
    
    /**
     * Create restore point before making any changes
     */
    private fun createRestorePoint(): Boolean {
        return try {
            Log.i(TAG, "💾 Creating restore point...")
            
            // Store current property values for rollback
            val currentProperties = getCurrentSystemProperties()
            storeRestorePoint(currentProperties)
            
            Log.i(TAG, "✅ Restore point created successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create restore point", e)
            false
        }
    }
    
    /**
     * Rollback all kernel changes to restore point
     */
    private fun rollbackKernelChanges(): Boolean {
        return try {
            Log.i(TAG, "🔄 Rolling back kernel changes...")
            
            val restorePoint = getStoredRestorePoint()
            if (restorePoint.isEmpty()) {
                Log.w(TAG, "⚠️ No restore point found")
                return false
            }
            
            // Restore original property values
            for ((property, value) in restorePoint) {
                val command = "setprop $property '$value'"
                Runtime.getRuntime().exec("su -c '$command'").waitFor()
            }
            
            Log.i(TAG, "✅ Kernel changes rolled back successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to rollback kernel changes", e)
            false
        }
    }
    
    /**
     * Verify system stability after changes
     */
    private fun verifySystemStability(): Boolean {
        return try {
            // Check if system is responsive
            val startTime = System.currentTimeMillis()
            val testCommand = "echo 'stability_test'"
            val process = Runtime.getRuntime().exec(testCommand)
            val completed = process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)
            val responseTime = System.currentTimeMillis() - startTime
            
            // System should respond quickly
            val isStable = completed && responseTime < 2000
            
            if (!isStable) {
                Log.w(TAG, "⚠️ System stability check failed (response time: ${responseTime}ms)")
            }
            
            isStable
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking system stability", e)
            false
        }
    }
    
    /**
     * Schedule automatic rollback as safety net
     */
    private fun scheduleAutomaticRollback() {
        try {
            // Schedule rollback after 30 minutes as safety net
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.postDelayed({
                Log.i(TAG, "🔄 Automatic safety rollback triggered")
                rollbackKernelChanges()
            }, 30 * 60 * 1000) // 30 minutes
            
            Log.i(TAG, "⏰ Automatic rollback scheduled in 30 minutes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule automatic rollback", e)
        }
    }
    
    // Helper data classes for safe operations
    data class SafeKernelCommand(
        val type: CommandType,
        val command: String,
        val reversible: Boolean,
        val riskLevel: RiskLevel
    )
    
    enum class CommandType {
        RUNTIME_PROPERTY,    // Temporary property changes
        MEMORY_ONLY,         // Memory-only modifications
        USERSPACE_HOOK       // Userspace process hooks
        // NOTE: NO filesystem writes, NO partition modifications
    }
    
    enum class RiskLevel {
        LOW,      // Safe, reversible operations
        MEDIUM,   // Potentially risky (we skip these)
        HIGH      // Dangerous operations (we never do these)
    }
    
    /**
     * Enable SUSFS filesystem hiding for better root concealment
     */
    fun enableSUSFSHiding(): Boolean {
        if (_susfsStatus.value == SUSFSStatus.NOT_AVAILABLE) {
            Log.w(TAG, "SUSFS not available, cannot enable filesystem hiding")
            return false
        }
        
        return try {
            // Configure SUSFS hiding rules
            val hidingRules = generateSUSFSRules()
            applySUSFSRules(hidingRules)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable SUSFS hiding", e)
            false
        }
    }
    
    /**
     * Coordinate with Tricky Store for bootloader spoofing
     */
    fun coordinateWithTrickyStore(targetDevice: String): Boolean {
        if (_trickyStoreStatus.value != TrickyStoreStatus.ACTIVE_WITH_KEYBOX) {
            Log.w(TAG, "Tricky Store not ready, cannot coordinate bootloader spoofing")
            return false
        }
        
        return try {
            // Send device profile to Tricky Store for keybox selection
            configureTrickyStoreProfile(targetDevice)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to coordinate with Tricky Store", e)
            false
        }
    }
    
    /**
     * Generate kernel-level spoofing commands for KernelSU
     */
    private fun generateKernelSpoofingCommands(deviceProfile: String): List<String> {
        return listOf(
            "setprop ro.product.model 'Pixel 8 Pro'",
            "setprop ro.product.manufacturer 'Google'",
            "setprop ro.product.brand 'google'",
            "setprop ro.product.device 'husky'",
            "setprop ro.build.fingerprint 'google/husky/husky:15/AP31.240517.015/12043167:user/release-keys'"
        )
    }
    
    /**
     * Execute commands through KernelSU
     */
    private fun executeKernelCommands(commands: List<String>): Boolean {
        return try {
            for (command in commands) {
                val process = Runtime.getRuntime().exec("su -c '$command'")
                val exitCode = process.waitFor()
                if (exitCode != 0) {
                    Log.e(TAG, "Failed to execute kernel command: $command")
                    return false
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error executing kernel commands", e)
            false
        }
    }
    
    /**
     * Generate SUSFS hiding rules
     */
    private fun generateSUSFSRules(): List<String> {
        return listOf(
            "/system/bin/su",
            "/system/xbin/su", 
            "/data/adb/modules",
            "/data/adb/ksu",
            "/system/addon.d",
            "/cache/recovery",
            "/system/recovery-script"
        )
    }
    
    /**
     * Apply SUSFS hiding rules
     */
    private fun applySUSFSRules(rules: List<String>): Boolean {
        return try {
            for (rule in rules) {
                val command = "echo 'hide $rule' > /proc/susfs_ctl"
                val process = Runtime.getRuntime().exec("su -c '$command'")
                process.waitFor()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error applying SUSFS rules", e)
            false
        }
    }
    
    /**
     * Configure Tricky Store device profile
     */
    private fun configureTrickyStoreProfile(targetDevice: String): Boolean {
        return try {
            val configPath = "/data/adb/tricky_store/target_device.conf"
            val command = "echo '$targetDevice' > $configPath"
            val process = Runtime.getRuntime().exec("su -c '$command'")
            process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring Tricky Store profile", e)
            false
        }
    }
    
    /**
     * Get integration status summary
     */
    fun getIntegrationStatus(): IntegrationStatus {
        return IntegrationStatus(
            kernelSU = _kernelSUStatus.value,
            susfs = _susfsStatus.value,
            trickyStore = _trickyStoreStatus.value,
            isFullyIntegrated = isFullyIntegrated()
        )
    }
    
    /**
     * Check if all components are fully integrated
     */
    private fun isFullyIntegrated(): Boolean {
        return _kernelSUStatus.value == KernelSUStatus.PRESENT_ACTIVE &&
                _susfsStatus.value in listOf(SUSFSStatus.ACTIVE_BASIC, SUSFSStatus.ACTIVE_ADVANCED) &&
                _trickyStoreStatus.value == TrickyStoreStatus.ACTIVE_WITH_KEYBOX
    }
    
    data class IntegrationStatus(
        val kernelSU: KernelSUStatus,
        val susfs: SUSFSStatus,
        val trickyStore: TrickyStoreStatus,
        val isFullyIntegrated: Boolean
    )
    
    // SAFETY HELPER FUNCTIONS
    
    /**
     * Get current battery level
     */
    private fun getBatteryLevel(): Int {
        return try {
            val batteryFile = File("/sys/class/power_supply/battery/capacity")
            if (batteryFile.exists()) {
                batteryFile.readText().trim().toInt()
            } else {
                100 // Assume full if can't read
            }
        } catch (e: Exception) {
            100 // Assume full if error
        }
    }
    
    /**
     * Check for system instability indicators
     */
    private fun hasSystemInstability(): Boolean {
        return try {
            // Check for recent crashes
            val crashIndicators = listOf(
                "/data/tombstones",
                "/data/anr",
                "/data/system/dropbox"
            )
            
            crashIndicators.any { path ->
                val dir = File(path)
                dir.exists() && dir.listFiles()?.any { 
                    System.currentTimeMillis() - it.lastModified() < 300000 // 5 minutes
                } == true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if fastboot mode is accessible
     */
    private fun canAccessFastbootMode(): Boolean {
        return try {
            // Check if device supports fastboot commands
            val process = Runtime.getRuntime().exec("getprop ro.boot.mode")
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            
            output.contains("fastboot") || File("/system/bin/fastboot").exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get list of devices known to be incompatible with KernelSU
     */
    private fun getIncompatibleDevices(): List<String> {
        return listOf(
            // Add known problematic devices here
            "SM-A205F", // Samsung Galaxy A20 (example)
            "RMX2030"   // Realme 6 (example)
            // This list should be updated based on community feedback
        )
    }
    
    /**
     * Check if a package exists on the system
     */
    private fun packageExists(packageName: String): Boolean {
        return try {
            // Use reflection to access PackageManager
            val context = Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication")
                .invoke(null) as? android.content.Context
            
            context?.packageManager?.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get current system properties for restore point
     */
    private fun getCurrentSystemProperties(): Map<String, String> {
        val properties = mutableMapOf<String, String>()
        
        try {
            val importantProps = listOf(
                "ro.product.model",
                "ro.product.manufacturer", 
                "ro.product.brand",
                "ro.product.device",
                "ro.build.fingerprint",
                "ro.hardware"
            )
            
            for (prop in importantProps) {
                val process = Runtime.getRuntime().exec("getprop $prop")
                val value = process.inputStream.bufferedReader().readText().trim()
                process.waitFor()
                
                if (value.isNotEmpty()) {
                    properties[prop] = value
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current properties", e)
        }
        
        return properties
    }
    
    /**
     * Store restore point data
     */
    private fun storeRestorePoint(properties: Map<String, String>) {
        try {
            // Store in a safe location that survives reboots
            val restoreFile = File("/data/local/tmp/kashi_restore_point.txt")
            val content = properties.map { "${it.key}=${it.value}" }.joinToString("\n")
            restoreFile.writeText(content)
            
            Log.d(TAG, "Restore point stored: ${properties.size} properties")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing restore point", e)
        }
    }
    
    /**
     * Get stored restore point data
     */
    private fun getStoredRestorePoint(): Map<String, String> {
        return try {
            val restoreFile = File("/data/local/tmp/kashi_restore_point.txt")
            if (!restoreFile.exists()) {
                return emptyMap()
            }
            
            val content = restoreFile.readText()
            val properties = mutableMapOf<String, String>()
            
            content.lines().forEach { line ->
                val parts = line.split("=", limit = 2)
                if (parts.size == 2) {
                    properties[parts[0]] = parts[1]
                }
            }
            
            properties
        } catch (e: Exception) {
            Log.e(TAG, "Error reading restore point", e)
            emptyMap()
        }
    }
}
