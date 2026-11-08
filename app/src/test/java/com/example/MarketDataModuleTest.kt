package com.example

import com.example.data.cache.MarketDataCache
import com.example.data.model.AssetCategory
import com.example.data.model.MarketAsset
import com.example.data.model.MarketDataStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarketDataModuleTest {

    private lateinit var cache: MarketDataCache

    @Before
    fun setUp() {
        cache = MarketDataCache()
    }

    @Test
    fun `test market data cache put and get`() {
        val btc = MarketAsset(
            id = "BTCUSDT",
            name = "Bitcoin",
            symbol = "BTC",
            category = AssetCategory.CRYPTO,
            currentPrice = 96540.50,
            change24h = 2.45,
            high24h = 97200.0,
            low24h = 94300.0,
            volume24h = "$28.45B",
            dataStatus = MarketDataStatus.LIVE,
            dataSourceName = "Binance WebSocket"
        )

        cache.put(btc)
        val retrieved = cache.get("BTCUSDT")

        assertNotNull(retrieved)
        assertEquals("BTC", retrieved?.symbol)
        assertEquals(96540.50, retrieved?.currentPrice ?: 0.0, 0.001)
        assertEquals(MarketDataStatus.LIVE, retrieved?.dataStatus)
        assertEquals("Binance WebSocket", retrieved?.dataSourceName)
    }

    @Test
    fun `test markAllAsStale preserves last price while marking status STALE`() {
        val eth = MarketAsset(
            id = "ETHUSDT",
            name = "Ethereum",
            symbol = "ETH",
            category = AssetCategory.CRYPTO,
            currentPrice = 2850.75,
            change24h = -1.2,
            high24h = 2920.0,
            low24h = 2800.0,
            volume24h = "$12.00B",
            dataStatus = MarketDataStatus.LIVE,
            dataSourceName = "Binance Public REST"
        )

        val unavail = MarketAsset(
            id = "UNAVAIL_ASSET",
            name = "Unknown Asset",
            symbol = "XYZ",
            category = AssetCategory.COMMODITIES,
            currentPrice = null,
            change24h = null,
            high24h = null,
            low24h = null,
            volume24h = null,
            dataStatus = MarketDataStatus.UNAVAILABLE,
            dataSourceName = "External Feed"
        )

        cache.putAll(listOf(eth, unavail))

        val staleList = cache.markAllAsStale(reason = "HTTP 429 Rate Limit")

        val staleEth = staleList.first { it.id == "ETHUSDT" }
        assertEquals(MarketDataStatus.STALE, staleEth.dataStatus)
        assertEquals(2850.75, staleEth.currentPrice ?: 0.0, 0.001)
        assertTrue(staleEth.dataSourceInfo.contains("Rate Limit"))

        val stillUnavail = staleList.first { it.id == "UNAVAIL_ASSET" }
        assertEquals(MarketDataStatus.UNAVAILABLE, stillUnavail.dataStatus)
        assertNull(stillUnavail.currentPrice)
    }

    @Test
    fun `test commodities have appropriate units and delayed or unavailable status`() {
        val gold = MarketAsset(
            id = "XAUUSD",
            name = "Gold Spot",
            symbol = "XAU",
            category = AssetCategory.COMMODITIES,
            currentPrice = 2915.40,
            change24h = 0.85,
            high24h = 2928.0,
            low24h = 2895.0,
            volume24h = null,
            unit = "/oz",
            dataStatus = MarketDataStatus.DELAYED,
            dataSourceName = "COMEX / Yahoo Finance (15m Delay)"
        )

        val wti = MarketAsset(
            id = "CL=F",
            name = "WTI Crude Oil",
            symbol = "WTI",
            category = AssetCategory.COMMODITIES,
            currentPrice = 71.85,
            change24h = -0.42,
            high24h = 72.90,
            low24h = 70.80,
            volume24h = null,
            unit = "/bbl",
            dataStatus = MarketDataStatus.DELAYED,
            dataSourceName = "NYMEX / Yahoo Finance (15m Delay)"
        )

        assertEquals("/oz", gold.unit)
        assertEquals(MarketDataStatus.DELAYED, gold.dataStatus)
        assertEquals("/bbl", wti.unit)
        assertEquals(MarketDataStatus.DELAYED, wti.dataStatus)
    }
}
