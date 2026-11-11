package com.example

import com.example.data.ai.MarketContextBuilder
import com.example.data.ai.OfflineTradingAnalysisEngine
import com.example.data.learning.LearningLanguageMode
import com.example.data.model.AssetCategory
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.model.MarketAsset
import com.example.data.settings.UrduFontSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusAiAssistantTest {

    private fun generateSampleCandles(count: Int = 50, startPrice: Double = 90000.0): List<Candle> {
        val candles = mutableListOf<Candle>()
        var currentPrice = startPrice
        val now = System.currentTimeMillis()
        for (i in 0 until count) {
            val open = currentPrice
            val high = open + 150.0
            val low = open - 100.0
            val close = open + 50.0
            currentPrice = close
            candles.add(
                Candle(
                    openTime = now - (count - i) * 3600000L,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = 1200.0,
                    closeTime = now - (count - i - 1) * 3600000L
                )
            )
        }
        return candles
    }

    @Test
    fun testMarketContextBuilder_BuildsValidSnapshot() {
        val asset = MarketAsset(
            id = "btc_usdt",
            name = "Bitcoin",
            symbol = "BTCUSDT",
            category = AssetCategory.CRYPTO,
            currentPrice = 92500.0,
            change24hPercent = 2.5
        )
        val candles = generateSampleCandles()

        val snapshot = MarketContextBuilder.buildSnapshot(
            asset = asset,
            candles = candles,
            timeframe = CandleTimeframe.H1,
            technicalAnalysis = null,
            smcAnalysis = null,
            signal = null,
            newsList = emptyList()
        )

        assertNotNull(snapshot)
        assertEquals("BTCUSDT", snapshot.symbol)
        assertEquals(92500.0, snapshot.currentPrice, 0.01)
        assertEquals(CandleTimeframe.H1.label, snapshot.timeframe)

        val promptContext = snapshot.toPromptContext(LearningLanguageMode.ENGLISH)
        assertTrue(promptContext.contains("BTCUSDT"))
        assertTrue(promptContext.contains("92500.0"))
        assertTrue(promptContext.contains("VERIFIED MARKET CONTEXT"))
    }

    @Test
    fun testOfflineTradingAnalysisEngine_GeneratesFourLanguages() {
        val asset = MarketAsset(
            id = "btc_usdt",
            name = "Bitcoin",
            symbol = "BTCUSDT",
            category = AssetCategory.CRYPTO,
            currentPrice = 93200.0,
            change24hPercent = -1.2
        )
        val snapshot = MarketContextBuilder.buildSnapshot(
            asset = asset,
            candles = generateSampleCandles(),
            timeframe = CandleTimeframe.H1
        )

        // 1. English
        val resultEn = OfflineTradingAnalysisEngine.generateAnalysis("What is the trend for BTC?", snapshot, LearningLanguageMode.ENGLISH)
        assertTrue(resultEn.contains("OFFLINE RULE-BASED ANALYSIS", ignoreCase = true))
        assertTrue(resultEn.contains("BTCUSDT"))

        // 2. Roman Urdu
        val resultRu = OfflineTradingAnalysisEngine.generateAnalysis("BTC ka trend kaisa hai?", snapshot, LearningLanguageMode.ROMAN_URDU)
        assertTrue(resultRu.contains("OFFLINE RULE-BASED ANALYSIS", ignoreCase = true))
        assertTrue(resultRu.contains("BTCUSDT"))

        // 3. Standard Urdu (Urdu Script)
        val resultUr = OfflineTradingAnalysisEngine.generateAnalysis("کیا بی ٹی سی اب اوپر جائے گا؟", snapshot, LearningLanguageMode.STANDARD_URDU)
        assertTrue(resultUr.contains("آف لائن اصولوں پر مبنی تجارتی تجزیہ"))
        // Check for authentic Urdu characters
        assertTrue(resultUr.any { it in '\u0600'..'\u06FF' })

        // 4. Bilingual
        val resultBi = OfflineTradingAnalysisEngine.generateAnalysis("Explain BTC position", snapshot, LearningLanguageMode.BILINGUAL)
        assertTrue(resultBi.contains("BILINGUAL ANALYSIS", ignoreCase = true))
        assertTrue(resultBi.contains("BTCUSDT"))
    }

    @Test
    fun testOfflineTradingAnalysisEngine_TimeframeAdvisor() {
        val asset = MarketAsset(
            id = "btc_usdt",
            name = "Bitcoin",
            symbol = "BTCUSDT",
            category = AssetCategory.CRYPTO,
            currentPrice = 93200.0,
            change24hPercent = 0.8
        )
        val snapshot = MarketContextBuilder.buildSnapshot(
            asset = asset,
            candles = generateSampleCandles(),
            timeframe = CandleTimeframe.H1
        )

        val query = "5m chart is bullish but 1h chart is showing resistance, what should I do?"
        val resultEn = OfflineTradingAnalysisEngine.generateAnalysis(query, snapshot, LearningLanguageMode.ENGLISH)
        assertTrue(resultEn.contains("TIMEFRAME ADVISOR", ignoreCase = true))

        val resultUr = OfflineTradingAnalysisEngine.generateAnalysis("پانچ منٹ اور ایک گھنٹے کے چارٹ میں تضاد ہے", snapshot, LearningLanguageMode.STANDARD_URDU)
        assertTrue(resultUr.any { it in '\u0600'..'\u06FF' })
    }

    @Test
    fun testOfflineTradingAnalysisEngine_MacroAndCpiReasoning() {
        val asset = MarketAsset(
            id = "btc_usdt",
            name = "Bitcoin",
            symbol = "BTCUSDT",
            category = AssetCategory.CRYPTO,
            currentPrice = 93200.0,
            change24hPercent = -3.5
        )
        val snapshot = MarketContextBuilder.buildSnapshot(
            asset = asset,
            candles = generateSampleCandles(),
            timeframe = CandleTimeframe.H1
        )

        val query = "CPI came lower than forecast but Bitcoin dumped immediately, why?"
        val resultEn = OfflineTradingAnalysisEngine.generateAnalysis(query, snapshot, LearningLanguageMode.ENGLISH)
        assertTrue(resultEn.contains("MACROECONOMIC & CPI REASONING", ignoreCase = true))

        val resultRu = OfflineTradingAnalysisEngine.generateAnalysis("CPI behtar aayi phir bhi market kyu giri?", snapshot, LearningLanguageMode.ROMAN_URDU)
        assertTrue(resultRu.contains("Sell the News", ignoreCase = true) || resultRu.contains("Liquidation Sweep", ignoreCase = true))
    }

    @Test
    fun testUrduFontSize_ScaleFactors() {
        assertEquals(0.9f, UrduFontSize.SMALL.scaleFactor, 0.01f)
        assertEquals(1.0f, UrduFontSize.MEDIUM.scaleFactor, 0.01f)
        assertEquals(1.15f, UrduFontSize.LARGE.scaleFactor, 0.01f)

        assertEquals("چھوٹا", UrduFontSize.SMALL.displayName(LearningLanguageMode.STANDARD_URDU))
        assertEquals("درمیانہ", UrduFontSize.MEDIUM.displayName(LearningLanguageMode.STANDARD_URDU))
        assertEquals("بڑا", UrduFontSize.LARGE.displayName(LearningLanguageMode.STANDARD_URDU))
    }
}
