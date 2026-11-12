package com.example.data.scanner

import com.example.data.model.CandleTimeframe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Signal Lifecycle States for Binance USDT-M Futures Scanner.
 */
enum class FuturesSignalLifecycle(val label: String, val badgeColorHex: Long) {
    WATCHLIST_SETUP("Watchlist Setup", 0xFFE5A93C),    // Developing setup, awaiting LTF confirmation
    ENTRY_CONFIRMED("Entry Confirmed", 0xFF00E676),    // All MTF conditions met, currently inside Entry Zone
    ACTIVE("Active in Trade", 0xFF29B6F6),             // Entry filled, monitoring targets and stop loss
    TARGET_1_HIT("TP1 Hit", 0xFF00C853),               // Conservative target reached (+1.5R - 2R)
    TARGET_2_HIT("TP2 Hit", 0xFF00E676),               // Structural target reached (+2.5R - 3R)
    TARGET_3_HIT("TP3 Hit (Max)", 0xFF69F0AE),         // Extended runner target reached (+4R+)
    STOP_LOSS_HIT("Stop Loss Hit", 0xFFFF5252),        // Invalidation level hit
    EXPIRED("Expired", 0xFF9E9E9E),                    // Price moved away without triggering entry
    INVALIDATED("Invalidated", 0xFF757575)             // Market structure broke before entry
}

/**
 * Direction of Futures Opportunity.
 */
enum class FuturesDirection(val label: String) {
    LONG("LONG"),
    SHORT("SHORT")
}

/**
 * Multi-Timeframe Stance for 4H, 1H, 15M, and 5M.
 */
data class TimeframeStance(
    val timeframe: CandleTimeframe,
    val trend: String,         // "Bullish", "Bearish", "Consolidating"
    val structure: String,     // "BOS Up", "CHoCH Down", "Inside Order Block", "At Support"
    val isConfirmed: Boolean,
    val detail: String
)

/**
 * Complete Scanned Signal Model for Binance USDT-M Futures.
 */
data class FuturesScanSignal(
    val id: String = UUID.randomUUID().toString(),
    val symbol: String,                          // e.g. "BTCUSDT"
    val baseAsset: String,                       // e.g. "BTC"
    val direction: FuturesDirection,             // LONG or SHORT
    val currentPrice: Double,
    val entryZoneMin: Double,
    val entryZoneMax: Double,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val takeProfit3: Double,
    val riskRewardRatio: Double,
    val qualityScore: Int,                       // 0 - 100 quality/confluence score
    val qualityRating: String,                   // "A+ Confluence", "High Quality", "Moderate"
    val timeframeConfirmations: List<TimeframeStance>,
    val reasonsForEntry: List<String>,           // Specific technical/SMC confluence reasons
    val invalidationCondition: String,          // Exact condition that cancels the trade
    val lifecycle: FuturesSignalLifecycle,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTimestamp: String = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US).format(Date(timestamp)),
    val primaryTimeframe: CandleTimeframe = CandleTimeframe.M15,
    val suggestedLeverage: Int = 3,              // Capped 3x-5x for risk control
    val atrVolatilityPercent: Double = 0.0,
    val fundingRatePercent: Double? = null,
    val dataSource: String = "Binance USDT-M Futures Public REST/WS",
    val maxPriceReached: Double = currentPrice,
    val minPriceReached: Double = currentPrice,
    val outcomeSummary: String? = null,
    val paperTradeExecuted: Boolean = false,
    val paperTradeEntryPrice: Double? = null,
    val paperTradeExitPrice: Double? = null,
    val paperTradePnlPercent: Double? = null
) {
    val entryZoneFormatted: String
        get() = String.format(Locale.US, "$%.2f - $%.2f", entryZoneMin, entryZoneMax)

    val currentPriceFormatted: String
        get() = String.format(Locale.US, "$%.2f", currentPrice)

    val stopLossFormatted: String
        get() = String.format(Locale.US, "$%.2f", stopLoss)

    val tp1Formatted: String
        get() = String.format(Locale.US, "$%.2f", takeProfit1)

    val tp2Formatted: String
        get() = String.format(Locale.US, "$%.2f", takeProfit2)

    val tp3Formatted: String
        get() = String.format(Locale.US, "$%.2f", takeProfit3)

    val slDistancePercent: Double
        get() = if (entryPrice > 0) Math.abs((entryPrice - stopLoss) / entryPrice) * 100.0 else 0.0

    val tp1DistancePercent: Double
        get() = if (entryPrice > 0) Math.abs((takeProfit1 - entryPrice) / entryPrice) * 100.0 else 0.0

    val tp2DistancePercent: Double
        get() = if (entryPrice > 0) Math.abs((takeProfit2 - entryPrice) / entryPrice) * 100.0 else 0.0

    val tp3DistancePercent: Double
        get() = if (entryPrice > 0) Math.abs((takeProfit3 - entryPrice) / entryPrice) * 100.0 else 0.0
}

/**
 * Watchlist Pair configured for USDT-M Futures scanning.
 */
data class FuturesWatchlistPair(
    val symbol: String,                 // e.g. "BTCUSDT"
    val baseAsset: String,              // e.g. "BTC"
    val displayName: String,            // e.g. "Bitcoin Futures"
    val isSelectedForScanning: Boolean = true,
    val currentPrice: Double = 0.0,
    val priceChange24hPercent: Double = 0.0,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24hUsdt: Double = 0.0,
    val fundingRatePercent: Double? = null,
    val lastScannedTimestamp: Long? = null,
    val activeSignalCount: Int = 0
)

/**
 * Scanner Service Running Status.
 */
enum class ScannerStatus {
    IDLE,
    SCANNING,
    PAUSED,
    ERROR,
    DISCONNECTED
}

/**
 * Comprehensive Scanner Performance Statistics.
 */
data class ScannerStatistics(
    val totalSignalsScanned: Int = 0,
    val activeSignalsCount: Int = 0,
    val confirmedSignalsCount: Int = 0,
    val watchlistSetupsCount: Int = 0,
    val tp1HitsCount: Int = 0,
    val tp2HitsCount: Int = 0,
    val tp3HitsCount: Int = 0,
    val stopLossHitsCount: Int = 0,
    val expiredCount: Int = 0,
    val invalidatedCount: Int = 0,
    val winRatePercent: Double = 0.0,
    val averageRiskReward: Double = 0.0,
    val totalPaperTrades: Int = 0,
    val paperTradingNetPnlPercent: Double = 0.0
)

/**
 * Real-time Scanner Engine State.
 */
data class ScannerEngineState(
    val status: ScannerStatus = ScannerStatus.IDLE,
    val isServiceRunning: Boolean = false,
    val isWebSocketConnected: Boolean = false,
    val selectedWatchlistCount: Int = 0,
    val currentlyScanningSymbol: String? = null,
    val lastScanCompletedTimestamp: Long = 0L,
    val lastScanFormatted: String = "Never",
    val activeSignals: List<FuturesScanSignal> = emptyList(),
    val watchlistSetups: List<FuturesScanSignal> = emptyList(),
    val signalHistory: List<FuturesScanSignal> = emptyList(),
    val statistics: ScannerStatistics = ScannerStatistics(),
    val paperTradingEnabled: Boolean = false,
    val scanIntervalSeconds: Int = 30,
    val minQualityScoreFilter: Int = 60,
    val soundAlertsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val errorMessage: String? = null
)
