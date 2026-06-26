package com.example.model

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val timestamp: Long = System.currentTimeMillis()
)
