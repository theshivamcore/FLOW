package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlowViewModel

data class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FlowViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Android system back button handler
    BackHandler(enabled = currentScreen != "home") {
        viewModel.navigateTo("home")
    }

    val navItems = listOf(
        NavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("habits", "Habits", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
        NavItem("challenges", "Challenges", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
        NavItem("reports", "Reports", Icons.Filled.Assessment, Icons.Outlined.Assessment),
        NavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FLOW",
                        style = MaterialTheme.typography.titleLarge,
                        color = FlowTeal,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                actions = {
                    // Quick Search shortcut
                    IconButton(
                        onClick = { viewModel.navigateTo("social") },
                        modifier = Modifier.testTag("top_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Users",
                            tint = if (currentScreen == "social") FlowTeal else TextMedium
                        )
                    }

                    // Calendar Heatmap shortcut
                    IconButton(
                        onClick = { viewModel.navigateTo("calendar") },
                        modifier = Modifier.testTag("top_calendar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar Heatmap",
                            tint = if (currentScreen == "calendar") FlowTeal else TextMedium
                        )
                    }

                    // Personal Goals shortcut
                    IconButton(
                        onClick = { viewModel.navigateTo("goals") },
                        modifier = Modifier.testTag("top_goals_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Personal Goals",
                            tint = if (currentScreen == "goals") FlowTeal else TextMedium
                        )
                    }

                    // FLOW Journey shortcut
                    IconButton(
                        onClick = { viewModel.navigateTo("journey") },
                        modifier = Modifier.testTag("top_journey_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "FLOW Journey",
                            tint = if (currentScreen == "journey") FlowTeal else TextMedium
                        )
                    }

                    // FLOW AI Planner shortcut
                    IconButton(
                        onClick = { viewModel.navigateTo("ai") },
                        modifier = Modifier.testTag("top_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "FLOW AI",
                            tint = if (currentScreen == "ai") FlowTeal else FlowTealGlow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = FlowTeal
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextHigh,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(item.route) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) DarkBackground else TextMedium
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) FlowTeal else TextMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = FlowTeal
                        ),
                        modifier = Modifier.testTag("nav_item_${item.route}")
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToHabits = { viewModel.navigateTo("habits") },
                    onNavigateToChallenges = { viewModel.navigateTo("challenges") },
                    onNavigateToAi = { viewModel.navigateTo("ai") },
                    onNavigateToCalendar = { viewModel.navigateTo("calendar") }
                )
                "habits" -> HabitsScreen(viewModel = viewModel)
                "calendar" -> CalendarScreen(viewModel = viewModel)
                "challenges" -> ChallengesScreen(viewModel = viewModel)
                "goals" -> GoalsScreen(viewModel = viewModel)
                "journey" -> FlowJourneyScreen(viewModel = viewModel)
                "reports" -> ReportsScreen(viewModel = viewModel)
                "social" -> SocialScreen(viewModel = viewModel)
                "ai" -> FlowAiScreen(viewModel = viewModel)
                "profile" -> ProfileScreen(viewModel = viewModel)
                else -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToHabits = { viewModel.navigateTo("habits") },
                    onNavigateToChallenges = { viewModel.navigateTo("challenges") },
                    onNavigateToAi = { viewModel.navigateTo("ai") },
                    onNavigateToCalendar = { viewModel.navigateTo("calendar") }
                )
            }
        }
    }
}
