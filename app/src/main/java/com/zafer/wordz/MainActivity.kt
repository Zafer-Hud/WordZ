package com.zafer.wordz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF07142D)) {
                    WordZGame()
                }
            }
        }
    }
}

data class Placement(
    val word: String,
    val row: Int,
    val col: Int,
    val horizontal: Boolean,
)

data class Level(
    val title: String,
    val letters: List<Char>,
    val targets: List<String>,
    val bonusWords: Set<String>,
    val placements: List<Placement>,
)

private data class Cell(val row: Int, val col: Int)

private val trLocale = Locale("tr", "TR")

private val levels = listOf(
    Level(
        title = "Başlangıç",
        letters = listOf('K', 'A', 'L', 'E'),
        targets = listOf("KALE", "KAL", "ELA"),
        bonusWords = setOf("ALE", "EL"),
        placements = listOf(
            Placement("KALE", 2, 0, true),
            Placement("KAL", 2, 0, false),
            Placement("ELA", 2, 3, false),
        )
    ),
    Level(
        title = "Sıcak Kumlar",
        letters = listOf('M', 'A', 'S', 'A'),
        targets = listOf("MASA", "ASA", "AMA"),
        bonusWords = setOf("AS"),
        placements = listOf(
            Placement("MASA", 2, 0, true),
            Placement("ASA", 2, 1, false),
            Placement("AMA", 1, 3, false),
        )
    ),
    Level(
        title = "Taş Yolu",
        letters = listOf('T', 'A', 'Ş'),
        targets = listOf("TAŞ", "AŞ", "TA"),
        bonusWords = emptySet(),
        placements = listOf(
            Placement("TAŞ", 2, 0, true),
            Placement("AŞ", 2, 1, false),
            Placement("TA", 2, 0, false),
        )
    ),
    Level(
        title = "Elma Bahçesi",
        letters = listOf('E', 'L', 'M', 'A'),
        targets = listOf("ELMA", "ELA", "MAL"),
        bonusWords = setOf("EL", "AL"),
        placements = listOf(
            Placement("ELMA", 2, 0, true),
            Placement("ELA", 2, 0, false),
            Placement("MAL", 2, 2, false),
        )
    ),
    Level(
        title = "Oyun Zamanı",
        letters = listOf('O', 'Y', 'U', 'N'),
        targets = listOf("OYUN", "OY", "YUN"),
        bonusWords = setOf("UN"),
        placements = listOf(
            Placement("OYUN", 2, 0, true),
            Placement("OY", 2, 0, false),
            Placement("YUN", 2, 1, false),
        )
    ),
)

@Composable
fun WordZGame() {
    var levelIndex by remember { mutableIntStateOf(0) }
    val level = levels[levelIndex]
    var wheelLetters by remember(levelIndex) { mutableStateOf(level.letters) }
    var foundWords by remember(levelIndex) { mutableStateOf(setOf<String>()) }
    var foundBonusWords by remember(levelIndex) { mutableStateOf(setOf<String>()) }
    var hintedCells by remember(levelIndex) { mutableStateOf(setOf<Cell>()) }
    var selectedIndices by remember(levelIndex) { mutableStateOf(emptyList<Int>()) }
    var liveWord by remember(levelIndex) { mutableStateOf("") }
    var status by remember(levelIndex) { mutableStateOf("Harfleri sürükleyerek birleştir") }
    var coins by remember { mutableIntStateOf(100) }

    val complete = level.targets.all { it in foundWords }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("WordZ", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text(level.title, color = Color(0xFF8FA8D6), fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("🪙 $coins", color = Color(0xFFFFD166), fontWeight = FontWeight.Black)
                Text("Bölüm ${levelIndex + 1}/${levels.size}", color = Color.White, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        CrosswordBoard(
            level = level,
            foundWords = foundWords,
            hintedCells = hintedCells,
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Bulunan ${foundWords.size}/${level.targets.size}",
                color = Color(0xFFA8BCE1),
                fontSize = 13.sp,
            )
            Text(
                "Bonus ${foundBonusWords.size}",
                color = Color(0xFFFFD166),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF12264B), RoundedCornerShape(16.dp))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (liveWord.isNotBlank()) liveWord else status,
                color = when {
                    liveWord.isNotBlank() -> Color.White
                    status.startsWith("Bonus") -> Color(0xFFFFD166)
                    status.startsWith("Harika") -> Color(0xFF7EF0B8)
                    else -> Color(0xFFA8BCE1)
                },
                fontWeight = FontWeight.Bold,
                fontSize = if (liveWord.isNotBlank()) 23.sp else 14.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        LetterWheel(
            letters = wheelLetters,
            selectedIndices = selectedIndices,
            onSelectionChanged = { selection ->
                selectedIndices = selection
                liveWord = selection.joinToString("") { wheelLetters[it].toString() }
            },
            onSelectionFinished = {
                val submitted = normalizeWord(liveWord)
                when {
                    submitted in level.targets && submitted !in foundWords -> {
                        foundWords = foundWords + submitted
                        coins += 5
                        status = "Harika! $submitted"
                    }
                    submitted in foundWords -> status = "$submitted zaten bulundu"
                    submitted in level.bonusWords && submitted !in foundBonusWords -> {
                        foundBonusWords = foundBonusWords + submitted
                        coins += 10
                        status = "Bonus kelime! +10 🪙"
                    }
                    submitted in foundBonusWords -> status = "$submitted bonus olarak bulundu"
                    submitted.isNotBlank() -> status = "$submitted bu bölümde yok"
                }
                selectedIndices = emptyList()
                liveWord = ""
            }
        )

        Spacer(Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    wheelLetters = wheelLetters.shuffled()
                    selectedIndices = emptyList()
                    liveWord = ""
                    status = "Harfler karıştırıldı"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234782)),
            ) {
                Text("Karıştır")
            }

            Button(
                enabled = coins >= 30 && !complete,
                onClick = {
                    val hidden = allCells(level).filter { cell ->
                        cell !in hintedCells && !cellRevealedByFoundWord(level, cell, foundWords)
                    }
                    val reveal = hidden.randomOrNull()
                    if (reveal != null) {
                        coins -= 30
                        hintedCells = hintedCells + reveal
                        status = "İpucu: bir harf açıldı"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7356B8)),
            ) {
                Text("İpucu 30 🪙")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (complete) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF123D36), RoundedCornerShape(18.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "BÖLÜM TAMAMLANDI!",
                        color = Color(0xFF7EF0B8),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "+${level.targets.size * 5} kelime ödülü · ${foundBonusWords.size} bonus",
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Button(
                        onClick = { levelIndex = (levelIndex + 1) % levels.size },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25A878)),
                    ) {
                        Text("Sonraki Bölüm")
                    }
                }
            }
        }
    }
}

private fun normalizeWord(word: String): String = word.trim().uppercase(trLocale)

private fun cellsFor(placement: Placement): List<Pair<Cell, Char>> =
    placement.word.mapIndexed { index, char ->
        val row = placement.row + if (placement.horizontal) 0 else index
        val col = placement.col + if (placement.horizontal) index else 0
        Cell(row, col) to char
    }

private fun allCells(level: Level): Set<Cell> = level.placements
    .flatMap(::cellsFor)
    .map { it.first }
    .toSet()

private fun cellRevealedByFoundWord(level: Level, cell: Cell, foundWords: Set<String>): Boolean =
    level.placements.any { placement ->
        placement.word in foundWords && cellsFor(placement).any { it.first == cell }
    }

@Composable
private fun CrosswordBoard(
    level: Level,
    foundWords: Set<String>,
    hintedCells: Set<Cell>,
) {
    val entries = remember(level) { level.placements.flatMap(::cellsFor) }
    val lettersByCell = remember(level) { entries.associate { it.first to it.second } }
    val rows = (lettersByCell.keys.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (lettersByCell.keys.maxOfOrNull { it.col } ?: 0) + 1
    val cellSize = when {
        cols >= 6 -> 42.dp
        cols == 5 -> 46.dp
        else -> 50.dp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1E3D), RoundedCornerShape(22.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(cols) { col ->
                        val cell = Cell(row, col)
                        val letter = lettersByCell[cell]
                        if (letter == null) {
                            Spacer(Modifier.size(cellSize))
                        } else {
                            val revealed = cell in hintedCells || cellRevealedByFoundWord(level, cell, foundWords)
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .background(
                                        if (revealed) Color(0xFF2E8CFF) else Color(0xFF1B315A),
                                        RoundedCornerShape(10.dp),
                                    )
                                    .border(1.dp, Color(0xFF3A5E98), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (revealed) letter.toString() else "",
                                    color = Color.White,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterWheel(
    letters: List<Char>,
    selectedIndices: List<Int>,
    onSelectionChanged: (List<Int>) -> Unit,
    onSelectionFinished: () -> Unit,
) {
    var wheelSize by remember { mutableStateOf(IntSize.Zero) }

    fun positions(): List<Offset> {
        if (wheelSize == IntSize.Zero) return emptyList()
        val center = Offset(wheelSize.width / 2f, wheelSize.height / 2f)
        val radius = minOf(wheelSize.width, wheelSize.height) * 0.33f
        return letters.indices.map { index ->
            val angle = -PI / 2 + (2 * PI * index / letters.size)
            Offset(
                center.x + (cos(angle) * radius).toFloat(),
                center.y + (sin(angle) * radius).toFloat(),
            )
        }
    }

    fun indexAt(point: Offset): Int? {
        val points = positions()
        if (points.isEmpty()) return null
        val hitRadius = minOf(wheelSize.width, wheelSize.height) * 0.13f
        return points.indices.firstOrNull { i ->
            hypot((point.x - points[i].x).toDouble(), (point.y - points[i].y).toDouble()) <= hitRadius
        }
    }

    Box(
        modifier = Modifier
            .size(270.dp)
            .onSizeChanged { wheelSize = it }
            .pointerInput(letters, wheelSize) {
                var gestureSelection = emptyList<Int>()
                detectDragGestures(
                    onDragStart = { point ->
                        val index = indexAt(point)
                        gestureSelection = if (index != null) listOf(index) else emptyList()
                        onSelectionChanged(gestureSelection)
                    },
                    onDragEnd = {
                        onSelectionFinished()
                        gestureSelection = emptyList()
                    },
                    onDragCancel = {
                        onSelectionFinished()
                        gestureSelection = emptyList()
                    },
                    onDrag = { change, _ ->
                        val index = indexAt(change.position)
                        if (index != null && index !in gestureSelection) {
                            gestureSelection = gestureSelection + index
                            onSelectionChanged(gestureSelection)
                        }
                    },
                )
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = Color(0xFF142B55),
                radius = minOf(size.width, size.height) * 0.46f,
                center = center,
            )

            val points = positions()
            selectedIndices.zipWithNext().forEach { (a, b) ->
                if (a in points.indices && b in points.indices) {
                    drawLine(
                        color = Color(0xFF58A6FF),
                        start = points[a],
                        end = points[b],
                        strokeWidth = 18f,
                        cap = StrokeCap.Round,
                    )
                }
            }

            points.forEachIndexed { index, point ->
                val selected = index in selectedIndices
                drawCircle(
                    color = if (selected) Color(0xFF2D8CFF) else Color.White,
                    radius = minOf(size.width, size.height) * 0.105f,
                    center = point,
                )
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = if (selected) {
                            android.graphics.Color.WHITE
                        } else {
                            android.graphics.Color.rgb(17, 31, 63)
                        }
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = minOf(size.width, size.height) * 0.10f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val metrics = paint.fontMetrics
                    val baseline = point.y - (metrics.ascent + metrics.descent) / 2f
                    canvas.nativeCanvas.drawText(letters[index].toString(), point.x, baseline, paint)
                }
            }
        }
    }
}
