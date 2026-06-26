package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SignalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedSignalScreen(
    viewModel: SignalViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val combinedResult = state.combinedSignalResult

    // History winrate
    val historyList by viewModel.signalHistory.collectAsState()
    val ratedSignals = historyList.filter { it.outcome == "WIN" || it.outcome == "LOSS" }
    val wins = ratedSignals.count { it.outcome == "WIN" }
    val overallWinRate = if (ratedSignals.isNotEmpty()) {
        (wins.toDouble() / ratedSignals.size) * 100.0
    } else {
        70.0 // Baseline default
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Combined Signal",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Multi-source platform convergence algorithms",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.AllInclusive,
                contentDescription = "Combined Signal",
                tint = CyberPurple,
                modifier = Modifier.size(28.dp)
            )
        }

        if (state.analysisResult == null) {
            // --- NO DATA EMPTY STATE ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = "No Analysis",
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No Active Analysis Found",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Please go to the 'Market QX' tab and run a chart screenshot analysis. Once analyzed, combined convergence signals will generate here.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // --- COMBINED ACTIVE RESULTS ---

            // Dynamic Signal Styling
            val (signalColor, signalText) = when (combinedResult?.signal?.uppercase()) {
                "BUY", "CALL" -> CyberGreen to "BUY (CALL)"
                "SELL", "PUT" -> CyberRed to "SELL (PUT)"
                "AVOID" -> Color.Gray to "AVOID MARKET"
                else -> CyberAmber to "WAIT (NO TRADE)"
            }

            // GAUGE CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, signalColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "CONVERGENCE ALGORITHM SIGNAL",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    // Large circular confidence gauge
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(3.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(80.dp))
                            .border(8.dp, signalColor.copy(alpha = 0.1f), RoundedCornerShape(80.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${combinedResult?.confidence ?: 0}%",
                                color = signalColor,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "CONFIDENCE",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Combined Signal Action Text
                    Text(
                        text = signalText,
                        color = signalColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    // Signal strength message
                    val signalStrengthLabel = when {
                        combinedResult?.signal == "WAIT" -> "No Trade Strategy"
                        combinedResult?.signal == "AVOID" -> "High Volatility Out"
                        (combinedResult?.confidence ?: 0) >= 90 -> "Very Strong Signal"
                        (combinedResult?.confidence ?: 0) >= 80 -> "Strong Signal"
                        else -> "Weak Signal"
                    }

                    Box(
                        modifier = Modifier
                            .background(signalColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = signalStrengthLabel.uppercase(),
                            color = signalColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // PLATE WEIGHT SCORES CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CONVERGENCE WEIGHT CONTRIBUTIONS",
                        color = CyberPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    val qxRawConf = state.analysisResult?.confidence ?: 0
                    val mt5RawConf = if (state.mt5Mode == "Screenshot") state.mt5ScreenshotAnalysis?.confidence else state.mt5BackendAnalysis?.confidence

                    // QX contribution (40%)
                    WeightBreakdownRow(
                        title = "Market QX Screenshot (40%)",
                        rawConfText = "$qxRawConf% raw",
                        weightedScoreText = String.format(java.util.Locale.US, "%.1f pts", qxRawConf * 0.4),
                        progress = (qxRawConf * 0.4f) / 40f,
                        color = CyberBlue
                    )

                    // MT5 contribution (40%)
                    val mt5Weighted = if (mt5RawConf != null) mt5RawConf * 0.4 else 20.0 // fallback to 50 raw (20 weighted)
                    WeightBreakdownRow(
                        title = "MetaTrader 5 (${state.mt5Mode}) (40%)",
                        rawConfText = if (mt5RawConf != null) "$mt5RawConf% raw" else "No Data (50% fallback)",
                        weightedScoreText = String.format(java.util.Locale.US, "%.1f pts", mt5Weighted),
                        progress = (mt5Weighted.toFloat()) / 40f,
                        color = CyberPurple
                    )

                    // Backtesting history contribution (20%)
                    val historyWeighted = overallWinRate * 0.2
                    WeightBreakdownRow(
                        title = "Saved Backtesting History (20%)",
                        rawConfText = String.format(java.util.Locale.US, "%.1f%% win rate", overallWinRate),
                        weightedScoreText = String.format(java.util.Locale.US, "%.1f pts", historyWeighted),
                        progress = (historyWeighted.toFloat()) / 20f,
                        color = CyberGreen
                    )
                }
            }

            // SIGNAL COMPARISON GRID
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PLATFORM ALIGNMENT AUDIT",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // QX Plate
                        PlatformComparisonPlate(
                            platformName = "Market QX",
                            signalText = if (state.analysisResult?.signal == "BUY") "CALL" else if (state.analysisResult?.signal == "SELL") "PUT" else state.analysisResult?.signal ?: "WAIT",
                            confidence = "${state.analysisResult?.confidence ?: 0}%",
                            color = if (state.analysisResult?.signal == "BUY") CyberGreen else if (state.analysisResult?.signal == "SELL") CyberRed else CyberAmber,
                            modifier = Modifier.weight(1f)
                        )

                        // MT5 Plate
                        val mt5SignalText = if (state.mt5Mode == "Screenshot") state.mt5ScreenshotAnalysis?.signal else state.mt5BackendAnalysis?.signal
                        val mt5RawConf = if (state.mt5Mode == "Screenshot") state.mt5ScreenshotAnalysis?.confidence else state.mt5BackendAnalysis?.confidence
                        
                        val mappedMt5Signal = if (mt5SignalText == "BUY" || mt5SignalText == "CALL") "CALL" else if (mt5SignalText == "SELL" || mt5SignalText == "PUT") "PUT" else mt5SignalText ?: "PENDING"
                        val plateColor = if (mappedMt5Signal == "CALL") CyberGreen else if (mappedMt5Signal == "PUT") CyberRed else CyberAmber

                        PlatformComparisonPlate(
                            platformName = "MetaTrader 5",
                            signalText = mappedMt5Signal,
                            confidence = if (mt5RawConf != null) "$mt5RawConf%" else "No Data",
                            color = plateColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Agreement status pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceLighter, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Convergence Status",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        val agreementText = combinedResult?.agreementStatus ?: "No MT5 Data"
                        val agreementColor = when (agreementText) {
                            "Matched" -> CyberGreen
                            "Conflict" -> CyberRed
                            "Weak" -> CyberAmber
                            else -> TextSecondary
                        }

                        Box(
                            modifier = Modifier
                                .background(agreementColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, agreementColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = agreementText.uppercase(),
                                color = agreementColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // DETAILED REASONING BLOCK
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CONVERGENCE ALGORITHM EXPLANATION",
                        color = CyberBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = combinedResult?.explanation ?: "",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WeightBreakdownRow(
    title: String,
    rawConfText: String,
    weightedScoreText: String,
    progress: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(text = title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(text = rawConfText, color = TextSecondary, fontSize = 9.sp)
            }
            Text(
                text = weightedScoreText,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
        LinearProgressIndicator(
            progress = progress,
            color = color,
            trackColor = Color.White.copy(alpha = 0.05f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
fun PlatformComparisonPlate(
    platformName: String,
    signalText: String,
    confidence: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DarkSurfaceLighter, RoundedCornerShape(10.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = platformName, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = signalText,
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = confidence,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
