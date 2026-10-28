package com.zafer.wordz

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private const val B44_THEME_KEY = "b44_active_theme"
private const val B44_OWNED_THEMES_KEY = "b44_owned_themes"
private const val B44_SOUND_KEY = "sound_enabled"
private const val B44_MUSIC_KEY = "music_enabled"

private data class B44V4Cell(val row: Int, val col: Int, val letter: Char)
private data class B44V4WordCells(val word: String, val cells: List<Pair<Int, Int>>)

private data class B44ThemeSpec(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val price: Int,
    val bgStart: Color,
    val bgMid: Color,
    val bgEnd: Color,
    val accent: Color,
    val lineColor: Color,
    val wheelStart: Color,
    val wheelEnd: Color,
    val wheelText: Color,
    val cellStart: Color,
    val cellEnd: Color,
)

private val b44ThemeSpecs = listOf(
    B44ThemeSpec(
        id = "default",
        name = "Kozmik Mor",
        icon = "🌌",
        description = "Mor nebulalar ve parlak yıldızlar",
        price = 0,
        bgStart = Color(0xFF06020F),
        bgMid = Color(0xFF1C0E40),
        bgEnd = Color(0xFF0A1628),
        accent = Color(0xFF7B3FC4),
        lineColor = Color(0xFF9B51E0),
        wheelStart = Color(0xFFFAFAF8),
        wheelEnd = Color(0xFFEDEDF2),
        wheelText = Color(0xFF1E0B3C),
        cellStart = Color(0xFF9B51E0),
        cellEnd = Color(0xFF5A189A),
    ),
    B44ThemeSpec(
        id = "nostalji",
        name = "Nostalji",
        icon = "📜",
        description = "Sıcak sepya ve eski kâğıt atmosferi",
        price = 300,
        bgStart = Color(0xFF1A0E08),
        bgMid = Color(0xFF2C1810),
        bgEnd = Color(0xFF1A1208),
        accent = Color(0xFFC8911F),
        lineColor = Color(0xFFD4A96A),
        wheelStart = Color(0xFFF5ECD5),
        wheelEnd = Color(0xFFE8D5A8),
        wheelText = Color(0xFF432A18),
        cellStart = Color(0xFFC8911F),
        cellEnd = Color(0xFF7A5010),
    ),
    B44ThemeSpec(
        id = "pastel",
        name = "Pastel Dünya",
        icon = "🌸",
        description = "Yumuşak pembe ve lavanta tonları",
        price = 300,
        bgStart = Color(0xFF120920),
        bgMid = Color(0xFF1E0F35),
        bgEnd = Color(0xFF120E22),
        accent = Color(0xFFD06BA8),
        lineColor = Color(0xFFEC8FBD),
        wheelStart = Color(0xFFFFF0F8),
        wheelEnd = Color(0xFFF0E8FF),
        wheelText = Color(0xFF3A1738),
        cellStart = Color(0xFFD06BA8),
        cellEnd = Color(0xFF86327A),
    ),
    B44ThemeSpec(
        id = "gece",
        name = "Gece Modu",
        icon = "🌙",
        description = "Siyah zemin ve neon yeşil vurgular",
        price = 500,
        bgStart = Color.Black,
        bgMid = Color(0xFF080808),
        bgEnd = Color(0xFF050510),
        accent = Color(0xFF00A876),
        lineColor = Color(0xFF00C896),
        wheelStart = Color(0xFF1A1A1A),
        wheelEnd = Color(0xFF111111),
        wheelText = Color(0xFFA0FFC8),
        cellStart = Color(0xFF00A876),
        cellEnd = Color(0xFF006040),
    ),
)

private fun b44V4BuildGrid(level: Level): Pair<List<B44V4Cell>, List<B44V4WordCells>> {
    val map = linkedMapOf<Pair<Int, Int>, Char>()
    val words = mutableListOf<B44V4WordCells>()
    level.placements.forEach { placement ->
        val coords = placement.word.indices.map { index ->
            if (placement.horizontal) placement.row to (placement.col + index)
            else (placement.row + index) to placement.col
        }
        placement.word.forEachIndexed { index, char ->
            val key = coords[index]
            if (map[key] == null || map[key] == char) map[key] = char
        }
        words += B44V4WordCells(placement.word, coords)
    }
    return map.map { (key, value) -> B44V4Cell(key.first, key.second, value) } to words
}

private fun b44V4Country(levelNumber: Int): B44Country =
    b44Countries.firstOrNull { levelNumber in it.start..it.end } ?: b44Countries.last()

private fun b44V4WheelLetters(words: List<String>): List<Char> {
    val maxCounts = linkedMapOf<Char, Int>()
    words.forEach { word ->
        val local = linkedMapOf<Char, Int>()
        word.forEach { char -> local[char] = (local[char] ?: 0) + 1 }
        local.forEach { (char, count) -> maxCounts[char] = maxOf(maxCounts[char] ?: 0, count) }
    }
    return buildList {
        maxCounts.forEach { (char, count) -> repeat(count) { add(char) } }
    }
}

@Composable
internal fun B44V4GameScreen(
    levelIndex: Int,
    coins: Int,
    hints: Int,
    completedBefore: Boolean,
    onCoins: (Int) -> Unit,
    onHints: (Int) -> Unit,
    onRewardXp: (Int) -> Unit,
    onComplete: () -> Unit,
    back: () -> Unit,
    next: () -> Unit,
    home: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(B44V2_PREFS, Context.MODE_PRIVATE) }
    val audio = remember { WordZAudio(context.applicationContext) }
    val playerStore = remember { PlayerProgressStore(prefs) }
    val startXp = remember(levelIndex) { playerStore.snapshot().totalXp }
    val wasCompletedBefore = remember(levelIndex) { completedBefore }

    var soundEnabled by remember { mutableStateOf(prefs.getBoolean(B44_SOUND_KEY, true)) }
    var musicEnabled by remember { mutableStateOf(prefs.getBoolean(B44_MUSIC_KEY, true)) }
    var activeThemeId by remember { mutableStateOf(prefs.getString(B44_THEME_KEY, "default") ?: "default") }
    var ownedThemes by remember {
        mutableStateOf(
            (prefs.getStringSet(B44_OWNED_THEMES_KEY, setOf("default")) ?: setOf("default"))
                .toSet() + "default"
        )
    }

    DisposableEffect(Unit) { onDispose { audio.release() } }
    LaunchedEffect(soundEnabled) { audio.setEffectsEnabled(soundEnabled) }
    LaunchedEffect(musicEnabled) { audio.setMusicEnabled(musicEnabled) }

    val theme = b44ThemeSpecs.firstOrNull { it.id == activeThemeId } ?: b44ThemeSpecs.first()
    val level = levels[levelIndex]
    val gridData = remember(levelIndex) { b44V4BuildGrid(level) }
    val cells = gridData.first
    val words = gridData.second
    val country = b44V4Country(levelIndex + 1)
    val wheelLetters = remember(levelIndex) { b44V4WheelLetters(level.targets).ifEmpty { level.letters } }

    var found by remember(levelIndex) { mutableStateOf<Set<String>>(emptySet()) }
    var revealed by remember(levelIndex) { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }
    var correctBanner by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showThemeStore by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var rewardXp by remember { mutableIntStateOf(0) }
    var rewardCoins by remember { mutableIntStateOf(0) }
    var speedBonus by remember { mutableIntStateOf(0) }
    var finished by remember(levelIndex) { mutableStateOf(false) }
    var wrongPulse by remember { mutableIntStateOf(0) }
    val startedAt = remember(levelIndex) { System.currentTimeMillis() }
    val shake = remember { Animatable(0f) }

    fun finishIfNeeded(newFound: Set<String>) {
        if (finished || newFound.size < level.targets.size) return
        finished = true
        val elapsed = System.currentTimeMillis() - startedAt
        val bonus = when {
            elapsed < 30_000 -> 150
            elapsed < 60_000 -> 50
            else -> 0
        }
        val xp = if (wasCompletedBefore) 0 else 100 + bonus
        val coinReward = if (wasCompletedBefore) 0 else 50
        speedBonus = if (wasCompletedBefore) 0 else bonus
        rewardXp = xp
        rewardCoins = coinReward
        if (coinReward > 0) onCoins(coins + coinReward)
        if (xp > 0) onRewardXp(xp)
        onComplete()
        audio.play(WordZAudio.Fx.COMPLETE)
        showResult = true
    }

    fun submit(raw: String) {
        if (raw.length < 2 || finished) return
        val word = raw.uppercase(Locale("tr", "TR"))
        if (word in level.targets && word !in found) {
            val target = words.firstOrNull { it.word == word }
            if (target != null) revealed = revealed + target.cells
            val newFound = found + word
            found = newFound
            correctBanner = word
            audio.play(WordZAudio.Fx.CORRECT)
            finishIfNeeded(newFound)
        } else if (word in level.bonusWords) {
            correctBanner = "BONUS · $word"
            audio.play(WordZAudio.Fx.BONUS)
        } else {
            correctBanner = null
            wrongPulse += 1
            audio.play(WordZAudio.Fx.WRONG)
        }
    }

    LaunchedEffect(correctBanner) {
        if (correctBanner != null) {
            delay(1500)
            correctBanner = null
        }
    }

    LaunchedEffect(wrongPulse) {
        if (wrongPulse == 0) return@LaunchedEffect
        shake.snapTo(0f)
        shake.animateTo(-8f, tween(70))
        shake.animateTo(8f, tween(70))
        shake.animateTo(-7f, tween(70))
        shake.animateTo(7f, tween(70))
        shake.animateTo(0f, tween(70))
    }

    B44V4GameBackground(theme) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                B44V4GameHeader(coins, hints) { showSettings = true }
                Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(country.emoji, fontSize = 18.sp)
                    Spacer(Modifier.width(7.dp))
                    Text(country.name, color = country.accent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.clip(CircleShape).background(Color.White.copy(alpha = .06f))
                            .border(1.dp, country.accent.copy(alpha = .20f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Bölüm ${levelIndex + 1}", color = Color.White.copy(alpha = .58f), fontSize = 9.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.offset(x = shake.value.dp)) {
                    B44V4CrosswordGrid(cells, revealed, theme)
                }
                Spacer(Modifier.height(10.dp))
                AnimatedVisibility(
                    visible = correctBanner != null,
                    enter = fadeIn() + scaleIn(initialScale = .7f),
                    exit = fadeOut() + scaleOut(targetScale = .8f),
                ) {
                    Box(
                        Modifier.clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(theme.accent.copy(alpha = .55f), theme.accent.copy(alpha = .18f))))
                            .border(1.dp, theme.lineColor.copy(alpha = .65f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("✦ ${correctBanner.orEmpty()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(7.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    level.targets.forEach { word ->
                        Box(
                            Modifier.padding(horizontal = 3.dp).clip(CircleShape)
                                .background(if (word in found) theme.accent.copy(alpha = .18f) else Color.White.copy(alpha = .04f))
                                .border(1.dp, if (word in found) theme.lineColor.copy(alpha = .35f) else Color.White.copy(alpha = .07f), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                if (word in found) word else "•".repeat(word.length),
                                color = if (word in found) Color.White else Color.White.copy(alpha = .24f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    val wheelSize = (maxWidth - 118.dp).coerceAtMost(260.dp).coerceAtLeast(210.dp)
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        B44V4ThemeButton(theme) { showThemeStore = true }
                        B44V4LetterWheel(wheelLetters, theme, wheelSize, ::submit)
                        B44V4HintButton(hints, coins) {
                            val canUse = when {
                                hints > 0 -> {
                                    onHints(hints - 1)
                                    true
                                }
                                coins >= 100 -> {
                                    onCoins(coins - 100)
                                    true
                                }
                                else -> false
                            }
                            if (canUse) {
                                audio.play(WordZAudio.Fx.SELECT)
                                val target = words.firstOrNull { it.word !in found }
                                val pos = target?.cells?.firstOrNull { it !in revealed }
                                if (target != null && pos != null) {
                                    val nextRevealed = revealed + pos
                                    revealed = nextRevealed
                                    if (target.cells.all { it in nextRevealed }) {
                                        val newFound = found + target.word
                                        found = newFound
                                        correctBanner = target.word
                                        audio.play(WordZAudio.Fx.CORRECT)
                                        finishIfNeeded(newFound)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showSettings,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 }),
            ) {
                B44V4SettingsSheet(
                    level = levelIndex + 1,
                    sound = soundEnabled,
                    music = musicEnabled,
                    onSound = {
                        soundEnabled = it
                        prefs.edit().putBoolean(B44_SOUND_KEY, it).apply()
                    },
                    onMusic = {
                        musicEnabled = it
                        prefs.edit().putBoolean(B44_MUSIC_KEY, it).apply()
                    },
                    close = { showSettings = false },
                    home = home,
                )
            }

            AnimatedVisibility(
                visible = showThemeStore,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            ) {
                B44V4ThemeStore(
                    coins = coins,
                    activeThemeId = activeThemeId,
                    ownedThemes = ownedThemes,
                    onBuyOrApply = { selected ->
                        if (selected.id in ownedThemes) {
                            activeThemeId = selected.id
                            prefs.edit().putString(B44_THEME_KEY, selected.id).apply()
                            audio.play(WordZAudio.Fx.SELECT)
                            true
                        } else if (coins >= selected.price) {
                            val nextOwned = ownedThemes + selected.id
                            ownedThemes = nextOwned
                            activeThemeId = selected.id
                            onCoins(coins - selected.price)
                            prefs.edit()
                                .putStringSet(B44_OWNED_THEMES_KEY, nextOwned)
                                .putString(B44_THEME_KEY, selected.id)
                                .apply()
                            audio.play(WordZAudio.Fx.REWARD)
                            true
                        } else {
                            false
                        }
                    },
                    close = { showThemeStore = false },
                )
            }

            if (showResult) {
                B44V4ResultOverlay(
                    level = levelIndex + 1,
                    oldXp = startXp,
                    xpGained = rewardXp,
                    coinsGained = rewardCoins,
                    speedBonus = speedBonus,
                    replay = wasCompletedBefore,
                    next = next,
                    home = home,
                )
            }
        }
    }
}

@Composable
private fun B44V4GameBackground(theme: B44ThemeSpec, content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(theme.bgStart, theme.bgMid, theme.bgEnd))
        )
    ) {
        val pulse = rememberInfiniteTransition(label = "themeAmbient")
        val scale by pulse.animateFloat(
            1f, 1.18f,
            infiniteRepeatable(tween(5200), RepeatMode.Reverse),
            label = "themeAmbientScale",
        )
        Box(
            Modifier.size(420.dp).offset((-120).dp, (-120).dp).scale(scale)
                .background(Brush.radialGradient(listOf(theme.accent.copy(alpha = .18f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.align(Alignment.BottomEnd).size(360.dp).offset(95.dp, 75.dp)
                .background(Brush.radialGradient(listOf(theme.lineColor.copy(alpha = .10f), Color.Transparent)), CircleShape)
        )
        content()
    }
}

@Composable
private fun B44V4GameHeader(coins: Int, hints: Int, settings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        B44V4ResourceChip("◆", coins.toString(), B44V2Colors.Gold)
        Spacer(Modifier.width(8.dp))
        B44V4ResourceChip("💡", hints.toString(), B44V2Colors.Emerald)
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .06f))
                .border(1.dp, Color.White.copy(alpha = .10f), RoundedCornerShape(14.dp))
                .clickable(onClick = settings),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Settings, null, tint = Color.White.copy(alpha = .62f), modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun B44V4ResourceChip(symbol: String, value: String, color: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = .10f))
            .border(1.dp, color.copy(alpha = .22f), RoundedCornerShape(16.dp))
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(25.dp).background(Brush.linearGradient(listOf(color, color.copy(alpha = .65f))), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) { Text(symbol, fontSize = 10.sp, color = Color.White) }
        Spacer(Modifier.width(6.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun B44V4CrosswordGrid(cells: List<B44V4Cell>, revealed: Set<Pair<Int, Int>>, theme: B44ThemeSpec) {
    if (cells.isEmpty()) return
    val minRow = cells.minOf { it.row }
    val maxRow = cells.maxOf { it.row }
    val minCol = cells.minOf { it.col }
    val maxCol = cells.maxOf { it.col }
    val rows = maxRow - minRow + 1
    val cols = maxCol - minCol + 1
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
        val gap = 7.dp
        val cellSize = minOf(58.dp, (maxWidth - gap * (cols - 1)) / cols)
        Column(verticalArrangement = Arrangement.spacedBy(gap), horizontalAlignment = Alignment.CenterHorizontally) {
            repeat(rows) { rr ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    repeat(cols) { cc ->
                        val row = rr + minRow
                        val col = cc + minCol
                        val cell = cells.firstOrNull { it.row == row && it.col == col }
                        if (cell == null) Spacer(Modifier.size(cellSize))
                        else B44V4GridCell(cell, cell.row to cell.col in revealed, theme, cellSize)
                    }
                }
            }
        }
    }
}

@Composable
private fun B44V4GridCell(cell: B44V4Cell, revealed: Boolean, theme: B44ThemeSpec, size: Dp) {
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 0f else -90f,
        animationSpec = spring(stiffness = 280f, dampingRatio = .78f),
        label = "cellFlip",
    )
    Box(
        Modifier.size(size).graphicsLayer { rotationY = rotation; cameraDistance = 12 * density }
            .shadow(if (revealed) 8.dp else 0.dp, RoundedCornerShape(15.dp), ambientColor = theme.accent.copy(alpha = .35f), spotColor = theme.accent.copy(alpha = .35f))
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (revealed) Brush.linearGradient(listOf(theme.cellStart, theme.cellEnd))
                else Brush.linearGradient(listOf(Color.White.copy(alpha = .07f), Color.White.copy(alpha = .025f)))
            )
            .border(1.dp, if (revealed) theme.lineColor.copy(alpha = .72f) else Color.White.copy(alpha = .10f), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (revealed) Text(cell.letter.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = (size.value * .40f).sp)
    }
}

@Composable
private fun B44V4ThemeButton(theme: B44ThemeSpec, click: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(Color.White.copy(alpha = .055f))
                .border(1.dp, theme.accent.copy(alpha = .25f), RoundedCornerShape(17.dp)).clickable(onClick = click),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Star, null, tint = theme.lineColor, modifier = Modifier.size(21.dp))
        }
        Text("TEMA", color = Color.White.copy(alpha = .30f), fontSize = 8.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun B44V4HintButton(hints: Int, coins: Int, click: () -> Unit) {
    val free = hints > 0
    Box(Modifier.size(62.dp)) {
        Box(
            Modifier.size(56.dp).align(Alignment.BottomStart).clip(RoundedCornerShape(18.dp))
                .background(
                    if (free) Brush.linearGradient(listOf(Color(0xFF9B51E0), Color(0xFF6B21A8)))
                    else Brush.linearGradient(listOf(Color(0xFF2D2D4E), Color(0xFF1E1E36)))
                )
                .border(1.dp, if (free) Color(0xFF9B51E0).copy(alpha = .45f) else Color.White.copy(alpha = .07f), RoundedCornerShape(18.dp))
                .clickable(onClick = click),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Lightbulb, null, tint = if (free) Color(0xFFFFF176) else Color.White.copy(alpha = .30f), modifier = Modifier.size(22.dp))
        }
        Box(
            Modifier.align(Alignment.TopEnd).height(20.dp).clip(CircleShape)
                .background(if (free) B44V2Colors.Gold else B44V2Colors.Red)
                .border(1.dp, B44V2Colors.Deep, CircleShape).padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (free) hints.toString() else if (coins >= 100) "◆" else "✗", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun B44V4LetterWheel(letters: List<Char>, theme: B44ThemeSpec, wheelSize: Dp, onSubmit: (String) -> Unit) {
    var shuffled by remember(letters) { mutableStateOf(letters) }
    var selected by remember(letters) { mutableStateOf<List<Int>>(emptyList()) }
    var dragging by remember { mutableStateOf(false) }
    var pointer by remember { mutableStateOf<Offset?>(null) }
    var wheelPx by remember { mutableStateOf(0 to 0) }
    val density = LocalDensity.current
    val centerDp = wheelSize.value / 2f
    val radiusDp = minOf(90f, centerDp - 34f)
    val current = selected.joinToString("") { shuffled[it].toString() }

    fun centersPx(): List<Offset> {
        if (shuffled.isEmpty()) return emptyList()
        val centerX = wheelPx.first / 2f
        val centerY = wheelPx.second / 2f
        val radiusPx = with(density) { radiusDp.dp.toPx() }
        return shuffled.indices.map { index ->
            val angle = (index.toDouble() / shuffled.size) * 2.0 * PI - PI / 2
            Offset(
                centerX + (radiusPx * cos(angle)).toFloat(),
                centerY + (radiusPx * sin(angle)).toFloat(),
            )
        }
    }

    fun findIndex(point: Offset): Int {
        val hit = with(density) { 30.dp.toPx() }
        return centersPx().indexOfFirst { center ->
            hypot((point.x - center.x).toDouble(), (point.y - center.y).toDouble()) < hit
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.height(42.dp), contentAlignment = Alignment.Center) {
            if (current.isBlank()) {
                Text("harfleri sürükle...", color = Color.White.copy(alpha = .25f), fontSize = 11.sp, letterSpacing = 1.sp)
            } else {
                Box(
                    Modifier.clip(RoundedCornerShape(15.dp)).background(theme.accent.copy(alpha = .32f))
                        .border(1.dp, theme.lineColor.copy(alpha = .50f), RoundedCornerShape(15.dp))
                        .padding(horizontal = 18.dp, vertical = 7.dp)
                ) {
                    Text(current, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 2.sp)
                }
            }
        }
        Box(
            Modifier.size(wheelSize).clip(CircleShape).background(Brush.linearGradient(listOf(theme.wheelStart, theme.wheelEnd)))
                .border(1.dp, theme.accent.copy(alpha = .16f), CircleShape)
                .onSizeChanged { wheelPx = it.width to it.height }
                .pointerInput(shuffled) {
                    detectDragGestures(
                        onDragStart = { point ->
                            val index = findIndex(point)
                            if (index >= 0) {
                                dragging = true
                                selected = listOf(index)
                                pointer = point
                            }
                        },
                        onDrag = { change, _ ->
                            if (dragging) {
                                pointer = change.position
                                val index = findIndex(change.position)
                                if (index >= 0 && index !in selected) selected = selected + index
                                change.consume()
                            }
                        },
                        onDragEnd = {
                            if (selected.size >= 2) onSubmit(selected.joinToString("") { shuffled[it].toString() })
                            dragging = false
                            selected = emptyList()
                            pointer = null
                        },
                        onDragCancel = {
                            dragging = false
                            selected = emptyList()
                            pointer = null
                        },
                    )
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(color = theme.accent.copy(alpha = .045f), radius = size.minDimension * .41f)
                drawCircle(color = theme.accent.copy(alpha = .12f), radius = size.minDimension * .41f, style = Stroke(width = 1.dp.toPx()))
                val centers = centersPx()
                if (selected.isNotEmpty() && centers.isNotEmpty()) {
                    val points = selected.map { centers[it] } + listOfNotNull(pointer.takeIf { dragging })
                    points.zipWithNext().forEach { (a, b) ->
                        drawLine(theme.lineColor, a, b, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                    }
                    selected.forEach { drawCircle(theme.lineColor, 6.dp.toPx(), centers[it]) }
                }
            }
            Box(
                Modifier.size(46.dp).align(Alignment.Center).clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(theme.accent, theme.lineColor)))
                    .clickable {
                        val list = shuffled.toMutableList()
                        for (index in list.lastIndex downTo 1) {
                            val swap = Random.nextInt(index + 1)
                            val tmp = list[index]
                            list[index] = list[swap]
                            list[swap] = tmp
                        }
                        shuffled = list
                        selected = emptyList()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Shuffle, null, tint = Color.White, modifier = Modifier.size(21.dp))
            }
            shuffled.forEachIndexed { index, char ->
                val angle = (index.toDouble() / shuffled.size) * 2.0 * PI - PI / 2
                val x = (centerDp + radiusDp * cos(angle) - 26).dp
                val y = (centerDp + radiusDp * sin(angle) - 26).dp
                val isSelected = index in selected
                val scale by animateFloatAsState(
                    if (isSelected) 1.22f else 1f,
                    spring(stiffness = 500f, dampingRatio = .72f),
                    label = "wheelLetter$index",
                )
                Box(
                    Modifier.offset(x, y).size(52.dp).scale(scale).clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) Brush.linearGradient(listOf(theme.accent, theme.lineColor))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(char.toString(), color = if (isSelected) Color.White else theme.wheelText, fontWeight = FontWeight.Black, fontSize = 19.sp)
                    if (isSelected) {
                        Box(
                            Modifier.size(16.dp).align(Alignment.TopEnd).background(B44V2Colors.Gold, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text((selected.indexOf(index) + 1).toString(), color = Color(0xFF1A0800), fontSize = 7.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun B44V4SettingsSheet(
    level: Int,
    sound: Boolean,
    music: Boolean,
    onSound: (Boolean) -> Unit,
    onMusic: (Boolean) -> Unit,
    close: () -> Unit,
    home: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .68f)).clickable(onClick = close),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(30.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF160832), Color(0xFF0A051A))))
                .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(30.dp))
                .clickable(enabled = false) {}.padding(20.dp).navigationBarsPadding()
        ) {
            Box(Modifier.width(48.dp).height(4.dp).background(Color.White.copy(alpha = .12f), CircleShape).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Ayarlar", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Bölüm $level", color = Color(0xFF9B51E0).copy(alpha = .75f), fontSize = 10.sp)
                }
                Text("✕", color = Color.White.copy(alpha = .50f), fontSize = 18.sp, modifier = Modifier.clickable(onClick = close).padding(8.dp))
            }
            Spacer(Modifier.height(14.dp))
            B44V4ToggleRow(Icons.Filled.VolumeUp, "Ses Efektleri", sound, onSound)
            Spacer(Modifier.height(9.dp))
            B44V4ToggleRow(Icons.Filled.MusicNote, "Müzik", music, onMusic)
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp))
                    .background(B44V2Colors.Red.copy(alpha = .10f))
                    .border(1.dp, B44V2Colors.Red.copy(alpha = .20f), RoundedCornerShape(16.dp))
                    .clickable(onClick = home),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.Home, null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ana Menüye Dön", color = Color(0xFFFCA5A5), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text("v0.21.0 · Words of Wonders", color = Color.White.copy(alpha = .18f), fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun B44V4ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .04f))
            .border(1.dp, Color.White.copy(alpha = .07f), RoundedCornerShape(16.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).background(Color(0xFF7B3FC4).copy(alpha = .15f), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(label, color = Color.White.copy(alpha = .68f), fontSize = 12.sp, modifier = Modifier.weight(1f))
        Box(
            Modifier.width(48.dp).height(26.dp).clip(CircleShape)
                .background(
                    if (value) Brush.horizontalGradient(listOf(Color(0xFF9B51E0), Color(0xFF6B21A8)))
                    else Brush.horizontalGradient(listOf(Color.White.copy(alpha = .08f), Color.White.copy(alpha = .08f)))
                ).clickable { onChange(!value) }
        ) {
            val knobX by animateFloatAsState(if (value) 23f else 3f, spring(stiffness = 500f, dampingRatio = .82f), label = "toggleKnob")
            Box(Modifier.offset(x = knobX.dp, y = 3.dp).size(20.dp).background(Color.White, CircleShape))
        }
    }
}

@Composable
private fun B44V4ThemeStore(
    coins: Int,
    activeThemeId: String,
    ownedThemes: Set<String>,
    onBuyOrApply: (B44ThemeSpec) -> Boolean,
    close: () -> Unit,
) {
    var feedback by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(2400)
            feedback = null
        }
    }
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .72f)).clickable(onClick = close),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier.fillMaxWidth().height(620.dp).clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF180B34), Color(0xFF090515))))
                .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .clickable(enabled = false) {}.padding(18.dp).navigationBarsPadding()
        ) {
            Box(Modifier.width(48.dp).height(4.dp).background(Color.White.copy(alpha = .12f), CircleShape).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tema Mağazası", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Oyun atmosferini değiştir", color = Color.White.copy(alpha = .36f), fontSize = 10.sp)
                }
                Box(
                    Modifier.clip(CircleShape).background(B44V2Colors.Gold.copy(alpha = .12f))
                        .border(1.dp, B44V2Colors.Gold.copy(alpha = .25f), CircleShape).padding(horizontal = 11.dp, vertical = 7.dp)
                ) {
                    Text("◆ $coins", color = B44V2Colors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(8.dp))
                Text("✕", color = Color.White.copy(alpha = .50f), fontSize = 18.sp, modifier = Modifier.clickable(onClick = close).padding(7.dp))
            }
            AnimatedVisibility(feedback != null, enter = fadeIn(), exit = fadeOut()) {
                feedback?.let { message ->
                    Box(
                        Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(13.dp))
                            .background((if (message.second) B44V2Colors.Emerald else B44V2Colors.Red).copy(alpha = .12f))
                            .border(1.dp, (if (message.second) B44V2Colors.Emerald else B44V2Colors.Red).copy(alpha = .28f), RoundedCornerShape(13.dp))
                            .padding(10.dp)
                    ) {
                        Text(message.first, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                b44ThemeSpecs.chunked(2).forEach { rowThemes ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowThemes.forEach { spec ->
                            B44V4ThemeCard(
                                spec = spec,
                                owned = spec.id in ownedThemes,
                                active = spec.id == activeThemeId,
                                action = {
                                    val alreadyOwned = spec.id in ownedThemes
                                    val ok = onBuyOrApply(spec)
                                    feedback = when {
                                        ok && alreadyOwned -> "${spec.name} uygulandı" to true
                                        ok -> "${spec.name} satın alındı ve uygulandı" to true
                                        else -> "Yeterli altının yok" to false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowThemes.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun B44V4ThemeCard(
    spec: B44ThemeSpec,
    owned: Boolean,
    active: Boolean,
    action: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .035f))
            .border(if (active) 2.dp else 1.dp, if (active) spec.accent else Color.White.copy(alpha = .08f), RoundedCornerShape(18.dp))
            .padding(10.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().height(82.dp).clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(spec.bgStart, spec.bgMid, spec.bgEnd))),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(58.dp).background(Brush.radialGradient(listOf(spec.accent.copy(alpha = .30f), Color.Transparent)), CircleShape))
            Text(spec.icon, fontSize = 28.sp)
            if (active) {
                Box(Modifier.align(Alignment.TopEnd).padding(7.dp).size(20.dp).background(spec.accent, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(spec.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text(spec.description, color = Color.White.copy(alpha = .34f), fontSize = 8.sp, lineHeight = 10.sp, minLines = 2)
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(12.dp))
                .background(if (active) spec.accent.copy(alpha = .14f) else Color.White.copy(alpha = .06f))
                .border(1.dp, if (active) spec.accent.copy(alpha = .28f) else Color.White.copy(alpha = .08f), RoundedCornerShape(12.dp))
                .clickable(onClick = action),
            contentAlignment = Alignment.Center,
        ) {
            val label = when {
                active -> "✓ Aktif"
                owned -> "Uygula"
                else -> "🔒 ${spec.price} ◆"
            }
            Text(label, color = if (active) spec.accent else Color.White.copy(alpha = .70f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun B44V4ResultOverlay(
    level: Int,
    oldXp: Int,
    xpGained: Int,
    coinsGained: Int,
    speedBonus: Int,
    replay: Boolean,
    next: () -> Unit,
    home: () -> Unit,
) {
    val newXp = oldXp + xpGained
    val oldPct = ((oldXp % 500) / 500f).coerceIn(0f, 1f)
    val newPct = ((newXp % 500) / 500f).coerceIn(0f, 1f)
    val didLevelUp = newXp / 500 > oldXp / 500
    var xpTarget by remember { mutableStateOf(oldPct) }
    var showLevelUp by remember { mutableStateOf(false) }
    var entered by remember { mutableStateOf(false) }
    val trophyScale by animateFloatAsState(if (entered) 1f else .2f, spring(stiffness = 260f, dampingRatio = .65f), label = "trophy")
    val xpProgress by animateFloatAsState(xpTarget, tween(650), label = "resultXp")

    LaunchedEffect(Unit) {
        entered = true
        delay(700)
        if (didLevelUp) {
            xpTarget = 1f
            delay(900)
            showLevelUp = true
            xpTarget = newPct
        } else {
            xpTarget = newPct
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF06020F), Color(0xFF130830), Color(0xFF060E1F))))
    ) {
        val ambient = rememberInfiniteTransition(label = "resultAmbient")
        val glowScale by ambient.animateFloat(1f, 1.28f, infiniteRepeatable(tween(3000), RepeatMode.Reverse), label = "glow")
        Box(
            Modifier.align(Alignment.Center).size(520.dp).scale(glowScale)
                .background(Brush.radialGradient(listOf(B44V2Colors.Gold.copy(alpha = .09f), Color(0xFF7B3FC4).copy(alpha = .12f), Color.Transparent)), CircleShape)
        )
        repeat(18) { index ->
            val seed = remember(index) { Random(index * 91 + 7) }
            val x = remember(index) { seed.nextFloat() }
            val width = remember(index) { (3 + seed.nextInt(6)).dp }
            val height = remember(index) { (2 + seed.nextInt(5)).dp }
            val colors = listOf(Color(0xFFF5C842), Color(0xFF9B51E0), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF43F5E))
            val transition = rememberInfiniteTransition(label = "confetti$index")
            val fall by transition.animateFloat(
                0f, 1f,
                infiniteRepeatable(tween(2200 + index * 35, delayMillis = index * 45, easing = LinearEasing), RepeatMode.Restart),
                label = "fall$index",
            )
            Box(
                Modifier.offset(x = maxWidth * x, y = maxHeight * fall - 16.dp).size(width, height)
                    .alpha(.75f).background(colors[index % colors.size], RoundedCornerShape(2.dp))
            )
        }

        Column(
            Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(82.dp).scale(trophyScale)
                    .shadow(28.dp, RoundedCornerShape(24.dp), ambientColor = B44V2Colors.Gold.copy(alpha = .50f), spotColor = B44V2Colors.Gold.copy(alpha = .50f))
                    .clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(B44V2Colors.Gold, Color(0xFFC8911F)))),
                contentAlignment = Alignment.Center,
            ) { Text("🏆", fontSize = 42.sp) }
            Spacer(Modifier.height(16.dp))
            Text("Harika!", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
            Text("Bölüm $level tamamlandı", color = Color.White.copy(alpha = .42f), fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Row { repeat(3) { Text("★", color = B44V2Colors.Gold, fontSize = 30.sp, modifier = Modifier.padding(horizontal = 3.dp)) } }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                B44V4RewardCard("⚡", "+$xpGained XP", B44V2Colors.Emerald, Modifier.weight(1f))
                B44V4RewardCard("◆", "+$coinsGained Altın", B44V2Colors.Gold, Modifier.weight(1f))
            }
            if (speedBonus > 0) {
                Spacer(Modifier.height(10.dp))
                B44V4RewardCard("🔥", "Hız Bonusu! +$speedBonus XP", B44V2Colors.Orange, Modifier.fillMaxWidth())
            }
            if (replay) {
                Spacer(Modifier.height(9.dp))
                Text("Tekrar oynama · yeni ödül yok", color = Color.White.copy(alpha = .30f), fontSize = 10.sp)
            }
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color.White.copy(alpha = .045f))
                    .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(17.dp)).padding(14.dp)
            ) {
                val shownLevel = newXp / 500 + 1
                Row(Modifier.fillMaxWidth()) {
                    Text("Seviye $shownLevel", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${newXp % 500}/500 XP", color = Color.White.copy(alpha = .38f), fontSize = 10.sp)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(Color.White.copy(alpha = .08f))) {
                    Box(Modifier.fillMaxWidth(xpProgress.coerceIn(0f, 1f)).height(7.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, B44V2Colors.Gold))))
                }
                AnimatedVisibility(showLevelUp) {
                    Text("🎉 SEVİYE ATLADIN!  Lv.$shownLevel", color = B44V2Colors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF9B51E0), Color(0xFF7B2FBE), Color(0xFF5A189A))))
                    .clickable(onClick = next),
                contentAlignment = Alignment.Center,
            ) {
                Text("Bölüm ${level + 1}  →", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("Ana Menü", color = Color.White.copy(alpha = .33f), fontSize = 10.sp, modifier = Modifier.clickable(onClick = home).padding(8.dp))
        }
    }
}

@Composable
private fun B44V4RewardCard(icon: String, text: String, tint: Color, modifier: Modifier) {
    Row(
        modifier.height(58.dp).clip(RoundedCornerShape(16.dp)).background(tint.copy(alpha = .08f))
            .border(1.dp, tint.copy(alpha = .18f), RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(icon, fontSize = 17.sp)
        Spacer(Modifier.width(7.dp))
        Text(text, color = tint, fontWeight = FontWeight.Black, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
