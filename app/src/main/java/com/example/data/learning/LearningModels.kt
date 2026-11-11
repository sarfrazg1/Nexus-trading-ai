package com.example.data.learning

import com.example.data.smc.SmcGlossaryItem

/**
 * Three user-selectable language modes for the Learning Center
 * and throughout all educational popups.
 */
enum class LearningLanguageMode(val id: String, val label: String, val nativeLabel: String) {
    ENGLISH("EN", "English", "English Only"),
    ROMAN_URDU("RU", "Roman Urdu", "Roman Urdu"),
    STANDARD_URDU("UR", "اردو (Urdu Script)", "اردو"),
    BILINGUAL("BI", "Bilingual", "English + اردو")
}

/**
 * 7 Official Learning Categories covering beginner to institutional concepts.
 */
enum class LessonCategory(
    val id: String,
    val title: String,
    val romanUrduTitle: String,
    val description: String,
    val order: Int
) {
    TRADING_BASICS(
        id = "basics",
        title = "Trading Basics",
        romanUrduTitle = "Trading ki Bunyad",
        description = "Spot, Futures, Long/Short, Market & Limit orders, and Margin types",
        order = 1
    ),
    TECHNICAL_ANALYSIS(
        id = "technical",
        title = "Technical Analysis",
        romanUrduTitle = "Technical Analysis aur Indicators",
        description = "Candlesticks, moving averages, RSI, MACD, Bollinger Bands, and Volume",
        order = 2
    ),
    SMART_MONEY(
        id = "smc",
        title = "Smart Money Concepts",
        romanUrduTitle = "Smart Money aur Institutional Concepts",
        description = "BOS, CHoCH, Order Blocks, Fair Value Gaps, and Liquidity Sweeps",
        order = 3
    ),
    FIBONACCI(
        id = "fibonacci",
        title = "Fibonacci Trading",
        romanUrduTitle = "Fibonacci aur Golden Pocket",
        description = "Retracements, Extensions, Golden Pocket (0.618), and Confluence",
        order = 4
    ),
    RISK_MANAGEMENT(
        id = "risk",
        title = "Risk Management",
        romanUrduTitle = "Risk Management aur Sarmaya Bachana",
        description = "Stop Loss, Position Sizing, Liquidation Protection, and Drawdown",
        order = 5
    ),
    FUNDAMENTAL_MACRO(
        id = "macro",
        title = "Fundamental & Macro",
        romanUrduTitle = "Bunyadi aur Macroeqtisaadi Analysis",
        description = "Crypto Cycles, Federal Reserve, Interest Rates, CPI, DXY, and ETF Flows",
        order = 6
    ),
    ADVANCED_TRADING(
        id = "advanced",
        title = "Advanced Trading",
        romanUrduTitle = "Advanced Institutional Tools",
        description = "Open Interest, Funding Rates, Liquidation Heatmaps, CVD, and Whale Activity",
        order = 7
    );

    val letter: String get() = ('A' + (order - 1)).toString()

    companion object {
        val SMART_MONEY_CONCEPTS: LessonCategory get() = SMART_MONEY
    }
}

/**
 * Production-grade lesson item with dual language fields, practical real-world examples,
 * pro tips, and capital risk warnings.
 */
data class LessonItem(
    val id: String,
    val termKey: String, // matching key for (i) icons e.g. "BOS", "LONG", "RSI", "LEVERAGE"
    val category: LessonCategory,
    val titleEnglish: String,
    val titleRomanUrdu: String,
    val summaryEnglish: String,
    val summaryRomanUrdu: String,
    val detailEnglish: String,
    val detailRomanUrdu: String,
    val practicalExampleEnglish: String,
    val practicalExampleRomanUrdu: String,
    val proTipEnglish: String,
    val proTipRomanUrdu: String,
    val riskWarningEnglish: String? = null,
    val riskWarningRomanUrdu: String? = null,
    val relatedTerms: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
    val isCompleted: Boolean = false,
    val lastViewedTimestamp: Long? = null
) {
    val englishTitle: String get() = titleEnglish
    val romanUrduTitle: String get() = titleRomanUrdu
    val detailedExplanationEnglish: String get() = detailEnglish
    val detailedExplanationRomanUrdu: String get() = detailRomanUrdu
    val searchKeywords: List<String> get() = relatedTerms
    /**
     * Converts to legacy SmcGlossaryItem for backwards-compatibility.
     */
    fun toSmcGlossaryItem(languageMode: LearningLanguageMode = LearningLanguageMode.BILINGUAL): SmcGlossaryItem {
        val summary = when (languageMode) {
            LearningLanguageMode.ENGLISH -> summaryEnglish
            LearningLanguageMode.ROMAN_URDU -> summaryRomanUrdu
            LearningLanguageMode.STANDARD_URDU -> summaryRomanUrdu
            LearningLanguageMode.BILINGUAL -> "$summaryEnglish\n\n[Roman Urdu]: $summaryRomanUrdu"
        }

        val detail = when (languageMode) {
            LearningLanguageMode.ENGLISH -> "$detailEnglish\n\nPractical Example:\n$practicalExampleEnglish"
            LearningLanguageMode.ROMAN_URDU -> "$detailRomanUrdu\n\nAmali Misaal (Practical Example):\n$practicalExampleRomanUrdu"
            LearningLanguageMode.STANDARD_URDU -> "$detailRomanUrdu\n\nعملی مثال (Practical Example):\n$practicalExampleRomanUrdu"
            LearningLanguageMode.BILINGUAL -> "$detailEnglish\n\nExample:\n$practicalExampleEnglish\n\n━━━━━━━━━━━━━━━━━━━━\n[Roman Urdu Wazaahat]:\n$detailRomanUrdu\n\nAmali Misaal:\n$practicalExampleRomanUrdu"
        }

        val tip = when (languageMode) {
            LearningLanguageMode.ENGLISH -> proTipEnglish
            LearningLanguageMode.ROMAN_URDU -> proTipRomanUrdu
            LearningLanguageMode.STANDARD_URDU -> proTipRomanUrdu
            LearningLanguageMode.BILINGUAL -> "$proTipEnglish | [Urdu Tip]: $proTipRomanUrdu"
        }

        return SmcGlossaryItem(
            termKey = termKey,
            title = "$titleEnglish ($titleRomanUrdu)",
            category = category.title,
            romanUrduSummary = summary,
            romanUrduDetail = detail,
            tradingTipRomanUrdu = tip
        )
    }
}
