package com.example.data.scanner

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.util.NexusNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository managing:
 * 1. User-selected watchlist pairs to scan.
 * 2. Active signals, developing setups, and historical outcomes.
 * 3. Real-time signal lifecycle state machine (Price vs TP1/TP2/TP3/SL).
 * 4. Paper trading execution & statistics.
 * 5. Cooldown rules to prevent repeat duplicate notifications.
 */
class FuturesScannerRepository(
    private val context: Context? = null,
    private val notificationManager: NexusNotificationManager? = null
) {
    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Default top USDT-M Futures pairs
    private val defaultPairs = listOf(
        FuturesWatchlistPair("BTCUSDT", "BTC", "Bitcoin Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("ETHUSDT", "ETH", "Ethereum Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("SOLUSDT", "SOL", "Solana Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("BNBUSDT", "BNB", "BNB Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("XRPUSDT", "XRP", "XRP Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("DOGEUSDT", "DOGE", "Dogecoin Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("ADAUSDT", "ADA", "Cardano Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("AVAXUSDT", "AVAX", "Avalanche Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("LINKUSDT", "LINK", "Chainlink Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("SUIUSDT", "SUI", "Sui Futures", isSelectedForScanning = true),
        FuturesWatchlistPair("NEARUSDT", "NEAR", "Near Protocol Futures", isSelectedForScanning = false),
        FuturesWatchlistPair("APTUSDT", "APT", "Aptos Futures", isSelectedForScanning = false)
    )

    private val _watchlist = MutableStateFlow<List<FuturesWatchlistPair>>(defaultPairs)
    val watchlist: StateFlow<List<FuturesWatchlistPair>> = _watchlist.asStateFlow()

    private val _scannerState = MutableStateFlow(ScannerEngineState())
    val scannerState: StateFlow<ScannerEngineState> = _scannerState.asStateFlow()

    // Alert cooldown per symbol (45 minutes = 2,700,000 ms)
    private val alertCooldowns = ConcurrentHashMap<String, Long>()
    private val COOLDOWN_DURATION_MS = 45 * 60 * 1000L

    init {
        loadWatchlistFromStorage()
        loadSignalHistoryFromStorage()
    }

    // ==========================================
    // WATCHLIST MANAGEMENT
    // ==========================================

    fun togglePairScanning(symbol: String) {
        _watchlist.update { currentList ->
            currentList.map { pair ->
                if (pair.symbol.equals(symbol, ignoreCase = true)) {
                    pair.copy(isSelectedForScanning = !pair.isSelectedForScanning)
                } else pair
            }
        }
        saveWatchlistToStorage()
        updateSelectedWatchlistCount()
    }

    fun addCustomPair(symbolInput: String) {
        val clean = symbolInput.uppercase().trim()
        val symbol = if (clean.endsWith("USDT")) clean else "${clean}USDT"
        val base = symbol.removeSuffix("USDT")

        if (_watchlist.value.none { it.symbol == symbol }) {
            val newPair = FuturesWatchlistPair(
                symbol = symbol,
                baseAsset = base,
                displayName = "$base Futures",
                isSelectedForScanning = true
            )
            _watchlist.update { it + newPair }
            saveWatchlistToStorage()
            updateSelectedWatchlistCount()
        }
    }

    fun removePair(symbol: String) {
        _watchlist.update { current ->
            current.filterNot { it.symbol.equals(symbol, ignoreCase = true) }
        }
        saveWatchlistToStorage()
        updateSelectedWatchlistCount()
    }

    fun getSelectedPairs(): List<FuturesWatchlistPair> {
        return _watchlist.value.filter { it.isSelectedForScanning }
    }

    private fun updateSelectedWatchlistCount() {
        _scannerState.update { state ->
            state.copy(selectedWatchlistCount = _watchlist.value.count { it.isSelectedForScanning })
        }
    }

    // ==========================================
    // LIVE SIGNAL INTAKE & STATE MACHINE
    // ==========================================

    /**
     * Ingests a new evaluation from BinanceFuturesScannerEngine.
     */
    fun onSignalEvaluated(signal: FuturesScanSignal) {
        val now = System.currentTimeMillis()

        if (signal.lifecycle == FuturesSignalLifecycle.ENTRY_CONFIRMED) {
            // Check if we already have an active signal for this symbol
            val existingActive = _scannerState.value.activeSignals.firstOrNull { it.symbol == signal.symbol }
            if (existingActive != null) {
                return // Do not duplicate active signal
            }

            // Check alert cooldown
            val lastAlertTime = alertCooldowns[signal.symbol] ?: 0L
            val canAlert = (now - lastAlertTime) > COOLDOWN_DURATION_MS

            if (canAlert) {
                alertCooldowns[signal.symbol] = now
                triggerSignalNotification(signal)
            }

            // Add to active signals
            _scannerState.update { state ->
                val updatedActive = listOf(signal) + state.activeSignals.filterNot { it.symbol == signal.symbol }
                val updatedSetups = state.watchlistSetups.filterNot { it.symbol == signal.symbol }
                state.copy(
                    activeSignals = updatedActive,
                    watchlistSetups = updatedSetups,
                    statistics = computeStatistics(updatedActive, state.signalHistory)
                )
            }
        } else if (signal.lifecycle == FuturesSignalLifecycle.WATCHLIST_SETUP) {
            // Update or add developing setup
            _scannerState.update { state ->
                val updatedSetups = listOf(signal) + state.watchlistSetups.filterNot { it.symbol == signal.symbol }
                state.copy(
                    watchlistSetups = updatedSetups,
                    statistics = computeStatistics(state.activeSignals, state.signalHistory)
                )
            }
        }
    }

    /**
     * Evaluates live prices from WebSocket stream against active signals to update lifecycles.
     */
    fun onLivePricesUpdated(priceMap: Map<String, Double>) {
        if (priceMap.isEmpty()) return

        // Update watchlist prices
        _watchlist.update { current ->
            current.map { pair ->
                val livePrice = priceMap[pair.symbol]
                if (livePrice != null) pair.copy(currentPrice = livePrice) else pair
            }
        }

        // Check active signals against live price
        _scannerState.update { state ->
            val stillActive = mutableListOf<FuturesScanSignal>()
            val newlyCompleted = mutableListOf<FuturesScanSignal>()

            for (signal in state.activeSignals) {
                val currentPrice = priceMap[signal.symbol] ?: signal.currentPrice
                var updatedLifecycle = signal.lifecycle
                var outcomeSummary: String? = null
                var paperExitPrice: Double? = null
                var paperPnl: Double? = null

                if (signal.direction == FuturesDirection.LONG) {
                    when {
                        currentPrice >= signal.takeProfit3 -> {
                            updatedLifecycle = FuturesSignalLifecycle.TARGET_3_HIT
                            outcomeSummary = "TP3 Hit: +${String.format(Locale.US, "%.2f", signal.tp3DistancePercent)}%"
                            paperExitPrice = signal.takeProfit3
                            paperPnl = signal.tp3DistancePercent - 0.10 // Less 0.05% entry + 0.05% exit fee
                        }
                        currentPrice >= signal.takeProfit2 && signal.lifecycle != FuturesSignalLifecycle.TARGET_2_HIT -> {
                            updatedLifecycle = FuturesSignalLifecycle.TARGET_2_HIT
                            outcomeSummary = "TP2 Hit: +${String.format(Locale.US, "%.2f", signal.tp2DistancePercent)}%"
                            paperExitPrice = signal.takeProfit2
                            paperPnl = signal.tp2DistancePercent - 0.10
                        }
                        currentPrice >= signal.takeProfit1 && signal.lifecycle == FuturesSignalLifecycle.ACTIVE -> {
                            updatedLifecycle = FuturesSignalLifecycle.TARGET_1_HIT
                        }
                        currentPrice <= signal.stopLoss -> {
                            updatedLifecycle = FuturesSignalLifecycle.STOP_LOSS_HIT
                            outcomeSummary = "Stop Loss Hit: -${String.format(Locale.US, "%.2f", signal.slDistancePercent)}%"
                            paperExitPrice = signal.stopLoss
                            paperPnl = -signal.slDistancePercent - 0.10
                        }
                    }
                } else {
                    // SHORT
                    when {
                        currentPrice <= signal.takeProfit3 -> {
                            updatedLifecycle = FuturesSignalLifecycle.TARGET_3_HIT
                            outcomeSummary = "TP3 Hit: +${String.format(Locale.US, "%.2f", signal.tp3DistancePercent)}%"
                            paperExitPrice = signal.takeProfit3
                            paperPnl = signal.tp3DistancePercent - 0.10
                        }
                        currentPrice <= signal.takeProfit2 && signal.lifecycle != FuturesSignalLifecycle.TARGET_2_HIT -> {
                            updatedLifecycle = FuturesSignalLifecycle.TARGET_2_HIT
                            outcomeSummary = "TP2 Hit: +${String.format(Locale.US, "%.2f", signal.tp2DistancePercent)}%"
                            paperExitPrice = signal.takeProfit2
                            paperPnl = signal.tp2DistancePercent - 0.10
                        }
                        currentPrice <= signal.takeProfit1 && signal.lifecycle == FuturesSignalLifecycle.ACTIVE -> {
                            updatedLifecycle = FuturesSignalLifecycle.TARGET_1_HIT
                        }
                        currentPrice >= signal.stopLoss -> {
                            updatedLifecycle = FuturesSignalLifecycle.STOP_LOSS_HIT
                            outcomeSummary = "Stop Loss Hit: -${String.format(Locale.US, "%.2f", signal.slDistancePercent)}%"
                            paperExitPrice = signal.stopLoss
                            paperPnl = -signal.slDistancePercent - 0.10
                        }
                    }
                }

                val updatedSignal = signal.copy(
                    currentPrice = currentPrice,
                    lifecycle = updatedLifecycle,
                    outcomeSummary = outcomeSummary ?: signal.outcomeSummary,
                    paperTradeExitPrice = paperExitPrice ?: signal.paperTradeExitPrice,
                    paperTradePnlPercent = paperPnl ?: signal.paperTradePnlPercent
                )

                // If resolved, move to history
                if (updatedLifecycle in listOf(
                        FuturesSignalLifecycle.TARGET_2_HIT,
                        FuturesSignalLifecycle.TARGET_3_HIT,
                        FuturesSignalLifecycle.STOP_LOSS_HIT,
                        FuturesSignalLifecycle.EXPIRED,
                        FuturesSignalLifecycle.INVALIDATED
                    )
                ) {
                    newlyCompleted.add(updatedSignal)
                } else {
                    stillActive.add(updatedSignal)
                }
            }

            val updatedHistory = newlyCompleted + state.signalHistory
            if (newlyCompleted.isNotEmpty()) {
                saveSignalHistoryToStorage(updatedHistory)
            }

            state.copy(
                activeSignals = stillActive,
                signalHistory = updatedHistory,
                statistics = computeStatistics(stillActive, updatedHistory)
            )
        }
    }

    fun updateScannerStatus(
        status: ScannerStatus,
        currentlyScanning: String? = null,
        errorMessage: String? = null
    ) {
        _scannerState.update { state ->
            state.copy(
                status = status,
                currentlyScanningSymbol = currentlyScanning,
                errorMessage = errorMessage,
                lastScanCompletedTimestamp = if (status == ScannerStatus.IDLE) System.currentTimeMillis() else state.lastScanCompletedTimestamp,
                lastScanFormatted = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            )
        }
    }

    fun setPaperTradingEnabled(enabled: Boolean) {
        _scannerState.update { it.copy(paperTradingEnabled = enabled) }
        prefs?.edit()?.putBoolean("paper_trading_enabled", enabled)?.apply()
    }

    fun setScanInterval(seconds: Int) {
        _scannerState.update { it.copy(scanIntervalSeconds = seconds) }
        prefs?.edit()?.putInt("scan_interval_seconds", seconds)?.apply()
    }

    fun setMinQualityScore(score: Int) {
        _scannerState.update { it.copy(minQualityScoreFilter = score) }
        prefs?.edit()?.putInt("min_quality_score", score)?.apply()
    }

    fun setSoundAlerts(enabled: Boolean) {
        _scannerState.update { it.copy(soundAlertsEnabled = enabled) }
        prefs?.edit()?.putBoolean("sound_alerts_enabled", enabled)?.apply()
    }

    fun setVibrationAlerts(enabled: Boolean) {
        _scannerState.update { it.copy(vibrationEnabled = enabled) }
        prefs?.edit()?.putBoolean("vibration_alerts_enabled", enabled)?.apply()
    }

    // ==========================================
    // NOTIFICATION TRIGGER
    // ==========================================

    private fun triggerSignalNotification(signal: FuturesScanSignal) {
        notificationManager?.notifyFuturesScanSignal(
            symbol = signal.symbol,
            isLong = signal.direction == FuturesDirection.LONG,
            entryZone = signal.entryZoneFormatted,
            sl = signal.stopLossFormatted,
            tp1 = signal.tp1Formatted,
            qualityScore = signal.qualityScore
        )
    }

    // ==========================================
    // STATISTICS CALCULATION
    // ==========================================

    private fun computeStatistics(
        active: List<FuturesScanSignal>,
        history: List<FuturesScanSignal>
    ): ScannerStatistics {
        val total = active.size + history.size
        val tp1 = history.count { it.lifecycle == FuturesSignalLifecycle.TARGET_1_HIT }
        val tp2 = history.count { it.lifecycle == FuturesSignalLifecycle.TARGET_2_HIT }
        val tp3 = history.count { it.lifecycle == FuturesSignalLifecycle.TARGET_3_HIT }
        val sl = history.count { it.lifecycle == FuturesSignalLifecycle.STOP_LOSS_HIT }
        val expired = history.count { it.lifecycle == FuturesSignalLifecycle.EXPIRED }
        val invalidated = history.count { it.lifecycle == FuturesSignalLifecycle.INVALIDATED }

        val wins = tp1 + tp2 + tp3
        val completedTrades = wins + sl
        val winRate = if (completedTrades > 0) (wins.toDouble() / completedTrades) * 100.0 else 0.0

        val avgRr = if (history.isNotEmpty()) {
            history.map { it.riskRewardRatio }.average()
        } else 2.5

        val paperTrades = history.filter { it.paperTradePnlPercent != null }
        val netPaperPnl = paperTrades.sumOf { it.paperTradePnlPercent ?: 0.0 }

        return ScannerStatistics(
            totalSignalsScanned = total,
            activeSignalsCount = active.size,
            confirmedSignalsCount = active.count { it.lifecycle == FuturesSignalLifecycle.ENTRY_CONFIRMED },
            watchlistSetupsCount = _scannerState.value.watchlistSetups.size,
            tp1HitsCount = tp1,
            tp2HitsCount = tp2,
            tp3HitsCount = tp3,
            stopLossHitsCount = sl,
            expiredCount = expired,
            invalidatedCount = invalidated,
            winRatePercent = winRate,
            averageRiskReward = avgRr,
            totalPaperTrades = paperTrades.size,
            paperTradingNetPnlPercent = netPaperPnl
        )
    }

    // ==========================================
    // PERSISTENCE STORAGE
    // ==========================================

    private fun saveWatchlistToStorage() {
        try {
            val array = JSONArray()
            _watchlist.value.forEach { pair ->
                val obj = JSONObject().apply {
                    put("symbol", pair.symbol)
                    put("base", pair.baseAsset)
                    put("name", pair.displayName)
                    put("selected", pair.isSelectedForScanning)
                }
                array.put(obj)
            }
            prefs?.edit()?.putString(KEY_WATCHLIST, array.toString())?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving watchlist: ${e.message}")
        }
    }

    private fun loadWatchlistFromStorage() {
        val jsonStr = prefs?.getString(KEY_WATCHLIST, null) ?: return
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<FuturesWatchlistPair>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FuturesWatchlistPair(
                        symbol = obj.getString("symbol"),
                        baseAsset = obj.getString("base"),
                        displayName = obj.getString("name"),
                        isSelectedForScanning = obj.optBoolean("selected", true)
                    )
                )
            }
            if (list.isNotEmpty()) {
                _watchlist.value = list
                updateSelectedWatchlistCount()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading watchlist: ${e.message}")
        }
    }

    private fun saveSignalHistoryToStorage(history: List<FuturesScanSignal>) {
        try {
            val array = JSONArray()
            history.take(50).forEach { sig ->
                val obj = JSONObject().apply {
                    put("id", sig.id)
                    put("symbol", sig.symbol)
                    put("base", sig.baseAsset)
                    put("direction", sig.direction.name)
                    put("currentPrice", sig.currentPrice)
                    put("entryZoneMin", sig.entryZoneMin)
                    put("entryZoneMax", sig.entryZoneMax)
                    put("entryPrice", sig.entryPrice)
                    put("stopLoss", sig.stopLoss)
                    put("tp1", sig.takeProfit1)
                    put("tp2", sig.takeProfit2)
                    put("tp3", sig.takeProfit3)
                    put("rr", sig.riskRewardRatio)
                    put("score", sig.qualityScore)
                    put("lifecycle", sig.lifecycle.name)
                    put("timestamp", sig.timestamp)
                    put("outcome", sig.outcomeSummary ?: "")
                    put("paperPnl", sig.paperTradePnlPercent ?: 0.0)
                }
                array.put(obj)
            }
            prefs?.edit()?.putString(KEY_HISTORY, array.toString())?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving history: ${e.message}")
        }
    }

    private fun loadSignalHistoryFromStorage() {
        val jsonStr = prefs?.getString(KEY_HISTORY, null) ?: return
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<FuturesScanSignal>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FuturesScanSignal(
                        id = obj.getString("id"),
                        symbol = obj.getString("symbol"),
                        baseAsset = obj.getString("base"),
                        direction = FuturesDirection.valueOf(obj.getString("direction")),
                        currentPrice = obj.getDouble("currentPrice"),
                        entryZoneMin = obj.getDouble("entryZoneMin"),
                        entryZoneMax = obj.getDouble("entryZoneMax"),
                        entryPrice = obj.getDouble("entryPrice"),
                        stopLoss = obj.getDouble("stopLoss"),
                        takeProfit1 = obj.getDouble("tp1"),
                        takeProfit2 = obj.getDouble("tp2"),
                        takeProfit3 = obj.getDouble("tp3"),
                        riskRewardRatio = obj.getDouble("rr"),
                        qualityScore = obj.getInt("score"),
                        qualityRating = "Scanned Setup",
                        timeframeConfirmations = emptyList(),
                        reasonsForEntry = listOf("Historical verified signal"),
                        invalidationCondition = "Closed",
                        lifecycle = FuturesSignalLifecycle.valueOf(obj.getString("lifecycle")),
                        timestamp = obj.getLong("timestamp"),
                        outcomeSummary = obj.optString("outcome").takeIf { it.isNotEmpty() },
                        paperTradePnlPercent = obj.optDouble("paperPnl").takeIf { !it.isNaN() }
                    )
                )
            }
            _scannerState.update { state ->
                state.copy(
                    signalHistory = list,
                    statistics = computeStatistics(state.activeSignals, list)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading signal history: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "FuturesScannerRepo"
        private const val PREFS_NAME = "nexus_futures_scanner_prefs"
        private const val KEY_WATCHLIST = "futures_watchlist_json"
        private const val KEY_HISTORY = "futures_history_json"

        // Singleton instance holder for service and UI coordination
        @Volatile
        private var instance: FuturesScannerRepository? = null

        fun getInstance(context: Context? = null, notificationManager: NexusNotificationManager? = null): FuturesScannerRepository {
            return instance ?: synchronized(this) {
                instance ?: FuturesScannerRepository(context?.applicationContext ?: context, notificationManager).also { instance = it }
            }
        }
    }
}
