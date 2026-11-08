package com.example.data.remote

import android.util.Log
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class CandleApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
) {
    // In-memory cache: "SYMBOL_TIMEFRAME" -> CachedCandles
    private val cache = ConcurrentHashMap<String, CachedCandles>()

    private data class CachedCandles(
        val timestamp: Long,
        val candles: List<Candle>
    )

    private val commodityToYahooMap = mapOf(
        "XAU" to "GC=F",
        "XAUUSD" to "GC=F",
        "GOLD" to "GC=F",
        "XAG" to "SI=F",
        "XAGUSD" to "SI=F",
        "SILVER" to "SI=F",
        "WTI" to "CL=F",
        "OIL" to "CL=F",
        "BRENT" to "BZ=F",
        "NATGAS" to "NG=F"
    )

    suspend fun fetchCandles(
        assetSymbol: String,
        timeframe: CandleTimeframe,
        limit: Int = 250
    ): List<Candle> = withContext(Dispatchers.IO) {
        val normalizedSymbol = assetSymbol.uppercase().trim()
        val cacheKey = "${normalizedSymbol}_${timeframe.label}"

        // Return cache if fresher than 25 seconds
        val cached = cache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 25_000) {
            return@withContext cached.candles
        }

        try {
            val candles = if (commodityToYahooMap.containsKey(normalizedSymbol)) {
                // Commodity: Fetch from Yahoo Finance or fallback to Binance PAXG for Gold
                val yahooTicker = commodityToYahooMap[normalizedSymbol]!!
                val result = fetchYahooCandles(yahooTicker, timeframe)
                if (result.isNotEmpty()) result
                else if (normalizedSymbol in listOf("XAU", "XAUUSD", "GOLD")) {
                    fetchBinanceCandles("PAXGUSDT", timeframe, limit)
                } else emptyList()
            } else {
                // Crypto: Map to Binance USDT pair
                val binancePair = when {
                    normalizedSymbol.endsWith("USDT") -> normalizedSymbol
                    normalizedSymbol == "BTC" -> "BTCUSDT"
                    normalizedSymbol == "ETH" -> "ETHUSDT"
                    normalizedSymbol == "SOL" -> "SOLUSDT"
                    normalizedSymbol == "BNB" -> "BNBUSDT"
                    normalizedSymbol == "XRP" -> "XRPUSDT"
                    normalizedSymbol == "DOGE" -> "DOGEUSDT"
                    normalizedSymbol == "ADA" -> "ADAUSDT"
                    normalizedSymbol == "LINK" -> "LINKUSDT"
                    normalizedSymbol == "AVAX" -> "AVAXUSDT"
                    normalizedSymbol == "TRX" -> "TRXUSDT"
                    normalizedSymbol == "TON" -> "TONUSDT"
                    normalizedSymbol == "LTC" -> "LTCUSDT"
                    normalizedSymbol == "DOT" -> "DOTUSDT"
                    normalizedSymbol == "BCH" -> "BCHUSDT"
                    normalizedSymbol == "SUI" -> "SUIUSDT"
                    else -> "${normalizedSymbol}USDT"
                }
                fetchBinanceCandles(binancePair, timeframe, limit)
            }

            if (candles.isNotEmpty()) {
                cache[cacheKey] = CachedCandles(System.currentTimeMillis(), candles)
            }
            candles
        } catch (e: Exception) {
            Log.e("CandleApiService", "Failed to fetch candles for $normalizedSymbol", e)
            cached?.candles ?: emptyList()
        }
    }

    private fun fetchBinanceCandles(
        symbol: String,
        timeframe: CandleTimeframe,
        limit: Int
    ): List<Candle> {
        val endpoints = listOf(
            "https://api.binance.com/api/v3/klines",
            "https://api.binance.us/api/v3/klines"
        )

        for (base in endpoints) {
            try {
                val url = "$base?symbol=$symbol&interval=${timeframe.binanceInterval}&limit=$limit"
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w("CandleApiService", "$base returned HTTP ${response.code} for $symbol")
                        return@use
                    }
                    val body = response.body?.string() ?: return@use
                    val array = JSONArray(body)
                    val candles = mutableListOf<Candle>()

                    for (i in 0 until array.length()) {
                        val item = array.getJSONArray(i)
                        val openTime = item.getLong(0)
                        val open = item.getString(1).toDoubleOrNull() ?: 0.0
                        val high = item.getString(2).toDoubleOrNull() ?: 0.0
                        val low = item.getString(3).toDoubleOrNull() ?: 0.0
                        val close = item.getString(4).toDoubleOrNull() ?: 0.0
                        val volume = item.getString(5).toDoubleOrNull() ?: 0.0
                        val closeTime = item.getLong(6)
                        val quoteVolume = item.getString(7).toDoubleOrNull() ?: 0.0
                        val tradesCount = item.getInt(8)
                        val takerBuyBase = item.getString(9).toDoubleOrNull() ?: 0.0
                        val takerBuyQuote = item.getString(10).toDoubleOrNull() ?: 0.0

                        candles.add(
                            Candle(
                                openTime = openTime,
                                open = open,
                                high = high,
                                low = low,
                                close = close,
                                volume = volume,
                                closeTime = closeTime,
                                quoteVolume = quoteVolume,
                                tradesCount = tradesCount,
                                takerBuyBaseVolume = takerBuyBase,
                                takerBuyQuoteVolume = takerBuyQuote
                            )
                        )
                    }
                    if (candles.isNotEmpty()) {
                        return candles
                    }
                }
            } catch (e: Exception) {
                Log.w("CandleApiService", "Failed $base for $symbol: ${e.message}")
            }
        }

        // Seamless fallback to Yahoo Finance crypto chart if Binance is geo-restricted
        val baseAsset = symbol.removeSuffix("USDT")
        val yahooTicker = "$baseAsset-USD"
        val yahooCandles = fetchYahooCandles(yahooTicker, timeframe)
        if (yahooCandles.isNotEmpty()) {
            return yahooCandles
        }

        return emptyList()
    }

    private fun fetchYahooCandles(
        ticker: String,
        timeframe: CandleTimeframe
    ): List<Candle> {
        val url = "https://query1.finance.yahoo.com/v8/finance/chart/$ticker?interval=${timeframe.yahooInterval}&range=${timeframe.yahooRange}"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val root = JSONObject(body)
            val chart = root.optJSONObject("chart") ?: return emptyList()
            val resultArr = chart.optJSONArray("result") ?: return emptyList()
            if (resultArr.length() == 0) return emptyList()

            val first = resultArr.getJSONObject(0)
            val timestamps = first.optJSONArray("timestamp") ?: return emptyList()
            val indicators = first.optJSONObject("indicators") ?: return emptyList()
            val quoteArr = indicators.optJSONArray("quote") ?: return emptyList()
            if (quoteArr.length() == 0) return emptyList()

            val quoteObj = quoteArr.getJSONObject(0)
            val opens = quoteObj.optJSONArray("open")
            val highs = quoteObj.optJSONArray("high")
            val lows = quoteObj.optJSONArray("low")
            val closes = quoteObj.optJSONArray("close")
            val volumes = quoteObj.optJSONArray("volume")

            val candles = mutableListOf<Candle>()
            for (i in 0 until timestamps.length()) {
                val t = timestamps.getLong(i) * 1000L
                val o = opens?.optDouble(i, Double.NaN) ?: Double.NaN
                val h = highs?.optDouble(i, Double.NaN) ?: Double.NaN
                val l = lows?.optDouble(i, Double.NaN) ?: Double.NaN
                val c = closes?.optDouble(i, Double.NaN) ?: Double.NaN
                val v = volumes?.optDouble(i, 0.0) ?: 0.0

                if (!o.isNaN() && !h.isNaN() && !l.isNaN() && !c.isNaN() && c > 0) {
                    candles.add(
                        Candle(
                            openTime = t,
                            open = o,
                            high = h,
                            low = l,
                            close = c,
                            volume = v
                        )
                    )
                }
            }
            return candles
        }
    }
}
