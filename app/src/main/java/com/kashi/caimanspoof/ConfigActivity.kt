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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch

/**
 * Modern Jetpack Compose configuration activity
 */
class ConfigActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PixelSpoofTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConfigScreen()
                }
            }
        }
    }
}

/**
 * Main configuration screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val availableProfiles by viewModel.availableProfiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastError by viewModel.lastError.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(lastError) {
        lastError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "PixelSpoof Configuration",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Current Profile Section
            item {
                CurrentProfileCard(
                    profile = selectedProfile,
                    isLoading = isLoading
                )
            }
            
            // Profile Selection Section
            item {
                Text(
                    text = "Select Device Profile:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            items(availableProfiles) { profile ->
                ProfileSelectionCard(
                    profile = profile,
                    isSelected = profile.displayName == selectedProfile?.displayName,
                    onSelect = { 
                        scope.launch {
                            viewModel.selectProfile(profile.displayName)
                            snackbarHostState.showSnackbar(
                                "Profile set to ${profile.displayName}. Reboot required to apply changes.",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                )
            }
            
            // Actions Section
            item {
                ActionsSection(
                    onRefresh = { 
                        scope.launch {
                            viewModel.refreshProfiles()
                        }
                    },
                    isLoading = isLoading
                )
            }
            
            // Settings Section
            item {
                SettingsSection(viewModel = viewModel)
            }
            
            // About Section
            item {
                AboutCard()
            }
        }
    }
}

/**
 * Current profile display card
 */
@Composable
fun CurrentProfileCard(
    profile: DeviceProfile?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading...")
                }
            } else if (profile != null) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Model: ${profile.model}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Build: ${profile.buildId}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Security Patch: ${profile.securityPatch}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Profile selection card
 */
@Composable
fun ProfileSelectionCard(
    profile: DeviceProfile,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = profile.model,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Actions section with refresh button
 */
@Composable
fun ActionsSection(
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refreshing...")
                } else {
                    Text("Refresh Profiles from Server")
                }
            }
        }
    }
}

/**
 * Settings section
 */
@Composable
fun SettingsSection(
    viewModel: ConfigViewModel
) {
    var stealthMode by remember { mutableStateOf(viewModel.isStealthModeEnabled()) }
    var autoUpdate by remember { mutableStateOf(viewModel.isAutoUpdateEnabled()) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stealth Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Stealth Mode",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Hide debug logs and reduce detection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = stealthMode,
                    onCheckedChange = { 
                        stealthMode = it
                        viewModel.setStealthMode(it)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Auto Update Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto Update",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Automatically check for profile updates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = autoUpdate,
                    onCheckedChange = { 
                        autoUpdate = it
                        viewModel.setAutoUpdate(it)
                    }
                )
            }
        }
    }
}

/**
 * About section with developer information
 */
@Composable
fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📱 About PixelSpoof",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "PixelSpoof - Advanced Device Fingerprint Spoofing",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Comprehensive Xposed module for spoofing device properties " +
                       "with real-time property interception, advanced anti-detection, " +
                       "and safety-first kernel integration.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Developer Information
            Text(
                text = "👨‍💻 Developer",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• Developer: kashi265\n" +
                       "• Telegram: @Kekashi265\n" +
                       "• Email: ace265brown@gmail.com\n" +
                       "• GitHub: github.com/samuelKuseka/PixelSPoof",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Important Information
            Text(
                text = "⚠️ Important Notes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• Reboot required after changing profiles\n" +
                       "• Profiles auto-update daily from GitHub\n" +
                       "• Comprehensive property interception active\n" +
                       "• Device safety measures implemented\n" +
                       "• Compatible with LSPosed framework",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Version Information
            Text(
                text = "📦 Version Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• Version: 2.0.0 (Comprehensive Spoofing)\n" +
                       "• Build: August 2025 - Enhanced Edition\n" +
                       "• Features: 200+ Properties, Safety-First Design\n" +
                       "• License: GNU GPL v3.0",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * App theme
 */
@Composable
fun PixelSpoofTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = dynamicDarkColorScheme(LocalContext.current),
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun ConfigScreenPreview() {
    PixelSpoofTheme {
        ConfigScreen()
    }
}
