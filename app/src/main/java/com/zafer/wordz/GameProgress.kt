package com.zafer.wordz

import android.content.SharedPreferences
import android.util.Log

data class SavedLevelProgress(
    val foundWords: Set<String> = emptySet(),
    val bonusWords: Set<String> = emptySet(),
    val hintedCells: Set<String> = emptySet(),
    val hintsUsed: Int = 0,
)

class GameProgressStore(private val prefs: SharedPreferences) {
    private fun foundKey(level: Int) = "level_${level}_found"
    private fun bonusKey(level: Int) = "level_${level}_bonus"
    private fun hintedKey(level: Int) = "level_${level}_hinted"
    private fun hintsKey(level: Int) = "level_${level}_hints_used"

    fun load(level: Int): SavedLevelProgress = SavedLevelProgress(
        foundWords = prefs.getStringSet(foundKey(level), emptySet()).orEmpty().toSet(),
        bonusWords = prefs.getStringSet(bonusKey(level), emptySet()).orEmpty().toSet(),
        hintedCells = prefs.getStringSet(hintedKey(level), emptySet()).orEmpty().toSet(),
        hintsUsed = prefs.getInt(hintsKey(level), 0).coerceAtLeast(0),
    )

    fun save(level: Int, progress: SavedLevelProgress) {
        prefs.edit()
            .putStringSet(foundKey(level), progress.foundWords.toSet())
            .putStringSet(bonusKey(level), progress.bonusWords.toSet())
            .putStringSet(hintedKey(level), progress.hintedCells.toSet())
            .putInt(hintsKey(level), progress.hintsUsed)
            .apply()
    }

    fun clear(level: Int) {
        prefs.edit()
            .remove(foundKey(level))
            .remove(bonusKey(level))
            .remove(hintedKey(level))
            .remove(hintsKey(level))
            .apply()
    }
}

/** Canonical-only validation used by startup diagnostics and CI. */
fun validateAllLevels(): List<String> {
    val issues = Base44RuntimeLevels.canonicalLevels.flatMap { level ->
        validateBase44WordLevel(level).map { "Bölüm ${level.levelNumber}: $it" }
    }
    issues.forEach { Log.e("WordZLevelValidation", it) }
    return issues
}

fun validateAllLevelsOrThrow() {
    val issues = validateAllLevels()
    require(issues.isEmpty()) { issues.joinToString(separator = "\n") }
}
