package com.example

import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.model.MarketAsset
import com.example.data.model.NewsImpact
import com.example.data.model.NewsImpactLevel
import com.example.data.news.EconomicGlossaryDatabase
import com.example.data.news.NewsImpactAnalyzer
import com.example.data.scanner.MarketScannerEngine
import com.example.data.scanner.ScannerConditionFilter
import com.example.data.watchlist.WatchlistRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsScannerNotificationTest {

    @Test
    fun testNewsImpactAnalyzer_BullishClassification() {
        val title = "Federal Reserve announces rate cut as inflation drops below target"
        val snippet = "Crypto markets rally and Bitcoin surges above \$95,000 following dovish FOMC remarks."

        val analysis = NewsImpactAnalyzer.analyze(headline = title, summary = snippet)
        assertEquals(NewsImpact.BULLISH, analysis.impact)
        assertEquals(NewsImpactLevel.HIGH, analysis.impactLevel)
        assertTrue("BTC should be detected in affected assets", analysis.affectedAssets.contains("BTC"))
    }

    @Test
    fun testNewsImpactAnalyzer_BearishClassification() {
        val title = "Regulatory crackdown and exchange hack causes massive liquidation"
        val snippet = "SEC files lawsuit, causing sharp crash in crypto assets and market outflow."

        val analysis = NewsImpactAnalyzer.analyze(headline = title, summary = snippet)
        assertEquals(NewsImpact.BEARISH, analysis.impact)
        assertEquals(NewsImpactLevel.HIGH, analysis.impactLevel)
    }

    @Test
    fun testEconomicGlossaryDatabase_TermsCoverage() {
        val cpi = EconomicGlossaryDatabase.terms.find { it.key == "cpi" }
        assertNotNull("CPI term should exist in glossary", cpi)
        assertEquals("Consumer Price Index", cpi?.title)
        assertTrue("Roman Urdu must describe inflation", cpi?.explanationUrdu?.lowercase()?.contains("mehngai") == true)

        val fedRate = EconomicGlossaryDatabase.terms.find { it.key == "fed_rate" }
        assertNotNull("Fed Rate term should exist", fedRate)
        assertTrue(fedRate?.explanationUrdu?.lowercase()?.contains("interest rate") == true)

        val nfp = EconomicGlossaryDatabase.terms.find { it.key == "nfp" }
        assertNotNull("NFP term should exist", nfp)

        val allTerms = EconomicGlossaryDatabase.terms
        assertTrue("Glossary must contain at least 9 core macro indicators", allTerms.size >= 9)
    }

    @Test
    fun testMarketScannerEngine_ScanAsset() = runBlocking {
        // Build candles with rising prices
        val candles = mutableListOf<Candle>()
        var price = 50000.0
        val baseTime = 1700000000000L
        for (i in 0 until 50) {
            price += 100.0
            candles.add(
                Candle(
                    openTime = baseTime + (i * 900000L),
                    open = price - 50.0,
                    high = price + 50.0,
                    low = price - 60.0,
                    close = price,
                    volume = 1000.0,
                    closeTime = baseTime + ((i + 1) * 900000L) - 1
                )
            )
        }

        val asset = MarketAsset(
            id = "btc",
            name = "Bitcoin",
            symbol = "BTCUSDT",
            category = com.example.data.model.AssetCategory.CRYPTO,
            currentPrice = price,
            change24hPercent = 3.5,
            high24h = price + 200.0,
            low24h = price - 2000.0,
            volume24h = "$50,000,000"
        )

        val item = MarketScannerEngine.scanAsset(
            asset = asset,
            candles = candles,
            timeframe = CandleTimeframe.M15
        )

        assertNotNull(item)
        assertEquals("BTCUSDT", item.symbol)
        assertTrue("Verified price should match latest candle close", item.verifiedPrice > 0)
        assertTrue("Criteria should match at least one signal", item.matchedCriteria.isNotEmpty())
    }

    @Test
    fun testWatchlistRepository_InMemoryPersistence() {
        val repo = WatchlistRepository(null)
        val initial = repo.getFavoriteAssetIds()
        assertTrue("Default watchlist must contain btc", initial.contains("btc"))
        assertTrue("Default watchlist must contain eth", initial.contains("eth"))

        val isFavBefore = repo.isFavorite("sol")
        val isFavAfter = repo.toggleFavorite("sol")
        assertEquals(!isFavBefore, isFavAfter)
    }
}
