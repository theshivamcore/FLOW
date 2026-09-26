package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel

@Composable
fun SocialScreen(viewModel: FlowViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedProfile by viewModel.selectedPublicProfile.collectAsState()
    val isFollowing by viewModel.isFollowingSelectedUser.collectAsState()
    val leaderboardUsers by viewModel.leaderboardUsers.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Search Users, 1: Community Leaderboard

    // Profile detail modal if a user is clicked
    selectedProfile?.let { (user, stats) ->
        if (user != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearSelectedPublicProfile() },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("@${user.username}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(user.fullName.ifBlank { "FLOW Member" }, style = MaterialTheme.typography.bodySmall, color = TextMedium)
                        }

                        Button(
                            onClick = { viewModel.toggleFollowSelectedUser(user.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) DarkSurfaceElevated else FlowTeal,
                                contentColor = if (isFollowing) TextHigh else DarkBackground
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isFollowing) "Following" else "Follow")
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(user.bio, style = MaterialTheme.typography.bodyMedium, color = TextHigh)

                        if (!user.isPublicProfile) {
                            Surface(
                                color = DarkSurfaceElevated,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextLow)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("This user has set their profile to private.", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                }
                            }
                        } else {
                            // Public metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (user.showStreaksPublicly) {
                                    val maxStreak = stats.maxOfOrNull { it.currentStreak } ?: 0
                                    MetricPill("Top Streak", "🔥 ${maxStreak}d", FlowTeal)
                                }
                                if (user.showHabitsPublicly) {
                                    MetricPill("Public Habits", "${stats.size}", FlowEmerald)
                                }
                                MetricPill("Completions", "${stats.sumOf { it.totalCompletions }}", FlowAmber)
                            }

                            if (user.showHabitsPublicly && stats.isNotEmpty()) {
                                HorizontalDivider(color = DarkBorder)
                                Text("Publicly Shared Habits:", style = MaterialTheme.typography.labelMedium, color = TextMedium)
                                stats.take(4).forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.habit.name, style = MaterialTheme.typography.bodySmall, color = TextHigh)
                                        Text("🔥 ${item.currentStreak}d", style = MaterialTheme.typography.bodySmall, color = FlowTeal)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearSelectedPublicProfile() }) {
                        Text("Close", color = FlowTeal)
                    }
                },
                containerColor = DarkSurface
            )
        }
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
            Column {
                Text("FLOW SOCIAL & COMMUNITY", style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                Text("Search & Connect", style = MaterialTheme.typography.headlineLarge, color = TextHigh)
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = DarkSurface,
                contentColor = FlowTeal,
                indicator = {}
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Text("Search Users", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal, color = if (activeTab == 0) FlowTeal else TextMedium)
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text("Leaderboard", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal, color = if (activeTab == 1) FlowTeal else TextMedium)
                    }
                )
            }
        }

        if (activeTab == 0) {
            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Search by exact or partial @username") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FlowTeal) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextLow)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("username_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FlowTeal,
                        focusedLabelColor = FlowTeal
                    )
                )
            }

            if (searchQuery.isBlank()) {
                item {
                    Text(
                        "Search another person using their exact username to see their public habits, streaks and challenges.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMedium
                    )
                }
            } else if (searchResults.isEmpty()) {
                item {
                    Text("No users found matching '$searchQuery'", color = TextLow)
                }
            } else {
                items(searchResults, key = { it.id }) { u ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.viewPublicProfile(u.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(FlowTeal.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = u.username.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = FlowTeal,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("@${u.username}", fontWeight = FontWeight.Bold, color = TextHigh)
                                    Text(u.fullName.ifBlank { "FLOW Member" }, style = MaterialTheme.typography.bodySmall, color = TextMedium)
                                }
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMedium)
                        }
                    }
                }
            }
        } else {
            // Optional Friends Leaderboard
            item {
                Text(
                    "Optional Leaderboard — Users who opted into public community sharing appear below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMedium
                )
            }

            if (leaderboardUsers.isEmpty()) {
                item {
                    Text("No public leaderboard participants currently.", color = TextLow)
                }
            } else {
                items(leaderboardUsers, key = { it.id }) { u ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.viewPublicProfile(u.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = FlowAmber)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("@${u.username}", fontWeight = FontWeight.Bold, color = TextHigh)
                                    Text(u.bio, style = MaterialTheme.typography.bodySmall, color = TextMedium, maxLines = 1)
                                }
                            }
                            Text("View Profile", color = FlowTeal, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
