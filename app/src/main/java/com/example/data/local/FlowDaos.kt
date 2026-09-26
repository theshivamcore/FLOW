package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdOnce(id: Long): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(:username) AND id != :excludeUserId")
    suspend fun countUsernameOccurrences(username: String, excludeUserId: Long = -1): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE LOWER(username) LIKE '%' || LOWER(:query) || '%' AND id != :currentUserId")
    fun searchUsers(query: String, currentUserId: Long): Flow<List<User>>

    @Query("SELECT * FROM users WHERE isPublicProfile = 1 AND optInLeaderboard = 1")
    fun getLeaderboardUsers(): Flow<List<User>>
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveHabits(userId: Long): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedHabits(userId: Long): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllHabits(userId: Long): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // Completions
    @Query("SELECT * FROM habit_completions WHERE userId = :userId AND date = :date")
    fun getCompletionsForDate(userId: Long, date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE userId = :userId AND date = :date")
    suspend fun getCompletionsForDateOnce(userId: Long, date: String): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getCompletionsForHabitOnce(habitId: Long): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE userId = :userId ORDER BY date DESC")
    fun getAllCompletionsForUser(userId: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllCompletionsForUserOnce(userId: Long): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCompletion(habitId: Long, date: String): HabitCompletion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteAllCompletionsForHabit(habitId: Long)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE userId = :userId AND isDeadline = 0 ORDER BY isCompleted ASC, endDate ASC")
    fun getChallenges(userId: Long): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE userId = :userId AND isDeadline = 1 ORDER BY isCompleted ASC, endDate ASC")
    fun getDeadlines(userId: Long): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllChallengesAndDeadlines(userId: Long): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE id = :id LIMIT 1")
    suspend fun getChallengeById(id: Long): Challenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge): Long

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Delete
    suspend fun deleteChallenge(challenge: Challenge)

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteChallengeById(id: Long)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM personal_goals WHERE userId = :userId ORDER BY isCompleted ASC, targetDeadline ASC")
    fun getGoals(userId: Long): Flow<List<PersonalGoal>>

    @Query("SELECT * FROM personal_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): PersonalGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: PersonalGoal): Long

    @Update
    suspend fun updateGoal(goal: PersonalGoal)

    @Delete
    suspend fun deleteGoal(goal: PersonalGoal)

    @Query("DELETE FROM personal_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)
}

@Dao
interface AiDao {
    @Query("SELECT * FROM ai_planner_tasks WHERE userId = :userId AND date = :date ORDER BY timeSlot ASC")
    fun getDayTasks(userId: Long, date: String): Flow<List<AiDayTask>>

    @Query("SELECT * FROM ai_planner_tasks WHERE userId = :userId AND date = :date ORDER BY timeSlot ASC")
    suspend fun getDayTasksOnce(userId: Long, date: String): List<AiDayTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTask(task: AiDayTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTasks(tasks: List<AiDayTask>)

    @Update
    suspend fun updateDayTask(task: AiDayTask)

    @Delete
    suspend fun deleteDayTask(task: AiDayTask)

    @Query("DELETE FROM ai_planner_tasks WHERE userId = :userId AND date = :date")
    suspend fun clearDayTasks(userId: Long, date: String)

    // AI Memories
    @Query("SELECT * FROM ai_memories WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMemories(userId: Long): Flow<List<AiMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AiMemory): Long

    @Update
    suspend fun updateMemory(memory: AiMemory)

    @Delete
    suspend fun deleteMemory(memory: AiMemory)

    @Query("DELETE FROM ai_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)
}

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followUser(follow: UserFollow)

    @Query("DELETE FROM user_following WHERE followerUserId = :followerId AND followingUserId = :followingId")
    suspend fun unfollowUser(followerId: Long, followingId: Long)

    @Query("SELECT COUNT(*) > 0 FROM user_following WHERE followerUserId = :followerId AND followingUserId = :followingId")
    fun isFollowing(followerId: Long, followingId: Long): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM user_following WHERE followingUserId = :userId")
    fun getFollowerCount(userId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_following WHERE followerUserId = :userId")
    fun getFollowingCount(userId: Long): Flow<Int>
}
