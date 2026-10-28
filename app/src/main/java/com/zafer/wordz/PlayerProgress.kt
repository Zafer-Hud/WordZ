package com.zafer.wordz

import android.content.SharedPreferences

data class PlayerProgress(
    val totalXp: Int,
    val level: Int,
    val currentLevelXp: Int,
    val nextLevelXp: Int,
)

data class XpGrantResult(
    val before: PlayerProgress,
    val after: PlayerProgress,
    val levelsGained: Int,
)

class PlayerProgressStore(private val prefs: SharedPreferences) {
    companion object {
        private const val XP_KEY = "player_total_xp"

        // Base44 parity: every player level is exactly 500 XP.
        const val XP_PER_LEVEL = 500

        // Kept for compatibility with legacy screens that still compile in the project.
        const val WORD_XP = 4
        const val LEVEL_XP = 35
        const val NO_HINT_BONUS_XP = 12
        const val DAILY_REWARD_XP = 10
        const val DAILY_TASK_XP = 12
        const val WEEKLY_TASK_XP = 30
        const val LEVEL_UP_COIN_REWARD = 50
    }

    fun snapshot(totalXp: Int = prefs.getInt(XP_KEY, 0).coerceAtLeast(0)): PlayerProgress {
        val safeXp = totalXp.coerceAtLeast(0)
        return PlayerProgress(
            totalXp = safeXp,
            level = (safeXp / XP_PER_LEVEL) + 1,
            currentLevelXp = safeXp % XP_PER_LEVEL,
            nextLevelXp = XP_PER_LEVEL,
        )
    }

    fun grant(amount: Int): XpGrantResult {
        val before = snapshot()
        val updated = (before.totalXp + amount.coerceAtLeast(0)).coerceAtLeast(0)
        prefs.edit().putInt(XP_KEY, updated).apply()
        val after = snapshot(updated)
        return XpGrantResult(
            before = before,
            after = after,
            levelsGained = (after.level - before.level).coerceAtLeast(0),
        )
    }
}
