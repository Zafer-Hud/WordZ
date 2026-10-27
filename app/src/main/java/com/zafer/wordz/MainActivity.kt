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
import androidx.compose.foundation.layout.weight
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B132B)) {
                    WordZGame()
                }
            }
        }
    }
}

data class Level(
    val letters: List<Char>,
    val targets: List<String>
)

private val levels = listOf(
    Level(listOf('K', 'A', 'L', 'E'), listOf("KALE", "KAL", "ELA")),
    Level(listOf('S', 'U', 'R', 'A'), listOf("SURA", "SU", "AR")),
    Level(listOf('T', 'A', 'Ş'), listOf("TAŞ", "AŞ")),
)

@Composable
fun WordZGame() {
    var levelIndex by remember { mutableIntStateOf(0) }
    val level = levels[levelIndex]
    var wheelLetters by remember(levelIndex) { mutableStateOf(level.letters) }
    var foundWords by remember(levelIndex) { mutableStateOf(setOf<String>()) }
    var selectedIndices by remember(levelIndex) { mutableStateOf(emptyList<Int>()) }
    var liveWord by remember(levelIndex) { mutableStateOf("") }
    var status by remember(levelIndex) { mutableStateOf("Harfleri sürükleyerek birleştir") }

    val complete = level.targets.all { it in foundWords }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("WordZ", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Text("Kelime Bulmaca", color = Color(0xFF92A4C8), fontSize = 15.sp)
        Spacer(Modifier.height(22.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Bölüm ${levelIndex + 1}", color = Color(0xFFFFD166), fontWeight = FontWeight.Bold)
            Text("${foundWords.size}/${level.targets.size}", color = Color.White)
        }

        Spacer(Modifier.height(18.dp))
        WordBoard(level.targets, foundWords)
        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121D3A), RoundedCornerShape(18.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (liveWord.isNotBlank()) liveWord else status,
                color = if (liveWord.isNotBlank()) Color.White else Color(0xFF9DAFD2),
                fontWeight = FontWeight.Bold,
                fontSize = if (liveWord.isNotBlank()) 24.sp else 14.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        LetterWheel(
            letters = wheelLetters,
            selectedIndices = selectedIndices,
            onSelectionChanged = { selection ->
                selectedIndices = selection
                liveWord = selection.joinToString("") { wheelLetters[it].toString() }
            },
            onSelectionFinished = {
                val submitted = liveWord.uppercase()
                when {
                    submitted in level.targets && submitted !in foundWords -> {
                        foundWords = foundWords + submitted
                        status = "Harika! $submitted"
                    }
                    submitted in foundWords -> status = "$submitted zaten bulundu"
                    submitted.isNotBlank() -> status = "$submitted geçerli hedef değil"
                }
                selectedIndices = emptyList()
                liveWord = ""
            }
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                wheelLetters = wheelLetters.shuffled()
                selectedIndices = emptyList()
                liveWord = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF243A73))
        ) {
            Text("Harfleri Karıştır")
        }

        Spacer(Modifier.weight(1f))
        if (complete) {
            Text("BÖLÜM TAMAMLANDI!", color = Color(0xFF72E1A6), fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { levelIndex = (levelIndex + 1) % levels.size },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28B487))
            ) {
                Text("Sonraki Bölüm")
            }
        }
    }
}

@Composable
private fun WordBoard(targets: List<String>, found: Set<String>) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF101A34), RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        targets.forEach { word ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                word.forEach { ch ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (word in found) Color(0xFF2D8CFF) else Color(0xFF1C294E),
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, Color(0xFF35558F), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (word in found) ch.toString() else "",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
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
    onSelectionFinished: () -> Unit
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
                center.y + (sin(angle) * radius).toFloat()
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
            .size(300.dp)
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
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Color(0xFF15254C), radius = minOf(size.width, size.height) * 0.46f, center = center)

            val points = positions()
            selectedIndices.zipWithNext().forEach { (a, b) ->
                if (a in points.indices && b in points.indices) {
                    drawLine(
                        color = Color(0xFF58A6FF),
                        start = points[a],
                        end = points[b],
                        strokeWidth = 18f,
                        cap = StrokeCap.Round
                    )
                }
            }

            points.forEachIndexed { index, point ->
                val selected = index in selectedIndices
                drawCircle(
                    color = if (selected) Color(0xFF2D8CFF) else Color.White,
                    radius = minOf(size.width, size.height) * 0.105f,
                    center = point
                )
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = if (selected) android.graphics.Color.WHITE else android.graphics.Color.rgb(17, 31, 63)
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
