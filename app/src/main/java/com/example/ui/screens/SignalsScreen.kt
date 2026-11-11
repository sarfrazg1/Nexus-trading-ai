package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleTimeframe
import com.example.data.model.Signal
import com.example.data.model.SignalStatus
import com.example.data.model.SignalType
import com.example.data.signal.AiTradingSignal
import com.example.data.signal.SignalDecision
import com.example.ui.components.SignalItemCard
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedBg
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.BullishGreenBg
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexusGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignalsScreen(
    currentAiSignal: AiTradingSignal?,
    isAnalyzing: Boolean,
    selectedSymbol: String,
    selectedTimeframe: CandleTimeframe,
    isExpanded: Boolean,
    onAssetSelected: (String) -> Unit,
    onTimeframeSelected: (CandleTimeframe) -> Unit,
    onAnalyzeClick: () -> Unit,
    onToggleExpanded: () -> Unit,
    onInfoClick: (String) -> Unit,
    aiSignalsList: List<AiTradingSignal>,
    signals: List<Signal>,
    signalFilter: String,
    onFilterSelected: (String) -> Unit,
    onSetAlertClick: (symbol: String) -> Unit,
    onCopyLevelsClick: (Signal) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val supportedAssets = listOf(
        Pair("BTC", "Bitcoin"),
        Pair("ETH", "Ethereum"),
        Pair("SOL", "Solana"),
        Pair("BNB", "BNB"),
        Pair("XRP", "XRP"),
        Pair("DOGE", "Dogecoin"),
        Pair("ADA", "Cardano"),
        Pair("LINK", "Chainlink"),
        Pair("XAUUSD", "Gold"),
        Pair("XAGUSD", "Silver"),
        Pair("OIL_WTI", "Crude Oil")
    )

    val timeframes = listOf(
        CandleTimeframe.M1,
        CandleTimeframe.M5,
        CandleTimeframe.M15,
        CandleTimeframe.M30,
        CandleTimeframe.H1,
        CandleTimeframe.H4,
        CandleTimeframe.D1,
        CandleTimeframe.W1
    )

    val filterTabs = listOf("ALL", "LONG", "SHORT", "NO TRADE")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ----------------------------------------------------
        // 1. HEADER & CONTROL PANEL
        // ----------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MULTI-FACTOR INTELLIGENCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = "AI Trading Signal Engine",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Button(
                    onClick = onAnalyzeClick,
                    enabled = !isAnalyzing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexusGold),
                    modifier = Modifier.testTag("signal_analyze_button")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            color = NavyBackground,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = NavyBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Analyze",
                            color = NavyBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Asset selector chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(supportedAssets) { (sym, name) ->
                    val isSel = selectedSymbol.equals(sym, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) NexusGold else NavyCard)
                            .border(1.dp, if (isSel) NexusGold else NavyBorder, RoundedCornerShape(12.dp))
                            .clickable { onAssetSelected(sym) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("signal_asset_chip_$sym"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sym,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSel) NavyBackground else TextPrimary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timeframe selector row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(timeframes) { tf ->
                    val isSel = selectedTimeframe == tf
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) NavySurfaceVariant else NavyCard)
                            .border(1.dp, if (isSel) NexusGold.copy(alpha = 0.6f) else NavyBorder, RoundedCornerShape(8.dp))
                            .clickable { onTimeframeSelected(tf) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("signal_tf_chip_${tf.label}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSel) NexusGold else TextSecondary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // ----------------------------------------------------
        // 2. HERO SIGNAL CARD (LONG / SHORT / NO TRADE)
        // ----------------------------------------------------
        item {
            if (isAnalyzing && currentAiSignal == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NexusGold, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing $selectedSymbol OHLCV, Indicators & SMC...",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else if (currentAiSignal != null) {
                HeroSignalCard(
                    signal = currentAiSignal,
                    isExpanded = isExpanded,
                    onToggleExpanded = onToggleExpanded,
                    onInfoClick = onInfoClick,
                    onCopyLevelsClick = {
                        val clip = currentAiSignal.riskPlan?.let { plan ->
                            "${currentAiSignal.symbol} ${currentAiSignal.decision.label} on ${currentAiSignal.primaryTimeframe.label}\n" +
                                    "Entry: ${plan.entryZoneFormatted}\n" +
                                    "Stop Loss: ${plan.stopLossFormatted} (-${String.format(Locale.US, "%.1f", plan.stopLossDistancePercent)}%)\n" +
                                    "TP1: ${plan.takeProfit1Formatted} (+${String.format(Locale.US, "%.1f", plan.takeProfit1DistancePercent)}%)\n" +
                                    "TP2: ${plan.takeProfit2Formatted} (+${String.format(Locale.US, "%.1f", plan.takeProfit2DistancePercent)}%)\n" +
                                    "TP3: ${plan.takeProfit3Formatted} (+${String.format(Locale.US, "%.1f", plan.takeProfit3DistancePercent)}%)\n" +
                                    "R:R: ${plan.riskRewardFormatted} | Max Leverage: ${plan.suggestedMaxLeverage}x\n" +
                                    "Confidence: ${currentAiSignal.confidence.totalScore}%\n" +
                                    "Invalidation: ${plan.invalidationCondition}"
                        } ?: "${currentAiSignal.symbol} NO TRADE: ${currentAiSignal.noTradeReason?.headline}"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("NEXUS Signal", clip))
                    },
                    onSetAlertClick = { onSetAlertClick(currentAiSignal.symbol) }
                )
            }
        }

        // ----------------------------------------------------
        // 3. TRANSPARENT CONFIDENCE SCORE BREAKDOWN
        // ----------------------------------------------------
        if (currentAiSignal != null) {
            item {
                ConfidenceBreakdownCard(
                    confidence = currentAiSignal.confidence,
                    onInfoClick = onInfoClick
                )
            }
        }

        // ----------------------------------------------------
        // 4. MULTI-TIMEFRAME CONFIRMATION MATRIX
        // ----------------------------------------------------
        if (currentAiSignal != null && currentAiSignal.timeframeConditions.isNotEmpty()) {
            item {
                MultiTimeframeMatrixCard(
                    conditions = currentAiSignal.timeframeConditions,
                    agreementCount = currentAiSignal.mtfAgreementCount,
                    totalCount = currentAiSignal.mtfTotalCount,
                    agreementPercent = currentAiSignal.mtfAgreementPercent,
                    decision = currentAiSignal.decision
                )
            }
        }

        // ----------------------------------------------------
        // 5. SAFETY & REGULATORY COMPLIANCE BANNER
        // ----------------------------------------------------
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Manual Execution",
                        tint = NexusGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Manual Execution Only: NEXUS AI provides decision analytics and never executes trades automatically. Risk is user-managed.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // ----------------------------------------------------
        // 6. MARKET-WIDE SIGNALS SCANNER FEED
        // ----------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Market Opportunities Feed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "${aiSignalsList.size} Scanned",
                    color = NexusGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterTabs) { filter ->
                    val isSel = signalFilter.equals(filter, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) NexusGold else NavyCard)
                            .clickable { onFilterSelected(filter) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("signal_feed_filter_${filter.lowercase().replace(" ", "_")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSel) NavyBackground else TextSecondary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Filter and display signals
        val filteredList = aiSignalsList.filter { sig ->
            when (signalFilter.uppercase(Locale.US)) {
                "ALL" -> true
                "LONG" -> sig.decision == SignalDecision.LONG
                "SHORT" -> sig.decision == SignalDecision.SHORT
                "NO TRADE" -> sig.decision == SignalDecision.NO_TRADE
                else -> true
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No signals matching filter '$signalFilter'",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { sig ->
                CompactAiSignalRow(
                    signal = sig,
                    isSelected = sig.symbol.equals(selectedSymbol, ignoreCase = true),
                    onSelect = { onAssetSelected(sig.symbol) },
                    onInfoClick = onInfoClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------------------------
// HERO SIGNAL CARD
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroSignalCard(
    signal: AiTradingSignal,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onInfoClick: (String) -> Unit,
    onCopyLevelsClick: () -> Unit,
    onSetAlertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLong = signal.decision == SignalDecision.LONG
    val isShort = signal.decision == SignalDecision.SHORT
    val isNoTrade = signal.decision == SignalDecision.NO_TRADE

    val accentColor = when {
        isLong -> BullishGreen
        isShort -> BearishRed
        else -> NexusGold
    }

    val badgeBg = when {
        isLong -> BullishGreenBg
        isShort -> BearishRedBg
        else -> NavySurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_signal_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(accentColor.copy(alpha = 0.6f), NavyBorder)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Symbol, Timeframe, Signal Decision Badge + Roman Urdu Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(NavySurface, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = signal.primaryTimeframe.label,
                            color = NexusGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Decision Badge + Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("signal_decision_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    isLong -> Icons.Default.TrendingUp
                                    isShort -> Icons.Default.TrendingDown
                                    else -> Icons.Default.HourglassTop
                                },
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = signal.decision.label,
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Roman Urdu Info Icon
                    IconButton(
                        onClick = {
                            val termKey = when {
                                isLong -> "LONG"
                                isShort -> "SHORT"
                                else -> "NO_TRADE"
                            }
                            onInfoClick(termKey)
                        },
                        modifier = Modifier.size(28.dp).testTag("signal_decision_info_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Roman Urdu Info",
                            tint = NexusGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-header: Freshness and Confluence Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${signal.dataSource} • ${signal.dataFreshness}",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Confidence: ",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${signal.confidence.totalScore}%",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Confidence Info",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onInfoClick("CONFIDENCE_SCORE") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ------------------------------------------------
            // BRANCH A: NO TRADE VIEW
            // ------------------------------------------------
            if (isNoTrade && signal.noTradeReason != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Advisory",
                                tint = NexusGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = signal.noTradeReason.headline,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = signal.noTradeReason.detailedExplanation,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        if (signal.noTradeReason.conflictingFactors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Conflicting Factors:",
                                color = BearishRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            signal.noTradeReason.conflictingFactors.forEach { factor ->
                                Text(
                                    text = "• $factor",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Recommended: ${signal.noTradeReason.recommendedAction}",
                            color = NexusGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ------------------------------------------------
            // BRANCH B: LONG / SHORT KEY LEVELS & RISK PLAN
            // ------------------------------------------------
            if (!isNoTrade && signal.riskPlan != null) {
                val plan = signal.riskPlan

                // Visual Risk-Reward Bar
                RiskRewardVisualBar(
                    riskPercent = plan.estimatedRiskPercent,
                    rewardPercent = plan.potentialRewardPercent,
                    rrFormatted = plan.riskRewardFormatted,
                    onInfoClick = onInfoClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Key Levels Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NavySurface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Entry Row
                    LevelRow(
                        label = "ENTRY ZONE",
                        value = plan.entryZoneFormatted,
                        subText = "Discount Structural Level",
                        color = TextPrimary,
                        termKey = "ENTRY",
                        onInfoClick = onInfoClick
                    )

                    // Stop Loss Row
                    LevelRow(
                        label = "STOP LOSS",
                        value = plan.stopLossFormatted,
                        subText = "-${String.format(Locale.US, "%.2f", plan.stopLossDistancePercent)}%",
                        color = BearishRed,
                        termKey = "STOP_LOSS",
                        onInfoClick = onInfoClick
                    )

                    // Take Profit Targets
                    LevelRow(
                        label = "TAKE PROFIT 1",
                        value = plan.takeProfit1Formatted,
                        subText = "+${String.format(Locale.US, "%.2f", plan.takeProfit1DistancePercent)}% (1.6x R:R)",
                        color = BullishGreen,
                        termKey = "TAKE_PROFIT",
                        onInfoClick = onInfoClick
                    )

                    LevelRow(
                        label = "TAKE PROFIT 2 (Main)",
                        value = plan.takeProfit2Formatted,
                        subText = "+${String.format(Locale.US, "%.2f", plan.takeProfit2DistancePercent)}% (${plan.riskRewardFormatted})",
                        color = BullishGreen,
                        termKey = "TAKE_PROFIT",
                        onInfoClick = onInfoClick
                    )

                    LevelRow(
                        label = "TAKE PROFIT 3 (Runner)",
                        value = plan.takeProfit3Formatted,
                        subText = "+${String.format(Locale.US, "%.2f", plan.takeProfit3DistancePercent)}% (Fib 1.618)",
                        color = NexusGold,
                        termKey = "TAKE_PROFIT",
                        onInfoClick = onInfoClick
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Leverage & Invalidation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Suggested Max Leverage
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Max Leverage: ${plan.suggestedMaxLeverage}x",
                            color = NexusGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Leverage Info",
                            tint = NexusGold,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onInfoClick("LEVERAGE") }
                        )
                    }

                    // Risk/Reward ratio badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "R:R: ${plan.riskRewardFormatted}",
                            color = BullishGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "R:R Info",
                            tint = BullishGreen,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onInfoClick("RISK_REWARD") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Invalidation Condition
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BearishRedBg)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Invalidation",
                            tint = BearishRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Invalidation Condition:",
                                color = BearishRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = plan.invalidationCondition,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        IconButton(
                            onClick = { onInfoClick("INVALIDATION") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Invalidation Info",
                                tint = BearishRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ------------------------------------------------
            // SUPPORTING CHIPS (INDICATORS & SMC)
            // ------------------------------------------------
            if (signal.supportingIndicators.isNotEmpty() || signal.smcConfirmations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    signal.smcConfirmations.forEach { smc ->
                        Box(
                            modifier = Modifier
                                .background(NavySurfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = smc, color = NexusGold, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    signal.supportingIndicators.forEach { ind ->
                        Box(
                            modifier = Modifier
                                .background(NavySurface, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = ind, color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Copy Levels + Set Alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyLevelsClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("copy_signal_levels_btn")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Copy Levels", color = TextPrimary, fontSize = 11.sp)
                }

                Button(
                    onClick = onSetAlertClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                    modifier = Modifier.weight(1f).testTag("set_signal_alert_btn")
                ) {
                    Icon(imageVector = Icons.Default.AddAlert, contentDescription = "Alert", tint = NexusGold, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Set Price Alert", color = NexusGold, fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// LEVEL ROW HELPER
// ---------------------------------------------------------------------------------------------

@Composable
fun LevelRow(
    label: String,
    value: String,
    subText: String,
    color: Color,
    termKey: String,
    onInfoClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "$label Info",
                tint = TextMuted,
                modifier = Modifier
                    .size(13.dp)
                    .clickable { onInfoClick(termKey) }
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subText,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// RISK-REWARD VISUAL BAR
// ---------------------------------------------------------------------------------------------

@Composable
fun RiskRewardVisualBar(
    riskPercent: Double,
    rewardPercent: Double,
    rrFormatted: String,
    onInfoClick: (String) -> Unit
) {
    val total = (riskPercent + rewardPercent).coerceAtLeast(1.0)
    val riskFraction = (riskPercent / total).toFloat().coerceIn(0.1f, 0.5f)
    val rewardFraction = 1f - riskFraction

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Risk: ${String.format(Locale.US, "%.1f", riskPercent)}%",
                color = BearishRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ratio $rrFormatted",
                    color = NexusGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Risk Reward Info",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(13.dp)
                        .clickable { onInfoClick("RISK_REWARD") }
                )
            }

            Text(
                text = "Target: +${String.format(Locale.US, "%.1f", rewardPercent)}%",
                color = BullishGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(riskFraction)
                    .fillMaxSize()
                    .background(BearishRed)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight(rewardFraction)
                    .fillMaxSize()
                    .background(BullishGreen)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// TRANSPARENT CONFIDENCE SCORE BREAKDOWN CARD
// ---------------------------------------------------------------------------------------------

@Composable
fun ConfidenceBreakdownCard(
    confidence: com.example.data.signal.ConfidenceScoreBreakdown,
    onInfoClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Confidence Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Confidence Info",
                        tint = NexusGold,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onInfoClick("CONFIDENCE_SCORE") }
                    )
                }

                Text(
                    text = "${confidence.totalScore} / 100",
                    color = NexusGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Transparent multi-factor scoring based on verified technical indicators and market structure.",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Factors progress rows
            confidence.factors.forEach { factor ->
                FactorProgressRow(factor = factor)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Notice: Confidence score reflects data agreement from historical patterns, not a guaranteed probability of profit.",
                color = TextMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun FactorProgressRow(factor: com.example.data.signal.SignalFactorReason) {
    val fraction = (factor.awardedPoints.toFloat() / factor.maxPoints).coerceIn(0f, 1f)
    val color = when (factor.stance) {
        "BULLISH" -> BullishGreen
        "BEARISH" -> BearishRed
        "CONFLICTING" -> NexusGold
        else -> TextSecondary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = factor.factorName,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${factor.awardedPoints} / ${factor.maxPoints} pts",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = NavySurface
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = factor.summary,
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}

// ---------------------------------------------------------------------------------------------
// MULTI-TIMEFRAME CONFIRMATION MATRIX
// ---------------------------------------------------------------------------------------------

@Composable
fun MultiTimeframeMatrixCard(
    conditions: List<com.example.data.signal.TimeframeCondition>,
    agreementCount: Int,
    totalCount: Int,
    agreementPercent: Int,
    decision: SignalDecision
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Multi-Timeframe Confirmation",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Box(
                    modifier = Modifier
                        .background(NavySurfaceVariant, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$agreementCount / $totalCount Aligned ($agreementPercent%)",
                        color = if (agreementPercent >= 65) BullishGreen else NexusGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 8-Timeframe Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                conditions.forEach { cond ->
                    val color = when (cond.stance) {
                        "BULLISH" -> BullishGreen
                        "BEARISH" -> BearishRed
                        else -> TextMuted
                    }
                    val bg = when (cond.stance) {
                        "BULLISH" -> BullishGreenBg
                        "BEARISH" -> BearishRedBg
                        else -> NavySurface
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = cond.timeframe.label,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cond.stance.take(4),
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// COMPACT SCANNED SIGNAL ROW
// ---------------------------------------------------------------------------------------------

@Composable
fun CompactAiSignalRow(
    signal: AiTradingSignal,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onInfoClick: (String) -> Unit
) {
    val isLong = signal.decision == SignalDecision.LONG
    val isShort = signal.decision == SignalDecision.SHORT
    val accentColor = when {
        isLong -> BullishGreen
        isShort -> BearishRed
        else -> NexusGold
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("signal_feed_row_${signal.symbol}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NavySurfaceVariant else NavyCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NexusGold else NavyBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = signal.primaryTimeframe.label,
                        color = NexusGold,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (signal.decision == SignalDecision.NO_TRADE) {
                        signal.noTradeReason?.headline ?: "No Setup"
                    } else {
                        "Entry: ${signal.riskPlan?.entryZoneFormatted ?: "N/A"}"
                    },
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            when {
                                isLong -> BullishGreenBg
                                isShort -> BearishRedBg
                                else -> NavySurface
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = signal.decision.label,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "${signal.confidence.totalScore}%",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
