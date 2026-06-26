package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.SignalEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.DarkSurfaceLighter
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SignalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: SignalViewModel,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.signalHistory.collectAsState()
    var selectedSignalForDetails by remember { mutableStateOf<SignalEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Header row with clearSweep ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Signal Logs",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Archive of past analyses",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (historyList.isNotEmpty()) {
                Button(
                    onClick = { viewModel.clearHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear all",
                        tint = CyberRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Log", color = CyberRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Empty State ---
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.White.copy(alpha = 0.02f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No logs",
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "History is Empty",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Analyze asset data on the first tab.\nLogs will be automatically archived here.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            // --- History List ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("history_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = { selectedSignalForDetails = item },
                        onDelete = { viewModel.deleteHistoryItem(item.id) }
                    )
                }
            }
        }
    }

    // --- Details Dialog ---
    selectedSignalForDetails?.let { item ->
        SignalDetailsDialog(
            item = item,
            onDismiss = { selectedSignalForDetails = null }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: SignalEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val signalColor = when (item.signalType) {
        "BUY" -> CyberGreen
        "SELL" -> CyberRed
        "WAIT" -> CyberAmber
        else -> CyberAmber // AVOID
    }

    val icon = when (item.signalType) {
        "BUY" -> Icons.Default.ArrowUpward
        "SELL" -> Icons.Default.ArrowDownward
        "WAIT" -> Icons.Default.Loop
        else -> Icons.Default.Warning
    }

    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeLabel = sdf.format(Date(item.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("history_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Left Icon Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(signalColor.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, signalColor.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.signalType,
                        tint = signalColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Mid Text info
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.asset,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            val tfLabel = when (item.timeframe) {
                                "1 minute" -> "1m"
                                "5 minutes" -> "5m"
                                "15 minutes" -> "15m"
                                else -> item.timeframe
                            }
                            Text(tfLabel, color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Close: ${getPriceFormatter(item.asset).format(item.candleClose)} | $timeLabel",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Right side score & action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.confidence}% Conf.",
                        color = signalColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = item.signalType,
                        color = signalColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete record",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SignalDetailsDialog(
    item: SignalEntity,
    onDismiss: () -> Unit
) {
    val formatter = getPriceFormatter(item.asset)
    val signalColor = when (item.signalType) {
        "BUY" -> CyberGreen
        "SELL" -> CyberRed
        "WAIT" -> CyberAmber
        else -> CyberAmber // AVOID
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (item.signalType) {
                        "BUY" -> Icons.Default.ArrowUpward
                        "SELL" -> Icons.Default.ArrowDownward
                        "WAIT" -> Icons.Default.Loop
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = signalColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.asset} - ${item.signalType} Signal",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Basic info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Timeframe:", color = TextSecondary, fontSize = 12.sp)
                    Text(item.timeframe, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Confidence Score:", color = TextSecondary, fontSize = 12.sp)
                    Text("${item.confidence}%", color = signalColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Prices
                Column(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Open: ${formatter.format(item.candleOpen)}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("Close: ${formatter.format(item.candleClose)}", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("High: ${formatter.format(item.candleHigh)}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("Low: ${formatter.format(item.candleLow)}", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Indicators list
                Text("Technical Indicators", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DialogIndicatorRow("RSI (14)", String.format("%.2f", item.rsi))
                    DialogIndicatorRow("EMA 9 / 21", "${String.format("%.2f", item.ema9)} / ${String.format("%.2f", item.ema21)}")
                    DialogIndicatorRow("MACD", "Val: ${String.format("%.4f", item.macdValue)} | Sig: ${String.format("%.4f", item.macdSignal)}")
                    DialogIndicatorRow("Bollinger Bands", "Up: ${String.format("%.2f", item.bbUpper)} | Low: ${String.format("%.2f", item.bbLower)}")
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Signal Reason description
                Text("Analysis Explanation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                    text = item.reason,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CyberGreen)
            }
        },
        containerColor = DarkSurfaceLighter,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DialogIndicatorRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

fun getPriceFormatter(asset: String): java.text.DecimalFormat {
    val pattern = if (asset.contains("BTC") || asset.contains("ETH") || asset.contains("Crypto")) {
        "#,##0.00"
    } else {
        "#,##0.00000"
    }
    return java.text.DecimalFormat(pattern)
}
