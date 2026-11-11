package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.learning.LearningLanguageMode
import com.example.data.model.ApiFeedConfig
import com.example.data.model.AssetCategory
import com.example.data.model.CandleTimeframe
import com.example.data.settings.AppSettingsState
import com.example.data.settings.AppThemeMode
import com.example.data.settings.NexusLocalization
import com.example.data.settings.RefreshRatePreference
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NexusTheme
import com.example.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settingsState: AppSettingsState,
    apiConfigs: List<ApiFeedConfig>,
    onThemeSelect: (AppThemeMode) -> Unit,
    onLanguageSelect: (LearningLanguageMode) -> Unit,
    onDataSavingToggle: (Boolean) -> Unit,
    onLowPerformanceToggle: (Boolean) -> Unit,
    onDefaultTimeframeSelect: (CandleTimeframe) -> Unit,
    onDefaultWatchlistSelect: (AssetCategory) -> Unit,
    onRefreshRateSelect: (RefreshRatePreference) -> Unit,
    onAlertsToggle: (Boolean) -> Unit,
    onSoundEffectsToggle: (Boolean) -> Unit,
    onClearCacheClick: () -> Unit,
    onConfigureFeed: (ApiFeedConfig) -> Unit,
    onRefreshFeeds: () -> Unit,
    onUrduFontSizeSelect: (com.example.data.settings.UrduFontSize) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lang = settingsState.languageMode
    val colors = NexusTheme.colors

    var showClearCacheConfirm by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showRiskDisclaimerDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val formattedLastRefresh = remember(settingsState.lastMarketRefreshTimestamp) {
        val sdf = SimpleDateFormat("h:mm:ss a", Locale.US)
        sdf.format(Date(settingsState.lastMarketRefreshTimestamp))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = NexusLocalization.settingsTitle(lang),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.gold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = NexusLocalization.settingsSubtitle(lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        )
                    )
                }

                // Data freshness pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (settingsState.isDataSavingMode) colors.gold else colors.bullishGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sync: $formattedLastRefresh",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        // Section 1: Display Theme System
        item {
            SettingsSectionHeader(
                icon = Icons.Default.Palette,
                title = NexusLocalization.sectionAppearance(lang),
                goldColor = colors.gold,
                textColor = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = NexusLocalization.themeLabel(lang),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = NexusLocalization.themeDesc(lang),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.values().forEach { mode ->
                            val isSelected = settingsState.themeMode == mode
                            ThemeOptionButton(
                                mode = mode,
                                isSelected = isSelected,
                                lang = lang,
                                colors = colors,
                                onClick = { onThemeSelect(mode) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Universal Language Mode
        item {
            SettingsSectionHeader(
                icon = Icons.Default.Language,
                title = NexusLocalization.sectionLanguage(lang),
                goldColor = colors.gold,
                textColor = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = NexusLocalization.languageLabel(lang),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = NexusLocalization.languageDesc(lang),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 4 Language Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LearningLanguageMode.values().forEach { lMode ->
                            val isSelected = settingsState.languageMode == lMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.gold else colors.surfaceVariant)
                                    .border(1.dp, if (isSelected) colors.gold else colors.border, RoundedCornerShape(8.dp))
                                    .clickable { onLanguageSelect(lMode) }
                                    .padding(vertical = 8.dp)
                                    .testTag("lang_setting_btn_${lMode.id.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (lMode) {
                                        LearningLanguageMode.ENGLISH -> "English"
                                        LearningLanguageMode.ROMAN_URDU -> "Roman"
                                        LearningLanguageMode.STANDARD_URDU -> "اردو"
                                        LearningLanguageMode.BILINGUAL -> "Bilingual"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) (if (colors.isDark) Color(0xFF070B14) else Color.White) else colors.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Urdu Font Size Selector
                    Text(
                        text = NexusLocalization.urduFontSizeLabel(lang),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = NexusLocalization.urduFontSizeDesc(lang),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.data.settings.UrduFontSize.values().forEach { uSize ->
                            val isSelected = settingsState.urduFontSize == uSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.gold else colors.surfaceVariant)
                                    .border(1.dp, if (isSelected) colors.gold else colors.border, RoundedCornerShape(8.dp))
                                    .clickable { onUrduFontSizeSelect(uSize) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uSize.displayName(lang),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) (if (colors.isDark) Color(0xFF070B14) else Color.White) else colors.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Performance & Resource Controls (Data Saving Mode & Low Performance Device Mode)
        item {
            SettingsSectionHeader(
                icon = Icons.Default.Speed,
                title = NexusLocalization.sectionPerformance(lang),
                goldColor = colors.gold,
                textColor = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("performance_controls_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Data Saving Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = NexusLocalization.dataSavingLabel(lang),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (settingsState.isDataSavingMode) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.gold.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ACTIVE", color = colors.gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = NexusLocalization.dataSavingDesc(lang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.textSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }

                        Switch(
                            checked = settingsState.isDataSavingMode,
                            onCheckedChange = onDataSavingToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.gold,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceVariant
                            ),
                            modifier = Modifier.testTag("toggle_data_saving_mode")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Low Performance Device Mode Toggle (2GB, 3GB, 4GB RAM)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = NexusLocalization.lowPerfLabel(lang),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (settingsState.isLowPerformanceMode) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.bullishGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("2GB-4GB OK", color = colors.bullishGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = NexusLocalization.lowPerfDesc(lang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.textSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }

                        Switch(
                            checked = settingsState.isLowPerformanceMode,
                            onCheckedChange = onLowPerformanceToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.bullishGreen,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceVariant
                            ),
                            modifier = Modifier.testTag("toggle_low_perf_mode")
                        )
                    }
                }
            }
        }

        // Section 4: General Terminal Defaults
        item {
            SettingsSectionHeader(
                icon = Icons.Default.Tune,
                title = NexusLocalization.sectionDefaults(lang),
                goldColor = colors.gold,
                textColor = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("terminal_defaults_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Default Chart Timeframe
                    Text(
                        text = NexusLocalization.defaultTfLabel(lang),
                        style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val timeframes = listOf(CandleTimeframe.M15, CandleTimeframe.H1, CandleTimeframe.H4, CandleTimeframe.D1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        timeframes.forEach { tf ->
                            val isSelected = settingsState.defaultTimeframe == tf
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.gold else colors.surfaceVariant)
                                    .clickable { onDefaultTimeframeSelect(tf) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tf.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) (if (colors.isDark) Color(0xFF070B14) else Color.White) else colors.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Default Watchlist Category
                    Text(
                        text = NexusLocalization.defaultWatchlistLabel(lang),
                        style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf(AssetCategory.ALL, AssetCategory.CRYPTO, AssetCategory.COMMODITIES, AssetCategory.FOREX)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = settingsState.defaultWatchlistCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.gold else colors.surfaceVariant)
                                    .clickable { onDefaultWatchlistSelect(cat) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) (if (colors.isDark) Color(0xFF070B14) else Color.White) else colors.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Polling Rate Selector
                    Text(
                        text = NexusLocalization.refreshIntervalLabel(lang),
                        style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RefreshRatePreference.values().forEach { rRate ->
                            val isSelected = settingsState.refreshRate == rRate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.gold else colors.surfaceVariant)
                                    .clickable { onRefreshRateSelect(rRate) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (rRate.seconds > 0) "${rRate.seconds}s" else "Manual",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) (if (colors.isDark) Color(0xFF070B14) else Color.White) else colors.textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Notification & Sound Preferences
        item {
            SettingsSectionHeader(
                icon = Icons.Default.NotificationsActive,
                title = NexusLocalization.sectionNotifications(lang),
                goldColor = colors.gold,
                textColor = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = NexusLocalization.priceAlertsLabel(lang),
                                style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Receive in-app alerts when price hits targets",
                                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = settingsState.alertNotificationsEnabled,
                            onCheckedChange = onAlertsToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.gold,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceVariant
                            ),
                            modifier = Modifier.testTag("toggle_price_alerts")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = NexusLocalization.soundFxLabel(lang),
                                style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Play audio cue when signals or alerts trigger",
                                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = settingsState.soundEffectsEnabled,
                            onCheckedChange = onSoundEffectsToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.gold,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.surfaceVariant
                            ),
                            modifier = Modifier.testTag("toggle_sound_effects")
                        )
                    }
                }
            }
        }

        // Section 6: Cache & Storage Management
        item {
            SettingsSectionHeader(
                icon = Icons.Default.DeleteSweep,
                title = NexusLocalization.sectionStorage(lang),
                goldColor = colors.gold,
                textColor = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cache_storage_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = NexusLocalization.clearCacheDesc(lang),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            lineHeight = 16.sp,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showClearCacheConfirm = true },
                        modifier = Modifier.fillMaxWidth().testTag("clear_cache_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.gold)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = NexusLocalization.btnClearCache(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section 7: Market Data Feeds (Existing Connectors)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsSectionHeader(
                    icon = Icons.Default.Cable,
                    title = "MARKET DATA PROVIDERS",
                    goldColor = colors.gold,
                    textColor = colors.textPrimary
                )

                OutlinedButton(
                    onClick = onRefreshFeeds,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.gold),
                    modifier = Modifier.testTag("test_all_feeds_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ping All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(apiConfigs, key = { it.id }) { config ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("feed_card_${config.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (config.isConnected) colors.bullishGreen.copy(alpha = 0.4f) else colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = config.providerName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Assets: ${config.assetCovered}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.gold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (config.isConnected) colors.bullishGreen else WarningOrange)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (config.isConnected) "CONNECTED" else "NOT CONNECTED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (config.isConnected) colors.bullishGreen else WarningOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Endpoint: ${config.endpointUrl}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textMuted,
                            fontSize = 10.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (config.requiresApiKey) {
                        Button(
                            onClick = { onConfigureFeed(config) },
                            modifier = Modifier.fillMaxWidth().testTag("config_key_btn_${config.id}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (config.isConnected) colors.surfaceVariant else colors.gold,
                                contentColor = if (config.isConnected) colors.textPrimary else (if (colors.isDark) Color(0xFF070B14) else Color.White)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (config.isConnected) "Manage API Key / Disconnect" else "Configure Market Feed API Key",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Public open endpoint active. No authentication token required.",
                                style = MaterialTheme.typography.bodySmall.copy(color = colors.bullishGreen, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }

        // Section 8: Legal Disclaimers & About
        item {
            SettingsSectionHeader(
                icon = Icons.Default.Security,
                title = NexusLocalization.sectionAboutLegal(lang),
                goldColor = colors.gold,
                textColor = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("about_legal_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Risk Disclaimer Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showRiskDisclaimerDialog = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = colors.bearishRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = NexusLocalization.riskDisclaimerTitle(lang),
                                style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                        Text("View", color = colors.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))

                    // Privacy Policy Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPrivacyPolicyDialog = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = colors.bullishGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = NexusLocalization.privacyPolicyTitle(lang),
                                style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                        Text("View", color = colors.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))

                    // About Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAboutDialog = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = colors.gold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = NexusLocalization.aboutTitle(lang),
                                style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                        Text("v1.0.0", color = colors.bullishGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Dialog 1: Clear Cache Confirmation
    if (showClearCacheConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCacheConfirm = false },
            title = {
                Text(
                    text = NexusLocalization.clearCacheConfirmTitle(lang),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = NexusLocalization.clearCacheDesc(lang),
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearCacheConfirm = false
                        onClearCacheClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.gold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = NexusLocalization.btnConfirm(lang),
                        color = if (colors.isDark) Color(0xFF070B14) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearCacheConfirm = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = NexusLocalization.btnCancel(lang), color = colors.textSecondary)
                }
            },
            containerColor = colors.card
        )
    }

    // Modal Dialog 2: Risk Disclaimer
    if (showRiskDisclaimerDialog) {
        LegalNoticeDialog(
            title = NexusLocalization.riskDisclaimerTitle(lang),
            englishContent = NexusLocalization.riskDisclaimerEnglish,
            romanUrduContent = NexusLocalization.riskDisclaimerRomanUrdu,
            languageMode = lang,
            accentColor = colors.bearishRed,
            onDismiss = { showRiskDisclaimerDialog = false }
        )
    }

    // Modal Dialog 3: Privacy Policy
    if (showPrivacyPolicyDialog) {
        LegalNoticeDialog(
            title = NexusLocalization.privacyPolicyTitle(lang),
            englishContent = NexusLocalization.privacyPolicyEnglish,
            romanUrduContent = NexusLocalization.privacyPolicyRomanUrdu,
            languageMode = lang,
            accentColor = colors.bullishGreen,
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    // Modal Dialog 4: About NEXUS
    if (showAboutDialog) {
        AboutNexusDialog(
            languageMode = lang,
            onDismiss = { showAboutDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    goldColor: Color,
    textColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = goldColor, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
private fun ThemeOptionButton(
    mode: AppThemeMode,
    isSelected: Boolean,
    lang: LearningLanguageMode,
    colors: com.example.ui.theme.TradingThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (mode) {
        AppThemeMode.DARK -> Icons.Default.DarkMode
        AppThemeMode.LIGHT -> Icons.Default.LightMode
        AppThemeMode.SYSTEM -> Icons.Default.Tune
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.gold else colors.surfaceVariant)
            .border(1.dp, if (isSelected) colors.gold else colors.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag("theme_btn_${mode.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) (if (colors.isDark) Color(0xFF070B14) else Color.White) else colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (mode) {
                    AppThemeMode.DARK -> "Dark"
                    AppThemeMode.LIGHT -> "Light"
                    AppThemeMode.SYSTEM -> "Auto"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) (if (colors.isDark) Color(0xFF070B14) else Color.White) else colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun LegalNoticeDialog(
    title: String,
    englishContent: String,
    romanUrduContent: String,
    languageMode: LearningLanguageMode,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val colors = NexusTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.card)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (languageMode == LearningLanguageMode.ENGLISH || languageMode == LearningLanguageMode.BILINGUAL) {
                    Text(
                        text = englishContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }

                if (languageMode == LearningLanguageMode.BILINGUAL) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (languageMode == LearningLanguageMode.ROMAN_URDU || languageMode == LearningLanguageMode.BILINGUAL) {
                    Text(
                        text = romanUrduContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.gold,
                            lineHeight = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.gold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "I Understand / Samajh Gaya",
                        color = if (colors.isDark) Color(0xFF070B14) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutNexusDialog(
    languageMode: LearningLanguageMode,
    onDismiss: () -> Unit
) {
    val colors = NexusTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, colors.gold.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.card)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEXUS TRADING AI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
                    }
                }

                Text(
                    text = "Professional Android Trading Terminal v1.0.0",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.gold,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = """
                        ENGINE SPECIFICATIONS:
                        • Smart Money Concepts (BOS, CHoCH, Order Blocks, FVG, Liquidity Sweeps)
                        • Fibonacci Confluence (0.618 Golden Pocket, 1.272 / 1.618 Extensions)
                        • 22 Multi-Timeframe Technical Indicators (EMA, RSI, MACD, Stoch RSI, VWAP, ATR)
                        • Multi-Factor AI Trading Signal Engine (LONG / SHORT / NO TRADE)
                        • 56-Lesson Complete Trading Academy (English & Roman Urdu)
                        • Performance Controls: Data Saving Mode & Low Performance Device Mode (2GB-4GB RAM)
                        • 100% Offline-Friendly Local Architecture
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        lineHeight = 18.sp,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.gold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Close / Band Karein",
                        color = if (colors.isDark) Color(0xFF070B14) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
