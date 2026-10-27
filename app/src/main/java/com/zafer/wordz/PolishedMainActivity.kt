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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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

private val PNavy = Color(0xFF041437)
private val PBlue = Color(0xFF0B4DB4)
private val PElectric = Color(0xFF149CFF)
private val PSky = Color(0xFF6DD5FF)
private val PGold = Color(0xFFFFCA3A)
private val PGoldDark = Color(0xFFE78A12)
private val PGreen = Color(0xFF26D267)
private val PGreenDark = Color(0xFF079943)
private val PPurple = Color(0xFF9157F6)
private val PSoft = Color(0xFFC5DBFF)
private val PCard = Color(0xD9123474)
private val PBorder = Color(0x775AC9FF)
private val PRed = Color(0xFFFF6470)
private val POrange = Color(0xFFFF8A3D)

private data class PCell(val row: Int, val col: Int)
private data class PChapter(val name: String, val subtitle: String, val start: Int, val endExclusive: Int)
private data class PStats(val completed: Int, val stars: Int, val words: Int, val hints: Int, val bestCombo: Int, val streak: Int)
private data class PAchievement(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val current: (PStats) -> Int,
    val goal: Int,
)
private data class PWorldTheme(val top: Color, val bottom: Color, val accent: Color, val name: String)
private enum class PScreen { HOME, CHAPTERS, MAP, GAME, SETTINGS, PROFILE, ACHIEVEMENTS }

private const val PPREFS = "wordz_progress"
private const val P_UNLOCKED = "unlocked_level"
private const val P_COINS = "coins"
private const val P_COMPLETED = "completed_levels"
private const val P_STARS = "level_stars"
private const val P_DAILY_DATE = "daily_reward_date"
private const val P_STAR_CHESTS = "claimed_star_chests"
private const val P_SOUND = "sound_enabled"
private const val P_HAPTIC = "haptic_enabled"
private const val P_STREAK = "daily_streak"
private const val P_CHAPTER_CHESTS = "claimed_chapter_chests"
private const val P_ACHIEVEMENTS = "claimed_achievements"
private const val P_WORDS = "stats_words"
private const val P_HINTS = "stats_hints"
private const val P_BEST_COMBO = "stats_best_combo"
private const val P_REDUCE_MOTION = "reduce_motion"
private val pTr = Locale("tr", "TR")

class PolishedMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = PElectric,
                    secondary = PGold,
                    background = PNavy,
                    surface = PNavy,
                    onPrimary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White,
                )
            ) { PWordZApp() }
        }
    }
}

private fun pChapters(): List<PChapter> = levels.indices.chunked(5).mapIndexed { index, chunk ->
    val names = listOf("Başlangıç Vadisi", "Sahil Yolu", "Yeşil Orman", "Güneş Tepeleri", "Gece Rotası", "Uzak Diyarlar", "Zafer Zirvesi")
    val subs = listOf("Macera burada başlıyor", "Dalgalar arasında kelime avı", "Ormanın gizli patikaları", "Güneşe doğru yüksel", "Yıldızların altında ilerle", "Yeni diyarlara yolculuk", "Zirveye son adımlar")
    PChapter(names.getOrElse(index) { "Macera" }, subs.getOrElse(index) { "Yeni kelimeleri keşfet" }, chunk.first(), chunk.last() + 1)
}

private fun pWorldTheme(index: Int): PWorldTheme = listOf(
    PWorldTheme(Color(0xFF1A69D8), Color(0xFF07163F), Color(0xFF64D8FF), "Vadi"),
    PWorldTheme(Color(0xFF057EC4), Color(0xFF062C62), Color(0xFF4DE1E8), "Sahil"),
    PWorldTheme(Color(0xFF167B67), Color(0xFF082E3E), Color(0xFF65E38A), "Orman"),
    PWorldTheme(Color(0xFFD27828), Color(0xFF532C3A), Color(0xFFFFCB52), "Güneş"),
    PWorldTheme(Color(0xFF3449A8), Color(0xFF100F39), Color(0xFFA788FF), "Gece"),
    PWorldTheme(Color(0xFF148D9B), Color(0xFF073347), Color(0xFF68E4D8), "Uzak Diyar"),
    PWorldTheme(Color(0xFF6C49BA), Color(0xFF1D164F), Color(0xFFFFCE4A), "Zirve"),
)[index.coerceIn(0, 6)]

private fun pDecodeStars(raw: Set<String>): Map<Int, Int> = raw.mapNotNull {
    val parts = it.split(':')
    val i = parts.getOrNull(0)?.toIntOrNull()
    val s = parts.getOrNull(1)?.toIntOrNull()
    if (i != null && s != null && i in levels.indices) i to s.coerceIn(0, 3) else null
}.toMap()

private fun pEncodeStars(stars: Map<Int, Int>): Set<String> = stars.map { "${it.key}:${it.value}" }.toSet()

private fun pAchievements(): List<PAchievement> = listOf(
    PAchievement("first", "İlk Adım", "1 level tamamla", 50, { it.completed }, 1),
    PAchievement("stars15", "Yıldız Avcısı", "15 yıldız topla", 75, { it.stars }, 15),
    PAchievement("words50", "Kelime Ustası", "50 doğru kelime bul", 100, { it.words }, 50),
    PAchievement("combo5", "Seri Ustası", "Combo x5 yap", 100, { it.bestCombo }, 5),
    PAchievement("streak3", "Sadık Oyuncu", "3 günlük seri yap", 120, { it.streak }, 3),
)

@Composable
private fun PWordZApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(PPREFS, Context.MODE_PRIVATE) }
    val savedUnlocked = prefs.getInt(P_UNLOCKED, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(P_COMPLETED, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
    val migratedCompleted = if (savedCompleted.isEmpty() && savedUnlocked > 0) (0 until savedUnlocked).toSet() else savedCompleted
    val starMap = pDecodeStars(prefs.getStringSet(P_STARS, emptySet()).orEmpty()).toMutableMap()
    migratedCompleted.forEach { if (it !in starMap) starMap[it] = 1 }

    var unlocked by remember { mutableIntStateOf(savedUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(P_COINS, 100)) }
    var completed by remember { mutableStateOf(migratedCompleted) }
    var stars by remember { mutableStateOf(starMap.toMap()) }
    var claimedStarChests by remember { mutableIntStateOf(prefs.getInt(P_STAR_CHESTS, 0)) }
    var dailyDate by remember { mutableStateOf(prefs.getString(P_DAILY_DATE, "").orEmpty()) }
    var sound by remember { mutableStateOf(prefs.getBoolean(P_SOUND, true)) }
    var haptic by remember { mutableStateOf(prefs.getBoolean(P_HAPTIC, true)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean(P_REDUCE_MOTION, false)) }
    var streak by remember { mutableIntStateOf(prefs.getInt(P_STREAK, 0)) }
    var claimedChapterChests by remember { mutableStateOf(prefs.getStringSet(P_CHAPTER_CHESTS, emptySet()).orEmpty()) }
    var claimedAchievements by remember { mutableStateOf(prefs.getStringSet(P_ACHIEVEMENTS, emptySet()).orEmpty()) }
    var words by remember { mutableIntStateOf(prefs.getInt(P_WORDS, 0)) }
    var hints by remember { mutableIntStateOf(prefs.getInt(P_HINTS, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(P_BEST_COMBO, 0)) }
    var screen by remember { mutableStateOf(PScreen.HOME) }
    var selectedLevel by remember { mutableIntStateOf(unlocked) }
    var selectedChapter by remember { mutableIntStateOf((unlocked / 5).coerceIn(0, pChapters().lastIndex)) }
    var gameSession by remember { mutableIntStateOf(0) }

    val totalStars = stars.values.sum()
    val today = LocalDate.now()
    val dailyAvailable = dailyDate != today.toString()
    val availableStarChests = (totalStars / 15 - claimedStarChests).coerceAtLeast(0)
    val stats = PStats(completed.size, totalStars, words, hints, bestCombo, streak)

    fun save(
        vUnlocked: Int = unlocked,
        vCoins: Int = coins,
        vCompleted: Set<Int> = completed,
        vStars: Map<Int, Int> = stars,
        vClaimedStarChests: Int = claimedStarChests,
        vDailyDate: String = dailyDate,
        vSound: Boolean = sound,
        vHaptic: Boolean = haptic,
        vReduceMotion: Boolean = reduceMotion,
        vStreak: Int = streak,
        vChapterChests: Set<String> = claimedChapterChests,
        vAchievements: Set<String> = claimedAchievements,
        vWords: Int = words,
        vHints: Int = hints,
        vBestCombo: Int = bestCombo,
    ) {
        prefs.edit().putInt(P_UNLOCKED, vUnlocked).putInt(P_COINS, vCoins)
            .putStringSet(P_COMPLETED, vCompleted.map(Int::toString).toSet())
            .putStringSet(P_STARS, pEncodeStars(vStars)).putInt(P_STAR_CHESTS, vClaimedStarChests)
            .putString(P_DAILY_DATE, vDailyDate).putBoolean(P_SOUND, vSound).putBoolean(P_HAPTIC, vHaptic)
            .putBoolean(P_REDUCE_MOTION, vReduceMotion).putInt(P_STREAK, vStreak)
            .putStringSet(P_CHAPTER_CHESTS, vChapterChests).putStringSet(P_ACHIEVEMENTS, vAchievements)
            .putInt(P_WORDS, vWords).putInt(P_HINTS, vHints).putInt(P_BEST_COMBO, vBestCombo).apply()
    }

    fun openLevel(index: Int) {
        selectedLevel = index
        selectedChapter = (index / 5).coerceIn(0, pChapters().lastIndex)
        gameSession += 1
        screen = PScreen.GAME
    }

    fun claimChapterChest(index: Int) {
        val chapter = pChapters()[index]
        val key = index.toString()
        val done = (chapter.start until chapter.endExclusive).all { it in completed }
        if (done && key !in claimedChapterChests) {
            val newClaimed = claimedChapterChests + key
            val newCoins = coins + 150
            claimedChapterChests = newClaimed
            coins = newCoins
            save(vCoins = newCoins, vChapterChests = newClaimed)
        }
    }

    when (screen) {
        PScreen.HOME -> PHomeScreen(
            coins, totalStars, completed.size, unlocked, streak, dailyAvailable, availableStarChests,
            pAchievements().count { it.current(stats) >= it.goal && it.id !in claimedAchievements },
            onContinue = { openLevel(unlocked) },
            onChapters = { screen = PScreen.CHAPTERS },
            onDaily = {
                if (dailyAvailable) {
                    val newStreak = if (dailyDate == today.minusDays(1).toString()) streak + 1 else 1
                    val newCoins = coins + 40
                    val newDate = today.toString()
                    streak = newStreak; coins = newCoins; dailyDate = newDate
                    save(vCoins = newCoins, vDailyDate = newDate, vStreak = newStreak)
                }
            },
            onChest = {
                if (availableStarChests > 0) {
                    val newCoins = coins + 75
                    val newClaimed = claimedStarChests + 1
                    coins = newCoins; claimedStarChests = newClaimed
                    save(vCoins = newCoins, vClaimedStarChests = newClaimed)
                }
            },
            onProfile = { screen = PScreen.PROFILE },
            onAchievements = { screen = PScreen.ACHIEVEMENTS },
            onSettings = { screen = PScreen.SETTINGS },
        )

        PScreen.CHAPTERS -> PChapterScreen(unlocked, stars, completed, claimedChapterChests, coins,
            onBack = { screen = PScreen.HOME },
            onSelect = { selectedChapter = it; screen = PScreen.MAP },
            onClaimChest = ::claimChapterChest,
        )

        PScreen.MAP -> PMapScreen(selectedChapter, unlocked, stars, completed, coins,
            onBack = { screen = PScreen.CHAPTERS }, onSelect = ::openLevel)

        PScreen.PROFILE -> PProfileScreen(coins, totalStars, completed.size, unlocked, words, hints, bestCombo, streak) { screen = PScreen.HOME }

        PScreen.ACHIEVEMENTS -> PAchievementsScreen(stats, claimedAchievements, coins,
            onBack = { screen = PScreen.HOME },
            onClaim = { achievement ->
                if (achievement.current(stats) >= achievement.goal && achievement.id !in claimedAchievements) {
                    val newClaimed = claimedAchievements + achievement.id
                    val newCoins = coins + achievement.reward
                    claimedAchievements = newClaimed; coins = newCoins
                    save(vCoins = newCoins, vAchievements = newClaimed)
                }
            })

        PScreen.SETTINGS -> PSettingsScreen(sound, haptic, reduceMotion,
            onBack = { screen = PScreen.HOME },
            onSound = { sound = !sound; save(vSound = sound) },
            onHaptic = { haptic = !haptic; save(vHaptic = haptic) },
            onMotion = { reduceMotion = !reduceMotion; save(vReduceMotion = reduceMotion) },
        )

        PScreen.GAME -> key(selectedLevel, gameSession) {
            val wasCompletedBefore = selectedLevel in completed
            val chapter = pChapters()[selectedChapter]
            val isChapterLast = selectedLevel == chapter.endExclusive - 1
            PGameScreen(
                levelIndex = selectedLevel,
                coins = coins,
                previousStars = stars[selectedLevel] ?: 0,
                soundEnabled = sound,
                hapticEnabled = haptic,
                reduceMotion = reduceMotion,
                wasCompletedBefore = wasCompletedBefore,
                isChapterLast = isChapterLast,
                chapterChestClaimed = selectedChapter.toString() in claimedChapterChests,
                onCoinsChanged = { coins = it; save(vCoins = it) },
                onWordFound = { words += 1; save(vWords = words) },
                onHintUsed = { hints += 1; save(vHints = hints) },
                onComboChanged = { value -> if (value > bestCombo) { bestCombo = value; save(vBestCombo = value) } },
                onBack = { screen = PScreen.MAP },
                onCompleted = { earned ->
                    val first = selectedLevel !in completed
                    val newCompleted = completed + selectedLevel
                    val old = stars[selectedLevel] ?: 0
                    val newStars = if (earned > old) stars + (selectedLevel to earned) else stars
                    val newCoins = coins + if (first) 25 else 0
                    val newUnlocked = if (selectedLevel >= unlocked && unlocked < levels.lastIndex) selectedLevel + 1 else unlocked
                    completed = newCompleted; stars = newStars; coins = newCoins; unlocked = newUnlocked
                    save(vUnlocked = newUnlocked, vCoins = newCoins, vCompleted = newCompleted, vStars = newStars)
                },
                onNext = {
                    if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = PScreen.MAP
                },
                onMap = { screen = PScreen.MAP },
                onRetry = { gameSession += 1 },
                onClaimChapterChest = { claimChapterChest(selectedChapter) },
            )
        }
    }
}

@Composable
private fun PBackground(themeIndex: Int = 0, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    val theme = pWorldTheme(themeIndex)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(theme.top, theme.bottom, PNavy)))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(theme.accent.copy(alpha = 0.14f), size.minDimension * 0.58f, Offset(size.width * 0.08f, size.height * 0.27f))
            drawCircle(Color(0x223F6CFF), size.minDimension * 0.5f, Offset(size.width * 0.93f, size.height * 0.58f))
            val dots = listOf(.1f to .12f, .8f to .1f, .9f to .24f, .22f to .34f, .68f to .31f, .83f to .49f, .13f to .65f, .55f to .72f, .9f to .78f, .3f to .86f)
            dots.forEachIndexed { i, p -> drawCircle(if (i % 3 == 0) PGold.copy(.65f) else Color.White.copy(.55f), if (i % 2 == 0) 3f else 2f, Offset(size.width * p.first, size.height * p.second)) }
        }
        content()
    }
}

@Composable
private fun PGlass(modifier: Modifier = Modifier, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(25.dp)
    Column(modifier.shadow(12.dp, shape).clip(shape).background(Brush.verticalGradient(listOf(Color(0xE0184D9C), PCard, Color(0xE00B2A61)))).border(1.dp, PBorder, shape).padding(16.dp), content = content)
}

@Composable
private fun PHud(icon: ImageVector, text: String, tint: Color = PGold, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(22.dp)).background(Color(0xD2082D68)).border(1.dp, PBorder, RoundedCornerShape(22.dp)).padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(5.dp)); Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

@Composable
private fun PRoundIcon(icon: ImageVector, onClick: () -> Unit, badge: Boolean = false) {
    Box(Modifier.size(43.dp).shadow(7.dp, CircleShape).clip(CircleShape).background(Color(0xE00D397E)).border(1.dp, PBorder, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(23.dp))
        if (badge) Box(Modifier.align(Alignment.TopEnd).size(11.dp).background(PRed, CircleShape).border(1.dp, Color.White, CircleShape))
    }
}

@Composable
private fun PTopBar(coins: Int, stars: Int? = null, back: (() -> Unit)? = null, profile: (() -> Unit)? = null, achievements: (() -> Unit)? = null, settings: (() -> Unit)? = null, badge: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        if (back != null) PRoundIcon(Icons.Filled.ArrowBack, back)
        PHud(Icons.Filled.AutoAwesome, coins.toString(), PGold)
        Spacer(Modifier.weight(1f))
        if (stars != null) PHud(Icons.Filled.Star, stars.toString(), PGold)
        if (profile != null) PRoundIcon(Icons.Filled.Person, profile)
        if (achievements != null) PRoundIcon(Icons.Filled.EmojiEvents, achievements, badge)
        if (settings != null) PRoundIcon(Icons.Filled.Settings, settings)
    }
}

@Composable
private fun PGradientButton(title: String, subtitle: String? = null, icon: ImageVector, colors: List<Color>, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val shape = RoundedCornerShape(27.dp)
    Row(modifier.height(if (subtitle == null) 60.dp else 74.dp).shadow(11.dp, shape).clip(shape).background(Brush.verticalGradient(if (enabled) colors else listOf(Color(0xFF42516B), Color(0xFF28354B)))).border(1.dp, Color.White.copy(if (enabled) .28f else .1f), shape).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(41.dp).background(Color.White.copy(.15f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = if (subtitle == null) 17.sp else 20.sp)
            if (subtitle != null) Text(subtitle, color = Color.White.copy(.82f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Color.White.copy(.75f))
    }
}

@Composable
private fun PHomeScreen(coins: Int, stars: Int, completed: Int, unlocked: Int, streak: Int, dailyAvailable: Boolean, availableChests: Int, claimableAchievements: Int, onContinue: () -> Unit, onChapters: () -> Unit, onDaily: () -> Unit, onChest: () -> Unit, onProfile: () -> Unit, onAchievements: () -> Unit, onSettings: () -> Unit) {
    PBackground((unlocked / 5).coerceIn(0, 6)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(17.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PTopBar(coins, stars, profile = onProfile, achievements = onAchievements, settings = onSettings, badge = claimableAchievements > 0)
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.Bottom) { Text("Word", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Black); Text("Z", color = PGold, fontSize = 64.sp, fontWeight = FontWeight.Black) }
            Text("KELİME BULMACA", color = PSoft, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(18.dp))
            PGlass(Modifier.fillMaxWidth()) {
                Text("İlerleme Durumu", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth()) {
                    PStat(Icons.Filled.AutoAwesome, coins.toString(), "Jeton", PGold, Modifier.weight(1f)); PStat(Icons.Filled.Star, stars.toString(), "Yıldız", PGold, Modifier.weight(1f)); PStat(Icons.Filled.Map, completed.toString(), "Level", PSky, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp)); LinearProgressIndicator(progress = { completed.toFloat() / levels.size.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = PGreen, trackColor = Color.White.copy(.12f))
            }
            Spacer(Modifier.height(14.dp))
            PGradientButton("Devam Et", "Level ${unlocked + 1} · ${pChapters()[(unlocked / 5).coerceIn(0, pChapters().lastIndex)].name}", Icons.Filled.PlayArrow, listOf(Color(0xFF69ED3D), PGreen, PGreenDark), onContinue, Modifier.fillMaxWidth())
            Spacer(Modifier.height(11.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PMenuTile("Bölümler", Icons.Filled.Map, listOf(Color(0xFFFFBE36), Color(0xFFD98211)), onChapters, Modifier.weight(1f))
                PMenuTile(if (dailyAvailable) "Günlük +40" else "Günlük ✓", Icons.Filled.CardGiftcard, listOf(Color(0xFFA768F5), Color(0xFF6632CE)), onDaily, Modifier.weight(1f), dailyAvailable, dailyAvailable)
                PMenuTile(if (availableChests > 0) "Sandık +75" else "Sandık", Icons.Filled.Redeem, listOf(Color(0xFF38AEFF), Color(0xFF1767D4)), onChest, Modifier.weight(1f), availableChests > 0, availableChests > 0)
            }
            Spacer(Modifier.height(12.dp))
            PDailyStrip(streak)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PMiniNav("Profil", Icons.Filled.Person, onProfile, Modifier.weight(1f)); PMiniNav("Görevler", Icons.Filled.EmojiEvents, onAchievements, Modifier.weight(1f), claimableAchievements > 0); PMiniNav("Ayarlar", Icons.Filled.Settings, onSettings, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp)); Text("v${BuildConfig.VERSION_NAME}", color = PSoft.copy(.7f), fontSize = 10.sp)
        }
    }
}

@Composable private fun PStat(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier = Modifier) { Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp)); Text(value, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black); Text(label, color = PSoft, fontSize = 10.sp) } }

@Composable
private fun PMenuTile(title: String, icon: ImageVector, colors: List<Color>, onClick: () -> Unit, modifier: Modifier = Modifier, badge: Boolean = false, enabled: Boolean = true) {
    val shape = RoundedCornerShape(21.dp)
    Box(modifier.height(100.dp).shadow(10.dp, shape).clip(shape).background(Brush.verticalGradient(if (enabled) colors else listOf(Color(0xFF47536A), Color(0xFF2D394D)))).border(1.dp, Color.White.copy(.22f), shape).clickable(enabled = enabled, onClick = onClick).padding(10.dp)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(33.dp)); Spacer(Modifier.height(5.dp)); Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, textAlign = TextAlign.Center) }
        if (badge) Box(Modifier.align(Alignment.TopEnd).size(10.dp).background(PRed, CircleShape))
    }
}

@Composable
private fun PDailyStrip(streak: Int) {
    PGlass(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.LocalFireDepartment, null, tint = POrange); Spacer(Modifier.width(7.dp)); Text("Günlük Seri", color = Color.White, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text("$streak gün", color = PGold, fontWeight = FontWeight.Black) }
        Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            repeat(7) { i -> val active = i < (streak % 7).let { if (it == 0 && streak > 0) 7 else it }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(28.dp).background(if (active) POrange else Color.White.copy(.1f), CircleShape), contentAlignment = Alignment.Center) { Text((i + 1).toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black) }; Text("G${i + 1}", color = PSoft, fontSize = 8.sp) } }
        }
    }
}

@Composable
private fun PMiniNav(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, badge: Boolean = false) {
    Box(modifier.height(58.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xB20C3472)).border(1.dp, PBorder, RoundedCornerShape(18.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(6.dp)); Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        if (badge) Box(Modifier.align(Alignment.TopEnd).offset((-5).dp, 5.dp).size(9.dp).background(PRed, CircleShape))
    }
}

@Composable
private fun PChapterScreen(unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, claimedChests: Set<String>, coins: Int, onBack: () -> Unit, onSelect: (Int) -> Unit, onClaimChest: (Int) -> Unit) {
    PBackground((unlocked / 5).coerceIn(0, 6)) {
        Column(Modifier.fillMaxSize().padding(17.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PTopBar(coins, stars.values.sum(), back = onBack); Spacer(Modifier.height(16.dp)); Text("Dünya Haritası", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black); Text("Her bölüm kendi atmosferi, level yolu ve büyük sandığıyla gelir", color = PSoft, textAlign = TextAlign.Center, fontSize = 12.sp); Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                pChapters().forEachIndexed { i, chapter ->
                    val range = chapter.start until chapter.endExclusive; val open = unlocked >= chapter.start; val done = range.count { it in completed }; val total = range.count(); val chapterStars = range.sumOf { stars[it] ?: 0 }; val theme = pWorldTheme(i); val chestClaimed = i.toString() in claimedChests
                    val shape = RoundedCornerShape(25.dp)
                    Column(Modifier.fillMaxWidth().padding(vertical = 7.dp).shadow(12.dp, shape).clip(shape).background(Brush.horizontalGradient(listOf(theme.top.copy(.95f), theme.bottom.copy(.95f)))).border(1.dp, if (open) theme.accent.copy(.65f) else Color.White.copy(.12f), shape).clickable(enabled = open) { onSelect(i) }.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(55.dp).background(theme.accent.copy(if (open) .2f else .07f), CircleShape), contentAlignment = Alignment.Center) { Icon(if (open) Icons.Filled.Map else Icons.Filled.Lock, null, tint = if (open) theme.accent else PSoft, modifier = Modifier.size(30.dp)) }
                            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("BÖLÜM ${i + 1} · ${theme.name}", color = theme.accent, fontSize = 10.sp, fontWeight = FontWeight.Black); Text(chapter.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp); Text(chapter.subtitle, color = PSoft, fontSize = 10.sp) }
                            Text(if (open) "★ $chapterStars/${total * 3}" else "KİLİTLİ", color = if (open) PGold else PSoft, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(10.dp)); LinearProgressIndicator(progress = { done.toFloat() / total.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = theme.accent, trackColor = Color.White.copy(.1f)); Spacer(Modifier.height(7.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("$done/$total level", color = PSoft, fontSize = 10.sp); if (done == total) Text(if (chestClaimed) "✓ Büyük sandık alındı" else "🎁 Sandık +150", color = if (chestClaimed) PSoft else PGold, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.clickable(enabled = !chestClaimed) { onClaimChest(i) }) }
                    }
                }; Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PMapScreen(chapterIndex: Int, unlocked: Int, stars: Map<Int, Int>, completed: Set<Int>, coins: Int, onBack: () -> Unit, onSelect: (Int) -> Unit) {
    val chapter = pChapters()[chapterIndex]; val indices = (chapter.start until chapter.endExclusive).toList(); val theme = pWorldTheme(chapterIndex); val y = listOf(.10f, .30f, .50f, .70f, .90f); val x = listOf(.18f, .53f, .82f, .53f, .18f); val mapHeight = 610.dp
    PBackground(chapterIndex) {
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PTopBar(coins, indices.sumOf { stars[it] ?: 0 }, back = onBack); Spacer(Modifier.height(13.dp)); PGlass(Modifier.fillMaxWidth()) {
                Text("BÖLÜM ${chapterIndex + 1} · ${theme.name}", color = theme.accent, fontSize = 11.sp, fontWeight = FontWeight.Black); Text(chapter.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black); Text(chapter.subtitle, color = PSoft, fontSize = 11.sp); Spacer(Modifier.height(8.dp)); val done = indices.count { it in completed }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("İlerleme $done/${indices.size}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("★ ${indices.sumOf { stars[it] ?: 0 }}/${indices.size * 3}", color = PGold, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                Spacer(Modifier.height(6.dp)); LinearProgressIndicator(progress = { done.toFloat() / indices.size.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = theme.accent, trackColor = Color.White.copy(.1f))
            }
            Spacer(Modifier.height(8.dp)); Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                BoxWithConstraints(Modifier.fillMaxWidth().height(mapHeight)) {
                    Canvas(Modifier.fillMaxSize()) {
                        for (i in 0 until indices.lastIndex) {
                            val sx = size.width * x[i]; val sy = size.height * y[i]; val ex = size.width * x[i + 1]; val ey = size.height * y[i + 1]
                            val p = Path(); p.moveTo(sx, sy); val midY = (sy + ey) / 2f; p.cubicTo(sx, midY, ex, midY, ex, ey)
                            val activeSegment = indices[i] in completed
                            drawPath(p, if (activeSegment) theme.accent.copy(.95f) else Color.White.copy(.2f), style = Stroke(width = if (activeSegment) 10f else 7f, cap = StrokeCap.Round))
                            if (activeSegment) drawPath(p, Color.White.copy(.45f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                        }
                    }
                    indices.forEachIndexed { local, global ->
                        val nodeSize = 88.dp; val open = global <= unlocked; val done = global in completed; val current = global == unlocked && !done
                        PLevelNode("${chapterIndex + 1}-${local + 1}", stars[global] ?: 0, open, done, current, theme.accent, { onSelect(global) }, Modifier.offset(x = maxWidth * x[local] - nodeSize / 2, y = mapHeight * y[local] - nodeSize / 2))
                    }
                }; Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun PLevelNode(label: String, stars: Int, unlocked: Boolean, completed: Boolean, current: Boolean, accent: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row { repeat(3) { i -> Icon(if (i < stars) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (i < stars) PGold else Color.White.copy(.38f), modifier = Modifier.size(16.dp)) } }
        Spacer(Modifier.height(2.dp)); val brush = when { !unlocked -> Brush.verticalGradient(listOf(Color(0xFF536078), Color(0xFF2D394F))); current -> Brush.verticalGradient(listOf(accent.copy(1f), PElectric)); completed -> Brush.verticalGradient(listOf(PGreen, PGreenDark)); else -> Brush.verticalGradient(listOf(PElectric, PBlue)) }
        Box(Modifier.size(78.dp).shadow(if (current) 18.dp else 9.dp, CircleShape).clip(CircleShape).background(brush).border(if (current) 4.dp else 2.dp, if (current) PGold else Color.White.copy(.38f), CircleShape).clickable(enabled = unlocked, onClick = onClick), contentAlignment = Alignment.Center) { if (unlocked) Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black) else Icon(Icons.Filled.Lock, null, tint = Color.White, modifier = Modifier.size(29.dp)) }
        if (current) Text("SIRADAKİ", color = PGold, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PProfileScreen(coins: Int, stars: Int, completed: Int, unlocked: Int, words: Int, hints: Int, combo: Int, streak: Int, onBack: () -> Unit) {
    val playerLevel = 1 + stars / 15; val xp = stars % 15
    PBackground((unlocked / 5).coerceIn(0, 6)) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(17.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PTopBar(coins, stars, back = onBack); Spacer(Modifier.height(20.dp)); Box(Modifier.size(84.dp).background(Brush.radialGradient(listOf(PSky, PBlue)), CircleShape).border(2.dp, Color.White.copy(.35f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(46.dp)) }; Spacer(Modifier.height(9.dp)); Text("Oyuncu Profili", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Black); Text("Seviye $playerLevel", color = PGold, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp)); PGlass(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("XP", color = Color.White, fontWeight = FontWeight.Bold); Text("$xp/15", color = PGold, fontWeight = FontWeight.Black) }; Spacer(Modifier.height(6.dp)); LinearProgressIndicator(progress = { xp / 15f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = PGold, trackColor = Color.White.copy(.1f)) }
        val rows = listOf(Triple(Icons.Filled.Star, "Toplam yıldız", "$stars/${levels.size * 3}"), Triple(Icons.Filled.CheckCircle, "Tamamlanan level", "$completed/${levels.size}"), Triple(Icons.Filled.Map, "Açılan level", "${unlocked + 1}"), Triple(Icons.Filled.AutoAwesome, "Doğru kelime", words.toString()), Triple(Icons.Filled.Lightbulb, "Kullanılan ipucu", hints.toString()), Triple(Icons.Filled.LocalFireDepartment, "En iyi combo", "x$combo"), Triple(Icons.Filled.CardGiftcard, "Günlük seri", "$streak gün"))
        rows.forEach { (icon, label, value) -> PGlass(Modifier.fillMaxWidth().padding(top = 8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = PGold, modifier = Modifier.size(23.dp)); Spacer(Modifier.width(10.dp)); Text(label, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(value, color = PGold, fontWeight = FontWeight.Black) } } }; Spacer(Modifier.height(18.dp))
    } }
}

@Composable
private fun PAchievementsScreen(stats: PStats, claimed: Set<String>, coins: Int, onBack: () -> Unit, onClaim: (PAchievement) -> Unit) {
    PBackground { Column(Modifier.fillMaxSize().padding(17.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PTopBar(coins, stats.stars, back = onBack); Spacer(Modifier.height(15.dp)); Icon(Icons.Filled.EmojiEvents, null, tint = PGold, modifier = Modifier.size(52.dp)); Text("Görevler & Başarımlar", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black); Text("İlerlemeni gör, ödülünü tek dokunuşla al", color = PSoft, fontSize = 11.sp); Spacer(Modifier.height(10.dp))
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) { pAchievements().forEach { a -> val current = a.current(stats).coerceAtMost(a.goal); val reached = current >= a.goal; val isClaimed = a.id in claimed; PGlass(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(48.dp).background(if (reached) PGold.copy(.16f) else Color.White.copy(.07f), CircleShape), contentAlignment = Alignment.Center) { Icon(if (isClaimed) Icons.Filled.CheckCircle else Icons.Filled.MilitaryTech, null, tint = if (reached) PGold else PSoft) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(a.title, color = Color.White, fontWeight = FontWeight.Black); Text(a.description, color = PSoft, fontSize = 10.sp) }; Text("+${a.reward}", color = PGold, fontWeight = FontWeight.Black) }
            Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("$current/${a.goal}", color = PSoft, fontSize = 10.sp); Text(if (isClaimed) "ALINDI" else if (reached) "HAZIR" else "DEVAM", color = if (reached && !isClaimed) PGreen else PSoft, fontSize = 9.sp, fontWeight = FontWeight.Black) }; Spacer(Modifier.height(5.dp)); LinearProgressIndicator(progress = { current.toFloat() / a.goal.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = if (reached) PGreen else PElectric, trackColor = Color.White.copy(.1f)); if (reached && !isClaimed) { Spacer(Modifier.height(8.dp)); PGradientButton("Ödülü Al", icon = Icons.Filled.Redeem, colors = listOf(PGreen, PGreenDark), onClick = { onClaim(a) }, modifier = Modifier.fillMaxWidth()) }
        } }; Spacer(Modifier.height(15.dp)) }
    } }
}

@Composable
private fun PSettingsScreen(sound: Boolean, haptic: Boolean, reduceMotion: Boolean, onBack: () -> Unit, onSound: () -> Unit, onHaptic: () -> Unit, onMotion: () -> Unit) {
    PBackground { Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) { PTopBar(0, back = onBack); Spacer(Modifier.height(24.dp)); Icon(Icons.Filled.Settings, null, tint = PSky, modifier = Modifier.size(55.dp)); Text("Ayarlar", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black); Text("Oyun deneyimini kendine göre ayarla", color = PSoft, fontSize = 11.sp); Spacer(Modifier.height(21.dp)); PSetting(Icons.Filled.VolumeUp, "Ses Efektleri", sound, onSound); Spacer(Modifier.height(10.dp)); PSetting(Icons.Filled.Vibration, "Titreşim", haptic, onHaptic); Spacer(Modifier.height(10.dp)); PSetting(Icons.Filled.AutoAwesome, "Animasyonları Azalt", reduceMotion, onMotion); Spacer(Modifier.height(18.dp)); PGlass(Modifier.fillMaxWidth()) { Text("WordZ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp); Text("Sürüm ${BuildConfig.VERSION_NAME}", color = PSoft, fontSize = 11.sp); Text("Tüm tercihler otomatik kaydedilir.", color = PSoft, fontSize = 11.sp) } } }
}

@Composable
private fun PSetting(icon: ImageVector, title: String, enabled: Boolean, onClick: () -> Unit) { val shape = RoundedCornerShape(21.dp); Row(Modifier.fillMaxWidth().shadow(8.dp, shape).clip(shape).background(PCard).border(1.dp, PBorder, shape).clickable(onClick = onClick).padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(45.dp).background(Color.White.copy(.09f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = PSky) }; Spacer(Modifier.width(11.dp)); Text(title, color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f)); Box(Modifier.width(60.dp).height(31.dp).clip(CircleShape).background(if (enabled) PGreen else Color(0xFF47546B)).padding(4.dp)) { Box(Modifier.size(23.dp).align(if (enabled) Alignment.CenterEnd else Alignment.CenterStart).background(Color.White, CircleShape)) } } }

@Composable
private fun PGameScreen(levelIndex: Int, coins: Int, previousStars: Int, soundEnabled: Boolean, hapticEnabled: Boolean, reduceMotion: Boolean, wasCompletedBefore: Boolean, isChapterLast: Boolean, chapterChestClaimed: Boolean, onCoinsChanged: (Int) -> Unit, onWordFound: () -> Unit, onHintUsed: () -> Unit, onComboChanged: (Int) -> Unit, onBack: () -> Unit, onCompleted: (Int) -> Unit, onNext: () -> Unit, onMap: () -> Unit, onRetry: () -> Unit, onClaimChapterChest: () -> Unit) {
    val level = levels[levelIndex]; val chapterIndex = (levelIndex / 5).coerceIn(0, 6); val haptics = LocalHapticFeedback.current; val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 65) }; DisposableEffect(Unit) { onDispose { tone.release() } }
    var wheelLetters by remember { mutableStateOf(level.letters) }; var foundWords by remember { mutableStateOf(setOf<String>()) }; var foundBonus by remember { mutableStateOf(setOf<String>()) }; var hinted by remember { mutableStateOf(setOf<PCell>()) }; var selected by remember { mutableStateOf(emptyList<Int>()) }; var live by remember { mutableStateOf("") }; var status by remember { mutableStateOf("Harfleri sürükleyerek birleştir") }; var hintsUsed by remember { mutableIntStateOf(0) }; var combo by remember { mutableIntStateOf(0) }; var bestLevelCombo by remember { mutableIntStateOf(0) }; var grossEarned by remember { mutableIntStateOf(0) }; var spent by remember { mutableIntStateOf(0) }; var completionHandled by remember { mutableStateOf(false) }; var showResult by remember { mutableStateOf(false) }; var revealedStars by remember { mutableIntStateOf(0) }
    val earnedStars = when (hintsUsed) { 0 -> 3; 1 -> 2; 2 -> 1; else -> 0 }; val complete = level.targets.all { it in foundWords }
    LaunchedEffect(complete) { if (complete && !completionHandled) { completionHandled = true; onCompleted(earnedStars); if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_ACK, 220); if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress); showResult = true; if (reduceMotion) revealedStars = earnedStars else { revealedStars = 0; repeat(earnedStars) { delay(260); revealedStars = it + 1 } } } }
    val statusScale by animateFloatAsState(if (live.isNotBlank()) 1.035f else 1f, tween(if (reduceMotion) 0 else 130), label = "status")
    PBackground(chapterIndex) { BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 760.dp; val wheel = if (compact) 205.dp else 238.dp
        Column(Modifier.fillMaxSize().padding(horizontal = 13.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { PRoundIcon(Icons.Filled.ArrowBack, onBack); Spacer(Modifier.weight(1f)); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("LEVEL ${levelIndex + 1}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp); Text(level.title, color = PSoft, fontSize = 10.sp) }; Spacer(Modifier.weight(1f)); PHud(Icons.Filled.AutoAwesome, coins.toString()) }
            Spacer(Modifier.height(if (compact) 4.dp else 7.dp)); PCrossword(level, foundWords, hinted, compact); Spacer(Modifier.height(5.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("${foundWords.size}/${level.targets.size} kelime · Bonus ${foundBonus.size}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); Row(verticalAlignment = Alignment.CenterVertically) { if (combo >= 2) { Icon(Icons.Filled.LocalFireDepartment, null, tint = POrange, modifier = Modifier.size(17.dp)); Text(" x$combo", color = PGold, fontWeight = FontWeight.Black, fontSize = 11.sp) } else Text("★".repeat(earnedStars) + "☆".repeat(3 - earnedStars), color = PGold, fontSize = 14.sp) } }
            Spacer(Modifier.height(4.dp)); Box(Modifier.fillMaxWidth().scale(statusScale).clip(RoundedCornerShape(17.dp)).background(Brush.horizontalGradient(listOf(Color(0xE00D3A80), Color(0xE01659B0)))).border(1.dp, PBorder, RoundedCornerShape(17.dp)).padding(vertical = 8.dp, horizontal = 10.dp), contentAlignment = Alignment.Center) { Text(if (live.isNotBlank()) live else status, color = when { live.isNotBlank() -> Color.White; status.startsWith("Bonus") || status.startsWith("Combo") -> PGold; status.startsWith("Harika") -> Color(0xFF88F2B5); else -> PSoft }, fontWeight = FontWeight.Black, fontSize = if (live.isNotBlank()) 20.sp else 11.sp, textAlign = TextAlign.Center) }
            Spacer(Modifier.height(if (compact) 1.dp else 4.dp)); PLetterWheel(wheelLetters, selected, wheel, hapticEnabled, haptics,
                onSelectionChanged = { selected = it; live = it.joinToString("") { idx -> wheelLetters[idx].toString() } },
                onSelectionFinished = {
                    val word = live.trim().uppercase(pTr)
                    when {
                        word in level.targets && word !in foundWords -> { foundWords = foundWords + word; combo += 1; bestLevelCombo = maxOf(bestLevelCombo, combo); onWordFound(); onComboChanged(combo); val comboReward = if (combo % 3 == 0) 10 else 0; val reward = 5 + comboReward; grossEarned += reward; onCoinsChanged(coins + reward); status = if (comboReward > 0) "Combo x$combo! +$reward jeton" else "Harika! $word"; if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 100) }
                        word in foundWords -> status = "$word zaten bulundu"
                        word in level.bonusWords && word !in foundBonus -> { foundBonus = foundBonus + word; combo += 1; bestLevelCombo = maxOf(bestLevelCombo, combo); onWordFound(); onComboChanged(combo); val bonus = 10 + if (combo % 3 == 0) 10 else 0; grossEarned += bonus; onCoinsChanged(coins + bonus); status = "Bonus kelime! +$bonus"; if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 90) }
                        word in foundBonus -> status = "$word bonus olarak bulundu"
                        word.isNotBlank() -> { combo = 0; status = "$word bu levelde yok · combo sıfırlandı"; if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_NACK, 90) }
                    }
                    selected = emptyList(); live = ""
                })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PGameButton("Karıştır", Icons.Filled.Shuffle, Color(0xFF2587E9), true, Modifier.weight(1f)) { wheelLetters = wheelLetters.shuffled(); selected = emptyList(); live = ""; status = "Harfler karıştırıldı" }
                PGameButton("Harf 25", Icons.Filled.Lightbulb, Color(0xFF38B65E), coins >= 25 && !complete, Modifier.weight(1f)) { val hidden = pAllCells(level).filter { it !in hinted && !pRevealed(level, it, foundWords) }; hidden.randomOrNull()?.let { onCoinsChanged(coins - 25); spent += 25; hinted = hinted + it; hintsUsed += 1; combo = 0; onHintUsed(); status = "Bir harf açıldı · yıldız etkilenir"; if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 70) } }
                PGameButton("Kelime 60", Icons.Filled.Search, PPurple, coins >= 60 && !complete, Modifier.weight(1f)) { level.targets.firstOrNull { it !in foundWords }?.let { onCoinsChanged(coins - 60); spent += 60; foundWords = foundWords + it; hintsUsed += 2; combo = 0; onHintUsed(); status = "Kelime ipucu: $it"; if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 90) } }
            }
        }
        if (showResult) PResultOverlay(earnedStars, revealedStars, previousStars, foundWords.size, foundBonus.size, hintsUsed, bestLevelCombo, grossEarned + if (!wasCompletedBefore) 25 else 0, spent, isChapterLast, chapterChestClaimed, onRetry, onMap, onNext, onClaimChapterChest)
    } }
}

@Composable
private fun PResultOverlay(earnedStars: Int, revealedStars: Int, previousStars: Int, words: Int, bonus: Int, hints: Int, combo: Int, gross: Int, spent: Int, chapterFinished: Boolean, chapterChestClaimed: Boolean, onRetry: () -> Unit, onMap: () -> Unit, onNext: () -> Unit, onClaimChest: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(.72f)).padding(18.dp), contentAlignment = Alignment.Center) {
        PGlass(Modifier.fillMaxWidth()) {
            Icon(if (chapterFinished) Icons.Filled.EmojiEvents else Icons.Filled.CheckCircle, null, tint = if (chapterFinished) PGold else Color(0xFF8CF5BA), modifier = Modifier.align(Alignment.CenterHorizontally).size(54.dp)); Spacer(Modifier.height(7.dp))
            Text(if (chapterFinished) "BÖLÜM TAMAMLANDI!" else "LEVEL TAMAMLANDI!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(if (chapterFinished) "Yeni bölüm yolculuğuna hazırsın" else "Harika iş! Özetin burada", color = PSoft, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(13.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { repeat(3) { i -> val active = i < earnedStars; val shown = i < revealedStars; val scale by animateFloatAsState(if (shown) 1f else .45f, tween(220), label = "star$i"); Icon(if (active) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (shown && active) PGold else Color.White.copy(.28f), modifier = Modifier.size(48.dp).scale(scale)) } }
            if (earnedStars > previousStars) Text("YENİ YILDIZ REKORU", color = PGold, fontWeight = FontWeight.Black, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { PResultStat("Kelime", words.toString(), Modifier.weight(1f)); PResultStat("Bonus", bonus.toString(), Modifier.weight(1f)); PResultStat("İpucu", hints.toString(), Modifier.weight(1f)); PResultStat("Combo", "x$combo", Modifier.weight(1f)) }
            Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Toplam kazanç", color = PSoft, fontSize = 11.sp); Text("+$gross jeton", color = PGold, fontWeight = FontWeight.Black) }; if (spent > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("İpucu maliyeti", color = PSoft, fontSize = 11.sp); Text("-$spent", color = PRed, fontWeight = FontWeight.Black) }
            if (chapterFinished && !chapterChestClaimed) { Spacer(Modifier.height(10.dp)); PGradientButton("Bölüm Sandığını Aç", "+150 jeton", Icons.Filled.Redeem, listOf(Color(0xFFFFC64B), PGoldDark), onClaimChest, Modifier.fillMaxWidth()) }
            Spacer(Modifier.height(11.dp)); PGradientButton(if (chapterFinished) "Haritaya Dön" else "Sonraki Level", icon = if (chapterFinished) Icons.Filled.Map else Icons.Filled.PlayArrow, colors = listOf(PGreen, PGreenDark), onClick = if (chapterFinished) onMap else onNext, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(7.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { PSmallButton("Tekrar Oyna", Icons.Filled.Refresh, onRetry, Modifier.weight(1f)); PSmallButton("Harita", Icons.Filled.Map, onMap, Modifier.weight(1f)) }
        }
    }
}

@Composable private fun PResultStat(label: String, value: String, modifier: Modifier = Modifier) { Column(modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(.07f)).padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp); Text(label, color = PSoft, fontSize = 8.sp) } }
@Composable private fun PSmallButton(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) { Row(modifier.height(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xC80C397A)).border(1.dp, PBorder, RoundedCornerShape(16.dp)).clickable(onClick = onClick), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp) } }

@Composable
private fun PGameButton(title: String, icon: ImageVector, color: Color, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) { val shape = RoundedCornerShape(18.dp); Column(modifier.height(60.dp).clip(shape).background(if (enabled) color else Color(0xFF3D4960)).border(1.dp, Color.White.copy(.22f), shape).clickable(enabled = enabled, onClick = onClick).padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp)); Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 9.sp) } }

private fun pCellsFor(p: Placement): List<Pair<PCell, Char>> = p.word.mapIndexed { i, ch -> PCell(p.row + if (p.horizontal) 0 else i, p.col + if (p.horizontal) i else 0) to ch }
private fun pAllCells(level: Level): Set<PCell> = level.placements.flatMap(::pCellsFor).map { it.first }.toSet()
private fun pRevealed(level: Level, cell: PCell, found: Set<String>): Boolean = level.placements.any { it.word in found && pCellsFor(it).any { pair -> pair.first == cell } }

@Composable
private fun PCrossword(level: Level, found: Set<String>, hinted: Set<PCell>, compact: Boolean) {
    val entries = remember(level) { level.placements.flatMap(::pCellsFor) }; val letters = remember(level) { val map = mutableMapOf<PCell, Char>(); entries.forEach { (cell, char) -> if (map[cell] == null || map[cell] == char) map[cell] = char }; map.toMap() }; val rows = (letters.keys.maxOfOrNull { it.row } ?: 0) + 1; val cols = (letters.keys.maxOfOrNull { it.col } ?: 0) + 1; val cellSize = when { compact && cols >= 5 -> 33.dp; cols >= 6 -> 35.dp; cols == 5 -> 39.dp; else -> 43.dp }
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp)).background(Brush.verticalGradient(listOf(Color(0x8A0F408E), Color(0xA00F285C)))).border(1.dp, PBorder, RoundedCornerShape(23.dp)).padding(if (compact) 7.dp else 9.dp), contentAlignment = Alignment.Center) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { repeat(rows) { r -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(cols) { c -> val cell = PCell(r, c); val letter = letters[cell]; if (letter == null) Spacer(Modifier.size(cellSize)) else { val open = cell in hinted || pRevealed(level, cell, found); val shape = RoundedCornerShape(8.dp); val scale by animateFloatAsState(if (open) 1f else .96f, tween(140), label = "cell"); Box(Modifier.size(cellSize).scale(scale).shadow(if (open) 5.dp else 0.dp, shape).clip(shape).background(if (open) Color(0xFFF7FBFF) else Color(0xFF1678DC)).border(2.dp, if (open) Color(0xFFB9E5FF) else Color(0xFF5BD0FF), shape), contentAlignment = Alignment.Center) { if (open) Text(letter.toString(), color = Color(0xFF0C3475), fontWeight = FontWeight.Black, fontSize = if (compact) 15.sp else 18.sp) } } } } } }
    }
}

@Composable
private fun PLetterWheel(letters: List<Char>, selected: List<Int>, wheelSize: Dp, hapticEnabled: Boolean, haptics: androidx.compose.ui.hapticfeedback.HapticFeedback, onSelectionChanged: (List<Int>) -> Unit, onSelectionFinished: () -> Unit) {
    var measured by remember { mutableStateOf(IntSize.Zero) }; var finger by remember { mutableStateOf<Offset?>(null) }
    fun positions(): List<Offset> { if (measured == IntSize.Zero) return emptyList(); val center = Offset(measured.width / 2f, measured.height / 2f); val radius = minOf(measured.width, measured.height) * .33f; return letters.indices.map { i -> val angle = -PI / 2 + 2 * PI * i / letters.size; Offset(center.x + (cos(angle) * radius).toFloat(), center.y + (sin(angle) * radius).toFloat()) } }
    fun indexAt(point: Offset): Int? { val pts = positions(); if (pts.isEmpty()) return null; val hit = minOf(measured.width, measured.height) * .14f; return pts.indices.firstOrNull { i -> hypot((point.x - pts[i].x).toDouble(), (point.y - pts[i].y).toDouble()) <= hit } }
    Box(Modifier.size(wheelSize).onSizeChanged { measured = it }.pointerInput(letters, measured) {
        var gesture = emptyList<Int>()
        detectDragGestures(onDragStart = { p -> finger = p; val idx = indexAt(p); gesture = if (idx != null) listOf(idx) else emptyList(); onSelectionChanged(gesture) }, onDragEnd = { finger = null; onSelectionFinished(); gesture = emptyList() }, onDragCancel = { finger = null; onSelectionFinished(); gesture = emptyList() }, onDrag = { change, _ -> finger = change.position; val idx = indexAt(change.position); if (idx != null) { val backtrack = gesture.size >= 2 && idx == gesture[gesture.lastIndex - 1]; when { backtrack -> { gesture = gesture.dropLast(1); onSelectionChanged(gesture) }; idx !in gesture -> { gesture = gesture + idx; if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); onSelectionChanged(gesture) } } } })
    }) {
        Canvas(Modifier.fillMaxSize()) { val center = Offset(size.width / 2f, size.height / 2f); val radius = minOf(size.width, size.height) * .47f; drawCircle(Color(0xFFF1F7FF), radius, center); drawCircle(Color(0xFFB6D7F3), radius, center, style = Stroke(5f)); drawCircle(Color(0x2272BEFF), radius * .83f, center); val pts = positions(); selected.zipWithNext().forEach { (a, b) -> if (a in pts.indices && b in pts.indices) drawLine(PSky, pts[a], pts[b], 18f, cap = StrokeCap.Round) }; if (selected.isNotEmpty() && selected.last() in pts.indices && finger != null) drawLine(PSky.copy(.8f), pts[selected.last()], finger!!, 12f, cap = StrokeCap.Round); pts.forEachIndexed { i, p -> val sel = i in selected; val rr = minOf(size.width, size.height) * if (sel) .115f else .107f; drawCircle(if (sel) Color(0xFF118BFF) else Color.White, rr, p); drawCircle(if (sel) PSky else Color(0xFFB8D2EA), rr, p, style = Stroke(4f)); drawIntoCanvas { canvas -> val paint = android.graphics.Paint().apply { color = if (sel) android.graphics.Color.WHITE else android.graphics.Color.rgb(17, 49, 96); textAlign = android.graphics.Paint.Align.CENTER; textSize = minOf(size.width, size.height) * .105f; typeface = android.graphics.Typeface.DEFAULT_BOLD }; val fm = paint.fontMetrics; canvas.nativeCanvas.drawText(letters[i].toString(), p.x, p.y - (fm.ascent + fm.descent) / 2f, paint) } } }
    }
}
