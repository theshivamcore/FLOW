package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.HabitWithStats
import com.example.data.model.User
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    data class ReportData(
        val reportType: String, // "Daily", "Weekly Review", "Monthly Recap", "Overall", "Habit"
        val user: User,
        val dateGenerated: String = DateUtils.getTodayString(),
        val completionRate: Int,
        val totalCompletions: Int,
        val activeHabitsCount: Int,
        val currentStreak: Int,
        val bestStreak: Int,
        val flowScore: Double,
        val items: List<HabitWithStats>,
        val notes: List<String> = emptyList()
    )

    /**
     * Generates a clean, professional PDF report and returns its File
     */
    fun generatePdfReport(context: Context, data: ReportData): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Accent top banner
        val bannerPaint = Paint().apply { color = Color.parseColor("#00D2B4") }
        canvas.drawRect(0f, 0f, 595f, 8f, bannerPaint)

        // Header Title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("FLOW PERFORMANCE REPORT", 40f, 55f, titlePaint)

        val subPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 13f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText("${data.reportType.uppercase()} • GENERATED FOR @${data.user.username} ON ${data.dateGenerated}", 40f, 78f, subPaint)

        // Divider
        val divPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            strokeWidth = 1.5f
        }
        canvas.drawLine(40f, 95f, 555f, 95f, divPaint)

        // Key Metrics Grid Cards
        val cardPaint = Paint().apply {
            color = Color.parseColor("#162032")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#334155")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val metricLabels = listOf("FLOW SCORE", "COMPLETION", "STREAK", "ACTIVE HABITS")
        val metricValues = listOf(
            "${data.flowScore}/10",
            "${data.completionRate}%",
            "${data.currentStreak} Days",
            "${data.activeHabitsCount}"
        )
        val cardWidth = 112f
        val cardHeight = 65f
        val gap = 9f

        for (i in 0..3) {
            val left = 40f + i * (cardWidth + gap)
            val rect = RectF(left, 115f, left + cardWidth, 115f + cardHeight)
            canvas.drawRoundRect(rect, 8f, 8f, cardPaint)
            canvas.drawRoundRect(rect, 8f, 8f, cardBorderPaint)

            val valPaint = Paint().apply {
                color = if (i == 0) Color.parseColor("#00D2B4") else Color.WHITE
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val lblPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 9f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            canvas.drawText(metricValues[i], left + 12f, 142f, valPaint)
            canvas.drawText(metricLabels[i], left + 12f, 162f, lblPaint)
        }

        // Section Title: Habit Breakdown
        val secTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Habit Breakdown & Consistency", 40f, 215f, secTitlePaint)

        // Table Header
        val thPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("HABIT", 40f, 240f, thPaint)
        canvas.drawText("CATEGORY", 230f, 240f, thPaint)
        canvas.drawText("DIFFICULTY", 330f, 240f, thPaint)
        canvas.drawText("STREAK", 430f, 240f, thPaint)
        canvas.drawText("RATE", 500f, 240f, thPaint)

        canvas.drawLine(40f, 248f, 555f, 248f, divPaint)

        var y = 270f
        val itemPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            textSize = 12f
            isAntiAlias = true
        }
        val tagPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 11f
            isAntiAlias = true
        }
        val streakPaint = Paint().apply {
            color = Color.parseColor("#00D2B4")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        for (item in data.items.take(14)) {
            val habit = item.habit
            val truncatedName = if (habit.name.length > 24) habit.name.take(22) + ".." else habit.name
            canvas.drawText(truncatedName, 40f, y, itemPaint)
            canvas.drawText(habit.category, 230f, y, tagPaint)
            canvas.drawText(habit.difficulty, 330f, y, tagPaint)
            canvas.drawText("${item.currentStreak}d (best ${item.bestStreak}d)", 430f, y, streakPaint)
            canvas.drawText("${(item.completionRate * 100).toInt()}%", 500f, y, itemPaint)

            canvas.drawLine(40f, y + 8f, 555f, y + 8f, Paint().apply {
                color = Color.parseColor("#1E293B")
                strokeWidth = 0.5f
            })
            y += 28f
        }

        // Footer
        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("Generated by FLOW • Track Habits with Clarity • Support: thenirantar@gmail.com", 40f, 815f, footerPaint)

        document.finishPage(page)

        // Save file
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "FLOW_${data.reportType.replace(" ", "_")}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    /**
     * Generates a high-resolution JPG share image and returns its File
     */
    fun generateJpgReport(context: Context, data: ReportData): File {
        val width = 1080
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply { color = Color.parseColor("#0A0F1D") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Gradient glow decorative header
        val shader = LinearGradient(
            0f, 0f, width.toFloat(), 300f,
            intArrayOf(Color.parseColor("#134E5E"), Color.parseColor("#0A0F1D")),
            null, Shader.TileMode.CLAMP
        )
        val glowPaint = Paint().apply { this.shader = shader }
        canvas.drawRect(0f, 0f, width.toFloat(), 300f, glowPaint)

        // Top Accent bar
        val barPaint = Paint().apply { color = Color.parseColor("#00D2B4") }
        canvas.drawRect(0f, 0f, width.toFloat(), 12f, barPaint)

        // App Logo / Header
        val brandPaint = Paint().apply {
            color = Color.parseColor("#00D2B4")
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("FLOW", 60f, 90f, brandPaint)

        val reportTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("${data.reportType} Summary", 60f, 160f, reportTitlePaint)

        val userPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 28f
            isAntiAlias = true
        }
        canvas.drawText("@${data.user.username} • ${data.dateGenerated}", 60f, 210f, userPaint)

        // Card Container for Score
        val scoreCard = RectF(60f, 250f, (width - 60).toFloat(), 480f)
        val cardBg = Paint().apply {
            color = Color.parseColor("#111827")
            isAntiAlias = true
        }
        val cardBorder = Paint().apply {
            color = Color.parseColor("#1F2937")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(scoreCard, 24f, 24f, cardBg)
        canvas.drawRoundRect(scoreCard, 24f, 24f, cardBorder)

        val scoreValPaint = Paint().apply {
            color = Color.parseColor("#00D2B4")
            textSize = 90f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("${data.flowScore}", 100f, 370f, scoreValPaint)

        val scoreSubPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("FLOW SCORE", 350f, 340f, scoreSubPaint)

        val scoreDescPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("Consistency is the catalyst of growth.", 350f, 385f, scoreDescPaint)

        // 3 Key Stats Box
        val boxWidth = (width - 120 - 40) / 3f
        val statLabels = listOf("COMPLETION", "CURRENT STREAK", "ACTIVE HABITS")
        val statVals = listOf("${data.completionRate}%", "${data.currentStreak}d", "${data.activeHabitsCount}")

        for (i in 0..2) {
            val left = 60f + i * (boxWidth + 20f)
            val rect = RectF(left, 520f, left + boxWidth, 680f)
            canvas.drawRoundRect(rect, 20f, 20f, cardBg)
            canvas.drawRoundRect(rect, 20f, 20f, cardBorder)

            val vPaint = Paint().apply {
                color = Color.WHITE
                textSize = 44f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val lPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 20f
                isAntiAlias = true
            }

            canvas.drawText(statVals[i], left + 25f, 600f, vPaint)
            canvas.drawText(statLabels[i], left + 25f, 645f, lPaint)
        }

        // Habits List Card
        val habitCard = RectF(60f, 720f, (width - 60).toFloat(), 1320f)
        canvas.drawRoundRect(habitCard, 24f, 24f, cardBg)
        canvas.drawRoundRect(habitCard, 24f, 24f, cardBorder)

        val listTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Habit Performance", 100f, 780f, listTitlePaint)

        var hy = 840f
        val hNamePaint = Paint().apply {
            color = Color.parseColor("#F3F4F6")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val hMetaPaint = Paint().apply {
            color = Color.parseColor("#9CA3AF")
            textSize = 22f
            isAntiAlias = true
        }
        val hStreakPaint = Paint().apply {
            color = Color.parseColor("#00D2B4")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        for (item in data.items.take(6)) {
            val habit = item.habit
            canvas.drawText(habit.name, 100f, hy, hNamePaint)
            canvas.drawText("${habit.category} • ${habit.difficulty}", 100f, hy + 32f, hMetaPaint)
            canvas.drawText("🔥 ${item.currentStreak} days", (width - 260).toFloat(), hy + 10f, hStreakPaint)

            canvas.drawLine(100f, hy + 50f, (width - 100).toFloat(), hy + 50f, Paint().apply {
                color = Color.parseColor("#1F2937")
                strokeWidth = 1.5f
            })
            hy += 75f
        }

        // Footer Brand
        val fPaint = Paint().apply {
            color = Color.parseColor("#6B7280")
            textSize = 22f
            isAntiAlias = true
        }
        canvas.drawText("Made with FLOW Habit Tracker • thenirantar@gmail.com", 60f, 1390f, fPaint)

        // Save JPG
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "FLOW_${data.reportType.replace(" ", "_")}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return file
    }

    /**
     * Launch native Android share sheet for a generated file
     */
    fun shareFile(context: Context, file: File, mimeType: String, subject: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "Check out my FLOW habit tracking report!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share FLOW Report"))
    }
}
