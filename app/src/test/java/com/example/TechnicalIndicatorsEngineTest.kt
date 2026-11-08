package com.example

import com.example.data.indicators.IndicatorCategory
import com.example.data.indicators.IndicatorStance
import com.example.data.indicators.TechnicalIndicatorsEngine
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class TechnicalIndicatorsEngineTest {

    private lateinit var sampleCandles: List<Candle>

    @Before
    fun setUp() {
        // Generate a realistic upward-trending OHLCV series of 250 candles
        val list = mutableListOf<Candle>()
        var price = 50000.0
        val baseTime = 1700000000000L

        for (i in 0 until 250) {
            val delta = (i % 7 - 2.8) * 120.0
            val open = price
            val close = open + delta
            val high = maxOf(open, close) + 80.0
            val low = minOf(open, close) - 80.0
            val volume = 1500.0 + (i % 5) * 200.0
            val takerBuy = volume * 0.58 // slight aggressive buy volume

            list.add(
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
        sampleCandles = list
    }

    @Test
    fun `test SMA and EMA calculations`() {
        val closes = DoubleArray(sampleCandles.size) { sampleCandles[it].close }

        val sma20 = TechnicalIndicatorsEngine.calculateSma(closes, 20)
        assertNotNull(sma20)
        assertTrue(sma20!! > 0)

        val ema9 = TechnicalIndicatorsEngine.calculateEma(closes, 9)
        val ema20 = TechnicalIndicatorsEngine.calculateEma(closes, 20)
        val ema50 = TechnicalIndicatorsEngine.calculateEma(closes, 50)
        val ema100 = TechnicalIndicatorsEngine.calculateEma(closes, 100)
        val ema200 = TechnicalIndicatorsEngine.calculateEma(closes, 200)

        assertNotNull(ema9)
        assertNotNull(ema20)
        assertNotNull(ema50)
        assertNotNull(ema100)
        assertNotNull(ema200)

        // For 250 candles, EMA 200 must be calculated
        assertTrue(ema200!! > 0)
    }

    @Test
    fun `test VWAP calculation`() {
        val vwap = TechnicalIndicatorsEngine.calculateVwap(sampleCandles)
        assertNotNull(vwap)
        assertTrue(vwap!! > 0)
        // VWAP should fall within the min and max prices of the sample
        val minP = sampleCandles.minOf { it.low }
        val maxP = sampleCandles.maxOf { it.high }
        assertTrue(vwap in minP..maxP)
    }

    @Test
    fun `test Supertrend and ADX calculations`() {
        val st = TechnicalIndicatorsEngine.calculateSupertrend(sampleCandles, period = 10, multiplier = 3.0)
        assertNotNull(st)
        assertTrue(st!!.value > 0)
        assertTrue(st.stance == IndicatorStance.BULLISH || st.stance == IndicatorStance.BEARISH)

        val adx = TechnicalIndicatorsEngine.calculateAdx(sampleCandles, period = 14)
        assertNotNull(adx)
        assertTrue(adx!!.adx in 0.0..100.0)
        assertTrue(adx.plusDI >= 0.0)
        assertTrue(adx.minusDI >= 0.0)
    }

    @Test
    fun `test Ichimoku Cloud calculation`() {
        val ichi = TechnicalIndicatorsEngine.calculateIchimoku(sampleCandles)
        assertNotNull(ichi)
        assertTrue(ichi!!.tenkanSen > 0)
        assertTrue(ichi.kijunSen > 0)
        assertTrue(ichi.senkouSpanA > 0)
        assertTrue(ichi.senkouSpanB > 0)
    }

    @Test
    fun `test Momentum RSI, MACD, Stochastic and Williams R`() {
        val closes = DoubleArray(sampleCandles.size) { sampleCandles[it].close }

        val rsi7 = TechnicalIndicatorsEngine.calculateRsi(closes, 7)
        val rsi14 = TechnicalIndicatorsEngine.calculateRsi(closes, 14)
        val rsi21 = TechnicalIndicatorsEngine.calculateRsi(closes, 21)

        assertNotNull(rsi7)
        assertNotNull(rsi14)
        assertNotNull(rsi21)
        assertTrue(rsi14!! in 0.0..100.0)

        val macd = TechnicalIndicatorsEngine.calculateMacd(closes, 12, 26, 9)
        assertNotNull(macd)
        assertEquals(macd!!.macdLine - macd.signalLine, macd.histogram, 0.001)

        val stoch = TechnicalIndicatorsEngine.calculateStochastic(sampleCandles, 14, 3, 3)
        assertNotNull(stoch)
        assertTrue(stoch!!.k in 0.0..100.0)

        val cci = TechnicalIndicatorsEngine.calculateCci(sampleCandles, 20)
        assertNotNull(cci)

        val williamsR = TechnicalIndicatorsEngine.calculateWilliamsR(sampleCandles, 14)
        assertNotNull(williamsR)
        assertTrue(williamsR!! in -100.0..0.0)

        val roc = TechnicalIndicatorsEngine.calculateRoc(closes, 14)
        assertNotNull(roc)
    }

    @Test
    fun `test Volatility Bollinger, ATR, Keltner, Donchian`() {
        val closes = DoubleArray(sampleCandles.size) { sampleCandles[it].close }

        val bb = TechnicalIndicatorsEngine.calculateBollingerBands(closes, 20, 2.0)
        assertNotNull(bb)
        assertTrue(bb!!.upper >= bb.middle)
        assertTrue(bb.middle >= bb.lower)
        assertTrue(bb.bandwidth > 0)

        val atr = TechnicalIndicatorsEngine.calculateAtr(sampleCandles, 14, closes.last())
        assertNotNull(atr)
        assertTrue(atr!!.value > 0)

        val keltner = TechnicalIndicatorsEngine.calculateKeltnerChannels(sampleCandles, 20, 10, 2.0)
        assertNotNull(keltner)
        assertTrue(keltner!!.upper >= keltner.lower)

        val donchian = TechnicalIndicatorsEngine.calculateDonchianChannels(sampleCandles, 20)
        assertNotNull(donchian)
        assertTrue(donchian!!.upper >= donchian.lower)
    }

    @Test
    fun `test Volume Indicators OBV, MFI, CMF, RVOL and Volume Delta`() {
        val obv = TechnicalIndicatorsEngine.calculateObv(sampleCandles)
        assertNotNull(obv)

        val mfi = TechnicalIndicatorsEngine.calculateMfi(sampleCandles, 14)
        assertNotNull(mfi)
        assertTrue(mfi!! in 0.0..100.0)

        val cmf = TechnicalIndicatorsEngine.calculateCmf(sampleCandles, 20)
        assertNotNull(cmf)

        val volumes = DoubleArray(sampleCandles.size) { sampleCandles[it].volume }
        val rvol = TechnicalIndicatorsEngine.calculateRvol(volumes, 20)
        assertNotNull(rvol)
        assertTrue(rvol!! > 0.0)

        val vd = TechnicalIndicatorsEngine.calculateVolumeDelta(sampleCandles)
        assertNotNull(vd)
        assertTrue(vd!!.hasReliableData)
        assertTrue(vd.delta != 0.0)
    }

    @Test
    fun `test Insufficient historical data handling`() = runBlocking {
        // Provide only 15 candles
        val shortSeries = sampleCandles.take(15)

        val analysis = TechnicalIndicatorsEngine.analyze(
            symbol = "BTC",
            assetName = "Bitcoin",
            timeframe = CandleTimeframe.H1,
            candles = shortSeries
        )

        assertEquals("BTC", analysis.symbol)
        assertEquals(15, analysis.candleCount)
        assertTrue(analysis.warnings.isNotEmpty())

        // 200-period EMA must be null and marked as Need Data / unavailable
        assertNull(analysis.trend.emaSet.ema200)
        val ema200Card = analysis.cards.first { it.id == "ema_200" }
        assertEquals(IndicatorStance.INSUFFICIENT_DATA, ema200Card.stance)
        assertEquals(false, ema200Card.isAvailable)

        // But 9-period EMA should still be available!
        assertNotNull(analysis.trend.emaSet.ema9)
        val ema9Card = analysis.cards.first { it.id == "ema_9" }
        assertTrue(ema9Card.isAvailable)
    }
}
