package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HighDensityCard
import com.example.ui.components.HighDensityBadge
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val settings by viewModel.settings.collectAsState()
    var pinText by remember { mutableStateOf(settings.pinHash) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Density Visual Theme
        item {
            HighDensityCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("APPEARANCE & DENSITY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Dark Theme",
                        subtitle = "Enable High Density dark color canvas",
                        checked = settings.isDarkTheme,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(isDarkTheme = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    SettingToggleRow(
                        title = "Glassmorphism Containers",
                        subtitle = "Apply translucent high density surfaces",
                        checked = settings.glassmorphismEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(glassmorphismEnabled = it)) }
                    )
                }
            }
        }

        // Reader & Code Inspector
        item {
            HighDensityCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("READER & CODE INSPECTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Restore Tabs on Launch",
                        subtitle = "Automatically re-open previously open documents",
                        checked = settings.restoreTabsOnRestart,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(restoreTabsOnRestart = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    SettingToggleRow(
                        title = "Remember Reading Scroll Position",
                        subtitle = "Save exact page and line position in Room DB",
                        checked = settings.rememberReadingPosition,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(rememberReadingPosition = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    SettingToggleRow(
                        title = "Keep Screen Awake While Reading",
                        subtitle = "Prevent screen sleep during document viewing",
                        checked = settings.keepScreenAwake,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(keepScreenAwake = it)) }
                    )
                }
            }
        }

        // Global PDF Watermarking Configuration
        item {
            var watermarkInput by remember(settings.globalWatermarkString) { mutableStateOf(settings.globalWatermarkString) }
            val watermarkStyles = listOf("Diagonal Center", "Signature Stamp", "Top Header Stamp")

            HighDensityCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GLOBAL PDF WATERMARKING & SIGNATURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Auto-Apply Watermark on PDF Export",
                        subtitle = "Automatically overlay defined watermark string during document export",
                        checked = settings.autoApplyWatermarkOnPdfExport,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoApplyWatermarkOnPdfExport = it)) }
                    )

                    if (settings.autoApplyWatermarkOnPdfExport) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = watermarkInput,
                            onValueChange = { watermarkInput = it },
                            label = { Text("Global Watermark Text String") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Default Placement Style", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            watermarkStyles.forEach { style ->
                                FilterChip(
                                    selected = settings.globalWatermarkStyle == style,
                                    onClick = { viewModel.updateSettings(settings.copy(globalWatermarkStyle = style)) },
                                    label = { Text(style, fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.updateSettings(settings.copy(globalWatermarkString = watermarkInput)) },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = watermarkInput.isNotBlank()
                        ) {
                            Text(
                                text = "Save Global Watermark String",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HighDensityBadge {
                            Text(
                                "Active Global Watermark: \"${settings.globalWatermarkString}\"",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Privacy & App Lock
        item {
            HighDensityCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PRIVACY & SECURITY VAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Enable App Lock / Vault PIN",
                        subtitle = "Protect private files with a PIN code",
                        checked = settings.appLockEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(appLockEnabled = it)) }
                    )

                    if (settings.appLockEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinText,
                            onValueChange = { pinText = it },
                            label = { Text("Set Vault PIN (Default: 1234)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.updateSettings(settings.copy(pinHash = pinText)) },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Save Vault PIN Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        SettingToggleRow(
                            title = "Enable Biometric / Fingerprint Unlock",
                            subtitle = "Unlock vault using biometric sensor fallback",
                            checked = settings.biometricsEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(biometricsEnabled = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    SettingToggleRow(
                        title = "Hide Content in App Switcher",
                        subtitle = "Prevent sensitive previews in recent apps screen",
                        checked = settings.hideContentInAppSwitcher,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(hideContentInAppSwitcher = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HighDensityBadge {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("🔒 AES-256 File-Level Encryption Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Document contents stored in local Room SQLite database records are protected with 256-bit AES/GCM cipher transformation.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // DATABASE BACKUP & LOCAL VAULT STORAGE
        item {
            val folderPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) { uri ->
                if (uri != null) {
                    viewModel.updateSettings(settings.copy(backupFolderUri = uri.toString()))
                }
            }

            HighDensityCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DATABASE BACKUP & VAULT STORAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Automatic Periodic Room DB Backup",
                        subtitle = "Automatically back up local database to local storage folder",
                        checked = settings.autoBackupEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoBackupEnabled = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Backup Target Directory:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (settings.backupFolderUri.isNotEmpty()) settings.backupFolderUri else "Default App Local Storage (/backups)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { folderPicker.launch(null) },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Select Backup Folder",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { viewModel.triggerDatabaseBackup() },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Backup Now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (settings.lastBackupTimestamp > 0) {
                        val formattedDate = remember(settings.lastBackupTimestamp) {
                            SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(settings.lastBackupTimestamp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Last successful backup: $formattedDate",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // About & Offline Guarantee
        item {
            HighDensityCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ABOUT LS DOCS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Version 1.0.0 (High Density)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("100% Offline Document Viewer, Reader, Inspector & Editor.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("LS Docs contains zero cloud analytics, zero advertising, and zero tracking code.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
