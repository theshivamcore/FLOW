package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitWithStats
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel
import com.example.util.DateUtils

data class HabitTemplate(
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val reminderTime: String,
    val reminderMessage: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(viewModel: FlowViewModel) {
    val habitsWithStats by viewModel.habitsWithStats.collectAsState()
    val archivedHabits by viewModel.archivedHabits.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var showArchivedDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var showSmartSuggestion by remember { mutableStateOf(true) }

    // Habit Form States
    var habitName by remember { mutableStateOf("") }
    var habitDesc by remember { mutableStateOf("") }
    var habitCat by remember { mutableStateOf("Productivity") }
    var habitDiff by remember { mutableStateOf("Medium") }
    var habitFreq by remember { mutableStateOf("Daily") }
    var habitDeadline by remember { mutableStateOf("") }
    var habitReminderTime by remember { mutableStateOf("08:00 AM") }
    var habitReminderMsg by remember { mutableStateOf("") }

    val categories = listOf("All", "Study", "Health", "Fitness", "Reading", "Sleep", "Productivity", "Personal", "Custom")

    val readyTemplates = listOf(
        HabitTemplate("30-Day Study Challenge", "Deep focus block with zero phone distractions.", "Study", "Hard", "09:00 AM", "Time for 60 min deep study. Future you will thank you."),
        HabitTemplate("Morning Routine", "Hydrate, cold shower, 5 min intentional mindfulness.", "Productivity", "Medium", "07:30 AM", "Win the morning to win the day."),
        HabitTemplate("Daily Reading", "Read 20 pages of high-leverage non-fiction.", "Reading", "Easy", "09:30 PM", "20 pages tonight compounds into immense wisdom."),
        HabitTemplate("Exam Preparation", "Work through 3 practice modules with timers.", "Study", "Hard", "04:00 PM", "Consistent exam prep removes all anxiety."),
        HabitTemplate("Sleep Wind Down", "No screens 45 minutes before sleep + dark room.", "Sleep", "Medium", "10:30 PM", "Deep restorative sleep is your superpower."),
        HabitTemplate("HIIT & Fitness", "30 minutes high intensity workout or run.", "Fitness", "Hard", "06:30 PM", "Push past comfort. Your body will thank you."),
        HabitTemplate("Hydration & Vitality", "Drink 2.5L fresh water throughout the day.", "Health", "Easy", "08:30 AM", "Hydrate now for sustained mental clarity.")
    )

    // Pre-fill form when editing
    LaunchedEffect(editingHabit) {
        editingHabit?.let { h ->
            habitName = h.name
            habitDesc = h.description
            habitCat = h.category
            habitDiff = h.difficulty
            habitFreq = h.frequency
            habitDeadline = h.deadline ?: ""
            habitReminderTime = h.reminderTime ?: "08:00 AM"
            habitReminderMsg = h.customReminderMessage
            showAddDialog = true
        }
    }

    // Filter habits by category
    val filteredHabits = remember(habitsWithStats, selectedCategory) {
        if (selectedCategory == "All") habitsWithStats
        else habitsWithStats.filter { it.habit.category.equals(selectedCategory, ignoreCase = true) }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingHabit = null
            },
            title = {
                Text(
                    text = if (editingHabit != null) "Edit Habit" else "Create New Habit",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("Habit Name *") },
                        placeholder = { Text("e.g. Study Physics") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("habit_name_input")
                    )

                    OutlinedTextField(
                        value = habitDesc,
                        onValueChange = { habitDesc = it },
                        label = { Text("Description / Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Study", "Health", "Fitness", "Reading", "Sleep", "Productivity", "Personal").forEach { cat ->
                            FilterChip(
                                selected = habitCat == cat,
                                onClick = { habitCat = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    // Difficulty Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Difficulty:", style = MaterialTheme.typography.bodyMedium, color = TextMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Easy", "Medium", "Hard").forEach { diff ->
                                FilterChip(
                                    selected = habitDiff == diff,
                                    onClick = { habitDiff = diff },
                                    label = { Text(diff) }
                                )
                            }
                        }
                    }

                    // Frequency & Reminder
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = habitFreq,
                            onValueChange = { habitFreq = it },
                            label = { Text("Frequency") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = habitReminderTime,
                            onValueChange = { habitReminderTime = it },
                            label = { Text("Reminder") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Custom Reminder Message
                    OutlinedTextField(
                        value = habitReminderMsg,
                        onValueChange = { habitReminderMsg = it },
                        label = { Text("Custom Reminder Message") },
                        placeholder = { Text("e.g. 30 minutes of Physics now. Future you will thank you.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Optional Deadline
                    OutlinedTextField(
                        value = habitDeadline,
                        onValueChange = { habitDeadline = it },
                        label = { Text("Optional Deadline (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            if (editingHabit != null) {
                                viewModel.updateHabit(
                                    editingHabit!!.copy(
                                        name = habitName.trim(),
                                        description = habitDesc.trim(),
                                        category = habitCat,
                                        difficulty = habitDiff,
                                        frequency = habitFreq,
                                        deadline = habitDeadline.ifBlank { null },
                                        reminderTime = habitReminderTime.ifBlank { null },
                                        customReminderMessage = habitReminderMsg.trim()
                                    )
                                )
                            } else {
                                viewModel.addHabit(
                                    Habit(
                                        userId = 0,
                                        name = habitName.trim(),
                                        description = habitDesc.trim(),
                                        category = habitCat,
                                        difficulty = habitDiff,
                                        frequency = habitFreq,
                                        startDate = DateUtils.getTodayString(),
                                        deadline = habitDeadline.ifBlank { null },
                                        reminderTime = habitReminderTime.ifBlank { null },
                                        customReminderMessage = habitReminderMsg.trim()
                                    )
                                )
                            }
                            showAddDialog = false
                            editingHabit = null
                            habitName = ""
                            habitDesc = ""
                            habitReminderMsg = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground),
                    modifier = Modifier.testTag("save_habit_button")
                ) {
                    Text(if (editingHabit != null) "Update" else "Save Habit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingHabit = null
                }) {
                    Text("Cancel", color = TextMedium)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Templates Picker Dialog
    if (showTemplatesDialog) {
        AlertDialog(
            onDismissRequest = { showTemplatesDialog = false },
            title = { Text("Choose a Habit Template", style = MaterialTheme.typography.titleLarge) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(readyTemplates) { tmpl ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    habitName = tmpl.title
                                    habitDesc = tmpl.description
                                    habitCat = tmpl.category
                                    habitDiff = tmpl.difficulty
                                    habitReminderTime = tmpl.reminderTime
                                    habitReminderMsg = tmpl.reminderMessage
                                    showTemplatesDialog = false
                                    showAddDialog = true
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(tmpl.title, fontWeight = FontWeight.Bold, color = TextHigh)
                                    Text(tmpl.difficulty, color = FlowTeal, style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(tmpl.description, style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Reminder: \"${tmpl.reminderMessage}\"", style = MaterialTheme.typography.bodySmall, color = FlowEmerald)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplatesDialog = false }) {
                    Text("Close", color = FlowTeal)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Delete Confirmation Dialog
    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete Habit?") },
            text = {
                Text("Are you sure you want to permanently delete '${habit.name}'? You can also archive it to preserve all streaks and historical stats.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHabit(habit.id)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlowRose)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel", color = TextMedium)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Archived Habits View Dialog
    if (showArchivedDialog) {
        AlertDialog(
            onDismissRequest = { showArchivedDialog = false },
            title = { Text("Archived Habits (${archivedHabits.size})") },
            text = {
                if (archivedHabits.isEmpty()) {
                    Text("No habits currently archived.", color = TextMedium)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(archivedHabits) { arch ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(arch.name, fontWeight = FontWeight.Bold, color = TextHigh)
                                    Text("${arch.category} • ${arch.difficulty}", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                }
                                TextButton(onClick = { viewModel.archiveHabit(arch.id, false) }) {
                                    Text("Unarchive", color = FlowTeal)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchivedDialog = false }) {
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("RITUALS & HABITS", style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                    Text("Habit System", style = MaterialTheme.typography.headlineLarge, color = TextHigh)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showTemplatesDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .testTag("templates_button")
                    ) {
                        Icon(Icons.Default.Widgets, contentDescription = "Templates", tint = FlowTeal)
                    }

                    IconButton(
                        onClick = {
                            editingHabit = null
                            habitName = ""
                            habitDesc = ""
                            habitReminderMsg = ""
                            showAddDialog = true
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FlowTeal)
                            .testTag("create_habit_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = DarkBackground)
                    }
                }
            }
        }

        // Smarter Reminder Suggestion Card
        if (showSmartSuggestion && habitsWithStats.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FlowIndigo.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlowIndigo.copy(alpha = 0.3f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = FlowIndigo, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Smarter Reminder Suggestion", fontWeight = FontWeight.Bold, color = TextHigh)
                            Text(
                                "You usually complete evening study habits around 08:30 PM. Would you like to set your reminders then?",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMedium
                            )
                        }
                        IconButton(onClick = { showSmartSuggestion = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextLow)
                        }
                    }
                }
            }
        }

        // Categories Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.setCategoryFilter(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FlowTeal,
                            selectedLabelColor = DarkBackground
                        )
                    )
                }
            }
        }

        // Habit Cards
        if (filteredHabits.isEmpty()) {
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(36.dp))
                        Text("No habits in '$selectedCategory'", style = MaterialTheme.typography.titleMedium, color = TextHigh)
                        Text("Pick a ready-made template or create your own custom habit.", style = MaterialTheme.typography.bodyMedium, color = TextMedium)
                    }
                }
            }
        } else {
            items(filteredHabits, key = { it.habit.id }) { item ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextHigh)
                                if (item.habit.description.isNotBlank()) {
                                    Text(item.habit.description, style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                }
                            }

                            // Quick Streak Tag
                            Surface(
                                color = FlowTeal.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔥 ${item.currentStreak}d (best ${item.bestStreak}d)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = FlowTeal,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Frequency, Category & Reminder details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(6.dp)) {
                                    Text(item.habit.category, style = MaterialTheme.typography.labelSmall, color = TextMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(6.dp)) {
                                    Text(item.habit.difficulty, style = MaterialTheme.typography.labelSmall, color = FlowEmerald, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                item.habit.reminderTime?.let { rem ->
                                    Surface(color = DarkSurfaceElevated, shape = RoundedCornerShape(6.dp)) {
                                        Text("⏰ $rem", style = MaterialTheme.typography.labelSmall, color = FlowAmber, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                            }

                            // Actions Row
                            Row {
                                IconButton(onClick = { editingHabit = item.habit }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextMedium, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { viewModel.archiveHabit(item.habit.id, true) }) {
                                    Icon(Icons.Default.Archive, contentDescription = "Archive", tint = TextMedium, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { habitToDelete = item.habit }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FlowRose, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        if (item.habit.customReminderMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💬 \"${item.habit.customReminderMessage}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = FlowTeal.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Archived Habits footer link
        if (archivedHabits.isNotEmpty()) {
            item {
                TextButton(
                    onClick = { showArchivedDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View ${archivedHabits.size} Archived Habits", color = TextMedium)
                }
            }
        }
    }
}
