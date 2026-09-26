package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel

@Composable
fun ReportsScreen(viewModel: FlowViewModel) {
    val context = LocalContext.current
    val habits by viewModel.habitsWithStats.collectAsState()
    val flowScore by viewModel.flowScore.collectAsState()
    val todayCompleted by viewModel.todayCompletedCount.collectAsState()
    val challenges by viewModel.challenges.collectAsState()

    var showJsonExportDialog by remember { mutableStateOf(false) }
    var jsonExportData by remember { mutableStateOf("") }
    var selectedReportType by remember { mutableStateOf("Weekly Review") }

    val mostConsistent = habits.maxByOrNull { it.currentStreak }
    val leastConsistent = habits.filter { it.currentStreak == 0 }.firstOrNull() ?: habits.minByOrNull { it.completionRate }
    val bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
    val totalCompletionsAll = habits.sumOf { it.totalCompletions }

    // JSON Data Export Dialog
    if (showJsonExportDialog) {
        AlertDialog(
            onDismissRequest = { showJsonExportDialog = false },
            title = { Text("Complete Data Export (JSON)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your complete FLOW data has been packaged securely:", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                    OutlinedTextField(
                        value = jsonExportData,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showJsonExportDialog = false }) {
                    Text("Close", color = FlowTeal)
                }
            },
            containerColor = DarkSurface
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text("ANALYTICS & EXPORTS", style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                Text("Performance Reports", style = MaterialTheme.typography.headlineLarge, color = TextHigh)
            }
        }

        // Report Type Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Daily", "Weekly Review", "Monthly Recap", "Overall").forEach { type ->
                    FilterChip(
                        selected = selectedReportType == type,
                        onClick = { selectedReportType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlowTeal,
                            selectedLabelColor = DarkBackground
                        )
                    )
                }
            }
        }

        // Main Report Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                modifier = Modifier.fillMaxWidth().testTag("report_summary_card")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(selectedReportType.uppercase(), style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                            Text(
                                if (selectedReportType == "Monthly Recap") "Your Month in FLOW" else "Consistency Breakdown",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextHigh
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(FlowTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = FlowTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Key Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill("Score", "${flowScore}/10", FlowTeal)
                        MetricPill("Completions", "$totalCompletionsAll", FlowEmerald)
                        MetricPill("Best Streak", "${bestStreak}d", FlowAmber)
                        MetricPill("Active", "${habits.size}", TextHigh)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Deep Insights for Weekly / Monthly
                    if (mostConsistent != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = FlowEmerald)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Most Consistent Habit", fontWeight = FontWeight.Bold, color = TextHigh)
                                    Text("${mostConsistent.habit.name} (${mostConsistent.currentStreak} day streak)", style = MaterialTheme.typography.bodySmall, color = FlowEmerald)
                                }
                            }
                        }
                    }

                    if (leastConsistent != null && leastConsistent != mostConsistent) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = FlowAmber)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Needs Gentle Focus", fontWeight = FontWeight.Bold, color = TextHigh)
                                    Text("${leastConsistent.habit.name} (0 day streak)", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Export Buttons (PDF & JPG)
                    Text("Download & Share Report:", style = MaterialTheme.typography.bodyMedium, color = TextMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.exportReport(context, selectedReportType, asPdf = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export PDF", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.exportReport(context, selectedReportType, asPdf = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_jpg_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export JPG", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Full Data Backup & Export Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = FlowTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Complete Data Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextHigh)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Export your complete FLOW data (habits, completion history, streaks, challenges, goals, AI memories) as JSON.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.exportFullData { json ->
                                jsonExportData = json
                                showJsonExportDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FlowTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Full JSON Export")
                    }
                }
            }
        }
    }
}
