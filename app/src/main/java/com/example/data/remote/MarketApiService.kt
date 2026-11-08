package com.example.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface MarketApiService {
    @GET("api/v3/ticker/24hr")
    suspend fun get24hrTicker(
        @Query("symbol") symbol: String = "BTCUSDT"
    ): BinanceTicker24hr

    @GET("api/v3/ticker/24hr")
    suspend fun get24hrTickers(
        @Query("symbols") symbols: String
    ): List<BinanceTicker24hr>

    @GET("api/v3/ticker/24hr")
    suspend fun getAllTickers(): List<BinanceTicker24hr>

    companion object {
        const val BASE_URL_GLOBAL = "https://api.binance.com/"
        const val BASE_URL_US = "https://api.binance.us/"

        fun create(baseUrl: String = BASE_URL_GLOBAL): MarketApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(MarketApiService::class.java)
        }
    }
}

/**
 * Intelligent client that handles automatic regional failover between Binance Global and Binance US.
 * Overcomes HTTP 451 ("Unavailable For Legal Reasons") geo-blocks transparently.
 */
class BinanceClient(
    private val globalService: MarketApiService = MarketApiService.create(MarketApiService.BASE_URL_GLOBAL),
    private val usService: MarketApiService = MarketApiService.create(MarketApiService.BASE_URL_US)
) {
    var preferUsEndpoint: Boolean = false
        private set

    val activeSourceLabel: String
        get() = if (preferUsEndpoint) "Binance US (Live Feed)" else "Binance Public REST"

    suspend fun get24hrTickers(symbolsJson: String): List<BinanceTicker24hr> {
        if (preferUsEndpoint) {
            return usService.get24hrTickers(symbolsJson)
        }
        return try {
            globalService.get24hrTickers(symbolsJson)
        } catch (e: Exception) {
            if (isGeoBlocked(e)) {
                preferUsEndpoint = true
                Log.i("BinanceClient", "Global Binance blocked (HTTP 451). Switching to Binance US.")
                usService.get24hrTickers(symbolsJson)
            } else {
                throw e
            }
        }
    }

    suspend fun get24hrTicker(symbol: String): BinanceTicker24hr {
        if (preferUsEndpoint) {
            return usService.get24hrTicker(symbol)
        }
        return try {
            globalService.get24hrTicker(symbol)
        } catch (e: Exception) {
            if (isGeoBlocked(e)) {
                preferUsEndpoint = true
                Log.i("BinanceClient", "Global Binance blocked (HTTP 451). Switching to Binance US.")
                usService.get24hrTicker(symbol)
            } else {
                throw e
            }
        }
    }

    private fun isGeoBlocked(e: Exception): Boolean {
        if (e is HttpException && (e.code() == 451 || e.code() == 403)) return true
        val msg = e.message ?: ""
        return msg.contains("451") || msg.contains("restricted location", ignoreCase = true)
    }
}
