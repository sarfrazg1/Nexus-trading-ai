package com.example

import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.model.MarketDataStatus
import com.example.data.signal.AiTradingSignalEngine
import com.example.data.signal.RomanUrduSignalGlossary
import com.example.data.signal.SignalDecision
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AiTradingSignalEngineTest {

    private lateinit var bullishCandles: List<Candle>
    private lateinit var shortCandles: List<Candle>

    @Before
    fun setUp() {
        val baseTime = 1700000000000L

        // Generate 120 upward trending candles with solid volume
        val bullList = mutableListOf<Candle>()
        var price = 50000.0
        for (i in 0 until 120) {
            val open = price
            val close = open + 150.0 + (i % 5) * 20.0
            val high = close + 80.0
            val low = open - 40.0
            val volume = 2000.0 + (i % 4) * 300.0
            val takerBuy = volume * 0.65 // strong buying volume
            bullList.add(
                Candle(
                    openTime = baseTime + i * 3600000L,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume,
                    takerBuyBaseVolume = takerBuy
                )
            )
            price = close
        }
        bullishCandles = bullList

        // Generate short series (< 30 candles)
        shortCandles = bullList.take(20)
    }

    @Test
    fun testDataSafetyMissingPriceReturnsNoTrade() = runBlocking {
        val signal = AiTradingSignalEngine.generateSignal(
            symbol = "BTC",
            assetName = "Bitcoin",
            currentPrice = null,
            dataStatus = MarketDataStatus.LIVE,
            dataSourceName = "Binance",
            lastUpdatedTime = System.currentTimeMillis(),
            primaryTimeframe = CandleTimeframe.H1,
            candles = bullishCandles,
            technicalAnalysis = null,
            smcAnalysis = null
        )

        assertEquals(SignalDecision.NO_TRADE, signal.decision)
        assertNotNull(signal.noTradeReason)
        assertEquals("DATA_UNAVAILABLE", signal.noTradeReason?.code)
        assertNull(signal.riskPlan)
    }

    @Test
    fun testDataSafetyStaleStatusReturnsNoTrade() = runBlocking {
        val signal = AiTradingSignalEngine.generateSignal(
            symbol = "BTC",
            assetName = "Bitcoin",
            currentPrice = 65000.0,
            dataStatus = MarketDataStatus.STALE,
            dataSourceName = "Binance",
            lastUpdatedTime = System.currentTimeMillis() - 600000L,
            primaryTimeframe = CandleTimeframe.H1,
            candles = bullishCandles,
            technicalAnalysis = null,
            smcAnalysis = null
        )

        assertEquals(SignalDecision.NO_TRADE, signal.decision)
        assertNotNull(signal.noTradeReason)
        assertEquals("MARKET_DATA_STALE", signal.noTradeReason?.code)
    }

    @Test
    fun testDataSafetyInsufficientCandlesReturnsNoTrade() = runBlocking {
        val signal = AiTradingSignalEngine.generateSignal(
            symbol = "BTC",
            assetName = "Bitcoin",
            currentPrice = 52000.0,
            dataStatus = MarketDataStatus.LIVE,
            dataSourceName = "Binance",
            lastUpdatedTime = System.currentTimeMillis(),
            primaryTimeframe = CandleTimeframe.H1,
            candles = shortCandles,
            technicalAnalysis = null,
            smcAnalysis = null
        )

        assertEquals(SignalDecision.NO_TRADE, signal.decision)
        assertNotNull(signal.noTradeReason)
        assertEquals("INSUFFICIENT_DATA", signal.noTradeReason?.code)
    }

    @Test
    fun testSignalGenerationAndRiskManagement() = runBlocking {
        val latestPrice = bullishCandles.last().close
        val signal = AiTradingSignalEngine.generateSignal(
            symbol = "BTC",
            assetName = "Bitcoin",
            currentPrice = latestPrice,
            dataStatus = MarketDataStatus.LIVE,
            dataSourceName = "Binance Public REST",
            lastUpdatedTime = System.currentTimeMillis(),
            primaryTimeframe = CandleTimeframe.H1,
            candles = bullishCandles,
            technicalAnalysis = null,
            smcAnalysis = null
        )

        // Must be one of the three valid decisions
        assertTrue(
            signal.decision == SignalDecision.LONG ||
                    signal.decision == SignalDecision.SHORT ||
                    signal.decision == SignalDecision.NO_TRADE
        )

        // Confidence scoring must be bounded 0..100
        assertTrue(signal.confidence.totalScore in 0..100)
        assertEquals(7, signal.confidence.factors.size)

        // Verify total possible points sum to 100
        val totalMaxPoints = signal.confidence.factors.sumOf { it.maxPoints }
        assertEquals(100, totalMaxPoints)

        // Multi-timeframe confirmation must evaluate 8 timeframes
        assertEquals(8, signal.timeframeConditions.size)

        // If signal is actionable (LONG/SHORT), verify strict risk management rules
        if (signal.decision == SignalDecision.LONG && signal.riskPlan != null) {
            val plan = signal.riskPlan!!
            // Stop loss must be below entry
            assertTrue("Stop loss must be below entry", plan.stopLoss < plan.entryPrice)
            // Take profit targets must be higher than entry
            assertTrue("TP1 must be higher than entry", plan.takeProfit1 > plan.entryPrice)
            assertTrue("TP2 must be higher than TP1", plan.takeProfit2 > plan.takeProfit1)
            assertTrue("TP3 must be higher than TP2", plan.takeProfit3 > plan.takeProfit2)
            // Risk reward must be >= 1.5
            assertTrue("R:R must be >= 1.5", plan.riskRewardRatio >= 1.5)
            // Max leverage must be disciplined (2x to 10x)
            assertTrue("Leverage must be between 2 and 10", plan.suggestedMaxLeverage in 2..10)
        }
    }

    @Test
    fun testRomanUrduGlossaryCoverage() {
        val requiredTerms = listOf(
            "LONG", "SHORT", "NO_TRADE", "ENTRY", "STOP_LOSS",
            "TAKE_PROFIT", "RISK_REWARD", "CONFIDENCE_SCORE", "LEVERAGE", "INVALIDATION"
        )

        for (term in requiredTerms) {
            val item = RomanUrduSignalGlossary.getTerm(term)
            assertNotNull("Glossary must contain term $term", item)
            assertTrue("Glossary item $term must have non-empty Roman Urdu summary", item!!.romanUrduSummary.isNotBlank())
            assertTrue("Glossary item $term must have non-empty Roman Urdu detail", item.romanUrduDetail.isNotBlank())
            assertTrue("Glossary item $term must have a pro trading tip", item.tradingTipRomanUrdu.isNotBlank())
        }
    }
}
