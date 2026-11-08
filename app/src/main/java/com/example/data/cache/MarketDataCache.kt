package com.example.data.cache

import com.example.data.model.MarketAsset
import com.example.data.model.MarketDataStatus
import java.util.concurrent.ConcurrentHashMap

class MarketDataCache {
    private val cache = ConcurrentHashMap<String, MarketAsset>()

    fun put(asset: MarketAsset) {
        cache[asset.id] = asset
    }

    fun putAll(assets: List<MarketAsset>) {
        assets.forEach { cache[it.id] = it }
    }

    fun get(id: String): MarketAsset? = cache[id]

    fun getAll(): List<MarketAsset> = cache.values.toList()

    /**
     * When network is offline or rate limited, returns cached assets with status marked as STALE
     */
    fun markAllAsStale(reason: String = "Network disconnected"): List<MarketAsset> {
        return cache.values.map { asset ->
            if (asset.currentPrice != null && asset.dataStatus != MarketDataStatus.UNAVAILABLE) {
                asset.copy(
                    dataStatus = MarketDataStatus.STALE,
                    dataSourceInfo = "${asset.dataSourceName} (Cached / $reason)"
                )
            } else {
                asset.copy(
                    dataStatus = MarketDataStatus.UNAVAILABLE,
                    dataSourceInfo = "Data unavailable ($reason)"
                )
            }
        }.also { putAll(it) }
    }

    fun clear() {
        cache.clear()
    }
}
