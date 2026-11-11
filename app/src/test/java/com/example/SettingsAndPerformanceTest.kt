package com.example

import com.example.data.learning.LearningLanguageMode
import com.example.data.learning.LearningPreferencesRepository
import com.example.data.model.AssetCategory
import com.example.data.model.CandleTimeframe
import com.example.data.repository.TradingRepository
import com.example.data.settings.AppSettingsRepository
import com.example.data.settings.AppThemeMode
import com.example.data.settings.NexusLocalization
import com.example.data.settings.RefreshRatePreference
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsAndPerformanceTest {

    @Test
    fun testDefaultSettingsState() {
        val repo = AppSettingsRepository(null)
        assertEquals(AppThemeMode.DARK, repo.themeMode.value)
        assertEquals(LearningLanguageMode.BILINGUAL, repo.languageMode.value)
        assertFalse(repo.isDataSavingMode.value)
        assertFalse(repo.isLowPerformanceMode.value)
        assertEquals(CandleTimeframe.H1, repo.defaultTimeframe.value)
        assertEquals(AssetCategory.ALL, repo.defaultWatchlistCategory.value)
        assertTrue(repo.alertNotificationsEnabled.value)
        assertFalse(repo.soundEffectsEnabled.value)
        assertEquals(15, repo.refreshIntervalSeconds.value)
    }

    @Test
    fun testThemeModeSwitching() {
        val repo = AppSettingsRepository(null)

        repo.setThemeMode(AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, repo.themeMode.value)

        repo.setThemeMode(AppThemeMode.SYSTEM)
        assertEquals(AppThemeMode.SYSTEM, repo.themeMode.value)

        repo.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, repo.themeMode.value)
    }

    @Test
    fun testLanguageModeSwitching() {
        val settingsRepo = AppSettingsRepository(null)
        val learningRepo = LearningPreferencesRepository(null)

        settingsRepo.setLanguageMode(LearningLanguageMode.ROMAN_URDU)
        learningRepo.setLanguageMode(LearningLanguageMode.ROMAN_URDU)

        assertEquals(LearningLanguageMode.ROMAN_URDU, settingsRepo.languageMode.value)
        assertEquals(LearningLanguageMode.ROMAN_URDU, learningRepo.languageMode.value)

        settingsRepo.setLanguageMode(LearningLanguageMode.ENGLISH)
        assertEquals(LearningLanguageMode.ENGLISH, settingsRepo.languageMode.value)
    }

    @Test
    fun testDataSavingModeBehavior() {
        val repo = AppSettingsRepository(null)
        assertFalse(repo.isDataSavingMode.value)

        // Enabling data saving mode should update flag and relax default interval
        repo.setDataSavingMode(true)
        assertTrue(repo.isDataSavingMode.value)
        assertEquals(60, repo.refreshIntervalSeconds.value)

        repo.setDataSavingMode(false)
        assertFalse(repo.isDataSavingMode.value)
    }

    @Test
    fun testLowPerformanceDeviceModeBehavior() {
        val repo = AppSettingsRepository(null)
        assertFalse(repo.isLowPerformanceMode.value)

        repo.setLowPerformanceMode(true)
        assertTrue(repo.isLowPerformanceMode.value)

        repo.setLowPerformanceMode(false)
        assertFalse(repo.isLowPerformanceMode.value)
    }

    @Test
    fun testTerminalDefaultsAndPreferences() {
        val repo = AppSettingsRepository(null)

        repo.setDefaultTimeframe(CandleTimeframe.H4)
        assertEquals(CandleTimeframe.H4, repo.defaultTimeframe.value)

        repo.setDefaultWatchlistCategory(AssetCategory.CRYPTO)
        assertEquals(AssetCategory.CRYPTO, repo.defaultWatchlistCategory.value)

        repo.setRefreshIntervalSeconds(30)
        assertEquals(30, repo.refreshIntervalSeconds.value)
        assertEquals(RefreshRatePreference.BALANCED_30S, RefreshRatePreference.fromSeconds(repo.refreshIntervalSeconds.value))

        repo.setAlertNotificationsEnabled(false)
        assertFalse(repo.alertNotificationsEnabled.value)

        repo.setSoundEffectsEnabled(true)
        assertTrue(repo.soundEffectsEnabled.value)
    }

    @Test
    fun testClearTemporaryCache() {
        val repo = AppSettingsRepository(null)
        val purgedCount = repo.clearTemporaryCache()
        assertTrue(purgedCount >= 0)
        assertTrue(repo.lastCacheClearedTimestamp.value > 0L)
    }

    @Test
    fun testNexusLocalizationOutputs() {
        // Test navigation
        assertEquals("Dashboard", NexusLocalization.tabDashboard(LearningLanguageMode.ENGLISH))
        assertEquals("Dashboard", NexusLocalization.tabDashboard(LearningLanguageMode.ROMAN_URDU))
        assertTrue(NexusLocalization.tabDashboard(LearningLanguageMode.BILINGUAL).contains("/"))

        // Test settings labels
        assertTrue(NexusLocalization.dataSavingLabel(LearningLanguageMode.ENGLISH).contains("Data Saving"))
        assertTrue(NexusLocalization.dataSavingLabel(LearningLanguageMode.ROMAN_URDU).contains("Internet Bachao"))

        assertTrue(NexusLocalization.lowPerfLabel(LearningLanguageMode.ENGLISH).contains("Low Performance"))
        assertTrue(NexusLocalization.lowPerfLabel(LearningLanguageMode.ROMAN_URDU).contains("Low Performance"))

        // Test legal disclaimers
        assertTrue(NexusLocalization.riskDisclaimerEnglish.contains("RISK", ignoreCase = true))
        assertTrue(NexusLocalization.riskDisclaimerRomanUrdu.contains("KHATRAAT", ignoreCase = true))
        assertTrue(NexusLocalization.privacyPolicyEnglish.contains("Privacy", ignoreCase = true))
        assertTrue(NexusLocalization.privacyPolicyRomanUrdu.contains("Nijiyat", ignoreCase = true))
    }

    @Test
    fun testTradingRepositoryPerformanceModes() = runBlocking {
        val repository = TradingRepository()

        // Test Data Saving Mode on repository
        repository.setDataSavingMode(true)
        assertTrue(repository.isDataSavingMode.value)

        // Test Low Performance Mode on repository
        repository.setLowPerformanceMode(true)
        assertTrue(repository.isLowPerformanceMode.value)

        // Test candle limit reduction under low performance mode
        val candles = repository.getCandles("BTC", CandleTimeframe.H1, limit = 250)
        assertNotNull(candles)
        assertTrue("Under low performance mode, candles should be capped to 45 max", candles.size <= 45)

        // Test clear cache on repository
        val cleared = repository.clearCache()
        assertTrue(cleared >= 0)
    }
}
