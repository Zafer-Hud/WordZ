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

private object W17Colors {
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

private object W17Economy {
    const val WORD = 3
    const val BONUS = 6
    const val COMBO = 8
    const val LEVEL = 20
    const val CHAPTER = 120
    const val LETTER_HINT = 30
    const val WORD_HINT = 75
}

private const val W17_PREFS = "wordz_progress"
private const val W17_UNLOCKED = "unlocked_level"
private const val W17_COINS = "coins"
private const val W17_COMPLETED = "completed_levels"
private const val W17_STARS = "level_stars"
private const val W17_SOUND = "sound_enabled"
private const val W17_MUSIC = "music_enabled"
private const val W17_HAPTIC = "haptic_enabled"
private const val W17_MOTION = "reduce_motion"
private const val W17_CHESTS = "claimed_chapter_chests"
private const val W17_WORDS = "stats_words"
private const val W17_HINTS = "stats_hints"
private const val W17_COMBO = "stats_best_combo"
private val w17Tr = Locale("tr", "TR")

private data class W17Cell(val row: Int, val col: Int) {
    fun encode(): String = "$row,$col"

    companion object {
        fun decode(raw: String): W17Cell? {
            val parts = raw.split(',')
            if (parts.size != 2) return null
            val row = parts[0].toIntOrNull() ?: return null
            val col = parts[1].toIntOrNull() ?: return null
            return W17Cell(row, col)
        }
    }
}

private data class W17Chapter(
    val name: String,
    val subtitle: String,
    val start: Int,
    val endExclusive: Int,
    val accent: Color,
    val soft: Color,
)

private data class W17Stats(
    val completed: Int,
    val stars: Int,
    val words: Int,
    val hints: Int,
    val combo: Int,
    val streak: Int,
)

private data class W17Task(
    val id: String,
    val title: String,
    val subtitle: String,
    val goal: Int,
    val reward: Int,
    val current: (ChallengeProgress) -> Int,
)

private enum class W17Screen { HOME, CHAPTERS, MAP, GAME, DAILY, TASKS, PROFILE, SETTINGS }
private enum class W17Feedback { NORMAL, CORRECT, BONUS, WRONG }

class V17MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validateAllLevels()
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = W17Colors.Blue,
                    secondary = W17Colors.Gold,
                    background = W17Colors.Bg,
                    surface = W17Colors.Surface,
                    onPrimary = Color.White,
                    onBackground = W17Colors.Ink,
                    onSurface = W17Colors.Ink,
                )
            ) {
                W17App()
            }
        }
    }
}

private fun w17Chapters(): List<W17Chapter> {
    val names = listOf("Başlangıç", "Sahil", "Orman", "Çöl", "Gece", "Uzak Diyarlar", "Zirve")
    val subtitles = listOf(
        "Temelleri keşfet",
        "Ritmi yakala",
        "Yeni yollar aç",
        "Zorlu kelimelere geç",
        "Gece rotasını tamamla",
        "Uzak diyarlara ilerle",
        "Son zirve",
    )
    val accents = listOf(
        Color(0xFF3976F6), Color(0xFF0E9FB5), Color(0xFF2A9D63),
        Color(0xFFD98A25), Color(0xFF6A62D8), Color(0xFF248A89), Color(0xFF5A57C8),
    )
    val softs = listOf(
        Color(0xFFEEF4FF), Color(0xFFEAF8FA), Color(0xFFECF8F1),
        Color(0xFFFFF4E6), Color(0xFFF1F0FF), Color(0xFFECF8F7), Color(0xFFF0F0FF),
    )
    return levels.indices.chunked(5).mapIndexed { index, chunk ->
        W17Chapter(
            name = names.getOrElse(index) { "Bölüm ${index + 1}" },
            subtitle = subtitles.getOrElse(index) { "Yeni kelimeler" },
            start = chunk.first(),
            endExclusive = chunk.last() + 1,
            accent = accents[index % accents.size],
            soft = softs[index % softs.size],
        )
    }
}

private fun w17DecodeStars(raw: Set<String>): Map<Int, Int> {
    return raw.mapNotNull { value ->
        val parts = value.split(':')
        val level = parts.getOrNull(0)?.toIntOrNull()
        val stars = parts.getOrNull(1)?.toIntOrNull()
        if (level != null && stars != null && level in levels.indices) level to stars.coerceIn(0, 3) else null
    }.toMap()
}

private fun w17EncodeStars(stars: Map<Int, Int>): Set<String> = stars.map { "${it.key}:${it.value}" }.toSet()

private fun w17DailyTasks() = listOf(
    W17Task("d_words", "8 kelime bul", "Bugün kelime zincirini büyüt", 8, 25, { it.words }),
    W17Task("d_levels", "2 level tamamla", "İki bulmacayı bitir", 2, 35, { it.levels }),
    W17Task("d_clean", "İpucusuz 1 level", "Bir leveli yardım almadan bitir", 1, 30, { it.noHintLevels }),
)

private fun w17WeeklyTasks() = listOf(
    W17Task("w_words", "50 kelime bul", "Hafta boyunca 50 kelime", 50, 100, { it.words }),
    W17Task("w_levels", "10 level tamamla", "Toplam 10 oyun bitir", 10, 120, { it.levels }),
    W17Task("w_stars", "18 yıldız kazan", "Hafta boyunca 18 yıldız", 18, 120, { it.stars }),
)

@Composable
private fun W17App() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(W17_PREFS, Context.MODE_PRIVATE) }
    val gameStore = remember { GameProgressStore(prefs) }
    val metaStore = remember { MetaProgressStore(prefs) }
    val playerStore = remember { PlayerProgressStore(prefs) }
    val audio = remember { WordZAudio(context.applicationContext) }
    val today = LocalDate.now()
    metaStore.ensurePeriods(today)

    val initialUnlocked = prefs.getInt(W17_UNLOCKED, 0).coerceIn(0, levels.lastIndex)
    val savedCompleted = prefs.getStringSet(W17_COMPLETED, emptySet()).orEmpty()
        .mapNotNull { it.toIntOrNull() }
        .filter { it in levels.indices }
        .toSet()
    val initialCompleted = if (savedCompleted.isEmpty() && initialUnlocked > 0) {
        (0 until initialUnlocked).toSet()
    } else {
        savedCompleted
    }
    val initialStars = w17DecodeStars(prefs.getStringSet(W17_STARS, emptySet()).orEmpty())
        .toMutableMap()
        .apply { initialCompleted.forEach { if (it !in this) this[it] = 1 } }
        .toMap()

    var unlocked by remember { mutableIntStateOf(initialUnlocked) }
    var coins by remember { mutableIntStateOf(prefs.getInt(W17_COINS, 100)) }
    var completed by remember { mutableStateOf(initialCompleted) }
    var stars by remember { mutableStateOf(initialStars) }
    var sound by remember { mutableStateOf(prefs.getBoolean(W17_SOUND, true)) }
    var music by remember { mutableStateOf(prefs.getBoolean(W17_MUSIC, false)) }
    var haptic by remember { mutableStateOf(prefs.getBoolean(W17_HAPTIC, true)) }
    var reduceMotion by remember { mutableStateOf(prefs.getBoolean(W17_MOTION, false)) }
    var chapterChests by remember { mutableStateOf(prefs.getStringSet(W17_CHESTS, emptySet()).orEmpty().toSet()) }
    var totalWords by remember { mutableIntStateOf(prefs.getInt(W17_WORDS, 0)) }
    var totalHints by remember { mutableIntStateOf(prefs.getInt(W17_HINTS, 0)) }
    var bestCombo by remember { mutableIntStateOf(prefs.getInt(W17_COMBO, 0)) }
    var player by remember { mutableStateOf(playerStore.snapshot()) }
    var pendingLevelUp by remember { mutableStateOf<Int?>(null) }

    var dailyProgress by remember { mutableStateOf(metaStore.daily(today)) }
    var weeklyProgress by remember { mutableStateOf(metaStore.weekly(today)) }
    var dailyClaims by remember { mutableStateOf(metaStore.dailyClaims(today)) }
    var weeklyClaims by remember { mutableStateOf(metaStore.weeklyClaims(today)) }
    var rewardState by remember { mutableStateOf(metaStore.rewardState(today)) }

    var screen by remember { mutableStateOf(W17Screen.HOME) }
    var selectedChapter by remember { mutableIntStateOf((unlocked / 5).coerceIn(0, w17Chapters().lastIndex)) }
    var selectedLevel by remember { mutableIntStateOf(unlocked) }
    var gameSession by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose { audio.release() }
    }
    LaunchedEffect(sound) { audio.setEffectsEnabled(sound) }
    LaunchedEffect(music) { audio.setMusicEnabled(music) }

    fun saveCore() {
        prefs.edit()
            .putInt(W17_UNLOCKED, unlocked)
            .putInt(W17_COINS, coins)
            .putStringSet(W17_COMPLETED, completed.map(Int::toString).toSet())
            .putStringSet(W17_STARS, w17EncodeStars(stars))
            .putBoolean(W17_SOUND, sound)
            .putBoolean(W17_MUSIC, music)
            .putBoolean(W17_HAPTIC, haptic)
            .putBoolean(W17_MOTION, reduceMotion)
            .putStringSet(W17_CHESTS, chapterChests)
            .putInt(W17_WORDS, totalWords)
            .putInt(W17_HINTS, totalHints)
            .putInt(W17_COMBO, bestCombo)
            .apply()
    }

    fun refreshMeta() {
        dailyProgress = metaStore.daily(today)
        weeklyProgress = metaStore.weekly(today)
        dailyClaims = metaStore.dailyClaims(today)
        weeklyClaims = metaStore.weeklyClaims(today)
        rewardState = metaStore.rewardState(today)
    }

    fun grantXp(amount: Int) {
        val result = playerStore.grant(amount)
        player = result.after
        if (result.levelsGained > 0) {
            coins += result.levelsGained * PlayerProgressStore.LEVEL_UP_COIN_REWARD
            pendingLevelUp = result.after.level
            audio.play(WordZAudio.Fx.LEVEL_UP)
            saveCore()
        }
    }

    fun openLevel(index: Int) {
        selectedLevel = index
        selectedChapter = (index / 5).coerceIn(0, w17Chapters().lastIndex)
        gameSession += 1
        screen = W17Screen.GAME
    }

    val totalStars = stars.values.sum()
    val stats = W17Stats(completed.size, totalStars, totalWords, totalHints, bestCombo, rewardState.streak)
    val taskBadge = w17DailyTasks().any { it.current(dailyProgress) >= it.goal && it.id !in dailyClaims } ||
        w17WeeklyTasks().any { it.current(weeklyProgress) >= it.goal && it.id !in weeklyClaims }

    Box(Modifier.fillMaxSize()) {
        when (screen) {
            W17Screen.HOME -> W17Home(
                coins = coins,
                stars = totalStars,
                unlocked = unlocked,
                completed = completed.size,
                reward = rewardState,
                player = player,
                taskBadge = taskBadge,
                onContinue = { openLevel(unlocked) },
                onChapters = { screen = W17Screen.CHAPTERS },
                onDaily = { screen = W17Screen.DAILY },
                onTasks = { screen = W17Screen.TASKS },
                onProfile = { screen = W17Screen.PROFILE },
                onSettings = { screen = W17Screen.SETTINGS },
            )

            W17Screen.CHAPTERS -> W17Chapters(
                unlocked = unlocked,
                stars = stars,
                completed = completed,
                back = { screen = W17Screen.HOME },
                open = { selectedChapter = it; screen = W17Screen.MAP },
                home = { screen = W17Screen.HOME },
                tasks = { screen = W17Screen.TASKS },
                profile = { screen = W17Screen.PROFILE },
            )

            W17Screen.MAP -> W17Map(
                chapterIndex = selectedChapter,
                unlocked = unlocked,
                stars = stars,
                completed = completed,
                reduceMotion = reduceMotion,
                back = { screen = W17Screen.CHAPTERS },
                open = { openLevel(it) },
            )

            W17Screen.GAME -> key(gameSession) {
                W17Game(
                    levelIndex = selectedLevel,
                    coins = coins,
                    previousStars = stars[selectedLevel] ?: 0,
                    completedBefore = selectedLevel in completed,
                    hapticEnabled = haptic,
                    reduceMotion = reduceMotion,
                    progressStore = gameStore,
                    audio = audio,
                    onCoins = { value -> coins = value; saveCore() },
                    onWord = {
                        totalWords += 1
                        metaStore.recordWord(today)
                        grantXp(PlayerProgressStore.WORD_XP)
                        refreshMeta()
                        saveCore()
                    },
                    onHint = {
                        totalHints += 1
                        saveCore()
                    },
                    onCombo = { value ->
                        if (value > bestCombo) {
                            bestCombo = value
                            saveCore()
                        }
                    },
                    onComplete = { earned, hintsUsed ->
                        val firstCompletion = selectedLevel !in completed
                        if (firstCompletion) {
                            metaStore.recordLevel(earned, hintsUsed == 0, today)
                            grantXp(
                                PlayerProgressStore.LEVEL_XP +
                                    if (hintsUsed == 0) PlayerProgressStore.NO_HINT_BONUS_XP else 0
                            )
                        }
                        completed = completed + selectedLevel
                        val oldStars = stars[selectedLevel] ?: 0
                        if (earned > oldStars) stars = stars + (selectedLevel to earned)
                        if (selectedLevel >= unlocked && unlocked < levels.lastIndex) unlocked = selectedLevel + 1
                        refreshMeta()
                        saveCore()
                    },
                    back = { screen = W17Screen.MAP },
                    replay = {
                        gameStore.clear(selectedLevel)
                        gameSession += 1
                    },
                    map = { screen = W17Screen.MAP },
                    next = {
                        val chapter = w17Chapters()[selectedChapter]
                        if (selectedLevel + 1 < chapter.endExclusive && selectedLevel < levels.lastIndex) {
                            openLevel(selectedLevel + 1)
                        } else {
                            screen = W17Screen.MAP
                        }
                    },
                    claimChapter = {
                        val chapter = w17Chapters()[selectedChapter]
                        val key = selectedChapter.toString()
                        val allDone = (chapter.start until chapter.endExclusive).all { it in completed || it == selectedLevel }
                        if (allDone && key !in chapterChests) {
                            chapterChests = chapterChests + key
                            coins += W17Economy.CHAPTER
                            audio.play(WordZAudio.Fx.REWARD)
                            saveCore()
                        }
                    },
                    chestClaimed = selectedChapter.toString() in chapterChests,
                )
            }

            W17Screen.DAILY -> W17Daily(
                state = rewardState,
                back = { screen = W17Screen.HOME },
                claim = {
                    val claimed = metaStore.claimDailyReward(today)
                    if (claimed != null) {
                        coins += claimed.rewardToday
                        rewardState = claimed
                        grantXp(PlayerProgressStore.DAILY_REWARD_XP)
                        audio.play(WordZAudio.Fx.REWARD)
                        saveCore()
                        refreshMeta()
                    }
                },
            )

            W17Screen.TASKS -> W17Tasks(
                daily = dailyProgress,
                weekly = weeklyProgress,
                dailyClaims = dailyClaims,
                weeklyClaims = weeklyClaims,
                back = { screen = W17Screen.HOME },
                claim = { task, weekly ->
                    val progress = if (weekly) weeklyProgress else dailyProgress
                    if (task.current(progress) >= task.goal) {
                        val claimed = if (weekly) metaStore.claimWeekly(task.id, today) else metaStore.claimDaily(task.id, today)
                        if (claimed) {
                            coins += task.reward
                            grantXp(if (weekly) PlayerProgressStore.WEEKLY_TASK_XP else PlayerProgressStore.DAILY_TASK_XP)
                            audio.play(WordZAudio.Fx.REWARD)
                            saveCore()
                            refreshMeta()
                        }
                    }
                },
                home = { screen = W17Screen.HOME },
                chapters = { screen = W17Screen.CHAPTERS },
                profile = { screen = W17Screen.PROFILE },
            )

            W17Screen.PROFILE -> W17Profile(
                coins = coins,
                stats = stats,
                player = player,
                back = { screen = W17Screen.HOME },
                home = { screen = W17Screen.HOME },
                chapters = { screen = W17Screen.CHAPTERS },
                tasks = { screen = W17Screen.TASKS },
                settings = { screen = W17Screen.SETTINGS },
            )

            W17Screen.SETTINGS -> W17Settings(
                sound = sound,
                music = music,
                haptic = haptic,
                reduceMotion = reduceMotion,
                back = { screen = W17Screen.PROFILE },
                onSound = { sound = it; saveCore() },
                onMusic = { music = it; saveCore() },
                onHaptic = { haptic = it; saveCore() },
                onMotion = { reduceMotion = it; saveCore() },
            )
        }

        pendingLevelUp?.let { level ->
            W17LevelUpOverlay(level, reduceMotion) { pendingLevelUp = null }
        }
    }
}

@Composable
private fun W17Surface(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(W17Colors.Bg).statusBarsPadding()) { content() }
}

@Composable
private fun W17TopBar(title: String? = null, back: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) {
            W17IconButton(Icons.Filled.ArrowBack, back)
            Spacer(Modifier.width(12.dp))
        }
        if (title != null) {
            Text(title, color = W17Colors.Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
        }
        trailing?.invoke()
    }
}

@Composable
private fun W17IconButton(icon: ImageVector, click: () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(CircleShape).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, CircleShape).clickable(onClick = click),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = W17Colors.Ink, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun W17Chip(icon: ImageVector, text: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(14.dp)).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, color = W17Colors.Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun W17Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x08000000), spotColor = Color(0x08000000))
            .clip(RoundedCornerShape(20.dp)).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(20.dp)).padding(18.dp),
        content = content,
    )
}

@Composable
private fun W17Primary(text: String, click: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    Row(
        modifier.height(54.dp).clip(RoundedCornerShape(16.dp))
            .background(if (enabled) W17Colors.Blue else Color(0xFFD5D9E2))
            .clickable(enabled = enabled, onClick = click),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun W17Secondary(text: String, click: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.height(46.dp).clip(RoundedCornerShape(14.dp)).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(14.dp)).clickable(onClick = click),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = W17Colors.Blue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(text, color = W17Colors.Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun W17Bottom(active: W17Screen, home: () -> Unit, chapters: () -> Unit, tasks: () -> Unit, profile: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().height(70.dp).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        W17Nav("Ana Sayfa", Icons.Filled.Home, active == W17Screen.HOME, home)
        W17Nav("Bölümler", Icons.Filled.Map, active == W17Screen.CHAPTERS, chapters)
        W17Nav("Görevler", Icons.Filled.TaskAlt, active == W17Screen.TASKS, tasks)
        W17Nav("Profil", Icons.Filled.Person, active == W17Screen.PROFILE, profile)
    }
}

@Composable
private fun W17Nav(label: String, icon: ImageVector, active: Boolean, click: () -> Unit) {
    Column(Modifier.clickable(onClick = click).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (active) W17Colors.Blue else W17Colors.Subtle, modifier = Modifier.size(20.dp))
        Text(
            label,
            color = if (active) W17Colors.Blue else W17Colors.Subtle,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun W17Home(
    coins: Int,
    stars: Int,
    unlocked: Int,
    completed: Int,
    reward: DailyRewardState,
    player: PlayerProgress,
    taskBadge: Boolean,
    onContinue: () -> Unit,
    onChapters: () -> Unit,
    onDaily: () -> Unit,
    onTasks: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
) {
    val chapter = w17Chapters()[(unlocked / 5).coerceIn(0, w17Chapters().lastIndex)]
    W17Surface {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp).padding(bottom = 88.dp)
            ) {
                Row(Modifier.fillMaxWidth().height(66.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("WordZ", color = W17Colors.Ink, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text("Kelime bulmaca", color = W17Colors.Muted, fontSize = 12.sp)
                    }
                    W17Chip(Icons.Filled.AutoAwesome, coins.toString(), W17Colors.Gold)
                    Spacer(Modifier.width(8.dp))
                    W17Chip(Icons.Filled.Star, stars.toString(), W17Colors.Gold)
                    Spacer(Modifier.width(8.dp))
                    W17IconButton(Icons.Filled.Settings, onSettings)
                }

                Spacer(Modifier.height(8.dp))
                W17Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).background(W17Colors.PurpleSoft, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = W17Colors.Purple)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Oyuncu seviyesi ${player.level}", color = W17Colors.Ink, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { player.currentLevelXp.toFloat() / player.nextLevelXp.coerceAtLeast(1) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = W17Colors.Purple,
                                trackColor = W17Colors.PurpleSoft,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("${player.currentLevelXp}/${player.nextLevelXp} XP", color = W17Colors.Muted, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.fillMaxWidth().height(198.dp).clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(chapter.accent, chapter.accent.copy(alpha = .82f))))
                        .padding(22.dp)
                ) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("DEVAM ET", color = Color.White.copy(alpha = .72f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(chapter.name, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                            Text("Level ${unlocked + 1} · ${levels[unlocked].title}", color = Color.White.copy(alpha = .86f), fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                LinearProgressIndicator(
                                    progress = { completed.toFloat() / levels.size.coerceAtLeast(1) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = .24f),
                                )
                                Spacer(Modifier.height(6.dp))
                                Text("$completed / ${levels.size} tamamlandı", color = Color.White.copy(alpha = .75f), fontSize = 11.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Box(
                                Modifier.size(50.dp).background(Color.White, CircleShape).clickable(onClick = onContinue),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.PlayArrow, null, tint = chapter.accent, modifier = Modifier.size(27.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    W17Quick(
                        title = "7 günlük ödül",
                        subtitle = if (reward.claimedToday) "Bugün alındı" else "+${reward.rewardToday} hazır",
                        icon = Icons.Filled.CardGiftcard,
                        bg = W17Colors.GoldSoft,
                        tint = W17Colors.Gold,
                        badge = !reward.claimedToday,
                        click = onDaily,
                        modifier = Modifier.weight(1f),
                    )
                    W17Quick(
                        title = "Görevler",
                        subtitle = if (taskBadge) "Ödül hazır" else "Günlük + haftalık",
                        icon = Icons.Filled.TaskAlt,
                        bg = W17Colors.BlueSoft,
                        tint = W17Colors.Blue,
                        badge = taskBadge,
                        click = onTasks,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                W17Bottom(W17Screen.HOME, {}, onChapters, onTasks, onProfile)
            }
        }
    }
}

@Composable
private fun W17Quick(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bg: Color,
    tint: Color,
    badge: Boolean,
    click: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.height(112.dp).clip(RoundedCornerShape(18.dp)).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(18.dp)).clickable(onClick = click).padding(15.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(36.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
            if (badge) Box(Modifier.size(8.dp).background(W17Colors.Danger, CircleShape))
        }
        Column {
            Text(title, color = W17Colors.Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = W17Colors.Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun W17Chapters(
    unlocked: Int,
    stars: Map<Int, Int>,
    completed: Set<Int>,
    back: () -> Unit,
    open: (Int) -> Unit,
    home: () -> Unit,
    tasks: () -> Unit,
    profile: () -> Unit,
) {
    W17Surface {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 86.dp)) {
                W17TopBar("Bölümler", back)
                Text("Her bölüm 5 level", color = W17Colors.Muted, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    w17Chapters().forEachIndexed { index, chapter ->
                        val range = chapter.start until chapter.endExclusive
                        W17ChapterCard(
                            index = index,
                            chapter = chapter,
                            done = range.count { it in completed },
                            total = range.count(),
                            stars = range.sumOf { stars[it] ?: 0 },
                            unlocked = unlocked >= chapter.start,
                            click = { open(index) },
                        )
                        Spacer(Modifier.height(11.dp))
                    }
                }
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                W17Bottom(W17Screen.CHAPTERS, home, {}, tasks, profile)
            }
        }
    }
}

@Composable
private fun W17ChapterCard(
    index: Int,
    chapter: W17Chapter,
    done: Int,
    total: Int,
    stars: Int,
    unlocked: Boolean,
    click: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(102.dp).clip(RoundedCornerShape(18.dp)).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(18.dp))
            .clickable(enabled = unlocked, onClick = click).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).background(if (unlocked) chapter.soft else Color(0xFFF1F3F6), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (unlocked) {
                Text("${index + 1}", color = chapter.accent, fontWeight = FontWeight.Black, fontSize = 18.sp)
            } else {
                Icon(Icons.Filled.Lock, null, tint = W17Colors.Subtle)
            }
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(chapter.name, color = if (unlocked) W17Colors.Ink else W17Colors.Subtle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(chapter.subtitle, color = W17Colors.Muted, fontSize = 11.sp)
            Spacer(Modifier.height(7.dp))
            LinearProgressIndicator(
                progress = { done.toFloat() / total.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = if (unlocked) chapter.accent else Color(0xFFD5D9E2),
                trackColor = Color(0xFFF0F2F5),
            )
            Spacer(Modifier.height(4.dp))
            Text(if (unlocked) "$done/$total · $stars/${total * 3} yıldız" else "Kilitli", color = W17Colors.Muted, fontSize = 10.sp)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = W17Colors.Subtle)
    }
}

@Composable
private fun W17Map(
    chapterIndex: Int,
    unlocked: Int,
    stars: Map<Int, Int>,
    completed: Set<Int>,
    reduceMotion: Boolean,
    back: () -> Unit,
    open: (Int) -> Unit,
) {
    val chapter = w17Chapters()[chapterIndex]
    val ids = (chapter.start until chapter.endExclusive).toList()
    val nodes = listOf(
        .18f to .82f,
        .43f to .65f,
        .73f to .48f,
        .47f to .31f,
        .22f to .14f,
    )

    W17Surface {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            W17TopBar("${chapterIndex + 1}. ${chapter.name}", back)
            Row {
                Text("${ids.count { it in completed }}/${ids.size} tamamlandı", color = W17Colors.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("${ids.sumOf { stars[it] ?: 0 }} yıldız", color = W17Colors.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(14.dp))

            BoxWithConstraints(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(listOf(chapter.soft, W17Colors.Surface)))
                    .border(1.dp, W17Colors.Border, RoundedCornerShape(26.dp))
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    fun segment(a: Pair<Float, Float>, b: Pair<Float, Float>): Path {
                        val x1 = size.width * a.first
                        val y1 = size.height * a.second
                        val x2 = size.width * b.first
                        val y2 = size.height * b.second
                        val mid = (y1 + y2) / 2f
                        return Path().apply {
                            moveTo(x1, y1)
                            cubicTo(x1, mid, x2, mid, x2, y2)
                        }
                    }

                    for (index in 0 until nodes.lastIndex) {
                        drawPath(
                            path = segment(nodes[index], nodes[index + 1]),
                            color = Color(0xFFD9DEE7),
                            style = Stroke(width = 9f, cap = StrokeCap.Round),
                        )
                    }
                    for (index in 0 until nodes.lastIndex) {
                        if (ids[index] in completed) {
                            drawPath(
                                path = segment(nodes[index], nodes[index + 1]),
                                color = chapter.accent.copy(alpha = .75f),
                                style = Stroke(width = 9f, cap = StrokeCap.Round),
                            )
                        }
                    }
                }

                nodes.forEachIndexed { index, pair ->
                    val global = ids[index]
                    val nodeSize = 64.dp
                    Column(
                        Modifier.offset(
                            x = maxWidth * pair.first - nodeSize / 2,
                            y = maxHeight * pair.second - nodeSize / 2,
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        W17LevelNode(
                            number = index + 1,
                            unlocked = global <= unlocked,
                            completed = global in completed,
                            current = global == unlocked && global !in completed,
                            accent = chapter.accent,
                            reduceMotion = reduceMotion,
                            click = { open(global) },
                        )
                        Spacer(Modifier.height(4.dp))
                        Row {
                            repeat(3) { star ->
                                Icon(
                                    if (star < (stars[global] ?: 0)) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    null,
                                    tint = if (star < (stars[global] ?: 0)) W17Colors.Gold else Color(0xFFCDD2DA),
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun W17LevelNode(
    number: Int,
    unlocked: Boolean,
    completed: Boolean,
    current: Boolean,
    accent: Color,
    reduceMotion: Boolean,
    click: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "node")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "pulse",
    )
    val background = when {
        !unlocked -> Color(0xFFF1F3F6)
        completed -> W17Colors.Green
        else -> accent
    }
    Box(
        Modifier.size(62.dp)
            .scale(if (current && !reduceMotion) pulse else 1f)
            .shadow(if (current) 10.dp else 3.dp, CircleShape)
            .background(background, CircleShape)
            .border(if (current) 3.dp else 1.dp, Color.White, CircleShape)
            .clickable(enabled = unlocked, onClick = click),
        contentAlignment = Alignment.Center,
    ) {
        when {
            !unlocked -> Icon(Icons.Filled.Lock, null, tint = W17Colors.Subtle)
            completed -> Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(26.dp))
            else -> Text(number.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp)
        }
    }
}

@Composable
private fun W17Game(
    levelIndex: Int,
    coins: Int,
    previousStars: Int,
    completedBefore: Boolean,
    hapticEnabled: Boolean,
    reduceMotion: Boolean,
    progressStore: GameProgressStore,
    audio: WordZAudio,
    onCoins: (Int) -> Unit,
    onWord: () -> Unit,
    onHint: () -> Unit,
    onCombo: (Int) -> Unit,
    onComplete: (Int, Int) -> Unit,
    back: () -> Unit,
    replay: () -> Unit,
    map: () -> Unit,
    next: () -> Unit,
    claimChapter: () -> Unit,
    chestClaimed: Boolean,
) {
    val level = levels[levelIndex]
    val haptics = LocalHapticFeedback.current
    val saved = remember(levelIndex) {
        if (completedBefore) SavedLevelProgress() else progressStore.load(levelIndex)
    }

    var localCoins by remember(levelIndex) { mutableIntStateOf(coins) }
    var wheel by remember(levelIndex) { mutableStateOf(level.letters) }
    var found by remember(levelIndex) { mutableStateOf(saved.foundWords.intersect(level.targets.toSet())) }
    var bonus by remember(levelIndex) { mutableStateOf(saved.bonusWords.intersect(level.bonusWords)) }
    var hinted by remember(levelIndex) { mutableStateOf(saved.hintedCells.mapNotNull(W17Cell::decode).toSet()) }
    var hintsUsed by remember(levelIndex) { mutableIntStateOf(saved.hintsUsed) }
    var selected by remember(levelIndex) { mutableStateOf(emptyList<Int>()) }
    var live by remember(levelIndex) { mutableStateOf("") }
    var status by remember(levelIndex) { mutableStateOf(if (found.isNotEmpty()) "Kaldığın yerden devam" else "Harfleri birleştir") }
    var combo by remember(levelIndex) { mutableIntStateOf(0) }
    var handled by remember(levelIndex) { mutableStateOf(false) }
    var showResult by remember(levelIndex) { mutableStateOf(false) }
    var net by remember(levelIndex) { mutableIntStateOf(0) }
    var feedback by remember(levelIndex) { mutableStateOf(W17Feedback.NORMAL) }
    var gainToast by remember { mutableStateOf<String?>(null) }
    var wrongTick by remember { mutableIntStateOf(0) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(coins) { localCoins = coins }
    LaunchedEffect(gainToast) {
        if (gainToast != null) {
            delay(700)
            gainToast = null
        }
    }
    LaunchedEffect(wrongTick) {
        if (wrongTick > 0 && !reduceMotion) {
            shake.snapTo(-5f)
            shake.animateTo(5f, tween(55))
            shake.animateTo(-3f, tween(55))
            shake.animateTo(0f, tween(55))
        }
    }

    val rewardEligible = !completedBefore
    val done = level.targets.all { it in found }
    val earnedStars = when (hintsUsed) {
        0 -> 3
        1 -> 2
        2 -> 1
        else -> 0
    }
    val chapter = w17Chapters()[levelIndex / 5]
    val chapterEnd = levelIndex == chapter.endExclusive - 1

    fun persist() {
        if (!completedBefore) {
            progressStore.save(
                levelIndex,
                SavedLevelProgress(
                    foundWords = found,
                    bonusWords = bonus,
                    hintedCells = hinted.map(W17Cell::encode).toSet(),
                    hintsUsed = hintsUsed,
                )
            )
        }
    }

    fun changeCoins(delta: Int) {
        localCoins += delta
        net += delta
        onCoins(localCoins)
        gainToast = if (delta >= 0) "+$delta jeton" else "$delta jeton"
    }

    LaunchedEffect(done) {
        if (done && !handled) {
            handled = true
            persist()
            if (rewardEligible) changeCoins(W17Economy.LEVEL)
            onComplete(earnedStars, hintsUsed)
            audio.play(WordZAudio.Fx.COMPLETE)
            if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            if (!reduceMotion) delay(260)
            showResult = true
        }
    }

    val wordScale by animateFloatAsState(
        targetValue = if (live.isNotBlank()) 1.025f else 1f,
        animationSpec = tween(if (reduceMotion) 0 else 100),
        label = "wordScale",
    )

    fun submitWord() {
        val submitted = live.trim().uppercase(w17Tr)
        when {
            submitted in level.targets && submitted !in found -> {
                found = found + submitted
                combo += 1
                if (rewardEligible) {
                    onWord()
                    onCombo(combo)
                }
                val comboReward = if (rewardEligible && combo % 3 == 0) W17Economy.COMBO else 0
                val gain = if (rewardEligible) W17Economy.WORD + comboReward else 0
                if (gain > 0) changeCoins(gain)
                feedback = W17Feedback.CORRECT
                status = if (gain > 0) "Doğru · +$gain" else "Doğru"
                persist()
                audio.play(WordZAudio.Fx.CORRECT)
            }

            submitted in found -> status = "Zaten bulundu"

            submitted in level.bonusWords && submitted !in bonus -> {
                bonus = bonus + submitted
                combo += 1
                if (rewardEligible) {
                    onWord()
                    onCombo(combo)
                }
                val gain = if (rewardEligible) W17Economy.BONUS else 0
                if (gain > 0) changeCoins(gain)
                feedback = W17Feedback.BONUS
                status = if (gain > 0) "Bonus · +$gain" else "Bonus kelime"
                persist()
                audio.play(WordZAudio.Fx.BONUS)
            }

            submitted.isNotBlank() -> {
                combo = 0
                feedback = W17Feedback.WRONG
                status = "Bu levelde yok"
                wrongTick += 1
                audio.play(WordZAudio.Fx.WRONG)
            }
        }
        selected = emptyList()
        live = ""
    }

    W17Surface {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxHeight < 760.dp
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp).offset(x = shake.value.dp)) {
                Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
                    W17IconButton(Icons.Filled.ArrowBack, back)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Level ${levelIndex + 1}", color = W17Colors.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(level.title, color = W17Colors.Muted, fontSize = 11.sp)
                    }
                    W17Chip(Icons.Filled.AutoAwesome, localCoins.toString(), W17Colors.Gold)
                }

                Spacer(Modifier.height(6.dp))
                W17Board(level, found, hinted, compact)
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${found.size}/${level.targets.size} kelime", color = W17Colors.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    AnimatedVisibility(visible = combo >= 3, enter = fadeIn(), exit = fadeOut()) {
                        Text("COMBO x$combo", color = W17Colors.Gold, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(7.dp))
                Box(
                    Modifier.fillMaxWidth().height(44.dp).scale(wordScale).clip(RoundedCornerShape(13.dp))
                        .background(if (live.isNotBlank()) W17Colors.BlueSoft else W17Colors.Surface)
                        .border(1.dp, W17Colors.Border, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (live.isNotBlank()) live else status,
                        color = if (live.isNotBlank()) W17Colors.BlueDark else W17Colors.Muted,
                        fontWeight = if (live.isNotBlank()) FontWeight.Bold else FontWeight.Medium,
                        fontSize = if (live.isNotBlank()) 19.sp else 12.sp,
                    )
                }

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    W17Wheel(
                        letters = wheel,
                        selected = selected,
                        sizeDp = if (compact) 214.dp else 244.dp,
                        feedback = feedback,
                        haptic = hapticEnabled,
                        audio = audio,
                        onChanged = { value ->
                            if (value.size == 1) feedback = W17Feedback.NORMAL
                            selected = value
                            live = value.joinToString("") { index -> wheel[index].toString() }
                        },
                        onFinished = { submitWord() },
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    W17Tool(Icons.Filled.Shuffle, "Karıştır", true, Modifier.weight(1f)) {
                        wheel = wheel.shuffled()
                        status = "Harfler karıştırıldı"
                    }
                    W17Tool(
                        Icons.Filled.Lightbulb,
                        "Harf ${W17Economy.LETTER_HINT}",
                        localCoins >= W17Economy.LETTER_HINT && !done,
                        Modifier.weight(1f),
                    ) {
                        val hidden = w17AllCells(level).filter { it !in hinted && !w17Revealed(level, it, found) }
                        hidden.randomOrNull()?.let { cell ->
                            changeCoins(-W17Economy.LETTER_HINT)
                            hinted = hinted + cell
                            hintsUsed += 1
                            combo = 0
                            onHint()
                            persist()
                            status = "Bir harf açıldı"
                        }
                    }
                    W17Tool(
                        Icons.Filled.Search,
                        "Kelime ${W17Economy.WORD_HINT}",
                        localCoins >= W17Economy.WORD_HINT && !done,
                        Modifier.weight(1f),
                    ) {
                        level.targets.firstOrNull { it !in found }?.let { word ->
                            changeCoins(-W17Economy.WORD_HINT)
                            found = found + word
                            hintsUsed += 2
                            combo = 0
                            onHint()
                            persist()
                            status = "Kelime açıldı"
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = gainToast != null,
                modifier = Modifier.align(Alignment.Center),
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xE6172033))
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(gainToast.orEmpty(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (showResult) {
                W17Result(
                    stars = earnedStars,
                    previous = previousStars,
                    found = found.size,
                    target = level.targets.size,
                    bonus = bonus.size,
                    hints = hintsUsed,
                    combo = combo,
                    net = net,
                    chapterEnd = chapterEnd,
                    chestClaimed = chestClaimed,
                    completedBefore = completedBefore,
                    reduceMotion = reduceMotion,
                    replay = replay,
                    map = map,
                    next = next,
                    claim = claimChapter,
                )
            }
        }
    }
}

@Composable
private fun W17Tool(icon: ImageVector, label: String, enabled: Boolean, modifier: Modifier = Modifier, click: () -> Unit) {
    Column(
        modifier.height(58.dp).clip(RoundedCornerShape(15.dp)).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(15.dp)).clickable(enabled = enabled, onClick = click),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = if (enabled) W17Colors.Blue else W17Colors.Subtle, modifier = Modifier.size(20.dp))
        Text(label, color = if (enabled) W17Colors.Ink else W17Colors.Subtle, fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
    }
}

private fun w17Cells(placement: Placement): List<Pair<W17Cell, Char>> {
    return placement.word.mapIndexed { index, char ->
        W17Cell(
            placement.row + if (placement.horizontal) 0 else index,
            placement.col + if (placement.horizontal) index else 0,
        ) to char
    }
}

private fun w17AllCells(level: Level): Set<W17Cell> = level.placements.flatMap(::w17Cells).map { it.first }.toSet()

private fun w17Revealed(level: Level, cell: W17Cell, found: Set<String>): Boolean {
    return level.placements.any { placement ->
        placement.word in found && w17Cells(placement).any { it.first == cell }
    }
}

@Composable
private fun W17Board(level: Level, found: Set<String>, hinted: Set<W17Cell>, compact: Boolean) {
    val validated = remember(level) { validateLevel(level) }
    val map = remember(validated) { validated.lettersByCell.mapKeys { W17Cell(it.key.row, it.key.col) } }
    val rows = (map.keys.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (map.keys.maxOfOrNull { it.col } ?: 0) + 1
    val cellSize = when {
        compact && cols >= 5 -> 33.dp
        cols >= 6 -> 35.dp
        cols == 5 -> 39.dp
        else -> 42.dp
    }

    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(20.dp)).padding(if (compact) 8.dp else 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(cols) { col ->
                        val cell = W17Cell(row, col)
                        val letter = map[cell]
                        if (letter == null) {
                            Spacer(Modifier.size(cellSize))
                        } else {
                            val fromHint = cell in hinted
                            val revealed = fromHint || w17Revealed(level, cell, found)
                            val scale by animateFloatAsState(
                                targetValue = if (revealed) 1f else .92f,
                                animationSpec = tween(180),
                                label = "cell",
                            )
                            Box(
                                Modifier.size(cellSize).scale(scale).clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            fromHint -> W17Colors.GoldSoft
                                            revealed -> W17Colors.BlueSoft
                                            else -> Color(0xFFF0F3F8)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when {
                                            fromHint -> W17Colors.Gold.copy(alpha = .55f)
                                            revealed -> W17Colors.Blue.copy(alpha = .45f)
                                            else -> Color(0xFFD8DEE8)
                                        },
                                        RoundedCornerShape(8.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (revealed) {
                                    Text(
                                        letter.toString(),
                                        color = if (fromHint) Color(0xFFB77900) else W17Colors.BlueDark,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (compact) 15.sp else 17.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun W17Wheel(
    letters: List<Char>,
    selected: List<Int>,
    sizeDp: Dp,
    feedback: W17Feedback,
    haptic: Boolean,
    audio: WordZAudio,
    onChanged: (List<Int>) -> Unit,
    onFinished: () -> Unit,
) {
    var measured by remember { mutableStateOf(IntSize.Zero) }
    var pointer by remember { mutableStateOf<Offset?>(null) }
    val haptics = LocalHapticFeedback.current

    fun positions(): List<Offset> {
        if (measured == IntSize.Zero) return emptyList()
        val center = Offset(measured.width / 2f, measured.height / 2f)
        val radius = minOf(measured.width, measured.height) * .34f
        return letters.indices.map { index ->
            val angle = -PI / 2 + 2 * PI * index / letters.size
            Offset(
                center.x + (cos(angle) * radius).toFloat(),
                center.y + (sin(angle) * radius).toFloat(),
            )
        }
    }

    fun indexAt(point: Offset): Int? {
        val points = positions()
        val hit = minOf(measured.width, measured.height) * .13f
        return points.indices.firstOrNull { index ->
            hypot((point.x - points[index].x).toDouble(), (point.y - points[index].y).toDouble()) <= hit
        }
    }

    Box(
        Modifier.size(sizeDp).onSizeChanged { measured = it }
            .pointerInput(letters, measured) {
                var gesture = emptyList<Int>()
                detectDragGestures(
                    onDragStart = { point ->
                        pointer = point
                        val index = indexAt(point)
                        gesture = if (index != null) listOf(index) else emptyList()
                        onChanged(gesture)
                        if (index != null) {
                            audio.play(WordZAudio.Fx.SELECT, .55f)
                            if (haptic) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    onDragEnd = {
                        pointer = null
                        onFinished()
                        gesture = emptyList()
                    },
                    onDragCancel = {
                        pointer = null
                        gesture = emptyList()
                    },
                ) { change, _ ->
                    pointer = change.position
                    val index = indexAt(change.position)
                    if (index != null) {
                        when {
                            gesture.size >= 2 && index == gesture[gesture.lastIndex - 1] -> {
                                gesture = gesture.dropLast(1)
                                onChanged(gesture)
                            }
                            index !in gesture -> {
                                gesture = gesture + index
                                onChanged(gesture)
                                audio.play(WordZAudio.Fx.SELECT, .55f)
                                if (haptic) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    }
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = minOf(size.width, size.height) * .47f
            val lineColor = when (feedback) {
                W17Feedback.CORRECT -> W17Colors.Green
                W17Feedback.BONUS -> W17Colors.Gold
                W17Feedback.WRONG -> W17Colors.Danger
                W17Feedback.NORMAL -> W17Colors.Blue
            }
            drawCircle(W17Colors.Surface, radius, center)
            drawCircle(Color(0xFFE3E7EE), radius, center, style = Stroke(width = 3f))

            val points = positions()
            selected.zipWithNext().forEach { (a, b) ->
                if (a in points.indices && b in points.indices) {
                    drawLine(lineColor, points[a], points[b], 9f, cap = StrokeCap.Round)
                }
            }
            if (selected.isNotEmpty() && pointer != null && selected.last() in points.indices) {
                drawLine(lineColor.copy(alpha = .6f), points[selected.last()], pointer!!, 7f, cap = StrokeCap.Round)
            }

            points.forEachIndexed { index, point ->
                val active = index in selected
                val r = minOf(size.width, size.height) * if (active) .11f else .101f
                drawCircle(if (active) lineColor else Color(0xFFF8F9FB), r, point)
                drawCircle(if (active) lineColor else Color(0xFFD2D8E1), r, point, style = Stroke(width = 2.4f))
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = if (active) android.graphics.Color.WHITE else android.graphics.Color.rgb(23, 32, 51)
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = minOf(size.width, size.height) * .104f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val metrics = paint.fontMetrics
                    canvas.nativeCanvas.drawText(
                        letters[index].toString(),
                        point.x,
                        point.y - (metrics.ascent + metrics.descent) / 2,
                        paint,
                    )
                }
            }
        }
    }
}

@Composable
private fun W17Result(
    stars: Int,
    previous: Int,
    found: Int,
    target: Int,
    bonus: Int,
    hints: Int,
    combo: Int,
    net: Int,
    chapterEnd: Boolean,
    chestClaimed: Boolean,
    completedBefore: Boolean,
    reduceMotion: Boolean,
    replay: () -> Unit,
    map: () -> Unit,
    next: () -> Unit,
    claim: () -> Unit,
) {
    var shownStars by remember { mutableIntStateOf(if (reduceMotion) stars else 0) }
    LaunchedEffect(stars, reduceMotion) {
        if (!reduceMotion) {
            repeat(stars) { index ->
                delay(220)
                shownStars = index + 1
            }
        }
    }
    val animatedNet by animateIntAsState(net, tween(if (reduceMotion) 0 else 650), label = "net")

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .38f)).padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        W17Card(Modifier.fillMaxWidth()) {
            Text(
                if (chapterEnd) "Bölüm tamamlandı" else "Level tamamlandı",
                color = W17Colors.Ink,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                repeat(3) { index ->
                    Icon(
                        if (index < shownStars) Icons.Filled.Star else Icons.Filled.StarBorder,
                        null,
                        tint = if (index < shownStars) W17Colors.Gold else Color(0xFFD0D5DD),
                        modifier = Modifier.size(if (index == 1) 42.dp else 34.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    stars > previous -> "Yeni en iyi skor"
                    completedBefore -> "Pratik tamamlandı"
                    else -> "Yeni level açıldı"
                },
                color = W17Colors.Blue,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            W17ResultRow("Bulunan kelime", "$found/$target")
            W17ResultRow("Bonus kelime", bonus.toString())
            W17ResultRow("Kullanılan ipucu", hints.toString())
            W17ResultRow("Combo", "x$combo")
            HorizontalDivider(color = W17Colors.Border, modifier = Modifier.padding(vertical = 10.dp))
            Row(Modifier.fillMaxWidth()) {
                Text("Net jeton", color = W17Colors.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(
                    if (animatedNet >= 0) "+$animatedNet" else animatedNet.toString(),
                    color = if (animatedNet >= 0) W17Colors.Green else W17Colors.Danger,
                    fontWeight = FontWeight.Black,
                    fontSize = 21.sp,
                )
            }
            if (chapterEnd && !chestClaimed) {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(W17Colors.GoldSoft)
                        .clickable(onClick = claim).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.CardGiftcard, null, tint = W17Colors.Gold)
                    Spacer(Modifier.width(10.dp))
                    Text("Bölüm sandığı +${W17Economy.CHAPTER}", color = W17Colors.Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Al", color = W17Colors.Gold, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(16.dp))
            W17Primary(
                if (chapterEnd) "Haritaya dön" else "Sonraki level",
                if (chapterEnd) map else next,
                Modifier.fillMaxWidth(),
                if (chapterEnd) Icons.Filled.Map else Icons.Filled.PlayArrow,
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                W17Secondary("Tekrar oyna", replay, Modifier.weight(1f), Icons.Filled.Refresh)
                if (!chapterEnd) W17Secondary("Harita", map, Modifier.weight(1f), Icons.Filled.Map)
            }
        }
    }
}

@Composable
private fun W17ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(label, color = W17Colors.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = W17Colors.Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun W17Daily(state: DailyRewardState, back: () -> Unit, claim: () -> Unit) {
    W17Surface {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            W17TopBar("7 günlük ödül", back)
            Text("Her gün gel, ödül büyüsün", color = W17Colors.Muted, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            W17Card(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(W17Colors.GoldSoft, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocalFireDepartment, null, tint = W17Colors.Gold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("${state.streak} günlük seri", color = W17Colors.Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(if (state.claimedToday) "Bugünkü ödül alındı" else "Bugünkü ödül +${state.rewardToday}", color = W17Colors.Muted, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetaProgressStore.DAILY_REWARDS.forEachIndexed { index, reward ->
                    val active = index == state.cycleDay
                    val claimed = index < state.cycleDay || (state.claimedToday && index == state.cycleDay)
                    Column(
                        Modifier.weight(1f).height(94.dp).clip(RoundedCornerShape(13.dp))
                            .background(if (active) W17Colors.GoldSoft else W17Colors.Surface)
                            .border(1.dp, if (active) W17Colors.Gold.copy(alpha = .45f) else W17Colors.Border, RoundedCornerShape(13.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("${index + 1}. gün", color = if (active) W17Colors.Gold else W17Colors.Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Icon(if (claimed) Icons.Filled.CheckCircle else Icons.Filled.CardGiftcard, null, tint = if (claimed) W17Colors.Green else W17Colors.Gold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(6.dp))
                        Text("+$reward", color = W17Colors.Ink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            W17Primary(
                if (state.claimedToday) "Bugünkü ödül alındı" else "+${state.rewardToday} jeton al",
                claim,
                Modifier.fillMaxWidth().padding(bottom = 24.dp),
                Icons.Filled.CardGiftcard,
                !state.claimedToday,
            )
        }
    }
}

@Composable
private fun W17Tasks(
    daily: ChallengeProgress,
    weekly: ChallengeProgress,
    dailyClaims: Set<String>,
    weeklyClaims: Set<String>,
    back: () -> Unit,
    claim: (W17Task, Boolean) -> Unit,
    home: () -> Unit,
    chapters: () -> Unit,
    profile: () -> Unit,
) {
    W17Surface {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 86.dp)) {
                W17TopBar("Görevler", back)
                Text("Günlük görevler her gün, haftalık görevler pazartesi yenilenir", color = W17Colors.Muted, fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    Text("Günlük", color = W17Colors.Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    w17DailyTasks().forEach { task ->
                        W17TaskCard(task, daily, task.id in dailyClaims, false, claim)
                        Spacer(Modifier.height(10.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Haftalık", color = W17Colors.Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    w17WeeklyTasks().forEach { task ->
                        W17TaskCard(task, weekly, task.id in weeklyClaims, true, claim)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                W17Bottom(W17Screen.TASKS, home, chapters, {}, profile)
            }
        }
    }
}

@Composable
private fun W17TaskCard(
    task: W17Task,
    progress: ChallengeProgress,
    claimed: Boolean,
    weekly: Boolean,
    claim: (W17Task, Boolean) -> Unit,
) {
    val current = task.current(progress).coerceAtMost(task.goal)
    val done = current >= task.goal
    W17Card(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(if (done) W17Colors.GreenSoft else W17Colors.BlueSoft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    if (done) Icons.Filled.CheckCircle else if (weekly) Icons.Filled.CalendarToday else Icons.Filled.TaskAlt,
                    null,
                    tint = if (done) W17Colors.Green else W17Colors.Blue,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, color = W17Colors.Ink, fontWeight = FontWeight.Bold)
                Text(task.subtitle, color = W17Colors.Muted, fontSize = 10.sp)
            }
            Text("+${task.reward}", color = W17Colors.Gold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(11.dp))
        LinearProgressIndicator(
            progress = { current.toFloat() / task.goal.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = if (done) W17Colors.Green else W17Colors.Blue,
            trackColor = Color(0xFFF0F2F5),
        )
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("$current/${task.goal}", color = W17Colors.Muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
            Text(
                if (claimed) "Alındı" else if (done) "Ödülü al" else "Devam ediyor",
                color = if (done && !claimed) W17Colors.Blue else W17Colors.Muted,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.clickable(enabled = done && !claimed) { claim(task, weekly) },
            )
        }
    }
}

@Composable
private fun W17Profile(
    coins: Int,
    stats: W17Stats,
    player: PlayerProgress,
    back: () -> Unit,
    home: () -> Unit,
    chapters: () -> Unit,
    tasks: () -> Unit,
    settings: () -> Unit,
) {
    W17Surface {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 86.dp)) {
                W17TopBar("Profil", back) { W17IconButton(Icons.Filled.Settings, settings) }
                Spacer(Modifier.height(12.dp))
                W17Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(64.dp).background(W17Colors.PurpleSoft, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = W17Colors.Purple, modifier = Modifier.size(31.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Seviye ${player.level}", color = W17Colors.Ink, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            Text("Toplam ${player.totalXp} XP", color = W17Colors.Muted, fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { player.currentLevelXp.toFloat() / player.nextLevelXp.coerceAtLeast(1) },
                                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                                color = W17Colors.Purple,
                                trackColor = W17Colors.PurpleSoft,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("${player.currentLevelXp}/${player.nextLevelXp} XP · seviye ödülü +${PlayerProgressStore.LEVEL_UP_COIN_REWARD}", color = W17Colors.Muted, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    W17Stat("Yıldız", stats.stars.toString(), Icons.Filled.Star, W17Colors.Gold, Modifier.weight(1f))
                    W17Stat("Level", stats.completed.toString(), Icons.Filled.CheckCircle, W17Colors.Green, Modifier.weight(1f))
                    W17Stat("Jeton", coins.toString(), Icons.Filled.AutoAwesome, W17Colors.Blue, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    W17Stat("Kelime", stats.words.toString(), Icons.Filled.TaskAlt, W17Colors.Blue, Modifier.weight(1f))
                    W17Stat("İpucu", stats.hints.toString(), Icons.Filled.Lightbulb, W17Colors.Gold, Modifier.weight(1f))
                    W17Stat("Combo", "x${stats.combo}", Icons.Filled.LocalFireDepartment, W17Colors.Danger, Modifier.weight(1f))
                }
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                W17Bottom(W17Screen.PROFILE, home, chapters, tasks, {})
            }
        }
    }
}

@Composable
private fun W17Stat(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.height(104.dp).clip(RoundedCornerShape(17.dp)).background(W17Colors.Surface)
            .border(1.dp, W17Colors.Border, RoundedCornerShape(17.dp)).padding(13.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Column {
            Text(value, color = W17Colors.Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
            Text(label, color = W17Colors.Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun W17Settings(
    sound: Boolean,
    music: Boolean,
    haptic: Boolean,
    reduceMotion: Boolean,
    back: () -> Unit,
    onSound: (Boolean) -> Unit,
    onMusic: (Boolean) -> Unit,
    onHaptic: (Boolean) -> Unit,
    onMotion: (Boolean) -> Unit,
) {
    W17Surface {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            W17TopBar("Ayarlar", back)
            Spacer(Modifier.height(14.dp))
            W17Setting(Icons.Filled.VolumeUp, "Ses efektleri", sound, onSound)
            W17Setting(Icons.Filled.MusicNote, "Arka plan müziği", music, onMusic)
            W17Setting(Icons.Filled.Vibration, "Titreşim", haptic, onHaptic)
            W17Setting(Icons.Filled.Refresh, "Animasyonları azalt", reduceMotion, onMotion)
            Spacer(Modifier.height(18.dp))
            Text("Sürüm ${BuildConfig.VERSION_NAME}", color = W17Colors.Subtle, fontSize = 11.sp)
        }
    }
}

@Composable
private fun W17Setting(icon: ImageVector, title: String, value: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().height(62.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).background(W17Colors.BlueSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = W17Colors.Blue, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = W17Colors.Ink, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = change)
    }
    HorizontalDivider(color = W17Colors.Border)
}

@Composable
private fun W17LevelUpOverlay(level: Int, reduceMotion: Boolean, dismiss: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(if (reduceMotion) 0 else 420),
        label = "levelUp",
    )
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .42f)).clickable(onClick = dismiss),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.scale(scale).padding(26.dp).fillMaxWidth(.84f).clip(RoundedCornerShape(26.dp))
                .background(W17Colors.Surface).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(72.dp).background(W17Colors.PurpleSoft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.EmojiEvents, null, tint = W17Colors.Purple, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("SEVİYE ATLADIN", color = W17Colors.Purple, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text("Seviye $level", color = W17Colors.Ink, fontWeight = FontWeight.Black, fontSize = 30.sp)
            Spacer(Modifier.height(8.dp))
            Text("+${PlayerProgressStore.LEVEL_UP_COIN_REWARD} jeton ödülü", color = W17Colors.Muted, fontSize = 13.sp)
            Spacer(Modifier.height(18.dp))
            W17Primary("Devam et", dismiss, Modifier.fillMaxWidth(), Icons.Filled.PlayArrow)
        }
    }
}
