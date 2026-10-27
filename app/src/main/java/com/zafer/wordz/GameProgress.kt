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

data class ValidatedCell(val row: Int, val col: Int)

data class ValidatedLevel(
    val lettersByCell: Map<ValidatedCell, Char>,
)

fun validateLevel(level: Level): ValidatedLevel {
    require(level.targets.toSet() == level.placements.map { it.word }.toSet()) {
        "${level.title}: hedef kelimeler ile yerleşimler eşleşmiyor"
    }

    val available = level.letters.groupingBy { it }.eachCount()
    fun canBuild(word: String): Boolean {
        val needed = word.groupingBy { it }.eachCount()
        return needed.all { (char, count) -> (available[char] ?: 0) >= count }
    }

    (level.targets + level.bonusWords).forEach { word ->
        require(canBuild(word)) { "${level.title}: '$word' harf çarkından üretilemiyor" }
    }

    val map = linkedMapOf<ValidatedCell, Char>()
    level.placements.forEach { placement ->
        placement.word.forEachIndexed { index, char ->
            val cell = ValidatedCell(
                row = placement.row + if (placement.horizontal) 0 else index,
                col = placement.col + if (placement.horizontal) index else 0,
            )
            val previous = map[cell]
            require(previous == null || previous == char) {
                "${level.title}: geçersiz kesişim $cell ($previous / $char)"
            }
            map[cell] = char
        }
    }
    return ValidatedLevel(map)
}

/**
 * Startup validation must never take the whole app down because of one bad level.
 * Strict validation is still performed by validateLevel(), while this method logs
 * every data problem and returns the messages so tests/tooling can inspect them.
 */
fun validateAllLevels(): List<String> {
    val issues = levels.mapNotNull { level ->
        runCatching { validateLevel(level) }
            .exceptionOrNull()
            ?.message
            ?.let { "${level.title}: $it" }
    }
    issues.forEach { Log.e("WordZLevelValidation", it) }
    return issues
}

fun validateAllLevelsOrThrow() {
    val issues = validateAllLevels()
    require(issues.isEmpty()) { issues.joinToString(separator = "\n") }
}
