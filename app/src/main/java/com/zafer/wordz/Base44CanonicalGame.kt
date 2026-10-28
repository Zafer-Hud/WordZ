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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
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

private const val ACTIVE_THEME_KEY = "b44_active_theme"
private const val OWNED_THEMES_KEY = "b44_owned_themes"
private const val SOUND_KEY = "sound_enabled"
private const val MUSIC_KEY = "music_enabled"

private data class CanonTheme(
    val id: String,
    val name: String,
    val icon: String,
    val price: Int,
    val bg: List<Color>,
    val accent: Color,
    val line: Color,
    val wheel: List<Color>,
    val wheelText: Color,
    val cell: List<Color>,
)

private val canonThemes = listOf(
    CanonTheme(
        "default", "Kozmik Mor", "🌌", 0,
        listOf(Color(0xFF06020F), Color(0xFF1C0E40), Color(0xFF0A1628)),
        Color(0xFF7B3FC4), Color(0xFF9B51E0),
        listOf(Color(0xFFFAFAF8), Color(0xFFEDEDF2)), Color(0xFF1E0B3C),
        listOf(Color(0xFF9B51E0), Color(0xFF5A189A)),
    ),
    CanonTheme(
        "nostalji", "Nostalji", "📜", 300,
        listOf(Color(0xFF1A0E08), Color(0xFF2C1810), Color(0xFF1A1208)),
        Color(0xFFC8911F), Color(0xFFD4A96A),
        listOf(Color(0xFFF5ECD5), Color(0xFFE8D5A8)), Color(0xFF432A18),
        listOf(Color(0xFFC8911F), Color(0xFF7A5010)),
    ),
    CanonTheme(
        "pastel", "Pastel Dünya", "🌸", 300,
        listOf(Color(0xFF120920), Color(0xFF1E0F35), Color(0xFF120E22)),
        Color(0xFFD06BA8), Color(0xFFEC8FBD),
        listOf(Color(0xFFFFF0F8), Color(0xFFF0E8FF)), Color(0xFF3A1738),
        listOf(Color(0xFFD06BA8), Color(0xFF86327A)),
    ),
    CanonTheme(
        "gece", "Gece Modu", "🌙", 500,
        listOf(Color.Black, Color(0xFF080808), Color(0xFF050510)),
        Color(0xFF00A876), Color(0xFF00C896),
        listOf(Color(0xFF1A1A1A), Color(0xFF111111)), Color(0xFFA0FFC8),
        listOf(Color(0xFF00A876), Color(0xFF006040)),
    ),
)

@Composable
internal fun Base44CanonicalGameScreen(
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
    val progressStore = remember { Base44ProgressStore(prefs) }
    val canonical = Base44RuntimeLevels.canonicalLevels[levelIndex]
    val targetWords = canonical.wordsToFind.map { it.word }
    val country = base44CountryForLevel(canonical.levelNumber)
    val wheelLetters = remember(canonical.levelNumber) { deriveBase44WheelLetters(targetWords) }
    val startXp = remember(levelIndex) { playerStore.snapshot().totalXp }
    val wasCompletedBefore = remember(levelIndex) { completedBefore }

    var soundEnabled by remember { mutableStateOf(prefs.getBoolean(SOUND_KEY, true)) }
    var musicEnabled by remember { mutableStateOf(prefs.getBoolean(MUSIC_KEY, true)) }
    var activeThemeId by remember { mutableStateOf(prefs.getString(ACTIVE_THEME_KEY, "default") ?: "default") }
    var ownedThemes by remember {
        mutableStateOf((prefs.getStringSet(OWNED_THEMES_KEY, setOf("default")) ?: setOf("default")).toSet() + "default")
    }
    val theme = canonThemes.firstOrNull { it.id == activeThemeId } ?: canonThemes.first()

    var found by remember(levelIndex) { mutableStateOf<Set<String>>(emptySet()) }
    var revealed by remember(levelIndex) { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) }
    var banner by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showThemes by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var rewardXp by remember { mutableIntStateOf(0) }
    var rewardCoins by remember { mutableIntStateOf(0) }
    var speedBonus by remember { mutableIntStateOf(0) }
    var finished by remember(levelIndex) { mutableStateOf(false) }
    var wrongPulse by remember { mutableIntStateOf(0) }
    val startedAt = remember(levelIndex) { System.currentTimeMillis() }
    val shake = remember { Animatable(0f) }

    DisposableEffect(Unit) { onDispose { audio.release() } }
    LaunchedEffect(soundEnabled) { audio.setEffectsEnabled(soundEnabled) }
    LaunchedEffect(musicEnabled) { audio.setMusicEnabled(musicEnabled) }

    fun finishIfNeeded(newFound: Set<String>) {
        if (finished || newFound.size < targetWords.size) return
        finished = true
        val reward = Base44ProgressStore.rewardFor(System.currentTimeMillis() - startedAt, wasCompletedBefore)
        rewardXp = reward.xp
        rewardCoins = reward.coins
        speedBonus = reward.speedBonus
        if (reward.coins > 0) onCoins(coins + reward.coins)
        if (reward.xp > 0) onRewardXp(reward.xp)
        if (reward.score > 0) progressStore.addScore(reward.score)
        onComplete()
        audio.play(WordZAudio.Fx.COMPLETE)
        showResult = true
    }

    fun submit(raw: String) {
        if (raw.length < 2 || finished) return
        val word = raw.uppercase(Locale("tr", "TR"))
        val target = canonical.wordsToFind.firstOrNull { it.word == word }
        if (target != null && word !in found) {
            revealed = revealed + target.cells.map { it.row to it.col }
            val nextFound = found + word
            found = nextFound
            banner = word
            audio.play(WordZAudio.Fx.CORRECT)
            finishIfNeeded(nextFound)
        } else {
            banner = null
            wrongPulse += 1
            audio.play(WordZAudio.Fx.WRONG)
        }
    }

    LaunchedEffect(banner) {
        if (banner != null) {
            delay(1500)
            banner = null
        }
    }
    LaunchedEffect(wrongPulse) {
        if (wrongPulse == 0) return@LaunchedEffect
        shake.snapTo(0f)
        listOf(-8f, 8f, -7f, 7f, 0f).forEach { shake.animateTo(it, tween(70)) }
    }

    CanonGameBackground(theme) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CanonGameHeader(coins, hints) { showSettings = true }
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
                        Text("Bölüm ${canonical.levelNumber}", color = Color.White.copy(alpha = .58f), fontSize = 9.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.offset(x = shake.value.dp)) {
                    CanonCrosswordGrid(canonical, revealed, theme)
                }
                Spacer(Modifier.height(10.dp))
                AnimatedVisibility(
                    visible = banner != null,
                    enter = fadeIn() + scaleIn(initialScale = .7f),
                    exit = fadeOut() + scaleOut(targetScale = .85f),
                ) {
                    Box(
                        Modifier.clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(theme.accent.copy(alpha = .55f), theme.accent.copy(alpha = .18f))))
                            .border(1.dp, theme.line.copy(alpha = .62f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("✦ ${banner.orEmpty()}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(7.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    targetWords.forEach { word ->
                        Box(
                            Modifier.padding(horizontal = 3.dp).clip(CircleShape)
                                .background(if (word in found) theme.accent.copy(alpha = .18f) else Color.White.copy(alpha = .04f))
                                .border(1.dp, if (word in found) theme.line.copy(alpha = .35f) else Color.White.copy(alpha = .07f), CircleShape)
                                .padding(horizontal = 9.dp, vertical = 5.dp)
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
                        CanonThemeButton(theme) { showThemes = true }
                        CanonLetterWheel(wheelLetters, theme, wheelSize, ::submit)
                        CanonHintButton(hints, coins) {
                            val canUse = when {
                                hints > 0 -> { onHints(hints - 1); true }
                                coins >= Base44ProgressStore.HINT_COIN_COST -> {
                                    onCoins(coins - Base44ProgressStore.HINT_COIN_COST); true
                                }
                                else -> false
                            }
                            if (!canUse) {
                                GameToastCenter.error("İpucu için yeterli hak veya altın yok.")
                            } else {
                                val target = canonical.wordsToFind.firstOrNull { it.word !in found }
                                val position = target?.cells?.firstOrNull { (it.row to it.col) !in revealed }
                                if (target != null && position != null) {
                                    val nextRevealed = revealed + (position.row to position.col)
                                    revealed = nextRevealed
                                    if (target.cells.all { (it.row to it.col) in nextRevealed }) {
                                        val nextFound = found + target.word
                                        found = nextFound
                                        banner = target.word
                                        finishIfNeeded(nextFound)
                                    }
                                    audio.play(WordZAudio.Fx.SELECT)
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
                CanonSettingsSheet(
                    sound = soundEnabled,
                    music = musicEnabled,
                    onSound = {
                        soundEnabled = it
                        prefs.edit().putBoolean(SOUND_KEY, it).apply()
                    },
                    onMusic = {
                        musicEnabled = it
                        prefs.edit().putBoolean(MUSIC_KEY, it).apply()
                    },
                    close = { showSettings = false },
                    home = home,
                )
            }

            AnimatedVisibility(
                visible = showThemes,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            ) {
                CanonThemeSheet(
                    coins = coins,
                    active = activeThemeId,
                    owned = ownedThemes,
                    onSelect = { selected ->
                        when {
                            selected.id in ownedThemes -> {
                                activeThemeId = selected.id
                                prefs.edit().putString(ACTIVE_THEME_KEY, selected.id).apply()
                                GameToastCenter.success("${selected.name} uygulandı.")
                            }
                            coins >= selected.price -> {
                                val nextOwned = ownedThemes + selected.id
                                ownedThemes = nextOwned
                                activeThemeId = selected.id
                                onCoins(coins - selected.price)
                                prefs.edit().putStringSet(OWNED_THEMES_KEY, nextOwned).putString(ACTIVE_THEME_KEY, selected.id).apply()
                                audio.play(WordZAudio.Fx.REWARD)
                                GameToastCenter.success("${selected.name} satın alındı.")
                            }
                            else -> GameToastCenter.error("Yetersiz altın.")
                        }
                    },
                    close = { showThemes = false },
                )
            }

            if (showResult) {
                CanonResultOverlay(
                    levelNumber = canonical.levelNumber,
                    oldXp = startXp,
                    xp = rewardXp,
                    coins = rewardCoins,
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
private fun CanonGameBackground(theme: CanonTheme, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(theme.bg))) {
        val transition = rememberInfiniteTransition(label = "gameAmbient")
        val pulse by transition.animateFloat(1f, 1.16f, infiniteRepeatable(tween(5200), RepeatMode.Reverse), label = "gamePulse")
        Box(
            Modifier.size(420.dp).offset((-120).dp, (-120).dp).scale(pulse)
                .background(Brush.radialGradient(listOf(theme.accent.copy(alpha = .18f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.align(Alignment.BottomEnd).size(360.dp).offset(95.dp, 75.dp)
                .background(Brush.radialGradient(listOf(theme.line.copy(alpha = .10f), Color.Transparent)), CircleShape)
        )
        content()
    }
}

@Composable
private fun CanonGameHeader(coins: Int, hints: Int, settings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        CanonResourceChip("◆", coins.toString(), B44V2Colors.Gold)
        Spacer(Modifier.width(8.dp))
        CanonResourceChip("💡", hints.toString(), B44V2Colors.Emerald)
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .06f))
                .border(1.dp, Color.White.copy(alpha = .10f), RoundedCornerShape(14.dp)).clickable(onClick = settings),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Settings, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(19.dp)) }
    }
}

@Composable
private fun CanonResourceChip(symbol: String, value: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(16.dp)).background(tint.copy(alpha = .10f))
            .border(1.dp, tint.copy(alpha = .22f), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(symbol, color = tint, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CanonCrosswordGrid(level: Base44WordLevel, revealed: Set<Pair<Int, Int>>, theme: CanonTheme) {
    if (level.gridCells.isEmpty()) return
    val minRow = level.gridCells.minOf { it.row }
    val maxRow = level.gridCells.maxOf { it.row }
    val minCol = level.gridCells.minOf { it.col }
    val maxCol = level.gridCells.maxOf { it.col }
    val rows = maxRow - minRow + 1
    val cols = maxCol - minCol + 1
    val byCell = remember(level.levelNumber) { level.gridCells.associateBy { it.row to it.col } }
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
        val gap = 7.dp
        val cellSize = minOf(58.dp, (maxWidth - gap * (cols - 1)) / cols)
        Column(verticalArrangement = Arrangement.spacedBy(gap), horizontalAlignment = Alignment.CenterHorizontally) {
            repeat(rows) { rr ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    repeat(cols) { cc ->
                        val key = (rr + minRow) to (cc + minCol)
                        val cell = byCell[key]
                        if (cell == null) Spacer(Modifier.size(cellSize))
                        else CanonGridCell(cell.letter, key in revealed, theme, cellSize)
                    }
                }
            }
        }
    }
}

@Composable
private fun CanonGridCell(letter: Char, revealed: Boolean, theme: CanonTheme, size: Dp) {
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 0f else -90f,
        animationSpec = spring(stiffness = 280f, dampingRatio = .78f),
        label = "canonicalCellFlip",
    )
    Box(
        Modifier.size(size).graphicsLayer { rotationY = rotation; cameraDistance = 12 * density }
            .shadow(if (revealed) 9.dp else 0.dp, RoundedCornerShape(15.dp), ambientColor = theme.accent.copy(alpha = .35f), spotColor = theme.accent.copy(alpha = .35f))
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (revealed) Brush.linearGradient(theme.cell)
                else Brush.linearGradient(listOf(Color.White.copy(alpha = .07f), Color.White.copy(alpha = .025f)))
            )
            .border(1.dp, if (revealed) theme.line.copy(alpha = .72f) else Color.White.copy(alpha = .10f), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (revealed) Text(letter.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = (size.value * .40f).sp)
    }
}

@Composable
private fun CanonThemeButton(theme: CanonTheme, click: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(Color.White.copy(alpha = .055f))
                .border(1.dp, theme.accent.copy(alpha = .25f), RoundedCornerShape(17.dp)).clickable(onClick = click),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Filled.Star, null, tint = theme.line, modifier = Modifier.size(21.dp)) }
        Text("TEMA", color = Color.White.copy(alpha = .30f), fontSize = 8.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun CanonHintButton(hints: Int, coins: Int, click: () -> Unit) {
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
        ) { Icon(Icons.Filled.Lightbulb, null, tint = if (free) Color(0xFFFFF176) else Color.White.copy(alpha = .30f), modifier = Modifier.size(22.dp)) }
        Box(
            Modifier.align(Alignment.TopEnd).height(20.dp).clip(CircleShape)
                .background(if (free) B44V2Colors.Gold else B44V2Colors.Red)
                .border(1.dp, B44V2Colors.Deep, CircleShape).padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (free) hints.toString() else if (coins >= Base44ProgressStore.HINT_COIN_COST) "◆" else "✗", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CanonLetterWheel(letters: List<Char>, theme: CanonTheme, wheelSize: Dp, onSubmit: (String) -> Unit) {
    var shuffled by remember(letters) { mutableStateOf(letters) }
    var selected by remember(letters) { mutableStateOf<List<Int>>(emptyList()) }
    var wheelPx by remember { mutableStateOf(0 to 0) }
    val density = LocalDensity.current
    val centerDp = wheelSize.value / 2f
    val radiusDp = minOf(90f, centerDp - 34f)
    val current = selected.joinToString("") { shuffled[it].toString() }

    fun centersPx(): List<Offset> {
        if (shuffled.isEmpty()) return emptyList()
        val cx = wheelPx.first / 2f
        val cy = wheelPx.second / 2f
        val radius = with(density) { radiusDp.dp.toPx() }
        return shuffled.indices.map { index ->
            val angle = index.toDouble() / shuffled.size * 2.0 * PI - PI / 2
            Offset(cx + (radius * cos(angle)).toFloat(), cy + (radius * sin(angle)).toFloat())
        }
    }
    fun hit(point: Offset): Int {
        val hitRadius = with(density) { 30.dp.toPx() }
        return centersPx().indexOfFirst { c -> hypot((point.x - c.x).toDouble(), (point.y - c.y).toDouble()) < hitRadius }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.height(42.dp), contentAlignment = Alignment.Center) {
            if (current.isBlank()) Text("harfleri sürükle...", color = Color.White.copy(alpha = .25f), fontSize = 11.sp, letterSpacing = 1.sp)
            else Box(
                Modifier.clip(RoundedCornerShape(15.dp)).background(theme.accent.copy(alpha = .32f))
                    .border(1.dp, theme.line.copy(alpha = .50f), RoundedCornerShape(15.dp))
                    .padding(horizontal = 18.dp, vertical = 7.dp)
            ) { Text(current, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 2.sp) }
        }
        Box(
            Modifier.size(wheelSize).clip(CircleShape).background(Brush.radialGradient(theme.wheel))
                .border(1.dp, Color.White.copy(alpha = .24f), CircleShape)
                .shadow(20.dp, CircleShape)
                .onSizeChanged { wheelPx = it.width to it.height }
                .pointerInput(shuffled) {
                    detectDragGestures(
                        onDragStart = { point ->
                            val index = hit(point)
                            if (index >= 0) selected = listOf(index)
                        },
                        onDrag = { change, _ ->
                            val index = hit(change.position)
                            if (index >= 0 && index !in selected) selected = selected + index
                        },
                        onDragEnd = {
                            if (selected.size >= 2) onSubmit(selected.joinToString("") { shuffled[it].toString() })
                            selected = emptyList()
                        },
                        onDragCancel = { selected = emptyList() },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            val radius = radiusDp.dp
            Canvas(Modifier.fillMaxSize()) {
                if (selected.size >= 2) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val r = radius.toPx()
                    val centers = shuffled.indices.map { index ->
                        val angle = index.toDouble() / shuffled.size * 2.0 * PI - PI / 2
                        Offset(c.x + (r * cos(angle)).toFloat(), c.y + (r * sin(angle)).toFloat())
                    }
                    selected.zipWithNext().forEach { (a, b) ->
                        drawLine(theme.line, centers[a], centers[b], strokeWidth = 7f, cap = StrokeCap.Round)
                    }
                }
            }
            shuffled.forEachIndexed { index, char ->
                val angle = index.toDouble() / shuffled.size.coerceAtLeast(1) * 2.0 * PI - PI / 2
                val x = (radiusDp * cos(angle)).toFloat().dp
                val y = (radiusDp * sin(angle)).toFloat().dp
                val isSelected = index in selected
                Box(
                    Modifier.offset(x, y).size(52.dp).scale(if (isSelected) 1.20f else 1f)
                        .shadow(if (isSelected) 10.dp else 2.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Brush.linearGradient(listOf(theme.accent, theme.line)) else Brush.linearGradient(theme.wheel))
                        .border(1.dp, if (isSelected) theme.line else Color.Black.copy(alpha = .08f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(char.toString(), color = if (isSelected) Color.White else theme.wheelText, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    if (isSelected) {
                        val order = selected.indexOf(index) + 1
                        Box(Modifier.align(Alignment.TopEnd).size(16.dp).background(B44V2Colors.Gold, CircleShape), contentAlignment = Alignment.Center) {
                            Text(order.toString(), color = Color(0xFF3A2500), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Box(
                Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = .72f))
                    .border(1.dp, Color.Black.copy(alpha = .08f), CircleShape)
                    .clickable {
                        shuffled = shuffled.shuffled()
                        selected = emptyList()
                    },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.Shuffle, null, tint = theme.accent, modifier = Modifier.size(21.dp)) }
        }
    }
}

@Composable
private fun CanonSettingsSheet(
    sound: Boolean,
    music: Boolean,
    onSound: (Boolean) -> Unit,
    onMusic: (Boolean) -> Unit,
    close: () -> Unit,
    home: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .68f)).clickable(onClick = close), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF17102C), Color(0xFF090716))))
                .border(1.dp, Color.White.copy(alpha = .09f), RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .clickable(enabled = false) {}.padding(22.dp).navigationBarsPadding()
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).width(46.dp).height(4.dp).background(Color.White.copy(alpha = .12f), CircleShape))
            Spacer(Modifier.height(18.dp))
            Text("Ayarlar", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            CanonToggleRow(Icons.Filled.VolumeUp, "Ses Efektleri", sound, onSound)
            Spacer(Modifier.height(10.dp))
            CanonToggleRow(Icons.Filled.MusicNote, "Müzik", music, onMusic)
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(15.dp))
                    .background(B44V2Colors.Red.copy(alpha = .10f)).border(1.dp, B44V2Colors.Red.copy(alpha = .22f), RoundedCornerShape(15.dp))
                    .clickable(onClick = home).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Home, null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Ana Menüye Dön", color = Color(0xFFF87171), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("v${BuildConfig.VERSION_NAME} · Words of Wonders", color = Color.White.copy(alpha = .22f), fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun CanonToggleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: Boolean, change: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .05f))
            .border(1.dp, Color.White.copy(alpha = .07f), RoundedCornerShape(16.dp)).clickable { change(!value) }.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color(0xFFB794F4), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(11.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Box(
            Modifier.width(42.dp).height(24.dp).clip(CircleShape).background(if (value) Color(0xFF7B3FC4) else Color.White.copy(alpha = .12f)).padding(3.dp)
        ) {
            Box(Modifier.align(if (value) Alignment.CenterEnd else Alignment.CenterStart).size(18.dp).background(Color.White, CircleShape))
        }
    }
}

@Composable
private fun CanonThemeSheet(coins: Int, active: String, owned: Set<String>, onSelect: (CanonTheme) -> Unit, close: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .68f)).clickable(onClick = close), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF151026), Color(0xFF080714))))
                .border(1.dp, Color.White.copy(alpha = .09f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clickable(enabled = false) {}.padding(18.dp).navigationBarsPadding()
        ) {
            Text("Tema Mağazası", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("◆ $coins", color = B44V2Colors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            canonThemes.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { theme ->
                        val isOwned = theme.id in owned
                        val isActive = theme.id == active
                        Column(
                            Modifier.weight(1f).height(142.dp).clip(RoundedCornerShape(18.dp))
                                .background(Brush.verticalGradient(theme.bg))
                                .border(1.dp, if (isActive) theme.line else Color.White.copy(alpha = .10f), RoundedCornerShape(18.dp))
                                .clickable { onSelect(theme) }.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(theme.icon, fontSize = 28.sp)
                            Text(theme.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(7.dp))
                            Text(
                                when {
                                    isActive -> "AKTİF"
                                    isOwned -> "UYGULA"
                                    else -> "◆ ${theme.price}"
                                },
                                color = if (isActive) B44V2Colors.Emerald else if (isOwned) Color.White else B44V2Colors.Gold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun CanonResultOverlay(
    levelNumber: Int,
    oldXp: Int,
    xp: Int,
    coins: Int,
    speedBonus: Int,
    replay: Boolean,
    next: () -> Unit,
    home: () -> Unit,
) {
    val newXp = oldXp + xp
    val level = newXp / PlayerProgressStore.XP_PER_LEVEL + 1
    val progress = (newXp % PlayerProgressStore.XP_PER_LEVEL) / PlayerProgressStore.XP_PER_LEVEL.toFloat()
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B0620), Color(0xFF17103C), Color(0xFF07101E)))),
        contentAlignment = Alignment.Center,
    ) {
        val confetti = remember(levelNumber) {
            val random = Random(levelNumber * 71)
            List(30) { Triple(random.nextFloat(), random.nextFloat(), random.nextInt(8, 18)) }
        }
        confetti.forEachIndexed { index, item ->
            val transition = rememberInfiniteTransition(label = "confetti$index")
            val drift by transition.animateFloat(-8f, 8f, infiniteRepeatable(tween(1600 + index * 31), RepeatMode.Reverse), label = "drift$index")
            BoxWithConstraints(Modifier.fillMaxSize()) {
                Box(
                    Modifier.offset(maxWidth * item.first + drift.dp, maxHeight * item.second).size(item.third.dp, 3.dp)
                        .graphicsLayer { rotationZ = (index * 23f) % 180f }
                        .background(if (index % 2 == 0) B44V2Colors.Gold else B44V2Colors.Emerald, CircleShape)
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🏆", fontSize = 64.sp)
            Text(if (replay) "Tekrar Harika!" else "Harika!", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text("Bölüm $levelNumber tamamlandı", color = Color.White.copy(alpha = .45f), fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Text("★ ★ ★", color = B44V2Colors.Gold, fontSize = 27.sp, letterSpacing = 5.sp)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CanonRewardCard("⚡", "+$xp XP", B44V2Colors.Emerald, Modifier.weight(1f))
                CanonRewardCard("◆", "+$coins", B44V2Colors.Gold, Modifier.weight(1f))
                if (speedBonus > 0) CanonRewardCard("⏱", "+$speedBonus", B44V2Colors.Orange, Modifier.weight(1f))
            }
            Spacer(Modifier.height(18.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .06f))
                    .border(1.dp, Color.White.copy(alpha = .09f), RoundedCornerShape(18.dp)).padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Lv.$level", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("${newXp % 500}/500 XP", color = Color.White.copy(alpha = .4f), fontSize = 10.sp)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(7.dp).background(Color.White.copy(alpha = .08f), CircleShape)) {
                    Box(Modifier.fillMaxWidth(progress).height(7.dp).background(B44V2Colors.Emerald, CircleShape))
                }
            }
            Spacer(Modifier.height(22.dp))
            Box(
                Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(17.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF9B51E0), Color(0xFF6B21A8))))
                    .clickable(onClick = next),
                contentAlignment = Alignment.Center,
            ) { Text("Bölüm ${levelNumber + 1}  →", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp) }
            Spacer(Modifier.height(10.dp))
            Text("Ana Menü", color = Color.White.copy(alpha = .42f), fontSize = 11.sp, modifier = Modifier.clickable(onClick = home).padding(10.dp))
        }
    }
}

@Composable
private fun CanonRewardCard(icon: String, value: String, tint: Color, modifier: Modifier) {
    Column(
        modifier.height(82.dp).clip(RoundedCornerShape(17.dp)).background(tint.copy(alpha = .10f))
            .border(1.dp, tint.copy(alpha = .22f), RoundedCornerShape(17.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(icon, fontSize = 18.sp)
        Text(value, color = tint, fontWeight = FontWeight.Black, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}
