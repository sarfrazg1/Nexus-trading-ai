package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AlertType
import com.example.data.model.MarketNews
import com.example.data.model.NewsImpactLevel
import com.example.data.signal.AiTradingSignal
import com.example.data.signal.SignalDecision
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class InAppNotificationEvent(
    val title: String,
    val message: String,
    val type: AlertType,
    val timestamp: Long = System.currentTimeMillis()
)

class NexusNotificationManager(private val context: Context? = null) {

    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val recentlyTriggered = mutableMapOf<String, Long>()

    private val _inAppEvents = MutableSharedFlow<InAppNotificationEvent>(extraBufferCapacity = 10)
    val inAppEvents: SharedFlow<InAppNotificationEvent> = _inAppEvents.asSharedFlow()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context != null) {
            val name = "Nexus Trading Signals & Alerts"
            val descriptionText = "Real-time market price triggers, AI trading signals, and macro news catalysts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager? =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    // ==========================================
    // PREFERENCES FOR THE 6 ALERT TYPES
    // ==========================================
    fun isPriceAlertsEnabled(): Boolean = prefs?.getBoolean(KEY_PRICE_ALERTS, true) ?: true
    fun setPriceAlertsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_PRICE_ALERTS, enabled)?.apply()
    }

    fun isLongSignalsEnabled(): Boolean = prefs?.getBoolean(KEY_LONG_SIGNALS, true) ?: true
    fun setLongSignalsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_LONG_SIGNALS, enabled)?.apply()
    }

    fun isShortSignalsEnabled(): Boolean = prefs?.getBoolean(KEY_SHORT_SIGNALS, true) ?: true
    fun setShortSignalsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_SHORT_SIGNALS, enabled)?.apply()
    }

    fun isNewsAlertsEnabled(): Boolean = prefs?.getBoolean(KEY_NEWS_ALERTS, true) ?: true
    fun setNewsAlertsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_NEWS_ALERTS, enabled)?.apply()
    }

    fun isMacroEventsEnabled(): Boolean = prefs?.getBoolean(KEY_MACRO_EVENTS, true) ?: true
    fun setMacroEventsEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_MACRO_EVENTS, enabled)?.apply()
    }

    fun isWatchlistUpdatesEnabled(): Boolean = prefs?.getBoolean(KEY_WATCHLIST_UPDATES, true) ?: true
    fun setWatchlistUpdatesEnabled(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_WATCHLIST_UPDATES, enabled)?.apply()
    }

    // ==========================================
    // TRIGGER DISPATCHERS (DUPLICATE-PREVENTED)
    // ==========================================

    fun notifyPriceTarget(
        alertId: String,
        symbol: String,
        targetPrice: Double,
        currentPrice: Double,
        directionAbove: Boolean
    ): Boolean {
        if (!isPriceAlertsEnabled()) return false
        val key = "price_${alertId}_$symbol"
        if (isRecentlyTriggered(key)) return false

        val dirStr = if (directionAbove) "crossed above" else "crossed below"
        val title = "🎯 Price Alert: $symbol"
        val message = "$symbol has $dirStr target of $$targetPrice (Current: $$currentPrice)"

        markTriggered(key)
        postNotification(title, message, AlertType.PRICE_TARGET, 1001)
        return true
    }

    fun notifySignal(signal: AiTradingSignal): Boolean {
        val isLong = signal.decision == SignalDecision.LONG
        val isShort = signal.decision == SignalDecision.SHORT

        if (isLong && !isLongSignalsEnabled()) return false
        if (isShort && !isShortSignalsEnabled()) return false
        if (!isLong && !isShort) return false

        val key = "signal_${signal.id}_${signal.symbol}_${signal.decision.name}"
        if (isRecentlyTriggered(key)) return false

        val alertType = if (isLong) AlertType.SIGNAL_LONG else AlertType.SIGNAL_SHORT
        val emoji = if (isLong) "🟢" else "🔴"
        val title = "$emoji ${signal.decision.name} Signal: ${signal.symbol}"
        val entryStr = signal.riskPlan?.entryZoneFormatted ?: "Market Zone"
        val slStr = signal.riskPlan?.stopLossFormatted ?: "Structure Buffer"
        val tpStr = signal.riskPlan?.takeProfit1Formatted ?: "Target 1"
        val message = "Entry: $entryStr • SL: $slStr • TP1: $tpStr (Conf: ${signal.confidence.totalScore}%)"

        markTriggered(key)
        postNotification(title, message, alertType, 2001)
        return true
    }

    fun notifyNews(news: MarketNews): Boolean {
        val isHighImpact = news.impactLevel == NewsImpactLevel.HIGH
        if (isHighImpact && !isMacroEventsEnabled()) return false
        if (!isHighImpact && !isNewsAlertsEnabled()) return false

        val key = "news_${news.id}"
        if (isRecentlyTriggered(key)) return false

        val alertType = if (isHighImpact) AlertType.HIGH_IMPACT_EVENT else AlertType.IMPORTANT_NEWS
        val title = if (isHighImpact) "⚡ High-Impact Event: ${news.source}" else "📰 Market News: ${news.source}"
        val message = news.title

        markTriggered(key)
        postNotification(title, message, alertType, 3001)
        return true
    }

    fun notifyWatchlistUpdate(symbol: String, condition: String): Boolean {
        if (!isWatchlistUpdatesEnabled()) return false
        val key = "watchlist_${symbol}_$condition"
        if (isRecentlyTriggered(key)) return false

        val title = "⭐ Watchlist Alert: $symbol"
        val message = "$symbol condition triggered: $condition"

        markTriggered(key)
        postNotification(title, message, AlertType.WATCHLIST_UPDATE, 4001)
        return true
    }

    private fun postNotification(title: String, message: String, type: AlertType, notifId: Int) {
        // Emit in-app event for immediate UI banner/snackbars
        _inAppEvents.tryEmit(InAppNotificationEvent(title, message, type))

        if (context == null) return

        // Check POST_NOTIFICATIONS permission on Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                Log.d("NexusNotification", "POST_NOTIFICATIONS permission not granted; using in-app notification only.")
                return
            }
        }

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(notifId, builder.build())
        } catch (e: Exception) {
            Log.w("NexusNotification", "Failed to dispatch system notification: ${e.message}")
        }
    }

    private fun isRecentlyTriggered(key: String): Boolean {
        val lastTime = recentlyTriggered[key] ?: return false
        val cooldownMs = 15 * 60 * 1000L // 15 minutes cooldown
        return (System.currentTimeMillis() - lastTime) < cooldownMs
    }

    private fun markTriggered(key: String) {
        recentlyTriggered[key] = System.currentTimeMillis()
    }

    companion object {
        const val CHANNEL_ID = "nexus_trading_alerts"
        private const val PREFS_NAME = "nexus_notification_prefs"
        private const val KEY_PRICE_ALERTS = "notif_price_alerts"
        private const val KEY_LONG_SIGNALS = "notif_long_signals"
        private const val KEY_SHORT_SIGNALS = "notif_short_signals"
        private const val KEY_NEWS_ALERTS = "notif_news_alerts"
        private const val KEY_MACRO_EVENTS = "notif_macro_events"
        private const val KEY_WATCHLIST_UPDATES = "notif_watchlist_updates"
    }
}
