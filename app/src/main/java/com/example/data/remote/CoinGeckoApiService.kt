package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CoinGeckoGlobalData(
    val totalMarketCapUsd: String,
    val totalVolume24hUsd: String,
    val btcDominance: String,
    val activeCryptocurrencies: Int,
    val timestamp: Long
)

data class CoinPriceQuote(
    val id: String,
    val price: Double,
    val change24hPercent: Double,
    val volume24hUsd: Double
)

class CoinGeckoApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    suspend fun getGlobalMarketData(): CoinGeckoGlobalData? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.coingecko.com/api/v3/global")
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.code == 429) {
                    Log.w("CoinGecko", "Rate limit reached (429). Using cached/fallback data.")
                    return@withContext null
                }
                if (!response.isSuccessful) return@withContext null

                val bodyStr = response.body?.string() ?: return@withContext null
                val root = JSONObject(bodyStr)
                val data = root.optJSONObject("data") ?: return@withContext null

                val totalCapObj = data.optJSONObject("total_market_cap")
                val totalVolObj = data.optJSONObject("total_volume")
                val marketCapPctObj = data.optJSONObject("market_cap_percentage")

                val totalCap = totalCapObj?.optDouble("usd", 0.0) ?: 0.0
                val totalVol = totalVolObj?.optDouble("usd", 0.0) ?: 0.0
                val btcDom = marketCapPctObj?.optDouble("btc", 0.0) ?: 0.0
                val activeCoins = data.optInt("active_cryptocurrencies", 0)

                val capFormatted = formatLargeCurrency(totalCap)
                val volFormatted = formatLargeCurrency(totalVol)
                val btcDomFormatted = String.format("%.1f%%", btcDom)

                CoinGeckoGlobalData(
                    totalMarketCapUsd = capFormatted,
                    totalVolume24hUsd = volFormatted,
                    btcDominance = btcDomFormatted,
                    activeCryptocurrencies = activeCoins,
                    timestamp = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.w("CoinGecko", "Failed to query CoinGecko global API: ${e.message}")
            null
        }
    }

    suspend fun fetchSimplePrices(
        coinGeckoIds: List<String>
    ): Map<String, CoinPriceQuote> = withContext(Dispatchers.IO) {
        if (coinGeckoIds.isEmpty()) return@withContext emptyMap()
        try {
            val idsParam = coinGeckoIds.joinToString(",")
            val url = "https://api.coingecko.com/api/v3/simple/price?ids=$idsParam&vs_currencies=usd&include_24hr_change=true&include_24hr_vol=true"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyMap()
                val bodyStr = response.body?.string() ?: return@withContext emptyMap()
                val root = JSONObject(bodyStr)
                val result = mutableMapOf<String, CoinPriceQuote>()
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = root.optJSONObject(key) ?: continue
                    val price = obj.optDouble("usd", Double.NaN)
                    val change = obj.optDouble("usd_24h_change", 0.0)
                    val vol = obj.optDouble("usd_24h_vol", 0.0)
                    if (!price.isNaN() && price > 0) {
                        result[key] = CoinPriceQuote(
                            id = key,
                            price = price,
                            change24hPercent = change,
                            volume24hUsd = vol
                        )
                    }
                }
                result
            }
        } catch (e: Exception) {
            Log.w("CoinGecko", "Simple prices fetch error: ${e.message}")
            emptyMap()
        }
    }

    private fun formatLargeCurrency(value: Double): String {
        return when {
            value >= 1_000_000_000_000.0 -> String.format("$%.2f Trillion", value / 1_000_000_000_000.0)
            value >= 1_000_000_000.0 -> String.format("$%.2f Billion", value / 1_000_000_000.0)
            value >= 1_000_000.0 -> String.format("$%.2f Million", value / 1_000_000.0)
            else -> String.format("$%,.2f", value)
        }
    }
}
