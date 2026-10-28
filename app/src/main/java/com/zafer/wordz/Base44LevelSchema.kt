package com.zafer.wordz

import org.json.JSONArray
import org.json.JSONObject

data class Base44CellPosition(val row: Int, val col: Int)
data class Base44GridCell(val row: Int, val col: Int, val letter: Char)
data class Base44TargetWord(val word: String, val cells: List<Base44CellPosition>)

data class Base44WordLevel(
    val levelNumber: Int,
    val country: String,
    val gridRows: Int,
    val gridCols: Int,
    val gridCells: List<Base44GridCell>,
    val wordsToFind: List<Base44TargetWord>,
    val isActive: Boolean = true,
    val title: String = "Bölüm $levelNumber",
)

private fun base44CountryIdForLevel(levelNumber: Int): String = when (levelNumber) {
    in 1..20 -> "misir"
    in 21..40 -> "fransa"
    in 41..60 -> "sili"
    in 61..80 -> "ispanya"
    in 81..100 -> "turkiye"
    else -> "japonya"
}

fun deriveBase44WheelLetters(words: List<String>): List<Char> {
    val maxCounts = linkedMapOf<Char, Int>()
    words.forEach { word ->
        val local = word.groupingBy { it }.eachCount()
        local.forEach { (char, count) -> maxCounts[char] = maxOf(maxCounts[char] ?: 0, count) }
    }
    return buildList { maxCounts.forEach { (char, count) -> repeat(count) { add(char) } } }
}

fun validateBase44WordLevel(level: Base44WordLevel): List<String> {
    val issues = mutableListOf<String>()
    if (level.levelNumber <= 0) issues += "level_number 1 veya daha büyük olmalı"
    if (level.country !in setOf("misir", "fransa", "sili", "ispanya", "turkiye", "japonya")) issues += "geçersiz country: ${level.country}"
    if (level.gridRows !in 3..10) issues += "grid_rows 3..10 aralığında olmalı"
    if (level.gridCols !in 3..10) issues += "grid_cols 3..10 aralığında olmalı"

    val cellMap = linkedMapOf<Pair<Int, Int>, Char>()
    level.gridCells.forEach { cell ->
        if (cell.row !in 0 until level.gridRows || cell.col !in 0 until level.gridCols) issues += "grid cell sınır dışında: (${cell.row},${cell.col})"
        val key = cell.row to cell.col
        val previous = cellMap[key]
        if (previous != null && previous != cell.letter) issues += "aynı koordinatta çakışan harf: $key ($previous/${cell.letter})"
        else if (previous != null) issues += "tekrarlanan grid cell: $key"
        cellMap[key] = cell.letter
    }

    val seenWords = mutableSetOf<String>()
    level.wordsToFind.forEach { target ->
        if (target.word.isBlank()) issues += "boş hedef kelime"
        if (!seenWords.add(target.word)) issues += "tekrarlanan hedef kelime: ${target.word}"
        if (target.word.length != target.cells.size) issues += "${target.word}: kelime uzunluğu (${target.word.length}) ile cell sayısı (${target.cells.size}) eşleşmiyor"
        if (target.cells.distinct().size != target.cells.size) issues += "${target.word}: aynı cell birden fazla kez kullanılmış"
        target.cells.forEachIndexed { index, pos ->
            if (pos.row !in 0 until level.gridRows || pos.col !in 0 until level.gridCols) {
                issues += "${target.word}: cell sınır dışında (${pos.row},${pos.col})"
                return@forEachIndexed
            }
            val gridLetter = cellMap[pos.row to pos.col]
            if (gridLetter == null) issues += "${target.word}: gridde olmayan cell (${pos.row},${pos.col})"
            else if (index < target.word.length && gridLetter != target.word[index]) issues += "${target.word}: (${pos.row},${pos.col}) için '$gridLetter', beklenen '${target.word[index]}'"
        }
    }
    return issues.distinct()
}

fun Base44WordLevel.toLegacyLevel(): Level {
    val issues = validateBase44WordLevel(this)
    require(issues.isEmpty()) { "$title: ${issues.joinToString("; ")}" }
    val placements = wordsToFind.map { target ->
        val sameRow = target.cells.map { it.row }.distinct().size == 1
        val sameCol = target.cells.map { it.col }.distinct().size == 1
        require(sameRow || sameCol) { "$title / ${target.word}: Android uyumluluk katmanı için hedef cell'ler tek satır veya sütunda olmalı" }
        val first = target.cells.first()
        Placement(target.word, first.row, first.col, sameRow)
    }
    return Level(
        title = title,
        letters = deriveBase44WheelLetters(wordsToFind.map { it.word }),
        targets = wordsToFind.map { it.word },
        bonusWords = emptySet(),
        placements = placements,
        country = country,
        gridRows = gridRows,
        gridCols = gridCols,
        gridCells = gridCells,
        wordsToFind = wordsToFind,
        isActive = isActive,
    )
}

fun Level.toBase44WordLevel(levelNumber: Int): Base44WordLevel {
    if (gridCells.isNotEmpty() && wordsToFind.isNotEmpty()) {
        return Base44WordLevel(
            levelNumber = levelNumber,
            country = country ?: base44CountryIdForLevel(levelNumber),
            gridRows = gridRows.coerceAtLeast(3),
            gridCols = gridCols.coerceAtLeast(3),
            gridCells = gridCells,
            wordsToFind = wordsToFind,
            isActive = isActive,
            title = title,
        )
    }

    val map = linkedMapOf<Pair<Int, Int>, Char>()
    val targets = placements.map { placement ->
        val cells = placement.word.indices.map { index ->
            val row = placement.row + if (placement.horizontal) 0 else index
            val col = placement.col + if (placement.horizontal) index else 0
            val char = placement.word[index]
            val key = row to col
            val previous = map[key]
            require(previous == null || previous == char) { "$title: geçersiz kesişim $key ($previous/$char)" }
            map[key] = char
            Base44CellPosition(row, col)
        }
        Base44TargetWord(placement.word, cells)
    }
    val rows = (map.keys.maxOfOrNull { it.first } ?: 2) + 1
    val cols = (map.keys.maxOfOrNull { it.second } ?: 2) + 1
    return Base44WordLevel(
        levelNumber = levelNumber,
        country = country ?: base44CountryIdForLevel(levelNumber),
        gridRows = rows.coerceAtLeast(3),
        gridCols = cols.coerceAtLeast(3),
        gridCells = map.map { (pos, letter) -> Base44GridCell(pos.first, pos.second, letter) },
        wordsToFind = targets,
        isActive = isActive,
        title = title,
    )
}

object Base44WordLevelJson {
    fun parse(json: String): List<Base44WordLevel> {
        val root = json.trim()
        val array = if (root.startsWith("[")) JSONArray(root) else JSONObject(root).getJSONArray("levels")
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val cellsJson = item.optJSONArray("grid_cells") ?: JSONArray()
                val wordsJson = item.optJSONArray("words_to_find") ?: JSONArray()
                val gridCells = buildList {
                    for (j in 0 until cellsJson.length()) {
                        val c = cellsJson.getJSONObject(j)
                        add(Base44GridCell(c.getInt("row"), c.getInt("col"), c.getString("letter").first()))
                    }
                }
                val words = buildList {
                    for (j in 0 until wordsJson.length()) {
                        val w = wordsJson.getJSONObject(j)
                        val positions = w.getJSONArray("cells")
                        val targetCells = buildList {
                            for (k in 0 until positions.length()) {
                                val p = positions.getJSONArray(k)
                                add(Base44CellPosition(p.getInt(0), p.getInt(1)))
                            }
                        }
                        add(Base44TargetWord(w.getString("word"), targetCells))
                    }
                }
                add(
                    Base44WordLevel(
                        levelNumber = item.getInt("level_number"),
                        country = item.getString("country"),
                        gridRows = item.getInt("grid_rows"),
                        gridCols = item.getInt("grid_cols"),
                        gridCells = gridCells,
                        wordsToFind = words,
                        isActive = item.optBoolean("is_active", true),
                        title = item.optString("title", "Bölüm ${item.getInt("level_number")}"),
                    )
                )
            }
        }.sortedBy { it.levelNumber }
    }
}

internal val base44ExactFallbackLevels = listOf(
    Base44WordLevel(
        1, "misir", 3, 3,
        listOf(Base44GridCell(0,1,'Ş'), Base44GridCell(1,0,'K'), Base44GridCell(1,1,'I'), Base44GridCell(1,2,'Ş'), Base44GridCell(2,1,'K')),
        listOf(
            Base44TargetWord("KIŞ", listOf(Base44CellPosition(1,0), Base44CellPosition(1,1), Base44CellPosition(1,2))),
            Base44TargetWord("ŞIK", listOf(Base44CellPosition(0,1), Base44CellPosition(1,1), Base44CellPosition(2,1)))
        ), title = "Başlangıç",
    ),
    Base44WordLevel(
        2, "misir", 5, 3,
        listOf(
            Base44GridCell(0,1,'M'), Base44GridCell(1,0,'M'), Base44GridCell(1,1,'A'), Base44GridCell(1,2,'Ç'),
            Base44GridCell(2,1,'Ç'), Base44GridCell(3,0,'A'), Base44GridCell(3,1,'M'), Base44GridCell(3,2,'A'), Base44GridCell(4,1,'Ç')
        ),
        listOf(
            Base44TargetWord("MAÇ", listOf(Base44CellPosition(1,0), Base44CellPosition(1,1), Base44CellPosition(1,2))),
            Base44TargetWord("AMA", listOf(Base44CellPosition(3,0), Base44CellPosition(3,1), Base44CellPosition(3,2))),
            Base44TargetWord("MAÇA", listOf(Base44CellPosition(0,1), Base44CellPosition(1,1), Base44CellPosition(2,1), Base44CellPosition(3,1)))
        ), title = "Sıcak Kumlar",
    ),
    Base44WordLevel(
        3, "misir", 3, 3,
        listOf(Base44GridCell(0,1,'A'), Base44GridCell(1,0,'A'), Base44GridCell(1,1,'T'), Base44GridCell(1,2,'Ş'), Base44GridCell(2,1,'A')),
        listOf(
            Base44TargetWord("ATŞ", listOf(Base44CellPosition(1,0), Base44CellPosition(1,1), Base44CellPosition(1,2))),
            Base44TargetWord("ATA", listOf(Base44CellPosition(0,1), Base44CellPosition(1,1), Base44CellPosition(2,1)))
        ), title = "Taş Yolu",
    ),
    Base44WordLevel(
        4, "misir", 3, 3,
        listOf(Base44GridCell(0,1,'E'), Base44GridCell(1,0,'İ'), Base44GridCell(1,1,'L'), Base44GridCell(1,2,'E'), Base44GridCell(2,1,'İ')),
        listOf(
            Base44TargetWord("İLE", listOf(Base44CellPosition(1,0), Base44CellPosition(1,1), Base44CellPosition(1,2))),
            Base44TargetWord("ELİ", listOf(Base44CellPosition(0,1), Base44CellPosition(1,1), Base44CellPosition(2,1)))
        ), title = "Elma Bahçesi",
    ),
    Base44WordLevel(
        5, "misir", 3, 3,
        listOf(Base44GridCell(0,1,'Y'), Base44GridCell(1,0,'Y'), Base44GridCell(1,1,'A'), Base44GridCell(1,2,'Z'), Base44GridCell(2,1,'Y')),
        listOf(
            Base44TargetWord("YAZ", listOf(Base44CellPosition(1,0), Base44CellPosition(1,1), Base44CellPosition(1,2))),
            Base44TargetWord("YAY", listOf(Base44CellPosition(0,1), Base44CellPosition(1,1), Base44CellPosition(2,1)))
        ), title = "Oyun Zamanı",
    ),
)
