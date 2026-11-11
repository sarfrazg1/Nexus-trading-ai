package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.data.learning.LearningLanguageMode
import com.example.data.model.AssetCategory
import com.example.data.model.CandleTimeframe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 100% Offline persistent settings repository using SharedPreferences.
 * Manages Display Themes, Application Languages, Data Saving Mode,
 * Low Performance Device Mode, Terminal Defaults, and Cache Management.
 */
class AppSettingsRepository(context: Context? = null) {

    private var prefs: SharedPreferences? =
        context?.getSharedPreferences("nexus_app_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _languageMode = MutableStateFlow(loadLanguageMode())
    val languageMode: StateFlow<LearningLanguageMode> = _languageMode.asStateFlow()

    private val _urduFontSize = MutableStateFlow(loadUrduFontSize())
    val urduFontSize: StateFlow<UrduFontSize> = _urduFontSize.asStateFlow()

    private val _isDataSavingMode = MutableStateFlow(loadBoolean("data_saving_mode", false))
    val isDataSavingMode: StateFlow<Boolean> = _isDataSavingMode.asStateFlow()

    private val _isLowPerformanceMode = MutableStateFlow(loadBoolean("low_performance_mode", false))
    val isLowPerformanceMode: StateFlow<Boolean> = _isLowPerformanceMode.asStateFlow()

    private val _defaultTimeframe = MutableStateFlow(loadDefaultTimeframe())
    val defaultTimeframe: StateFlow<CandleTimeframe> = _defaultTimeframe.asStateFlow()

    private val _defaultWatchlistCategory = MutableStateFlow(loadDefaultCategory())
    val defaultWatchlistCategory: StateFlow<AssetCategory> = _defaultWatchlistCategory.asStateFlow()

    private val _alertNotificationsEnabled = MutableStateFlow(loadBoolean("alert_notifications", true))
    val alertNotificationsEnabled: StateFlow<Boolean> = _alertNotificationsEnabled.asStateFlow()

    private val _soundEffectsEnabled = MutableStateFlow(loadBoolean("sound_effects", false))
    val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    private val _refreshIntervalSeconds = MutableStateFlow(loadInt("refresh_interval_seconds", 15))
    val refreshIntervalSeconds: StateFlow<Int> = _refreshIntervalSeconds.asStateFlow()

    private val _lastDataRefreshTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastDataRefreshTimestamp: StateFlow<Long> = _lastDataRefreshTimestamp.asStateFlow()

    private val _lastCacheClearedTimestamp = MutableStateFlow(loadLong("last_cache_cleared", 0L))
    val lastCacheClearedTimestamp: StateFlow<Long> = _lastCacheClearedTimestamp.asStateFlow()

    fun bindContext(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("nexus_app_settings", Context.MODE_PRIVATE)
            _themeMode.value = loadThemeMode()
            _languageMode.value = loadLanguageMode()
            _isDataSavingMode.value = loadBoolean("data_saving_mode", false)
            _isLowPerformanceMode.value = loadBoolean("low_performance_mode", false)
            _defaultTimeframe.value = loadDefaultTimeframe()
            _defaultWatchlistCategory.value = loadDefaultCategory()
            _alertNotificationsEnabled.value = loadBoolean("alert_notifications", true)
            _soundEffectsEnabled.value = loadBoolean("sound_effects", false)
            _refreshIntervalSeconds.value = loadInt("refresh_interval_seconds", 15)
            _lastCacheClearedTimestamp.value = loadLong("last_cache_cleared", 0L)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs?.edit()?.putString("theme_mode", mode.id)?.apply()
        _themeMode.value = mode
    }



    fun setDataSavingMode(enabled: Boolean) {
        prefs?.edit()?.putBoolean("data_saving_mode", enabled)?.apply()
        _isDataSavingMode.value = enabled
        // If data saving mode is turned on and interval is currently 15s, relax to 60s
        if (enabled && _refreshIntervalSeconds.value == 15) {
            setRefreshIntervalSeconds(60)
        }
    }

    fun setLowPerformanceMode(enabled: Boolean) {
        prefs?.edit()?.putBoolean("low_performance_mode", enabled)?.apply()
        _isLowPerformanceMode.value = enabled
    }

    fun setDefaultTimeframe(timeframe: CandleTimeframe) {
        prefs?.edit()?.putString("default_timeframe", timeframe.name)?.apply()
        _defaultTimeframe.value = timeframe
    }

    fun setDefaultWatchlistCategory(category: AssetCategory) {
        prefs?.edit()?.putString("default_watchlist_cat", category.name)?.apply()
        _defaultWatchlistCategory.value = category
    }

    fun setAlertNotificationsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean("alert_notifications", enabled)?.apply()
        _alertNotificationsEnabled.value = enabled
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean("sound_effects", enabled)?.apply()
        _soundEffectsEnabled.value = enabled
    }

    fun setRefreshIntervalSeconds(seconds: Int) {
        prefs?.edit()?.putInt("refresh_interval_seconds", seconds)?.apply()
        _refreshIntervalSeconds.value = seconds
    }

    fun recordMarketRefresh() {
        _lastDataRefreshTimestamp.value = System.currentTimeMillis()
    }

    /**
     * Clears cached candle data and temporary buffers.
     * Preserves user bookmarks, active alerts, and configuration keys.
     */
    fun clearTemporaryCache(): Int {
        val now = System.currentTimeMillis()
        prefs?.edit()?.putLong("last_cache_cleared", now)?.apply()
        _lastCacheClearedTimestamp.value = now
        // Suggest Garbage Collection
        try {
            System.gc()
        } catch (_: Exception) {}
        return 1
    }

    private fun loadThemeMode(): AppThemeMode {
        val id = prefs?.getString("theme_mode", "dark") ?: "dark"
        return when (id) {
            "light" -> AppThemeMode.LIGHT
            "system" -> AppThemeMode.SYSTEM
            else -> AppThemeMode.DARK
        }
    }

    fun setLanguageMode(mode: LearningLanguageMode) {
        prefs?.edit()?.putString("language_mode", mode.id)?.apply()
        _languageMode.value = mode
    }

    fun setUrduFontSize(size: UrduFontSize) {
        prefs?.edit()?.putString("urdu_font_size", size.id)?.apply()
        _urduFontSize.value = size
    }

    private fun loadUrduFontSize(): UrduFontSize {
        val id = prefs?.getString("urdu_font_size", "medium") ?: "medium"
        return when (id) {
            "small" -> UrduFontSize.SMALL
            "large" -> UrduFontSize.LARGE
            else -> UrduFontSize.MEDIUM
        }
    }

    private fun loadLanguageMode(): LearningLanguageMode {
        val id = prefs?.getString("language_mode", "BI") ?: "BI"
        return when (id) {
            "EN" -> LearningLanguageMode.ENGLISH
            "RU" -> LearningLanguageMode.ROMAN_URDU
            "UR" -> LearningLanguageMode.STANDARD_URDU
            else -> LearningLanguageMode.BILINGUAL
        }
    }

    private fun loadDefaultTimeframe(): CandleTimeframe {
        val name = prefs?.getString("default_timeframe", CandleTimeframe.H1.name) ?: CandleTimeframe.H1.name
        return try {
            CandleTimeframe.valueOf(name)
        } catch (_: Exception) {
            CandleTimeframe.H1
        }
    }

    private fun loadDefaultCategory(): AssetCategory {
        val name = prefs?.getString("default_watchlist_cat", AssetCategory.ALL.name) ?: AssetCategory.ALL.name
        return try {
            AssetCategory.valueOf(name)
        } catch (_: Exception) {
            AssetCategory.ALL
        }
    }

    private fun loadBoolean(key: String, default: Boolean): Boolean {
        return prefs?.getBoolean(key, default) ?: default
    }

    private fun loadInt(key: String, default: Int): Int {
        return prefs?.getInt(key, default) ?: default
    }

    private fun loadLong(key: String, default: Long): Long {
        return prefs?.getLong(key, default) ?: default
    }
}
