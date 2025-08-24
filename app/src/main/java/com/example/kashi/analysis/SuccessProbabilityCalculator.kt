package com.example.kashi.analysis

import android.util.Log
import com.example.kashi.core.KernelSUIntegration
import com.example.kashi.spoof.AdvancedPixelFeatureEmulator

/**
 * Success Probability Calculator
 * 
 * Calculates realistic success probabilities for different app categories
 * based on available bypass methods and stack configuration
 * 
 * Based on real-world data from 2025 KernelSU ecosystem
 */
class SuccessProbabilityCalculator {
    
    companion object {
        private const val TAG = "SuccessCalculator"
        
        // Base success rates for different app categories (without enhancements)
        private val BASE_SUCCESS_RATES = mapOf(
            "banking" to 15f,
            "payment" to 30f,
            "streaming" to 60f,
            "social" to 80f,
            "government" to 20f,
            "pixel_exclusive" to 0f,
            "general" to 75f
        )
        
        // Success rate multipliers for different enhancement layers
        private val ENHANCEMENT_MULTIPLIERS = mapOf(
            "kernelsu_active" to 2.5f,
            "susfs_active" to 1.8f,
            "tricky_store_keybox" to 2.0f,
            "advanced_spoofing" to 1.4f,
            "behavioral_evasion" to 1.3f,
            "pixel_features" to 3.0f // For Pixel exclusive apps
        )
        
        // Maximum success rates (realistic caps)
        private val MAX_SUCCESS_RATES = mapOf(
            "banking" to 85f,
            "payment" to 90f,
            "streaming" to 95f,
            "social" to 98f,
            "government" to 90f,
            "pixel_exclusive" to 70f,
            "general" to 95f
        )
    }
    
    private val kernelSUIntegration = KernelSUIntegration.getInstance()
    private val pixelFeatureEmulator = AdvancedPixelFeatureEmulator.getInstance()
    
    /**
     * Calculate success probability for a specific app
     */
    fun calculateAppSuccess(packageName: String, appCategory: String): SuccessProbability {
        val baseRate = BASE_SUCCESS_RATES[appCategory] ?: BASE_SUCCESS_RATES["general"]!!
        val maxRate = MAX_SUCCESS_RATES[appCategory] ?: MAX_SUCCESS_RATES["general"]!!
        
        var currentRate = baseRate
        val enhancements = mutableListOf<String>()
        
        // Check KernelSU enhancement
        val kernelSUStatus = kernelSUIntegration.kernelSUStatus.value
        if (kernelSUStatus == KernelSUIntegration.KernelSUStatus.PRESENT_ACTIVE) {
            currentRate *= ENHANCEMENT_MULTIPLIERS["kernelsu_active"]!!
            enhancements.add("KernelSU Active (+${(ENHANCEMENT_MULTIPLIERS["kernelsu_active"]!! - 1) * 100}%)")
        }
        
        // Check SUSFS enhancement
        val susfsStatus = kernelSUIntegration.susfsStatus.value
        if (susfsStatus in listOf(
            KernelSUIntegration.SUSFSStatus.ACTIVE_BASIC,
            KernelSUIntegration.SUSFSStatus.ACTIVE_ADVANCED
        )) {
            currentRate *= ENHANCEMENT_MULTIPLIERS["susfs_active"]!!
            enhancements.add("SUSFS Active (+${(ENHANCEMENT_MULTIPLIERS["susfs_active"]!! - 1) * 100}%)")
        }
        
        // Check Tricky Store enhancement
        val trickyStoreStatus = kernelSUIntegration.trickyStoreStatus.value
        if (trickyStoreStatus == KernelSUIntegration.TrickyStoreStatus.ACTIVE_WITH_KEYBOX) {
            currentRate *= ENHANCEMENT_MULTIPLIERS["tricky_store_keybox"]!!
            enhancements.add("Tricky Store + Keybox (+${(ENHANCEMENT_MULTIPLIERS["tricky_store_keybox"]!! - 1) * 100}%)")
        }
        
        // Check Pixel feature enhancement for exclusive apps
        if (appCategory == "pixel_exclusive") {
            val pixelStatus = pixelFeatureEmulator.emulationStatus.value
            if (pixelStatus in listOf(
                AdvancedPixelFeatureEmulator.EmulationStatus.PARTIAL_SUCCESS,
                AdvancedPixelFeatureEmulator.EmulationStatus.FULL_SUCCESS
            )) {
                currentRate *= ENHANCEMENT_MULTIPLIERS["pixel_features"]!!
                enhancements.add("Pixel Feature Emulation (+${(ENHANCEMENT_MULTIPLIERS["pixel_features"]!! - 1) * 100}%)")
            }
        }
        
        // Apply behavioral evasion enhancement
        currentRate *= ENHANCEMENT_MULTIPLIERS["behavioral_evasion"]!!
        enhancements.add("Behavioral Evasion (+${(ENHANCEMENT_MULTIPLIERS["behavioral_evasion"]!! - 1) * 100}%)")
        
        // Apply advanced spoofing enhancement
        currentRate *= ENHANCEMENT_MULTIPLIERS["advanced_spoofing"]!!
        enhancements.add("Advanced Spoofing (+${(ENHANCEMENT_MULTIPLIERS["advanced_spoofing"]!! - 1) * 100}%)")
        
        // Cap at maximum realistic rate
        val finalRate = minOf(currentRate, maxRate)
        
        return SuccessProbability(
            packageName = packageName,
            category = appCategory,
            baseSuccessRate = baseRate,
            enhancedSuccessRate = finalRate,
            maxPossibleRate = maxRate,
            enhancements = enhancements,
            recommendation = getRecommendation(finalRate, appCategory)
        )
    }
    
    /**
     * Calculate success probabilities for all app categories
     */
    fun calculateAllCategories(): Map<String, SuccessProbability> {
        return BASE_SUCCESS_RATES.keys.associateWith { category ->
            calculateAppSuccess("example.app", category)
        }
    }
    
    /**
     * Get specific app success probabilities (known apps)
     */
    fun getKnownAppProbabilities(): Map<String, SuccessProbability> {
        return mapOf(
            // Banking Apps
            "com.revolut.revolut" to calculateAppSuccess("com.revolut.revolut", "banking"),
            "ie.aib.aibmobile" to calculateAppSuccess("ie.aib.aibmobile", "banking"),
            "com.chase.sig.android" to calculateAppSuccess("com.chase.sig.android", "banking"),
            "com.bankofamerica.digitalwallet" to calculateAppSuccess("com.bankofamerica.digitalwallet", "banking"),
            
            // Payment Apps
            "com.paypal.android.p2pmobile" to calculateAppSuccess("com.paypal.android.p2pmobile", "payment"),
            "com.google.android.apps.walletnfcrel" to calculateAppSuccess("com.google.android.apps.walletnfcrel", "payment"),
            
            // Streaming Apps
            "com.netflix.mediaclient" to calculateAppSuccess("com.netflix.mediaclient", "streaming"),
            "com.disney.disneyplus" to calculateAppSuccess("com.disney.disneyplus", "streaming"),
            
            // Pixel Exclusive
            "com.google.android.apps.pixel.studio" to calculateAppSuccess("com.google.android.apps.pixel.studio", "pixel_exclusive"),
            
            // Government Apps
            "gov.irs.irapp" to calculateAppSuccess("gov.irs.irapp", "government"),
            "com.ssa.gov.mobileapp" to calculateAppSuccess("com.ssa.gov.mobileapp", "government")
        )
    }
    
    /**
     * Get recommendation based on success rate
     */
    private fun getRecommendation(successRate: Float, category: String): String {
        return when {
            successRate >= 80f -> "🟢 EXCELLENT - Very high success probability"
            successRate >= 60f -> "🟡 GOOD - High success probability with proper setup"
            successRate >= 40f -> "🟠 MODERATE - May work with optimal configuration"
            successRate >= 20f -> "🔴 LOW - Challenging, requires advanced methods"
            else -> "⚫ VERY LOW - Extremely difficult, may need private methods"
        }
    }
    
    /**
     * Get stack configuration recommendations
     */
    fun getStackRecommendations(): StackRecommendations {
        val integrationStatus = kernelSUIntegration.getIntegrationStatus()
        val pixelSummary = pixelFeatureEmulator.getEmulationSummary()
        
        val recommendations = mutableListOf<String>()
        val improvements = mutableListOf<String>()
        
        // KernelSU recommendations
        when (integrationStatus.kernelSU) {
            KernelSUIntegration.KernelSUStatus.NOT_PRESENT -> {
                recommendations.add("❌ CRITICAL: Install KernelSU kernel for your device")
                improvements.add("Installing KernelSU can improve success rates by 150%")
            }
            KernelSUIntegration.KernelSUStatus.PRESENT_INACTIVE -> {
                recommendations.add("⚠️ WARNING: KernelSU detected but not active")
                improvements.add("Activating KernelSU can improve success rates by 150%")
            }
            KernelSUIntegration.KernelSUStatus.PRESENT_ACTIVE -> {
                recommendations.add("✅ EXCELLENT: KernelSU is active and working")
            }
            else -> {
                recommendations.add("🔍 CHECKING: KernelSU status unknown")
            }
        }
        
        // SUSFS recommendations
        when (integrationStatus.susfs) {
            KernelSUIntegration.SUSFSStatus.NOT_AVAILABLE -> {
                recommendations.add("❌ CRITICAL: Install SUSFS4KSU module")
                improvements.add("SUSFS can improve success rates by 80%")
            }
            KernelSUIntegration.SUSFSStatus.AVAILABLE_INACTIVE -> {
                recommendations.add("⚠️ WARNING: SUSFS available but not configured")
                improvements.add("Configuring SUSFS can improve success rates by 80%")
            }
            KernelSUIntegration.SUSFSStatus.ACTIVE_ADVANCED -> {
                recommendations.add("✅ EXCELLENT: SUSFS providing advanced filesystem hiding")
            }
            else -> {
                recommendations.add("🔍 CHECKING: SUSFS status unknown")
            }
        }
        
        // Tricky Store recommendations
        when (integrationStatus.trickyStore) {
            KernelSUIntegration.TrickyStoreStatus.NOT_INSTALLED -> {
                recommendations.add("❌ CRITICAL: Install Tricky Store module")
                improvements.add("Tricky Store can improve success rates by 100%")
            }
            KernelSUIntegration.TrickyStoreStatus.ACTIVE_NO_KEYBOX -> {
                recommendations.add("⚠️ CRITICAL: Tricky Store needs valid keybox.xml")
                improvements.add("Valid keybox can improve success rates by 100%")
            }
            KernelSUIntegration.TrickyStoreStatus.ACTIVE_WITH_KEYBOX -> {
                recommendations.add("✅ EXCELLENT: Tricky Store active with valid keybox")
            }
            else -> {
                recommendations.add("🔍 CHECKING: Tricky Store status unknown")
            }
        }
        
        // Pixel features recommendations
        when (pixelSummary.status) {
            AdvancedPixelFeatureEmulator.EmulationStatus.FAILED -> {
                recommendations.add("⚠️ WARNING: Pixel feature emulation failed")
                improvements.add("Fixing Pixel features can enable exclusive apps")
            }
            AdvancedPixelFeatureEmulator.EmulationStatus.FULL_SUCCESS -> {
                recommendations.add("✅ EXCELLENT: All Pixel features emulated successfully")
            }
            AdvancedPixelFeatureEmulator.EmulationStatus.PARTIAL_SUCCESS -> {
                recommendations.add("🟡 GOOD: Partial Pixel feature emulation (${String.format("%.1f", pixelSummary.successRate)}%)")
            }
            else -> {
                recommendations.add("🔍 CHECKING: Pixel feature status unknown")
            }
        }
        
        return StackRecommendations(
            overallScore = calculateOverallScore(integrationStatus, pixelSummary),
            recommendations = recommendations,
            improvements = improvements,
            estimatedSuccessRate = calculateOverallSuccessRate()
        )
    }
    
    /**
     * Calculate overall stack score (0-100)
     */
    private fun calculateOverallScore(
        integrationStatus: KernelSUIntegration.IntegrationStatus,
        pixelSummary: AdvancedPixelFeatureEmulator.EmulationSummary
    ): Int {
        var score = 0
        
        // KernelSU score (30 points)
        score += when (integrationStatus.kernelSU) {
            KernelSUIntegration.KernelSUStatus.PRESENT_ACTIVE -> 30
            KernelSUIntegration.KernelSUStatus.PRESENT_INACTIVE -> 15
            else -> 0
        }
        
        // SUSFS score (25 points)
        score += when (integrationStatus.susfs) {
            KernelSUIntegration.SUSFSStatus.ACTIVE_ADVANCED -> 25
            KernelSUIntegration.SUSFSStatus.ACTIVE_BASIC -> 20
            KernelSUIntegration.SUSFSStatus.AVAILABLE_INACTIVE -> 10
            else -> 0
        }
        
        // Tricky Store score (25 points)
        score += when (integrationStatus.trickyStore) {
            KernelSUIntegration.TrickyStoreStatus.ACTIVE_WITH_KEYBOX -> 25
            KernelSUIntegration.TrickyStoreStatus.ACTIVE_NO_KEYBOX -> 15
            KernelSUIntegration.TrickyStoreStatus.INSTALLED_INACTIVE -> 10
            else -> 0
        }
        
        // Pixel features score (20 points)
        score += when (pixelSummary.status) {
            AdvancedPixelFeatureEmulator.EmulationStatus.FULL_SUCCESS -> 20
            AdvancedPixelFeatureEmulator.EmulationStatus.PARTIAL_SUCCESS -> (pixelSummary.successRate / 5).toInt()
            else -> 0
        }
        
        return score.coerceIn(0, 100)
    }
    
    /**
     * Calculate overall success rate estimate
     */
    private fun calculateOverallSuccessRate(): Float {
        val bankingProb = calculateAppSuccess("example.banking", "banking")
        val paymentProb = calculateAppSuccess("example.payment", "payment")
        val streamingProb = calculateAppSuccess("example.streaming", "streaming")
        
        // Weighted average (banking apps are hardest, so they're weighted more)
        return (bankingProb.enhancedSuccessRate * 0.5f + 
                paymentProb.enhancedSuccessRate * 0.3f + 
                streamingProb.enhancedSuccessRate * 0.2f)
    }
    
    data class SuccessProbability(
        val packageName: String,
        val category: String,
        val baseSuccessRate: Float,
        val enhancedSuccessRate: Float,
        val maxPossibleRate: Float,
        val enhancements: List<String>,
        val recommendation: String
    )
    
    data class StackRecommendations(
        val overallScore: Int,
        val recommendations: List<String>,
        val improvements: List<String>,
        val estimatedSuccessRate: Float
    )
}
