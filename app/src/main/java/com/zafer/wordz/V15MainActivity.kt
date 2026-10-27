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

private object V15Ui {
    val Background = Color(0xFFF5F7FA)
    val Surface = Color.White
    val Ink = Color(0xFF172033)
    val Muted = Color(0xFF697386)
    val Subtle = Color(0xFF98A2B3)
    val Border = Color(0xFFE7EAF0)
    val Primary = Color(0xFF356DF3)
    val PrimaryDark = Color(0xFF2456D6)
    val PrimarySoft = Color(0xFFEEF3FF)
    val Success = Color(0xFF25A765)
    val SuccessSoft = Color(0xFFECF8F1)
    val Gold = Color(0xFFF4A817)
    val GoldSoft = Color(0xFFFFF6E3)
    val Danger = Color(0xFFE94B5F)
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

private data class VCell(val row: Int, val col: Int) {
    fun encode() = "$row,$col"
    companion object {
        fun decode(raw: String): VCell? {
            val p = raw.split(',')
            return if (p.size == 2) VCell(p[0].toIntOrNull() ?: return null, p[1].toIntOrNull() ?: return null) else null
        }
    }
}
private data class VChapter(val name: String, val subtitle: String, val start: Int, val endExclusive: Int, val accent: Color, val soft: Color)
private data class VStats(val completed: Int, val stars: Int, val words: Int, val hints: Int, val combo: Int, val streak: Int)
private data class VTask(val id: String, val title: String, val reward: Int, val current: (VStats) -> Int, val goal: Int)
private enum class VScreen { HOME, CHAPTERS, MAP, GAME, DAILY, TASKS, PROFILE, SETTINGS }
private enum class WheelFeedback { NORMAL, CORRECT, BONUS, WRONG }

class V15MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validateAllLevels()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = V15Ui.Primary,
                    secondary = V15Ui.Gold,
                    background = V15Ui.Background,
                    surface = V15Ui.Surface,
                    onPrimary = Color.White,
                    onBackground = V15Ui.Ink,
                    onSurface = V15Ui.Ink,
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
    val p = it.split(':'); val l = p.getOrNull(0)?.toIntOrNull(); val s = p.getOrNull(1)?.toIntOrNull()
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
    val progressStore = remember { GameProgressStore(prefs) }
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
                coins = coins,
                previousStars = stars[selectedLevel] ?: 0,
                completedBefore = selectedLevel in completed,
                soundEnabled = sound,
                hapticEnabled = haptic,
                reduceMotion = reduceMotion,
                progressStore = progressStore,
                coinsChanged = { newCoins -> coins = newCoins; save(c = newCoins) },
                wordFound = { val v = words + 1; words = v; save(w = v) },
                hintUsed = { val v = hints + 1; hints = v; save(h = v) },
                comboChanged = { v -> if (v > bestCombo) { bestCombo = v; save(combo = v) } },
                completedCallback = { earned ->
                    val first = selectedLevel !in completed
                    val newCompleted = completed + selectedLevel
                    val oldStars = stars[selectedLevel] ?: 0
                    val newStars = if (earned > oldStars) stars + (selectedLevel to earned) else stars
                    val newUnlocked = if (selectedLevel >= unlocked && unlocked < levels.lastIndex) selectedLevel + 1 else unlocked
                    val newCoins = coins + if (first) 25 else 0
                    completed = newCompleted; stars = newStars; unlocked = newUnlocked; coins = newCoins
                    save(u = newUnlocked, c = newCoins, comp = newCompleted, st = newStars)
                },
                back = { screen = VScreen.MAP },
                replay = { gameSession++ },
                map = { screen = VScreen.MAP },
                next = {
                    val chapter = vChapters()[selectedChapter]
                    if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = VScreen.MAP
                },
                claimChapter = {
                    val chapter = vChapters()[selectedChapter]; val chestKey = selectedChapter.toString()
                    val allDone = (chapter.start until chapter.endExclusive).all { it in completed || it == selectedLevel }
                    if (allDone && chestKey !in chapterChests) {
                        val nc = coins + 150; val ns = chapterChests + chestKey
                        coins = nc; chapterChests = ns; save(c = nc, chests = ns)
                    }
                },
                chapterChestClaimed = selectedChapter.toString() in chapterChests,
            )
        }
        VScreen.DAILY -> VDaily(streak, dailyAvailable, { screen = VScreen.HOME }) {
            if (dailyAvailable) {
                val ns = if (dailyDate == today.minusDays(1).toString()) streak + 1 else 1
                val nc = coins + 40; val nd = today.toString()
                streak = ns; coins = nc; dailyDate = nd; save(c = nc, date = nd, str = ns)
            }
        }
        VScreen.TASKS -> VTasks(stats, claimedTasks, { screen = VScreen.HOME }, { task ->
            if (task.current(stats) >= task.goal && task.id !in claimedTasks) {
                val nt = claimedTasks + task.id; val nc = coins + task.reward
                claimedTasks = nt; coins = nc; save(c = nc, tasks = nt)
            }
        }, { screen = VScreen.HOME }, { screen = VScreen.CHAPTERS }, { screen = VScreen.PROFILE })
        VScreen.PROFILE -> VProfile(coins, stats, { screen = VScreen.HOME }, { screen = VScreen.HOME }, { screen = VScreen.CHAPTERS }, { screen = VScreen.TASKS }, { screen = VScreen.SETTINGS })
        VScreen.SETTINGS -> VSettings(sound, haptic, reduceMotion, { screen = VScreen.PROFILE }, { sound = it; save(snd = it) }, { haptic = it; save(hap = it) }, { reduceMotion = it; save(motion = it) })
    }
}

@Composable private fun VSurface(content: @Composable () -> Unit) { Box(Modifier.fillMaxSize().background(V15Ui.Background).statusBarsPadding()) { content() } }
@Composable private fun VTopBar(title: String? = null, back: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) { VIconButton(Icons.Filled.ArrowBack, back); Spacer(Modifier.width(12.dp)) }
        if (title != null) Text(title, color = V15Ui.Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}
@Composable private fun VIconButton(icon: ImageVector, onClick: () -> Unit, badge: Boolean = false) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(V15Ui.Surface).border(1.dp, V15Ui.Border, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = V15Ui.Ink, modifier = Modifier.size(20.dp)); if (badge) Box(Modifier.align(Alignment.TopEnd).size(9.dp).background(V15Ui.Danger, CircleShape))
    }
}
@Composable private fun VChip(icon: ImageVector, value: String, tint: Color) {
    Row(Modifier.clip(RoundedCornerShape(14.dp)).background(V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp)); Text(value, color = V15Ui.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
@Composable private fun VCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.shadow(5.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000)).clip(RoundedCornerShape(20.dp)).background(V15Ui.Surface).border(1.dp, V15Ui.Border.copy(alpha = .7f), RoundedCornerShape(20.dp)).padding(18.dp), content = content)
}
@Composable private fun VPrimary(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    Row(modifier.height(54.dp).clip(RoundedCornerShape(16.dp)).background(if (enabled) V15Ui.Primary else Color(0xFFD5D9E2)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        if (icon != null) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
@Composable private fun VSecondary(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(modifier.height(46.dp).clip(RoundedCornerShape(14.dp)).background(V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        if (icon != null) { Icon(icon, null, tint = V15Ui.Primary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)) }
        Text(text, color = V15Ui.Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
@Composable private fun VBottom(active: VScreen, home: () -> Unit, chapters: () -> Unit, tasks: () -> Unit, profile: () -> Unit) {
    Row(Modifier.fillMaxWidth().navigationBarsPadding().height(70.dp).background(V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
        VNav("Ana Sayfa", Icons.Filled.Home, active == VScreen.HOME, home); VNav("Bölümler", Icons.Filled.Map, active == VScreen.CHAPTERS, chapters); VNav("Görevler", Icons.Filled.TaskAlt, active == VScreen.TASKS, tasks); VNav("Profil", Icons.Filled.Person, active == VScreen.PROFILE, profile)
    }
}
@Composable private fun VNav(label: String, icon: ImageVector, active: Boolean, click: () -> Unit) {
    Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = click).padding(horizontal = 10.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (active) V15Ui.Primary else V15Ui.Subtle, modifier = Modifier.size(20.dp)); Text(label, color = if (active) V15Ui.Primary else V15Ui.Subtle, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable private fun VHome(coins: Int, stars: Int, unlocked: Int, completed: Int, streak: Int, daily: Boolean, taskBadge: Boolean, onContinue: () -> Unit, onChapters: () -> Unit, onDaily: () -> Unit, onTasks: () -> Unit, onProfile: () -> Unit, onSettings: () -> Unit) {
    val chapter = vChapters()[(unlocked / 5).coerceIn(0, vChapters().lastIndex)]
    VSurface { Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 88.dp)) {
            Row(Modifier.fillMaxWidth().height(66.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("WordZ", color = V15Ui.Ink, fontSize = 28.sp, fontWeight = FontWeight.Black); Text("Kelime bulmaca", color = V15Ui.Muted, fontSize = 12.sp) }
                VChip(Icons.Filled.AutoAwesome, coins.toString(), V15Ui.Gold); Spacer(Modifier.width(8.dp)); VChip(Icons.Filled.Star, stars.toString(), V15Ui.Gold); Spacer(Modifier.width(8.dp)); VIconButton(Icons.Filled.Settings, onSettings)
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(214.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(chapter.accent, chapter.accent.copy(alpha = .82f)))).padding(22.dp)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("DEVAM ET", color = Color.White.copy(alpha = .72f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(Modifier.height(7.dp)); Text(chapter.name, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black); Text("Level ${unlocked + 1} · ${levels[unlocked].title}", color = Color.White.copy(alpha = .86f), fontSize = 13.sp) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { LinearProgressIndicator(progress = { completed.toFloat() / levels.size.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = .24f)); Spacer(Modifier.height(6.dp)); Text("$completed / ${levels.size} tamamlandı", color = Color.White.copy(alpha = .75f), fontSize = 11.sp) }
                        Spacer(Modifier.width(16.dp)); Box(Modifier.size(50.dp).background(Color.White, CircleShape).clickable(onClick = onContinue), contentAlignment = Alignment.Center) { Icon(Icons.Filled.PlayArrow, null, tint = chapter.accent, modifier = Modifier.size(27.dp)) }
                    }
                }
            }
            Spacer(Modifier.height(18.dp)); Text("Bugün", color = V15Ui.Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VQuick("Günlük ödül", if (daily) "+40 hazır" else "Bugün alındı", Icons.Filled.CardGiftcard, V15Ui.GoldSoft, V15Ui.Gold, daily, onDaily, Modifier.weight(1f))
                VQuick("Görevler", if (taskBadge) "Ödül hazır" else "İlerlemeyi gör", Icons.Filled.TaskAlt, V15Ui.PrimarySoft, V15Ui.Primary, taskBadge, onTasks, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp)); VCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.LocalFireDepartment, null, tint = V15Ui.Gold); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("$streak günlük seri", color = V15Ui.Ink, fontWeight = FontWeight.Bold); Text("Serini koru, ödülleri büyüt", color = V15Ui.Muted, fontSize = 11.sp) }; Icon(Icons.Filled.ChevronRight, null, tint = V15Ui.Subtle) } }
        }
        Box(Modifier.align(Alignment.BottomCenter)) { VBottom(VScreen.HOME, {}, onChapters, onTasks, onProfile) }
    } }
}
@Composable private fun VQuick(title: String, subtitle: String, icon: ImageVector, bg: Color, tint: Color, badge: Boolean, click: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.height(112.dp).clip(RoundedCornerShape(18.dp)).background(V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(18.dp)).clickable(onClick = click).padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Box(Modifier.size(36.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }; if (badge) Box(Modifier.size(8.dp).background(V15Ui.Danger, CircleShape)) }
        Column { Text(title, color = V15Ui.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(subtitle, color = V15Ui.Muted, fontSize = 11.sp) }
    }
}

@Composable private fun VChapters(unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, open: (Int) -> Unit, home: () -> Unit, tasks: () -> Unit, profile: () -> Unit) {
    VSurface { Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 86.dp)) {
            VTopBar("Bölümler", back); Text("Her bölüm 5 level", color = V15Ui.Muted, fontSize = 13.sp); Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                vChapters().forEachIndexed { i, ch -> val range = ch.start until ch.endExclusive; val openable = unlocked >= ch.start; val done = range.count { it in completed }; val star = range.sumOf { stars[it] ?: 0 }; VChapterCard(i, ch, done, range.count(), star, openable) { open(i) }; Spacer(Modifier.height(11.dp)) }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter)) { VBottom(VScreen.CHAPTERS, home, {}, tasks, profile) }
    } }
}
@Composable private fun VChapterCard(index: Int, ch: VChapter, done: Int, total: Int, stars: Int, unlocked: Boolean, click: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(102.dp).clip(RoundedCornerShape(18.dp)).background(V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(18.dp)).clickable(enabled = unlocked, onClick = click).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).background(if (unlocked) ch.soft else Color(0xFFF1F3F6), CircleShape), contentAlignment = Alignment.Center) { if (unlocked) Text("${index + 1}", color = ch.accent, fontWeight = FontWeight.Black, fontSize = 18.sp) else Icon(Icons.Filled.Lock, null, tint = V15Ui.Subtle) }
        Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(ch.name, color = if (unlocked) V15Ui.Ink else V15Ui.Subtle, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(ch.subtitle, color = V15Ui.Muted, fontSize = 11.sp); Spacer(Modifier.height(7.dp)); LinearProgressIndicator(progress = { done.toFloat() / total.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = if (unlocked) ch.accent else Color(0xFFD5D9E2), trackColor = Color(0xFFF0F2F5)); Spacer(Modifier.height(4.dp)); Text(if (unlocked) "$done/$total · $stars/${total * 3} yıldız" else "Kilitli", color = V15Ui.Muted, fontSize = 10.sp) }
        Icon(Icons.Filled.ChevronRight, null, tint = V15Ui.Subtle)
    }
}

@Composable private fun VMap(chapterIndex: Int, unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, openLevel: (Int) -> Unit) {
    val ch = vChapters()[chapterIndex]; val indices = (ch.start until ch.endExclusive).toList(); val nodes = listOf(.18f to .82f, .43f to .65f, .73f to .48f, .47f to .31f, .22f to .14f)
    VSurface { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        VTopBar("${chapterIndex + 1}. ${ch.name}", back)
        Row(verticalAlignment = Alignment.CenterVertically) { Text("${indices.count { it in completed }}/${indices.size} tamamlandı", color = V15Ui.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("${indices.sumOf { stars[it] ?: 0 }} yıldız", color = V15Ui.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        Spacer(Modifier.height(14.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(ch.soft, V15Ui.Surface))).border(1.dp, V15Ui.Border, RoundedCornerShape(26.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                fun segment(a: Pair<Float, Float>, b: Pair<Float, Float>) = Path().apply { val x1 = size.width * a.first; val y1 = size.height * a.second; val x2 = size.width * b.first; val y2 = size.height * b.second; val mid = (y1 + y2) / 2; moveTo(x1, y1); cubicTo(x1, mid, x2, mid, x2, y2) }
                for (i in 0 until nodes.lastIndex) drawPath(segment(nodes[i], nodes[i + 1]), Color(0xFFD9DEE7), style = Stroke(width = 9f, cap = StrokeCap.Round))
                for (i in 0 until nodes.lastIndex) if (indices[i] in completed) drawPath(segment(nodes[i], nodes[i + 1]), ch.accent.copy(alpha = .75f), style = Stroke(width = 9f, cap = StrokeCap.Round))
            }
            nodes.forEachIndexed { i, p -> val global = indices[i]; val nodeSize = 64.dp; val x = maxWidth * p.first - nodeSize / 2; val y = maxHeight * p.second - nodeSize / 2; val current = global == unlocked && global !in completed; Column(Modifier.offset(x, y), horizontalAlignment = Alignment.CenterHorizontally) { VLevelNode(i + 1, global <= unlocked, global in completed, current, ch.accent) { openLevel(global) }; Spacer(Modifier.height(4.dp)); Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) { repeat(3) { s -> Icon(if (s < (stars[global] ?: 0)) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (s < (stars[global] ?: 0)) V15Ui.Gold else Color(0xFFCDD2DA), modifier = Modifier.size(12.dp)) } } } }
        }
        Spacer(Modifier.height(16.dp))
    } }
}
@Composable private fun VLevelNode(number: Int, unlocked: Boolean, completed: Boolean, current: Boolean, accent: Color, click: () -> Unit) {
    val bg = when { !unlocked -> Color(0xFFF1F3F6); completed -> V15Ui.Success; current -> accent; else -> accent.copy(alpha = .82f) }
    Box(Modifier.size(62.dp).shadow(if (current) 10.dp else 3.dp, CircleShape).background(bg, CircleShape).border(if (current) 3.dp else 1.dp, Color.White, CircleShape).clickable(enabled = unlocked, onClick = click), contentAlignment = Alignment.Center) {
        when { !unlocked -> Icon(Icons.Filled.Lock, null, tint = V15Ui.Subtle, modifier = Modifier.size(21.dp)); completed -> Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(26.dp)); else -> Text(number.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp) }
    }
}

@Composable
private fun VGame(
    levelIndex: Int,
    coins: Int,
    previousStars: Int,
    completedBefore: Boolean,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    reduceMotion: Boolean,
    progressStore: GameProgressStore,
    coinsChanged: (Int) -> Unit,
    wordFound: () -> Unit,
    hintUsed: () -> Unit,
    comboChanged: (Int) -> Unit,
    completedCallback: (Int) -> Unit,
    back: () -> Unit,
    replay: () -> Unit,
    map: () -> Unit,
    next: () -> Unit,
    claimChapter: () -> Unit,
    chapterChestClaimed: Boolean,
) {
    val level = levels[levelIndex]
    val saved = remember(levelIndex) { progressStore.load(levelIndex) }
    val haptics = LocalHapticFeedback.current
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 60) }
    DisposableEffect(Unit) { onDispose { tone.release() } }

    var wheel by remember { mutableStateOf(level.letters) }
    var found by remember { mutableStateOf(saved.foundWords.intersect(level.targets.toSet())) }
    var bonus by remember { mutableStateOf(saved.bonusWords.intersect(level.bonusWords)) }
    var hintCells by remember { mutableStateOf(saved.hintedCells.mapNotNull(VCell::decode).toSet()) }
    var hints by remember { mutableIntStateOf(saved.hintsUsed) }
    var selection by remember { mutableStateOf(emptyList<Int>()) }
    var live by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(if (saved.foundWords.isNotEmpty() || saved.hintedCells.isNotEmpty()) "Kaldığın yerden devam" else "Harfleri birleştir") }
    var combo by remember { mutableIntStateOf(0) }
    var handled by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var net by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf(WheelFeedback.NORMAL) }
    var recentWord by remember { mutableStateOf<String?>(null) }
    var revealStep by remember { mutableIntStateOf(Int.MAX_VALUE) }
    var lastHintCell by remember { mutableStateOf<VCell?>(null) }
    val shake = remember { Animatable(0f) }

    fun persist(newFound: Set<String> = found, newBonus: Set<String> = bonus, newCells: Set<VCell> = hintCells, newHints: Int = hints) {
        progressStore.save(levelIndex, SavedLevelProgress(newFound, newBonus, newCells.map(VCell::encode).toSet(), newHints))
    }

    LaunchedEffect(recentWord) {
        val word = recentWord ?: return@LaunchedEffect
        revealStep = 0
        if (reduceMotion) revealStep = word.length else {
            repeat(word.length) { i -> delay(65); revealStep = i + 1 }
        }
        delay(if (reduceMotion) 0 else 140)
        recentWord = null
        revealStep = Int.MAX_VALUE
    }
    LaunchedEffect(lastHintCell) {
        if (lastHintCell != null) { delay(if (reduceMotion) 0 else 700); lastHintCell = null }
    }
    LaunchedEffect(feedback) {
        if (feedback == WheelFeedback.WRONG && !reduceMotion) {
            shake.snapTo(0f)
            shake.animateTo(8f, tween(45)); shake.animateTo(-8f, tween(55)); shake.animateTo(5f, tween(45)); shake.animateTo(0f, tween(55))
        }
        if (feedback != WheelFeedback.NORMAL) { delay(if (reduceMotion) 80 else 330); feedback = WheelFeedback.NORMAL }
    }

    val done = level.targets.all { it in found }
    val earned = when (hints) { 0 -> 3; 1 -> 2; 2 -> 1; else -> 0 }
    val chapter = vChapters()[levelIndex / 5]
    val chapterEnd = levelIndex == chapter.endExclusive - 1
    LaunchedEffect(done) {
        if (done && !handled) {
            handled = true
            if (!completedBefore) completedCallback(earned) else if (earned > previousStars) completedCallback(earned)
            if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_ACK, 160)
            if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (!reduceMotion) delay(260)
            showResult = true
        }
    }

    val wordScale by animateFloatAsState(if (live.isNotBlank()) 1.025f else 1f, tween(if (reduceMotion) 0 else 100), label = "wordScale")
    val rewardEligible = !completedBefore

    VSurface {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxHeight < 760.dp
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
                    VIconButton(Icons.Filled.ArrowBack, back); Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Level ${levelIndex + 1}", color = V15Ui.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(level.title, color = V15Ui.Muted, fontSize = 11.sp) }
                    VChip(Icons.Filled.AutoAwesome, coins.toString(), V15Ui.Gold)
                }
                Spacer(Modifier.height(if (compact) 4.dp else 8.dp))
                VBoard(level, found, hintCells, compact, recentWord, revealStep, lastHintCell, reduceMotion)
                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${found.size}/${level.targets.size} kelime", color = V15Ui.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    if (combo >= 2) Text("Combo x$combo", color = V15Ui.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp) else Row { repeat(3) { s -> Icon(if (s < earned) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (s < earned) V15Ui.Gold else Color(0xFFCDD2DA), modifier = Modifier.size(16.dp)) } }
                }
                Spacer(Modifier.height(7.dp))
                Box(Modifier.fillMaxWidth().height(44.dp).scale(wordScale).clip(RoundedCornerShape(13.dp)).background(if (live.isNotBlank()) V15Ui.PrimarySoft else V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                    Text(if (live.isNotBlank()) live else status, color = if (live.isNotBlank()) V15Ui.PrimaryDark else V15Ui.Muted, fontWeight = if (live.isNotBlank()) FontWeight.Bold else FontWeight.Medium, fontSize = if (live.isNotBlank()) 19.sp else 12.sp)
                }
                Spacer(Modifier.height(if (compact) 1.dp else 5.dp))
                Box(Modifier.fillMaxWidth().offset(x = shake.value.dp), contentAlignment = Alignment.Center) {
                    VWheel(
                        letters = wheel,
                        selected = selection,
                        sizeDp = if (compact) 214.dp else 244.dp,
                        feedback = feedback,
                        onStep = {
                            if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onChanged = { selection = it; live = it.joinToString("") { idx -> wheel[idx].toString() } },
                        onFinished = {
                            val submitted = live.trim().uppercase(vTr)
                            when {
                                submitted in level.targets && submitted !in found -> {
                                    val nf = found + submitted
                                    found = nf; persist(newFound = nf)
                                    recentWord = submitted
                                    feedback = WheelFeedback.CORRECT
                                    combo++
                                    if (rewardEligible) {
                                        wordFound(); comboChanged(combo)
                                        val extra = if (combo % 3 == 0) 10 else 0
                                        val gain = 5 + extra
                                        net += gain; coinsChanged(coins + gain)
                                        status = if (extra > 0) "Combo x$combo · +$gain" else "Doğru · $submitted"
                                    } else status = "Doğru · $submitted"
                                    if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 90)
                                }
                                submitted in found -> status = "Zaten bulundu"
                                submitted in level.bonusWords && submitted !in bonus -> {
                                    val nb = bonus + submitted
                                    bonus = nb; persist(newBonus = nb)
                                    feedback = WheelFeedback.BONUS
                                    combo++
                                    if (rewardEligible) { wordFound(); comboChanged(combo); net += 10; coinsChanged(coins + 10); status = "Bonus kelime · +10" } else status = "Bonus kelime"
                                }
                                submitted.isNotBlank() -> {
                                    combo = 0; feedback = WheelFeedback.WRONG; status = "Bu levelde yok"
                                    if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_NACK, 80)
                                }
                            }
                            selection = emptyList(); live = ""
                        },
                        onCancelled = { selection = emptyList(); live = "" },
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VTool(Icons.Filled.Shuffle, "Karıştır", true, Modifier.weight(1f)) { wheel = wheel.shuffled(); status = "Harfler karıştırıldı" }
                    VTool(Icons.Filled.Lightbulb, "Harf 25", coins >= 25 && !done, Modifier.weight(1f)) {
                        val hidden = vAllCells(level).filter { it !in hintCells && !vRevealedByFound(level, it, found) }
                        hidden.randomOrNull()?.let { cell ->
                            val nc = hintCells + cell; val nh = hints + 1
                            hintCells = nc; hints = nh; lastHintCell = cell; persist(newCells = nc, newHints = nh)
                            coinsChanged(coins - 25); net -= 25; combo = 0; hintUsed(); status = "Bir harf açıldı"
                        }
                    }
                    VTool(Icons.Filled.Search, "Kelime 60", coins >= 60 && !done, Modifier.weight(1f)) {
                        level.targets.firstOrNull { it !in found }?.let { word ->
                            val nf = found + word; val nh = hints + 2
                            found = nf; hints = nh; recentWord = word; persist(newFound = nf, newHints = nh)
                            coinsChanged(coins - 60); net -= 60; combo = 0; hintUsed(); status = "Kelime açıldı"
                        }
                    }
                }
            }
            if (showResult) VResult(levelIndex, earned, previousStars, found.size, level.targets.size, bonus.size, hints, combo, net, chapterEnd, chapterChestClaimed, !completedBefore, reduceMotion,
                replay = { progressStore.clear(levelIndex); replay() }, map = map, next = next, claimChapter = claimChapter)
        }
    }
}

@Composable private fun VTool(icon: ImageVector, label: String, enabled: Boolean, modifier: Modifier = Modifier, click: () -> Unit) {
    Column(modifier.height(58.dp).clip(RoundedCornerShape(15.dp)).background(V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(15.dp)).clickable(enabled = enabled, onClick = click).padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = if (enabled) V15Ui.Primary else V15Ui.Subtle, modifier = Modifier.size(20.dp)); Text(label, color = if (enabled) V15Ui.Ink else V15Ui.Subtle, fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
    }
}

private fun vCellsFor(p: Placement) = p.word.mapIndexed { i, ch -> VCell(p.row + if (p.horizontal) 0 else i, p.col + if (p.horizontal) i else 0) to ch }
private fun vAllCells(level: Level) = level.placements.flatMap(::vCellsFor).map { it.first }.toSet()
private fun vRevealedByFound(level: Level, cell: VCell, found: Set<String>, excludingWord: String? = null): Boolean = level.placements.any { p -> p.word != excludingWord && p.word in found && vCellsFor(p).any { it.first == cell } }
private fun vRecentWordIndex(level: Level, word: String?, cell: VCell): Int? {
    if (word == null) return null
    val p = level.placements.firstOrNull { it.word == word } ?: return null
    return vCellsFor(p).indexOfFirst { it.first == cell }.takeIf { it >= 0 }
}

@Composable
private fun VBoard(level: Level, found: Set<String>, hinted: Set<VCell>, compact: Boolean, recentWord: String?, revealStep: Int, lastHint: VCell?, reduceMotion: Boolean) {
    val validated = remember(level) { validateLevel(level) }
    val map = remember(level) { validated.lettersByCell.mapKeys { VCell(it.key.row, it.key.col) } }
    val rows = (map.keys.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (map.keys.maxOfOrNull { it.col } ?: 0) + 1
    val size = when { compact && cols >= 5 -> 33.dp; cols >= 6 -> 35.dp; cols == 5 -> 39.dp; else -> 42.dp }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(20.dp)).padding(if (compact) 8.dp else 11.dp), contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { r ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(cols) { c ->
                        val cell = VCell(r, c); val letter = map[cell]
                        if (letter == null) Spacer(Modifier.size(size)) else {
                            val recentIndex = vRecentWordIndex(level, recentWord, cell)
                            val revealFromRecent = recentIndex != null && recentIndex < revealStep
                            val reveal = cell in hinted || vRevealedByFound(level, cell, found, recentWord) || revealFromRecent
                            val isHint = cell in hinted
                            var appeared by remember(cell) { mutableStateOf(reveal) }
                            LaunchedEffect(reveal) {
                                if (reveal && !appeared) { if (!reduceMotion) delay(12); appeared = true }
                                if (!reveal) appeared = false
                            }
                            val scale by animateFloatAsState(if (appeared) 1f else .78f, tween(if (reduceMotion) 0 else 150), label = "cellScale")
                            val bg = when { cell == lastHint -> V15Ui.GoldSoft; isHint -> Color(0xFFFFFAEC); reveal -> V15Ui.PrimarySoft; else -> Color(0xFFF0F3F8) }
                            val border = when { cell == lastHint -> V15Ui.Gold; isHint -> V15Ui.Gold.copy(alpha = .55f); reveal -> V15Ui.Primary.copy(alpha = .45f); else -> Color(0xFFD8DEE8) }
                            Box(Modifier.size(size).scale(scale).clip(RoundedCornerShape(8.dp)).background(bg).border(1.dp, border, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                if (reveal) Text(letter.toString(), color = if (isHint) Color(0xFF9A6700) else V15Ui.PrimaryDark, fontWeight = FontWeight.Black, fontSize = if (compact) 15.sp else 17.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VWheel(
    letters: List<Char>, selected: List<Int>, sizeDp: Dp, feedback: WheelFeedback,
    onStep: () -> Unit, onChanged: (List<Int>) -> Unit, onFinished: () -> Unit, onCancelled: () -> Unit,
) {
    var measured by remember { mutableStateOf(IntSize.Zero) }
    var pointer by remember { mutableStateOf<Offset?>(null) }
    fun positions(): List<Offset> {
        if (measured == IntSize.Zero) return emptyList()
        val center = Offset(measured.width / 2f, measured.height / 2f); val radius = minOf(measured.width, measured.height) * .34f
        return letters.indices.map { i -> val a = -PI / 2 + 2 * PI * i / letters.size; Offset(center.x + (cos(a) * radius).toFloat(), center.y + (sin(a) * radius).toFloat()) }
    }
    fun at(p: Offset): Int? {
        val pts = positions(); val hit = minOf(measured.width, measured.height) * .145f
        return pts.indices.minByOrNull { i -> hypot((p.x - pts[i].x).toDouble(), (p.y - pts[i].y).toDouble()) }?.takeIf { i -> hypot((p.x - pts[i].x).toDouble(), (p.y - pts[i].y).toDouble()) <= hit }
    }
    val lineColor = when (feedback) { WheelFeedback.CORRECT -> V15Ui.Success; WheelFeedback.BONUS -> V15Ui.Gold; WheelFeedback.WRONG -> V15Ui.Danger; WheelFeedback.NORMAL -> V15Ui.Primary }
    Box(Modifier.size(sizeDp).onSizeChanged { measured = it }.pointerInput(letters, measured) {
        var gesture = emptyList<Int>()
        detectDragGestures(
            onDragStart = { p -> pointer = p; val i = at(p); gesture = if (i != null) listOf(i) else emptyList(); if (i != null) onStep(); onChanged(gesture) },
            onDragEnd = { pointer = null; if (gesture.isNotEmpty()) onFinished() else onCancelled(); gesture = emptyList() },
            onDragCancel = { pointer = null; onCancelled(); gesture = emptyList() },
        ) { change, _ ->
            pointer = change.position
            val i = at(change.position)
            if (i != null) {
                when {
                    gesture.size >= 2 && i == gesture[gesture.lastIndex - 1] -> { gesture = gesture.dropLast(1); onStep(); onChanged(gesture) }
                    i !in gesture -> { gesture = gesture + i; onStep(); onChanged(gesture) }
                }
            }
        }
    }) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2); val radius = minOf(size.width, size.height) * .47f
            drawCircle(V15Ui.Surface, radius, center); drawCircle(Color(0xFFE3E7EE), radius, center, style = Stroke(3f))
            val pts = positions()
            selected.zipWithNext().forEach { (a, b) -> if (a in pts.indices && b in pts.indices) drawLine(lineColor, pts[a], pts[b], 9f, cap = StrokeCap.Round) }
            if (selected.isNotEmpty() && pointer != null && selected.last() in pts.indices) drawLine(lineColor.copy(alpha = .62f), pts[selected.last()], pointer!!, 7f, cap = StrokeCap.Round)
            pts.forEachIndexed { i, p ->
                val sel = i in selected; val r = minOf(size.width, size.height) * (if (sel) .116f else .101f)
                drawCircle(if (sel) lineColor else Color(0xFFF8F9FB), r, p); drawCircle(if (sel) lineColor else Color(0xFFD2D8E1), r, p, style = Stroke(2.4f))
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply { color = if (sel) android.graphics.Color.WHITE else android.graphics.Color.rgb(23, 32, 51); textAlign = android.graphics.Paint.Align.CENTER; textSize = minOf(size.width, size.height) * .104f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
                    val m = paint.fontMetrics; canvas.nativeCanvas.drawText(letters[i].toString(), p.x, p.y - (m.ascent + m.descent) / 2, paint)
                }
            }
        }
    }
}

@Composable private fun VResult(levelIndex: Int, stars: Int, previousStars: Int, found: Int, target: Int, bonus: Int, hints: Int, combo: Int, net: Int, chapterEnd: Boolean, chestClaimed: Boolean, firstCompletion: Boolean, reduceMotion: Boolean, replay: () -> Unit, map: () -> Unit, next: () -> Unit, claimChapter: () -> Unit) {
    var visibleStars by remember { mutableIntStateOf(if (reduceMotion) stars else 0) }
    LaunchedEffect(stars, reduceMotion) { if (!reduceMotion) repeat(stars) { delay(240); visibleStars = it + 1 } }
    val newRecord = stars > previousStars
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .38f)).padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
        VCard(Modifier.fillMaxWidth()) {
            Text(if (chapterEnd) "Bölüm tamamlandı" else "Level tamamlandı", color = V15Ui.Ink, fontWeight = FontWeight.Black, fontSize = 24.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { repeat(3) { i -> Icon(if (i < visibleStars) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (i < visibleStars) V15Ui.Gold else Color(0xFFD0D5DD), modifier = Modifier.size(if (i == 1) 42.dp else 34.dp)) } }
            Spacer(Modifier.height(8.dp)); Text(when { newRecord -> "Yeni en iyi skor"; firstCompletion -> "Yeni level açıldı"; else -> "En iyi skor: $previousStars yıldız" }, color = if (newRecord || firstCompletion) V15Ui.Primary else V15Ui.Muted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp)); VResultRow("Bulunan kelime", "$found/$target"); VResultRow("Bonus kelime", bonus.toString()); VResultRow("Kullanılan ipucu", hints.toString()); VResultRow("Combo", "x$combo")
            HorizontalDivider(color = V15Ui.Border, modifier = Modifier.padding(vertical = 10.dp)); Row(Modifier.fillMaxWidth()) { Text("Bu oturumdaki net jeton", color = V15Ui.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(if (net >= 0) "+$net" else net.toString(), color = if (net >= 0) V15Ui.Success else V15Ui.Danger, fontWeight = FontWeight.Black, fontSize = 21.sp) }
            if (chapterEnd && !chestClaimed) { Spacer(Modifier.height(12.dp)); Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(V15Ui.GoldSoft).clickable(onClick = claimChapter).padding(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.CardGiftcard, null, tint = V15Ui.Gold); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("Bölüm ödülü", color = V15Ui.Ink, fontWeight = FontWeight.Bold); Text("+150 jeton", color = V15Ui.Muted, fontSize = 11.sp) }; Text("Al", color = V15Ui.Gold, fontWeight = FontWeight.Black) } } }
            Spacer(Modifier.height(16.dp)); VPrimary(if (chapterEnd) "Haritaya dön" else "Sonraki level", if (chapterEnd) map else next, Modifier.fillMaxWidth(), if (chapterEnd) Icons.Filled.Map else Icons.Filled.PlayArrow)
            Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { VSecondary("Tekrar oyna", replay, Modifier.weight(1f), Icons.Filled.Refresh); if (!chapterEnd) VSecondary("Harita", map, Modifier.weight(1f), Icons.Filled.Map) }
        }
    }
}
@Composable private fun VResultRow(label: String, value: String) { Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) { Text(label, color = V15Ui.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text(value, color = V15Ui.Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }

@Composable private fun VDaily(streak: Int, available: Boolean, back: () -> Unit, claim: () -> Unit) {
    VSurface { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { VTopBar("Günlük ödül", back); Text("Serini koru ve ödülleri büyüt", color = V15Ui.Muted, fontSize = 13.sp); Spacer(Modifier.height(22.dp)); VCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).background(V15Ui.GoldSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.LocalFireDepartment, null, tint = V15Ui.Gold) }; Spacer(Modifier.width(12.dp)); Column { Text("$streak günlük seri", color = V15Ui.Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text("Bugünkü ödül +40 jeton", color = V15Ui.Muted, fontSize = 12.sp) } } }; Spacer(Modifier.weight(1f)); VPrimary(if (available) "Bugünün ödülünü al" else "Bugünkü ödül alındı", claim, Modifier.fillMaxWidth().padding(bottom = 24.dp), Icons.Filled.CardGiftcard, available) } }
}
@Composable private fun VTasks(stats: VStats, claimed: Set<String>, back: () -> Unit, claim: (VTask) -> Unit, home: () -> Unit, chapters: () -> Unit, profile: () -> Unit) {
    VSurface { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 86.dp)) { VTopBar("Görevler", back); Text("Oynadıkça ilerle, tamamladıkça kazan", color = V15Ui.Muted, fontSize = 13.sp); Spacer(Modifier.height(14.dp)); Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) { vTasks().forEach { t -> val current = t.current(stats).coerceAtMost(t.goal); val done = current >= t.goal; val got = t.id in claimed; VCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).background(if (done) V15Ui.SuccessSoft else V15Ui.PrimarySoft, CircleShape), contentAlignment = Alignment.Center) { Icon(if (done) Icons.Filled.CheckCircle else Icons.Filled.TaskAlt, null, tint = if (done) V15Ui.Success else V15Ui.Primary, modifier = Modifier.size(19.dp)) }; Spacer(Modifier.width(12.dp)); Text(t.title, color = V15Ui.Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("+${t.reward}", color = V15Ui.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp) }; Spacer(Modifier.height(11.dp)); LinearProgressIndicator(progress = { current.toFloat() / t.goal.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = if (done) V15Ui.Success else V15Ui.Primary, trackColor = Color(0xFFF0F2F5)); Spacer(Modifier.height(7.dp)); Row(Modifier.fillMaxWidth()) { Text("$current/${t.goal}", color = V15Ui.Muted, fontSize = 10.sp, modifier = Modifier.weight(1f)); Text(if (got) "Alındı" else if (done) "Ödülü al" else "Devam ediyor", color = if (done && !got) V15Ui.Primary else V15Ui.Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.clickable(enabled = done && !got) { claim(t) }) } }; Spacer(Modifier.height(11.dp)) } } }; Box(Modifier.align(Alignment.BottomCenter)) { VBottom(VScreen.TASKS, home, chapters, {}, profile) } } }
}
@Composable private fun VProfile(coins: Int, stats: VStats, back: () -> Unit, home: () -> Unit, chapters: () -> Unit, tasks: () -> Unit, settings: () -> Unit) {
    val level = 1 + stats.stars / 15; val xp = (stats.stars % 15) / 15f
    VSurface { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 86.dp)) { VTopBar("Profil", back) { VIconButton(Icons.Filled.Settings, settings) }; Spacer(Modifier.height(12.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(68.dp).background(V15Ui.PrimarySoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, tint = V15Ui.Primary, modifier = Modifier.size(32.dp)) }; Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text("Oyuncu", color = V15Ui.Ink, fontWeight = FontWeight.Black, fontSize = 21.sp); Text("Seviye $level", color = V15Ui.Muted, fontSize = 12.sp); Spacer(Modifier.height(7.dp)); LinearProgressIndicator(progress = { xp }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = V15Ui.Primary, trackColor = Color(0xFFF0F2F5)) } }; Spacer(Modifier.height(22.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { VStat("Yıldız", stats.stars.toString(), Icons.Filled.Star, V15Ui.Gold, Modifier.weight(1f)); VStat("Level", stats.completed.toString(), Icons.Filled.CheckCircle, V15Ui.Success, Modifier.weight(1f)); VStat("Jeton", coins.toString(), Icons.Filled.AutoAwesome, V15Ui.Primary, Modifier.weight(1f)) }; Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { VStat("Kelime", stats.words.toString(), Icons.Filled.TaskAlt, V15Ui.Primary, Modifier.weight(1f)); VStat("İpucu", stats.hints.toString(), Icons.Filled.Lightbulb, V15Ui.Gold, Modifier.weight(1f)); VStat("Combo", "x${stats.combo}", Icons.Filled.LocalFireDepartment, V15Ui.Danger, Modifier.weight(1f)) } }; Box(Modifier.align(Alignment.BottomCenter)) { VBottom(VScreen.PROFILE, home, chapters, tasks, {}) } } }
}
@Composable private fun VStat(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) { Column(modifier.height(104.dp).clip(RoundedCornerShape(17.dp)).background(V15Ui.Surface).border(1.dp, V15Ui.Border, RoundedCornerShape(17.dp)).padding(13.dp), verticalArrangement = Arrangement.SpaceBetween) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)); Column { Text(value, color = V15Ui.Ink, fontWeight = FontWeight.Black, fontSize = 19.sp); Text(label, color = V15Ui.Muted, fontSize = 10.sp) } } }
@Composable private fun VSettings(sound: Boolean, haptic: Boolean, motion: Boolean, back: () -> Unit, onSound: (Boolean) -> Unit, onHaptic: (Boolean) -> Unit, onMotion: (Boolean) -> Unit) {
    VSurface { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { VTopBar("Ayarlar", back); Spacer(Modifier.height(14.dp)); VSetting(Icons.Filled.VolumeUp, "Ses efektleri", sound, onSound); VSetting(Icons.Filled.Vibration, "Titreşim", haptic, onHaptic); VSetting(Icons.Filled.Refresh, "Animasyonları azalt", motion, onMotion); Spacer(Modifier.height(18.dp)); Text("Sürüm ${BuildConfig.VERSION_NAME}", color = V15Ui.Subtle, fontSize = 11.sp) } }
}
@Composable private fun VSetting(icon: ImageVector, title: String, value: Boolean, change: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth().height(62.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).background(V15Ui.PrimarySoft, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = V15Ui.Primary, modifier = Modifier.size(18.dp)) }; Spacer(Modifier.width(12.dp)); Text(title, color = V15Ui.Ink, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)); Switch(checked = value, onCheckedChange = change) }; HorizontalDivider(color = V15Ui.Border) }
