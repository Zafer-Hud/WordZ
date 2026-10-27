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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageVector
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import java.time.LocalDate
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private val DeepNavy = Color(0xFF06163D)
private val RoyalBlue = Color(0xFF083B98)
private val ElectricBlue = Color(0xFF1497FF)
private val SkyBlue = Color(0xFF63C8FF)
private val Gold = Color(0xFFFFC83D)
private val GoldDark = Color(0xFFE89513)
private val Emerald = Color(0xFF20C866)
private val EmeraldDark = Color(0xFF07953D)
private val Purple = Color(0xFF8D54F5)
private val SoftText = Color(0xFFBFD7FF)
private val CardBlue = Color(0xD9143472)
private val CardBorder = Color(0x665FCBFF)
private val DangerRed = Color(0xFFFF6E75)

private data class ModernCell(val row: Int, val col: Int)
private data class ModernChapter(val name: String, val subtitle: String, val start: Int, val endExclusive: Int)
private data class ModernAchievement(
    val id: String,
    val title: String,
    val description: String,
    val reward: Int,
    val reached: (ModernStats) -> Boolean,
)
private data class ModernStats(
    val completed: Int,
    val stars: Int,
    val words: Int,
    val hints: Int,
    val bestCombo: Int,
    val streak: Int,
)
private enum class ModernScreen { HOME, CHAPTERS, MAP, GAME, SETTINGS, PROFILE, ACHIEVEMENTS }

private const val PREFS_MODERN = "wordz_progress"
private const val KEY_UNLOCKED_MODERN = "unlocked_level"
private const val KEY_COINS_MODERN = "coins"
private const val KEY_COMPLETED_MODERN = "completed_levels"
private const val KEY_STARS_MODERN = "level_stars"
private const val KEY_DAILY_DATE_MODERN = "daily_reward_date"
private const val KEY_CLAIMED_CHESTS_MODERN = "claimed_star_chests"
private const val KEY_SOUND_MODERN = "sound_enabled"
private const val KEY_HAPTIC_MODERN = "haptic_enabled"
private const val KEY_STREAK_MODERN = "daily_streak"
private const val KEY_CHAPTER_CHESTS_MODERN = "claimed_chapter_chests"
private const val KEY_ACHIEVEMENTS_MODERN = "claimed_achievements"
private const val KEY_WORDS_MODERN = "stats_words"
private const val KEY_HINTS_MODERN = "stats_hints"
private const val KEY_BEST_COMBO_MODERN = "stats_best_combo"

private val modernTrLocale = Locale("tr", "TR")

class ModernMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = ElectricBlue,
                    secondary = Gold,
                    background = DeepNavy,
                    surface = DeepNavy,
                    onPrimary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White,
                )
            ) {
                ModernWordZApp()
            }
        }
    }
}

private fun modernChapters(): List<ModernChapter> = levels.indices.chunked(5).mapIndexed { index, chunk ->
    val names = listOf(
        "Başlangıç Vadisi",
        "Sahil Yolu",
        "Yeşil Orman",
        "Güneş Tepeleri",
        "Gece Rotası",
        "Uzak Diyarlar",
        "Zafer Zirvesi",
    )
    val flavor = listOf(
        "Macera burada başlıyor",
        "Dalgaların arasındaki kelimeler",
        "Ormanın gizli patikaları",
        "Güneşe doğru yüksel",
        "Gece yıldızlarını topla",
        "Yeni diyarlara yolculuk",
        "Zirveye son adımlar",
    )
    ModernChapter(
        name = names.getOrElse(index) { "Macera" },
        subtitle = flavor.getOrElse(index) { "Yeni kelimeleri keşfet" },
        start = chunk.first(),
        endExclusive = chunk.last() + 1,
    )
}

private fun modernDecodeStars(raw: Set<String>): Map<Int, Int> = raw.mapNotNull { item ->
    val parts = item.split(':')
    val level = parts.getOrNull(0)?.toIntOrNull()
    val star = parts.getOrNull(1)?.toIntOrNull()
    if (level != null && star != null && level in levels.indices) level to star.coerceIn(0, 3) else null
}.toMap()

private fun modernEncodeStars(stars: Map<Int, Int>): Set<String> = stars.map { "${it.key}:${it.value}" }.toSet()

private fun modernAchievements(): List<ModernAchievement> = listOf(
    ModernAchievement("first", "İlk Adım", "İlk levelini tamamla", 50) { it.completed >= 1 },
    ModernAchievement("stars15", "Yıldız Avcısı", "15 yıldız topla", 75) { it.stars >= 15 },
    ModernAchievement("words50", "Kelime Ustası", "50 doğru kelime bul", 100) { it.words >= 50 },
    ModernAchievement("combo5", "Seri Ustası", "Combo x5 yap", 100) { it.bestCombo >= 5 },
    ModernAchievement("streak3", "Sadık Oyuncu", "3 günlük seri yap", 120) { it.streak >= 3 },
)

@Composable
private fun ModernWordZApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_MODERN, Context.MODE_PRIVATE) }

    val savedUnlocked = prefs.getInt(KEY_UNLOCKED_MODERN, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(KEY_COMPLETED_MODERN, emptySet()).orEmpty()
        .mapNotNull { it.toIntOrNull() }
        .filter { it in levels.indices }
        .toSet()
    val migratedCompleted = if (savedCompleted.isEmpty() && savedUnlocked > 0) (0 until savedUnlocked).toSet() else savedCompleted
    val savedStars = modernDecodeStars(prefs.getStringSet(KEY_STARS_MODERN, emptySet()).orEmpty()).toMutableMap()
    migratedCompleted.forEach { if (it !in savedStars) savedStars[it] = 1 }

    var unlockedLevel by remember { mutableIntStateOf(savedUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(KEY_COINS_MODERN, 100)) }
    var completedLevels by remember { mutableStateOf(migratedCompleted) }
    var levelStars by remember { mutableStateOf(savedStars.toMap()) }
    var claimedChests by remember { mutableIntStateOf(prefs.getInt(KEY_CLAIMED_CHESTS_MODERN, 0)) }
    var dailyDate by remember { mutableStateOf(prefs.getString(KEY_DAILY_DATE_MODERN, "").orEmpty()) }
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_SOUND_MODERN, true)) }
    var hapticEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_HAPTIC_MODERN, true)) }
    var streak by remember { mutableIntStateOf(prefs.getInt(KEY_STREAK_MODERN, 0)) }
    var claimedChapterChests by remember { mutableStateOf(prefs.getStringSet(KEY_CHAPTER_CHESTS_MODERN, emptySet()).orEmpty()) }
    var claimedAchievements by remember { mutableStateOf(prefs.getStringSet(KEY_ACHIEVEMENTS_MODERN, emptySet()).orEmpty()) }
    var totalWords by remember { mutableIntStateOf(prefs.getInt(KEY_WORDS_MODERN, 0)) }
    var totalHints by remember { mutableIntStateOf(prefs.getInt(KEY_HINTS_MODERN, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(KEY_BEST_COMBO_MODERN, 0)) }

    var screen by remember { mutableStateOf(ModernScreen.HOME) }
    var selectedLevel by remember { mutableIntStateOf(unlockedLevel) }
    var selectedChapter by remember { mutableIntStateOf((unlockedLevel / 5).coerceIn(0, modernChapters().lastIndex)) }

    val totalStars = levelStars.values.sum()
    val availableChests = (totalStars / 15 - claimedChests).coerceAtLeast(0)
    val today = LocalDate.now()
    val dailyAvailable = dailyDate != today.toString()
    val stats = ModernStats(completedLevels.size, totalStars, totalWords, totalHints, bestCombo, streak)

    fun persist(
        savedUnlockedLevel: Int = unlockedLevel,
        savedCoins: Int = coins,
        savedCompletedLevels: Set<Int> = completedLevels,
        savedLevelStars: Map<Int, Int> = levelStars,
        savedClaimedChests: Int = claimedChests,
        savedDailyDate: String = dailyDate,
        savedSoundEnabled: Boolean = soundEnabled,
        savedHapticEnabled: Boolean = hapticEnabled,
        savedStreak: Int = streak,
        savedChapterChests: Set<String> = claimedChapterChests,
        savedAchievements: Set<String> = claimedAchievements,
        savedWords: Int = totalWords,
        savedHints: Int = totalHints,
        savedBestCombo: Int = bestCombo,
    ) {
        prefs.edit()
            .putInt(KEY_UNLOCKED_MODERN, savedUnlockedLevel)
            .putInt(KEY_COINS_MODERN, savedCoins)
            .putStringSet(KEY_COMPLETED_MODERN, savedCompletedLevels.map(Int::toString).toSet())
            .putStringSet(KEY_STARS_MODERN, modernEncodeStars(savedLevelStars))
            .putInt(KEY_CLAIMED_CHESTS_MODERN, savedClaimedChests)
            .putString(KEY_DAILY_DATE_MODERN, savedDailyDate)
            .putBoolean(KEY_SOUND_MODERN, savedSoundEnabled)
            .putBoolean(KEY_HAPTIC_MODERN, savedHapticEnabled)
            .putInt(KEY_STREAK_MODERN, savedStreak)
            .putStringSet(KEY_CHAPTER_CHESTS_MODERN, savedChapterChests)
            .putStringSet(KEY_ACHIEVEMENTS_MODERN, savedAchievements)
            .putInt(KEY_WORDS_MODERN, savedWords)
            .putInt(KEY_HINTS_MODERN, savedHints)
            .putInt(KEY_BEST_COMBO_MODERN, savedBestCombo)
            .apply()
    }

    when (screen) {
        ModernScreen.HOME -> ModernHomeScreen(
            coins = coins,
            completedCount = completedLevels.size,
            totalStars = totalStars,
            unlockedLevel = unlockedLevel,
            streak = streak,
            dailyAvailable = dailyAvailable,
            availableChests = availableChests,
            claimableAchievements = modernAchievements().count { it.reached(stats) && it.id !in claimedAchievements },
            onContinue = {
                selectedLevel = unlockedLevel
                selectedChapter = (unlockedLevel / 5).coerceIn(0, modernChapters().lastIndex)
                screen = ModernScreen.GAME
            },
            onChapters = { screen = ModernScreen.CHAPTERS },
            onDaily = {
                if (dailyAvailable) {
                    val newStreak = if (dailyDate == today.minusDays(1).toString()) streak + 1 else 1
                    val newCoins = coins + 40
                    val newDate = today.toString()
                    streak = newStreak
                    coins = newCoins
                    dailyDate = newDate
                    persist(savedCoins = newCoins, savedDailyDate = newDate, savedStreak = newStreak)
                }
            },
            onChest = {
                if (availableChests > 0) {
                    val newCoins = coins + 75
                    val newClaimed = claimedChests + 1
                    coins = newCoins
                    claimedChests = newClaimed
                    persist(savedCoins = newCoins, savedClaimedChests = newClaimed)
                }
            },
            onProfile = { screen = ModernScreen.PROFILE },
            onAchievements = { screen = ModernScreen.ACHIEVEMENTS },
            onSettings = { screen = ModernScreen.SETTINGS },
        )

        ModernScreen.PROFILE -> ModernProfileScreen(
            coins = coins,
            totalStars = totalStars,
            completed = completedLevels.size,
            unlockedLevel = unlockedLevel,
            words = totalWords,
            hints = totalHints,
            bestCombo = bestCombo,
            streak = streak,
            onBack = { persist(); screen = ModernScreen.HOME },
        )

        ModernScreen.ACHIEVEMENTS -> ModernAchievementsScreen(
            stats = stats,
            claimed = claimedAchievements,
            onClaim = { achievement ->
                if (achievement.reached(stats) && achievement.id !in claimedAchievements) {
                    val newClaimed = claimedAchievements + achievement.id
                    val newCoins = coins + achievement.reward
                    claimedAchievements = newClaimed
                    coins = newCoins
                    persist(savedCoins = newCoins, savedAchievements = newClaimed)
                }
            },
            onBack = { persist(); screen = ModernScreen.HOME },
        )

        ModernScreen.SETTINGS -> ModernSettingsScreen(
            soundEnabled = soundEnabled,
            hapticEnabled = hapticEnabled,
            onToggleSound = {
                val value = !soundEnabled
                soundEnabled = value
                persist(savedSoundEnabled = value)
            },
            onToggleHaptic = {
                val value = !hapticEnabled
                hapticEnabled = value
                persist(savedHapticEnabled = value)
            },
            onBack = { persist(); screen = ModernScreen.HOME },
        )

        ModernScreen.CHAPTERS -> ModernChapterScreen(
            unlockedLevel = unlockedLevel,
            stars = levelStars,
            completedLevels = completedLevels,
            claimedChapterChests = claimedChapterChests,
            coins = coins,
            onBack = { persist(); screen = ModernScreen.HOME },
            onSelect = { index -> selectedChapter = index; screen = ModernScreen.MAP },
            onClaimChapterChest = { index ->
                val chapter = modernChapters()[index]
                val key = index.toString()
                val complete = (chapter.start until chapter.endExclusive).all { it in completedLevels }
                if (complete && key !in claimedChapterChests) {
                    val newClaimed = claimedChapterChests + key
                    val newCoins = coins + 150
                    claimedChapterChests = newClaimed
                    coins = newCoins
                    persist(savedCoins = newCoins, savedChapterChests = newClaimed)
                }
            },
        )

        ModernScreen.MAP -> ModernZigzagLevelMap(
            chapterIndex = selectedChapter,
            unlockedLevel = unlockedLevel,
            stars = levelStars,
            completedLevels = completedLevels,
            coins = coins,
            onBack = { persist(); screen = ModernScreen.CHAPTERS },
            onSelect = { selectedLevel = it; screen = ModernScreen.GAME },
        )

        ModernScreen.GAME -> ModernGameScreen(
            levelIndex = selectedLevel,
            coins = coins,
            previousStars = levelStars[selectedLevel] ?: 0,
            soundEnabled = soundEnabled,
            hapticEnabled = hapticEnabled,
            onCoinsChanged = { newCoins ->
                coins = newCoins
                persist(savedCoins = newCoins)
            },
            onWordFound = {
                val newWords = totalWords + 1
                totalWords = newWords
                persist(savedWords = newWords)
            },
            onHintUsed = {
                val newHints = totalHints + 1
                totalHints = newHints
                persist(savedHints = newHints)
            },
            onComboChanged = { value ->
                if (value > bestCombo) {
                    bestCombo = value
                    persist(savedBestCombo = value)
                }
            },
            onBack = { persist(); screen = ModernScreen.MAP },
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
                persist(
                    savedUnlockedLevel = newUnlocked,
                    savedCoins = newCoins,
                    savedCompletedLevels = newCompleted,
                    savedLevelStars = newStars,
                )
            },
            onNext = {
                val chapter = modernChapters()[selectedChapter]
                if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) selectedLevel += 1 else screen = ModernScreen.MAP
            },
        )
    }
}

@Composable
private fun ModernBackground(content: @Composable Box.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF064BB7), Color(0xFF102770), Color(0xFF07163F), Color(0xFF050D28))
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0x334DB8FF), size.minDimension * 0.55f, Offset(size.width * 0.12f, size.height * 0.28f))
            drawCircle(Color(0x226E54FF), size.minDimension * 0.48f, Offset(size.width * 0.95f, size.height * 0.54f))
            drawCircle(Color(0x2247E8FF), size.minDimension * 0.34f, Offset(size.width * 0.52f, size.height * 0.96f))
            val stars = listOf(
                0.12f to 0.12f, 0.76f to 0.08f, 0.9f to 0.21f, 0.22f to 0.36f,
                0.68f to 0.31f, 0.83f to 0.47f, 0.11f to 0.63f, 0.55f to 0.67f,
                0.92f to 0.76f, 0.29f to 0.82f, 0.67f to 0.9f,
            )
            stars.forEachIndexed { index, pair ->
                drawCircle(
                    if (index % 3 == 0) Gold.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.65f),
                    if (index % 2 == 0) 3.2f else 2.1f,
                    Offset(size.width * pair.first, size.height * pair.second),
                )
            }
        }
        content()
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable Column.() -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier
            .shadow(14.dp, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xDD174A9C), CardBlue, Color(0xDD0C2A63))
                )
            )
            .border(1.dp, CardBorder, shape)
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun HudPill(icon: ImageVector, text: String, iconTint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xC90A2B66))
            .border(1.dp, Color(0x665FCBFF), RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
    }
}

@Composable
private fun RoundIconButton(icon: ImageVector, onClick: () -> Unit, badge: Boolean = false) {
    Box(
        Modifier
            .size(44.dp)
            .shadow(9.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(0xD90D397E))
            .border(1.dp, Color(0x775CC8FF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(23.dp))
        if (badge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(11.dp)
                    .background(DangerRed, CircleShape)
                    .border(1.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun GradientActionButton(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier
            .height(if (subtitle == null) 62.dp else 76.dp)
            .shadow(14.dp, shape)
            .clip(shape)
            .background(if (enabled) Brush.verticalGradient(colors) else Brush.verticalGradient(listOf(Color(0xFF334565), Color(0xFF24334E))))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.28f else 0.12f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).background(Color.White.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = if (subtitle == null) 18.sp else 21.sp)
            if (subtitle != null) Text(subtitle, color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Color.White.copy(alpha = 0.78f))
    }
}

@Composable
private fun MenuTile(
    title: String,
    icon: ImageVector,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Boolean = false,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier
            .height(104.dp)
            .shadow(12.dp, shape)
            .clip(shape)
            .background(if (enabled) Brush.verticalGradient(colors) else Brush.verticalGradient(listOf(Color(0xFF3A4660), Color(0xFF29354D))))
            .border(1.dp, Color.White.copy(alpha = 0.25f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp)
    ) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(5.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        if (badge) Box(Modifier.align(Alignment.TopEnd).size(10.dp).background(DangerRed, CircleShape))
    }
}

@Composable
private fun ModernTopBar(
    coins: Int,
    stars: Int? = null,
    back: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
    onAchievements: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    achievementBadge: Boolean = false,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (back != null) RoundIconButton(Icons.Filled.ArrowBack, back) else HudPill(Icons.Filled.AutoAwesome, coins.toString(), Gold)
        if (back != null) HudPill(Icons.Filled.AutoAwesome, coins.toString(), Gold)
        Spacer(Modifier.weight(1f))
        if (stars != null) HudPill(Icons.Filled.Star, stars.toString(), Gold)
        if (onProfile != null) RoundIconButton(Icons.Filled.Person, onProfile)
        if (onAchievements != null) RoundIconButton(Icons.Filled.EmojiEvents, onAchievements, achievementBadge)
        if (onSettings != null) RoundIconButton(Icons.Filled.Settings, onSettings)
    }
}

@Composable
private fun ModernHomeScreen(
    coins: Int,
    completedCount: Int,
    totalStars: Int,
    unlockedLevel: Int,
    streak: Int,
    dailyAvailable: Boolean,
    availableChests: Int,
    claimableAchievements: Int,
    onContinue: () -> Unit,
    onChapters: () -> Unit,
    onDaily: () -> Unit,
    onChest: () -> Unit,
    onProfile: () -> Unit,
    onAchievements: () -> Unit,
    onSettings: () -> Unit,
) {
    ModernBackground {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ModernTopBar(
                coins = coins,
                stars = totalStars,
                onProfile = onProfile,
                onAchievements = onAchievements,
                onSettings = onSettings,
                achievementBadge = claimableAchievements > 0,
            )
            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text("Word", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Black)
                Text("Z", color = Gold, fontSize = 66.sp, fontWeight = FontWeight.Black)
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xAA0A2E6E))
                    .border(1.dp, Color(0x445FCBFF), RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text("KELİME BULMACA", color = SoftText, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, fontSize = 12.sp)
            }

            Spacer(Modifier.height(22.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Text("İlerleme Durumu", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(13.dp))
                Row(Modifier.fillMaxWidth()) {
                    HomeStat(Icons.Filled.AutoAwesome, coins.toString(), "Jeton", Gold, Modifier.weight(1f))
                    HomeStat(Icons.Filled.Star, totalStars.toString(), "Yıldız", Gold, Modifier.weight(1f))
                    HomeStat(Icons.Filled.Map, completedCount.toString(), "Level", SkyBlue, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                val progress = completedCount.toFloat() / levels.size.coerceAtLeast(1)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Emerald,
                    trackColor = Color.White.copy(alpha = 0.12f),
                )
            }

            Spacer(Modifier.height(18.dp))
            GradientActionButton(
                title = "Devam Et",
                subtitle = "Level ${unlockedLevel + 1} · Maceraya devam",
                icon = Icons.Filled.PlayArrow,
                colors = listOf(Color(0xFF60EA39), Emerald, EmeraldDark),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MenuTile("Bölümler", Icons.Filled.Map, listOf(Color(0xFFFFBE37), Color(0xFFD88712)), onChapters, Modifier.weight(1f))
                MenuTile(
                    if (dailyAvailable) "Günlük +40" else "Günlük Alındı",
                    Icons.Filled.CardGiftcard,
                    listOf(Color(0xFFA36AF7), Color(0xFF6730CE)),
                    onDaily,
                    Modifier.weight(1f),
                    badge = dailyAvailable,
                    enabled = dailyAvailable,
                )
                MenuTile(
                    if (availableChests > 0) "Sandık +75" else "Yıldız Sandığı",
                    Icons.Filled.Redeem,
                    listOf(Color(0xFF35A9FF), Color(0xFF1764D2)),
                    onChest,
                    Modifier.weight(1f),
                    badge = availableChests > 0,
                    enabled = availableChests > 0,
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0x73102F69)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF8B3D))
                    Spacer(Modifier.width(7.dp))
                    Column {
                        Text("Günlük Seri", color = Color.White, fontWeight = FontWeight.Black)
                        Text("$streak gün", color = SoftText, fontSize = 12.sp)
                    }
                }
                Text("v${BuildConfig.VERSION_NAME}", color = SoftText, fontSize = 11.sp)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun HomeStat(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(label, color = SoftText, fontSize = 11.sp)
    }
}

@Composable
private fun ModernProfileScreen(
    coins: Int,
    totalStars: Int,
    completed: Int,
    unlockedLevel: Int,
    words: Int,
    hints: Int,
    bestCombo: Int,
    streak: Int,
    onBack: () -> Unit,
) {
    ModernBackground {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ModernTopBar(coins = coins, stars = totalStars, back = onBack)
            Spacer(Modifier.height(24.dp))
            Box(Modifier.size(84.dp).background(Brush.radialGradient(listOf(SkyBlue, RoyalBlue)), CircleShape).border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("Oyuncu Profili", color = Color.White, fontWeight = FontWeight.Black, fontSize = 30.sp)
            Text("WordZ yolculuğun", color = SoftText)
            Spacer(Modifier.height(20.dp))

            val rows = listOf(
                Triple(Icons.Filled.Star, "Toplam yıldız", "$totalStars / ${levels.size * 3}"),
                Triple(Icons.Filled.CheckCircle, "Tamamlanan level", "$completed / ${levels.size}"),
                Triple(Icons.Filled.Map, "Açılan level", "${unlockedLevel + 1}"),
                Triple(Icons.Filled.AutoAwesome, "Doğru kelime", words.toString()),
                Triple(Icons.Filled.Lightbulb, "Kullanılan ipucu", hints.toString()),
                Triple(Icons.Filled.LocalFireDepartment, "En iyi combo", "x$bestCombo"),
                Triple(Icons.Filled.CardGiftcard, "Günlük seri", "$streak gün"),
            )
            rows.forEach { (icon, label, value) ->
                GlassCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = Gold, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(label, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(value, color = Gold, fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModernAchievementsScreen(
    stats: ModernStats,
    claimed: Set<String>,
    onClaim: (ModernAchievement) -> Unit,
    onBack: () -> Unit,
) {
    ModernBackground {
        Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ModernTopBar(coins = 0, back = onBack)
            Spacer(Modifier.height(18.dp))
            Icon(Icons.Filled.EmojiEvents, null, tint = Gold, modifier = Modifier.size(54.dp))
            Text("Görevler & Başarımlar", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Hedefleri tamamla, ödülleri kap", color = SoftText)
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                modernAchievements().forEach { achievement ->
                    val reached = achievement.reached(stats)
                    val isClaimed = achievement.id in claimed
                    GlassCard(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(52.dp).background(if (reached) Gold.copy(alpha = 0.17f) else Color.White.copy(alpha = 0.08f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(if (isClaimed) Icons.Filled.CheckCircle else Icons.Filled.EmojiEvents, null, tint = if (reached) Gold else SoftText)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(achievement.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Text(achievement.description, color = SoftText, fontSize = 12.sp)
                            }
                            Text("+${achievement.reward}", color = Gold, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(10.dp))
                        GradientActionButton(
                            title = when { isClaimed -> "Ödül Alındı"; reached -> "Ödülü Al"; else -> "Devam Et" },
                            icon = if (isClaimed) Icons.Filled.CheckCircle else Icons.Filled.Redeem,
                            colors = listOf(Emerald, EmeraldDark),
                            onClick = { onClaim(achievement) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = reached && !isClaimed,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ModernSettingsScreen(
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    onBack: () -> Unit,
) {
    ModernBackground {
        Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ModernTopBar(coins = 0, back = onBack)
            Spacer(Modifier.height(30.dp))
            Icon(Icons.Filled.Settings, null, tint = SkyBlue, modifier = Modifier.size(58.dp))
            Text("Ayarlar", color = Color.White, fontWeight = FontWeight.Black, fontSize = 32.sp)
            Text("Oyun deneyimini kendine göre ayarla", color = SoftText)
            Spacer(Modifier.height(24.dp))
            SettingCard(Icons.Filled.VolumeUp, "Ses Efektleri", soundEnabled, onToggleSound)
            Spacer(Modifier.height(12.dp))
            SettingCard(Icons.Filled.Vibration, "Titreşim", hapticEnabled, onToggleHaptic)
            Spacer(Modifier.height(24.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Text("WordZ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Sürüm ${BuildConfig.VERSION_NAME}", color = SoftText, fontSize = 12.sp)
                Text("Ayarların otomatik kaydedilir.", color = SoftText, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SettingCard(icon: ImageVector, title: String, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        Modifier.fillMaxWidth().shadow(9.dp, shape).clip(shape).background(CardBlue).border(1.dp, CardBorder, shape).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = SkyBlue)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Box(
            Modifier.width(64.dp).height(32.dp).clip(CircleShape).background(if (enabled) Emerald else Color(0xFF44516A)).clickable(onClick = onClick).padding(4.dp)
        ) {
            Box(Modifier.size(24.dp).align(if (enabled) Alignment.CenterEnd else Alignment.CenterStart).background(Color.White, CircleShape))
        }
    }
}

@Composable
private fun ModernChapterScreen(
    unlockedLevel: Int,
    stars: Map<Int, Int>,
    completedLevels: Set<Int>,
    claimedChapterChests: Set<String>,
    coins: Int,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit,
    onClaimChapterChest: (Int) -> Unit,
) {
    val chapterList = modernChapters()
    ModernBackground {
        Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ModernTopBar(coins = coins, stars = stars.values.sum(), back = onBack)
            Spacer(Modifier.height(18.dp))
            Text("Dünya Haritası", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black)
            Text("Bölümleri tamamla, yıldızları ve sandıkları topla", color = SoftText, textAlign = TextAlign.Center, fontSize = 13.sp)
            Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                chapterList.forEachIndexed { index, chapter ->
                    val unlocked = unlockedLevel >= chapter.start
                    val chapterLevels = chapter.start until chapter.endExclusive
                    val chapterStars = chapterLevels.sumOf { stars[it] ?: 0 }
                    val completed = chapterLevels.count { it in completedLevels }
                    val maxStars = chapterLevels.count() * 3
                    val chapterComplete = completed == chapterLevels.count()
                    val chestClaimed = index.toString() in claimedChapterChests
                    val accent = listOf(Color(0xFF38BDF8), Color(0xFF48C774), Color(0xFF8D66F4), Color(0xFFFFA13D), Color(0xFF4A6CFF), Color(0xFF18B6A0), Gold)[index % 7]

                    val shape = RoundedCornerShape(25.dp)
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 7.dp).shadow(12.dp, shape).clip(shape)
                            .background(Brush.horizontalGradient(listOf(Color(0xE9143777), Color(0xE90A2455))))
                            .border(1.dp, if (unlocked) accent.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.12f), shape)
                            .clickable(enabled = unlocked) { onSelect(index) }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(54.dp).background(if (unlocked) accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(if (unlocked) Icons.Filled.Map else Icons.Filled.Lock, null, tint = if (unlocked) accent else SoftText, modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("BÖLÜM ${index + 1}", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Text(chapter.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text(chapter.subtitle, color = SoftText, fontSize = 11.sp)
                            }
                            Text(if (unlocked) "★ $chapterStars/$maxStars" else "Kilitli", color = if (unlocked) Gold else SoftText, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { completed.toFloat() / chapterLevels.count().coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = accent,
                            trackColor = Color.White.copy(alpha = 0.1f),
                        )
                        Spacer(Modifier.height(7.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$completed/${chapterLevels.count()} level", color = SoftText, fontSize = 11.sp)
                            if (chapterComplete) {
                                Text(
                                    if (chestClaimed) "✓ Sandık alındı" else "🎁 Büyük sandık +150",
                                    color = if (chestClaimed) SoftText else Gold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.clickable(enabled = !chestClaimed) { onClaimChapterChest(index) },
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ModernZigzagLevelMap(
    chapterIndex: Int,
    unlockedLevel: Int,
    stars: Map<Int, Int>,
    completedLevels: Set<Int>,
    coins: Int,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    val chapter = modernChapters()[chapterIndex]
    val levelIndices = (chapter.start until chapter.endExclusive).toList()
    val chapterStars = levelIndices.sumOf { stars[it] ?: 0 }
    val progress = levelIndices.count { it in completedLevels }
    val pathFractions = listOf(0.18f, 0.5f, 0.82f, 0.5f, 0.18f)

    ModernBackground {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ModernTopBar(coins = coins, stars = chapterStars, back = onBack)
            Spacer(Modifier.height(14.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Text("Bölüm ${chapterIndex + 1}", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(chapter.name, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text(chapter.subtitle, color = SoftText, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("İlerleme $progress/${levelIndices.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("★ $chapterStars/${levelIndices.size * 3}", color = Gold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                Spacer(Modifier.height(7.dp))
                LinearProgressIndicator(
                    progress = { progress.toFloat() / levelIndices.size.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Emerald,
                    trackColor = Color.White.copy(alpha = 0.12f),
                )
            }
            Spacer(Modifier.height(12.dp))

            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                levelIndices.forEachIndexed { localIndex, globalIndex ->
                    val unlocked = globalIndex <= unlockedLevel
                    val earned = stars[globalIndex] ?: 0
                    val completed = globalIndex in completedLevels
                    val current = globalIndex == unlockedLevel && !completed
                    val fraction = pathFractions.getOrElse(localIndex) { 0.5f }
                    BoxWithConstraints(Modifier.fillMaxWidth().height(104.dp)) {
                        val nodeSize = 86.dp
                        val x = maxWidth * fraction - nodeSize / 2
                        ModernLevelNode(
                            label = "${chapterIndex + 1}-${localIndex + 1}",
                            stars = earned,
                            unlocked = unlocked,
                            completed = completed,
                            current = current,
                            onClick = { onSelect(globalIndex) },
                            modifier = Modifier.offset(x = x, y = 4.dp),
                        )
                    }
                    if (localIndex < levelIndices.lastIndex) {
                        ZigzagConnector(pathFractions[localIndex], pathFractions[localIndex + 1], completed)
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun ZigzagConnector(from: Float, to: Float, completed: Boolean) {
    Canvas(Modifier.fillMaxWidth().height(42.dp)) {
        drawLine(
            color = if (completed) SkyBlue else Color.White.copy(alpha = 0.25f),
            start = Offset(size.width * from, 0f),
            end = Offset(size.width * to, size.height),
            strokeWidth = if (completed) 8f else 6f,
            cap = StrokeCap.Round,
        )
        drawCircle(if (completed) Color.White else Color.White.copy(alpha = 0.35f), 5f, Offset(size.width * ((from + to) / 2f), size.height / 2f))
    }
}

@Composable
private fun ModernLevelNode(
    label: String,
    stars: Int,
    unlocked: Boolean,
    completed: Boolean,
    current: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("★".repeat(stars) + "☆".repeat(3 - stars), color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(2.dp))
        val nodeBrush = when {
            !unlocked -> Brush.verticalGradient(listOf(Color(0xFF56647D), Color(0xFF303C54)))
            current -> Brush.verticalGradient(listOf(Color(0xFF53C7FF), Color(0xFF087BEB)))
            completed -> Brush.verticalGradient(listOf(Color(0xFF31D987), Color(0xFF0A9D58)))
            else -> Brush.verticalGradient(listOf(Color(0xFF389FFF), Color(0xFF1467D1)))
        }
        Box(
            Modifier.size(78.dp).shadow(if (current) 20.dp else 10.dp, CircleShape).clip(CircleShape).background(nodeBrush)
                .border(if (current) 4.dp else 2.dp, if (current) Gold else Color.White.copy(alpha = 0.42f), CircleShape)
                .clickable(enabled = unlocked, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (unlocked) Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp) else Icon(Icons.Filled.Lock, null, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        if (current) Text("SIRADAKİ", color = Gold, fontWeight = FontWeight.Black, fontSize = 9.sp)
    }
}

@Composable
private fun ModernGameScreen(
    levelIndex: Int,
    coins: Int,
    previousStars: Int,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onCoinsChanged: (Int) -> Unit,
    onWordFound: () -> Unit,
    onHintUsed: () -> Unit,
    onComboChanged: (Int) -> Unit,
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
    var hintedCells by remember(levelIndex) { mutableStateOf(setOf<ModernCell>()) }
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

    val statusScale by animateFloatAsState(if (liveWord.isNotBlank()) 1.035f else 1f, tween(130), label = "modernStatus")

    ModernBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxHeight < 760.dp
            val wheelSize = if (compact) 208.dp else 238.dp
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(Icons.Filled.ArrowBack, onBack)
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LEVEL ${levelIndex + 1}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Text(level.title, color = SoftText, fontSize = 11.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    HudPill(Icons.Filled.AutoAwesome, coins.toString(), Gold)
                }

                Spacer(Modifier.height(if (compact) 5.dp else 8.dp))
                ModernCrosswordBoard(level, foundWords, hintedCells, compact)
                Spacer(Modifier.height(6.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${foundWords.size}/${level.targets.size} kelime", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (combo >= 2) {
                            Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF8B3D), modifier = Modifier.size(18.dp))
                            Text(" x$combo", color = Gold, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        } else {
                            Text("★".repeat(earnedStars) + "☆".repeat(3 - earnedStars), color = Gold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(Modifier.height(5.dp))
                Box(
                    Modifier.fillMaxWidth().scale(statusScale).clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xDD0F397D), Color(0xDD1555A9))))
                        .border(1.dp, Color(0x555FCBFF), RoundedCornerShape(18.dp)).padding(vertical = 9.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (liveWord.isNotBlank()) liveWord else status,
                        color = when {
                            liveWord.isNotBlank() -> Color.White
                            status.startsWith("Bonus") || status.startsWith("Combo") -> Gold
                            status.startsWith("Harika") -> Color(0xFF82F1B0)
                            else -> SoftText
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = if (liveWord.isNotBlank()) 21.sp else 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(if (compact) 1.dp else 4.dp))
                ModernLetterWheel(
                    letters = wheelLetters,
                    selectedIndices = selectedIndices,
                    wheelSize = wheelSize,
                    onSelectionChanged = { selection ->
                        selectedIndices = selection
                        liveWord = selection.joinToString("") { wheelLetters[it].toString() }
                    },
                    onSelectionFinished = {
                        val submitted = liveWord.trim().uppercase(modernTrLocale)
                        when {
                            submitted in level.targets && submitted !in foundWords -> {
                                foundWords = foundWords + submitted
                                combo += 1
                                onWordFound()
                                onComboChanged(combo)
                                val comboReward = if (combo % 3 == 0) 10 else 0
                                onCoinsChanged(coins + 5 + comboReward)
                                status = if (comboReward > 0) "Combo x$combo! +$comboReward jeton" else "Harika! $submitted"
                                if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                                if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            submitted in foundWords -> status = "$submitted zaten bulundu"
                            submitted in level.bonusWords && submitted !in foundBonusWords -> {
                                foundBonusWords = foundBonusWords + submitted
                                combo += 1
                                onWordFound()
                                onComboChanged(combo)
                                val comboReward = if (combo % 3 == 0) 10 else 0
                                onCoinsChanged(coins + 10 + comboReward)
                                status = if (comboReward > 0) "Combo x$combo! +${10 + comboReward} jeton" else "Bonus kelime! +10"
                                if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                            }
                            submitted in foundBonusWords -> status = "$submitted bonus olarak bulundu"
                            submitted.isNotBlank() -> {
                                combo = 0
                                status = "$submitted bu levelde yok"
                                if (soundEnabled) tone.startTone(ToneGenerator.TONE_PROP_NACK, 90)
                            }
                        }
                        selectedIndices = emptyList()
                        liveWord = ""
                    },
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    GameActionButton("Karıştır", Icons.Filled.Shuffle, Color(0xFF2587E9), true, Modifier.weight(1f)) {
                        wheelLetters = wheelLetters.shuffled()
                        selectedIndices = emptyList()
                        liveWord = ""
                        status = "Harfler karıştırıldı"
                    }
                    GameActionButton("Harf 25", Icons.Filled.Lightbulb, Color(0xFF38B65E), coins >= 25 && !complete, Modifier.weight(1f)) {
                        val hidden = modernAllCells(level).filter { it !in hintedCells && !modernCellRevealed(level, it, foundWords) }
                        hidden.randomOrNull()?.let {
                            onCoinsChanged(coins - 25)
                            hintedCells = hintedCells + it
                            hintsUsed += 1
                            combo = 0
                            onHintUsed()
                            status = "Bir harf açıldı · yıldız etkilenir"
                        }
                    }
                    GameActionButton("Kelime 60", Icons.Filled.Search, Purple, coins >= 60 && !complete, Modifier.weight(1f)) {
                        level.targets.firstOrNull { it !in foundWords }?.let {
                            onCoinsChanged(coins - 60)
                            foundWords = foundWords + it
                            hintsUsed += 2
                            combo = 0
                            onHintUsed()
                            status = "Kelime ipucu: $it"
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                AnimatedVisibility(complete) {
                    GlassCard(Modifier.fillMaxWidth()) {
                        Text("LEVEL TAMAMLANDI", color = Color(0xFF82F1B0), fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text("★".repeat(earnedStars) + "☆".repeat(3 - earnedStars), color = Gold, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Text(if (earnedStars > previousStars) "Yeni yıldız rekoru!" else "En iyi skor: $previousStars★", color = SoftText, fontSize = 11.sp)
                        Spacer(Modifier.height(7.dp))
                        GradientActionButton("Devam", icon = Icons.Filled.PlayArrow, colors = listOf(Emerald, EmeraldDark), onClick = onNext, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun GameActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.height(62.dp).clip(shape).background(if (enabled) color else Color(0xFF3D4960)).border(1.dp, Color.White.copy(alpha = 0.25f), shape)
            .clickable(enabled = enabled, onClick = onClick).padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(21.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
    }
}

private fun modernCellsFor(placement: Placement): List<Pair<ModernCell, Char>> = placement.word.mapIndexed { index, char ->
    val row = placement.row + if (placement.horizontal) 0 else index
    val col = placement.col + if (placement.horizontal) index else 0
    ModernCell(row, col) to char
}

private fun modernAllCells(level: Level): Set<ModernCell> = level.placements.flatMap(::modernCellsFor).map { it.first }.toSet()

private fun modernCellRevealed(level: Level, cell: ModernCell, foundWords: Set<String>): Boolean =
    level.placements.any { it.word in foundWords && modernCellsFor(it).any { pair -> pair.first == cell } }

@Composable
private fun ModernCrosswordBoard(
    level: Level,
    foundWords: Set<String>,
    hintedCells: Set<ModernCell>,
    compact: Boolean,
) {
    val entries = remember(level) { level.placements.flatMap(::modernCellsFor) }
    val lettersByCell = remember(level) { entries.associate { it.first to it.second } }
    val rows = (lettersByCell.keys.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (lettersByCell.keys.maxOfOrNull { it.col } ?: 0) + 1
    val cellSize = when {
        compact && cols >= 5 -> 34.dp
        cols >= 6 -> 36.dp
        cols == 5 -> 40.dp
        else -> 43.dp
    }

    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0x88103E8D), Color(0x99112656))))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp)).padding(if (compact) 7.dp else 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(cols) { col ->
                        val cell = ModernCell(row, col)
                        val letter = lettersByCell[cell]
                        if (letter == null) {
                            Spacer(Modifier.size(cellSize))
                        } else {
                            val revealed = cell in hintedCells || modernCellRevealed(level, cell, foundWords)
                            val shape = RoundedCornerShape(9.dp)
                            Box(
                                Modifier.size(cellSize).shadow(if (revealed) 5.dp else 0.dp, shape).clip(shape)
                                    .background(if (revealed) Color(0xFFF6FAFF) else Color(0xFF1476DA))
                                    .border(2.dp, if (revealed) Color(0xFFBFE4FF) else Color(0xFF57C8FF), shape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (revealed) Text(letter.toString(), color = Color(0xFF0D3474), fontWeight = FontWeight.Black, fontSize = if (compact) 16.sp else 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernLetterWheel(
    letters: List<Char>,
    selectedIndices: List<Int>,
    wheelSize: Dp,
    onSelectionChanged: (List<Int>) -> Unit,
    onSelectionFinished: () -> Unit,
) {
    var measured by remember { mutableStateOf(IntSize.Zero) }

    fun positions(): List<Offset> {
        if (measured == IntSize.Zero) return emptyList()
        val center = Offset(measured.width / 2f, measured.height / 2f)
        val radius = minOf(measured.width, measured.height) * 0.33f
        return letters.indices.map { index ->
            val angle = -PI / 2 + (2 * PI * index / letters.size)
            Offset(center.x + (cos(angle) * radius).toFloat(), center.y + (sin(angle) * radius).toFloat())
        }
    }

    fun indexAt(point: Offset): Int? {
        val points = positions()
        if (points.isEmpty()) return null
        val hitRadius = minOf(measured.width, measured.height) * 0.13f
        return points.indices.firstOrNull { i ->
            hypot((point.x - points[i].x).toDouble(), (point.y - points[i].y).toDouble()) <= hitRadius
        }
    }

    Box(
        Modifier.size(wheelSize).onSizeChanged { measured = it }.pointerInput(letters, measured) {
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
        }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) * 0.47f
            drawCircle(Color(0xFFF0F6FF), radius, center)
            drawCircle(Color(0xFFB7D5F0), radius, center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
            drawCircle(Color(0x2275BFFF), radius * 0.83f, center)

            val points = positions()
            selectedIndices.zipWithNext().forEach { (a, b) ->
                if (a in points.indices && b in points.indices) drawLine(SkyBlue, points[a], points[b], 17f, cap = StrokeCap.Round)
            }
            points.forEachIndexed { index, point ->
                val selected = index in selectedIndices
                drawCircle(if (selected) Color(0xFF118BFF) else Color.White, minOf(size.width, size.height) * 0.108f, point)
                drawCircle(if (selected) Color(0xFF78D5FF) else Color(0xFFB8D2EA), minOf(size.width, size.height) * 0.108f, point, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = if (selected) android.graphics.Color.WHITE else android.graphics.Color.rgb(17, 49, 96)
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = minOf(size.width, size.height) * 0.105f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val metrics = paint.fontMetrics
                    canvas.nativeCanvas.drawText(letters[index].toString(), point.x, point.y - (metrics.ascent + metrics.descent) / 2f, paint)
                }
            }
        }
    }
}
