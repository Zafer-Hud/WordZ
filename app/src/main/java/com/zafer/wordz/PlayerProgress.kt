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
        const val WORD_XP = 4
        const val LEVEL_XP = 35
        const val NO_HINT_BONUS_XP = 12
        const val DAILY_REWARD_XP = 10
        const val DAILY_TASK_XP = 12
        const val WEEKLY_TASK_XP = 30
        const val LEVEL_UP_COIN_REWARD = 50
    }

    private fun thresholdFor(level: Int): Int = 100 + ((level - 1).coerceAtLeast(0) * 35)

    fun snapshot(totalXp: Int = prefs.getInt(XP_KEY, 0).coerceAtLeast(0)): PlayerProgress {
        var level = 1
        var spent = 0
        while (true) {
            val need = thresholdFor(level)
            if (totalXp - spent < need) {
                return PlayerProgress(totalXp, level, totalXp - spent, need)
            }
            spent += need
            level += 1
        }
    }

    fun grant(amount: Int): XpGrantResult {
        val before = snapshot()
        val updated = (before.totalXp + amount.coerceAtLeast(0)).coerceAtLeast(0)
        prefs.edit().putInt(XP_KEY, updated).apply()
        val after = snapshot(updated)
        return XpGrantResult(before, after, (after.level - before.level).coerceAtLeast(0))
    }
}
