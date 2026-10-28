package com.zafer.wordz

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Single state owner for the active Base44-compatible app shell.
 * Existing SharedPreferences keys are intentionally preserved so installs upgrade in place.
 */
internal class WordZAppState(private val prefs: SharedPreferences) {
    private val playerStore = PlayerProgressStore(prefs)

    val levelCount: Int
        get() = Base44RuntimeLevels.canonicalLevels.size

    var coins by mutableIntStateOf(if (prefs.contains(B44V2_COINS)) prefs.getInt(B44V2_COINS, 500) else 500)
        private set
    var hints by mutableIntStateOf(prefs.getInt(B44V2_HINTS, 3).coerceAtLeast(0))
        private set
    var unlocked by mutableIntStateOf(
        prefs.getInt(B44V2_UNLOCKED, 0).coerceIn(0, (levelCount - 1).coerceAtLeast(0))
    )
        private set
    var selectedLevel by mutableIntStateOf(unlocked)
        private set
    var completed by mutableStateOf(loadCompleted())
        private set
    var player by mutableStateOf(playerStore.snapshot())
        private set

    private fun loadCompleted(): Set<Int> = prefs.getStringSet(B44V2_COMPLETED, emptySet()).orEmpty()
        .mapNotNull { it.toIntOrNull() }
        .filter { it in 0 until levelCount }
        .toSet()

    fun updateCoins(value: Int) {
        coins = value.coerceAtLeast(0)
        prefs.edit().putInt(B44V2_COINS, coins).apply()
    }

    fun updateHints(value: Int) {
        hints = value.coerceAtLeast(0)
        prefs.edit().putInt(B44V2_HINTS, hints).apply()
    }

    fun grantXp(amount: Int) {
        if (amount <= 0) return
        player = playerStore.grant(amount).after
    }

    fun canOpen(index: Int): Boolean = index in 0 until levelCount

    fun select(index: Int): Boolean {
        if (!canOpen(index)) return false
        selectedLevel = index
        return true
    }

    fun completeSelected() {
        if (!canOpen(selectedLevel)) return
        val nextCompleted = completed + selectedLevel
        val nextUnlocked = if (selectedLevel >= unlocked && unlocked < levelCount - 1) selectedLevel + 1 else unlocked
        completed = nextCompleted
        unlocked = nextUnlocked
        prefs.edit()
            .putInt(B44V2_UNLOCKED, nextUnlocked)
            .putStringSet(B44V2_COMPLETED, nextCompleted.map(Int::toString).toSet())
            .apply()
    }

    fun refreshFromDisk() {
        coins = if (prefs.contains(B44V2_COINS)) prefs.getInt(B44V2_COINS, 500).coerceAtLeast(0) else 500
        hints = prefs.getInt(B44V2_HINTS, 3).coerceAtLeast(0)
        unlocked = prefs.getInt(B44V2_UNLOCKED, 0).coerceIn(0, (levelCount - 1).coerceAtLeast(0))
        completed = loadCompleted()
        player = playerStore.snapshot()
        if (!canOpen(selectedLevel)) selectedLevel = unlocked
    }
}
