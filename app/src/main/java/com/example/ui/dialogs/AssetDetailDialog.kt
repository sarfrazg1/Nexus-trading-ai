package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MarketAsset
import com.example.data.model.MarketDataStatus
import com.example.data.model.Timeframe
import com.example.ui.components.MarketStatusBadge
import com.example.ui.components.MiniSparkline
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexusGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import java.util.Locale

@Composable
fun AssetDetailDialog(
    asset: MarketAsset,
    onDismiss: () -> Unit,
    onSetAlert: (symbol: String) -> Unit,
    onConfigureFeed: () -> Unit,
    onOpenTechnicalAnalysis: (symbol: String) -> Unit = {}
) {
    var selectedTf by remember { mutableStateOf(Timeframe._24H) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("asset_detail_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, NavyBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Close button & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = asset.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${asset.symbol} • ${asset.category.label}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NexusGold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MarketStatusBadge(status = asset.dataStatus)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Timeframe selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Timeframe.values().forEach { tf ->
                        val isSel = selectedTf == tf
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) NexusGold else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable { selectedTf = tf }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tf.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) NavyCard else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price display or Unavailable status
                if (asset.currentPrice != null && asset.dataStatus != MarketDataStatus.UNAVAILABLE) {
                    val isPositive = (asset.change24hPercent ?: 0.0) >= 0.0
                    Text(
                        text = String.format(Locale.US, "$%,.2f", asset.currentPrice),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) BullishGreen else BearishRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(
                                Locale.US,
                                "%s%.2f (%.2f%%)",
                                if (isPositive) "+" else "",
                                asset.change24h ?: 0.0,
                                asset.change24hPercent ?: 0.0
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isPositive) BullishGreen else BearishRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (asset.sparklinePoints.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            MiniSparkline(
                                points = asset.sparklinePoints,
                                isPositive = isPositive,
                                modifier = Modifier.fillMaxWidth().height(80.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Grid: High, Low, Volume, Source, Updated Time
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("24h High:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                            Text(if (asset.high24h != null) String.format(Locale.US, "$%,.2f", asset.high24h) else "N/A", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("24h Low:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                            Text(if (asset.low24h != null) String.format(Locale.US, "$%,.2f", asset.low24h) else "N/A", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("24h Volume:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                            Text(asset.volume24h ?: "N/A", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Data Source:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                            Text(asset.dataSourceName, style = MaterialTheme.typography.bodySmall.copy(color = BullishGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Updated:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                            Text(asset.lastUpdatedFormatted, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                        }
                    }

                } else {
                    // UNAVAILABLE VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurfaceVariant)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.CloudOff, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DATA UNAVAILABLE",
                            style = MaterialTheme.typography.titleMedium.copy(color = WarningOrange, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asset.setupInstructions
                                ?: "Live price streaming requires an active authenticated provider feed for ${asset.name}. Simulated pricing is disabled.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onConfigureFeed,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NexusGold, contentColor = NavyCard)
                        ) {
                            Icon(imageVector = Icons.Default.Cable, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configure Market Feed", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Technical Analysis Button
                Button(
                    onClick = {
                        onDismiss()
                        onOpenTechnicalAnalysis(asset.symbol)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("detail_technical_analysis_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexusGold, contentColor = NavyCard)
                ) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analyze 22 Indicators (${asset.symbol})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Set Price Alert Button
                Button(
                    onClick = {
                        onDismiss()
                        onSetAlert(asset.symbol)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("detail_set_alert_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant, contentColor = NexusGold)
                ) {
                    Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set Price Alert for ${asset.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
