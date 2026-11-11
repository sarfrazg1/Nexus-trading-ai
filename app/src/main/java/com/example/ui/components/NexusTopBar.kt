package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketDataStatus
import com.example.ui.theme.NexusTheme
import com.example.ui.theme.WarningOrange

@Composable
fun NexusTopBar(
    isWebSocketLive: Boolean,
    btcStatus: MarketDataStatus,
    activeAlertsCount: Int,
    isRefreshing: Boolean,
    isLowPerformanceMode: Boolean = false,
    onRefreshClick: () -> Unit,
    onAddAlertClick: () -> Unit,
    onOpenAnalysisClick: () -> Unit = {},
    onOpenLearningClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = NexusTheme.colors

    // In Low Performance Mode, skip heavy continuous spin animation to save CPU and battery
    val rotation by if (isRefreshing && !isLowPerformanceMode) {
        val infiniteTransition = rememberInfiniteTransition(label = "spin")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(900, easing = LinearEasing)),
            label = "spin_angle"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .border(width = 1.dp, color = colors.border.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Identity
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.gold)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Nexus AI",
                        tint = colors.card,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NEXUS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TRADING AI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = colors.gold,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    // Feed Status pill
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusDotColor = when {
                            isWebSocketLive -> colors.bullishGreen
                            btcStatus == MarketDataStatus.LIVE -> colors.bullishGreen
                            btcStatus == MarketDataStatus.DELAYED -> colors.gold
                            btcStatus == MarketDataStatus.STALE -> WarningOrange
                            else -> colors.bearishRed
                        }

                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = when {
                                isWebSocketLive -> "Binance WebSocket: Live"
                                btcStatus == MarketDataStatus.LIVE -> "Binance REST: Live"
                                btcStatus == MarketDataStatus.DELAYED -> "Feed Delayed"
                                btcStatus == MarketDataStatus.STALE -> "Data Stale / Offline"
                                else -> "Stream Disconnected"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Academy / Learning Center button
                IconButton(
                    onClick = onOpenLearningClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .testTag("topbar_learning_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Trading Academy",
                        tint = colors.gold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Technical Analysis button
                IconButton(
                    onClick = onOpenAnalysisClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .testTag("topbar_analysis_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Technical Analysis",
                        tint = colors.gold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Add alert button
                IconButton(
                    onClick = onAddAlertClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .testTag("topbar_add_alert_btn")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "New Alert",
                            tint = colors.gold,
                            modifier = Modifier.size(18.dp)
                        )
                        if (activeAlertsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(colors.bullishGreen)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Manual Refresh button
                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .testTag("topbar_refresh_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Manual Refresh",
                        tint = colors.textPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(rotation)
                    )
                }
            }
        }
    }
}
