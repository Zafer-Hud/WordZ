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
    val thirdCross = main.indexOf(third.first()).let { if (it >= 0) it else main.lastIndex }
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

val levels = listOf(
    level("Başlangıç", "KALE", "KALE", "KAL", "ELA", setOf("ALE", "EL")),
    level("Sıcak Kumlar", "MASA", "MASA", "ASA", "AMA", setOf("AS")),
    level("Taş Yolu", "TAŞ", "TAŞ", "AŞ", "TA", setOf("AŞ")),
    level("Elma Bahçesi", "ELMA", "ELMA", "ELA", "MAL", setOf("EL", "AL")),
    level("Oyun Zamanı", "OYUN", "OYUN", "OY", "YUN", setOf("UN")),
    level("Deniz Esintisi", "DENİZ", "DENİZ", "DİZ", "İZ", setOf("DEN")),
    level("Gece Işıkları", "GECE", "GECE", "ECE", "GE", setOf("CE")),
    level("Kış Sabahı", "KIŞ", "KIŞ", "IŞ", "KI", emptySet()),
    level("Renkli Sokak", "RENK", "RENK", "ERK", "EN", setOf("ER")),
    level("Yolculuk", "YOL", "YOL", "OY", "OL", setOf("YO")),
    level("Bahar Rüzgarı", "BAHAR", "BAHAR", "ARA", "HAR", setOf("AH", "BAR")),
    level("Kum Saati", "SAAT", "SAAT", "SAT", "ATA", setOf("AS")),
    level("Mavi Göl", "MAVİ", "MAVİ", "AV", "Vİ", setOf("MA")),
    level("Orman Yolu", "ORMAN", "ORMAN", "ORAN", "ROM", setOf("AN", "ON")),
    level("Güneşli Gün", "GÜNEŞ", "GÜNEŞ", "GÜN", "EŞ", setOf("ÜÇ")),
    level("Tatlı Mola", "TATLI", "TATLI", "TAT", "ALT", setOf("AT", "AL")),
    level("Kırmızı Çatı", "ÇATI", "ÇATI", "ATI", "ÇAT", setOf("AT")),
    level("Sessiz Gece", "SESSİZ", "SESSİZ", "SES", "SİZ", setOf("SİS")),
    level("Uzak Ada", "ADA", "ADA", "AD", "DA", emptySet()),
    level("Final Tepesi", "ZAFER", "ZAFER", "ZAR", "FER", setOf("AF", "ER")),
)
