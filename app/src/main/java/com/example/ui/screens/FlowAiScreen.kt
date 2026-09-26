package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.AiDayTask
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel
import com.example.util.DateUtils

@Composable
fun FlowAiScreen(viewModel: FlowViewModel) {
    val chatMessages by viewModel.aiChatMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    val suggestedPlan by viewModel.suggestedPlan.collectAsState()
    val dayTasks by viewModel.dayTasks.collectAsState()
    val aiMemories by viewModel.aiMemories.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showMemoriesDialog by remember { mutableStateOf(false) }
    var newMemoryTitle by remember { mutableStateOf("") }
    var newMemoryContent by remember { mutableStateOf("") }
    var newMemoryCategory by remember { mutableStateOf("Routine") }
    var showAddMemoryDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom on new messages
    LaunchedEffect(chatMessages.size, isThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // AI Memories Management Dialog
    if (showMemoriesDialog) {
        AlertDialog(
            onDismissRequest = { showMemoriesDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Memory System", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { showAddMemoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Memory", tint = FlowTeal)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "FLOW AI remembers key routine preferences to personalize your day plans without judging. You have full control:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMedium
                    )

                    if (aiMemories.isEmpty()) {
                        Text("No saved memories yet.", color = TextLow)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(aiMemories, key = { it.id }) { mem ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(mem.title, fontWeight = FontWeight.Bold, color = TextHigh)
                                            Text(mem.content, style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                            Text(mem.category, style = MaterialTheme.typography.labelSmall, color = FlowTeal)
                                        }
                                        IconButton(onClick = { viewModel.deleteAiMemory(mem.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FlowRose, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMemoriesDialog = false }) {
                    Text("Done", color = FlowTeal)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Add Memory Dialog
    if (showAddMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemoryDialog = false },
            title = { Text("Save to AI Memory") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newMemoryTitle,
                        onValueChange = { newMemoryTitle = it },
                        label = { Text("Title / Preference") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newMemoryContent,
                        onValueChange = { newMemoryContent = it },
                        label = { Text("Content") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMemoryTitle.isNotBlank() && newMemoryContent.isNotBlank()) {
                            viewModel.addAiMemory(newMemoryTitle.trim(), newMemoryContent.trim(), newMemoryCategory)
                            showAddMemoryDialog = false
                            newMemoryTitle = ""
                            newMemoryContent = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground)
                ) {
                    Text("Remember This")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemoryDialog = false }) {
                    Text("Cancel", color = TextMedium)
                }
            },
            containerColor = DarkSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(bottom = 80.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FLOW AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextHigh)
                }
                Text("Personal Planning & Mindful Companion", style = MaterialTheme.typography.bodySmall, color = TextMedium)
            }

            IconButton(
                onClick = { showMemoriesDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .testTag("ai_memory_button")
            ) {
                Icon(Icons.Default.Psychology, contentDescription = "AI Memory", tint = FlowTeal)
            }
        }

        // Active Day Tasks Banner if present
        if (dayTasks.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val completedCount = dayTasks.count { it.isCompleted }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Today's AI Schedule", fontWeight = FontWeight.Bold, color = TextHigh)
                        Text("$completedCount / ${dayTasks.size} Done", style = MaterialTheme.typography.labelSmall, color = FlowTeal)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    dayTasks.take(3).forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleDayTask(task) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle",
                                    tint = if (task.isCompleted) FlowEmerald else TextMedium,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${task.timeSlot} • ${task.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (task.isCompleted) TextLow else TextHigh
                            )
                        }
                    }
                }
            }
        }

        // Suggested Plan Banner (Accept / Dismiss)
        suggestedPlan?.let { plan ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FlowTeal.copy(alpha = 0.12f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(FlowTeal.copy(alpha = 0.4f))),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Suggested Day Plan (${plan.size} blocks)", fontWeight = FontWeight.Bold, color = FlowTeal)
                        IconButton(onClick = { viewModel.dismissSuggestedPlan() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    plan.forEach { item ->
                        Text("• ${item.timeSlot} — ${item.title}", style = MaterialTheme.typography.bodySmall, color = TextHigh)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.acceptSuggestedPlan() },
                        colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("accept_plan_button")
                    ) {
                        Text("Accept & Sync to Today's Tasks", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Chat messages LazyColumn
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(chatMessages) { (role, message) ->
                val isUser = role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FlowTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = FlowTeal, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        color = if (isUser) FlowTeal else DarkSurfaceElevated,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isUser) DarkBackground else TextHigh,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = FlowTeal, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("FLOW AI is reflecting...", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                    }
                }
            }
        }

        // Quick Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Plan my day", "I have 4 hours", "Feeling stressed", "Simplify plan").forEach { suggestion ->
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { viewModel.sendAiMessage(suggestion) }
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = FlowTeal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask FLOW AI to plan or reflect...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlowTeal,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendAiMessage(textInput.trim())
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(FlowTeal)
                    .testTag("send_ai_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = DarkBackground)
            }
        }
    }
}
