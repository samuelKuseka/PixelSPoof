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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Modern Kotlin data class for device profiles with enhanced security features
 */
data class DeviceProfile(
    val manufacturer: String,
    val brand: String,
    val device: String,
    val model: String,
    val board: String,
    val product: String,
    val buildId: String,
    val fingerprint: String,
    val tags: String,
    val type: String,
    val displayName: String,
    val description: String = "",
    val securityPatch: String = "",
    val hardwareRevision: String = "",
    val radioVersion: String = "",
    val vendorFingerprint: String = fingerprint,
    val bootloaderVersion: String = "",
    val serialNumber: String = generateSecureSerial()
) {
    
    companion object {
        
        /**
         * Generate a secure, realistic serial number
         */
        private fun generateSecureSerial(): String {
            val chars = "0123456789ABCDEF"
            return (1..16).map { chars.random() }.joinToString("")
        }
        
        /**
         * Create from JSON with enhanced error handling
         */
        suspend fun fromJson(json: JSONObject): DeviceProfile = withContext(Dispatchers.Default) {
            try {
                DeviceProfile(
                    manufacturer = json.getString("manufacturer"),
                    brand = json.getString("brand"),
                    device = json.getString("device"),
                    model = json.getString("model"),
                    board = json.getString("board"),
                    product = json.getString("product"),
                    buildId = json.getString("buildId"),
                    fingerprint = json.getString("fingerprint"),
                    tags = json.getString("tags"),
                    type = json.getString("type"),
                    displayName = json.getString("displayName"),
                    description = json.optString("description", ""),
                    securityPatch = json.optString("securityPatch", "2025-08-05"),
                    hardwareRevision = json.optString("hardwareRevision", "rev_1.0"),
                    radioVersion = json.optString("radioVersion", "g5300q-130417-240514-B-11205441"),
                    vendorFingerprint = json.optString("vendorFingerprint", json.getString("fingerprint")),
                    bootloaderVersion = json.optString("bootloaderVersion", "mustang-1.0-12345678"),
                    serialNumber = generateSecureSerial()
                )
            } catch (e: Exception) {
                getPixel10ProXL() // Fallback to default
            }
        }
        
        /**
         * Default Pixel 10 Pro XL profile with latest specs
         */
        fun getPixel10ProXL() = DeviceProfile(
            manufacturer = "Google",
            brand = "google",
            device = "mustang",
            model = "Pixel 10 Pro XL",
            board = "mustang",
            product = "mustang_beta",
            buildId = "BP41.250725.006",
            fingerprint = "google/mustang_beta/mustang:16/BP41.250725.006/12701944:user/release-keys",
            tags = "release-keys",
            type = "user",
            displayName = "Pixel 10 Pro XL",
            description = "Latest Pixel 10 Pro XL with Android 16 QPR2 Beta",
            securityPatch = "2025-08-05",
            hardwareRevision = "MP1.0",
            radioVersion = "g5300q-250725-250805-B-12701944",
            bootloaderVersion = "mustang-1.0-12701944"
        )
        
        /**
         * Pixel 10 Pro profile
         */
        fun getPixel10Pro() = DeviceProfile(
            manufacturer = "Google",
            brand = "google",
            device = "frankel",
            model = "Pixel 10 Pro",
            board = "frankel",
            product = "frankel_beta",
            buildId = "BP41.250725.006",
            fingerprint = "google/frankel_beta/frankel:16/BP41.250725.006/12701944:user/release-keys",
            tags = "release-keys",
            type = "user",
            displayName = "Pixel 10 Pro",
            description = "Pixel 10 Pro with Android 16 QPR2 Beta",
            securityPatch = "2025-08-05",
            hardwareRevision = "FP1.0",
            radioVersion = "g5300q-250725-250805-B-12701944",
            bootloaderVersion = "frankel-1.0-12701944"
        )
        
        /**
         * Pixel 9 Pro XL profile 
         */
        fun getPixel9ProXL() = DeviceProfile(
            manufacturer = "Google",
            brand = "google",
            device = "caiman",
            model = "Pixel 9 Pro XL",
            board = "caiman",
            product = "caiman_beta",
            buildId = "BP31.250610.009",
            fingerprint = "google/caiman_beta/caiman:16/BP31.250610.009/12345678:user/release-keys",
            tags = "release-keys",
            type = "user",
            displayName = "Pixel 9 Pro XL",
            description = "Pixel 9 Pro XL with Android 16 QPR1 Beta",
            securityPatch = "2025-07-05",
            hardwareRevision = "CP1.0",
            radioVersion = "g5300q-250610-250705-B-12345678",
            bootloaderVersion = "caiman-1.0-12345678"
        )
        
        /**
         * Get all available profiles
         */
        fun getAllProfiles(): List<DeviceProfile> = listOf(
            getPixel10ProXL(),
            getPixel10Pro(),
            getPixel9ProXL()
        )
    }
    
    /**
     * Validate profile consistency
     */
    fun isValid(): Boolean {
        return manufacturer.isNotBlank() &&
               brand.isNotBlank() &&
               device.isNotBlank() &&
               model.isNotBlank() &&
               buildId.isNotBlank() &&
               fingerprint.contains(device) &&
               fingerprint.contains(buildId)
    }
    
    /**
     * Get enhanced system properties map
     */
    fun getSystemProperties(): Map<String, String> = mapOf(
        // Core properties
        "ro.product.manufacturer" to manufacturer,
        "ro.product.brand" to brand,
        "ro.product.device" to device,
        "ro.product.model" to model,
        "ro.product.board" to board,
        "ro.product.name" to product,
        
        // Vendor properties
        "ro.product.vendor.manufacturer" to manufacturer,
        "ro.product.vendor.brand" to brand,
        "ro.product.vendor.device" to device,
        "ro.product.vendor.model" to model,
        "ro.product.vendor.name" to product,
        
        // System properties
        "ro.product.system.manufacturer" to manufacturer,
        "ro.product.system.brand" to brand,
        "ro.product.system.device" to device,
        "ro.product.system.model" to model,
        "ro.product.system.name" to product,
        
        // Hardware properties
        "ro.hardware" to device,
        "ro.hardware.revision" to hardwareRevision,
        "ro.board.platform" to "tensor",
        "ro.soc.manufacturer" to "Google",
        "ro.soc.model" to "Tensor G5",
        
        // Build properties
        "ro.build.id" to buildId,
        "ro.build.display.id" to "$buildId",
        "ro.build.version.security_patch" to securityPatch,
        "ro.build.fingerprint" to fingerprint,
        "ro.vendor.build.fingerprint" to vendorFingerprint,
        
        // Radio properties
        "ro.baseband" to radioVersion,
        "ro.bootloader" to bootloaderVersion,
        
        // Security properties
        "ro.boot.serialno" to serialNumber,
        "ro.serialno" to serialNumber,
        "ro.boot.bootloader" to bootloaderVersion
    )
}
