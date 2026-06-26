package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
fun Mt5ConfirmationScreen(
    viewModel: SignalViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    // Launchers for Screenshot selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                viewModel.setMt5UploadedImage(argbBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val argbBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            viewModel.setMt5UploadedImage(argbBitmap)
        }
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
                    text = "MT5 Confirmation",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Verify setups using real MetaTrader 5 technicals",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "MT5 Confirmation",
                tint = CyberBlue,
                modifier = Modifier.size(28.dp)
            )
        }

        // --- OTC WARNING BANNER ---
        val isOtc = state.selectedAsset.contains("OTC", ignoreCase = true) || state.manualMarketTypeInput == "OTC"
        if (isOtc) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberAmber.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "OTC Warning",
                        tint = CyberAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "⚠️ MT5 may not match OTC price movement. Use MT5 only as trend confirmation.",
                        color = CyberAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // --- MT5 SCREENSHOT CONFIRMATION ---
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
                        text = "UPLOAD MT5 SCREENSHOT",
                        color = CyberBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (state.mt5UploadedImageBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .background(Color.Black)
                        ) {
                            Image(
                                bitmap = state.mt5UploadedImageBitmap!!.asImageBitmap(),
                                contentDescription = "Uploaded MT5 Screenshot",
                                modifier = Modifier.fillMaxSize()
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.setMt5UploadedImage(null) },
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Remove",
                                        tint = CyberRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty State Upload Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceLighter),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("mt5_gallery_button")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = CyberBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gallery", color = TextPrimary)
                            }

                            Button(
                                onClick = { cameraLauncher.launch() },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceLighter),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("mt5_camera_button")
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = CyberBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Camera", color = TextPrimary)
                            }
                        }
                    }

                    if (state.isAnalyzingMt5Screenshot) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = CyberBlue,
                            trackColor = DarkSurfaceLighter
                        )
                        Text(
                            text = "Analyzing MT5 chart patterns with AI Vision...",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // --- SCREENSHOT SCANNING RESULTS ---
                    if (state.mt5DetectedSymbol != null || state.mt5ScreenshotAnalysis != null) {
                        Divider(color = Color.White.copy(alpha = 0.05f))

                        Text(
                            text = "DETECTED MT5 PARAMETERS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ParameterChip(label = "Symbol", value = state.mt5DetectedSymbol ?: "Unknown", icon = Icons.Default.Label, color = CyberBlue, modifier = Modifier.weight(1f))
                            ParameterChip(label = "Timeframe", value = state.mt5DetectedTimeframe ?: "Unknown", icon = Icons.Default.AccessTime, color = CyberAmber, modifier = Modifier.weight(1f))
                            ParameterChip(label = "Trend", value = state.mt5DetectedTrend ?: "Unknown", icon = Icons.Default.TrendingUp, color = CyberGreen, modifier = Modifier.weight(1f))
                        }

                        state.mt5ScreenshotAnalysis?.let { analysis ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceLighter),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "MT5 SIGNAL ANALYSIS",
                                            color = CyberBlue,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        SignalBadge(signal = analysis.signal, confidence = analysis.confidence)
                                    }

                                    Text(
                                        text = analysis.reason,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )

                                    Divider(color = Color.White.copy(alpha = 0.04f))

                                    // Score indicators
                                    Text(
                                        text = "TECHNICAL METERS",
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    ScoreProgress(label = "Trend Strength", score = analysis.trendScore, max = 20, color = CyberGreen)
                                    ScoreProgress(label = "Candlestick Patterns", score = analysis.candlePatternScore, max = 20, color = CyberPurple)
                                    ScoreProgress(label = "S/R Rejection", score = analysis.supportResistanceScore, max = 20, color = CyberBlue)
                                    ScoreProgress(label = "Momentum Alignment", score = analysis.momentumIndicatorScore, max = 20, color = CyberAmber)
                                    ScoreProgress(label = "Market Volatility", score = analysis.volatilityScore, max = 20, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
    }
}

@Composable
fun ParameterChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DarkSurfaceLighter, RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(12.dp))
                Text(text = label.uppercase(), color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ScoreProgress(
    label: String,
    score: Int,
    max: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = TextPrimary, fontSize = 11.sp)
            Text(text = "$score/$max", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
        LinearProgressIndicator(
            progress = score.toFloat() / max.toFloat(),
            color = color,
            trackColor = Color.White.copy(alpha = 0.05f),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun IndicatorValueTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(6.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(
                text = value,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SignalBadge(
    signal: String,
    confidence: Int
) {
    val (color, labelText) = when (signal.uppercase()) {
        "BUY", "CALL" -> CyberGreen to "CALL"
        "SELL", "PUT" -> CyberRed to "PUT"
        "AVOID" -> Color.Gray to "AVOID MARKET"
        else -> CyberAmber to "WAIT"
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).background(color, RoundedCornerShape(3.dp)))
            Text(
                text = "$labelText $confidence%",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}


