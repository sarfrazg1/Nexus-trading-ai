package com.example.data.watchlist

import android.content.Context
import android.content.SharedPreferences

class WatchlistRepository(private val context: Context? = null) {

    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val inMemoryFavorites = mutableSetOf(
        "btc", "eth", "sol", "gold", "sp500"
    )

    fun getFavoriteAssetIds(): Set<String> {
        val stored = prefs?.getStringSet(KEY_FAVORITES, null)
        return if (stored != null) {
            stored.toSet()
        } else {
            inMemoryFavorites.toSet()
        }
    }

    fun isFavorite(assetId: String): Boolean {
        val normalizedId = assetId.lowercase().trim()
        return getFavoriteAssetIds().contains(normalizedId)
    }

    fun toggleFavorite(assetId: String): Boolean {
        val normalizedId = assetId.lowercase().trim()
        val current = getFavoriteAssetIds().toMutableSet()
        val newState = if (current.contains(normalizedId)) {
            current.remove(normalizedId)
            false
        } else {
            current.add(normalizedId)
            true
        }

        inMemoryFavorites.clear()
        inMemoryFavorites.addAll(current)

        prefs?.edit()?.putStringSet(KEY_FAVORITES, current)?.apply()
        return newState
    }

    fun setFavorite(assetId: String, isFav: Boolean) {
        val normalizedId = assetId.lowercase().trim()
        val current = getFavoriteAssetIds().toMutableSet()
        if (isFav) {
            current.add(normalizedId)
        } else {
            current.remove(normalizedId)
        }

        inMemoryFavorites.clear()
        inMemoryFavorites.addAll(current)

        prefs?.edit()?.putStringSet(KEY_FAVORITES, current)?.apply()
    }

    companion object {
        private const val PREFS_NAME = "nexus_watchlist_prefs"
        private const val KEY_FAVORITES = "favorite_asset_ids"
    }
}
