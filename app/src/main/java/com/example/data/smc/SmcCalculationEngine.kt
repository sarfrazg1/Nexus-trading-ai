package com.example.data.smc

import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Pure Kotlin mathematical and algorithmic engine for Smart Money Concepts (SMC),
 * Liquidity Analysis, Support & Resistance, and Fibonacci Levels.
 *
 * Runs efficiently on Dispatchers.Default using primitive calculations.
 */
object SmcCalculationEngine {

    suspend fun analyze(
        symbol: String,
        assetName: String,
        timeframe: CandleTimeframe,
        candles: List<Candle>,
        allTimeframeSnapshots: List<TimeframeSmcSnapshot> = emptyList()
    ): SmcAnalysisResult = withContext(Dispatchers.Default) {
        if (candles.size < 15) {
            return@withContext createInsufficientDataResult(symbol, assetName, timeframe, candles)
        }

        val currentPrice = candles.last().close

        // 1. Swing Points & Market Structure (BOS, CHoCH, HH, HL, LH, LL)
        val structureAnalysis = detectMarketStructure(candles)

        // 2. Order Blocks (Bullish & Bearish with Mitigation & Invalidation)
        val orderBlocks = detectOrderBlocks(candles, structureAnalysis)

        // 3. Fair Value Gaps (Bullish & Bearish with Fill Status)
        val fvgs = detectFairValueGaps(candles)

        // 4. Liquidity Pools (BSL, SSL, EQH, EQL, Sweeps, Stop-Hunt)
        val liquidityAnalysis = detectLiquidity(candles, structureAnalysis.swingPoints)

        // 5. Support & Resistance + Supply & Demand
        val priceZones = detectSupportResistanceAndSupplyDemand(candles, structureAnalysis)

        // 6. Fibonacci Retracement & Extensions
        val fibonacci = calculateFibonacciLevels(candles, structureAnalysis)

        // 7. Multi-Timeframe Analysis
        val multiTimeframe = if (allTimeframeSnapshots.isNotEmpty()) {
            buildMultiTimeframeAnalysis(allTimeframeSnapshots)
        } else null

        SmcAnalysisResult(
            symbol = symbol,
            assetName = assetName,
            timeframe = timeframe,
            currentPrice = currentPrice,
            candleCount = candles.size,
            marketStructure = structureAnalysis,
            orderBlocks = orderBlocks,
            fairValueGaps = fvgs,
            liquidity = liquidityAnalysis,
            priceZones = priceZones,
            fibonacci = fibonacci,
            multiTimeframe = multiTimeframe,
            isInsufficientData = false
        )
    }

    // ==========================================
    // 1. MARKET STRUCTURE ENGINE
    // ==========================================

    private fun detectMarketStructure(candles: List<Candle>): MarketStructureAnalysis {
        val n = candles.size
        val swingPoints = mutableListOf<SwingPoint>()
        val lookback = 2 // 2 bars left, 2 bars right (standard 5-bar fractal)

        for (i in lookback until (n - lookback)) {
            val high = candles[i].high
            val low = candles[i].low

            var isSwingHigh = true
            var isSwingLow = true

            for (j in (i - lookback)..(i + lookback)) {
                if (j == i) continue
                if (candles[j].high > high || (candles[j].high == high && j < i)) isSwingHigh = false
                if (candles[j].low < low || (candles[j].low == low && j < i)) isSwingLow = false
            }

            if (isSwingHigh) {
                swingPoints.add(
                    SwingPoint(
                        index = i,
                        timestamp = candles[i].openTime,
                        price = high,
                        type = SwingType.SWING_HIGH,
                        isHigh = true
                    )
                )
            }
            if (isSwingLow) {
                swingPoints.add(
                    SwingPoint(
                        index = i,
                        timestamp = candles[i].openTime,
                        price = low,
                        type = SwingType.SWING_LOW,
                        isHigh = false
                    )
                )
            }
        }

        // Sort swing points by candle index
        swingPoints.sortBy { it.index }

        // Classify into HH, HL, LH, LL
        val classifiedSwings = mutableListOf<SwingPoint>()
        var lastHighPrice: Double? = null
        var lastLowPrice: Double? = null

        for (sp in swingPoints) {
            val classifiedType = if (sp.isHigh) {
                val prevHigh = lastHighPrice
                lastHighPrice = sp.price
                if (prevHigh == null) SwingType.SWING_HIGH
                else if (sp.price > prevHigh) SwingType.HIGHER_HIGH
                else SwingType.LOWER_HIGH
            } else {
                val prevLow = lastLowPrice
                lastLowPrice = sp.price
                if (prevLow == null) SwingType.SWING_LOW
                else if (sp.price > prevLow) SwingType.HIGHER_LOW
                else SwingType.LOWER_LOW
            }
            classifiedSwings.add(sp.copy(type = classifiedType))
        }

        // Detect BOS & CHoCH breaks
        val structureBreaks = mutableListOf<MarketStructureBreak>()
        var currentTrend = MarketTrendState.NEUTRAL_RANGING
        var activeSwingHigh: SwingPoint? = null
        var activeSwingLow: SwingPoint? = null

        for (sp in classifiedSwings) {
            if (sp.isHigh) {
                // Check if price broke above active swing high
                val prevHigh = activeSwingHigh
                if (prevHigh != null && sp.price > prevHigh.price) {
                    val breakType = if (currentTrend == MarketTrendState.BEARISH_DOWNTREND) {
                        currentTrend = MarketTrendState.BULLISH_UPTREND
                        MarketStructureBreakType.CHOCH_BULLISH
                    } else {
                        currentTrend = MarketTrendState.BULLISH_UPTREND
                        MarketStructureBreakType.BOS_BULLISH
                    }

                    structureBreaks.add(
                        MarketStructureBreak(
                            type = breakType,
                            breakPrice = sp.price,
                            brokenSwingPrice = prevHigh.price,
                            breakCandleIndex = sp.index,
                            timestamp = sp.timestamp,
                            description = if (breakType == MarketStructureBreakType.CHOCH_BULLISH)
                                "Bullish Character Change (CHoCH) above $%.2f".format(prevHigh.price)
                            else
                                "Bullish Structure Break (BOS) continuation above $%.2f".format(prevHigh.price)
                        )
                    )
                }
                activeSwingHigh = sp
            } else {
                // Check if price broke below active swing low
                val prevLow = activeSwingLow
                if (prevLow != null && sp.price < prevLow.price) {
                    val breakType = if (currentTrend == MarketTrendState.BULLISH_UPTREND) {
                        currentTrend = MarketTrendState.BEARISH_DOWNTREND
                        MarketStructureBreakType.CHOCH_BEARISH
                    } else {
                        currentTrend = MarketTrendState.BEARISH_DOWNTREND
                        MarketStructureBreakType.BOS_BEARISH
                    }

                    structureBreaks.add(
                        MarketStructureBreak(
                            type = breakType,
                            breakPrice = sp.price,
                            brokenSwingPrice = prevLow.price,
                            breakCandleIndex = sp.index,
                            timestamp = sp.timestamp,
                            description = if (breakType == MarketStructureBreakType.CHOCH_BEARISH)
                                "Bearish Character Change (CHoCH) below $%.2f".format(prevLow.price)
                            else
                                "Bearish Structure Break (BOS) continuation below $%.2f".format(prevLow.price)
                        )
                    )
                }
                activeSwingLow = sp
            }
        }

        // Determine final trend state based on recent swings
        val recentHighs = classifiedSwings.filter { it.isHigh }.takeLast(2)
        val recentLows = classifiedSwings.filter { !it.isHigh }.takeLast(2)

        val finalTrend = when {
            recentHighs.size >= 2 && recentLows.size >= 2 -> {
                val hh = recentHighs[1].price > recentHighs[0].price
                val hl = recentLows[1].price > recentLows[0].price
                val lh = recentHighs[1].price < recentHighs[0].price
                val ll = recentLows[1].price < recentLows[0].price

                if (hh && hl) MarketTrendState.BULLISH_UPTREND
                else if (lh && ll) MarketTrendState.BEARISH_DOWNTREND
                else if (hh && ll) MarketTrendState.TRANSITION_REVERSAL
                else MarketTrendState.NEUTRAL_RANGING
            }
            structureBreaks.isNotEmpty() -> {
                if (structureBreaks.last().type.isBullish) MarketTrendState.BULLISH_UPTREND
                else MarketTrendState.BEARISH_DOWNTREND
            }
            else -> MarketTrendState.NEUTRAL_RANGING
        }

        return MarketStructureAnalysis(
            trendState = finalTrend,
            swingPoints = classifiedSwings,
            structureBreaks = structureBreaks,
            latestBreak = structureBreaks.lastOrNull(),
            recentHigh = activeSwingHigh?.price,
            recentLow = activeSwingLow?.price,
            isBullishBOSPresent = structureBreaks.any { it.type == MarketStructureBreakType.BOS_BULLISH },
            isBearishBOSPresent = structureBreaks.any { it.type == MarketStructureBreakType.BOS_BEARISH },
            isBullishCHoCHPresent = structureBreaks.any { it.type == MarketStructureBreakType.CHOCH_BULLISH },
            isBearishCHoCHPresent = structureBreaks.any { it.type == MarketStructureBreakType.CHOCH_BEARISH }
        )
    }

    // ==========================================
    // 2. ORDER BLOCKS ENGINE (Mitigation & Invalidation)
    // ==========================================

    private fun detectOrderBlocks(
        candles: List<Candle>,
        structure: MarketStructureAnalysis
    ): List<OrderBlock> {
        val n = candles.size
        val orderBlocks = mutableListOf<OrderBlock>()

        // Look for impulse displacement candles that triggered momentum
        val avgBody = candles.map { abs(it.close - it.open) }.average()

        for (i in 2 until (n - 2)) {
            val c = candles[i]
            val next1 = candles[i + 1]
            val next2 = candles[i + 2]

            // Bullish Order Block: Red (down) candle followed by a massive green displacement
            val isBullishDisplacement = (next1.close > next1.open) &&
                    (next1.close - next1.open > avgBody * 1.5) &&
                    (next2.close >= next1.close)
            val isDownCandle = c.close < c.open

            if (isDownCandle && isBullishDisplacement) {
                val top = max(c.open, c.close)
                val bottom = c.low

                // Check status against all subsequent candles up to latest
                var status = OrderBlockStatus.UNMITIGATED
                var mitigationIndex: Int? = null

                for (j in (i + 2) until n) {
                    val subCandle = candles[j]
                    if (subCandle.close < bottom) {
                        status = OrderBlockStatus.INVALIDATED
                        break
                    }
                    if (subCandle.low <= top && subCandle.low >= bottom) {
                        if (status != OrderBlockStatus.INVALIDATED) {
                            status = OrderBlockStatus.MITIGATED
                            mitigationIndex = j
                        }
                    }
                }

                orderBlocks.add(
                    OrderBlock(
                        id = "OB_BULL_$i",
                        type = OrderBlockType.BULLISH,
                        topPrice = top,
                        bottomPrice = bottom,
                        meanThresholdPrice = (top + bottom) / 2.0,
                        candleIndex = i,
                        timestamp = c.openTime,
                        volume = c.volume,
                        status = status,
                        mitigationCandleIndex = mitigationIndex,
                        strengthScore = if (status == OrderBlockStatus.UNMITIGATED) 5 else 3
                    )
                )
            }

            // Bearish Order Block: Green (up) candle followed by a massive red displacement
            val isBearishDisplacement = (next1.close < next1.open) &&
                    (next1.open - next1.close > avgBody * 1.5) &&
                    (next2.close <= next1.close)
            val isUpCandle = c.close > c.open

            if (isUpCandle && isBearishDisplacement) {
                val top = c.high
                val bottom = min(c.open, c.close)

                var status = OrderBlockStatus.UNMITIGATED
                var mitigationIndex: Int? = null

                for (j in (i + 2) until n) {
                    val subCandle = candles[j]
                    if (subCandle.close > top) {
                        status = OrderBlockStatus.INVALIDATED
                        break
                    }
                    if (subCandle.high >= bottom && subCandle.high <= top) {
                        if (status != OrderBlockStatus.INVALIDATED) {
                            status = OrderBlockStatus.MITIGATED
                            mitigationIndex = j
                        }
                    }
                }

                orderBlocks.add(
                    OrderBlock(
                        id = "OB_BEAR_$i",
                        type = OrderBlockType.BEARISH,
                        topPrice = top,
                        bottomPrice = bottom,
                        meanThresholdPrice = (top + bottom) / 2.0,
                        candleIndex = i,
                        timestamp = c.openTime,
                        volume = c.volume,
                        status = status,
                        mitigationCandleIndex = mitigationIndex,
                        strengthScore = if (status == OrderBlockStatus.UNMITIGATED) 5 else 3
                    )
                )
            }
        }

        // Return latest active order blocks prioritized by recency and unmitigated state
        return orderBlocks.takeLast(12).reversed()
    }

    // ==========================================
    // 3. FAIR VALUE GAPS (FVG) ENGINE
    // ==========================================

    private fun detectFairValueGaps(candles: List<Candle>): List<FairValueGap> {
        val n = candles.size
        val fvgs = mutableListOf<FairValueGap>()

        for (i in 2 until n) {
            val c1 = candles[i - 2]
            val c2 = candles[i - 1]
            val c3 = candles[i]

            // Bullish FVG: Low of candle 3 is strictly above High of candle 1
            if (c3.low > c1.high) {
                val top = c3.low
                val bottom = c1.high
                val gapSize = top - bottom
                val gapSizePercent = if (bottom > 0) (gapSize / bottom) * 100.0 else 0.0

                // Check fill status in all candles after i
                var lowestLowAfter = Double.MAX_VALUE
                for (j in (i + 1) until n) {
                    if (candles[j].low < lowestLowAfter) {
                        lowestLowAfter = candles[j].low
                    }
                }

                val status: FvgStatus
                val fillPercentage: Double

                if (lowestLowAfter == Double.MAX_VALUE || lowestLowAfter >= top) {
                    status = FvgStatus.UNFILLED
                    fillPercentage = 0.0
                } else if (lowestLowAfter <= bottom) {
                    status = FvgStatus.FULLY_FILLED
                    fillPercentage = 100.0
                } else {
                    status = FvgStatus.PARTIALLY_FILLED
                    val filledDepth = top - lowestLowAfter
                    fillPercentage = ((filledDepth / gapSize) * 100.0).coerceIn(0.0, 100.0)
                }

                fvgs.add(
                    FairValueGap(
                        id = "FVG_BULL_$i",
                        type = FvgType.BULLISH,
                        topPrice = top,
                        bottomPrice = bottom,
                        midPrice = (top + bottom) / 2.0,
                        candleIndex = i - 1,
                        timestamp = c2.openTime,
                        status = status,
                        fillPercentage = fillPercentage,
                        gapSizePercent = gapSizePercent
                    )
                )
            }

            // Bearish FVG: High of candle 3 is strictly below Low of candle 1
            if (c3.high < c1.low) {
                val top = c1.low
                val bottom = c3.high
                val gapSize = top - bottom
                val gapSizePercent = if (bottom > 0) (gapSize / bottom) * 100.0 else 0.0

                var highestHighAfter = Double.MIN_VALUE
                for (j in (i + 1) until n) {
                    if (candles[j].high > highestHighAfter) {
                        highestHighAfter = candles[j].high
                    }
                }

                val status: FvgStatus
                val fillPercentage: Double

                if (highestHighAfter == Double.MIN_VALUE || highestHighAfter <= bottom) {
                    status = FvgStatus.UNFILLED
                    fillPercentage = 0.0
                } else if (highestHighAfter >= top) {
                    status = FvgStatus.FULLY_FILLED
                    fillPercentage = 100.0
                } else {
                    status = FvgStatus.PARTIALLY_FILLED
                    val filledDepth = highestHighAfter - bottom
                    fillPercentage = ((filledDepth / gapSize) * 100.0).coerceIn(0.0, 100.0)
                }

                fvgs.add(
                    FairValueGap(
                        id = "FVG_BEAR_$i",
                        type = FvgType.BEARISH,
                        topPrice = top,
                        bottomPrice = bottom,
                        midPrice = (top + bottom) / 2.0,
                        candleIndex = i - 1,
                        timestamp = c2.openTime,
                        status = status,
                        fillPercentage = fillPercentage,
                        gapSizePercent = gapSizePercent
                    )
                )
            }
        }

        return fvgs.takeLast(16).reversed()
    }

    // ==========================================
    // 4. LIQUIDITY ANALYSIS ENGINE
    // ==========================================

    private fun detectLiquidity(
        candles: List<Candle>,
        swingPoints: List<SwingPoint>
    ): LiquidityAnalysis {
        val pools = mutableListOf<LiquidityPool>()
        val sweeps = mutableListOf<LiquiditySweepEvent>()
        val n = candles.size

        val swingHighs = swingPoints.filter { it.isHigh }
        val swingLows = swingPoints.filter { !it.isHigh }

        // Equal Highs (EQH) detection: Swings within 0.15% threshold
        for (i in 0 until (swingHighs.size - 1)) {
            val sh1 = swingHighs[i]
            val sh2 = swingHighs[i + 1]
            val diffPct = abs(sh1.price - sh2.price) / sh1.price

            if (diffPct <= 0.0015) {
                val avgPrice = (sh1.price + sh2.price) / 2.0
                // Check if swept by subsequent candles
                var isSwept = false
                var sweepIndex: Int? = null
                for (k in (sh2.index + 1) until n) {
                    if (candles[k].high > avgPrice) {
                        isSwept = true
                        sweepIndex = k
                        break
                    }
                }

                pools.add(
                    LiquidityPool(
                        id = "EQH_${sh1.index}_${sh2.index}",
                        type = LiquidityPoolType.EQUAL_HIGHS,
                        priceLevel = avgPrice,
                        swingIndices = listOf(sh1.index, sh2.index),
                        isSwept = isSwept,
                        sweepCandleIndex = sweepIndex,
                        description = "Equal Highs pool at $%.2f (Delta: %.2f%%)".format(avgPrice, diffPct * 100.0)
                    )
                )
            }
        }

        // Equal Lows (EQL) detection: Swings within 0.15% threshold
        for (i in 0 until (swingLows.size - 1)) {
            val sl1 = swingLows[i]
            val sl2 = swingLows[i + 1]
            val diffPct = abs(sl1.price - sl2.price) / sl1.price

            if (diffPct <= 0.0015) {
                val avgPrice = (sl1.price + sl2.price) / 2.0
                var isSwept = false
                var sweepIndex: Int? = null
                for (k in (sl2.index + 1) until n) {
                    if (candles[k].low < avgPrice) {
                        isSwept = true
                        sweepIndex = k
                        break
                    }
                }

                pools.add(
                    LiquidityPool(
                        id = "EQL_${sl1.index}_${sl2.index}",
                        type = LiquidityPoolType.EQUAL_LOWS,
                        priceLevel = avgPrice,
                        swingIndices = listOf(sl1.index, sl2.index),
                        isSwept = isSwept,
                        sweepCandleIndex = sweepIndex,
                        description = "Equal Lows pool at $%.2f (Delta: %.2f%%)".format(avgPrice, diffPct * 100.0)
                    )
                )
            }
        }

        // Individual BSL & SSL pools for major swing points
        swingHighs.takeLast(4).forEach { sh ->
            var isSwept = false
            for (k in (sh.index + 1) until n) {
                if (candles[k].high > sh.price) {
                    isSwept = true
                    break
                }
            }
            pools.add(
                LiquidityPool(
                    id = "BSL_${sh.index}",
                    type = LiquidityPoolType.BUY_SIDE_LIQUIDITY,
                    priceLevel = sh.price,
                    swingIndices = listOf(sh.index),
                    isSwept = isSwept,
                    description = "Buy-Side Liquidity resting above $%.2f".format(sh.price)
                )
            )
        }

        swingLows.takeLast(4).forEach { sl ->
            var isSwept = false
            for (k in (sl.index + 1) until n) {
                if (candles[k].low < sl.price) {
                    isSwept = true
                    break
                }
            }
            pools.add(
                LiquidityPool(
                    id = "SSL_${sl.index}",
                    type = LiquidityPoolType.SELL_SIDE_LIQUIDITY,
                    priceLevel = sl.price,
                    swingIndices = listOf(sl.index),
                    isSwept = isSwept,
                    description = "Sell-Side Liquidity resting below $%.2f".format(sl.price)
                )
            )
        }

        // Detect Liquidity Sweeps & Potential Stop-Hunts in recent price action
        val recentLookback = min(30, n - 1)
        for (i in (n - recentLookback) until n) {
            val c = candles[i]

            // Check against previous swing highs: Wick above swing high, but close below it!
            for (sh in swingHighs.filter { it.index < i }) {
                if (c.high > sh.price && c.close < sh.price) {
                    // Potential Stop-Hunt: immediate aggressive displacement down
                    val nextBar = if (i + 1 < n) candles[i + 1] else null
                    val isPotentialStopHunt = nextBar != null && nextBar.close < nextBar.open && (nextBar.open - nextBar.close > (c.high - c.close) * 0.5)

                    sweeps.add(
                        LiquiditySweepEvent(
                            candleIndex = i,
                            timestamp = c.openTime,
                            sweptLevel = sh.price,
                            poolType = LiquidityPoolType.BUY_SIDE_LIQUIDITY,
                            highWickPrice = c.high,
                            lowWickPrice = c.low,
                            closePrice = c.close,
                            isPotentialStopHunt = isPotentialStopHunt,
                            note = if (isPotentialStopHunt)
                                "Potential Bearish Stop-Hunt (Not Guaranteed): High wicked to $%.2f then rejected".format(c.high)
                            else
                                "Buy-side liquidity swept at $%.2f with wick rejection".format(sh.price)
                        )
                    )
                }
            }

            // Check against previous swing lows: Wick below swing low, but close above it!
            for (sl in swingLows.filter { it.index < i }) {
                if (c.low < sl.price && c.close > sl.price) {
                    val nextBar = if (i + 1 < n) candles[i + 1] else null
                    val isPotentialStopHunt = nextBar != null && nextBar.close > nextBar.open && (nextBar.close - nextBar.open > (c.close - c.low) * 0.5)

                    sweeps.add(
                        LiquiditySweepEvent(
                            candleIndex = i,
                            timestamp = c.openTime,
                            sweptLevel = sl.price,
                            poolType = LiquidityPoolType.SELL_SIDE_LIQUIDITY,
                            highWickPrice = c.high,
                            lowWickPrice = c.low,
                            closePrice = c.close,
                            isPotentialStopHunt = isPotentialStopHunt,
                            note = if (isPotentialStopHunt)
                                "Potential Bullish Stop-Hunt (Not Guaranteed): Low wicked to $%.2f then reclaimed".format(c.low)
                            else
                                "Sell-side liquidity swept at $%.2f with wick rejection".format(sl.price)
                        )
                    )
                }
            }
        }

        val currentPrice = candles.last().close
        val nearestBsl = pools.filter { it.type == LiquidityPoolType.BUY_SIDE_LIQUIDITY && it.priceLevel > currentPrice && !it.isSwept }
            .minByOrNull { it.priceLevel - currentPrice }?.priceLevel

        val nearestSsl = pools.filter { it.type == LiquidityPoolType.SELL_SIDE_LIQUIDITY && it.priceLevel < currentPrice && !it.isSwept }
            .minByOrNull { currentPrice - it.priceLevel }?.priceLevel

        return LiquidityAnalysis(
            pools = pools,
            recentSweeps = sweeps.takeLast(6).reversed(),
            bslNearestPrice = nearestBsl,
            sslNearestPrice = nearestSsl,
            potentialStopHuntActive = sweeps.any { it.isPotentialStopHunt && (n - 1 - it.candleIndex) <= 3 }
        )
    }

    // ==========================================
    // 5. SUPPORT & RESISTANCE / SUPPLY & DEMAND
    // ==========================================

    private fun detectSupportResistanceAndSupplyDemand(
        candles: List<Candle>,
        structure: MarketStructureAnalysis
    ): List<PriceLevelZone> {
        val currentPrice = candles.last().close
        val zones = mutableListOf<PriceLevelZone>()

        val pivots = structure.swingPoints.map { it.price }
        if (pivots.isEmpty()) return emptyList()

        // Group nearby pivots within 0.8% into zones
        val threshold = currentPrice * 0.008
        val sortedPivots = pivots.sorted()
        val clusters = mutableListOf<MutableList<Double>>()

        for (p in sortedPivots) {
            val existingCluster = clusters.firstOrNull { cluster ->
                val clusterMean = cluster.average()
                abs(p - clusterMean) <= threshold
            }
            if (existingCluster != null) {
                existingCluster.add(p)
            } else {
                clusters.add(mutableListOf(p))
            }
        }

        clusters.forEachIndexed { idx, cluster ->
            val center = cluster.average()
            val minP = cluster.minOrNull() ?: center
            val maxP = cluster.maxOrNull() ?: center
            val touches = cluster.size
            val distPct = ((center - currentPrice) / currentPrice) * 100.0

            val zoneType = when {
                touches >= 3 && center < currentPrice -> PriceZoneType.DEMAND
                touches >= 3 && center > currentPrice -> PriceZoneType.SUPPLY
                center < currentPrice -> PriceZoneType.SUPPORT
                else -> PriceZoneType.RESISTANCE
            }

            zones.add(
                PriceLevelZone(
                    id = "ZONE_$idx",
                    type = zoneType,
                    centralPrice = center,
                    upperPrice = max(maxP, center * 1.002),
                    lowerPrice = min(minP, center * 0.998),
                    touchesCount = touches,
                    strength = min(5, touches + 1),
                    isCurrentlyActive = abs(distPct) <= 15.0,
                    distancePercent = distPct
                )
            )
        }

        return zones.sortedBy { abs(it.distancePercent) }.take(8)
    }

    // ==========================================
    // 6. FIBONACCI RETRACEMENT & EXTENSION ENGINE
    // ==========================================

    private fun calculateFibonacciLevels(
        candles: List<Candle>,
        structure: MarketStructureAnalysis
    ): FibonacciAnalysis? {
        val swings = structure.swingPoints
        if (swings.size < 2) return null

        val lastHigh = swings.filter { it.isHigh }.lastOrNull() ?: return null
        val lastLow = swings.filter { !it.isHigh }.lastOrNull() ?: return null

        val isUptrend = lastHigh.index > lastLow.index
        val swingHighPrice = lastHigh.price
        val swingLowPrice = lastLow.price
        val range = swingHighPrice - swingLowPrice
        if (range <= 0.0) return null

        val retracementRatios = listOf(0.236, 0.382, 0.500, 0.618, 0.786)
        val extensionRatios = listOf(1.272, 1.618, 2.618)

        val retracementLevels = mutableListOf<FibLevel>()
        val extensionLevels = mutableListOf<FibLevel>()

        if (isUptrend) {
            // Uptrend swing: price pulled back down from high towards low
            for (ratio in retracementRatios) {
                val p = swingHighPrice - (range * ratio)
                retracementLevels.add(
                    FibLevel(
                        ratio = ratio,
                        price = p,
                        label = "Fib %.3f".format(ratio),
                        isExtension = false,
                        isGoldenPocket = ratio == 0.618,
                        isEquilibrium = ratio == 0.500
                    )
                )
            }

            for (ratio in extensionRatios) {
                val p = swingLowPrice + (range * ratio)
                extensionLevels.add(
                    FibLevel(
                        ratio = ratio,
                        price = p,
                        label = "Ext %.3f".format(ratio),
                        isExtension = true,
                        isGoldenPocket = ratio == 1.618
                    )
                )
            }
        } else {
            // Downtrend swing: price bounced up from low towards high
            for (ratio in retracementRatios) {
                val p = swingLowPrice + (range * ratio)
                retracementLevels.add(
                    FibLevel(
                        ratio = ratio,
                        price = p,
                        label = "Fib %.3f".format(ratio),
                        isExtension = false,
                        isGoldenPocket = ratio == 0.618,
                        isEquilibrium = ratio == 0.500
                    )
                )
            }

            for (ratio in extensionRatios) {
                val p = swingHighPrice - (range * ratio)
                extensionLevels.add(
                    FibLevel(
                        ratio = ratio,
                        price = p,
                        label = "Ext %.3f".format(ratio),
                        isExtension = true,
                        isGoldenPocket = ratio == 1.618
                    )
                )
            }
        }

        val gp618 = retracementLevels.firstOrNull { it.isGoldenPocket }?.price ?: 0.0
        val gp650 = if (isUptrend) swingHighPrice - (range * 0.650) else swingLowPrice + (range * 0.650)
        val goldenLow = min(gp618, gp650)
        val goldenHigh = max(gp618, gp650)
        val eqPrice = retracementLevels.firstOrNull { it.isEquilibrium }?.price ?: ((swingHighPrice + swingLowPrice) / 2.0)

        val currentPrice = candles.last().close
        val zoneDesc = when {
            isUptrend && currentPrice in goldenLow..goldenHigh -> "In Golden Pocket (0.618 - 0.650) Discount Zone"
            isUptrend && currentPrice < eqPrice -> "In Discount Zone (< 0.500) - Institutional Accumulation"
            isUptrend && currentPrice >= eqPrice -> "In Premium Zone (> 0.500)"
            !isUptrend && currentPrice in goldenLow..goldenHigh -> "In Golden Pocket Rejection Zone"
            !isUptrend && currentPrice > eqPrice -> "In Premium Zone (> 0.500) - Institutional Distribution"
            else -> "In Discount Zone (< 0.500)"
        }

        return FibonacciAnalysis(
            swingHighPrice = swingHighPrice,
            swingLowPrice = swingLowPrice,
            swingHighTime = lastHigh.timestamp,
            swingLowTime = lastLow.timestamp,
            isUptrendSwing = isUptrend,
            retracementLevels = retracementLevels,
            extensionLevels = extensionLevels,
            goldenPocketLow = goldenLow,
            goldenPocketHigh = goldenHigh,
            equilibriumPrice = eqPrice,
            currentFibZoneDescription = zoneDesc
        )
    }

    // ==========================================
    // 7. MULTI-TIMEFRAME ANALYSIS ENGINE
    // ==========================================

    private fun buildMultiTimeframeAnalysis(
        snapshots: List<TimeframeSmcSnapshot>
    ): MultiTimeframeSMCAnalysis {
        val bullishCount = snapshots.count { it.isBullishBias }
        val bearishCount = snapshots.count { !it.isBullishBias && it.trend == MarketTrendState.BEARISH_DOWNTREND }
        val neutralCount = snapshots.size - bullishCount - bearishCount

        val stance = when {
            bullishCount >= snapshots.size - 1 && snapshots.size >= 3 -> MtfConfluenceStance.STRONG_BULLISH_CONFLUENCE
            bullishCount > bearishCount && bullishCount >= 2 -> MtfConfluenceStance.MODERATE_BULLISH
            bearishCount >= snapshots.size - 1 && snapshots.size >= 3 -> MtfConfluenceStance.STRONG_BEARISH_CONFLUENCE
            bearishCount > bullishCount && bearishCount >= 2 -> MtfConfluenceStance.MODERATE_BEARISH
            else -> MtfConfluenceStance.NEUTRAL_MIXED
        }

        val agreeingTfs = snapshots.filter { it.isBullishBias }.joinToString(", ") { it.timeframe.label }
        val opposingTfs = snapshots.filter { !it.isBullishBias }.joinToString(", ") { it.timeframe.label }

        val summary = "$bullishCount/${snapshots.size} Timeframes Bullish. Dominant Bias: ${stance.label}."
        val details = if (opposingTfs.isNotBlank()) {
            "Agreement across [$agreeingTfs]. Counter-trend or pullbacks observed on [$opposingTfs]."
        } else {
            "Unanimous structural agreement across all monitored timeframes."
        }

        return MultiTimeframeSMCAnalysis(
            snapshots = snapshots,
            bullishTimeframeCount = bullishCount,
            bearishTimeframeCount = bearishCount,
            neutralTimeframeCount = neutralCount,
            confluenceStance = stance,
            agreementSummary = summary,
            disagreementDetails = details
        )
    }

    private fun createInsufficientDataResult(
        symbol: String,
        assetName: String,
        timeframe: CandleTimeframe,
        candles: List<Candle>
    ): SmcAnalysisResult {
        val curPrice = candles.lastOrNull()?.close ?: 0.0
        return SmcAnalysisResult(
            symbol = symbol,
            assetName = assetName,
            timeframe = timeframe,
            currentPrice = curPrice,
            candleCount = candles.size,
            marketStructure = MarketStructureAnalysis(
                trendState = MarketTrendState.NEUTRAL_RANGING,
                swingPoints = emptyList(),
                structureBreaks = emptyList(),
                latestBreak = null,
                recentHigh = null,
                recentLow = null,
                isBullishBOSPresent = false,
                isBearishBOSPresent = false,
                isBullishCHoCHPresent = false,
                isBearishCHoCHPresent = false
            ),
            orderBlocks = emptyList(),
            fairValueGaps = emptyList(),
            liquidity = LiquidityAnalysis(
                pools = emptyList(),
                recentSweeps = emptyList(),
                bslNearestPrice = null,
                sslNearestPrice = null,
                potentialStopHuntActive = false
            ),
            priceZones = emptyList(),
            fibonacci = null,
            multiTimeframe = null,
            isInsufficientData = true
        )
    }
}
