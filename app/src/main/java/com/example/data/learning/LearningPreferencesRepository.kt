package com.example.data.learning

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 100% Offline, lightweight persistent storage for:
 * - Selected Language Mode (English, Roman Urdu, Bilingual)
 * - Bookmarked lessons
 * - Completed lessons / Progress tracking
 * - Recently viewed lessons
 */
class LearningPreferencesRepository(context: Context? = null) {

    private var prefs: SharedPreferences? = context?.getSharedPreferences("nexus_learning_preferences", Context.MODE_PRIVATE)

    private val _languageMode = MutableStateFlow(loadLanguageMode())
    val languageMode: StateFlow<LearningLanguageMode> = _languageMode.asStateFlow()

    private val _bookmarkedIds = MutableStateFlow(loadStringSet("bookmarked_lessons"))
    val bookmarkedIds: StateFlow<Set<String>> = _bookmarkedIds.asStateFlow()

    private val _completedIds = MutableStateFlow(loadStringSet("completed_lessons"))
    val completedIds: StateFlow<Set<String>> = _completedIds.asStateFlow()

    private val _recentlyViewedIds = MutableStateFlow(loadRecentlyViewed())
    val recentlyViewedIds: StateFlow<List<String>> = _recentlyViewedIds.asStateFlow()

    fun bindContext(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("nexus_learning_preferences", Context.MODE_PRIVATE)
            _languageMode.value = loadLanguageMode()
            _bookmarkedIds.value = loadStringSet("bookmarked_lessons")
            _completedIds.value = loadStringSet("completed_lessons")
            _recentlyViewedIds.value = loadRecentlyViewed()
        }
    }

    fun setLanguageMode(mode: LearningLanguageMode) {
        prefs?.edit()?.putString("language_mode", mode.id)?.apply()
        _languageMode.value = mode
    }

    fun toggleBookmark(lessonId: String) {
        val current = _bookmarkedIds.value.toMutableSet()
        if (current.contains(lessonId)) {
            current.remove(lessonId)
        } else {
            current.add(lessonId)
        }
        prefs?.edit()?.putStringSet("bookmarked_lessons", current)?.apply()
        _bookmarkedIds.value = current
    }

    fun toggleCompleted(lessonId: String) {
        val current = _completedIds.value.toMutableSet()
        if (current.contains(lessonId)) {
            current.remove(lessonId)
        } else {
            current.add(lessonId)
        }
        prefs?.edit()?.putStringSet("completed_lessons", current)?.apply()
        _completedIds.value = current
    }

    fun recordLessonViewed(lessonId: String) {
        val current = _recentlyViewedIds.value.toMutableList()
        current.remove(lessonId)
        current.add(0, lessonId) // Place at front
        val trimmed = current.take(15) // Keep last 15
        val joined = trimmed.joinToString(",")
        prefs?.edit()?.putString("recently_viewed", joined)?.apply()
        _recentlyViewedIds.value = trimmed
    }

    private fun loadLanguageMode(): LearningLanguageMode {
        val id = prefs?.getString("language_mode", "BI") ?: "BI"
        return when (id) {
            "EN" -> LearningLanguageMode.ENGLISH
            "RU" -> LearningLanguageMode.ROMAN_URDU
            else -> LearningLanguageMode.BILINGUAL
        }
    }

    private fun loadStringSet(key: String): Set<String> {
        return prefs?.getStringSet(key, emptySet()) ?: emptySet()
    }

    private fun loadRecentlyViewed(): List<String> {
        val raw = prefs?.getString("recently_viewed", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }
}
