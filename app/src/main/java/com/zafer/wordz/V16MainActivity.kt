package com.zafer.wordz

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private object U16 {
    val Bg = Color(0xFFF5F7FA)
    val Surface = Color.White
    val Ink = Color(0xFF172033)
    val Muted = Color(0xFF697386)
    val Subtle = Color(0xFF98A2B3)
    val Border = Color(0xFFE7EAF0)
    val Blue = Color(0xFF356DF3)
    val BlueDark = Color(0xFF2456D6)
    val BlueSoft = Color(0xFFEEF3FF)
    val Green = Color(0xFF25A765)
    val GreenSoft = Color(0xFFECF8F1)
    val Gold = Color(0xFFF4A817)
    val GoldSoft = Color(0xFFFFF6E3)
    val Danger = Color(0xFFE94B5F)
}

private object Economy16 {
    const val WORD = 3
    const val BONUS = 6
    const val COMBO = 8
    const val LEVEL = 20
    const val CHAPTER = 120
    const val LETTER_HINT = 30
    const val WORD_HINT = 75
}

private const val P16 = "wordz_progress"
private const val K_UNLOCKED = "unlocked_level"
private const val K_COINS = "coins"
private const val K_COMPLETED = "completed_levels"
private const val K_STARS = "level_stars"
private const val K_SOUND = "sound_enabled"
private const val K_HAPTIC = "haptic_enabled"
private const val K_MOTION = "reduce_motion"
private const val K_CHESTS = "claimed_chapter_chests"
private const val K_WORDS = "stats_words"
private const val K_HINTS = "stats_hints"
private const val K_COMBO = "stats_best_combo"
private val tr16 = Locale("tr", "TR")

private data class C16(val row: Int, val col: Int) {
    fun encode() = "$row,$col"
    companion object {
        fun decode(raw: String): C16? {
            val p = raw.split(',')
            if (p.size != 2) return null
            return C16(p[0].toIntOrNull() ?: return null, p[1].toIntOrNull() ?: return null)
        }
    }
}
private data class Chapter16(val name: String, val subtitle: String, val start: Int, val end: Int, val accent: Color, val soft: Color)
private data class Stats16(val completed: Int, val stars: Int, val words: Int, val hints: Int, val combo: Int, val streak: Int)
private data class Task16(val id: String, val title: String, val subtitle: String, val goal: Int, val reward: Int, val current: (ChallengeProgress) -> Int)
private enum class Screen16 { HOME, CHAPTERS, MAP, GAME, DAILY, TASKS, PROFILE, SETTINGS }
private enum class Feedback16 { NORMAL, CORRECT, BONUS, WRONG }

class V16MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validateAllLevels()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = U16.Blue,
                    secondary = U16.Gold,
                    background = U16.Bg,
                    surface = U16.Surface,
                    onPrimary = Color.White,
                    onBackground = U16.Ink,
                    onSurface = U16.Ink,
                )
            ) { App16() }
        }
    }
}

private fun chapters16(): List<Chapter16> {
    val names = listOf("Başlangıç", "Sahil", "Orman", "Çöl", "Gece", "Uzak Diyarlar", "Zirve")
    val subs = listOf("Temelleri keşfet", "Ritmi yakala", "Yeni yollar aç", "Zorlu kelimelere geç", "Gece rotasını tamamla", "Uzak diyarlara ilerle", "Son zirve")
    val colors = listOf(Color(0xFF3976F6), Color(0xFF0E9FB5), Color(0xFF2A9D63), Color(0xFFD98A25), Color(0xFF6A62D8), Color(0xFF248A89), Color(0xFF5A57C8))
    val softs = listOf(Color(0xFFEEF4FF), Color(0xFFEAF8FA), Color(0xFFECF8F1), Color(0xFFFFF4E6), Color(0xFFF1F0FF), Color(0xFFECF8F7), Color(0xFFF0F0FF))
    return levels.indices.chunked(5).mapIndexed { i, chunk -> Chapter16(names.getOrElse(i) { "Bölüm ${i + 1}" }, subs.getOrElse(i) { "Yeni kelimeler" }, chunk.first(), chunk.last() + 1, colors[i % colors.size], softs[i % softs.size]) }
}

private fun decodeStars16(raw: Set<String>): Map<Int, Int> = raw.mapNotNull {
    val p = it.split(':'); val l = p.getOrNull(0)?.toIntOrNull(); val s = p.getOrNull(1)?.toIntOrNull()
    if (l != null && s != null && l in levels.indices) l to s.coerceIn(0, 3) else null
}.toMap()
private fun encodeStars16(map: Map<Int, Int>) = map.map { "${it.key}:${it.value}" }.toSet()

private fun dailyTasks16() = listOf(
    Task16("d_words", "8 kelime bul", "Bugün kelime zincirini büyüt", 8, 25, { it.words }),
    Task16("d_levels", "2 level tamamla", "İki bulmacayı bitir", 2, 35, { it.levels }),
    Task16("d_clean", "İpucusuz 1 level", "Bir leveli yardım almadan bitir", 1, 30, { it.noHintLevels }),
)
private fun weeklyTasks16() = listOf(
    Task16("w_words", "50 kelime bul", "Hafta boyunca 50 kelime", 50, 100, { it.words }),
    Task16("w_levels", "10 level tamamla", "Toplam 10 oyun bitir", 10, 120, { it.levels }),
    Task16("w_stars", "18 yıldız kazan", "Hafta boyunca 18 yıldız", 18, 120, { it.stars }),
)

@Composable
private fun App16() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(P16, Context.MODE_PRIVATE) }
    val gameStore = remember { GameProgressStore(prefs) }
    val meta = remember { MetaProgressStore(prefs) }
    val today = LocalDate.now()
    meta.ensurePeriods(today)

    val initialUnlocked = prefs.getInt(K_UNLOCKED, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(K_COMPLETED, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
    val migratedCompleted = if (savedCompleted.isEmpty() && initialUnlocked > 0) (0 until initialUnlocked).toSet() else savedCompleted
    val starSeed = decodeStars16(prefs.getStringSet(K_STARS, emptySet()).orEmpty()).toMutableMap().apply { migratedCompleted.forEach { if (it !in this) this[it] = 1 } }

    var unlocked by remember { mutableIntStateOf(initialUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(K_COINS, 100)) }
    var completed by remember { mutableStateOf(migratedCompleted) }
    var stars by remember { mutableStateOf(starSeed.toMap()) }
    var sound by remember { mutableStateOf(prefs.getBoolean(K_SOUND, true)) }
    var haptic by remember { mutableStateOf(prefs.getBoolean(K_HAPTIC, true)) }
    var motion by remember { mutableStateOf(prefs.getBoolean(K_MOTION, false)) }
    var chapterChests by remember { mutableStateOf(prefs.getStringSet(K_CHESTS, emptySet()).orEmpty()) }
    var totalWords by remember { mutableIntStateOf(prefs.getInt(K_WORDS, 0)) }
    var totalHints by remember { mutableIntStateOf(prefs.getInt(K_HINTS, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(K_COMBO, 0)) }
    var dailyProgress by remember { mutableStateOf(meta.daily(today)) }
    var weeklyProgress by remember { mutableStateOf(meta.weekly(today)) }
    var dailyClaims by remember { mutableStateOf(meta.dailyClaims(today)) }
    var weeklyClaims by remember { mutableStateOf(meta.weeklyClaims(today)) }
    var rewardState by remember { mutableStateOf(meta.rewardState(today)) }
    var screen by remember { mutableStateOf(Screen16.HOME) }
    var selectedChapter by remember { mutableIntStateOf((unlocked / 5).coerceIn(0, chapters16().lastIndex)) }
    var selectedLevel by remember { mutableIntStateOf(unlocked) }
    var gameSession by remember { mutableIntStateOf(0) }

    fun saveCore() {
        prefs.edit().putInt(K_UNLOCKED, unlocked).putInt(K_COINS, coins)
            .putStringSet(K_COMPLETED, completed.map(Int::toString).toSet()).putStringSet(K_STARS, encodeStars16(stars))
            .putBoolean(K_SOUND, sound).putBoolean(K_HAPTIC, haptic).putBoolean(K_MOTION, motion)
            .putStringSet(K_CHESTS, chapterChests).putInt(K_WORDS, totalWords).putInt(K_HINTS, totalHints).putInt(K_COMBO, bestCombo).apply()
    }
    fun openLevel(index: Int) { selectedLevel = index; selectedChapter = (index / 5).coerceIn(0, chapters16().lastIndex); gameSession++; screen = Screen16.GAME }
    fun refreshChallenges() { dailyProgress = meta.daily(today); weeklyProgress = meta.weekly(today); dailyClaims = meta.dailyClaims(today); weeklyClaims = meta.weeklyClaims(today); rewardState = meta.rewardState(today) }

    val totalStars = stars.values.sum()
    val stats = Stats16(completed.size, totalStars, totalWords, totalHints, bestCombo, rewardState.streak)
    val taskBadge = dailyTasks16().any { it.current(dailyProgress) >= it.goal && it.id !in dailyClaims } || weeklyTasks16().any { it.current(weeklyProgress) >= it.goal && it.id !in weeklyClaims }

    when (screen) {
        Screen16.HOME -> Home16(coins, totalStars, unlocked, completed.size, rewardState, taskBadge, { openLevel(unlocked) }, { screen = Screen16.CHAPTERS }, { screen = Screen16.DAILY }, { screen = Screen16.TASKS }, { screen = Screen16.PROFILE }, { screen = Screen16.SETTINGS })
        Screen16.CHAPTERS -> Chapters16(unlocked, stars, completed, { screen = Screen16.HOME }, { selectedChapter = it; screen = Screen16.MAP }, { screen = Screen16.HOME }, { screen = Screen16.TASKS }, { screen = Screen16.PROFILE })
        Screen16.MAP -> Map16(selectedChapter, unlocked, stars, completed, { screen = Screen16.CHAPTERS }) { openLevel(it) }
        Screen16.GAME -> key(gameSession) {
            Game16(
                levelIndex = selectedLevel,
                coins = coins,
                previousStars = stars[selectedLevel] ?: 0,
                completedBefore = selectedLevel in completed,
                soundEnabled = sound,
                hapticEnabled = haptic,
                reduceMotion = motion,
                progressStore = gameStore,
                onCoins = { coins = it; saveCore() },
                onWord = {
                    totalWords += 1; meta.recordWord(today); refreshChallenges(); saveCore()
                },
                onHint = { totalHints += 1; saveCore() },
                onCombo = { if (it > bestCombo) { bestCombo = it; saveCore() } },
                onComplete = { earned, hintsUsed ->
                    meta.recordLevel(earned, hintsUsed == 0, today); refreshChallenges()
                    val first = selectedLevel !in completed
                    completed = completed + selectedLevel
                    val old = stars[selectedLevel] ?: 0
                    if (earned > old) stars = stars + (selectedLevel to earned)
                    if (selectedLevel >= unlocked && unlocked < levels.lastIndex) unlocked = selectedLevel + 1
                    if (first) coins += Economy16.LEVEL
                    saveCore()
                },
                back = { screen = Screen16.MAP },
                replay = { gameStore.clear(selectedLevel); gameSession++ },
                map = { screen = Screen16.MAP },
                next = {
                    val ch = chapters16()[selectedChapter]
                    if (selectedLevel + 1 < ch.end && selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = Screen16.MAP
                },
                claimChapter = {
                    val ch = chapters16()[selectedChapter]; val k = selectedChapter.toString()
                    val allDone = (ch.start until ch.end).all { it in completed || it == selectedLevel }
                    if (allDone && k !in chapterChests) { chapterChests = chapterChests + k; coins += Economy16.CHAPTER; saveCore() }
                },
                chestClaimed = selectedChapter.toString() in chapterChests,
            )
        }
        Screen16.DAILY -> Daily16(rewardState, { screen = Screen16.HOME }) {
            val claimed = meta.claimDailyReward(today)
            if (claimed != null) { coins += claimed.rewardToday; rewardState = claimed; saveCore(); refreshChallenges() }
        }
        Screen16.TASKS -> Tasks16(dailyProgress, weeklyProgress, dailyClaims, weeklyClaims, { screen = Screen16.HOME }, { task, weekly ->
            val current = if (weekly) task.current(weeklyProgress) else task.current(dailyProgress)
            if (current >= task.goal) {
                val ok = if (weekly) meta.claimWeekly(task.id, today) else meta.claimDaily(task.id, today)
                if (ok) { coins += task.reward; saveCore(); refreshChallenges() }
            }
        }, { screen = Screen16.HOME }, { screen = Screen16.CHAPTERS }, { screen = Screen16.PROFILE })
        Screen16.PROFILE -> Profile16(coins, stats, { screen = Screen16.HOME }, { screen = Screen16.HOME }, { screen = Screen16.CHAPTERS }, { screen = Screen16.TASKS }, { screen = Screen16.SETTINGS })
        Screen16.SETTINGS -> Settings16(sound, haptic, motion, { screen = Screen16.PROFILE }, { sound = it; saveCore() }, { haptic = it; saveCore() }, { motion = it; saveCore() })
    }
}

@Composable private fun Surface16(content: @Composable () -> Unit) { Box(Modifier.fillMaxSize().background(U16.Bg).statusBarsPadding()) { content() } }
@Composable private fun Top16(title: String? = null, back: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) { IconButton16(Icons.Filled.ArrowBack, back); Spacer(Modifier.width(12.dp)) }
        if (title != null) Text(title, color = U16.Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}
@Composable private fun IconButton16(icon: ImageVector, click: () -> Unit, badge: Boolean = false) { Box(Modifier.size(42.dp).clip(CircleShape).background(U16.Surface).border(1.dp, U16.Border, CircleShape).clickable(onClick = click), contentAlignment = Alignment.Center) { Icon(icon, null, tint = U16.Ink, modifier = Modifier.size(20.dp)); if (badge) Box(Modifier.align(Alignment.TopEnd).size(9.dp).background(U16.Danger, CircleShape)) } }
@Composable private fun Chip16(icon: ImageVector, text: String, tint: Color) { Row(Modifier.clip(RoundedCornerShape(14.dp)).background(U16.Surface).border(1.dp, U16.Border, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text(text, color = U16.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp) } }
@Composable private fun Card16(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) { Column(modifier.shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x08000000), spotColor = Color(0x08000000)).clip(RoundedCornerShape(20.dp)).background(U16.Surface).border(1.dp, U16.Border, RoundedCornerShape(20.dp)).padding(18.dp), content = content) }
@Composable private fun Primary16(text: String, click: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) { Row(modifier.height(54.dp).clip(RoundedCornerShape(16.dp)).background(if (enabled) U16.Blue else Color(0xFFD5D9E2)).clickable(enabled = enabled, onClick = click), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { if (icon != null) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }; Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) } }
@Composable private fun Secondary16(text: String, click: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) { Row(modifier.height(46.dp).clip(RoundedCornerShape(14.dp)).background(U16.Surface).border(1.dp, U16.Border, RoundedCornerShape(14.dp)).clickable(onClick = click), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { if (icon != null) { Icon(icon, null, tint = U16.Blue, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)) }; Text(text, color = U16.Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) } }
@Composable private fun Bottom16(active: Screen16, home: () -> Unit, chapters: () -> Unit, tasks: () -> Unit, profile: () -> Unit) { Row(Modifier.fillMaxWidth().navigationBarsPadding().height(70.dp).background(U16.Surface).border(1.dp, U16.Border, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) { Nav16("Ana Sayfa", Icons.Filled.Home, active == Screen16.HOME, home); Nav16("Bölümler", Icons.Filled.Map, active == Screen16.CHAPTERS, chapters); Nav16("Görevler", Icons.Filled.TaskAlt, active == Screen16.TASKS, tasks); Nav16("Profil", Icons.Filled.Person, active == Screen16.PROFILE, profile) } }
@Composable private fun Nav16(label: String, icon: ImageVector, active: Boolean, click: () -> Unit) { Column(Modifier.clickable(onClick = click).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = if (active) U16.Blue else U16.Subtle, modifier = Modifier.size(20.dp)); Text(label, color = if (active) U16.Blue else U16.Subtle, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium) } }

@Composable private fun Home16(coins: Int, stars: Int, unlocked: Int, completed: Int, reward: DailyRewardState, taskBadge: Boolean, cont: () -> Unit, chapters: () -> Unit, daily: () -> Unit, tasks: () -> Unit, profile: () -> Unit, settings: () -> Unit) {
    val ch = chapters16()[(unlocked / 5).coerceIn(0, chapters16().lastIndex)]
    Surface16 { Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 88.dp)) {
            Row(Modifier.fillMaxWidth().height(66.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("WordZ", color = U16.Ink, fontSize = 28.sp, fontWeight = FontWeight.Black); Text("Kelime bulmaca", color = U16.Muted, fontSize = 12.sp) }; Chip16(Icons.Filled.AutoAwesome, coins.toString(), U16.Gold); Spacer(Modifier.width(8.dp)); Chip16(Icons.Filled.Star, stars.toString(), U16.Gold); Spacer(Modifier.width(8.dp)); IconButton16(Icons.Filled.Settings, settings) }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(ch.accent, ch.accent.copy(alpha = .82f)))).padding(22.dp)) { Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) { Column { Text("DEVAM ET", color = Color.White.copy(alpha = .72f), fontSize = 11.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Text(ch.name, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black); Text("Level ${unlocked + 1} · ${levels[unlocked].title}", color = Color.White.copy(alpha = .86f), fontSize = 13.sp) }; Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { LinearProgressIndicator(progress = { completed.toFloat() / levels.size.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = .24f)); Spacer(Modifier.height(6.dp)); Text("$completed / ${levels.size} tamamlandı", color = Color.White.copy(alpha = .75f), fontSize = 11.sp) }; Spacer(Modifier.width(16.dp)); Box(Modifier.size(50.dp).background(Color.White, CircleShape).clickable(onClick = cont), contentAlignment = Alignment.Center) { Icon(Icons.Filled.PlayArrow, null, tint = ch.accent, modifier = Modifier.size(27.dp)) } } } }
            Spacer(Modifier.height(18.dp)); Text("Bugün", color = U16.Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Quick16("7 günlük ödül", if (reward.claimedToday) "Bugün alındı" else "+${reward.rewardToday} hazır", Icons.Filled.CardGiftcard, U16.GoldSoft, U16.Gold, !reward.claimedToday, daily, Modifier.weight(1f)); Quick16("Görevler", if (taskBadge) "Ödül hazır" else "Günlük + haftalık", Icons.Filled.TaskAlt, U16.BlueSoft, U16.Blue, taskBadge, tasks, Modifier.weight(1f)) }
            Spacer(Modifier.height(12.dp)); Card16(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.LocalFireDepartment, null, tint = U16.Gold); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("${reward.streak} günlük seri", color = U16.Ink, fontWeight = FontWeight.Bold); Text("7. gün ödülü ${MetaProgressStore.DAILY_REWARDS.last()} jeton", color = U16.Muted, fontSize = 11.sp) }; Icon(Icons.Filled.ChevronRight, null, tint = U16.Subtle) } }
        }
        Box(Modifier.align(Alignment.BottomCenter)) { Bottom16(Screen16.HOME, {}, chapters, tasks, profile) }
    } }
}
@Composable private fun Quick16(title: String, sub: String, icon: ImageVector, bg: Color, tint: Color, badge: Boolean, click: () -> Unit, modifier: Modifier = Modifier) { Column(modifier.height(112.dp).clip(RoundedCornerShape(18.dp)).background(U16.Surface).border(1.dp, U16.Border, RoundedCornerShape(18.dp)).clickable(onClick = click).padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Box(Modifier.size(36.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }; if (badge) Box(Modifier.size(8.dp).background(U16.Danger, CircleShape)) }; Column { Text(title, color = U16.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(sub, color = U16.Muted, fontSize = 11.sp) } } }

@Composable private fun Chapters16(unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, open: (Int) -> Unit, home: () -> Unit, tasks: () -> Unit, profile: () -> Unit) { Surface16 { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 86.dp)) { Top16("Bölümler", back); Text("Her bölüm 5 level", color = U16.Muted, fontSize = 13.sp); Spacer(Modifier.height(14.dp)); Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) { chapters16().forEachIndexed { i, ch -> val range = ch.start until ch.end; val isOpen = unlocked >= ch.start; ChapterCard16(i, ch, range.count { it in completed }, range.count(), range.sumOf { stars[it] ?: 0 }, isOpen) { open(i) }; Spacer(Modifier.height(11.dp)) } } }; Box(Modifier.align(Alignment.BottomCenter)) { Bottom16(Screen16.CHAPTERS, home, {}, tasks, profile) } } } }
@Composable private fun ChapterCard16(index: Int, ch: Chapter16, done: Int, total: Int, stars: Int, unlocked: Boolean, click: () -> Unit) { Row(Modifier.fillMaxWidth().height(102.dp).clip(RoundedCornerShape(18.dp)).background(U16.Surface).border(1.dp, U16.Border, RoundedCornerShape(18.dp)).clickable(enabled = unlocked, onClick = click).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(if (unlocked) ch.soft else Color(0xFFF1F3F6), CircleShape), contentAlignment = Alignment.Center) { if (unlocked) Text("${index + 1}", color = ch.accent, fontWeight = FontWeight.Black, fontSize = 18.sp) else Icon(Icons.Filled.Lock, null, tint = U16.Subtle) }; Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(ch.name, color = if (unlocked) U16.Ink else U16.Subtle, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(ch.subtitle, color = U16.Muted, fontSize = 11.sp); Spacer(Modifier.height(7.dp)); LinearProgressIndicator(progress = { done.toFloat() / total.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = if (unlocked) ch.accent else Color(0xFFD5D9E2), trackColor = Color(0xFFF0F2F5)); Spacer(Modifier.height(4.dp)); Text(if (unlocked) "$done/$total · $stars/${total * 3} yıldız" else "Kilitli", color = U16.Muted, fontSize = 10.sp) }; Icon(Icons.Filled.ChevronRight, null, tint = U16.Subtle) } }

@Composable private fun Map16(chapterIndex: Int, unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, open: (Int) -> Unit) {
    val ch = chapters16()[chapterIndex]; val ids = (ch.start until ch.end).toList(); val nodes = listOf(.18f to .82f, .43f to .65f, .73f to .48f, .47f to .31f, .22f to .14f)
    Surface16 { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { Top16("${chapterIndex + 1}. ${ch.name}", back); Row { Text("${ids.count { it in completed }}/${ids.size} tamamlandı", color = U16.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("${ids.sumOf { stars[it] ?: 0 }} yıldız", color = U16.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp) }; Spacer(Modifier.height(14.dp)); BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(ch.soft, U16.Surface))).border(1.dp, U16.Border, RoundedCornerShape(26.dp))) { Canvas(Modifier.fillMaxSize()) { fun seg(a: Pair<Float,Float>, b: Pair<Float,Float>) = Path().apply { val x1=size.width*a.first;val y1=size.height*a.second;val x2=size.width*b.first;val y2=size.height*b.second;val m=(y1+y2)/2;moveTo(x1,y1);cubicTo(x1,m,x2,m,x2,y2) }; for(i in 0 until nodes.lastIndex) drawPath(seg(nodes[i],nodes[i+1]),Color(0xFFD9DEE7),style=Stroke(9f,cap=StrokeCap.Round)); for(i in 0 until nodes.lastIndex) if(ids[i] in completed) drawPath(seg(nodes[i],nodes[i+1]),ch.accent.copy(alpha=.75f),style=Stroke(9f,cap=StrokeCap.Round)) }; nodes.forEachIndexed { i,p -> val global=ids[i]; val s=64.dp; Column(Modifier.offset(maxWidth*p.first-s/2,maxHeight*p.second-s/2),horizontalAlignment=Alignment.CenterHorizontally){LevelNode16(i+1,global<=unlocked,global in completed,global==unlocked&&global !in completed,ch.accent){open(global)};Spacer(Modifier.height(4.dp));Row{repeat(3){x->Icon(if(x<(stars[global]?:0))Icons.Filled.Star else Icons.Filled.StarBorder,null,tint=if(x<(stars[global]?:0))U16.Gold else Color(0xFFCDD2DA),modifier=Modifier.size(12.dp))}}} } }; Spacer(Modifier.height(16.dp)) } }
}
@Composable private fun LevelNode16(number: Int, unlocked: Boolean, completed: Boolean, current: Boolean, accent: Color, click: () -> Unit) { val scale by animateFloatAsState(if(current)1.06f else 1f,tween(300),label="node"); val bg=when{!unlocked->Color(0xFFF1F3F6);completed->U16.Green;else->accent}; Box(Modifier.size(62.dp).scale(scale).shadow(if(current)10.dp else 3.dp,CircleShape).background(bg,CircleShape).border(if(current)3.dp else 1.dp,Color.White,CircleShape).clickable(enabled=unlocked,onClick=click),contentAlignment=Alignment.Center){when{!unlocked->Icon(Icons.Filled.Lock,null,tint=U16.Subtle);completed->Icon(Icons.Filled.CheckCircle,null,tint=Color.White,modifier=Modifier.size(26.dp));else->Text(number.toString(),color=Color.White,fontWeight=FontWeight.Black,fontSize=19.sp)}} }

@Composable private fun Game16(levelIndex: Int, coins: Int, previousStars: Int, completedBefore: Boolean, soundEnabled: Boolean, hapticEnabled: Boolean, reduceMotion: Boolean, progressStore: GameProgressStore, onCoins: (Int) -> Unit, onWord: () -> Unit, onHint: () -> Unit, onCombo: (Int) -> Unit, onComplete: (Int, Int) -> Unit, back: () -> Unit, replay: () -> Unit, map: () -> Unit, next: () -> Unit, claimChapter: () -> Unit, chestClaimed: Boolean) {
    val level = levels[levelIndex]; val haptics = LocalHapticFeedback.current; val tone=remember{ToneGenerator(AudioManager.STREAM_MUSIC,60)}; DisposableEffect(Unit){onDispose{tone.release()}}
    val saved = remember(levelIndex) { if (completedBefore) SavedLevelProgress() else progressStore.load(levelIndex) }
    var wheel by remember { mutableStateOf(level.letters) }; var found by remember { mutableStateOf(saved.foundWords.intersect(level.targets.toSet())) }; var bonus by remember { mutableStateOf(saved.bonusWords.intersect(level.bonusWords)) }; var hinted by remember { mutableStateOf(saved.hintedCells.mapNotNull(C16::decode).toSet()) }; var hintsUsed by remember { mutableIntStateOf(saved.hintsUsed) }; var selected by remember { mutableStateOf(emptyList<Int>()) }; var live by remember { mutableStateOf("") }; var status by remember { mutableStateOf(if(found.isNotEmpty())"Kaldığın yerden devam" else "Harfleri birleştir") }; var combo by remember { mutableIntStateOf(0) }; var handled by remember { mutableStateOf(false) }; var showResult by remember { mutableStateOf(false) }; var net by remember { mutableIntStateOf(0) }; var feedback by remember { mutableStateOf(Feedback16.NORMAL) }; val shake=remember{Animatable(0f)}
    val rewardEligible=!completedBefore
    fun persist(){ if(!completedBefore) progressStore.save(levelIndex,SavedLevelProgress(found,bonus,hinted.map(C16::encode).toSet(),hintsUsed)) }
    val done=level.targets.all{it in found}; val earned=when(hintsUsed){0->3;1->2;2->1;else->0}; val chapter=chapters16()[levelIndex/5]; val chapterEnd=levelIndex==chapter.end-1
    LaunchedEffect(done){if(done&&!handled){handled=true;persist();onComplete(earned,hintsUsed);if(soundEnabled)tone.startTone(ToneGenerator.TONE_PROP_ACK,160);if(hapticEnabled)haptics.performHapticFeedback(HapticFeedbackType.LongPress);if(!reduceMotion)delay(260);showResult=true}}
    val wordScale by animateFloatAsState(if(live.isNotBlank())1.025f else 1f,tween(if(reduceMotion)0 else 100),label="word")
    Surface16 { BoxWithConstraints(Modifier.fillMaxSize()) { val compact=maxHeight<760.dp; Column(Modifier.fillMaxSize().padding(horizontal=18.dp).offset(x=shake.value.dp)) { Row(Modifier.fillMaxWidth().height(58.dp),verticalAlignment=Alignment.CenterVertically){IconButton16(Icons.Filled.ArrowBack,back);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Level ${levelIndex+1}",color=U16.Ink,fontWeight=FontWeight.Bold,fontSize=16.sp);Text(level.title,color=U16.Muted,fontSize=11.sp)};Chip16(Icons.Filled.AutoAwesome,coins.toString(),U16.Gold)}; Spacer(Modifier.height(6.dp)); Board16(level,found,hinted,compact); Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth()){Text("${found.size}/${level.targets.size} kelime",color=U16.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));if(combo>=2)Text("Combo x$combo",color=U16.Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)}; Spacer(Modifier.height(7.dp)); Box(Modifier.fillMaxWidth().height(44.dp).scale(wordScale).clip(RoundedCornerShape(13.dp)).background(if(live.isNotBlank())U16.BlueSoft else U16.Surface).border(1.dp,U16.Border,RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){Text(if(live.isNotBlank())live else status,color=if(live.isNotBlank())U16.BlueDark else U16.Muted,fontWeight=if(live.isNotBlank())FontWeight.Bold else FontWeight.Medium,fontSize=if(live.isNotBlank())19.sp else 12.sp)}; Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Wheel16(wheel,selected,if(compact)214.dp else 244.dp,feedback,hapticEnabled,{selected=it;live=it.joinToString(""){x->wheel[x].toString()}},{
            val submitted=live.trim().uppercase(tr16); when { submitted in level.targets && submitted !in found->{found=found+submitted;combo++;onWord();onCombo(combo);val extra=if(rewardEligible&&combo%3==0)Economy16.COMBO else 0;val gain=if(rewardEligible)Economy16.WORD+extra else 0;if(gain>0){net+=gain;onCoins(coins+gain)};feedback=Feedback16.CORRECT;status=if(gain>0)"Doğru · +$gain" else "Doğru";persist();if(soundEnabled)tone.startTone(ToneGenerator.TONE_PROP_BEEP2,90)}; submitted in found->status="Zaten bulundu"; submitted in level.bonusWords && submitted !in bonus->{bonus=bonus+submitted;combo++;onWord();val gain=if(rewardEligible)Economy16.BONUS else 0;if(gain>0){net+=gain;onCoins(coins+gain)};feedback=Feedback16.BONUS;status=if(gain>0)"Bonus · +$gain" else "Bonus kelime";persist()}; submitted.isNotBlank()->{combo=0;feedback=Feedback16.WRONG;status="Bu levelde yok";if(!reduceMotion){shake.snapTo(-5f);shake.animateTo(5f,tween(55));shake.animateTo(0f,tween(55))};if(soundEnabled)tone.startTone(ToneGenerator.TONE_PROP_NACK,80)}};selected=emptyList();live="";if(feedback!=Feedback16.WRONG)feedback=Feedback16.NORMAL
        })}; Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Tool16(Icons.Filled.Shuffle,"Karıştır",true,Modifier.weight(1f)){wheel=wheel.shuffled()};Tool16(Icons.Filled.Lightbulb,"Harf ${Economy16.LETTER_HINT}",coins>=Economy16.LETTER_HINT&&!done,Modifier.weight(1f)){val hidden=allCells16(level).filter{it !in hinted&&!revealed16(level,it,found)};hidden.randomOrNull()?.let{onCoins(coins-Economy16.LETTER_HINT);net-=Economy16.LETTER_HINT;hinted=hinted+it;hintsUsed++;combo=0;onHint();persist();status="Bir harf açıldı"}};Tool16(Icons.Filled.Search,"Kelime ${Economy16.WORD_HINT}",coins>=Economy16.WORD_HINT&&!done,Modifier.weight(1f)){level.targets.firstOrNull{it !in found}?.let{onCoins(coins-Economy16.WORD_HINT);net-=Economy16.WORD_HINT;found=found+it;hintsUsed+=2;combo=0;onHint();persist();status="Kelime açıldı"}}} }; if(showResult)Result16(earned,previousStars,found.size,level.targets.size,bonus.size,hintsUsed,combo,net,chapterEnd,chestClaimed,completedBefore,reduceMotion,replay,map,next,claimChapter) } }
}
@Composable private fun Tool16(icon:ImageVector,label:String,enabled:Boolean,modifier:Modifier=Modifier,click:()->Unit){Column(modifier.height(58.dp).clip(RoundedCornerShape(15.dp)).background(U16.Surface).border(1.dp,U16.Border,RoundedCornerShape(15.dp)).clickable(enabled=enabled,onClick=click),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(icon,null,tint=if(enabled)U16.Blue else U16.Subtle,modifier=Modifier.size(20.dp));Text(label,color=if(enabled)U16.Ink else U16.Subtle,fontWeight=FontWeight.SemiBold,fontSize=9.sp)}}
private fun cells16(p:Placement)=p.word.mapIndexed{i,ch->C16(p.row+if(p.horizontal)0 else i,p.col+if(p.horizontal)i else 0) to ch}
private fun allCells16(level:Level)=level.placements.flatMap(::cells16).map{it.first}.toSet()
private fun revealed16(level:Level,cell:C16,found:Set<String>)=level.placements.any{it.word in found&&cells16(it).any{x->x.first==cell}}
@Composable private fun Board16(level:Level,found:Set<String>,hinted:Set<C16>,compact:Boolean){val validated=remember(level){validateLevel(level)};val map=remember(validated){validated.lettersByCell.mapKeys{C16(it.key.row,it.key.col)}};val rows=(map.keys.maxOfOrNull{it.row}?:0)+1;val cols=(map.keys.maxOfOrNull{it.col}?:0)+1;val cellSize=when{compact&&cols>=5->33.dp;cols>=6->35.dp;cols==5->39.dp;else->42.dp};Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(U16.Surface).border(1.dp,U16.Border,RoundedCornerShape(20.dp)).padding(if(compact)8.dp else 11.dp),contentAlignment=Alignment.Center){Column(verticalArrangement=Arrangement.spacedBy(4.dp)){repeat(rows){r->Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){repeat(cols){c->val cell=C16(r,c);val letter=map[cell];if(letter==null)Spacer(Modifier.size(cellSize))else{val reveal=cell in hinted||revealed16(level,cell,found);Box(Modifier.size(cellSize).clip(RoundedCornerShape(8.dp)).background(if(reveal)U16.BlueSoft else Color(0xFFF0F3F8)).border(1.dp,if(reveal)U16.Blue.copy(alpha=.45f) else Color(0xFFD8DEE8),RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center){if(reveal)Text(letter.toString(),color=U16.BlueDark,fontWeight=FontWeight.Black,fontSize=if(compact)15.sp else 17.sp)}}}}}}}}
@Composable private fun Wheel16(letters:List<Char>,selected:List<Int>,sizeDp:Dp,feedback:Feedback16,haptic:Boolean,onChanged:(List<Int>)->Unit,onFinished:suspend()->Unit){var measured by remember{mutableStateOf(IntSize.Zero)};var pointer by remember{mutableStateOf<Offset?>(null)};val haptics=LocalHapticFeedback.current;fun points():List<Offset>{if(measured==IntSize.Zero)return emptyList();val center=Offset(measured.width/2f,measured.height/2f);val radius=minOf(measured.width,measured.height)*.34f;return letters.indices.map{i->val a=-PI/2+2*PI*i/letters.size;Offset(center.x+(cos(a)*radius).toFloat(),center.y+(sin(a)*radius).toFloat())}};fun hit(p:Offset):Int?{val pts=points();val range=minOf(measured.width,measured.height)*.13f;return pts.indices.firstOrNull{i->hypot((p.x-pts[i].x).toDouble(),(p.y-pts[i].y).toDouble())<=range}}
    Box(Modifier.size(sizeDp).onSizeChanged{measured=it}.pointerInput(letters,measured){var gesture=emptyList<Int>();detectDragGestures(onDragStart={p->pointer=p;val i=hit(p);gesture=if(i!=null)listOf(i)else emptyList();onChanged(gesture);if(i!=null&&haptic)haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)},onDragEnd={pointer=null;onFinished();gesture=emptyList()},onDragCancel={pointer=null;gesture=emptyList()}){change,_->pointer=change.position;val i=hit(change.position);if(i!=null){when{gesture.size>=2&&i==gesture[gesture.lastIndex-1]->{gesture=gesture.dropLast(1);onChanged(gesture)};i !in gesture->{gesture=gesture+i;onChanged(gesture);if(haptic)haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)}}}}}){Canvas(Modifier.fillMaxSize()){val center=Offset(size.width/2,size.height/2);val radius=minOf(size.width,size.height)*.47f;val lineColor=when(feedback){Feedback16.CORRECT->U16.Green;Feedback16.BONUS->U16.Gold;Feedback16.WRONG->U16.Danger;else->U16.Blue};drawCircle(U16.Surface,radius,center);drawCircle(Color(0xFFE3E7EE),radius,center,style=Stroke(3f));val pts=points();selected.zipWithNext().forEach{(a,b)->if(a in pts.indices&&b in pts.indices)drawLine(lineColor,pts[a],pts[b],9f,cap=StrokeCap.Round)};if(selected.isNotEmpty()&&pointer!=null)drawLine(lineColor.copy(alpha=.6f),pts[selected.last()],pointer!!,7f,cap=StrokeCap.Round);pts.forEachIndexed{i,p->val sel=i in selected;val rr=minOf(size.width,size.height)*(if(sel).11f else .101f);drawCircle(if(sel)lineColor else Color(0xFFF8F9FB),rr,p);drawCircle(if(sel)lineColor else Color(0xFFD2D8E1),rr,p,style=Stroke(2.4f));drawIntoCanvas{canvas->val paint=android.graphics.Paint().apply{color=if(sel)android.graphics.Color.WHITE else android.graphics.Color.rgb(23,32,51);textAlign=android.graphics.Paint.Align.CENTER;textSize=minOf(size.width,size.height)*.104f;typeface=android.graphics.Typeface.DEFAULT_BOLD};val m=paint.fontMetrics;canvas.nativeCanvas.drawText(letters[i].toString(),p.x,p.y-(m.ascent+m.descent)/2,paint)}}}}}

@Composable private fun Result16(stars:Int,previous:Int,found:Int,target:Int,bonus:Int,hints:Int,combo:Int,net:Int,chapterEnd:Boolean,chestClaimed:Boolean,completedBefore:Boolean,reduce:Boolean,replay:()->Unit,map:()->Unit,next:()->Unit,claim:()->Unit){var shown by remember{mutableIntStateOf(if(reduce)stars else 0)};LaunchedEffect(stars,reduce){if(!reduce)repeat(stars){delay(220);shown=it+1}};Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.38f)).padding(horizontal=22.dp),contentAlignment=Alignment.Center){Card16(Modifier.fillMaxWidth()){Text(if(chapterEnd)"Bölüm tamamlandı" else "Level tamamlandı",color=U16.Ink,fontWeight=FontWeight.Black,fontSize=24.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center);Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){repeat(3){i->Icon(if(i<shown)Icons.Filled.Star else Icons.Filled.StarBorder,null,tint=if(i<shown)U16.Gold else Color(0xFFD0D5DD),modifier=Modifier.size(if(i==1)42.dp else 34.dp))}};Spacer(Modifier.height(8.dp));Text(if(stars>previous)"Yeni en iyi skor" else if(completedBefore)"Pratik tamamlandı" else "Yeni level açıldı",color=U16.Blue,fontWeight=FontWeight.SemiBold,fontSize=12.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center);Spacer(Modifier.height(14.dp));ResultRow16("Bulunan kelime","$found/$target");ResultRow16("Bonus kelime",bonus.toString());ResultRow16("Kullanılan ipucu",hints.toString());ResultRow16("Combo","x$combo");HorizontalDivider(color=U16.Border,modifier=Modifier.padding(vertical=10.dp));Row(Modifier.fillMaxWidth()){Text("Net jeton",color=U16.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));Text(if(net>=0)"+$net" else net.toString(),color=if(net>=0)U16.Green else U16.Danger,fontWeight=FontWeight.Black,fontSize=21.sp)};if(chapterEnd&&!chestClaimed){Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(U16.GoldSoft).clickable(onClick=claim).padding(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Filled.CardGiftcard,null,tint=U16.Gold);Spacer(Modifier.width(10.dp));Text("Bölüm sandığı +${Economy16.CHAPTER}",color=U16.Ink,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text("Al",color=U16.Gold,fontWeight=FontWeight.Black)}};Spacer(Modifier.height(16.dp));Primary16(if(chapterEnd)"Haritaya dön" else "Sonraki level",if(chapterEnd)map else next,Modifier.fillMaxWidth(),if(chapterEnd)Icons.Filled.Map else Icons.Filled.PlayArrow);Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Secondary16("Tekrar oyna",replay,Modifier.weight(1f),Icons.Filled.Refresh);if(!chapterEnd)Secondary16("Harita",map,Modifier.weight(1f),Icons.Filled.Map)}}}}
@Composable private fun ResultRow16(label:String,value:String){Row(Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(label,color=U16.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));Text(value,color=U16.Ink,fontWeight=FontWeight.Bold,fontSize=12.sp)}}

@Composable private fun Daily16(state:DailyRewardState,back:()->Unit,claim:()->Unit){Surface16{Column(Modifier.fillMaxSize().padding(horizontal=20.dp)){Top16("7 günlük ödül",back);Text("Her gün gel, ödül büyüsün",color=U16.Muted,fontSize=13.sp);Spacer(Modifier.height(20.dp));Card16(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(46.dp).background(U16.GoldSoft,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.LocalFireDepartment,null,tint=U16.Gold)};Spacer(Modifier.width(12.dp));Column{Text("${state.streak} günlük seri",color=U16.Ink,fontWeight=FontWeight.Bold,fontSize=18.sp);Text(if(state.claimedToday)"Bugünkü ödül alındı" else "Bugünkü ödül +${state.rewardToday}",color=U16.Muted,fontSize=12.sp)}}};Spacer(Modifier.height(18.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){MetaProgressStore.DAILY_REWARDS.forEachIndexed{i,reward->val active=i==state.cycleDay;val claimed=i<state.cycleDay||state.claimedToday&&i==state.cycleDay;Column(Modifier.weight(1f).height(94.dp).clip(RoundedCornerShape(13.dp)).background(if(active)U16.GoldSoft else U16.Surface).border(1.dp,if(active)U16.Gold.copy(alpha=.45f) else U16.Border,RoundedCornerShape(13.dp)),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("${i+1}. gün",color=if(active)U16.Gold else U16.Muted,fontSize=9.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Icon(if(claimed)Icons.Filled.CheckCircle else Icons.Filled.CardGiftcard,null,tint=if(claimed)U16.Green else U16.Gold,modifier=Modifier.size(20.dp));Spacer(Modifier.height(6.dp));Text("+$reward",color=U16.Ink,fontSize=10.sp,fontWeight=FontWeight.Bold)}}};Spacer(Modifier.weight(1f));Primary16(if(state.claimedToday)"Bugünkü ödül alındı" else "+${state.rewardToday} jeton al",claim,Modifier.fillMaxWidth().padding(bottom=24.dp),Icons.Filled.CardGiftcard,!state.claimedToday)}}}

@Composable private fun Tasks16(daily:ChallengeProgress,weekly:ChallengeProgress,dClaims:Set<String>,wClaims:Set<String>,back:()->Unit,claim:(Task16,Boolean)->Unit,home:()->Unit,chapters:()->Unit,profile:()->Unit){Surface16{Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(horizontal=20.dp).padding(bottom=86.dp)){Top16("Görevler",back);Text("Günlük görevler her gün, haftalık görevler pazartesi yenilenir",color=U16.Muted,fontSize=12.sp);Spacer(Modifier.height(14.dp));Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())){Text("Günlük",color=U16.Ink,fontWeight=FontWeight.Black,fontSize=18.sp);Spacer(Modifier.height(8.dp));dailyTasks16().forEach{TaskCard16(it,daily,it.id in dClaims,false,claim);Spacer(Modifier.height(10.dp))};Spacer(Modifier.height(8.dp));Text("Haftalık",color=U16.Ink,fontWeight=FontWeight.Black,fontSize=18.sp);Spacer(Modifier.height(8.dp));weeklyTasks16().forEach{TaskCard16(it,weekly,it.id in wClaims,true,claim);Spacer(Modifier.height(10.dp))}}};Box(Modifier.align(Alignment.BottomCenter)){Bottom16(Screen16.TASKS,home,chapters,{},profile)}}}}
@Composable private fun TaskCard16(task:Task16,progress:ChallengeProgress,claimed:Boolean,weekly:Boolean,claim:(Task16,Boolean)->Unit){val current=task.current(progress).coerceAtMost(task.goal);val done=current>=task.goal;Card16(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).background(if(done)U16.GreenSoft else U16.BlueSoft,CircleShape),contentAlignment=Alignment.Center){Icon(if(done)Icons.Filled.CheckCircle else if(weekly)Icons.Filled.CalendarToday else Icons.Filled.TaskAlt,null,tint=if(done)U16.Green else U16.Blue,modifier=Modifier.size(19.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(task.title,color=U16.Ink,fontWeight=FontWeight.Bold);Text(task.subtitle,color=U16.Muted,fontSize=10.sp)};Text("+${task.reward}",color=U16.Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)};Spacer(Modifier.height(11.dp));LinearProgressIndicator(progress={current.toFloat()/task.goal.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),color=if(done)U16.Green else U16.Blue,trackColor=Color(0xFFF0F2F5));Spacer(Modifier.height(7.dp));Row(Modifier.fillMaxWidth()){Text("$current/${task.goal}",color=U16.Muted,fontSize=10.sp,modifier=Modifier.weight(1f));Text(if(claimed)"Alındı" else if(done)"Ödülü al" else "Devam ediyor",color=if(done&&!claimed)U16.Blue else U16.Muted,fontWeight=FontWeight.Bold,fontSize=11.sp,modifier=Modifier.clickable(enabled=done&&!claimed){claim(task,weekly)})}}}

@Composable private fun Profile16(coins:Int,stats:Stats16,back:()->Unit,home:()->Unit,chapters:()->Unit,tasks:()->Unit,settings:()->Unit){val level=1+stats.stars/15;val xp=(stats.stars%15)/15f;Surface16{Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(horizontal=20.dp).padding(bottom=86.dp)){Top16("Profil",back){IconButton16(Icons.Filled.Settings,settings)};Spacer(Modifier.height(12.dp));Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(68.dp).background(U16.BlueSoft,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.Person,null,tint=U16.Blue,modifier=Modifier.size(32.dp))};Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text("Oyuncu",color=U16.Ink,fontWeight=FontWeight.Black,fontSize=21.sp);Text("Seviye $level",color=U16.Muted,fontSize=12.sp);Spacer(Modifier.height(7.dp));LinearProgressIndicator(progress={xp},modifier=Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),color=U16.Blue,trackColor=Color(0xFFF0F2F5))}};Spacer(Modifier.height(20.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Stat16("Yıldız",stats.stars.toString(),Icons.Filled.Star,U16.Gold,Modifier.weight(1f));Stat16("Level",stats.completed.toString(),Icons.Filled.CheckCircle,U16.Green,Modifier.weight(1f));Stat16("Jeton",coins.toString(),Icons.Filled.AutoAwesome,U16.Blue,Modifier.weight(1f))};Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Stat16("Kelime",stats.words.toString(),Icons.Filled.TaskAlt,U16.Blue,Modifier.weight(1f));Stat16("İpucu",stats.hints.toString(),Icons.Filled.Lightbulb,U16.Gold,Modifier.weight(1f));Stat16("Combo","x${stats.combo}",Icons.Filled.LocalFireDepartment,U16.Danger,Modifier.weight(1f))};Spacer(Modifier.height(14.dp));Card16(Modifier.fillMaxWidth()){Text("Ekonomi",color=U16.Ink,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text("Kelime +${Economy16.WORD} · Bonus +${Economy16.BONUS} · Level +${Economy16.LEVEL}",color=U16.Muted,fontSize=11.sp);Text("Harf ipucu ${Economy16.LETTER_HINT} · Kelime ipucu ${Economy16.WORD_HINT}",color=U16.Muted,fontSize=11.sp)}};Box(Modifier.align(Alignment.BottomCenter)){Bottom16(Screen16.PROFILE,home,chapters,tasks,{})}}}}
@Composable private fun Stat16(label:String,value:String,icon:ImageVector,tint:Color,modifier:Modifier=Modifier){Column(modifier.height(104.dp).clip(RoundedCornerShape(17.dp)).background(U16.Surface).border(1.dp,U16.Border,RoundedCornerShape(17.dp)).padding(13.dp),verticalArrangement=Arrangement.SpaceBetween){Icon(icon,null,tint=tint,modifier=Modifier.size(20.dp));Column{Text(value,color=U16.Ink,fontWeight=FontWeight.Black,fontSize=19.sp);Text(label,color=U16.Muted,fontSize=10.sp)}}}
@Composable private fun Settings16(sound:Boolean,haptic:Boolean,motion:Boolean,back:()->Unit,onSound:(Boolean)->Unit,onHaptic:(Boolean)->Unit,onMotion:(Boolean)->Unit){Surface16{Column(Modifier.fillMaxSize().padding(horizontal=20.dp)){Top16("Ayarlar",back);Spacer(Modifier.height(14.dp));Setting16(Icons.Filled.VolumeUp,"Ses efektleri",sound,onSound);Setting16(Icons.Filled.Vibration,"Titreşim",haptic,onHaptic);Setting16(Icons.Filled.Refresh,"Animasyonları azalt",motion,onMotion);Spacer(Modifier.height(18.dp));Text("Sürüm ${BuildConfig.VERSION_NAME}",color=U16.Subtle,fontSize=11.sp)}}}
@Composable private fun Setting16(icon:ImageVector,title:String,value:Boolean,change:(Boolean)->Unit){Row(Modifier.fillMaxWidth().height(62.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(38.dp).background(U16.BlueSoft,CircleShape),contentAlignment=Alignment.Center){Icon(icon,null,tint=U16.Blue,modifier=Modifier.size(18.dp))};Spacer(Modifier.width(12.dp));Text(title,color=U16.Ink,fontWeight=FontWeight.Medium,modifier=Modifier.weight(1f));Switch(checked=value,onCheckedChange=change)};HorizontalDivider(color=U16.Border)}
