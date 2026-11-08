package com.example.data.remote

import android.util.Log
import com.example.data.model.MarketDataStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CommodityQuote(
    val id: String,
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val high24h: Double?,
    val low24h: Double?,
    val volume: String?,
    val status: MarketDataStatus,
    val sourceName: String,
    val timestamp: Long,
    val sparkline: List<Float>
)

class CommoditiesApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) {
    // Tickers on Yahoo Finance public chart API
    private val commoditySymbols = mapOf(
        "XAU" to "GC=F",   // Gold Futures
        "XAG" to "SI=F",   // Silver Futures
        "WTI" to "CL=F",   // WTI Crude Oil
        "BRENT" to "BZ=F", // Brent Crude Oil
        "NATGAS" to "NG=F" // Natural Gas
    )

    suspend fun fetchCommodityQuotes(
        userApiKey: String = ""
    ): Map<String, CommodityQuote> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, CommodityQuote>()

        // If user provided a TwelveData key, attempt to query TwelveData
        if (userApiKey.isNotBlank() && userApiKey != "Public (No Key Required)") {
            tryTwelveData(userApiKey, results)
        }

        // For any remaining commodities, try legitimate free public delayed quotes
        for ((id, ticker) in commoditySymbols) {
            if (results.containsKey(id)) continue
            val quote = fetchPublicYahooQuote(id, ticker)
            if (quote != null) {
                results[id] = quote
            }
        }

        results
    }

    private fun fetchPublicYahooQuote(id: String, ticker: String): CommodityQuote? {
        try {
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$ticker?interval=1d&range=5d"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .header("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val root = JSONObject(body)
                val chart = root.optJSONObject("chart") ?: return null
                val resultArr = chart.optJSONArray("result") ?: return null
                if (resultArr.length() == 0) return null

                val first = resultArr.getJSONObject(0)
                val meta = first.optJSONObject("meta") ?: return null

                val price = meta.optDouble("regularMarketPrice", Double.NaN)
                if (price.isNaN() || price <= 0.0) return null

                val prevClose = meta.optDouble("previousClose", meta.optDouble("chartPreviousClose", price))
                val change = price - prevClose
                val changePct = if (prevClose > 0) (change / prevClose) * 100.0 else 0.0
                val high = meta.optDouble("regularMarketDayHigh", Double.NaN).takeIf { !it.isNaN() }
                val low = meta.optDouble("regularMarketDayLow", Double.NaN).takeIf { !it.isNaN() }
                val volRaw = meta.optLong("regularMarketVolume", 0L)
                val volStr = if (volRaw > 0) formatVolume(volRaw, id) else null

                // Extract sparkline points from indicators if available
                val sparkline = mutableListOf<Float>()
                val indicators = first.optJSONObject("indicators")
                val quoteArr = indicators?.optJSONArray("quote")
                if (quoteArr != null && quoteArr.length() > 0) {
                    val closes = quoteArr.getJSONObject(0).optJSONArray("close")
                    if (closes != null) {
                        for (i in 0 until closes.length()) {
                            val c = closes.optDouble(i, Double.NaN)
                            if (!c.isNaN()) sparkline.add(c.toFloat())
                        }
                    }
                }

                val (symbolDisplay, nameDisplay) = when (id) {
                    "XAU" -> "XAU/USD" to "Gold Spot (COMEX)"
                    "XAG" -> "XAG/USD" to "Silver Spot (COMEX)"
                    "WTI" -> "WTI/USD" to "WTI Crude Oil"
                    "BRENT" -> "BRENT/USD" to "Brent Crude Oil"
                    "NATGAS" -> "NG/USD" to "Natural Gas"
                    else -> id to id
                }

                return CommodityQuote(
                    id = id,
                    symbol = symbolDisplay,
                    name = nameDisplay,
                    price = price,
                    change = change,
                    changePercent = changePct,
                    high24h = high,
                    low24h = low,
                    volume = volStr,
                    status = MarketDataStatus.DELAYED,
                    sourceName = "NYMEX/COMEX (Exchange Delayed)",
                    timestamp = System.currentTimeMillis(),
                    sparkline = sparkline
                )
            }
        } catch (e: Exception) {
            Log.w("CommoditiesAPI", "Failed to fetch public quote for $id: ${e.message}")
            return null
        }
    }

    private fun tryTwelveData(apiKey: String, outMap: MutableMap<String, CommodityQuote>) {
        try {
            val url = "https://api.twelvedata.com/price?symbol=XAU/USD,XAG/USD,WTI/USD,BRENT/USD,NG/USD&apikey=$apiKey"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return
                val body = response.body?.string() ?: return
                val root = JSONObject(body)
                // If TwelveData returns valid data, parse and set status LIVE
                val xauObj = root.optJSONObject("XAU/USD")
                if (xauObj != null) {
                    val price = xauObj.optString("price").toDoubleOrNull()
                    if (price != null && price > 0) {
                        outMap["XAU"] = CommodityQuote(
                            id = "XAU",
                            symbol = "XAU/USD",
                            name = "Gold Spot",
                            price = price,
                            change = 0.0,
                            changePercent = 0.0,
                            high24h = null,
                            low24h = null,
                            volume = null,
                            status = MarketDataStatus.LIVE,
                            sourceName = "TwelveData API (Live)",
                            timestamp = System.currentTimeMillis(),
                            sparkline = emptyList()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("CommoditiesAPI", "TwelveData attempt failed: ${e.message}")
        }
    }

    private fun formatVolume(vol: Long, id: String): String {
        val unit = when (id) {
            "XAU", "XAG" -> "oz"
            "WTI", "BRENT" -> "bbl"
            "NATGAS" -> "MMBtu"
            else -> ""
        }
        return when {
            vol >= 1_000_000 -> String.format("%.1fM %s", vol / 1_000_000.0, unit).trim()
            vol >= 1_000 -> String.format("%.1fK %s", vol / 1_000.0, unit).trim()
            else -> "$vol $unit".trim()
        }
    }
}
