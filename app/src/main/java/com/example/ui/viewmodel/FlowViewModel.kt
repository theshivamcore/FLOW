package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FlowAiService
import com.example.data.model.*
import com.example.data.repository.FlowRepository
import com.example.util.DateUtils
import com.example.util.ExportUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MotivationalQuote(
    val quote: String,
    val author: String
)

@OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FlowViewModel(application: Application) : AndroidViewModel(application) {
    val repository = FlowRepository(application)
    private val aiService = FlowAiService()

    // --- Auth State ---
    val currentUser = repository.getCurrentUser().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _usernameAvailability = MutableStateFlow<Boolean?>(null)
    val usernameAvailability: StateFlow<Boolean?> = _usernameAvailability.asStateFlow()

    private var usernameCheckJob: Job? = null

    // --- Active screen / Navigation ---
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // --- Habits ---
    val habitsWithStats = repository.getHabitsWithStats().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val archivedHabits = repository.getArchivedHabits().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    // --- Calendar & Heatmap ---
    private val _selectedCalendarDate = MutableStateFlow(DateUtils.getTodayString())
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    fun selectCalendarDate(date: String) {
        _selectedCalendarDate.value = date
    }

    val allCompletions = repository.getAllCompletions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Challenges & Goals ---
    val challenges = repository.getChallenges().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val deadlines = repository.getDeadlines().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val goals = repository.getGoals().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Milestones ---
    val milestones = repository.getMilestones().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- FLOW Score & Summary Metrics ---
    val flowScore: StateFlow<Double> = combine(habitsWithStats) { habitsArray ->
        val habits = habitsArray[0]
        val total = habits.size
        val done = habits.count { it.isCompletedToday }
        val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        DateUtils.calculateFlowScore(total, done, maxStreak)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10.0)

    val todayCompletedCount: StateFlow<Int> = habitsWithStats.map { list ->
        list.count { it.isCompletedToday }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Daily Motivational Quote ---
    private val quotesList = listOf(
        MotivationalQuote("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Aristotle"),
        MotivationalQuote("Small disciplines repeated with consistency every day lead to great achievements gained slowly over time.", "John C. Maxwell"),
        MotivationalQuote("Focus is a muscle. The more you protect your calm, the deeper your flow becomes.", "Marcus Aurelius"),
        MotivationalQuote("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear"),
        MotivationalQuote("The secret of getting ahead is getting started.", "Mark Twain"),
        MotivationalQuote("Energy flows where attention goes. Protect your sacred focus.", "Ancient Proverb"),
        MotivationalQuote("Consistency is the catalyst that turns fleeting sparks into an enduring flame.", "FLOW Philosophy")
    )

    private val _currentQuote = MutableStateFlow(quotesList[Math.abs(DateUtils.getTodayString().hashCode()) % quotesList.size])
    val currentQuote: StateFlow<MotivationalQuote> = _currentQuote.asStateFlow()

    private val _showMotivationalDialog = MutableStateFlow(true)
    val showMotivationalDialog: StateFlow<Boolean> = _showMotivationalDialog.asStateFlow()

    fun dismissMotivationalDialog() {
        _showMotivationalDialog.value = false
    }

    // --- AI Companion & Day Planner ---
    val dayTasks = repository.getDayTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiMemories = repository.getAiMemories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiChatMessages = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            Pair("model", "Hello 👋 I am FLOW AI, your personal planning companion. Tell me how you're feeling, or say 'Plan my day' to map out your habits and priorities.")
        )
    )
    val aiChatMessages: StateFlow<List<Pair<String, String>>> = _aiChatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _suggestedPlan = MutableStateFlow<List<AiDayTask>?>(null)
    val suggestedPlan: StateFlow<List<AiDayTask>?> = _suggestedPlan.asStateFlow()

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank()) return
        val current = _aiChatMessages.value.toMutableList()
        current.add(Pair("user", prompt))
        _aiChatMessages.value = current

        _isAiThinking.value = true
        viewModelScope.launch {
            try {
                val (reply, plan) = aiService.chatWithFlowAi(
                    userMessage = prompt,
                    habits = habitsWithStats.value,
                    challenges = challenges.value,
                    goals = goals.value,
                    memories = aiMemories.value,
                    conversationHistory = current
                )
                val updated = _aiChatMessages.value.toMutableList()
                updated.add(Pair("model", reply))
                _aiChatMessages.value = updated
                _suggestedPlan.value = plan
            } catch (e: Exception) {
                val updated = _aiChatMessages.value.toMutableList()
                updated.add(Pair("model", "I'm right here with you. Let's take it one step at a time."))
                _aiChatMessages.value = updated
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun acceptSuggestedPlan() {
        val plan = _suggestedPlan.value ?: return
        viewModelScope.launch {
            repository.setDayTasks(DateUtils.getTodayString(), plan)
            _suggestedPlan.value = null
        }
    }

    fun dismissSuggestedPlan() {
        _suggestedPlan.value = null
    }

    fun toggleDayTask(task: AiDayTask) {
        viewModelScope.launch {
            repository.toggleDayTaskComplete(task)
        }
    }

    fun skipDayTask(task: AiDayTask) {
        viewModelScope.launch {
            repository.skipDayTask(task)
        }
    }

    fun addAiMemory(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.addAiMemory(title, content, category)
        }
    }

    fun deleteAiMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteAiMemory(id)
        }
    }

    // --- Search & Public Profiles ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<User>> = _searchQuery
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else repository.searchUsers(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _selectedPublicProfile = MutableStateFlow<Pair<User?, List<HabitWithStats>>?>(null)
    val selectedPublicProfile: StateFlow<Pair<User?, List<HabitWithStats>>?> = _selectedPublicProfile.asStateFlow()

    private val _isFollowingSelectedUser = MutableStateFlow(false)
    val isFollowingSelectedUser: StateFlow<Boolean> = _isFollowingSelectedUser.asStateFlow()

    val leaderboardUsers = repository.getLeaderboardUsers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun viewPublicProfile(userId: Long) {
        viewModelScope.launch {
            val data = repository.getPublicUserData(userId)
            _selectedPublicProfile.value = data
            repository.isFollowing(userId).collectLatest { isF ->
                _isFollowingSelectedUser.value = isF
            }
        }
    }

    fun clearSelectedPublicProfile() {
        _selectedPublicProfile.value = null
    }

    fun toggleFollowSelectedUser(targetUserId: Long) {
        viewModelScope.launch {
            if (_isFollowingSelectedUser.value) {
                repository.unfollowUser(targetUserId)
            } else {
                repository.followUser(targetUserId)
            }
        }
    }

    // --- Auth Actions ---
    fun checkUsername(username: String, currentUserId: Long = -1) {
        usernameCheckJob?.cancel()
        if (username.trim().length < 3) {
            _usernameAvailability.value = null
            return
        }
        usernameCheckJob = viewModelScope.launch {
            delay(250) // debounce typing
            val available = repository.isUsernameAvailable(username, currentUserId)
            _usernameAvailability.value = available
        }
    }

    fun signup(username: String, email: String, password: String, fullName: String) {
        _authError.value = null
        viewModelScope.launch {
            val result = repository.registerUser(username, email, password, fullName)
            result.onFailure {
                _authError.value = it.message ?: "Sign up failed"
            }
        }
    }

    fun login(emailOrUser: String, password: String) {
        _authError.value = null
        viewModelScope.launch {
            val result = repository.loginUser(emailOrUser, password)
            result.onFailure {
                _authError.value = it.message ?: "Login failed"
            }
        }
    }

    fun resetPassword(email: String, newPass: String) {
        _authError.value = null
        viewModelScope.launch {
            val result = repository.resetPassword(email, newPass)
            result.onFailure {
                _authError.value = it.message ?: "Reset password failed"
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            val res = repository.updateUserProfile(user)
            res.onFailure {
                _authError.value = it.message
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _selectedPublicProfile.value = null
            _currentScreen.value = "home"
        }
    }

    // --- Habit Operations ---
    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: 1L
            repository.addHabit(habit.copy(userId = userId))
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun archiveHabit(habitId: Long, archive: Boolean) {
        viewModelScope.launch {
            repository.archiveHabit(habitId, archive)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun toggleHabit(habitId: Long, date: String = DateUtils.getTodayString(), note: String = "") {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, date, note)
        }
    }

    fun markRestDay(habitId: Long, date: String) {
        viewModelScope.launch {
            repository.setHabitStatus(habitId, date, "REST_DAY", "Rest day taken")
        }
    }

    fun markSkipped(habitId: Long, date: String) {
        viewModelScope.launch {
            repository.setHabitStatus(habitId, date, "SKIPPED", "Skipped")
        }
    }

    fun useFreeze(habitId: Long, date: String) {
        viewModelScope.launch {
            repository.useStreakFreeze(habitId, date)
        }
    }

    // --- Challenge & Deadline Operations ---
    fun addChallenge(challenge: Challenge) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: 1L
            repository.addChallenge(challenge.copy(userId = userId))
        }
    }

    fun updateChallengeProgress(id: Long, newProgress: Int, target: Int) {
        viewModelScope.launch {
            repository.updateChallengeProgress(id, newProgress, target)
        }
    }

    fun deleteChallenge(id: Long) {
        viewModelScope.launch {
            repository.deleteChallenge(id)
        }
    }

    // --- Personal Goal Operations ---
    fun addGoal(goal: PersonalGoal) {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: 1L
            repository.addGoal(goal.copy(userId = userId))
        }
    }

    fun updateGoal(goal: PersonalGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    // --- Reports Generation & Export ---
    fun exportReport(context: Context, reportType: String, asPdf: Boolean) {
        val user = currentUser.value ?: return
        val habits = habitsWithStats.value
        val totalCompletions = habits.sumOf { it.totalCompletions }
        val maxStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        val bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
        val compRate = if (habits.isNotEmpty()) ((todayCompletedCount.value.toFloat() / habits.size.toFloat()) * 100).toInt() else 100

        val data = ExportUtils.ReportData(
            reportType = reportType,
            user = user,
            completionRate = compRate,
            totalCompletions = totalCompletions,
            activeHabitsCount = habits.size,
            currentStreak = maxStreak,
            bestStreak = bestStreak,
            flowScore = flowScore.value,
            items = habits
        )

        viewModelScope.launch {
            if (asPdf) {
                val file = ExportUtils.generatePdfReport(context, data)
                ExportUtils.shareFile(context, file, "application/pdf", "FLOW $reportType Report")
            } else {
                val file = ExportUtils.generateJpgReport(context, data)
                ExportUtils.shareFile(context, file, "image/jpeg", "FLOW $reportType Summary")
            }
        }
    }

    fun exportFullData(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportAllDataJson()
            onResult(json)
        }
    }
}
