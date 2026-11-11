package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.learning.LearningContentDatabase
import com.example.data.learning.LearningLanguageMode
import com.example.data.learning.LessonCategory
import com.example.data.learning.LessonItem
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BearishRedBg
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.BullishGreenBg
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.NexusGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AcademyViewTab(val label: String) {
    ALL_LESSONS("All Lessons"),
    BOOKMARKS("Bookmarks"),
    GLOSSARY_AZ("Glossary A-Z"),
    RECENTLY_VIEWED("Recent")
}

@Composable
fun LearningCenterScreen(
    languageMode: LearningLanguageMode,
    selectedCategory: LessonCategory?,
    searchQuery: String,
    bookmarkedLessonIds: Set<String>,
    completedLessonIds: Set<String>,
    recentlyViewedLessonIds: List<String>,
    selectedLesson: LessonItem?,
    onLanguageModeChange: (LearningLanguageMode) -> Unit,
    onCategorySelect: (LessonCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectLesson: (LessonItem) -> Unit,
    onCloseLessonDetail: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onToggleCompleted: (String) -> Unit,
    onPreviousLesson: () -> Unit,
    onNextLesson: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeViewTab by remember { mutableStateOf(AcademyViewTab.ALL_LESSONS) }
    var selectedGlossaryLetter by remember { mutableStateOf<Char?>(null) }

    val allLessons: List<LessonItem> = remember { LearningContentDatabase.allLessons }
    val totalLessonsCount: Int = allLessons.size
    val completedCount: Int = completedLessonIds.size
    val progressPercent: Float = if (totalLessonsCount > 0) (completedCount.toFloat() / totalLessonsCount.toFloat()) else 0f

    // Filtered list based on view tab, category, search, and letter
    val displayedLessons = remember(
        activeViewTab,
        selectedCategory,
        searchQuery,
        bookmarkedLessonIds,
        recentlyViewedLessonIds,
        selectedGlossaryLetter
    ) {
        val baseList = when (activeViewTab) {
            AcademyViewTab.ALL_LESSONS -> {
                if (selectedCategory != null) {
                    LearningContentDatabase.getLessonsByCategory(selectedCategory)
                } else {
                    allLessons
                }
            }
            AcademyViewTab.BOOKMARKS -> {
                allLessons.filter { bookmarkedLessonIds.contains(it.id) }
            }
            AcademyViewTab.RECENTLY_VIEWED -> {
                recentlyViewedLessonIds.mapNotNull { id -> LearningContentDatabase.findLesson(id) }
            }
            AcademyViewTab.GLOSSARY_AZ -> {
                val sorted = allLessons.sortedBy { it.englishTitle }
                if (selectedGlossaryLetter != null) {
                    sorted.filter { it.englishTitle.startsWith(selectedGlossaryLetter!!, ignoreCase = true) }
                } else {
                    sorted
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            baseList.filter { lesson ->
                lesson.englishTitle.contains(searchQuery, ignoreCase = true) ||
                lesson.romanUrduTitle.contains(searchQuery, ignoreCase = true) ||
                lesson.searchKeywords.any { it.contains(searchQuery, ignoreCase = true) } ||
                lesson.summaryEnglish.contains(searchQuery, ignoreCase = true) ||
                lesson.summaryRomanUrdu.contains(searchQuery, ignoreCase = true)
            }
        } else {
            baseList
        }
    }

    Box(modifier = modifier.fillMaxSize().background(NavyBackground)) {
        if (selectedLesson != null) {
            // Full Detailed Lesson Reader
            LessonDetailView(
                lesson = selectedLesson,
                languageMode = languageMode,
                isBookmarked = bookmarkedLessonIds.contains(selectedLesson.id),
                isCompleted = completedLessonIds.contains(selectedLesson.id),
                onLanguageModeChange = onLanguageModeChange,
                onToggleBookmark = { onToggleBookmark(selectedLesson.id) },
                onToggleCompleted = { onToggleCompleted(selectedLesson.id) },
                onClose = onCloseLessonDetail,
                onPrevious = onPreviousLesson,
                onNext = onNextLesson
            )
        } else {
            // Academy Home / Catalog View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .testTag("learning_center_list")
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    AcademyHeroHeader(
                        completedCount = completedCount,
                        totalCount = totalLessonsCount,
                        progressPercent = progressPercent,
                        languageMode = languageMode,
                        onLanguageModeChange = onLanguageModeChange
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SearchBarSection(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    ViewTabSelector(
                        selectedTab = activeViewTab,
                        bookmarksCount = bookmarkedLessonIds.size,
                        recentCount = recentlyViewedLessonIds.size,
                        onTabSelected = { tab ->
                            activeViewTab = tab
                            if (tab != AcademyViewTab.GLOSSARY_AZ) {
                                selectedGlossaryLetter = null
                            }
                        }
                    )
                }

                // Sub-selectors based on tab
                if (activeViewTab == AcademyViewTab.ALL_LESSONS) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryFilterChips(
                            selectedCategory = selectedCategory,
                            onSelect = onCategorySelect
                        )
                    }
                } else if (activeViewTab == AcademyViewTab.GLOSSARY_AZ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        GlossaryAlphabetSelector(
                            selectedLetter = selectedGlossaryLetter,
                            onLetterSelected = { letter ->
                                selectedGlossaryLetter = if (selectedGlossaryLetter == letter) null else letter
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (activeViewTab) {
                                AcademyViewTab.ALL_LESSONS -> selectedCategory?.title ?: "All Masterclasses"
                                AcademyViewTab.BOOKMARKS -> "Saved Bookmarks (${displayedLessons.size})"
                                AcademyViewTab.GLOSSARY_AZ -> "A-Z Glossary Index (${displayedLessons.size})"
                                AcademyViewTab.RECENTLY_VIEWED -> "Recently Studied (${displayedLessons.size})"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${displayedLessons.size} lessons",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (displayedLessons.isEmpty()) {
                    item {
                        EmptyStateCard(activeViewTab = activeViewTab, searchQuery = searchQuery)
                    }
                } else {
                    items(displayedLessons, key = { it.id }) { lesson ->
                        LessonCardItem(
                            lesson = lesson,
                            languageMode = languageMode,
                            isBookmarked = bookmarkedLessonIds.contains(lesson.id),
                            isCompleted = completedLessonIds.contains(lesson.id),
                            onClick = { onSelectLesson(lesson) },
                            onToggleBookmark = { onToggleBookmark(lesson.id) },
                            onToggleCompleted = { onToggleCompleted(lesson.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun AcademyHeroHeader(
    completedCount: Int,
    totalCount: Int,
    progressPercent: Float,
    languageMode: LearningLanguageMode,
    onLanguageModeChange: (LearningLanguageMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, NexusGold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("academy_hero_card"),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(NexusGold.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Academy",
                            tint = NexusGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NEXUS TRADING ACADEMY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "56 Professional Lessons • 100% Offline",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NexusGold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Language Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Language / Zaban:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LearningLanguageMode.values().forEach { mode ->
                        val isSelected = languageMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NexusGold else NavySurfaceVariant)
                                .clickable { onLanguageModeChange(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("lang_chip_${mode.id.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (mode) {
                                    LearningLanguageMode.ENGLISH -> "English"
                                    LearningLanguageMode.ROMAN_URDU -> "Roman Urdu"
                                    LearningLanguageMode.STANDARD_URDU -> "اردو"
                                    LearningLanguageMode.BILINGUAL -> "Bilingual (Dono)"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) NavyBackground else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Course Progress (Mukammal Karda)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "$completedCount / $totalCount (${(progressPercent * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (progressPercent > 0.8f) BullishGreen else NexusGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NexusGold,
                trackColor = NavySurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("academy_search_input"),
        placeholder = {
            Text(
                text = "Search terms, SMC, indicators, Urdu...",
                color = TextMuted,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NexusGold, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NavySurface,
            unfocusedContainerColor = NavySurface,
            focusedBorderColor = NexusGold,
            unfocusedBorderColor = NavyBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun ViewTabSelector(
    selectedTab: AcademyViewTab,
    bookmarksCount: Int,
    recentCount: Int,
    onTabSelected: (AcademyViewTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AcademyViewTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val label = when (tab) {
                AcademyViewTab.ALL_LESSONS -> "Lessons"
                AcademyViewTab.BOOKMARKS -> if (bookmarksCount > 0) "Saved ($bookmarksCount)" else "Saved"
                AcademyViewTab.GLOSSARY_AZ -> "A-Z Index"
                AcademyViewTab.RECENTLY_VIEWED -> if (recentCount > 0) "Recent ($recentCount)" else "Recent"
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) NexusGold else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp)
                    .testTag("academy_view_tab_${tab.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) NavyBackground else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: LessonCategory?,
    onSelect: (LessonCategory?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("category_filter_row")
    ) {
        item {
            val isSelected = selectedCategory == null
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) NexusGold else NavySurfaceVariant)
                    .border(1.dp, if (isSelected) NexusGold else NavyBorder, RoundedCornerShape(10.dp))
                    .clickable { onSelect(null) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("cat_chip_all"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "All Categories",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) NavyBackground else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            }
        }

        items(LessonCategory.values()) { category ->
            val isSelected = selectedCategory == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) NexusGold else NavySurfaceVariant)
                    .border(1.dp, if (isSelected) NexusGold else NavyBorder, RoundedCornerShape(10.dp))
                    .clickable { onSelect(category) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("cat_chip_${category.id}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${category.letter}. ${category.title}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) NavyBackground else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun GlossaryAlphabetSelector(
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit
) {
    val alphabet = ('A'..'Z').toList()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(alphabet) { letter ->
            val isSelected = selectedLetter == letter
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) NexusGold else NavySurfaceVariant)
                    .clickable { onLetterSelected(letter) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) NavyBackground else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun LessonCardItem(
    lesson: LessonItem,
    languageMode: LearningLanguageMode,
    isBookmarked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleCompleted: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (isCompleted) BullishGreen.copy(alpha = 0.35f) else NavyBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("lesson_card_${lesson.id}"),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Category Badge + Completion check + Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NavySurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${lesson.category.letter}. ${lesson.category.title}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleCompleted,
                        modifier = Modifier.size(32.dp).testTag("btn_complete_${lesson.id}")
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                            contentDescription = if (isCompleted) "Completed" else "Mark Complete",
                            tint = if (isCompleted) BullishGreen else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(32.dp).testTag("btn_bookmark_${lesson.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Bookmarked" else "Bookmark",
                            tint = if (isBookmarked) NexusGold else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title according to language mode
            when (languageMode) {
                LearningLanguageMode.ENGLISH -> {
                    Text(
                        text = lesson.englishTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> {
                    Text(
                        text = lesson.romanUrduTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                LearningLanguageMode.BILINGUAL -> {
                    Text(
                        text = lesson.englishTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Urdu: ${lesson.romanUrduTitle}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            val summaryText = when (languageMode) {
                LearningLanguageMode.ENGLISH -> lesson.summaryEnglish
                LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> lesson.summaryRomanUrdu
                LearningLanguageMode.BILINGUAL -> "${lesson.summaryEnglish}\n\nUrdu: ${lesson.summaryRomanUrdu}"
            }

            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 16.sp
                ),
                maxLines = if (languageMode == LearningLanguageMode.BILINGUAL) 4 else 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer row: read time / tap to read
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3-5 min masterclass",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Read Full Lesson",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = NexusGold,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonDetailView(
    lesson: LessonItem,
    languageMode: LearningLanguageMode,
    isBookmarked: Boolean,
    isCompleted: Boolean,
    onLanguageModeChange: (LearningLanguageMode) -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleCompleted: () -> Unit,
    onClose: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .testTag("lesson_detail_view")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .border(1.dp, NavyBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp).testTag("btn_close_lesson_detail")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NexusGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "MASTERCLASS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "${lesson.category.letter}. ${lesson.category.title}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleCompleted, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                        contentDescription = "Complete",
                        tint = if (isCompleted) BullishGreen else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) NexusGold else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Language Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurface)
                    .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LearningLanguageMode.values().forEach { mode ->
                    val isSelected = languageMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NexusGold else Color.Transparent)
                            .clickable { onLanguageModeChange(mode) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (mode) {
                                LearningLanguageMode.ENGLISH -> "English"
                                LearningLanguageMode.ROMAN_URDU -> "Roman Urdu"
                                LearningLanguageMode.STANDARD_URDU -> "اردو"
                                LearningLanguageMode.BILINGUAL -> "Bilingual"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) NavyBackground else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = lesson.englishTitle,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
            )

            if (languageMode != LearningLanguageMode.ENGLISH) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Roman Urdu: ${lesson.romanUrduTitle}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = NexusGold,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Callout Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NexusGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "QUICK SUMMARY / KHULASA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (languageMode == LearningLanguageMode.ENGLISH || languageMode == LearningLanguageMode.BILINGUAL) {
                        Text(
                            text = lesson.summaryEnglish,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                    if (languageMode == LearningLanguageMode.BILINGUAL) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (languageMode == LearningLanguageMode.ROMAN_URDU || languageMode == LearningLanguageMode.STANDARD_URDU || languageMode == LearningLanguageMode.BILINGUAL) {
                        Text(
                            text = lesson.summaryRomanUrdu,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = NexusGold,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Detailed Explanation
            Text(
                text = "Comprehensive Guide",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (languageMode == LearningLanguageMode.ENGLISH || languageMode == LearningLanguageMode.BILINGUAL) {
                Text(
                    text = lesson.detailedExplanationEnglish,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 22.sp,
                        fontSize = 14.sp
                    )
                )
            }

            if (languageMode == LearningLanguageMode.BILINGUAL) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(NavyBorder)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (languageMode == LearningLanguageMode.ROMAN_URDU || languageMode == LearningLanguageMode.STANDARD_URDU || languageMode == LearningLanguageMode.BILINGUAL) {
                Text(
                    text = "Asaan Roman Urdu Mein Tafseel:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NexusGold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lesson.detailedExplanationRomanUrdu,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        lineHeight = 22.sp,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Practical Real-Market Example
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BullishGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = BullishGreenBg)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Example",
                            tint = BullishGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Practical Market Example / Amli Misaal",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = BullishGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (languageMode) {
                            LearningLanguageMode.ENGLISH -> lesson.practicalExampleEnglish
                            LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> lesson.practicalExampleRomanUrdu ?: lesson.practicalExampleEnglish
                            LearningLanguageMode.BILINGUAL -> "${lesson.practicalExampleEnglish}\n\nUrdu Misaal: ${lesson.practicalExampleRomanUrdu ?: ""}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nexus Pro Tip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NexusGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = NavyCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Pro Tip",
                            tint = NexusGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nexus Pro Tip / Kamyabi Ka Raaz",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = NexusGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (languageMode) {
                            LearningLanguageMode.ENGLISH -> lesson.proTipEnglish
                            LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> lesson.proTipRomanUrdu ?: lesson.proTipEnglish
                            LearningLanguageMode.BILINGUAL -> "${lesson.proTipEnglish}\n\nPro Tip: ${lesson.proTipRomanUrdu ?: ""}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Risk Warning
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BearishRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = BearishRedBg)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = BearishRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Risk Warning / Khatraat Ki Wazahad",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = BearishRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (languageMode) {
                            LearningLanguageMode.ENGLISH -> lesson.riskWarningEnglish ?: "Trading futures and leveraged assets carries high risk of capital loss."
                            LearningLanguageMode.ROMAN_URDU, LearningLanguageMode.STANDARD_URDU -> lesson.riskWarningRomanUrdu ?: (lesson.riskWarningEnglish ?: "Futures aur leverage trading mein nuqsan ka shadeed khatra hota hai.")
                            LearningLanguageMode.BILINGUAL -> "${lesson.riskWarningEnglish ?: "Trading futures carries high risk of loss."}\n\nKhatray Ki Baat: ${lesson.riskWarningRomanUrdu ?: "Leverage trading mein nuqsan ka khatra rehta hai."}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Bottom Navigation Bar (Previous Lesson / Next Lesson)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .border(1.dp, NavyBorder)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurfaceVariant)
                    .clickable(onClick = onPrevious)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("btn_prev_lesson")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = NexusGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Previous",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Quick Complete button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCompleted) BullishGreen else NexusGold)
                    .clickable(onClick = onToggleCompleted)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("btn_complete_bottom")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NavyBackground,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompleted) "Completed" else "Mark Complete",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NavyBackground,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurfaceVariant)
                    .clickable(onClick = onNext)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("btn_next_lesson")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NexusGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = NexusGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(activeViewTab: AcademyViewTab, searchQuery: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = NexusGold.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "No lessons matching '$searchQuery'"
                else when (activeViewTab) {
                    AcademyViewTab.BOOKMARKS -> "No saved bookmarks yet. Tap the bookmark icon on any lesson to save it here!"
                    AcademyViewTab.RECENTLY_VIEWED -> "No recently viewed lessons yet. Start reading any masterclass!"
                    else -> "No lessons found in this section."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 13.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
