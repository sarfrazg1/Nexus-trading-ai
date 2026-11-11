package com.example.ui.dialogs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.learning.LearningContentDatabase
import com.example.data.learning.LearningLanguageMode
import com.example.data.smc.SmcGlossaryItem
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedBg
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexusGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Unified Educational Dialog serving all technical term info clicks throughout NEXUS AI.
 * Supports on-the-fly toggling between English, Roman Urdu, and Bilingual modes,
 * and seamlessly links into the complete Learning Center.
 */
@Composable
fun RomanUrduGlossaryDialog(
    item: SmcGlossaryItem,
    initialLanguageMode: LearningLanguageMode = LearningLanguageMode.BILINGUAL,
    onOpenFullLesson: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var activeLang by remember { mutableStateOf(initialLanguageMode) }

    // Look up enriched lesson if present in database
    val matchingLesson = remember(item.termKey) {
        LearningContentDatabase.findLesson(item.termKey)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, NexusGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("roman_urdu_glossary_dialog"),
            colors = CardDefaults.cardColors(containerColor = NavyCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header: Icon + Category Badge + Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NexusGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Education",
                                tint = NexusGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Box(
                                modifier = Modifier
                                    .background(NexusGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = (matchingLesson?.category?.title ?: item.category).uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NexusGold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = matchingLesson?.let {
                                    when (activeLang) {
                                        LearningLanguageMode.ENGLISH -> it.titleEnglish
                                        LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> it.titleRomanUrdu
                                        LearningLanguageMode.BILINGUAL -> "${it.titleEnglish} • ${it.titleRomanUrdu}"
                                    }
                                } ?: item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(NavySurface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Language Mode Selector Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavySurface)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LearningLanguageMode.entries.forEach { mode ->
                        val isSel = activeLang == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) NexusGold else NavySurface)
                                .clickable { activeLang = mode }
                                .padding(vertical = 6.dp)
                                .testTag("glossary_lang_${mode.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) NavyBackground else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Short Summary Card
                val summaryText = matchingLesson?.let {
                    when (activeLang) {
                        LearningLanguageMode.ENGLISH -> it.summaryEnglish
                        LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> it.summaryRomanUrdu
                        LearningLanguageMode.BILINGUAL -> "${it.summaryEnglish}\n\n[Roman Urdu]: ${it.summaryRomanUrdu}"
                    }
                } ?: item.romanUrduSummary

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavySurface, RoundedCornerShape(12.dp))
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NexusGold,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detailed Explanation
                Text(
                    text = when (activeLang) {
                        LearningLanguageMode.ENGLISH -> "In-Depth Explanation:"
                        LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> "Wazaahat (Tafseel):"
                        LearningLanguageMode.BILINGUAL -> "Detailed Explanation / Tafseel:"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                val detailText = matchingLesson?.let {
                    when (activeLang) {
                        LearningLanguageMode.ENGLISH -> it.detailEnglish
                        LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> it.detailRomanUrdu
                        LearningLanguageMode.BILINGUAL -> "${it.detailEnglish}\n\n━━━━━━━━━━━━━━━━━━━━\n[Roman Urdu]:\n${it.detailRomanUrdu}"
                    }
                } ?: item.romanUrduDetail

                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary.copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                // Practical Real-World Example (if available from database)
                if (matchingLesson != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = when (activeLang) {
                            LearningLanguageMode.ENGLISH -> "Practical Example:"
                            LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> "Amali Misaal (Example):"
                            LearningLanguageMode.BILINGUAL -> "Practical Example / Amali Misaal:"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val exampleText = when (activeLang) {
                        LearningLanguageMode.ENGLISH -> matchingLesson.practicalExampleEnglish
                        LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> matchingLesson.practicalExampleRomanUrdu
                        LearningLanguageMode.BILINGUAL -> "${matchingLesson.practicalExampleEnglish}\n\n[Urdu]: ${matchingLesson.practicalExampleRomanUrdu}"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavySurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = exampleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pro Trading Tip
                val tipText = matchingLesson?.let {
                    when (activeLang) {
                        LearningLanguageMode.ENGLISH -> it.proTipEnglish
                        LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> it.proTipRomanUrdu
                        LearningLanguageMode.BILINGUAL -> "${it.proTipEnglish}\n\n[Urdu Tip]: ${it.proTipRomanUrdu}"
                    }
                } ?: item.tradingTipRomanUrdu

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexusGold.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .border(1.dp, NexusGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Trading Tip",
                            tint = NexusGold,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pro Trading Tip:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NexusGold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tipText,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Risk Warning (if applicable)
                if (matchingLesson?.riskWarningEnglish != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BearishRedBg, RoundedCornerShape(10.dp))
                            .border(1.dp, BearishRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Risk Warning",
                                tint = BearishRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (activeLang) {
                                    LearningLanguageMode.ENGLISH -> matchingLesson.riskWarningEnglish
                                    LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> matchingLesson.riskWarningRomanUrdu ?: matchingLesson.riskWarningEnglish
                                    LearningLanguageMode.BILINGUAL -> "${matchingLesson.riskWarningEnglish} | ${matchingLesson.riskWarningRomanUrdu ?: ""}"
                                },
                                color = TextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Buttons
                if (matchingLesson != null && onOpenFullLesson != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenFullLesson(matchingLesson.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("open_in_learning_center_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = NexusGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Study Full Lesson in Academy",
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("dismiss_glossary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NexusGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Samajh Gaya (Got It)",
                        color = NavyBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
