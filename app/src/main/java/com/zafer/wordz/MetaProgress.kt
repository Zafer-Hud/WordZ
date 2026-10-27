package com.zafer.wordz

import android.content.SharedPreferences
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ChallengeProgress(
    val words: Int = 0,
    val levels: Int = 0,
    val noHintLevels: Int = 0,
    val stars: Int = 0,
)

data class DailyRewardState(
    val streak: Int,
    val claimedToday: Boolean,
    val cycleDay: Int,
    val rewardToday: Int,
)

class MetaProgressStore(private val prefs: SharedPreferences) {
    companion object {
        private const val DAY_KEY = "meta_day_key"
        private const val WEEK_KEY = "meta_week_key"
        private const val D_WORDS = "meta_daily_words"
        private const val D_LEVELS = "meta_daily_levels"
        private const val D_NO_HINT = "meta_daily_no_hint"
        private const val D_STARS = "meta_daily_stars"
        private const val W_WORDS = "meta_weekly_words"
        private const val W_LEVELS = "meta_weekly_levels"
        private const val W_NO_HINT = "meta_weekly_no_hint"
        private const val W_STARS = "meta_weekly_stars"
        private const val D_CLAIMS = "meta_daily_claims"
        private const val W_CLAIMS = "meta_weekly_claims"
        private const val REWARD_DATE = "daily_reward_date"
        private const val STREAK = "daily_streak"
        val DAILY_REWARDS = listOf(30, 40, 50, 60, 75, 90, 150)
    }

    fun ensurePeriods(today: LocalDate = LocalDate.now()) {
        val day = today.toString()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
        val edit = prefs.edit()
        var changed = false
        if (prefs.getString(DAY_KEY, "") != day) {
            edit.putString(DAY_KEY, day)
                .putInt(D_WORDS, 0).putInt(D_LEVELS, 0).putInt(D_NO_HINT, 0).putInt(D_STARS, 0)
                .putStringSet(D_CLAIMS, emptySet())
            changed = true
        }
        if (prefs.getString(WEEK_KEY, "") != monday) {
            edit.putString(WEEK_KEY, monday)
                .putInt(W_WORDS, 0).putInt(W_LEVELS, 0).putInt(W_NO_HINT, 0).putInt(W_STARS, 0)
                .putStringSet(W_CLAIMS, emptySet())
            changed = true
        }
        if (changed) edit.apply()
    }

    fun daily(today: LocalDate = LocalDate.now()): ChallengeProgress {
        ensurePeriods(today)
        return ChallengeProgress(
            words = prefs.getInt(D_WORDS, 0),
            levels = prefs.getInt(D_LEVELS, 0),
            noHintLevels = prefs.getInt(D_NO_HINT, 0),
            stars = prefs.getInt(D_STARS, 0),
        )
    }

    fun weekly(today: LocalDate = LocalDate.now()): ChallengeProgress {
        ensurePeriods(today)
        return ChallengeProgress(
            words = prefs.getInt(W_WORDS, 0),
            levels = prefs.getInt(W_LEVELS, 0),
            noHintLevels = prefs.getInt(W_NO_HINT, 0),
            stars = prefs.getInt(W_STARS, 0),
        )
    }

    fun recordWord(today: LocalDate = LocalDate.now()) {
        ensurePeriods(today)
        prefs.edit()
            .putInt(D_WORDS, prefs.getInt(D_WORDS, 0) + 1)
            .putInt(W_WORDS, prefs.getInt(W_WORDS, 0) + 1)
            .apply()
    }

    fun recordLevel(stars: Int, noHint: Boolean, today: LocalDate = LocalDate.now()) {
        ensurePeriods(today)
        prefs.edit()
            .putInt(D_LEVELS, prefs.getInt(D_LEVELS, 0) + 1)
            .putInt(W_LEVELS, prefs.getInt(W_LEVELS, 0) + 1)
            .putInt(D_STARS, prefs.getInt(D_STARS, 0) + stars.coerceIn(0, 3))
            .putInt(W_STARS, prefs.getInt(W_STARS, 0) + stars.coerceIn(0, 3))
            .putInt(D_NO_HINT, prefs.getInt(D_NO_HINT, 0) + if (noHint) 1 else 0)
            .putInt(W_NO_HINT, prefs.getInt(W_NO_HINT, 0) + if (noHint) 1 else 0)
            .apply()
    }

    fun dailyClaims(today: LocalDate = LocalDate.now()): Set<String> {
        ensurePeriods(today)
        return prefs.getStringSet(D_CLAIMS, emptySet()).orEmpty().toSet()
    }

    fun weeklyClaims(today: LocalDate = LocalDate.now()): Set<String> {
        ensurePeriods(today)
        return prefs.getStringSet(W_CLAIMS, emptySet()).orEmpty().toSet()
    }

    fun claimDaily(id: String, today: LocalDate = LocalDate.now()): Boolean {
        ensurePeriods(today)
        val current = dailyClaims(today)
        if (id in current) return false
        prefs.edit().putStringSet(D_CLAIMS, current + id).apply()
        return true
    }

    fun claimWeekly(id: String, today: LocalDate = LocalDate.now()): Boolean {
        ensurePeriods(today)
        val current = weeklyClaims(today)
        if (id in current) return false
        prefs.edit().putStringSet(W_CLAIMS, current + id).apply()
        return true
    }

    fun rewardState(today: LocalDate = LocalDate.now()): DailyRewardState {
        val last = prefs.getString(REWARD_DATE, "").orEmpty()
        val storedStreak = prefs.getInt(STREAK, 0).coerceAtLeast(0)
        val effectiveStreak = when {
            last == today.toString() -> storedStreak.coerceAtLeast(1)
            last == today.minusDays(1).toString() -> storedStreak.coerceAtLeast(0) + 1
            else -> 1
        }
        val cycle = ((effectiveStreak - 1) % DAILY_REWARDS.size).coerceIn(0, DAILY_REWARDS.lastIndex)
        return DailyRewardState(
            streak = if (last == today.toString()) storedStreak.coerceAtLeast(1) else effectiveStreak,
            claimedToday = last == today.toString(),
            cycleDay = cycle,
            rewardToday = DAILY_REWARDS[cycle],
        )
    }

    fun claimDailyReward(today: LocalDate = LocalDate.now()): DailyRewardState? {
        val before = rewardState(today)
        if (before.claimedToday) return null
        val last = prefs.getString(REWARD_DATE, "").orEmpty()
        val old = prefs.getInt(STREAK, 0).coerceAtLeast(0)
        val newStreak = if (last == today.minusDays(1).toString()) old + 1 else 1
        prefs.edit().putString(REWARD_DATE, today.toString()).putInt(STREAK, newStreak).apply()
        val cycle = ((newStreak - 1) % DAILY_REWARDS.size).coerceIn(0, DAILY_REWARDS.lastIndex)
        return DailyRewardState(newStreak, true, cycle, DAILY_REWARDS[cycle])
    }
}
