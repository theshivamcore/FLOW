package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel

@Composable
fun ProfileScreen(viewModel: FlowViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val usernameAvailability by viewModel.usernameAvailability.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editFullName by remember { mutableStateOf("") }
    var editUsername by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editPublicProfile by remember { mutableStateOf(true) }
    var editShowHabits by remember { mutableStateOf(true) }
    var editShowStreaks by remember { mutableStateOf(true) }
    var editShowChallenges by remember { mutableStateOf(true) }
    var editOptInLeaderboard by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        user?.let { u ->
            editFullName = u.fullName
            editUsername = u.username
            editBio = u.bio
            editPublicProfile = u.isPublicProfile
            editShowHabits = u.showHabitsPublicly
            editShowStreaks = u.showStreaksPublicly
            editShowChallenges = u.showChallengesPublicly
            editOptInLeaderboard = u.optInLeaderboard
        }
    }

    // Edit Profile Modal
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile & Privacy", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = {
                                editUsername = it.lowercase().trim()
                                viewModel.checkUsername(it, user?.id ?: -1)
                            },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (editUsername.length >= 3 && editUsername != user?.username) {
                            if (usernameAvailability == false) {
                                Text("Username unavailable", color = FlowRose, style = MaterialTheme.typography.labelSmall)
                            } else if (usernameAvailability == true) {
                                Text("✓ Username available", color = FlowEmerald, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = DarkBorder)
                    Text("Privacy & Visibility Controls:", style = MaterialTheme.typography.labelMedium, color = FlowTeal)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Public Profile", color = TextHigh)
                        Switch(checked = editPublicProfile, onCheckedChange = { editPublicProfile = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Streaks Publicly", color = TextHigh)
                        Switch(checked = editShowStreaks, onCheckedChange = { editShowStreaks = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Habits Publicly", color = TextHigh)
                        Switch(checked = editShowHabits, onCheckedChange = { editShowHabits = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Opt-in Friends Leaderboard", color = TextHigh)
                        Switch(checked = editOptInLeaderboard, onCheckedChange = { editOptInLeaderboard = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        user?.let { u ->
                            if (editUsername != u.username && usernameAvailability == false) {
                                return@Button
                            }
                            viewModel.updateProfile(
                                u.copy(
                                    fullName = editFullName.trim(),
                                    username = editUsername.trim(),
                                    bio = editBio.trim(),
                                    isPublicProfile = editPublicProfile,
                                    showStreaksPublicly = editShowStreaks,
                                    showHabitsPublicly = editShowHabits,
                                    showChallengesPublicly = editShowChallenges,
                                    optInLeaderboard = editOptInLeaderboard
                                )
                            )
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlowTeal, contentColor = DarkBackground)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
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
            Column {
                Text("ACCOUNT & PREFERENCES", style = MaterialTheme.typography.labelMedium, color = FlowTeal, letterSpacing = 1.sp)
                Text("Profile & Settings", style = MaterialTheme.typography.headlineLarge, color = TextHigh)
            }
        }

        // User Avatar Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(FlowTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.fullName?.take(1) ?: user?.username?.take(1) ?: "F").uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = FlowTeal
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(user?.fullName?.ifBlank { "FLOW Practitioner" } ?: "FLOW Practitioner", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextHigh)
                            Text("@${user?.username ?: "user"}", style = MaterialTheme.typography.bodyMedium, color = FlowTeal)
                            Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                        }

                        IconButton(onClick = { showEditProfileDialog = true }, modifier = Modifier.testTag("edit_profile_button")) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = FlowTeal)
                        }
                    }

                    if (!user?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(user?.bio ?: "", style = MaterialTheme.typography.bodyMedium, color = TextHigh)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DarkBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Streak Freezes Bank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = FlowTeal)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Streak Freezes Available", fontWeight = FontWeight.Bold, color = TextHigh)
                                Text("Protects streak if a day is missed", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                            }
                        }

                        Text("${user?.availableFreezes ?: 0} / 3", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FlowTeal)
                    }
                }
            }
        }

        // Actions & Social Sharing
        item {
            Text("Community & Support", style = MaterialTheme.typography.titleLarge, color = TextHigh)
        }

        // Invite Friends Button
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Join me on FLOW")
                            putExtra(Intent.EXTRA_TEXT, "I'm elevating my daily habits and flow state with FLOW! Download the app and connect with me @${user?.username ?: ""}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Invite Friends to FLOW"))
                    }
                    .testTag("invite_friends_button"),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(FlowTeal.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = FlowTeal)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Invite Friends to FLOW", fontWeight = FontWeight.Bold, color = TextHigh)
                        Text("Share your personal invite link", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMedium)
                }
            }
        }

        // Send Feedback Button (to thenirantar@gmail.com)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:thenirantar@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "FLOW Habit Tracker Feedback - @${user?.username ?: ""}")
                            putExtra(Intent.EXTRA_TEXT, "Hello Nirantar,\n\nI have the following feedback/suggestion for FLOW:\n\n")
                        }
                        try {
                            context.startActivity(Intent.createChooser(emailIntent, "Send Feedback via Email"))
                        } catch (e: Exception) {
                            // Fallback generic send
                            val generic = Intent(Intent.ACTION_SEND).apply {
                                type = "message/rfc822"
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("thenirantar@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "FLOW Habit Tracker Feedback")
                            }
                            context.startActivity(Intent.createChooser(generic, "Send Feedback"))
                        }
                    }
                    .testTag("send_feedback_button"),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(FlowEmerald.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Feedback, contentDescription = null, tint = FlowEmerald)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Send Feedback & Support", fontWeight = FontWeight.Bold, color = TextHigh)
                        Text("Direct contact: thenirantar@gmail.com", style = MaterialTheme.typography.bodySmall, color = TextMedium)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMedium)
                }
            }
        }

        // Logout Button
        item {
            OutlinedButton(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FlowRose),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}
