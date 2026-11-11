package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventStatus
import com.example.data.model.MarketNews
import com.example.data.model.NewsImpact
import com.example.data.model.NewsImpactLevel
import com.example.data.news.EconomicGlossaryDatabase
import com.example.data.news.EconomicTerm
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedBg
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.BullishGreenBg
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexusGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NewsItemCard(
    news: MarketNews,
    onCardClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onTermClick: ((EconomicTerm) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val detectedTerms = remember(news.title) {
        EconomicGlossaryDatabase.detectTermsInText(news.title)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("news_card_${news.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Category, Impact pill, Impact Level, Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavySurfaceVariant)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = news.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NexusGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    // Impact Direction
                    val impactColor = when (news.impact) {
                        NewsImpact.BULLISH -> BullishGreen
                        NewsImpact.BEARISH -> BearishRed
                        NewsImpact.NEUTRAL -> TextMuted
                    }
                    val impactBg = when (news.impact) {
                        NewsImpact.BULLISH -> BullishGreenBg
                        NewsImpact.BEARISH -> BearishRedBg
                        NewsImpact.NEUTRAL -> NavySurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(impactBg)
                            .border(1.dp, impactColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = news.impact.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = impactColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp
                            )
                        )
                    }

                    // Impact Level Pill
                    val levelColor = when (news.impactLevel) {
                        NewsImpactLevel.HIGH -> BearishRed
                        NewsImpactLevel.MEDIUM -> NexusGold
                        NewsImpactLevel.LOW -> TextMuted
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavySurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = news.impactLevel.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = levelColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp
                            )
                        )
                    }

                    // Event status badge if Scheduled or Unverified
                    if (news.eventStatus != EventStatus.CONFIRMED) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavySurfaceVariant)
                                .border(1.dp, NexusGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = news.eventStatus.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NexusGold,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(24.dp).testTag("bookmark_btn_${news.id}")
                ) {
                    Icon(
                        imageVector = if (news.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save News",
                        tint = if (news.isSaved) NexusGold else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            )

            // Detected Economic Terms Roman Urdu trigger row
            if (detectedTerms.isNotEmpty() && onTermClick != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Urdu Wazaahat:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                    detectedTerms.take(2).forEach { term ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NexusGold.copy(alpha = 0.12f))
                                .clickable { onTermClick(term) }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Explain ${term.abbreviation}",
                                tint = NexusGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${term.abbreviation} (i)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NexusGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary
            Text(
                text = news.summary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Affected assets & Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Affected assets pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    news.affectedAssets.take(4).forEach { asset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NavySurfaceVariant.copy(alpha = 0.7f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = asset,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NexusGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Source and Timestamp
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = news.source,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "• ${news.timestamp}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
