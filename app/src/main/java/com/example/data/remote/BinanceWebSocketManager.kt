package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class BinanceWebSocketManager(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _tickerUpdates = MutableSharedFlow<List<LiveTickerUpdate>>(extraBufferCapacity = 64)
    val tickerUpdates: SharedFlow<List<LiveTickerUpdate>> = _tickerUpdates.asSharedFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _lastMessageTime = MutableStateFlow<Long?>(null)
    val lastMessageTime: StateFlow<Long?> = _lastMessageTime.asStateFlow()

    private var webSocket: WebSocket? = null
    private val isRunning = AtomicBoolean(false)
    private var reconnectAttempts = 0

    // Symbols we are monitoring
    private val monitoredSymbols = setOf(
        "BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT",
        "DOGEUSDT", "ADAUSDT", "LINKUSDT", "AVAXUSDT", "TRXUSDT",
        "TONUSDT", "LTCUSDT", "DOTUSDT", "BCHUSDT", "SUIUSDT",
        "PAXGUSDT" // Real physical Gold backed token on Binance
    )

    fun start() {
        if (isRunning.getAndSet(true)) return
        connect()
    }

    fun stop() {
        isRunning.set(false)
        webSocket?.close(1000, "Client stopped")
        webSocket = null
        _isConnected.value = false
    }

    private val wsEndpoints = listOf(
        "wss://stream.binance.com:9443/ws/!miniTicker@arr",
        "wss://stream.binance.us:9443/ws/!miniTicker@arr"
    )
    private var currentEndpointIndex = 0

    private fun connect() {
        if (!isRunning.get()) return

        val endpoint = wsEndpoints[currentEndpointIndex]
        val request = Request.Builder()
            .url(endpoint)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
                reconnectAttempts = 0
                Log.d("BinanceWS", "WebSocket connected successfully to $endpoint")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _lastMessageTime.value = System.currentTimeMillis()
                parseMiniTickerArray(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                Log.w("BinanceWS", "WebSocket failure on $endpoint: ${t.message} (HTTP ${response?.code})")
                // Rotate to alternate endpoint on error (e.g. HTTP 451 geo-block)
                currentEndpointIndex = (currentEndpointIndex + 1) % wsEndpoints.size
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!isRunning.get()) return
        scope.launch {
            val backoffMs = (1000L * (1 shl reconnectAttempts.coerceAtMost(5))).coerceAtMost(30000L)
            reconnectAttempts++
            delay(backoffMs)
            connect()
        }
    }

    private fun parseMiniTickerArray(jsonStr: String) {
        try {
            val array = JSONArray(jsonStr)
            val updates = mutableListOf<LiveTickerUpdate>()
            val now = System.currentTimeMillis()

            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val symbol = item.optString("s")
                if (symbol in monitoredSymbols) {
                    val closePrice = item.optString("c").toDoubleOrNull() ?: continue
                    val openPrice = item.optString("o").toDoubleOrNull() ?: closePrice
                    val highPrice = item.optString("h").toDoubleOrNull() ?: closePrice
                    val lowPrice = item.optString("l").toDoubleOrNull() ?: closePrice
                    val volume = item.optString("v").toDoubleOrNull() ?: 0.0
                    val quoteVol = item.optString("q").toDoubleOrNull() ?: 0.0

                    val changePct = if (openPrice > 0.0) {
                        ((closePrice - openPrice) / openPrice) * 100.0
                    } else 0.0

                    updates.add(
                        LiveTickerUpdate(
                            symbol = symbol,
                            lastPrice = closePrice,
                            priceChangePercent = changePct,
                            highPrice = highPrice,
                            lowPrice = lowPrice,
                            volume = volume,
                            quoteVolume = quoteVol,
                            timestamp = now
                        )
                    )
                }
            }

            if (updates.isNotEmpty()) {
                _tickerUpdates.tryEmit(updates)
            }
        } catch (e: Exception) {
            Log.e("BinanceWS", "Error parsing ticker update: ${e.message}")
        }
    }
}
