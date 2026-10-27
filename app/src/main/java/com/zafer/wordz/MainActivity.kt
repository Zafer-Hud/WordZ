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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import java.time.LocalDate
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

private data class Cell(val row: Int, val col: Int)
private data class Chapter(val title: String, val subtitle: String, val start: Int, val endExclusive: Int)
private enum class Screen { HOME, CHAPTERS, MAP, GAME, SETTINGS }

private val trLocale = Locale("tr", "TR")
private const val PREFS = "wordz_progress"
private const val KEY_UNLOCKED = "unlocked_level"
private const val KEY_COINS = "coins"
private const val KEY_COMPLETED = "completed_levels"
private const val KEY_STARS = "level_stars"
private const val KEY_DAILY_DATE = "daily_reward_date"
private const val KEY_CLAIMED_CHESTS = "claimed_star_chests"
private const val KEY_SOUND = "sound_enabled"
private const val KEY_HAPTIC = "haptic_enabled"

private fun chapters(): List<Chapter> = levels.indices.chunked(5).mapIndexed { index, chunk ->
    val names = listOf("Başlangıç Vadisi", "Sahil Yolu", "Yeşil Orman", "Güneş Tepeleri", "Gece Rotası", "Uzak Diyarlar", "Zafer Zirvesi")
    Chapter(
        title = "Bölüm ${index + 1} · ${names.getOrElse(index) { "Macera" }}",
        subtitle = "${chunk.size} level",
        start = chunk.first(),
        endExclusive = chunk.last() + 1,
    )
}

private fun decodeStars(raw: Set<String>): Map<Int, Int> = raw.mapNotNull { item ->
    val parts = item.split(':')
    val level = parts.getOrNull(0)?.toIntOrNull()
    val stars = parts.getOrNull(1)?.toIntOrNull()
    if (level != null && stars != null && level in levels.indices) level to stars.coerceIn(0, 3) else null
}.toMap()

private fun encodeStars(stars: Map<Int, Int>): Set<String> = stars.map { "${it.key}:${it.value}" }.toSet()

@Composable
fun WordZApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    val savedUnlocked = prefs.getInt(KEY_UNLOCKED, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(KEY_COMPLETED, emptySet()).orEmpty()
        .mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
    val migratedCompleted = if (savedCompleted.isEmpty() && savedUnlocked > 0) (0 until savedUnlocked).toSet() else savedCompleted
    val savedStars = decodeStars(prefs.getStringSet(KEY_STARS, emptySet()).orEmpty()).toMutableMap()
    migratedCompleted.forEach { if (it !in savedStars) savedStars[it] = 1 }

    var unlockedLevel by remember { mutableIntStateOf(savedUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(KEY_COINS, 100)) }
    var completedLevels by remember { mutableStateOf(migratedCompleted) }
    var levelStars by remember { mutableStateOf(savedStars.toMap()) }
    var claimedChests by remember { mutableIntStateOf(prefs.getInt(KEY_CLAIMED_CHESTS, 0)) }
    var dailyDate by remember { mutableStateOf(prefs.getString(KEY_DAILY_DATE, "").orEmpty()) }
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_SOUND, true)) }
    var hapticEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_HAPTIC, true)) }
    var screen by remember { mutableStateOf(Screen.HOME) }
    var selectedLevel by remember { mutableIntStateOf(unlockedLevel) }
    var selectedChapter by remember { mutableIntStateOf((unlockedLevel / 5).coerceIn(0, chapters().lastIndex)) }

    val totalStars = levelStars.values.sum()
    val availableChests = (totalStars / 15 - claimedChests).coerceAtLeast(0)
    val today = LocalDate.now().toString()
    val dailyAvailable = dailyDate != today

    fun persist() {
        prefs.edit()
            .putInt(KEY_UNLOCKED, unlockedLevel)
            .putInt(KEY_COINS, coins)
            .putStringSet(KEY_COMPLETED, completedLevels.map(Int::toString).toSet())
            .putStringSet(KEY_STARS, encodeStars(levelStars))
            .putInt(KEY_CLAIMED_CHESTS, claimedChests)
            .putString(KEY_DAILY_DATE, dailyDate)
            .putBoolean(KEY_SOUND, soundEnabled)
            .putBoolean(KEY_HAPTIC, hapticEnabled)
            .apply()
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            coins = coins,
            completedCount = completedLevels.size,
            totalStars = totalStars,
            dailyAvailable = dailyAvailable,
            availableChests = availableChests,
            onContinue = {
                selectedLevel = unlockedLevel
                selectedChapter = (unlockedLevel / 5).coerceIn(0, chapters().lastIndex)
                screen = Screen.GAME
            },
            onChapters = { screen = Screen.CHAPTERS },
            onDaily = {
                if (dailyAvailable) {
                    coins += 40
                    dailyDate = today
                    prefs.edit().putInt(KEY_COINS, coins).putString(KEY_DAILY_DATE, dailyDate).apply()
                }
            },
            onChest = {
                if (availableChests > 0) {
                    coins += 75
                    claimedChests += 1
                    prefs.edit().putInt(KEY_COINS, coins).putInt(KEY_CLAIMED_CHESTS, claimedChests).apply()
                }
            },
            onSettings = { screen = Screen.SETTINGS },
        )
        Screen.SETTINGS -> SettingsScreen(
            soundEnabled = soundEnabled,
            hapticEnabled = hapticEnabled,
            onToggleSound = {
                soundEnabled = !soundEnabled
                prefs.edit().putBoolean(KEY_SOUND, soundEnabled).apply()
            },
            onToggleHaptic = {
                hapticEnabled = !hapticEnabled
                prefs.edit().putBoolean(KEY_HAPTIC, hapticEnabled).apply()
            },
            onBack = { persist(); screen = Screen.HOME },
        )
        Screen.CHAPTERS -> ChapterScreen(
            unlockedLevel = unlockedLevel,
            stars = levelStars,
            coins = coins,
            onBack = { persist(); screen = Screen.HOME },
            onSelect = { chapterIndex -> selectedChapter = chapterIndex; screen = Screen.MAP },
        )
        Screen.MAP -> ZigzagLevelMap(
            chapterIndex = selectedChapter,
            unlockedLevel = unlockedLevel,
            stars = levelStars,
            coins = coins,
            onBack = { persist(); screen = Screen.CHAPTERS },
            onSelect = { selectedLevel = it; screen = Screen.GAME },
        )
        Screen.GAME -> GameScreen(
            levelIndex = selectedLevel,
            coins = coins,
            previousStars = levelStars[selectedLevel] ?: 0,
            soundEnabled = soundEnabled,
            hapticEnabled = hapticEnabled,
            onCoinsChanged = { newCoins ->
                coins = newCoins
                prefs.edit().putInt(KEY_COINS, newCoins).apply()
            },
            onBack = { persist(); screen = Screen.MAP },
            onCompleted = { earnedStars ->
                val firstCompletion = selectedLevel !in completedLevels
                val newCompleted = completedLevels + selectedLevel
                val oldStars = levelStars[selectedLevel] ?: 0
                val newStars = if (earnedStars > oldStars) levelStars + (selectedLevel to earnedStars) else levelStars
                val newCoins = coins + if (firstCompletion) 25 else 0
                val newUnlocked = if (selectedLevel >= unlockedLevel && unlockedLevel < levels.lastIndex) selectedLevel + 1 else unlockedLevel
                completedLevels = newCompleted
                levelStars = newStars
                coins = newCoins
                unlockedLevel = newUnlocked
                prefs.edit()
                    .putInt(KEY_UNLOCKED, newUnlocked)
                    .putInt(KEY_COINS, newCoins)
                    .putStringSet(KEY_COMPLETED, newCompleted.map(Int::toString).toSet())
                    .putStringSet(KEY_STARS, encodeStars(newStars))
                    .apply()
            },
            onNext = {
                val chapter = chapters()[selectedChapter]
                if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) selectedLevel += 1 else screen = Screen.MAP
            },
        )
    }
}

@Composable
private fun HomeScreen(
    coins: Int,
    completedCount: Int,
    totalStars: Int,
    dailyAvailable: Boolean,
    availableChests: Int,
    onContinue: () -> Unit,
    onChapters: () -> Unit,
    onDaily: () -> Unit,
    onChest: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("WordZ", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Black)
        Text("Kelime Bulmaca", color = Color(0xFF9EB5E4), fontSize = 18.sp)
        Spacer(Modifier.height(20.dp))
        Box(Modifier.background(Color(0xFF11264D), RoundedCornerShape(28.dp)).padding(18.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("★ $totalStars / ${levels.size * 3}", color = Color(0xFFFFD166), fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("$completedCount/${levels.size} level tamamlandı", color = Color.White, fontWeight = FontWeight.Bold)
                Text("🪙 $coins", color = Color(0xFFFFD166), fontSize = 15.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onDaily,
                enabled = dailyAvailable,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A5A16)),
            ) { Text(if (dailyAvailable) "🎁 Günlük +40" else "✓ Günlük Alındı", fontSize = 12.sp) }
            Button(
                onClick = onChest,
                enabled = availableChests > 0,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C5C20)),
            ) { Text(if (availableChests > 0) "📦 Sandık +75" else "📦 ${15 - (totalStars % 15)}★ sonra", fontSize = 12.sp) }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D8CFF))) {
            Text("DEVAM ET", fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(9.dp))
        Button(onClick = onChapters, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234782))) { Text("BÖLÜMLER") }
        Spacer(Modifier.height(9.dp))
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3159))) { Text("⚙ AYARLAR") }
        Spacer(Modifier.height(18.dp))
        Text("v${BuildConfig.VERSION_NAME}", color = Color(0xFF6984B7), fontSize = 12.sp)
    }
}

@Composable
private fun SettingsScreen(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203A6D))) { Text("← Geri") }
        }
        Spacer(Modifier.height(40.dp))
        Text("Ayarlar", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onToggleSound, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF193B70))) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🔊 Ses Efektleri", fontWeight = FontWeight.Bold)
                Text(if (soundEnabled) "AÇIK" else "KAPALI", color = if (soundEnabled) Color(0xFF7EF0B8) else Color(0xFFFFA0A0))
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onToggleHaptic, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF193B70))) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📳 Titreşim", fontWeight = FontWeight.Bold)
                Text(if (hapticEnabled) "AÇIK" else "KAPALI", color = if (hapticEnabled) Color(0xFF7EF0B8) else Color(0xFFFFA0A0))
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Ayarların otomatik kaydedilir.", color = Color(0xFF9EB5E4), fontSize = 13.sp)
    }
}

@Composable
private fun ChapterScreen(
    unlockedLevel: Int,
    stars: Map<Int, Int>,
    coins: Int,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val chapterList = chapters()
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203A6D))) { Text("← Ana Menü") }
            Text("🪙 $coins", color = Color(0xFFFFD166), fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(12.dp))
        Text("Dünya Haritası", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Bir bölüm seç ve yıldızlarını tamamla", color = Color(0xFF9EB5E4))
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            chapterList.forEachIndexed { index, chapter ->
                val unlocked = unlockedLevel >= chapter.start
                val chapterStars = (chapter.start until chapter.endExclusive).sumOf { stars[it] ?: 0 }
                val maxStars = (chapter.endExclusive - chapter.start) * 3
                Button(
                    enabled = unlocked,
                    onClick = { onSelect(index) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF193B70), disabledContainerColor = Color(0xFF101E36)),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(chapter.title, fontWeight = FontWeight.Black)
                            Text(if (unlocked) "★ $chapterStars/$maxStars" else "🔒")
                        }
                        Text(chapter.subtitle, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ZigzagLevelMap(
    chapterIndex: Int,
    unlockedLevel: Int,
    stars: Map<Int, Int>,
    coins: Int,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val chapter = chapters()[chapterIndex]
    val levelIndices = (chapter.start until chapter.endExclusive).toList()
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203A6D))) { Text("← Bölümler") }
            Text("🪙 $coins", color = Color(0xFFFFD166), fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        Text(chapter.title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text("Level yolu", color = Color(0xFF9EB5E4))
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            levelIndices.forEachIndexed { localIndex, globalIndex ->
                val unlocked = globalIndex <= unlockedLevel
                val earned = stars[globalIndex] ?: 0
                val arrangement = when (localIndex % 3) { 0 -> Arrangement.Start; 1 -> Arrangement.Center; else -> Arrangement.End }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = arrangement) {
                    Button(
                        enabled = unlocked,
                        onClick = { onSelect(globalIndex) },
                        modifier = Modifier.size(108.dp, 82.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (earned > 0) Color(0xFF19765E) else Color(0xFF2D8CFF),
                            disabledContainerColor = Color(0xFF101E36),
                        ),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LEVEL ${globalIndex + 1}", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Text(if (unlocked) "★".repeat(earned) + "☆".repeat(3 - earned) else "🔒", color = Color(0xFFFFD166), fontSize = 15.sp)
                        }
                    }
                }
                if (localIndex < levelIndices.lastIndex) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text(if (localIndex % 2 == 0) "╲  ·  ╲" else "╱  ·  ╱", color = Color(0xFF4E78B8), fontSize = 26.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GameScreen(
    levelIndex: Int,
    coins: Int,
    previousStars: Int,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onCoinsChanged: (Int) -> Unit,
    onBack: () -> Unit,
    onCompleted: (Int) -> Unit,
    onNext: () -> Unit,
) {
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
    var hintsUsed by remember(levelIndex) { mutableIntStateOf(0) }
    var combo by remember(levelIndex) { mutableIntStateOf(0) }

    val earnedStars = when (hintsUsed) { 0 -> 3; 1 -> 2; 2 -> 1; else -> 0 }
    val complete = level.targets.all { it in foundWords }
    LaunchedEffect(complete) {
        if (complete) {
            onCompleted(earnedStars)
            if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_ACK, 180)
            if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val statusScale by animateFloatAsState(if (liveWord.isNotBlank()) 1.04f else 1f, tween(140), label = "statusScale")

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF203A6D))) { Text("←") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LEVEL ${levelIndex + 1}", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text(level.title, color = Color(0xFF9EB5E4), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("🪙 $coins", color = Color(0xFFFFD166), fontWeight = FontWeight.Black)
                Text("★".repeat(previousStars) + "☆".repeat(3 - previousStars), color = Color(0xFFFFD166), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        CrosswordBoard(level, foundWords, hintedCells)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${foundWords.size}/${level.targets.size} kelime", color = Color.White, fontSize = 12.sp)
            Text(if (combo >= 2) "🔥 Combo x$combo" else "Bu tur: " + "★".repeat(earnedStars) + "☆".repeat(3 - earnedStars), color = Color(0xFFFFD166), fontSize = 12.sp)
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().scale(statusScale).background(Color(0xFF12264B), RoundedCornerShape(16.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
            Text(
                if (liveWord.isNotBlank()) liveWord else status,
                color = when {
                    liveWord.isNotBlank() -> Color.White
                    status.startsWith("Bonus") -> Color(0xFFFFD166)
                    status.startsWith("Harika") || status.startsWith("Combo") -> Color(0xFF7EF0B8)
                    else -> Color(0xFFA8BCE1)
                },
                fontWeight = FontWeight.Bold,
                fontSize = if (liveWord.isNotBlank()) 22.sp else 13.sp,
            )
        }
        Spacer(Modifier.height(2.dp))
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
                        combo += 1
                        val comboReward = if (combo > 0 && combo % 3 == 0) 10 else 0
                        onCoinsChanged(coins + 5 + comboReward)
                        status = if (comboReward > 0) "Combo x$combo! +$comboReward 🪙" else "Harika! $submitted"
                        if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                        if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    submitted in foundWords -> status = "$submitted zaten bulundu"
                    submitted in level.bonusWords && submitted !in foundBonusWords -> {
                        foundBonusWords = foundBonusWords + submitted
                        combo += 1
                        val comboReward = if (combo > 0 && combo % 3 == 0) 10 else 0
                        onCoinsChanged(coins + 10 + comboReward)
                        status = if (comboReward > 0) "Combo x$combo! +${10 + comboReward} 🪙" else "Bonus kelime! +10 🪙"
                        if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                    }
                    submitted in foundBonusWords -> status = "$submitted bonus olarak bulundu"
                    submitted.isNotBlank() -> {
                        combo = 0
                        status = "$submitted bu levelde yok · combo sıfırlandı"
                        if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_NACK, 90)
                    }
                }
                selectedIndices = emptyList()
                liveWord = ""
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { wheelLetters = wheelLetters.shuffled(); selectedIndices = emptyList(); liveWord = ""; status = "Harfler karıştırıldı" }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF234782))) {
                Text("Karıştır", fontSize = 12.sp)
            }
            Button(
                enabled = coins >= 25 && !complete,
                onClick = {
                    val hidden = allCells(level).filter { it !in hintedCells && !cellRevealedByFoundWord(level, it, foundWords) }
                    hidden.randomOrNull()?.let {
                        onCoinsChanged(coins - 25)
                        hintedCells = hintedCells + it
                        hintsUsed += 1
                        combo = 0
                        status = "Harf ipucu açıldı · yıldız etkilenir"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7356B8)),
            ) { Text("Harf 25", fontSize = 12.sp) }
            Button(
                enabled = coins >= 60 && !complete,
                onClick = {
                    level.targets.firstOrNull { it !in foundWords }?.let {
                        onCoinsChanged(coins - 60)
                        foundWords = foundWords + it
                        hintsUsed += 2
                        combo = 0
                        status = "Kelime ipucu: $it"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9A5B32)),
            ) { Text("Kelime 60", fontSize = 12.sp) }
        }
        Spacer(Modifier.height(4.dp))
        AnimatedVisibility(complete) {
            Box(Modifier.fillMaxWidth().background(Color(0xFF123D36), RoundedCornerShape(22.dp)).padding(11.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LEVEL TAMAMLANDI", color = Color(0xFF7EF0B8), fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text("★".repeat(earnedStars) + "☆".repeat(3 - earnedStars), color = Color(0xFFFFD166), fontSize = 27.sp, fontWeight = FontWeight.Black)
                    Text(if (earnedStars > previousStars) "Yeni yıldız rekoru! · İlk bitiriş +25 🪙" else "En iyi: $previousStars★", color = Color.White, fontSize = 12.sp)
                    Spacer(Modifier.height(5.dp))
                    Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25A878))) { Text("Devam") }
                }
            }
        }
    }
}

private fun normalizeWord(word: String) = word.trim().uppercase(trLocale)

private fun cellsFor(placement: Placement): List<Pair<Cell, Char>> = placement.word.mapIndexed { index, char ->
    val row = placement.row + if (placement.horizontal) 0 else index
    val col = placement.col + if (placement.horizontal) index else 0
    Cell(row, col) to char
}

private fun allCells(level: Level): Set<Cell> = level.placements.flatMap(::cellsFor).map { it.first }.toSet()

private fun cellRevealedByFoundWord(level: Level, cell: Cell, foundWords: Set<String>): Boolean =
    level.placements.any { it.word in foundWords && cellsFor(it).any { pair -> pair.first == cell } }

@Composable
private fun CrosswordBoard(level: Level, foundWords: Set<String>, hintedCells: Set<Cell>) {
    val entries = remember(level) { level.placements.flatMap(::cellsFor) }
    val lettersByCell = remember(level) { entries.associate { it.first to it.second } }
    val rows = (lettersByCell.keys.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (lettersByCell.keys.maxOfOrNull { it.col } ?: 0) + 1
    val cellSize = when { cols >= 6 -> 38.dp; cols == 5 -> 42.dp; else -> 46.dp }

    Box(Modifier.fillMaxWidth().background(Color(0xFF0D1E3D), RoundedCornerShape(22.dp)).padding(9.dp), contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(cols) { col ->
                        val cell = Cell(row, col)
                        val letter = lettersByCell[cell]
                        if (letter == null) Spacer(Modifier.size(cellSize)) else {
                            val revealed = cell in hintedCells || cellRevealedByFoundWord(level, cell, foundWords)
                            Box(
                                Modifier.size(cellSize)
                                    .background(if (revealed) Color(0xFF2E8CFF) else Color(0xFF1B315A), RoundedCornerShape(9.dp))
                                    .border(1.dp, Color(0xFF3A5E98), RoundedCornerShape(9.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(if (revealed) letter.toString() else "", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
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
            Offset(center.x + (cos(angle) * radius).toFloat(), center.y + (sin(angle) * radius).toFloat())
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
        Modifier.size(242.dp).onSizeChanged { wheelSize = it }.pointerInput(letters, wheelSize) {
            var gestureSelection = emptyList<Int>()
            detectDragGestures(
                onDragStart = { point ->
                    val index = indexAt(point)
                    gestureSelection = if (index != null) listOf(index) else emptyList()
                    onSelectionChanged(gestureSelection)
                },
                onDragEnd = { onSelectionFinished(); gestureSelection = emptyList() },
                onDragCancel = { onSelectionFinished(); gestureSelection = emptyList() },
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
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Color(0xFF15254C), minOf(size.width, size.height) * 0.46f, center)
            val points = positions()
            selectedIndices.zipWithNext().forEach { (a, b) ->
                if (a in points.indices && b in points.indices) drawLine(Color(0xFF58A6FF), points[a], points[b], 18f, cap = StrokeCap.Round)
            }
            points.forEachIndexed { index, point ->
                val selected = index in selectedIndices
                drawCircle(if (selected) Color(0xFF2D8CFF) else Color.White, minOf(size.width, size.height) * 0.105f, point)
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = if (selected) android.graphics.Color.WHITE else android.graphics.Color.rgb(17, 31, 63)
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = minOf(size.width, size.height) * 0.10f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val metrics = paint.fontMetrics
                    canvas.nativeCanvas.drawText(letters[index].toString(), point.x, point.y - (metrics.ascent + metrics.descent) / 2f, paint)
                }
            }
        }
    }
}
