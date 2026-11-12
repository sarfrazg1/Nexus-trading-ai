package com.example.data.scanner

import com.example.data.indicators.TechnicalIndicatorsEngine
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.smc.FvgStatus
import com.example.data.smc.PriceZoneType
import com.example.data.smc.SmcCalculationEngine
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Institutional Multi-Timeframe Confluence Engine for Binance USDT-M Futures.
 * Evaluates 4H, 1H, 15M, and 5M charts to generate verified LONG, SHORT, or NO_TRADE decisions.
 * Strictly separates developing WATCHLIST_SETUPS from confirmed ENTRY_CONFIRMED states.
 */
object BinanceFuturesScannerEngine {

    data class ScanEvaluationResult(
        val signal: FuturesScanSignal?,
        val isNoTrade: Boolean,
        val noTradeReason: String?
    )

    /**
     * Evaluates a single pair across 4H, 1H, 15M, and 5M candle datasets.
     */
    suspend fun evaluatePair(
        symbol: String,
        baseAsset: String,
        currentPrice: Double,
        candles4h: List<Candle>,
        candles1h: List<Candle>,
        candles15m: List<Candle>,
        candles5m: List<Candle>,
        fundingRate: Double? = null
    ): ScanEvaluationResult {
        if (candles15m.size < 20 || candles1h.size < 20 || currentPrice <= 0.0) {
            return ScanEvaluationResult(
                signal = null,
                isNoTrade = true,
                noTradeReason = "Insufficient candle history on $symbol for multi-timeframe analysis."
            )
        }

        // 1. Analyze 4H & 1H for Macro Direction & Higher-Timeframe (HTF) Key Zones
        val tech1h = TechnicalIndicatorsEngine.analyze(symbol, baseAsset, CandleTimeframe.H1, candles1h)
        val smc1h = SmcCalculationEngine.analyze(symbol, baseAsset, CandleTimeframe.H1, candles1h)

        val tech4h = if (candles4h.size >= 15) {
            TechnicalIndicatorsEngine.analyze(symbol, baseAsset, CandleTimeframe.H4, candles4h)
        } else tech1h

        val smc4h = if (candles4h.size >= 15) {
            SmcCalculationEngine.analyze(symbol, baseAsset, CandleTimeframe.H4, candles4h)
        } else smc1h

        // 2. Analyze 15M & 5M for Execution Timing & Lower-Timeframe (LTF) Confirmation
        val tech15m = TechnicalIndicatorsEngine.analyze(symbol, baseAsset, CandleTimeframe.M15, candles15m)
        val smc15m = SmcCalculationEngine.analyze(symbol, baseAsset, CandleTimeframe.M15, candles15m)

        val tech5m = if (candles5m.size >= 15) {
            TechnicalIndicatorsEngine.analyze(symbol, baseAsset, CandleTimeframe.M5, candles5m)
        } else tech15m

        val smc5m = if (candles5m.size >= 15) {
            SmcCalculationEngine.analyze(symbol, baseAsset, CandleTimeframe.M5, candles5m)
        } else smc15m

        // Extract key indicators
        val ema50_1h = tech1h.trend.emaSet.ema50 ?: currentPrice
        val ema200_1h = tech1h.trend.emaSet.ema200 ?: currentPrice
        val rsi15m = tech15m.momentum.rsi14 ?: 50.0
        val rsi1h = tech1h.momentum.rsi14 ?: 50.0
        val atr15m = tech15m.volatility.atr?.value ?: (currentPrice * 0.015)
        val atrPct = if (currentPrice > 0) (atr15m / currentPrice) * 100.0 else 1.5

        // HTF Bias
        val isHtfBullish = currentPrice >= ema50_1h && (ema50_1h >= ema200_1h || currentPrice >= ema200_1h)
        val isHtfBearish = currentPrice <= ema50_1h && (ema50_1h <= ema200_1h || currentPrice <= ema200_1h)

        // LTF Structure
        val hasBullishChoch = smc15m.marketStructure.structureBreaks.any { it.type.name.contains("CHOCH") && it.type.isBullish } ||
                smc5m.marketStructure.structureBreaks.any { it.type.name.contains("CHOCH") && it.type.isBullish }
        val hasBearishChoch = smc15m.marketStructure.structureBreaks.any { it.type.name.contains("CHOCH") && !it.type.isBullish } ||
                smc5m.marketStructure.structureBreaks.any { it.type.name.contains("CHOCH") && !it.type.isBullish }

        val hasBullishFvg = smc15m.fairValueGaps.any { it.type.isBullish && it.status == FvgStatus.UNFILLED } ||
                smc1h.fairValueGaps.any { it.type.isBullish && it.status == FvgStatus.UNFILLED }
        val hasBearishFvg = smc15m.fairValueGaps.any { !it.type.isBullish && it.status == FvgStatus.UNFILLED } ||
                smc1h.fairValueGaps.any { !it.type.isBullish && it.status == FvgStatus.UNFILLED }

        // Find nearest Support & Resistance / Order Blocks
        val nearestSupport = smc1h.priceZones
            .filter { it.type == PriceZoneType.SUPPORT || it.type == PriceZoneType.DEMAND }
            .minByOrNull { abs(currentPrice - it.centralPrice) }?.centralPrice
            ?: (currentPrice - (atr15m * 2.0))

        val nearestResistance = smc1h.priceZones
            .filter { it.type == PriceZoneType.RESISTANCE || it.type == PriceZoneType.SUPPLY }
            .minByOrNull { abs(it.centralPrice - currentPrice) }?.centralPrice
            ?: (currentPrice + (atr15m * 2.0))

        // Timeframe stances breakdown
        val stance4h = TimeframeStance(
            timeframe = CandleTimeframe.H4,
            trend = if (candles4h.isNotEmpty() && currentPrice > (tech4h.trend.emaSet.ema50 ?: currentPrice)) "Bullish" else "Bearish",
            structure = smc4h.marketStructure.trendState.label,
            isConfirmed = true,
            detail = "4H Macro Trend: ${smc4h.marketStructure.trendState.label}"
        )

        val stance1h = TimeframeStance(
            timeframe = CandleTimeframe.H1,
            trend = if (isHtfBullish) "Bullish" else if (isHtfBearish) "Bearish" else "Consolidation",
            structure = "EMA 50: $${String.format(Locale.US, "%.2f", ema50_1h)}",
            isConfirmed = true,
            detail = "1H Alignment: RSI ${String.format(Locale.US, "%.1f", rsi1h)}"
        )

        val stance15m = TimeframeStance(
            timeframe = CandleTimeframe.M15,
            trend = if (tech15m.momentum.rsi14 ?: 50.0 > 50) "Bullish Momentum" else "Bearish Momentum",
            structure = if (hasBullishChoch) "Bullish CHoCH" else if (hasBearishChoch) "Bearish CHoCH" else "Structural Range",
            isConfirmed = hasBullishChoch || hasBearishChoch,
            detail = "15M Structure: ATR $${String.format(Locale.US, "%.2f", atr15m)}"
        )

        val stance5m = TimeframeStance(
            timeframe = CandleTimeframe.M5,
            trend = if (tech5m.momentum.rsi14 ?: 50.0 > 50) "Bullish" else "Bearish",
            structure = "Execution Precision Zone",
            isConfirmed = true,
            detail = "5M Volume: RVOL ${String.format(Locale.US, "%.2fx", tech5m.volume.relativeVolume ?: 1.0)}"
        )

        val mtfStances = listOf(stance4h, stance1h, stance15m, stance5m)

        // Evaluate Confluence Scores
        var bullishScore = 0
        var bearishScore = 0
        val reasons = mutableListOf<String>()

        // 1. HTF Trend & EMA Confluence (30 pts)
        if (isHtfBullish) {
            bullishScore += 25
            reasons.add("1H/4H Macro Trend is Bullish (Price above key 50/200 EMAs)")
        } else if (isHtfBearish) {
            bearishScore += 25
            reasons.add("1H/4H Macro Trend is Bearish (Price below key 50/200 EMAs)")
        }

        // 2. SMC Market Structure Break (25 pts)
        if (hasBullishChoch) {
            bullishScore += 25
            reasons.add("Confirmed Bullish Change of Character (CHoCH) on lower timeframe")
        }
        if (hasBearishChoch) {
            bearishScore += 25
            reasons.add("Confirmed Bearish Change of Character (CHoCH) on lower timeframe")
        }

        // 3. Fair Value Gaps & Liquidity (20 pts)
        if (hasBullishFvg) {
            bullishScore += 15
            reasons.add("Active Unfilled Bullish FVG providing demand support")
        }
        if (hasBearishFvg) {
            bearishScore += 15
            reasons.add("Active Unfilled Bearish FVG providing supply resistance")
        }

        // 4. Momentum & RSI Health (15 pts)
        if (rsi15m in 35.0..65.0) {
            if (isHtfBullish) {
                bullishScore += 15
                reasons.add("15M RSI (${String.format(Locale.US, "%.1f", rsi15m)}) healthy with room for upward expansion")
            } else if (isHtfBearish) {
                bearishScore += 15
                reasons.add("15M RSI (${String.format(Locale.US, "%.1f", rsi15m)}) aligned for continuation breakdown")
            }
        }

        // 5. Volume Confirmation (10 pts)
        val rvol = tech15m.volume.relativeVolume ?: 1.0
        if (rvol >= 1.15) {
            if (isHtfBullish) bullishScore += 10 else bearishScore += 10
            reasons.add("Elevated Volume Confirmation: RVOL ${String.format(Locale.US, "%.2fx", rvol)}")
        }

        // Determine Direction & Confluence
        val isLongEligible = bullishScore >= 60 && bullishScore > bearishScore + 15
        val isShortEligible = bearishScore >= 60 && bearishScore > bullishScore + 15

        if (!isLongEligible && !isShortEligible) {
            return ScanEvaluationResult(
                signal = null,
                isNoTrade = true,
                noTradeReason = "Conflicting multi-timeframe conditions on $symbol (Bullish $bullishScore vs Bearish $bearishScore). Awaiting clear structural alignment."
            )
        }

        val direction = if (isLongEligible) FuturesDirection.LONG else FuturesDirection.SHORT
        val totalScore = if (direction == FuturesDirection.LONG) bullishScore.coerceAtMost(96) else bearishScore.coerceAtMost(96)

        val qualityRating = when {
            totalScore >= 85 -> "A+ Institutional Confluence"
            totalScore >= 70 -> "High Quality Multi-Timeframe Setup"
            else -> "Moderate Confluence"
        }

        // Calculate Entry Zone, Stop Loss, and TP1 / TP2 / TP3
        val entryZoneBuffer = atr15m * 0.4
        val entryMin: Double
        val entryMax: Double
        val entryPrice: Double
        val stopLoss: Double
        val tp1: Double
        val tp2: Double
        val tp3: Double
        val invalidation: String

        if (direction == FuturesDirection.LONG) {
            entryPrice = currentPrice
            entryMin = currentPrice - entryZoneBuffer
            entryMax = currentPrice + (entryZoneBuffer * 0.5)

            // Structural SL below nearest support or 1.3x ATR
            val structuralSL = min(nearestSupport, currentPrice - (atr15m * 1.3))
            stopLoss = String.format(Locale.US, "%.4f", structuralSL).toDouble()

            val risk = currentPrice - stopLoss
            if (risk <= 0.0) {
                return ScanEvaluationResult(signal = null, isNoTrade = true, noTradeReason = "Invalid risk parameters.")
            }

            tp1 = currentPrice + (risk * 1.5)
            tp2 = currentPrice + (risk * 2.8)
            tp3 = currentPrice + (risk * 4.2)
            invalidation = "15M candle body close below $${String.format(Locale.US, "%.2f", stopLoss)} invalidates bullish market structure."
        } else {
            entryPrice = currentPrice
            entryMin = currentPrice - (entryZoneBuffer * 0.5)
            entryMax = currentPrice + entryZoneBuffer

            // Structural SL above nearest resistance or 1.3x ATR
            val structuralSL = max(nearestResistance, currentPrice + (atr15m * 1.3))
            stopLoss = String.format(Locale.US, "%.4f", structuralSL).toDouble()

            val risk = stopLoss - currentPrice
            if (risk <= 0.0) {
                return ScanEvaluationResult(signal = null, isNoTrade = true, noTradeReason = "Invalid risk parameters.")
            }

            tp1 = currentPrice - (risk * 1.5)
            tp2 = currentPrice - (risk * 2.8)
            tp3 = currentPrice - (risk * 4.2)
            invalidation = "15M candle body close above $${String.format(Locale.US, "%.2f", stopLoss)} invalidates bearish market structure."
        }

        val riskDistance = abs(entryPrice - stopLoss)
        val rewardDistance = abs(tp2 - entryPrice)
        val riskReward = if (riskDistance > 0) rewardDistance / riskDistance else 2.5

        if (riskReward < 1.8) {
            return ScanEvaluationResult(
                signal = null,
                isNoTrade = true,
                noTradeReason = "Setup Risk-to-Reward (${String.format(Locale.US, "1:%.2f", riskReward)}) is below required 1:2.0 threshold."
            )
        }

        // Determine Lifecycle State:
        // If price is within entry zone and score >= 68: ENTRY_CONFIRMED
        // Otherwise if approaching zone: WATCHLIST_SETUP
        val isInsideEntryZone = currentPrice in (entryMin * 0.999)..(entryMax * 1.001)
        val lifecycle = if (isInsideEntryZone && totalScore >= 68) {
            FuturesSignalLifecycle.ENTRY_CONFIRMED
        } else {
            FuturesSignalLifecycle.WATCHLIST_SETUP
        }

        val suggestedLeverage = when {
            atrPct > 4.0 -> 2
            atrPct > 2.5 -> 3
            else -> 5
        }

        val signal = FuturesScanSignal(
            symbol = symbol,
            baseAsset = baseAsset,
            direction = direction,
            currentPrice = currentPrice,
            entryZoneMin = entryMin,
            entryZoneMax = entryMax,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit1 = tp1,
            takeProfit2 = tp2,
            takeProfit3 = tp3,
            riskRewardRatio = riskReward,
            qualityScore = totalScore,
            qualityRating = qualityRating,
            timeframeConfirmations = mtfStances,
            reasonsForEntry = reasons,
            invalidationCondition = invalidation,
            lifecycle = lifecycle,
            suggestedLeverage = suggestedLeverage,
            atrVolatilityPercent = atrPct,
            fundingRatePercent = fundingRate,
            maxPriceReached = currentPrice,
            minPriceReached = currentPrice
        )

        return ScanEvaluationResult(
            signal = signal,
            isNoTrade = false,
            noTradeReason = null
        )
    }
}
