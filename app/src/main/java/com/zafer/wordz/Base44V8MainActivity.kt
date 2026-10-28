package com.zafer.wordz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class Base44V8MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validateAllLevels()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = B44V2Colors.Emerald,
                    secondary = B44V2Colors.Gold,
                    background = B44V2Colors.Deep,
                    surface = B44V2Colors.Mid,
                    onPrimary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White,
                )
            ) { WordZV8App() }
        }
    }
}

@Composable
private fun WordZV8App() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(B44V2_PREFS, Context.MODE_PRIVATE) }
    val state = remember { WordZAppState(prefs) }
    val profileStore = remember { Base44UserProfileStore(prefs) }
    val modeStore = remember { Base44ModeStore(prefs) }
    var splash by remember { mutableStateOf(true) }
    var screen by remember { mutableStateOf(B44V2Screen.HOME) }
    var mode by remember { mutableStateOf(Base44PlayMode.CLASSIC) }
    var session by remember { mutableIntStateOf(0) }

    fun openLevel(index: Int, playMode: Base44PlayMode) {
        if (!state.select(index)) {
            GameToastCenter.warning("Bu bölüm henüz açılmadı.")
            return
        }
        mode = playMode
        session++
        screen = B44V2Screen.GAME
    }

    fun openDaily() {
        val available = (state.unlocked + 1).coerceIn(1, state.levelCount)
        openLevel(modeStore.dailyLevelIndex(available), Base44PlayMode.DAILY)
    }

    Box(Modifier.fillMaxSize()) {
        when (screen) {
            B44V2Screen.HOME -> WordZHome(
                state = state,
                profile = profileStore.snapshot().copy(completedLevels = state.completed),
                modeStore = modeStore,
                classic = { openLevel(state.unlocked, Base44PlayMode.CLASSIC) },
                speed = { openLevel(state.unlocked, Base44PlayMode.SPEED) },
                daily = ::openDaily,
                tab = { screen = it },
            )
            B44V2Screen.MAP -> WordZWorlds(
                state = state,
                back = { screen = B44V2Screen.HOME },
                openLevel = { openLevel(it, Base44PlayMode.CLASSIC) },
                tab = { screen = it },
            )
            B44V2Screen.GAME -> {
                val available = (state.unlocked + 1).coerceIn(1, state.levelCount)
                val daily = modeStore.dailySnapshot(available)
                Box(Modifier.fillMaxSize(), key = session) {
                    Base44ModeGameScreen(
                        mode = mode,
                        levelIndex = state.selectedLevel,
                        coins = state.coins,
                        hints = state.hints,
                        completedBefore = state.selectedLevel in state.completed,
                        dailyStreak = daily.streak,
                        onCoins = state::setCoins,
                        onHints = state::setHints,
                        onRewardXp = state::grantXp,
                        onComplete = { elapsed ->
                            state.completeSelected()
                            when (mode) {
                                Base44PlayMode.CLASSIC -> GameToastCenter.success("Bölüm ${state.selectedLevel + 1} tamamlandı!")
                                Base44PlayMode.SPEED -> {
                                    val best = modeStore.recordSpeed(elapsed)
                                    GameToastCenter.success("Hız turu tamamlandı · En iyi ${formatSeconds(best)}")
                                }
                                Base44PlayMode.DAILY -> {
                                    val streak = modeStore.completeDaily()
                                    GameToastCenter.success("Günlük görev tamamlandı · 🔥 $streak")
                                }
                            }
                        },
                        back = { screen = if (mode == Base44PlayMode.CLASSIC) B44V2Screen.MAP else B44V2Screen.HOME },
                        next = {
                            if (mode != Base44PlayMode.CLASSIC) screen = B44V2Screen.HOME
                            else {
                                val next = state.selectedLevel + 1
                                if (state.canOpen(next)) openLevel(next, Base44PlayMode.CLASSIC) else screen = B44V2Screen.MAP
                            }
                        },
                        home = { screen = B44V2Screen.HOME },
                    )
                }
            }
            B44V2Screen.MARKET -> B44V5Market(state.coins, state.hints, state::setCoins, state::setHints) { screen = it }
            B44V2Screen.PROFILE -> WordZProfileScreen(state.coins, state.hints, state.player, state.completed) { screen = it }
        }

        GameToastHost(Modifier.align(Alignment.TopCenter))
        AnimatedVisibility(
            visible = splash,
            enter = fadeIn(),
            exit = fadeOut(tween(B44Ui.SplashExitMs)) + scaleOut(targetScale = 1.05f),
        ) { WordZSplash { splash = false } }
    }
}

private fun formatSeconds(ms: Long): String = String.format(java.util.Locale.US, "%.1f sn", ms / 1000f)

@Composable
private fun WordZSplash(done: () -> Unit) {
    var progress by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        repeat(50) { delay(38L); progress = (it + 1) * 2 }
        delay(400L)
        done()
    }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF040C18), Color(0xFF071420), Color(0xFF030A14)))),
        contentAlignment = Alignment.Center,
    ) {
        B44AmbientStars(seed = 144, count = 25)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("WORDS", color = Color.White, fontSize = 58.sp, lineHeight = 58.sp, fontWeight = FontWeight.Black)
            Text(
                "Z",
                color = B44V2Colors.Emerald,
                fontSize = 104.sp,
                lineHeight = 90.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.shadow(25.dp, CircleShape, ambientColor = B44V2Colors.Emerald.copy(alpha = .4f), spotColor = B44V2Colors.Emerald.copy(alpha = .4f)),
            )
            Text("TÜRKÇE KELİME BULMACA", color = B44Ui.DimText, fontSize = 10.sp, letterSpacing = 2.2.sp)
            Spacer(Modifier.height(54.dp))
            Box(Modifier.width(208.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
                Box(
                    Modifier.fillMaxWidth((progress / 100f).coerceIn(0f, 1f)).height(4.dp)
                        .background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, B44V2Colors.Gold)))
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("YÜKLENİYOR", color = Color.White.copy(alpha = .25f), fontSize = 10.sp, letterSpacing = 3.sp)
        }
    }
}

@Composable
private fun WordZHome(
    state: WordZAppState,
    profile: Base44LocalProfile,
    modeStore: Base44ModeStore,
    classic: () -> Unit,
    speed: () -> Unit,
    daily: () -> Unit,
    tab: (B44V2Screen) -> Unit,
) {
    val dailySnapshot = modeStore.dailySnapshot((state.unlocked + 1).coerceIn(1, state.levelCount))
    val best = modeStore.bestSpeedMs()
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(180); entered = true }

    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = B44Ui.ScreenPadding).padding(bottom = 104.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    WordZChip("◆", state.coins.toString(), B44V2Colors.Gold)
                    WordZChip("⚡", "Lv.${state.player.level} · ${state.player.totalXp} XP", B44V2Colors.Emerald)
                }
                Spacer(Modifier.height(22.dp))
                AnimatedVisibility(entered, enter = fadeIn(tween(650)) + slideInVertically(tween(650), initialOffsetY = { 34 })) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TÜRKÇE KELİME OYUNU", color = B44V2Colors.Emerald.copy(alpha = .68f), fontSize = 10.sp, letterSpacing = 2.7.sp)
                        Text("WORDS", color = Color.White, fontSize = 52.sp, lineHeight = 50.sp, fontWeight = FontWeight.Black)
                        Text("Z", color = B44V2Colors.Emerald, fontSize = 86.sp, lineHeight = 74.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(16.dp))
                        WordZPlay(classic)
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.clip(CircleShape).background(B44Ui.Glass).border(1.dp, B44Ui.Border, CircleShape)
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val pulse = rememberInfiniteTransition(label = "active")
                            val a by pulse.animateFloat(.35f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "dot")
                            Box(Modifier.size(8.dp).alpha(a).background(B44V2Colors.Emerald, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            val world = base44CountryForLevel(state.unlocked + 1)
                            Text("${world.emoji} ${world.name} · Bölüm ${state.unlocked + 1}", color = Color.White.copy(alpha = .64f), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
                Text("OYUN MODU", color = B44Ui.DimText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(9.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WordZMode("📖", "Klasik", "4 tema · 120 bölüm", B44V2Colors.Emerald, "Bölüm ${state.unlocked + 1}", classic, Modifier.weight(1f))
                    WordZMode("⚡", "Hız", "90 saniyelik yarış", B44V2Colors.Orange, if (best > 0) "En iyi ${formatSeconds(best)}" else "İlk rekorunu kır", speed, Modifier.weight(1f))
                    WordZMode("🏆", "Günlük", "Her gün yeni seçim", B44V2Colors.Gold, if (dailySnapshot.completedToday) "✓ Tamamlandı" else "🔥 ${dailySnapshot.streak} seri", daily, Modifier.weight(1f))
                }

                Spacer(Modifier.height(22.dp))
                WordZLeaderboard(profile)
                Spacer(Modifier.height(16.dp))
                Text("120 özgün Türkçe bölüm · Hayvanlar · Şehirler · Yemekler · Spor", color = B44Ui.DimText, fontSize = 9.sp, textAlign = TextAlign.Center)
            }
            WordZBottomNav(B44V2Screen.HOME, Modifier.align(Alignment.BottomCenter), tab)
        }
    }
}

@Composable
private fun WordZChip(symbol: String, value: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(16.dp)).background(B44Ui.GlassStrong)
            .border(1.dp, B44Ui.Border, RoundedCornerShape(16.dp)).padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(symbol, color = tint, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WordZPlay(click: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "play")
    val pulse by transition.animateFloat(.97f, 1.04f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "playScale")
    Box(Modifier.size(176.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(168.dp).alpha(.18f).border(1.dp, B44V2Colors.Emerald.copy(alpha = .32f), CircleShape))
        Column(
            Modifier.size(144.dp).scale(pulse).shadow(30.dp, CircleShape, ambientColor = B44V2Colors.Emerald.copy(alpha = .45f), spotColor = B44V2Colors.Emerald.copy(alpha = .45f))
                .clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF1AE896), B44V2Colors.Emerald, B44V2Colors.EmeraldDark)))
                .border(1.dp, Color.White.copy(alpha = .16f), CircleShape).clickable(onClick = click),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(42.dp))
            Text("OYNA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun WordZMode(emoji: String, title: String, description: String, tint: Color, footer: String, click: () -> Unit, modifier: Modifier) {
    Column(
        modifier.height(136.dp).clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(tint.copy(alpha = .17f), tint.copy(alpha = .04f))))
            .border(1.dp, tint.copy(alpha = .28f), RoundedCornerShape(18.dp)).clickable(onClick = click).padding(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = 23.sp)
        Text(title, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(description, color = B44Ui.MutedText, fontSize = 8.sp, textAlign = TextAlign.Center, lineHeight = 10.sp)
        Spacer(Modifier.height(6.dp))
        Text(footer, color = Color.White.copy(alpha = .28f), fontSize = 7.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WordZLeaderboard(profile: Base44LocalProfile) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(B44Ui.CardRadius)).background(B44Ui.Glass)
            .border(1.dp, B44Ui.Border, RoundedCornerShape(B44Ui.CardRadius))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", fontSize = 17.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Skorun", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Yerel oyuncu profili", color = B44Ui.DimText, fontSize = 8.sp)
            }
        }
        Row(Modifier.fillMaxWidth().background(B44V2Colors.Gold.copy(alpha = .05f)).padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🥇", fontSize = 14.sp, modifier = Modifier.width(30.dp))
            Box(Modifier.size(30.dp).background(B44V2Colors.Emerald.copy(alpha = .30f), CircleShape), contentAlignment = Alignment.Center) {
                Text(profile.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(profile.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Lv.${profile.playerLevel} · ${profile.completedLevels.size}/120 bölüm", color = Color.White.copy(alpha = .35f), fontSize = 9.sp)
            }
            Text(profile.score.toString(), color = B44V2Colors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WordZWorlds(state: WordZAppState, back: () -> Unit, openLevel: (Int) -> Unit, tab: (B44V2Screen) -> Unit) {
    var selected by remember { mutableStateOf<B44Country?>(null) }
    val activeWorld = Base44RuntimeLevels.canonicalLevels.getOrNull(state.unlocked)?.country

    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 92.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(B44Ui.SmallRadius)).background(B44Ui.GlassStrong)
                            .border(1.dp, B44Ui.BorderStrong, RoundedCornerShape(B44Ui.SmallRadius)).clickable(onClick = back),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Tema Dünyaları", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("4 tema · 120 Türkçe bölüm", color = B44Ui.MutedText, fontSize = 10.sp)
                    }
                    Text("${base44CanonicalCountries.count { base44CountryUnlocked(it.id, state.completed) }}/${base44CanonicalCountries.size}", color = B44V2Colors.Emerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 20.dp)) {
                    base44CanonicalCountries.forEachIndexed { index, world ->
                        val indices = base44CountryLevelIndices(world.id)
                        val done = base44CountryCompletedCount(world.id, state.completed)
                        val open = base44CountryUnlocked(world.id, state.completed)
                        WordZWorldCard(world, done, indices.size, open, activeWorld == world.id) { if (open) selected = world }
                        if (index < base44CanonicalCountries.lastIndex) Box(Modifier.padding(start = 25.dp).width(2.dp).height(22.dp).background(B44Ui.Border))
                    }
                }
            }
            WordZBottomNav(B44V2Screen.MAP, Modifier.align(Alignment.BottomCenter), tab)
            AnimatedVisibility(
                visible = selected != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            ) {
                selected?.let { world ->
                    WordZWorldSheet(world, state.completed, state.unlocked, { selected = null }) {
                        val indices = base44CountryLevelIndices(world.id)
                        val candidate = if (state.unlocked in indices && state.canOpen(state.unlocked)) state.unlocked else indices.firstOrNull()
                        if (candidate != null && state.canOpen(candidate)) openLevel(candidate)
                        else GameToastCenter.warning("Bu temada açılabilir bölüm yok.")
                        selected = null
                    }
                }
            }
        }
    }
}

@Composable
private fun WordZWorldCard(world: B44Country, done: Int, total: Int, open: Boolean, active: Boolean, click: () -> Unit) {
    val progress = if (total == 0) 0f else done / total.toFloat()
    Row(
        Modifier.fillMaxWidth().height(122.dp).clip(RoundedCornerShape(B44Ui.CardRadius))
            .background(Brush.linearGradient(if (active) listOf(world.accent.copy(alpha = .17f), B44Ui.Glass) else listOf(B44Ui.GlassStrong, Color.White.copy(alpha = .02f))))
            .border(1.dp, if (active) world.accent.copy(alpha = .40f) else B44Ui.Border, RoundedCornerShape(B44Ui.CardRadius))
            .alpha(if (open) 1f else .48f).clickable(enabled = open, onClick = click).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(58.dp).clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(world.accent.copy(alpha = .32f), world.accent.copy(alpha = .08f))))
                .border(1.dp, world.accent.copy(alpha = .34f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) { if (open) Text(world.emoji, fontSize = 29.sp) else Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = .45f)) }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(world.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                if (active) {
                    Spacer(Modifier.width(8.dp))
                    Text("● Aktif", color = world.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("${world.start}–${world.end} · ${world.subtitle}", color = B44Ui.MutedText, fontSize = 9.sp)
            Spacer(Modifier.height(9.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
                Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(5.dp).background(world.accent, CircleShape))
            }
            Spacer(Modifier.height(5.dp))
            Text(if (open) "$done/$total" else "Önceki temayı %50 tamamla", color = Color.White.copy(alpha = .35f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun WordZWorldSheet(world: B44Country, completed: Set<Int>, unlocked: Int, close: () -> Unit, play: () -> Unit) {
    val indices = base44CountryLevelIndices(world.id)
    val total = indices.size
    val done = indices.count { it in completed }
    val stars = if (total == 0) 0 else ((done * 5) / total).coerceIn(0, 5)
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .66f)).clickable(onClick = close), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF13223B), Color(0xFF07101E))))
                .border(1.dp, world.accent.copy(alpha = .28f), RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .clickable(enabled = false) {}.padding(22.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.width(46.dp).height(4.dp).background(Color.White.copy(alpha = .12f), CircleShape))
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.size(98.dp).clip(RoundedCornerShape(28.dp))
                    .background(Brush.radialGradient(listOf(world.accent.copy(alpha = .30f), Color.White.copy(alpha = .03f))))
                    .border(1.dp, world.accent.copy(alpha = .25f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(world.emoji, fontSize = 48.sp) }
            Spacer(Modifier.height(10.dp))
            Text(world.name, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("$total BÖLÜM", color = world.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text(world.subtitle, color = B44Ui.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Row { repeat(5) { i -> Icon(Icons.Filled.Star, null, tint = if (i < stars) B44V2Colors.Gold else Color.White.copy(alpha = .12f), modifier = Modifier.size(16.dp)) } }
            Spacer(Modifier.height(8.dp))
            Text("$done/$total tamamlandı", color = Color.White.copy(alpha = .52f), fontSize = 10.sp)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(world.accent, world.accent.copy(alpha = .72f))))
                    .clickable(onClick = play),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (unlocked in indices) "DEVAM ET" else if (done > 0) "TEKRAR OYNA" else "BAŞLA", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

private data class WordZNavItem(val screen: B44V2Screen, val icon: ImageVector, val label: String)

@Composable
private fun WordZBottomNav(active: B44V2Screen, modifier: Modifier = Modifier, tab: (B44V2Screen) -> Unit) {
    val items = remember {
        listOf(
            WordZNavItem(B44V2Screen.HOME, Icons.Filled.Home, "Ana Sayfa"),
            WordZNavItem(B44V2Screen.MAP, Icons.Filled.Map, "Bölümler"),
            WordZNavItem(B44V2Screen.MARKET, Icons.Filled.ShoppingBag, "Market"),
            WordZNavItem(B44V2Screen.PROFILE, Icons.Filled.Person, "Profil"),
        )
    }
    val activeIndex = items.indexOfFirst { it.screen == active }.coerceAtLeast(0)
    Box(
        modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, B44V2Colors.Deep.copy(alpha = .99f))))
            .padding(horizontal = 14.dp, vertical = 8.dp).navigationBarsPadding()
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xF20E1B2E), Color(0xFA081321))))
                .border(1.dp, B44Ui.Border, RoundedCornerShape(20.dp))
        ) {
            val tabWidth = maxWidth / items.size
            val indicatorX by animateDpAsState(tabWidth * activeIndex + (tabWidth - 30.dp) / 2, spring(stiffness = 420f, dampingRatio = .78f), label = "indicator")
            Box(Modifier.offset(x = indicatorX).size(width = 30.dp, height = 3.dp).background(B44V2Colors.Emerald, CircleShape))
            Row(Modifier.fillMaxSize().padding(horizontal = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                items.forEach { item ->
                    val selected = item.screen == active
                    val s by animateFloatAsState(if (selected) 1.06f else 1f, spring(stiffness = 500f, dampingRatio = .72f), label = item.label)
                    Column(
                        Modifier.weight(1f).height(58.dp).scale(s).clip(RoundedCornerShape(15.dp))
                            .background(if (selected) B44V2Colors.Emerald.copy(alpha = .09f) else Color.Transparent)
                            .clickable { tab(item.screen) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(item.icon, null, tint = if (selected) B44V2Colors.Emerald else Color.White.copy(alpha = .35f), modifier = Modifier.size(20.dp))
                        Text(item.label, color = if (selected) B44V2Colors.Emerald else Color.White.copy(alpha = .30f), fontSize = 9.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
        }
    }
}
