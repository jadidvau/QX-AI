package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkSurfaceLighter
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StrategyScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Indicators", "Candles", "Risk Management")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = "Educational Center",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Learn indicators, patterns & risk rules",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // --- Custom Sub TabRow ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = CyberGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyberGreen
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Tab Content ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> IndicatorsTab()
                1 -> CandlesTab()
                2 -> RiskTab()
            }
        }
    }
}

@Composable
fun IndicatorsTab() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StrategyGuideCard(
            title = "Relative Strength Index (RSI 14)",
            icon = Icons.Default.ShowChart,
            iconColor = CyberBlue,
            description = "RSI measures the speed and change of price movements on a scale from 0 to 100. It is primarily used to identify overbought or oversold conditions.",
            rules = listOf(
                "Oversold (< 30): Suggests the asset is traded too low. Reversal upward is possible (CALL/BUY trigger).",
                "Overbought (> 70): Suggests the asset is traded too high. Correction downward is possible (PUT/SELL trigger).",
                "Neutral Zone (45 - 55): Avoid predicting trend reversals. Look for strong EMA momentum instead."
            )
        )

        StrategyGuideCard(
            title = "EMA 9 & EMA 21 Crossovers",
            icon = Icons.Default.AutoAwesome,
            iconColor = CyberGreen,
            description = "Exponential Moving Averages give weight to recent prices. Comparing a fast EMA (9 periods) and a slow EMA (21 periods) helps isolate trend direction.",
            rules = listOf(
                "Golden Cross (EMA 9 crossed ABOVE EMA 21): Indicates immediate bullish momentum (favors CALL/BUY).",
                "Death Cross (EMA 9 crossed BELOW EMA 21): Indicates immediate bearish momentum (favors PUT/SELL).",
                "Price distance: If price is extremely far above or below both EMAs, expect a reversion back to the averages."
            )
        )

        StrategyGuideCard(
            title = "MACD (Moving Average Convergence Divergence)",
            icon = Icons.Default.Info,
            iconColor = CyberPurple,
            description = "MACD subtracts a 26-day EMA from a 12-day EMA. A 9-day EMA of the MACD line acts as the signal trigger. The histogram displays convergence/divergence.",
            rules = listOf(
                "Histogram > 0: Momentum is upward and accelerating (supports CALL/BUY signals).",
                "Histogram < 0: Momentum is downward and accelerating (supports PUT/SELL signals).",
                "Zero Crossover: Crossing the zero line signifies a powerful structural shift in market dominance."
            )
        )

        StrategyGuideCard(
            title = "Bollinger Bands (20-period, 2 StdDev)",
            icon = Icons.Default.Book,
            iconColor = CyberAmber,
            description = "Bollinger Bands are volatility bands placed above and below a central simple moving average. The band width expands and contracts dynamically based on market volatility.",
            rules = listOf(
                "Band Squeeze: Narrow bands indicate extremely low volatility. Expect an imminent breakout.",
                "Lower Band touch: Often acts as local dynamic support. Prone to bounces (suggests BUY near boundaries).",
                "Upper Band touch: Often acts as local dynamic resistance. Prone to corrections (suggests SELL near boundaries)."
            )
        )
    }
}

@Composable
fun CandlesTab() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StrategyGuideCard(
            title = "The Doji (Indecision)",
            icon = Icons.Default.CandlestickChart,
            iconColor = CyberAmber,
            description = "A Doji forms when an asset's open and close are virtually equal. The long wicks signify that both bulls and bears pushed prices, but neither could take control.",
            rules = listOf(
                "Significance: Indicates market saturation or upcoming trend exhaustion.",
                "Wait Rule: Do NOT trade immediately on a Doji. Always wait for the NEXT candle to confirm the breakout direction."
            )
        )

        StrategyGuideCard(
            title = "The Hammer (Bullish Reversal)",
            icon = Icons.Default.CandlestickChart,
            iconColor = CyberGreen,
            description = "A Hammer occurs at the bottom of a downtrend. It features a small body at the top, a long lower shadow (at least 2x the body), and little or no upper shadow.",
            rules = listOf(
                "Aesthetic: The lower shadow shows that sellers drove price down, but buyers pushed it back near the open.",
                "Signal: Strong bullish indicator, especially when it prints near calculated support lines."
            )
        )

        StrategyGuideCard(
            title = "The Shooting Star (Bearish Reversal)",
            icon = Icons.Default.CandlestickChart,
            iconColor = CyberRed,
            description = "The inverse of a Hammer. It occurs at the peak of an uptrend, with a small body near the bottom and a very long upper shadow (wick).",
            rules = listOf(
                "Aesthetic: Indicates that buyers drove price up aggressively but were completely overwhelmed by sellers before close.",
                "Signal: Strong bearish reversal indicator, especially when touching Bollinger Band upper levels."
            )
        )

        StrategyGuideCard(
            title = "Bullish & Bearish Engulfing",
            icon = Icons.Default.CandlestickChart,
            iconColor = CyberBlue,
            description = "Two-candle reversal patterns where the second candle body completely overlaps ('engulfs') the body of the previous candle.",
            rules = listOf(
                "Bullish Engulfing: A small red candle engulfed by a subsequent large green candle. Signals massive buying interest.",
                "Bearish Engulfing: A small green candle engulfed by a subsequent large red candle. Signals massive selling pressure."
            )
        )
    }
}

@Composable
fun RiskTab() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StrategyGuideCard(
            title = "The Core Rule: Capital Preservation",
            icon = Icons.Default.Security,
            iconColor = CyberGreen,
            description = "Trading is not about making millions in a day; it is about protecting your account so you can continue participating tomorrow. Surviving bad streaks is the key.",
            rules = listOf(
                "Never trade more than 1% to 2% of your total balance on a single trade.",
                "Never chase losses. If you have two consecutive losing setups, close the terminal and walk away.",
                "Embrace the 'WAIT' state. No-trade is a profitable trade because it costs zero capital."
            )
        )

        StrategyGuideCard(
            title = "Managing Volatility & Conflicts",
            icon = Icons.Default.Info,
            iconColor = CyberAmber,
            description = "Trading systems rely on mathematical averages. High-impact news, speeches, or macroeconomic events break typical mathematical models.",
            rules = listOf(
                "Avoid trading 30 minutes before and after major news releases (Extreme Volatility).",
                "When indicators conflict, the consensus score drops. That is why the app outputs 'WAIT'. Never override wait signals out of boredom.",
                "Recognize that AI and Indicators are probability estimations, never guarantees."
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberRed.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "A Strict Word on Financial Scams",
                    color = CyberRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Any broker, channel, or software that promises '100% winning rate', 'guaranteed profit', or 'bug strategies' is committing fraud. Markets are stochastic networks driven by trillions of variables. This analyzer is built strictly to demonstrate technical math calculations for educational study only.",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun StrategyGuideCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    description: String,
    rules: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                rules.forEach { rule ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = iconColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = rule,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
