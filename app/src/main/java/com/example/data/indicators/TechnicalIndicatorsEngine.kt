package com.example.data.indicators

import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * High-performance, pure Kotlin Technical Indicators Engine.
 * Calculates Trend, Momentum, Volatility, and Volume indicators from real historical OHLCV data.
 * Designed to execute on background threads and maintain ultra-low RAM usage.
 */
object TechnicalIndicatorsEngine {

    suspend fun analyze(
        symbol: String,
        assetName: String,
        timeframe: CandleTimeframe,
        candles: List<Candle>
    ): OverallAnalysisResult = withContext(Dispatchers.Default) {
        val warnings = mutableListOf<String>()
        val count = candles.size

        if (count == 0) {
            return@withContext createEmptyAnalysis(symbol, assetName, timeframe)
        }

        val closes = DoubleArray(count) { candles[it].close }
        val volumes = DoubleArray(count) { candles[it].volume }

        val currentClose = closes.last()
        val prevClose = if (count > 1) closes[count - 2] else currentClose
        val priceChangePercent = if (prevClose > 0) ((currentClose - prevClose) / prevClose) * 100.0 else 0.0

        if (count < 200) {
            warnings.add("Loaded $count candles. 200-period indicators require 200+ candles.")
        }
        if (count < 50) {
            warnings.add("Small candle sample ($count). Short-term indicators prioritized.")
        }

        // ==========================================
        // 1. TREND INDICATORS
        // ==========================================
        val ema9 = calculateEma(closes, 9)
        val ema20 = calculateEma(closes, 20)
        val ema50 = calculateEma(closes, 50)
        val ema100 = calculateEma(closes, 100)
        val ema200 = calculateEma(closes, 200)
        val emaSet = EmaSet(ema9, ema20, ema50, ema100, ema200)

        val sma20 = calculateSma(closes, 20)
        val sma50 = calculateSma(closes, 50)
        val sma100 = calculateSma(closes, 100)
        val sma200 = calculateSma(closes, 200)
        val smaSet = SmaSet(sma20, sma50, sma100, sma200)

        val vwap = calculateVwap(candles)
        val vwapStance = when {
            vwap == null -> IndicatorStance.INSUFFICIENT_DATA
            currentClose > vwap * 1.002 -> IndicatorStance.BULLISH
            currentClose < vwap * 0.998 -> IndicatorStance.BEARISH
            else -> IndicatorStance.NEUTRAL
        }

        val supertrend = calculateSupertrend(candles, 10, 3.0)
        val adx = calculateAdx(candles, 14)
        val ichimoku = calculateIchimoku(candles)

        val trend = TrendIndicators(
            emaSet = emaSet,
            smaSet = smaSet,
            vwap = vwap,
            vwapStance = vwapStance,
            supertrend = supertrend,
            adx = adx,
            ichimoku = ichimoku
        )

        // ==========================================
        // 2. MOMENTUM INDICATORS
        // ==========================================
        val rsi7 = calculateRsi(closes, 7)
        val rsi14 = calculateRsi(closes, 14)
        val rsi21 = calculateRsi(closes, 21)

        val macd = calculateMacd(closes, 12, 26, 9)
        val stochRsi = calculateStochRsi(closes, 14, 14, 3, 3)
        val stochastic = calculateStochastic(candles, 14, 3, 3)
        val cci = calculateCci(candles, 20)
        val cciStance = when {
            cci == null -> IndicatorStance.INSUFFICIENT_DATA
            cci > 100.0 -> IndicatorStance.OVERBOUGHT
            cci < -100.0 -> IndicatorStance.OVERSOLD
            cci > 0.0 -> IndicatorStance.BULLISH
            else -> IndicatorStance.BEARISH
        }

        val williamsR = calculateWilliamsR(candles, 14)
        val williamsRStance = when {
            williamsR == null -> IndicatorStance.INSUFFICIENT_DATA
            williamsR > -20.0 -> IndicatorStance.OVERBOUGHT
            williamsR < -80.0 -> IndicatorStance.OVERSOLD
            williamsR > -50.0 -> IndicatorStance.BULLISH
            else -> IndicatorStance.BEARISH
        }

        val roc = calculateRoc(closes, 14)
        val rocStance = when {
            roc == null -> IndicatorStance.INSUFFICIENT_DATA
            roc > 0.5 -> IndicatorStance.BULLISH
            roc < -0.5 -> IndicatorStance.BEARISH
            else -> IndicatorStance.NEUTRAL
        }

        val momentum = MomentumIndicators(
            rsi7 = rsi7,
            rsi7Stance = stanceForRsi(rsi7),
            rsi14 = rsi14,
            rsi14Stance = stanceForRsi(rsi14),
            rsi21 = rsi21,
            rsi21Stance = stanceForRsi(rsi21),
            macd = macd,
            stochRsi = stochRsi,
            stochastic = stochastic,
            cci = cci,
            cciStance = cciStance,
            williamsR = williamsR,
            williamsRStance = williamsRStance,
            roc = roc,
            rocStance = rocStance
        )

        // ==========================================
        // 3. VOLATILITY INDICATORS
        // ==========================================
        val bollinger = calculateBollingerBands(closes, 20, 2.0)
        val atr = calculateAtr(candles, 14, currentClose)
        val keltner = calculateKeltnerChannels(candles, 20, 10, 2.0)
        val donchian = calculateDonchianChannels(candles, 20)

        val volatility = VolatilityIndicators(
            bollingerBands = bollinger,
            atr = atr,
            keltnerChannels = keltner,
            donchianChannels = donchian
        )

        // ==========================================
        // 4. VOLUME INDICATORS
        // ==========================================
        val obv = calculateObv(candles)
        val mfi = calculateMfi(candles, 14)
        val mfiStance = when {
            mfi == null -> IndicatorStance.INSUFFICIENT_DATA
            mfi > 80.0 -> IndicatorStance.OVERBOUGHT
            mfi < 20.0 -> IndicatorStance.OVERSOLD
            mfi > 50.0 -> IndicatorStance.BULLISH
            else -> IndicatorStance.BEARISH
        }

        val cmf = calculateCmf(candles, 20)
        val cmfStance = when {
            cmf == null -> IndicatorStance.INSUFFICIENT_DATA
            cmf > 0.05 -> IndicatorStance.STRONG_BULLISH
            cmf > 0.0 -> IndicatorStance.BULLISH
            cmf < -0.05 -> IndicatorStance.STRONG_BEARISH
            cmf < 0.0 -> IndicatorStance.BEARISH
            else -> IndicatorStance.NEUTRAL
        }

        val rvol = calculateRvol(volumes, 20)
        val rvolStance = when {
            rvol == null -> IndicatorStance.INSUFFICIENT_DATA
            rvol >= 2.0 -> IndicatorStance.STRONG_BULLISH
            rvol >= 1.2 -> IndicatorStance.BULLISH
            rvol <= 0.6 -> IndicatorStance.BEARISH
            else -> IndicatorStance.NEUTRAL
        }

        val volumeDelta = calculateVolumeDelta(candles)

        val volume = VolumeIndicators(
            obv = obv,
            mfi = mfi,
            mfiStance = mfiStance,
            cmf = cmf,
            cmfStance = cmfStance,
            relativeVolume = rvol,
            relativeVolumeStance = rvolStance,
            volumeDelta = volumeDelta
        )

        val cards = buildCardsList(
            currentClose = currentClose,
            trend = trend,
            momentum = momentum,
            volatility = volatility,
            volume = volume,
            candleCount = count
        )

        var bullishVotes = 0
        var bearishVotes = 0
        var neutralVotes = 0

        for (c in cards) {
            if (!c.isAvailable) continue
            when (c.stance) {
                IndicatorStance.STRONG_BULLISH -> bullishVotes += 2
                IndicatorStance.BULLISH, IndicatorStance.OVERSOLD -> bullishVotes += 1
                IndicatorStance.STRONG_BEARISH -> bearishVotes += 2
                IndicatorStance.BEARISH, IndicatorStance.OVERBOUGHT -> bearishVotes += 1
                IndicatorStance.NEUTRAL -> neutralVotes += 1
                IndicatorStance.INSUFFICIENT_DATA -> {}
            }
        }

        val totalVotes = bullishVotes + bearishVotes + neutralVotes
        val overallStance = when {
            totalVotes == 0 -> IndicatorStance.NEUTRAL
            bullishVotes >= bearishVotes * 2 && bullishVotes > 6 -> IndicatorStance.STRONG_BULLISH
            bullishVotes > bearishVotes + 2 -> IndicatorStance.BULLISH
            bearishVotes >= bullishVotes * 2 && bearishVotes > 6 -> IndicatorStance.STRONG_BEARISH
            bearishVotes > bullishVotes + 2 -> IndicatorStance.BEARISH
            else -> IndicatorStance.NEUTRAL
        }

        val summaryText = buildSummaryNarrative(
            symbol = symbol,
            timeframe = timeframe,
            overallStance = overallStance,
            bullish = bullishVotes,
            bearish = bearishVotes,
            neutral = neutralVotes,
            rsi14 = rsi14,
            macd = macd,
            supertrend = supertrend,
            adx = adx
        )

        OverallAnalysisResult(
            symbol = symbol,
            assetName = assetName,
            timeframe = timeframe,
            candleCount = count,
            currentClose = currentClose,
            priceChangePercent = priceChangePercent,
            stance = overallStance,
            summaryText = summaryText,
            bullishCount = bullishVotes,
            bearishCount = bearishVotes,
            neutralCount = neutralVotes,
            trend = trend,
            momentum = momentum,
            volatility = volatility,
            volume = volume,
            cards = cards,
            warnings = warnings
        )
    }

    // =========================================================================
    // MATHEMATICAL ROUTINES
    // =========================================================================

    fun calculateSma(data: DoubleArray, period: Int): Double? {
        if (data.size < period || period <= 0) return null
        var sum = 0.0
        val start = data.size - period
        for (i in start until data.size) {
            sum += data[i]
        }
        return sum / period
    }

    fun calculateEma(data: DoubleArray, period: Int): Double? {
        if (data.size < period || period <= 0) return null
        var sum = 0.0
        for (i in 0 until period) {
            sum += data[i]
        }
        var ema = sum / period
        val alpha = 2.0 / (period + 1.0)
        for (i in period until data.size) {
            ema = data[i] * alpha + ema * (1.0 - alpha)
        }
        return ema
    }

    fun calculateEmaSeries(data: DoubleArray, period: Int): DoubleArray? {
        if (data.size < period || period <= 0) return null
        val result = DoubleArray(data.size)
        var sum = 0.0
        for (i in 0 until period) {
            sum += data[i]
            result[i] = sum / (i + 1)
        }
        var ema = sum / period
        result[period - 1] = ema
        val alpha = 2.0 / (period + 1.0)
        for (i in period until data.size) {
            ema = data[i] * alpha + ema * (1.0 - alpha)
            result[i] = ema
        }
        return result
    }

    fun calculateVwap(candles: List<Candle>): Double? {
        if (candles.isEmpty()) return null
        var cumVolPrice = 0.0
        var cumVol = 0.0
        for (c in candles) {
            val tp = c.typicalPrice
            cumVolPrice += tp * c.volume
            cumVol += c.volume
        }
        return if (cumVol > 0.0) cumVolPrice / cumVol else null
    }

    fun calculateSupertrend(candles: List<Candle>, period: Int = 10, multiplier: Double = 3.0): SupertrendResult? {
        if (candles.size < period + 1) return null

        val count = candles.size
        val atrSeries = calculateAtrSeries(candles, period) ?: return null

        var prevUpper = 0.0
        var prevLower = 0.0
        var prevSupertrend = 0.0
        var isBullish = true

        for (i in period until count) {
            val hl2 = (candles[i].high + candles[i].low) / 2.0
            val atr = atrSeries[i]
            val basicUpper = hl2 + (multiplier * atr)
            val basicLower = hl2 - (multiplier * atr)

            val currentUpper = if (i == period || basicUpper < prevUpper || candles[i - 1].close > prevUpper) {
                basicUpper
            } else {
                prevUpper
            }

            val currentLower = if (i == period || basicLower > prevLower || candles[i - 1].close < prevLower) {
                basicLower
            } else {
                prevLower
            }

            if (i == period) {
                isBullish = candles[i].close >= currentLower
                prevSupertrend = if (isBullish) currentLower else currentUpper
            } else {
                if (prevSupertrend == prevUpper) {
                    isBullish = candles[i].close > currentUpper
                } else {
                    isBullish = candles[i].close >= currentLower
                }
                prevSupertrend = if (isBullish) currentLower else currentUpper
            }

            prevUpper = currentUpper
            prevLower = currentLower
        }

        val stance = if (isBullish) IndicatorStance.BULLISH else IndicatorStance.BEARISH
        return SupertrendResult(
            value = prevSupertrend,
            isBullish = isBullish,
            upperBand = prevUpper,
            lowerBand = prevLower,
            stance = stance
        )
    }

    fun calculateAdx(candles: List<Candle>, period: Int = 14): AdxResult? {
        if (candles.size < 2 * period + 1) return null
        val count = candles.size

        val tr = DoubleArray(count)
        val plusDm = DoubleArray(count)
        val minusDm = DoubleArray(count)

        for (i in 1 until count) {
            val h = candles[i].high
            val l = candles[i].low
            val prevH = candles[i - 1].high
            val prevL = candles[i - 1].low
            val prevC = candles[i - 1].close

            val upMove = h - prevH
            val downMove = prevL - l

            plusDm[i] = if (upMove > downMove && upMove > 0) upMove else 0.0
            minusDm[i] = if (downMove > upMove && downMove > 0) downMove else 0.0

            val tr1 = h - l
            val tr2 = abs(h - prevC)
            val tr3 = abs(l - prevC)
            tr[i] = max(tr1, max(tr2, tr3))
        }

        var smoothTr = 0.0
        var smoothPlusDm = 0.0
        var smoothMinusDm = 0.0

        for (i in 1..period) {
            smoothTr += tr[i]
            smoothPlusDm += plusDm[i]
            smoothMinusDm += minusDm[i]
        }

        val dxList = mutableListOf<Double>()

        for (i in (period + 1) until count) {
            smoothTr = smoothTr - (smoothTr / period) + tr[i]
            smoothPlusDm = smoothPlusDm - (smoothPlusDm / period) + plusDm[i]
            smoothMinusDm = smoothMinusDm - (smoothMinusDm / period) + minusDm[i]

            val plusDI = if (smoothTr > 0.0) (smoothPlusDm / smoothTr) * 100.0 else 0.0
            val minusDI = if (smoothTr > 0.0) (smoothMinusDm / smoothTr) * 100.0 else 0.0

            val diSum = plusDI + minusDI
            val dx = if (diSum > 0.0) (abs(plusDI - minusDI) / diSum) * 100.0 else 0.0
            dxList.add(dx)
        }

        if (dxList.size < period) return null

        var adx = dxList.take(period).average()
        for (i in period until dxList.size) {
            adx = (adx * (period - 1) + dxList[i]) / period
        }

        val lastPlusDI = if (smoothTr > 0.0) (smoothPlusDm / smoothTr) * 100.0 else 0.0
        val lastMinusDI = if (smoothTr > 0.0) (smoothMinusDm / smoothTr) * 100.0 else 0.0

        val (stance, desc) = when {
            adx >= 25.0 && lastPlusDI > lastMinusDI -> Pair(IndicatorStance.STRONG_BULLISH, "Strong Bullish Trend")
            adx >= 25.0 && lastMinusDI > lastPlusDI -> Pair(IndicatorStance.STRONG_BEARISH, "Strong Bearish Trend")
            adx < 20.0 -> Pair(IndicatorStance.NEUTRAL, "Ranging / Weak Trend")
            lastPlusDI > lastMinusDI -> Pair(IndicatorStance.BULLISH, "Mild Bullish Trend")
            else -> Pair(IndicatorStance.BEARISH, "Mild Bearish Trend")
        }

        return AdxResult(
            adx = adx,
            plusDI = lastPlusDI,
            minusDI = lastMinusDI,
            stance = stance,
            strengthDescription = desc
        )
    }

    fun calculateIchimoku(candles: List<Candle>): IchimokuResult? {
        if (candles.size < 52) return null
        val count = candles.size

        fun midPrice(startIdx: Int, endIdx: Int): Double {
            var highest = Double.NEGATIVE_INFINITY
            var lowest = Double.POSITIVE_INFINITY
            for (i in startIdx..endIdx) {
                if (candles[i].high > highest) highest = candles[i].high
                if (candles[i].low < lowest) lowest = candles[i].low
            }
            return (highest + lowest) / 2.0
        }

        val tenkanSen = midPrice(count - 9, count - 1)
        val kijunSen = midPrice(count - 26, count - 1)
        val senkouSpanA = (tenkanSen + kijunSen) / 2.0
        val senkouSpanB = midPrice(count - 52, count - 1)
        val chikouSpan = candles.last().close

        val currentClose = candles.last().close
        val (stance, desc) = when {
            currentClose > senkouSpanA && currentClose > senkouSpanB && senkouSpanA > senkouSpanB ->
                Pair(IndicatorStance.STRONG_BULLISH, "Above Green Cloud (Kumo Breakout)")
            currentClose < senkouSpanA && currentClose < senkouSpanB && senkouSpanA < senkouSpanB ->
                Pair(IndicatorStance.STRONG_BEARISH, "Below Red Cloud (Bearish Dominance)")
            currentClose > max(senkouSpanA, senkouSpanB) ->
                Pair(IndicatorStance.BULLISH, "Above Cloud")
            currentClose < min(senkouSpanA, senkouSpanB) ->
                Pair(IndicatorStance.BEARISH, "Below Cloud")
            else ->
                Pair(IndicatorStance.NEUTRAL, "Inside Cloud (Consolidation)")
        }

        return IchimokuResult(
            tenkanSen = tenkanSen,
            kijunSen = kijunSen,
            senkouSpanA = senkouSpanA,
            senkouSpanB = senkouSpanB,
            chikouSpan = chikouSpan,
            stance = stance,
            cloudDescription = desc
        )
    }

    fun calculateRsi(data: DoubleArray, period: Int): Double? {
        if (data.size < period + 1 || period <= 0) return null

        var avgGain = 0.0
        var avgLoss = 0.0

        for (i in 1..period) {
            val change = data[i] - data[i - 1]
            if (change > 0) avgGain += change else avgLoss += -change
        }

        avgGain /= period
        avgLoss /= period

        for (i in (period + 1) until data.size) {
            val change = data[i] - data[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    fun calculateRsiSeries(data: DoubleArray, period: Int): DoubleArray? {
        if (data.size < period + 1 || period <= 0) return null
        val rsiSeries = DoubleArray(data.size)

        var avgGain = 0.0
        var avgLoss = 0.0

        for (i in 1..period) {
            val change = data[i] - data[i - 1]
            if (change > 0) avgGain += change else avgLoss += -change
        }
        avgGain /= period
        avgLoss /= period

        rsiSeries[period] = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + (avgGain / avgLoss)))

        for (i in (period + 1) until data.size) {
            val change = data[i] - data[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period

            rsiSeries[i] = if (avgLoss == 0.0) 100.0 else 100.0 - (100.0 / (1.0 + (avgGain / avgLoss)))
        }

        return rsiSeries
    }

    fun calculateMacd(data: DoubleArray, fast: Int = 12, slow: Int = 26, signalPeriod: Int = 9): MacdResult? {
        if (data.size < slow + signalPeriod) return null

        val fastEma = calculateEmaSeries(data, fast) ?: return null
        val slowEma = calculateEmaSeries(data, slow) ?: return null

        val macdLineSeries = DoubleArray(data.size)
        for (i in (slow - 1) until data.size) {
            macdLineSeries[i] = fastEma[i] - slowEma[i]
        }

        val macdSlice = macdLineSeries.copyOfRange(slow - 1, data.size)
        val signalLine = calculateEma(macdSlice, signalPeriod) ?: return null
        val macdLine = macdLineSeries.last()
        val histogram = macdLine - signalLine

        val (stance, crossover) = when {
            histogram > 0 && macdLine > 0 -> Pair(IndicatorStance.STRONG_BULLISH, "Bullish Momentum Above Zero")
            histogram > 0 -> Pair(IndicatorStance.BULLISH, "Bullish Crossover Below Zero")
            histogram < 0 && macdLine < 0 -> Pair(IndicatorStance.STRONG_BEARISH, "Bearish Momentum Below Zero")
            histogram < 0 -> Pair(IndicatorStance.BEARISH, "Bearish Divergence / Turn")
            else -> Pair(IndicatorStance.NEUTRAL, "Neutral Crossover")
        }

        return MacdResult(
            macdLine = macdLine,
            signalLine = signalLine,
            histogram = histogram,
            stance = stance,
            crossoverStatus = crossover
        )
    }

    fun calculateStochRsi(
        data: DoubleArray,
        rsiPeriod: Int = 14,
        stochPeriod: Int = 14,
        kPeriod: Int = 3,
        dPeriod: Int = 3
    ): StochRsiResult? {
        val rsiSeries = calculateRsiSeries(data, rsiPeriod) ?: return null
        val validStart = rsiPeriod
        if (data.size - validStart < stochPeriod + kPeriod + dPeriod) return null

        val stochRsiRaw = mutableListOf<Double>()
        for (i in (validStart + stochPeriod) until data.size) {
            val window = rsiSeries.copyOfRange(i - stochPeriod + 1, i + 1)
            val minRsi = window.minOrNull() ?: 0.0
            val maxRsi = window.maxOrNull() ?: 100.0
            val diff = maxRsi - minRsi
            val currentRsi = rsiSeries[i]
            val valStoch = if (diff > 0.0) ((currentRsi - minRsi) / diff) * 100.0 else 50.0
            stochRsiRaw.add(valStoch)
        }

        if (stochRsiRaw.size < kPeriod + dPeriod) return null

        val kValues = mutableListOf<Double>()
        for (i in (kPeriod - 1) until stochRsiRaw.size) {
            val avgK = stochRsiRaw.subList(i - kPeriod + 1, i + 1).average()
            kValues.add(avgK)
        }

        if (kValues.size < dPeriod) return null
        val k = kValues.last()
        val d = kValues.takeLast(dPeriod).average()

        val stance = when {
            k >= 80.0 -> IndicatorStance.OVERBOUGHT
            k <= 20.0 -> IndicatorStance.OVERSOLD
            k > d -> IndicatorStance.BULLISH
            else -> IndicatorStance.BEARISH
        }

        return StochRsiResult(k = k, d = d, stance = stance)
    }

    fun calculateStochastic(
        candles: List<Candle>,
        kPeriod: Int = 14,
        slowing: Int = 3,
        dPeriod: Int = 3
    ): StochasticResult? {
        if (candles.size < kPeriod + slowing + dPeriod) return null

        val fastKList = mutableListOf<Double>()
        for (i in (kPeriod - 1) until candles.size) {
            var highest = Double.NEGATIVE_INFINITY
            var lowest = Double.POSITIVE_INFINITY
            for (j in (i - kPeriod + 1)..i) {
                if (candles[j].high > highest) highest = candles[j].high
                if (candles[j].low < lowest) lowest = candles[j].low
            }
            val range = highest - lowest
            val close = candles[i].close
            val fk = if (range > 0.0) ((close - lowest) / range) * 100.0 else 50.0
            fastKList.add(fk)
        }

        if (fastKList.size < slowing + dPeriod) return null

        val slowKList = mutableListOf<Double>()
        for (i in (slowing - 1) until fastKList.size) {
            val avg = fastKList.subList(i - slowing + 1, i + 1).average()
            slowKList.add(avg)
        }

        if (slowKList.size < dPeriod) return null
        val k = slowKList.last()
        val d = slowKList.takeLast(dPeriod).average()

        val stance = when {
            k >= 80.0 -> IndicatorStance.OVERBOUGHT
            k <= 20.0 -> IndicatorStance.OVERSOLD
            k > d -> IndicatorStance.BULLISH
            else -> IndicatorStance.BEARISH
        }

        return StochasticResult(k = k, d = d, stance = stance)
    }

    fun calculateCci(candles: List<Candle>, period: Int = 20): Double? {
        if (candles.size < period) return null
        val count = candles.size
        val tps = DoubleArray(period) { i ->
            candles[count - period + i].typicalPrice
        }
        val smaTp = tps.average()
        var meanDev = 0.0
        for (tp in tps) {
            meanDev += abs(tp - smaTp)
        }
        meanDev /= period
        if (meanDev == 0.0) return 0.0
        val currentTp = candles.last().typicalPrice
        return (currentTp - smaTp) / (0.015 * meanDev)
    }

    fun calculateWilliamsR(candles: List<Candle>, period: Int = 14): Double? {
        if (candles.size < period) return null
        val count = candles.size
        var highest = Double.NEGATIVE_INFINITY
        var lowest = Double.POSITIVE_INFINITY
        for (i in (count - period) until count) {
            if (candles[i].high > highest) highest = candles[i].high
            if (candles[i].low < lowest) lowest = candles[i].low
        }
        val range = highest - lowest
        if (range <= 0.0) return -50.0
        val currentClose = candles.last().close
        return ((highest - currentClose) / range) * -100.0
    }

    fun calculateRoc(data: DoubleArray, period: Int = 14): Double? {
        if (data.size <= period) return null
        val past = data[data.size - 1 - period]
        if (past == 0.0) return 0.0
        val current = data.last()
        return ((current - past) / past) * 100.0
    }

    fun calculateBollingerBands(data: DoubleArray, period: Int = 20, multiplier: Double = 2.0): BollingerResult? {
        if (data.size < period) return null
        val middle = calculateSma(data, period) ?: return null
        var variance = 0.0
        val start = data.size - period
        for (i in start until data.size) {
            val diff = data[i] - middle
            variance += diff * diff
        }
        val stdDev = sqrt(variance / period)
        val upper = middle + (multiplier * stdDev)
        val lower = middle - (multiplier * stdDev)
        val bandwidth = if (middle > 0.0) ((upper - lower) / middle) * 100.0 else 0.0
        val close = data.last()
        val bandDiff = upper - lower
        val percentB = if (bandDiff > 0.0) (close - lower) / bandDiff else 0.5
        val isSqueeze = bandwidth < 4.0

        val stance = when {
            close > upper -> IndicatorStance.OVERBOUGHT
            close < lower -> IndicatorStance.OVERSOLD
            close > middle -> IndicatorStance.BULLISH
            else -> IndicatorStance.BEARISH
        }

        return BollingerResult(
            upper = upper,
            middle = middle,
            lower = lower,
            bandwidth = bandwidth,
            percentB = percentB,
            isSqueeze = isSqueeze,
            stance = stance
        )
    }

    fun calculateAtr(candles: List<Candle>, period: Int = 14, currentClose: Double): AtrResult? {
        val series = calculateAtrSeries(candles, period) ?: return null
        val atr = series.last()
        val percent = if (currentClose > 0.0) (atr / currentClose) * 100.0 else 0.0
        return AtrResult(value = atr, percentOfPrice = percent)
    }

    private fun calculateAtrSeries(candles: List<Candle>, period: Int): DoubleArray? {
        if (candles.size < period + 1) return null
        val count = candles.size
        val tr = DoubleArray(count)

        for (i in 1 until count) {
            val h = candles[i].high
            val l = candles[i].low
            val prevC = candles[i - 1].close
            tr[i] = max(h - l, max(abs(h - prevC), abs(l - prevC)))
        }

        val atr = DoubleArray(count)
        var sum = 0.0
        for (i in 1..period) {
            sum += tr[i]
        }
        atr[period] = sum / period

        for (i in (period + 1) until count) {
            atr[i] = (atr[i - 1] * (period - 1) + tr[i]) / period
        }

        return atr
    }

    fun calculateKeltnerChannels(
        candles: List<Candle>,
        emaPeriod: Int = 20,
        atrPeriod: Int = 10,
        multiplier: Double = 2.0
    ): KeltnerResult? {
        if (candles.size < max(emaPeriod, atrPeriod) + 1) return null
        val closes = DoubleArray(candles.size) { candles[it].close }
        val middle = calculateEma(closes, emaPeriod) ?: return null
        val atrSeries = calculateAtrSeries(candles, atrPeriod) ?: return null
        val atr = atrSeries.last()

        val upper = middle + (multiplier * atr)
        val lower = middle - (multiplier * atr)
        val close = candles.last().close

        val stance = when {
            close > upper -> IndicatorStance.STRONG_BULLISH
            close < lower -> IndicatorStance.STRONG_BEARISH
            close > middle -> IndicatorStance.BULLISH
            else -> IndicatorStance.BEARISH
        }

        return KeltnerResult(upper = upper, middle = middle, lower = lower, stance = stance)
    }

    fun calculateDonchianChannels(candles: List<Candle>, period: Int = 20): DonchianResult? {
        if (candles.size < period) return null
        val count = candles.size
        var highest = Double.NEGATIVE_INFINITY
        var lowest = Double.POSITIVE_INFINITY
        for (i in (count - period) until count) {
            if (candles[i].high > highest) highest = candles[i].high
            if (candles[i].low < lowest) lowest = candles[i].low
        }
        val middle = (highest + lowest) / 2.0
        val close = candles.last().close

        val stance = when {
            close >= highest * 0.999 -> IndicatorStance.STRONG_BULLISH
            close <= lowest * 1.001 -> IndicatorStance.STRONG_BEARISH
            close > middle -> IndicatorStance.BULLISH
            else -> IndicatorStance.BEARISH
        }

        return DonchianResult(upper = highest, middle = middle, lower = lowest, stance = stance)
    }

    fun calculateObv(candles: List<Candle>): ObvResult? {
        if (candles.size < 2) return null
        var obv = 0.0
        val obvHistory = DoubleArray(candles.size)

        for (i in 1 until candles.size) {
            val current = candles[i].close
            val prev = candles[i - 1].close
            val vol = candles[i].volume
            if (current > prev) {
                obv += vol
            } else if (current < prev) {
                obv -= vol
            }
            obvHistory[i] = obv
        }

        val obvSma20 = calculateSma(obvHistory, min(20, candles.size - 1))
        val stance = when {
            obvSma20 == null -> IndicatorStance.NEUTRAL
            obv > obvSma20 * 1.02 -> IndicatorStance.BULLISH
            obv < obvSma20 * 0.98 -> IndicatorStance.BEARISH
            else -> IndicatorStance.NEUTRAL
        }

        return ObvResult(obv = obv, obvSma20 = obvSma20, stance = stance)
    }

    fun calculateMfi(candles: List<Candle>, period: Int = 14): Double? {
        if (candles.size < period + 1) return null
        val count = candles.size
        var posFlow = 0.0
        var negFlow = 0.0

        for (i in (count - period) until count) {
            val currTp = candles[i].typicalPrice
            val prevTp = candles[i - 1].typicalPrice
            val rawFlow = currTp * candles[i].volume

            if (currTp > prevTp) {
                posFlow += rawFlow
            } else if (currTp < prevTp) {
                negFlow += rawFlow
            }
        }

        if (negFlow == 0.0) return 100.0
        val moneyRatio = posFlow / negFlow
        return 100.0 - (100.0 / (1.0 + moneyRatio))
    }

    fun calculateCmf(candles: List<Candle>, period: Int = 20): Double? {
        if (candles.size < period) return null
        val count = candles.size
        var sumMfv = 0.0
        var sumVol = 0.0

        for (i in (count - period) until count) {
            val c = candles[i]
            val range = c.high - c.low
            val multiplier = if (range > 0.0) {
                ((c.close - c.low) - (c.high - c.close)) / range
            } else {
                0.0
            }
            sumMfv += multiplier * c.volume
            sumVol += c.volume
        }

        return if (sumVol > 0.0) sumMfv / sumVol else 0.0
    }

    fun calculateRvol(volumes: DoubleArray, period: Int = 20): Double? {
        if (volumes.size < period + 1) return null
        val currentVol = volumes.last()
        var sum = 0.0
        val start = volumes.size - 1 - period
        for (i in start until (volumes.size - 1)) {
            sum += volumes[i]
        }
        val avgVol = sum / period
        return if (avgVol > 0.0) currentVol / avgVol else 1.0
    }

    fun calculateVolumeDelta(candles: List<Candle>): VolumeDeltaResult? {
        if (candles.isEmpty()) return null
        val last = candles.last()

        if (!last.hasTakerData) {
            return VolumeDeltaResult(
                delta = 0.0,
                buyerVolume = 0.0,
                sellerVolume = last.volume,
                cvd = 0.0,
                stance = IndicatorStance.NEUTRAL,
                hasReliableData = false,
                telemetryNote = "Taker aggression requires Binance order-flow feed."
            )
        }

        var runningCvd = 0.0
        for (c in candles) {
            runningCvd += c.volumeDelta
        }

        val delta = last.volumeDelta
        val stance = when {
            delta > last.volume * 0.15 -> IndicatorStance.STRONG_BULLISH
            delta > 0 -> IndicatorStance.BULLISH
            delta < -last.volume * 0.15 -> IndicatorStance.STRONG_BEARISH
            delta < 0 -> IndicatorStance.BEARISH
            else -> IndicatorStance.NEUTRAL
        }

        return VolumeDeltaResult(
            delta = delta,
            buyerVolume = last.buyerVolume,
            sellerVolume = last.sellerVolume,
            cvd = runningCvd,
            stance = stance,
            hasReliableData = true,
            telemetryNote = "Calculated from Binance real taker buyer/seller volume."
        )
    }

    private fun stanceForRsi(rsi: Double?): IndicatorStance = when {
        rsi == null -> IndicatorStance.INSUFFICIENT_DATA
        rsi >= 70.0 -> IndicatorStance.OVERBOUGHT
        rsi <= 30.0 -> IndicatorStance.OVERSOLD
        rsi > 52.0 -> IndicatorStance.BULLISH
        rsi < 48.0 -> IndicatorStance.BEARISH
        else -> IndicatorStance.NEUTRAL
    }

    // =========================================================================
    // PRESENTATION CARD BUILDER
    // =========================================================================

    private fun buildCardsList(
        currentClose: Double,
        trend: TrendIndicators,
        momentum: MomentumIndicators,
        volatility: VolatilityIndicators,
        volume: VolumeIndicators,
        candleCount: Int
    ): List<IndicatorCardItem> {
        val list = mutableListOf<IndicatorCardItem>()

        fun fmt(num: Double?, decimals: Int = 2): String =
            if (num != null) String.format(Locale.US, "%,.${decimals}f", num) else "N/A"

        // --- TREND CARDS ---
        fun addEmaCard(period: Int, value: Double?) {
            val available = value != null
            val stance = if (available) {
                if (currentClose >= value!!) IndicatorStance.BULLISH else IndicatorStance.BEARISH
            } else IndicatorStance.INSUFFICIENT_DATA

            list.add(
                IndicatorCardItem(
                    id = "ema_$period",
                    name = "EMA $period",
                    category = IndicatorCategory.TREND,
                    primaryValue = fmt(value),
                    secondaryValue = if (available) (if (currentClose >= value!!) "Above EMA" else "Below EMA") else "Needs $period candles",
                    stance = stance,
                    interpretation = if (available) {
                        if (currentClose >= value!!) "Price trades above $period EMA (Bullish support)"
                        else "Price trades below $period EMA (Bearish resistance)"
                    } else "Insufficient data ($candleCount/$period candles)",
                    formulaInfo = "Exponential Moving Average (alpha = 2/(N+1))",
                    isAvailable = available
                )
            )
        }

        addEmaCard(9, trend.emaSet.ema9)
        addEmaCard(20, trend.emaSet.ema20)
        addEmaCard(50, trend.emaSet.ema50)
        addEmaCard(100, trend.emaSet.ema100)
        addEmaCard(200, trend.emaSet.ema200)

        fun addSmaCard(period: Int, value: Double?) {
            val available = value != null
            val stance = if (available) {
                if (currentClose >= value!!) IndicatorStance.BULLISH else IndicatorStance.BEARISH
            } else IndicatorStance.INSUFFICIENT_DATA

            list.add(
                IndicatorCardItem(
                    id = "sma_$period",
                    name = "SMA $period",
                    category = IndicatorCategory.TREND,
                    primaryValue = fmt(value),
                    secondaryValue = if (available) (if (currentClose >= value!!) "Above SMA" else "Below SMA") else "Needs $period candles",
                    stance = stance,
                    interpretation = if (available) {
                        if (currentClose >= value!!) "Price trades above $period SMA"
                        else "Price trades below $period SMA"
                    } else "Insufficient data ($candleCount/$period candles)",
                    formulaInfo = "Simple Moving Average over $period candles",
                    isAvailable = available
                )
            )
        }

        addSmaCard(20, trend.smaSet.sma20)
        addSmaCard(50, trend.smaSet.sma50)
        addSmaCard(100, trend.smaSet.sma100)
        addSmaCard(200, trend.smaSet.sma200)

        // VWAP
        val vwapVal = trend.vwap
        list.add(
            IndicatorCardItem(
                id = "vwap",
                name = "VWAP",
                category = IndicatorCategory.TREND,
                primaryValue = fmt(vwapVal),
                secondaryValue = if (vwapVal != null) "${fmt(((currentClose - vwapVal) / vwapVal) * 100.0, 2)}% vs VWAP" else null,
                stance = trend.vwapStance,
                interpretation = if (vwapVal != null) {
                    if (currentClose > vwapVal) "Institutional benchmark: Buyers in control above VWAP"
                    else "Institutional benchmark: Sellers in control below VWAP"
                } else "Volume data insufficient for VWAP",
                formulaInfo = "Volume Weighted Average Price: sum(TP * V) / sum(V)",
                isAvailable = vwapVal != null
            )
        )

        // Supertrend
        val st = trend.supertrend
        list.add(
            IndicatorCardItem(
                id = "supertrend",
                name = "Supertrend (10, 3.0)",
                category = IndicatorCategory.TREND,
                primaryValue = fmt(st?.value),
                secondaryValue = if (st != null) (if (st.isBullish) "BULLISH SUPPORT" else "BEARISH RESISTANCE") else null,
                stance = st?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = if (st != null) {
                    if (st.isBullish) "Trend is Bullish. Trailing stop level at ${fmt(st.value)}"
                    else "Trend is Bearish. Overhead trailing barrier at ${fmt(st.value)}"
                } else "Supertrend requires at least 11 candles",
                formulaInfo = "ATR Trailing Band: HL2 +/- (3.0 * ATR10)",
                isAvailable = st != null
            )
        )

        // ADX
        val adx = trend.adx
        list.add(
            IndicatorCardItem(
                id = "adx",
                name = "ADX (14)",
                category = IndicatorCategory.TREND,
                primaryValue = fmt(adx?.adx, 1),
                secondaryValue = if (adx != null) "+DI: ${fmt(adx.plusDI, 1)} | -DI: ${fmt(adx.minusDI, 1)}" else null,
                stance = adx?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = adx?.strengthDescription ?: "ADX requires 29+ candles for Wilder's smoothing",
                formulaInfo = "Average Directional Index (Wilder's DX smoothing)",
                isAvailable = adx != null
            )
        )

        // Ichimoku
        val ichi = trend.ichimoku
        list.add(
            IndicatorCardItem(
                id = "ichimoku",
                name = "Ichimoku Cloud (9, 26, 52)",
                category = IndicatorCategory.TREND,
                primaryValue = fmt(ichi?.tenkanSen),
                secondaryValue = if (ichi != null) "Span A: ${fmt(ichi.senkouSpanA)} | Span B: ${fmt(ichi.senkouSpanB)}" else null,
                stance = ichi?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = ichi?.cloudDescription ?: "Requires 52 candles for Senkou Span B cloud calculation",
                formulaInfo = "Tenkan (9), Kijun (26), Span A ((T+K)/2), Span B (52)",
                isAvailable = ichi != null
            )
        )

        // --- MOMENTUM CARDS ---
        fun addRsiCard(period: Int, value: Double?, stance: IndicatorStance) {
            val available = value != null
            list.add(
                IndicatorCardItem(
                    id = "rsi_$period",
                    name = "RSI $period",
                    category = IndicatorCategory.MOMENTUM,
                    primaryValue = fmt(value, 1),
                    secondaryValue = if (available) {
                        when {
                            value!! >= 70.0 -> "Overbought (>70)"
                            value <= 30.0 -> "Oversold (<30)"
                            value > 50.0 -> "Bullish (>50)"
                            else -> "Bearish (<50)"
                        }
                    } else null,
                    stance = stance,
                    interpretation = if (available) {
                        when {
                            value!! >= 70.0 -> "Extended rally. Potential profit taking or pullback zone."
                            value <= 30.0 -> "Exhaustion selling. Potential bounce or reversal zone."
                            value > 50.0 -> "Positive momentum dominant."
                            else -> "Negative momentum dominant."
                        }
                    } else "Requires $period+ candles",
                    formulaInfo = "Relative Strength Index (Wilder's smoothed $period)",
                    isAvailable = available
                )
            )
        }

        addRsiCard(7, momentum.rsi7, momentum.rsi7Stance)
        addRsiCard(14, momentum.rsi14, momentum.rsi14Stance)
        addRsiCard(21, momentum.rsi21, momentum.rsi21Stance)

        // MACD
        val macd = momentum.macd
        list.add(
            IndicatorCardItem(
                id = "macd",
                name = "MACD (12, 26, 9)",
                category = IndicatorCategory.MOMENTUM,
                primaryValue = fmt(macd?.histogram, 2),
                secondaryValue = if (macd != null) "MACD: ${fmt(macd.macdLine)} | Signal: ${fmt(macd.signalLine)}" else null,
                stance = macd?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = macd?.crossoverStatus ?: "MACD requires 35+ candles for full signal convergence",
                formulaInfo = "Moving Average Convergence Divergence: EMA12 - EMA26",
                isAvailable = macd != null
            )
        )

        // Stoch RSI
        val sRsi = momentum.stochRsi
        list.add(
            IndicatorCardItem(
                id = "stoch_rsi",
                name = "Stochastic RSI (14, 14, 3, 3)",
                category = IndicatorCategory.MOMENTUM,
                primaryValue = fmt(sRsi?.k, 1),
                secondaryValue = if (sRsi != null) "%K: ${fmt(sRsi.k, 1)} | %D: ${fmt(sRsi.d, 1)}" else null,
                stance = sRsi?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = if (sRsi != null) {
                    when {
                        sRsi.k >= 80.0 -> "Overbought momentum territory"
                        sRsi.k <= 20.0 -> "Oversold momentum territory"
                        sRsi.k > sRsi.d -> "Bullish %K crossover above %D"
                        else -> "Bearish %K cross below %D"
                    }
                } else "Requires 35+ candles",
                formulaInfo = "Stochastic of RSI: (RSI - MinRSI) / (MaxRSI - MinRSI)",
                isAvailable = sRsi != null
            )
        )

        // Stochastic Oscillator
        val stoch = momentum.stochastic
        list.add(
            IndicatorCardItem(
                id = "stochastic",
                name = "Stochastic Osc (14, 3, 3)",
                category = IndicatorCategory.MOMENTUM,
                primaryValue = fmt(stoch?.k, 1),
                secondaryValue = if (stoch != null) "%K: ${fmt(stoch.k, 1)} | %D: ${fmt(stoch.d, 1)}" else null,
                stance = stoch?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = if (stoch != null) {
                    when {
                        stoch.k >= 80.0 -> "Overbought range ceiling"
                        stoch.k <= 20.0 -> "Oversold range floor"
                        stoch.k > stoch.d -> "Fast K leading Slow D upward"
                        else -> "Fast K leading Slow D downward"
                    }
                } else "Requires 20+ candles",
                formulaInfo = "Lane's Stochastic: (Close - Low14)/(High14 - Low14)",
                isAvailable = stoch != null
            )
        )

        // CCI
        val cciVal = momentum.cci
        list.add(
            IndicatorCardItem(
                id = "cci",
                name = "CCI (20)",
                category = IndicatorCategory.MOMENTUM,
                primaryValue = fmt(cciVal, 1),
                secondaryValue = if (cciVal != null) (if (cciVal > 100.0) "Overbought (>100)" else if (cciVal < -100.0) "Oversold (<-100)" else "Normal Channel") else null,
                stance = momentum.cciStance,
                interpretation = if (cciVal != null) {
                    when {
                        cciVal > 100.0 -> "Strong bullish momentum breakout"
                        cciVal < -100.0 -> "Strong bearish momentum expansion"
                        cciVal > 0 -> "Bullish bias within channel"
                        else -> "Bearish bias within channel"
                    }
                } else "Requires 20 candles",
                formulaInfo = "Commodity Channel Index: (TP - SMA) / (0.015 * MeanDev)",
                isAvailable = cciVal != null
            )
        )

        // Williams %R
        val wR = momentum.williamsR
        list.add(
            IndicatorCardItem(
                id = "williams_r",
                name = "Williams %R (14)",
                category = IndicatorCategory.MOMENTUM,
                primaryValue = fmt(wR, 1),
                secondaryValue = if (wR != null) (if (wR > -20.0) "Overbought (>-20)" else if (wR < -80.0) "Oversold (<-80)" else "Mid-Range") else null,
                stance = momentum.williamsRStance,
                interpretation = if (wR != null) {
                    when {
                        wR > -20.0 -> "Near recent 14-period highs (Overbought)"
                        wR < -80.0 -> "Near recent 14-period lows (Oversold)"
                        else -> "Consolidating within recent range"
                    }
                } else "Requires 14 candles",
                formulaInfo = "Larry Williams %R: (High14 - Close) / (High14 - Low14) * -100",
                isAvailable = wR != null
            )
        )

        // ROC
        val rocVal = momentum.roc
        list.add(
            IndicatorCardItem(
                id = "roc",
                name = "ROC (14)",
                category = IndicatorCategory.MOMENTUM,
                primaryValue = "${fmt(rocVal, 2)}%",
                secondaryValue = if (rocVal != null) (if (rocVal >= 0) "Positive Velocity" else "Negative Velocity") else null,
                stance = momentum.rocStance,
                interpretation = if (rocVal != null) {
                    if (rocVal > 0) "Price is expanding higher compared to 14 candles ago"
                    else "Price is contracting compared to 14 candles ago"
                } else "Requires 15 candles",
                formulaInfo = "Rate of Change: (Close_t - Close_t-14) / Close_t-14 * 100",
                isAvailable = rocVal != null
            )
        )

        // --- VOLATILITY CARDS ---
        val bb = volatility.bollingerBands
        list.add(
            IndicatorCardItem(
                id = "bollinger_bands",
                name = "Bollinger Bands (20, 2.0)",
                category = IndicatorCategory.VOLATILITY,
                primaryValue = "BW: ${fmt(bb?.bandwidth, 1)}%",
                secondaryValue = if (bb != null) "Upper: ${fmt(bb.upper)} | Lower: ${fmt(bb.lower)}" else null,
                stance = bb?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = if (bb != null) {
                    when {
                        bb.isSqueeze -> "VOLATILITY SQUEEZE: Bands compressed. Breakout imminent."
                        currentClose > bb.upper -> "Price pressing above Upper Band (Overbought expansion)"
                        currentClose < bb.lower -> "Price pressing below Lower Band (Oversold expansion)"
                        else -> "Price trading within normal 2-sigma envelope (%B: ${fmt(bb.percentB, 2)})"
                    }
                } else "Requires 20 candles",
                formulaInfo = "Middle SMA20 +/- (2.0 * StdDev)",
                isAvailable = bb != null
            )
        )

        val atr = volatility.atr
        list.add(
            IndicatorCardItem(
                id = "atr",
                name = "ATR (14)",
                category = IndicatorCategory.VOLATILITY,
                primaryValue = fmt(atr?.value),
                secondaryValue = if (atr != null) "${fmt(atr.percentOfPrice, 2)}% of asset price" else null,
                stance = IndicatorStance.NEUTRAL,
                interpretation = if (atr != null) {
                    "Expected candle volatility is +/- ${fmt(atr.value)} (${fmt(atr.percentOfPrice, 2)}%)"
                } else "Requires 15 candles for ATR",
                formulaInfo = "Average True Range (Wilder's smoothed TR)",
                isAvailable = atr != null
            )
        )

        val keltner = volatility.keltnerChannels
        list.add(
            IndicatorCardItem(
                id = "keltner",
                name = "Keltner Channels (20, 2.0)",
                category = IndicatorCategory.VOLATILITY,
                primaryValue = fmt(keltner?.middle),
                secondaryValue = if (keltner != null) "Upper: ${fmt(keltner.upper)} | Lower: ${fmt(keltner.lower)}" else null,
                stance = keltner?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = if (keltner != null) {
                    when {
                        currentClose > keltner.upper -> "Strong upward expansion through upper channel"
                        currentClose < keltner.lower -> "Strong downward breakdown through lower channel"
                        else -> "Price fluctuating within ATR bands"
                    }
                } else "Requires 21 candles",
                formulaInfo = "EMA20 +/- (2.0 * ATR10)",
                isAvailable = keltner != null
            )
        )

        val donchian = volatility.donchianChannels
        list.add(
            IndicatorCardItem(
                id = "donchian",
                name = "Donchian Channels (20)",
                category = IndicatorCategory.VOLATILITY,
                primaryValue = fmt(donchian?.middle),
                secondaryValue = if (donchian != null) "20H: ${fmt(donchian.upper)} | 20L: ${fmt(donchian.lower)}" else null,
                stance = donchian?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = if (donchian != null) {
                    when {
                        currentClose >= donchian.upper * 0.999 -> "Turtle Breakout: Trading at 20-period peak"
                        currentClose <= donchian.lower * 1.001 -> "Turtle Breakdown: Trading at 20-period trough"
                        else -> "Trading between 20-period high and low bounds"
                    }
                } else "Requires 20 candles",
                formulaInfo = "Highest High (20) & Lowest Low (20)",
                isAvailable = donchian != null
            )
        )

        // --- VOLUME CARDS ---
        val obv = volume.obv
        list.add(
            IndicatorCardItem(
                id = "obv",
                name = "On-Balance Volume (OBV)",
                category = IndicatorCategory.VOLUME,
                primaryValue = if (obv != null) formatAbbreviated(obv.obv) else "N/A",
                secondaryValue = if (obv?.obvSma20 != null) "OBV SMA: ${formatAbbreviated(obv.obvSma20)}" else null,
                stance = obv?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = if (obv != null) {
                    if (obv.stance == IndicatorStance.BULLISH) "Volume accumulation confirms upward price pressure"
                    else "Volume distribution confirms downward price pressure"
                } else "Requires multi-candle volume stream",
                formulaInfo = "Cumulative volume signed by close price delta",
                isAvailable = obv != null
            )
        )

        val mfiVal = volume.mfi
        list.add(
            IndicatorCardItem(
                id = "mfi",
                name = "Money Flow Index (14)",
                category = IndicatorCategory.VOLUME,
                primaryValue = fmt(mfiVal, 1),
                secondaryValue = if (mfiVal != null) (if (mfiVal > 80.0) "Overbought Flow" else if (mfiVal < 20.0) "Oversold Flow" else "Balanced Flow") else null,
                stance = volume.mfiStance,
                interpretation = if (mfiVal != null) {
                    when {
                        mfiVal > 80.0 -> "High volume flow into highs. Potential buyer exhaustion."
                        mfiVal < 20.0 -> "Heavy volume dumped at lows. Potential seller exhaustion."
                        mfiVal > 50.0 -> "Capital inflows exceed outflows."
                        else -> "Capital outflows exceed inflows."
                    }
                } else "Requires 15 candles with volume",
                formulaInfo = "Volume-weighted RSI over typical prices",
                isAvailable = mfiVal != null
            )
        )

        val cmfVal = volume.cmf
        list.add(
            IndicatorCardItem(
                id = "cmf",
                name = "Chaikin Money Flow (20)",
                category = IndicatorCategory.VOLUME,
                primaryValue = fmt(cmfVal, 3),
                secondaryValue = if (cmfVal != null) (if (cmfVal > 0.05) "Accumulation" else if (cmfVal < -0.05) "Distribution" else "Neutral") else null,
                stance = volume.cmfStance,
                interpretation = if (cmfVal != null) {
                    when {
                        cmfVal > 0.05 -> "Strong institutional accumulation (+CMF)"
                        cmfVal < -0.05 -> "Strong institutional distribution (-CMF)"
                        else -> "Balanced buy/sell volume pressure"
                    }
                } else "Requires 20 candles",
                formulaInfo = "Sum(CLV * Volume) / Sum(Volume)",
                isAvailable = cmfVal != null
            )
        )

        val rvolVal = volume.relativeVolume
        list.add(
            IndicatorCardItem(
                id = "rvol",
                name = "Relative Volume (RVOL 20)",
                category = IndicatorCategory.VOLUME,
                primaryValue = "${fmt(rvolVal, 2)}x",
                secondaryValue = if (rvolVal != null) {
                    when {
                        rvolVal >= 2.0 -> "VERY HIGH (>2.0x)"
                        rvolVal >= 1.2 -> "Above Average"
                        rvolVal <= 0.6 -> "LOW (<0.6x)"
                        else -> "Average Volume"
                    }
                } else null,
                stance = volume.relativeVolumeStance,
                interpretation = if (rvolVal != null) {
                    when {
                        rvolVal >= 2.0 -> "High volume breakout signature. Institutional participation."
                        rvolVal <= 0.6 -> "Low volume chop. Moves lack conviction."
                        else -> "Volume is trading at baseline average."
                    }
                } else "Requires 21 candles",
                formulaInfo = "Current Volume / 20-period Moving Average Volume",
                isAvailable = rvolVal != null
            )
        )

        val vd = volume.volumeDelta
        val vdAvailable = vd != null && vd.hasReliableData
        list.add(
            IndicatorCardItem(
                id = "volume_delta",
                name = "Volume Delta & CVD",
                category = IndicatorCategory.VOLUME,
                primaryValue = if (vdAvailable) formatAbbreviated(vd!!.delta) else "Telemetry Pending",
                secondaryValue = if (vdAvailable) "CVD: ${formatAbbreviated(vd!!.cvd)}" else "Order Flow Only",
                stance = vd?.stance ?: IndicatorStance.INSUFFICIENT_DATA,
                interpretation = vd?.telemetryNote ?: "Binance aggressive taker order flow data required.",
                formulaInfo = "Taker Buy Volume minus Taker Sell Volume",
                isAvailable = vdAvailable
            )
        )

        return list
    }

    private fun buildSummaryNarrative(
        symbol: String,
        timeframe: CandleTimeframe,
        overallStance: IndicatorStance,
        bullish: Int,
        bearish: Int,
        neutral: Int,
        rsi14: Double?,
        macd: MacdResult?,
        supertrend: SupertrendResult?,
        adx: AdxResult?
    ): String {
        val trendText = when {
            supertrend?.isBullish == true -> "Supertrend is Bullish"
            supertrend?.isBullish == false -> "Supertrend is Bearish"
            else -> "Trend is Undefined"
        }

        val momText = when {
            rsi14 != null && rsi14 > 65.0 -> "RSI14 is elevated at ${String.format(Locale.US, "%.1f", rsi14)}"
            rsi14 != null && rsi14 < 35.0 -> "RSI14 is depressed at ${String.format(Locale.US, "%.1f", rsi14)}"
            macd?.histogram != null && macd.histogram > 0 -> "MACD histogram shows expanding bullish momentum"
            macd?.histogram != null && macd.histogram < 0 -> "MACD histogram indicates selling pressure"
            else -> "Momentum is neutral"
        }

        val adxText = if (adx != null && adx.adx > 25.0) "with strong trend strength (ADX ${String.format(Locale.US, "%.1f", adx.adx)})" else "in a consolidating regime"

        return "$symbol on ${timeframe.label}: $trendText $adxText. $momText. Consensus score: $bullish Bullish vs $bearish Bearish ($neutral Neutral signals)."
    }

    private fun formatAbbreviated(num: Double): String {
        val absVal = abs(num)
        val sign = if (num < 0) "-" else ""
        return when {
            absVal >= 1_000_000_000.0 -> String.format(Locale.US, "%s%.2fB", sign, absVal / 1_000_000_000.0)
            absVal >= 1_000_000.0 -> String.format(Locale.US, "%s%.2fM", sign, absVal / 1_000_000.0)
            absVal >= 1_000.0 -> String.format(Locale.US, "%s%.1fK", sign, absVal / 1_000.0)
            else -> String.format(Locale.US, "%s%.2f", sign, absVal)
        }
    }

    private fun createEmptyAnalysis(
        symbol: String,
        assetName: String,
        timeframe: CandleTimeframe
    ): OverallAnalysisResult {
        return OverallAnalysisResult(
            symbol = symbol,
            assetName = assetName,
            timeframe = timeframe,
            candleCount = 0,
            currentClose = 0.0,
            priceChangePercent = 0.0,
            stance = IndicatorStance.INSUFFICIENT_DATA,
            summaryText = "No historical candle data loaded for $symbol on timeframe ${timeframe.label}.",
            bullishCount = 0,
            bearishCount = 0,
            neutralCount = 0,
            trend = TrendIndicators(
                emaSet = EmaSet(null, null, null, null, null),
                smaSet = SmaSet(null, null, null, null),
                vwap = null,
                vwapStance = IndicatorStance.INSUFFICIENT_DATA,
                supertrend = null,
                adx = null,
                ichimoku = null
            ),
            momentum = MomentumIndicators(
                rsi7 = null,
                rsi7Stance = IndicatorStance.INSUFFICIENT_DATA,
                rsi14 = null,
                rsi14Stance = IndicatorStance.INSUFFICIENT_DATA,
                rsi21 = null,
                rsi21Stance = IndicatorStance.INSUFFICIENT_DATA,
                macd = null,
                stochRsi = null,
                stochastic = null,
                cci = null,
                cciStance = IndicatorStance.INSUFFICIENT_DATA,
                williamsR = null,
                williamsRStance = IndicatorStance.INSUFFICIENT_DATA,
                roc = null,
                rocStance = IndicatorStance.INSUFFICIENT_DATA
            ),
            volatility = VolatilityIndicators(
                bollingerBands = null,
                atr = null,
                keltnerChannels = null,
                donchianChannels = null
            ),
            volume = VolumeIndicators(
                obv = null,
                mfi = null,
                mfiStance = IndicatorStance.INSUFFICIENT_DATA,
                cmf = null,
                cmfStance = IndicatorStance.INSUFFICIENT_DATA,
                relativeVolume = null,
                relativeVolumeStance = IndicatorStance.INSUFFICIENT_DATA,
                volumeDelta = null
            ),
            cards = emptyList(),
            warnings = listOf("No candle history found. Please check internet connection or switch timeframe.")
        )
    }
}
