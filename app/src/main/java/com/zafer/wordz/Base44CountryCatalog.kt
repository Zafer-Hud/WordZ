package com.zafer.wordz

import androidx.compose.ui.graphics.Color

/**
 * Canonical country order/ranges shared by the active Base44-compatible Android flow.
 * AdminCountries.jsx and lib/gameData agree on this order.
 */
internal val base44CanonicalCountries = listOf(
    B44Country("misir", "MISIR", "🇪🇬", 1, 20, Color(0xFFF5C842), "Piramitlerin gizemini çöz"),
    B44Country("fransa", "FRANSA", "🇫🇷", 21, 40, Color(0xFF60A5FA), "Işıklar şehrinde kelimeleri bul"),
    B44Country("sili", "ŞİLİ", "🇨🇱", 41, 60, Color(0xFF34D399), "And Dağları'na uzan"),
    B44Country("ispanya", "İSPANYA", "🇪🇸", 61, 80, Color(0xFFFC8181), "Akdeniz ritmini yakala"),
    B44Country("turkiye", "TÜRKİYE", "🇹🇷", 81, 100, Color(0xFFFB923C), "Anadolu boyunca keşfet"),
    B44Country("japonya", "JAPONYA", "🇯🇵", 101, 120, Color(0xFFF472B6), "Uzak doğunun son meydan okuması"),
)

internal fun ensureBase44CountryCatalog() {
    // b44Countries predates the Base44 source audit. Keep the symbol for old screens,
    // but normalize its six fixed slots so every active screen resolves the same ranges.
    runCatching {
        @Suppress("UNCHECKED_CAST")
        val target = b44Countries as MutableList<B44Country>
        if (target.size == base44CanonicalCountries.size) {
            base44CanonicalCountries.forEachIndexed { index, country ->
                target[index] = country
            }
        }
    }
}

internal fun base44CountryForLevel(levelNumber: Int): B44Country =
    base44CanonicalCountries.firstOrNull { levelNumber in it.start..it.end }
        ?: base44CanonicalCountries.last()
