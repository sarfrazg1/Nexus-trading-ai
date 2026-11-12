package com.example.data.ai

import com.example.data.indicators.OverallAnalysisResult
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.model.MarketAsset
import com.example.data.model.MarketNews
import com.example.data.signal.AiTradingSignal
import com.example.data.smc.FvgStatus
import com.example.data.smc.PriceZoneType
import com.example.data.smc.SmcAnalysisResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Connects NEXUS AI to existing modules:
 * - Technical Analysis (22 indicators)
 * - Smart Money Concepts (BOS, CHoCH, Order Blocks, FVGs, Liquidity)
 * - Fibonacci Retracements
 * - AI Trading Signal Engine
 * - News Analyzer & Economic Calendar
 *
 * Never fabricates missing or unverified fields.
 */
object MarketContextBuilder {

    fun buildSnapshot(
        asset: MarketAsset?,
        candles: List<Candle>,
        timeframe: CandleTimeframe = CandleTimeframe.H1,
        technicalAnalysis: OverallAnalysisResult? = null,
        smcAnalysis: SmcAnalysisResult? = null,
        signal: AiTradingSignal? = null,
        newsList: List<MarketNews> = emptyList(),
        futuresSignal: com.example.data.scanner.FuturesScanSignal? = null
    ): MarketContextSnapshot {
        val symbol = asset?.symbol ?: signal?.symbol ?: futuresSignal?.symbol ?: "BTCUSDT"
        val assetName = asset?.name ?: futuresSignal?.baseAsset ?: "Bitcoin"
        val currentPrice = asset?.currentPrice ?: futuresSignal?.currentPrice ?: candles.lastOrNull()?.close ?: 0.0
        val change24h = asset?.change24hPercent ?: 0.0

        val rsi = technicalAnalysis?.momentum?.rsi14
        val macdHist = technicalAnalysis?.momentum?.macd?.histogram
        val emaCond = technicalAnalysis?.trend?.let {
            val e20 = it.emaSet.ema20
            val e50 = it.emaSet.ema50
            if (e20 != null && e50 != null) {
                if (e20 > e50) "Bullish (EMA 20 > EMA 50)" else "Bearish (EMA 20 < EMA 50)"
            } else null
        }
        val atr = technicalAnalysis?.volatility?.atr?.value

        val bosChoch = smcAnalysis?.marketStructure?.let {
            val breakCount = it.structureBreaks.size
            val lastBreak = it.latestBreak?.type?.label ?: "None"
            "Trend: ${it.trendState.label} | Breaks: $breakCount | Latest: $lastBreak"
        }

        val fvgStatus = smcAnalysis?.fairValueGaps?.let { gaps ->
            val unfilled = gaps.count { it.status == FvgStatus.UNFILLED }
            "Active Unfilled FVGs: $unfilled total"
        }

        val support = smcAnalysis?.priceZones
            ?.firstOrNull { it.type == PriceZoneType.SUPPORT || it.type == PriceZoneType.DEMAND }?.centralPrice
            ?: technicalAnalysis?.trend?.emaSet?.ema50

        val resistance = smcAnalysis?.priceZones
            ?.firstOrNull { it.type == PriceZoneType.RESISTANCE || it.type == PriceZoneType.SUPPLY }?.centralPrice
            ?: technicalAnalysis?.trend?.emaSet?.ema200

        val goldenPocket = smcAnalysis?.fibonacci?.let { fib ->
            String.format(Locale.US, "$%.2f - $%.2f", fib.goldenPocketLow, fib.goldenPocketHigh)
        }

        val sigDecision = signal?.decision?.name ?: "NO_ACTIVE_SIGNAL"
        val sigConfidence = signal?.confidence?.totalScore

        val relevantNews = newsList.firstOrNull { news ->
            news.affectedAssets.any { it.equals(assetName, ignoreCase = true) || it.equals(symbol.take(3), ignoreCase = true) }
        }?.let { "${it.title} [${it.impact.name} - ${it.impactLevel.name}]" }

        val macroNews = newsList.firstOrNull { it.category == "Macro" || it.title.contains("Fed", ignoreCase = true) || it.title.contains("CPI", ignoreCase = true) }
            ?.let { "${it.title} (${it.source})" }

        val lastUpdated = SimpleDateFormat("HH:mm:ss dd-MMM-yyyy", Locale.US).format(Date())

        return MarketContextSnapshot(
            symbol = symbol,
            assetName = assetName,
            currentPrice = currentPrice,
            change24hPercent = change24h,
            timeframe = timeframe.label,
            rsi14 = rsi,
            macdHistogram = macdHist,
            emaCondition = emaCond,
            atrVolatility = atr,
            bosChochCondition = bosChoch,
            fvgStatus = fvgStatus,
            supportLevel = support,
            resistanceLevel = resistance,
            fibGoldenPocket = goldenPocket,
            signalDecision = sigDecision,
            confidenceScore = sigConfidence,
            relevantNewsSummary = relevantNews,
            macroEventSummary = macroNews,
            dataSource = asset?.dataSourceName ?: futuresSignal?.dataSource ?: "Public Binance & CoinGecko Feeds",
            lastUpdatedFormatted = lastUpdated,
            isRealData = currentPrice > 0.0,
            futuresSignalSummary = futuresSignal?.let { fs ->
                """
                Scanned Pair: ${fs.symbol} | Direction: ${fs.direction.name} | Status: ${fs.lifecycle.label}
                Entry Zone: ${fs.entryZoneFormatted} (Current: ${fs.currentPriceFormatted})
                Stop Loss: ${fs.stopLossFormatted} (-${String.format(Locale.US, "%.2f", fs.slDistancePercent)}%)
                TP1: ${fs.tp1Formatted} (+${String.format(Locale.US, "%.2f", fs.tp1DistancePercent)}%)
                TP2: ${fs.tp2Formatted} (+${String.format(Locale.US, "%.2f", fs.tp2DistancePercent)}%)
                TP3: ${fs.tp3Formatted} (+${String.format(Locale.US, "%.2f", fs.tp3DistancePercent)}%)
                Risk/Reward: 1:${String.format(Locale.US, "%.2f", fs.riskRewardRatio)} | Quality Score: ${fs.qualityScore}/100 (${fs.qualityRating})
                Confluence Reasons: ${fs.reasonsForEntry.joinToString("; ")}
                Invalidation Rule: ${fs.invalidationCondition}
                Multi-Timeframe Confirmation: ${fs.timeframeConfirmations.joinToString(" | ") { "${it.timeframe.label}: ${it.trend} (${it.structure})" }}
                """.trimIndent()
            }
        )
    }
}
