package com.example

import com.example.data.model.HabitCompletion
import com.example.util.DateUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testFlowScoreCalculation() {
        val scoreFull = DateUtils.calculateFlowScore(5, 5, 7)
        assertTrue(scoreFull >= 9.5)

        val scorePartial = DateUtils.calculateFlowScore(10, 5, 0)
        assertEquals(4.5, scorePartial, 0.1)

        val scoreZero = DateUtils.calculateFlowScore(0, 0, 0)
        assertEquals(10.0, scoreZero, 0.01)
    }

    @Test
    fun testStreakCalculation() {
        val today = DateUtils.getTodayString()
        val yesterday = DateUtils.getDaysAgo(1)
        val twoDaysAgo = DateUtils.getDaysAgo(2)

        val completions = listOf(
            HabitCompletion(habitId = 1, userId = 1, date = twoDaysAgo, status = "COMPLETED"),
            HabitCompletion(habitId = 1, userId = 1, date = yesterday, status = "COMPLETED"),
            HabitCompletion(habitId = 1, userId = 1, date = today, status = "COMPLETED")
        )

        val (currentStreak, bestStreak) = DateUtils.calculateStreaks(twoDaysAgo, completions)
        assertEquals(3, currentStreak)
        assertEquals(3, bestStreak)
    }

    @Test
    fun testRestDayProtectsStreak() {
        val today = DateUtils.getTodayString()
        val yesterday = DateUtils.getDaysAgo(1)

        val completions = listOf(
            HabitCompletion(habitId = 1, userId = 1, date = yesterday, status = "COMPLETED"),
            HabitCompletion(habitId = 1, userId = 1, date = today, status = "REST_DAY")
        )

        val (currentStreak, _) = DateUtils.calculateStreaks(yesterday, completions)
        assertEquals(2, currentStreak)
    }
}
