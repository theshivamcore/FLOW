package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User Account Entity
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val fullName: String = "",
    val bio: String = "Habit architect striving for daily flow.",
    val avatarColor: Long = 0xFF00D2B4,
    val isPublicProfile: Boolean = true,
    val showHabitsPublicly: Boolean = true,
    val showStreaksPublicly: Boolean = true,
    val showChallengesPublicly: Boolean = true,
    val optInLeaderboard: Boolean = true,
    val availableFreezes: Int = 3,
    val usedFreezes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Habit Entity
 */
@Entity(
    tableName = "habits",
    indices = [Index(value = ["userId"])]
)
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String = "",
    val category: String = "Productivity", // Study, Health, Fitness, Reading, Sleep, Productivity, Personal, Custom
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val frequency: String = "Daily", // Daily, Weekly, Monthly
    val targetPerPeriod: Int = 1,
    val unit: String = "times", // times, mins, pages, km, etc.
    val startDate: String, // yyyy-MM-dd
    val deadline: String? = null, // yyyy-MM-dd
    val reminderTime: String? = null, // e.g. "08:00 AM"
    val customReminderMessage: String = "",
    val isArchived: Boolean = false,
    val colorHex: Long = 0xFF00D2B4,
    val iconName: String = "Check",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Habit Completion Record
 */
@Entity(
    tableName = "habit_completions",
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["userId", "date"])
    ]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val userId: Long,
    val date: String, // yyyy-MM-dd
    val status: String = "COMPLETED", // COMPLETED, REST_DAY, SKIPPED, FREEZE_PROTECTED
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Challenge & Deadline Entity
 */
@Entity(
    tableName = "challenges",
    indices = [Index(value = ["userId"])]
)
data class Challenge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String = "",
    val category: String = "Productivity",
    val startDate: String, // yyyy-MM-dd
    val endDate: String, // yyyy-MM-dd
    val targetCount: Int = 30,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isDeadline: Boolean = false, // true = personal deadline, false = multi-day challenge
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Personal Goal Entity
 */
@Entity(
    tableName = "personal_goals",
    indices = [Index(value = ["userId"])]
)
data class PersonalGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String = "",
    val targetDeadline: String, // yyyy-MM-dd
    val progressPercentage: Int = 0,
    val relatedCategory: String = "Study",
    val milestonesJson: String = "[]", // JSON array of string milestones
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * FLOW AI Day Planner Task Entity
 */
@Entity(
    tableName = "ai_planner_tasks",
    indices = [Index(value = ["userId", "date"])]
)
data class AiDayTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: String, // yyyy-MM-dd
    val timeSlot: String, // e.g. "08:00 AM"
    val title: String,
    val description: String = "",
    val category: String = "Habit", // Habit, Priority, Challenge, Rest, Break
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false,
    val relatedHabitId: Long? = null
)

/**
 * FLOW AI Long-term Memory Entity
 */
@Entity(
    tableName = "ai_memories",
    indices = [Index(value = ["userId"])]
)
data class AiMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val content: String,
    val category: String = "Routine", // Routine, Habit Preference, Productive Hours, Goal, Personal
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Following / Social Entity
 */
@Entity(
    tableName = "user_following",
    indices = [Index(value = ["followerUserId", "followingUserId"], unique = true)]
)
data class UserFollow(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val followerUserId: Long,
    val followingUserId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Milestone / Achievement data class
 */
data class MilestoneItem(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null,
    val progress: Float = 0f // 0f to 1f
)

/**
 * Full Habit With Progress & Streak summary
 */
data class HabitWithStats(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val todayStatus: String? = null, // COMPLETED, REST_DAY, SKIPPED, FREEZE_PROTECTED
    val todayNote: String = "",
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val completionRate: Float, // 0.0 to 1.0
    val daysRemaining: Int? = null
)
