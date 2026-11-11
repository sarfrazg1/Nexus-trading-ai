package com.example.data.scanner

import com.example.data.indicators.TechnicalIndicatorsEngine
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.model.MarketAsset
import com.example.data.model.ScannerItem
import com.example.data.smc.FvgStatus
import com.example.data.smc.PriceZoneType
import com.example.data.smc.SmcCalculationEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class ScannerConditionFilter(val id: String, val label: String) {
    ALL("ALL", "All Anomalies"),
    STRONG_BULLISH_TREND("STRONG_BULLISH_TREND", "Strong Bullish Trend"),
    STRONG_BEARISH_TREND("STRONG_BEARISH_TREND", "Strong Bearish Trend"),
    RSI_OVERBOUGHT("RSI_OVERBOUGHT", "RSI Overbought (>70)"),
    RSI_OVERSOLD("RSI_OVERSOLD", "RSI Oversold (<30)"),
    EMA_CROSSOVERS("EMA_CROSSOVERS", "EMA Crossovers"),
    MACD_CROSSOVERS("MACD_CROSSOVERS", "MACD Crossovers"),
    HIGH_VOLUME("HIGH_VOLUME", "High Volume (RVOL > 1.5x)"),
    HIGH_VOLATILITY("HIGH_VOLATILITY", "High Volatility (ATR)"),
    BOS_AND_CHOCH("BOS_AND_CHOCH", "BOS & CHoCH"),
    ACTIVE_FVG_ZONES("ACTIVE_FVG_ZONES", "Active FVG Zones"),
    LIQUIDITY_SWEEPS("LIQUIDITY_SWEEPS", "Liquidity Sweeps"),
    NEAR_SUPPORT("NEAR_SUPPORT", "Near Support (≤0.8%)"),
    NEAR_RESISTANCE("NEAR_RESISTANCE", "Near Resistance (≤0.8%)")
}

object MarketScannerEngine {

    suspend fun scanAsset(
        asset: MarketAsset,
        candles: List<Candle>,
        timeframe: CandleTimeframe = CandleTimeframe.H1
    ): ScannerItem {
        val currentPrice = asset.currentPrice ?: candles.lastOrNull()?.close ?: 0.0
        val change24hPct = asset.change24hPercent ?: 0.0

        if (candles.size < 15) {
            // Insufficient candles for full multi-factor scan
            return ScannerItem(
                id = "scan_${asset.id}_${timeframe.binanceInterval}",
                symbol = asset.symbol,
                name = asset.name,
                category = asset.category,
                rsi14 = 50.0,
                macdStatus = "Neutral",
                emaCondition = "Waiting for data",
                volumeMultiplier = 1.0,
                aiScore = 50,
                matchedCriteria = listOf("Awaiting Candles"),
                verifiedPrice = currentPrice,
                change24hPercent = change24hPct,
                detectedCondition = "Neutral Baseline",
                timeframe = timeframe.label,
                lastUpdated = formatTimestamp(System.currentTimeMillis()),
                assetId = asset.id
            )
        }

        // 1. Run Technical Indicators Engine
        val techAnalysis = TechnicalIndicatorsEngine.analyze(
            symbol = asset.symbol,
            assetName = asset.name,
            timeframe = timeframe,
            candles = candles
        )

        // 2. Run SMC Engine
        val smcAnalysis = SmcCalculationEngine.analyze(
            symbol = asset.symbol,
            assetName = asset.name,
            timeframe = timeframe,
            candles = candles
        )

        val rsiVal = techAnalysis.momentum.rsi14 ?: 50.0
        val macd = techAnalysis.momentum.macd
        val macdState = when {
            macd != null && macd.histogram > 0 && macd.macdLine > macd.signalLine -> "Bullish Cross"
            macd != null && macd.histogram < 0 && macd.macdLine < macd.signalLine -> "Bearish Cross"
            else -> "Neutral Histogram"
        }

        val ema9 = techAnalysis.trend.emaSet.ema9 ?: currentPrice
        val ema20 = techAnalysis.trend.emaSet.ema20 ?: currentPrice
        val ema50 = techAnalysis.trend.emaSet.ema50 ?: currentPrice
        val ema200 = techAnalysis.trend.emaSet.ema200 ?: currentPrice

        val emaState = when {
            currentPrice > ema200 && ema50 > ema200 -> "Above 200 EMA (Bullish)"
            currentPrice < ema200 && ema50 < ema200 -> "Below 200 EMA (Bearish)"
            currentPrice > ema200 -> "200 EMA Breakout"
            else -> "Testing 200 EMA"
        }

        val rvol = techAnalysis.volume.relativeVolume ?: 1.0

        // 3. Match against the 13 required scan criteria
        val matched = mutableListOf<String>()

        // 1. Strong Bullish Trend
        val adx = techAnalysis.trend.adx
        val isTrending = adx != null && adx.adx >= 25.0
        val isBullishTrend = currentPrice > ema20 && ema20 > ema50 && (!isTrending || (adx != null && adx.plusDI > adx.minusDI))
        if (isBullishTrend) matched.add(ScannerConditionFilter.STRONG_BULLISH_TREND.label)

        // 2. Strong Bearish Trend
        val isBearishTrend = currentPrice < ema20 && ema20 < ema50 && (!isTrending || (adx != null && adx.minusDI > adx.plusDI))
        if (isBearishTrend) matched.add(ScannerConditionFilter.STRONG_BEARISH_TREND.label)

        // 3. RSI Overbought (>70)
        if (rsiVal >= 70.0) matched.add(ScannerConditionFilter.RSI_OVERBOUGHT.label)

        // 4. RSI Oversold (<30)
        if (rsiVal <= 30.0) matched.add(ScannerConditionFilter.RSI_OVERSOLD.label)

        // 5. EMA Crossovers
        if ((ema9 > ema20 && ema20 > ema50) || (ema50 > ema200 && abs(ema50 - ema200) / ema200 < 0.015)) {
            matched.add(ScannerConditionFilter.EMA_CROSSOVERS.label)
        }

        // 6. MACD Crossovers
        if (macdState.contains("Cross")) {
            matched.add(ScannerConditionFilter.MACD_CROSSOVERS.label)
        }

        // 7. High Volume (RVOL > 1.5x)
        if (rvol >= 1.5) {
            matched.add(ScannerConditionFilter.HIGH_VOLUME.label)
        }

        // 8. High Volatility (ATR)
        val atr = techAnalysis.volatility.atr?.value ?: 0.0
        val lastCandleRange = if (candles.isNotEmpty()) candles.last().high - candles.last().low else 0.0
        if (atr > 0 && lastCandleRange >= 1.7 * atr) {
            matched.add(ScannerConditionFilter.HIGH_VOLATILITY.label)
        }

        // 9. BOS and CHoCH
        val hasBosOrChoch = smcAnalysis.marketStructure.structureBreaks.any {
            it.type.name.contains("BOS") || it.type.name.contains("CHOCH")
        }
        if (hasBosOrChoch) matched.add(ScannerConditionFilter.BOS_AND_CHOCH.label)

        // 10. Active FVG Zones
        val hasActiveFvg = smcAnalysis.fairValueGaps.any { it.status == FvgStatus.UNFILLED }
        if (hasActiveFvg) matched.add(ScannerConditionFilter.ACTIVE_FVG_ZONES.label)

        // 11. Liquidity Sweeps
        val hasSweeps = smcAnalysis.liquidity.recentSweeps.isNotEmpty()
        if (hasSweeps) matched.add(ScannerConditionFilter.LIQUIDITY_SWEEPS.label)

        // 12. Near Support (<= 0.8% away)
        val isNearSupport = smcAnalysis.priceZones.any {
            (it.type == PriceZoneType.SUPPORT || it.type == PriceZoneType.DEMAND) &&
            it.distancePercent <= 0.8
        }
        if (isNearSupport) matched.add(ScannerConditionFilter.NEAR_SUPPORT.label)

        // 13. Near Resistance (<= 0.8% away)
        val isNearResistance = smcAnalysis.priceZones.any {
            (it.type == PriceZoneType.RESISTANCE || it.type == PriceZoneType.SUPPLY) &&
            it.distancePercent <= 0.8
        }
        if (isNearResistance) matched.add(ScannerConditionFilter.NEAR_RESISTANCE.label)

        if (matched.isEmpty()) {
            matched.add("Balanced Range")
        }

        // 4. Calculate AI Technical Score
        var score = 50
        if (isBullishTrend) score += 15
        if (isBearishTrend) score -= 15
        if (rsiVal in 45.0..60.0) score += 5
        if (rsiVal < 30.0) score += 12 // Oversold bounce setup
        if (rsiVal > 70.0) score -= 10 // Overextended
        if (rvol >= 1.5) score += 10
        if (hasActiveFvg) score += 8
        if (hasSweeps) score += 8
        if (isNearSupport) score += 10
        if (isNearResistance) score -= 8
        score = score.coerceIn(15, 98)

        val primaryCondition = matched.firstOrNull { it != "Balanced Range" } ?: matched.first()

        return ScannerItem(
            id = "scan_${asset.id}_${timeframe.binanceInterval}",
            symbol = asset.symbol,
            name = asset.name,
            category = asset.category,
            rsi14 = rsiVal,
            macdStatus = macdState,
            emaCondition = emaState,
            volumeMultiplier = Math.round(rvol * 10.0) / 10.0,
            aiScore = score,
            matchedCriteria = matched,
            verifiedPrice = currentPrice,
            change24hPercent = change24hPct,
            detectedCondition = primaryCondition,
            timeframe = timeframe.label,
            lastUpdated = formatTimestamp(System.currentTimeMillis()),
            assetId = asset.id
        )
    }

    private fun formatTimestamp(timeMs: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        return sdf.format(Date(timeMs))
    }
}
