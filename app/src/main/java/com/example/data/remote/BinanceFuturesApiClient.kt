package com.example.data.remote

import android.util.Log
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Public REST & WebSocket Client for Binance USDT-M Futures.
 * Requires ZERO API keys, tokens, or permissions. 100% read-only public market data.
 */
class BinanceFuturesApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()
) {
    // In-memory candle cache: "SYMBOL_TIMEFRAME" -> CachedCandles
    private val candleCache = ConcurrentHashMap<String, CachedCandles>()

    private data class CachedCandles(
        val timestamp: Long,
        val candles: List<Candle>
    )

    // Live price updates flow
    private val _livePrices = MutableSharedFlow<Map<String, Double>>(extraBufferCapacity = 64)
    val livePrices: SharedFlow<Map<String, Double>> = _livePrices.asSharedFlow()

    private val _isWebSocketConnected = MutableStateFlow(false)
    val isWebSocketConnected: StateFlow<Boolean> = _isWebSocketConnected.asStateFlow()

    private var webSocket: WebSocket? = null
    private val isWsActive = AtomicBoolean(false)
    private var reconnectAttempt = 0

    companion object {
        private const val TAG = "BinanceFuturesApi"
        private const val FUTURES_BASE_URL = "https://fapi.binance.com"
        private const val SPOT_FALLBACK_URL = "https://api.binance.com"
        private const val FUTURES_WS_URL = "wss://fstream.binance.com/ws/!miniTicker@arr"
        private const val SPOT_WS_FALLBACK = "wss://stream.binance.com:9443/ws/!miniTicker@arr"
    }

    /**
     * Fetches historical OHLCV candles for a given Binance USDT-M Futures pair.
     * Maps intervals directly to Binance standard: 4h, 1h, 15m, 5m.
     */
    suspend fun fetchFuturesCandles(
        symbol: String,
        timeframe: CandleTimeframe,
        limit: Int = 100
    ): List<Candle> = withContext(Dispatchers.IO) {
        val normalizedSymbol = symbol.uppercase().trim()
        val cacheKey = "${normalizedSymbol}_${timeframe.label}"

        // Use cache if under 15 seconds fresh
        val cached = candleCache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 15_000) {
            return@withContext cached.candles
        }

        val endpoints = listOf(
            "$FUTURES_BASE_URL/fapi/v1/klines",
            "$SPOT_FALLBACK_URL/api/v3/klines"
        )

        for (endpoint in endpoints) {
            try {
                val intervalStr = when (timeframe) {
                    CandleTimeframe.M5 -> "5m"
                    CandleTimeframe.M15 -> "15m"
                    CandleTimeframe.H1 -> "1h"
                    CandleTimeframe.H4 -> "4h"
                    CandleTimeframe.D1 -> "1d"
                    else -> timeframe.binanceInterval
                }

                val url = "$endpoint?symbol=$normalizedSymbol&interval=$intervalStr&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Android; NexusTradingAI)")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.code == 429) {
                        Log.w(TAG, "Rate limited on $endpoint, backing off...")
                        delay(2000L)
                        return@use
                    }

                    if (!response.isSuccessful) {
                        Log.w(TAG, "Failed $endpoint with HTTP ${response.code} for $normalizedSymbol")
                        return@use
                    }

                    val body = response.body?.string() ?: return@use
                    val array = JSONArray(body)
                    val candles = ArrayList<Candle>(array.length())

                    for (i in 0 until array.length()) {
                        val item = array.getJSONArray(i)
                        val openTime = item.getLong(0)
                        val open = item.getString(1).toDoubleOrNull() ?: 0.0
                        val high = item.getString(2).toDoubleOrNull() ?: 0.0
                        val low = item.getString(3).toDoubleOrNull() ?: 0.0
                        val close = item.getString(4).toDoubleOrNull() ?: 0.0
                        val volume = item.getString(5).toDoubleOrNull() ?: 0.0
                        val closeTime = item.getLong(6)
                        val quoteVolume = item.getString(7).toDoubleOrNull() ?: 0.0
                        val tradesCount = item.getInt(8)
                        val takerBuyBase = item.getString(9).toDoubleOrNull() ?: 0.0
                        val takerBuyQuote = item.getString(10).toDoubleOrNull() ?: 0.0

                        candles.add(
                            Candle(
                                openTime = openTime,
                                open = open,
                                high = high,
                                low = low,
                                close = close,
                                volume = volume,
                                closeTime = closeTime,
                                quoteVolume = quoteVolume,
                                tradesCount = tradesCount,
                                takerBuyBaseVolume = takerBuyBase,
                                takerBuyQuoteVolume = takerBuyQuote
                            )
                        )
                    }

                    if (candles.isNotEmpty()) {
                        candleCache[cacheKey] = CachedCandles(System.currentTimeMillis(), candles)
                        return@withContext candles
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching from $endpoint for $normalizedSymbol: ${e.message}")
            }
        }

        // Return cached or empty list if network failed
        cached?.candles ?: emptyList()
    }

    /**
     * Fetches Funding Rate and Mark Price for USDT-M Futures.
     */
    suspend fun fetchFundingRate(symbol: String): Double? = withContext(Dispatchers.IO) {
        val normalizedSymbol = symbol.uppercase().trim()
        val url = "$FUTURES_BASE_URL/fapi/v1/premiumIndex?symbol=$normalizedSymbol"

        try {
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val json = JSONObject(body)
                    val rate = json.optString("lastFundingRate").toDoubleOrNull()
                    return@withContext rate?.let { it * 100.0 } // As percentage
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Funding rate unavailable for $normalizedSymbol: ${e.message}")
        }
        null
    }

    /**
     * Connects to Binance Public Futures WebSocket stream for live ticker prices.
     */
    fun startWebSocket(monitoredSymbols: Set<String>) {
        if (isWsActive.getAndSet(true)) return
        connectWebSocket(monitoredSymbols)
    }

    fun stopWebSocket() {
        isWsActive.set(false)
        webSocket?.close(1000, "Scanner stopped")
        webSocket = null
        _isWebSocketConnected.value = false
    }

    private fun connectWebSocket(symbols: Set<String>) {
        if (!isWsActive.get()) return

        val endpoint = if (reconnectAttempt % 2 == 0) FUTURES_WS_URL else SPOT_WS_FALLBACK
        val request = Request.Builder().url(endpoint).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isWebSocketConnected.value = true
                reconnectAttempt = 0
                Log.d(TAG, "Futures WebSocket connected to $endpoint")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val array = JSONArray(text)
                    val priceMap = mutableMapOf<String, Double>()

                    for (i in 0 until array.length()) {
                        val item = array.optJSONObject(i) ?: continue
                        val sym = item.optString("s")
                        if (symbols.isEmpty() || sym in symbols) {
                            val price = item.optString("c").toDoubleOrNull() ?: continue
                            priceMap[sym] = price
                        }
                    }

                    if (priceMap.isNotEmpty()) {
                        _livePrices.tryEmit(priceMap)
                    }
                } catch (e: Exception) {
                    // Ignore parse jitter
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _isWebSocketConnected.value = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isWebSocketConnected.value = false
                scheduleReconnect(symbols)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isWebSocketConnected.value = false
                Log.w(TAG, "WebSocket error: ${t.message}. Reconnecting...")
                scheduleReconnect(symbols)
            }
        })
    }

    private fun scheduleReconnect(symbols: Set<String>) {
        if (!isWsActive.get()) return
        reconnectAttempt++
        val backoff = (1000L * (1 shl reconnectAttempt.coerceAtMost(5))).coerceAtMost(30000L)
        CoroutineScope(Dispatchers.IO).launch {
            delay(backoff)
            connectWebSocket(symbols)
        }
    }
}
