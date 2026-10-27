package com.zafer.wordz

data class Placement(val word: String, val row: Int, val col: Int, val horizontal: Boolean)
data class Level(
    val title: String,
    val letters: List<Char>,
    val targets: List<String>,
    val bonusWords: Set<String>,
    val placements: List<Placement>,
)

private fun level(
    title: String,
    letters: String,
    main: String,
    second: String,
    third: String,
    bonus: Set<String> = emptySet(),
): Level {
    val firstCross = main.indexOf(second.first()).coerceAtLeast(0)
    val thirdCross = main.lastIndexOf(third.first()).let { if (it >= 0) it else main.lastIndex }
    return Level(
        title = title,
        letters = letters.toList(),
        targets = listOf(main, second, third),
        bonusWords = bonus,
        placements = listOf(
            Placement(main, 2, 0, true),
            Placement(second, 2, firstCross, false),
            Placement(third, 2, thirdCross, false),
        ),
    )
}

private val handCraftedLevels = listOf(
    level("Başlangıç", "KALE", "KALE", "KAL", "ELA", setOf("ALE", "EL")),
    level("Sıcak Kumlar", "MASA", "MASA", "ASA", "AMA", setOf("AS")),
    level("Taş Yolu", "TAŞ", "TAŞ", "AŞ", "TA", emptySet()),
    level("Elma Bahçesi", "ELMA", "ELMA", "ELA", "MAL", setOf("EL", "AL")),
    level("Oyun Zamanı", "OYUN", "OYUN", "OY", "YUN", setOf("UN")),
    level("Deniz Esintisi", "DENİZ", "DENİZ", "DİZ", "İZ", setOf("DEN")),
    level("Gece Işıkları", "GECE", "GECE", "ECE", "GE", setOf("CE")),
    level("Kış Sabahı", "KIŞ", "KIŞ", "IŞ", "KI", emptySet()),
    level("Renkli Sokak", "RENK", "RENK", "ERK", "NE", setOf("ER")),
    level("Yolculuk", "YOL", "YOL", "OY", "OL", setOf("YO")),
    level("Bahar Rüzgarı", "BAHAR", "BAHAR", "ARA", "HAR", setOf("AH", "BAR")),
    level("Kum Saati", "SAAT", "SAAT", "SAT", "ATA", setOf("AS")),
    level("Mavi Göl", "MAVİ", "MAVİ", "AV", "Vİ", setOf("MA")),
    level("Orman Yolu", "ORMAN", "ORMAN", "ORAN", "ROM", setOf("AN", "ON")),
    level("Güneşli Gün", "GÜNEŞ", "GÜNEŞ", "GÜN", "EŞ", setOf("ÜN")),
    level("Tatlı Mola", "TATLI", "TATLI", "TAT", "ALT", setOf("AT", "AL")),
    level("Kırmızı Çatı", "ÇATI", "ÇATI", "ATI", "ÇAT", setOf("AT")),
    level("Sessiz Gece", "SESSİZ", "SESSİZ", "SES", "SİZ", setOf("SİS")),
    level("Uzak Ada", "ADA", "ADA", "AD", "DA", emptySet()),
    level("Final Tepesi", "ZAFER", "ZAFER", "ZAR", "FER", setOf("AF", "ER")),
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
    "TABAK", "BAK", "BAT", "KAT"
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

private fun generateLevel(title: String, main: String): Level {
    val candidates = turkishWordBank
        .asSequence()
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

    val second = targets[0]
    val third = targets[1]
    val bonus = candidates
        .filter { it !in targets }
        .take(4)
        .toSet()

    return level(
        title = "Otomatik · $title",
        letters = main,
        main = main,
        second = second,
        third = third,
        bonus = bonus,
    )
}

private val generatedLevels = automaticSeeds.map { (title, main) -> generateLevel(title, main) }

val levels: List<Level> = handCraftedLevels + generatedLevels
