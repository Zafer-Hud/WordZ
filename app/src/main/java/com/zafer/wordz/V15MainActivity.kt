package com.zafer.wordz

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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

private object VUi {
    val Background = Color(0xFFF5F7FA)
    val Surface = Color.White
    val Ink = Color(0xFF172033)
    val Muted = Color(0xFF697386)
    val Subtle = Color(0xFF98A2B3)
    val Border = Color(0xFFE5E9F0)
    val Primary = Color(0xFF356DF3)
    val PrimaryDark = Color(0xFF2456D6)
    val PrimarySoft = Color(0xFFEEF3FF)
    val Success = Color(0xFF25A765)
    val SuccessSoft = Color(0xFFECF8F1)
    val Gold = Color(0xFFF4A817)
    val GoldSoft = Color(0xFFFFF6E3)
    val Danger = Color(0xFFE94B5F)
    val DangerSoft = Color(0xFFFFEEF1)
}

private const val VPREFS = "wordz_progress"
private const val V_UNLOCKED = "unlocked_level"
private const val V_COINS = "coins"
private const val V_COMPLETED = "completed_levels"
private const val V_STARS = "level_stars"
private const val V_DAILY_DATE = "daily_reward_date"
private const val V_STREAK = "daily_streak"
private const val V_SOUND = "sound_enabled"
private const val V_HAPTIC = "haptic_enabled"
private const val V_MOTION = "reduce_motion"
private const val V_CHAPTER_CHESTS = "claimed_chapter_chests"
private const val V_TASKS = "claimed_achievements"
private const val V_WORDS = "stats_words"
private const val V_HINTS = "stats_hints"
private const val V_COMBO = "stats_best_combo"
private val vTr = Locale("tr", "TR")

private fun vKey(level: Int, suffix: String) = "level_${level}_$suffix"
private const val P_FOUND = "found"
private const val P_BONUS = "bonus"
private const val P_HINT_CELLS = "hint_cells"
private const val P_HINT_COUNT = "hint_count"
private const val P_CLAIMED = "claimed_words"
private const val P_CLAIMED_BONUS = "claimed_bonus"

private data class VCell(val row: Int, val col: Int)
private data class VChapter(val name: String, val subtitle: String, val start: Int, val endExclusive: Int, val accent: Color, val soft: Color)
private data class VStats(val completed: Int, val stars: Int, val words: Int, val hints: Int, val combo: Int, val streak: Int)
private data class VTask(val id: String, val title: String, val reward: Int, val current: (VStats) -> Int, val goal: Int)
private data class VValidation(val cells: Map<VCell, Char>, val errors: List<String>)
private enum class VScreen { HOME, CHAPTERS, MAP, GAME, DAILY, TASKS, PROFILE, SETTINGS }
private enum class WheelFeedback { NEUTRAL, CORRECT, BONUS, WRONG }

class V15MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = VUi.Primary,
                    secondary = VUi.Gold,
                    background = VUi.Background,
                    surface = VUi.Surface,
                    onPrimary = Color.White,
                    onBackground = VUi.Ink,
                    onSurface = VUi.Ink,
                )
            ) { V15App() }
        }
    }
}

private fun vChapters(): List<VChapter> {
    val names = listOf("Başlangıç", "Sahil", "Orman", "Çöl", "Gece", "Uzak Diyarlar", "Zirve")
    val subs = listOf("Temelleri keşfet", "Ritmi yakala", "Yeni yollar aç", "Zorlu kelimelere geç", "Gece rotasını tamamla", "Uzak diyarlara ilerle", "Son zirve")
    val accents = listOf(Color(0xFF3976F6), Color(0xFF0E9FB5), Color(0xFF2A9D63), Color(0xFFD98A25), Color(0xFF6A62D8), Color(0xFF248A89), Color(0xFF5A57C8))
    val softs = listOf(Color(0xFFEEF4FF), Color(0xFFEAF8FA), Color(0xFFECF8F1), Color(0xFFFFF4E6), Color(0xFFF1F0FF), Color(0xFFECF8F7), Color(0xFFF0F0FF))
    return levels.indices.chunked(5).mapIndexed { i, chunk ->
        VChapter(names.getOrElse(i) { "Bölüm ${i + 1}" }, subs.getOrElse(i) { "Yeni kelimeler" }, chunk.first(), chunk.last() + 1, accents[i % accents.size], softs[i % softs.size])
    }
}

private fun vDecodeStars(raw: Set<String>): Map<Int, Int> = raw.mapNotNull {
    val p = it.split(':')
    val l = p.getOrNull(0)?.toIntOrNull()
    val s = p.getOrNull(1)?.toIntOrNull()
    if (l != null && s != null && l in levels.indices) l to s.coerceIn(0, 3) else null
}.toMap()

private fun vEncodeStars(stars: Map<Int, Int>) = stars.map { "${it.key}:${it.value}" }.toSet()

private fun vTasks() = listOf(
    VTask("first", "İlk leveli tamamla", 30, { it.completed }, 1),
    VTask("stars15", "15 yıldız topla", 75, { it.stars }, 15),
    VTask("words50", "50 kelime bul", 100, { it.words }, 50),
    VTask("combo5", "Combo x5 yap", 80, { it.combo }, 5),
    VTask("streak3", "3 günlük seri yap", 100, { it.streak }, 3),
)

@Composable
private fun V15App() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(VPREFS, Context.MODE_PRIVATE) }
    val initialUnlocked = prefs.getInt(V_UNLOCKED, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(V_COMPLETED, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
    val migratedCompleted = if (savedCompleted.isEmpty() && initialUnlocked > 0) (0 until initialUnlocked).toSet() else savedCompleted
    val starSeed = vDecodeStars(prefs.getStringSet(V_STARS, emptySet()).orEmpty()).toMutableMap().apply { migratedCompleted.forEach { if (it !in this) this[it] = 1 } }

    var unlocked by remember { mutableIntStateOf(initialUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(V_COINS, 100)) }
    var completed by remember { mutableStateOf(migratedCompleted) }
    var stars by remember { mutableStateOf(starSeed.toMap()) }
    var dailyDate by remember { mutableStateOf(prefs.getString(V_DAILY_DATE, "").orEmpty()) }
    var streak by remember { mutableIntStateOf(prefs.getInt(V_STREAK, 0)) }
    var sound by remember { mutableStateOf(prefs.getBoolean(V_SOUND, true)) }
    var haptic by remember { mutableStateOf(prefs.getBoolean(V_HAPTIC, true)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean(V_MOTION, false)) }
    var chapterChests by remember { mutableStateOf(prefs.getStringSet(V_CHAPTER_CHESTS, emptySet()).orEmpty()) }
    var claimedTasks by remember { mutableStateOf(prefs.getStringSet(V_TASKS, emptySet()).orEmpty()) }
    var words by remember { mutableIntStateOf(prefs.getInt(V_WORDS, 0)) }
    var hints by remember { mutableIntStateOf(prefs.getInt(V_HINTS, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(V_COMBO, 0)) }
    var screen by remember { mutableStateOf(VScreen.HOME) }
    var selectedChapter by remember { mutableIntStateOf((unlocked / 5).coerceIn(0, vChapters().lastIndex)) }
    var selectedLevel by remember { mutableIntStateOf(unlocked) }
    var gameSession by remember { mutableIntStateOf(0) }

    val today = LocalDate.now()
    val totalStars = stars.values.sum()
    val stats = VStats(completed.size, totalStars, words, hints, bestCombo, streak)
    val dailyAvailable = dailyDate != today.toString()

    fun save(
        u: Int = unlocked, c: Int = coins, comp: Set<Int> = completed, st: Map<Int, Int> = stars,
        date: String = dailyDate, str: Int = streak, snd: Boolean = sound, hap: Boolean = haptic,
        motion: Boolean = reduceMotion, chests: Set<String> = chapterChests, tasks: Set<String> = claimedTasks,
        w: Int = words, h: Int = hints, combo: Int = bestCombo,
    ) {
        prefs.edit().putInt(V_UNLOCKED, u).putInt(V_COINS, c)
            .putStringSet(V_COMPLETED, comp.map(Int::toString).toSet()).putStringSet(V_STARS, vEncodeStars(st))
            .putString(V_DAILY_DATE, date).putInt(V_STREAK, str).putBoolean(V_SOUND, snd).putBoolean(V_HAPTIC, hap)
            .putBoolean(V_MOTION, motion).putStringSet(V_CHAPTER_CHESTS, chests).putStringSet(V_TASKS, tasks)
            .putInt(V_WORDS, w).putInt(V_HINTS, h).putInt(V_COMBO, combo).apply()
    }

    fun openLevel(index: Int) {
        selectedLevel = index
        selectedChapter = (index / 5).coerceIn(0, vChapters().lastIndex)
        gameSession++
        screen = VScreen.GAME
    }

    when (screen) {
        VScreen.HOME -> VHome(coins, totalStars, unlocked, completed.size, streak, dailyAvailable, vTasks().any { it.current(stats) >= it.goal && it.id !in claimedTasks }, { openLevel(unlocked) }, { screen = VScreen.CHAPTERS }, { screen = VScreen.DAILY }, { screen = VScreen.TASKS }, { screen = VScreen.PROFILE }, { screen = VScreen.SETTINGS })
        VScreen.CHAPTERS -> VChapters(unlocked, stars, completed, { screen = VScreen.HOME }, { selectedChapter = it; screen = VScreen.MAP }, { screen = VScreen.HOME }, { screen = VScreen.TASKS }, { screen = VScreen.PROFILE })
        VScreen.MAP -> VMap(selectedChapter, unlocked, stars, completed, { screen = VScreen.CHAPTERS }, { openLevel(it) })
        VScreen.GAME -> key(gameSession) {
            VGame(
                levelIndex = selectedLevel,
                parentCoins = coins,
                previousStars = stars[selectedLevel] ?: 0,
                firstCompletion = selectedLevel !in completed,
                soundEnabled = sound,
                hapticEnabled = haptic,
                reduceMotion = reduceMotion,
                onCoinsChanged = { value -> coins = value; save(c = value) },
                onWordFound = { val v = words + 1; words = v; save(w = v) },
                onHintUsed = { val v = hints + 1; hints = v; save(h = v) },
                onCombo = { v -> if (v > bestCombo) { bestCombo = v; save(combo = v) } },
                onCompleted = { earned ->
                    val first = selectedLevel !in completed
                    val newCompleted = completed + selectedLevel
                    val oldStars = stars[selectedLevel] ?: 0
                    val newStars = if (earned > oldStars) stars + (selectedLevel to earned) else stars
                    val newUnlocked = if (selectedLevel >= unlocked && unlocked < levels.lastIndex) selectedLevel + 1 else unlocked
                    val newCoins = coins + if (first) 25 else 0
                    completed = newCompleted; stars = newStars; unlocked = newUnlocked; coins = newCoins
                    save(u = newUnlocked, c = newCoins, comp = newCompleted, st = newStars)
                },
                onBack = { screen = VScreen.MAP },
                onReplay = { clearLevelSession(prefs, selectedLevel); gameSession++ },
                onMap = { screen = VScreen.MAP },
                onNext = {
                    val chapter = vChapters()[selectedChapter]
                    if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = VScreen.MAP
                },
                onClaimChapter = {
                    val chapter = vChapters()[selectedChapter]
                    val key = selectedChapter.toString()
                    val done = (chapter.start until chapter.endExclusive).all { it in completed || it == selectedLevel }
                    if (done && key !in chapterChests) {
                        val nc = coins + 150
                        val ns = chapterChests + key
                        coins = nc; chapterChests = ns
                        save(c = nc, chests = ns)
                    }
                },
                chapterChestClaimed = selectedChapter.toString() in chapterChests,
            )
        }
        VScreen.DAILY -> VDaily(streak, dailyAvailable, { screen = VScreen.HOME }) {
            if (dailyAvailable) {
                val ns = if (dailyDate == today.minusDays(1).toString()) streak + 1 else 1
                val nc = coins + 40
                val nd = today.toString()
                streak = ns; coins = nc; dailyDate = nd
                save(c = nc, date = nd, str = ns)
            }
        }
        VScreen.TASKS -> VTasks(stats, claimedTasks, { screen = VScreen.HOME }, { task ->
            if (task.current(stats) >= task.goal && task.id !in claimedTasks) {
                val nt = claimedTasks + task.id
                val nc = coins + task.reward
                claimedTasks = nt; coins = nc
                save(c = nc, tasks = nt)
            }
        }, { screen = VScreen.HOME }, { screen = VScreen.CHAPTERS }, { screen = VScreen.PROFILE })
        VScreen.PROFILE -> VProfile(coins, stats, { screen = VScreen.HOME }, { screen = VScreen.HOME }, { screen = VScreen.CHAPTERS }, { screen = VScreen.TASKS }, { screen = VScreen.SETTINGS })
        VScreen.SETTINGS -> VSettings(sound, haptic, reduceMotion, { screen = VScreen.PROFILE }, { sound = it; save(snd = it) }, { haptic = it; save(hap = it) }, { reduceMotion = it; save(motion = it) })
    }
}

private fun clearLevelSession(prefs: android.content.SharedPreferences, level: Int) {
    prefs.edit()
        .remove(vKey(level, P_FOUND))
        .remove(vKey(level, P_BONUS))
        .remove(vKey(level, P_HINT_CELLS))
        .remove(vKey(level, P_HINT_COUNT))
        .apply()
}

@Composable private fun VSurface(content: @Composable () -> Unit) { Box(Modifier.fillMaxSize().background(VUi.Background).statusBarsPadding()) { content() } }
@Composable private fun VCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) { Column(modifier.shadow(7.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x10000000), spotColor = Color(0x0D000000)).clip(RoundedCornerShape(20.dp)).background(VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(20.dp)).padding(18.dp), content = content) }
@Composable private fun VIconButton(icon: ImageVector, click: () -> Unit, badge: Boolean = false) { Box(Modifier.size(42.dp).clip(CircleShape).background(VUi.Surface).border(1.dp, VUi.Border, CircleShape).clickable(onClick = click), contentAlignment = Alignment.Center) { Icon(icon, null, tint = VUi.Ink, modifier = Modifier.size(20.dp)); if (badge) Box(Modifier.align(Alignment.TopEnd).size(9.dp).background(VUi.Danger, CircleShape)) } }
@Composable private fun VChip(icon: ImageVector, value: String, tint: Color) { Row(Modifier.clip(RoundedCornerShape(14.dp)).background(VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) { Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp)); Text(value, color = VUi.Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }
@Composable private fun VPrimary(text: String, click: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) { Row(modifier.height(54.dp).clip(RoundedCornerShape(16.dp)).background(if (enabled) VUi.Primary else Color(0xFFD2D7DF)).clickable(enabled = enabled, onClick = click).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { if (icon != null) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }; Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) } }
@Composable private fun VTop(title: String, back: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) { Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) { if (back != null) { VIconButton(Icons.Filled.ArrowBack, back); Spacer(Modifier.width(12.dp)) }; Text(title, color = VUi.Ink, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.weight(1f)); trailing?.invoke() } }

@Composable private fun VBottom(active: VScreen, home: () -> Unit, chapters: () -> Unit, tasks: () -> Unit, profile: () -> Unit) {
    Row(Modifier.fillMaxWidth().navigationBarsPadding().height(72.dp).background(VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
        VNav("Ana", Icons.Filled.Home, active == VScreen.HOME, home); VNav("Bölümler", Icons.Filled.Map, active == VScreen.CHAPTERS, chapters); VNav("Görevler", Icons.Filled.TaskAlt, active == VScreen.TASKS, tasks); VNav("Profil", Icons.Filled.Person, active == VScreen.PROFILE, profile)
    }
}
@Composable private fun VNav(label: String, icon: ImageVector, active: Boolean, click: () -> Unit) { Column(Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = click).padding(horizontal = 12.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = if (active) VUi.Primary else VUi.Subtle, modifier = Modifier.size(21.dp)); Text(label, color = if (active) VUi.Primary else VUi.Subtle, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium) } }

@Composable private fun VHome(coins: Int, stars: Int, unlocked: Int, completed: Int, streak: Int, daily: Boolean, taskBadge: Boolean, play: () -> Unit, chapters: () -> Unit, dailyClick: () -> Unit, tasks: () -> Unit, profile: () -> Unit, settings: () -> Unit) {
    val chapter = vChapters()[(unlocked / 5).coerceIn(0, vChapters().lastIndex)]
    VSurface { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 92.dp)) {
        Row(Modifier.fillMaxWidth().height(66.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("WordZ", color = VUi.Ink, fontWeight = FontWeight.Black, fontSize = 27.sp); Text("Kelime bulmaca", color = VUi.Muted, fontSize = 11.sp) }; VChip(Icons.Filled.AutoAwesome, coins.toString(), VUi.Gold); Spacer(Modifier.width(8.dp)); VChip(Icons.Filled.Star, stars.toString(), VUi.Gold); Spacer(Modifier.width(8.dp)); VIconButton(Icons.Filled.Settings, settings) }
        Spacer(Modifier.height(14.dp)); VCard(Modifier.fillMaxWidth()) { Text("DEVAM ET", color = VUi.Primary, fontWeight = FontWeight.Bold, fontSize = 10.sp); Spacer(Modifier.height(8.dp)); Text(chapter.name, color = VUi.Ink, fontWeight = FontWeight.Black, fontSize = 26.sp); Text("Level ${unlocked + 1} · ${levels[unlocked].title}", color = VUi.Muted, fontSize = 12.sp); Spacer(Modifier.height(18.dp)); LinearProgressIndicator(progress = { completed.toFloat() / levels.size.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = chapter.accent, trackColor = Color(0xFFF0F2F6)); Spacer(Modifier.height(12.dp)); VPrimary("Oyuna devam et", play, Modifier.fillMaxWidth(), Icons.Filled.PlayArrow) }
        Spacer(Modifier.height(18.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { VQuick("Günlük ödül", if (daily) "+40 hazır" else "Bugün alındı", Icons.Filled.CardGiftcard, VUi.Gold, daily, dailyClick, Modifier.weight(1f)); VQuick("Seri", "$streak gün", Icons.Filled.LocalFireDepartment, VUi.Danger, false, dailyClick, Modifier.weight(1f)) }
        Spacer(Modifier.height(12.dp)); VQuick("Görevler", if (taskBadge) "Ödül bekliyor" else "İlerlemeni takip et", Icons.Filled.TaskAlt, VUi.Primary, taskBadge, tasks, Modifier.fillMaxWidth()); Spacer(Modifier.height(24.dp))
    }; Box(Modifier.align(Alignment.BottomCenter)) { VBottom(VScreen.HOME, {}, chapters, tasks, profile) } } }
}
@Composable private fun VQuick(title: String, subtitle: String, icon: ImageVector, tint: Color, badge: Boolean, click: () -> Unit, modifier: Modifier) { Row(modifier.height(92.dp).clip(RoundedCornerShape(18.dp)).background(VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(18.dp)).clickable(onClick = click).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).background(tint.copy(alpha = .1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = VUi.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(subtitle, color = VUi.Muted, fontSize = 10.sp) }; if (badge) Box(Modifier.size(8.dp).background(VUi.Danger, CircleShape)) } }

@Composable private fun VChapters(unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, open: (Int) -> Unit, home: () -> Unit, tasks: () -> Unit, profile: () -> Unit) {
    VSurface { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 92.dp)) { VTop("Bölümler", back); Text("Her bölüm 5 level", color = VUi.Muted, fontSize = 12.sp); Spacer(Modifier.height(14.dp)); vChapters().forEachIndexed { i, ch -> val range = ch.start until ch.endExclusive; val available = unlocked >= ch.start; val done = range.count { it in completed }; val s = range.sumOf { stars[it] ?: 0 }; Row(Modifier.fillMaxWidth().height(104.dp).clip(RoundedCornerShape(20.dp)).background(VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(20.dp)).clickable(enabled = available) { open(i) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(if (available) ch.soft else Color(0xFFF1F3F6), CircleShape), contentAlignment = Alignment.Center) { if (available) Text("${i + 1}", color = ch.accent, fontWeight = FontWeight.Black, fontSize = 18.sp) else Icon(Icons.Filled.Lock, null, tint = VUi.Subtle) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(ch.name, color = if (available) VUi.Ink else VUi.Subtle, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(ch.subtitle, color = VUi.Muted, fontSize = 10.sp); Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress = { done / range.count().toFloat().coerceAtLeast(1f) }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = ch.accent, trackColor = Color(0xFFF0F2F6)); Spacer(Modifier.height(5.dp)); Text(if (available) "$done/${range.count()} level · $s/${range.count() * 3} yıldız" else "Kilitli", color = VUi.Muted, fontSize = 9.sp) }; Icon(Icons.Filled.ChevronRight, null, tint = VUi.Subtle) }; Spacer(Modifier.height(11.dp)) } }; Box(Modifier.align(Alignment.BottomCenter)) { VBottom(VScreen.CHAPTERS, home, {}, tasks, profile) } } }
}

@Composable private fun VMap(chapterIndex: Int, unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, open: (Int) -> Unit) {
    val ch = vChapters()[chapterIndex]; val indexes = (ch.start until ch.endExclusive).toList(); val nodes = listOf(.18f to .82f, .42f to .65f, .72f to .49f, .45f to .32f, .22f to .15f)
    VSurface { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { VTop("${chapterIndex + 1}. ${ch.name}", back); Row { Text("${indexes.count { it in completed }}/${indexes.size} tamamlandı", color = VUi.Muted, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text("${indexes.sumOf { stars[it] ?: 0 }} yıldız", color = VUi.Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp) }; Spacer(Modifier.height(14.dp)); BoxWithConstraints(Modifier.fillMaxWidth().height(590.dp).clip(RoundedCornerShape(26.dp)).background(ch.soft.copy(alpha = .55f)).border(1.dp, VUi.Border, RoundedCornerShape(26.dp))) { Canvas(Modifier.fillMaxSize()) { val path = Path(); nodes.forEachIndexed { i, n -> val x = size.width * n.first; val y = size.height * n.second; if (i == 0) path.moveTo(x, y) else { val prev = nodes[i - 1]; val px = size.width * prev.first; val py = size.height * prev.second; val mid = (py + y) / 2; path.cubicTo(px, mid, x, mid, x, y) } }; drawPath(path, Color(0xFFD8DEE8), style = Stroke(9f, cap = StrokeCap.Round)); indexes.forEachIndexed { i, global -> if (global in completed && i < nodes.lastIndex) { val a = nodes[i]; val b = nodes[i + 1]; val p = Path().apply { val x1 = size.width * a.first; val y1 = size.height * a.second; val x2 = size.width * b.first; val y2 = size.height * b.second; val mid = (y1 + y2) / 2; moveTo(x1, y1); cubicTo(x1, mid, x2, mid, x2, y2) }; drawPath(p, ch.accent.copy(alpha = .72f), style = Stroke(9f, cap = StrokeCap.Round)) } } }; nodes.forEachIndexed { i, n -> val global = indexes[i]; val node = 64.dp; Column(Modifier.offset(maxWidth * n.first - node / 2, maxHeight * n.second - node / 2), horizontalAlignment = Alignment.CenterHorizontally) { VLevelNode(i + 1, global <= unlocked, global in completed, global == unlocked && global !in completed, ch.accent) { open(global) }; Spacer(Modifier.height(3.dp)); Row { repeat(3) { s -> Icon(if (s < (stars[global] ?: 0)) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (s < (stars[global] ?: 0)) VUi.Gold else Color(0xFFC9CED7), modifier = Modifier.size(12.dp)) } } } } }; Spacer(Modifier.height(14.dp)) } }
}
@Composable private fun VLevelNode(number: Int, available: Boolean, done: Boolean, current: Boolean, accent: Color, click: () -> Unit) { val pulse by animateFloatAsState(if (current) 1.07f else 1f, tween(550), label = "node"); val bg = when { !available -> Color(0xFFF0F2F5); done -> VUi.Success; current -> accent; else -> accent.copy(alpha = .82f) }; Box(Modifier.size(62.dp).scale(pulse).shadow(if (current) 9.dp else 3.dp, CircleShape).background(bg, CircleShape).border(if (current) 3.dp else 1.dp, Color.White, CircleShape).clickable(enabled = available, onClick = click), contentAlignment = Alignment.Center) { when { !available -> Icon(Icons.Filled.Lock, null, tint = VUi.Subtle); done -> Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(26.dp)); else -> Text(number.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp) } } }

private fun vCellsFor(p: Placement) = p.word.mapIndexed { i, ch -> VCell(p.row + if (p.horizontal) 0 else i, p.col + if (p.horizontal) i else 0) to ch }
private fun vAllCells(level: Level) = level.placements.flatMap(::vCellsFor).map { it.first }.toSet()
private fun vRevealed(level: Level, cell: VCell, found: Set<String>) = level.placements.any { it.word in found && vCellsFor(it).any { pair -> pair.first == cell } }
private fun vCanMake(word: String, letters: List<Char>): Boolean { val counts = letters.groupingBy { it }.eachCount().toMutableMap(); word.forEach { c -> val n = counts[c] ?: return false; if (n <= 0) return false; counts[c] = n - 1 }; return true }
private fun vValidate(level: Level): VValidation { val map = mutableMapOf<VCell, Char>(); val errors = mutableListOf<String>(); level.placements.forEach { p -> if (p.word !in level.targets) errors += "Yerleşim hedef değil: ${p.word}"; vCellsFor(p).forEach { (cell, ch) -> val old = map[cell]; if (old != null && old != ch) errors += "Kesişim hatası $cell: $old/$ch" else map[cell] = ch } }; level.targets.forEach { if (!vCanMake(it, level.letters)) errors += "Harflerle kurulamıyor: $it" }; return VValidation(map, errors) }
private fun parseCells(raw: Set<String>) = raw.mapNotNull { s -> val p = s.split(','); val r = p.getOrNull(0)?.toIntOrNull(); val c = p.getOrNull(1)?.toIntOrNull(); if (r != null && c != null) VCell(r, c) else null }.toSet()
private fun encodeCells(cells: Set<VCell>) = cells.map { "${it.row},${it.col}" }.toSet()

@Composable private fun VGame(levelIndex: Int, parentCoins: Int, previousStars: Int, firstCompletion: Boolean, soundEnabled: Boolean, hapticEnabled: Boolean, reduceMotion: Boolean, onCoinsChanged: (Int) -> Unit, onWordFound: () -> Unit, onHintUsed: () -> Unit, onCombo: (Int) -> Unit, onCompleted: (Int) -> Unit, onBack: () -> Unit, onReplay: () -> Unit, onMap: () -> Unit, onNext: () -> Unit, onClaimChapter: () -> Unit, chapterChestClaimed: Boolean) {
    val context = LocalContext.current; val prefs = remember { context.getSharedPreferences(VPREFS, Context.MODE_PRIVATE) }; val level = levels[levelIndex]; val validation = remember(levelIndex) { vValidate(level) }; val haptics = LocalHapticFeedback.current; val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 60) }; DisposableEffect(Unit) { onDispose { tone.release() } }
    var localCoins by remember { mutableIntStateOf(parentCoins) }; var wheel by remember { mutableStateOf(level.letters) }
    var found by remember { mutableStateOf(prefs.getStringSet(vKey(levelIndex, P_FOUND), emptySet()).orEmpty().toSet()) }
    var bonus by remember { mutableStateOf(prefs.getStringSet(vKey(levelIndex, P_BONUS), emptySet()).orEmpty().toSet()) }
    var hintedCells by remember { mutableStateOf(parseCells(prefs.getStringSet(vKey(levelIndex, P_HINT_CELLS), emptySet()).orEmpty())) }
    var hintsUsed by remember { mutableIntStateOf(prefs.getInt(vKey(levelIndex, P_HINT_COUNT), 0)) }
    var claimedWords by remember { mutableStateOf(prefs.getStringSet(vKey(levelIndex, P_CLAIMED), emptySet()).orEmpty().toSet()) }
    var claimedBonus by remember { mutableStateOf(prefs.getStringSet(vKey(levelIndex, P_CLAIMED_BONUS), emptySet()).orEmpty().toSet()) }
    var selection by remember { mutableStateOf(emptyList<Int>()) }; var live by remember { mutableStateOf("") }; var status by remember { mutableStateOf(if (validation.errors.isEmpty()) "Harfleri birleştir" else "Level verisi kontrol edildi") }; var combo by remember { mutableIntStateOf(0) }; var handled by remember { mutableStateOf(false) }; var showResult by remember { mutableStateOf(false) }; var net by remember { mutableIntStateOf(0) }; var recentWord by remember { mutableStateOf<String?>(null) }; var recentHint by remember { mutableStateOf<VCell?>(null) }; var feedback by remember { mutableStateOf(WheelFeedback.NEUTRAL) }
    fun persist() { prefs.edit().putStringSet(vKey(levelIndex, P_FOUND), found).putStringSet(vKey(levelIndex, P_BONUS), bonus).putStringSet(vKey(levelIndex, P_HINT_CELLS), encodeCells(hintedCells)).putInt(vKey(levelIndex, P_HINT_COUNT), hintsUsed).putStringSet(vKey(levelIndex, P_CLAIMED), claimedWords).putStringSet(vKey(levelIndex, P_CLAIMED_BONUS), claimedBonus).apply() }
    LaunchedEffect(validation.errors) { if (validation.errors.isNotEmpty()) { Log.e("WordZValidator", "${level.title}: ${validation.errors.joinToString(" | ")}"); status = "Bulmaca verisi doğrulandı · ${validation.errors.size} uyarı" } }
    LaunchedEffect(recentWord) { if (recentWord != null) { delay(if (reduceMotion) 50 else 650); recentWord = null } }
    LaunchedEffect(recentHint) { if (recentHint != null) { delay(if (reduceMotion) 50 else 700); recentHint = null } }
    LaunchedEffect(feedback) { if (feedback != WheelFeedback.NEUTRAL) { delay(if (reduceMotion) 80 else 360); feedback = WheelFeedback.NEUTRAL } }
    val done = level.targets.all { it in found }; val earned = when (hintsUsed) { 0 -> 3; 1 -> 2; 2 -> 1; else -> 0 }; val chapter = vChapters()[levelIndex / 5]; val chapterEnd = levelIndex == chapter.endExclusive - 1
    LaunchedEffect(done) { if (done && !handled) { handled = true; onCompleted(earned); if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_ACK, 150); if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress); delay(if (reduceMotion) 80 else 300); showResult = true } }
    val wordScale by animateFloatAsState(if (live.isNotBlank()) 1.025f else 1f, tween(if (reduceMotion) 0 else 100), label = "word")
    VSurface { BoxWithConstraints(Modifier.fillMaxSize()) { val compact = maxHeight < 760.dp; Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) { Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) { VIconButton(Icons.Filled.ArrowBack, onBack); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Level ${levelIndex + 1}", color = VUi.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(level.title, color = VUi.Muted, fontSize = 10.sp) }; VChip(Icons.Filled.AutoAwesome, localCoins.toString(), VUi.Gold) }
        Spacer(Modifier.height(if (compact) 3.dp else 8.dp)); VBoard(level, validation, found, hintedCells, recentWord, recentHint, compact); Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("${found.size}/${level.targets.size} kelime", color = VUi.Muted, fontSize = 11.sp, modifier = Modifier.weight(1f)); if (combo >= 2) Text("Combo x$combo", color = VUi.Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp) else Row { repeat(3) { i -> Icon(if (i < earned) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (i < earned) VUi.Gold else Color(0xFFCDD2DA), modifier = Modifier.size(15.dp)) } } }
        Spacer(Modifier.height(6.dp)); Box(Modifier.fillMaxWidth().height(44.dp).scale(wordScale).clip(RoundedCornerShape(13.dp)).background(if (live.isNotBlank()) VUi.PrimarySoft else VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Text(if (live.isNotBlank()) live else status, color = if (live.isNotBlank()) VUi.PrimaryDark else VUi.Muted, fontWeight = if (live.isNotBlank()) FontWeight.Bold else FontWeight.Medium, fontSize = if (live.isNotBlank()) 19.sp else 11.sp, textAlign = TextAlign.Center) }
        Spacer(Modifier.height(if (compact) 0.dp else 4.dp)); Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { VWheel(wheel, selection, if (compact) 214.dp else 244.dp, feedback, { if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }, { selection = it; live = it.joinToString("") { idx -> wheel[idx].toString() } }) {
            val submitted = live.trim().uppercase(vTr); when {
                submitted in level.targets && submitted !in found -> { found = found + submitted; recentWord = submitted; combo++; onCombo(combo); val firstReward = submitted !in claimedWords; if (firstReward) { claimedWords = claimedWords + submitted; val extra = if (combo % 3 == 0) 10 else 0; val gain = 5 + extra; localCoins += gain; net += gain; onCoinsChanged(localCoins); onWordFound(); status = if (extra > 0) "Combo x$combo · +$gain" else "Doğru · $submitted" } else status = "Doğru · $submitted"; feedback = WheelFeedback.CORRECT; if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 90) }
                submitted in found -> status = "Zaten bulundu"
                submitted in level.bonusWords && submitted !in bonus -> { bonus = bonus + submitted; combo++; val firstReward = submitted !in claimedBonus; if (firstReward) { claimedBonus = claimedBonus + submitted; localCoins += 10; net += 10; onCoinsChanged(localCoins); onWordFound() }; status = if (firstReward) "Bonus kelime · +10" else "Bonus kelime"; feedback = WheelFeedback.BONUS; if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80) }
                submitted.isNotBlank() -> { combo = 0; status = "$submitted bulunamadı"; feedback = WheelFeedback.WRONG; if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_NACK, 80); if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
            }; persist(); selection = emptyList(); live = ""
        } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { VTool(Icons.Filled.Shuffle, "Karıştır", true, Modifier.weight(1f)) { wheel = wheel.shuffled(); status = "Harfler karıştırıldı" }; VTool(Icons.Filled.Lightbulb, "Harf 25", localCoins >= 25 && !done, Modifier.weight(1f)) { val hidden = vAllCells(level).filter { it !in hintedCells && !vRevealed(level, it, found) }; hidden.randomOrNull()?.let { localCoins -= 25; net -= 25; hintedCells = hintedCells + it; recentHint = it; hintsUsed++; combo = 0; onCoinsChanged(localCoins); onHintUsed(); status = "Bir harf açıldı"; persist() } }; VTool(Icons.Filled.Search, "Kelime 60", localCoins >= 60 && !done, Modifier.weight(1f)) { level.targets.firstOrNull { it !in found }?.let { word -> localCoins -= 60; net -= 60; found = found + word; claimedWords = claimedWords + word; recentWord = word; hintsUsed += 2; combo = 0; onCoinsChanged(localCoins); onHintUsed(); status = "Kelime açıldı"; persist() } } }
    }; if (showResult) VResult(levelIndex, earned, previousStars, found.size, level.targets.size, bonus.size, hintsUsed, combo, net, firstCompletion, chapterEnd, chapterChestClaimed, onReplay, onMap, onNext, onClaimChapter) } }
}

@Composable private fun VTool(icon: ImageVector, label: String, enabled: Boolean, modifier: Modifier = Modifier, click: () -> Unit) { Column(modifier.height(58.dp).clip(RoundedCornerShape(15.dp)).background(VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(15.dp)).clickable(enabled = enabled, onClick = click).padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = if (enabled) VUi.Primary else VUi.Subtle, modifier = Modifier.size(20.dp)); Text(label, color = if (enabled) VUi.Ink else VUi.Subtle, fontWeight = FontWeight.SemiBold, fontSize = 9.sp) } }

@Composable private fun VBoard(level: Level, validation: VValidation, found: Set<String>, hinted: Set<VCell>, recentWord: String?, recentHint: VCell?, compact: Boolean) {
    val map = validation.cells; val rows = (map.keys.maxOfOrNull { it.row } ?: 0) + 1; val cols = (map.keys.maxOfOrNull { it.col } ?: 0) + 1; val cellSize = when { compact && cols >= 5 -> 33.dp; cols >= 6 -> 35.dp; cols == 5 -> 39.dp; else -> 42.dp }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(20.dp)).padding(if (compact) 8.dp else 11.dp), contentAlignment = Alignment.Center) { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { repeat(rows) { r -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(cols) { c -> val cell = VCell(r, c); val letter = map[cell]; if (letter == null) Spacer(Modifier.size(cellSize)) else { val revealed = cell in hinted || vRevealed(level, cell, found); val inRecentWord = recentWord != null && level.placements.any { it.word == recentWord && vCellsFor(it).any { pair -> pair.first == cell } }; val targetScale by animateFloatAsState(if (revealed) 1f else .94f, tween(180), label = "cell"); val bg = when { cell == recentHint -> VUi.GoldSoft; inRecentWord -> VUi.SuccessSoft; revealed -> VUi.PrimarySoft; else -> Color(0xFFF0F3F8) }; val border = when { cell == recentHint -> VUi.Gold.copy(alpha = .65f); inRecentWord -> VUi.Success.copy(alpha = .6f); revealed -> VUi.Primary.copy(alpha = .42f); else -> Color(0xFFD8DEE8) }; Box(Modifier.size(cellSize).scale(targetScale).clip(RoundedCornerShape(8.dp)).background(bg).border(1.dp, border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { if (revealed) Text(letter.toString(), color = if (cell == recentHint) Color(0xFF9B6610) else if (inRecentWord) Color(0xFF147746) else VUi.PrimaryDark, fontWeight = FontWeight.Black, fontSize = if (compact) 15.sp else 17.sp) } } } } } }
}

@Composable private fun VWheel(letters: List<Char>, selected: List<Int>, sizeDp: Dp, feedback: WheelFeedback, onLetterEntered: () -> Unit, onChanged: (List<Int>) -> Unit, onFinished: () -> Unit) {
    var measured by remember { mutableStateOf(IntSize.Zero) }; var pointer by remember { mutableStateOf<Offset?>(null) }; val shake = remember { Animatable(0f) }
    LaunchedEffect(feedback) { if (feedback == WheelFeedback.WRONG) { listOf(-9f, 9f, -6f, 6f, 0f).forEach { target -> shake.animateTo(target, tween(36)) } } else shake.snapTo(0f) }
    fun positions(): List<Offset> { if (measured == IntSize.Zero) return emptyList(); val center = Offset(measured.width / 2f, measured.height / 2f); val radius = minOf(measured.width, measured.height) * .34f; return letters.indices.map { i -> val a = -PI / 2 + 2 * PI * i / letters.size; Offset(center.x + (cos(a) * radius).toFloat(), center.y + (sin(a) * radius).toFloat()) } }
    fun at(p: Offset): Int? { val pts = positions(); val hit = minOf(measured.width, measured.height) * .135f; return pts.indices.firstOrNull { i -> hypot((p.x - pts[i].x).toDouble(), (p.y - pts[i].y).toDouble()) <= hit } }
    val lineColor = when (feedback) { WheelFeedback.CORRECT -> VUi.Success; WheelFeedback.BONUS -> VUi.Gold; WheelFeedback.WRONG -> VUi.Danger; else -> VUi.Primary }
    Box(Modifier.size(sizeDp).graphicsLayer(translationX = shake.value).onSizeChanged { measured = it }.pointerInput(letters, measured) { var gesture = emptyList<Int>(); detectDragGestures(onDragStart = { p -> pointer = p; val i = at(p); gesture = if (i != null) listOf(i) else emptyList(); if (i != null) onLetterEntered(); onChanged(gesture) }, onDragEnd = { pointer = null; onFinished(); gesture = emptyList() }, onDragCancel = { pointer = null; onFinished(); gesture = emptyList() }) { change, _ -> pointer = change.position; val i = at(change.position); if (i != null) { when { gesture.size >= 2 && i == gesture[gesture.lastIndex - 1] -> { gesture = gesture.dropLast(1); onLetterEntered(); onChanged(gesture) }; i !in gesture -> { gesture = gesture + i; onLetterEntered(); onChanged(gesture) } } } } }) { Canvas(Modifier.fillMaxSize()) { val center = Offset(size.width / 2, size.height / 2); val radius = minOf(size.width, size.height) * .47f; drawCircle(VUi.Surface, radius, center); drawCircle(Color(0xFFE3E7EE), radius, center, style = Stroke(3f)); val pts = positions(); selected.zipWithNext().forEach { (a, b) -> if (a in pts.indices && b in pts.indices) drawLine(lineColor, pts[a], pts[b], 9f, cap = StrokeCap.Round) }; if (selected.isNotEmpty() && pointer != null && selected.last() in pts.indices) drawLine(lineColor.copy(alpha = .58f), pts[selected.last()], pointer!!, 7f, cap = StrokeCap.Round); pts.forEachIndexed { i, p -> val sel = i in selected; val r = minOf(size.width, size.height) * if (sel) .116f else .101f; if (sel) drawCircle(lineColor.copy(alpha = .13f), r * 1.23f, p); drawCircle(if (sel) lineColor else Color(0xFFF8F9FB), r, p); drawCircle(if (sel) lineColor else Color(0xFFD2D8E1), r, p, style = Stroke(if (sel) 3f else 2.4f)); drawIntoCanvas { canvas -> val paint = android.graphics.Paint().apply { color = if (sel) android.graphics.Color.WHITE else android.graphics.Color.rgb(23, 32, 51); textAlign = android.graphics.Paint.Align.CENTER; textSize = minOf(size.width, size.height) * if (sel) .111f else .104f; typeface = android.graphics.Typeface.DEFAULT_BOLD }; val m = paint.fontMetrics; canvas.nativeCanvas.drawText(letters[i].toString(), p.x, p.y - (m.ascent + m.descent) / 2, paint) } } } }
}

@Composable private fun VResult(levelIndex: Int, stars: Int, previous: Int, found: Int, targets: Int, bonus: Int, hints: Int, combo: Int, net: Int, firstCompletion: Boolean, chapterEnd: Boolean, chestClaimed: Boolean, replay: () -> Unit, map: () -> Unit, next: () -> Unit, claim: () -> Unit) { Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .38f)).padding(horizontal = 22.dp), contentAlignment = Alignment.Center) { VCard(Modifier.fillMaxWidth()) { Text(if (chapterEnd) "Bölüm tamamlandı" else "Level tamamlandı", color = VUi.Ink, fontWeight = FontWeight.Black, fontSize = 23.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { repeat(3) { i -> Icon(if (i < stars) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (i < stars) VUi.Gold else Color(0xFFCDD2DA), modifier = Modifier.size(if (i == 1) 42.dp else 35.dp)) } }; Spacer(Modifier.height(6.dp)); Text(if (stars > previous) "Yeni en iyi skor" else "En iyi skor: $previous yıldız", color = if (stars > previous) VUi.Primary else VUi.Muted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center); if (firstCompletion && !chapterEnd) { Spacer(Modifier.height(8.dp)); Text("Level ${levelIndex + 2} açıldı", color = VUi.Success, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }; Spacer(Modifier.height(14.dp)); VResultRow("Kelime", "$found/$targets"); VResultRow("Bonus", bonus.toString()); VResultRow("İpucu", hints.toString()); VResultRow("Combo", "x$combo"); HorizontalDivider(color = VUi.Border, modifier = Modifier.padding(vertical = 10.dp)); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Bu oyundaki net jeton", color = VUi.Muted, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text(if (net >= 0) "+$net" else net.toString(), color = if (net >= 0) VUi.Success else VUi.Danger, fontWeight = FontWeight.Black, fontSize = 20.sp) }; if (chapterEnd && !chestClaimed) { Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(VUi.GoldSoft).clickable(onClick = claim).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.CardGiftcard, null, tint = VUi.Gold); Spacer(Modifier.width(9.dp)); Text("Bölüm sandığı · +150", color = VUi.Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("AL", color = VUi.Gold, fontWeight = FontWeight.Black) } }; Spacer(Modifier.height(15.dp)); VPrimary(if (chapterEnd) "Haritaya dön" else "Sonraki level", if (chapterEnd) map else next, Modifier.fillMaxWidth(), if (chapterEnd) Icons.Filled.Map else Icons.Filled.PlayArrow); Spacer(Modifier.height(9.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { VTool(Icons.Filled.Refresh, "Tekrar", true, Modifier.weight(1f), replay); if (!chapterEnd) VTool(Icons.Filled.Map, "Harita", true, Modifier.weight(1f), map) } } } }
@Composable private fun VResultRow(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(label, color = VUi.Muted, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text(value, color = VUi.Ink, fontWeight = FontWeight.Bold, fontSize = 11.sp) } }

@Composable private fun VDaily(streak: Int, available: Boolean, back: () -> Unit, claim: () -> Unit) { VSurface { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { VTop("Günlük ödül", back); Spacer(Modifier.height(18.dp)); VCard(Modifier.fillMaxWidth()) { Text("$streak günlük seri", color = VUi.Ink, fontWeight = FontWeight.Black, fontSize = 22.sp); Text("Bugünkü ödül +40 jeton", color = VUi.Muted, fontSize = 11.sp) }; Spacer(Modifier.height(14.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { repeat(7) { d -> Column(Modifier.weight(1f).height(82.dp).clip(RoundedCornerShape(13.dp)).background(if (d == streak % 7) VUi.PrimarySoft else VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(13.dp)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("${d + 1}", color = VUi.Muted, fontSize = 10.sp); Icon(if (d == 6) Icons.Filled.CardGiftcard else Icons.Filled.AutoAwesome, null, tint = VUi.Gold, modifier = Modifier.size(18.dp)); Text(if (d == 6) "+200" else "+${40 + d * 10}", color = VUi.Ink, fontWeight = FontWeight.Bold, fontSize = 9.sp) } } }; Spacer(Modifier.weight(1f)); VPrimary(if (available) "Ödülü al" else "Bugün alındı", claim, Modifier.fillMaxWidth().padding(bottom = 24.dp), Icons.Filled.CardGiftcard, available) } } }

@Composable private fun VTasks(stats: VStats, claimed: Set<String>, back: () -> Unit, claim: (VTask) -> Unit, home: () -> Unit, chapters: () -> Unit, profile: () -> Unit) { VSurface { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 92.dp)) { VTop("Görevler", back); Spacer(Modifier.height(12.dp)); vTasks().forEach { task -> val current = task.current(stats).coerceAtMost(task.goal); val done = current >= task.goal; val got = task.id in claimed; VCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (done) Icons.Filled.CheckCircle else Icons.Filled.TaskAlt, null, tint = if (done) VUi.Success else VUi.Primary, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)); Text(task.title, color = VUi.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("+${task.reward}", color = VUi.Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp) }; Spacer(Modifier.height(10.dp)); LinearProgressIndicator(progress = { current.toFloat() / task.goal.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = if (done) VUi.Success else VUi.Primary, trackColor = Color(0xFFF0F2F6)); Spacer(Modifier.height(7.dp)); Row { Text("$current/${task.goal}", color = VUi.Muted, fontSize = 9.sp, modifier = Modifier.weight(1f)); Text(if (got) "Alındı" else if (done) "Ödülü al" else "Devam ediyor", color = if (done && !got) VUi.Primary else VUi.Muted, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.clickable(enabled = done && !got) { claim(task) }) } }; Spacer(Modifier.height(10.dp)) } }; Box(Modifier.align(Alignment.BottomCenter)) { VBottom(VScreen.TASKS, home, chapters, {}, profile) } } } }

@Composable private fun VProfile(coins: Int, stats: VStats, back: () -> Unit, home: () -> Unit, chapters: () -> Unit, tasks: () -> Unit, settings: () -> Unit) { val lvl = 1 + stats.stars / 15; VSurface { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 92.dp)) { VTop("Profil", back) { VIconButton(Icons.Filled.Settings, settings) }; Spacer(Modifier.height(14.dp)); VCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(58.dp).background(VUi.PrimarySoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, tint = VUi.Primary, modifier = Modifier.size(28.dp)) }; Spacer(Modifier.width(12.dp)); Column { Text("Oyuncu", color = VUi.Ink, fontWeight = FontWeight.Black, fontSize = 19.sp); Text("Seviye $lvl", color = VUi.Muted, fontSize = 10.sp) } } }; Spacer(Modifier.height(12.dp)); listOf("Yıldız" to stats.stars, "Level" to stats.completed, "Kelime" to stats.words, "İpucu" to stats.hints, "Combo" to stats.combo, "Jeton" to coins).chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { item -> VCard(Modifier.weight(1f)) { Text(item.first, color = VUi.Muted, fontSize = 9.sp); Text(item.second.toString(), color = VUi.Ink, fontWeight = FontWeight.Black, fontSize = 20.sp) } }; Spacer(Modifier.height(10.dp)) } }; Box(Modifier.align(Alignment.BottomCenter)) { VBottom(VScreen.PROFILE, home, chapters, tasks, {}) } } } }

@Composable private fun VSettings(sound: Boolean, haptic: Boolean, motion: Boolean, back: () -> Unit, soundChanged: (Boolean) -> Unit, hapticChanged: (Boolean) -> Unit, motionChanged: (Boolean) -> Unit) { VSurface { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { VTop("Ayarlar", back); Spacer(Modifier.height(14.dp)); VSetting("Ses efektleri", Icons.Filled.VolumeUp, sound, soundChanged); Spacer(Modifier.height(9.dp)); VSetting("Titreşim", Icons.Filled.Vibration, haptic, hapticChanged); Spacer(Modifier.height(9.dp)); VSetting("Animasyonları azalt", Icons.Filled.Refresh, motion, motionChanged) } } }
@Composable private fun VSetting(label: String, icon: ImageVector, value: Boolean, changed: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth().height(68.dp).clip(RoundedCornerShape(17.dp)).background(VUi.Surface).border(1.dp, VUi.Border, RoundedCornerShape(17.dp)).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = VUi.Primary); Spacer(Modifier.width(12.dp)); Text(label, color = VUi.Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f)); Switch(checked = value, onCheckedChange = changed) } }
