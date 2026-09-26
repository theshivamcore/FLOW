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
import com.example.data.model.PersonalGoal
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel
import com.example.util.DateUtils

@Composable
fun GoalsScreen(viewModel: FlowViewModel) {
    val goals by viewModel.goals.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var goalTitle by remember { mutableStateOf("") }
    var goalDesc by remember { mutableStateOf("") }
    var goalDeadline by remember { mutableStateOf(DateUtils.getDaysAgo(-60)) }
    var goalCat by remember { mutableStateOf("Study") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Personal Goal", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title *") },
                        placeholder = { Text("e.g. Master Class 12 Preparation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
                    )
                    OutlinedTextField(
                        value = goalDesc,
                        onValueChange = { goalDesc = it },
                        label = { Text("Description & Milestones") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalDeadline,
                        onValueChange = { goalDeadline = it },
                        label = { Text("Target Deadline (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalTitle.isNotBlank()) {
                            viewModel.addGoal(
                                PersonalGoal(
                                    userId = 0,
                                    title = goalTitle.trim(),
                                    description = goalDesc.trim(),
                                    targetDeadline = goalDeadline.trim(),
                                    progressPercentage = 0,
                                    relatedCategory = goalCat
                                )
                            )
                            showCreateDialog = false
                            goalTitle = ""
                            goalDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground)
                ) {
                    Text("Create Goal", fontWeight = FontWeight.Bold)
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
                    Text("BIG PICTURE VISIONS", style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                    Text("Personal Goals", style = MaterialTheme.typography.headlineLarge, color = TextHigh)
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.clip(CircleShape).background(FlowTeal).testTag("add_goal_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = DarkBackground)
                }
            }
        }

        if (goals.isEmpty()) {
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
                        Icon(Icons.Default.Flag, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(40.dp))
                        Text("No personal goals yet", style = MaterialTheme.typography.titleMedium, color = TextHigh)
                        Text(
                            "Connect your daily habits to grander overarching milestones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                val daysLeft = DateUtils.getDaysRemaining(goal.targetDeadline)

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
                                Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextHigh)
                                if (goal.description.isNotBlank()) {
                                    Text(goal.description, style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                }
                            }

                            Text(
                                "${goal.progressPercentage}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = FlowTeal
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { goal.progressPercentage.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = FlowTeal,
                            trackColor = DarkSurfaceElevated
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (daysLeft != null && daysLeft >= 0) "Target: ${goal.targetDeadline} ($daysLeft days left)" else "Target: ${goal.targetDeadline}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMedium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        val next = (goal.progressPercentage + 10).coerceAtMost(100)
                                        viewModel.updateGoal(goal.copy(progressPercentage = next, isCompleted = next >= 100))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = FlowTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("+10%")
                                }
                                IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FlowRose, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
