package com.zafer.wordz

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF07142D)) { WordZApp() }
            }
        }
    }
}

data class Placement(val word: String, val row: Int, val col: Int, val horizontal: Boolean)
data class Level(val title: String, val letters: List<Char>, val targets: List<String>, val bonusWords: Set<String>, val placements: List<Placement>)
private data class Cell(val row: Int, val col: Int)
private enum class Screen { HOME, MAP, GAME }

private val trLocale = Locale("tr", "TR")
private const val PREFS = "wordz_progress"
private const val KEY_UNLOCKED = "unlocked_level"
private const val KEY_COINS = "coins"

private val levels = listOf(
    Level("Başlangıç", listOf('K','A','L','E'), listOf("KALE","KAL","ELA"), setOf("ALE","EL"), listOf(Placement("KALE",2,0,true),Placement("KAL",2,0,false),Placement("ELA",2,3,false))),
    Level("Sıcak Kumlar", listOf('M','A','S','A'), listOf("MASA","ASA","AMA"), setOf("AS"), listOf(Placement("MASA",2,0,true),Placement("ASA",2,1,false),Placement("AMA",1,3,false))),
    Level("Taş Yolu", listOf('T','A','Ş'), listOf("TAŞ","AŞ","TA"), emptySet(), listOf(Placement("TAŞ",2,0,true),Placement("AŞ",2,1,false),Placement("TA",2,0,false))),
    Level("Elma Bahçesi", listOf('E','L','M','A'), listOf("ELMA","ELA","MAL"), setOf("EL","AL"), listOf(Placement("ELMA",2,0,true),Placement("ELA",2,0,false),Placement("MAL",2,2,false))),
    Level("Oyun Zamanı", listOf('O','Y','U','N'), listOf("OYUN","OY","YUN"), setOf("UN"), listOf(Placement("OYUN",2,0,true),Placement("OY",2,0,false),Placement("YUN",2,1,false))),
    Level("Deniz Esintisi", listOf('D','E','N','İ','Z'), listOf("DENİZ","DİZ","İZ"), setOf("DEN"), listOf(Placement("DENİZ",2,0,true),Placement("DİZ",2,0,false),Placement("İZ",2,3,false))),
    Level("Gece Işıkları", listOf('G','E','C','E'), listOf("GECE","ECE","GE"), setOf("CE"), listOf(Placement("GECE",2,0,true),Placement("ECE",2,1,false),Placement("GE",2,0,false))),
    Level("Kış Sabahı", listOf('K','I','Ş'), listOf("KIŞ","IŞ","KI"), setOf("İŞ"), listOf(Placement("KIŞ",2,0,true),Placement("IŞ",2,1,false),Placement("KI",2,0,false))),
    Level("Renkli Sokak", listOf('R','E','N','K'), listOf("RENK","ERK","EN"), setOf("ER"), listOf(Placement("RENK",2,0,true),Placement("ERK",2,1,false),Placement("EN",2,1,true))),
    Level("Yolculuk", listOf('Y','O','L'), listOf("YOL","OY","OL"), setOf("YO"), listOf(Placement("YOL",2,0,true),Placement("OY",2,1,false),Placement("OL",2,1,true)))
)

@Composable
fun WordZApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var unlockedLevel by remember { mutableIntStateOf(prefs.getInt(KEY_UNLOCKED, 0).coerceIn(0, levels.lastIndex)) }
    var coins by remember { mutableIntStateOf(prefs.getInt(KEY_COINS, 100)) }
    var screen by remember { mutableStateOf(Screen.HOME) }
    var selectedLevel by remember { mutableIntStateOf(unlockedLevel) }
    fun save() { prefs.edit().putInt(KEY_UNLOCKED, unlockedLevel).putInt(KEY_COINS, coins).apply() }
    when (screen) {
        Screen.HOME -> HomeScreen(coins, unlockedLevel, { selectedLevel = unlockedLevel; screen = Screen.GAME }, { screen = Screen.MAP })
        Screen.MAP -> LevelMapScreen(unlockedLevel, coins, { screen = Screen.HOME }) { selectedLevel = it; screen = Screen.GAME }
        Screen.GAME -> GameScreen(selectedLevel, coins, { coins = it; save() }, { save(); screen = Screen.MAP }, {
            if (selectedLevel >= unlockedLevel && unlockedLevel < levels.lastIndex) unlockedLevel = selectedLevel + 1
            save()
        }) { if (selectedLevel < levels.lastIndex) selectedLevel += 1 else screen = Screen.MAP }
    }
}

@Composable
private fun HomeScreen(coins: Int, unlockedLevel: Int, onContinue: () -> Unit, onMap: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("WordZ", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Black)
        Text("Kelime Bulmaca", color = Color(0xFF9EB5E4), fontSize = 18.sp)
        Spacer(Modifier.height(28.dp))
        Box(Modifier.background(Color(0xFF11264D), RoundedCornerShape(28.dp)).padding(20.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🪙 $coins", color = Color(0xFFFFD166), fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Açık bölüm: ${unlockedLevel + 1}/${levels.size}", color = Color.White)
            }
        }
        Spacer(Modifier.height(30.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D8CFF))) { Text("DEVAM ET", fontWeight = FontWeight.Black) }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onMap, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234782))) { Text("BÖLÜMLER") }
        Spacer(Modifier.height(24.dp))
        Text("v0.4.0", color = Color(0xFF6984B7), fontSize = 12.sp)
    }
}

@Composable
private fun LevelMapScreen(unlockedLevel: Int, coins: Int, onBack: () -> Unit, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203A6D))) { Text("← Geri") }
            Text("🪙 $coins", color = Color(0xFFFFD166), fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(14.dp)); Text("Bölüm Haritası", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black); Text("10 bölümlük ilk seri", color = Color(0xFF9EB5E4)); Spacer(Modifier.height(18.dp))
        levels.forEachIndexed { index, level ->
            val unlocked = index <= unlockedLevel
            Button(onClick = { if (unlocked) onSelect(index) }, enabled = unlocked, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = if (index == unlockedLevel) Color(0xFF2D8CFF) else Color(0xFF18345F), disabledContainerColor = Color(0xFF101E36))) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${index + 1}. ${level.title}", fontWeight = FontWeight.Bold); Text(if (unlocked) "▶" else "🔒") }
            }
        }
    }
}

@Composable
private fun GameScreen(levelIndex: Int, coins: Int, onCoinsChanged: (Int) -> Unit, onBack: () -> Unit, onCompleted: () -> Unit, onNext: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val level = levels[levelIndex]
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 65) }
    DisposableEffect(Unit) { onDispose { tone.release() } }
    var wheelLetters by remember(levelIndex) { mutableStateOf(level.letters) }
    var foundWords by remember(levelIndex) { mutableStateOf(setOf<String>()) }
    var foundBonusWords by remember(levelIndex) { mutableStateOf(setOf<String>()) }
    var hintedCells by remember(levelIndex) { mutableStateOf(setOf<Cell>()) }
    var selectedIndices by remember(levelIndex) { mutableStateOf(emptyList<Int>()) }
    var liveWord by remember(levelIndex) { mutableStateOf("") }
    var status by remember(levelIndex) { mutableStateOf("Harfleri sürükleyerek birleştir") }
    var completionHandled by remember(levelIndex) { mutableStateOf(false) }
    val complete = level.targets.all { it in foundWords }
    if (complete && !completionHandled) {
        completionHandled = true
        onCoinsChanged(coins + 25)
        onCompleted()
        tone.startTone(ToneGenerator.TONE_PROP_ACK, 180)
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    val statusScale by animateFloatAsState(if (liveWord.isNotBlank()) 1.04f else 1f, tween(140), label = "statusScale")
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203A6D))) { Text("←") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("WordZ", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black); Text(level.title, color = Color(0xFF9EB5E4), fontSize = 12.sp) }
            Text("🪙 $coins", color = Color(0xFFFFD166), fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(8.dp)); CrosswordBoard(level, foundWords, hintedCells); Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Bölüm ${levelIndex + 1}/${levels.size}", color = Color.White, fontSize = 13.sp); Text("${foundWords.size}/${level.targets.size} kelime · Bonus ${foundBonusWords.size}", color = Color(0xFFA8BCE1), fontSize = 12.sp) }
        Spacer(Modifier.height(7.dp))
        Box(Modifier.fillMaxWidth().scale(statusScale).background(Color(0xFF12264B), RoundedCornerShape(16.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
            Text(if (liveWord.isNotBlank()) liveWord else status, color = when { liveWord.isNotBlank() -> Color.White; status.startsWith("Bonus") -> Color(0xFFFFD166); status.startsWith("Harika") -> Color(0xFF7EF0B8); else -> Color(0xFFA8BCE1) }, fontWeight = FontWeight.Bold, fontSize = if (liveWord.isNotBlank()) 23.sp else 14.sp)
        }
        Spacer(Modifier.height(4.dp))
        LetterWheel(wheelLetters, selectedIndices, { sel -> selectedIndices = sel; liveWord = sel.joinToString("") { wheelLetters[it].toString() } }) {
            val submitted = normalizeWord(liveWord)
            when {
                submitted in level.targets && submitted !in foundWords -> { foundWords = foundWords + submitted; onCoinsChanged(coins + 5); status = "Harika! $submitted"; tone.startTone(ToneGenerator.TONE_PROP_BEEP2,100); haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
                submitted in foundWords -> status = "$submitted zaten bulundu"
                submitted in level.bonusWords && submitted !in foundBonusWords -> { foundBonusWords = foundBonusWords + submitted; onCoinsChanged(coins + 10); status = "Bonus kelime! +10 🪙"; tone.startTone(ToneGenerator.TONE_PROP_BEEP,80) }
                submitted in foundBonusWords -> status = "$submitted bonus olarak bulundu"
                submitted.isNotBlank() -> { status = "$submitted bu bölümde yok"; tone.startTone(ToneGenerator.TONE_PROP_NACK,90) }
            }
            selectedIndices = emptyList(); liveWord = ""
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { wheelLetters = wheelLetters.shuffled(); status = "Harfler karıştırıldı" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234782))) { Text("Karıştır") }
            Button(enabled = coins >= 30 && !complete, onClick = { val hidden = allCells(level).filter { it !in hintedCells && !cellRevealedByFoundWord(level,it,foundWords) }; hidden.randomOrNull()?.let { onCoinsChanged(coins - 30); hintedCells = hintedCells + it; status = "İpucu: bir harf açıldı" } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7356B8))) { Text("İpucu 30 🪙") }
        }
        Spacer(Modifier.height(6.dp))
        AnimatedVisibility(complete) {
            Box(Modifier.fillMaxWidth().background(Color(0xFF123D36), RoundedCornerShape(22.dp)).padding(14.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("★ BÖLÜM TAMAMLANDI ★", color = Color(0xFFFFD166), fontSize = 18.sp, fontWeight = FontWeight.Black); Text("Tamamlama ödülü +25 🪙", color = Color.White); Spacer(Modifier.height(8.dp)); Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25A878))) { Text(if (levelIndex < levels.lastIndex) "Sonraki Bölüm" else "Haritaya Dön") } }
            }
        }
    }
}

private fun normalizeWord(word: String) = word.trim().uppercase(trLocale)
private fun cellsFor(p: Placement) = p.word.mapIndexed { i,ch -> Cell(p.row + if (p.horizontal) 0 else i, p.col + if (p.horizontal) i else 0) to ch }
private fun allCells(level: Level) = level.placements.flatMap(::cellsFor).map { it.first }.toSet()
private fun cellRevealedByFoundWord(level: Level, cell: Cell, foundWords: Set<String>) = level.placements.any { it.word in foundWords && cellsFor(it).any { pair -> pair.first == cell } }

@Composable
private fun CrosswordBoard(level: Level, foundWords: Set<String>, hintedCells: Set<Cell>) {
    val entries = remember(level) { level.placements.flatMap(::cellsFor) }; val lettersByCell = remember(level) { entries.associate { it.first to it.second } }
    val rows = (lettersByCell.keys.maxOfOrNull { it.row } ?: 0) + 1; val cols = (lettersByCell.keys.maxOfOrNull { it.col } ?: 0) + 1; val cellSize = if (cols >= 5) 42.dp else 48.dp
    Box(Modifier.fillMaxWidth().background(Color(0xFF0D1E3D), RoundedCornerShape(22.dp)).padding(10.dp), contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { repeat(rows) { r -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(cols) { c -> val cell = Cell(r,c); val letter = lettersByCell[cell]; if (letter == null) Spacer(Modifier.size(cellSize)) else { val rev = cell in hintedCells || cellRevealedByFoundWord(level,cell,foundWords); Box(Modifier.size(cellSize).background(if (rev) Color(0xFF2E8CFF) else Color(0xFF1B315A), RoundedCornerShape(10.dp)).border(1.dp,Color(0xFF3A5E98),RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text(if (rev) letter.toString() else "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black) } } } } } }
    }
}

@Composable
private fun LetterWheel(letters: List<Char>, selectedIndices: List<Int>, onSelectionChanged: (List<Int>) -> Unit, onSelectionFinished: () -> Unit) {
    var wheelSize by remember { mutableStateOf(IntSize.Zero) }
    fun positions(): List<Offset> { if (wheelSize == IntSize.Zero) return emptyList(); val center = Offset(wheelSize.width/2f,wheelSize.height/2f); val radius = minOf(wheelSize.width,wheelSize.height)*0.33f; return letters.indices.map { i -> val a=-PI/2+2*PI*i/letters.size; Offset(center.x+(cos(a)*radius).toFloat(),center.y+(sin(a)*radius).toFloat()) } }
    fun indexAt(p: Offset): Int? { val pts=positions(); val hit=minOf(wheelSize.width,wheelSize.height)*0.13f; return pts.indices.firstOrNull { i -> hypot((p.x-pts[i].x).toDouble(),(p.y-pts[i].y).toDouble())<=hit } }
    Box(Modifier.size(250.dp).onSizeChanged { wheelSize=it }.pointerInput(letters,wheelSize) { var sel=emptyList<Int>(); detectDragGestures(onDragStart={p-> val i=indexAt(p); sel=if(i!=null) listOf(i) else emptyList(); onSelectionChanged(sel)},onDragEnd={onSelectionFinished();sel=emptyList()},onDragCancel={onSelectionFinished();sel=emptyList()}) { change,_-> val i=indexAt(change.position); if(i!=null&&i !in sel){sel=sel+i;onSelectionChanged(sel)} } }) {
        Canvas(Modifier.fillMaxSize()) { val center=Offset(size.width/2f,size.height/2f); drawCircle(Color(0xFF15254C),minOf(size.width,size.height)*0.46f,center); val pts=positions(); selectedIndices.zipWithNext().forEach{(a,b)->if(a in pts.indices&&b in pts.indices)drawLine(Color(0xFF58A6FF),pts[a],pts[b],18f,StrokeCap.Round)}; pts.forEachIndexed{index,point->val selected=index in selectedIndices;drawCircle(if(selected)Color(0xFF2D8CFF)else Color.White,minOf(size.width,size.height)*0.105f,point);drawIntoCanvas{canvas->val paint=android.graphics.Paint().apply{color=if(selected)android.graphics.Color.WHITE else android.graphics.Color.rgb(17,31,63);textAlign=android.graphics.Paint.Align.CENTER;textSize=minOf(size.width,size.height)*0.10f;typeface=android.graphics.Typeface.DEFAULT_BOLD};val m=paint.fontMetrics;canvas.nativeCanvas.drawText(letters[index].toString(),point.x,point.y-(m.ascent+m.descent)/2f,paint)}} }
    }
}
