package com.zafer.wordz

import android.content.SharedPreferences
import java.time.LocalDate

internal enum class Base44PlayMode { CLASSIC, SPEED, DAILY }

data class Base44DailySnapshot(
    val date: LocalDate,
    val levelIndex: Int,
    val streak: Int,
    val completedToday: Boolean,
)

internal fun base44DailyLevelIndex(levelCount: Int, epochDay: Long): Int {
    if (levelCount <= 0) return 0
    val mixed = epochDay * 1103515245L + 12345L
    return Math.floorMod(mixed, levelCount.toLong()).toInt()
}

internal class Base44ModeStore(private val prefs: SharedPreferences) {
    companion object {
        private const val DAILY_DONE_DATE = "b44_daily_done_date"
        private const val DAILY_STREAK = "b44_daily_streak"
        private const val SPEED_BEST_MS = "b44_speed_best_ms"
        const val SPEED_LIMIT_MS = 90_000L
    }

    fun dailyLevelIndex(levelCount: Int, date: LocalDate = LocalDate.now()): Int =
        base44DailyLevelIndex(levelCount, date.toEpochDay())

    fun dailySnapshot(levelCount: Int, date: LocalDate = LocalDate.now()): Base44DailySnapshot {
        val doneDate = prefs.getString(DAILY_DONE_DATE, null)?.let { runCatching(LocalDate::parse).getOrNull() }
        return Base44DailySnapshot(
            date = date,
            levelIndex = dailyLevelIndex(levelCount, date),
            streak = prefs.getInt(DAILY_STREAK, 0).coerceAtLeast(0),
            completedToday = doneDate == date,
        )
    }

    fun completeDaily(date: LocalDate = LocalDate.now()): Int {
        val previousDate = prefs.getString(DAILY_DONE_DATE, null)?.let { runCatching(LocalDate::parse).getOrNull() }
        if (previousDate == date) return prefs.getInt(DAILY_STREAK, 0).coerceAtLeast(0)
        val previousStreak = prefs.getInt(DAILY_STREAK, 0).coerceAtLeast(0)
        val nextStreak = if (previousDate == date.minusDays(1)) previousStreak + 1 else 1
        prefs.edit()
            .putString(DAILY_DONE_DATE, date.toString())
            .putInt(DAILY_STREAK, nextStreak)
            .apply()
        return nextStreak
    }

    fun bestSpeedMs(): Long = prefs.getLong(SPEED_BEST_MS, 0L).coerceAtLeast(0L)

    fun recordSpeed(elapsedMs: Long): Long {
        if (elapsedMs <= 0L) return bestSpeedMs()
        val previous = bestSpeedMs()
        val next = if (previous == 0L || elapsedMs < previous) elapsedMs else previous
        prefs.edit().putLong(SPEED_BEST_MS, next).apply()
        return next
    }
}
