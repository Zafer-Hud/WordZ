package com.zafer.wordz

import android.content.SharedPreferences

data class Base44LevelReward(
    val xp: Int,
    val coins: Int,
    val speedBonus: Int,
    val score: Int,
)

internal class Base44ProgressStore(private val prefs: SharedPreferences) {
    companion object {
        const val SCORE_KEY = "b44_score"
        const val BASE_XP = 100
        const val BASE_COINS = 50
        const val SPEED_COINS = 20
        const val BASE_SCORE = 200
        const val HINT_COIN_COST = 100

        fun rewardFor(elapsedMs: Long, alreadyCompleted: Boolean): Base44LevelReward {
            if (alreadyCompleted) return Base44LevelReward(0, 0, 0, 0)
            val speedBonus = when {
                elapsedMs > 0L && elapsedMs < 30_000L -> 150
                elapsedMs > 0L && elapsedMs < 60_000L -> 50
                else -> 0
            }
            return Base44LevelReward(
                xp = BASE_XP + speedBonus,
                coins = BASE_COINS + if (speedBonus > 0) SPEED_COINS else 0,
                speedBonus = speedBonus,
                score = BASE_SCORE + speedBonus * 2,
            )
        }
    }

    fun score(): Int {
        if (prefs.contains(SCORE_KEY)) return prefs.getInt(SCORE_KEY, 0).coerceAtLeast(0)
        val completedCount = prefs.getStringSet(B44V2_COMPLETED, emptySet()).orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .distinct()
            .size
        val migrated = completedCount * BASE_SCORE
        prefs.edit().putInt(SCORE_KEY, migrated).apply()
        return migrated
    }

    fun addScore(amount: Int): Int {
        val updated = (score() + amount.coerceAtLeast(0)).coerceAtLeast(0)
        prefs.edit().putInt(SCORE_KEY, updated).apply()
        return updated
    }
}
