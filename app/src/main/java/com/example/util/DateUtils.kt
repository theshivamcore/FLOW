package com.example.util

import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

object DateUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"

    fun getTodayString(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
        return sdf.format(Date())
    }

    fun getFormattedDate(dateStr: String, pattern: String = "EEE, MMM d"): String {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
            val date = sdf.parse(dateStr) ?: Date()
            val out = SimpleDateFormat(pattern, Locale.US)
            out.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getDaysAgo(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
        return sdf.format(cal.time)
    }

    fun getDaysRemaining(deadlineStr: String?): Int? {
        if (deadlineStr.isNullOrBlank()) return null
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
            val deadlineDate = sdf.parse(deadlineStr) ?: return null
            val todayDate = sdf.parse(getTodayString()) ?: return null
            val diff = deadlineDate.time - todayDate.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            null
        }
    }

    fun getPastDaysList(count: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
        for (i in 0 until count) {
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            list.add(sdf.format(c.time))
        }
        return list.reversed()
    }

    /**
     * Streak calculation:
     * - Returns pair of (currentStreak, bestStreak)
     */
    fun calculateStreaks(
        habitStartDate: String,
        completions: List<HabitCompletion>
    ): Pair<Int, Int> {
        if (completions.isEmpty()) return Pair(0, 0)

        val completionMap = completions.associateBy { it.date }
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
        val todayStr = getTodayString()

        // 1. Current streak
        var currentStreak = 0
        val cal = Calendar.getInstance()
        var checkDate = sdf.format(cal.time)

        // If today is completed or rest day or freeze, start from today
        val todayComp = completionMap[checkDate]
        val todayValid = todayComp != null && (todayComp.status == "COMPLETED" || todayComp.status == "REST_DAY" || todayComp.status == "FREEZE_PROTECTED")

        if (!todayValid) {
            // Check yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
            checkDate = sdf.format(cal.time)
        }

        while (true) {
            val comp = completionMap[checkDate]
            if (comp != null && (comp.status == "COMPLETED" || comp.status == "REST_DAY" || comp.status == "FREEZE_PROTECTED")) {
                currentStreak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkDate = sdf.format(cal.time)
                // Stop if we go before habit start date
                if (checkDate < habitStartDate) break
            } else {
                break
            }
        }

        // 2. Best streak calculation
        val sortedDates = completions
            .filter { it.status == "COMPLETED" || it.status == "REST_DAY" || it.status == "FREEZE_PROTECTED" }
            .map { it.date }
            .distinct()
            .sorted()

        var bestStreak = 0
        var tempStreak = 0
        var prevCal: Calendar? = null

        for (dateStr in sortedDates) {
            try {
                val curDate = sdf.parse(dateStr) ?: continue
                val curCal = Calendar.getInstance().apply { time = curDate }
                if (prevCal == null) {
                    tempStreak = 1
                } else {
                    val prevPlusOne = (prevCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                    if (sdf.format(prevPlusOne.time) == dateStr) {
                        tempStreak++
                    } else {
                        tempStreak = 1
                    }
                }
                prevCal = curCal
                bestStreak = max(bestStreak, tempStreak)
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }

        bestStreak = max(bestStreak, currentStreak)
        return Pair(currentStreak, bestStreak)
    }

    /**
     * Calculate FLOW Score out of 10.0
     */
    fun calculateFlowScore(
        habitsCount: Int,
        completedCount: Int,
        currentStreak: Int
    ): Double {
        if (habitsCount == 0) return 10.0
        val completionRatio = completedCount.toDouble() / habitsCount.toDouble()
        val streakBonus = (currentStreak.coerceAtMost(10) * 0.1) // up to 1.0 bonus
        val baseScore = completionRatio * 9.0 + streakBonus
        val finalScore = (baseScore.coerceIn(1.0, 10.0) * 10).roundToInt() / 10.0
        return finalScore
    }
}
