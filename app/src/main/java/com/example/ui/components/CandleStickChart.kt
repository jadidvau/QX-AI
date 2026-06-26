package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.TextSecondary

@Composable
fun CandleStickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    supportPrice: Double? = null,
    resistancePrice: Double? = null
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No chart data available. Press Reload.",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
        return
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val minPrice = candles.map { it.low }.minOrNull() ?: 0.0
            val maxPrice = candles.map { it.high }.maxOrNull() ?: 1.0
            val priceRange = maxPrice - minPrice
            val paddedMin = minPrice - (priceRange * 0.05)
            val paddedMax = maxPrice + (priceRange * 0.05)
            val paddedRange = if (paddedMax - paddedMin == 0.0) 1.0 else paddedMax - paddedMin

            // Helper to map price to Y coordinate
            fun priceToY(price: Double): Float {
                return (height - ((price - paddedMin) / paddedRange * height)).toFloat()
            }

            // Draw grid lines (horizontal)
            val gridCount = 4
            for (i in 0..gridCount) {
                val y = height * i / gridCount
                drawLine(
                    color = Color.White.copy(alpha = 0.07f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw Support & Resistance Lines if present
            if (supportPrice != null && supportPrice > 0.0) {
                val supportY = priceToY(supportPrice)
                drawLine(
                    color = CyberGreen.copy(alpha = 0.4f),
                    start = Offset(0f, supportY),
                    end = Offset(width, supportY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
            if (resistancePrice != null && resistancePrice > 0.0) {
                val resistanceY = priceToY(resistancePrice)
                drawLine(
                    color = CyberRed.copy(alpha = 0.4f),
                    start = Offset(0f, resistanceY),
                    end = Offset(width, resistanceY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Draw Candlesticks
            val candleCount = candles.size
            val candleWidth = width / candleCount
            val spacing = candleWidth * 0.15f

            for (i in 0 until candleCount) {
                val candle = candles[i]
                val isBullish = candle.close >= candle.open
                val candleColor = if (isBullish) CyberGreen else CyberRed

                val xCenter = (i * candleWidth) + (candleWidth / 2f)
                val left = (i * candleWidth) + spacing
                val rectWidth = candleWidth - (spacing * 2f)

                val highY = priceToY(candle.high)
                val lowY = priceToY(candle.low)
                val openY = priceToY(candle.open)
                val closeY = priceToY(candle.close)

                // Draw Wick (High to Low)
                drawLine(
                    color = candleColor,
                    start = Offset(xCenter, highY),
                    end = Offset(xCenter, lowY),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Draw Body (Open to Close)
                val topY = minOf(openY, closeY)
                val bottomY = maxOf(openY, closeY)
                val rectHeight = maxOf(bottomY - topY, 2f) // Ensure at least 2px body

                drawRect(
                    color = candleColor,
                    topLeft = Offset(left, topY),
                    size = Size(rectWidth, rectHeight)
                )
            }
        }
    }
}
