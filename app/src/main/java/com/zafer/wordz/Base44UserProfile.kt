package com.zafer.wordz

import android.content.SharedPreferences

data class Base44LocalProfile(
    val name: String,
    val coins: Int,
    val hints: Int,
    val xp: Int,
    val playerLevel: Int,
    val score: Int,
    val currentLevel: Int,
    val completedLevels: Set<Int>,
    val purchasedItems: Set<String>,
    val collectedItems: Set<String>,
    val banned: Boolean,
    val userRole: String,
)

data class Base44LeaderboardEntry(
    val name: String,
    val level: Int,
    val score: Int,
    val completed: Int,
    val isLocalPlayer: Boolean = false,
)

class Base44UserProfileStore(private val prefs: SharedPreferences) {
    companion object {
        private const val NAME = "b44_profile_name"
        private const val PURCHASED = "b44_purchased_items"
        private const val COLLECTED = "b44_collected_items"
        private const val BANNED = "b44_banned"
        private const val ROLE = "b44_user_role"
    }

    fun snapshot(): Base44LocalProfile {
        val xp = prefs.getInt("player_total_xp", 0).coerceAtLeast(0)
        val completed = prefs.getStringSet(B44V2_COMPLETED, emptySet()).orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
        return Base44LocalProfile(
            name = prefs.getString(NAME, "Oyuncu")?.ifBlank { "Oyuncu" } ?: "Oyuncu",
            coins = if (prefs.contains(B44V2_COINS)) prefs.getInt(B44V2_COINS, 500).coerceAtLeast(0) else 500,
            hints = prefs.getInt(B44V2_HINTS, 3).coerceAtLeast(0),
            xp = xp,
            playerLevel = (xp / PlayerProgressStore.XP_PER_LEVEL) + 1,
            score = Base44ProgressStore(prefs).score(),
            currentLevel = prefs.getInt(B44V2_UNLOCKED, 0).coerceAtLeast(0) + 1,
            completedLevels = completed,
            purchasedItems = prefs.getStringSet(PURCHASED, emptySet()).orEmpty().toSet(),
            collectedItems = prefs.getStringSet(COLLECTED, emptySet()).orEmpty().toSet(),
            banned = prefs.getBoolean(BANNED, false),
            userRole = prefs.getString(ROLE, "user") ?: "user",
        )
    }

    fun setName(value: String) {
        prefs.edit().putString(NAME, value.trim().ifBlank { "Oyuncu" }).apply()
    }

    fun recordPurchase(itemId: String) {
        val next = prefs.getStringSet(PURCHASED, emptySet()).orEmpty().toMutableSet().apply { add(itemId) }
        prefs.edit().putStringSet(PURCHASED, next).apply()
    }

    fun addCollected(itemId: String) {
        val next = prefs.getStringSet(COLLECTED, emptySet()).orEmpty().toMutableSet().apply { add(itemId) }
        prefs.edit().putStringSet(COLLECTED, next).apply()
    }

    fun setBanned(value: Boolean) {
        prefs.edit().putBoolean(BANNED, value).apply()
    }

    fun setRole(value: String) {
        prefs.edit().putString(ROLE, value.ifBlank { "user" }).apply()
    }

    fun localLeaderboard(): List<Base44LeaderboardEntry> {
        val profile = snapshot()
        return listOf(
            Base44LeaderboardEntry(
                name = profile.name,
                level = profile.playerLevel,
                score = profile.score,
                completed = profile.completedLevels.size,
                isLocalPlayer = true,
            )
        )
    }
}
