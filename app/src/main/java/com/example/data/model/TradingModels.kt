package com.example.data.model

enum class MarketDataStatus(val label: String) {
    LIVE("LIVE"),
    DELAYED("DELAYED"),
    STALE("STALE"),
    UNAVAILABLE("UNAVAILABLE")
}

enum class ConnectionStatus {
    CONNECTED,
    NOT_CONNECTED,
    CONNECTING,
    ERROR
}

enum class AssetCategory(val label: String) {
    ALL("All Assets"),
    CRYPTO("Crypto"),
    COMMODITIES("Commodities"),
    FOREX("Forex"),
    INDICES("Indices")
}

data class MarketAsset(
    val id: String,
    val name: String,
    val symbol: String,
    val category: AssetCategory,
    val currentPrice: Double? = null,
    val change24h: Double? = null,
    val change24hPercent: Double? = null,
    val high24h: Double? = null,
    val low24h: Double? = null,
    val volume24h: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.NOT_CONNECTED,
    val dataStatus: MarketDataStatus = MarketDataStatus.UNAVAILABLE,
    val dataSourceName: String = "Unconnected",
    val lastUpdated: Long? = null,
    val lastUpdatedFormatted: String = "Never",
    val sparklinePoints: List<Float> = emptyList(),
    val dataSourceInfo: String = "",
    val setupInstructions: String? = null,
    val unit: String = "$"
)

enum class SignalType(val label: String) {
    STRONG_BUY("Strong Buy"),
    BUY("Buy"),
    NEUTRAL("Neutral"),
    SELL("Sell"),
    STRONG_SELL("Strong Sell")
}

enum class SignalStatus(val label: String) {
    ACTIVE("Active"),
    TRIGGERED("Target Hit"),
    EXPIRED("Expired")
}

data class Signal(
    val id: String,
    val assetSymbol: String,
    val assetName: String,
    val type: SignalType,
    val entryPrice: String,
    val targetPrice: String,
    val stopLoss: String,
    val riskReward: String,
    val confidenceScore: Int,
    val timeframe: String,
    val timestamp: Long,
    val status: SignalStatus,
    val thesis: String,
    val indicators: List<String>
)

enum class AlertDirection(val symbol: String, val label: String) {
    ABOVE("≥", "Crosses Above"),
    BELOW("≤", "Crosses Below")
}

data class MarketAlert(
    val id: String,
    val assetSymbol: String,
    val assetName: String,
    val targetPrice: Double,
    val direction: AlertDirection,
    val isActive: Boolean = true,
    val isTriggered: Boolean = false,
    val createdTime: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class MarketOverview(
    val fearAndGreedIndex: Int = 67,
    val sentimentLabel: String = "Greed",
    val bullishAssetsPercent: Int = 62,
    val volatilityState: String = "Moderate (19.4%)",
    val activeSignalsCount: Int = 8,
    val marketStatus: String = "Open / Trading",
    val connectedFeedsCount: Int = 1,
    val totalFeedsCount: Int = 20,
    val totalCryptoMarketCap: String = "$2.48 Trillion",
    val cryptoVolume24h: String = "$84.2 Billion",
    val btcDominance: String = "56.4%",
    val dataProviderSummary: String = "Binance WebSocket (Live) • CoinGecko API • Commodities Feed"
)

data class ScannerCriteria(
    val id: String,
    val title: String,
    val description: String
)

data class ScannerItem(
    val id: String,
    val symbol: String,
    val name: String,
    val category: AssetCategory,
    val rsi14: Double,
    val macdStatus: String,
    val emaCondition: String,
    val volumeMultiplier: Double,
    val aiScore: Int,
    val matchedCriteria: List<String>
)

enum class NewsImpact(val label: String) {
    BULLISH("Bullish"),
    BEARISH("Bearish"),
    NEUTRAL("Neutral")
}

data class MarketNews(
    val id: String,
    val title: String,
    val source: String,
    val timestamp: String,
    val category: String,
    val summary: String,
    val impact: NewsImpact,
    val isSaved: Boolean = false,
    val fullContent: String
)

enum class Timeframe(val label: String) {
    _1H("1H"),
    _24H("24H"),
    _7D("7D"),
    _1M("1M"),
    _1Y("1Y")
}

data class ApiFeedConfig(
    val id: String,
    val providerName: String,
    val assetCovered: String,
    val apiKey: String = "",
    val isConnected: Boolean = false,
    val endpointUrl: String,
    val lastPing: String = "Never",
    val requiresApiKey: Boolean = true,
    val setupDocumentationUrl: String = "",
    val instructions: String = ""
)

enum class NavigationTab(val title: String) {
    DASHBOARD("Dashboard"),
    MARKETS("Markets"),
    ANALYSIS("Technical Analysis"),
    AI_SIGNALS("AI Signals"),
    SCANNER("Scanner"),
    NEWS("News"),
    SETTINGS("Settings")
}
