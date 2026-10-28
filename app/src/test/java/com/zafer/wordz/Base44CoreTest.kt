package com.zafer.wordz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Base44CoreTest {
    @Test
    fun rewardThresholdsMatchBase44() {
        assertEquals(Base44LevelReward(250, 70, 150, 500), Base44ProgressStore.rewardFor(29_999, false))
        assertEquals(Base44LevelReward(150, 70, 50, 300), Base44ProgressStore.rewardFor(30_000, false))
        assertEquals(Base44LevelReward(150, 70, 50, 300), Base44ProgressStore.rewardFor(59_999, false))
        assertEquals(Base44LevelReward(100, 50, 0, 200), Base44ProgressStore.rewardFor(60_000, false))
    }

    @Test
    fun replayNeverGrantsRewards() {
        assertEquals(Base44LevelReward(0, 0, 0, 0), Base44ProgressStore.rewardFor(10_000, true))
    }

    @Test
    fun wheelLettersUseMaximumMultiplicityAcrossWords() {
        val letters = deriveBase44WheelLetters(listOf("MAÇ", "AMA", "MAÇA"))
        val counts = letters.groupingBy { it }.eachCount()
        assertEquals(2, counts['A'])
        assertEquals(1, counts['M'])
        assertEquals(1, counts['Ç'])
    }

    @Test
    fun validatorRejectsWordGridLetterMismatch() {
        val broken = Base44WordLevel(
            levelNumber = 1,
            country = "misir",
            gridRows = 3,
            gridCols = 3,
            gridCells = listOf(
                Base44GridCell(1, 0, 'K'),
                Base44GridCell(1, 1, 'A'),
            ),
            wordsToFind = listOf(
                Base44TargetWord(
                    "KI",
                    listOf(Base44CellPosition(1, 0), Base44CellPosition(1, 1)),
                )
            ),
        )
        assertTrue(validateBase44WordLevel(broken).any { "beklenen" in it })
    }

    @Test
    fun canonicalLevelAcceptsArbitraryOrderedCellListsWhenLettersMatch() {
        val level = Base44WordLevel(
            levelNumber = 1,
            country = "misir",
            gridRows = 3,
            gridCols = 3,
            gridCells = listOf(
                Base44GridCell(0, 0, 'K'),
                Base44GridCell(1, 1, 'I'),
                Base44GridCell(2, 2, 'Ş'),
            ),
            wordsToFind = listOf(
                Base44TargetWord(
                    "KIŞ",
                    listOf(
                        Base44CellPosition(0, 0),
                        Base44CellPosition(1, 1),
                        Base44CellPosition(2, 2),
                    ),
                )
            ),
        )
        assertTrue(validateBase44WordLevel(level).isEmpty())
    }

    @Test
    fun localFallbackIsCanonicalAndValid() {
        assertEquals(35, base44CanonicalLevels.size)
        assertTrue(base44CanonicalLevels.flatMap(::validateBase44WordLevel).isEmpty())
        assertEquals((1..35).toList(), base44CanonicalLevels.map { it.levelNumber })
    }

    @Test
    fun dailySelectorIsDeterministicAndInRange() {
        val first = base44DailyLevelIndex(35, 20_000L)
        val second = base44DailyLevelIndex(35, 20_000L)
        assertEquals(first, second)
        assertTrue(first in 0 until 35)
        assertTrue(base44DailyLevelIndex(1, Long.MIN_VALUE) == 0)
    }
}
