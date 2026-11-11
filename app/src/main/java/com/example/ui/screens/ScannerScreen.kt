package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleTimeframe
import com.example.data.model.ScannerItem
import com.example.data.scanner.ScannerConditionFilter
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
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
    modifier: Modifier = Modifier
) {
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

    val filteredItems = scannerItems.filter { item ->
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
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MULTI-TIMEFRAME MARKET SCANNER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = "Real-Time Anomaly Screener",
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
        }

        if (filteredItems.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No scanner anomalies detected under '$scannerFilter'", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInspectAsset(item.assetId, item.symbol) }
                        .testTag("scanner_card_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header: Symbol, Name, Category, Price, and Score Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.symbol,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NavySurfaceVariant)
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.timeframe,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NexusGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "${item.name} • ${item.category.label}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )

                                if (item.verifiedPrice > 0.0) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$${String.format(Locale.US, "%,.2f", item.verifiedPrice)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val isPositive = item.change24hPercent >= 0.0
                                        Text(
                                            text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", item.change24hPercent)}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isPositive) BullishGreen else BearishRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // AI Technical Score badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(NavySurfaceVariant)
                                    .border(1.dp, if (item.aiScore >= 85) BullishGreen else NexusGold, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NexusGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Score: ${item.aiScore}/100",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (item.aiScore >= 85) BullishGreen else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Primary Detected Condition banner
                        if (item.detectedCondition.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NexusGold.copy(alpha = 0.1f))
                                    .border(1.dp, NexusGold.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TRIGGER: ${item.detectedCondition}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NexusGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = item.lastUpdated,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Technical indicators grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavySurfaceVariant.copy(alpha = 0.6f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("RSI (14)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                                Text(
                                    text = String.format(Locale.US, "%.1f", item.rsi14),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (item.rsi14 <= 30) BullishGreen else if (item.rsi14 >= 70) BearishRed else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Column {
                                Text("MACD STATE", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
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
