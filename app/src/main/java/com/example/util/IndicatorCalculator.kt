package com.example.util

import com.example.model.Candle
import kotlin.math.sqrt

data class IndicatorResults(
    val rsi: Double,
    val ema9: Double,
    val ema21: Double,
    val macdValue: Double,
    val macdSignal: Double,
    val macdHist: Double,
    val bbUpper: Double,
    val bbMiddle: Double,
    val bbLower: Double,
    val support: Double,
    val resistance: Double,
    val trend: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val candlestickPattern: String // "None", "Hammer", "Shooting Star", "Doji", "Bullish Engulfing", "Bearish Engulfing"
)

object IndicatorCalculator {

    fun calculate(candles: List<Candle>): IndicatorResults {
        if (candles.isEmpty()) {
            return IndicatorResults(50.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "NEUTRAL", "None")
        }

        val closes = candles.map { it.close }
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }

        val lastClose = closes.last()

        // 1. EMA 9 and EMA 21
        val ema9 = calculateEMA(closes, 9).lastOrNull() ?: lastClose
        val ema21 = calculateEMA(closes, 21).lastOrNull() ?: lastClose

        // 2. RSI 14
        val rsi = calculateRSI(closes, 14).lastOrNull() ?: 50.0

        // 3. MACD
        val ema12List = calculateEMA(closes, 12)
        val ema26List = calculateEMA(closes, 26)
        val macdLine = mutableListOf<Double>()
        val minSize = minOf(ema12List.size, ema26List.size)
        for (i in 0 until minSize) {
            val idx12 = ema12List.size - minSize + i
            val idx26 = ema26List.size - minSize + i
            macdLine.add(ema12List[idx12] - ema26List[idx26])
        }
        val macdValue = macdLine.lastOrNull() ?: 0.0
        val macdSignalList = calculateEMA(macdLine, 9)
        val macdSignal = macdSignalList.lastOrNull() ?: 0.0
        val macdHist = macdValue - macdSignal

        // 4. Bollinger Bands (20 period)
        val bbPeriod = 20
        val bbMiddle = if (closes.size >= bbPeriod) {
            closes.takeLast(bbPeriod).average()
        } else {
            closes.average()
        }
        val variance = if (closes.size >= bbPeriod) {
            closes.takeLast(bbPeriod).map { Math.pow(it - bbMiddle, 2.0) }.sum() / bbPeriod
        } else {
            closes.map { Math.pow(it - bbMiddle, 2.0) }.sum() / closes.size
        }
        val stdDev = sqrt(variance)
        val bbUpper = bbMiddle + (2 * stdDev)
        val bbLower = bbMiddle - (2 * stdDev)

        // 5. Support and Resistance
        val support = lows.minOrNull() ?: lastClose
        val resistance = highs.maxOrNull() ?: lastClose

        // 6. Trend direction
        val trend = when {
            ema9 > ema21 && lastClose > ema21 -> "BULLISH"
            ema9 < ema21 && lastClose < ema21 -> "BEARISH"
            else -> "NEUTRAL"
        }

        // 7. Candlestick patterns (analyzing last two candles)
        val candlestickPattern = detectCandlestickPattern(candles)

        return IndicatorResults(
            rsi = rsi,
            ema9 = ema9,
            ema21 = ema21,
            macdValue = macdValue,
            macdSignal = macdSignal,
            macdHist = macdHist,
            bbUpper = bbUpper,
            bbMiddle = bbMiddle,
            bbLower = bbLower,
            support = support,
            resistance = resistance,
            trend = trend,
            candlestickPattern = candlestickPattern
        )
    }

    private fun calculateEMA(values: List<Double>, period: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        val ema = mutableListOf<Double>()
        val multiplier = 2.0 / (period + 1)

        // Start first EMA as simple average or first element
        var currentEma = if (values.size >= period) {
            values.take(period).average()
        } else {
            values.first()
        }
        ema.add(currentEma)

        val startIndex = if (values.size >= period) period else 1
        for (i in startIndex until values.size) {
            currentEma = (values[i] - currentEma) * multiplier + currentEma
            ema.add(currentEma)
        }
        return ema
    }

    private fun calculateRSI(closes: List<Double>, period: Int = 14): List<Double> {
        if (closes.size < 2) return emptyList()
        val rsiList = mutableListOf<Double>()
        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()

        for (i in 1 until closes.size) {
            val change = closes[i] - closes[i - 1]
            if (change > 0) {
                gains.add(change)
                losses.add(0.0)
            } else {
                gains.add(0.0)
                losses.add(-change)
            }
        }

        if (gains.size < period) {
            // Not enough candles for a proper 14-period RSI, make an estimate
            val avgGain = gains.average().coerceAtLeast(0.0)
            val avgLoss = losses.average().coerceAtLeast(0.0001)
            val rs = avgGain / avgLoss
            val currentRsi = 100.0 - (100.0 / (1.0 + rs))
            rsiList.add(currentRsi)
            return rsiList
        }

        var avgGain = gains.take(period).average()
        var avgLoss = losses.take(period).average()

        var rs = avgGain / if (avgLoss == 0.0) 0.00001 else avgLoss
        rsiList.add(100.0 - (100.0 / (1.0 + rs)))

        for (i in period until gains.size) {
            avgGain = (avgGain * (period - 1) + gains[i]) / period
            avgLoss = (avgLoss * (period - 1) + losses[i]) / period
            rs = avgGain / if (avgLoss == 0.0) 0.00001 else avgLoss
            rsiList.add(100.0 - (100.0 / (1.0 + rs)))
        }

        return rsiList
    }

    private fun detectCandlestickPattern(candles: List<Candle>): String {
        if (candles.isEmpty()) return "None"
        val last = candles.last()
        val body = Math.abs(last.close - last.open)
        val totalRange = last.high - last.low
        if (totalRange <= 0.0) return "None"

        val isGreen = last.close >= last.open

        // Doji check
        if (body <= totalRange * 0.08) {
            return "Doji"
        }

        // Hammer or Shooting Star check
        val lowerShadow = if (isGreen) last.open - last.low else last.close - last.low
        val upperShadow = if (isGreen) last.high - last.close else last.high - last.open

        // Hammer: lower shadow is at least 2 times the body, very small upper shadow
        if (lowerShadow >= body * 2.0 && upperShadow <= totalRange * 0.15) {
            return "Hammer"
        }

        // Shooting Star: upper shadow is at least 2 times the body, very small lower shadow
        if (upperShadow >= body * 2.0 && lowerShadow <= totalRange * 0.15) {
            return "Shooting Star"
        }

        // Engulfing checks (requires last 2 candles)
        if (candles.size >= 2) {
            val secondLast = candles[candles.size - 2]
            val prevBody = Math.abs(secondLast.close - secondLast.open)
            val prevIsGreen = secondLast.close >= secondLast.open

            if (isGreen && !prevIsGreen && body > prevBody) {
                // Bullish Engulfing
                if (last.close >= secondLast.open && last.open <= secondLast.close) {
                    return "Bullish Engulfing"
                }
            } else if (!isGreen && prevIsGreen && body > prevBody) {
                // Bearish Engulfing
                if (last.close <= secondLast.open && last.open >= secondLast.close) {
                    return "Bearish Engulfing"
                }
            }
        }

        return "None"
    }
}
