package com.zafer.wordz

private fun fallbackCountryId(levelNumber: Int): String = when (levelNumber) {
    in 1..20 -> "misir"
    in 21..40 -> "fransa"
    in 41..60 -> "sili"
    in 61..80 -> "ispanya"
    in 81..100 -> "turkiye"
    else -> "japonya"
}

private data class GridPlacementSeed(
    val word: String,
    val row: Int,
    val col: Int,
    val horizontal: Boolean,
)

private fun canonicalPlaced(
    levelNumber: Int,
    title: String,
    placements: List<GridPlacementSeed>,
): Base44WordLevel {
    val map = linkedMapOf<Pair<Int, Int>, Char>()
    val targets = placements.map { placement ->
        val cells = placement.word.indices.map { index ->
            val row = placement.row + if (placement.horizontal) 0 else index
            val col = placement.col + if (placement.horizontal) index else 0
            val letter = placement.word[index]
            val key = row to col
            val previous = map[key]
            require(previous == null || previous == letter) { "$title: geçersiz kesişim $key ($previous/$letter)" }
            map[key] = letter
            Base44CellPosition(row, col)
        }
        Base44TargetWord(placement.word, cells)
    }
    val rows = ((map.keys.maxOfOrNull { it.first } ?: 2) + 1).coerceAtLeast(3)
    val cols = ((map.keys.maxOfOrNull { it.second } ?: 2) + 1).coerceAtLeast(3)
    return Base44WordLevel(
        levelNumber = levelNumber,
        country = fallbackCountryId(levelNumber),
        gridRows = rows,
        gridCols = cols,
        gridCells = map.map { (pos, letter) -> Base44GridCell(pos.first, pos.second, letter) },
        wordsToFind = targets,
        title = title,
    )
}

private fun canonicalCrossword(
    levelNumber: Int,
    title: String,
    main: String,
    second: String,
    third: String,
): Base44WordLevel {
    if (title == "Yolculuk" && main == "YOL" && second == "OY" && third == "OL") {
        return canonicalPlaced(
            levelNumber,
            title,
            listOf(
                GridPlacementSeed("YOL", 2, 0, true),
                GridPlacementSeed("OY", 2, 1, false),
                GridPlacementSeed("OL", 1, 2, false),
            ),
        )
    }
    val firstCross = main.indexOf(second.first()).coerceAtLeast(0)
    val thirdCross = main.lastIndexOf(third.first()).let { if (it >= 0) it else main.lastIndex }
    return canonicalPlaced(
        levelNumber,
        title,
        listOf(
            GridPlacementSeed(main, 2, 0, true),
            GridPlacementSeed(second, 2, firstCross, false),
            GridPlacementSeed(third, 2, thirdCross, false),
        ),
    )
}

private data class FallbackSeed(val title: String, val main: String, val second: String, val third: String)

private val handCraftedSeeds = listOf(
    FallbackSeed("Başlangıç", "KALE", "KAL", "ELA"),
    FallbackSeed("Sıcak Kumlar", "MASA", "ASA", "AMA"),
    FallbackSeed("Taş Yolu", "TAŞ", "AŞ", "TA"),
    FallbackSeed("Elma Bahçesi", "ELMA", "ELA", "MAL"),
    FallbackSeed("Oyun Zamanı", "OYUN", "OY", "YUN"),
    FallbackSeed("Deniz Esintisi", "DENİZ", "DİZ", "İZ"),
    FallbackSeed("Gece Işıkları", "GECE", "ECE", "GE"),
    FallbackSeed("Kış Sabahı", "KIŞ", "IŞ", "KI"),
    FallbackSeed("Renkli Sokak", "RENK", "ERK", "NE"),
    FallbackSeed("Yolculuk", "YOL", "OY", "OL"),
    FallbackSeed("Bahar Rüzgarı", "BAHAR", "ARA", "HAR"),
    FallbackSeed("Kum Saati", "SAAT", "SAT", "ATA"),
    FallbackSeed("Mavi Göl", "MAVİ", "AV", "Vİ"),
    FallbackSeed("Orman Yolu", "ORMAN", "ORAN", "ROM"),
    FallbackSeed("Güneşli Gün", "GÜNEŞ", "GÜN", "EŞ"),
    FallbackSeed("Tatlı Mola", "TATLI", "TAT", "ALT"),
    FallbackSeed("Kırmızı Çatı", "ÇATI", "ATI", "ÇAT"),
    FallbackSeed("Sessiz Gece", "SESSİZ", "SES", "SİZ"),
    FallbackSeed("Uzak Ada", "ADA", "AD", "DA"),
    FallbackSeed("Final Tepesi", "ZAFER", "ZAR", "FER"),
)

private val turkishWordBank = setOf(
    "KALEM", "KALE", "KAL", "MAL", "ELA", "KAM", "EL", "AL",
    "KOLAY", "KOL", "KAY", "YOL", "OY", "AY", "OK",
    "BİLET", "BİL", "BEL", "TEL", "İLE",
    "DURAK", "DUR", "DAR", "KUR", "KAR", "AR", "AK",
    "ÇANTA", "ÇAN", "TAN", "TAÇ", "ÇAT", "ANA", "ATA",
    "YAPRAK", "YAP", "YAR", "YAK", "KAP", "ARA", "PAY",
    "TOPRAK", "TOP", "TOK", "KOP", "KOR",
    "KURAL", "KUL",
    "KUMAR", "KUM",
    "DİKEN", "DİN", "DİK", "KİN", "KEDİ", "İNEK", "EK",
    "ÇİLEK", "ÇEK", "ÇİL", "İL",
    "KİLİM", "KİL", "MİL", "İLİK",
    "MASAL", "MASA", "SAL", "AS",
    "KASAP", "KASA", "SAP", "KAS", "PAS",
    "TABAK", "BAK", "BAT", "KAT",
)

private val automaticSeeds = listOf(
    "Kalemlik" to "KALEM",
    "Kolay Yol" to "KOLAY",
    "Bilet Gişesi" to "BİLET",
    "Durak" to "DURAK",
    "Çanta" to "ÇANTA",
    "Yaprak" to "YAPRAK",
    "Toprak" to "TOPRAK",
    "Kural" to "KURAL",
    "Kumar" to "KUMAR",
    "Diken" to "DİKEN",
    "Çilek" to "ÇİLEK",
    "Kilim" to "KİLİM",
    "Masal" to "MASAL",
    "Kasap" to "KASAP",
    "Tabak" to "TABAK",
)

private fun canBuildWord(word: String, letters: String): Boolean {
    val remaining = letters.groupingBy { it }.eachCount().toMutableMap()
    for (char in word) {
        val count = remaining[char] ?: return false
        if (count <= 0) return false
        remaining[char] = count - 1
    }
    return true
}

private fun generatedSeed(title: String, main: String): FallbackSeed {
    val candidates = turkishWordBank.asSequence()
        .filter { it != main }
        .filter { it.length in 2 until main.length }
        .filter { canBuildWord(it, main) }
        .filter { it.first() in main }
        .sortedWith(compareByDescending<String> { it.length }.thenBy { it })
        .toList()

    val targets = mutableListOf<String>()
    val usedInitials = mutableSetOf<Char>()
    for (candidate in candidates) {
        if (candidate.first() !in usedInitials) {
            targets += candidate
            usedInitials += candidate.first()
        }
        if (targets.size == 2) break
    }
    require(targets.size == 2) { "Otomatik bölüm için yeterli kesişen kelime yok: $main" }
    return FallbackSeed("Otomatik · $title", main, targets[0], targets[1])
}

private val generatedSeeds = automaticSeeds.map { (title, main) -> generatedSeed(title, main) }

/**
 * Local safety fallback only. Production Base44 content is supplied through assets/word_levels.json.
 * The exact source fallback wins when it passes canonical validation; otherwise the safe local seed is used.
 */
val base44CanonicalLevels: List<Base44WordLevel> = (handCraftedSeeds + generatedSeeds).mapIndexed { index, seed ->
    val levelNumber = index + 1
    val exact = base44ExactFallbackLevels.firstOrNull { it.levelNumber == levelNumber }
    if (exact != null && validateBase44WordLevel(exact).isEmpty()) {
        exact
    } else {
        canonicalCrossword(levelNumber, seed.title, seed.main, seed.second, seed.third)
    }
}
