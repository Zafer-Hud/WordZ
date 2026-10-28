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
            country = "hayvanlar",
            gridRows = 3,
            gridCols = 3,
            gridCells = listOf(
                Base44GridCell(1, 0, 'K'),
                Base44GridCell(1, 1, 'A'),
            ),
            wordsToFind = listOf(
                Base44TargetWord("KI", listOf(Base44CellPosition(1, 0), Base44CellPosition(1, 1)))
            ),
        )
        assertTrue(validateBase44WordLevel(broken).any { "beklenen" in it })
    }

    @Test
    fun canonicalLevelAcceptsArbitraryOrderedCellListsWhenLettersMatch() {
        val level = Base44WordLevel(
            levelNumber = 1,
            country = "hayvanlar",
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
                    listOf(Base44CellPosition(0, 0), Base44CellPosition(1, 1), Base44CellPosition(2, 2)),
                )
            ),
        )
        assertTrue(validateBase44WordLevel(level).isEmpty())
    }

    @Test
    fun nativeWordZContentHas120ValidLevels() {
        assertEquals(120, base44CanonicalLevels.size)
        assertEquals((1..120).toList(), base44CanonicalLevels.map { it.levelNumber })
        assertTrue(base44CanonicalLevels.flatMap(::validateBase44WordLevel).isEmpty())
    }

    @Test
    fun eachThemeHasThirtyLevels() {
        val counts = base44CanonicalLevels.groupingBy { it.country }.eachCount()
        assertEquals(30, counts["hayvanlar"])
        assertEquals(30, counts["sehirler"])
        assertEquals(30, counts["yemekler"])
        assertEquals(30, counts["spor"])
        assertEquals(4, counts.size)
    }

    @Test
    fun dailySelectorIsDeterministicAndInRange() {
        val first = base44DailyLevelIndex(120, 20_000L)
        val second = base44DailyLevelIndex(120, 20_000L)
        assertEquals(first, second)
        assertTrue(first in 0 until 120)
        assertTrue(base44DailyLevelIndex(1, Long.MIN_VALUE) == 0)
    }
}
