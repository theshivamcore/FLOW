package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Challenge
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel
import com.example.util.DateUtils

@Composable
fun ChallengesScreen(viewModel: FlowViewModel) {
    val challenges by viewModel.challenges.collectAsState()
    val deadlines by viewModel.deadlines.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Challenges, 1: Deadlines
    var showCreateDialog by remember { mutableStateOf(false) }

    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("30") }
    var endDateInput by remember { mutableStateOf(DateUtils.getDaysAgo(-30)) }

    // Create Modal
    if (showCreateDialog) {
        val isDeadline = selectedTab == 1
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (isDeadline) "New Personal Deadline" else "New Challenge", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text(if (isDeadline) "Deadline Title *" else "Challenge Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("challenge_title_input")
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Description / Rules") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isDeadline) {
                        OutlinedTextField(
                            value = targetInput,
                            onValueChange = { targetInput = it },
                            label = { Text("Target Goal (e.g. 30 days, 20 workouts)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = endDateInput,
                        onValueChange = { endDateInput = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            val target = targetInput.toIntOrNull() ?: 30
                            viewModel.addChallenge(
                                Challenge(
                                    userId = 0,
                                    title = titleInput.trim(),
                                    description = descInput.trim(),
                                    startDate = DateUtils.getTodayString(),
                                    endDate = endDateInput.trim(),
                                    targetCount = if (isDeadline) 1 else target,
                                    currentProgress = 0,
                                    isDeadline = isDeadline
                                )
                            )
                            showCreateDialog = false
                            titleInput = ""
                            descInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("GOALS & TIMELINES", style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                    Text("Deadlines & Challenges", style = MaterialTheme.typography.headlineLarge, color = TextHigh)
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.clip(CircleShape).background(FlowTeal).testTag("add_challenge_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = DarkBackground)
                }
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = FlowTeal,
                indicator = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Challenges (${challenges.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) FlowTeal else TextMedium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Deadlines (${deadlines.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) FlowRose else TextMedium
                        )
                    }
                )
            }
        }

        val itemsList = if (selectedTab == 0) challenges else deadlines

        if (itemsList.isEmpty()) {
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.EmojiEvents else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = FlowTeal,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (selectedTab == 0) "No active challenges" else "No upcoming deadlines",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextHigh
                        )
                        Text(
                            text = if (selectedTab == 0) "Challenge yourself with a 30-day consistency sprint." else "Set a strict target deadline to spark focus.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(itemsList, key = { it.id }) { ch ->
                val daysRemaining = DateUtils.getDaysRemaining(ch.endDate)
                val progressRatio = if (ch.targetCount > 0) (ch.currentProgress.toFloat() / ch.targetCount.toFloat()).coerceIn(0f, 1f) else 0f

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
                                Text(ch.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextHigh)
                                if (ch.description.isNotBlank()) {
                                    Text(ch.description, style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                }
                            }

                            // Days Remaining Badge
                            Surface(
                                color = if (daysRemaining != null && daysRemaining <= 3) FlowRose.copy(alpha = 0.15f) else DarkSurfaceElevated,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (daysRemaining != null && daysRemaining >= 0) "$daysRemaining days left" else "Ended",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (daysRemaining != null && daysRemaining <= 3) FlowRose else FlowTeal,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar & Counts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress: ${ch.currentProgress} / ${ch.targetCount} (${(progressRatio * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMedium
                            )

                            // Quick progress increment for challenges
                            if (!ch.isDeadline && !ch.isCompleted) {
                                Button(
                                    onClick = { viewModel.updateChallengeProgress(ch.id, ch.currentProgress + 1, ch.targetCount) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("+1 Day", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            } else if (ch.isDeadline) {
                                IconButton(
                                    onClick = {
                                        viewModel.updateChallengeProgress(ch.id, if (ch.isCompleted) 0 else 1, 1)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (ch.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Toggle Complete",
                                        tint = if (ch.isCompleted) FlowEmerald else TextMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (ch.isCompleted) FlowEmerald else FlowTeal,
                            trackColor = DarkSurfaceElevated
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Timeline: ${ch.startDate} to ${ch.endDate}", style = MaterialTheme.typography.bodySmall, color = TextLow)
                            IconButton(onClick = { viewModel.deleteChallenge(ch.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FlowRose, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
