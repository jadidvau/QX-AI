package com.example.network

import android.util.Log
import com.example.BuildConfig
import com.example.util.SignalAnalysis
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: ResponseSchema? = null,
    val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    val type: String,
    val properties: Map<String, SchemaProperty>? = null,
    val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class SchemaProperty(
    val type: String,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    private fun android.graphics.Bitmap.toBase64(): String {
        val outputStream = java.io.ByteArrayOutputStream()
        compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    /**
     * Sends a user-uploaded chart screenshot to Gemini to analyze visually,
     * performs OCR, candlestick reading, technical and indicator assessment,
     * and returns a detailed structured SignalAnalysis.
     */
    suspend fun analyzeScreenshotWithGemini(
        bitmap: android.graphics.Bitmap,
        manualAsset: String?,
        manualTimeframe: String?,
        manualLastCandleColor: String?,
        manualTrend: String?,
        manualSrTouch: String?
    ): SignalAnalysis? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            Log.d(TAG, "Gemini API key is not configured for Vision. Falling back to local/mock analysis.")
            return null
        }

        val prompt = """
            Analyze the attached screenshot of a trading chart (typically Quotex / Market QX / binary options platform chart).
            
            Perform the following operations:
            1. **OCR Text Extraction**: Look carefully for text labels on the screen:
               - Asset name (e.g., EUR/USD, GBP/USD OTC, Crypto, etc.)
               - Timeframe (e.g., M1, 1m, M5, 5m, 15m)
               - Current Price (the current active price quote)
               - Timer / Candle Expiry (countdowns like 00:45, 14:22, etc.)
               - Payout percentage (e.g., 85%, 92%)
            2. **Visual Candlestick Reading**: Analyze the candlestick pattern:
               - Colors (Green/Bullish vs Red/Bearish)
               - Wick lengths and body sizes
               - Last 10-30 visible candles
               - Support & resistance zones (where price touches and bounces or gets rejected)
               - Breakout or rejection zones
               - Trend direction (Upward, Downward, Sideways)
            3. **Technical Logic & Indicators**:
               - Identify visible indicators (RSI line, Bollinger Bands, EMAs, MACD) if present, and read their signals.
               - If no indicators are visible, analyze purely based on candle price action and support/resistance.
            4. **Manual User Input Overrides (if provided)**:
               - Manual Asset Selection: ${manualAsset ?: "Not specified"}
               - Manual Timeframe Selection: ${manualTimeframe ?: "Not specified"}
               - Manual Last Candle Color: ${manualLastCandleColor ?: "Not specified"}
               - Manual Trend: ${manualTrend ?: "Not specified"}
               - Manual Support/Resistance Touch: ${manualSrTouch ?: "Not specified"}
               Combine these manual inputs with what is visually detected. Manual inputs should override or refine any visual ambiguity.
               
            **SCORING SYSTEM**:
            Assess and output these integer scores (0 to 25 each):
            - `trendScore`: Strength of trend alignment (0 to 25)
            - `candlePatternScore`: Strength of candlestick pattern (e.g. Hammer, Shooting Star, Engulfing) (0 to 25)
            - `supportResistanceScore`: Proximity and strength of S/R rejection or breakout (0 to 25)
            - `momentumIndicatorScore`: Indicator alignment or price action momentum strength (0 to 25)
            
            The total sum of these four scores must equal the `confidence` score (0 to 100).
            
            **SIGNAL DECISION RULES**:
            - Must return exactly one of: 'BUY' (for CALL), 'SELL' (for PUT), 'WAIT' (for NO TRADE), 'AVOID' (for AVOID MARKET).
            - If the image is unclear, blurry, cropped badly, or candles are not readable, you MUST output signal = 'WAIT', confidence = 0, and in the reason state: "Image unclear, upload a clearer chart screenshot."
            - If confidence is below 60%, you MUST output signal = 'WAIT'.
            - If candles give mixed signals (bullish indicators conflict with bearish indicators), you MUST output signal = 'WAIT'.
            - If the market is too volatile or has extreme price jumps, you MUST output signal = 'AVOID'.
            - If confidence is 60–74%, marketCondition must be 'Weak signal'.
            - If confidence is 75–84%, marketCondition must be 'Trending' or 'Ranging' (Strong Signal).
            - If confidence is above 85%, marketCondition must be 'Trending' or 'Ranging' (Very Strong Signal).
            - SUGGESTED EXPIRY:
              - 1 minute chart: 1-2 minute expiry
              - 5 minute chart: 5-10 minute expiry
              - 15 minute chart: 15-30 minute expiry
              - Otherwise: None or reasonable default.
            
            **SAFE TRADING CONSTRAINTS**:
            - NEVER state 100% confidence. Never promise guaranteed profits.
            - NEVER say "sure win".
            - NEVER encourage unlimited leverage or unsafe trading sizes.
            
            Provide your response strictly in the specified JSON schema.
        """.trimIndent()

        val systemInstruction = """
            You are Market QX AI, a professional binary options and financial chart screenshot analyzer.
            You must reply ONLY with a valid JSON object matching the requested schema.
            Do not provide financial advice, and do NOT guarantee profit.
        """.trimIndent()

        val properties = mapOf(
            "signal" to SchemaProperty("STRING", "Must be exactly one of: 'BUY', 'SELL', 'WAIT', 'AVOID'"),
            "confidence" to SchemaProperty("INTEGER", "An integer score from 0 to 100 indicating the total score of indicators. Never give 100%."),
            "reason" to SchemaProperty("STRING", "Bulleted list of reasons for why this signal was determined based on visual and manual analysis."),
            "entry" to SchemaProperty("STRING", "Suggested entry. One of: 'Now', 'Wait for next candle', 'No entry'"),
            "expiry" to SchemaProperty("STRING", "Suggested expiry based on timeframe (e.g. 1-2 minutes, 5-10 minutes, 15-30 minutes, or None)"),
            "riskLevel" to SchemaProperty("STRING", "One of: 'Low', 'Medium', 'High'"),
            "marketCondition" to SchemaProperty("STRING", "One of: 'Trending', 'Ranging', 'Volatile', 'Weak signal'"),
            "detectedAsset" to SchemaProperty("STRING", "Asset pair name extracted via OCR or 'Unknown'"),
            "detectedTimeframe" to SchemaProperty("STRING", "Timeframe extracted via OCR or 'Unknown'"),
            "detectedPrice" to SchemaProperty("STRING", "Current price extracted via OCR or 'Unknown'"),
            "detectedExpiry" to SchemaProperty("STRING", "Candle/timer expiry extracted via OCR or 'Unknown'"),
            "detectedPayout" to SchemaProperty("STRING", "Payout percentage extracted via OCR or 'Unknown'"),
            "trendScore" to SchemaProperty("INTEGER", "Trend score from 0 to 25"),
            "candlePatternScore" to SchemaProperty("INTEGER", "Candle pattern score from 0 to 25"),
            "supportResistanceScore" to SchemaProperty("INTEGER", "Support/resistance score from 0 to 25"),
            "momentumIndicatorScore" to SchemaProperty("INTEGER", "Momentum/indicator score from 0 to 25")
        )

        val responseSchema = ResponseSchema(
            type = "OBJECT",
            properties = properties,
            required = listOf(
                "signal", "confidence", "reason", "entry", "expiry", "riskLevel", "marketCondition",
                "detectedAsset", "detectedTimeframe", "detectedPrice", "detectedExpiry", "detectedPayout",
                "trendScore", "candlePatternScore", "supportResistanceScore", "momentumIndicatorScore"
            )
        )

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = bitmap.toBase64()))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = responseSchema,
                temperature = 0.15
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val nonNullJson: String = jsonText
                moshi.adapter(SignalAnalysis::class.java).fromJson(nonNullJson)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini vision API request failed: ${e.message}", e)
            null
        }
    }

    /**
     * Sends candle data & indicators to Gemini to analyze.
     * Returns SignalAnalysis or null if it fails.
     */
    suspend fun analyzeWithGemini(
        asset: String,
        timeframe: String,
        volatility: String,
        candleOpen: Double,
        candleHigh: Double,
        candleLow: Double,
        candleClose: Double,
        rsi: Double,
        ema9: Double,
        ema21: Double,
        macdValue: Double,
        macdSignal: Double,
        bbUpper: Double,
        bbLower: Double,
        trend: String,
        pattern: String
    ): SignalAnalysis? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            Log.d(TAG, "Gemini API key is not configured. Falling back to local rule engine.")
            return null
        }

        val prompt = """
            Asset: $asset
            Timeframe: $timeframe
            Volatility Level: $volatility
            
            Current Candle Metrics:
            - Open: $candleOpen
            - High: $candleHigh
            - Low: $candleLow
            - Close: $candleClose
            
            Calculated Technical Indicators:
            - RSI (14): $rsi
            - EMA (9): $ema9
            - EMA (21): $ema21
            - MACD Value: $macdValue, Signal: $macdSignal
            - Bollinger Bands: Upper=$bbUpper, Lower=$bbLower
            - Primary Trend: $trend
            - Detected Candlestick Pattern: $pattern
            
            Based on these metrics, provide an educational trading analysis.
            Remember:
            - Do NOT promise guaranteed profits. Do NOT state 100% accuracy.
            - Weigh the signals from the technical indicators (RSI, EMAs, MACD, Bollinger Bands) and the detected candlestick pattern.
            - If indicators suggest opposing actions (e.g., some suggest BUY while others suggest SELL), you MUST default to a 'WAIT' signal, or an 'AVOID' signal if market volatility is High or Extreme.
            - If market volatility is Extreme or Extreme (News), you MUST output signal = 'AVOID'.
            - In the 'reason' field, provide a clear, educational explanation of your combined reasoning, detailing how you weighed the indicators and resolved any conflicts. Format with clean, structured markdown.
        """.trimIndent()

        val systemInstruction = """
            You are Market QX AI, an educational financial assistant. You analyze candlestick data and technical indicators to provide educational trading insights only.
            You must reply ONLY with a valid JSON object matching the requested schema.
            Do not provide financial advice, and do NOT guarantee profit.
        """.trimIndent()

        // Create response schema
        val properties = mapOf(
            "signal" to SchemaProperty("STRING", "Must be exactly one of: 'BUY' (CALL), 'SELL' (PUT), 'WAIT' (NO TRADE), 'AVOID' (AVOID TRADE)"),
            "confidence" to SchemaProperty("INTEGER", "An integer score from 0 to 100 indicating the strength/congruence of the indicators. Never give 100%."),
            "reason" to SchemaProperty("STRING", "Clear, educational explanation of why this signal was determined based on the interaction of the indicators.")
        )
        val responseSchema = ResponseSchema(
            type = "OBJECT",
            properties = properties,
            required = listOf("signal", "confidence", "reason")
        )

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = responseSchema,
                temperature = 0.2
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val nonNullJson: String = jsonText
                moshi.adapter(SignalAnalysis::class.java).fromJson(nonNullJson)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API request failed: ${e.message}", e)
            null
        }
    }
}
