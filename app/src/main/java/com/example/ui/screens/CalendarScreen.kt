package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel
import com.example.util.DateUtils

@Composable
fun CalendarScreen(viewModel: FlowViewModel) {
    val habits by viewModel.habitsWithStats.collectAsState()
    val allCompletions by viewModel.allCompletions.collectAsState()
    val selectedDate by viewModel.selectedCalendarDate.collectAsState()

    // 35 days for heatmap
    val pastDays = remember { DateUtils.getPastDaysList(35) }

    // Map completions by date
    val completionsByDate = remember(allCompletions) {
        allCompletions.groupBy { it.date }
    }

    // Selected date completions
    val selectedDayCompletions = remember(selectedDate, allCompletions) {
        allCompletions.filter { it.date == selectedDate }
    }
    val selectedDayCompletionMap = remember(selectedDayCompletions) {
        selectedDayCompletions.associateBy { it.habitId }
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
                Text("CONSISTENCY HEATMAP", style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                Text("Calendar & History", style = MaterialTheme.typography.headlineLarge, color = TextHigh)
            }
        }

        // Heatmap Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("35-Day Consistency Grid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextHigh)
                        // Legend
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(DarkSurfaceElevated))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(FlowTeal.copy(alpha = 0.35f)))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(FlowTeal.copy(alpha = 0.7f)))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(FlowTeal))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5 rows x 7 cols grid
                    val chunkedWeeks = pastDays.chunked(7)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunkedWeeks.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                week.forEach { dateStr ->
                                    val count = completionsByDate[dateStr]?.count { it.status == "COMPLETED" || it.status == "REST_DAY" } ?: 0
                                    val totalHabits = habits.size.coerceAtLeast(1)
                                    val ratio = count.toFloat() / totalHabits.toFloat()

                                    val cellColor = when {
                                        count == 0 -> DarkSurfaceElevated
                                        ratio < 0.4f -> FlowTeal.copy(alpha = 0.35f)
                                        ratio < 0.8f -> FlowTeal.copy(alpha = 0.7f)
                                        else -> FlowTeal
                                    }

                                    val isSelected = dateStr == selectedDate

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(cellColor)
                                            .clickable { viewModel.selectCalendarDate(dateStr) }
                                            .then(
                                                if (isSelected) Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)) else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val dayNumber = dateStr.takeLast(2)
                                        Text(
                                            text = dayNumber,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (count > 0 && ratio >= 0.8f) DarkBackground else TextHigh,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Selected: ${DateUtils.getFormattedDate(selectedDate, "EEEE, MMMM d, yyyy")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FlowTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Habit details for the selected day
        item {
            Text("Habit Status on ${DateUtils.getFormattedDate(selectedDate)}", style = MaterialTheme.typography.titleLarge, color = TextHigh)
        }

        if (habits.isEmpty()) {
            item {
                Text("No habits configured.", color = TextMedium)
            }
        } else {
            items(habits) { item ->
                val comp = selectedDayCompletionMap[item.habit.id]
                val status = comp?.status ?: "INCOMPLETE"

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextHigh)
                            Text("${item.habit.category} • ${item.habit.difficulty}", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                            if (!comp?.note.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Note: ${comp?.note}", style = MaterialTheme.typography.bodySmall, color = FlowEmerald)
                            }
                        }

                        // Status Tag & Toggle
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = when (status) {
                                    "COMPLETED" -> FlowEmerald.copy(alpha = 0.2f)
                                    "REST_DAY" -> FlowIndigo.copy(alpha = 0.2f)
                                    "FREEZE_PROTECTED" -> FlowTeal.copy(alpha = 0.2f)
                                    "SKIPPED" -> TextLow.copy(alpha = 0.2f)
                                    else -> DarkSurfaceElevated
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = status.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (status) {
                                        "COMPLETED" -> FlowEmerald
                                        "REST_DAY" -> FlowIndigo
                                        "FREEZE_PROTECTED" -> FlowTeal
                                        else -> TextMedium
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(onClick = { viewModel.toggleHabit(item.habit.id, selectedDate) }) {
                                Icon(
                                    imageVector = if (status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle",
                                    tint = if (status == "COMPLETED") FlowTeal else TextMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
