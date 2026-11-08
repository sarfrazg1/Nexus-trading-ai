package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.indicators.IndicatorCardItem
import com.example.data.indicators.IndicatorCategory
import com.example.data.indicators.IndicatorStance
import com.example.data.indicators.OverallAnalysisResult
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.ui.theme.CandleGreen
import com.example.ui.theme.CandleRed
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusNeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun TechnicalAnalysisScreen(
    analysis: OverallAnalysisResult?,
    candles: List<Candle>,
    isLoading: Boolean,
    selectedSymbol: String,
    selectedTimeframe: CandleTimeframe,
    selectedCategory: IndicatorCategory,
    errorMessage: String?,
    onSymbolSelected: (String) -> Unit,
    onTimeframeSelected: (CandleTimeframe) -> Unit,
    onCategorySelected: (IndicatorCategory) -> Unit,
    onRefresh: () -> Unit,
    onSetAlert: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val supportedSymbols = listOf(
        "BTC", "ETH", "SOL", "BNB", "XRP", "DOGE", "ADA", "LINK", "AVAX", "SUI",
        "XAU", "XAG", "WTI", "BRENT", "NATGAS"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("technical_analysis_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TOP HEADER & BACK NAVIGATION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(NavySurface, CircleShape)
                            .border(1.dp, NavyBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TECHNICAL ENGINE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(NexusGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "22 INDICATORS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NexusGold
                                )
                            }
                        }
                        Text(
                            text = "Real OHLCV mathematical models",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NexusGold,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(38.dp)
                            .background(NavySurface, CircleShape)
                            .border(1.dp, NavyBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Indicators",
                            tint = NexusGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. ASSET SELECTOR CHIPS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT ASSET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(supportedSymbols) { sym ->
                        val isSelected = sym.equals(selectedSymbol, ignoreCase = true)
                        val isCommodity = sym in listOf("XAU", "XAG", "WTI", "BRENT", "NATGAS")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) NexusGold.copy(alpha = 0.2f)
                                    else NavySurface
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NexusGold else NavyBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSymbolSelected(sym) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCommodity) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(NexusGold, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = sym,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NexusGold else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 12 TIMEFRAMES SELECTOR (1m, 3m, 5m, 15m, 30m, 1h, 2h, 4h, 6h, 12h, 1D, 1W)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIMEFRAME (12 SUPPORTED)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedTimeframe.displayName,
                        fontSize = 11.sp,
                        color = NexusGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(CandleTimeframe.entries) { tf ->
                        val isSelected = tf == selectedTimeframe
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) NexusGold
                                    else NavySurfaceVariant.copy(alpha = 0.7f)
                                )
                                .clickable { onTimeframeSelected(tf) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tf.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NavyBackground else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 4. OVERALL SENTIMENT & CONSENSUS HERO CARD
        item {
            if (analysis != null) {
                SentimentOverviewCard(
                    analysis = analysis,
                    onSetAlert = { onSetAlert(analysis.symbol) }
                )
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    border = BorderStroke(1.dp, CandleRed.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CandleRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Data Feed Alert", fontWeight = FontWeight.Bold, color = CandleRed)
                            Text(errorMessage, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // 5. MINI CANDLESTICK / PRICE ACTION PREVIEW
        if (candles.isNotEmpty()) {
            item {
                CandlestickPreviewCard(
                    candles = candles,
                    symbol = selectedSymbol,
                    timeframe = selectedTimeframe
                )
            }
        }

        // 6. WARNING BANNERS (IF INSUFFICIENT DATA)
        if (analysis != null && analysis.warnings.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    analysis.warnings.forEach { warn ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NexusGold.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .border(1.dp, NexusGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = NexusGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = warn,
                                    fontSize = 11.sp,
                                    color = NexusGold,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. CATEGORY FILTER TABS
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(IndicatorCategory.entries) { cat ->
                    val isSelected = cat == selectedCategory
                    val count = if (analysis != null) {
                        if (cat == IndicatorCategory.ALL) analysis.cards.size
                        else analysis.cards.count { it.category == cat }
                    } else 0

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) NexusGold.copy(alpha = 0.2f)
                                else NavySurface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NexusGold else NavyBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onCategorySelected(cat) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "${cat.label} ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NexusGold else TextSecondary
                        )
                    }
                }
            }
        }

        // 8. INDICATOR CARDS LIST
        if (analysis != null) {
            val filteredCards = if (selectedCategory == IndicatorCategory.ALL) {
                analysis.cards
            } else {
                analysis.cards.filter { it.category == selectedCategory }
            }

            items(filteredCards, key = { it.id }) { card ->
                IndicatorItemCard(card = card)
            }
        } else if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NexusGold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Streaming historical candles & computing 22 indicators...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 9. BOTTOM DISCLAIMER / NOTE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavySurface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, NavyBorder.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NexusGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEXUS MATHEMATICAL INTEGRITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All indicators are strictly calculated from real exchange OHLCV candle feeds on background worker threads. No simulated prices or mock values are ever presented.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

// =========================================================================
// HERO SENTIMENT OVERVIEW CARD
// =========================================================================

@Composable
fun SentimentOverviewCard(
    analysis: OverallAnalysisResult,
    onSetAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, NavyBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Price & Stance Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = analysis.symbol,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${analysis.timeframe.label})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NexusGold
                        )
                    }
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", analysis.currentClose)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = "${if (analysis.priceChangePercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", analysis.priceChangePercent)}% last candle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (analysis.priceChangePercent >= 0) CandleGreen else CandleRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    StanceBadge(stance = analysis.stance)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${analysis.candleCount} candles analyzed",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scoreboard Bar: Bullish vs Neutral vs Bearish
            val total = (analysis.bullishCount + analysis.bearishCount + analysis.neutralCount).coerceAtLeast(1)
            val bullRatio = analysis.bullishCount.toFloat() / total
            val neuRatio = analysis.neutralCount.toFloat() / total
            val bearRatio = analysis.bearishCount.toFloat() / total

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bullish: ${analysis.bullishCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandleGreen
                    )
                    Text(
                        text = "Neutral: ${analysis.neutralCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NexusGold
                    )
                    Text(
                        text = "Bearish: ${analysis.bearishCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandleRed
                    )
                }

                // Visual consensus meter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NavySurface)
                ) {
                    if (bullRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(bullRatio.coerceAtLeast(0.01f))
                                .height(8.dp)
                                .background(CandleGreen)
                        )
                    }
                    if (neuRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(neuRatio.coerceAtLeast(0.01f))
                                .height(8.dp)
                                .background(NexusGold)
                        )
                    }
                    if (bearRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(bearRatio.coerceAtLeast(0.01f))
                                .height(8.dp)
                                .background(CandleRed)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Narrative Summary
            Text(
                text = analysis.summaryText,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onSetAlert,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, NexusGold.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = null,
                    tint = NexusGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Set Price Alert at $${String.format(Locale.US, "%,.2f", analysis.currentClose)}",
                    fontSize = 12.sp,
                    color = NexusGold,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// =========================================================================
// MINI CANDLESTICK CANVAS PREVIEW
// =========================================================================

@Composable
fun CandlestickPreviewCard(
    candles: List<Candle>,
    symbol: String,
    timeframe: CandleTimeframe,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = BorderStroke(1.dp, NavyBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = NexusGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRICE ACTION (${candles.takeLast(30).size} CANDLES)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                val recent = candles.takeLast(30)
                val highest = recent.maxOfOrNull { it.high } ?: 0.0
                val lowest = recent.minOfOrNull { it.low } ?: 0.0
                Text(
                    text = "H: $${String.format(Locale.US, "%.1f", highest)}  L: $${String.format(Locale.US, "%.1f", lowest)}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val displayCandles = remember(candles) { candles.takeLast(32) }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(NavyBackground, RoundedCornerShape(6.dp))
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                if (displayCandles.isEmpty()) return@Canvas

                val maxP = displayCandles.maxOf { it.high }
                val minP = displayCandles.minOf { it.low }
                val priceRange = (maxP - minP).takeIf { it > 0.0 } ?: 1.0

                val candleCount = displayCandles.size
                val slotWidth = size.width / candleCount
                val barWidth = max(2f, slotWidth * 0.65f)

                displayCandles.forEachIndexed { i, c ->
                    val xCenter = i * slotWidth + (slotWidth / 2f)

                    val yHigh = size.height - (((c.high - minP) / priceRange) * size.height).toFloat()
                    val yLow = size.height - (((c.low - minP) / priceRange) * size.height).toFloat()
                    val yOpen = size.height - (((c.open - minP) / priceRange) * size.height).toFloat()
                    val yClose = size.height - (((c.close - minP) / priceRange) * size.height).toFloat()

                    val isBull = c.close >= c.open
                    val candleColor = if (isBull) CandleGreen else CandleRed

                    // Draw wick
                    drawLine(
                        color = candleColor,
                        start = Offset(xCenter, yHigh),
                        end = Offset(xCenter, yLow),
                        strokeWidth = 1.2f
                    )

                    // Draw body
                    val top = min(yOpen, yClose)
                    val bodyHeight = max(2f, kotlin.math.abs(yClose - yOpen))
                    drawRect(
                        color = candleColor,
                        topLeft = Offset(xCenter - barWidth / 2f, top),
                        size = Size(barWidth, bodyHeight)
                    )
                }
            }
        }
    }
}

// =========================================================================
// INDIVIDUAL INDICATOR CARD
// =========================================================================

@Composable
fun IndicatorItemCard(
    card: IndicatorCardItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("indicator_card_${card.id}"),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, NavyBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = card.category.label,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                StanceBadge(stance = card.stance)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Values row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = card.primaryValue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (card.isAvailable) TextPrimary else TextMuted
                )

                card.secondaryValue?.let { sec ->
                    Text(
                        text = sec,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (card.stance) {
                            IndicatorStance.STRONG_BULLISH, IndicatorStance.BULLISH -> CandleGreen
                            IndicatorStance.STRONG_BEARISH, IndicatorStance.BEARISH -> CandleRed
                            IndicatorStance.OVERBOUGHT -> Color(0xFFFF8A65)
                            IndicatorStance.OVERSOLD -> Color(0xFF81D4FA)
                            else -> TextSecondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interpretation box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBackground, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = card.interpretation,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Formula detail footnote
            Text(
                text = card.formulaInfo,
                fontSize = 9.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// =========================================================================
// STANCE BADGE
// =========================================================================

@Composable
fun StanceBadge(
    stance: IndicatorStance,
    modifier: Modifier = Modifier
) {
    val (bg, fg, label) = when (stance) {
        IndicatorStance.STRONG_BULLISH -> Triple(CandleGreen.copy(alpha = 0.2f), CandleGreen, "STRONG BUY")
        IndicatorStance.BULLISH -> Triple(CandleGreen.copy(alpha = 0.15f), CandleGreen, "BULLISH")
        IndicatorStance.STRONG_BEARISH -> Triple(CandleRed.copy(alpha = 0.2f), CandleRed, "STRONG SELL")
        IndicatorStance.BEARISH -> Triple(CandleRed.copy(alpha = 0.15f), CandleRed, "BEARISH")
        IndicatorStance.OVERBOUGHT -> Triple(Color(0xFFFF7043).copy(alpha = 0.2f), Color(0xFFFF8A65), "OVERBOUGHT")
        IndicatorStance.OVERSOLD -> Triple(Color(0xFF29B6F6).copy(alpha = 0.2f), Color(0xFF81D4FA), "OVERSOLD")
        IndicatorStance.NEUTRAL -> Triple(NexusGold.copy(alpha = 0.15f), NexusGold, "NEUTRAL")
        IndicatorStance.INSUFFICIENT_DATA -> Triple(Color.Gray.copy(alpha = 0.15f), Color.LightGray, "NEED DATA")
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(4.dp))
            .border(1.dp, fg.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = fg
        )
    }
}
