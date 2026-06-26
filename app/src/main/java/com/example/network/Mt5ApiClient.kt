package com.example.network

import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Mt5StatusResponse(
    val status: String, // "connected", "error"
    val message: String
)

@JsonClass(generateAdapter = true)
data class Mt5SymbolsResponse(
    val symbols: List<String>
)

@JsonClass(generateAdapter = true)
data class Mt5CandleResponse(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val tick_volume: Double,
    val time: Long
)

@JsonClass(generateAdapter = true)
data class Mt5AnalysisResponse(
    val signal: String, // "CALL", "PUT", "WAIT", "AVOID"
    val confidence: Int,
    val trend: String,
    val support: Double,
    val resistance: Double,
    val rsi: Double,
    val ema9: Double,
    val ema21: Double,
    val ema50: Double,
    val macdValue: Double,
    val macdSignal: Double,
    val atr: Double,
    val reason: String,
    val riskLevel: String // "Low", "Medium", "High"
)

interface Mt5ApiService {
    @GET("mt5/status")
    suspend fun getStatus(): Mt5StatusResponse

    @GET("mt5/symbols")
    suspend fun getSymbols(): Mt5SymbolsResponse

    @GET("mt5/candles")
    suspend fun getCandles(
        @Query("symbol") symbol: String,
        @Query("timeframe") timeframe: String,
        @Query("candle_count") candleCount: Int
    ): List<Mt5CandleResponse>

    @GET("mt5/analyze")
    suspend fun analyze(
        @Query("symbol") symbol: String,
        @Query("timeframe") timeframe: String,
        @Query("candle_count") candleCount: Int
    ): Mt5AnalysisResponse
}

object Mt5ApiClient {
    private const val TAG = "Mt5ApiClient"
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private var currentBaseUrl: String? = null
    private var cachedService: Mt5ApiService? = null

    @Synchronized
    fun getApiService(baseUrl: String): Mt5ApiService {
        // Sanitize trailing slash
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        if (cachedService != null && currentBaseUrl == sanitizedUrl) {
            return cachedService!!
        }

        Log.d(TAG, "Creating new Mt5ApiService for base URL: $sanitizedUrl")
        currentBaseUrl = sanitizedUrl
        
        val retrofit = Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(Mt5ApiService::class.java)
        cachedService = service
        return service
    }
}
