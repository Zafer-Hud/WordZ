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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redeem
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

private val WzBackground = Color(0xFFF5F8FD)
private val WzSurface = Color.White
private val WzInk = Color(0xFF13254A)
private val WzMuted = Color(0xFF7383A3)
private val WzBlue = Color(0xFF2D7FF9)
private val WzBlueDark = Color(0xFF1762D5)
private val WzBlueSoft = Color(0xFFEAF3FF)
private val WzGreen = Color(0xFF35BE70)
private val WzGreenDark = Color(0xFF1C9B56)
private val WzGold = Color(0xFFFFB928)
private val WzGoldDark = Color(0xFFEA8F16)
private val WzBorder = Color(0xFFE4EAF3)
private val WzRed = Color(0xFFF05E6E)
private val WzShadow = Color(0x1A17345F)
private val trLocalePremium = Locale("tr", "TR")

private const val PREFS_PREMIUM = "wordz_progress"
private const val KEY_UNLOCKED = "unlocked_level"
private const val KEY_COINS = "coins"
private const val KEY_COMPLETED = "completed_levels"
private const val KEY_STARS = "level_stars"
private const val KEY_DAILY_DATE = "daily_reward_date"
private const val KEY_STREAK = "daily_streak"
private const val KEY_SOUND = "sound_enabled"
private const val KEY_HAPTIC = "haptic_enabled"
private const val KEY_REDUCE_MOTION = "reduce_motion"
private const val KEY_CHAPTER_CHESTS = "claimed_chapter_chests"
private const val KEY_ACHIEVEMENTS = "claimed_achievements"
private const val KEY_WORDS = "stats_words"
private const val KEY_HINTS = "stats_hints"
private const val KEY_BEST_COMBO = "stats_best_combo"

private data class UiCell(val row: Int, val col: Int)
private data class UiChapter(val name: String, val subtitle: String, val start: Int, val endExclusive: Int)
private data class UiStats(val completed: Int, val stars: Int, val words: Int, val hints: Int, val bestCombo: Int, val streak: Int)
private data class UiTask(val id: String, val title: String, val reward: Int, val current: (UiStats) -> Int, val goal: Int)
private enum class UiScreen { HOME, CHAPTERS, MAP, GAME, DAILY, TASKS, PROFILE, SETTINGS }

class PremiumMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = WzBlue,
                    secondary = WzGold,
                    background = WzBackground,
                    surface = WzSurface,
                    onPrimary = Color.White,
                    onBackground = WzInk,
                    onSurface = WzInk,
                )
            ) {
                PremiumWordZApp()
            }
        }
    }
}

private fun uiChapters(): List<UiChapter> = levels.indices.chunked(5).mapIndexed { index, chunk ->
    val names = listOf("Başlangıç Vadisi", "Sahil Yolu", "Yeşil Orman", "Çöl Yolculuğu", "Gece Rotası", "Uzak Diyarlar", "Zafer Zirvesi")
    val subs = listOf("Kelime yolculuğuna başla", "Deniz kıyısında yeni kelimeler", "Ormanın saklı yolları", "Sıcak kumların meydan okuması", "Yıldızların altında ilerle", "Yeni diyarlara açıl", "Zirveye son adımlar")
    UiChapter(names.getOrElse(index) { "Yeni Bölüm" }, subs.getOrElse(index) { "Yeni kelimeler seni bekliyor" }, chunk.first(), chunk.last() + 1)
}

private fun decodeStars(raw: Set<String>): Map<Int, Int> = raw.mapNotNull {
    val parts = it.split(':')
    val level = parts.getOrNull(0)?.toIntOrNull()
    val stars = parts.getOrNull(1)?.toIntOrNull()
    if (level != null && stars != null && level in levels.indices) level to stars.coerceIn(0, 3) else null
}.toMap()

private fun encodeStars(stars: Map<Int, Int>): Set<String> = stars.map { "${it.key}:${it.value}" }.toSet()

private fun uiTasks(): List<UiTask> = listOf(
    UiTask("first", "1 level tamamla", 30, { it.completed }, 1),
    UiTask("stars15", "15 yıldız topla", 75, { it.stars }, 15),
    UiTask("words50", "50 doğru kelime bul", 100, { it.words }, 50),
    UiTask("combo5", "Combo x5 yap", 80, { it.bestCombo }, 5),
    UiTask("streak3", "3 günlük seri yap", 100, { it.streak }, 3),
)

@Composable
private fun PremiumWordZApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_PREMIUM, Context.MODE_PRIVATE) }

    val savedUnlocked = prefs.getInt(KEY_UNLOCKED, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(KEY_COMPLETED, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
    val migratedCompleted = if (savedCompleted.isEmpty() && savedUnlocked > 0) (0 until savedUnlocked).toSet() else savedCompleted
    val savedStars = decodeStars(prefs.getStringSet(KEY_STARS, emptySet()).orEmpty()).toMutableMap()
    migratedCompleted.forEach { if (it !in savedStars) savedStars[it] = 1 }

    var unlocked by remember { mutableIntStateOf(savedUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(KEY_COINS, 100)) }
    var completed by remember { mutableStateOf(migratedCompleted) }
    var stars by remember { mutableStateOf(savedStars.toMap()) }
    var dailyDate by remember { mutableStateOf(prefs.getString(KEY_DAILY_DATE, "").orEmpty()) }
    var streak by remember { mutableIntStateOf(prefs.getInt(KEY_STREAK, 0)) }
    var sound by remember { mutableStateOf(prefs.getBoolean(KEY_SOUND, true)) }
    var haptic by remember { mutableStateOf(prefs.getBoolean(KEY_HAPTIC, true)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean(KEY_REDUCE_MOTION, false)) }
    var chapterChests by remember { mutableStateOf(prefs.getStringSet(KEY_CHAPTER_CHESTS, emptySet()).orEmpty()) }
    var claimedTasks by remember { mutableStateOf(prefs.getStringSet(KEY_ACHIEVEMENTS, emptySet()).orEmpty()) }
    var totalWords by remember { mutableIntStateOf(prefs.getInt(KEY_WORDS, 0)) }
    var totalHints by remember { mutableIntStateOf(prefs.getInt(KEY_HINTS, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(KEY_BEST_COMBO, 0)) }

    var screen by remember { mutableStateOf(UiScreen.HOME) }
    var selectedChapter by remember { mutableIntStateOf((unlocked / 5).coerceIn(0, uiChapters().lastIndex)) }
    var selectedLevel by remember { mutableIntStateOf(unlocked) }
    var gameSession by remember { mutableIntStateOf(0) }

    val totalStars = stars.values.sum()
    val stats = UiStats(completed.size, totalStars, totalWords, totalHints, bestCombo, streak)
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
        vClaimedTasks: Set<String> = claimedTasks,
        vWords: Int = totalWords,
        vHints: Int = totalHints,
        vBestCombo: Int = bestCombo,
    ) {
        prefs.edit()
            .putInt(KEY_UNLOCKED, vUnlocked)
            .putInt(KEY_COINS, vCoins)
            .putStringSet(KEY_COMPLETED, vCompleted.map(Int::toString).toSet())
            .putStringSet(KEY_STARS, encodeStars(vStars))
            .putString(KEY_DAILY_DATE, vDailyDate)
            .putInt(KEY_STREAK, vStreak)
            .putBoolean(KEY_SOUND, vSound)
            .putBoolean(KEY_HAPTIC, vHaptic)
            .putBoolean(KEY_REDUCE_MOTION, vReduceMotion)
            .putStringSet(KEY_CHAPTER_CHESTS, vChapterChests)
            .putStringSet(KEY_ACHIEVEMENTS, vClaimedTasks)
            .putInt(KEY_WORDS, vWords)
            .putInt(KEY_HINTS, vHints)
            .putInt(KEY_BEST_COMBO, vBestCombo)
            .apply()
    }

    fun openLevel(index: Int) {
        selectedLevel = index
        selectedChapter = (index / 5).coerceIn(0, uiChapters().lastIndex)
        gameSession += 1
        screen = UiScreen.GAME
    }

    when (screen) {
        UiScreen.HOME -> PremiumHomeScreen(
            coins = coins,
            stars = totalStars,
            unlockedLevel = unlocked,
            dailyAvailable = dailyAvailable,
            taskBadge = uiTasks().any { it.current(stats) >= it.goal && it.id !in claimedTasks },
            onContinue = { openLevel(unlocked) },
            onChapters = { screen = UiScreen.CHAPTERS },
            onDaily = { screen = UiScreen.DAILY },
            onTasks = { screen = UiScreen.TASKS },
            onProfile = { screen = UiScreen.PROFILE },
            onSettings = { screen = UiScreen.SETTINGS },
        )

        UiScreen.CHAPTERS -> PremiumChapterScreen(
            unlocked = unlocked,
            stars = stars,
            completed = completed,
            chapterChests = chapterChests,
            onBack = { screen = UiScreen.HOME },
            onOpen = { selectedChapter = it; screen = UiScreen.MAP },
        )

        UiScreen.MAP -> PremiumMapScreen(
            chapterIndex = selectedChapter,
            unlocked = unlocked,
            stars = stars,
            completed = completed,
            onBack = { screen = UiScreen.CHAPTERS },
            onOpenLevel = { openLevel(it) },
        )

        UiScreen.GAME -> key(gameSession) {
            PremiumGameScreen(
                levelIndex = selectedLevel,
                initialCoins = coins,
                previousStars = stars[selectedLevel] ?: 0,
                firstCompletion = selectedLevel !in completed,
                soundEnabled = sound,
                hapticEnabled = haptic,
                reduceMotion = reduceMotion,
                onCoinsChanged = { newCoins -> coins = newCoins; save(vCoins = newCoins) },
                onWordFound = {
                    val newValue = totalWords + 1
                    totalWords = newValue
                    save(vWords = newValue)
                },
                onHintUsed = {
                    val newValue = totalHints + 1
                    totalHints = newValue
                    save(vHints = newValue)
                },
                onCombo = { combo ->
                    if (combo > bestCombo) {
                        bestCombo = combo
                        save(vBestCombo = combo)
                    }
                },
                onCompleted = { earnedStars ->
                    val first = selectedLevel !in completed
                    val newCompleted = completed + selectedLevel
                    val oldStars = stars[selectedLevel] ?: 0
                    val newStars = if (earnedStars > oldStars) stars + (selectedLevel to earnedStars) else stars
                    val newUnlocked = if (selectedLevel >= unlocked && unlocked < levels.lastIndex) selectedLevel + 1 else unlocked
                    val newCoins = coins + if (first) 25 else 0
                    completed = newCompleted
                    stars = newStars
                    unlocked = newUnlocked
                    coins = newCoins
                    save(vUnlocked = newUnlocked, vCoins = newCoins, vCompleted = newCompleted, vStars = newStars)
                },
                onBack = { screen = UiScreen.MAP },
                onReplay = { gameSession += 1 },
                onMap = { screen = UiScreen.MAP },
                onNext = {
                    val chapter = uiChapters()[selectedChapter]
                    if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) {
                        openLevel(selectedLevel + 1)
                    } else {
                        screen = UiScreen.MAP
                    }
                },
                onClaimChapter = {
                    val chapter = uiChapters()[selectedChapter]
                    val key = selectedChapter.toString()
                    val done = (chapter.start until chapter.endExclusive).all { it in completed }
                    if (done && key !in chapterChests) {
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

        UiScreen.DAILY -> PremiumDailyScreen(
            streak = streak,
            available = dailyAvailable,
            onBack = { screen = UiScreen.HOME },
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

        UiScreen.TASKS -> PremiumTasksScreen(
            stats = stats,
            claimed = claimedTasks,
            onBack = { screen = UiScreen.HOME },
            onClaim = { task ->
                if (task.current(stats) >= task.goal && task.id !in claimedTasks) {
                    val newClaimed = claimedTasks + task.id
                    val newCoins = coins + task.reward
                    claimedTasks = newClaimed
                    coins = newCoins
                    save(vCoins = newCoins, vClaimedTasks = newClaimed)
                }
            },
        )

        UiScreen.PROFILE -> PremiumProfileScreen(
            coins = coins,
            totalStars = totalStars,
            completed = completed.size,
            words = totalWords,
            hints = totalHints,
            bestCombo = bestCombo,
            streak = streak,
            onBack = { screen = UiScreen.HOME },
        )

        UiScreen.SETTINGS -> PremiumSettingsScreen(
            sound = sound,
            haptic = haptic,
            reduceMotion = reduceMotion,
            onBack = { screen = UiScreen.HOME },
            onSound = { sound = it; save(vSound = it) },
            onHaptic = { haptic = it; save(vHaptic = it) },
            onReduceMotion = { reduceMotion = it; save(vReduceMotion = it) },
        )
    }
}

@Composable
private fun PremiumHomeScreen(
    coins: Int,
    stars: Int,
    unlockedLevel: Int,
    dailyAvailable: Boolean,
    taskBadge: Boolean,
    onContinue: () -> Unit,
    onChapters: () -> Unit,
    onDaily: () -> Unit,
    onTasks: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
) {
    PremiumScaffold(active = UiScreen.HOME, onHome = {}, onChapters = onChapters, onTasks = onTasks, onProfile = onProfile) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            PremiumTopHud(coins, stars, onProfile, onSettings)
            Spacer(Modifier.height(16.dp))

            ScenicHero(Modifier.fillMaxWidth().height(250.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("Word", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Black)
                        Text("Z", color = WzGold, fontSize = 62.sp, fontWeight = FontWeight.Black)
                    }
                    Text("KELİMELERLE YENİ UFUKLARA", color = Color.White.copy(alpha = 0.88f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp)
                }
            }

            Spacer(Modifier.height(14.dp))
            PremiumCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiniLandscapeThumb()
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Devam Et", color = WzInk, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("${unlockedLevel / 5 + 1}. Bölüm · ${uiChapters()[(unlockedLevel / 5).coerceIn(0, uiChapters().lastIndex)].name}", color = WzMuted, fontSize = 12.sp)
                        Text("${unlockedLevel + 1}. Level", color = WzBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    CircleAction(Icons.Filled.PlayArrow, WzBlue, onContinue)
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeFeatureCard("Günlük Ödül", Icons.Filled.CardGiftcard, WzGold, dailyAvailable, onDaily, Modifier.weight(1f))
                HomeFeatureCard("Görevler", Icons.Filled.TaskAlt, WzBlue, taskBadge, onTasks, Modifier.weight(1f))
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun PremiumTopHud(coins: Int, stars: Int, onProfile: () -> Unit, onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).shadow(6.dp, CircleShape).clip(CircleShape).background(WzBlueSoft).border(2.dp, Color.White, CircleShape).clickable(onClick = onProfile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Person, null, tint = WzBlue)
        }
        Spacer(Modifier.weight(1f))
        StatPill(Icons.Filled.AutoAwesome, coins.toString(), WzGold)
        Spacer(Modifier.width(8.dp))
        StatPill(Icons.Filled.Star, stars.toString(), WzGold)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(40.dp).clip(CircleShape).background(WzSurface).border(1.dp, WzBorder, CircleShape).clickable(onClick = onSettings), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Settings, null, tint = WzBlue, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun ScenicHero(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(28.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF79C8FF), Color(0xFFCDEBFF))))
            drawCircle(Color(0xFFFFE69A), size.minDimension * 0.12f, Offset(size.width * 0.78f, size.height * 0.22f))
            val back = Path().apply {
                moveTo(0f, size.height * 0.55f)
                lineTo(size.width * 0.18f, size.height * 0.34f)
                lineTo(size.width * 0.36f, size.height * 0.54f)
                lineTo(size.width * 0.57f, size.height * 0.28f)
                lineTo(size.width * 0.76f, size.height * 0.5f)
                lineTo(size.width, size.height * 0.3f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(back, Color(0xFF6D91C7))
            val front = Path().apply {
                moveTo(0f, size.height * 0.72f)
                cubicTo(size.width * 0.25f, size.height * 0.58f, size.width * 0.45f, size.height * 0.88f, size.width * 0.67f, size.height * 0.67f)
                cubicTo(size.width * 0.83f, size.height * 0.55f, size.width * 0.93f, size.height * 0.65f, size.width, size.height * 0.58f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(front, Color(0xFF2E7D5F))
            drawRoundRect(Color(0xFF4DA4DE), topLeft = Offset(size.width * 0.08f, size.height * 0.68f), size = androidx.compose.ui.geometry.Size(size.width * 0.74f, size.height * 0.18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(60f, 60f))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x330E386F)))), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.padding(top = 34.dp)) { content() }
        }
    }
}

@Composable
private fun MiniLandscapeThumb() {
    Box(Modifier.size(74.dp).clip(RoundedCornerShape(18.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8AD2FF), Color(0xFFEAF7FF))))
            val hill = Path().apply {
                moveTo(0f, size.height * 0.62f)
                lineTo(size.width * 0.35f, size.height * 0.32f)
                lineTo(size.width * 0.62f, size.height * 0.57f)
                lineTo(size.width, size.height * 0.4f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(hill, Color(0xFF4E8E64))
        }
    }
}

@Composable
private fun HomeFeatureCard(title: String, icon: ImageVector, tint: Color, badge: Boolean, onClick: () -> Unit, modifier: Modifier) {
    PremiumCard(modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(tint.copy(alpha = 0.12f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint)
            }
            Spacer(Modifier.width(9.dp))
            Text(title, color = WzInk, fontWeight = FontWeight.Black, fontSize = 13.sp, modifier = Modifier.weight(1f))
            if (badge) Box(Modifier.size(9.dp).background(WzRed, CircleShape))
        }
    }
}

@Composable
private fun PremiumScaffold(
    active: UiScreen,
    onHome: () -> Unit,
    onChapters: () -> Unit,
    onTasks: () -> Unit,
    onProfile: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(WzBackground).statusBarsPadding()) {
        Box(Modifier.fillMaxSize().padding(bottom = 74.dp)) { content() }
        PremiumBottomNav(active, onHome, onChapters, onTasks, onProfile, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun PremiumBottomNav(active: UiScreen, onHome: () -> Unit, onChapters: () -> Unit, onTasks: () -> Unit, onProfile: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().navigationBarsPadding().background(WzSurface).border(1.dp, WzBorder).padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem("Ana Sayfa", Icons.Filled.Home, active == UiScreen.HOME, onHome)
        BottomNavItem("Bölümler", Icons.Filled.Map, active == UiScreen.CHAPTERS || active == UiScreen.MAP, onChapters)
        BottomNavItem("Görevler", Icons.Filled.TaskAlt, active == UiScreen.TASKS, onTasks)
        BottomNavItem("Profil", Icons.Filled.Person, active == UiScreen.PROFILE, onProfile)
    }
}

@Composable
private fun BottomNavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.width(76.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (selected) WzBlue else Color(0xFF91A0BA), modifier = Modifier.size(22.dp))
        Text(label, color = if (selected) WzBlue else Color(0xFF91A0BA), fontSize = 9.sp, fontWeight = if (selected) FontWeight.Black else FontWeight.Bold)
    }
}

@Composable
private fun PremiumChapterScreen(unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, chapterChests: Set<String>, onBack: () -> Unit, onOpen: (Int) -> Unit) {
    PremiumScaffold(UiScreen.CHAPTERS, onBack, {}, {}, {}) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
            SimpleHeader("Bölümler", onBack)
            Spacer(Modifier.height(10.dp))
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                uiChapters().forEachIndexed { index, chapter ->
                    val range = chapter.start until chapter.endExclusive
                    val chapterStars = range.sumOf { stars[it] ?: 0 }
                    val maxStars = range.count() * 3
                    val isUnlocked = unlocked >= chapter.start
                    ChapterCard(
                        index = index,
                        chapter = chapter,
                        stars = chapterStars,
                        maxStars = maxStars,
                        unlocked = isUnlocked,
                        complete = range.all { it in completed },
                        chestClaimed = index.toString() in chapterChests,
                        onClick = { if (isUnlocked) onOpen(index) },
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ChapterCard(index: Int, chapter: UiChapter, stars: Int, maxStars: Int, unlocked: Boolean, complete: Boolean, chestClaimed: Boolean, onClick: () -> Unit) {
    val palettes = listOf(
        listOf(Color(0xFF7DCBFF), Color(0xFF5DAA7B)),
        listOf(Color(0xFF64C8E7), Color(0xFF2D8F9F)),
        listOf(Color(0xFF4F9D73), Color(0xFF246249)),
        listOf(Color(0xFFE8A65A), Color(0xFFAA6A43)),
        listOf(Color(0xFF526EC6), Color(0xFF263A7E)),
        listOf(Color(0xFF5AB9B0), Color(0xFF2D6C70)),
        listOf(Color(0xFF8A77C8), Color(0xFF4B4484)),
    )
    val colors = palettes[index % palettes.size]
    Box(
        Modifier.fillMaxWidth().height(130.dp).shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = WzShadow, spotColor = WzShadow)
            .clip(RoundedCornerShape(24.dp)).clickable(enabled = unlocked, onClick = onClick)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.horizontalGradient(if (unlocked) colors else listOf(Color(0xFFAAB2C0), Color(0xFF747D8D))))
            drawCircle(Color.White.copy(alpha = 0.16f), size.height * 0.65f, Offset(size.width * 0.78f, size.height * 0.25f))
            val hill = Path().apply {
                moveTo(0f, size.height * 0.75f)
                cubicTo(size.width * 0.28f, size.height * 0.52f, size.width * 0.45f, size.height * 0.88f, size.width * 0.7f, size.height * 0.6f)
                cubicTo(size.width * 0.84f, size.height * 0.46f, size.width * 0.93f, size.height * 0.65f, size.width, size.height * 0.54f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(hill, Color.Black.copy(alpha = 0.12f))
        }
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${index + 1}. ${chapter.name}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(chapter.subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                }
                if (!unlocked) {
                    Box(Modifier.size(38.dp).background(Color.Black.copy(alpha = 0.22f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Lock, null, tint = Color.White) }
                } else if (complete && !chestClaimed) {
                    Icon(Icons.Filled.Redeem, null, tint = WzGold, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { if (maxStars == 0) 0f else stars.toFloat() / maxStars },
                    modifier = Modifier.weight(1f).height(7.dp).clip(CircleShape),
                    color = Color(0xFF73D9FF),
                    trackColor = Color.White.copy(alpha = 0.28f),
                )
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Filled.Star, null, tint = WzGold, modifier = Modifier.size(17.dp))
                Text("$stars/$maxStars", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PremiumMapScreen(chapterIndex: Int, unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, onBack: () -> Unit, onOpenLevel: (Int) -> Unit) {
    val chapter = uiChapters()[chapterIndex]
    val levelIndices = (chapter.start until chapter.endExclusive).toList()
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        MapLandscapeBackground(chapterIndex)
        Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                CircleBack(onBack)
                Spacer(Modifier.weight(1f))
                Text("${chapterIndex + 1}. ${chapter.name}", color = WzInk, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(42.dp))
            }
            Spacer(Modifier.height(10.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val xs = listOf(0.28f, 0.5f, 0.72f, 0.5f, 0.28f)
                val ys = listOf(0.78f, 0.62f, 0.46f, 0.30f, 0.14f)
                Canvas(Modifier.fillMaxSize()) {
                    for (i in 0 until levelIndices.lastIndex) {
                        val start = Offset(size.width * xs[i], size.height * ys[i])
                        val end = Offset(size.width * xs[i + 1], size.height * ys[i + 1])
                        val path = Path().apply {
                            moveTo(start.x, start.y)
                            val midY = (start.y + end.y) / 2f
                            cubicTo(start.x, midY, end.x, midY, end.x, end.y)
                        }
                        drawPath(path, Color.White.copy(alpha = 0.95f), style = Stroke(width = 15f, cap = StrokeCap.Round))
                        drawPath(path, if (levelIndices[i] in completed) WzBlue.copy(alpha = 0.72f) else Color(0xFFB9C6D8), style = Stroke(width = 7f, cap = StrokeCap.Round))
                    }
                }
                levelIndices.forEachIndexed { localIndex, globalIndex ->
                    val nodeSize = 74.dp
                    val x = maxWidth * xs[localIndex] - nodeSize / 2
                    val y = maxHeight * ys[localIndex] - nodeSize / 2
                    MapNode(
                        number = localIndex + 1,
                        stars = stars[globalIndex] ?: 0,
                        unlocked = globalIndex <= unlocked,
                        current = globalIndex == unlocked && globalIndex !in completed,
                        complete = globalIndex in completed,
                        onClick = { if (globalIndex <= unlocked) onOpenLevel(globalIndex) },
                        modifier = Modifier.offset(x = x, y = y),
                    )
                }
            }
        }
    }
}

@Composable
private fun MapLandscapeBackground(chapterIndex: Int) {
    val palettes = listOf(
        listOf(Color(0xFFD8F1FF), Color(0xFF8CCCEB), Color(0xFF76A66B)),
        listOf(Color(0xFFDDF8FF), Color(0xFF72D1E8), Color(0xFF4B9D8D)),
        listOf(Color(0xFFDDF4E4), Color(0xFF7DB691), Color(0xFF3D7657)),
        listOf(Color(0xFFFFE6C5), Color(0xFFE5AF6F), Color(0xFFB47B4E)),
        listOf(Color(0xFFE2E7FF), Color(0xFF7886C7), Color(0xFF424F8B)),
        listOf(Color(0xFFDDF7F5), Color(0xFF7DBDBA), Color(0xFF4D8587)),
        listOf(Color(0xFFECE8FF), Color(0xFF9B8CC8), Color(0xFF5A5588)),
    )[chapterIndex % 7]
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(palettes[0], palettes[1])))
        drawCircle(Color.White.copy(alpha = 0.45f), size.minDimension * 0.24f, Offset(size.width * 0.82f, size.height * 0.12f))
        val hills = Path().apply {
            moveTo(0f, size.height * 0.52f)
            cubicTo(size.width * 0.25f, size.height * 0.38f, size.width * 0.47f, size.height * 0.63f, size.width * 0.68f, size.height * 0.45f)
            cubicTo(size.width * 0.82f, size.height * 0.34f, size.width * 0.92f, size.height * 0.47f, size.width, size.height * 0.38f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(hills, palettes[2])
        val water = Path().apply {
            moveTo(size.width * 0.22f, size.height)
            cubicTo(size.width * 0.38f, size.height * 0.82f, size.width * 0.44f, size.height * 0.58f, size.width * 0.55f, size.height * 0.4f)
            cubicTo(size.width * 0.58f, size.height * 0.33f, size.width * 0.62f, size.height * 0.28f, size.width * 0.66f, size.height * 0.2f)
            lineTo(size.width * 0.78f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(water, Color(0x885AC5F5))
    }
}

@Composable
private fun MapNode(number: Int, stars: Int, unlocked: Boolean, current: Boolean, complete: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(74.dp).shadow(if (current) 12.dp else 6.dp, CircleShape).clip(CircleShape)
                .background(if (!unlocked) Color(0xFF96A4B8) else if (current) WzBlue else Color.White)
                .border(if (current) 4.dp else 3.dp, if (current) Color.White else if (complete) WzGold else Color(0xFFB7C2D1), CircleShape)
                .clickable(enabled = unlocked, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (unlocked) Text(number.toString(), color = if (current) Color.White else WzInk, fontSize = 22.sp, fontWeight = FontWeight.Black)
            else Icon(Icons.Filled.Lock, null, tint = Color.White)
        }
        if (unlocked) {
            Row(Modifier.padding(top = 3.dp), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                repeat(3) { i -> Icon(if (i < stars) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = WzGold, modifier = Modifier.size(15.dp)) }
            }
        }
    }
}

@Composable
private fun PremiumGameScreen(
    levelIndex: Int,
    initialCoins: Int,
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
    val haptics = LocalHapticFeedback.current
    val level = levels[levelIndex]
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 64) }
    DisposableEffect(Unit) { onDispose { tone.release() } }

    var localCoins by remember { mutableIntStateOf(initialCoins) }
    var wheelLetters by remember { mutableStateOf(level.letters) }
    var foundWords by remember { mutableStateOf(setOf<String>()) }
    var bonusWords by remember { mutableStateOf(setOf<String>()) }
    var hintedCells by remember { mutableStateOf(setOf<UiCell>()) }
    var selected by remember { mutableStateOf(emptyList<Int>()) }
    var liveWord by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("Harfleri birleştir") }
    var hintsUsed by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var sessionCoins by remember { mutableIntStateOf(0) }
    var resultVisible by remember { mutableStateOf(false) }

    val earnedStars = when (hintsUsed) { 0 -> 3; 1 -> 2; 2 -> 1; else -> 0 }
    val complete = level.targets.all { it in foundWords }
    val chapter = uiChapters()[(levelIndex / 5).coerceIn(0, uiChapters().lastIndex)]
    val chapterEnd = levelIndex == chapter.endExclusive - 1

    fun changeCoins(delta: Int) {
        localCoins += delta
        sessionCoins += delta
        onCoinsChanged(localCoins)
    }

    LaunchedEffect(complete) {
        if (complete && !resultVisible) {
            onCompleted(earnedStars)
            if (firstCompletion) {
                localCoins += 25
                sessionCoins += 25
            }
            if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_ACK, 180)
            if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (!reduceMotion) delay(450) else delay(80)
            resultVisible = true
        }
    }

    Box(Modifier.fillMaxSize().background(WzBackground).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CircleBack(onBack)
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${levelIndex / 5 + 1}. Bölüm · ${levelIndex % 5 + 1}. Level", color = WzInk, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(level.title, color = WzMuted, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                StatPill(Icons.Filled.AutoAwesome, localCoins.toString(), WzGold)
            }

            Spacer(Modifier.height(8.dp))
            PremiumCrossword(level, foundWords, hintedCells)
            Spacer(Modifier.height(8.dp))
            Text(if (liveWord.isNotBlank()) liveWord else message, color = if (liveWord.isNotBlank()) WzBlue else WzMuted, fontSize = if (liveWord.isNotBlank()) 24.sp else 12.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))

            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val wheel = if (maxHeight < 330.dp) 210.dp else 245.dp
                PremiumLetterWheel(
                    letters = wheelLetters,
                    selectedIndices = selected,
                    size = wheel,
                    hapticEnabled = hapticEnabled,
                    onSelection = {
                        selected = it
                        liveWord = it.joinToString("") { idx -> wheelLetters[idx].toString() }
                    },
                    onSubmit = {
                        val submitted = liveWord.trim().uppercase(trLocalePremium)
                        when {
                            submitted in level.targets && submitted !in foundWords -> {
                                foundWords = foundWords + submitted
                                combo += 1
                                onWordFound()
                                onCombo(combo)
                                val comboBonus = if (combo % 3 == 0) 10 else 0
                                changeCoins(5 + comboBonus)
                                message = if (comboBonus > 0) "Combo x$combo · +${5 + comboBonus}" else "Harika! $submitted"
                                if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 90)
                            }
                            submitted in level.bonusWords && submitted !in bonusWords -> {
                                bonusWords = bonusWords + submitted
                                combo += 1
                                onWordFound()
                                onCombo(combo)
                                val comboBonus = if (combo % 3 == 0) 10 else 0
                                changeCoins(10 + comboBonus)
                                message = "Bonus kelime · +${10 + comboBonus}"
                                if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                            }
                            submitted.isNotBlank() -> {
                                combo = 0
                                message = "$submitted bu levelde yok"
                                if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_NACK, 80)
                            }
                        }
                        selected = emptyList()
                        liveWord = ""
                    },
                )
            }

            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                ToolCircle(Icons.Filled.Shuffle, "Karıştır", WzInk, enabled = true) {
                    wheelLetters = wheelLetters.shuffled()
                    message = "Harfler karıştırıldı"
                }
                ToolCircle(Icons.Filled.Lightbulb, "25", WzBlue, enabled = localCoins >= 25 && !complete) {
                    val hidden = allCells(level).filter { it !in hintedCells && !cellRevealed(level, it, foundWords) }
                    hidden.randomOrNull()?.let {
                        changeCoins(-25)
                        hintedCells = hintedCells + it
                        hintsUsed += 1
                        combo = 0
                        onHintUsed()
                        message = "Bir harf açıldı"
                    }
                }
                ToolCircle(Icons.Filled.Search, "60", WzBlue, enabled = localCoins >= 60 && !complete) {
                    level.targets.firstOrNull { it !in foundWords }?.let {
                        changeCoins(-60)
                        foundWords = foundWords + it
                        hintsUsed += 2
                        combo = 0
                        onHintUsed()
                        message = "Kelime ipucu: $it"
                    }
                }
            }
        }

        AnimatedVisibility(resultVisible) {
            PremiumResultOverlay(
                chapterComplete = chapterEnd,
                stars = earnedStars,
                previousStars = previousStars,
                found = foundWords.size,
                total = level.targets.size,
                bonus = bonusWords.size,
                hints = hintsUsed,
                combo = combo,
                coinDelta = sessionCoins,
                chapterChestClaimed = chapterChestClaimed,
                onReplay = onReplay,
                onMap = onMap,
                onNext = onNext,
                onClaimChapter = onClaimChapter,
            )
        }
    }
}

@Composable
private fun PremiumResultOverlay(
    chapterComplete: Boolean,
    stars: Int,
    previousStars: Int,
    found: Int,
    total: Int,
    bonus: Int,
    hints: Int,
    combo: Int,
    coinDelta: Int,
    chapterChestClaimed: Boolean,
    onReplay: () -> Unit,
    onMap: () -> Unit,
    onNext: () -> Unit,
    onClaimChapter: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Color(0x990F1A2D)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth(0.9f).shadow(22.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).background(WzSurface).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (chapterComplete) {
                Icon(Icons.Filled.Redeem, null, tint = WzGold, modifier = Modifier.size(62.dp))
                Text("BÖLÜM TAMAMLANDI!", color = WzInk, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Harika! Bölümdeki tüm levelleri bitirdin.", color = WzMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) { i -> Icon(if (i < stars) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = WzGold, modifier = Modifier.size(if (i == 1) 54.dp else 42.dp)) }
                }
                Text("LEVEL TAMAMLANDI!", color = WzInk, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Harika! Tüm kelimeleri buldun.", color = WzMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(14.dp))
            SummaryRow("Bulunan Kelime", "$found/$total")
            SummaryRow("Bonus Kelime", bonus.toString())
            SummaryRow("Kullanılan İpucu", hints.toString())
            SummaryRow("En Yüksek Combo", "x$combo")
            HorizontalDivider(color = WzBorder, modifier = Modifier.padding(vertical = 10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, null, tint = WzGold, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Kazanılan Coin", color = WzMuted, fontSize = 11.sp)
                    Text("${if (coinDelta >= 0) "+" else ""}$coinDelta", color = WzInk, fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.weight(1f))
                if (stars > previousStars) {
                    Box(Modifier.background(WzGold.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("Yeni Rekor!", color = WzGoldDark, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (chapterComplete && !chapterChestClaimed) {
                PrimaryButton("Bölüm Ödülünü Al +150", Icons.Filled.Redeem, WzBlue, onClaimChapter)
                Spacer(Modifier.height(8.dp))
            }
            PrimaryButton(if (chapterComplete) "Haritaya Dön" else "Sonraki Level", if (chapterComplete) Icons.Filled.Map else Icons.Filled.ChevronRight, WzGreen, if (chapterComplete) onMap else onNext)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("Tekrar Oyna", Icons.Filled.Refresh, onReplay, Modifier.weight(1f))
                SecondaryButton("Haritaya Dön", Icons.Filled.Home, onMap, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Star, null, tint = Color(0xFF91A0BA), modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = WzMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = WzInk, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
private fun PremiumCrossword(level: Level, foundWords: Set<String>, hinted: Set<UiCell>) {
    val entries = remember(level) { level.placements.flatMap(::cellsFor) }
    val map = remember(level) { entries.associate { it.first to it.second } }
    val rows = (map.keys.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (map.keys.maxOfOrNull { it.col } ?: 0) + 1
    val cellSize = when { cols >= 6 -> 36.dp; cols == 5 -> 39.dp; else -> 43.dp }
    Box(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { r ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(cols) { c ->
                        val cell = UiCell(r, c)
                        val letter = map[cell]
                        if (letter == null) Spacer(Modifier.size(cellSize))
                        else {
                            val shown = cell in hinted || cellRevealed(level, cell, foundWords)
                            val scale by animateFloatAsState(if (shown) 1f else 0.97f, tween(160), label = "cell")
                            Box(
                                Modifier.size(cellSize).scale(scale).shadow(if (shown) 4.dp else 0.dp, RoundedCornerShape(7.dp))
                                    .clip(RoundedCornerShape(7.dp)).background(if (shown) WzBlue else WzSurface)
                                    .border(1.dp, if (shown) WzBlueDark else Color(0xFFC8D2E0), RoundedCornerShape(7.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (shown) Text(letter.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun cellsFor(p: Placement): List<Pair<UiCell, Char>> = p.word.mapIndexed { i, ch ->
    UiCell(p.row + if (p.horizontal) 0 else i, p.col + if (p.horizontal) i else 0) to ch
}
private fun allCells(level: Level): Set<UiCell> = level.placements.flatMap(::cellsFor).map { it.first }.toSet()
private fun cellRevealed(level: Level, cell: UiCell, found: Set<String>): Boolean = level.placements.any { it.word in found && cellsFor(it).any { pair -> pair.first == cell } }

@Composable
private fun PremiumLetterWheel(
    letters: List<Char>,
    selectedIndices: List<Int>,
    size: Dp,
    hapticEnabled: Boolean,
    onSelection: (List<Int>) -> Unit,
    onSubmit: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var measured by remember { mutableStateOf(IntSize.Zero) }
    var pointer by remember { mutableStateOf<Offset?>(null) }

    fun positions(): List<Offset> {
        if (measured == IntSize.Zero) return emptyList()
        val center = Offset(measured.width / 2f, measured.height / 2f)
        val radius = minOf(measured.width, measured.height) * 0.34f
        return letters.indices.map { i ->
            val a = -PI / 2 + (2 * PI * i / letters.size)
            Offset(center.x + (cos(a) * radius).toFloat(), center.y + (sin(a) * radius).toFloat())
        }
    }

    fun hit(point: Offset): Int? {
        val pts = positions()
        val radius = minOf(measured.width, measured.height) * 0.14f
        return pts.indices.firstOrNull { i -> hypot((point.x - pts[i].x).toDouble(), (point.y - pts[i].y).toDouble()) <= radius }
    }

    Box(
        Modifier.size(size).onSizeChanged { measured = it }.pointerInput(letters, measured) {
            var gesture = emptyList<Int>()
            detectDragGestures(
                onDragStart = { p ->
                    pointer = p
                    val i = hit(p)
                    gesture = if (i != null) listOf(i) else emptyList()
                    onSelection(gesture)
                    if (i != null && hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onDrag = { change, _ ->
                    pointer = change.position
                    val i = hit(change.position)
                    if (i != null) {
                        if (gesture.size >= 2 && i == gesture[gesture.lastIndex - 1]) {
                            gesture = gesture.dropLast(1)
                            onSelection(gesture)
                        } else if (i !in gesture) {
                            gesture = gesture + i
                            onSelection(gesture)
                            if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                },
                onDragEnd = { pointer = null; onSubmit(); gesture = emptyList() },
                onDragCancel = { pointer = null; onSubmit(); gesture = emptyList() },
            )
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val r = minOf(this.size.width, this.size.height) * 0.48f
            drawCircle(Color.White, r, center)
            drawCircle(Color(0xFFE0E7F0), r, center, style = Stroke(width = 3f))
            val pts = positions()
            selectedIndices.zipWithNext().forEach { (a, b) ->
                if (a in pts.indices && b in pts.indices) drawLine(WzBlue.copy(alpha = 0.65f), pts[a], pts[b], 9f, cap = StrokeCap.Round)
            }
            if (selectedIndices.isNotEmpty() && pointer != null) {
                val last = selectedIndices.last()
                if (last in pts.indices) drawLine(WzBlue.copy(alpha = 0.45f), pts[last], pointer!!, 7f, cap = StrokeCap.Round)
            }
            pts.forEachIndexed { index, point ->
                val active = index in selectedIndices
                val rr = minOf(this.size.width, this.size.height) * if (active) 0.12f else 0.105f
                drawCircle(if (active) WzBlue else Color.Transparent, rr, point)
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = if (active) android.graphics.Color.WHITE else android.graphics.Color.rgb(19, 37, 74)
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = minOf(this@Canvas.size.width, this@Canvas.size.height) * 0.105f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val fm = paint.fontMetrics
                    canvas.nativeCanvas.drawText(letters[index].toString(), point.x, point.y - (fm.ascent + fm.descent) / 2f, paint)
                }
            }
        }
    }
}

@Composable
private fun PremiumDailyScreen(streak: Int, available: Boolean, onBack: () -> Unit, onClaim: () -> Unit) {
    ScreenColumn("Günlük Ödül", onBack) {
        Text("Her gün oyuna gel, daha büyük ödüller kazan!", color = WzMuted, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        val rewards = listOf(40, 50, 50, 75, 75, 100, 200)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rewards.take(4).forEachIndexed { index, reward -> DailyBox(index + 1, reward, streak, Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            rewards.drop(4).forEachIndexed { index, reward -> DailyBox(index + 5, reward, streak, Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(if (available) "Bugünün Ödülünü Al" else "Bugünün Ödülü Alındı", Icons.Filled.CardGiftcard, WzGreen, onClaim, enabled = available)
    }
}

@Composable
private fun DailyBox(day: Int, reward: Int, streak: Int, modifier: Modifier) {
    val done = day <= streak.coerceAtMost(7)
    PremiumCard(modifier.height(116.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Text("$day. Gün", color = if (done) WzGreenDark else WzInk, fontSize = 11.sp, fontWeight = FontWeight.Black)
            Icon(if (day == 7) Icons.Filled.Redeem else if (done) Icons.Filled.CheckCircle else Icons.Filled.AutoAwesome, null, tint = if (done) WzGreen else WzGold, modifier = Modifier.size(if (day == 7) 34.dp else 27.dp))
            Text("+$reward", color = WzGoldDark, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun PremiumTasksScreen(stats: UiStats, claimed: Set<String>, onBack: () -> Unit, onClaim: (UiTask) -> Unit) {
    ScreenColumn("Görevler", onBack) {
        PremiumCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(WzBlue, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text("Günlük Görevler", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp) }
                Spacer(Modifier.width(8.dp))
                Text("Başarımlar", color = WzMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        uiTasks().forEach { task ->
            val current = task.current(stats).coerceAtMost(task.goal)
            val reached = current >= task.goal
            val isClaimed = task.id in claimed
            PremiumCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isClaimed) Icons.Filled.CheckCircle else if (reached) Icons.Filled.TaskAlt else Icons.Filled.Lock, null, tint = if (reached) WzGreen else Color(0xFF9CA9BC))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(task.title, color = WzInk, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        Spacer(Modifier.height(5.dp))
                        LinearProgressIndicator(progress = { current.toFloat() / task.goal.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = WzBlue, trackColor = WzBlueSoft)
                        Text("$current/${task.goal}", color = WzMuted, fontSize = 10.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("+${task.reward}", color = WzGoldDark, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        if (reached && !isClaimed) Text("AL", color = WzBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.clickable { onClaim(task) }.padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumProfileScreen(coins: Int, totalStars: Int, completed: Int, words: Int, hints: Int, bestCombo: Int, streak: Int, onBack: () -> Unit) {
    val playerLevel = totalStars / 15 + 1
    val xp = totalStars % 15
    ScreenColumn("Profil", onBack) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(94.dp).background(WzBlueSoft, CircleShape).border(3.dp, Color.White, CircleShape).shadow(4.dp, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, null, tint = WzBlue, modifier = Modifier.size(54.dp))
            }
            Text("Oyuncu", color = WzInk, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("Seviye $playerLevel", color = WzMuted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { xp / 15f }, modifier = Modifier.fillMaxWidth(0.55f).height(7.dp).clip(CircleShape), color = WzBlue, trackColor = WzBlueSoft)
            Text("$xp / 15 XP", color = WzBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Toplam Yıldız", totalStars.toString(), Icons.Filled.Star, WzGold, Modifier.weight(1f))
            StatCard("Tamamlanan", completed.toString(), Icons.Filled.TaskAlt, WzGreen, Modifier.weight(1f))
            StatCard("Bulunan Kelime", words.toString(), Icons.Filled.Search, WzBlue, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Kullanılan İpucu", hints.toString(), Icons.Filled.Lightbulb, WzBlue, Modifier.weight(1f))
            StatCard("En Yüksek Combo", "x$bestCombo", Icons.Filled.LocalFireDepartment, WzGoldDark, Modifier.weight(1f))
            StatCard("Günlük Seri", streak.toString(), Icons.Filled.CardGiftcard, WzRed, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        PremiumCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Jeton", color = WzMuted, fontSize = 12.sp)
                Text(coins.toString(), color = WzGoldDark, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier) {
    PremiumCard(modifier.height(120.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(27.dp))
            Text(value, color = WzInk, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(label, color = WzMuted, fontSize = 9.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PremiumSettingsScreen(sound: Boolean, haptic: Boolean, reduceMotion: Boolean, onBack: () -> Unit, onSound: (Boolean) -> Unit, onHaptic: (Boolean) -> Unit, onReduceMotion: (Boolean) -> Unit) {
    ScreenColumn("Ayarlar", onBack) {
        PremiumCard(Modifier.fillMaxWidth()) {
            SettingRow(Icons.Filled.VolumeUp, "Ses Efektleri", sound, onSound)
            HorizontalDivider(color = WzBorder)
            SettingRow(Icons.Filled.Vibration, "Titreşim", haptic, onHaptic)
            HorizontalDivider(color = WzBorder)
            SettingRow(Icons.Filled.AutoAwesome, "Animasyonları Azalt", reduceMotion, onReduceMotion)
        }
        Spacer(Modifier.height(14.dp))
        PremiumCard(Modifier.fillMaxWidth()) {
            StaticSettingRow("Dil", "Türkçe")
            HorizontalDivider(color = WzBorder)
            StaticSettingRow("Gizlilik Politikası", "")
            HorizontalDivider(color = WzBorder)
            StaticSettingRow("Kullanım Şartları", "")
            HorizontalDivider(color = WzBorder)
            StaticSettingRow("Uygulama Sürümü", "v${BuildConfig.VERSION_NAME}")
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, label: String, value: Boolean, onValue: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = WzInk)
        Spacer(Modifier.width(12.dp))
        Text(label, color = WzInk, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onValue)
    }
}

@Composable
private fun StaticSettingRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = WzInk, modifier = Modifier.weight(1f), fontSize = 13.sp)
        if (value.isNotBlank()) Text(value, color = WzMuted, fontSize = 12.sp)
        else Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF9CA9BC), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ScreenColumn(title: String, onBack: () -> Unit, content: @Composable Column.() -> Unit) {
    Column(Modifier.fillMaxSize().background(WzBackground).statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        SimpleHeader(title, onBack)
        Spacer(Modifier.height(12.dp))
        content()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SimpleHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        CircleBack(onBack)
        Spacer(Modifier.weight(1f))
        Text(title, color = WzInk, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(42.dp))
    }
}

@Composable
private fun CircleBack(onClick: () -> Unit) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(WzSurface).border(1.dp, WzBorder, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.ArrowBack, null, tint = WzInk, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun PremiumCard(modifier: Modifier = Modifier, content: @Composable Column.() -> Unit) {
    Column(
        modifier.shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = WzShadow, spotColor = WzShadow)
            .clip(RoundedCornerShape(22.dp)).background(WzSurface).border(1.dp, WzBorder, RoundedCornerShape(22.dp)).padding(14.dp),
        content = content,
    )
}

@Composable
private fun StatPill(icon: ImageVector, text: String, tint: Color) {
    Row(Modifier.clip(RoundedCornerShape(18.dp)).background(WzSurface).border(1.dp, WzBorder, RoundedCornerShape(18.dp)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, color = WzInk, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

@Composable
private fun CircleAction(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(color).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(27.dp))
    }
}

@Composable
private fun PrimaryButton(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, enabled: Boolean = true) {
    Row(
        Modifier.fillMaxWidth().height(58.dp).shadow(if (enabled) 7.dp else 0.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp))
            .background(if (enabled) color else Color(0xFFD0D7E2)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
private fun SecondaryButton(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.height(48.dp).clip(RoundedCornerShape(16.dp)).background(WzBlueSoft).clickable(onClick = onClick).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = WzBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(5.dp))
        Text(title, color = WzBlueDark, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ToolCircle(icon: ImageVector, label: String, tint: Color, enabled: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(if (enabled) WzSurface else Color(0xFFE4E8EE)).border(1.dp, WzBorder, CircleShape).clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = if (enabled) tint else Color(0xFFADB7C7), modifier = Modifier.size(22.dp))
        }
        Text(label, color = if (enabled) WzMuted else Color(0xFFADB7C7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
