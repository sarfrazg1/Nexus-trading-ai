package com.example.data.ai

import com.example.data.learning.LearningLanguageMode
import java.util.UUID

/**
 * Message Model for NEXUS AI Chat.
 */
data class NexusChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isOfflineEngine: Boolean = false,
    val attachedContextSummary: String? = null,
    val suggestedFollowUps: List<String> = emptyList()
)

enum class MessageSender {
    USER,
    ASSISTANT
}

/**
 * Conversation Thread Model for NEXUS AI.
 */
data class NexusConversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val languageMode: LearningLanguageMode = LearningLanguageMode.BILINGUAL
)

/**
 * Market Context snapshot attached to user queries or generated on demand.
 */
data class MarketContextSnapshot(
    val symbol: String,
    val assetName: String,
    val currentPrice: Double,
    val change24hPercent: Double,
    val timeframe: String,
    val rsi14: Double?,
    val macdHistogram: Double?,
    val emaCondition: String?,
    val atrVolatility: Double?,
    val bosChochCondition: String?,
    val fvgStatus: String?,
    val supportLevel: Double?,
    val resistanceLevel: Double?,
    val fibGoldenPocket: String?,
    val signalDecision: String?,
    val confidenceScore: Int?,
    val relevantNewsSummary: String?,
    val macroEventSummary: String?,
    val dataSource: String,
    val lastUpdatedFormatted: String,
    val isRealData: Boolean = true,
    val futuresSignalSummary: String? = null
) {
    fun toPromptContext(lang: LearningLanguageMode): String {
        val sb = StringBuilder()
        sb.appendLine("=== VERIFIED MARKET CONTEXT (DO NOT FABRICATE DATA) ===")
        sb.appendLine("Asset: $assetName ($symbol)")
        sb.appendLine("Price: $$currentPrice (24h: $change24hPercent%)")
        sb.appendLine("Timeframe: $timeframe")
        rsi14?.let { sb.appendLine("RSI (14): $it ${if (it > 70) "[OVERBOUGHT]" else if (it < 30) "[OVERSOLD]" else "[NEUTRAL]"}") }
        macdHistogram?.let { sb.appendLine("MACD Histogram: $it") }
        emaCondition?.let { sb.appendLine("EMA Structure: $it") }
        atrVolatility?.let { sb.appendLine("ATR Volatility: $it") }
        bosChochCondition?.let { sb.appendLine("SMC Structure: $it") }
        fvgStatus?.let { sb.appendLine("Fair Value Gaps: $it") }
        supportLevel?.let { sb.appendLine("Nearest Support: $$it") }
        resistanceLevel?.let { sb.appendLine("Nearest Resistance: $$it") }
        fibGoldenPocket?.let { sb.appendLine("Fibonacci 0.5-0.618 Zone: $it") }
        signalDecision?.let { sb.appendLine("NEXUS Signal Engine Decision: $it (Confidence: $confidenceScore/100)") }
        relevantNewsSummary?.let { sb.appendLine("Recent News Factor: $it") }
        macroEventSummary?.let { sb.appendLine("Macro Environment: $it") }
        futuresSignalSummary?.let {
            sb.appendLine("=== BINANCE USDT-M FUTURES SCANNER CONFLUENCE ===")
            sb.appendLine(it)
            sb.appendLine("==================================================")
        }
        sb.appendLine("Data Source: $dataSource | Freshness: $lastUpdatedFormatted")
        sb.appendLine("======================================================")
        return sb.toString()
    }
}
