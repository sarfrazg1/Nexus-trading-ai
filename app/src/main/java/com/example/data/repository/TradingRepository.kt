package com.example.data.repository

import android.util.Log
import com.example.data.cache.MarketDataCache
import com.example.data.model.AlertDirection
import com.example.data.model.ApiFeedConfig
import com.example.data.model.AssetCategory
import com.example.data.model.ConnectionStatus
import com.example.data.model.MarketAlert
import com.example.data.model.MarketAsset
import com.example.data.model.MarketDataStatus
import com.example.data.model.MarketNews
import com.example.data.model.MarketOverview
import com.example.data.model.NewsImpact
import com.example.data.model.ScannerItem
import com.example.data.model.Signal
import com.example.data.model.SignalStatus
import com.example.data.model.SignalType
import com.example.data.signal.AiTradingSignal
import com.example.data.signal.AiTradingSignalEngine
import com.example.data.signal.SignalDecision
import com.example.data.remote.BinanceTicker24hr
import com.example.data.remote.BinanceWebSocketManager
import com.example.data.remote.CoinGeckoApiService
import com.example.data.remote.CommoditiesApiService
import com.example.data.remote.CommodityQuote
import com.example.data.indicators.OverallAnalysisResult
import com.example.data.indicators.TechnicalIndicatorsEngine
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.smc.FvgStatus
import com.example.data.smc.MarketTrendState
import com.example.data.smc.OrderBlockStatus
import com.example.data.smc.PriceZoneType
import com.example.data.smc.SmcAnalysisResult
import com.example.data.smc.SmcCalculationEngine
import com.example.data.smc.TimeframeSmcSnapshot
import com.example.data.remote.CandleApiService
import com.example.data.remote.BinanceClient
import com.example.data.remote.CoinPriceQuote
import com.example.data.remote.LiveTickerUpdate
import com.example.data.remote.MarketApiService
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TradingRepository(
    private val binanceClient: BinanceClient = BinanceClient(),
    private val webSocketManager: BinanceWebSocketManager = BinanceWebSocketManager(),
    private val coinGeckoService: CoinGeckoApiService = CoinGeckoApiService(),
    private val commoditiesService: CommoditiesApiService = CommoditiesApiService(),
    private val candleService: CandleApiService = CandleApiService()
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val cache = MarketDataCache()

    val watchlistRepository = com.example.data.watchlist.WatchlistRepository()
    val financialNewsService = com.example.data.news.FinancialNewsService()
    val notificationManager = com.example.util.NexusNotificationManager()

    private val _assets = MutableStateFlow<List<MarketAsset>>(initialAssets().map {
        it.copy(isFavorite = watchlistRepository.isFavorite(it.id))
    })
    val assets: StateFlow<List<MarketAsset>> = _assets.asStateFlow()

    private val _signals = MutableStateFlow<List<Signal>>(initialSignals())
    val signals: StateFlow<List<Signal>> = _signals.asStateFlow()

    private val _aiSignals = MutableStateFlow<List<AiTradingSignal>>(emptyList())
    val aiSignals: StateFlow<List<AiTradingSignal>> = _aiSignals.asStateFlow()

    private val _alerts = MutableStateFlow<List<MarketAlert>>(initialAlerts())
    val alerts: StateFlow<List<MarketAlert>> = _alerts.asStateFlow()

    private val _news = MutableStateFlow<List<MarketNews>>(financialNewsService.getVerifiedMacroNews())
    val news: StateFlow<List<MarketNews>> = _news.asStateFlow()

    private val _scannerItems = MutableStateFlow<List<ScannerItem>>(initialScannerItems())
    val scannerItems: StateFlow<List<ScannerItem>> = _scannerItems.asStateFlow()

    private val _apiConfigs = MutableStateFlow<List<ApiFeedConfig>>(initialApiConfigs())
    val apiConfigs: StateFlow<List<ApiFeedConfig>> = _apiConfigs.asStateFlow()

    private val _marketOverview = MutableStateFlow(MarketOverview())
    val marketOverview: StateFlow<MarketOverview> = _marketOverview.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val isWebSocketLive: StateFlow<Boolean> = webSocketManager.isConnected

    // Performance & Data Saving Controls
    private val _isDataSavingMode = MutableStateFlow(false)
    val isDataSavingMode: StateFlow<Boolean> = _isDataSavingMode.asStateFlow()

    private val _isLowPerformanceMode = MutableStateFlow(false)
    val isLowPerformanceMode: StateFlow<Boolean> = _isLowPerformanceMode.asStateFlow()

    private val _refreshIntervalSeconds = MutableStateFlow(15)
    val refreshIntervalSeconds: StateFlow<Int> = _refreshIntervalSeconds.asStateFlow()

    private val _lastMarketDataUpdateMillis = MutableStateFlow(System.currentTimeMillis())
    val lastMarketDataUpdateMillis: StateFlow<Long> = _lastMarketDataUpdateMillis.asStateFlow()

    private val candleCache = mutableMapOf<String, Pair<Long, List<Candle>>>()

    init {
        // Cache initial asset templates
        cache.putAll(_assets.value)

        // Observe real-time Binance WebSocket ticker stream
        repositoryScope.launch {
            webSocketManager.tickerUpdates.collect { updates ->
                if (!_isDataSavingMode.value) {
                    handleWebSocketUpdates(updates)
                }
            }
        }

        // Start WebSocket streaming if not in data-saving mode
        if (!_isDataSavingMode.value) {
            webSocketManager.start()
        }

        // Trigger initial data load
        repositoryScope.launch {
            refreshData()
        }
    }

    fun setDataSavingMode(enabled: Boolean) {
        _isDataSavingMode.value = enabled
        if (enabled) {
            webSocketManager.stop()
            if (_refreshIntervalSeconds.value == 15) {
                _refreshIntervalSeconds.value = 60
            }
        } else {
            webSocketManager.start()
        }
    }

    fun setLowPerformanceMode(enabled: Boolean) {
        _isLowPerformanceMode.value = enabled
        if (enabled) {
            candleCache.clear()
        }
    }

    fun setRefreshIntervalSeconds(seconds: Int) {
        _refreshIntervalSeconds.value = seconds
    }

    fun clearCache(): Int {
        val count = candleCache.size
        candleCache.clear()
        return count
    }

    private fun handleWebSocketUpdates(updates: List<LiveTickerUpdate>) {
        val now = System.currentTimeMillis()
        val timeStr = formatTimestamp(now)
        val updateMap = updates.associateBy { it.symbol }

        _assets.value = _assets.value.map { asset ->
            val binanceSymbol = assetToBinanceSymbol(asset.id)
            val update = updateMap[binanceSymbol]

            if (update != null) {
                val formattedVol = formatCryptoVolume(update.volume, asset.id)
                val newAsset = asset.copy(
                    currentPrice = update.lastPrice,
                    change24h = (update.lastPrice * (update.priceChangePercent / 100.0)),
                    change24hPercent = update.priceChangePercent,
                    high24h = update.highPrice,
                    low24h = update.lowPrice,
                    volume24h = formattedVol,
                    connectionStatus = ConnectionStatus.CONNECTED,
                    dataStatus = MarketDataStatus.LIVE,
                    dataSourceName = "Binance WebSocket (Live)",
                    dataSourceInfo = "Real-time ticker stream • Updated $timeStr",
                    lastUpdated = now,
                    lastUpdatedFormatted = timeStr,
                    sparklinePoints = if (asset.sparklinePoints.isNotEmpty()) {
                        asset.sparklinePoints.drop(1) + update.lastPrice.toFloat()
                    } else {
                        generateSparkline(update.lastPrice, update.priceChangePercent)
                    }
                )
                cache.put(newAsset)
                checkAlertTriggers(asset.symbol, update.lastPrice)
                newAsset
            } else {
                asset
            }
        }
        updateOverviewCounts()
    }

    suspend fun refreshData() {
        _isRefreshing.value = true
        withContext(Dispatchers.IO) {
            try {
                // 1. Fetch Binance REST Tickers for all supported crypto symbols
                fetchCryptoRestTickers()

                // 2. Fetch Commodities (Gold, Silver, WTI, Brent, Natural Gas)
                val commoditiesKey = _apiConfigs.value.firstOrNull { it.id == "commodities_feed" }?.apiKey ?: ""
                val commodityQuotes = commoditiesService.fetchCommodityQuotes(commoditiesKey)
                updateCommoditiesState(commodityQuotes)

                // 3. Fetch CoinGecko Global Data
                fetchCoinGeckoData()

            } catch (e: Exception) {
                Log.w("TradingRepo", "Data refresh error: ${e.message}")
                // In case of network disconnection or rate limits, mark existing data as STALE
                _assets.value = cache.markAllAsStale(e.localizedMessage ?: "Network error")
            } finally {
                updateOverviewCounts()
                _isRefreshing.value = false
            }
        }
    }

    private val cryptoToCoinGeckoMap = mapOf(
        "BTC" to "bitcoin",
        "ETH" to "ethereum",
        "BNB" to "binancecoin",
        "SOL" to "solana",
        "XRP" to "ripple",
        "DOGE" to "dogecoin",
        "ADA" to "cardano",
        "LINK" to "chainlink",
        "AVAX" to "avalanche-2",
        "TRX" to "tron",
        "TON" to "the-open-network",
        "LTC" to "litecoin",
        "DOT" to "polkadot",
        "BCH" to "bitcoin-cash",
        "SUI" to "sui"
    )

    private suspend fun fetchCryptoRestTickers() {
        val symbolsList = listOf(
            "BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT",
            "DOGEUSDT", "ADAUSDT", "LINKUSDT", "AVAXUSDT", "TRXUSDT",
            "TONUSDT", "LTCUSDT", "DOTUSDT", "BCHUSDT", "SUIUSDT"
        )
        val jsonSymbols = "[\"" + symbolsList.joinToString("\",\"") + "\"]"

        // Tier 1 & 2: Binance Global with automatic failover to Binance US on HTTP 451
        try {
            val tickers = binanceClient.get24hrTickers(jsonSymbols)
            if (tickers.isNotEmpty()) {
                applyBinanceTickers(tickers, binanceClient.activeSourceLabel)
                return
            }
        } catch (e: Exception) {
            Log.w("TradingRepo", "Binance batch tickers unavailable (${e.message}). Switching to CoinGecko.")
        }

        // Tier 3: CoinGecko simple price quotes (no geo-restrictions)
        try {
            val cgQuotes = coinGeckoService.fetchSimplePrices(cryptoToCoinGeckoMap.values.toList())
            if (cgQuotes.isNotEmpty()) {
                applyCoinGeckoQuotes(cgQuotes)
                return
            }
        } catch (e: Exception) {
            Log.w("TradingRepo", "CoinGecko fallback failed (${e.message}). Trying Coinbase spot.")
        }

        // Tier 4: Coinbase public spot fallback for major pairs
        fetchCoinbaseSpotFallback()
    }

    private fun applyBinanceTickers(tickers: List<BinanceTicker24hr>, sourceLabel: String) {
        val tickerMap = tickers.associateBy { it.symbol }
        val now = System.currentTimeMillis()
        val timeStr = formatTimestamp(now)

        _assets.value = _assets.value.map { asset ->
            val binanceSymbol = assetToBinanceSymbol(asset.id)
            val ticker = tickerMap[binanceSymbol]

            if (ticker != null) {
                val lastPrice = ticker.lastPrice.toDoubleOrNull()
                val change = ticker.priceChange.toDoubleOrNull()
                val changePct = ticker.priceChangePercent.toDoubleOrNull()
                val high = ticker.highPrice.toDoubleOrNull()
                val low = ticker.lowPrice.toDoubleOrNull()
                val vol = ticker.volume.toDoubleOrNull() ?: 0.0

                if (lastPrice != null) {
                    val isWsActive = webSocketManager.isConnected.value
                    val source = if (isWsActive) "Binance WebSocket (Live)" else sourceLabel
                    val status = if (isWsActive) MarketDataStatus.LIVE else MarketDataStatus.DELAYED

                    val newAsset = asset.copy(
                        currentPrice = lastPrice,
                        change24h = change,
                        change24hPercent = changePct,
                        high24h = high,
                        low24h = low,
                        volume24h = formatCryptoVolume(vol, asset.id),
                        connectionStatus = ConnectionStatus.CONNECTED,
                        dataStatus = status,
                        dataSourceName = source,
                        dataSourceInfo = "$source • 24hr Ticker",
                        lastUpdated = now,
                        lastUpdatedFormatted = timeStr,
                        sparklinePoints = generateSparkline(lastPrice, changePct ?: 0.0)
                    )
                    cache.put(newAsset)
                    checkAlertTriggers(asset.symbol, lastPrice)
                    newAsset
                } else asset
            } else asset
        }
    }

    private fun applyCoinGeckoQuotes(quotes: Map<String, CoinPriceQuote>) {
        val now = System.currentTimeMillis()
        val timeStr = formatTimestamp(now)

        _assets.value = _assets.value.map { asset ->
            val cgId = cryptoToCoinGeckoMap[asset.id]
            val quote = quotes[cgId]
            if (quote != null) {
                val price = quote.price
                val changePct = quote.change24hPercent
                val change = price * (changePct / 100.0)
                val volStr = formatLargeCurrency(quote.volume24hUsd)

                val newAsset = asset.copy(
                    currentPrice = price,
                    change24h = change,
                    change24hPercent = changePct,
                    high24h = price * (1.0 + (if (changePct > 0) changePct / 100.0 else 0.015)),
                    low24h = price * (1.0 - (if (changePct < 0) -changePct / 100.0 else 0.015)),
                    volume24h = volStr,
                    connectionStatus = ConnectionStatus.CONNECTED,
                    dataStatus = MarketDataStatus.LIVE,
                    dataSourceName = "CoinGecko Free API",
                    dataSourceInfo = "CoinGecko Real-Time Feed • 24hr Change",
                    lastUpdated = now,
                    lastUpdatedFormatted = timeStr,
                    sparklinePoints = generateSparkline(price, changePct)
                )
                cache.put(newAsset)
                checkAlertTriggers(asset.symbol, price)
                newAsset
            } else asset
        }
    }

    private suspend fun fetchCoinbaseSpotFallback() = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val now = System.currentTimeMillis()
        val timeStr = formatTimestamp(now)

        val updatedMap = mutableMapOf<String, Double>()
        for ((assetId, pair) in listOf("BTC" to "BTC-USD", "ETH" to "ETH-USD", "SOL" to "SOL-USD", "XRP" to "XRP-USD")) {
            try {
                val req = Request.Builder()
                    .url("https://api.coinbase.com/v2/prices/$pair/spot")
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: return@use
                        val root = JSONObject(body)
                        val data = root.optJSONObject("data")
                        val amount = data?.optDouble("amount", Double.NaN) ?: Double.NaN
                        if (!amount.isNaN() && amount > 0) {
                            updatedMap[assetId] = amount
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("TradingRepo", "Coinbase spot fetch failed for $assetId: ${e.message}")
            }
        }

        if (updatedMap.isNotEmpty()) {
            _assets.value = _assets.value.map { asset ->
                val price = updatedMap[asset.id]
                if (price != null) {
                    val newAsset = asset.copy(
                        currentPrice = price,
                        connectionStatus = ConnectionStatus.CONNECTED,
                        dataStatus = MarketDataStatus.LIVE,
                        dataSourceName = "Coinbase Public API",
                        dataSourceInfo = "Coinbase Spot • Live Price",
                        lastUpdated = now,
                        lastUpdatedFormatted = timeStr,
                        sparklinePoints = generateSparkline(price, 0.5)
                    )
                    cache.put(newAsset)
                    newAsset
                } else asset
            }
        }
    }

    private fun updateCommoditiesState(quotes: Map<String, CommodityQuote>) {
        val now = System.currentTimeMillis()
        val timeStr = formatTimestamp(now)

        _assets.value = _assets.value.map { asset ->
            val quote = quotes[asset.id]
            if (quote != null) {
                val updated = asset.copy(
                    currentPrice = quote.price,
                    change24h = quote.change,
                    change24hPercent = quote.changePercent,
                    high24h = quote.high24h,
                    low24h = quote.low24h,
                    volume24h = quote.volume,
                    connectionStatus = ConnectionStatus.CONNECTED,
                    dataStatus = quote.status,
                    dataSourceName = quote.sourceName,
                    dataSourceInfo = "${quote.sourceName} • $timeStr",
                    lastUpdated = quote.timestamp,
                    lastUpdatedFormatted = timeStr,
                    sparklinePoints = if (quote.sparkline.isNotEmpty()) quote.sparkline else generateSparkline(quote.price, quote.changePercent),
                    setupInstructions = null
                )
                cache.put(updated)
                checkAlertTriggers(asset.symbol, quote.price)
                updated
            } else if (asset.category == AssetCategory.COMMODITIES && asset.currentPrice == null) {
                // Commodity has no live quote: clearly mark as UNAVAILABLE with setup instructions
                asset.copy(
                    connectionStatus = ConnectionStatus.NOT_CONNECTED,
                    dataStatus = MarketDataStatus.UNAVAILABLE,
                    dataSourceName = "Exchange Feed (Key Required)",
                    dataSourceInfo = "Data Not Connected • API Key Required",
                    setupInstructions = "Setup required: Add a free TwelveData or AlphaVantage API key in Settings > Market Data Providers to stream real-time quotes for ${asset.name}."
                )
            } else {
                asset
            }
        }
    }

    private suspend fun fetchCoinGeckoData() {
        val global = coinGeckoService.getGlobalMarketData()
        if (global != null) {
            _marketOverview.value = _marketOverview.value.copy(
                totalCryptoMarketCap = global.totalMarketCapUsd,
                cryptoVolume24h = global.totalVolume24hUsd,
                btcDominance = global.btcDominance,
                dataProviderSummary = "Binance WebSocket (Active) • CoinGecko API • Commodities Feed"
            )
        }
    }

    private fun checkAlertTriggers(symbol: String, currentPrice: Double) {
        _alerts.value = _alerts.value.map { alert ->
            if (alert.isActive && !alert.isTriggered && alert.assetSymbol.equals(symbol, ignoreCase = true)) {
                val hit = when (alert.direction) {
                    AlertDirection.ABOVE -> currentPrice >= alert.targetPrice
                    AlertDirection.BELOW -> currentPrice <= alert.targetPrice
                }
                if (hit) {
                    notificationManager.notifyPriceTarget(
                        alertId = alert.id,
                        symbol = alert.assetSymbol,
                        targetPrice = alert.targetPrice,
                        currentPrice = currentPrice,
                        directionAbove = alert.direction == AlertDirection.ABOVE
                    )
                    alert.copy(isTriggered = true, lastTriggeredTime = System.currentTimeMillis())
                } else alert
            } else alert
        }
    }

    fun toggleFavorite(assetId: String) {
        val isFav = watchlistRepository.toggleFavorite(assetId)
        _assets.value = _assets.value.map {
            if (it.id.equals(assetId, ignoreCase = true)) it.copy(isFavorite = isFav) else it
        }
        if (isFav) {
            notificationManager.notifyWatchlistUpdate(assetId.uppercase(), "Added to priority Watchlist")
        }
    }

    suspend fun refreshNews(forceRefresh: Boolean = false) {
        try {
            val freshNews = financialNewsService.fetchMarketNews(forceRefresh)
            if (freshNews.isNotEmpty()) {
                _news.value = freshNews
                freshNews.firstOrNull { it.impactLevel == com.example.data.model.NewsImpactLevel.HIGH }?.let { highNews ->
                    notificationManager.notifyNews(highNews)
                }
            }
        } catch (e: Exception) {
            Log.w("TradingRepo", "Failed refreshing news: ${e.message}")
        }
    }

    suspend fun refreshScanner(timeframe: com.example.data.model.CandleTimeframe = com.example.data.model.CandleTimeframe.H1) {
        try {
            val assetsToScan = _assets.value.take(12)
            val scanned = mutableListOf<com.example.data.model.ScannerItem>()
            for (asset in assetsToScan) {
                val candles = getCandles(asset.id, timeframe)
                val item = com.example.data.scanner.MarketScannerEngine.scanAsset(asset, candles, timeframe)
                scanned.add(item)
            }
            if (scanned.isNotEmpty()) {
                _scannerItems.value = scanned
            }
        } catch (e: Exception) {
            Log.w("TradingRepo", "Failed scanning market assets: ${e.message}")
        }
    }

    private fun updateOverviewCounts() {
        val liveOrDelayedCount = _assets.value.count {
            it.dataStatus == MarketDataStatus.LIVE || it.dataStatus == MarketDataStatus.DELAYED
        }
        val totalCount = _assets.value.size
        _marketOverview.value = _marketOverview.value.copy(
            connectedFeedsCount = liveOrDelayedCount,
            totalFeedsCount = totalCount,
            activeSignalsCount = _signals.value.count { it.status == SignalStatus.ACTIVE }
        )
    }

    fun toggleAlert(alertId: String) {
        _alerts.value = _alerts.value.map { alert ->
            if (alert.id == alertId) alert.copy(isActive = !alert.isActive) else alert
        }
    }

    fun deleteAlert(alertId: String) {
        _alerts.value = _alerts.value.filter { it.id != alertId }
    }

    fun createAlert(
        assetSymbol: String,
        assetName: String,
        targetPrice: Double,
        direction: AlertDirection,
        note: String
    ) {
        val newAlert = MarketAlert(
            id = "alert_${System.currentTimeMillis()}",
            assetSymbol = assetSymbol,
            assetName = assetName,
            targetPrice = targetPrice,
            direction = direction,
            isActive = true,
            isTriggered = false,
            createdTime = System.currentTimeMillis(),
            note = note
        )
        _alerts.value = listOf(newAlert) + _alerts.value
    }

    fun toggleNewsBookmark(newsId: String) {
        _news.value = _news.value.map { item ->
            if (item.id == newsId) item.copy(isSaved = !item.isSaved) else item
        }
    }

    fun connectApiFeed(feedId: String, apiKey: String): Boolean {
        if (apiKey.isBlank()) return false
        _apiConfigs.value = _apiConfigs.value.map { config ->
            if (config.id == feedId) {
                config.copy(
                    apiKey = apiKey,
                    isConnected = true,
                    lastPing = "Connected just now"
                )
            } else config
        }
        repositoryScope.launch {
            refreshData()
        }
        return true
    }

    fun disconnectApiFeed(feedId: String) {
        _apiConfigs.value = _apiConfigs.value.map { config ->
            if (config.id == feedId) config.copy(isConnected = false, lastPing = "Disconnected") else config
        }
        updateOverviewCounts()
    }

    private fun assetToBinanceSymbol(id: String): String {
        return when (id) {
            "BTC" -> "BTCUSDT"
            "ETH" -> "ETHUSDT"
            "BNB" -> "BNBUSDT"
            "SOL" -> "SOLUSDT"
            "XRP" -> "XRPUSDT"
            "DOGE" -> "DOGEUSDT"
            "ADA" -> "ADAUSDT"
            "LINK" -> "LINKUSDT"
            "AVAX" -> "AVAXUSDT"
            "TRX" -> "TRXUSDT"
            "TON" -> "TONUSDT"
            "LTC" -> "LTCUSDT"
            "DOT" -> "DOTUSDT"
            "BCH" -> "BCHUSDT"
            "SUI" -> "SUIUSDT"
            "XAU" -> "PAXGUSDT" // Real physical Gold token on Binance
            else -> "${id}USDT"
        }
    }

    private fun formatCryptoVolume(vol: Double, id: String): String {
        return when {
            vol >= 1_000_000 -> String.format("%.2fM %s", vol / 1_000_000.0, id)
            vol >= 1_000 -> String.format("%.2fK %s", vol / 1_000.0, id)
            else -> String.format("%.2f %s", vol, id)
        }
    }

    private fun formatLargeCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000_000.0 -> String.format(Locale.US, "$%.2fB", amount / 1_000_000_000.0)
            amount >= 1_000_000.0 -> String.format(Locale.US, "$%.2fM", amount / 1_000_000.0)
            amount >= 1_000.0 -> String.format(Locale.US, "$%.2fK", amount / 1_000.0)
            else -> String.format(Locale.US, "$%.2f", amount)
        }
    }

    private fun formatTimestamp(millis: Long): String {
        val sdf = SimpleDateFormat("h:mm:ss a", Locale.US)
        return sdf.format(Date(millis))
    }

    private fun generateSparkline(basePrice: Double, changePct: Double): List<Float> {
        val points = mutableListOf<Float>()
        val startPrice = basePrice * (1.0 - (changePct / 100.0))
        val step = (basePrice - startPrice) / 8.0
        for (i in 0..8) {
            val variance = (if (i % 2 == 0) 1 else -1) * (basePrice * 0.003)
            val p = (startPrice + (step * i) + variance).toFloat()
            points.add(p)
        }
        points[points.size - 1] = basePrice.toFloat()
        return points
    }

    companion object {
        fun initialAssets(): List<MarketAsset> = listOf(
            // 1. Primary Dashboard Crypto
            MarketAsset(
                id = "BTC",
                name = "Bitcoin",
                symbol = "BTC/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "ETH",
                name = "Ethereum",
                symbol = "ETH/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "BNB",
                name = "BNB Chain",
                symbol = "BNB/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "SOL",
                name = "Solana",
                symbol = "SOL/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "XRP",
                name = "Ripple",
                symbol = "XRP/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "DOGE",
                name = "Dogecoin",
                symbol = "DOGE/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "ADA",
                name = "Cardano",
                symbol = "ADA/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "LINK",
                name = "Chainlink",
                symbol = "LINK/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "AVAX",
                name = "Avalanche",
                symbol = "AVAX/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "TRX",
                name = "TRON",
                symbol = "TRX/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "TON",
                name = "Toncoin",
                symbol = "TON/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "LTC",
                name = "Litecoin",
                symbol = "LTC/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "DOT",
                name = "Polkadot",
                symbol = "DOT/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "BCH",
                name = "Bitcoin Cash",
                symbol = "BCH/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),
            MarketAsset(
                id = "SUI",
                name = "Sui Network",
                symbol = "SUI/USDT",
                category = AssetCategory.CRYPTO,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Binance WebSocket",
                dataSourceInfo = "Binance Public Stream",
                unit = "$"
            ),

            // 2. Commodities (Gold, Silver, WTI, Brent, Natural Gas)
            MarketAsset(
                id = "XAU",
                name = "Gold Spot (XAUUSD)",
                symbol = "XAU/USD",
                category = AssetCategory.COMMODITIES,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "NYMEX/COMEX Delayed Feed",
                dataSourceInfo = "Precious Metals Benchmark",
                setupInstructions = "Provider key required. Configure in Settings > Market Data Providers.",
                unit = "$/oz"
            ),
            MarketAsset(
                id = "XAG",
                name = "Silver Spot (XAGUSD)",
                symbol = "XAG/USD",
                category = AssetCategory.COMMODITIES,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "NYMEX/COMEX Delayed Feed",
                dataSourceInfo = "Precious Metals Benchmark",
                setupInstructions = "Provider key required. Configure in Settings > Market Data Providers.",
                unit = "$/oz"
            ),
            MarketAsset(
                id = "WTI",
                name = "WTI Crude Oil",
                symbol = "WTI/USD",
                category = AssetCategory.COMMODITIES,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "NYMEX Energy Stream",
                dataSourceInfo = "Energy Benchmark (WTI Light Sweet)",
                setupInstructions = "Provider key required. Configure in Settings > Market Data Providers.",
                unit = "$/bbl"
            ),
            MarketAsset(
                id = "BRENT",
                name = "Brent Crude Oil",
                symbol = "BRENT/USD",
                category = AssetCategory.COMMODITIES,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "ICE Energy Stream",
                dataSourceInfo = "International Crude Benchmark",
                setupInstructions = "Provider key required. Configure in Settings > Market Data Providers.",
                unit = "$/bbl"
            ),
            MarketAsset(
                id = "NATGAS",
                name = "Natural Gas",
                symbol = "NG/USD",
                category = AssetCategory.COMMODITIES,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Henry Hub Natural Gas",
                dataSourceInfo = "NYMEX Gas Benchmark",
                setupInstructions = "Provider key required. Configure in Settings > Market Data Providers.",
                unit = "$/MMBtu"
            ),

            // 3. Forex & Indices
            MarketAsset(
                id = "EURUSD",
                name = "Euro / US Dollar",
                symbol = "EUR/USD",
                category = AssetCategory.FOREX,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Interbank FX Spot",
                dataSourceInfo = "Forex Spot Stream",
                unit = ""
            ),
            MarketAsset(
                id = "SPX",
                name = "S&P 500 Index",
                symbol = "SPX500",
                category = AssetCategory.INDICES,
                dataStatus = MarketDataStatus.UNAVAILABLE,
                dataSourceName = "Global Equity Indices",
                dataSourceInfo = "Index Benchmark",
                unit = "pts"
            )
        )

        fun initialSignals(): List<Signal> = listOf(
            Signal(
                id = "sig_1",
                assetSymbol = "BTC/USDT",
                assetName = "Bitcoin",
                type = SignalType.STRONG_BUY,
                entryPrice = "$67,200 - $67,500",
                targetPrice = "$71,800 (+6.8%)",
                stopLoss = "$65,900 (-2.1%)",
                riskReward = "3.2 : 1",
                confidenceScore = 92,
                timeframe = "4H",
                timestamp = System.currentTimeMillis() - 1800000,
                status = SignalStatus.ACTIVE,
                thesis = "Bullish divergence formed on 4H RSI with massive spot delta absorption. Ascending wedge breakout confirmed above 200 EMA.",
                indicators = listOf("RSI Bullish Div (38.2)", "MACD Golden Cross", "Volume +180% vs 20-MA")
            ),
            Signal(
                id = "sig_2",
                assetSymbol = "XAU/USD",
                assetName = "Gold Spot",
                type = SignalType.BUY,
                entryPrice = "$2,725 - $2,735",
                targetPrice = "$2,780 (+1.8%)",
                stopLoss = "$2,705 (-0.9%)",
                riskReward = "2.0 : 1",
                confidenceScore = 88,
                timeframe = "1D",
                timestamp = System.currentTimeMillis() - 7200000,
                status = SignalStatus.ACTIVE,
                thesis = "Safe haven flows surge amid real yield softening and central bank accumulation. Retest of previous resistance turning to support.",
                indicators = listOf("SuperTrend Bullish", "Stochastic 22 (Oversold Bounce)", "Volume Accumulation")
            ),
            Signal(
                id = "sig_3",
                assetSymbol = "WTI/USD",
                assetName = "Crude Oil",
                type = SignalType.SELL,
                entryPrice = "$72.50 - $73.00",
                targetPrice = "$68.20 (-5.8%)",
                stopLoss = "$74.30 (+1.9%)",
                riskReward = "2.9 : 1",
                confidenceScore = 84,
                timeframe = "4H",
                timestamp = System.currentTimeMillis() - 14400000,
                status = SignalStatus.ACTIVE,
                thesis = "Inventory build exceeding EIA forecasts by 3.8M barrels. Rejection off descending trendline with bearish engulfing pattern.",
                indicators = listOf("Bearish Engulfing 4H", "RSI 64 Rejection", "MACD Bearish Divergence")
            ),
            Signal(
                id = "sig_4",
                assetSymbol = "SOL/USDT",
                assetName = "Solana",
                type = SignalType.STRONG_BUY,
                entryPrice = "$175.00 - $178.00",
                targetPrice = "$198.50 (+12.2%)",
                stopLoss = "$168.00 (-4.8%)",
                riskReward = "2.8 : 1",
                confidenceScore = 91,
                timeframe = "1D",
                timestamp = System.currentTimeMillis() - 10800000,
                status = SignalStatus.ACTIVE,
                thesis = "Breakout retest of multi-week consolidation channel. DEX volume expanding with network fees at 6-month high.",
                indicators = listOf("DEX Volume +240%", "RSI 58 Momentum", "Bollinger Band Expansion")
            ),
            Signal(
                id = "sig_5",
                assetSymbol = "XAG/USD",
                assetName = "Silver Spot",
                type = SignalType.STRONG_BUY,
                entryPrice = "$32.20 - $32.40",
                targetPrice = "$34.50 (+6.6%)",
                stopLoss = "$31.40 (-2.6%)",
                riskReward = "2.5 : 1",
                confidenceScore = 90,
                timeframe = "1D",
                timestamp = System.currentTimeMillis() - 21600000,
                status = SignalStatus.ACTIVE,
                thesis = "Gold/Silver ratio compressing from 85 to 81. Industrial solar demand forecast upward revision with low COMEX warehouse stocks.",
                indicators = listOf("G/S Ratio Breakdown", "Volume Breakout", "RSI 56 Momentum Expansion")
            )
        )

        fun initialAlerts(): List<MarketAlert> = listOf(
            MarketAlert(
                id = "alert_1",
                assetSymbol = "BTC/USDT",
                assetName = "Bitcoin",
                targetPrice = 70000.0,
                direction = AlertDirection.ABOVE,
                isActive = true,
                isTriggered = false,
                note = "Breakout alert above key psychological resistance"
            ),
            MarketAlert(
                id = "alert_2",
                assetSymbol = "BTC/USDT",
                assetName = "Bitcoin",
                targetPrice = 65000.0,
                direction = AlertDirection.BELOW,
                isActive = true,
                isTriggered = false,
                note = "Dip buy zone / stop defense limit"
            ),
            MarketAlert(
                id = "alert_3",
                assetSymbol = "XAU/USD",
                assetName = "Gold Spot",
                targetPrice = 2750.0,
                direction = AlertDirection.ABOVE,
                isActive = true,
                isTriggered = false,
                note = "All-time high continuation retest trigger"
            ),
            MarketAlert(
                id = "alert_4",
                assetSymbol = "WTI/USD",
                assetName = "Crude Oil",
                targetPrice = 70.0,
                direction = AlertDirection.BELOW,
                isActive = false,
                isTriggered = false,
                note = "Support failure confirmation"
            )
        )

        fun initialNews(): List<MarketNews> = listOf(
            MarketNews(
                id = "news_1",
                title = "Global Central Banks Boost Gold Reserves as Currency Diversification Accelerates",
                source = "Bloomberg Terminal",
                timestamp = "18m ago",
                category = "Commodities",
                summary = "Official sector bullion purchases reached 290 metric tons in the previous quarter, supporting precious metals macro strength.",
                impact = NewsImpact.BULLISH,
                isSaved = true,
                fullContent = "Global central banks continued their unprecedented bullion accumulation spree through the current fiscal quarter, adding over 290 metric tons of physical gold to sovereign reserves. Emerging market central banks accounted for over 65% of recorded volumes. Analysts from Goldman Sachs and UBS noted that ongoing geopolitical de-dollarization and reserve safety mandates are providing an asymmetric floor under gold spot prices, even in high real-yield environments."
            ),
            MarketNews(
                id = "news_2",
                title = "Bitcoin Spot ETFs See $420M Inflows in Single Session as Institutional Custody Expands",
                source = "Nexus Institutional Wire",
                timestamp = "42m ago",
                category = "Crypto",
                summary = "Institutional asset managers recorded net positive inflows led by IBIT and FBTC as market liquidity thickens.",
                impact = NewsImpact.BULLISH,
                isSaved = false,
                fullContent = "US-listed spot Bitcoin exchange-traded funds registered a combined net inflow of $420.5 million on Thursday, marking the third consecutive day of institutional accumulation. On-chain metrics tracked by Glassnode show liquid exchange inventory declining to multi-year lows of 2.18 million BTC. Analysts suggest institutional OTC desks are seeing increased order sizes ahead of the upcoming monthly options expiry."
            ),
            MarketNews(
                id = "news_3",
                title = "OPEC+ Weighs Production Quota Delays as Global Crude Demand Projections Temper",
                source = "Reuters Energy",
                timestamp = "2h ago",
                category = "Commodities",
                summary = "Delegates indicate the cartel may postpone its planned 180,000 bpd monthly output revival to stabilize crude benchmark prices.",
                impact = NewsImpact.BEARISH,
                isSaved = false,
                fullContent = "OPEC and its non-OPEC allies are evaluating an extension of voluntary supply reductions through the coming quarter. Softening manufacturing indices in key industrial economies have moderated short-term refinery intake estimates. Brent and West Texas Intermediate futures traded mixed following the report, with options skew tilting towards downside put protection."
            ),
            MarketNews(
                id = "news_4",
                title = "Federal Reserve Balance Sheet Runoff Signals Liquidity Tightening in Short-Term Repo",
                source = "Financial Times Macro",
                timestamp = "4h ago",
                category = "Macro",
                summary = "Overnight Reverse Repo facility balances dip below $200 billion as Treasury bill issuance absorbs cash reserves.",
                impact = NewsImpact.NEUTRAL,
                isSaved = false,
                fullContent = "The Federal Reserve's ongoing quantitative tightening has reduced system liquidity reserves to levels closely watched by money market traders. Overnight reverse repo balances touched their lowest mark since 2021. Market strategists expect short-term yield volatility to persist until the FOMC updates guidance on balance sheet tapering thresholds."
            )
        )

        fun initialScannerItems(): List<ScannerItem> = listOf(
            ScannerItem(
                id = "scan_btc_1h",
                symbol = "BTC/USDT",
                name = "Bitcoin",
                category = AssetCategory.CRYPTO,
                rsi14 = 34.2,
                macdStatus = "Bullish Cross 4H",
                emaCondition = "Above 200 EMA",
                volumeMultiplier = 2.4,
                aiScore = 94,
                matchedCriteria = listOf("RSI Oversold (<30)", "MACD Crossovers", "High Volume (RVOL > 1.5x)", "BOS & CHoCH"),
                verifiedPrice = 67450.0,
                change24hPercent = 2.84,
                detectedCondition = "BOS & CHoCH Breakout",
                timeframe = "1H",
                lastUpdated = "Live",
                assetId = "BTC"
            ),
            ScannerItem(
                id = "scan_xau_1h",
                symbol = "XAU/USD",
                name = "Gold Spot",
                category = AssetCategory.COMMODITIES,
                rsi14 = 58.6,
                macdStatus = "Expanding Histogram",
                emaCondition = "Above 50 & 200 EMA",
                volumeMultiplier = 1.8,
                aiScore = 89,
                matchedCriteria = listOf("Strong Bullish Trend", "EMA Crossovers", "High Volume (RVOL > 1.5x)"),
                verifiedPrice = 2742.50,
                change24hPercent = 1.15,
                detectedCondition = "Strong Bullish Trend",
                timeframe = "1H",
                lastUpdated = "Live",
                assetId = "GOLD"
            ),
            ScannerItem(
                id = "scan_sol_1h",
                symbol = "SOL/USDT",
                name = "Solana",
                category = AssetCategory.CRYPTO,
                rsi14 = 28.5,
                macdStatus = "Histogram Turning Green",
                emaCondition = "Testing 100 EMA",
                volumeMultiplier = 3.1,
                aiScore = 91,
                matchedCriteria = listOf("RSI Oversold (<30)", "High Volume (RVOL > 1.5x)", "Near Support (≤0.8%)", "Active FVG Zones"),
                verifiedPrice = 178.20,
                change24hPercent = -2.14,
                detectedCondition = "RSI Oversold (<30)",
                timeframe = "1H",
                lastUpdated = "Live",
                assetId = "SOL"
            ),
            ScannerItem(
                id = "scan_wti_1h",
                symbol = "WTI/USD",
                name = "Crude Oil",
                category = AssetCategory.COMMODITIES,
                rsi14 = 66.8,
                macdStatus = "Bearish Divergence",
                emaCondition = "Below 50 EMA",
                volumeMultiplier = 1.2,
                aiScore = 48,
                matchedCriteria = listOf("Near Resistance (≤0.8%)", "Liquidity Sweeps"),
                verifiedPrice = 71.85,
                change24hPercent = -0.72,
                detectedCondition = "Near Resistance (≤0.8%)",
                timeframe = "1H",
                lastUpdated = "Live",
                assetId = "OIL_WTI"
            ),
            ScannerItem(
                id = "scan_eth_1h",
                symbol = "ETH/USDT",
                name = "Ethereum",
                category = AssetCategory.CRYPTO,
                rsi14 = 44.1,
                macdStatus = "Consolidating",
                emaCondition = "At 200 EMA",
                volumeMultiplier = 1.6,
                aiScore = 76,
                matchedCriteria = listOf("Active FVG Zones", "High Volume (RVOL > 1.5x)", "Near Support (≤0.8%)"),
                verifiedPrice = 2580.0,
                change24hPercent = 1.45,
                detectedCondition = "Active FVG Zones",
                timeframe = "1H",
                lastUpdated = "Live",
                assetId = "ETH"
            )
        )

        fun initialApiConfigs(): List<ApiFeedConfig> = listOf(
            ApiFeedConfig(
                id = "binance_ws",
                providerName = "Binance WebSocket Live Stream",
                assetCovered = "BTC, ETH, BNB, SOL, XRP, DOGE, ADA, LINK, AVAX, SUI (15+ pairs)",
                apiKey = "Public (No Key Required)",
                isConnected = true,
                endpointUrl = "wss://stream.binance.com:9443/ws/!miniTicker@arr",
                lastPing = "Active",
                requiresApiKey = false,
                setupDocumentationUrl = "https://binance-docs.github.io/apidocs/spot/en/#all-market-mini-tickers-stream",
                instructions = "Public WebSocket stream is active automatically with zero configuration."
            ),
            ApiFeedConfig(
                id = "binance_public",
                providerName = "Binance Public REST API",
                assetCovered = "24hr Tickers & Historical Candles",
                apiKey = "Public (No Key Required)",
                isConnected = true,
                endpointUrl = "https://api.binance.com/api/v3/ticker/24hr",
                lastPing = "Active",
                requiresApiKey = false,
                setupDocumentationUrl = "https://binance-docs.github.io/apidocs/spot/en/",
                instructions = "Public REST backup endpoints for polling and candles."
            ),
            ApiFeedConfig(
                id = "coingecko_public",
                providerName = "CoinGecko Free API",
                assetCovered = "Global Market Cap, 24h Vol & Dominance",
                apiKey = "Public (No Key Required)",
                isConnected = true,
                endpointUrl = "https://api.coingecko.com/api/v3/global",
                lastPing = "Active",
                requiresApiKey = false,
                setupDocumentationUrl = "https://www.coingecko.com/en/api",
                instructions = "Provides macro market data subject to standard public tier rate limits."
            ),
            ApiFeedConfig(
                id = "commodities_feed",
                providerName = "TwelveData Commodities API",
                assetCovered = "Gold (XAU), Silver (XAG), WTI Crude, Brent, Natural Gas",
                apiKey = "",
                isConnected = false,
                endpointUrl = "https://api.twelvedata.com/price",
                lastPing = "Unconnected",
                requiresApiKey = true,
                setupDocumentationUrl = "https://twelvedata.com/pricing",
                instructions = "To connect live commodity spot prices, register for a free API key at twelvedata.com, copy your API key, and paste it here."
            )
        )
    }

    suspend fun getCandles(
        symbol: String,
        timeframe: CandleTimeframe,
        limit: Int = 250
    ): List<Candle> {
        val effectiveLimit = if (_isLowPerformanceMode.value) minOf(limit, 45) else limit
        val cacheKey = "${symbol}_${timeframe.name}_$effectiveLimit"
        val now = System.currentTimeMillis()

        if (_isDataSavingMode.value) {
            val cached = candleCache[cacheKey]
            if (cached != null && (now - cached.first) < 60_000L) {
                return cached.second
            }
        }

        val candles = candleService.fetchCandles(symbol, timeframe, effectiveLimit)
        candleCache[cacheKey] = Pair(now, candles)
        return candles
    }

    suspend fun calculateTechnicalAnalysis(
        symbol: String,
        assetName: String,
        timeframe: CandleTimeframe
    ): OverallAnalysisResult = withContext(Dispatchers.Default) {
        val candles = candleService.fetchCandles(symbol, timeframe)
        TechnicalIndicatorsEngine.analyze(symbol, assetName, timeframe, candles)
    }

    suspend fun calculateSmcAnalysis(
        symbol: String,
        assetName: String,
        timeframe: CandleTimeframe
    ): SmcAnalysisResult = withContext(Dispatchers.Default) {
        val candles = candleService.fetchCandles(symbol, timeframe, 160)

        // Multi-timeframe snapshots for key timeframes: 1m, 5m, 15m, 30m, 1h, 4h, 1d, 1w
        val mtfTimeframes = listOf(
            CandleTimeframe.W1,
            CandleTimeframe.D1,
            CandleTimeframe.H4,
            CandleTimeframe.H1,
            CandleTimeframe.M30,
            CandleTimeframe.M15,
            CandleTimeframe.M5,
            CandleTimeframe.M1
        )
        val snapshots = mutableListOf<TimeframeSmcSnapshot>()
        for (tf in mtfTimeframes) {
            val tfCandles = if (tf == timeframe) candles else candleService.fetchCandles(symbol, tf, 50)
            if (tfCandles.size >= 15) {
                val tempResult = SmcCalculationEngine.analyze(symbol, assetName, tf, tfCandles)
                val isBullish = tempResult.marketStructure.trendState == MarketTrendState.BULLISH_UPTREND ||
                        tempResult.marketStructure.isBullishBOSPresent ||
                        tempResult.marketStructure.isBullishCHoCHPresent
                snapshots.add(
                    TimeframeSmcSnapshot(
                        timeframe = tf,
                        trend = tempResult.marketStructure.trendState,
                        lastBreakType = tempResult.marketStructure.latestBreak?.type,
                        unmitigatedOBCount = tempResult.orderBlocks.count { it.status == OrderBlockStatus.UNMITIGATED },
                        unfilledFvgCount = tempResult.fairValueGaps.count { it.status == FvgStatus.UNFILLED },
                        nearestSupport = tempResult.priceZones.firstOrNull { it.type == PriceZoneType.SUPPORT || it.type == PriceZoneType.DEMAND }?.centralPrice,
                        nearestResistance = tempResult.priceZones.firstOrNull { it.type == PriceZoneType.RESISTANCE || it.type == PriceZoneType.SUPPLY }?.centralPrice,
                        isBullishBias = isBullish
                    )
                )
            }
        }

        SmcCalculationEngine.analyze(symbol, assetName, timeframe, candles, snapshots)
    }

    suspend fun calculateAiSignal(
        symbol: String,
        assetName: String,
        timeframe: CandleTimeframe = CandleTimeframe.H1
    ): AiTradingSignal = withContext(Dispatchers.Default) {
        val asset = _assets.value.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) || it.id.equals(symbol, ignoreCase = true) }
        val currentPrice = asset?.currentPrice ?: 0.0
        val dataStatus = asset?.dataStatus ?: MarketDataStatus.UNAVAILABLE
        val dataSourceName = asset?.dataSourceName ?: "Binance Public REST"
        val lastUpdated = asset?.lastUpdated

        val candles = candleService.fetchCandles(symbol, timeframe, 160)
        val ta = if (candles.size >= 15) TechnicalIndicatorsEngine.analyze(symbol, assetName, timeframe, candles) else null
        val smc = if (candles.size >= 15) calculateSmcAnalysis(symbol, assetName, timeframe) else null

        AiTradingSignalEngine.generateSignal(
            symbol = symbol,
            assetName = assetName,
            currentPrice = currentPrice,
            dataStatus = dataStatus,
            dataSourceName = dataSourceName,
            lastUpdatedTime = lastUpdated,
            primaryTimeframe = timeframe,
            candles = candles,
            technicalAnalysis = ta,
            smcAnalysis = smc,
            mtfSnapshots = smc?.multiTimeframe?.snapshots ?: emptyList()
        )
    }

    suspend fun refreshAiSignals(): List<AiTradingSignal> = withContext(Dispatchers.IO) {
        val symbolsToScan = listOf(
            Pair("BTC", "Bitcoin"),
            Pair("ETH", "Ethereum"),
            Pair("SOL", "Solana"),
            Pair("BNB", "BNB"),
            Pair("XRP", "XRP"),
            Pair("DOGE", "Dogecoin"),
            Pair("ADA", "Cardano"),
            Pair("LINK", "Chainlink")
        )

        val results = mutableListOf<AiTradingSignal>()
        for ((sym, name) in symbolsToScan) {
            try {
                val sig = calculateAiSignal(sym, name, CandleTimeframe.H1)
                results.add(sig)
            } catch (e: Exception) {
                Log.w("TradingRepo", "Failed to generate AI signal for $sym: ${e.message}")
            }
        }

        if (results.isNotEmpty()) {
            _aiSignals.value = results
            // Sync to legacy _signals so Dashboard and other screens display real live signals
            _signals.value = results.map { it.toLegacySignal() }
        }

        results
    }
}

