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
import android.content.SharedPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Modern Kotlin configuration manager with coroutines and flow
 */
class ConfigManager private constructor(private val context: Context?) {
    
    companion object {
        private const val PREF_NAME = "pixelspoof_config"
        private const val KEY_SELECTED_PROFILE = "selected_profile"
        private const val KEY_LAST_UPDATE = "last_update"
        private const val KEY_CACHED_PROFILES = "cached_profiles"
        private const val KEY_STEALTH_MODE = "stealth_mode"
        private const val KEY_AUTO_UPDATE = "auto_update"
        
        private const val PROFILES_URL = "https://raw.githubusercontent.com/samuelKuseka/PixelSpoof/main/device_profiles.json"
        private const val UPDATE_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
        
        @Volatile
        private var INSTANCE: ConfigManager? = null
        
        fun getInstance(context: Context? = null): ConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConfigManager(context).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    
    // StateFlow for reactive updates
    private val _availableProfiles = MutableStateFlow<List<DeviceProfile>>(emptyList())
    val availableProfiles: StateFlow<List<DeviceProfile>> = _availableProfiles.asStateFlow()
    
    private val _selectedProfile = MutableStateFlow<DeviceProfile?>(null)
    val selectedProfile: StateFlow<DeviceProfile?> = _selectedProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    init {
        scope.launch {
            loadConfiguration()
        }
    }
    
    /**
     * Load configuration with coroutines
     */
    private suspend fun loadConfiguration() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        
        try {
            // Load cached profiles first
            val cachedProfiles = loadCachedProfiles()
            if (cachedProfiles.isNotEmpty()) {
                _availableProfiles.value = cachedProfiles
                StealthManager.stealthLog("Loaded ${cachedProfiles.size} cached profiles")
            } else {
                // Load default profiles if no cache
                _availableProfiles.value = DeviceProfile.getAllProfiles()
                StealthManager.stealthLog("Loaded default profiles")
            }
            
            // Load selected profile
            val selectedProfileName = prefs?.getString(KEY_SELECTED_PROFILE, null)
            _selectedProfile.value = findProfileByName(selectedProfileName) 
                ?: DeviceProfile.getPixel10ProXL()
            
            // Check if we should update from server
            if (shouldUpdateFromServer()) {
                updateProfilesFromServer()
            }
            
            _lastError.value = null
        } catch (e: Exception) {
            _lastError.value = "Failed to load configuration: ${e.message}"
            StealthManager.stealthLog("Configuration load error: ${e.message}")
            
            // Fallback to defaults
            _availableProfiles.value = DeviceProfile.getAllProfiles()
            _selectedProfile.value = DeviceProfile.getPixel10ProXL()
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Load cached profiles from preferences
     */
    private suspend fun loadCachedProfiles(): List<DeviceProfile> = withContext(Dispatchers.Default) {
        try {
            val cachedJson = prefs?.getString(KEY_CACHED_PROFILES, null) ?: return@withContext emptyList()
            parseProfilesFromJson(cachedJson)
        } catch (e: Exception) {
            StealthManager.stealthLog("Failed to load cached profiles: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Check if we should update from server
     */
    private fun shouldUpdateFromServer(): Boolean {
        val autoUpdate = prefs?.getBoolean(KEY_AUTO_UPDATE, true) ?: true
        if (!autoUpdate) return false
        
        val lastUpdate = prefs?.getLong(KEY_LAST_UPDATE, 0L) ?: 0L
        return System.currentTimeMillis() - lastUpdate > UPDATE_INTERVAL_MS
    }
    
    /**
     * Update profiles from GitHub server with coroutines
     */
    suspend fun updateProfilesFromServer() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        
        try {
            val jsonData = downloadProfilesFromGitHub()
            if (jsonData != null) {
                val newProfiles = parseProfilesFromJson(jsonData)
                
                if (newProfiles.isNotEmpty()) {
                    _availableProfiles.value = newProfiles
                    
                    // Cache the data
                    prefs?.edit()
                        ?.putString(KEY_CACHED_PROFILES, jsonData)
                        ?.putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                        ?.apply()
                    
                    StealthManager.stealthLog("Updated ${newProfiles.size} profiles from server")
                    _lastError.value = null
                } else {
                    _lastError.value = "No valid profiles received from server"
                }
            } else {
                _lastError.value = "Failed to download profiles from server"
            }
        } catch (e: Exception) {
            _lastError.value = "Server update failed: ${e.message}"
            StealthManager.stealthLog("Server update error: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Download profiles from GitHub using OkHttp
     */
    private suspend fun downloadProfilesFromGitHub(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(PROFILES_URL)
                .addHeader("User-Agent", "PixelSpoof/2.0")
                .addHeader("Cache-Control", "no-cache")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                StealthManager.stealthLog("HTTP error: ${response.code}")
                null
            }
        } catch (e: IOException) {
            StealthManager.stealthLog("Network error: ${e.message}")
            null
        } catch (e: Exception) {
            StealthManager.stealthLog("Download error: ${e.message}")
            null
        }
    }
    
    /**
     * Parse profiles from JSON with error handling
     */
    private suspend fun parseProfilesFromJson(jsonData: String): List<DeviceProfile> = withContext(Dispatchers.Default) {
        try {
            val json = JSONObject(jsonData)
            val profilesArray = json.getJSONArray("profiles")
            
            val profiles = mutableListOf<DeviceProfile>()
            for (i in 0 until profilesArray.length()) {
                val profileJson = profilesArray.getJSONObject(i)
                val profile = DeviceProfile.fromJson(profileJson)
                if (profile.isValid()) {
                    profiles.add(profile)
                }
            }
            
            profiles.ifEmpty { DeviceProfile.getAllProfiles() }
        } catch (e: Exception) {
            StealthManager.stealthLog("JSON parsing error: ${e.message}")
            DeviceProfile.getAllProfiles()
        }
    }
    
    /**
     * Find profile by display name
     */
    private fun findProfileByName(name: String?): DeviceProfile? {
        return _availableProfiles.value.find { it.displayName == name }
    }
    
    /**
     * Set selected profile
     */
    suspend fun setSelectedProfile(profileName: String) = withContext(Dispatchers.IO) {
        val profile = findProfileByName(profileName) ?: return@withContext
        
        _selectedProfile.value = profile
        prefs?.edit()?.putString(KEY_SELECTED_PROFILE, profile.displayName)?.apply()
        
        StealthManager.stealthLog("Selected profile: ${profile.displayName}")
    }
    
    /**
     * Force refresh from server
     */
    suspend fun forceRefresh() = withContext(Dispatchers.IO) {
        prefs?.edit()?.putLong(KEY_LAST_UPDATE, 0L)?.apply()
        updateProfilesFromServer()
    }
    
    /**
     * Get current configuration synchronously (for Xposed hooks)
     */
    fun getCurrentProfileSync(): DeviceProfile {
        return _selectedProfile.value ?: DeviceProfile.getPixel10ProXL()
    }
    
    /**
     * Settings management
     */
    fun setStealthMode(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_STEALTH_MODE, enabled)?.apply()
        StealthManager.getInstance().setDebugMode(!enabled)
    }
    
    fun isStealthModeEnabled(): Boolean {
        return prefs?.getBoolean(KEY_STEALTH_MODE, true) ?: true
    }
    
    fun setAutoUpdate(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_AUTO_UPDATE, enabled)?.apply()
    }
    
    fun isAutoUpdateEnabled(): Boolean {
        return prefs?.getBoolean(KEY_AUTO_UPDATE, true) ?: true
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        scope.cancel()
        okHttpClient.dispatcher.executorService.shutdown()
    }
}
