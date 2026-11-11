package com.example

import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.smc.FvgStatus
import com.example.data.smc.FvgType
import com.example.data.smc.LiquidityPoolType
import com.example.data.smc.MarketStructureBreakType
import com.example.data.smc.MarketTrendState
import com.example.data.smc.OrderBlockStatus
import com.example.data.smc.OrderBlockType
import com.example.data.smc.RomanUrduSMCGlossary
import com.example.data.smc.SmcCalculationEngine
import com.example.data.smc.SwingType
import com.example.data.smc.TimeframeSmcSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmcCalculationEngineTest {

    private fun generateTrendingCandles(count: Int, startPrice: Double, step: Double): List<Candle> {
        val candles = mutableListOf<Candle>()
        var price = startPrice
        val now = 1700000000000L

        for (i in 0 until count) {
            val isGreen = i % 2 == 0 || step > 0
            val open = price
            val close = open + step
            val high = maxOf(open, close) + 2.0
            val low = minOf(open, close) - 2.0
            candles.add(
                Candle(
                    openTime = now + (i * 60000L),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = 1000.0 + (i * 10.0),
                    takerBuyBaseVolume = 600.0
                )
            )
            price = close
        }
        return candles
    }

    private fun generateSwingCycleCandles(): List<Candle> {
        val candles = mutableListOf<Candle>()
        val now = 1700000000000L
        val prices = listOf(
            100.0, 104.0, 108.0, 114.0, 122.0, 118.0, 112.0, 106.0,
            110.0, 118.0, 128.0, 138.0, 146.0, 140.0, 132.0, 124.0,
            128.0, 136.0, 148.0, 158.0, 168.0, 160.0, 150.0, 138.0,
            142.0, 152.0, 164.0, 175.0, 185.0, 178.0, 165.0, 155.0
        )
        for (i in 0 until prices.size - 1) {
            val open = prices[i]
            val close = prices[i + 1]
            val isPeak = (i in listOf(4, 12, 20, 28))
            val isTrough = (i in listOf(7, 15, 23))
            val high = maxOf(open, close) + (if (isPeak) 4.0 else 1.0)
            val low = minOf(open, close) - (if (isTrough) 4.0 else 1.0)
            candles.add(
                Candle(
                    openTime = now + (i * 60000L),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = 1500.0,
                    takerBuyBaseVolume = 900.0
                )
            )
        }
        return candles
    }

    @Test
    fun testMarketStructureDetection() = runBlocking {
        val candles = generateSwingCycleCandles()
        val result = SmcCalculationEngine.analyze("BTC", "Bitcoin", CandleTimeframe.H1, candles)

        assertFalse(result.isInsufficientData)
        assertTrue(result.marketStructure.swingPoints.isNotEmpty())

        val highSwings = result.marketStructure.swingPoints.filter { it.isHigh }
        val lowSwings = result.marketStructure.swingPoints.filter { !it.isHigh }

        assertTrue(highSwings.isNotEmpty())
        assertTrue(lowSwings.isNotEmpty())

        // Confirms presence of Higher High or Higher Low in uptrend wave
        val hasHhOrHl = result.marketStructure.swingPoints.any {
            it.type == SwingType.HIGHER_HIGH || it.type == SwingType.HIGHER_LOW || it.type == SwingType.SWING_HIGH
        }
        assertTrue(hasHhOrHl)
    }

    @Test
    fun testFibonacciRetracementAndExtension() = runBlocking {
        val candles = generateSwingCycleCandles()
        val result = SmcCalculationEngine.analyze("BTC", "Bitcoin", CandleTimeframe.H1, candles)

        val fib = result.fibonacci
        assertNotNull(fib)
        assertTrue(fib!!.swingHighPrice > fib.swingLowPrice)

        // Verify key retracement ratios: 0.236, 0.382, 0.500, 0.618, 0.786
        val ratios = fib.retracementLevels.map { it.ratio }
        assertTrue(ratios.contains(0.236))
        assertTrue(ratios.contains(0.382))
        assertTrue(ratios.contains(0.500))
        assertTrue(ratios.contains(0.618))
        assertTrue(ratios.contains(0.786))

        // Verify key extension ratios: 1.272, 1.618, 2.618
        val extRatios = fib.extensionLevels.map { it.ratio }
        assertTrue(extRatios.contains(1.272))
        assertTrue(extRatios.contains(1.618))
        assertTrue(extRatios.contains(2.618))

        // Verify Golden Pocket calculation bounds
        assertTrue(fib.goldenPocketHigh >= fib.goldenPocketLow)
    }

    @Test
    fun testFairValueGapsDetection() = runBlocking {
        // Create an explicit gap:
        // Candle 1: Low 100, High 105
        // Candle 2: Open 105, Close 120, Low 105, High 121 (Big green displacement)
        // Candle 3: Low 112, High 125, Open 120, Close 124
        // Gap exists between Candle 1 High (105) and Candle 3 Low (112)
        val now = 1700000000000L
        val candles = mutableListOf<Candle>()
        for (i in 0..15) {
            candles.add(Candle(now + (i * 60000L), 95.0 + i, 97.0 + i, 93.0 + i, 96.0 + i, 500.0))
        }

        candles.add(Candle(now + 1000000L, 100.0, 105.0, 99.0, 104.0, 1000.0)) // C1
        candles.add(Candle(now + 1060000L, 105.0, 121.0, 105.0, 120.0, 4500.0)) // C2: Displacement
        candles.add(Candle(now + 1120000L, 120.0, 125.0, 112.0, 124.0, 2000.0)) // C3: Gap between 105 and 112

        val result = SmcCalculationEngine.analyze("BTC", "Bitcoin", CandleTimeframe.H1, candles)
        val bullFvgs = result.fairValueGaps.filter { it.type == FvgType.BULLISH }
        assertTrue("Expected at least 1 Bullish FVG", bullFvgs.isNotEmpty())
    }

    @Test
    fun testLiquidityAnalysisAndSweeps() = runBlocking {
        val candles = generateSwingCycleCandles()
        val result = SmcCalculationEngine.analyze("BTC", "Bitcoin", CandleTimeframe.H1, candles)

        val liquidity = result.liquidity
        assertNotNull(liquidity)
        // Pools should be populated from swing points
        assertTrue(liquidity.pools.isNotEmpty())
    }

    @Test
    fun testMultiTimeframeConfluence() = runBlocking {
        val candles = generateSwingCycleCandles()
        val mockSnapshots = listOf(
            TimeframeSmcSnapshot(
                timeframe = CandleTimeframe.H4,
                trend = MarketTrendState.BULLISH_UPTREND,
                lastBreakType = MarketStructureBreakType.BOS_BULLISH,
                unmitigatedOBCount = 2,
                unfilledFvgCount = 1,
                nearestSupport = 120.0,
                nearestResistance = 160.0,
                isBullishBias = true
            ),
            TimeframeSmcSnapshot(
                timeframe = CandleTimeframe.H1,
                trend = MarketTrendState.BULLISH_UPTREND,
                lastBreakType = MarketStructureBreakType.BOS_BULLISH,
                unmitigatedOBCount = 3,
                unfilledFvgCount = 2,
                nearestSupport = 125.0,
                nearestResistance = 160.0,
                isBullishBias = true
            ),
            TimeframeSmcSnapshot(
                timeframe = CandleTimeframe.M15,
                trend = MarketTrendState.NEUTRAL_RANGING,
                lastBreakType = null,
                unmitigatedOBCount = 1,
                unfilledFvgCount = 1,
                nearestSupport = 128.0,
                nearestResistance = 145.0,
                isBullishBias = true
            )
        )

        val result = SmcCalculationEngine.analyze(
            symbol = "BTC",
            assetName = "Bitcoin",
            timeframe = CandleTimeframe.H1,
            candles = candles,
            allTimeframeSnapshots = mockSnapshots
        )

        assertNotNull(result.multiTimeframe)
        assertEquals(3, result.multiTimeframe!!.bullishTimeframeCount)
        assertTrue(result.multiTimeframe!!.agreementSummary.contains("3/3"))
    }

    @Test
    fun testInsufficientDataSafety() = runBlocking {
        // Less than 15 candles should degrade gracefully without throwing
        val fewCandles = listOf(
            Candle(1000L, 100.0, 102.0, 99.0, 101.0, 50.0),
            Candle(2000L, 101.0, 103.0, 100.0, 102.0, 50.0)
        )

        val result = SmcCalculationEngine.analyze("BTC", "Bitcoin", CandleTimeframe.H1, fewCandles)
        assertTrue(result.isInsufficientData)
        assertEquals(2, result.candleCount)
    }

    @Test
    fun testRomanUrduGlossaryCoverage() {
        val requiredTerms = listOf(
            "BOS", "CHOCH", "HH_HL", "LH_LL", "BULLISH_OB", "BEARISH_OB",
            "MITIGATION", "FVG", "BSL", "SSL", "EQH_EQL", "LIQUIDITY_SWEEP",
            "STOP_HUNT", "SUPPLY_DEMAND", "FIB_RETRACEMENT", "GOLDEN_POCKET",
            "FIB_EXTENSION", "MTF_CONFLUENCE"
        )

        val allItems = RomanUrduSMCGlossary.getAllItems()
        assertTrue(allItems.size >= 18)

        for (term in requiredTerms) {
            val item = RomanUrduSMCGlossary.getItem(term)
            assertNotNull("Glossary item missing for $term", item)
            assertTrue("Roman Urdu summary should not be empty for $term", item.romanUrduSummary.isNotBlank())
            assertTrue("Roman Urdu detail should not be empty for $term", item.romanUrduDetail.isNotBlank())
            assertTrue("Trading tip should not be empty for $term", item.tradingTipRomanUrdu.isNotBlank())
        }
    }
}
