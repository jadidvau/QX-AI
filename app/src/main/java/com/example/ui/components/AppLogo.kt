package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    showText: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF131A26), Color(0xFF090D14)),
                    center = Offset.Unspecified,
                    radius = Float.POSITIVE_INFINITY
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.02f))
                ),
                shape = RoundedCornerShape(size * 0.22f)
            )
            .padding(size * 0.06f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Draw background track and indicators
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sizePx = size.toPx()
                
                // Draw circular frame border (CyberRed to CyberGreen)
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to CyberRed,
                        0.5f to CyberGreen,
                        1.0f to CyberRed
                    ),
                    startAngle = -210f,
                    sweepAngle = 330f,
                    useCenter = false,
                    style = Stroke(width = sizePx * 0.015f, cap = StrokeCap.Round)
                )

                // Candlestick markers
                val candleWidth = sizePx * 0.015f
                val h1 = sizePx * 0.07f
                val h2 = sizePx * 0.11f

                // Red Candlestick 1
                drawRect(
                    color = CyberRed,
                    topLeft = Offset(sizePx * 0.11f, sizePx * 0.38f),
                    size = androidx.compose.ui.geometry.Size(candleWidth, h1)
                )
                drawLine(
                    color = CyberRed,
                    start = Offset(sizePx * 0.117f, sizePx * 0.32f),
                    end = Offset(sizePx * 0.117f, sizePx * 0.52f),
                    strokeWidth = sizePx * 0.004f
                )

                // Red Candlestick 2
                drawRect(
                    color = CyberRed,
                    topLeft = Offset(sizePx * 0.19f, sizePx * 0.30f),
                    size = androidx.compose.ui.geometry.Size(candleWidth, h2)
                )
                drawLine(
                    color = CyberRed,
                    start = Offset(sizePx * 0.197f, sizePx * 0.24f),
                    end = Offset(sizePx * 0.197f, sizePx * 0.48f),
                    strokeWidth = sizePx * 0.004f
                )

                // Green Candlestick 1
                drawRect(
                    color = CyberGreen,
                    topLeft = Offset(sizePx * 0.68f, sizePx * 0.25f),
                    size = androidx.compose.ui.geometry.Size(candleWidth, h1)
                )
                drawLine(
                    color = CyberGreen,
                    start = Offset(sizePx * 0.687f, sizePx * 0.19f),
                    end = Offset(sizePx * 0.687f, sizePx * 0.38f),
                    strokeWidth = sizePx * 0.004f
                )

                // Green Candlestick 2
                drawRect(
                    color = CyberGreen,
                    topLeft = Offset(sizePx * 0.76f, sizePx * 0.15f),
                    size = androidx.compose.ui.geometry.Size(candleWidth, h2)
                )
                drawLine(
                    color = CyberGreen,
                    start = Offset(sizePx * 0.767f, sizePx * 0.09f),
                    end = Offset(sizePx * 0.767f, sizePx * 0.33f),
                    strokeWidth = sizePx * 0.004f
                )
            }

            // Wordmark and Inner Elements
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "AI",
                        color = Color.White,
                        fontSize = (size.value * 0.25f).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-1).sp
                    )
                    
                    Spacer(modifier = Modifier.width(size * 0.02f))
                    
                    // Spark / Star
                    Canvas(modifier = Modifier.size(size * 0.06f)) {
                        val c = size.toPx() * 0.03f
                        drawLine(Color.White, Offset(c, 0f), Offset(c, c * 2f), strokeWidth = c * 0.4f, cap = StrokeCap.Round)
                        drawLine(Color.White, Offset(0f, c), Offset(c * 2f, c), strokeWidth = c * 0.4f, cap = StrokeCap.Round)
                    }
                }

                if (showText) {
                    Text(
                        text = "TRADE",
                        color = Color.White,
                        fontSize = (size.value * 0.11f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(top = 1.dp)
                    )

                    Text(
                        text = "SMARTER TRADING",
                        color = CyberGreen,
                        fontSize = (size.value * 0.045f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = size * 0.015f)
                    )

                    Text(
                        text = "— BETTER DECISIONS —",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = (size.value * 0.035f).sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                
                // Bull and Bear indicator row at the bottom
                Row(
                    horizontalArrangement = Arrangement.spacedBy(size * 0.08f),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = size * 0.02f)
                ) {
                    Text(text = "🐂", fontSize = (size.value * 0.08f).sp)
                    Box(modifier = Modifier.width(1.dp).height(size * 0.05f).background(Color.White.copy(alpha = 0.15f)))
                    Text(text = "🐻", fontSize = (size.value * 0.08f).sp)
                }
            }

            // Upward arrow layer
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier
                        .size(size * 0.28f)
                        .padding(top = size * 0.05f, end = size * 0.05f)
                )
            }
        }
    }
}
