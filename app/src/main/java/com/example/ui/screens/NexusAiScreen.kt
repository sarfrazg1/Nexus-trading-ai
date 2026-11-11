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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.MessageSender
import com.example.data.ai.NexusChatMessage
import com.example.data.ai.NexusConversation
import com.example.data.learning.LearningLanguageMode
import com.example.data.settings.NexusLocalization
import com.example.data.settings.UrduFontSize
import com.example.ui.theme.NotoNastaliqUrduFontFamily
import com.example.ui.theme.NexusTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NexusAiScreen(
    messages: List<NexusChatMessage>,
    conversations: List<NexusConversation>,
    currentConversationId: String,
    isLoading: Boolean,
    isOnlineAiAvailable: Boolean,
    currentAssetSymbol: String,
    currentAssetPrice: Double,
    languageMode: LearningLanguageMode,
    urduFontSize: UrduFontSize = UrduFontSize.MEDIUM,
    onSendMessage: (String) -> Unit,
    onNewChat: () -> Unit,
    onClearChat: () -> Unit,
    onDeleteConversation: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onAttachCurrentMarket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = NexusTheme.colors
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    // Scroll to latest message when messages change
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isUrduScript = languageMode == LearningLanguageMode.STANDARD_URDU
    val isBilingual = languageMode == LearningLanguageMode.BILINGUAL
    val urduScale = urduFontSize.scaleFactor

    val suggestedQuestions = remember(languageMode) {
        when (languageMode) {
            LearningLanguageMode.ENGLISH -> listOf(
                "Analyze Current Market",
                "Why Is Bitcoin Moving?",
                "Compare Timeframes",
                "Explain Current Signal",
                "Analyze CPI and Fed News",
                "Explain Technical Indicators",
                "Check Trading Risk",
                "Teach Me Trading"
            )
            LearningLanguageMode.ROMAN_URDU -> listOf(
                "Current Market Analyze Karein",
                "BTC Neeche Kyun Gir Raha Hai?",
                "Timeframes Ka Mawazna Karein",
                "Mojooda Signal Samjhayein",
                "CPI Aur Fed News Ka Asar",
                "Indicators Ki Wazahat Karein",
                "Trading Risk Check Karein",
                "Mujhe Trading Sikhayein"
            )
            LearningLanguageMode.STANDARD_URDU -> listOf(
                "موجودہ مارکیٹ کا تجزیہ کریں",
                "بٹ کوائن نیچے کیوں جا رہا ہے؟",
                "ٹائم فریمز کا موازنہ کریں",
                "موجودہ سگنل کی وضاحت کریں",
                "سی پی آئی اور فیڈ نیوز کا اثر",
                "تکنیکی اشارے سمجھائیں",
                "تجارتی رسک کا جائزہ لیں",
                "مجھے ٹریڈنگ سکھائیں"
            )
            LearningLanguageMode.BILINGUAL -> listOf(
                "Analyze Market (مارکیٹ تجزیہ)",
                "Why Is BTC Moving? (بٹ کوائن کیوں متحرک ہے؟)",
                "Compare Timeframes (ٹائم فریم موازنہ)",
                "Explain Signal (سگنل کی وضاحت)",
                "Analyze CPI & Fed (سی پی آئی اور فیڈ ڈیٹا)",
                "Check Risk (رسک مینجمنٹ)"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top Header Bar
        Surface(
            color = colors.card,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.gold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "NEXUS AI",
                                tint = colors.gold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NEXUS AI",
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnlineAiAvailable) colors.bullishGreen else colors.gold)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnlineAiAvailable) "Gemini AI Online • Hybrid" else "Offline Rule Engine Active",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(
                            onClick = onNewChat,
                            modifier = Modifier.testTag("ai_new_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Chat",
                                tint = colors.textSecondary
                            )
                        }
                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.testTag("ai_clear_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Chat",
                                tint = colors.textSecondary
                            )
                        }
                    }
                }

                // Active Market Context Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Asset: $currentAssetSymbol • $${String.format(java.util.Locale.US, "%.2f", currentAssetPrice)}",
                        color = colors.gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Nastaleeq RTL: ${if (isUrduScript) "Active" else "Ready"}",
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Messages List or Welcome Screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                // Welcome Screen with suggested questions
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(colors.gold.copy(alpha = 0.12f))
                                .border(1.dp, colors.gold.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isUrduScript) "نیکسس اے آئی میں خوش آمدید" else "Welcome to NEXUS AI",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = (20 * urduScale).sp,
                            fontFamily = if (isUrduScript) NotoNastaliqUrduFontFamily else FontFamily.Default
                        )
                        Text(
                            text = if (isUrduScript) "آپ کا ذاتی اور خودمختار ٹریڈنگ اینالسٹ" else "Your Personal AI Trading Analyst",
                            color = colors.textSecondary,
                            fontSize = (13 * urduScale).sp,
                            fontFamily = if (isUrduScript) NotoNastaliqUrduFontFamily else FontFamily.Default
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        // Context Attachment Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.card,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = colors.bullishGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isUrduScript) "لائیو مارکیٹ ڈیٹا سے منسلک" else "Connected to Live NEXUS Engines",
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isUrduScript) 
                                        "ٹیکنیکل انڈیکیٹرز، اسمارٹ منی کانسپٹس، فیبوناچی اور نیوز اینالائزر سے تصدیق شدہ ڈیٹا استعمال ہوتا ہے۔"
                                    else 
                                        "Understands Technical Analysis (22 indicators), SMC (BOS, CHoCH, FVGs), Fibonacci, and macroeconomic news.",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = if (isUrduScript) NotoNastaliqUrduFontFamily else FontFamily.Default
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (isUrduScript) "تجویز کردہ سوالات:" else "Suggested Questions:",
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(suggestedQuestions) { question ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.card,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.8f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSendMessage(question)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = colors.gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = question,
                                    color = colors.textPrimary,
                                    fontSize = (13 * urduScale).sp,
                                    fontFamily = if (isUrduScript || (isBilingual && question.contains("[\u0600-\u06FF]".toRegex()))) 
                                        NotoNastaliqUrduFontFamily else FontFamily.Default
                                )
                            }
                        }
                    }
                }
            } else {
                // Scrollable Chat Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatMessageItem(
                            message = msg,
                            languageMode = languageMode,
                            urduFontSize = urduFontSize,
                            onCopy = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("NEXUS AI Response", text)
                                clipboard.setPrimaryClip(clip)
                            },
                            onFollowUpClick = { followUp ->
                                onSendMessage(followUp)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = colors.gold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isUrduScript) "نیکسس تجزیہ کار مارکیٹ کا جائزہ لے رہا ہے..." else "Analyzing verified market context...",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = if (isUrduScript) NotoNastaliqUrduFontFamily else FontFamily.Default
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Field & Action Bar
        Surface(
            color = colors.card,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick context pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.gold.copy(alpha = 0.1f))
                            .clickable { onAttachCurrentMarket() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Attach Market Data: $currentAssetSymbol",
                            color = colors.gold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "100% Verified Feeds",
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isUrduInput = inputText.any { it in '\u0600'..'\u06FF' }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isUrduScript) "مارکیٹ، سگنلز یا معاشی خبروں کے متعلق پوچھیں..." 
                                else if (languageMode == LearningLanguageMode.ROMAN_URDU) "Market ya signal ke baray mein sawal poochein..."
                                else "Ask about market, signals, SMC or news...",
                                color = colors.textSecondary.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontFamily = if (isUrduScript) NotoNastaliqUrduFontFamily else FontFamily.Default
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.gold,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_input_text_field"),
                        maxLines = 4,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = (14 * urduScale).sp,
                            fontFamily = if (isUrduInput || isUrduScript) NotoNastaliqUrduFontFamily else FontFamily.Default,
                            textAlign = if (isUrduInput || isUrduScript) TextAlign.Right else TextAlign.Left
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val query = inputText
                                inputText = ""
                                onSendMessage(query)
                            }
                        })
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val query = inputText
                                inputText = ""
                                onSendMessage(query)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isLoading) colors.gold else colors.border.copy(alpha = 0.4f))
                            .testTag("ai_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isLoading) Color.Black else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: NexusChatMessage,
    languageMode: LearningLanguageMode,
    urduFontSize: UrduFontSize,
    onCopy: (String) -> Unit,
    onFollowUpClick: (String) -> Unit
) {
    val colors = NexusTheme.colors
    val isUser = message.sender == MessageSender.USER
    val containsUrduCharacters = message.text.any { it in '\u0600'..'\u06FF' }
    val useNastaleeq = containsUrduCharacters || languageMode == LearningLanguageMode.STANDARD_URDU
    val urduScale = urduFontSize.scaleFactor

    // Proper RTL Layout direction when text contains Urdu script
    val layoutDir = if (containsUrduCharacters) LayoutDirection.Rtl else LayoutDirection.Ltr

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender label + Offline Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = if (isUser) "You" else "NEXUS AI",
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isUser && message.isOfflineEngine) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.gold.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Rule-Based Analysis",
                        color = colors.gold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Message Bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) colors.gold.copy(alpha = 0.2f) else colors.card,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isUser) colors.gold.copy(alpha = 0.5f) else colors.border
            ),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Attached context badge if present
                message.attachedContextSummary?.let { summary ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.background)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "Market Context: $summary",
                            color = colors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Main Text with RTL CompositionLocalProvider when Urdu
                CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                    Text(
                        text = message.text,
                        color = colors.textPrimary,
                        fontSize = (14 * urduScale).sp,
                        lineHeight = (22 * urduScale).sp,
                        fontFamily = if (useNastaleeq) NotoNastaliqUrduFontFamily else FontFamily.Default,
                        textAlign = if (containsUrduCharacters) TextAlign.Right else TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bottom Action: Copy response
                if (!isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onCopy(message.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Response",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Suggested Follow-up chips
        if (!isUser && message.suggestedFollowUps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Column(modifier = Modifier.padding(start = 4.dp)) {
                message.suggestedFollowUps.forEach { followUp ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.background,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable { onFollowUpClick(followUp) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = colors.gold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = followUp,
                                color = colors.gold,
                                fontSize = (11 * urduScale).sp,
                                fontFamily = if (followUp.any { it in '\u0600'..'\u06FF' }) NotoNastaliqUrduFontFamily else FontFamily.Default
                            )
                        }
                    }
                }
            }
        }
    }
}
