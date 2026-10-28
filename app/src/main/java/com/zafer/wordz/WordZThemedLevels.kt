package com.zafer.wordz

private data class WordZThemeSeed(
    val world: String,
    val title: String,
    val main: String,
    val second: String,
    val third: String,
)

private data class WordZPlacement(val word: String, val row: Int, val col: Int, val horizontal: Boolean)

private fun buildThemedLevel(levelNumber: Int, seed: WordZThemeSeed): Base44WordLevel {
    val map = linkedMapOf<Pair<Int, Int>, Char>()

    fun place(p: WordZPlacement): Base44TargetWord {
        val cells = p.word.indices.map { index ->
            val row = p.row + if (p.horizontal) 0 else index
            val col = p.col + if (p.horizontal) index else 0
            val letter = p.word[index]
            val key = row to col
            val previous = map[key]
            require(previous == null || previous == letter) {
                "${seed.title}: geçersiz kesişim $key ($previous/$letter)"
            }
            map[key] = letter
            Base44CellPosition(row, col)
        }
        return Base44TargetWord(p.word, cells)
    }

    val secondCol = seed.main.indexOf(seed.second.first())
    val thirdCol = seed.main.lastIndexOf(seed.third.first())
    require(secondCol >= 0 && thirdCol >= 0 && secondCol != thirdCol) {
        "${seed.title}: alt kelimeler ana kelimeyle farklı noktalarda kesişmeli"
    }

    val targets = listOf(
        place(WordZPlacement(seed.main, 2, 0, true)),
        place(WordZPlacement(seed.second, 2, secondCol, false)),
        place(WordZPlacement(seed.third, 2, thirdCol, false)),
    )
    val rows = ((map.keys.maxOfOrNull { it.first } ?: 2) + 1).coerceAtLeast(3)
    val cols = ((map.keys.maxOfOrNull { it.second } ?: 2) + 1).coerceAtLeast(3)
    return Base44WordLevel(
        levelNumber = levelNumber,
        country = seed.world,
        gridRows = rows,
        gridCols = cols,
        gridCells = map.map { (pos, letter) -> Base44GridCell(pos.first, pos.second, letter) },
        wordsToFind = targets,
        title = seed.title,
        isActive = true,
    )
}

private val wordzThemeSeeds = listOf(
    WordZThemeSeed("hayvanlar", "Hayvanlar · Kedi", "KEDİ", "DİK", "EK"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Tavuk", "TAVUK", "KAT", "AV"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Ördek", "ÖRDEK", "DÖK", "KÖR"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Balık", "BALIK", "BAK", "KAL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Martı", "MARTI", "ARI", "MAT"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Karga", "KARGA", "ARA", "KAR"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Kumru", "KUMRU", "UMUR", "KUM"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Keçi", "KEÇİ", "ÇEK", "EK"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Koyun", "KOYUN", "OYUN", "KOY"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Aslan", "ASLAN", "ALA", "NAL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Kaplan", "KAPLAN", "ALA", "KAL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Ayılar", "AYILAR", "ALA", "YAR"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Tilki", "TİLKİ", "İLİK", "KİL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Kurt", "KURT", "KUR", "TUR"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Ceylan", "CEYLAN", "CAN", "YEL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Tavşan", "TAVŞAN", "ŞAN", "ANA"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Zebra", "ZEBRA", "BAR", "ZAR"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Panda", "PANDALAR", "PANDA", "DAL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Koala", "KOALA", "ALA", "KOL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Maymun", "MAYMUN", "UYMA", "MAY"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Yılan", "YILAN", "AYI", "NAL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Kartal", "KARTAL", "ALA", "KAR"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Arılar", "ARILIK", "ARI", "KIL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Kaplumbağa", "KAPLUMBAĞA", "KAP", "ALA"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Leylek", "LEYLEK", "YELEK", "KEL"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Şahin", "ŞAHİN", "HAN", "ŞAN"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Doğan", "DOĞAN", "DOĞA", "ODA"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Foklar", "FOKLAR", "FAL", "KAR"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Yunus", "YUNUS", "SUN", "UYU"),
    WordZThemeSeed("hayvanlar", "Hayvanlar · Balina", "BALİNA", "BAL", "ANA"),

    WordZThemeSeed("sehirler", "Şehirler · Ankara", "ANKARA", "ANA", "KAR"),
    WordZThemeSeed("sehirler", "Şehirler · Adana", "ADANALI", "ADANA", "DAL"),
    WordZThemeSeed("sehirler", "Şehirler · Konya", "KONYA", "KANO", "YAK"),
    WordZThemeSeed("sehirler", "Şehirler · Antalya", "ANTALYA", "ALT", "YAN"),
    WordZThemeSeed("sehirler", "Şehirler · Mersin", "MERSİN", "SEN", "ER"),
    WordZThemeSeed("sehirler", "Şehirler · Sivas", "SİVAS", "SAV", "AS"),
    WordZThemeSeed("sehirler", "Şehirler · Kayseri", "KAYSERİ", "ESKİ", "KAY"),
    WordZThemeSeed("sehirler", "Şehirler · Malatya", "MALATYA", "MAL", "YAT"),
    WordZThemeSeed("sehirler", "Şehirler · Manisa", "MANİSA", "MASA", "İMAN"),
    WordZThemeSeed("sehirler", "Şehirler · Samsun", "SAMSUN", "SUN", "UN"),
    WordZThemeSeed("sehirler", "Şehirler · Trabzon", "TRABZON", "BAR", "ZOR"),
    WordZThemeSeed("sehirler", "Şehirler · Diyarbakır", "DİYARBAKIR", "DİYAR", "BAKIR"),
    WordZThemeSeed("sehirler", "Şehirler · Şanlıurfa", "ŞANLIURFA", "ŞAN", "FAL"),
    WordZThemeSeed("sehirler", "Şehirler · Eskişehir", "ESKİŞEHİR", "ESKİ", "ŞEHİR"),
    WordZThemeSeed("sehirler", "Şehirler · Tekirdağ", "TEKİRDAĞ", "TEKİR", "DAĞ"),
    WordZThemeSeed("sehirler", "Şehirler · Karaman", "KARAMAN", "KAR", "ANA"),
    WordZThemeSeed("sehirler", "Şehirler · Amasya", "AMASYA", "AMA", "YAS"),
    WordZThemeSeed("sehirler", "Şehirler · Tokat", "TOKAT", "TOK", "KAT"),
    WordZThemeSeed("sehirler", "Şehirler · Ordu", "ORDU", "DUR", "UD"),
    WordZThemeSeed("sehirler", "Şehirler · Kars", "KARS", "KAR", "AS"),
    WordZThemeSeed("sehirler", "Şehirler · Uşak", "UŞAK", "KUŞ", "AK"),
    WordZThemeSeed("sehirler", "Şehirler · Afyon", "AFYON", "YAN", "OY"),
    WordZThemeSeed("sehirler", "Şehirler · Denizli", "DENİZLİ", "DENİZ", "İZ"),
    WordZThemeSeed("sehirler", "Şehirler · Balıkesir", "BALIKESİR", "BALIK", "ESİR"),
    WordZThemeSeed("sehirler", "Şehirler · Çanakkale", "ÇANAKKALE", "ÇANAK", "KALE"),
    WordZThemeSeed("sehirler", "Şehirler · Kocaeli", "KOCAELİ", "KALE", "İLE"),
    WordZThemeSeed("sehirler", "Şehirler · Sakarya", "SAKARYA", "KASA", "YAR"),
    WordZThemeSeed("sehirler", "Şehirler · Bursa", "BURSA", "BAR", "AR"),
    WordZThemeSeed("sehirler", "Şehirler · Bolu", "BOLU", "BOL", "OL"),
    WordZThemeSeed("sehirler", "Şehirler · Rize", "RİZE", "ER", "İZ"),

    WordZThemeSeed("yemekler", "Yemekler · Kebap", "KEBAP", "BAK", "KAP"),
    WordZThemeSeed("yemekler", "Yemekler · Pilav", "PİLAVLAR", "PİLAV", "VAR"),
    WordZThemeSeed("yemekler", "Yemekler · Dolma", "DOLMA", "DAL", "MAL"),
    WordZThemeSeed("yemekler", "Yemekler · Sarma", "SARMA", "MASA", "AMA"),
    WordZThemeSeed("yemekler", "Yemekler · Mantı", "MANTI", "MAT", "ANI"),
    WordZThemeSeed("yemekler", "Yemekler · Köfte", "KÖFTE", "TEK", "ÖTE"),
    WordZThemeSeed("yemekler", "Yemekler · Börek", "BÖREK", "ÖBEK", "KÖR"),
    WordZThemeSeed("yemekler", "Yemekler · Lahmacun", "LAHMACUN", "CAN", "HAM"),
    WordZThemeSeed("yemekler", "Yemekler · Karnıyarık", "KARNIYARIK", "KAR", "YAR"),
    WordZThemeSeed("yemekler", "Yemekler · Tarhana", "TARHANA", "ANA", "HAN"),
    WordZThemeSeed("yemekler", "Yemekler · Baklava", "BAKLAVA", "BAK", "ALA"),
    WordZThemeSeed("yemekler", "Yemekler · Kadayıf", "KADAYIF", "ADA", "KAY"),
    WordZThemeSeed("yemekler", "Yemekler · Lokum", "LOKUM", "KUM", "OL"),
    WordZThemeSeed("yemekler", "Yemekler · Helva", "HELVA", "ELA", "HAL"),
    WordZThemeSeed("yemekler", "Yemekler · Simit", "SİMİT", "İSİM", "SİT"),
    WordZThemeSeed("yemekler", "Yemekler · Poğaça", "POĞAÇA", "ÇAĞ", "AĞA"),
    WordZThemeSeed("yemekler", "Yemekler · Künefe", "KÜNEFE", "KÜFE", "EFE"),
    WordZThemeSeed("yemekler", "Yemekler · Bakliyat", "BAKLİYAT", "BAL", "YAT"),
    WordZThemeSeed("yemekler", "Yemekler · Makarna", "MAKARNA", "KAR", "AMA"),
    WordZThemeSeed("yemekler", "Yemekler · Salata", "SALATA", "SAL", "ATA"),
    WordZThemeSeed("yemekler", "Yemekler · Yoğurt", "YOĞURT", "YURT", "TUR"),
    WordZThemeSeed("yemekler", "Yemekler · Peynir", "PEYNİR", "YENİ", "ER"),
    WordZThemeSeed("yemekler", "Yemekler · Kurabiye", "KURABİYE", "KABİR", "UYAR"),
    WordZThemeSeed("yemekler", "Yemekler · Muhallebi", "MUHALLEBİ", "HAL", "BİL"),
    WordZThemeSeed("yemekler", "Yemekler · Şekerpare", "ŞEKERPARE", "ŞEKER", "PARE"),
    WordZThemeSeed("yemekler", "Yemekler · Fasulye", "FASULYE", "SEL", "FAL"),
    WordZThemeSeed("yemekler", "Yemekler · Nohut", "NOHUT", "NOT", "UN"),
    WordZThemeSeed("yemekler", "Yemekler · Patates", "PATATES", "TAT", "PAS"),
    WordZThemeSeed("yemekler", "Yemekler · Domates", "DOMATES", "DAM", "SET"),
    WordZThemeSeed("yemekler", "Yemekler · Patlıcan", "PATLICAN", "CAN", "ALT"),

    WordZThemeSeed("spor", "Spor · Futbol", "FUTBOL", "BUL", "OL"),
    WordZThemeSeed("spor", "Spor · Basketbol", "BASKETBOL", "KES", "BOL"),
    WordZThemeSeed("spor", "Spor · Voleybol", "VOLEYBOL", "YOL", "BEL"),
    WordZThemeSeed("spor", "Spor · Tenis", "TENİS", "NET", "SEN"),
    WordZThemeSeed("spor", "Spor · Güreş", "GÜREŞ", "GÜR", "EŞ"),
    WordZThemeSeed("spor", "Spor · Boks", "BOKSÖR", "KÖR", "BOR"),
    WordZThemeSeed("spor", "Spor · Yüzme", "YÜZME", "YEM", "EZ"),
    WordZThemeSeed("spor", "Spor · Koşu", "KOŞU", "KUŞ", "OK"),
    WordZThemeSeed("spor", "Spor · Atletizm", "ATLETİZM", "METAL", "İZ"),
    WordZThemeSeed("spor", "Spor · Hentbol", "HENTBOL", "NET", "BOL"),
    WordZThemeSeed("spor", "Spor · Kayak", "KAYAK", "KAY", "YAK"),
    WordZThemeSeed("spor", "Spor · Kaykay", "KAYKAY", "KAYAK", "YAK"),
    WordZThemeSeed("spor", "Spor · Halter", "HALTER", "HAL", "TER"),
    WordZThemeSeed("spor", "Spor · Golf", "GOLF", "GOL", "OF"),
    WordZThemeSeed("spor", "Spor · Ragbi", "RAGBİ", "BAR", "GAR"),
    WordZThemeSeed("spor", "Spor · Karate", "KARATE", "KARE", "ARA"),
    WordZThemeSeed("spor", "Spor · Tekvando", "TEKVANDO", "TEK", "KANO"),
    WordZThemeSeed("spor", "Spor · Eskrim", "ESKRİM", "ESKİ", "KREM"),
    WordZThemeSeed("spor", "Spor · Okçuluk", "OKÇULUK", "KOL", "OK"),
    WordZThemeSeed("spor", "Spor · Binicilik", "BİNİCİLİK", "İLİK", "BİL"),
    WordZThemeSeed("spor", "Spor · Yelken", "YELKEN", "YELEK", "KEL"),
    WordZThemeSeed("spor", "Spor · Kürek", "KÜREK", "KÜRE", "ERK"),
    WordZThemeSeed("spor", "Spor · Kano", "KANO", "KAN", "OK"),
    WordZThemeSeed("spor", "Spor · Dalış", "DALIŞ", "ALIŞ", "DAL"),
    WordZThemeSeed("spor", "Spor · Tırmanış", "TIRMANIŞ", "TIR", "ARI"),
    WordZThemeSeed("spor", "Spor · Bisiklet", "BİSİKLET", "BİLET", "ESKİ"),
    WordZThemeSeed("spor", "Spor · Maraton", "MARATON", "NOT", "ARA"),
    WordZThemeSeed("spor", "Spor · Bilardo", "BİLARDO", "DAL", "BİR"),
    WordZThemeSeed("spor", "Spor · Badminton", "BADMİNTON", "TON", "BİN"),
    WordZThemeSeed("spor", "Spor · Jimnastik", "JİMNASTİK", "SİMİT", "İMAN"),
)

internal val wordzThemedLevels: List<Base44WordLevel> = wordzThemeSeeds.mapIndexed { index, seed ->
    buildThemedLevel(index + 1, seed)
}
