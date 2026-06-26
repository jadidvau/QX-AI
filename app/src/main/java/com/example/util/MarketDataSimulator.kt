package com.example.util

import com.example.model.Candle
import java.util.Random

object MarketDataSimulator {

    private val random = Random()

    fun getBasePrice(asset: String): Double {
        return when (asset) {
            "EUR/USD" -> 1.0852
            "GBP/USD" -> 1.2685
            "USD/JPY" -> 156.42
            "BTC/USD" -> 67450.0
            "ETH/USD" -> 3520.0
            "XAU/USD (Gold)" -> 2335.0
            else -> 100.0
        }
    }

    fun getPriceStep(asset: String): Double {
        return when (asset) {
            "EUR/USD", "GBP/USD" -> 0.0001
            "USD/JPY", "XAU/USD (Gold)" -> 0.01
            else -> 1.0 // BTC/USD, ETH/USD
        }
    }

    fun generateCandleHistory(asset: String, count: Int, volatility: String): List<Candle> {
        val basePrice = getBasePrice(asset)
        val spreadPercent = when (volatility) {
            "Low" -> 0.0005
            "High" -> 0.004
            "Extreme (News)" -> 0.012
            else -> 0.0015 // "Normal"
        }

        val list = mutableListOf<Candle>()
        var currentPrice = basePrice * (0.98 + random.nextDouble() * 0.04) // Add some variation to start price
        
        for (i in 1..count) {
            val open = currentPrice
            val change = open * spreadPercent * random.nextGaussian()
            val close = open + change
            
            val upperMargin = open * (spreadPercent * 0.6) * random.nextDouble()
            val high = maxOf(open, close) + upperMargin
            
            val lowerMargin = open * (spreadPercent * 0.6) * random.nextDouble()
            val low = minOf(open, close) - lowerMargin
            
            val volume = 5000.0 + random.nextDouble() * 45000.0

            list.add(Candle(
                open = open,
                high = high,
                low = low,
                close = close,
                volume = volume,
                timestamp = System.currentTimeMillis() - (count - i) * 60000 // stagger minutes back
            ))
            currentPrice = close
        }
        return list
    }

    fun generateNextCandle(previousClose: Double, asset: String, volatility: String): Candle {
        val spreadPercent = when (volatility) {
            "Low" -> 0.0005
            "High" -> 0.004
            "Extreme (News)" -> 0.012
            else -> 0.0015 // "Normal"
        }

        val open = previousClose
        val change = open * spreadPercent * random.nextGaussian()
        val close = open + change
        
        val upperMargin = open * (spreadPercent * 0.6) * random.nextDouble()
        val high = maxOf(open, close) + upperMargin
        
        val lowerMargin = open * (spreadPercent * 0.6) * random.nextDouble()
        val low = minOf(open, close) - lowerMargin
        
        val volume = 5000.0 + random.nextDouble() * 45000.0

        return Candle(
            open = open,
            high = high,
            low = low,
            close = close,
            volume = volume,
            timestamp = System.currentTimeMillis()
        )
    }
}
