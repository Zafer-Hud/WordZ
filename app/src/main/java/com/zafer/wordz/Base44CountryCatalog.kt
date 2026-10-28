package com.zafer.wordz

import androidx.compose.ui.graphics.Color
import kotlin.math.ceil

/** Visual metadata. Actual level-to-country assignment comes from Base44WordLevel.country. */
internal val base44CanonicalCountries = listOf(
    B44Country("misir", "MISIR", "🇪🇬", 1, 20, Color(0xFFF5C842), "Piramitlerin gizemini çöz"),
    B44Country("fransa", "FRANSA", "🇫🇷", 21, 40, Color(0xFF60A5FA), "Işıklar şehrinde kelimeleri bul"),
    B44Country("sili", "ŞİLİ", "🇨🇱", 41, 60, Color(0xFF34D399), "And Dağları'na uzan"),
    B44Country("ispanya", "İSPANYA", "🇪🇸", 61, 80, Color(0xFFFC8181), "Akdeniz ritmini yakala"),
    B44Country("turkiye", "TÜRKİYE", "🇹🇷", 81, 100, Color(0xFFFB923C), "Anadolu boyunca keşfet"),
    B44Country("japonya", "JAPONYA", "🇯🇵", 101, 120, Color(0xFFF472B6), "Uzak doğunun son meydan okuması"),
)

internal fun ensureBase44CountryCatalog() = Unit

internal fun base44CountryById(id: String): B44Country =
    base44CanonicalCountries.firstOrNull { it.id == id } ?: base44CanonicalCountries.first()

internal fun base44CountryForLevel(levelNumber: Int): B44Country {
    val canonicalCountry = Base44RuntimeLevels.canonicalLevels
        .firstOrNull { it.levelNumber == levelNumber }
        ?.country
    if (canonicalCountry != null) return base44CountryById(canonicalCountry)
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
    val index = base44CanonicalCountries.indexOfFirst { it.id == countryId }
    if (index <= 0) return true
    val previousId = base44CanonicalCountries[index - 1].id
    val previousLevels = base44CountryLevelIndices(previousId)
    if (previousLevels.isEmpty()) return false
    val needed = ceil(previousLevels.size * 0.50).toInt().coerceAtLeast(1)
    return previousLevels.count { it in completed } >= needed
}

internal fun base44FirstLevelIndex(countryId: String): Int? =
    base44CountryLevelIndices(countryId).firstOrNull()
