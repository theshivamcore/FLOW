package com.example.data.repository

import android.content.Context
import com.example.data.local.FlowDatabase
import com.example.data.model.*
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FlowRepository(context: Context) {
    private val db = FlowDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val habitDao = db.habitDao()
    private val challengeDao = db.challengeDao()
    private val goalDao = db.goalDao()
    private val aiDao = db.aiDao()
    private val followDao = db.followDao()

    // Current active user ID state
    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    fun setCurrentUserId(id: Long?) {
        _currentUserId.value = id
    }

    // --- User Auth & Profile ---
    fun getCurrentUser(): Flow<User?> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null) else userDao.getUserById(id)
    }

    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByUsername(username.trim())
    }

    suspend fun isUsernameAvailable(username: String, excludeUserId: Long = -1): Boolean = withContext(Dispatchers.IO) {
        val trimmed = username.trim()
        if (trimmed.length < 3) return@withContext false
        userDao.countUsernameOccurrences(trimmed, excludeUserId) == 0
    }

    suspend fun registerUser(username: String, email: String, passwordHash: String, fullName: String): Result<User> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val cleanEmail = email.trim().lowercase()

        if (cleanUsername.length < 3) {
            return@withContext Result.failure(Exception("Username must be at least 3 characters"))
        }
        if (!isUsernameAvailable(cleanUsername)) {
            return@withContext Result.failure(Exception("Username unavailable"))
        }
        val existingEmail = userDao.getUserByEmail(cleanEmail)
        if (existingEmail != null) {
            return@withContext Result.failure(Exception("An account with this email already exists"))
        }

        val newUser = User(
            username = cleanUsername,
            email = cleanEmail,
            passwordHash = passwordHash,
            fullName = fullName.trim(),
            bio = "Habit architect in flow."
        )
        val id = userDao.insertUser(newUser)
        val created = newUser.copy(id = id)
        _currentUserId.value = id

        // Prepopulate with a great initial sample habit to get started
        initSampleDataForUser(id)

        Result.success(created)
    }

    suspend fun loginUser(emailOrUsername: String, passwordHash: String): Result<User> = withContext(Dispatchers.IO) {
        val query = emailOrUsername.trim().lowercase()
        val user = if (query.contains("@")) {
            userDao.getUserByEmail(query)
        } else {
            userDao.getUserByUsername(query)
        }

        if (user == null) {
            return@withContext Result.failure(Exception("User not found"))
        }
        if (user.passwordHash != passwordHash) {
            return@withContext Result.failure(Exception("Invalid credentials"))
        }

        _currentUserId.value = user.id
        Result.success(user)
    }

    suspend fun resetPassword(email: String, newPasswordHash: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email.trim().lowercase())
            ?: return@withContext Result.failure(Exception("No account registered with this email"))
        userDao.updateUser(user.copy(passwordHash = newPasswordHash))
        Result.success(true)
    }

    suspend fun updateUserProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        val available = isUsernameAvailable(user.username, excludeUserId = user.id)
        if (!available) {
            return@withContext Result.failure(Exception("Username unavailable"))
        }
        userDao.updateUser(user)
        Result.success(Unit)
    }

    suspend fun logout() {
        _currentUserId.value = null
    }

    // --- Habits ---
    fun getActiveHabits(): Flow<List<Habit>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else habitDao.getActiveHabits(id)
    }

    fun getArchivedHabits(): Flow<List<Habit>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else habitDao.getArchivedHabits(id)
    }

    suspend fun addHabit(habit: Habit): Long = withContext(Dispatchers.IO) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) = withContext(Dispatchers.IO) {
        habitDao.updateHabit(habit)
    }

    suspend fun archiveHabit(habitId: Long, archive: Boolean) = withContext(Dispatchers.IO) {
        val habit = habitDao.getHabitById(habitId) ?: return@withContext
        habitDao.updateHabit(habit.copy(isArchived = archive))
    }

    suspend fun deleteHabit(habitId: Long) = withContext(Dispatchers.IO) {
        habitDao.deleteHabitById(habitId)
        habitDao.deleteAllCompletionsForHabit(habitId)
    }

    // --- Completions & Stats ---
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else habitDao.getCompletionsForDate(id, date)
    }

    fun getAllCompletions(): Flow<List<HabitCompletion>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else habitDao.getAllCompletionsForUser(id)
    }

    suspend fun toggleHabitCompletion(habitId: Long, date: String = DateUtils.getTodayString(), note: String = "") = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value ?: return@withContext
        val existing = habitDao.getCompletion(habitId, date)
        if (existing != null) {
            // Undo / remove completion
            habitDao.deleteCompletion(habitId, date)
        } else {
            // Mark completed
            val completion = HabitCompletion(
                habitId = habitId,
                userId = userId,
                date = date,
                status = "COMPLETED",
                note = note
            )
            habitDao.insertCompletion(completion)
        }
    }

    suspend fun setHabitStatus(habitId: Long, date: String, status: String, note: String = "") = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value ?: return@withContext
        val completion = HabitCompletion(
            habitId = habitId,
            userId = userId,
            date = date,
            status = status,
            note = note
        )
        habitDao.insertCompletion(completion)
    }

    suspend fun useStreakFreeze(habitId: Long, date: String) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value ?: return@withContext
        val user = userDao.getUserByIdOnce(userId) ?: return@withContext
        if (user.availableFreezes <= 0) return@withContext

        // Deduct 1 freeze from user
        userDao.updateUser(
            user.copy(
                availableFreezes = user.availableFreezes - 1,
                usedFreezes = user.usedFreezes + 1
            )
        )
        // Mark habit date as FREEZE_PROTECTED
        setHabitStatus(habitId, date, "FREEZE_PROTECTED", "Streak Freeze used")
    }

    /**
     * Compute rich HabitWithStats list
     */
    fun getHabitsWithStats(): Flow<List<HabitWithStats>> = combine(
        getActiveHabits(),
        getAllCompletions()
    ) { habits, completions ->
        val todayStr = DateUtils.getTodayString()
        val completionsByHabit = completions.groupBy { it.habitId }

        habits.map { habit ->
            val habitComps = completionsByHabit[habit.id] ?: emptyList()
            val todayComp = habitComps.firstOrNull { it.date == todayStr }
            val (currentStreak, bestStreak) = DateUtils.calculateStreaks(habit.startDate, habitComps)
            val completedComps = habitComps.filter { it.status == "COMPLETED" || it.status == "REST_DAY" || it.status == "FREEZE_PROTECTED" }
            val totalCompletions = completedComps.size
            val daysRemaining = DateUtils.getDaysRemaining(habit.deadline)

            // Approximate completion rate over last 30 days
            val last30Dates = DateUtils.getPastDaysList(30)
            val doneIn30 = habitComps.count { it.date in last30Dates && (it.status == "COMPLETED" || it.status == "REST_DAY") }
            val rate = (doneIn30.toFloat() / 30f).coerceIn(0f, 1f)

            HabitWithStats(
                habit = habit,
                isCompletedToday = todayComp?.status == "COMPLETED",
                todayStatus = todayComp?.status,
                todayNote = todayComp?.note ?: "",
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                totalCompletions = totalCompletions,
                completionRate = rate,
                daysRemaining = daysRemaining
            )
        }
    }

    // --- Challenges & Deadlines ---
    fun getChallenges(): Flow<List<Challenge>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else challengeDao.getChallenges(id)
    }

    fun getDeadlines(): Flow<List<Challenge>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else challengeDao.getDeadlines(id)
    }

    suspend fun addChallenge(challenge: Challenge) = withContext(Dispatchers.IO) {
        challengeDao.insertChallenge(challenge)
    }

    suspend fun updateChallengeProgress(id: Long, newProgress: Int, target: Int) = withContext(Dispatchers.IO) {
        val item = challengeDao.getChallengeById(id) ?: return@withContext
        val isDone = newProgress >= target
        challengeDao.updateChallenge(item.copy(currentProgress = newProgress, isCompleted = isDone))
    }

    suspend fun deleteChallenge(id: Long) = withContext(Dispatchers.IO) {
        challengeDao.deleteChallengeById(id)
    }

    // --- Personal Goals ---
    fun getGoals(): Flow<List<PersonalGoal>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else goalDao.getGoals(id)
    }

    suspend fun addGoal(goal: PersonalGoal) = withContext(Dispatchers.IO) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: PersonalGoal) = withContext(Dispatchers.IO) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: Long) = withContext(Dispatchers.IO) {
        goalDao.deleteGoalById(id)
    }

    // --- AI Day Planner & Memories ---
    fun getDayTasks(date: String = DateUtils.getTodayString()): Flow<List<AiDayTask>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else aiDao.getDayTasks(id, date)
    }

    suspend fun setDayTasks(date: String, tasks: List<AiDayTask>) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value ?: return@withContext
        aiDao.clearDayTasks(userId, date)
        aiDao.insertDayTasks(tasks.map { it.copy(userId = userId, date = date) })
    }

    suspend fun toggleDayTaskComplete(task: AiDayTask) = withContext(Dispatchers.IO) {
        aiDao.updateDayTask(task.copy(isCompleted = !task.isCompleted))
        // If it links to a habit, mark habit as completed too!
        if (task.relatedHabitId != null) {
            toggleHabitCompletion(task.relatedHabitId, task.date)
        }
    }

    suspend fun skipDayTask(task: AiDayTask) = withContext(Dispatchers.IO) {
        aiDao.updateDayTask(task.copy(isSkipped = !task.isSkipped))
    }

    fun getAiMemories(): Flow<List<AiMemory>> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else aiDao.getMemories(id)
    }

    suspend fun addAiMemory(title: String, content: String, category: String) = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value ?: return@withContext
        aiDao.insertMemory(AiMemory(userId = userId, title = title, content = content, category = category))
    }

    suspend fun deleteAiMemory(id: Long) = withContext(Dispatchers.IO) {
        aiDao.deleteMemoryById(id)
    }

    // --- Social & Profiles ---
    fun searchUsers(query: String): Flow<List<User>> = _currentUserId.flatMapLatest { currentId ->
        userDao.searchUsers(query, currentId ?: -1)
    }

    fun getLeaderboardUsers(): Flow<List<User>> = userDao.getLeaderboardUsers()

    fun isFollowing(targetUserId: Long): Flow<Boolean> = _currentUserId.flatMapLatest { currentId ->
        if (currentId == null) flowOf(false) else followDao.isFollowing(currentId, targetUserId)
    }

    suspend fun followUser(targetUserId: Long) = withContext(Dispatchers.IO) {
        val currentId = _currentUserId.value ?: return@withContext
        followDao.followUser(UserFollow(followerUserId = currentId, followingUserId = targetUserId))
    }

    suspend fun unfollowUser(targetUserId: Long) = withContext(Dispatchers.IO) {
        val currentId = _currentUserId.value ?: return@withContext
        followDao.unfollowUser(currentId, targetUserId)
    }

    fun getFollowerCount(userId: Long): Flow<Int> = followDao.getFollowerCount(userId)
    fun getFollowingCount(userId: Long): Flow<Int> = followDao.getFollowingCount(userId)

    suspend fun getPublicUserData(userId: Long): Pair<User?, List<HabitWithStats>> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByIdOnce(userId) ?: return@withContext Pair(null, emptyList())
        val habits = habitDao.getActiveHabits(userId).firstOrNull() ?: emptyList()
        val completions = habitDao.getAllCompletionsForUserOnce(userId)
        val completionsByHabit = completions.groupBy { it.habitId }

        val stats = habits.map { habit ->
            val habitComps = completionsByHabit[habit.id] ?: emptyList()
            val (currentStreak, bestStreak) = DateUtils.calculateStreaks(habit.startDate, habitComps)
            HabitWithStats(
                habit = habit,
                isCompletedToday = false,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                totalCompletions = habitComps.size,
                completionRate = (habitComps.size.toFloat() / 30f).coerceIn(0f, 1f)
            )
        }
        Pair(user, stats)
    }

    // --- Milestones & FLOW JOURNEY ---
    fun getMilestones(): Flow<List<MilestoneItem>> = combine(
        getActiveHabits(),
        getAllCompletions(),
        getChallenges()
    ) { habits, completions, challenges ->
        val totalCompletions = completions.count { it.status == "COMPLETED" }
        val maxStreak = habits.map { habit ->
            val habitComps = completions.filter { it.habitId == habit.id }
            DateUtils.calculateStreaks(habit.startDate, habitComps).second
        }.maxOrNull() ?: 0

        val completedChallenges = challenges.count { it.isCompleted }

        listOf(
            MilestoneItem(
                id = "first_habit",
                title = "The Foundation",
                description = "Create your first habit in FLOW.",
                iconName = "Flag",
                isUnlocked = habits.isNotEmpty(),
                progress = if (habits.isNotEmpty()) 1f else 0f
            ),
            MilestoneItem(
                id = "first_completion",
                title = "Initial Momentum",
                description = "Complete your very first habit tracking check-in.",
                iconName = "CheckCircle",
                isUnlocked = totalCompletions >= 1,
                progress = if (totalCompletions >= 1) 1f else 0f
            ),
            MilestoneItem(
                id = "streak_7",
                title = "7-Day Velocity",
                description = "Maintain a continuous 7-day streak on any habit.",
                iconName = "LocalFireDepartment",
                isUnlocked = maxStreak >= 7,
                progress = (maxStreak.toFloat() / 7f).coerceIn(0f, 1f)
            ),
            MilestoneItem(
                id = "streak_14",
                title = "Habitual Mastery",
                description = "Reach a 14-day unbreakable streak.",
                iconName = "Bolt",
                isUnlocked = maxStreak >= 14,
                progress = (maxStreak.toFloat() / 14f).coerceIn(0f, 1f)
            ),
            MilestoneItem(
                id = "streak_30",
                title = "30-Day Flow State",
                description = "A full month of relentless consistency.",
                iconName = "WorkspacePremium",
                isUnlocked = maxStreak >= 30,
                progress = (maxStreak.toFloat() / 30f).coerceIn(0f, 1f)
            ),
            MilestoneItem(
                id = "century_club",
                title = "Century Club",
                description = "Log 100 total habit completions across all rituals.",
                iconName = "Stars",
                isUnlocked = totalCompletions >= 100,
                progress = (totalCompletions.toFloat() / 100f).coerceIn(0f, 1f)
            ),
            MilestoneItem(
                id = "challenge_conqueror",
                title = "Challenge Conqueror",
                description = "Successfully complete a structured challenge.",
                iconName = "EmojiEvents",
                isUnlocked = completedChallenges >= 1,
                progress = if (completedChallenges >= 1) 1f else 0f
            )
        )
    }

    // --- Complete Data Export (JSON) ---
    suspend fun exportAllDataJson(): String = withContext(Dispatchers.IO) {
        val userId = _currentUserId.value ?: return@withContext "{}"
        val user = userDao.getUserByIdOnce(userId) ?: return@withContext "{}"
        val habits = habitDao.getAllHabits(userId).firstOrNull() ?: emptyList()
        val completions = habitDao.getAllCompletionsForUserOnce(userId)
        val challenges = challengeDao.getAllChallengesAndDeadlines(userId).firstOrNull() ?: emptyList()
        val goals = goalDao.getGoals(userId).firstOrNull() ?: emptyList()
        val memories = aiDao.getMemories(userId).firstOrNull() ?: emptyList()

        val root = JSONObject().apply {
            put("app", "FLOW")
            put("exportedAt", System.currentTimeMillis())
            put("user", JSONObject().apply {
                put("username", user.username)
                put("email", user.email)
                put("fullName", user.fullName)
                put("bio", user.bio)
            })

            put("habits", JSONArray().apply {
                habits.forEach { h ->
                    put(JSONObject().apply {
                        put("id", h.id)
                        put("name", h.name)
                        put("category", h.category)
                        put("difficulty", h.difficulty)
                        put("frequency", h.frequency)
                        put("startDate", h.startDate)
                        put("deadline", h.deadline ?: "")
                        put("reminderTime", h.reminderTime ?: "")
                        put("isArchived", h.isArchived)
                    })
                }
            })

            put("completions", JSONArray().apply {
                completions.forEach { c ->
                    put(JSONObject().apply {
                        put("habitId", c.habitId)
                        put("date", c.date)
                        put("status", c.status)
                        put("note", c.note)
                    })
                }
            })

            put("challenges", JSONArray().apply {
                challenges.forEach { ch ->
                    put(JSONObject().apply {
                        put("title", ch.title)
                        put("category", ch.category)
                        put("currentProgress", ch.currentProgress)
                        put("targetCount", ch.targetCount)
                        put("isCompleted", ch.isCompleted)
                        put("isDeadline", ch.isDeadline)
                        put("endDate", ch.endDate)
                    })
                }
            })

            put("goals", JSONArray().apply {
                goals.forEach { g ->
                    put(JSONObject().apply {
                        put("title", g.title)
                        put("progressPercentage", g.progressPercentage)
                        put("targetDeadline", g.targetDeadline)
                        put("relatedCategory", g.relatedCategory)
                    })
                }
            })

            put("aiMemories", JSONArray().apply {
                memories.forEach { m ->
                    put(JSONObject().apply {
                        put("title", m.title)
                        put("content", m.content)
                        put("category", m.category)
                    })
                }
            })
        }

        root.toString(2)
    }

    private suspend fun initSampleDataForUser(userId: Long) {
        val today = DateUtils.getTodayString()
        val habit1 = Habit(
            userId = userId,
            name = "Morning Deep Focus",
            description = "Uninterrupted 90 minutes on highest leverage priority.",
            category = "Productivity",
            difficulty = "Hard",
            frequency = "Daily",
            targetPerPeriod = 1,
            startDate = DateUtils.getDaysAgo(14),
            reminderTime = "08:00 AM",
            customReminderMessage = "90 minutes of Deep Focus now. Future you will thank you.",
            colorHex = 0xFF00D2B4
        )
        val habit2 = Habit(
            userId = userId,
            name = "Hydration & Vitality",
            description = "Drink 2.5L clean water throughout the day.",
            category = "Health",
            difficulty = "Easy",
            frequency = "Daily",
            targetPerPeriod = 1,
            startDate = DateUtils.getDaysAgo(10),
            reminderTime = "09:00 AM",
            customReminderMessage = "Hydrate now for sustained mental clarity.",
            colorHex = 0xFF38BDF8
        )
        val habit3 = Habit(
            userId = userId,
            name = "Read 20 Pages",
            description = "Expand mind with non-fiction books.",
            category = "Reading",
            difficulty = "Medium",
            frequency = "Daily",
            targetPerPeriod = 1,
            startDate = DateUtils.getDaysAgo(8),
            reminderTime = "09:30 PM",
            customReminderMessage = "20 pages tonight will compound into mastery.",
            colorHex = 0xFFA78BFA
        )

        val h1Id = habitDao.insertHabit(habit1)
        val h2Id = habitDao.insertHabit(habit2)
        val h3Id = habitDao.insertHabit(habit3)

        // Seed some initial completions for habit 1 & 2 to give initial streaks
        for (i in 1..6) {
            habitDao.insertCompletion(
                HabitCompletion(
                    habitId = h1Id,
                    userId = userId,
                    date = DateUtils.getDaysAgo(i),
                    status = "COMPLETED",
                    note = "Great morning energy."
                )
            )
            habitDao.insertCompletion(
                HabitCompletion(
                    habitId = h2Id,
                    userId = userId,
                    date = DateUtils.getDaysAgo(i),
                    status = "COMPLETED"
                )
            )
        }

        // Add an active challenge
        challengeDao.insertChallenge(
            Challenge(
                userId = userId,
                title = "30-Day Relentless Consistency",
                description = "Complete all habits every day for 30 consecutive days.",
                category = "Productivity",
                startDate = DateUtils.getDaysAgo(6),
                endDate = DateUtils.getDaysAgo(-24),
                targetCount = 30,
                currentProgress = 6,
                isCompleted = false,
                isDeadline = false
            )
        )

        // Add a personal deadline
        challengeDao.insertChallenge(
            Challenge(
                userId = userId,
                title = "Q4 Project Milestone Deadline",
                description = "Finalize research and prepare documentation review.",
                category = "Study",
                startDate = today,
                endDate = DateUtils.getDaysAgo(-5),
                targetCount = 1,
                currentProgress = 0,
                isCompleted = false,
                isDeadline = true
            )
        )

        // Add a personal goal
        goalDao.insertGoal(
            PersonalGoal(
                userId = userId,
                title = "Master Advanced Systems Architecture",
                description = "Complete 5 core modules and launch practical capstone.",
                targetDeadline = DateUtils.getDaysAgo(-60),
                progressPercentage = 42,
                relatedCategory = "Study"
            )
        )

        // Add initial AI Memory preference
        aiDao.insertMemory(
            AiMemory(
                userId = userId,
                title = "Preferred Deep Work Time",
                content = "User excels at difficult cognitive tasks in morning hours between 08:30 and 11:30.",
                category = "Productive Hours"
            )
        )
    }
}
