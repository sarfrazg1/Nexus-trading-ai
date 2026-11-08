package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BinanceTicker24hr(
    @Json(name = "symbol") val symbol: String = "",
    @Json(name = "lastPrice") val lastPrice: String = "0.0",
    @Json(name = "priceChange") val priceChange: String = "0.0",
    @Json(name = "priceChangePercent") val priceChangePercent: String = "0.0",
    @Json(name = "highPrice") val highPrice: String = "0.0",
    @Json(name = "lowPrice") val lowPrice: String = "0.0",
    @Json(name = "volume") val volume: String = "0.0",
    @Json(name = "quoteVolume") val quoteVolume: String = "0.0",
    @Json(name = "openTime") val openTime: Long = 0L,
    @Json(name = "closeTime") val closeTime: Long = 0L
)

data class LiveTickerUpdate(
    val symbol: String,
    val lastPrice: Double,
    val priceChangePercent: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Double,
    val quoteVolume: Double,
    val timestamp: Long
)
