package com.zafer.wordz

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.weight
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

private object RUi {
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
    val R16 = 16.dp
    val R20 = 20.dp
    val ScreenPad = 20.dp
}

private const val RPREFS = "wordz_progress"
private const val R_UNLOCKED = "unlocked_level"
private const val R_COINS = "coins"
private const val R_COMPLETED = "completed_levels"
private const val R_STARS = "level_stars"
private const val R_DAILY_DATE = "daily_reward_date"
private const val R_STREAK = "daily_streak"
private const val R_SOUND = "sound_enabled"
private const val R_HAPTIC = "haptic_enabled"
private const val R_MOTION = "reduce_motion"
private const val R_CHAPTER_CHESTS = "claimed_chapter_chests"
private const val R_TASKS = "claimed_achievements"
private const val R_WORDS = "stats_words"
private const val R_HINTS = "stats_hints"
private const val R_COMBO = "stats_best_combo"
private val rTr = Locale("tr", "TR")

private data class RCell(val row: Int, val col: Int)
private data class RChapter(val name: String, val subtitle: String, val start: Int, val endExclusive: Int, val accent: Color, val soft: Color)
private data class RStats(val completed: Int, val stars: Int, val words: Int, val hints: Int, val combo: Int, val streak: Int)
private data class RTask(val id: String, val title: String, val reward: Int, val current: (RStats) -> Int, val goal: Int)
private enum class RScreen { HOME, CHAPTERS, MAP, GAME, DAILY, TASKS, PROFILE, SETTINGS }

class RefinedMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = RUi.Primary,
                    secondary = RUi.Gold,
                    background = RUi.Background,
                    surface = RUi.Surface,
                    onPrimary = Color.White,
                    onBackground = RUi.Ink,
                    onSurface = RUi.Ink,
                )
            ) { RefinedWordZApp() }
        }
    }
}

private fun rChapters(): List<RChapter> {
    val names = listOf("Başlangıç", "Sahil", "Orman", "Çöl", "Gece", "Uzak Diyarlar", "Zirve")
    val subs = listOf("Temelleri keşfet", "Ritmi yakala", "Yeni yollar aç", "Zorlu kelimelere geç", "Gece rotasını tamamla", "Uzak diyarlara ilerle", "Son zirve")
    val accents = listOf(Color(0xFF3976F6), Color(0xFF0E9FB5), Color(0xFF2A9D63), Color(0xFFD98A25), Color(0xFF6A62D8), Color(0xFF248A89), Color(0xFF5A57C8))
    val softs = listOf(Color(0xFFEEF4FF), Color(0xFFEAF8FA), Color(0xFFECF8F1), Color(0xFFFFF4E6), Color(0xFFF1F0FF), Color(0xFFECF8F7), Color(0xFFF0F0FF))
    return levels.indices.chunked(5).mapIndexed { i, chunk -> RChapter(names.getOrElse(i) { "Bölüm ${i + 1}" }, subs.getOrElse(i) { "Yeni kelimeler" }, chunk.first(), chunk.last() + 1, accents[i % accents.size], softs[i % softs.size]) }
}

private fun rDecodeStars(raw: Set<String>): Map<Int, Int> = raw.mapNotNull {
    val p = it.split(':'); val l = p.getOrNull(0)?.toIntOrNull(); val s = p.getOrNull(1)?.toIntOrNull()
    if (l != null && s != null && l in levels.indices) l to s.coerceIn(0, 3) else null
}.toMap()
private fun rEncodeStars(stars: Map<Int, Int>) = stars.map { "${it.key}:${it.value}" }.toSet()
private fun rTasks() = listOf(
    RTask("first", "İlk leveli tamamla", 30, { it.completed }, 1),
    RTask("stars15", "15 yıldız topla", 75, { it.stars }, 15),
    RTask("words50", "50 kelime bul", 100, { it.words }, 50),
    RTask("combo5", "Combo x5 yap", 80, { it.combo }, 5),
    RTask("streak3", "3 günlük seri yap", 100, { it.streak }, 3),
)

@Composable
private fun RefinedWordZApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(RPREFS, Context.MODE_PRIVATE) }
    val initialUnlocked = prefs.getInt(R_UNLOCKED, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(R_COMPLETED, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
    val migratedCompleted = if (savedCompleted.isEmpty() && initialUnlocked > 0) (0 until initialUnlocked).toSet() else savedCompleted
    val starSeed = rDecodeStars(prefs.getStringSet(R_STARS, emptySet()).orEmpty()).toMutableMap().apply { migratedCompleted.forEach { if (it !in this) this[it] = 1 } }

    var unlocked by remember { mutableIntStateOf(initialUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(R_COINS, 100)) }
    var completed by remember { mutableStateOf(migratedCompleted) }
    var stars by remember { mutableStateOf(starSeed.toMap()) }
    var dailyDate by remember { mutableStateOf(prefs.getString(R_DAILY_DATE, "").orEmpty()) }
    var streak by remember { mutableIntStateOf(prefs.getInt(R_STREAK, 0)) }
    var sound by remember { mutableStateOf(prefs.getBoolean(R_SOUND, true)) }
    var haptic by remember { mutableStateOf(prefs.getBoolean(R_HAPTIC, true)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean(R_MOTION, false)) }
    var chapterChests by remember { mutableStateOf(prefs.getStringSet(R_CHAPTER_CHESTS, emptySet()).orEmpty()) }
    var claimedTasks by remember { mutableStateOf(prefs.getStringSet(R_TASKS, emptySet()).orEmpty()) }
    var words by remember { mutableIntStateOf(prefs.getInt(R_WORDS, 0)) }
    var hints by remember { mutableIntStateOf(prefs.getInt(R_HINTS, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(R_COMBO, 0)) }
    var screen by remember { mutableStateOf(RScreen.HOME) }
    var selectedChapter by remember { mutableIntStateOf((unlocked / 5).coerceIn(0, rChapters().lastIndex)) }
    var selectedLevel by remember { mutableIntStateOf(unlocked) }
    var gameSession by remember { mutableIntStateOf(0) }

    val today = LocalDate.now()
    val totalStars = stars.values.sum()
    val stats = RStats(completed.size, totalStars, words, hints, bestCombo, streak)
    val dailyAvailable = dailyDate != today.toString()

    fun save(
        u: Int = unlocked, c: Int = coins, comp: Set<Int> = completed, st: Map<Int, Int> = stars,
        date: String = dailyDate, str: Int = streak, snd: Boolean = sound, hap: Boolean = haptic,
        motion: Boolean = reduceMotion, chests: Set<String> = chapterChests, tasks: Set<String> = claimedTasks,
        w: Int = words, h: Int = hints, combo: Int = bestCombo,
    ) {
        prefs.edit().putInt(R_UNLOCKED, u).putInt(R_COINS, c)
            .putStringSet(R_COMPLETED, comp.map(Int::toString).toSet()).putStringSet(R_STARS, rEncodeStars(st))
            .putString(R_DAILY_DATE, date).putInt(R_STREAK, str).putBoolean(R_SOUND, snd).putBoolean(R_HAPTIC, hap)
            .putBoolean(R_MOTION, motion).putStringSet(R_CHAPTER_CHESTS, chests).putStringSet(R_TASKS, tasks)
            .putInt(R_WORDS, w).putInt(R_HINTS, h).putInt(R_COMBO, combo).apply()
    }
    fun openLevel(index: Int) { selectedLevel = index; selectedChapter = (index / 5).coerceIn(0, rChapters().lastIndex); gameSession++; screen = RScreen.GAME }

    when (screen) {
        RScreen.HOME -> RHome(coins, totalStars, unlocked, completed.size, streak, dailyAvailable, rTasks().any { it.current(stats) >= it.goal && it.id !in claimedTasks }, { openLevel(unlocked) }, { screen = RScreen.CHAPTERS }, { screen = RScreen.DAILY }, { screen = RScreen.TASKS }, { screen = RScreen.PROFILE }, { screen = RScreen.SETTINGS })
        RScreen.CHAPTERS -> RChapters(unlocked, stars, completed, { screen = RScreen.HOME }, { selectedChapter = it; screen = RScreen.MAP }, { screen = RScreen.HOME }, { screen = RScreen.TASKS }, { screen = RScreen.PROFILE })
        RScreen.MAP -> RMap(selectedChapter, unlocked, stars, completed, { screen = RScreen.CHAPTERS }, { openLevel(it) })
        RScreen.GAME -> key(gameSession) {
            RGame(
                selectedLevel, coins, stars[selectedLevel] ?: 0, selectedLevel !in completed, sound, haptic, reduceMotion,
                { newCoins -> coins = newCoins; save(c = newCoins) },
                { val v = words + 1; words = v; save(w = v) },
                { val v = hints + 1; hints = v; save(h = v) },
                { v -> if (v > bestCombo) { bestCombo = v; save(combo = v) } },
                { earned ->
                    val first = selectedLevel !in completed
                    val newCompleted = completed + selectedLevel
                    val oldStars = stars[selectedLevel] ?: 0
                    val newStars = if (earned > oldStars) stars + (selectedLevel to earned) else stars
                    val newUnlocked = if (selectedLevel >= unlocked && unlocked < levels.lastIndex) selectedLevel + 1 else unlocked
                    val newCoins = coins + if (first) 25 else 0
                    completed = newCompleted; stars = newStars; unlocked = newUnlocked; coins = newCoins
                    save(u = newUnlocked, c = newCoins, comp = newCompleted, st = newStars)
                },
                { screen = RScreen.MAP }, { gameSession++ }, { screen = RScreen.MAP },
                {
                    val chapter = rChapters()[selectedChapter]
                    if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = RScreen.MAP
                },
                {
                    val chapter = rChapters()[selectedChapter]; val key = selectedChapter.toString()
                    val done = (chapter.start until chapter.endExclusive).all { it in completed || it == selectedLevel }
                    if (done && key !in chapterChests) { val nc = coins + 150; val ns = chapterChests + key; coins = nc; chapterChests = ns; save(c = nc, chests = ns) }
                }, selectedChapter.toString() in chapterChests,
            )
        }
        RScreen.DAILY -> RDaily(streak, dailyAvailable, { screen = RScreen.HOME }) {
            if (dailyAvailable) { val ns = if (dailyDate == today.minusDays(1).toString()) streak + 1 else 1; val nc = coins + 40; val nd = today.toString(); streak = ns; coins = nc; dailyDate = nd; save(c = nc, date = nd, str = ns) }
        }
        RScreen.TASKS -> RTasks(stats, claimedTasks, { screen = RScreen.HOME }, { task ->
            if (task.current(stats) >= task.goal && task.id !in claimedTasks) { val nt = claimedTasks + task.id; val nc = coins + task.reward; claimedTasks = nt; coins = nc; save(c = nc, tasks = nt) }
        }, { screen = RScreen.HOME }, { screen = RScreen.CHAPTERS }, { screen = RScreen.PROFILE })
        RScreen.PROFILE -> RProfile(coins, stats, { screen = RScreen.HOME }, { screen = RScreen.HOME }, { screen = RScreen.CHAPTERS }, { screen = RScreen.TASKS }, { screen = RScreen.SETTINGS })
        RScreen.SETTINGS -> RSettings(sound, haptic, reduceMotion, { screen = RScreen.PROFILE }, { sound = it; save(snd = it) }, { haptic = it; save(hap = it) }, { reduceMotion = it; save(motion = it) })
    }
}

@Composable private fun RSurface(content: @Composable () -> Unit) { Box(Modifier.fillMaxSize().background(RUi.Background).statusBarsPadding()) { content() } }
@Composable private fun RTopBar(title: String? = null, back: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) { RIconButton(Icons.Filled.ArrowBack, back); Spacer(Modifier.width(12.dp)) }
        if (title != null) Text(title, color = RUi.Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}
@Composable private fun RIconButton(icon: ImageVector, onClick: () -> Unit, badge: Boolean = false) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(RUi.Surface).border(1.dp, RUi.Border, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = RUi.Ink, modifier = Modifier.size(20.dp)); if (badge) Box(Modifier.align(Alignment.TopEnd).size(9.dp).background(RUi.Danger, CircleShape))
    }
}
@Composable private fun RChip(icon: ImageVector, value: String, tint: Color) {
    Row(Modifier.clip(RoundedCornerShape(14.dp)).background(RUi.Surface).border(1.dp, RUi.Border, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp)); Text(value, color = RUi.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
@Composable private fun RCard(modifier: Modifier = Modifier, content: @Composable Column.() -> Unit) {
    Column(modifier.shadow(5.dp, RoundedCornerShape(RUi.R20), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000)).clip(RoundedCornerShape(RUi.R20)).background(RUi.Surface).border(1.dp, RUi.Border.copy(alpha = .7f), RoundedCornerShape(RUi.R20)).padding(18.dp), content = content)
}
@Composable private fun RPrimary(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    Row(modifier.height(54.dp).clip(RoundedCornerShape(RUi.R16)).background(if (enabled) RUi.Primary else Color(0xFFD5D9E2)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        if (icon != null) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }; Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
@Composable private fun RSecondary(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(modifier.height(46.dp).clip(RoundedCornerShape(14.dp)).background(RUi.Surface).border(1.dp, RUi.Border, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        if (icon != null) { Icon(icon, null, tint = RUi.Primary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)) }; Text(text, color = RUi.Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
@Composable private fun RBottom(active: RScreen, home: () -> Unit, chapters: () -> Unit, tasks: () -> Unit, profile: () -> Unit) {
    Row(Modifier.fillMaxWidth().navigationBarsPadding().height(70.dp).background(RUi.Surface).border(1.dp, RUi.Border, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
        RNav("Ana Sayfa", Icons.Filled.Home, active == RScreen.HOME, home); RNav("Bölümler", Icons.Filled.Map, active == RScreen.CHAPTERS, chapters); RNav("Görevler", Icons.Filled.TaskAlt, active == RScreen.TASKS, tasks); RNav("Profil", Icons.Filled.Person, active == RScreen.PROFILE, profile)
    }
}
@Composable private fun RNav(label: String, icon: ImageVector, active: Boolean, click: () -> Unit) { Column(Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = click).padding(horizontal = 10.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = if (active) RUi.Primary else RUi.Subtle, modifier = Modifier.size(20.dp)); Text(label, color = if (active) RUi.Primary else RUi.Subtle, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium) } }

@Composable private fun RHome(coins: Int, stars: Int, unlocked: Int, completed: Int, streak: Int, daily: Boolean, taskBadge: Boolean, onContinue: () -> Unit, onChapters: () -> Unit, onDaily: () -> Unit, onTasks: () -> Unit, onProfile: () -> Unit, onSettings: () -> Unit) {
    val chapter = rChapters()[(unlocked / 5).coerceIn(0, rChapters().lastIndex)]
    RSurface { Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = RUi.ScreenPad).padding(bottom = 88.dp)) {
            Row(Modifier.fillMaxWidth().height(66.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("WordZ", color = RUi.Ink, fontSize = 28.sp, fontWeight = FontWeight.Black); Text("Kelime bulmaca", color = RUi.Muted, fontSize = 12.sp) }
                RChip(Icons.Filled.AutoAwesome, coins.toString(), RUi.Gold); Spacer(Modifier.width(8.dp)); RChip(Icons.Filled.Star, stars.toString(), RUi.Gold); Spacer(Modifier.width(8.dp)); RIconButton(Icons.Filled.Settings, onSettings)
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(214.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(chapter.accent, chapter.accent.copy(alpha = .82f)))).padding(22.dp)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("DEVAM ET", color = Color.White.copy(alpha = .72f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Spacer(Modifier.height(7.dp)); Text(chapter.name, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black); Text("Level ${unlocked + 1} · ${levels[unlocked].title}", color = Color.White.copy(alpha = .86f), fontSize = 13.sp) }
                    Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { LinearProgressIndicator(progress = { completed.toFloat() / levels.size.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = .24f)); Spacer(Modifier.height(6.dp)); Text("$completed / ${levels.size} tamamlandı", color = Color.White.copy(alpha = .75f), fontSize = 11.sp) }; Spacer(Modifier.width(16.dp)); Box(Modifier.size(50.dp).background(Color.White, CircleShape).clickable(onClick = onContinue), contentAlignment = Alignment.Center) { Icon(Icons.Filled.PlayArrow, null, tint = chapter.accent, modifier = Modifier.size(27.dp)) } }
                }
            }
            Spacer(Modifier.height(18.dp)); Text("Bugün", color = RUi.Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { RQuick("Günlük ödül", if (daily) "+40 hazır" else "Bugün alındı", Icons.Filled.CardGiftcard, RUi.GoldSoft, RUi.Gold, daily, onDaily, Modifier.weight(1f)); RQuick("Görevler", if (taskBadge) "Ödül hazır" else "İlerlemeyi gör", Icons.Filled.TaskAlt, RUi.PrimarySoft, RUi.Primary, taskBadge, onTasks, Modifier.weight(1f)) }
            Spacer(Modifier.height(12.dp)); RCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.LocalFireDepartment, null, tint = RUi.Gold); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("$streak günlük seri", color = RUi.Ink, fontWeight = FontWeight.Bold); Text("Serini koru, ödülleri büyüt", color = RUi.Muted, fontSize = 11.sp) }; Icon(Icons.Filled.ChevronRight, null, tint = RUi.Subtle) } }
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.align(Alignment.BottomCenter)) { RBottom(RScreen.HOME, {}, onChapters, onTasks, onProfile) }
    } }
}
@Composable private fun RQuick(title: String, subtitle: String, icon: ImageVector, bg: Color, tint: Color, badge: Boolean, click: () -> Unit, modifier: Modifier = Modifier) { Column(modifier.height(112.dp).clip(RoundedCornerShape(18.dp)).background(RUi.Surface).border(1.dp, RUi.Border, RoundedCornerShape(18.dp)).clickable(onClick = click).padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Box(Modifier.size(36.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }; if (badge) Box(Modifier.size(8.dp).background(RUi.Danger, CircleShape)) }; Column { Text(title, color = RUi.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(subtitle, color = RUi.Muted, fontSize = 11.sp) } } }

@Composable private fun RChapters(unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, open: (Int) -> Unit, home: () -> Unit, tasks: () -> Unit, profile: () -> Unit) {
    RSurface { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(horizontal = RUi.ScreenPad).padding(bottom = 86.dp)) { RTopBar("Bölümler", back); Text("Her bölüm 5 level", color = RUi.Muted, fontSize = 13.sp); Spacer(Modifier.height(14.dp)); Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) { rChapters().forEachIndexed { i, ch -> val range = ch.start until ch.endExclusive; val openable = unlocked >= ch.start; val done = range.count { it in completed }; val star = range.sumOf { stars[it] ?: 0 }; RChapterCard(i, ch, done, range.count(), star, openable) { open(i) }; Spacer(Modifier.height(11.dp)) } } }; Box(Modifier.align(Alignment.BottomCenter)) { RBottom(RScreen.CHAPTERS, home, {}, tasks, profile) } } }
}
@Composable private fun RChapterCard(index: Int, ch: RChapter, done: Int, total: Int, stars: Int, unlocked: Boolean, click: () -> Unit) { Row(Modifier.fillMaxWidth().height(102.dp).clip(RoundedCornerShape(18.dp)).background(RUi.Surface).border(1.dp, RUi.Border, RoundedCornerShape(18.dp)).clickable(enabled = unlocked, onClick = click).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(if (unlocked) ch.soft else Color(0xFFF1F3F6), CircleShape), contentAlignment = Alignment.Center) { if (unlocked) Text("${index + 1}", color = ch.accent, fontWeight = FontWeight.Black, fontSize = 18.sp) else Icon(Icons.Filled.Lock, null, tint = RUi.Subtle) }; Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(ch.name, color = if (unlocked) RUi.Ink else RUi.Subtle, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(ch.subtitle, color = RUi.Muted, fontSize = 11.sp); Spacer(Modifier.height(7.dp)); LinearProgressIndicator(progress = { done.toFloat() / total.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = if (unlocked) ch.accent else Color(0xFFD5D9E2), trackColor = Color(0xFFF0F2F5)); Spacer(Modifier.height(4.dp)); Text(if (unlocked) "$done/$total · $stars/${total * 3} yıldız" else "Kilitli", color = RUi.Muted, fontSize = 10.sp) }; Spacer(Modifier.width(8.dp)); Icon(Icons.Filled.ChevronRight, null, tint = RUi.Subtle) } }

@Composable private fun RMap(chapterIndex: Int, unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, openLevel: (Int) -> Unit) {
    val ch = rChapters()[chapterIndex]; val indices = (ch.start until ch.endExclusive).toList(); val nodes = listOf(.18f to .82f, .43f to .65f, .73f to .48f, .47f to .31f, .22f to .14f)
    RSurface { Column(Modifier.fillMaxSize().padding(horizontal = RUi.ScreenPad)) { RTopBar("${chapterIndex + 1}. ${ch.name}", back); Row(verticalAlignment = Alignment.CenterVertically) { Text("${indices.count { it in completed }}/${indices.size} tamamlandı", color = RUi.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("${indices.sumOf { stars[it] ?: 0 }} yıldız", color = RUi.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp) }; Spacer(Modifier.height(14.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(ch.soft, RUi.Surface))).border(1.dp, RUi.Border, RoundedCornerShape(26.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                fun segment(a: Pair<Float,Float>, b: Pair<Float,Float>) = Path().apply { val x1=size.width*a.first;val y1=size.height*a.second;val x2=size.width*b.first;val y2=size.height*b.second;val mid=(y1+y2)/2;moveTo(x1,y1);cubicTo(x1,mid,x2,mid,x2,y2) }
                for (i in 0 until nodes.lastIndex) drawPath(segment(nodes[i], nodes[i+1]), Color(0xFFD9DEE7), style=Stroke(width=9f,cap=StrokeCap.Round))
                for (i in 0 until nodes.lastIndex) if (indices[i] in completed) drawPath(segment(nodes[i], nodes[i+1]), ch.accent.copy(alpha=.75f), style=Stroke(width=9f,cap=StrokeCap.Round))
            }
            nodes.forEachIndexed { i,p -> val global=indices[i]; val nodeSize=64.dp; val x=maxWidth*p.first-nodeSize/2; val y=maxHeight*p.second-nodeSize/2; val current=global==unlocked && global !in completed; Column(Modifier.offset(x,y), horizontalAlignment=Alignment.CenterHorizontally) { RLevelNode(i+1, global<=unlocked, global in completed, current, ch.accent) { openLevel(global) }; Spacer(Modifier.height(4.dp)); Row(horizontalArrangement=Arrangement.spacedBy(1.dp)) { repeat(3) { s -> Icon(if(s<(stars[global]?:0)) Icons.Filled.Star else Icons.Filled.StarBorder,null,tint=if(s<(stars[global]?:0))RUi.Gold else Color(0xFFCDD2DA),modifier=Modifier.size(12.dp)) } } } }
        }; Spacer(Modifier.height(16.dp)) }
    }
}
@Composable private fun RLevelNode(number: Int, unlocked: Boolean, completed: Boolean, current: Boolean, accent: Color, click: () -> Unit) {
    val transition = rememberInfiniteTransition(label="nodePulse"); val pulse by transition.animateFloat(1f,1.08f,infiniteRepeatable(tween(900),RepeatMode.Reverse),label="pulse")
    val bg=when{!unlocked->Color(0xFFF1F3F6);completed->RUi.Success;current->accent;else->accent.copy(alpha=.82f)}
    Box(Modifier.size(62.dp).scale(if(current)pulse else 1f).shadow(if(current)10.dp else 3.dp,CircleShape).background(bg,CircleShape).border(if(current)3.dp else 1.dp,if(current)Color.White else Color.White.copy(alpha=.8f),CircleShape).clickable(enabled=unlocked,onClick=click), contentAlignment=Alignment.Center) { when { !unlocked->Icon(Icons.Filled.Lock,null,tint=RUi.Subtle,modifier=Modifier.size(21.dp)); completed->Icon(Icons.Filled.CheckCircle,null,tint=Color.White,modifier=Modifier.size(26.dp)); else->Text(number.toString(),color=Color.White,fontWeight=FontWeight.Black,fontSize=19.sp) } }
}

@Composable private fun RGame(levelIndex: Int, coins: Int, previousStars: Int, firstCompletion: Boolean, soundEnabled: Boolean, hapticEnabled: Boolean, reduceMotion: Boolean, coinsChanged: (Int)->Unit, wordFound:()->Unit, hintUsed:()->Unit, comboChanged:(Int)->Unit, completedCallback:(Int)->Unit, back:()->Unit, replay:()->Unit, map:()->Unit, next:()->Unit, claimChapter:()->Unit, chapterChestClaimed:Boolean) {
    val level=levels[levelIndex]; val haptics=LocalHapticFeedback.current; val tone=remember{ToneGenerator(AudioManager.STREAM_MUSIC,60)}; DisposableEffect(Unit){onDispose{tone.release()}}
    var wheel by remember{mutableStateOf(level.letters)}; var found by remember{mutableStateOf(setOf<String>())}; var bonus by remember{mutableStateOf(setOf<String>())}; var hintsCells by remember{mutableStateOf(setOf<RCell>())}; var selection by remember{mutableStateOf(emptyList<Int>())}; var live by remember{mutableStateOf("")}; var status by remember{mutableStateOf("Harfleri birleştir")}; var hints by remember{mutableIntStateOf(0)}; var combo by remember{mutableIntStateOf(0)}; var handled by remember{mutableStateOf(false)}; var showResult by remember{mutableStateOf(false)}; var net by remember{mutableIntStateOf(0)}
    val done=level.targets.all{it in found}; val earned=when(hints){0->3;1->2;2->1;else->0}; val chapter=rChapters()[levelIndex/5]; val chapterEnd=levelIndex==chapter.endExclusive-1
    LaunchedEffect(done){if(done&&!handled){handled=true;completedCallback(earned);if(soundEnabled)tone.startTone(ToneGenerator.TONE_PROP_ACK,160);if(hapticEnabled)haptics.performHapticFeedback(HapticFeedbackType.LongPress);if(!reduceMotion)delay(260);showResult=true}}
    val scale by animateFloatAsState(if(live.isNotBlank())1.025f else 1f,tween(if(reduceMotion)0 else 100),label="wordScale")
    RSurface { BoxWithConstraints(Modifier.fillMaxSize()) { val compact=maxHeight<760.dp; Column(Modifier.fillMaxSize().padding(horizontal=18.dp)) {
        Row(Modifier.fillMaxWidth().height(58.dp),verticalAlignment=Alignment.CenterVertically){RIconButton(Icons.Filled.ArrowBack,back);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Level ${levelIndex+1}",color=RUi.Ink,fontWeight=FontWeight.Bold,fontSize=16.sp);Text(level.title,color=RUi.Muted,fontSize=11.sp)};RChip(Icons.Filled.AutoAwesome,coins.toString(),RUi.Gold)}
        Spacer(Modifier.height(if(compact)4.dp else 8.dp)); RBoard(level,found,hintsCells,compact); Spacer(Modifier.height(9.dp)); Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("${found.size}/${level.targets.size} kelime",color=RUi.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));if(combo>=2)Text("Combo x$combo",color=RUi.Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)else Row{repeat(3){s->Icon(if(s<earned)Icons.Filled.Star else Icons.Filled.StarBorder,null,tint=if(s<earned)RUi.Gold else Color(0xFFCDD2DA),modifier=Modifier.size(16.dp))}}}
        Spacer(Modifier.height(7.dp)); Box(Modifier.fillMaxWidth().height(44.dp).scale(scale).clip(RoundedCornerShape(13.dp)).background(if(live.isNotBlank())RUi.PrimarySoft else RUi.Surface).border(1.dp,RUi.Border,RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){Text(if(live.isNotBlank())live else status,color=if(live.isNotBlank())RUi.PrimaryDark else RUi.Muted,fontWeight=if(live.isNotBlank())FontWeight.Bold else FontWeight.Medium,fontSize=if(live.isNotBlank())19.sp else 12.sp)}
        Spacer(Modifier.height(if(compact)1.dp else 5.dp)); Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){RWheel(wheel,selection,if(compact)214.dp else 244.dp,{selection=it;live=it.joinToString(""){idx->wheel[idx].toString()}},{
            val submitted=live.trim().uppercase(rTr); when { submitted in level.targets && submitted !in found->{found=found+submitted;combo++;wordFound();comboChanged(combo);val extra=if(combo%3==0)10 else 0;val gain=5+extra;net+=gain;coinsChanged(coins+gain);status=if(extra>0)"Combo x$combo · +$gain" else "Doğru · $submitted";if(soundEnabled)tone.startTone(ToneGenerator.TONE_PROP_BEEP2,90);if(hapticEnabled)haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)};submitted in found->status="Zaten bulundu";submitted in level.bonusWords&&submitted !in bonus->{bonus=bonus+submitted;combo++;wordFound();val gain=10;net+=gain;coinsChanged(coins+gain);status="Bonus kelime · +10"};submitted.isNotBlank()->{combo=0;status="Bu levelde yok";if(soundEnabled)tone.startTone(ToneGenerator.TONE_PROP_NACK,80)}};selection=emptyList();live=""
        })}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){RTool(Icons.Filled.Shuffle,"Karıştır",true,Modifier.weight(1f)){wheel=wheel.shuffled();status="Harfler karıştırıldı"};RTool(Icons.Filled.Lightbulb,"Harf 25",coins>=25&&!done,Modifier.weight(1f)){val hidden=rAllCells(level).filter{it !in hintsCells&&!rRevealed(level,it,found)};hidden.randomOrNull()?.let{coinsChanged(coins-25);net-=25;hintsCells=hintsCells+it;hints++;combo=0;hintUsed();status="Bir harf açıldı"}};RTool(Icons.Filled.Search,"Kelime 60",coins>=60&&!done,Modifier.weight(1f)){level.targets.firstOrNull{it !in found}?.let{coinsChanged(coins-60);net-=60;found=found+it;hints+=2;combo=0;hintUsed();status="Kelime açıldı"}}}
    }; if(showResult)RResult(levelIndex,earned,previousStars,found.size,level.targets.size,bonus.size,hints,combo,net,chapterEnd,chapterChestClaimed,firstCompletion,reduceMotion,replay,map,next,claimChapter) } }
}
@Composable private fun RTool(icon:ImageVector,label:String,enabled:Boolean,modifier:Modifier=Modifier,click:()->Unit){Column(modifier.height(58.dp).clip(RoundedCornerShape(15.dp)).background(RUi.Surface).border(1.dp,RUi.Border,RoundedCornerShape(15.dp)).clickable(enabled=enabled,onClick=click).padding(5.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(icon,null,tint=if(enabled)RUi.Primary else RUi.Subtle,modifier=Modifier.size(20.dp));Text(label,color=if(enabled)RUi.Ink else RUi.Subtle,fontWeight=FontWeight.SemiBold,fontSize=9.sp)}}
private fun rCellsFor(p:Placement)=p.word.mapIndexed{i,ch->RCell(p.row+if(p.horizontal)0 else i,p.col+if(p.horizontal)i else 0) to ch}
private fun rAllCells(level:Level)=level.placements.flatMap(::rCellsFor).map{it.first}.toSet()
private fun rRevealed(level:Level,cell:RCell,found:Set<String>)=level.placements.any{it.word in found&&rCellsFor(it).any{pair->pair.first==cell}}
@Composable private fun RBoard(level:Level,found:Set<String>,hinted:Set<RCell>,compact:Boolean){val entries=remember(level){level.placements.flatMap(::rCellsFor)};val map=remember(level){entries.associate{it.first to it.second}};val rows=(map.keys.maxOfOrNull{it.row}?:0)+1;val cols=(map.keys.maxOfOrNull{it.col}?:0)+1;val size=when{compact&&cols>=5->33.dp;cols>=6->35.dp;cols==5->39.dp;else->42.dp};Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(RUi.Surface).border(1.dp,RUi.Border,RoundedCornerShape(20.dp)).padding(if(compact)8.dp else 11.dp),contentAlignment=Alignment.Center){Column(verticalArrangement=Arrangement.spacedBy(4.dp)){repeat(rows){r->Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){repeat(cols){c->val cell=RCell(r,c);val letter=map[cell];if(letter==null)Spacer(Modifier.size(size))else{val reveal=cell in hinted||rRevealed(level,cell,found);Box(Modifier.size(size).clip(RoundedCornerShape(8.dp)).background(if(reveal)RUi.PrimarySoft else Color(0xFFF0F3F8)).border(1.dp,if(reveal)RUi.Primary.copy(alpha=.45f) else Color(0xFFD8DEE8),RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center){if(reveal)Text(letter.toString(),color=RUi.PrimaryDark,fontWeight=FontWeight.Black,fontSize=if(compact)15.sp else 17.sp)}}}}}}}}
@Composable private fun RWheel(letters:List<Char>,selected:List<Int>,sizeDp:Dp,onChanged:(List<Int>)->Unit,onFinished:()->Unit){var measured by remember{mutableStateOf(IntSize.Zero)};var pointer by remember{mutableStateOf<Offset?>(null)};fun positions():List<Offset>{if(measured==IntSize.Zero)return emptyList();val center=Offset(measured.width/2f,measured.height/2f);val radius=minOf(measured.width,measured.height)*.34f;return letters.indices.map{i->val a=-PI/2+2*PI*i/letters.size;Offset(center.x+(cos(a)*radius).toFloat(),center.y+(sin(a)*radius).toFloat())}};fun at(p:Offset):Int?{val pts=positions();val hit=minOf(measured.width,measured.height)*.13f;return pts.indices.firstOrNull{i->hypot((p.x-pts[i].x).toDouble(),(p.y-pts[i].y).toDouble())<=hit}}
    Box(Modifier.size(sizeDp).onSizeChanged{measured=it}.pointerInput(letters,measured){var gesture=emptyList<Int>();detectDragGestures(onDragStart={p->pointer=p;val i=at(p);gesture=if(i!=null)listOf(i)else emptyList();onChanged(gesture)},onDragEnd={pointer=null;onFinished();gesture=emptyList()},onDragCancel={pointer=null;onFinished();gesture=emptyList()}){change,_->pointer=change.position;val i=at(change.position);if(i!=null){when{gesture.size>=2&&i==gesture[gesture.lastIndex-1]-> {gesture=gesture.dropLast(1);onChanged(gesture)};i !in gesture->{gesture=gesture+i;onChanged(gesture)}}}}}){
        Canvas(Modifier.fillMaxSize()){val center=Offset(size.width/2,size.height/2);val radius=minOf(size.width,size.height)*.47f;drawCircle(RUi.Surface,radius,center);drawCircle(Color(0xFFE3E7EE),radius,center,style=Stroke(3f));val pts=positions();selected.zipWithNext().forEach{(a,b)->if(a in pts.indices&&b in pts.indices)drawLine(RUi.Primary,pts[a],pts[b],9f,cap=StrokeCap.Round)};if(selected.isNotEmpty()&&pointer!=null)drawLine(RUi.Primary.copy(alpha=.55f),pts[selected.last()],pointer!!,7f,cap=StrokeCap.Round);pts.forEachIndexed{i,p->val sel=i in selected;val r=minOf(size.width,size.height)*(if(sel).11f else .101f);drawCircle(if(sel)RUi.Primary else Color(0xFFF8F9FB),r,p);drawCircle(if(sel)RUi.PrimaryDark else Color(0xFFD2D8E1),r,p,style=Stroke(2.4f));drawIntoCanvas{canvas->val paint=android.graphics.Paint().apply{color=if(sel)android.graphics.Color.WHITE else android.graphics.Color.rgb(23,32,51);textAlign=android.graphics.Paint.Align.CENTER;textSize=minOf(size.width,size.height)*.104f;typeface=android.graphics.Typeface.DEFAULT_BOLD};val m=paint.fontMetrics;canvas.nativeCanvas.drawText(letters[i].toString(),p.x,p.y-(m.ascent+m.descent)/2,paint)}}}
    }}

@Composable private fun RResult(levelIndex:Int,stars:Int,previousStars:Int,found:Int,target:Int,bonus:Int,hints:Int,combo:Int,net:Int,chapterEnd:Boolean,chestClaimed:Boolean,firstCompletion:Boolean,reduceMotion:Boolean,replay:()->Unit,map:()->Unit,next:()->Unit,claimChapter:()->Unit){var visibleStars by remember{mutableIntStateOf(if(reduceMotion)stars else 0)};LaunchedEffect(stars,reduceMotion){if(!reduceMotion){repeat(stars){delay(260);visibleStars=it+1}}};val animatedNet by animateIntAsState(net,tween(if(reduceMotion)0 else 650),label="coinCount");val newRecord=stars>previousStars
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.38f)).padding(horizontal=22.dp),contentAlignment=Alignment.Center){RCard(Modifier.fillMaxWidth()){Text(if(chapterEnd)"Bölüm tamamlandı" else "Level tamamlandı",color=RUi.Ink,fontWeight=FontWeight.Black,fontSize=24.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center);Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically){repeat(3){i->val active=i<visibleStars;Icon(if(active)Icons.Filled.Star else Icons.Filled.StarBorder,null,tint=if(active)RUi.Gold else Color(0xFFD0D5DD),modifier=Modifier.size(if(i==1)42.dp else 34.dp).scale(if(active)1.06f else 1f))}};Spacer(Modifier.height(8.dp));Text(when{newRecord->"Yeni en iyi skor";firstCompletion->"Yeni level açıldı";else->"En iyi skor: $previousStars yıldız"},color=if(newRecord||firstCompletion)RUi.Primary else RUi.Muted,fontWeight=FontWeight.SemiBold,fontSize=12.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center);Spacer(Modifier.height(16.dp));RResultRow("Bulunan kelime","$found/$target");RResultRow("Bonus kelime",bonus.toString());RResultRow("Kullanılan ipucu",hints.toString());RResultRow("En yüksek combo","x$combo");HorizontalDivider(color=RUi.Border,modifier=Modifier.padding(vertical=10.dp));Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Bu oyundaki net jeton",color=RUi.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));Text(if(animatedNet>=0)"+$animatedNet" else animatedNet.toString(),color=if(animatedNet>=0)RUi.Success else RUi.Danger,fontWeight=FontWeight.Black,fontSize=22.sp)};if(chapterEnd&&!chestClaimed){Spacer(Modifier.height(12.dp));Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(RUi.GoldSoft).clickable(onClick=claimChapter).padding(12.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Filled.CardGiftcard,null,tint=RUi.Gold);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("Bölüm ödülü",color=RUi.Ink,fontWeight=FontWeight.Bold);Text("+150 jeton",color=RUi.Muted,fontSize=11.sp)};Text("Al",color=RUi.Gold,fontWeight=FontWeight.Black)}}};Spacer(Modifier.height(16.dp));RPrimary(if(chapterEnd)"Haritaya dön" else "Sonraki level",if(chapterEnd)map else next,Modifier.fillMaxWidth(),if(chapterEnd)Icons.Filled.Map else Icons.Filled.PlayArrow);Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){RSecondary("Tekrar oyna",replay,Modifier.weight(1f),Icons.Filled.Refresh);if(!chapterEnd)RSecondary("Harita",map,Modifier.weight(1f),Icons.Filled.Map)}}}}
@Composable private fun RResultRow(label:String,value:String){Row(Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(label,color=RUi.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));Text(value,color=RUi.Ink,fontWeight=FontWeight.Bold,fontSize=12.sp)}}

@Composable private fun RDaily(streak:Int,available:Boolean,back:()->Unit,claim:()->Unit){RSurface{Column(Modifier.fillMaxSize().padding(horizontal=RUi.ScreenPad)){RTopBar("Günlük ödül",back);Text("Serini koru ve ödülleri büyüt",color=RUi.Muted,fontSize=13.sp);Spacer(Modifier.height(22.dp));RCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(44.dp).background(RUi.GoldSoft,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.LocalFireDepartment,null,tint=RUi.Gold)};Spacer(Modifier.width(12.dp));Column{Text("$streak günlük seri",color=RUi.Ink,fontWeight=FontWeight.Bold,fontSize=17.sp);Text("Bugünkü ödül +40 jeton",color=RUi.Muted,fontSize=12.sp)}}};Spacer(Modifier.weight(1f));RPrimary(if(available)"Bugünün ödülünü al" else "Bugünkü ödül alındı",claim,Modifier.fillMaxWidth().padding(bottom=24.dp),Icons.Filled.CardGiftcard,available)}}}
@Composable private fun RTasks(stats:RStats,claimed:Set<String>,back:()->Unit,claim:(RTask)->Unit,home:()->Unit,chapters:()->Unit,profile:()->Unit){RSurface{Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(horizontal=RUi.ScreenPad).padding(bottom=86.dp)){RTopBar("Görevler",back);Text("Oynadıkça ilerle, tamamladıkça kazan",color=RUi.Muted,fontSize=13.sp);Spacer(Modifier.height(14.dp));Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())){rTasks().forEach{t->val current=t.current(stats).coerceAtMost(t.goal);val done=current>=t.goal;val got=t.id in claimed;RCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).background(if(done)RUi.SuccessSoft else RUi.PrimarySoft,CircleShape),contentAlignment=Alignment.Center){Icon(if(done)Icons.Filled.CheckCircle else Icons.Filled.TaskAlt,null,tint=if(done)RUi.Success else RUi.Primary,modifier=Modifier.size(19.dp))};Spacer(Modifier.width(12.dp));Text(t.title,color=RUi.Ink,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text("+${t.reward}",color=RUi.Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)};Spacer(Modifier.height(11.dp));LinearProgressIndicator(progress={current.toFloat()/t.goal.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),color=if(done)RUi.Success else RUi.Primary,trackColor=Color(0xFFF0F2F5));Spacer(Modifier.height(7.dp));Row(Modifier.fillMaxWidth()){Text("$current/${t.goal}",color=RUi.Muted,fontSize=10.sp,modifier=Modifier.weight(1f));Text(if(got)"Alındı" else if(done)"Ödülü al" else "Devam ediyor",color=if(done&&!got)RUi.Primary else RUi.Muted,fontWeight=FontWeight.Bold,fontSize=11.sp,modifier=Modifier.clickable(enabled=done&&!got){claim(t)})}};Spacer(Modifier.height(11.dp))}}};Box(Modifier.align(Alignment.BottomCenter)){RBottom(RScreen.TASKS,home,chapters,{},profile)}}}}
@Composable private fun RProfile(coins:Int,stats:RStats,back:()->Unit,home:()->Unit,chapters:()->Unit,tasks:()->Unit,settings:()->Unit){val level=1+stats.stars/15;val xp=(stats.stars%15)/15f;RSurface{Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(horizontal=RUi.ScreenPad).padding(bottom=86.dp)){RTopBar("Profil",back){RIconButton(Icons.Filled.Settings,settings)};Spacer(Modifier.height(12.dp));Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(68.dp).background(RUi.PrimarySoft,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.Person,null,tint=RUi.Primary,modifier=Modifier.size(32.dp))};Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text("Oyuncu",color=RUi.Ink,fontWeight=FontWeight.Black,fontSize=21.sp);Text("Seviye $level",color=RUi.Muted,fontSize=12.sp);Spacer(Modifier.height(7.dp));LinearProgressIndicator(progress={xp},modifier=Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),color=RUi.Primary,trackColor=Color(0xFFF0F2F5))}};Spacer(Modifier.height(22.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){RStat("Yıldız",stats.stars.toString(),Icons.Filled.Star,RUi.Gold,Modifier.weight(1f));RStat("Level",stats.completed.toString(),Icons.Filled.CheckCircle,RUi.Success,Modifier.weight(1f));RStat("Jeton",coins.toString(),Icons.Filled.AutoAwesome,RUi.Primary,Modifier.weight(1f))};Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){RStat("Kelime",stats.words.toString(),Icons.Filled.TaskAlt,RUi.Primary,Modifier.weight(1f));RStat("İpucu",stats.hints.toString(),Icons.Filled.Lightbulb,RUi.Gold,Modifier.weight(1f));RStat("Combo","x${stats.combo}",Icons.Filled.LocalFireDepartment,RUi.Danger,Modifier.weight(1f))}};Box(Modifier.align(Alignment.BottomCenter)){RBottom(RScreen.PROFILE,home,chapters,tasks,{})}}}}
@Composable private fun RStat(label:String,value:String,icon:ImageVector,tint:Color,modifier:Modifier=Modifier){Column(modifier.height(104.dp).clip(RoundedCornerShape(17.dp)).background(RUi.Surface).border(1.dp,RUi.Border,RoundedCornerShape(17.dp)).padding(13.dp),verticalArrangement=Arrangement.SpaceBetween){Icon(icon,null,tint=tint,modifier=Modifier.size(20.dp));Column{Text(value,color=RUi.Ink,fontWeight=FontWeight.Black,fontSize=19.sp);Text(label,color=RUi.Muted,fontSize=10.sp)}}}
@Composable private fun RSettings(sound:Boolean,haptic:Boolean,motion:Boolean,back:()->Unit,onSound:(Boolean)->Unit,onHaptic:(Boolean)->Unit,onMotion:(Boolean)->Unit){RSurface{Column(Modifier.fillMaxSize().padding(horizontal=RUi.ScreenPad)){RTopBar("Ayarlar",back);Spacer(Modifier.height(14.dp));RSetting(Icons.Filled.VolumeUp,"Ses efektleri",sound,onSound);RSetting(Icons.Filled.Vibration,"Titreşim",haptic,onHaptic);RSetting(Icons.Filled.Refresh,"Animasyonları azalt",motion,onMotion);Spacer(Modifier.height(18.dp));Text("Sürüm ${BuildConfig.VERSION_NAME}",color=RUi.Subtle,fontSize=11.sp)}}}
@Composable private fun RSetting(icon:ImageVector,title:String,value:Boolean,change:(Boolean)->Unit){Row(Modifier.fillMaxWidth().height(62.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(38.dp).background(RUi.PrimarySoft,CircleShape),contentAlignment=Alignment.Center){Icon(icon,null,tint=RUi.Primary,modifier=Modifier.size(18.dp))};Spacer(Modifier.width(12.dp));Text(title,color=RUi.Ink,fontWeight=FontWeight.Medium,modifier=Modifier.weight(1f));Switch(checked=value,onCheckedChange=change)};HorizontalDivider(color=RUi.Border)}