package com.example.data.news

import com.example.data.model.EventStatus
import com.example.data.model.MarketNews
import com.example.data.model.NewsImpact
import com.example.data.model.NewsImpactLevel
import java.util.UUID

data class AnalyzedNewsResult(
    val impact: NewsImpact,
    val impactLevel: NewsImpactLevel,
    val eventStatus: EventStatus,
    val affectedAssets: List<String>,
    val category: String,
    val rationale: String,
    val analysisEngine: String = "Rule-Based Financial NLP (No Fake AI)"
)

object NewsImpactAnalyzer {

    private val bullishKeywords = listOf(
        "rate cut", "easing", "inflow", "inflows", "record high", "all-time high", "breakout",
        "adoption", "approved", "approval", "cooling inflation", "stimulus", "surplus",
        "bullish", "halving", "reserves drop", "institutional buying", "etf approval",
        "reserve currency", "dovish", "rate pause", "expansion", "growth"
    )

    private val bearishKeywords = listOf(
        "rate hike", "hawkish", "outflow", "outflows", "crackdown", "lawsuit", "ban",
        "recession", "inflation spikes", "war", "conflict", "escalation", "sanctions",
        "investigation", "subpoena", "default", "bankruptcy", "liquidation", "bearish",
        "selloff", "dump", "tightening", "deficit", "collapse", "contraction"
    )

    private val highImpactKeywords = listOf(
        "fomc", "federal reserve", "interest rate", "cpi", "inflation", "nfp", "pce",
        "sec", "etf", "blackrock", "war", "middle east", "opec", "treasury", "biden",
        "trump", "china", "default", "emergency", "powell", "crude oil", "gold"
    )

    private val scheduledKeywords = listOf(
        "scheduled", "tomorrow", "next week", "upcoming", "expected to", "forecast",
        "preview", "meeting set for", "to decide", "announcement pending"
    )

    private val unverifiedKeywords = listOf(
        "rumor", "speculation", "alleged", "reportedly", "sources say", "unconfirmed",
        "claim", "claims", "might", "could face", "insider"
    )

    fun analyze(
        headline: String,
        summary: String = "",
        content: String = ""
    ): AnalyzedNewsResult {
        val fullText = "$headline $summary $content".lowercase()

        // 1. Detect Affected Assets
        val assets = mutableListOf<String>()
        if (fullText.contains("bitcoin") || fullText.contains("btc")) assets.add("BTC")
        if (fullText.contains("ethereum") || fullText.contains("eth")) assets.add("ETH")
        if (fullText.contains("solana") || fullText.contains("sol")) assets.add("SOL")
        if (fullText.contains("gold") || fullText.contains("xau")) assets.add("XAU")
        if (fullText.contains("silver") || fullText.contains("xag")) assets.add("XAG")
        if (fullText.contains("crude") || fullText.contains("oil") || fullText.contains("brent") || fullText.contains("wti")) assets.add("OIL")
        if (fullText.contains("gas") || fullText.contains("lng")) assets.add("GAS")
        if (fullText.contains("dollar") || fullText.contains("dxy") || fullText.contains("forex")) assets.add("DXY")
        if (fullText.contains("s&p") || fullText.contains("nasdaq") || fullText.contains("equities")) assets.add("SPX")

        if (assets.isEmpty()) {
            if (fullText.contains("crypto")) assets.add("CRYPTO")
            else if (fullText.contains("fed") || fullText.contains("inflation")) {
                assets.add("DXY")
                assets.add("BTC")
            } else {
                assets.add("MACRO")
            }
        }

        // 2. Classify Category
        val category = when {
            assets.contains("BTC") || assets.contains("ETH") || assets.contains("SOL") || assets.contains("CRYPTO") -> "Crypto"
            assets.contains("XAU") || assets.contains("XAG") -> "Gold & Silver"
            assets.contains("OIL") || assets.contains("GAS") -> "Oil & Energy"
            else -> "Forex & Macro"
        }

        // 3. Score Bullish vs Bearish Sentiment
        var bullScore = 0
        var bearScore = 0

        for (kw in bullishKeywords) {
            if (fullText.contains(kw)) bullScore += 1
        }
        for (kw in bearishKeywords) {
            if (fullText.contains(kw)) bearScore += 1
        }

        val impact = when {
            bullScore > bearScore -> NewsImpact.BULLISH
            bearScore > bullScore -> NewsImpact.BEARISH
            else -> NewsImpact.NEUTRAL
        }

        // 4. Classify Impact Level
        var isHighImpact = false
        for (kw in highImpactKeywords) {
            if (fullText.contains(kw)) {
                isHighImpact = true
                break
            }
        }
        val impactLevel = when {
            isHighImpact || (bullScore + bearScore >= 3) -> NewsImpactLevel.HIGH
            (bullScore + bearScore >= 1) -> NewsImpactLevel.MEDIUM
            else -> NewsImpactLevel.LOW
        }

        // 5. Classify Event Status
        val eventStatus = when {
            unverifiedKeywords.any { fullText.contains(it) } -> EventStatus.UNVERIFIED
            scheduledKeywords.any { fullText.contains(it) } -> EventStatus.SCHEDULED
            else -> EventStatus.CONFIRMED
        }

        val rationale = buildString {
            append("Detected ${assets.joinToString(", ")} catalyst. ")
            when (impact) {
                NewsImpact.BULLISH -> append("Positive catalyst weight (+${bullScore} vs -${bearScore} factors).")
                NewsImpact.BEARISH -> append("Downside risk weight (-${bearScore} vs +${bullScore} factors).")
                NewsImpact.NEUTRAL -> append("Balanced or mixed macroeconomic drivers.")
            }
        }

        return AnalyzedNewsResult(
            impact = impact,
            impactLevel = impactLevel,
            eventStatus = eventStatus,
            affectedAssets = assets,
            category = category,
            rationale = rationale
        )
    }

    fun enrichNewsItem(raw: MarketNews): MarketNews {
        val analysis = analyze(raw.title, raw.summary, raw.fullContent)
        return raw.copy(
            impact = analysis.impact,
            impactLevel = analysis.impactLevel,
            eventStatus = analysis.eventStatus,
            affectedAssets = analysis.affectedAssets,
            category = analysis.category,
            analysisMethod = "Rule-Based NLP Analysis"
        )
    }
}
