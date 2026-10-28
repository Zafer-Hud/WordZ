package com.zafer.wordz

import androidx.compose.ui.graphics.Color
import kotlin.math.ceil

/**
 * WordZ no longer uses countries. The existing B44Country data class is kept as a small
 * compatibility shell for UI code, while these records represent theme worlds.
 */
private val wordzWorldTemplates = listOf(
    B44Country("hayvanlar", "HAYVANLAR", "🐾", 1, 30, Color(0xFF34D399), "Evcil dostlardan vahşi doğaya"),
    B44Country("sehirler", "ŞEHİRLER", "🏙️", 31, 60, Color(0xFF60A5FA), "Türkiye'nin şehirlerinde kelime turu"),
    B44Country("yemekler", "YEMEKLER", "🍽️", 61, 90, Color(0xFFFB923C), "Lezzetli Türkçe kelimeleri bul"),
    B44Country("spor", "SPOR", "🏆", 91, 120, Color(0xFFA78BFA), "Sahadan piste kelime mücadelesi"),
)

internal val base44CanonicalCountries: List<B44Country>
    get() = wordzWorldTemplates.map { template ->
        val numbers = Base44RuntimeLevels.canonicalLevels
            .asSequence()
            .filter { it.country == template.id }
            .map { it.levelNumber }
            .toList()
        if (numbers.isEmpty()) template
        else template.copy(start = numbers.minOrNull() ?: template.start, end = numbers.maxOrNull() ?: template.end)
    }

internal fun ensureBase44CountryCatalog() = Unit

internal fun base44CountryById(id: String): B44Country =
    base44CanonicalCountries.firstOrNull { it.id == id } ?: base44CanonicalCountries.first()

internal fun base44CountryForLevel(levelNumber: Int): B44Country {
    val worldId = Base44RuntimeLevels.canonicalLevels
        .firstOrNull { it.levelNumber == levelNumber }
        ?.country
    if (worldId != null) return base44CountryById(worldId)
    return base44CanonicalCountries.firstOrNull { levelNumber in it.start..it.end }
        ?: base44CanonicalCountries.last()
}

internal fun base44CountryLevelIndices(countryId: String): List<Int> =
    Base44RuntimeLevels.canonicalLevels.mapIndexedNotNull { index, level ->
        index.takeIf { level.country == countryId }
    }

internal fun base44CountryCompletedCount(countryId: String, completed: Set<Int>): Int =
    base44CountryLevelIndices(countryId).count { it in completed }

internal fun base44CountryUnlocked(countryId: String, completed: Set<Int>): Boolean {
    val worlds = base44CanonicalCountries
    val index = worlds.indexOfFirst { it.id == countryId }
    if (index <= 0) return true
    val previousId = worlds[index - 1].id
    val previousLevels = base44CountryLevelIndices(previousId)
    if (previousLevels.isEmpty()) return false
    val needed = ceil(previousLevels.size * 0.50).toInt().coerceAtLeast(1)
    return previousLevels.count { it in completed } >= needed
}

internal fun base44FirstLevelIndex(countryId: String): Int? =
    base44CountryLevelIndices(countryId).firstOrNull()
