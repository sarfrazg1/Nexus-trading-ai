package com.example.data.model

/**
 * Real historical OHLCV Candlestick data model.
 */
data class Candle(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val closeTime: Long = openTime,
    val quoteVolume: Double = 0.0,
    val tradesCount: Int = 0,
    val takerBuyBaseVolume: Double = 0.0,
    val takerBuyQuoteVolume: Double = 0.0
) {
    val typicalPrice: Double
        get() = (high + low + close) / 3.0

    val trueRange: Double
        get() = high - low

    // Volume Delta calculation from buyer-initiated vs seller-initiated volume
    val hasTakerData: Boolean
        get() = takerBuyBaseVolume > 0.0 && volume > 0.0

    val buyerVolume: Double
        get() = takerBuyBaseVolume

    val sellerVolume: Double
        get() = (volume - takerBuyBaseVolume).coerceAtLeast(0.0)

    val volumeDelta: Double
        get() = if (hasTakerData) (buyerVolume - sellerVolume) else 0.0
}

/**
 * 12 supported timeframes as specified by the user:
 * 1m, 3m, 5m, 15m, 30m, 1h, 2h, 4h, 6h, 12h, 1D and 1W
 */
enum class CandleTimeframe(
    val label: String,
    val binanceInterval: String,
    val yahooInterval: String,
    val yahooRange: String,
    val displayName: String
) {
    M1("1m", "1m", "1m", "1d", "1 Minute"),
    M3("3m", "3m", "2m", "1d", "3 Minutes"),
    M5("5m", "5m", "5m", "2d", "5 Minutes"),
    M15("15m", "15m", "15m", "5d", "15 Minutes"),
    M30("30m", "30m", "30m", "5d", "30 Minutes"),
    H1("1h", "1h", "60m", "1mo", "1 Hour"),
    H2("2h", "2h", "60m", "1mo", "2 Hours"),
    H4("4h", "4h", "60m", "3mo", "4 Hours"),
    H6("6h", "6h", "1d", "6mo", "6 Hours"),
    H12("12h", "12h", "1d", "1y", "12 Hours"),
    D1("1D", "1d", "1d", "1y", "1 Day"),
    W1("1W", "1w", "1wk", "2y", "1 Week");

    companion object {
        fun fromLabel(label: String): CandleTimeframe =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: H1
    }
}
