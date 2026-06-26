package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.components.AppLogo
import com.example.ui.viewmodel.SignalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: SignalViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    // Launchers for Image Capture and Selection
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
                // Convert to ARGB_8888 config if needed
                val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                viewModel.scanScreenshotForPreFill(argbBitmap)
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
            viewModel.scanScreenshotForPreFill(argbBitmap)
        }
    }

    // Expanding Manual Fallbacks state
    var manualExpanded by remember { mutableStateOf(false) }

    // Pulsing Scanner animation for crop boundary
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerLine"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SCREEN TITLE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppLogo(size = 50.dp, showText = false)
                
                Column {
                    Text(
                        text = "QX CHART ANALYZER",
                        color = CyberGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "AI Technical Scanner",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            IconButton(
                onClick = { viewModel.clearUpload() },
                modifier = Modifier
                    .background(DarkSurfaceLighter, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset analyzer",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // --- UPLOAD PANEL ---
        if (state.uploadedImageBitmap == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(
                        BorderStroke(1.dp, Brush.sweepGradient(listOf(CyberGreen, CyberPurple, CyberGreen))),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Upload placeholder",
                        tint = CyberPurple,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "UPLOAD CHART SCREENSHOT",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Supports Market QX / Quotex trading charts",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // --- IMAGE VIEW & AUTO CROP SIMULATOR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, CyberPurple, RoundedCornerShape(16.dp))
            ) {
                // Main Selected Image
                Image(
                    bitmap = state.uploadedImageBitmap!!.asImageBitmap(),
                    contentDescription = "Uploaded Chart Screenshot",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // High-Tech Scanner Animation Overlay (Represents automatic chart detection & crop)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                )

                // Scanning Glow Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (240 * scanY).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, CyberGreen, Color.Transparent)
                            )
                        )
                )

                // Autocrop Boundary Highlights
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.5.dp, CyberGreen.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(CyberGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CHART DETECTED & CROPPED",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top right clear button
                IconButton(
                    onClick = { viewModel.clearUpload() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- MANUAL FALLBACK OVERRIDES PANEL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { manualExpanded = !manualExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Manual Fallbacks",
                            tint = CyberAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Manual Parameters Overrides",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Enforce custom parameters to aid AI reading",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (manualExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle Expand",
                        tint = TextSecondary
                    )
                }

                AnimatedVisibility(visible = manualExpanded) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                        // Asset Selector
                        Text("Asset Pair", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("EUR/USD", "GBP/USD", "BTC/USD").forEach { asset ->
                                val active = state.manualAssetInput == asset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberAmber.copy(alpha = 0.15f) else DarkSurfaceLighter)
                                        .border(1.dp, if (active) CyberAmber else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualAsset(if (active) null else asset) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(asset, color = if (active) CyberAmber else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Timeframe Selector
                        Text("Timeframe", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("1 minute", "5 minutes", "15 minutes").forEach { tf ->
                                val active = state.manualTimeframeInput == tf
                                val label = when (tf) {
                                    "1 minute" -> "1m"
                                    "5 minutes" -> "5m"
                                    "15 minutes" -> "15m"
                                    else -> tf
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberAmber.copy(alpha = 0.15f) else DarkSurfaceLighter)
                                        .border(1.dp, if (active) CyberAmber else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualTimeframe(if (active) null else tf) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = if (active) CyberAmber else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Last Candle Color
                        Text("Last Candle Color", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Green", "Red").forEach { col ->
                                val active = state.manualLastCandleColorInput == col
                                val colAccent = if (col == "Green") CyberGreen else CyberRed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) colAccent.copy(alpha = 0.15f) else DarkSurfaceLighter)
                                        .border(1.dp, if (active) colAccent else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualLastCandleColor(if (active) null else col) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(col, color = if (active) colAccent else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Trend Direction
                        Text("Trend Direction", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("BULLISH", "BEARISH", "NEUTRAL").forEach { trend ->
                                val active = state.manualTrendInput == trend
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberBlue.copy(alpha = 0.15f) else DarkSurfaceLighter)
                                        .border(1.dp, if (active) CyberBlue else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualTrend(if (active) null else trend) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(trend, color = if (active) CyberBlue else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Support/Resistance proximity
                        Text("Support / Resistance Touch", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Support Touch", "Resistance Touch", "None").forEach { touch ->
                                val active = state.manualSrTouchInput == touch
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberPurple.copy(alpha = 0.15f) else DarkSurfaceLighter)
                                        .border(1.dp, if (active) CyberPurple else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualSrTouch(if (active) null else touch) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = touch.replace(" Touch", ""),
                                        color = if (active) CyberPurple else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- RUN ANALYSIS TRIGGER BUTTON ---
        Button(
            onClick = {
                state.uploadedImageBitmap?.let {
                    viewModel.setShowConfirmationDialog(true)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("analyze_screenshot_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberGreen,
                disabledContainerColor = DarkSurfaceLighter
            ),
            shape = RoundedCornerShape(14.dp),
            enabled = state.uploadedImageBitmap != null && !state.isAnalyzing
        ) {
            if (state.isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("AUTOMATIC CHART SCANNING...", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analyze Icon",
                    tint = if (state.uploadedImageBitmap != null) Color.Black else TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.uploadedImageBitmap == null) "UPLOAD SCREENSHOT TO SCAN" else "CONFIRM & ANALYZE CHART",
                    color = if (state.uploadedImageBitmap != null) Color.Black else TextSecondary,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        // --- SIGNAL ANALYSIS RESULTS CARD ---
        AnimatedVisibility(
            visible = state.analysisResult != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            state.analysisResult?.let { result ->
                val signalColor = when (result.signal) {
                    "BUY" -> CyberGreen
                    "SELL" -> CyberRed
                    "WAIT" -> CyberAmber
                    else -> Color.Gray
                }

                val signalLabel = when (result.signal) {
                    "BUY" -> "CALL"
                    "SELL" -> "PUT"
                    "WAIT" -> "WAIT"
                    else -> "AVOID"
                }

                val signalBadgeText = when (result.signal) {
                    "BUY" -> "BULLISH CALL SIGNAL DETECTED"
                    "SELL" -> "BEARISH PUT SIGNAL DETECTED"
                    "WAIT" -> "MARKET CONFLICTS: NO TRADE"
                    else -> "EXTREME VOLATILITY: AVOID"
                }

                // Confidence Label Class
                val confidenceLabel = when {
                    result.confidence < 60 -> "UNRELIABLE STATE"
                    result.confidence in 60..74 -> "WEAK SIGNAL"
                    result.confidence in 75..84 -> "STRONG SIGNAL"
                    else -> "VERY STRONG SIGNAL"
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Giant Neon Immersive Result Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, signalColor.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(signalColor.copy(alpha = 0.08f), Color.Transparent)
                                    )
                                )
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Pill Badge
                            Box(
                                modifier = Modifier
                                    .background(signalColor.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                                    .border(1.dp, signalColor.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = signalBadgeText,
                                    color = signalColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Giant Signal Type
                            Text(
                                text = signalLabel,
                                color = signalColor,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-2).sp
                            )

                            // Confidence Percentage Circle Meter
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(signalColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${result.confidence}% Confidence",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Text(
                                text = "[$confidenceLabel]",
                                color = if (result.confidence >= 75) CyberGreen else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            // Source details badge
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (state.isGeminiAnalyzed) "REAL-TIME GEMINI VISION INTEGRATION" else "LOCAL PATTERN MATCH FALLBACK",
                                    color = if (state.isGeminiAnalyzed) CyberPurple else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bulleted Reasons Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "SCAN ANALYSIS HIGHLIGHTS",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )

                                result.reason.split("-").filter { it.trim().isNotEmpty() }.forEach { line ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            color = signalColor,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = line.trim(),
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // OCR DETECTION METRICS TABLE
                    Text(
                        text = "OCR TEXT EXTRACTION",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .height(140.dp)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item { OcrItemView("Detected Asset", result.detectedAsset) }
                            item { OcrItemView("Timeframe", result.detectedTimeframe) }
                            item { OcrItemView("Current Price", result.detectedPrice) }
                            item { OcrItemView("Timer/Expiry", result.detectedExpiry) }
                            item { OcrItemView("Payout Ratio", result.detectedPayout) }
                            item { OcrItemView("Model Type", if (state.isGeminiAnalyzed) "Vision API" else "Local Engine") }
                        }
                    }

                    // TECHNICAL SCORING BREAKDOWN
                    Text(
                        text = "TECHNICAL ALIGNMENT BREAKDOWN",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ScoreProgressBar("Trend Score", result.trendScore, 25, CyberBlue)
                            ScoreProgressBar("Candlestick Pattern", result.candlePatternScore, 25, CyberGreen)
                            ScoreProgressBar("Support / Resistance Alignment", result.supportResistanceScore, 25, CyberPurple)
                            ScoreProgressBar("Momentum Indicators", result.momentumIndicatorScore, 25, CyberAmber)

                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Cumulative Score:",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${result.trendScore + result.candlePatternScore + result.supportResistanceScore + result.momentumIndicatorScore} / 100",
                                    color = signalColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // EXTRA DETAILS ROW
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(76.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item { InfoValueCard("Risk Level", result.riskLevel, if (result.riskLevel == "Low") CyberGreen else if (result.riskLevel == "High") CyberRed else CyberAmber) }
                        item { InfoValueCard("Suggested Expiry", result.expiry, CyberBlue) }
                        item { InfoValueCard("Entry Recommendation", result.entry, CyberPurple) }
                    }
                }
            }
        }

        // --- SIGNAL RECENT LOGS HISTORY (Room Linked) ---
        Text(
            text = "SCAN RUNS HISTORY",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 12.dp)
        )

        val historyList by viewModel.signalHistory.collectAsState()
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(10.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No scanning logs found. Run your first analysis!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Show last 3 runs
                historyList.take(3).forEach { entity ->
                    val colorAccent = when (entity.signalType) {
                        "BUY" -> CyberGreen
                        "SELL" -> CyberRed
                        "WAIT" -> CyberAmber
                        else -> Color.Gray
                    }
                    val labelText = when (entity.signalType) {
                        "BUY" -> "CALL"
                        "SELL" -> "PUT"
                        "WAIT" -> "WAIT"
                        else -> "AVOID"
                    }

                    val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(entity.timestamp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(10.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(colorAccent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${entity.asset} (${entity.timeframe})",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$dateString • Risk: ${entity.riskLevel ?: "Medium"}",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = labelText,
                                color = colorAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${entity.confidence}% Conf",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // --- RISK DISCLAIMER FOOTER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(10.dp))
                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Risk Warning",
                    tint = CyberRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RISK DISCLAIMER",
                    color = CyberRed.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Trading is risky. Signals are analysis-based and can be wrong.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    if (state.showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowConfirmationDialog(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify & Adjust Scan",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Verify the OCR scan results below to ensure extreme accuracy before triggering AI analysis.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // 1. Asset Pair
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Asset Pair", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        val pairs = listOf("EUR/USD", "GBP/USD", "USD/JPY", "Crypto")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            pairs.forEach { asset ->
                                val active = state.manualAssetInput == asset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberGreen.copy(alpha = 0.15f) else DarkSurface)
                                        .border(1.dp, if (active) CyberGreen else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualAsset(asset) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(asset, color = if (active) CyberGreen else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 2. Timeframe Selector
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Timeframe", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("1 minute", "5 minutes", "15 minutes").forEach { tf ->
                                val active = state.manualTimeframeInput == tf
                                val label = when (tf) {
                                    "1 minute" -> "1m"
                                    "5 minutes" -> "5m"
                                    "15 minutes" -> "15m"
                                    else -> tf
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberAmber.copy(alpha = 0.15f) else DarkSurface)
                                        .border(1.dp, if (active) CyberAmber else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualTimeframe(tf) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = if (active) CyberAmber else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 3. Last Candle Color
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Last Candle Color", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Green", "Red").forEach { col ->
                                val active = state.manualLastCandleColorInput == col
                                val colAccent = if (col == "Green") CyberGreen else CyberRed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) colAccent.copy(alpha = 0.15f) else DarkSurface)
                                        .border(1.dp, if (active) colAccent else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualLastCandleColor(col) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(col, color = if (active) colAccent else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. Trend Direction
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Trend Direction", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Uptrend", "Downtrend", "Sideways").forEach { trend ->
                                val active = state.manualTrendInput == trend
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberBlue.copy(alpha = 0.15f) else DarkSurface)
                                        .border(1.dp, if (active) CyberBlue else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualTrend(trend) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(trend, color = if (active) CyberBlue else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 5. Support / Resistance Touch
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Support / Resistance Touch", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Support Touch", "Resistance Touch", "None").forEach { touch ->
                                val active = state.manualSrTouchInput == touch
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberPurple.copy(alpha = 0.15f) else DarkSurface)
                                        .border(1.dp, if (active) CyberPurple else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualSrTouch(touch) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(touch.replace(" Touch", ""), color = if (active) CyberPurple else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 6. Market Type
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Market Type", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Normal", "OTC").forEach { market ->
                                val active = state.manualMarketTypeInput == market
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberAmber.copy(alpha = 0.15f) else DarkSurface)
                                        .border(1.dp, if (active) CyberAmber else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualMarketType(market) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(market, color = if (active) CyberAmber else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 7. Volatility Level
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Volatility Level", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Low", "Normal", "High", "Extreme").forEach { vol ->
                                val active = state.manualVolatilityInput == vol
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberRed.copy(alpha = 0.15f) else DarkSurface)
                                        .border(1.dp, if (active) CyberRed else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .clickable { viewModel.setManualVolatility(vol) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(vol, color = if (active) CyberRed else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // 8. Multi-Timeframe Confirmation Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Multi-Timeframe Trend Confirmation", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Cross-checks overall 5m/15m trend structure", color = TextSecondary, fontSize = 9.sp)
                        }
                        Switch(
                            checked = state.manualMultiTimeframeInput,
                            onCheckedChange = { viewModel.setManualMultiTimeframe(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberGreen,
                                checkedTrackColor = CyberGreen.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                            )
                        )
                    }

                    // 9. Image Clear Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Screenshot is Clear", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Ensures high fidelity OCR & Vision reading", color = TextSecondary, fontSize = 9.sp)
                        }
                        Switch(
                            checked = state.manualIsImageClearInput,
                            onCheckedChange = { viewModel.setManualIsImageClear(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberGreen,
                                checkedTrackColor = CyberGreen.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setShowConfirmationDialog(false)
                        state.uploadedImageBitmap?.let {
                            viewModel.analyzeScreenshot(it)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CONFIRM & ANALYZE SCREENSHOT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowConfirmationDialog(false) }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceLighter,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun OcrItemView(label: String, value: String) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ScoreProgressBar(label: String, score: Int, maxScore: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "$score / $maxScore",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        LinearProgressIndicator(
            progress = { score.toFloat() / maxScore.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.08f)
        )
    }
}

@Composable
fun InfoValueCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceLighter),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
