package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CombinedSignalResult(
    val signal: String, // "BUY", "SELL", "WAIT", "AVOID"
    val confidence: Int, // Combined confidence score (0 to 100)
    val agreementStatus: String, // "Matched", "Conflict", "Weak", "No MT5 Data"
    val explanation: String,
    val qxWeightScore: Double = 0.0,
    val mt5WeightScore: Double = 0.0,
    val historyWeightScore: Double = 0.0
)
