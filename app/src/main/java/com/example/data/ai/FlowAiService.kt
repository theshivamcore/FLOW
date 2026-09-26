package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.AiDayTask
import com.example.data.model.HabitWithStats
import com.example.data.model.Challenge
import com.example.data.model.PersonalGoal
import com.example.data.model.AiMemory
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FlowAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Generate a conversational empathetic response or day plan
     */
    suspend fun chatWithFlowAi(
        userMessage: String,
        habits: List<HabitWithStats>,
        challenges: List<Challenge>,
        goals: List<PersonalGoal>,
        memories: List<AiMemory>,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Pair<String, List<AiDayTask>?> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" }

        // If user is asking to plan the day or modify schedule
        val isPlanningIntent = isPlanningRequest(userMessage)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = buildSystemPrompt(habits, challenges, goals, memories)
                val responseText = callGeminiRest(apiKey, systemPrompt, userMessage, conversationHistory)
                if (responseText.isNotBlank()) {
                    val parsedPlan = if (isPlanningIntent) extractScheduleFromAi(responseText, habits) else null
                    return@withContext Pair(cleanTextForDisplay(responseText), parsedPlan)
                }
            } catch (e: Exception) {
                // Fall back gracefully to local intelligent engine
            }
        }

        // Offline / intelligent local companion engine
        return@withContext generateLocalResponse(userMessage, habits, challenges, goals, memories, isPlanningIntent)
    }

    private fun isPlanningRequest(msg: String): Boolean {
        val lower = msg.lowercase()
        return lower.contains("plan") || lower.contains("schedule") ||
                lower.contains("routine") || lower.contains("hours") ||
                lower.contains("today") || lower.contains("tomorrow") ||
                lower.contains("overwhelm") || lower.contains("exam") ||
                lower.contains("study") || lower.contains("simplify")
    }

    private fun buildSystemPrompt(
        habits: List<HabitWithStats>,
        challenges: List<Challenge>,
        goals: List<PersonalGoal>,
        memories: List<AiMemory>
    ): String {
        val habitsSummary = habits.joinToString("; ") { h ->
            "${h.habit.name} (${h.habit.category}, ${h.habit.difficulty}, streak: ${h.currentStreak}d, done today: ${h.isCompletedToday}, reminder: ${h.habit.reminderTime ?: "none"})"
        }
        val challengesSummary = challenges.joinToString("; ") { c ->
            "${c.title} (due: ${c.endDate}, progress: ${c.currentProgress}/${c.targetCount})"
        }
        val goalsSummary = goals.joinToString("; ") { g ->
            "${g.title} (${g.progressPercentage}%)"
        }
        val memorySummary = memories.joinToString("; ") { "${it.title}: ${it.content}" }

        return """
            You are FLOW AI, a calm, intelligent, understanding and supportive personal day planning companion.
            You speak with genuine warmth, clarity, emotional intelligence, and calm precision.
            You do NOT lecture, judge, or overwhelm the user.
            
            USER'S CURRENT FLOW DATA:
            - Habits: $habitsSummary
            - Deadlines & Challenges: $challengesSummary
            - Goals: $goalsSummary
            - AI Memories of User: $memorySummary
            
            GUIDELINES:
            1. If the user mentions stress, fatigue, or being overwhelmed, first acknowledge and validate their feelings with empathy. Recommend simplifying their day.
            2. When planning, give a realistic, spaced out, serene schedule with time slots (e.g. 08:30 AM — Morning Routine).
            3. Incorporate their real habits and priorities.
            4. Keep responses concise, elegant and visually pleasing.
        """.trimIndent()
    }

    private fun callGeminiRest(
        apiKey: String,
        systemInstruction: String,
        userMessage: String,
        history: List<Pair<String, String>>
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val contentsArray = JSONArray()
        // Add past 4 messages for context
        for ((role, text) in history.takeLast(4)) {
            val part = JSONObject().put("text", text)
            contentsArray.put(JSONObject().put("role", if (role == "user") "user" else "model").put("parts", JSONArray().put(part)))
        }
        // Current user message
        contentsArray.put(
            JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
        )

        val root = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("topP", 0.9)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return ""
            }
            val bodyStr = response.body?.string() ?: return ""
            val json = JSONObject(bodyStr)
            val candidates = json.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            return parts.getJSONObject(0).optString("text", "")
        }
    }

    private fun cleanTextForDisplay(raw: String): String {
        return raw.trim()
    }

    private fun extractScheduleFromAi(text: String, habits: List<HabitWithStats>): List<AiDayTask> {
        val tasks = mutableListOf<AiDayTask>()
        val today = DateUtils.getTodayString()
        val lines = text.split("\n")
        val timeRegex = Regex("(\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm)?)\\s*[-—:]?\\s*(.*)")

        for (line in lines) {
            val match = timeRegex.find(line.trim())
            if (match != null) {
                val time = match.groupValues[1].trim()
                val title = match.groupValues[2].trim().replace("**", "").replace("*", "")
                if (title.isNotBlank()) {
                    // Match with related habit if exists
                    val matchedHabit = habits.firstOrNull {
                        title.contains(it.habit.name, ignoreCase = true) || it.habit.name.contains(title, ignoreCase = true)
                    }
                    tasks.add(
                        AiDayTask(
                            userId = 0,
                            date = today,
                            timeSlot = time,
                            title = title,
                            category = if (matchedHabit != null) "Habit" else if (title.contains("break", ignoreCase = true) || title.contains("rest", ignoreCase = true)) "Rest" else "Priority",
                            relatedHabitId = matchedHabit?.habit?.id
                        )
                    )
                }
            }
        }

        if (tasks.isEmpty()) {
            return generateDefaultTasks(habits)
        }
        return tasks
    }

    private fun generateLocalResponse(
        userMessage: String,
        habits: List<HabitWithStats>,
        challenges: List<Challenge>,
        goals: List<PersonalGoal>,
        memories: List<AiMemory>,
        isPlanning: Boolean
    ): Pair<String, List<AiDayTask>?> {
        val lower = userMessage.lowercase()
        val today = DateUtils.getTodayString()

        if (lower.contains("stress") || lower.contains("tired") || lower.contains("overwhelm") || lower.contains("bad day")) {
            val reply = "Take a gentle breath. You don't have to conquer everything today. Life isn't linear, and consistency is about returning gently, not burning out.\n\nI have simplified today to just your core anchor habit and plenty of restful breathing room. Your streak is safe, and we will protect your energy."
            val gentleTasks = listOf(
                AiDayTask(userId = 0, date = today, timeSlot = "09:00 AM", title = "Gentle Hydration & Fresh Air", category = "Rest"),
                AiDayTask(userId = 0, date = today, timeSlot = "11:00 AM", title = habits.firstOrNull()?.habit?.name ?: "One Light Habit", category = "Habit", relatedHabitId = habits.firstOrNull()?.habit?.id),
                AiDayTask(userId = 0, date = today, timeSlot = "02:00 PM", title = "Quiet Recharge Break", category = "Rest"),
                AiDayTask(userId = 0, date = today, timeSlot = "08:30 PM", title = "Evening Rest & Wind Down", category = "Rest")
            )
            return Pair(reply, gentleTasks)
        }

        if (lower.contains("exam") || lower.contains("study") || lower.contains("deadline")) {
            val reply = "Understood. When preparing under deadline pressure, high-leverage focused blocks with deliberate recovery work best.\n\nHere is a structured, balanced schedule designed to maintain deep focus without cognitive exhaustion."
            val studyTasks = listOf(
                AiDayTask(userId = 0, date = today, timeSlot = "08:30 AM", title = "Morning Alignment & Review", category = "Priority"),
                AiDayTask(userId = 0, date = today, timeSlot = "09:15 AM", title = "Deep Focus Block 1 (High Priority)", category = "Priority"),
                AiDayTask(userId = 0, date = today, timeSlot = "11:30 AM", title = "Physical Walk & Rest", category = "Rest"),
                AiDayTask(userId = 0, date = today, timeSlot = "01:00 PM", title = habits.firstOrNull { it.habit.category == "Study" || it.habit.category == "Productivity" }?.habit?.name ?: "Study Sprint", category = "Habit"),
                AiDayTask(userId = 0, date = today, timeSlot = "04:30 PM", title = "Practice Problems / Notes Consolidation", category = "Priority"),
                AiDayTask(userId = 0, date = today, timeSlot = "08:00 PM", title = "Light Habit Check-in & Review", category = "Habit")
            )
            return Pair(reply, studyTasks)
        }

        if (isPlanning) {
            val plan = generateDefaultTasks(habits)
            val reply = "Good morning 👋\n\nBased on your active habits and current flow momentum, here is a realistic, balanced day plan. Tap 'Accept Plan' to sync these tasks into your day."
            return Pair(reply, plan)
        }

        // General reflective conversational reply
        val topHabit = habits.maxByOrNull { it.currentStreak }
        val reply = "I'm right here with you. " +
                if (topHabit != null && topHabit.currentStreak > 0) {
                    "You've already built an inspiring ${topHabit.currentStreak}-day streak on '${topHabit.habit.name}'. "
                } else {
                    "Every new day is a fresh blank canvas to step into flow. "
                } +
                "How are you feeling about your tasks today, or would you like me to map out a calm schedule for you?"

        return Pair(reply, null)
    }

    private fun generateDefaultTasks(habits: List<HabitWithStats>): List<AiDayTask> {
        val today = DateUtils.getTodayString()
        val tasks = mutableListOf<AiDayTask>()

        tasks.add(AiDayTask(userId = 0, date = today, timeSlot = "08:00 AM", title = "Morning Awakening & Hydration", category = "Rest"))

        var hour = 9
        for (item in habits.take(4)) {
            val time = String.format("%02d:00 %s", if (hour > 12) hour - 12 else hour, if (hour >= 12) "PM" else "AM")
            tasks.add(
                AiDayTask(
                    userId = 0,
                    date = today,
                    timeSlot = item.habit.reminderTime ?: time,
                    title = item.habit.name,
                    category = "Habit",
                    relatedHabitId = item.habit.id
                )
            )
            hour += 3
        }

        tasks.add(AiDayTask(userId = 0, date = today, timeSlot = "09:00 PM", title = "Daily FLOW Reflection & Wind Down", category = "Rest"))
        return tasks
    }
}
