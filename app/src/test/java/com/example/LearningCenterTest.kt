package com.example

import com.example.data.learning.LearningContentDatabase
import com.example.data.learning.LearningLanguageMode
import com.example.data.learning.LearningPreferencesRepository
import com.example.data.learning.LessonCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningCenterTest {

    @Test
    fun testTotalLessonCountIsExactly56() {
        val allLessons = LearningContentDatabase.allLessons
        assertEquals("Total lessons must be exactly 56", 56, allLessons.size)
    }

    @Test
    fun testCategoryBreakdownMatchesRequirements() {
        val basics = LearningContentDatabase.getLessonsByCategory(LessonCategory.TRADING_BASICS)
        assertEquals("Trading Basics must have 7 lessons", 7, basics.size)

        val technical = LearningContentDatabase.getLessonsByCategory(LessonCategory.TECHNICAL_ANALYSIS)
        assertEquals("Technical Analysis must have 12 lessons", 12, technical.size)

        val smc = LearningContentDatabase.getLessonsByCategory(LessonCategory.SMART_MONEY_CONCEPTS)
        assertEquals("Smart Money Concepts must have 9 lessons", 9, smc.size)

        val fibonacci = LearningContentDatabase.getLessonsByCategory(LessonCategory.FIBONACCI)
        assertEquals("Fibonacci must have 4 lessons", 4, fibonacci.size)

        val risk = LearningContentDatabase.getLessonsByCategory(LessonCategory.RISK_MANAGEMENT)
        assertEquals("Risk Management must have 7 lessons", 7, risk.size)

        val macro = LearningContentDatabase.getLessonsByCategory(LessonCategory.FUNDAMENTAL_MACRO)
        assertEquals("Fundamental & Macro must have 9 lessons", 9, macro.size)

        val advanced = LearningContentDatabase.getLessonsByCategory(LessonCategory.ADVANCED_TRADING)
        assertEquals("Advanced Trading must have 8 lessons", 8, advanced.size)
    }

    @Test
    fun testAllLessonsHaveCompleteDualLanguageContent() {
        val allLessons = LearningContentDatabase.allLessons
        for (lesson in allLessons) {
            assertTrue("Lesson ${lesson.id} missing English title", lesson.englishTitle.isNotBlank())
            assertTrue("Lesson ${lesson.id} missing Roman Urdu title", lesson.romanUrduTitle.isNotBlank())
            assertTrue("Lesson ${lesson.id} missing English summary", lesson.summaryEnglish.isNotBlank())
            assertTrue("Lesson ${lesson.id} missing Roman Urdu summary", lesson.summaryRomanUrdu.isNotBlank())
            assertTrue("Lesson ${lesson.id} missing English detailed explanation", lesson.detailedExplanationEnglish.isNotBlank())
            assertTrue("Lesson ${lesson.id} missing Roman Urdu detailed explanation", lesson.detailedExplanationRomanUrdu.isNotBlank())
            assertTrue("Lesson ${lesson.id} missing English example", lesson.practicalExampleEnglish.isNotBlank())
            assertTrue("Lesson ${lesson.id} missing English pro tip", lesson.proTipEnglish.isNotBlank())
            lesson.riskWarningEnglish?.let {
                assertTrue("Lesson ${lesson.id} risk warning should not be blank", it.isNotBlank())
            }
        }
    }

    @Test
    fun testSearchFunctionalityInEnglishAndRomanUrdu() {
        val bosResults = LearningContentDatabase.search("BOS")
        assertTrue("Search for BOS should return results", bosResults.isNotEmpty())
        assertEquals("BOS", bosResults.first().termKey)

        val urduResults = LearningContentDatabase.search("Munafa")
        assertTrue("Search for Roman Urdu words should find matching lessons", urduResults.isNotEmpty())

        val fibResults = LearningContentDatabase.search("Golden Pocket")
        assertTrue("Search for Golden Pocket should find lesson", fibResults.any { it.id == "fibonacci_golden_pocket" })

        val fedResults = LearningContentDatabase.search("Federal Reserve")
        assertTrue("Search for Fed should find macro lesson", fedResults.any { it.id == "federal_reserve_macro" })
    }

    @Test
    fun testConversionToSmcGlossaryItem() {
        val lesson = LearningContentDatabase.findLesson("order_blocks") ?: LearningContentDatabase.findLesson("ORDER_BLOCK")
        assertNotNull("Order block lesson should exist", lesson)
        val glossaryItem = lesson!!.toSmcGlossaryItem()
        assertEquals("ORDER_BLOCK", glossaryItem.termKey)
        assertTrue(glossaryItem.title.contains("Order Block"))
        assertTrue(glossaryItem.romanUrduSummary.isNotBlank())
    }

    @Test
    fun testInMemoryPreferencesRepository() {
        val repo = LearningPreferencesRepository(null)

        // Language mode switching
        assertEquals(LearningLanguageMode.BILINGUAL, repo.languageMode.value)
        repo.setLanguageMode(LearningLanguageMode.ROMAN_URDU)
        assertEquals(LearningLanguageMode.ROMAN_URDU, repo.languageMode.value)

        // Bookmarks toggling
        assertFalse(repo.bookmarkedIds.value.contains("bos"))
        repo.toggleBookmark("bos")
        assertTrue(repo.bookmarkedIds.value.contains("bos"))
        repo.toggleBookmark("bos")
        assertFalse(repo.bookmarkedIds.value.contains("bos"))

        // Completion toggling
        assertFalse(repo.completedIds.value.contains("spot_trading"))
        repo.toggleCompleted("spot_trading")
        assertTrue(repo.completedIds.value.contains("spot_trading"))

        // Recently viewed
        repo.recordLessonViewed("spot_trading")
        repo.recordLessonViewed("futures_trading")
        assertEquals(listOf("futures_trading", "spot_trading"), repo.recentlyViewedIds.value)
    }
}
