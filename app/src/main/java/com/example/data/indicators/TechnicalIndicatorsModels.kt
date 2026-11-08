package com.example.data.indicators

import com.example.data.model.CandleTimeframe

enum class IndicatorStance(val label: String) {
    STRONG_BULLISH("Strong Bullish"),
    BULLISH("Bullish"),
    NEUTRAL("Neutral"),
    BEARISH("Bearish"),
    STRONG_BEARISH("Strong Bearish"),
    OVERBOUGHT("Overbought"),
    OVERSOLD("Oversold"),
    INSUFFICIENT_DATA("Need Data")
}

enum class IndicatorCategory(val label: String) {
    ALL("All Indicators"),
    TREND("Trend"),
    MOMENTUM("Momentum"),
    VOLATILITY("Volatility"),
    VOLUME("Volume")
}

// ---- Trend Group Models ----
data class EmaSet(
    val ema9: Double?,
    val ema20: Double?,
    val ema50: Double?,
    val ema100: Double?,
    val ema200: Double?
)

data class SmaSet(
    val sma20: Double?,
    val sma50: Double?,
    val sma100: Double?,
    val sma200: Double?
)

data class SupertrendResult(
    val value: Double,
    val isBullish: Boolean,
    val upperBand: Double,
    val lowerBand: Double,
    val stance: IndicatorStance
)

data class AdxResult(
    val adx: Double,
    val plusDI: Double,
    val minusDI: Double,
    val stance: IndicatorStance,
    val strengthDescription: String
)

data class IchimokuResult(
    val tenkanSen: Double,
    val kijunSen: Double,
    val senkouSpanA: Double,
    val senkouSpanB: Double,
    val chikouSpan: Double,
    val stance: IndicatorStance,
    val cloudDescription: String
)

data class TrendIndicators(
    val emaSet: EmaSet,
    val smaSet: SmaSet,
    val vwap: Double?,
    val vwapStance: IndicatorStance,
    val supertrend: SupertrendResult?,
    val adx: AdxResult?,
    val ichimoku: IchimokuResult?
)

// ---- Momentum Group Models ----
data class MacdResult(
    val macdLine: Double,
    val signalLine: Double,
    val histogram: Double,
    val stance: IndicatorStance,
    val crossoverStatus: String
)

data class StochRsiResult(
    val k: Double,
    val d: Double,
    val stance: IndicatorStance
)

data class StochasticResult(
    val k: Double,
    val d: Double,
    val stance: IndicatorStance
)

data class MomentumIndicators(
    val rsi7: Double?,
    val rsi7Stance: IndicatorStance,
    val rsi14: Double?,
    val rsi14Stance: IndicatorStance,
    val rsi21: Double?,
    val rsi21Stance: IndicatorStance,
    val macd: MacdResult?,
    val stochRsi: StochRsiResult?,
    val stochastic: StochasticResult?,
    val cci: Double?,
    val cciStance: IndicatorStance,
    val williamsR: Double?,
    val williamsRStance: IndicatorStance,
    val roc: Double?,
    val rocStance: IndicatorStance
)

// ---- Volatility Group Models ----
data class BollingerResult(
    val upper: Double,
    val middle: Double,
    val lower: Double,
    val bandwidth: Double,
    val percentB: Double,
    val isSqueeze: Boolean,
    val stance: IndicatorStance
)

data class AtrResult(
    val value: Double,
    val percentOfPrice: Double
)

data class KeltnerResult(
    val upper: Double,
    val middle: Double,
    val lower: Double,
    val stance: IndicatorStance
)

data class DonchianResult(
    val upper: Double,
    val middle: Double,
    val lower: Double,
    val stance: IndicatorStance
)

data class VolatilityIndicators(
    val bollingerBands: BollingerResult?,
    val atr: AtrResult?,
    val keltnerChannels: KeltnerResult?,
    val donchianChannels: DonchianResult?
)

// ---- Volume Group Models ----
data class ObvResult(
    val obv: Double,
    val obvSma20: Double?,
    val stance: IndicatorStance
)

data class VolumeDeltaResult(
    val delta: Double,
    val buyerVolume: Double,
    val sellerVolume: Double,
    val cvd: Double,
    val stance: IndicatorStance,
    val hasReliableData: Boolean,
    val telemetryNote: String
)

data class VolumeIndicators(
    val obv: ObvResult?,
    val mfi: Double?,
    val mfiStance: IndicatorStance,
    val cmf: Double?,
    val cmfStance: IndicatorStance,
    val relativeVolume: Double?,
    val relativeVolumeStance: IndicatorStance,
    val volumeDelta: VolumeDeltaResult?
)

// ---- UI Presentation Card Model ----
data class IndicatorCardItem(
    val id: String,
    val name: String,
    val category: IndicatorCategory,
    val primaryValue: String,
    val secondaryValue: String? = null,
    val stance: IndicatorStance,
    val interpretation: String,
    val formulaInfo: String,
    val isAvailable: Boolean = true
)

// ---- Complete Comprehensive Analysis Package ----
data class OverallAnalysisResult(
    val symbol: String,
    val assetName: String,
    val timeframe: CandleTimeframe,
    val candleCount: Int,
    val currentClose: Double,
    val priceChangePercent: Double,
    val stance: IndicatorStance,
    val summaryText: String,
    val bullishCount: Int,
    val bearishCount: Int,
    val neutralCount: Int,
    val trend: TrendIndicators,
    val momentum: MomentumIndicators,
    val volatility: VolatilityIndicators,
    val volume: VolumeIndicators,
    val cards: List<IndicatorCardItem>,
    val warnings: List<String> = emptyList(),
    val calculationTimestamp: Long = System.currentTimeMillis()
)
