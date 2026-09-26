package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.HabitWithStats
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel
import com.example.util.DateUtils

@Composable
fun HomeScreen(
    viewModel: FlowViewModel,
    onNavigateToHabits: () -> Unit,
    onNavigateToChallenges: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    val habits by viewModel.habitsWithStats.collectAsState()
    val flowScore by viewModel.flowScore.collectAsState()
    val todayCompleted by viewModel.todayCompletedCount.collectAsState()
    val challenges by viewModel.challenges.collectAsState()
    val deadlines by viewModel.deadlines.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val quote by viewModel.currentQuote.collectAsState()
    val showMotivationalPopup by viewModel.showMotivationalDialog.collectAsState()

    var habitForOptions by remember { mutableStateOf<HabitWithStats?>(null) }
    var habitNoteInput by remember { mutableStateOf("") }
    var showNoteDialogForHabit by remember { mutableStateOf<HabitWithStats?>(null) }

    // Motivational Popup Dialog
    if (showMotivationalPopup) {
        Dialog(onDismissRequest = { viewModel.dismissMotivationalDialog() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(FlowTeal, DarkBorder))),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("motivational_dialog")
            ) {
                Column {
                    // Aesthetic Image Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_morning_calm_1790432661726),
                            contentDescription = "Morning Calm Aesthetic",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, DarkSurface.copy(alpha = 0.95f))
                                    )
                                )
                        )
                        IconButton(
                            onClick = { viewModel.dismissMotivationalDialog() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DAILY INSPIRATION",
                                style = MaterialTheme.typography.labelMedium,
                                color = FlowTeal,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "\"${quote.quote}\"",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextHigh,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp
                        )

                        Text(
                            text = "— ${quote.author}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium
                        )

                        Button(
                            onClick = { viewModel.dismissMotivationalDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("continue_to_flow_button")
                        ) {
                            Text("Continue to FLOW", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Context / Options Bottom Sheet Dialog for a Habit (Rest Day, Skip, Note, Streak Freeze)
    habitForOptions?.let { item ->
        AlertDialog(
            onDismissRequest = { habitForOptions = null },
            title = { Text(item.habit.name, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select status or action for today:", color = TextMedium)

                    // Mark Rest Day
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.markRestDay(item.habit.id, DateUtils.getTodayString())
                                habitForOptions = null
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Hotel, contentDescription = null, tint = FlowIndigo)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Mark Rest Day", fontWeight = FontWeight.SemiBold, color = TextHigh)
                                Text("Protects your streak without penalty", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                            }
                        }
                    }

                    // Mark Skipped
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.markSkipped(item.habit.id, DateUtils.getTodayString())
                                habitForOptions = null
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, tint = TextLow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Mark Skipped", fontWeight = FontWeight.SemiBold, color = TextHigh)
                                Text("Record that you skipped this habit", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                            }
                        }
                    }

                    // Use Streak Freeze
                    val freezesLeft = user?.availableFreezes ?: 0
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = freezesLeft > 0) {
                                viewModel.useFreeze(item.habit.id, DateUtils.getTodayString())
                                habitForOptions = null
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (freezesLeft > 0) DarkSurfaceElevated else DarkSurfaceElevated.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = if (freezesLeft > 0) FlowTeal else TextLow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Use Streak Freeze ($freezesLeft available)", fontWeight = FontWeight.SemiBold, color = TextHigh)
                                Text("Shields streak if missed", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                            }
                        }
                    }

                    // Attach Note
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                habitNoteInput = item.todayNote
                                showNoteDialogForHabit = item
                                habitForOptions = null
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = FlowEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Add Habit Note", fontWeight = FontWeight.SemiBold, color = TextHigh)
                                Text(if (item.todayNote.isNotBlank()) "Note: ${item.todayNote}" else "Add a reflection note", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { habitForOptions = null }) {
                    Text("Close", color = FlowTeal)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Attach Note Dialog
    showNoteDialogForHabit?.let { item ->
        AlertDialog(
            onDismissRequest = { showNoteDialogForHabit = null },
            title = { Text("Add Note for ${item.habit.name}") },
            text = {
                OutlinedTextField(
                    value = habitNoteInput,
                    onValueChange = { habitNoteInput = it },
                    label = { Text("Reflection / Session Note") },
                    placeholder = { Text("e.g. Completed after my evening study session.") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleHabit(item.habit.id, DateUtils.getTodayString(), habitNoteInput)
                        showNoteDialogForHabit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialogForHabit = null }) {
                    Text("Cancel", color = TextMedium)
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
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.getFormattedDate(DateUtils.getTodayString(), "EEEE, MMM d").uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = FlowTeal,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Good day, ${user?.fullName?.ifBlank { user?.username } ?: "Explorer"}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextHigh
                    )
                }

                // AI Planner Assistant Shortcut
                IconButton(
                    onClick = onNavigateToAi,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(FlowTeal.copy(alpha = 0.15f))
                        .testTag("home_ai_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "FLOW AI Planner",
                        tint = FlowTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Daily FLOW Score Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(FlowTeal.copy(alpha = 0.5f), DarkBorder))),
                modifier = Modifier.fillMaxWidth().testTag("flow_score_card")
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "FLOW SCORE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextMedium,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", flowScore),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = FlowTeal,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " / 10",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextMedium,
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                )
                            }
                        }

                        // Circular Progress Indicator
                        val ratio = if (habits.isNotEmpty()) todayCompleted.toFloat() / habits.size.toFloat() else 1f
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(68.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = DarkBorder,
                                strokeWidth = 7.dp
                            )
                            CircularProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxSize(),
                                color = FlowTeal,
                                strokeWidth = 7.dp
                            )
                            Text(
                                text = "${(ratio * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextHigh
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricPill("Completed", "$todayCompleted/${habits.size}", FlowEmerald)
                        val remaining = (habits.size - todayCompleted).coerceAtLeast(0)
                        MetricPill("Remaining", "$remaining", if (remaining > 0) FlowAmber else FlowEmerald)
                        val bestStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
                        MetricPill("Max Streak", "${bestStreak}d", FlowTeal)
                    }
                }
            }
        }

        // Active Challenges & Deadlines Banner
        if (challenges.isNotEmpty() || deadlines.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Focus & Deadlines", style = MaterialTheme.typography.titleLarge, color = TextHigh)
                    TextButton(onClick = onNavigateToChallenges) {
                        Text("View All", color = FlowTeal)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Show top deadline
                    deadlines.firstOrNull { !it.isCompleted }?.let { dl ->
                        val days = DateUtils.getDaysRemaining(dl.endDate)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(FlowRose.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = FlowRose)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dl.title, style = MaterialTheme.typography.titleMedium, color = TextHigh)
                                    Text(
                                        text = if (days != null && days >= 0) "Deadline in $days days (${dl.endDate})" else "Due ${dl.endDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FlowRose
                                    )
                                }
                            }
                        }
                    }

                    // Show top challenge
                    challenges.firstOrNull { !it.isCompleted }?.let { ch ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = FlowAmber, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(ch.title, style = MaterialTheme.typography.titleMedium, color = TextHigh)
                                    }
                                    Text(
                                        "${ch.currentProgress}/${ch.targetCount}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = FlowAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val prog = if (ch.targetCount > 0) (ch.currentProgress.toFloat() / ch.targetCount.toFloat()).coerceIn(0f, 1f) else 0f
                                LinearProgressIndicator(
                                    progress = { prog },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = FlowAmber,
                                    trackColor = DarkBorder
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Habits Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Habits", style = MaterialTheme.typography.titleLarge, color = TextHigh)
                    Text("Tap checkbox to complete or undo", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                }

                Row {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar Heatmap", tint = FlowTeal)
                    }
                    IconButton(onClick = onNavigateToHabits, modifier = Modifier.testTag("add_habit_quick_button")) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Habit", tint = FlowTeal, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // Habits List
        if (habits.isEmpty()) {
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Spa, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(44.dp))
                        Text("No active habits yet", style = MaterialTheme.typography.titleMedium, color = TextHigh)
                        Text(
                            "Start building your personal momentum with clean, deliberate daily rituals.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onNavigateToHabits,
                            colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create First Habit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(habits, key = { it.habit.id }) { item ->
                HabitCardItem(
                    item = item,
                    onToggleComplete = { viewModel.toggleHabit(item.habit.id, DateUtils.getTodayString()) },
                    onOptionsClick = { habitForOptions = item }
                )
            }
        }
    }
}

@Composable
fun HabitCardItem(
    item: HabitWithStats,
    onToggleComplete: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val isDone = item.isCompletedToday
    val isRest = item.todayStatus == "REST_DAY"
    val isFreeze = item.todayStatus == "FREEZE_PROTECTED"
    val isSkipped = item.todayStatus == "SKIPPED"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) DarkSurfaceElevated.copy(alpha = 0.8f) else DarkSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isDone) FlowTeal.copy(alpha = 0.4f) else DarkBorder
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_item_${item.habit.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Circle (One tap toggle with Undo support)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDone) FlowTeal
                        else if (isRest) FlowIndigo
                        else if (isFreeze) FlowTealGlow
                        else Color.Transparent
                    )
                    .clickable { onToggleComplete() }
                    .then(
                        if (!isDone && !isRest && !isFreeze) Modifier.background(DarkBorder, CircleShape) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = "Completed", tint = DarkBackground, modifier = Modifier.size(20.dp))
                } else if (isRest) {
                    Icon(Icons.Default.Hotel, contentDescription = "Rest Day", tint = DarkBackground, modifier = Modifier.size(18.dp))
                } else if (isFreeze) {
                    Icon(Icons.Default.AcUnit, contentDescription = "Freeze Used", tint = DarkBackground, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Habit Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDone) TextHigh.copy(alpha = 0.85f) else TextHigh,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category & Difficulty tag
                    Text(
                        text = "${item.habit.category} • ${item.habit.difficulty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium
                    )

                    // Streak Badge
                    if (item.currentStreak > 0) {
                        Surface(
                            color = FlowTeal.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥 ${item.currentStreak}d",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FlowTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Status Indicator if Rest or Freeze
                    if (isRest) {
                        Text("Rest Day", color = FlowIndigo, style = MaterialTheme.typography.labelSmall)
                    } else if (isFreeze) {
                        Text("Freeze Shield", color = FlowTeal, style = MaterialTheme.typography.labelSmall)
                    } else if (isSkipped) {
                        Text("Skipped", color = TextLow, style = MaterialTheme.typography.labelSmall)
                    }
                }

                // If note exists
                if (item.todayNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Note: ${item.todayNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FlowEmerald,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Options (Rest, Freeze, Note, Undo)
            IconButton(onClick = onOptionsClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMedium)
            }
        }
    }
}

@Composable
fun MetricPill(label: String, value: String, valueColor: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
