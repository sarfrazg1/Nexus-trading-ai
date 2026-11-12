package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleTimeframe
import com.example.data.model.ScannerItem
import com.example.data.scanner.FuturesScanSignal
import com.example.data.scanner.FuturesSignalLifecycle
import com.example.data.scanner.FuturesWatchlistPair
import com.example.data.scanner.ScannerEngineState
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
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
fun ScannerScreen(
    scannerItems: List<ScannerItem>,
    scannerFilter: String,
    onFilterSelected: (String) -> Unit,
    onSetAlertClick: (symbol: String) -> Unit,
    selectedTimeframe: CandleTimeframe = CandleTimeframe.H1,
    onTimeframeSelected: (CandleTimeframe) -> Unit = {},
    onInspectAsset: (assetId: String, symbol: String) -> Unit = { _, _ -> },
    onRefreshScanner: () -> Unit = {},
    // Binance USDT-M Futures Auto-Scanner parameters
    futuresScannerState: ScannerEngineState = ScannerEngineState(),
    futuresWatchlist: List<FuturesWatchlistPair> = emptyList(),
    scannerScreenMode: Int = 0, // 0 = Futures Auto-Scanner, 1 = Market Anomaly Screener
    onModeSelected: (Int) -> Unit = {},
    onStartFuturesScanner: () -> Unit = {},
    onStopFuturesScanner: () -> Unit = {},
    onToggleFuturesWatchlist: (symbol: String, enabled: Boolean) -> Unit = { _, _ -> },
    onAddCustomFuturesPair: (rawInput: String) -> Unit = {},
    onRemoveFuturesPair: (symbol: String) -> Unit = {},
    onIntervalSelected: (Int) -> Unit = {},
    onTogglePaperTrading: (Boolean) -> Unit = {},
    onExplainSignalWithAi: (FuturesScanSignal) -> Unit = {},
    onSimulatePaperTrade: (FuturesScanSignal) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var futuresSubTab by remember { mutableIntStateOf(0) }

    // 13 Scan Filters from Task 8 Part B
    val filterOptions = listOf(
        "ALL",
        "STRONG BULLISH TREND",
        "STRONG BEARISH TREND",
        "RSI OVERBOUGHT (>70)",
        "RSI OVERSOLD (<30)",
        "EMA CROSSOVERS",
        "MACD CROSSOVERS",
        "HIGH VOLUME (RVOL > 1.5X)",
        "HIGH VOLATILITY (ATR)",
        "BOS & CHOCH",
        "ACTIVE FVG ZONES",
        "LIQUIDITY SWEEPS",
        "NEAR SUPPORT (≤0.8%)",
        "NEAR RESISTANCE (≤0.8%)"
    )

    val supportedTimeframes = listOf(
        CandleTimeframe.M15,
        CandleTimeframe.H1,
        CandleTimeframe.H4,
        CandleTimeframe.D1
    )

    val filteredAnomalyItems = scannerItems.filter { item ->
        when (scannerFilter.uppercase()) {
            "ALL" -> true
            "STRONG BULLISH TREND" -> item.matchedCriteria.any { it.contains("Strong Bullish Trend", ignoreCase = true) }
            "STRONG BEARISH TREND" -> item.matchedCriteria.any { it.contains("Strong Bearish Trend", ignoreCase = true) }
            "RSI OVERBOUGHT (>70)" -> item.rsi14 >= 70.0 || item.matchedCriteria.any { it.contains("Overbought", ignoreCase = true) }
            "RSI OVERSOLD (<30)" -> item.rsi14 <= 30.0 || item.matchedCriteria.any { it.contains("Oversold", ignoreCase = true) }
            "EMA CROSSOVERS" -> item.matchedCriteria.any { it.contains("EMA", ignoreCase = true) }
            "MACD CROSSOVERS" -> item.macdStatus.contains("Cross", ignoreCase = true) || item.matchedCriteria.any { it.contains("MACD", ignoreCase = true) }
            "HIGH VOLUME (RVOL > 1.5X)" -> item.volumeMultiplier >= 1.5 || item.matchedCriteria.any { it.contains("Volume", ignoreCase = true) }
            "HIGH VOLATILITY (ATR)" -> item.matchedCriteria.any { it.contains("Volatility", ignoreCase = true) }
            "BOS & CHOCH" -> item.matchedCriteria.any { it.contains("BOS", ignoreCase = true) || it.contains("CHOCH", ignoreCase = true) }
            "ACTIVE FVG ZONES" -> item.matchedCriteria.any { it.contains("FVG", ignoreCase = true) }
            "LIQUIDITY SWEEPS" -> item.matchedCriteria.any { it.contains("Liquidity", ignoreCase = true) || it.contains("Sweep", ignoreCase = true) }
            "NEAR SUPPORT (≤0.8%)" -> item.matchedCriteria.any { it.contains("Support", ignoreCase = true) }
            "NEAR RESISTANCE (≤0.8%)" -> item.matchedCriteria.any { it.contains("Resistance", ignoreCase = true) }
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HEADER WITH TITLE & MODE SWITCHER
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REAL-TIME MARKET INTELLIGENCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = if (scannerScreenMode == 0) "Binance Futures Auto-Scanner" else "Market Anomaly Screener",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                IconButton(
                    onClick = onRefreshScanner,
                    modifier = Modifier.size(36.dp).testTag("refresh_scanner_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Scanner",
                        tint = NexusGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PRIMARY MODE SELECTOR: Binance Futures Auto-Scanner vs Anomaly Screener
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (scannerScreenMode == 0) NexusGold else NavyCard)
                        .clickable { onModeSelected(0) }
                        .padding(vertical = 8.dp)
                        .testTag("mode_tab_futures_scanner"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = if (scannerScreenMode == 0) NavyBackground else NexusGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Futures Auto-Scanner",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (scannerScreenMode == 0) NavyBackground else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (scannerScreenMode == 1) NexusGold else NavyCard)
                        .clickable { onModeSelected(1) }
                        .padding(vertical = 8.dp)
                        .testTag("mode_tab_anomaly_screener"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = if (scannerScreenMode == 1) NavyBackground else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Anomaly Screener",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (scannerScreenMode == 1) NavyBackground else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // =====================================================================
        // MODE 0: BINANCE USDT-M FUTURES AUTO-SCANNER
        // =====================================================================
        if (scannerScreenMode == 0) {
            item {
                FuturesScannerDashboardHeader(
                    state = futuresScannerState,
                    onStartScanner = onStartFuturesScanner,
                    onStopScanner = onStopFuturesScanner,
                    onIntervalSelected = onIntervalSelected,
                    onTogglePaperTrading = onTogglePaperTrading
                )
            }

            item {
                BatteryOptimizationNoticeCard()
            }

            // Sub-tabs for Futures Scanner
            item {
                val activeCount = futuresScannerState.activeSignals.count {
                    it.lifecycle == FuturesSignalLifecycle.ENTRY_CONFIRMED ||
                            it.lifecycle == FuturesSignalLifecycle.ACTIVE
                }
                val setupsCount = futuresScannerState.watchlistSetups.size
                val watchlistActiveCount = futuresWatchlist.count { it.isSelectedForScanning }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val subTabs = listOf(
                        "Confirmed ($activeCount)" to 0,
                        "Setups ($setupsCount)" to 1,
                        "Watchlist ($watchlistActiveCount)" to 2,
                        "History & Stats" to 3
                    )

                    subTabs.forEach { (title, index) ->
                        val isSel = futuresSubTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) NexusGold else NavyCard)
                                .clickable { futuresSubTab = index }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) NavyBackground else TextSecondary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            when (futuresSubTab) {
                0 -> {
                    // Confirmed Entries
                    val confirmedSignals = futuresScannerState.activeSignals.filter {
                        it.lifecycle == FuturesSignalLifecycle.ENTRY_CONFIRMED ||
                                it.lifecycle == FuturesSignalLifecycle.ACTIVE
                    }

                    if (confirmedSignals.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = NavyCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NexusGold.copy(alpha = 0.6f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Confirmed Entries Right Now",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "The NEXUS confluence engine never forces a trade. When 4H macro trend, 1H OB/FVG, and 15M/5M BOS align across your selected watchlist coins, confirmed entries will appear here instantly with sound/push alerts.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextMuted,
                                            lineHeight = 16.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        items(confirmedSignals, key = { it.id }) { signal ->
                            FuturesSignalCard(
                                signal = signal,
                                onExplainClick = { onExplainSignalWithAi(signal) },
                                onCopyLevels = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(
                                        "NEXUS Futures Signal",
                                        "${signal.symbol} ${signal.direction.name}\nEntry: ${signal.entryZoneFormatted}\nSL: ${signal.stopLossFormatted}\nTP1: ${signal.tp1Formatted}\nTP2: ${signal.tp2Formatted}\nTP3: ${signal.tp3Formatted}\nR:R: 1:${String.format(Locale.US, "%.2f", signal.riskRewardRatio)}"
                                    )
                                    clipboard.setPrimaryClip(clip)
                                },
                                onSimulatePaperTrade = { onSimulatePaperTrade(signal) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                1 -> {
                    // Developing Setups
                    val setups = futuresScannerState.watchlistSetups
                    if (setups.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = NavyCard)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = NexusGold.copy(alpha = 0.6f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No Developing Setups Detected",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Monitoring 4H and 1H structure on selected watchlist coins. Potential setups approaching Order Blocks or liquidity pools will display here as WATCHLIST_SETUP prior to entry confirmation.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextMuted,
                                            lineHeight = 16.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        items(setups, key = { it.id }) { signal ->
                            FuturesSignalCard(
                                signal = signal,
                                onExplainClick = { onExplainSignalWithAi(signal) },
                                onCopyLevels = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(
                                        "NEXUS Futures Setup",
                                        "${signal.symbol} ${signal.direction.name} (Developing)\nZone: ${signal.entryZoneFormatted}\nSL: ${signal.stopLossFormatted}"
                                    )
                                    clipboard.setPrimaryClip(clip)
                                },
                                onSimulatePaperTrade = { onSimulatePaperTrade(signal) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                2 -> {
                    // Watchlist Manager
                    item {
                        FuturesWatchlistManagerSection(
                            watchlist = futuresWatchlist,
                            onToggleScanning = { symbol ->
                                val pair = futuresWatchlist.firstOrNull { it.symbol == symbol }
                                if (pair != null) {
                                    onToggleFuturesWatchlist(symbol, !pair.isSelectedForScanning)
                                }
                            },
                            onAddCustomPair = onAddCustomFuturesPair,
                            onRemovePair = onRemoveFuturesPair
                        )
                    }
                }

                3 -> {
                    // History & Analytics
                    item {
                        FuturesSignalHistoryTab(
                            history = futuresScannerState.signalHistory,
                            statistics = futuresScannerState.statistics
                        )
                    }
                }
            }
        }

        // =====================================================================
        // MODE 1: MARKET ANOMALY SCREENER (TASK 8)
        // =====================================================================
        if (scannerScreenMode == 1) {
            item {
                // Timeframe Selector Row (15m, 1h, 4h, 1d)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    supportedTimeframes.forEach { tf ->
                        val isSel = selectedTimeframe == tf
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) NexusGold else NavyCard)
                                .clickable { onTimeframeSelected(tf) }
                                .padding(vertical = 7.dp)
                                .testTag("scanner_tf_${tf.binanceInterval}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tf.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) NavyBackground else TextSecondary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 13 Strategy Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { filter ->
                        val isSel = scannerFilter.equals(filter, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSel) NexusGold else NavyCard)
                                .clickable { onFilterSelected(filter) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("scanner_filter_${filter.lowercase().replace(" ", "_").replace("(", "").replace(")", "").replace(">", "gt_").replace("<", "lt_").replace("≤", "lte_").replace(".", "_").replace("%", "")}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSel) NavyBackground else TextSecondary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Screener Stats Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCAN RESULTS: ${filteredAnomalyItems.size} ASSETS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "Timeframe: ${selectedTimeframe.label}",
                        style = MaterialTheme.typography.labelSmall.copy(color = NexusGold)
                    )
                }
            }

            // Anomaly item cards
            items(filteredAnomalyItems, key = { it.id }) { item ->
                val isBullish = item.detectedCondition.contains("Bullish", ignoreCase = true)
                val trendColor = if (isBullish) BullishGreen else BearishRed

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .testTag("scanner_card_${item.symbol}"),
                    colors = CardDefaults.cardColors(containerColor = NavyCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header row: Symbol, Name, Price, Trend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.symbol,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", item.verifiedPrice)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(trendColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.detectedCondition,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = trendColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Technical Grid: RSI, MACD, 200 EMA, RVOL
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavySurface)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("RSI (14)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                                val rsiColor = when {
                                    item.rsi14 >= 70.0 -> BearishRed
                                    item.rsi14 <= 30.0 -> BullishGreen
                                    else -> TextPrimary
                                }
                                Text(
                                    text = String.format(Locale.US, "%.1f", item.rsi14),
                                    style = MaterialTheme.typography.bodySmall.copy(color = rsiColor, fontWeight = FontWeight.Bold)
                                )
                            }
                            Column {
                                Text("MACD", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                                Text(item.macdStatus, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.Medium))
                            }
                            Column {
                                Text("200 EMA", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                                Text(item.emaCondition, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.Medium))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("VOL DELTA", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                                Text("${item.volumeMultiplier}x", style = MaterialTheme.typography.bodySmall.copy(color = NexusGold, fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Matched criteria chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.matchedCriteria.forEach { crit ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NavySurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = crit,
                                        style = MaterialTheme.typography.bodySmall.copy(color = NexusGold, fontSize = 10.sp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions: Inspect in Technical Analysis + Set Alert
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onInspectAsset(item.assetId, item.symbol) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scanner_inspect_btn_${item.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                            ) {
                                Icon(imageVector = Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(14.dp), tint = NexusGold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onSetAlertClick(item.symbol) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("scanner_alert_btn_${item.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusGold),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NexusGold.copy(alpha = 0.5f))
                            ) {
                                Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
