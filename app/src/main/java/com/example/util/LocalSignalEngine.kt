package com.example.util

import java.util.Locale
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignalAnalysis(
    val signal: String, // "BUY" (CALL), "SELL" (PUT), "WAIT" (NO TRADE), "AVOID" (AVOID TRADE)
    val confidence: Int, // 0 to 100
    val reason: String,
    val entry: String = "No entry",
    val expiry: String = "None",
    val riskLevel: String = "Medium",
    val marketCondition: String = "Consolidating",
    val detectedAsset: String = "Unknown",
    val detectedTimeframe: String = "Unknown",
    val detectedPrice: String = "Unknown",
    val detectedExpiry: String = "Unknown",
    val detectedPayout: String = "Unknown",
    val trendScore: Int = 0,
    val candlePatternScore: Int = 0,
    val supportResistanceScore: Int = 0,
    val momentumIndicatorScore: Int = 0,
    val volatilityScore: Int = 0,
    val accuracyNote: String? = null
)

data class MarketSignal(
    val indicatorName: String,
    val description: String,
    val direction: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val weight: Double,
    val isStrong: Boolean
)

object LocalSignalEngine {

    fun analyze(
        asset: String,
        timeframe: String,
        indicators: IndicatorResults,
        volatility: String, // "Low", "Normal", "High", "Extreme", "Extreme (News)"
        candleClose: Double
    ): SignalAnalysis {
        // Rule 1: Extreme Volatility Check
        if (volatility == "Extreme" || volatility == "Extreme (News)") {
            return SignalAnalysis(
                signal = "AVOID",
                confidence = 90,
                reason = "⚠️ **AVOID TRADE (EXTREME VOLATILITY)**\n\nMarket volatility is currently extreme. Standard technical indicators are highly unreliable during news events or severe price gaps. Safe trading rules dictate AVOIDING trades until volatility subsides."
            )
        }

        val rsi = indicators.rsi
        val trend = indicators.trend
        val ema9 = indicators.ema9
        val ema21 = indicators.ema21
        val macdValue = indicators.macdValue
        val macdSignal = indicators.macdSignal
        val macdHist = indicators.macdHist
        val bbUpper = indicators.bbUpper
        val bbLower = indicators.bbLower
        val bbMiddle = indicators.bbMiddle
        val pattern = indicators.candlestickPattern

        val signals = mutableListOf<MarketSignal>()

        // 1. EMA & Trend
        if (ema9 > ema21 && trend == "BULLISH") {
            signals.add(MarketSignal("EMA & Trend", "EMA 9 is above EMA 21 with a primary BULLISH trend, indicating strong upward structure.", "BULLISH", 2.0, true))
        } else if (ema9 < ema21 && trend == "BEARISH") {
            signals.add(MarketSignal("EMA & Trend", "EMA 9 is below EMA 21 with a primary BEARISH trend, indicating strong downward structure.", "BEARISH", 2.0, true))
        } else if (ema9 > ema21) {
            signals.add(MarketSignal("EMA & Trend", "EMA 9 is above EMA 21 showing positive alignment, but overall trend is neutral/mixed.", "BULLISH", 1.0, false))
        } else if (ema9 < ema21) {
            signals.add(MarketSignal("EMA & Trend", "EMA 9 is below EMA 21 showing negative alignment, but overall trend is neutral/mixed.", "BEARISH", 1.0, false))
        }

        // 2. RSI (Relative Strength Index)
        if (rsi < 30) {
            signals.add(MarketSignal("RSI", "RSI is oversold at ${String.format(Locale.US, "%.1f", rsi)}, signaling strong potential for a bullish reversal.", "BULLISH", 2.0, true))
        } else if (rsi > 70) {
            signals.add(MarketSignal("RSI", "RSI is overbought at ${String.format(Locale.US, "%.1f", rsi)}, signaling strong potential for a bearish reversal.", "BEARISH", 2.0, true))
        } else if (rsi in 50.0..70.0) {
            signals.add(MarketSignal("RSI", "RSI is at ${String.format(Locale.US, "%.1f", rsi)} in bullish momentum territory, supporting upward momentum.", "BULLISH", 1.0, false))
        } else if (rsi in 30.0..50.0) {
            signals.add(MarketSignal("RSI", "RSI is at ${String.format(Locale.US, "%.1f", rsi)} in bearish momentum territory, supporting downward momentum.", "BEARISH", 1.0, false))
        }

        // 3. MACD
        if (macdValue > macdSignal && macdHist > 0) {
            signals.add(MarketSignal("MACD", "MACD has crossed above signal line with positive histogram (${String.format(Locale.US, "%.4f", macdHist)}), indicating solid bullish momentum.", "BULLISH", 1.5, true))
        } else if (macdValue < macdSignal && macdHist < 0) {
            signals.add(MarketSignal("MACD", "MACD has crossed below signal line with negative histogram (${String.format(Locale.US, "%.4f", macdHist)}), indicating solid bearish momentum.", "BEARISH", 1.5, true))
        } else if (macdValue > macdSignal) {
            signals.add(MarketSignal("MACD", "MACD has crossed above signal line, showing initial bullish bias.", "BULLISH", 0.5, false))
        } else if (macdValue < macdSignal) {
            signals.add(MarketSignal("MACD", "MACD has crossed below signal line, showing initial bearish bias.", "BEARISH", 0.5, false))
        }

        // 4. Bollinger Bands
        if (candleClose <= bbLower && bbLower != 0.0) {
            signals.add(MarketSignal("Bollinger Bands", "Price is at or below the lower band (${String.format(Locale.US, "%.4f", bbLower)}), suggesting extreme oversold conditions.", "BULLISH", 2.0, true))
        } else if (candleClose >= bbUpper && bbUpper != 0.0) {
            signals.add(MarketSignal("Bollinger Bands", "Price is at or above the upper band (${String.format(Locale.US, "%.4f", bbUpper)}), suggesting extreme overbought conditions.", "BEARISH", 2.0, true))
        } else if (candleClose > bbMiddle && bbMiddle != 0.0) {
            signals.add(MarketSignal("Bollinger Bands", "Price is trading in the upper Bollinger Band channel, indicating short-term bullish positioning.", "BULLISH", 0.5, false))
        } else if (candleClose < bbMiddle && bbMiddle != 0.0) {
            signals.add(MarketSignal("Bollinger Bands", "Price is trading in the lower Bollinger Band channel, indicating short-term bearish positioning.", "BEARISH", 0.5, false))
        }

        // 5. Candlestick Patterns
        if (pattern == "Hammer" || pattern == "Bullish Engulfing") {
            signals.add(MarketSignal("Candlestick Pattern", "Bullish pattern detected: $pattern, suggesting buyers are actively stepping in.", "BULLISH", 2.0, true))
        } else if (pattern == "Shooting Star" || pattern == "Bearish Engulfing") {
            signals.add(MarketSignal("Candlestick Pattern", "Bearish pattern detected: $pattern, suggesting sellers are active at highs.", "BEARISH", 2.0, true))
        } else if (pattern == "Doji") {
            signals.add(MarketSignal("Candlestick Pattern", "Doji candlestick detected, signaling market indecision and temporary equilibrium.", "NEUTRAL", 0.5, false))
        }

        val bullishSignals = signals.filter { it.direction == "BULLISH" }
        val bearishSignals = signals.filter { it.direction == "BEARISH" }

        val totalBullishWeight = bullishSignals.sumOf { it.weight }
        val totalBearishWeight = bearishSignals.sumOf { it.weight }

        val hasStrongBullish = bullishSignals.any { it.isStrong }
        val hasStrongBearish = bearishSignals.any { it.isStrong }

        // Check for opposing/conflicting indicators
        val hasOpposingActions = (hasStrongBullish && hasStrongBearish) ||
                (totalBullishWeight >= 1.5 && totalBearishWeight >= 1.5)

        if (hasOpposingActions) {
            if (volatility == "High") {
                val reason = """
                    ⚠️ **AVOID TRADE (HIGH RISK / CONFLICTING SIGNALS)**
                    
                    **Market Volatility:** HIGH
                    **Indicator Status:** HIGHLY CONFLICTING
                    
                    The local system has detected strong opposing forces in the technical setup:
                    • Bullish pressure: **${String.format(Locale.US, "%.1f", totalBullishWeight)}** points
                    • Bearish pressure: **${String.format(Locale.US, "%.1f", totalBearishWeight)}** points
                    
                    **Key Bullish Signals:**
                    ${bullishSignals.joinToString("\n") { "• [" + it.indicatorName + "] " + it.description }}
                    
                    **Key Bearish Signals:**
                    ${bearishSignals.joinToString("\n") { "• [" + it.indicatorName + "] " + it.description }}
                    
                    **Explanation:** Standard technical indicators are suggesting completely opposing actions under High Volatility. In such environments, indicators become highly unreliable, and the risk of stop-outs/whipsaws is exceptionally high. Safe trading principles dictate **AVOIDING** trades entirely until the conflict resolves and volatility subsides.
                """.trimIndent()

                return SignalAnalysis(
                    signal = "AVOID",
                    confidence = 85,
                    reason = reason
                )
            } else {
                val reason = """
                    🟡 **WAIT (CONTRARY / CONFLICTING SIGNALS)**
                    
                    **Market Volatility:** $volatility
                    **Indicator Status:** CONFLICTING
                    
                    The local system has detected competing signals:
                    • Bullish strength: **${String.format(Locale.US, "%.1f", totalBullishWeight)}** points
                    • Bearish strength: **${String.format(Locale.US, "%.1f", totalBearishWeight)}** points
                    
                    **Bullish Signals:**
                    ${bullishSignals.joinToString("\n") { "• [" + it.indicatorName + "] " + it.description }}
                    
                    **Bearish Signals:**
                    ${bearishSignals.joinToString("\n") { "• [" + it.indicatorName + "] " + it.description }}
                    
                    **Explanation:** Technical indicators suggest opposing actions. We have strong indicators signaling a BUY simultaneously with other strong indicators signaling a SELL. In these cases, the market is in a high-friction consolidation zone. Taking a trade carries elevated risk of whipsaw, so the system defaults to **WAIT** (No Trade) for capital preservation.
                """.trimIndent()

                return SignalAnalysis(
                    signal = "WAIT",
                    confidence = 50,
                    reason = reason
                )
            }
        }

        // Determine outcome based on signal weights
        val threshold = 2.5
        val signal = when {
            totalBullishWeight > totalBearishWeight && totalBullishWeight >= threshold -> "BUY"
            totalBearishWeight > totalBullishWeight && totalBearishWeight >= threshold -> "SELL"
            else -> "WAIT"
        }

        val highVolatilityPenalty = if (volatility == "High") 10 else 0

        val confidence = when (signal) {
            "BUY" -> {
                val totalWeight = totalBullishWeight + totalBearishWeight
                val ratio = if (totalWeight > 0.0) totalBullishWeight / totalWeight else 0.5
                ((ratio * 100).toInt() - highVolatilityPenalty).coerceIn(55, 95)
            }
            "SELL" -> {
                val totalWeight = totalBullishWeight + totalBearishWeight
                val ratio = if (totalWeight > 0.0) totalBearishWeight / totalWeight else 0.5
                ((ratio * 100).toInt() - highVolatilityPenalty).coerceIn(55, 95)
            }
            else -> 50
        }

        val reasonBuilder = StringBuilder()
        when (signal) {
            "BUY" -> {
                reasonBuilder.append("🟢 **CALL (BUY) SIGNAL IDENTIFIED**\n\n")
                reasonBuilder.append("Weighed analysis of technical metrics shows solid bullish alignment with **")
                reasonBuilder.append(String.format(Locale.US, "%.1f", totalBullishWeight))
                reasonBuilder.append("** bullish points versus **")
                reasonBuilder.append(String.format(Locale.US, "%.1f", totalBearishWeight))
                reasonBuilder.append("** bearish points.\n\n")

                reasonBuilder.append("**Key Bullish Drivers:**\n")
                bullishSignals.forEach { reasonBuilder.append("• [${it.indicatorName}] ${it.description}\n") }

                if (bearishSignals.isNotEmpty()) {
                    reasonBuilder.append("\n**Contrarian Factors (Bearish):**\n")
                    bearishSignals.forEach { reasonBuilder.append("• [${it.indicatorName}] ${it.description}\n") }
                }

                reasonBuilder.append("\n**Conclusion:** The convergence of these indicators suggests robust upward momentum. ")
                if (volatility == "High") {
                    reasonBuilder.append("However, market volatility is **HIGH**, so execute with strict risk management.")
                } else {
                    reasonBuilder.append("Market conditions support a potential continuation of the bullish trend.")
                }
            }
            "SELL" -> {
                reasonBuilder.append("🔴 **PUT (SELL) SIGNAL IDENTIFIED**\n\n")
                reasonBuilder.append("Weighed analysis of technical metrics shows solid bearish alignment with **")
                reasonBuilder.append(String.format(Locale.US, "%.1f", totalBearishWeight))
                reasonBuilder.append("** bearish points versus **")
                reasonBuilder.append(String.format(Locale.US, "%.1f", totalBullishWeight))
                reasonBuilder.append("** bullish points.\n\n")

                reasonBuilder.append("**Key Bearish Drivers:**\n")
                bearishSignals.forEach { reasonBuilder.append("• [${it.indicatorName}] ${it.description}\n") }

                if (bullishSignals.isNotEmpty()) {
                    reasonBuilder.append("\n**Contrarian Factors (Bullish):**\n")
                    bullishSignals.forEach { reasonBuilder.append("• [${it.indicatorName}] ${it.description}\n") }
                }

                reasonBuilder.append("\n**Conclusion:** The convergence of these indicators suggests robust downward momentum. ")
                if (volatility == "High") {
                    reasonBuilder.append("However, market volatility is **HIGH**, so execute with strict risk management.")
                } else {
                    reasonBuilder.append("Market conditions support a potential continuation of the bearish trend.")
                }
            }
            else -> {
                reasonBuilder.append("🟡 **WAIT (NO TRADE) - NEUTRAL MOMENTUM**\n\n")
                reasonBuilder.append("Combined indicator analysis shows insufficient momentum or conviction to trigger a trade. ")
                reasonBuilder.append("Bullish points: **${String.format(Locale.US, "%.1f", totalBullishWeight)}** | ")
                reasonBuilder.append("Bearish points: **${String.format(Locale.US, "%.1f", totalBearishWeight)}**.\n\n")

                if (bullishSignals.isNotEmpty()) {
                    reasonBuilder.append("**Bullish Indicators:**\n")
                    bullishSignals.forEach { reasonBuilder.append("• [${it.indicatorName}] ${it.description}\n") }
                }
                if (bearishSignals.isNotEmpty()) {
                    if (bullishSignals.isNotEmpty()) reasonBuilder.append("\n")
                    reasonBuilder.append("**Bearish Indicators:**\n")
                    bearishSignals.forEach { reasonBuilder.append("• [${it.indicatorName}] ${it.description}\n") }
                }
                reasonBuilder.append("\n**Conclusion:** Current market structures lack a unified direction. Recommending patience and capital preservation.")
            }
        }

        return SignalAnalysis(
            signal = signal,
            confidence = confidence,
            reason = reasonBuilder.toString()
        )
    }
}

