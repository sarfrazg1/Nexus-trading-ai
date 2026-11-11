package com.example.data.signal

import com.example.data.indicators.IndicatorStance
import com.example.data.indicators.OverallAnalysisResult
import com.example.data.indicators.TechnicalIndicatorsEngine
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.model.MarketDataStatus
import com.example.data.smc.FibLevel
import com.example.data.smc.FibonacciAnalysis
import com.example.data.smc.FvgStatus
import com.example.data.smc.FvgType
import com.example.data.smc.LiquidityPoolType
import com.example.data.smc.MarketTrendState
import com.example.data.smc.OrderBlock
import com.example.data.smc.OrderBlockStatus
import com.example.data.smc.OrderBlockType
import com.example.data.smc.PriceLevelZone
import com.example.data.smc.PriceZoneType
import com.example.data.smc.SmcAnalysisResult
import com.example.data.smc.SmcCalculationEngine
import com.example.data.smc.TimeframeSmcSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Pure Kotlin Multi-Factor AI Trading Signal Engine.
 * Evaluates real technical indicators, Smart Money Concepts, and Multi-Timeframe confirmation.
 * Yields only 3 strict decisions: LONG, SHORT, or NO TRADE.
 */
object AiTradingSignalEngine {

    suspend fun generateSignal(
        symbol: String,
        assetName: String,
        currentPrice: Double?,
        dataStatus: MarketDataStatus,
        dataSourceName: String,
        lastUpdatedTime: Long?,
        primaryTimeframe: CandleTimeframe,
        candles: List<Candle>,
        technicalAnalysis: OverallAnalysisResult?,
        smcAnalysis: SmcAnalysisResult?,
        mtfSnapshots: List<TimeframeSmcSnapshot> = emptyList()
    ): AiTradingSignal = withContext(Dispatchers.Default) {
        val now = System.currentTimeMillis()
        val formattedTime = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US).format(Date(now))
        val freshnessSeconds = if (lastUpdatedTime != null && lastUpdatedTime > 0) {
            max(0L, (now - lastUpdatedTime) / 1000L)
        } else 0L

        // ----------------------------------------------------
        // 1. DATA SAFETY & VALIDITY CHECKS (NO TRADE TRIGGER)
        // ----------------------------------------------------
        if (currentPrice == null || currentPrice <= 0.0) {
            return@withContext createNoTradeSignal(
                symbol = symbol,
                assetName = assetName,
                primaryTimeframe = primaryTimeframe,
                code = "DATA_UNAVAILABLE",
                headline = "Market Price Data Not Connected",
                explanation = "Current live price for $symbol is unavailable or zero. Trading signals require verified market feeds.",
                conflictingFactors = listOf("Price feed is null or non-positive", "Data status: ${dataStatus.label}"),
                recommendedAction = "Connect a live market API or check network connectivity before analyzing signals.",
                dataSource = dataSourceName,
                freshness = dataStatus.label,
                timestamp = now,
                formattedTime = formattedTime
            )
        }

        if (dataStatus == MarketDataStatus.UNAVAILABLE || dataStatus == MarketDataStatus.STALE) {
            return@withContext createNoTradeSignal(
                symbol = symbol,
                assetName = assetName,
                primaryTimeframe = primaryTimeframe,
                code = "MARKET_DATA_STALE",
                headline = "Market Data is Stale or Unavailable",
                explanation = "The data feed for $symbol is marked as ${dataStatus.label} (age: ${freshnessSeconds}s). Stale prices introduce severe execution slippage.",
                conflictingFactors = listOf("Feed status is ${dataStatus.label}", "Last update was ${freshnessSeconds}s ago"),
                recommendedAction = "Wait for fresh tick updates or tap 'Refresh Data' to re-sync live market stream.",
                dataSource = dataSourceName,
                freshness = dataStatus.label,
                timestamp = now,
                formattedTime = formattedTime
            )
        }

        if (candles.size < 30) {
            return@withContext createNoTradeSignal(
                symbol = symbol,
                assetName = assetName,
                primaryTimeframe = primaryTimeframe,
                code = "INSUFFICIENT_DATA",
                headline = "Insufficient Candle History",
                explanation = "Only ${candles.size} candles available on ${primaryTimeframe.label}. A minimum of 30 historical candles is required for reliable indicator confluence.",
                conflictingFactors = listOf("Available candles: ${candles.size} (Minimum required: 30)", "Long-term EMAs and Multi-period swings cannot be computed"),
                recommendedAction = "Switch to a higher timeframe or allow historical candle buffer to synchronize.",
                dataSource = dataSourceName,
                freshness = dataStatus.label,
                timestamp = now,
                formattedTime = formattedTime
            )
        }

        // If technical analysis or SMC analysis is missing, compute them on-the-fly
        val ta = technicalAnalysis ?: TechnicalIndicatorsEngine.analyze(symbol, assetName, primaryTimeframe, candles)
        val smc = smcAnalysis ?: SmcCalculationEngine.analyze(symbol, assetName, primaryTimeframe, candles, mtfSnapshots)

        // ----------------------------------------------------
        // 2. MULTI-TIMEFRAME EVALUATION (1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w)
        // ----------------------------------------------------
        val tfConditions = evaluateTimeframeConditions(smc, mtfSnapshots, primaryTimeframe)
        val bullishTfCount = tfConditions.count { it.stance == "BULLISH" }
        val bearishTfCount = tfConditions.count { it.stance == "BEARISH" }
        val neutralTfCount = tfConditions.count { it.stance == "NEUTRAL" }
        val totalTfCount = tfConditions.size.coerceAtLeast(1)

        // ----------------------------------------------------
        // 3. FACTOR SCORING: TECHNICAL INDICATORS & SMC
        // ----------------------------------------------------
        var bullishPoints = 0
        var bearishPoints = 0
        val factorReasons = mutableListOf<SignalFactorReason>()
        val supportingIndicators = mutableListOf<String>()
        val smcConfirmations = mutableListOf<String>()
        val conflicts = mutableListOf<String>()

        // --- Factor 1: Trend Direction (Max 20 pts) ---
        var trendPts = 0
        val trendStance: String
        val trendSummary: String

        val emaSet = ta.trend.emaSet
        val emaBullishCount = listOfNotNull(
            emaSet.ema9?.let { currentPrice > it },
            emaSet.ema20?.let { currentPrice > it },
            emaSet.ema50?.let { currentPrice > it },
            emaSet.ema100?.let { currentPrice > it },
            emaSet.ema200?.let { currentPrice > it }
        ).count { it }

        val emaTotalCount = listOfNotNull(emaSet.ema9, emaSet.ema20, emaSet.ema50, emaSet.ema100, emaSet.ema200).size
        val supertrend = ta.trend.supertrend
        val adx = ta.trend.adx
        val isAdxTrending = adx != null && adx.adx >= 20.0

        if (emaTotalCount > 0 && emaBullishCount >= (emaTotalCount * 0.7) && supertrend?.isBullish == true) {
            trendPts = 18 + (if (isAdxTrending) 2 else 0)
            bullishPoints += trendPts
            trendStance = "BULLISH"
            trendSummary = "Price above $emaBullishCount/$emaTotalCount EMAs with Bullish Supertrend (${String.format(Locale.US, "$%.2f", supertrend.value)})"
            supportingIndicators.add("EMA Stack Bullish ($emaBullishCount/$emaTotalCount above)")
            supportingIndicators.add("Supertrend Bullish (${String.format(Locale.US, "$%.2f", supertrend.value)})")
        } else if (emaTotalCount > 0 && emaBullishCount <= (emaTotalCount * 0.3) && supertrend?.isBullish == false) {
            trendPts = 18 + (if (isAdxTrending) 2 else 0)
            bearishPoints += trendPts
            trendStance = "BEARISH"
            trendSummary = "Price below ${emaTotalCount - emaBullishCount}/$emaTotalCount EMAs with Bearish Supertrend (${String.format(Locale.US, "$%.2f", supertrend.value)})"
            supportingIndicators.add("EMA Stack Bearish (${emaTotalCount - emaBullishCount}/$emaTotalCount below)")
            supportingIndicators.add("Supertrend Bearish (${String.format(Locale.US, "$%.2f", supertrend.value)})")
        } else {
            trendPts = 8
            trendStance = "CONFLICTING"
            trendSummary = "Mixed EMA alignment ($emaBullishCount/$emaTotalCount above) with chop near moving averages"
            conflicts.add("Trend Direction: Moving averages are intertwined without clear directional tilt")
        }
        factorReasons.add(SignalFactorReason("Trend Direction", 20, trendPts, trendStance, trendSummary))

        // --- Factor 2: Momentum (Max 15 pts) ---
        var momPts = 0
        val momStance: String
        val momSummary: String

        val rsi14 = ta.momentum.rsi14
        val macd = ta.momentum.macd
        val stoch = ta.momentum.stochastic

        var bullMomPoints = 0
        var bearMomPoints = 0

        if (rsi14 != null) {
            val rVal = rsi14
            if (rVal in 42.0..66.0) bullMomPoints += 4
            else if (rVal in 34.0..58.0) bearMomPoints += 4
            else if (rVal < 30.0) bullMomPoints += 5 // Oversold bounce potential
            else if (rVal > 70.0) bearMomPoints += 5 // Overbought rejection potential
        }

        if (macd != null) {
            val isBullishCross = macd.histogram > 0 && macd.macdLine > macd.signalLine
            if (isBullishCross && macd.histogram > 0) {
                bullMomPoints += 6
                supportingIndicators.add("MACD Bullish Momentum (Histogram: +${String.format(Locale.US, "%.2f", macd.histogram)})")
            } else if (!isBullishCross && macd.histogram < 0) {
                bearMomPoints += 6
                supportingIndicators.add("MACD Bearish Momentum (Histogram: ${String.format(Locale.US, "%.2f", macd.histogram)})")
            }
        }

        if (stoch != null) {
            if (stoch.k > stoch.d && stoch.k < 80.0) bullMomPoints += 4
            else if (stoch.k < stoch.d && stoch.k > 20.0) bearMomPoints += 4
        }

        val rsiText = if (rsi14 != null) String.format(Locale.US, "%.1f", rsi14) else "N/A"

        if (bullMomPoints >= 9 && bullMomPoints > bearMomPoints) {
            momPts = min(15, bullMomPoints)
            bullishPoints += momPts
            momStance = "BULLISH"
            momSummary = "RSI ($rsiText) + MACD positive acceleration"
        } else if (bearMomPoints >= 9 && bearMomPoints > bullMomPoints) {
            momPts = min(15, bearMomPoints)
            bearishPoints += momPts
            momStance = "BEARISH"
            momSummary = "RSI ($rsiText) + MACD negative acceleration"
        } else {
            momPts = 6
            momStance = "NEUTRAL"
            momSummary = "Momentum oscillators are hovering near equilibrium ($rsiText)"
            if (abs(bullMomPoints - bearMomPoints) <= 2) {
                conflicts.add("Momentum: RSI and MACD do not show a decisive expansion bias")
            }
        }
        factorReasons.add(SignalFactorReason("Momentum", 15, momPts, momStance, momSummary))

        // --- Factor 3: Volume & Participation (Max 15 pts) ---
        var volPts = 0
        val volStance: String
        val volSummary: String

        val rvol = ta.volume.relativeVolume ?: 1.0
        val volDelta = ta.volume.volumeDelta
        val cmf = ta.volume.cmf

        if (rvol >= 1.15 && volDelta != null && volDelta.delta > 0 && (cmf ?: 0.0) >= 0.0) {
            volPts = 14
            bullishPoints += volPts
            volStance = "BULLISH"
            volSummary = "Elevated buying participation (RVOL: ${String.format(Locale.US, "%.2f", rvol)}x, Net Buy Delta: +${String.format(Locale.US, "%.1f", volDelta.delta)})"
            supportingIndicators.add("RVOL ${String.format(Locale.US, "%.2f", rvol)}x (Institutional Buy Volume)")
        } else if (rvol >= 1.15 && volDelta != null && volDelta.delta < 0 && (cmf ?: 0.0) <= 0.0) {
            volPts = 14
            bearishPoints += volPts
            volStance = "BEARISH"
            volSummary = "Elevated selling participation (RVOL: ${String.format(Locale.US, "%.2f", rvol)}x, Net Sell Delta: ${String.format(Locale.US, "%.1f", volDelta.delta)})"
            supportingIndicators.add("RVOL ${String.format(Locale.US, "%.2f", rvol)}x (Heavy Selling Pressure)")
        } else {
            volPts = 7
            volStance = "NEUTRAL"
            volSummary = "Average volume (RVOL: ${String.format(Locale.US, "%.2f", rvol)}x) without dominant delta divergence"
        }
        factorReasons.add(SignalFactorReason("Volume & Participation", 15, volPts, volStance, volSummary))

        // --- Factor 4: Market Structure (SMC) (Max 20 pts) ---
        var structPts = 0
        val structStance: String
        val structSummary: String

        val ms = smc.marketStructure
        val isBullishStructure = ms.trendState == MarketTrendState.BULLISH_UPTREND
        val isBearishStructure = ms.trendState == MarketTrendState.BEARISH_DOWNTREND
        val hasBullishBreak = ms.isBullishBOSPresent || ms.isBullishCHoCHPresent
        val hasBearishBreak = ms.isBearishBOSPresent || ms.isBearishCHoCHPresent

        // Check active order blocks near price
        val unmitigatedBullishOB = smc.orderBlocks.firstOrNull {
            it.type == OrderBlockType.BULLISH && it.status == OrderBlockStatus.UNMITIGATED && currentPrice >= (it.bottomPrice * 0.985) && currentPrice <= (it.topPrice * 1.025)
        }
        val unmitigatedBearishOB = smc.orderBlocks.firstOrNull {
            it.type == OrderBlockType.BEARISH && it.status == OrderBlockStatus.UNMITIGATED && currentPrice <= (it.topPrice * 1.015) && currentPrice >= (it.bottomPrice * 0.975)
        }

        if (isBullishStructure || (hasBullishBreak && unmitigatedBullishOB != null)) {
            structPts = 18
            bullishPoints += structPts
            structStance = "BULLISH"
            val breakLabel = if (ms.isBullishCHoCHPresent) "Bullish CHoCH" else "Bullish BOS"
            structSummary = "$breakLabel confirmed with Higher Highs/Lows" + (if (unmitigatedBullishOB != null) " + Active Demand OB" else "")
            smcConfirmations.add("$breakLabel on ${primaryTimeframe.label}")
            if (unmitigatedBullishOB != null) {
                smcConfirmations.add("Unmitigated Bullish OB [${String.format(Locale.US, "$%.2f", unmitigatedBullishOB.bottomPrice)} - ${String.format(Locale.US, "$%.2f", unmitigatedBullishOB.topPrice)}]")
            }
        } else if (isBearishStructure || (hasBearishBreak && unmitigatedBearishOB != null)) {
            structPts = 18
            bearishPoints += structPts
            structStance = "BEARISH"
            val breakLabel = if (ms.isBearishCHoCHPresent) "Bearish CHoCH" else "Bearish BOS"
            structSummary = "$breakLabel confirmed with Lower Highs/Lows" + (if (unmitigatedBearishOB != null) " + Active Supply OB" else "")
            smcConfirmations.add("$breakLabel on ${primaryTimeframe.label}")
            if (unmitigatedBearishOB != null) {
                smcConfirmations.add("Unmitigated Bearish OB [${String.format(Locale.US, "$%.2f", unmitigatedBearishOB.bottomPrice)} - ${String.format(Locale.US, "$%.2f", unmitigatedBearishOB.topPrice)}]")
            }
        } else {
            structPts = 7
            structStance = "CONFLICTING"
            structSummary = "Consolidating market structure without recent BOS or CHoCH"
            conflicts.add("Market Structure: Trend is ranging or consolidating inside equilibrium")
        }
        factorReasons.add(SignalFactorReason("Market Structure (SMC)", 20, structPts, structStance, structSummary))

        // --- Factor 5: Liquidity & Stop Runs (Max 10 pts) ---
        var liqPts = 0
        val liqStance: String
        val liqSummary: String

        val recentSweep = smc.liquidity.recentSweeps.firstOrNull()
        val sslPools = smc.liquidity.pools.filter { it.type == LiquidityPoolType.SELL_SIDE_LIQUIDITY || it.type == LiquidityPoolType.EQUAL_LOWS }
        val bslPools = smc.liquidity.pools.filter { it.type == LiquidityPoolType.BUY_SIDE_LIQUIDITY || it.type == LiquidityPoolType.EQUAL_HIGHS }

        if (recentSweep != null && (recentSweep.poolType == LiquidityPoolType.SELL_SIDE_LIQUIDITY || recentSweep.poolType == LiquidityPoolType.EQUAL_LOWS)) {
            liqPts = 10
            bullishPoints += liqPts
            liqStance = "BULLISH"
            liqSummary = "Sell-Side Liquidity (SSL) swept below ${String.format(Locale.US, "$%.2f", recentSweep.sweptLevel)} followed by candle reclamation"
            smcConfirmations.add("SSL / Equal Lows Liquidity Swept (Institutional Spring)")
        } else if (recentSweep != null && (recentSweep.poolType == LiquidityPoolType.BUY_SIDE_LIQUIDITY || recentSweep.poolType == LiquidityPoolType.EQUAL_HIGHS)) {
            liqPts = 10
            bearishPoints += liqPts
            liqStance = "BEARISH"
            liqSummary = "Buy-Side Liquidity (BSL) swept above ${String.format(Locale.US, "$%.2f", recentSweep.sweptLevel)} followed by candle rejection"
            smcConfirmations.add("BSL / Equal Highs Liquidity Swept (Institutional Upthrust)")
        } else if (smc.liquidity.potentialStopHuntActive) {
            liqPts = 8
            val sweepHunt = smc.liquidity.recentSweeps.firstOrNull { it.isPotentialStopHunt }
            if (sweepHunt != null && (sweepHunt.poolType == LiquidityPoolType.SELL_SIDE_LIQUIDITY || sweepHunt.poolType == LiquidityPoolType.EQUAL_LOWS)) {
                bullishPoints += liqPts
                liqStance = "BULLISH"
                liqSummary = "Potential Bullish Stop-Hunt detected near ${String.format(Locale.US, "$%.2f", sweepHunt.sweptLevel)}"
                smcConfirmations.add("Potential Stop-Hunt (Discount accumulation)")
            } else {
                bearishPoints += liqPts
                liqStance = "BEARISH"
                val trig = sweepHunt?.sweptLevel ?: currentPrice
                liqSummary = "Potential Bearish Stop-Hunt detected near ${String.format(Locale.US, "$%.2f", trig)}"
                smcConfirmations.add("Potential Stop-Hunt (Premium distribution)")
            }
        } else {
            liqPts = 5
            liqStance = "NEUTRAL"
            liqSummary = "No recent sweep event; resting BSL (${bslPools.size} pools) and SSL (${sslPools.size} pools) intact"
        }
        factorReasons.add(SignalFactorReason("Liquidity & Stop Runs", 10, liqPts, liqStance, liqSummary))

        // --- Factor 6: Support/Resistance & Fib Golden Pocket (Max 10 pts) ---
        var srPts = 0
        val srStance: String
        val srSummary: String

        val goldenPocket = smc.fibonacci?.retracementLevels?.firstOrNull { it.isGoldenPocket }
        val isNearGoldenPocket = goldenPocket != null && abs(currentPrice - goldenPocket.price) / currentPrice <= 0.025
        val nearestSupport = smc.priceZones.firstOrNull { it.type == PriceZoneType.SUPPORT || it.type == PriceZoneType.DEMAND }
        val nearestResistance = smc.priceZones.firstOrNull { it.type == PriceZoneType.RESISTANCE || it.type == PriceZoneType.SUPPLY }

        val unfilledBullishFvg = smc.fairValueGaps.firstOrNull { it.type == FvgType.BULLISH && it.status != FvgStatus.FULLY_FILLED }
        val unfilledBearishFvg = smc.fairValueGaps.firstOrNull { it.type == FvgType.BEARISH && it.status != FvgStatus.FULLY_FILLED }

        val distToSupport = nearestSupport?.let { abs(currentPrice - it.centralPrice) / currentPrice } ?: 0.1
        val distToResistance = nearestResistance?.let { abs(currentPrice - it.centralPrice) / currentPrice } ?: 0.1

        if (isNearGoldenPocket && goldenPocket != null && (distToSupport < distToResistance || unfilledBullishFvg != null)) {
            srPts = 9
            bullishPoints += srPts
            srStance = "BULLISH"
            srSummary = "0.618 Fib Golden Pocket confluence (${String.format(Locale.US, "$%.2f", goldenPocket.price)}) + Demand Zone support"
            smcConfirmations.add("Fibonacci 0.618 Golden Pocket Zone (${String.format(Locale.US, "$%.2f", goldenPocket.price)})")
            if (unfilledBullishFvg != null) {
                smcConfirmations.add("Unfilled Bullish FVG [${String.format(Locale.US, "$%.2f", unfilledBullishFvg.bottomPrice)} - ${String.format(Locale.US, "$%.2f", unfilledBullishFvg.topPrice)}]")
            }
        } else if (distToResistance < 0.015 && nearestResistance != null && (distToResistance < distToSupport || unfilledBearishFvg != null)) {
            srPts = 9
            bearishPoints += srPts
            srStance = "BEARISH"
            srSummary = "Testing key Supply / Resistance zone (${String.format(Locale.US, "$%.2f", nearestResistance.centralPrice)})"
            smcConfirmations.add("Supply / Key Resistance Barrier (${String.format(Locale.US, "$%.2f", nearestResistance.centralPrice)})")
            if (unfilledBearishFvg != null) {
                smcConfirmations.add("Unfilled Bearish FVG [${String.format(Locale.US, "$%.2f", unfilledBearishFvg.bottomPrice)} - ${String.format(Locale.US, "$%.2f", unfilledBearishFvg.topPrice)}]")
            }
        } else {
            srPts = 5
            srStance = "NEUTRAL"
            srSummary = "Price hovering between Support (${nearestSupport?.let { String.format(Locale.US, "$%.2f", it.centralPrice) } ?: "N/A"}) and Resistance (${nearestResistance?.let { String.format(Locale.US, "$%.2f", it.centralPrice) } ?: "N/A"})"
        }
        factorReasons.add(SignalFactorReason("Support / Resistance & Fib", 10, srPts, srStance, srSummary))

        // --- Factor 7: Multi-Timeframe Confirmation (Max 10 pts) ---
        var mtfPts = 0
        val mtfStance: String
        val mtfSummary: String

        val agreementPercent = if (bullishPoints >= bearishPoints) {
            (bullishTfCount.toDouble() / totalTfCount * 100).toInt()
        } else {
            (bearishTfCount.toDouble() / totalTfCount * 100).toInt()
        }

        if (bullishTfCount >= (totalTfCount * 0.62) && bullishPoints >= bearishPoints) {
            mtfPts = 9
            bullishPoints += mtfPts
            mtfStance = "BULLISH"
            mtfSummary = "$bullishTfCount of $totalTfCount timeframes agree Bullish ($agreementPercent% alignment)"
            supportingIndicators.add("MTF Confluence: $bullishTfCount/$totalTfCount timeframes Bullish")
        } else if (bearishTfCount >= (totalTfCount * 0.62) && bearishPoints > bullishPoints) {
            mtfPts = 9
            bearishPoints += mtfPts
            mtfStance = "BEARISH"
            mtfSummary = "$bearishTfCount of $totalTfCount timeframes agree Bearish ($agreementPercent% alignment)"
            supportingIndicators.add("MTF Confluence: $bearishTfCount/$totalTfCount timeframes Bearish")
        } else {
            mtfPts = 4
            mtfStance = "CONFLICTING"
            mtfSummary = "Split timeframes: $bullishTfCount Bullish, $bearishTfCount Bearish, $neutralTfCount Neutral"
            conflicts.add("Multi-Timeframe Conflict: Lower timeframes and higher timeframes do not align")
        }
        factorReasons.add(SignalFactorReason("Multi-Timeframe Confirmation", 10, mtfPts, mtfStance, mtfSummary))

        // ----------------------------------------------------
        // 4. CONFIDENCE SCORE AGGREGATION & DECISION LOGIC
        // ----------------------------------------------------
        val rawConfidence = max(bullishPoints, bearishPoints).coerceIn(0, 100)
        val isBullishDominant = bullishPoints > bearishPoints && bullishPoints >= 62
        val isBearishDominant = bearishPoints > bullishPoints && bearishPoints >= 62
        val hasSevereConflict = abs(bullishPoints - bearishPoints) < 18 || conflicts.size >= 2

        val ratingLabel = when {
            rawConfidence >= 80 && !hasSevereConflict -> "High Confluence"
            rawConfidence >= 65 && !hasSevereConflict -> "Moderate Confluence"
            else -> "Low / Conflicting Confluence"
        }

        val confidenceBreakdown = ConfidenceScoreBreakdown(
            trendScore = trendPts,
            momentumScore = momPts,
            volumeScore = volPts,
            marketStructureScore = structPts,
            liquidityScore = liqPts,
            supportResistanceScore = srPts,
            multiTimeframeScore = mtfPts,
            totalScore = rawConfidence,
            rating = ratingLabel,
            factors = factorReasons
        )

        // ----------------------------------------------------
        // 5. NO TRADE DECISION CHECK
        // ----------------------------------------------------
        if (!isBullishDominant && !isBearishDominant) {
            return@withContext createNoTradeSignal(
                symbol = symbol,
                assetName = assetName,
                primaryTimeframe = primaryTimeframe,
                code = "CONFLICTING_INDICATORS",
                headline = "Conflicting Indicators & Lack of Edge",
                explanation = "Bullish signals ($bullishPoints pts) and Bearish signals ($bearishPoints pts) are canceling each other out. Market has no clear directional edge.",
                conflictingFactors = conflicts.ifEmpty { listOf("Trend and momentum are not in unified agreement", "Confidence score ($rawConfidence%) is below the minimum 65% actionable threshold") },
                recommendedAction = "Wait for a confirmed Break of Structure (BOS) or liquidity sweep to resolve the current range.",
                dataSource = dataSourceName,
                freshness = dataStatus.label,
                timestamp = now,
                formattedTime = formattedTime,
                confidence = confidenceBreakdown,
                timeframeConditions = tfConditions
            )
        }

        // ----------------------------------------------------
        // 6. RISK MANAGEMENT PLAN (FOR LONG OR SHORT)
        // ----------------------------------------------------
        val atr = ta.volatility.atr?.value ?: (currentPrice * 0.015)

        val decision = if (isBullishDominant) SignalDecision.LONG else SignalDecision.SHORT

        val riskPlan = if (decision == SignalDecision.LONG) {
            calculateLongRiskPlan(
                currentPrice = currentPrice,
                atr = atr,
                unmitigatedOB = unmitigatedBullishOB,
                nearestSupport = nearestSupport,
                goldenPocket = goldenPocket,
                fibAnalysis = smc.fibonacci
            )
        } else {
            calculateShortRiskPlan(
                currentPrice = currentPrice,
                atr = atr,
                unmitigatedOB = unmitigatedBearishOB,
                nearestResistance = nearestResistance,
                goldenPocket = goldenPocket,
                fibAnalysis = smc.fibonacci
            )
        }

        // ----------------------------------------------------
        // 7. RISK-TO-REWARD VALIDATION GATE
        // ----------------------------------------------------
        if (riskPlan.riskRewardRatio < 1.5) {
            return@withContext createNoTradeSignal(
                symbol = symbol,
                assetName = assetName,
                primaryTimeframe = primaryTimeframe,
                code = "POOR_RISK_REWARD",
                headline = "Unfavorable Risk-to-Reward Ratio (${riskPlan.riskRewardFormatted})",
                explanation = "Calculated Risk-to-Reward is ${riskPlan.riskRewardFormatted}, which is below the mandatory 1:1.5 threshold. Taking this trade exposes capital to asymmetric risk.",
                conflictingFactors = listOf(
                    "Calculated Risk: ${String.format(Locale.US, "%.2f", riskPlan.estimatedRiskPercent)}%",
                    "Calculated Reward (TP2): ${String.format(Locale.US, "%.2f", riskPlan.potentialRewardPercent)}%",
                    "Target is too close or Stop Loss is too wide relative to market structure"
                ),
                recommendedAction = "Wait for a deeper pullback into the entry zone to improve the entry price and tighten the stop loss.",
                dataSource = dataSourceName,
                freshness = dataStatus.label,
                timestamp = now,
                formattedTime = formattedTime,
                confidence = confidenceBreakdown,
                timeframeConditions = tfConditions
            )
        }

        // ----------------------------------------------------
        // 8. FINAL VALID TRADING SIGNAL
        // ----------------------------------------------------
        val signalId = "SIG_${symbol.replace("/", "_")}_${System.currentTimeMillis()}"

        return@withContext AiTradingSignal(
            id = signalId,
            symbol = symbol,
            assetName = assetName,
            primaryTimeframe = primaryTimeframe,
            decision = decision,
            confidence = confidenceBreakdown,
            riskPlan = riskPlan,
            noTradeReason = null,
            supportingIndicators = supportingIndicators,
            smcConfirmations = smcConfirmations,
            timeframeConditions = tfConditions,
            mtfAgreementCount = if (decision == SignalDecision.LONG) bullishTfCount else bearishTfCount,
            mtfTotalCount = totalTfCount,
            mtfAgreementPercent = agreementPercent,
            timestamp = now,
            formattedTimestamp = formattedTime,
            dataSource = dataSourceName,
            dataFreshness = dataStatus.label
        )
    }

    // -----------------------------------------------------------------------------------------
    // RISK MANAGEMENT CALCULATIONS (STRICT STOP LOSS, TARGETS, LEVERAGE)
    // -----------------------------------------------------------------------------------------

    private fun calculateLongRiskPlan(
        currentPrice: Double,
        atr: Double,
        unmitigatedOB: OrderBlock?,
        nearestSupport: PriceLevelZone?,
        goldenPocket: FibLevel?,
        fibAnalysis: FibonacciAnalysis?
    ): RiskManagementPlan {
        // Entry zone: between current price and nearest discount structural level
        val entryLow = unmitigatedOB?.bottomPrice
            ?: goldenPocket?.price?.let { min(currentPrice, it) }
            ?: (currentPrice - atr * 0.4)
        val entryHigh = max(currentPrice, entryLow + atr * 0.2)
        val entryMid = (entryLow + entryHigh) / 2.0

        // Stop loss: below Order Block bottom, Support, or ATR buffer
        val rawSl = unmitigatedOB?.let { it.bottomPrice - (atr * 0.35) }
            ?: nearestSupport?.let { it.lowerPrice - (atr * 0.35) }
            ?: (entryMid - atr * 1.5)

        // Ensure SL is strictly below entry and realistic (minimum 0.7%, max 5.0%)
        val minSlDist = entryMid * 0.008
        val maxSlDist = entryMid * 0.05
        val slDist = (entryMid - rawSl).coerceIn(minSlDist, maxSlDist)
        val stopLoss = entryMid - slDist
        val riskPercent = (slDist / entryMid) * 100.0

        // Targets based on R:R and Fib extensions / key zones
        val tp1 = entryMid + (slDist * 1.6)
        val tp2 = fibAnalysis?.extensionLevels?.firstOrNull()?.price
            ?: (entryMid + (slDist * 2.8))
        val tp3 = fibAnalysis?.extensionLevels?.getOrNull(1)?.price
            ?: (entryMid + (slDist * 4.2))

        val tp1Dist = ((tp1 - entryMid) / entryMid) * 100.0
        val tp2Dist = ((tp2 - entryMid) / entryMid) * 100.0
        val tp3Dist = ((tp3 - entryMid) / entryMid) * 100.0
        val rrRatio = ((tp2 - entryMid) / slDist).coerceAtLeast(1.0)

        // Leverage capped based on Risk %: Never exceed 10x!
        val suggestedLeverage = calculateSafeLeverage(riskPercent)
        val leverageTier = when {
            suggestedLeverage <= 3 -> "Low Risk (2x - 3x)"
            suggestedLeverage <= 6 -> "Moderate (4x - 6x)"
            else -> "Disciplined Cap (7x - 10x)"
        }
        val levExplanation = "Max $suggestedLeverage" + "x based on ${String.format(Locale.US, "%.2f", riskPercent)}% stop loss distance. Keeps trade liquidation well beyond invalidation point."

        val invalidation = "Hourly candle close below ${formatPrice(stopLoss)} or Bearish CHoCH on Lower Timeframe."

        return RiskManagementPlan(
            entryPrice = entryMid,
            entryZoneMin = min(entryLow, entryHigh),
            entryZoneMax = max(entryLow, entryHigh),
            entryZoneFormatted = "${formatPrice(min(entryLow, entryHigh))} - ${formatPrice(max(entryLow, entryHigh))}",
            stopLoss = stopLoss,
            stopLossFormatted = formatPrice(stopLoss),
            stopLossDistancePercent = riskPercent,
            takeProfit1 = tp1,
            takeProfit1Formatted = formatPrice(tp1),
            takeProfit1DistancePercent = tp1Dist,
            takeProfit2 = tp2,
            takeProfit2Formatted = formatPrice(tp2),
            takeProfit2DistancePercent = tp2Dist,
            takeProfit3 = tp3,
            takeProfit3Formatted = formatPrice(tp3),
            takeProfit3DistancePercent = tp3Dist,
            riskRewardRatio = rrRatio,
            riskRewardFormatted = "1 : ${String.format(Locale.US, "%.1f", rrRatio)}",
            estimatedRiskPercent = riskPercent,
            potentialRewardPercent = tp2Dist,
            suggestedMaxLeverage = suggestedLeverage,
            leverageTier = leverageTier,
            leverageExplanation = levExplanation,
            invalidationCondition = invalidation
        )
    }

    private fun calculateShortRiskPlan(
        currentPrice: Double,
        atr: Double,
        unmitigatedOB: OrderBlock?,
        nearestResistance: PriceLevelZone?,
        goldenPocket: FibLevel?,
        fibAnalysis: FibonacciAnalysis?
    ): RiskManagementPlan {
        // Entry zone: between current price and nearest premium structural level
        val entryHigh = unmitigatedOB?.topPrice
            ?: goldenPocket?.price?.let { max(currentPrice, it) }
            ?: (currentPrice + atr * 0.4)
        val entryLow = min(currentPrice, entryHigh - atr * 0.2)
        val entryMid = (entryLow + entryHigh) / 2.0

        // Stop loss: above Order Block top, Resistance, or ATR buffer
        val rawSl = unmitigatedOB?.let { it.topPrice + (atr * 0.35) }
            ?: nearestResistance?.let { it.upperPrice + (atr * 0.35) }
            ?: (entryMid + atr * 1.5)

        val minSlDist = entryMid * 0.008
        val maxSlDist = entryMid * 0.05
        val slDist = (rawSl - entryMid).coerceIn(minSlDist, maxSlDist)
        val stopLoss = entryMid + slDist
        val riskPercent = (slDist / entryMid) * 100.0

        val tp1 = entryMid - (slDist * 1.6)
        val tp2 = entryMid - (slDist * 2.8)
        val tp3 = entryMid - (slDist * 4.2)

        val tp1Dist = ((entryMid - tp1) / entryMid) * 100.0
        val tp2Dist = ((entryMid - tp2) / entryMid) * 100.0
        val tp3Dist = ((entryMid - tp3) / entryMid) * 100.0
        val rrRatio = ((entryMid - tp2) / slDist).coerceAtLeast(1.0)

        val suggestedLeverage = calculateSafeLeverage(riskPercent)
        val leverageTier = when {
            suggestedLeverage <= 3 -> "Low Risk (2x - 3x)"
            suggestedLeverage <= 6 -> "Moderate (4x - 6x)"
            else -> "Disciplined Cap (7x - 10x)"
        }
        val levExplanation = "Max $suggestedLeverage" + "x based on ${String.format(Locale.US, "%.2f", riskPercent)}% stop loss distance. Never risk more than 1-2% of total account balance."

        val invalidation = "Hourly candle close above ${formatPrice(stopLoss)} or Bullish CHoCH on Lower Timeframe."

        return RiskManagementPlan(
            entryPrice = entryMid,
            entryZoneMin = min(entryLow, entryHigh),
            entryZoneMax = max(entryLow, entryHigh),
            entryZoneFormatted = "${formatPrice(min(entryLow, entryHigh))} - ${formatPrice(max(entryLow, entryHigh))}",
            stopLoss = stopLoss,
            stopLossFormatted = formatPrice(stopLoss),
            stopLossDistancePercent = riskPercent,
            takeProfit1 = tp1,
            takeProfit1Formatted = formatPrice(tp1),
            takeProfit1DistancePercent = tp1Dist,
            takeProfit2 = tp2,
            takeProfit2Formatted = formatPrice(tp2),
            takeProfit2DistancePercent = tp2Dist,
            takeProfit3 = tp3,
            takeProfit3Formatted = formatPrice(tp3),
            takeProfit3DistancePercent = tp3Dist,
            riskRewardRatio = rrRatio,
            riskRewardFormatted = "1 : ${String.format(Locale.US, "%.1f", rrRatio)}",
            estimatedRiskPercent = riskPercent,
            potentialRewardPercent = tp2Dist,
            suggestedMaxLeverage = suggestedLeverage,
            leverageTier = leverageTier,
            leverageExplanation = levExplanation,
            invalidationCondition = invalidation
        )
    }

    private fun calculateSafeLeverage(riskPercent: Double): Int {
        val target = floor(10.0 / max(1.0, riskPercent)).toInt()
        return target.coerceIn(2, 10)
    }

    // -----------------------------------------------------------------------------------------
    // TIMEFRAME CONDITION EVALUATION (1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w)
    // -----------------------------------------------------------------------------------------

    private fun evaluateTimeframeConditions(
        smc: SmcAnalysisResult,
        mtfSnapshots: List<TimeframeSmcSnapshot>,
        primaryTf: CandleTimeframe
    ): List<TimeframeCondition> {
        val allTimeframes = listOf(
            CandleTimeframe.M1,
            CandleTimeframe.M5,
            CandleTimeframe.M15,
            CandleTimeframe.M30,
            CandleTimeframe.H1,
            CandleTimeframe.H4,
            CandleTimeframe.D1,
            CandleTimeframe.W1
        )

        val snapshotMap = mtfSnapshots.associateBy { it.timeframe }

        return allTimeframes.map { tf ->
            if (tf == primaryTf) {
                val stance = when (smc.marketStructure.trendState) {
                    MarketTrendState.BULLISH_UPTREND -> "BULLISH"
                    MarketTrendState.BEARISH_DOWNTREND -> "BEARISH"
                    else -> if (smc.marketStructure.isBullishBOSPresent) "BULLISH" else if (smc.marketStructure.isBearishBOSPresent) "BEARISH" else "NEUTRAL"
                }
                TimeframeCondition(
                    timeframe = tf,
                    stance = stance,
                    summary = "${smc.marketStructure.trendState.label} (Primary Timeframe)",
                    trendDescription = smc.marketStructure.trendState.label,
                    keyLevel = smc.priceZones.firstOrNull()?.let { formatPrice(it.centralPrice) }
                )
            } else {
                val snap = snapshotMap[tf]
                if (snap != null) {
                    val stance = if (snap.isBullishBias) "BULLISH" else when (snap.trend) {
                        MarketTrendState.BEARISH_DOWNTREND -> "BEARISH"
                        else -> "NEUTRAL"
                    }
                    TimeframeCondition(
                        timeframe = tf,
                        stance = stance,
                        summary = "${snap.trend.label} • OBs: ${snap.unmitigatedOBCount}, FVGs: ${snap.unfilledFvgCount}",
                        trendDescription = snap.trend.label,
                        keyLevel = (if (stance == "BULLISH") snap.nearestSupport else snap.nearestResistance)?.let { formatPrice(it) }
                    )
                } else {
                    val stance = if (tf == CandleTimeframe.W1 || tf == CandleTimeframe.D1) {
                        if (smc.marketStructure.trendState == MarketTrendState.BULLISH_UPTREND) "BULLISH" else "NEUTRAL"
                    } else "NEUTRAL"
                    TimeframeCondition(
                        timeframe = tf,
                        stance = stance,
                        summary = "Awaiting snapshot sync",
                        trendDescription = "Synchronizing",
                        keyLevel = null
                    )
                }
            }
        }
    }

    private fun createNoTradeSignal(
        symbol: String,
        assetName: String,
        primaryTimeframe: CandleTimeframe,
        code: String,
        headline: String,
        explanation: String,
        conflictingFactors: List<String>,
        recommendedAction: String,
        dataSource: String,
        freshness: String,
        timestamp: Long,
        formattedTime: String,
        confidence: ConfidenceScoreBreakdown? = null,
        timeframeConditions: List<TimeframeCondition> = emptyList()
    ): AiTradingSignal {
        val defaultConfidence = confidence ?: ConfidenceScoreBreakdown(
            trendScore = 5,
            momentumScore = 5,
            volumeScore = 5,
            marketStructureScore = 5,
            liquidityScore = 5,
            supportResistanceScore = 5,
            multiTimeframeScore = 5,
            totalScore = 35,
            rating = "No Trade Advisory",
            factors = listOf(
                SignalFactorReason("Safety Check", 100, 35, "CONFLICTING", headline)
            )
        )

        return AiTradingSignal(
            id = "SIG_NOTRADE_${symbol.replace("/", "_")}_$timestamp",
            symbol = symbol,
            assetName = assetName,
            primaryTimeframe = primaryTimeframe,
            decision = SignalDecision.NO_TRADE,
            confidence = defaultConfidence,
            riskPlan = null,
            noTradeReason = NoTradeReason(
                code = code,
                headline = headline,
                detailedExplanation = explanation,
                conflictingFactors = conflictingFactors,
                recommendedAction = recommendedAction
            ),
            supportingIndicators = emptyList(),
            smcConfirmations = emptyList(),
            timeframeConditions = timeframeConditions,
            mtfAgreementCount = 0,
            mtfTotalCount = timeframeConditions.size,
            mtfAgreementPercent = 0,
            timestamp = timestamp,
            formattedTimestamp = formattedTime,
            dataSource = dataSource,
            dataFreshness = freshness
        )
    }

    private fun formatPrice(price: Double): String {
        return when {
            price >= 1000.0 -> String.format(Locale.US, "$%,.2f", price)
            price >= 1.0 -> String.format(Locale.US, "$%.2f", price)
            price >= 0.01 -> String.format(Locale.US, "$%.4f", price)
            else -> String.format(Locale.US, "$%.6f", price)
        }
    }
}
