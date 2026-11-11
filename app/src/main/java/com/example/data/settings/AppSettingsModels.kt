package com.example.data.settings

import com.example.data.learning.LearningLanguageMode
import com.example.data.model.AssetCategory
import com.example.data.model.CandleTimeframe

/**
 * 3 User-selectable Theme Modes.
 */
enum class AppThemeMode(val id: String, val titleEnglish: String, val titleRomanUrdu: String) {
    DARK("dark", "Dark Mode", "Dark Mode (Raat)"),
    LIGHT("light", "Light Mode", "Light Mode (Din)"),
    SYSTEM("system", "System Default", "System Default (Auto)");

    fun displayName(lang: LearningLanguageMode): String = when (lang) {
        LearningLanguageMode.ENGLISH -> titleEnglish
        LearningLanguageMode.ROMAN_URDU -> titleRomanUrdu
        LearningLanguageMode.STANDARD_URDU -> when (this) {
            DARK -> "ڈارک موڈ"
            LIGHT -> "لائٹ موڈ"
            SYSTEM -> "سسٹم ڈیفالٹ"
        }
        LearningLanguageMode.BILINGUAL -> "$titleEnglish ($titleRomanUrdu)"
    }
}

/**
 * Market Data Polling Refresh Preference options.
 */
enum class RefreshRatePreference(val seconds: Int, val titleEnglish: String, val titleRomanUrdu: String) {
    FAST_15S(15, "15 Seconds (Real-time)", "15 Seconds (Tez)"),
    BALANCED_30S(30, "30 Seconds (Balanced)", "30 Seconds (Munasib)"),
    RELAXED_60S(60, "60 Seconds (Data Saver)", "60 Seconds (Data Bachao)"),
    MANUAL_ONLY(0, "Manual Only (Zero Background Data)", "Sirf Manual (Zero Data)");

    fun displayName(lang: LearningLanguageMode): String = when (lang) {
        LearningLanguageMode.ENGLISH -> titleEnglish
        LearningLanguageMode.ROMAN_URDU -> titleRomanUrdu
        LearningLanguageMode.STANDARD_URDU -> "$seconds سیکنڈز"
        LearningLanguageMode.BILINGUAL -> "$titleEnglish • $titleRomanUrdu"
    }

    companion object {
        fun fromSeconds(sec: Int): RefreshRatePreference =
            values().firstOrNull { it.seconds == sec } ?: FAST_15S
    }
}

enum class UrduFontSize(val id: String, val titleEnglish: String, val titleUrdu: String, val scaleFactor: Float) {
    SMALL("small", "Small", "چھوٹا", 0.9f),
    MEDIUM("medium", "Medium", "درمیانہ", 1.0f),
    LARGE("large", "Large", "بڑا", 1.15f);

    fun displayName(lang: LearningLanguageMode): String = when (lang) {
        LearningLanguageMode.ENGLISH -> titleEnglish
        LearningLanguageMode.ROMAN_URDU -> when (this) {
            SMALL -> "Chhota (Small)"
            MEDIUM -> "Darmiyana (Medium)"
            LARGE -> "Bada (Large)"
        }
        LearningLanguageMode.STANDARD_URDU -> titleUrdu
        LearningLanguageMode.BILINGUAL -> "$titleEnglish ($titleUrdu)"
    }
}

/**
 * Immutable State snapshot representing all Terminal & App Settings.
 */
data class AppSettingsState(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val languageMode: LearningLanguageMode = LearningLanguageMode.BILINGUAL,
    val urduFontSize: UrduFontSize = UrduFontSize.MEDIUM,
    val isDataSavingMode: Boolean = false,
    val isLowPerformanceMode: Boolean = false,
    val defaultTimeframe: CandleTimeframe = CandleTimeframe.H1,
    val defaultWatchlistCategory: AssetCategory = AssetCategory.ALL,
    val alertNotificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = false,
    val refreshRate: RefreshRatePreference = RefreshRatePreference.FAST_15S,
    val lastCacheClearedTimestamp: Long? = null,
    val lastMarketRefreshTimestamp: Long = System.currentTimeMillis()
)
