package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val asset: String,
    val timeframe: String,
    val timestamp: Long = System.currentTimeMillis(),
    val candleOpen: Double,
    val candleHigh: Double,
    val candleLow: Double,
    val candleClose: Double,
    val candleVolume: Double,
    val rsi: Double,
    val ema9: Double,
    val ema21: Double,
    val macdValue: Double,
    val macdSignal: Double,
    val bbUpper: Double,
    val bbLower: Double,
    val signalType: String, // "BUY", "SELL", "WAIT", "AVOID"
    val confidence: Int,
    val reason: String,
    
    // Screenshot analysis fields
    val imagePath: String? = null,
    val entrySuggestion: String? = null,
    val expirySuggestion: String? = null,
    val riskLevel: String? = null,
    val marketCondition: String? = null,
    val detectedPrice: String? = null,
    val detectedPayout: String? = null,
    val trendScore: Int = 0,
    val candlePatternScore: Int = 0,
    val supportResistanceScore: Int = 0,
    val momentumIndicatorScore: Int = 0,
    val volatilityScore: Int = 0,
    
    // Outcome tracking
    val outcome: String = "PENDING", // "PENDING", "WIN", "LOSS", "DRAW"
    val marketType: String = "Normal", // "Normal", "OTC"
    val trendType: String = "Sideways", // "Uptrend", "Downtrend", "Sideways"
    val isImageClear: Boolean = true,
    val lastCandleColor: String = "Green",
    val isNearSr: String = "None"
)
