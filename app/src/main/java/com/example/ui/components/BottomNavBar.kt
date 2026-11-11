package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.learning.LearningLanguageMode
import com.example.data.model.NavigationTab
import com.example.data.settings.NexusLocalization
import com.example.ui.theme.NexusTheme

@Composable
fun BottomNavBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    languageMode: LearningLanguageMode = LearningLanguageMode.BILINGUAL,
    modifier: Modifier = Modifier
) {
    val colors = NexusTheme.colors

    val tabs = listOf(
        TabItem(NavigationTab.DASHBOARD, NexusLocalization.tabDashboard(languageMode), Icons.Default.Dashboard),
        TabItem(NavigationTab.MARKETS, NexusLocalization.tabMarkets(languageMode), Icons.Default.ShowChart),
        TabItem(NavigationTab.AI_SIGNALS, NexusLocalization.tabSignals(languageMode), Icons.Default.Bolt),
        TabItem(NavigationTab.NEXUS_AI, NexusLocalization.tabNexusAi(languageMode), Icons.Default.AutoAwesome),
        TabItem(NavigationTab.LEARNING, NexusLocalization.tabAcademy(languageMode), Icons.Default.School),
        TabItem(NavigationTab.SCANNER, NexusLocalization.tabScanner(languageMode), Icons.Default.FilterAlt),
        TabItem(NavigationTab.NEWS, NexusLocalization.tabNews(languageMode), Icons.Default.Article),
        TabItem(NavigationTab.SETTINGS, NexusLocalization.tabSettings(languageMode), Icons.Default.Settings)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .border(width = 1.dp, color = colors.border.copy(alpha = 0.6f))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { item ->
                val isSelected = currentTab == item.tab || (item.tab == NavigationTab.MARKETS && currentTab == NavigationTab.ANALYSIS)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = { onTabSelected(item.tab) })
                        .padding(vertical = 4.dp)
                        .testTag("nav_tab_${item.tab.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.surfaceVariant else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) colors.gold else colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) colors.textPrimary else colors.textMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 9.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private data class TabItem(
    val tab: NavigationTab,
    val label: String,
    val icon: ImageVector
)
