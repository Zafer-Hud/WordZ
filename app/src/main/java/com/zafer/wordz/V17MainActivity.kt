package com.zafer.wordz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.draw.alpha
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

private object U17 {
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
    val Purple = Color(0xFF7559E8)
    val PurpleSoft = Color(0xFFF2EEFF)
}

private object Economy17 {
    const val WORD = 3
    const val BONUS = 6
    const val COMBO = 8
    const val LEVEL = 20
    const val CHAPTER = 120
    const val LETTER_HINT = 30
    const val WORD_HINT = 75
}

private const val PREFS17 = "wordz_progress"
private const val K_UNLOCKED17 = "unlocked_level"
private const val K_COINS17 = "coins"
private const val K_COMPLETED17 = "completed_levels"
private const val K_STARS17 = "level_stars"
private const val K_SOUND17 = "sound_enabled"
private const val K_MUSIC17 = "music_enabled"
private const val K_HAPTIC17 = "haptic_enabled"
private const val K_MOTION17 = "reduce_motion"
private const val K_CHESTS17 = "claimed_chapter_chests"
private const val K_WORDS17 = "stats_words"
private const val K_HINTS17 = "stats_hints"
private const val K_COMBO17 = "stats_best_combo"
private val tr17 = Locale("tr", "TR")

private data class Cell17(val row: Int, val col: Int) {
    fun encode() = "$row,$col"
    companion object {
        fun decode(raw: String): Cell17? {
            val p = raw.split(',')
            if (p.size != 2) return null
            return Cell17(p[0].toIntOrNull() ?: return null, p[1].toIntOrNull() ?: return null)
        }
    }
}
private data class Chapter17(val name: String, val subtitle: String, val start: Int, val end: Int, val accent: Color, val soft: Color)
private data class Stats17(val completed: Int, val stars: Int, val words: Int, val hints: Int, val combo: Int, val streak: Int)
private data class Task17(val id: String, val title: String, val subtitle: String, val goal: Int, val reward: Int, val current: (ChallengeProgress) -> Int)
private enum class Screen17 { HOME, CHAPTERS, MAP, GAME, DAILY, TASKS, PROFILE, SETTINGS }
private enum class Feedback17 { NORMAL, CORRECT, BONUS, WRONG }

class V17MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validateAllLevels()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = U17.Blue,
                    secondary = U17.Gold,
                    background = U17.Bg,
                    surface = U17.Surface,
                    onPrimary = Color.White,
                    onBackground = U17.Ink,
                    onSurface = U17.Ink,
                )
            ) { App17() }
        }
    }
}

private fun chapters17(): List<Chapter17> {
    val names = listOf("Başlangıç", "Sahil", "Orman", "Çöl", "Gece", "Uzak Diyarlar", "Zirve")
    val subs = listOf("Temelleri keşfet", "Ritmi yakala", "Yeni yollar aç", "Zorlu kelimelere geç", "Gece rotasını tamamla", "Uzak diyarlara ilerle", "Son zirve")
    val accents = listOf(Color(0xFF3976F6), Color(0xFF0E9FB5), Color(0xFF2A9D63), Color(0xFFD98A25), Color(0xFF6A62D8), Color(0xFF248A89), Color(0xFF5A57C8))
    val softs = listOf(Color(0xFFEEF4FF), Color(0xFFEAF8FA), Color(0xFFECF8F1), Color(0xFFFFF4E6), Color(0xFFF1F0FF), Color(0xFFECF8F7), Color(0xFFF0F0FF))
    return levels.indices.chunked(5).mapIndexed { i, chunk -> Chapter17(names.getOrElse(i) { "Bölüm ${i + 1}" }, subs.getOrElse(i) { "Yeni kelimeler" }, chunk.first(), chunk.last() + 1, accents[i % accents.size], softs[i % softs.size]) }
}

private fun decodeStars17(raw: Set<String>): Map<Int, Int> = raw.mapNotNull {
    val p = it.split(':'); val l = p.getOrNull(0)?.toIntOrNull(); val s = p.getOrNull(1)?.toIntOrNull()
    if (l != null && s != null && l in levels.indices) l to s.coerceIn(0, 3) else null
}.toMap()
private fun encodeStars17(stars: Map<Int, Int>) = stars.map { "${it.key}:${it.value}" }.toSet()
private fun dailyTasks17() = listOf(
    Task17("d_words", "8 kelime bul", "Bugün kelime zincirini büyüt", 8, 25, { it.words }),
    Task17("d_levels", "2 level tamamla", "İki bulmacayı bitir", 2, 35, { it.levels }),
    Task17("d_clean", "İpucusuz 1 level", "Bir leveli yardım almadan bitir", 1, 30, { it.noHintLevels }),
)
private fun weeklyTasks17() = listOf(
    Task17("w_words", "50 kelime bul", "Hafta boyunca 50 kelime", 50, 100, { it.words }),
    Task17("w_levels", "10 level tamamla", "Toplam 10 oyun bitir", 10, 120, { it.levels }),
    Task17("w_stars", "18 yıldız kazan", "Hafta boyunca 18 yıldız", 18, 120, { it.stars }),
)

@Composable
private fun App17() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS17, Context.MODE_PRIVATE) }
    val gameStore = remember { GameProgressStore(prefs) }
    val metaStore = remember { MetaProgressStore(prefs) }
    val playerStore = remember { PlayerProgressStore(prefs) }
    val audio = remember { WordZAudio(context.applicationContext) }
    val today = LocalDate.now()
    metaStore.ensurePeriods(today)

    val initialUnlocked = prefs.getInt(K_UNLOCKED17, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(K_COMPLETED17, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
    val initialCompleted = if (savedCompleted.isEmpty() && initialUnlocked > 0) (0 until initialUnlocked).toSet() else savedCompleted
    val initialStars = decodeStars17(prefs.getStringSet(K_STARS17, emptySet()).orEmpty()).toMutableMap().apply { initialCompleted.forEach { if (it !in this) this[it] = 1 } }.toMap()

    var unlocked by remember { mutableIntStateOf(initialUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(K_COINS17, 100)) }
    var completed by remember { mutableStateOf(initialCompleted) }
    var stars by remember { mutableStateOf(initialStars) }
    var sound by remember { mutableStateOf(prefs.getBoolean(K_SOUND17, true)) }
    var music by remember { mutableStateOf(prefs.getBoolean(K_MUSIC17, false)) }
    var haptic by remember { mutableStateOf(prefs.getBoolean(K_HAPTIC17, true)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean(K_MOTION17, false)) }
    var chapterChests by remember { mutableStateOf(prefs.getStringSet(K_CHESTS17, emptySet()).orEmpty().toSet()) }
    var totalWords by remember { mutableIntStateOf(prefs.getInt(K_WORDS17, 0)) }
    var totalHints by remember { mutableIntStateOf(prefs.getInt(K_HINTS17, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(K_COMBO17, 0)) }
    var player by remember { mutableStateOf(playerStore.snapshot()) }
    var levelUp by remember { mutableStateOf<Int?>(null) }

    var dailyProgress by remember { mutableStateOf(metaStore.daily(today)) }
    var weeklyProgress by remember { mutableStateOf(metaStore.weekly(today)) }
    var dailyClaims by remember { mutableStateOf(metaStore.dailyClaims(today)) }
    var weeklyClaims by remember { mutableStateOf(metaStore.weeklyClaims(today)) }
    var rewardState by remember { mutableStateOf(metaStore.rewardState(today)) }
    var screen by remember { mutableStateOf(Screen17.HOME) }
    var selectedChapter by remember { mutableIntStateOf((unlocked / 5).coerceIn(0, chapters17().lastIndex)) }
    var selectedLevel by remember { mutableIntStateOf(unlocked) }
    var gameSession by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) { onDispose { audio.release() } }
    LaunchedEffect(sound) { audio.setEffectsEnabled(sound) }
    LaunchedEffect(music) { audio.setMusicEnabled(music) }

    fun saveCore() {
        prefs.edit().putInt(K_UNLOCKED17, unlocked).putInt(K_COINS17, coins)
            .putStringSet(K_COMPLETED17, completed.map(Int::toString).toSet()).putStringSet(K_STARS17, encodeStars17(stars))
            .putBoolean(K_SOUND17, sound).putBoolean(K_MUSIC17, music).putBoolean(K_HAPTIC17, haptic).putBoolean(K_MOTION17, reduceMotion)
            .putStringSet(K_CHESTS17, chapterChests).putInt(K_WORDS17, totalWords).putInt(K_HINTS17, totalHints).putInt(K_COMBO17, bestCombo).apply()
    }
    fun refreshMeta() {
        dailyProgress = metaStore.daily(today); weeklyProgress = metaStore.weekly(today)
        dailyClaims = metaStore.dailyClaims(today); weeklyClaims = metaStore.weeklyClaims(today); rewardState = metaStore.rewardState(today)
    }
    fun grantXp(amount: Int) {
        val result = playerStore.grant(amount)
        player = result.after
        if (result.levelsGained > 0) {
            coins += result.levelsGained * PlayerProgressStore.LEVEL_UP_COIN_REWARD
            levelUp = result.after.level
            audio.play(WordZAudio.Fx.LEVEL_UP)
            saveCore()
        }
    }
    fun openLevel(index: Int) {
        selectedLevel = index; selectedChapter = (index / 5).coerceIn(0, chapters17().lastIndex); gameSession += 1; screen = Screen17.GAME
    }

    val totalStars = stars.values.sum()
    val stats = Stats17(completed.size, totalStars, totalWords, totalHints, bestCombo, rewardState.streak)
    val taskBadge = dailyTasks17().any { it.current(dailyProgress) >= it.goal && it.id !in dailyClaims } || weeklyTasks17().any { it.current(weeklyProgress) >= it.goal && it.id !in weeklyClaims }

    Box(Modifier.fillMaxSize()) {
        when (screen) {
            Screen17.HOME -> Home17(coins, totalStars, unlocked, completed.size, rewardState, player, taskBadge, { openLevel(unlocked) }, { screen = Screen17.CHAPTERS }, { screen = Screen17.DAILY }, { screen = Screen17.TASKS }, { screen = Screen17.PROFILE }, { screen = Screen17.SETTINGS })
            Screen17.CHAPTERS -> Chapters17(unlocked, stars, completed, { screen = Screen17.HOME }, { selectedChapter = it; screen = Screen17.MAP }, { screen = Screen17.HOME }, { screen = Screen17.TASKS }, { screen = Screen17.PROFILE })
            Screen17.MAP -> Map17(selectedChapter, unlocked, stars, completed, reduceMotion, { screen = Screen17.CHAPTERS }, { openLevel(it) })
            Screen17.GAME -> key(gameSession) {
                Game17(
                    selectedLevel, coins, stars[selectedLevel] ?: 0, selectedLevel in completed, sound, haptic, reduceMotion, gameStore, audio,
                    onCoins = { coins = it; saveCore() },
                    onWord = { totalWords += 1; metaStore.recordWord(today); grantXp(PlayerProgressStore.WORD_XP); refreshMeta(); saveCore() },
                    onHint = { totalHints += 1; saveCore() },
                    onCombo = { if (it > bestCombo) { bestCombo = it; saveCore() } },
                    onComplete = { earned, hintsUsed ->
                        val first = selectedLevel !in completed
                        if (first) {
                            metaStore.recordLevel(earned, hintsUsed == 0, today)
                            grantXp(PlayerProgressStore.LEVEL_XP + if (hintsUsed == 0) PlayerProgressStore.NO_HINT_BONUS_XP else 0)
                        }
                        completed = completed + selectedLevel
                        val old = stars[selectedLevel] ?: 0
                        if (earned > old) stars = stars + (selectedLevel to earned)
                        if (selectedLevel >= unlocked && unlocked < levels.lastIndex) unlocked = selectedLevel + 1
                        refreshMeta(); saveCore()
                    },
                    back = { screen = Screen17.MAP },
                    replay = { gameStore.clear(selectedLevel); gameSession += 1 },
                    map = { screen = Screen17.MAP },
                    next = { val ch = chapters17()[selectedChapter]; if (selectedLevel + 1 < ch.end && selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = Screen17.MAP },
                    claimChapter = {
                        val ch = chapters17()[selectedChapter]; val key = selectedChapter.toString(); val allDone = (ch.start until ch.end).all { it in completed || it == selectedLevel }
                        if (allDone && key !in chapterChests) { chapterChests = chapterChests + key; coins += Economy17.CHAPTER; audio.play(WordZAudio.Fx.REWARD); saveCore() }
                    },
                    chestClaimed = selectedChapter.toString() in chapterChests,
                )
            }
            Screen17.DAILY -> Daily17(rewardState, { screen = Screen17.HOME }) {
                val claimed = metaStore.claimDailyReward(today)
                if (claimed != null) { coins += claimed.rewardToday; rewardState = claimed; grantXp(PlayerProgressStore.DAILY_REWARD_XP); audio.play(WordZAudio.Fx.REWARD); saveCore(); refreshMeta() }
            }
            Screen17.TASKS -> Tasks17(dailyProgress, weeklyProgress, dailyClaims, weeklyClaims, { screen = Screen17.HOME }, { task, weekly ->
                val progress = if (weekly) weeklyProgress else dailyProgress
                if (task.current(progress) >= task.goal) {
                    val claimed = if (weekly) metaStore.claimWeekly(task.id, today) else metaStore.claimDaily(task.id, today)
                    if (claimed) { coins += task.reward; grantXp(if (weekly) PlayerProgressStore.WEEKLY_TASK_XP else PlayerProgressStore.DAILY_TASK_XP); audio.play(WordZAudio.Fx.REWARD); saveCore(); refreshMeta() }
                }
            }, { screen = Screen17.HOME }, { screen = Screen17.CHAPTERS }, { screen = Screen17.PROFILE })
            Screen17.PROFILE -> Profile17(coins, stats, player, { screen = Screen17.HOME }, { screen = Screen17.HOME }, { screen = Screen17.CHAPTERS }, { screen = Screen17.TASKS }, { screen = Screen17.SETTINGS })
            Screen17.SETTINGS -> Settings17(sound, music, haptic, reduceMotion, { screen = Screen17.PROFILE }, { sound = it; saveCore() }, { music = it; saveCore() }, { haptic = it; saveCore() }, { reduceMotion = it; saveCore() })
        }
        if (levelUp != null) LevelUpOverlay17(levelUp!!, reduceMotion) { levelUp = null }
    }
}

@Composable private fun Surface17(content: @Composable () -> Unit) { Box(Modifier.fillMaxSize().background(U17.Bg).statusBarsPadding()) { content() } }
@Composable private fun Top17(title: String? = null, back: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) { Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) { if (back != null) { IconButton17(Icons.Filled.ArrowBack, back); Spacer(Modifier.width(12.dp)) }; if (title != null) Text(title, color = U17.Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)) else Spacer(Modifier.weight(1f)); trailing?.invoke() } }
@Composable private fun IconButton17(icon: ImageVector, click: () -> Unit) { Box(Modifier.size(42.dp).clip(CircleShape).background(U17.Surface).border(1.dp, U17.Border, CircleShape).clickable(onClick = click), contentAlignment = Alignment.Center) { Icon(icon, null, tint = U17.Ink, modifier = Modifier.size(20.dp)) } }
@Composable private fun Chip17(icon: ImageVector, text: String, tint: Color) { Row(Modifier.clip(RoundedCornerShape(14.dp)).background(U17.Surface).border(1.dp, U17.Border, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text(text, color = U17.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp) } }
@Composable private fun Card17(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) { Column(modifier.shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x08000000), spotColor = Color(0x08000000)).clip(RoundedCornerShape(20.dp)).background(U17.Surface).border(1.dp, U17.Border, RoundedCornerShape(20.dp)).padding(18.dp), content = content) }
@Composable private fun Primary17(text: String, click: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) { Row(modifier.height(54.dp).clip(RoundedCornerShape(16.dp)).background(if (enabled) U17.Blue else Color(0xFFD5D9E2)).clickable(enabled = enabled, onClick = click), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { if (icon != null) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }; Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) } }
@Composable private fun Secondary17(text: String, click: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) { Row(modifier.height(46.dp).clip(RoundedCornerShape(14.dp)).background(U17.Surface).border(1.dp, U17.Border, RoundedCornerShape(14.dp)).clickable(onClick = click), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { if (icon != null) { Icon(icon, null, tint = U17.Blue, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)) }; Text(text, color = U17.Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) } }
@Composable private fun Bottom17(active: Screen17, home: () -> Unit, chapters: () -> Unit, tasks: () -> Unit, profile: () -> Unit) { Row(Modifier.fillMaxWidth().navigationBarsPadding().height(70.dp).background(U17.Surface).border(1.dp, U17.Border, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) { Nav17("Ana Sayfa", Icons.Filled.Home, active == Screen17.HOME, home); Nav17("Bölümler", Icons.Filled.Map, active == Screen17.CHAPTERS, chapters); Nav17("Görevler", Icons.Filled.TaskAlt, active == Screen17.TASKS, tasks); Nav17("Profil", Icons.Filled.Person, active == Screen17.PROFILE, profile) } }
@Composable private fun Nav17(label: String, icon: ImageVector, active: Boolean, click: () -> Unit) { Column(Modifier.clickable(onClick = click).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = if (active) U17.Blue else U17.Subtle, modifier = Modifier.size(20.dp)); Text(label, color = if (active) U17.Blue else U17.Subtle, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium) } }

@Composable private fun Home17(coins: Int, stars: Int, unlocked: Int, completed: Int, reward: DailyRewardState, player: PlayerProgress, taskBadge: Boolean, cont: () -> Unit, chapters: () -> Unit, daily: () -> Unit, tasks: () -> Unit, profile: () -> Unit, settings: () -> Unit) {
    val ch = chapters17()[(unlocked / 5).coerceIn(0, chapters17().lastIndex)]
    Surface17 { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 88.dp)) {
        Row(Modifier.fillMaxWidth().height(66.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("WordZ", color = U17.Ink, fontSize = 28.sp, fontWeight = FontWeight.Black); Text("Kelime bulmaca", color = U17.Muted, fontSize = 12.sp) }; Chip17(Icons.Filled.AutoAwesome, coins.toString(), U17.Gold); Spacer(Modifier.width(8.dp)); Chip17(Icons.Filled.Star, stars.toString(), U17.Gold); Spacer(Modifier.width(8.dp)); IconButton17(Icons.Filled.Settings, settings) }
        Spacer(Modifier.height(8.dp)); Card17(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).background(U17.PurpleSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.EmojiEvents, null, tint = U17.Purple) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Oyuncu seviyesi ${player.level}", color = U17.Ink, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); LinearProgressIndicator(progress = { player.currentLevelXp.toFloat() / player.nextLevelXp.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = U17.Purple, trackColor = U17.PurpleSoft); Spacer(Modifier.height(4.dp)); Text("${player.currentLevelXp}/${player.nextLevelXp} XP", color = U17.Muted, fontSize = 10.sp) } } }
        Spacer(Modifier.height(12.dp)); Box(Modifier.fillMaxWidth().height(198.dp).clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(ch.accent, ch.accent.copy(alpha = .82f)))).padding(22.dp)) { Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) { Column { Text("DEVAM ET", color = Color.White.copy(alpha = .72f), fontSize = 11.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(6.dp)); Text(ch.name, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black); Text("Level ${unlocked + 1} · ${levels[unlocked].title}", color = Color.White.copy(alpha = .86f), fontSize = 13.sp) }; Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { LinearProgressIndicator(progress = { completed.toFloat() / levels.size.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = .24f)); Spacer(Modifier.height(6.dp)); Text("$completed / ${levels.size} tamamlandı", color = Color.White.copy(alpha = .75f), fontSize = 11.sp) }; Spacer(Modifier.width(16.dp)); Box(Modifier.size(50.dp).background(Color.White, CircleShape).clickable(onClick = cont), contentAlignment = Alignment.Center) { Icon(Icons.Filled.PlayArrow, null, tint = ch.accent, modifier = Modifier.size(27.dp)) } } } }
        Spacer(Modifier.height(16.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Quick17("7 günlük ödül", if (reward.claimedToday) "Bugün alındı" else "+${reward.rewardToday} hazır", Icons.Filled.CardGiftcard, U17.GoldSoft, U17.Gold, !reward.claimedToday, daily, Modifier.weight(1f)); Quick17("Görevler", if (taskBadge) "Ödül hazır" else "Günlük + haftalık", Icons.Filled.TaskAlt, U17.BlueSoft, U17.Blue, taskBadge, tasks, Modifier.weight(1f)) }
    }; Box(Modifier.align(Alignment.BottomCenter)) { Bottom17(Screen17.HOME, {}, chapters, tasks, profile) } } }
}
@Composable private fun Quick17(title: String, subtitle: String, icon: ImageVector, bg: Color, tint: Color, badge: Boolean, click: () -> Unit, modifier: Modifier = Modifier) { Column(modifier.height(112.dp).clip(RoundedCornerShape(18.dp)).background(U17.Surface).border(1.dp, U17.Border, RoundedCornerShape(18.dp)).clickable(onClick = click).padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Box(Modifier.size(36.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }; if (badge) Box(Modifier.size(8.dp).background(U17.Danger, CircleShape)) }; Column { Text(title, color = U17.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(subtitle, color = U17.Muted, fontSize = 11.sp) } } }

@Composable private fun Chapters17(unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, back: () -> Unit, open: (Int) -> Unit, home: () -> Unit, tasks: () -> Unit, profile: () -> Unit) { Surface17 { Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 86.dp)) { Top17("Bölümler", back); Text("Her bölüm 5 level", color = U17.Muted, fontSize = 13.sp); Spacer(Modifier.height(14.dp)); Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) { chapters17().forEachIndexed { i, ch -> val range = ch.start until ch.end; val openable = unlocked >= ch.start; ChapterCard17(i, ch, range.count { it in completed }, range.count(), range.sumOf { stars[it] ?: 0 }, openable) { open(i) }; Spacer(Modifier.height(11.dp)) } } }; Box(Modifier.align(Alignment.BottomCenter)) { Bottom17(Screen17.CHAPTERS, home, {}, tasks, profile) } } } }
@Composable private fun ChapterCard17(index: Int, ch: Chapter17, done: Int, total: Int, stars: Int, unlocked: Boolean, click: () -> Unit) { Row(Modifier.fillMaxWidth().height(102.dp).clip(RoundedCornerShape(18.dp)).background(U17.Surface).border(1.dp, U17.Border, RoundedCornerShape(18.dp)).clickable(enabled = unlocked, onClick = click).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(if (unlocked) ch.soft else Color(0xFFF1F3F6), CircleShape), contentAlignment = Alignment.Center) { if (unlocked) Text("${index + 1}", color = ch.accent, fontWeight = FontWeight.Black, fontSize = 18.sp) else Icon(Icons.Filled.Lock, null, tint = U17.Subtle) }; Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(ch.name, color = if (unlocked) U17.Ink else U17.Subtle, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(ch.subtitle, color = U17.Muted, fontSize = 11.sp); Spacer(Modifier.height(7.dp)); LinearProgressIndicator(progress = { done.toFloat() / total.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = if (unlocked) ch.accent else Color(0xFFD5D9E2), trackColor = Color(0xFFF0F2F5)); Spacer(Modifier.height(4.dp)); Text(if (unlocked) "$done/$total · $stars/${total * 3} yıldız" else "Kilitli", color = U17.Muted, fontSize = 10.sp) }; Icon(Icons.Filled.ChevronRight, null, tint = U17.Subtle) } }

@Composable private fun Map17(chapterIndex: Int, unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, reduceMotion: Boolean, back: () -> Unit, open: (Int) -> Unit) {
    val ch = chapters17()[chapterIndex]; val ids = (ch.start until ch.end).toList(); val nodes = listOf(.18f to .82f, .43f to .65f, .73f to .48f, .47f to .31f, .22f to .14f)
    Surface17 { Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) { Top17("${chapterIndex + 1}. ${ch.name}", back); Row { Text("${ids.count { it in completed }}/${ids.size} tamamlandı", color = U17.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("${ids.sumOf { stars[it] ?: 0 }} yıldız", color = U17.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp) }; Spacer(Modifier.height(14.dp)); BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(ch.soft, U17.Surface))).border(1.dp, U17.Border, RoundedCornerShape(26.dp))) {
        Canvas(Modifier.fillMaxSize()) { fun seg(a: Pair<Float, Float>, b: Pair<Float, Float>) = Path().apply { val x1=size.width*a.first; val y1=size.height*a.second; val x2=size.width*b.first; val y2=size.height*b.second; val m=(y1+y2)/2; moveTo(x1,y1); cubicTo(x1,m,x2,m,x2,y2) }; for(i in 0 until nodes.lastIndex) drawPath(seg(nodes[i],nodes[i+1]),Color(0xFFD9DEE7),style=Stroke(9f,cap=StrokeCap.Round)); for(i in 0 until nodes.lastIndex) if(ids[i] in completed) drawPath(seg(nodes[i],nodes[i+1]),ch.accent.copy(alpha=.75f),style=Stroke(9f,cap=StrokeCap.Round)) }
        nodes.forEachIndexed { i,p -> val global=ids[i]; val s=64.dp; Column(Modifier.offset(maxWidth*p.first-s/2,maxHeight*p.second-s/2), horizontalAlignment=Alignment.CenterHorizontally) { LevelNode17(i+1,global<=unlocked,global in completed,global==unlocked&&global !in completed,ch.accent,reduceMotion){open(global)}; Spacer(Modifier.height(4.dp)); Row{repeat(3){x->Icon(if(x<(stars[global]?:0))Icons.Filled.Star else Icons.Filled.StarBorder,null,tint=if(x<(stars[global]?:0))U17.Gold else Color(0xFFCDD2DA),modifier=Modifier.size(12.dp))}} } }
    }; Spacer(Modifier.height(16.dp)) } }
}
@Composable private fun LevelNode17(number: Int, unlocked: Boolean, completed: Boolean, current: Boolean, accent: Color, reduceMotion: Boolean, click: () -> Unit) { val transition=rememberInfiniteTransition(label="node"); val pulse by transition.animateFloat(1f,1.08f,infiniteRepeatable(tween(850),RepeatMode.Reverse),label="pulse"); val bg=when{!unlocked->Color(0xFFF1F3F6);completed->U17.Green;else->accent}; Box(Modifier.size(62.dp).scale(if(current&&!reduceMotion)pulse else 1f).shadow(if(current)10.dp else 3.dp,CircleShape).background(bg,CircleShape).border(if(current)3.dp else 1.dp,Color.White,CircleShape).clickable(enabled=unlocked,onClick=click),contentAlignment=Alignment.Center){when{!unlocked->Icon(Icons.Filled.Lock,null,tint=U17.Subtle);completed->Icon(Icons.Filled.CheckCircle,null,tint=Color.White,modifier=Modifier.size(26.dp));else->Text(number.toString(),color=Color.White,fontWeight=FontWeight.Black,fontSize=19.sp)}} }

@Composable private fun Game17(levelIndex: Int, coins: Int, previousStars: Int, completedBefore: Boolean, soundEnabled: Boolean, hapticEnabled: Boolean, reduceMotion: Boolean, progressStore: GameProgressStore, audio: WordZAudio, onCoins: (Int) -> Unit, onWord: () -> Unit, onHint: () -> Unit, onCombo: (Int) -> Unit, onComplete: (Int, Int) -> Unit, back: () -> Unit, replay: () -> Unit, map: () -> Unit, next: () -> Unit, claimChapter: () -> Unit, chestClaimed: Boolean) {
    val level=levels[levelIndex]; val haptics=LocalHapticFeedback.current; val saved=remember(levelIndex){if(completedBefore)SavedLevelProgress() else progressStore.load(levelIndex)}
    var localCoins by remember(levelIndex){mutableIntStateOf(coins)}; var wheel by remember(levelIndex){mutableStateOf(level.letters)}; var found by remember(levelIndex){mutableStateOf(saved.foundWords.intersect(level.targets.toSet()))}; var bonus by remember(levelIndex){mutableStateOf(saved.bonusWords.intersect(level.bonusWords))}; var hinted by remember(levelIndex){mutableStateOf(saved.hintedCells.mapNotNull(Cell17::decode).toSet())}; var hintsUsed by remember(levelIndex){mutableIntStateOf(saved.hintsUsed)}; var selected by remember(levelIndex){mutableStateOf(emptyList<Int>())}; var live by remember(levelIndex){mutableStateOf("")}; var status by remember(levelIndex){mutableStateOf(if(found.isNotEmpty())"Kaldığın yerden devam" else "Harfleri birleştir")}; var combo by remember(levelIndex){mutableIntStateOf(0)}; var handled by remember(levelIndex){mutableStateOf(false)}; var showResult by remember(levelIndex){mutableStateOf(false)}; var net by remember(levelIndex){mutableIntStateOf(0)}; var feedback by remember(levelIndex){mutableStateOf(Feedback17.NORMAL)}; var gainToast by remember{mutableStateOf<String?>(null)}; var wrongTick by remember{mutableIntStateOf(0)}; val shake=remember{Animatable(0f)}
    LaunchedEffect(coins){localCoins=coins}; LaunchedEffect(gainToast){if(gainToast!=null){delay(700);gainToast=null}}; LaunchedEffect(wrongTick){if(wrongTick>0&&!reduceMotion){shake.snapTo(-5f);shake.animateTo(5f,tween(55));shake.animateTo(-3f,tween(55));shake.animateTo(0f,tween(55))}}
    val rewardEligible=!completedBefore; val done=level.targets.all{it in found}; val earned=when(hintsUsed){0->3;1->2;2->1;else->0}; val ch=chapters17()[levelIndex/5]; val chapterEnd=levelIndex==ch.end-1
    fun persist(){if(!completedBefore)progressStore.save(levelIndex,SavedLevelProgress(found,bonus,hinted.map(Cell17::encode).toSet(),hintsUsed))}
    fun changeCoins(delta:Int){localCoins+=delta;net+=delta;onCoins(localCoins);gainToast=if(delta>=0)"+$delta jeton" else "$delta jeton"}
    LaunchedEffect(done){if(done&&!handled){handled=true;persist();if(rewardEligible)changeCoins(Economy17.LEVEL);onComplete(earned,hintsUsed);audio.play(WordZAudio.Fx.COMPLETE);if(hapticEnabled)haptics.performHapticFeedback(HapticFeedbackType.LongPress);if(!reduceMotion)delay(260);showResult=true}}
    val wordScale by animateFloatAsState(if(live.isNotBlank())1.025f else 1f,tween(if(reduceMotion)0 else 100),label="word")
    fun submit(){val s=live.trim().uppercase(tr17);when{ s in level.targets&&s !in found->{found=found+s;combo++;if(rewardEligible){onWord();onCombo(combo)};val extra=if(rewardEligible&&combo%3==0)Economy17.COMBO else 0;val gain=if(rewardEligible)Economy17.WORD+extra else 0;if(gain>0)changeCoins(gain);feedback=Feedback17.CORRECT;status=if(gain>0)"Doğru · +$gain" else "Doğru";persist();audio.play(WordZAudio.Fx.CORRECT)}; s in found->status="Zaten bulundu"; s in level.bonusWords&&s !in bonus->{bonus=bonus+s;combo++;if(rewardEligible){onWord();onCombo(combo)};val gain=if(rewardEligible)Economy17.BONUS else 0;if(gain>0)changeCoins(gain);feedback=Feedback17.BONUS;status=if(gain>0)"Bonus · +$gain" else "Bonus kelime";persist();audio.play(WordZAudio.Fx.BONUS)}; s.isNotBlank()->{combo=0;feedback=Feedback17.WRONG;status="Bu levelde yok";wrongTick++;audio.play(WordZAudio.Fx.WRONG)}};selected=emptyList();live=""}
    Surface17 { BoxWithConstraints(Modifier.fillMaxSize()) { val compact=maxHeight<760.dp; Column(Modifier.fillMaxSize().padding(horizontal=18.dp).offset(x=shake.value.dp)) {
        Row(Modifier.fillMaxWidth().height(58.dp),verticalAlignment=Alignment.CenterVertically){IconButton17(Icons.Filled.ArrowBack,back);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Level ${levelIndex+1}",color=U17.Ink,fontWeight=FontWeight.Bold,fontSize=16.sp);Text(level.title,color=U17.Muted,fontSize=11.sp)};Chip17(Icons.Filled.AutoAwesome,localCoins.toString(),U17.Gold)}
        Spacer(Modifier.height(6.dp)); Board17(level,found,hinted,compact); Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("${found.size}/${level.targets.size} kelime",color=U17.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));AnimatedVisibility(combo>=3,enter=fadeIn(),exit=fadeOut()){Text("COMBO x$combo",color=U17.Gold,fontWeight=FontWeight.Black,fontSize=13.sp)}}; Spacer(Modifier.height(7.dp)); Box(Modifier.fillMaxWidth().height(44.dp).scale(wordScale).clip(RoundedCornerShape(13.dp)).background(if(live.isNotBlank())U17.BlueSoft else U17.Surface).border(1.dp,U17.Border,RoundedCornerShape(13.dp)),contentAlignment=Alignment.Center){Text(if(live.isNotBlank())live else status,color=if(live.isNotBlank())U17.BlueDark else U17.Muted,fontWeight=if(live.isNotBlank())FontWeight.Bold else FontWeight.Medium,fontSize=if(live.isNotBlank())19.sp else 12.sp)}
        Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Wheel17(wheel,selected,if(compact)214.dp else 244.dp,feedback,hapticEnabled,audio,{v->if(v.size==1)feedback=Feedback17.NORMAL;selected=v;live=v.joinToString(""){wheel[it].toString()}},{submit()})}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Tool17(Icons.Filled.Shuffle,"Karıştır",true,Modifier.weight(1f)){wheel=wheel.shuffled();status="Harfler karıştırıldı"};Tool17(Icons.Filled.Lightbulb,"Harf ${Economy17.LETTER_HINT}",localCoins>=Economy17.LETTER_HINT&&!done,Modifier.weight(1f)){val hidden=allCells17(level).filter{it !in hinted&&!revealed17(level,it,found)};hidden.randomOrNull()?.let{changeCoins(-Economy17.LETTER_HINT);hinted=hinted+it;hintsUsed++;combo=0;onHint();persist();status="Bir harf açıldı"}};Tool17(Icons.Filled.Search,"Kelime ${Economy17.WORD_HINT}",localCoins>=Economy17.WORD_HINT&&!done,Modifier.weight(1f)){level.targets.firstOrNull{it !in found}?.let{changeCoins(-Economy17.WORD_HINT);found=found+it;hintsUsed+=2;combo=0;onHint();persist();status="Kelime açıldı"}}}
    }; AnimatedVisibility(visible=gainToast!=null,modifier=Modifier.align(Alignment.Center),enter=fadeIn()+slideInVertically{it/2},exit=fadeOut()+slideOutVertically{-it/2}){Box(Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xE6172033)).padding(horizontal=16.dp,vertical=9.dp)){Text(gainToast.orEmpty(),color=Color.White,fontWeight=FontWeight.Bold)}}; if(showResult)Result17(earned,previousStars,found.size,level.targets.size,bonus.size,hintsUsed,combo,net,chapterEnd,chestClaimed,completedBefore,reduceMotion,replay,map,next,claimChapter) } }
}
@Composable private fun Tool17(icon:ImageVector,label:String,enabled:Boolean,modifier:Modifier=Modifier,click:()->Unit){Column(modifier.height(58.dp).clip(RoundedCornerShape(15.dp)).background(U17.Surface).border(1.dp,U17.Border,RoundedCornerShape(15.dp)).clickable(enabled=enabled,onClick=click),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(icon,null,tint=if(enabled)U17.Blue else U17.Subtle,modifier=Modifier.size(20.dp));Text(label,color=if(enabled)U17.Ink else U17.Subtle,fontWeight=FontWeight.SemiBold,fontSize=9.sp)}}
private fun cells17(p:Placement)=p.word.mapIndexed{i,ch->Cell17(p.row+if(p.horizontal)0 else i,p.col+if(p.horizontal)i else 0) to ch}
private fun allCells17(level:Level)=level.placements.flatMap(::cells17).map{it.first}.toSet()
private fun revealed17(level:Level,cell:Cell17,found:Set<String>)=level.placements.any{it.word in found&&cells17(it).any{x->x.first==cell}}
@Composable private fun Board17(level:Level,found:Set<String>,hinted:Set<Cell17>,compact:Boolean){val validated=remember(level){validateLevel(level)};val map=remember(validated){validated.lettersByCell.mapKeys{Cell17(it.key.row,it.key.col)}};val rows=(map.keys.maxOfOrNull{it.row}?:0)+1;val cols=(map.keys.maxOfOrNull{it.col}?:0)+1;val cellSize=when{compact&&cols>=5->33.dp;cols>=6->35.dp;cols==5->39.dp;else->42.dp};Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(U17.Surface).border(1.dp,U17.Border,RoundedCornerShape(20.dp)).padding(if(compact)8.dp else 11.dp),contentAlignment=Alignment.Center){Column(verticalArrangement=Arrangement.spacedBy(4.dp)){repeat(rows){r->Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){repeat(cols){c->val cell=Cell17(r,c);val letter=map[cell];if(letter==null)Spacer(Modifier.size(cellSize))else{val fromHint=cell in hinted;val reveal=fromHint||revealed17(level,cell,found);val scale by animateFloatAsState(if(reveal)1f else .92f,tween(180),label="cell");Box(Modifier.size(cellSize).scale(scale).clip(RoundedCornerShape(8.dp)).background(if(fromHint)U17.GoldSoft else if(reveal)U17.BlueSoft else Color(0xFFF0F3F8)).border(1.dp,if(fromHint)U17.Gold.copy(alpha=.55f) else if(reveal)U17.Blue.copy(alpha=.45f) else Color(0xFFD8DEE8),RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center){if(reveal)Text(letter.toString(),color=if(fromHint)Color(0xFFB77900) else U17.BlueDark,fontWeight=FontWeight.Black,fontSize=if(compact)15.sp else 17.sp)}}}}}}}}
@Composable private fun Wheel17(letters:List<Char>,selected:List<Int>,sizeDp:Dp,feedback:Feedback17,haptic:Boolean,audio:WordZAudio,onChanged:(List<Int>)->Unit,onFinished:()->Unit){var measured by remember{mutableStateOf(IntSize.Zero)};var pointer by remember{mutableStateOf<Offset?>(null)};val haptics=LocalHapticFeedback.current;fun positions():List<Offset>{if(measured==IntSize.Zero)return emptyList();val center=Offset(measured.width/2f,measured.height/2f);val radius=minOf(measured.width,measured.height)*.34f;return letters.indices.map{i->val a=-PI/2+2*PI*i/letters.size;Offset(center.x+(cos(a)*radius).toFloat(),center.y+(sin(a)*radius).toFloat())}};fun at(p:Offset):Int?{val pts=positions();val hit=minOf(measured.width,measured.height)*.13f;return pts.indices.firstOrNull{i->hypot((p.x-pts[i].x).toDouble(),(p.y-pts[i].y).toDouble())<=hit}}
    Box(Modifier.size(sizeDp).onSizeChanged{measured=it}.pointerInput(letters,measured){var gesture=emptyList<Int>();detectDragGestures(onDragStart={p->pointer=p;val i=at(p);gesture=if(i!=null)listOf(i)else emptyList();onChanged(gesture);if(i!=null){audio.play(WordZAudio.Fx.SELECT,.55f);if(haptic)haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)}},onDragEnd={pointer=null;onFinished();gesture=emptyList()},onDragCancel={pointer=null;gesture=emptyList()}){change,_->pointer=change.position;val i=at(change.position);if(i!=null){when{gesture.size>=2&&i==gesture[gesture.lastIndex-1]->{gesture=gesture.dropLast(1);onChanged(gesture)};i !in gesture->{gesture=gesture+i;onChanged(gesture);audio.play(WordZAudio.Fx.SELECT,.55f);if(haptic)haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)}}}}}){Canvas(Modifier.fillMaxSize()){val center=Offset(size.width/2,size.height/2);val radius=minOf(size.width,size.height)*.47f;val line=when(feedback){Feedback17.CORRECT->U17.Green;Feedback17.BONUS->U17.Gold;Feedback17.WRONG->U17.Danger;else->U17.Blue};drawCircle(U17.Surface,radius,center);drawCircle(Color(0xFFE3E7EE),radius,center,style=Stroke(3f));val pts=positions();selected.zipWithNext().forEach{(a,b)->if(a in pts.indices&&b in pts.indices)drawLine(line,pts[a],pts[b],9f,cap=StrokeCap.Round)};if(selected.isNotEmpty()&&pointer!=null&&selected.last() in pts.indices)drawLine(line.copy(alpha=.6f),pts[selected.last()],pointer!!,7f,cap=StrokeCap.Round);pts.forEachIndexed{i,p->val active=i in selected;val r=minOf(size.width,size.height)*(if(active).11f else .101f);drawCircle(if(active)line else Color(0xFFF8F9FB),r,p);drawCircle(if(active)line else Color(0xFFD2D8E1),r,p,style=Stroke(2.4f));drawIntoCanvas{canvas->val paint=android.graphics.Paint().apply{color=if(active)android.graphics.Color.WHITE else android.graphics.Color.rgb(23,32,51);textAlign=android.graphics.Paint.Align.CENTER;textSize=minOf(size.width,size.height)*.104f;typeface=android.graphics.Typeface.DEFAULT_BOLD};val m=paint.fontMetrics;canvas.nativeCanvas.drawText(letters[i].toString(),p.x,p.y-(m.ascent+m.descent)/2,paint)}}}}}
}

@Composable private fun Result17(stars:Int,previous:Int,found:Int,target:Int,bonus:Int,hints:Int,combo:Int,net:Int,chapterEnd:Boolean,chestClaimed:Boolean,completedBefore:Boolean,reduceMotion:Boolean,replay:()->Unit,map:()->Unit,next:()->Unit,claim:()->Unit){var shown by remember{mutableIntStateOf(if(reduceMotion)stars else 0)};LaunchedEffect(stars,reduceMotion){if(!reduceMotion)repeat(stars){i->delay(220);shown=i+1}};val animatedNet by animateIntAsState(net,tween(if(reduceMotion)0 else 650),label="net");Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.38f)).padding(horizontal=22.dp),contentAlignment=Alignment.Center){Card17(Modifier.fillMaxWidth()){Text(if(chapterEnd)"Bölüm tamamlandı" else "Level tamamlandı",color=U17.Ink,fontWeight=FontWeight.Black,fontSize=24.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center);Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){repeat(3){i->Icon(if(i<shown)Icons.Filled.Star else Icons.Filled.StarBorder,null,tint=if(i<shown)U17.Gold else Color(0xFFD0D5DD),modifier=Modifier.size(if(i==1)42.dp else 34.dp))}};Spacer(Modifier.height(8.dp));Text(when{stars>previous->"Yeni en iyi skor";completedBefore->"Pratik tamamlandı";else->"Yeni level açıldı"},color=U17.Blue,fontWeight=FontWeight.SemiBold,fontSize=12.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center);Spacer(Modifier.height(14.dp));ResultRow17("Bulunan kelime","$found/$target");ResultRow17("Bonus kelime",bonus.toString());ResultRow17("Kullanılan ipucu",hints.toString());ResultRow17("Combo","x$combo");HorizontalDivider(color=U17.Border,modifier=Modifier.padding(vertical=10.dp));Row(Modifier.fillMaxWidth()){Text("Net jeton",color=U17.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));Text(if(animatedNet>=0)"+$animatedNet" else animatedNet.toString(),color=if(animatedNet>=0)U17.Green else U17.Danger,fontWeight=FontWeight.Black,fontSize=21.sp)};if(chapterEnd&&!chestClaimed){Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(U17.GoldSoft).clickable(onClick=claim).padding(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Filled.CardGiftcard,null,tint=U17.Gold);Spacer(Modifier.width(10.dp));Text("Bölüm sandığı +${Economy17.CHAPTER}",color=U17.Ink,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text("Al",color=U17.Gold,fontWeight=FontWeight.Black)}};Spacer(Modifier.height(16.dp));Primary17(if(chapterEnd)"Haritaya dön" else "Sonraki level",if(chapterEnd)map else next,Modifier.fillMaxWidth(),if(chapterEnd)Icons.Filled.Map else Icons.Filled.PlayArrow);Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Secondary17("Tekrar oyna",replay,Modifier.weight(1f),Icons.Filled.Refresh);if(!chapterEnd)Secondary17("Harita",map,Modifier.weight(1f),Icons.Filled.Map)}}}}
@Composable private fun ResultRow17(label:String,value:String){Row(Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(label,color=U17.Muted,fontSize=12.sp,modifier=Modifier.weight(1f));Text(value,color=U17.Ink,fontWeight=FontWeight.Bold,fontSize=12.sp)}}

@Composable private fun Daily17(state:DailyRewardState,back:()->Unit,claim:()->Unit){Surface17{Column(Modifier.fillMaxSize().padding(horizontal=20.dp)){Top17("7 günlük ödül",back);Text("Her gün gel, ödül büyüsün",color=U17.Muted,fontSize=13.sp);Spacer(Modifier.height(20.dp));Card17(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(46.dp).background(U17.GoldSoft,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.LocalFireDepartment,null,tint=U17.Gold)};Spacer(Modifier.width(12.dp));Column{Text("${state.streak} günlük seri",color=U17.Ink,fontWeight=FontWeight.Bold,fontSize=18.sp);Text(if(state.claimedToday)"Bugünkü ödül alındı" else "Bugünkü ödül +${state.rewardToday}",color=U17.Muted,fontSize=12.sp)}}};Spacer(Modifier.height(18.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){MetaProgressStore.DAILY_REWARDS.forEachIndexed{i,reward->val active=i==state.cycleDay;val claimed=i<state.cycleDay||(state.claimedToday&&i==state.cycleDay);Column(Modifier.weight(1f).height(94.dp).clip(RoundedCornerShape(13.dp)).background(if(active)U17.GoldSoft else U17.Surface).border(1.dp,if(active)U17.Gold.copy(alpha=.45f) else U17.Border,RoundedCornerShape(13.dp)),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("${i+1}. gün",color=if(active)U17.Gold else U17.Muted,fontSize=9.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Icon(if(claimed)Icons.Filled.CheckCircle else Icons.Filled.CardGiftcard,null,tint=if(claimed)U17.Green else U17.Gold,modifier=Modifier.size(20.dp));Spacer(Modifier.height(6.dp));Text("+$reward",color=U17.Ink,fontSize=10.sp,fontWeight=FontWeight.Bold)}}};Spacer(Modifier.weight(1f));Primary17(if(state.claimedToday)"Bugünkü ödül alındı" else "+${state.rewardToday} jeton al",claim,Modifier.fillMaxWidth().padding(bottom=24.dp),Icons.Filled.CardGiftcard,!state.claimedToday)}}}

@Composable private fun Tasks17(daily:ChallengeProgress,weekly:ChallengeProgress,dailyClaims:Set<String>,weeklyClaims:Set<String>,back:()->Unit,claim:(Task17,Boolean)->Unit,home:()->Unit,chapters:()->Unit,profile:()->Unit){Surface17{Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(horizontal=20.dp).padding(bottom=86.dp)){Top17("Görevler",back);Text("Günlük görevler her gün, haftalık görevler pazartesi yenilenir",color=U17.Muted,fontSize=12.sp);Spacer(Modifier.height(14.dp));Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())){Text("Günlük",color=U17.Ink,fontWeight=FontWeight.Black,fontSize=18.sp);Spacer(Modifier.height(8.dp));dailyTasks17().forEach{TaskCard17(it,daily,it.id in dailyClaims,false,claim);Spacer(Modifier.height(10.dp))};Spacer(Modifier.height(8.dp));Text("Haftalık",color=U17.Ink,fontWeight=FontWeight.Black,fontSize=18.sp);Spacer(Modifier.height(8.dp));weeklyTasks17().forEach{TaskCard17(it,weekly,it.id in weeklyClaims,true,claim);Spacer(Modifier.height(10.dp))}}};Box(Modifier.align(Alignment.BottomCenter)){Bottom17(Screen17.TASKS,home,chapters,{},profile)}}}}
@Composable private fun TaskCard17(task:Task17,progress:ChallengeProgress,claimed:Boolean,weekly:Boolean,claim:(Task17,Boolean)->Unit){val current=task.current(progress).coerceAtMost(task.goal);val done=current>=task.goal;Card17(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).background(if(done)U17.GreenSoft else U17.BlueSoft,CircleShape),contentAlignment=Alignment.Center){Icon(if(done)Icons.Filled.CheckCircle else if(weekly)Icons.Filled.CalendarToday else Icons.Filled.TaskAlt,null,tint=if(done)U17.Green else U17.Blue,modifier=Modifier.size(19.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(task.title,color=U17.Ink,fontWeight=FontWeight.Bold);Text(task.subtitle,color=U17.Muted,fontSize=10.sp)};Text("+${task.reward}",color=U17.Gold,fontWeight=FontWeight.Bold,fontSize=12.sp)};Spacer(Modifier.height(11.dp));LinearProgressIndicator(progress={current.toFloat()/task.goal.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),color=if(done)U17.Green else U17.Blue,trackColor=Color(0xFFF0F2F5));Spacer(Modifier.height(7.dp));Row(Modifier.fillMaxWidth()){Text("$current/${task.goal}",color=U17.Muted,fontSize=10.sp,modifier=Modifier.weight(1f));Text(if(claimed)"Alındı" else if(done)"Ödülü al" else "Devam ediyor",color=if(done&&!claimed)U17.Blue else U17.Muted,fontWeight=FontWeight.Bold,fontSize=11.sp,modifier=Modifier.clickable(enabled=done&&!claimed){claim(task,weekly)})}}}

@Composable private fun Profile17(coins:Int,stats:Stats17,player:PlayerProgress,back:()->Unit,home:()->Unit,chapters:()->Unit,tasks:()->Unit,settings:()->Unit){Surface17{Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().padding(horizontal=20.dp).padding(bottom=86.dp)){Top17("Profil",back){IconButton17(Icons.Filled.Settings,settings)};Spacer(Modifier.height(12.dp));Card17(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(64.dp).background(U17.PurpleSoft,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.EmojiEvents,null,tint=U17.Purple,modifier=Modifier.size(31.dp))};Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text("Seviye ${player.level}",color=U17.Ink,fontWeight=FontWeight.Black,fontSize=22.sp);Text("Toplam ${player.totalXp} XP",color=U17.Muted,fontSize=11.sp);Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={player.currentLevelXp.toFloat()/player.nextLevelXp.coerceAtLeast(1)},modifier=Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),color=U17.Purple,trackColor=U17.PurpleSoft);Spacer(Modifier.height(4.dp));Text("${player.currentLevelXp}/${player.nextLevelXp} XP · seviye ödülü +${PlayerProgressStore.LEVEL_UP_COIN_REWARD}",color=U17.Muted,fontSize=10.sp)}}};Spacer(Modifier.height(14.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Stat17("Yıldız",stats.stars.toString(),Icons.Filled.Star,U17.Gold,Modifier.weight(1f));Stat17("Level",stats.completed.toString(),Icons.Filled.CheckCircle,U17.Green,Modifier.weight(1f));Stat17("Jeton",coins.toString(),Icons.Filled.AutoAwesome,U17.Blue,Modifier.weight(1f))};Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Stat17("Kelime",stats.words.toString(),Icons.Filled.TaskAlt,U17.Blue,Modifier.weight(1f));Stat17("İpucu",stats.hints.toString(),Icons.Filled.Lightbulb,U17.Gold,Modifier.weight(1f));Stat17("Combo","x${stats.combo}",Icons.Filled.LocalFireDepartment,U17.Danger,Modifier.weight(1f))}};Box(Modifier.align(Alignment.BottomCenter)){Bottom17(Screen17.PROFILE,home,chapters,tasks,{})}}}}
@Composable private fun Stat17(label:String,value:String,icon:ImageVector,tint:Color,modifier:Modifier=Modifier){Column(modifier.height(104.dp).clip(RoundedCornerShape(17.dp)).background(U17.Surface).border(1.dp,U17.Border,RoundedCornerShape(17.dp)).padding(13.dp),verticalArrangement=Arrangement.SpaceBetween){Icon(icon,null,tint=tint,modifier=Modifier.size(20.dp));Column{Text(value,color=U17.Ink,fontWeight=FontWeight.Black,fontSize=19.sp);Text(label,color=U17.Muted,fontSize=10.sp)}}}

@Composable private fun Settings17(sound:Boolean,music:Boolean,haptic:Boolean,motion:Boolean,back:()->Unit,onSound:(Boolean)->Unit,onMusic:(Boolean)->Unit,onHaptic:(Boolean)->Unit,onMotion:(Boolean)->Unit){Surface17{Column(Modifier.fillMaxSize().padding(horizontal=20.dp)){Top17("Ayarlar",back);Spacer(Modifier.height(14.dp));Setting17(Icons.Filled.VolumeUp,"Ses efektleri",sound,onSound);Setting17(Icons.Filled.MusicNote,"Arka plan müziği",music,onMusic);Setting17(Icons.Filled.Vibration,"Titreşim",haptic,onHaptic);Setting17(Icons.Filled.Refresh,"Animasyonları azalt",motion,onMotion);Spacer(Modifier.height(18.dp));Text("Sürüm ${BuildConfig.VERSION_NAME}",color=U17.Subtle,fontSize=11.sp)}}}
@Composable private fun Setting17(icon:ImageVector,title:String,value:Boolean,change:(Boolean)->Unit){Row(Modifier.fillMaxWidth().height(62.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(38.dp).background(U17.BlueSoft,CircleShape),contentAlignment=Alignment.Center){Icon(icon,null,tint=U17.Blue,modifier=Modifier.size(18.dp))};Spacer(Modifier.width(12.dp));Text(title,color=U17.Ink,fontWeight=FontWeight.Medium,modifier=Modifier.weight(1f));Switch(checked=value,onCheckedChange=change)};HorizontalDivider(color=U17.Border)}

@Composable private fun LevelUpOverlay17(level:Int,reduceMotion:Boolean,dismiss:()->Unit){val scale by animateFloatAsState(if(reduceMotion)1f else 1.06f,tween(420),label="levelUp");Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.42f)).clickable(onClick=dismiss),contentAlignment=Alignment.Center){Column(Modifier.scale(scale).padding(26.dp).fillMaxWidth(.84f).clip(RoundedCornerShape(26.dp)).background(U17.Surface).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(72.dp).background(U17.PurpleSoft,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.EmojiEvents,null,tint=U17.Purple,modifier=Modifier.size(38.dp))};Spacer(Modifier.height(14.dp));Text("SEVİYE ATLADIN",color=U17.Purple,fontWeight=FontWeight.Black,fontSize=13.sp);Text("Seviye $level",color=U17.Ink,fontWeight=FontWeight.Black,fontSize=30.sp);Spacer(Modifier.height(8.dp));Text("+${PlayerProgressStore.LEVEL_UP_COIN_REWARD} jeton ödülü",color=U17.Muted,fontSize=13.sp);Spacer(Modifier.height(18.dp));Primary17("Devam et",dismiss,Modifier.fillMaxWidth(),Icons.Filled.PlayArrow)}}}
