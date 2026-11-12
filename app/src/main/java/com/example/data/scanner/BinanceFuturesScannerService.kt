package com.example.data.scanner

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CandleTimeframe
import com.example.data.remote.BinanceFuturesApiClient
import com.example.util.NexusNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android Foreground Service for Continuous Binance USDT-M Futures Scanning.
 * Runs in the background while active with a persistent notification.
 * Connects to public Binance REST and WebSocket endpoints (Zero API keys required).
 */
class BinanceFuturesScannerService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var repository: FuturesScannerRepository
    private val apiClient = BinanceFuturesApiClient()
    private var scanLoopJob: Job? = null
    private var priceCollectorJob: Job? = null

    companion object {
        private const val TAG = "FuturesScannerService"
        private const val NOTIFICATION_ID = 8801
        private const val CHANNEL_ID = "nexus_scanner_service_channel"

        const val ACTION_START = "com.example.ACTION_START_SCANNER"
        const val ACTION_STOP = "com.example.ACTION_STOP_SCANNER"

        fun startService(context: Context) {
            val intent = Intent(context, BinanceFuturesScannerService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, BinanceFuturesScannerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val notifManager = NexusNotificationManager(applicationContext)
        repository = FuturesScannerRepository.getInstance(applicationContext, notifManager)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopScanner()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                startForegroundWithNotification("NEXUS Scanner Starting...", "Connecting to Binance Futures...")
                startScanner()
            }
            else -> {
                startForegroundWithNotification("NEXUS Scanner Active", "Monitoring Binance Futures...")
                startScanner()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopScanner()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startScanner() {
        if (scanLoopJob?.isActive == true) return

        repository.updateScannerStatus(ScannerStatus.SCANNING)

        // 1. Start WebSocket for real-time price monitoring
        val selectedSymbols = repository.getSelectedPairs().map { it.symbol }.toSet()
        apiClient.startWebSocket(selectedSymbols)

        // Listen for live prices
        priceCollectorJob?.cancel()
        priceCollectorJob = serviceScope.launch {
            apiClient.livePrices.collect { priceMap ->
                repository.onLivePricesUpdated(priceMap)
            }
        }

        // 2. Start Multi-Timeframe Scan Loop
        scanLoopJob = serviceScope.launch {
            while (isActive) {
                val pairsToScan = repository.getSelectedPairs()

                if (pairsToScan.isEmpty()) {
                    repository.updateScannerStatus(
                        status = ScannerStatus.PAUSED,
                        errorMessage = "No pairs selected in Watchlist. Select at least 1 coin to scan."
                    )
                    updateNotification("Scanner Paused", "No pairs selected in Watchlist.")
                    delay(5000L)
                    continue
                }

                repository.updateScannerStatus(ScannerStatus.SCANNING)
                updateNotification("NEXUS Futures Scanner", "Monitoring ${pairsToScan.size} pairs on 4H, 1H, 15M, 5M")

                for (pair in pairsToScan) {
                    if (!isActive) break

                    repository.updateScannerStatus(
                        status = ScannerStatus.SCANNING,
                        currentlyScanning = pair.symbol
                    )

                    try {
                        // Fetch 15M, 1H, 4H, and 5M candles for MTF confluence
                        val candles15m = apiClient.fetchFuturesCandles(pair.symbol, CandleTimeframe.M15, limit = 50)
                        val candles1h = apiClient.fetchFuturesCandles(pair.symbol, CandleTimeframe.H1, limit = 50)
                        val candles4h = apiClient.fetchFuturesCandles(pair.symbol, CandleTimeframe.H4, limit = 30)
                        val candles5m = apiClient.fetchFuturesCandles(pair.symbol, CandleTimeframe.M5, limit = 30)

                        val currentPrice = pair.currentPrice.takeIf { it > 0 }
                            ?: candles15m.lastOrNull()?.close
                            ?: 0.0

                        if (currentPrice > 0.0) {
                            val fundingRate = apiClient.fetchFundingRate(pair.symbol)

                            val result = BinanceFuturesScannerEngine.evaluatePair(
                                symbol = pair.symbol,
                                baseAsset = pair.baseAsset,
                                currentPrice = currentPrice,
                                candles4h = candles4h,
                                candles1h = candles1h,
                                candles15m = candles15m,
                                candles5m = candles5m,
                                fundingRate = fundingRate
                            )

                            result.signal?.let { signal ->
                                repository.onSignalEvaluated(signal)
                                if (signal.lifecycle == FuturesSignalLifecycle.ENTRY_CONFIRMED) {
                                    val emoji = if (signal.direction == FuturesDirection.LONG) "🟢 LONG" else "🔴 SHORT"
                                    updateNotification("Confirmed Entry: ${signal.symbol}", "$emoji at ${signal.entryZoneFormatted} | Quality: ${signal.qualityScore}%")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error scanning ${pair.symbol}: ${e.message}")
                    }

                    // Polite pacing between pairs to respect API weight limit
                    delay(400L)
                }

                repository.updateScannerStatus(
                    status = ScannerStatus.IDLE,
                    currentlyScanning = null
                )

                // Wait for configured scan interval (e.g. 20-30 seconds)
                val intervalMs = (repository.scannerState.value.scanIntervalSeconds * 1000L).coerceAtLeast(10000L)
                delay(intervalMs)
            }
        }
    }

    private fun stopScanner() {
        scanLoopJob?.cancel()
        scanLoopJob = null
        priceCollectorJob?.cancel()
        priceCollectorJob = null
        apiClient.stopWebSocket()
        repository.updateScannerStatus(ScannerStatus.IDLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Binance Futures Scanner"
            val desc = "Background multi-timeframe market scanner and entry alert notifications"
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW).apply {
                description = desc
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundWithNotification(title: String, text: String) {
        val notification = buildNotification(title, text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(title: String, text: String) {
        val notification = buildNotification(title, text)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(title: String, text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, BinanceFuturesScannerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop Scanner", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
