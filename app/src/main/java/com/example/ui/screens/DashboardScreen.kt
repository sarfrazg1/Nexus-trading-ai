package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiFeedConfig
import com.example.data.model.MarketAlert
import com.example.data.model.MarketAsset
import com.example.data.model.MarketOverview
import com.example.data.model.NavigationTab
import com.example.data.model.Signal
import com.example.ui.components.AlertItemCard
import com.example.ui.components.PriceTickerCard
import com.example.ui.components.SentimentGauge
import com.example.ui.components.SignalItemCard
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexusGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange

@Composable
fun DashboardScreen(
    assets: List<MarketAsset>,
    overview: MarketOverview,
    signals: List<Signal>,
    alerts: List<MarketAlert>,
    apiConfigs: List<ApiFeedConfig>,
    onAssetClick: (MarketAsset) -> Unit,
    onConnectFeedClick: (MarketAsset) -> Unit,
    onSetAlertClick: (symbol: String) -> Unit,
    onToggleAlert: (alertId: String) -> Unit,
    onDeleteAlert: (alertId: String) -> Unit,
    onViewAllSignals: () -> Unit,
    onNavigateTab: (NavigationTab) -> Unit,
    onCopySignalLevels: (Signal) -> Unit,
    modifier: Modifier = Modifier
) {
    // Primary assets for dashboard sections
    val btcAsset = assets.firstOrNull { it.id == "BTC" } ?: assets.firstOrNull()
    val goldAsset = assets.firstOrNull { it.id == "XAU" }
    val silverAsset = assets.firstOrNull { it.id == "XAG" }

    // Energy assets: WTI, Brent, Natural Gas
    var selectedEnergyId by remember { mutableStateOf("WTI") }
    val energyAsset = assets.firstOrNull { it.id == selectedEnergyId }
        ?: assets.firstOrNull { it.id == "WTI" }
        ?: assets.firstOrNull { it.id == "OIL" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Dashboard Title & Overview Banner
            MarketOverviewHeader(overview = overview, onConfigureFeeds = { onNavigateTab(NavigationTab.SETTINGS) })
        }

        // 1. Bitcoin Price Section
        item {
            SectionHeader(
                title = "BITCOIN PRICE SECTION",
                subtitle = "Cryptocurrency Market Anchor • Real-Time Stream",
                icon = Icons.Default.Timeline
            )
            Spacer(modifier = Modifier.height(8.dp))
            btcAsset?.let { asset ->
                PriceTickerCard(
                    asset = asset,
                    onCardClick = { onAssetClick(asset) },
                    onConnectFeedClick = { onConnectFeedClick(asset) },
                    onSetAlertClick = { onSetAlertClick(asset.symbol) }
                )
            }
        }

        // 2. Gold Price Section
        item {
            SectionHeader(
                title = "GOLD PRICE SECTION (XAUUSD)",
                subtitle = "Precious Metal & Sovereign Reserve Benchmark",
                icon = Icons.Default.AutoAwesome
            )
            Spacer(modifier = Modifier.height(8.dp))
            goldAsset?.let { asset ->
                PriceTickerCard(
                    asset = asset,
                    onCardClick = { onAssetClick(asset) },
                    onConnectFeedClick = { onConnectFeedClick(asset) },
                    onSetAlertClick = { onSetAlertClick(asset.symbol) }
                )
            }
        }

        // 3. Silver Price Section
        item {
            SectionHeader(
                title = "SILVER PRICE SECTION (XAGUSD)",
                subtitle = "Industrial Demand & Monetary Hybrid",
                icon = Icons.Default.Speed
            )
            Spacer(modifier = Modifier.height(8.dp))
            silverAsset?.let { asset ->
                PriceTickerCard(
                    asset = asset,
                    onCardClick = { onAssetClick(asset) },
                    onConnectFeedClick = { onConnectFeedClick(asset) },
                    onSetAlertClick = { onSetAlertClick(asset.symbol) }
                )
            }
        }

        // 4. Crude Oil & Energy Price Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "CRUDE OIL & ENERGY SECTION",
                    subtitle = "WTI, Brent Crude & Natural Gas Benchmarks",
                    icon = Icons.Default.Speed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Energy benchmark selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("WTI" to "WTI Crude", "BRENT" to "Brent Crude", "NATGAS" to "Nat Gas").forEach { (id, label) ->
                    val isSel = selectedEnergyId == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) NexusGold else NavySurfaceVariant)
                            .clickable { selectedEnergyId = id }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSel) NavyCard else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            energyAsset?.let { asset ->
                PriceTickerCard(
                    asset = asset,
                    onCardClick = { onAssetClick(asset) },
                    onConnectFeedClick = { onConnectFeedClick(asset) },
                    onSetAlertClick = { onSetAlertClick(asset.symbol) }
                )
            }
        }

        // 5. Market Sentiment
        item {
            SectionHeader(
                title = "MARKET SENTIMENT & VOLATILITY",
                subtitle = "Multi-Model Algorithmic Index",
                icon = Icons.Default.Speed
            )
            Spacer(modifier = Modifier.height(8.dp))
            SentimentGauge(
                fearAndGreedIndex = overview.fearAndGreedIndex,
                sentimentLabel = overview.sentimentLabel,
                bullishPercent = overview.bullishAssetsPercent,
                volatilityState = overview.volatilityState
            )
        }

        // 6. Recent Signals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "RECENT AI SIGNALS",
                    subtitle = "High-Conviction Quantitative Alerts",
                    icon = Icons.Default.AutoAwesome
                )

                OutlinedButton(
                    onClick = onViewAllSignals,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusGold),
                    modifier = Modifier.testTag("dashboard_view_all_signals_btn")
                ) {
                    Text("View All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(signals.take(2)) { signal ->
            SignalItemCard(
                signal = signal,
                onSetAlertClick = { onSetAlertClick(signal.assetSymbol) },
                onCopyLevelsClick = { onCopySignalLevels(signal) }
            )
        }

        // 7. Recent Alerts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "PRICE ALERTS",
                    subtitle = "Active Triggers & Thresholds",
                    icon = Icons.Default.NotificationsActive
                )

                Button(
                    onClick = { onSetAlertClick("BTC/USDT") },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexusGold, contentColor = NavyCard),
                    modifier = Modifier.testTag("dashboard_add_alert_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (alerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No active price alerts set. Tap 'New Alert' to add one.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(alerts.take(3)) { alert ->
                AlertItemCard(
                    alert = alert,
                    onToggle = { onToggleAlert(alert.id) },
                    onDelete = { onDeleteAlert(alert.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MarketOverviewHeader(
    overview: MarketOverview,
    onConfigureFeeds: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("market_overview_header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(NexusGold.copy(alpha = 0.4f), NavyBorder))
        )
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
                Column {
                    Text(
                        text = "EXECUTIVE TRADING TERMINAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = "NEXUS Core Intelligence Engine",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = overview.marketStatus,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BullishGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Macro metrics row from CoinGecko & Binance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceVariant.copy(alpha = 0.6f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Crypto Market Cap", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text(
                        text = overview.totalCryptoMarketCap,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                }
                Column {
                    Text("24h Volume", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text(
                        text = overview.cryptoVolume24h,
                        style = MaterialTheme.typography.bodyMedium.copy(color = NexusGold, fontWeight = FontWeight.Bold)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("BTC Dominance", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp))
                    Text(
                        text = overview.btcDominance,
                        style = MaterialTheme.typography.bodyMedium.copy(color = BullishGreen, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${overview.connectedFeedsCount} Active Feeds • ${overview.dataProviderSummary}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp),
                    maxLines = 1
                )
                Text(
                    text = "Feeds Setup →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NexusGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.clickable { onConfigureFeeds() }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(NavySurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NexusGold, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 10.sp
                )
            )
        }
    }
}
