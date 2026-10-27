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

private val MBackground = Color(0xFFF6F7FB)
private val MSurface = Color.White
private val MInk = Color(0xFF101828)
private val MMuted = Color(0xFF667085)
private val MSubtle = Color(0xFF98A2B3)
private val MBorder = Color(0xFFE7EAF0)
private val MBlue = Color(0xFF2563EB)
private val MBlueDark = Color(0xFF1D4ED8)
private val MBlueSoft = Color(0xFFEEF4FF)
private val MGreen = Color(0xFF16A34A)
private val MGreenSoft = Color(0xFFECFDF3)
private val MGold = Color(0xFFF59E0B)
private val MGoldSoft = Color(0xFFFFF7E6)
private val MDanger = Color(0xFFEF4444)
private val trLocaleMinimal = Locale("tr", "TR")

private const val MPREFS = "wordz_progress"
private const val M_UNLOCKED = "unlocked_level"
private const val M_COINS = "coins"
private const val M_COMPLETED = "completed_levels"
private const val M_STARS = "level_stars"
private const val M_DAILY_DATE = "daily_reward_date"
private const val M_STREAK = "daily_streak"
private const val M_SOUND = "sound_enabled"
private const val M_HAPTIC = "haptic_enabled"
private const val M_REDUCE_MOTION = "reduce_motion"
private const val M_CHAPTER_CHESTS = "claimed_chapter_chests"
private const val M_TASKS = "claimed_achievements"
private const val M_WORDS = "stats_words"
private const val M_HINTS = "stats_hints"
private const val M_BEST_COMBO = "stats_best_combo"

private data class MCell(val row: Int, val col: Int)
private data class MChapter(val name: String, val subtitle: String, val start: Int, val endExclusive: Int)
private data class MStats(val completed: Int, val stars: Int, val words: Int, val hints: Int, val bestCombo: Int, val streak: Int)
private data class MTask(val id: String, val title: String, val description: String, val reward: Int, val current: (MStats) -> Int, val goal: Int)
private enum class MScreen { HOME, CHAPTERS, MAP, GAME, DAILY, TASKS, PROFILE, SETTINGS }

class MinimalMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = MBlue,
                    secondary = MGold,
                    background = MBackground,
                    surface = MSurface,
                    onPrimary = Color.White,
                    onBackground = MInk,
                    onSurface = MInk,
                )
            ) {
                MinimalWordZApp()
            }
        }
    }
}

private fun mChapters(): List<MChapter> = levels.indices.chunked(5).mapIndexed { index, chunk ->
    val names = listOf("Başlangıç", "Sahil", "Orman", "Çöl", "Gece", "Uzak Diyarlar", "Zirve")
    val subtitles = listOf("Temelleri keşfet", "Ritmi yakala", "Yeni yollar aç", "Zorlu kelimelere geç", "Gece rotasını tamamla", "Uzak diyarlara ilerle", "Son zirve")
    MChapter(names.getOrElse(index) { "Bölüm ${index + 1}" }, subtitles.getOrElse(index) { "Yeni kelimeler" }, chunk.first(), chunk.last() + 1)
}

private fun mDecodeStars(raw: Set<String>): Map<Int, Int> = raw.mapNotNull {
    val parts = it.split(':')
    val level = parts.getOrNull(0)?.toIntOrNull()
    val star = parts.getOrNull(1)?.toIntOrNull()
    if (level != null && star != null && level in levels.indices) level to star.coerceIn(0, 3) else null
}.toMap()

private fun mEncodeStars(stars: Map<Int, Int>): Set<String> = stars.map { "${it.key}:${it.value}" }.toSet()

private fun mTasks(): List<MTask> = listOf(
    MTask("first", "İlk adım", "1 level tamamla", 30, { it.completed }, 1),
    MTask("stars15", "Yıldız avcısı", "15 yıldız topla", 75, { it.stars }, 15),
    MTask("words50", "Kelime ustası", "50 doğru kelime bul", 100, { it.words }, 50),
    MTask("combo5", "Seri ustası", "Combo x5 yap", 80, { it.bestCombo }, 5),
    MTask("streak3", "İstikrar", "3 günlük seri yap", 100, { it.streak }, 3),
)

@Composable
private fun MinimalWordZApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(MPREFS, Context.MODE_PRIVATE) }

    val savedUnlocked = prefs.getInt(M_UNLOCKED, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(M_COMPLETED, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
    val migratedCompleted = if (savedCompleted.isEmpty() && savedUnlocked > 0) (0 until savedUnlocked).toSet() else savedCompleted
    val savedStars = mDecodeStars(prefs.getStringSet(M_STARS, emptySet()).orEmpty()).toMutableMap()
    migratedCompleted.forEach { if (it !in savedStars) savedStars[it] = 1 }

    var unlocked by remember { mutableIntStateOf(savedUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(M_COINS, 100)) }
    var completed by remember { mutableStateOf(migratedCompleted) }
    var stars by remember { mutableStateOf(savedStars.toMap()) }
    var dailyDate by remember { mutableStateOf(prefs.getString(M_DAILY_DATE, "").orEmpty()) }
    var streak by remember { mutableIntStateOf(prefs.getInt(M_STREAK, 0)) }
    var sound by remember { mutableStateOf(prefs.getBoolean(M_SOUND, true)) }
    var haptic by remember { mutableStateOf(prefs.getBoolean(M_HAPTIC, true)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean(M_REDUCE_MOTION, false)) }
    var chapterChests by remember { mutableStateOf(prefs.getStringSet(M_CHAPTER_CHESTS, emptySet()).orEmpty()) }
    var claimedTasks by remember { mutableStateOf(prefs.getStringSet(M_TASKS, emptySet()).orEmpty()) }
    var words by remember { mutableIntStateOf(prefs.getInt(M_WORDS, 0)) }
    var hints by remember { mutableIntStateOf(prefs.getInt(M_HINTS, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(M_BEST_COMBO, 0)) }

    var screen by remember { mutableStateOf(MScreen.HOME) }
    var selectedChapter by remember { mutableIntStateOf((unlocked / 5).coerceIn(0, mChapters().lastIndex)) }
    var selectedLevel by remember { mutableIntStateOf(unlocked) }
    var gameSession by remember { mutableIntStateOf(0) }

    val totalStars = stars.values.sum()
    val stats = MStats(completed.size, totalStars, words, hints, bestCombo, streak)
    val today = LocalDate.now()
    val dailyAvailable = dailyDate != today.toString()

    fun save(
        vUnlocked: Int = unlocked,
        vCoins: Int = coins,
        vCompleted: Set<Int> = completed,
        vStars: Map<Int, Int> = stars,
        vDailyDate: String = dailyDate,
        vStreak: Int = streak,
        vSound: Boolean = sound,
        vHaptic: Boolean = haptic,
        vReduceMotion: Boolean = reduceMotion,
        vChapterChests: Set<String> = chapterChests,
        vTasks: Set<String> = claimedTasks,
        vWords: Int = words,
        vHints: Int = hints,
        vBestCombo: Int = bestCombo,
    ) {
        prefs.edit()
            .putInt(M_UNLOCKED, vUnlocked)
            .putInt(M_COINS, vCoins)
            .putStringSet(M_COMPLETED, vCompleted.map(Int::toString).toSet())
            .putStringSet(M_STARS, mEncodeStars(vStars))
            .putString(M_DAILY_DATE, vDailyDate)
            .putInt(M_STREAK, vStreak)
            .putBoolean(M_SOUND, vSound)
            .putBoolean(M_HAPTIC, vHaptic)
            .putBoolean(M_REDUCE_MOTION, vReduceMotion)
            .putStringSet(M_CHAPTER_CHESTS, vChapterChests)
            .putStringSet(M_TASKS, vTasks)
            .putInt(M_WORDS, vWords)
            .putInt(M_HINTS, vHints)
            .putInt(M_BEST_COMBO, vBestCombo)
            .apply()
    }

    fun openLevel(index: Int) {
        selectedLevel = index
        selectedChapter = (index / 5).coerceIn(0, mChapters().lastIndex)
        gameSession += 1
        screen = MScreen.GAME
    }

    when (screen) {
        MScreen.HOME -> MHomeScreen(
            coins = coins,
            stars = totalStars,
            unlocked = unlocked,
            completed = completed.size,
            streak = streak,
            dailyAvailable = dailyAvailable,
            taskBadge = mTasks().any { it.current(stats) >= it.goal && it.id !in claimedTasks },
            onContinue = { openLevel(unlocked) },
            onChapters = { screen = MScreen.CHAPTERS },
            onDaily = { screen = MScreen.DAILY },
            onTasks = { screen = MScreen.TASKS },
            onProfile = { screen = MScreen.PROFILE },
            onSettings = { screen = MScreen.SETTINGS },
        )

        MScreen.CHAPTERS -> MChaptersScreen(
            unlocked = unlocked,
            stars = stars,
            completed = completed,
            onBack = { screen = MScreen.HOME },
            onOpen = { selectedChapter = it; screen = MScreen.MAP },
            onTasks = { screen = MScreen.TASKS },
            onProfile = { screen = MScreen.PROFILE },
            onHome = { screen = MScreen.HOME },
        )

        MScreen.MAP -> MMapScreen(
            chapterIndex = selectedChapter,
            unlocked = unlocked,
            stars = stars,
            completed = completed,
            onBack = { screen = MScreen.CHAPTERS },
            onOpenLevel = { openLevel(it) },
        )

        MScreen.GAME -> key(gameSession) {
            MGameScreen(
                levelIndex = selectedLevel,
                coins = coins,
                previousStars = stars[selectedLevel] ?: 0,
                firstCompletion = selectedLevel !in completed,
                soundEnabled = sound,
                hapticEnabled = haptic,
                reduceMotion = reduceMotion,
                onCoinsChanged = { newCoins -> coins = newCoins; save(vCoins = newCoins) },
                onWordFound = {
                    val value = words + 1
                    words = value
                    save(vWords = value)
                },
                onHintUsed = {
                    val value = hints + 1
                    hints = value
                    save(vHints = value)
                },
                onCombo = { combo ->
                    if (combo > bestCombo) {
                        bestCombo = combo
                        save(vBestCombo = combo)
                    }
                },
                onCompleted = { earned ->
                    val first = selectedLevel !in completed
                    val newCompleted = completed + selectedLevel
                    val oldStars = stars[selectedLevel] ?: 0
                    val newStars = if (earned > oldStars) stars + (selectedLevel to earned) else stars
                    val newUnlocked = if (selectedLevel >= unlocked && unlocked < levels.lastIndex) selectedLevel + 1 else unlocked
                    val newCoins = coins + if (first) 25 else 0
                    completed = newCompleted
                    stars = newStars
                    unlocked = newUnlocked
                    coins = newCoins
                    save(vUnlocked = newUnlocked, vCoins = newCoins, vCompleted = newCompleted, vStars = newStars)
                },
                onBack = { screen = MScreen.MAP },
                onReplay = { gameSession += 1 },
                onMap = { screen = MScreen.MAP },
                onNext = {
                    val chapter = mChapters()[selectedChapter]
                    if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = MScreen.MAP
                },
                onClaimChapter = {
                    val chapter = mChapters()[selectedChapter]
                    val key = selectedChapter.toString()
                    val isComplete = (chapter.start until chapter.endExclusive).all { it in completed || it == selectedLevel }
                    if (isComplete && key !in chapterChests) {
                        val newChests = chapterChests + key
                        val newCoins = coins + 150
                        chapterChests = newChests
                        coins = newCoins
                        save(vCoins = newCoins, vChapterChests = newChests)
                    }
                },
                chapterChestClaimed = selectedChapter.toString() in chapterChests,
            )
        }

        MScreen.DAILY -> MDailyScreen(
            streak = streak,
            available = dailyAvailable,
            onBack = { screen = MScreen.HOME },
            onClaim = {
                if (dailyAvailable) {
                    val newStreak = if (dailyDate == today.minusDays(1).toString()) streak + 1 else 1
                    val newCoins = coins + 40
                    val newDate = today.toString()
                    streak = newStreak
                    coins = newCoins
                    dailyDate = newDate
                    save(vCoins = newCoins, vDailyDate = newDate, vStreak = newStreak)
                }
            },
        )

        MScreen.TASKS -> MTasksScreen(
            stats = stats,
            claimed = claimedTasks,
            onBack = { screen = MScreen.HOME },
            onClaim = { task ->
                if (task.current(stats) >= task.goal && task.id !in claimedTasks) {
                    val newClaimed = claimedTasks + task.id
                    val newCoins = coins + task.reward
                    claimedTasks = newClaimed
                    coins = newCoins
                    save(vCoins = newCoins, vTasks = newClaimed)
                }
            },
            onHome = { screen = MScreen.HOME },
            onChapters = { screen = MScreen.CHAPTERS },
            onProfile = { screen = MScreen.PROFILE },
        )

        MScreen.PROFILE -> MProfileScreen(
            coins = coins,
            stats = stats,
            onBack = { screen = MScreen.HOME },
            onHome = { screen = MScreen.HOME },
            onChapters = { screen = MScreen.CHAPTERS },
            onTasks = { screen = MScreen.TASKS },
            onSettings = { screen = MScreen.SETTINGS },
        )

        MScreen.SETTINGS -> MSettingsScreen(
            sound = sound,
            haptic = haptic,
            reduceMotion = reduceMotion,
            onBack = { screen = MScreen.PROFILE },
            onSound = { sound = it; save(vSound = it) },
            onHaptic = { haptic = it; save(vHaptic = it) },
            onReduceMotion = { reduceMotion = it; save(vReduceMotion = it) },
        )
    }
}

@Composable
private fun MScreenSurface(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(MBackground).statusBarsPadding()) { content() }
}

@Composable
private fun MTopBar(title: String? = null, onBack: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            MIconButton(Icons.Filled.ArrowBack, onBack)
            Spacer(Modifier.width(12.dp))
        }
        if (title != null) Text(title, color = MInk, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
        if (trailing != null) trailing()
    }
}

@Composable
private fun MIconButton(icon: ImageVector, onClick: () -> Unit, badge: Boolean = false) {
    Box(
        Modifier.size(42.dp).clip(CircleShape).background(MSurface).border(1.dp, MBorder, CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = MInk, modifier = Modifier.size(21.dp))
        if (badge) Box(Modifier.align(Alignment.TopEnd).size(9.dp).background(MDanger, CircleShape))
    }
}

@Composable
private fun MMetricChip(icon: ImageVector, value: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(14.dp)).background(MSurface).border(1.dp, MBorder, RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Text(value, color = MInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun MCard(modifier: Modifier = Modifier, content: @Composable Column.() -> Unit) {
    Column(
        modifier.shadow(10.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x14000000), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(20.dp)).background(MSurface).padding(18.dp),
        content = content,
    )
}

@Composable
private fun MPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    Row(
        modifier.height(54.dp).clip(RoundedCornerShape(16.dp)).background(if (enabled) MBlue else Color(0xFFD0D5DD)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun MSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.height(46.dp).clip(RoundedCornerShape(14.dp)).background(MSurface).border(1.dp, MBorder, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) { Icon(icon, null, tint = MBlue, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)) }
        Text(text, color = MInk, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun MBottomNav(active: MScreen, onHome: () -> Unit, onChapters: () -> Unit, onTasks: () -> Unit, onProfile: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().height(72.dp).background(MSurface).border(1.dp, MBorder, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        MNavItem("Ana Sayfa", Icons.Filled.Home, active == MScreen.HOME, onHome)
        MNavItem("Bölümler", Icons.Filled.Map, active == MScreen.CHAPTERS, onChapters)
        MNavItem("Görevler", Icons.Filled.TaskAlt, active == MScreen.TASKS, onTasks)
        MNavItem("Profil", Icons.Filled.Person, active == MScreen.PROFILE, onProfile)
    }
}

@Composable
private fun MNavItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = if (active) MBlue else MSubtle, modifier = Modifier.size(21.dp))
        Text(label, color = if (active) MBlue else MSubtle, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun MHomeScreen(
    coins: Int,
    stars: Int,
    unlocked: Int,
    completed: Int,
    streak: Int,
    dailyAvailable: Boolean,
    taskBadge: Boolean,
    onContinue: () -> Unit,
    onChapters: () -> Unit,
    onDaily: () -> Unit,
    onTasks: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
) {
    val chapterIndex = (unlocked / 5).coerceIn(0, mChapters().lastIndex)
    val chapter = mChapters()[chapterIndex]
    val progress = completed.toFloat() / levels.size.coerceAtLeast(1)
    MScreenSurface {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 92.dp)) {
                Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("WordZ", color = MInk, fontSize = 27.sp, fontWeight = FontWeight.Black)
                        Text("Kelime bulmaca", color = MMuted, fontSize = 12.sp)
                    }
                    MMetricChip(Icons.Filled.AutoAwesome, coins.toString(), MGold)
                    Spacer(Modifier.width(8.dp))
                    MMetricChip(Icons.Filled.Star, stars.toString(), MGold)
                    Spacer(Modifier.width(8.dp))
                    MIconButton(Icons.Filled.Settings, onSettings)
                }

                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF315CF5), Color(0xFF5B52E8))))
                        .padding(22.dp)
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(alpha = 0.08f), size.minDimension * 0.52f, Offset(size.width * 0.88f, size.height * 0.15f))
                        drawCircle(Color.White.copy(alpha = 0.06f), size.minDimension * 0.36f, Offset(size.width * 0.18f, size.height * 0.92f))
                    }
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("DEVAM ET", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(chapter.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            Text("Level ${unlocked + 1} · ${levels[unlocked].title}", color = Color.White.copy(alpha = 0.86f), fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.22f),
                                )
                                Spacer(Modifier.height(7.dp))
                                Text("$completed / ${levels.size} level tamamlandı", color = Color.White.copy(alpha = 0.78f), fontSize = 11.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Box(Modifier.size(52.dp).background(Color.White, CircleShape).clickable(onClick = onContinue), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.PlayArrow, null, tint = MBlue, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("Bugün", color = MInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MQuickCard("Günlük ödül", if (dailyAvailable) "+40 jeton hazır" else "Bugün alındı", Icons.Filled.CardGiftcard, MGoldSoft, MGold, dailyAvailable, onDaily, Modifier.weight(1f))
                    MQuickCard("Görevler", if (taskBadge) "Ödül bekliyor" else "İlerlemeni gör", Icons.Filled.TaskAlt, MBlueSoft, MBlue, taskBadge, onTasks, Modifier.weight(1f))
                }

                Spacer(Modifier.height(18.dp))
                MCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(MGreenSoft, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocalFireDepartment, null, tint = MGreen)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("$streak günlük seri", color = MInk, fontWeight = FontWeight.Bold)
                            Text("Her gün geri gelerek serini koru", color = MMuted, fontSize = 12.sp)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MSubtle)
                    }
                }

                Spacer(Modifier.height(18.dp))
                MSecondaryButton("Tüm bölümleri gör", onChapters, Modifier.fillMaxWidth(), Icons.Filled.Map)
                Spacer(Modifier.height(24.dp))
            }
            Box(Modifier.align(Alignment.BottomCenter)) { MBottomNav(MScreen.HOME, {}, onChapters, onTasks, onProfile) }
        }
    }
}

@Composable
private fun MQuickCard(title: String, subtitle: String, icon: ImageVector, bg: Color, tint: Color, badge: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.height(116.dp).clip(RoundedCornerShape(20.dp)).background(MSurface).border(1.dp, MBorder, RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(38.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp)) }
            if (badge) Box(Modifier.size(8.dp).background(MDanger, CircleShape))
        }
        Column {
            Text(title, color = MInk, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = MMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MChaptersScreen(
    unlocked: Int,
    stars: Map<Int, Int>,
    completed: Set<Int>,
    onBack: () -> Unit,
    onOpen: (Int) -> Unit,
    onTasks: () -> Unit,
    onProfile: () -> Unit,
    onHome: () -> Unit,
) {
    MScreenSurface {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 88.dp)) {
                MTopBar("Bölümler", onBack)
                Text("Her bölüm 5 levelden oluşur", color = MMuted, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    mChapters().forEachIndexed { index, chapter ->
                        val levelsRange = chapter.start until chapter.endExclusive
                        val isUnlocked = unlocked >= chapter.start
                        val done = levelsRange.count { it in completed }
                        val chapterStars = levelsRange.sumOf { stars[it] ?: 0 }
                        MChapterCard(index, chapter, done, levelsRange.count(), chapterStars, isUnlocked) { onOpen(index) }
                        Spacer(Modifier.height(12.dp))
                    }
                    Spacer(Modifier.height(18.dp))
                }
            }
            Box(Modifier.align(Alignment.BottomCenter)) { MBottomNav(MScreen.CHAPTERS, onHome, {}, onTasks, onProfile) }
        }
    }
}

@Composable
private fun MChapterCard(index: Int, chapter: MChapter, done: Int, total: Int, stars: Int, unlocked: Boolean, onClick: () -> Unit) {
    val accent = listOf(Color(0xFF2563EB), Color(0xFF0891B2), Color(0xFF16A34A), Color(0xFFD97706), Color(0xFF7C3AED), Color(0xFF0F766E), Color(0xFF4F46E5))[index % 7]
    Row(
        Modifier.fillMaxWidth().height(108.dp).clip(RoundedCornerShape(20.dp)).background(MSurface).border(1.dp, MBorder, RoundedCornerShape(20.dp)).clickable(enabled = unlocked, onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(8.dp).height(70.dp).background(if (unlocked) accent else Color(0xFFD0D5DD), RoundedCornerShape(8.dp)))
        Spacer(Modifier.width(14.dp))
        Box(Modifier.size(50.dp).background(if (unlocked) accent.copy(alpha = 0.1f) else Color(0xFFF2F4F7), CircleShape), contentAlignment = Alignment.Center) {
            if (unlocked) Text("${index + 1}", color = accent, fontWeight = FontWeight.Black, fontSize = 19.sp) else Icon(Icons.Filled.Lock, null, tint = MSubtle)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(chapter.name, color = if (unlocked) MInk else MSubtle, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(chapter.subtitle, color = MMuted, fontSize = 11.sp)
            Spacer(Modifier.height(9.dp))
            LinearProgressIndicator(
                progress = { done.toFloat() / total.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = if (unlocked) accent else Color(0xFFD0D5DD),
                trackColor = Color(0xFFF2F4F7),
            )
            Spacer(Modifier.height(5.dp))
            Text(if (unlocked) "$done/$total level · $stars/${total * 3} yıldız" else "Kilitli", color = MMuted, fontSize = 10.sp)
        }
        Spacer(Modifier.width(10.dp))
        Icon(Icons.Filled.ChevronRight, null, tint = if (unlocked) MSubtle else Color(0xFFD0D5DD))
    }
}

@Composable
private fun MMapScreen(chapterIndex: Int, unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, onBack: () -> Unit, onOpenLevel: (Int) -> Unit) {
    val chapter = mChapters()[chapterIndex]
    val levelIndices = (chapter.start until chapter.endExclusive).toList()
    val nodes = listOf(0.18f to 0.82f, 0.42f to 0.64f, 0.72f to 0.48f, 0.44f to 0.31f, 0.22f to 0.14f)
    MScreenSurface {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            MTopBar("${chapterIndex + 1}. ${chapter.name}", onBack)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${levelIndices.count { it in completed }}/${levelIndices.size} tamamlandı", color = MMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("${levelIndices.sumOf { stars[it] ?: 0 }} yıldız", color = MGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            BoxWithConstraints(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(28.dp)).background(MSurface).border(1.dp, MBorder, RoundedCornerShape(28.dp))
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val path = Path()
                    nodes.forEachIndexed { index, pair ->
                        val x = size.width * pair.first
                        val y = size.height * pair.second
                        if (index == 0) path.moveTo(x, y) else {
                            val prev = nodes[index - 1]
                            val px = size.width * prev.first
                            val py = size.height * prev.second
                            val midY = (py + y) / 2f
                            path.cubicTo(px, midY, x, midY, x, y)
                        }
                    }
                    drawPath(path, color = Color(0xFFD8DEE9), style = Stroke(width = 10f, cap = StrokeCap.Round))
                    levelIndices.forEachIndexed { index, global ->
                        if (global in completed && index < nodes.lastIndex) {
                            val from = nodes[index]
                            val to = nodes[index + 1]
                            val segment = Path().apply {
                                val x1 = size.width * from.first; val y1 = size.height * from.second
                                val x2 = size.width * to.first; val y2 = size.height * to.second
                                val midY = (y1 + y2) / 2f
                                moveTo(x1, y1); cubicTo(x1, midY, x2, midY, x2, y2)
                            }
                            drawPath(segment, color = MBlue.copy(alpha = 0.55f), style = Stroke(width = 10f, cap = StrokeCap.Round))
                        }
                    }
                }
                nodes.forEachIndexed { index, pair ->
                    val global = levelIndices[index]
                    val nodeSize = 66.dp
                    val x = maxWidth * pair.first - nodeSize / 2
                    val y = maxHeight * pair.second - nodeSize / 2
                    val isUnlocked = global <= unlocked
                    val isCompleted = global in completed
                    val isCurrent = global == unlocked && !isCompleted
                    Column(Modifier.offset(x, y), horizontalAlignment = Alignment.CenterHorizontally) {
                        MLevelNode(index + 1, isUnlocked, isCompleted, isCurrent) { onOpenLevel(global) }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                            repeat(3) { s -> Icon(if (s < (stars[global] ?: 0)) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (s < (stars[global] ?: 0)) MGold else Color(0xFFD0D5DD), modifier = Modifier.size(12.dp)) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MLevelNode(number: Int, unlocked: Boolean, completed: Boolean, current: Boolean, onClick: () -> Unit) {
    val bg = when { !unlocked -> Color(0xFFF2F4F7); completed -> MGreen; current -> MBlue; else -> Color(0xFF5B8DEF) }
    Box(
        Modifier.size(62.dp).shadow(if (current) 10.dp else 4.dp, CircleShape).background(bg, CircleShape)
            .border(if (current) 3.dp else 1.dp, if (current) MBlueDark else Color.White, CircleShape).clickable(enabled = unlocked, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (!unlocked) Icon(Icons.Filled.Lock, null, tint = MSubtle, modifier = Modifier.size(22.dp))
        else if (completed) Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(27.dp))
        else Text(number.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
    }
}

@Composable
private fun MGameScreen(
    levelIndex: Int,
    coins: Int,
    previousStars: Int,
    firstCompletion: Boolean,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    reduceMotion: Boolean,
    onCoinsChanged: (Int) -> Unit,
    onWordFound: () -> Unit,
    onHintUsed: () -> Unit,
    onCombo: (Int) -> Unit,
    onCompleted: (Int) -> Unit,
    onBack: () -> Unit,
    onReplay: () -> Unit,
    onMap: () -> Unit,
    onNext: () -> Unit,
    onClaimChapter: () -> Unit,
    chapterChestClaimed: Boolean,
) {
    val level = levels[levelIndex]
    val haptics = LocalHapticFeedback.current
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 60) }
    DisposableEffect(Unit) { onDispose { tone.release() } }

    var wheelLetters by remember { mutableStateOf(level.letters) }
    var foundWords by remember { mutableStateOf(setOf<String>()) }
    var bonusWords by remember { mutableStateOf(setOf<String>()) }
    var hintedCells by remember { mutableStateOf(setOf<MCell>()) }
    var selection by remember { mutableStateOf(emptyList<Int>()) }
    var liveWord by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Harfleri birleştir") }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var completionHandled by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var sessionNet by remember { mutableIntStateOf(0) }

    val complete = level.targets.all { it in foundWords }
    val earnedStars = when (hintsUsed) { 0 -> 3; 1 -> 2; 2 -> 1; else -> 0 }
    val chapter = mChapters()[levelIndex / 5]
    val chapterEnd = levelIndex == chapter.endExclusive - 1

    LaunchedEffect(complete) {
        if (complete && !completionHandled) {
            completionHandled = true
            onCompleted(earnedStars)
            if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_ACK, 160)
            if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (!reduceMotion) delay(350)
            showResult = true
        }
    }

    val wordScale by animateFloatAsState(if (liveWord.isNotBlank()) 1.03f else 1f, tween(if (reduceMotion) 0 else 120), label = "wordScale")

    MScreenSurface {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxHeight < 760.dp
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
                    MIconButton(Icons.Filled.ArrowBack, onBack)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Level ${levelIndex + 1}", color = MInk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(level.title, color = MMuted, fontSize = 11.sp)
                    }
                    MMetricChip(Icons.Filled.AutoAwesome, coins.toString(), MGold)
                }

                Spacer(Modifier.height(if (compact) 4.dp else 10.dp))
                MBoard(level, foundWords, hintedCells, compact)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${foundWords.size}/${level.targets.size} kelime", color = MMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    if (combo >= 2) Text("Combo x$combo", color = MGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    else Row { repeat(3) { s -> Icon(if (s < earnedStars) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (s < earnedStars) MGold else Color(0xFFD0D5DD), modifier = Modifier.size(16.dp)) } }
                }

                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(46.dp).scale(wordScale).clip(RoundedCornerShape(14.dp)).background(if (liveWord.isNotBlank()) MBlueSoft else MSurface).border(1.dp, MBorder, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Text(if (liveWord.isNotBlank()) liveWord else status, color = if (liveWord.isNotBlank()) MBlueDark else MMuted, fontWeight = if (liveWord.isNotBlank()) FontWeight.Bold else FontWeight.Medium, fontSize = if (liveWord.isNotBlank()) 20.sp else 12.sp)
                }

                Spacer(Modifier.height(if (compact) 2.dp else 8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    MLetterWheel(
                        letters = wheelLetters,
                        selected = selection,
                        sizeDp = if (compact) 218.dp else 252.dp,
                        onSelectionChanged = {
                            selection = it
                            liveWord = it.joinToString("") { index -> wheelLetters[index].toString() }
                        },
                        onFinished = {
                            val submitted = liveWord.trim().uppercase(trLocaleMinimal)
                            when {
                                submitted in level.targets && submitted !in foundWords -> {
                                    foundWords = foundWords + submitted
                                    combo += 1
                                    onWordFound(); onCombo(combo)
                                    val extra = if (combo % 3 == 0) 10 else 0
                                    val gain = 5 + extra
                                    sessionNet += gain
                                    onCoinsChanged(coins + gain)
                                    status = if (extra > 0) "Combo x$combo · +$gain jeton" else "Doğru · $submitted"
                                    if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 90)
                                    if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                submitted in foundWords -> status = "Bu kelime zaten bulundu"
                                submitted in level.bonusWords && submitted !in bonusWords -> {
                                    bonusWords = bonusWords + submitted
                                    combo += 1
                                    onWordFound(); onCombo(combo)
                                    val gain = 10 + if (combo % 3 == 0) 10 else 0
                                    sessionNet += gain
                                    onCoinsChanged(coins + gain)
                                    status = "Bonus kelime · +$gain jeton"
                                }
                                submitted in bonusWords -> status = "Bu bonus kelime zaten bulundu"
                                submitted.isNotBlank() -> {
                                    combo = 0
                                    status = "Kelime bulunamadı"
                                    if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_NACK, 80)
                                }
                            }
                            selection = emptyList(); liveWord = ""
                        },
                    )
                }

                Spacer(Modifier.height(if (compact) 2.dp else 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MToolButton("Karıştır", Icons.Filled.Shuffle, true, Modifier.weight(1f)) {
                        wheelLetters = wheelLetters.shuffled(); selection = emptyList(); liveWord = ""; status = "Harfler karıştırıldı"
                    }
                    MToolButton("Harf ·25", Icons.Filled.Lightbulb, coins >= 25 && !complete, Modifier.weight(1f)) {
                        val hidden = mAllCells(level).filter { it !in hintedCells && !mCellRevealed(level, it, foundWords) }
                        hidden.randomOrNull()?.let {
                            onCoinsChanged(coins - 25); sessionNet -= 25; hintedCells = hintedCells + it; hintsUsed += 1; combo = 0; onHintUsed(); status = "Bir harf açıldı"
                        }
                    }
                    MToolButton("Kelime ·60", Icons.Filled.Search, coins >= 60 && !complete, Modifier.weight(1f)) {
                        level.targets.firstOrNull { it !in foundWords }?.let {
                            onCoinsChanged(coins - 60); sessionNet -= 60; foundWords = foundWords + it; hintsUsed += 2; combo = 0; onHintUsed(); status = "Kelime açıldı"
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            if (showResult) {
                MResultOverlay(
                    levelIndex = levelIndex,
                    stars = earnedStars,
                    previousStars = previousStars,
                    found = foundWords.size,
                    targetCount = level.targets.size,
                    bonus = bonusWords.size,
                    hints = hintsUsed,
                    combo = combo,
                    sessionNet = sessionNet + if (firstCompletion) 25 else 0,
                    chapterEnd = chapterEnd,
                    chapterChestClaimed = chapterChestClaimed,
                    onReplay = onReplay,
                    onMap = onMap,
                    onNext = onNext,
                    onClaimChapter = onClaimChapter,
                )
            }
        }
    }
}

@Composable
private fun MToolButton(label: String, icon: ImageVector, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier.height(58.dp).clip(RoundedCornerShape(16.dp)).background(if (enabled) MSurface else Color(0xFFF2F4F7)).border(1.dp, MBorder, RoundedCornerShape(16.dp)).clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = if (enabled) MBlue else MSubtle, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = if (enabled) MInk else MSubtle, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun mCellsFor(p: Placement): List<Pair<MCell, Char>> = p.word.mapIndexed { index, char ->
    MCell(p.row + if (p.horizontal) 0 else index, p.col + if (p.horizontal) index else 0) to char
}
private fun mAllCells(level: Level): Set<MCell> = level.placements.flatMap(::mCellsFor).map { it.first }.toSet()
private fun mCellRevealed(level: Level, cell: MCell, found: Set<String>): Boolean = level.placements.any { it.word in found && mCellsFor(it).any { pair -> pair.first == cell } }

@Composable
private fun MBoard(level: Level, found: Set<String>, hinted: Set<MCell>, compact: Boolean) {
    val entries = remember(level) { level.placements.flatMap(::mCellsFor) }
    val map = remember(level) { entries.associate { it.first to it.second } }
    val rows = (map.keys.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (map.keys.maxOfOrNull { it.col } ?: 0) + 1
    val cell = when { compact && cols >= 5 -> 33.dp; cols >= 6 -> 34.dp; cols == 5 -> 39.dp; else -> 42.dp }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MSurface).border(1.dp, MBorder, RoundedCornerShape(20.dp)).padding(if (compact) 10.dp else 14.dp), contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { r ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(cols) { c ->
                        val pos = MCell(r, c)
                        val letter = map[pos]
                        if (letter == null) Spacer(Modifier.size(cell)) else {
                            val reveal = pos in hinted || mCellRevealed(level, pos, found)
                            Box(
                                Modifier.size(cell).clip(RoundedCornerShape(9.dp)).background(if (reveal) MBlue else Color(0xFFF2F4F7)).border(1.dp, if (reveal) MBlueDark else Color(0xFFDDE2EA), RoundedCornerShape(9.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (reveal) Text(letter.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = if (compact) 16.sp else 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MLetterWheel(letters: List<Char>, selected: List<Int>, sizeDp: Dp, onSelectionChanged: (List<Int>) -> Unit, onFinished: () -> Unit) {
    var measured by remember { mutableStateOf(IntSize.Zero) }
    var pointer by remember { mutableStateOf<Offset?>(null) }
    fun positions(): List<Offset> {
        if (measured == IntSize.Zero) return emptyList()
        val center = Offset(measured.width / 2f, measured.height / 2f)
        val radius = minOf(measured.width, measured.height) * 0.34f
        return letters.indices.map { index ->
            val angle = -PI / 2 + 2 * PI * index / letters.size
            Offset(center.x + (cos(angle) * radius).toFloat(), center.y + (sin(angle) * radius).toFloat())
        }
    }
    fun hit(point: Offset): Int? {
        val pts = positions(); if (pts.isEmpty()) return null
        val hitRadius = minOf(measured.width, measured.height) * 0.13f
        return pts.indices.firstOrNull { hypot((point.x - pts[it].x).toDouble(), (point.y - pts[it].y).toDouble()) <= hitRadius }
    }
    Box(
        Modifier.size(sizeDp).onSizeChanged { measured = it }.pointerInput(letters, measured) {
            var gesture = emptyList<Int>()
            detectDragGestures(
                onDragStart = { p ->
                    pointer = p
                    val i = hit(p); gesture = if (i != null) listOf(i) else emptyList(); onSelectionChanged(gesture)
                },
                onDragEnd = { pointer = null; onFinished(); gesture = emptyList() },
                onDragCancel = { pointer = null; onFinished(); gesture = emptyList() },
                onDrag = { change, _ ->
                    pointer = change.position
                    val i = hit(change.position)
                    if (i != null) {
                        if (gesture.size >= 2 && i == gesture[gesture.lastIndex - 1]) gesture = gesture.dropLast(1)
                        else if (i !in gesture) gesture = gesture + i
                        onSelectionChanged(gesture)
                    }
                },
            )
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) * 0.47f
            drawCircle(MSurface, radius, center)
            drawCircle(Color(0xFFE4E7EC), radius, center, style = Stroke(width = 3f))
            val pts = positions()
            selected.zipWithNext().forEach { (a, b) -> if (a in pts.indices && b in pts.indices) drawLine(MBlue, pts[a], pts[b], 10f, cap = StrokeCap.Round) }
            if (selected.isNotEmpty() && selected.last() in pts.indices && pointer != null) drawLine(MBlue.copy(alpha = 0.6f), pts[selected.last()], pointer!!, 8f, cap = StrokeCap.Round)
            pts.forEachIndexed { index, point ->
                val isSelected = index in selected
                val r = minOf(size.width, size.height) * if (isSelected) 0.112f else 0.102f
                drawCircle(if (isSelected) MBlue else Color(0xFFF9FAFB), r, point)
                drawCircle(if (isSelected) MBlueDark else Color(0xFFD0D5DD), r, point, style = Stroke(width = 2.5f))
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.rgb(16, 24, 40)
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = minOf(size.width, size.height) * 0.105f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val metrics = paint.fontMetrics
                    canvas.nativeCanvas.drawText(letters[index].toString(), point.x, point.y - (metrics.ascent + metrics.descent) / 2f, paint)
                }
            }
            drawCircle(Color(0xFFF2F4F7), minOf(size.width, size.height) * 0.085f, center)
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply { color = android.graphics.Color.rgb(102, 112, 133); textAlign = android.graphics.Paint.Align.CENTER; textSize = minOf(size.width, size.height) * 0.10f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
                val metrics = paint.fontMetrics
                canvas.nativeCanvas.drawText("↻", center.x, center.y - (metrics.ascent + metrics.descent) / 2f, paint)
            }
        }
    }
}

@Composable
private fun MResultOverlay(
    levelIndex: Int,
    stars: Int,
    previousStars: Int,
    found: Int,
    targetCount: Int,
    bonus: Int,
    hints: Int,
    combo: Int,
    sessionNet: Int,
    chapterEnd: Boolean,
    chapterChestClaimed: Boolean,
    onReplay: () -> Unit,
    onMap: () -> Unit,
    onNext: () -> Unit,
    onClaimChapter: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.42f)).padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
        MCard(Modifier.fillMaxWidth()) {
            Text(if (chapterEnd) "Bölüm tamamlandı" else "Level tamamlandı", color = MInk, fontWeight = FontWeight.Black, fontSize = 24.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(3) { index ->
                    Icon(if (index < stars) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (index < stars) MGold else Color(0xFFD0D5DD), modifier = Modifier.size(if (index == 1) 42.dp else 34.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(if (stars > previousStars) "Yeni en iyi skor" else "En iyi skor: $previousStars yıldız", color = if (stars > previousStars) MBlue else MMuted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            MResultRow("Bulunan kelime", "$found/$targetCount")
            MResultRow("Bonus kelime", bonus.toString())
            MResultRow("Kullanılan ipucu", hints.toString())
            MResultRow("En yüksek combo", "x$combo")
            HorizontalDivider(color = MBorder, modifier = Modifier.padding(vertical = 10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Bu oyundaki net jeton", color = MMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(if (sessionNet >= 0) "+$sessionNet" else sessionNet.toString(), color = if (sessionNet >= 0) MGreen else MDanger, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
            if (chapterEnd && !chapterChestClaimed) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MGoldSoft).clickable(onClick = onClaimChapter).padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CardGiftcard, null, tint = MGold)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text("Bölüm ödülü", color = MInk, fontWeight = FontWeight.Bold); Text("+150 jeton", color = MMuted, fontSize = 11.sp) }
                        Text("Al", color = MGold, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            MPrimaryButton(if (chapterEnd) "Haritaya dön" else "Sonraki level", if (chapterEnd) onMap else onNext, Modifier.fillMaxWidth(), if (chapterEnd) Icons.Filled.Map else Icons.Filled.PlayArrow)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MSecondaryButton("Tekrar oyna", onReplay, Modifier.weight(1f), Icons.Filled.Refresh)
                if (!chapterEnd) MSecondaryButton("Harita", onMap, Modifier.weight(1f), Icons.Filled.Map)
            }
        }
    }
}

@Composable
private fun MResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, color = MMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = MInk, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun MDailyScreen(streak: Int, available: Boolean, onBack: () -> Unit, onClaim: () -> Unit) {
    MScreenSurface {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            MTopBar("Günlük ödül", onBack)
            Text("Serini koru ve ödülleri büyüt", color = MMuted, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))
            MCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(MGoldSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.LocalFireDepartment, null, tint = MGold) }
                    Spacer(Modifier.width(12.dp))
                    Column { Text("$streak günlük seri", color = MInk, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text("Bugünkü ödül +40 jeton", color = MMuted, fontSize = 12.sp) }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(7) { day ->
                    val active = day == (streak % 7).coerceAtMost(6)
                    Column(Modifier.weight(1f).height(92.dp).clip(RoundedCornerShape(14.dp)).background(if (active) MBlueSoft else MSurface).border(1.dp, if (active) MBlue.copy(alpha = 0.35f) else MBorder, RoundedCornerShape(14.dp)), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("${day + 1}", color = if (active) MBlue else MMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(7.dp))
                        Icon(if (day == 6) Icons.Filled.CardGiftcard else Icons.Filled.AutoAwesome, null, tint = if (active) MGold else MSubtle, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(5.dp))
                        Text(if (day == 6) "+200" else "+${40 + day * 10}", color = MInk, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            MPrimaryButton(if (available) "Bugünün ödülünü al" else "Bugünkü ödül alındı", onClaim, Modifier.fillMaxWidth().padding(bottom = 24.dp), Icons.Filled.CardGiftcard, available)
        }
    }
}

@Composable
private fun MTasksScreen(stats: MStats, claimed: Set<String>, onBack: () -> Unit, onClaim: (MTask) -> Unit, onHome: () -> Unit, onChapters: () -> Unit, onProfile: () -> Unit) {
    MScreenSurface {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 88.dp)) {
                MTopBar("Görevler", onBack)
                Text("Oynadıkça ilerler, tamamladıkça kazanırsın", color = MMuted, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    mTasks().forEach { task ->
                        val current = task.current(stats).coerceAtMost(task.goal)
                        val done = current >= task.goal
                        val isClaimed = task.id in claimed
                        MCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(42.dp).background(if (done) MGreenSoft else MBlueSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(if (done) Icons.Filled.CheckCircle else Icons.Filled.TaskAlt, null, tint = if (done) MGreen else MBlue, modifier = Modifier.size(20.dp)) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) { Text(task.title, color = MInk, fontWeight = FontWeight.Bold); Text(task.description, color = MMuted, fontSize = 11.sp) }
                                Text("+${task.reward}", color = MGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(progress = { current.toFloat() / task.goal.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = if (done) MGreen else MBlue, trackColor = Color(0xFFF2F4F7))
                            Spacer(Modifier.height(7.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("$current/${task.goal}", color = MMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                Text(when { isClaimed -> "Alındı"; done -> "Ödülü al"; else -> "Devam ediyor" }, color = when { isClaimed -> MSubtle; done -> MBlue; else -> MMuted }, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.clickable(enabled = done && !isClaimed) { onClaim(task) })
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            Box(Modifier.align(Alignment.BottomCenter)) { MBottomNav(MScreen.TASKS, onHome, onChapters, {}, onProfile) }
        }
    }
}

@Composable
private fun MProfileScreen(coins: Int, stats: MStats, onBack: () -> Unit, onHome: () -> Unit, onChapters: () -> Unit, onTasks: () -> Unit, onSettings: () -> Unit) {
    val level = 1 + stats.stars / 15
    val xp = (stats.stars % 15) / 15f
    MScreenSurface {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 88.dp)) {
                MTopBar("Profil", onBack) { MIconButton(Icons.Filled.Settings, onSettings) }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(72.dp).background(MBlueSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, tint = MBlue, modifier = Modifier.size(34.dp)) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Oyuncu", color = MInk, fontWeight = FontWeight.Black, fontSize = 21.sp)
                        Text("Seviye $level", color = MMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { xp }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = MBlue, trackColor = Color(0xFFEFF1F5))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MStatTile("Yıldız", stats.stars.toString(), Icons.Filled.Star, MGold, Modifier.weight(1f))
                    MStatTile("Level", stats.completed.toString(), Icons.Filled.CheckCircle, MGreen, Modifier.weight(1f))
                    MStatTile("Jeton", coins.toString(), Icons.Filled.AutoAwesome, MBlue, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MStatTile("Kelime", stats.words.toString(), Icons.Filled.TaskAlt, MBlue, Modifier.weight(1f))
                    MStatTile("İpucu", stats.hints.toString(), Icons.Filled.Lightbulb, MGold, Modifier.weight(1f))
                    MStatTile("Combo", "x${stats.bestCombo}", Icons.Filled.LocalFireDepartment, MDanger, Modifier.weight(1f))
                }
                Spacer(Modifier.height(18.dp))
                MCard(Modifier.fillMaxWidth()) {
                    Text("Günlük seri", color = MMuted, fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("${stats.streak} gün", color = MInk, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }
            Box(Modifier.align(Alignment.BottomCenter)) { MBottomNav(MScreen.PROFILE, onHome, onChapters, onTasks, {}) }
        }
    }
}

@Composable
private fun MStatTile(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Column(modifier.height(108.dp).clip(RoundedCornerShape(18.dp)).background(MSurface).border(1.dp, MBorder, RoundedCornerShape(18.dp)).padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp))
        Column { Text(value, color = MInk, fontWeight = FontWeight.Black, fontSize = 20.sp); Text(label, color = MMuted, fontSize = 10.sp) }
    }
}

@Composable
private fun MSettingsScreen(sound: Boolean, haptic: Boolean, reduceMotion: Boolean, onBack: () -> Unit, onSound: (Boolean) -> Unit, onHaptic: (Boolean) -> Unit, onReduceMotion: (Boolean) -> Unit) {
    MScreenSurface {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            MTopBar("Ayarlar", onBack)
            Spacer(Modifier.height(16.dp))
            MCard(Modifier.fillMaxWidth()) {
                MSettingRow(Icons.Filled.VolumeUp, "Ses efektleri", "Doğru, yanlış ve ödül sesleri", sound, onSound)
                HorizontalDivider(color = MBorder, modifier = Modifier.padding(vertical = 8.dp))
                MSettingRow(Icons.Filled.Vibration, "Titreşim", "Harf ve başarı geri bildirimi", haptic, onHaptic)
                HorizontalDivider(color = MBorder, modifier = Modifier.padding(vertical = 8.dp))
                MSettingRow(Icons.Filled.AutoAwesome, "Animasyonları azalt", "Daha sakin geçişler kullan", reduceMotion, onReduceMotion)
            }
            Spacer(Modifier.height(16.dp))
            MCard(Modifier.fillMaxWidth()) {
                Text("WordZ", color = MInk, fontWeight = FontWeight.Bold)
                Text("Sürüm ${BuildConfig.VERSION_NAME}", color = MMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MSettingRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(MBlueSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MBlue, modifier = Modifier.size(19.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, color = MInk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); Text(subtitle, color = MMuted, fontSize = 10.sp) }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
