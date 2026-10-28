package com.zafer.wordz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class Base44V6MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureBase44CountryCatalog()
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
            ) { Base44V6App() }
        }
    }
}

@Composable
private fun Base44V6App() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(B44V2_PREFS, Context.MODE_PRIVATE) }
    val appState = remember { WordZAppState(prefs) }
    val profileStore = remember { Base44UserProfileStore(prefs) }
    var showSplash by remember { mutableStateOf(true) }
    var screen by remember { mutableStateOf(B44V2Screen.HOME) }

    fun openLevel(index: Int) {
        if (!appState.select(index)) {
            GameToastCenter.warning("Bu bölümün içeriği henüz yüklenmemiş.")
            return
        }
        screen = B44V2Screen.GAME
    }

    Box(Modifier.fillMaxSize()) {
        when (screen) {
            B44V2Screen.HOME -> V6Home(
                state = appState,
                profileStore = profileStore,
                onPlay = { openLevel(appState.unlocked) },
                onTab = { screen = it },
            )
            B44V2Screen.MAP -> V6Map(
                state = appState,
                back = { screen = B44V2Screen.HOME },
                onOpenLevel = ::openLevel,
                onTab = { screen = it },
            )
            B44V2Screen.GAME -> Base44CanonicalGameScreen(
                levelIndex = appState.selectedLevel,
                coins = appState.coins,
                hints = appState.hints,
                completedBefore = appState.selectedLevel in appState.completed,
                onCoins = appState::setCoins,
                onHints = appState::setHints,
                onRewardXp = appState::grantXp,
                onComplete = {
                    appState.completeSelected()
                    GameToastCenter.success("Bölüm ${appState.selectedLevel + 1} tamamlandı!")
                },
                back = { screen = B44V2Screen.MAP },
                next = {
                    val next = appState.selectedLevel + 1
                    if (appState.canOpen(next)) openLevel(next) else screen = B44V2Screen.MAP
                },
                home = { screen = B44V2Screen.HOME },
            )
            B44V2Screen.MARKET -> B44V5Market(
                appState.coins,
                appState.hints,
                appState::setCoins,
                appState::setHints,
            ) { screen = it }
            B44V2Screen.PROFILE -> B44V5Profile(
                appState.coins,
                appState.hints,
                appState.player,
                appState.completed,
            ) { screen = it }
        }

        GameToastHost(Modifier.align(Alignment.TopCenter))
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut(tween(700)) + scaleOut(targetScale = 1.05f),
        ) {
            V6Splash { showSplash = false }
        }
    }
}

@Composable
private fun V6Splash(done: () -> Unit) {
    var progress by remember { mutableStateOf(0) }
    var entered by remember { mutableStateOf(false) }
    val logoAlpha by animateFloatAsState(if (entered) 1f else 0f, tween(650), label = "splashAlpha")
    val logoScale by animateFloatAsState(if (entered) 1f else .90f, spring(stiffness = 250f, dampingRatio = .78f), label = "splashScale")
    val logoY by animateFloatAsState(if (entered) 0f else 40f, tween(650), label = "splashY")
    val spin = rememberInfiniteTransition(label = "splashSpark")
    val sparkleRotation by spin.animateFloat(0f, 360f, infiniteRepeatable(tween(8000)), label = "sparkleRotation")

    LaunchedEffect(Unit) {
        entered = true
        repeat(50) {
            delay(38)
            progress = (it + 1) * 2
        }
        delay(400)
        done()
    }

    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF040C18), Color(0xFF071420), Color(0xFF030A14)))),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(420.dp).align(Alignment.TopStart).padding(top = 10.dp)
                .background(Brush.radialGradient(listOf(B44V2Colors.Emerald.copy(alpha = .14f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.size(340.dp).align(Alignment.BottomEnd)
                .background(Brush.radialGradient(listOf(B44V2Colors.Cyan.copy(alpha = .10f), Color.Transparent)), CircleShape)
        )
        B44AmbientStars(seed = 144, count = 25)
        Column(
            Modifier.graphicsLayerCompat(alpha = logoAlpha, scaleX = logoScale, scaleY = logoScale, translationY = logoY),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, B44V2Colors.Emerald))))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.size(8.dp).background(B44V2Colors.Emerald, CircleShape))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, Color.Transparent))))
            }
            Spacer(Modifier.height(22.dp))
            Text("WORDS", color = Color.White, fontSize = 58.sp, lineHeight = 58.sp, fontWeight = FontWeight.Black)
            Box(contentAlignment = Alignment.TopEnd) {
                Text(
                    "Z",
                    color = B44V2Colors.Emerald,
                    fontSize = 104.sp,
                    lineHeight = 90.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.shadow(24.dp, CircleShape, ambientColor = B44V2Colors.Emerald.copy(alpha = .35f), spotColor = B44V2Colors.Emerald.copy(alpha = .35f)),
                )
                Text(
                    "✦",
                    color = B44V2Colors.Gold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 2.dp).graphicsLayerCompat(rotationZ = sparkleRotation),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("KELİME BULMACA", color = Color.White.copy(alpha = .30f), fontSize = 10.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(58.dp))
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
private fun V6Home(
    state: WordZAppState,
    profileStore: Base44UserProfileStore,
    onPlay: () -> Unit,
    onTab: (B44V2Screen) -> Unit,
) {
    val profile = profileStore.snapshot().copy(completedLevels = state.completed)
    var heroVisible by remember { mutableStateOf(false) }
    var modesVisible by remember { mutableStateOf(false) }
    var boardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(220); heroVisible = true
        delay(420); modesVisible = true
        delay(160); boardVisible = true
    }

    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp).padding(bottom = 104.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    V6Chip("◆", state.coins.toString(), B44V2Colors.Gold)
                    V6Chip("⚡", "Lv.${state.player.level}   ${state.player.totalXp} XP", B44V2Colors.Emerald)
                }
                Spacer(Modifier.height(24.dp))
                AnimatedVisibility(
                    visible = heroVisible,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700), initialOffsetY = { 36 }),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, B44V2Colors.Emerald.copy(alpha = .55f)))))
                            Spacer(Modifier.width(10.dp))
                            Text("KELİME OYUNU", color = B44V2Colors.Emerald.copy(alpha = .65f), fontSize = 10.sp, letterSpacing = 3.sp)
                            Spacer(Modifier.width(10.dp))
                            Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald.copy(alpha = .55f), Color.Transparent))))
                        }
                        Spacer(Modifier.height(13.dp))
                        Text("WORDS", color = Color.White, fontSize = 52.sp, lineHeight = 50.sp, fontWeight = FontWeight.Black)
                        Text("Z", color = B44V2Colors.Emerald, fontSize = 86.sp, lineHeight = 74.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(20.dp))
                        V6Play(onPlay)
                        Spacer(Modifier.height(18.dp))
                        Row(
                            Modifier.clip(CircleShape).background(Color.White.copy(alpha = .045f))
                                .border(1.dp, Color.White.copy(alpha = .09f), CircleShape)
                                .padding(horizontal = 18.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val pulse = rememberInfiniteTransition(label = "levelPulse")
                            val dotAlpha by pulse.animateFloat(.35f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "dotAlpha")
                            Box(Modifier.size(8.dp).alpha(dotAlpha).background(B44V2Colors.Emerald, CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("Bölüm ${state.unlocked + 1} aktif", color = Color.White.copy(alpha = .62f), fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
                AnimatedVisibility(
                    visible = modesVisible,
                    enter = fadeIn(tween(550)) + slideInVertically(tween(550), initialOffsetY = { 28 }),
                ) {
                    Column {
                        Text("OYUN MODU", color = Color.White.copy(alpha = .30f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            V6Mode("📖", "Klasik", "Kelimeleri bul, dünyayı keşfet", B44V2Colors.Emerald, false, onPlay, Modifier.weight(1f))
                            V6Mode("⚡", "Hız Modu", "Süre yarışı — ne kadar hızlısın?", B44V2Colors.Orange, true, { GameToastCenter.info("Hız Modu yakında.") }, Modifier.weight(1f))
                            V6Mode("🏆", "Günlük Görev", "Her gün yeni meydan okuma", B44V2Colors.Gold, true, { GameToastCenter.info("Günlük Görev yakında.") }, Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                AnimatedVisibility(
                    visible = boardVisible,
                    enter = fadeIn(tween(550)) + slideInVertically(tween(550), initialOffsetY = { 24 }),
                ) { V6Leaderboard(profile) }
            }
            B44V2BottomNav(B44V2Screen.HOME, Modifier.align(Alignment.BottomCenter), onTab)
        }
    }
}

@Composable
private fun V6Chip(symbol: String, value: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .05f))
            .border(1.dp, Color.White.copy(alpha = .09f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(21.dp).background(Brush.linearGradient(listOf(tint, tint.copy(alpha = .65f))), CircleShape), contentAlignment = Alignment.Center) {
            Text(symbol, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(7.dp))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun V6Play(click: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "v6Play")
    val outer by transition.animateFloat(1f, 1.24f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "outer")
    val inner by transition.animateFloat(.96f, 1.04f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "inner")
    Box(Modifier.size(194.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(170.dp).scale(outer).alpha(.16f).border(1.dp, B44V2Colors.Emerald.copy(alpha = .28f), CircleShape))
        Box(Modifier.size(154.dp).scale(outer * .96f).alpha(.17f).background(B44V2Colors.Emerald.copy(alpha = .12f), CircleShape))
        Column(
            Modifier.size(144.dp).scale(inner).shadow(30.dp, CircleShape, ambientColor = B44V2Colors.Emerald.copy(alpha = .45f), spotColor = B44V2Colors.Emerald.copy(alpha = .45f))
                .clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF1AE896), B44V2Colors.Emerald, B44V2Colors.EmeraldDark)))
                .border(1.dp, Color.White.copy(alpha = .16f), CircleShape).clickable(onClick = click),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(42.dp))
            Text("OYNA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun V6Mode(emoji: String, title: String, description: String, tint: Color, locked: Boolean, click: () -> Unit, modifier: Modifier) {
    Box(
        modifier.height(128.dp).clip(RoundedCornerShape(17.dp))
            .background(Brush.linearGradient(listOf(tint.copy(alpha = .18f), tint.copy(alpha = .045f))))
            .border(1.dp, tint.copy(alpha = .30f), RoundedCornerShape(17.dp))
            .alpha(if (locked) .60f else 1f).clickable(onClick = click).padding(9.dp)
    ) {
        if (locked) Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = .35f), modifier = Modifier.size(13.dp).align(Alignment.TopEnd))
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 23.sp)
            Text(title, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(description, color = Color.White.copy(alpha = .40f), fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
        }
    }
}

@Composable
private fun V6Leaderboard(profile: Base44LocalProfile) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .04f))
            .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(18.dp))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Liderlik Tablosu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Yerel profil", color = Color.White.copy(alpha = .30f), fontSize = 8.sp)
            }
        }
        Row(
            Modifier.fillMaxWidth().background(B44V2Colors.Gold.copy(alpha = .05f)).padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🥇", fontSize = 14.sp, modifier = Modifier.width(30.dp))
            Box(Modifier.size(30.dp).background(B44V2Colors.Emerald.copy(alpha = .35f), CircleShape), contentAlignment = Alignment.Center) {
                Text(profile.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(profile.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Lv.${profile.playerLevel} · ${profile.completedLevels.size} bölüm", color = Color.White.copy(alpha = .35f), fontSize = 9.sp)
            }
            Text(profile.score.toString(), color = B44V2Colors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun V6CountryUnlocked(country: B44Country, completed: Set<Int>): Boolean {
    val index = base44CanonicalCountries.indexOfFirst { it.id == country.id }
    if (index <= 0) return true
    val previous = base44CanonicalCountries[index - 1]
    return (previous.start - 1..previous.end - 1).count { it in completed } >= 10
}

@Composable
private fun V6Map(
    state: WordZAppState,
    back: () -> Unit,
    onOpenLevel: (Int) -> Unit,
    onTab: (B44V2Screen) -> Unit,
) {
    var selected by remember { mutableStateOf<B44Country?>(null) }
    var cardsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(160); cardsVisible = true }

    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 92.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .07f))
                            .border(1.dp, Color.White.copy(alpha = .10f), RoundedCornerShape(12.dp)).clickable(onClick = back),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dünya Haritası", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("${state.levelCount}/120 bölüm verisi yüklü", color = Color.White.copy(alpha = .38f), fontSize = 10.sp)
                    }
                    Text("${base44CanonicalCountries.count { V6CountryUnlocked(it, state.completed) }}/6", color = B44V2Colors.Emerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 20.dp)) {
                    base44CanonicalCountries.forEachIndexed { index, country ->
                        val open = V6CountryUnlocked(country, state.completed)
                        val done = (country.start - 1..country.end - 1).count { it in state.completed }
                        val active = (state.unlocked + 1) in country.start..country.end
                        val hasContent = country.start <= state.levelCount
                        AnimatedVisibility(
                            visible = cardsVisible,
                            enter = fadeIn(tween(420, delayMillis = index * 75)) + slideInVertically(tween(420, delayMillis = index * 75), initialOffsetY = { 28 }),
                        ) {
                            V6CountryCard(country, done, open, active, hasContent) { if (open) selected = country }
                        }
                        if (index < base44CanonicalCountries.lastIndex) {
                            Box(Modifier.padding(start = 25.dp).width(2.dp).height(22.dp).background(Color.White.copy(alpha = .08f)))
                        }
                    }
                }
            }
            B44V2BottomNav(B44V2Screen.MAP, Modifier.align(Alignment.BottomCenter), onTab)
            AnimatedVisibility(
                visible = selected != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            ) {
                selected?.let { country ->
                    V6CountrySheet(country, state.completed, state.unlocked, state.levelCount, close = { selected = null }) {
                        val first = country.start - 1
                        val candidate = if ((state.unlocked + 1) in country.start..country.end) state.unlocked else first
                        if (candidate in 0 until state.levelCount) onOpenLevel(candidate)
                        else GameToastCenter.warning("${country.name} bölüm verileri henüz yüklenmedi.")
                        selected = null
                    }
                }
            }
        }
    }
}

@Composable
private fun V6CountryCard(country: B44Country, done: Int, open: Boolean, active: Boolean, hasContent: Boolean, click: () -> Unit) {
    val progress = (done / 20f).coerceIn(0f, 1f)
    Row(
        Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(20.dp))
            .background(
                if (active) Brush.linearGradient(listOf(country.accent.copy(alpha = .15f), Color.White.copy(alpha = .03f)))
                else Brush.linearGradient(listOf(Color.White.copy(alpha = .045f), Color.White.copy(alpha = .02f)))
            )
            .border(1.dp, if (active) country.accent.copy(alpha = .38f) else Color.White.copy(alpha = .08f), RoundedCornerShape(20.dp))
            .alpha(if (open) 1f else .48f).clickable(enabled = open, onClick = click).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(54.dp).shadow(if (active) 12.dp else 0.dp, RoundedCornerShape(17.dp), ambientColor = country.accent.copy(alpha = .25f), spotColor = country.accent.copy(alpha = .25f))
                .clip(RoundedCornerShape(17.dp)).background(Brush.linearGradient(listOf(country.accent.copy(alpha = .32f), country.accent.copy(alpha = .10f))))
                .border(1.dp, country.accent.copy(alpha = .34f), RoundedCornerShape(17.dp)),
            contentAlignment = Alignment.Center,
        ) { if (!open) Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = .45f)) else Text(country.emoji, fontSize = 27.sp) }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(country.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                if (active) {
                    Spacer(Modifier.width(8.dp))
                    Text("● Aktif", color = country.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("${country.start}–${country.end} · ${country.subtitle}", color = Color.White.copy(alpha = .38f), fontSize = 9.sp)
            Spacer(Modifier.height(9.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
                Box(Modifier.fillMaxWidth(progress).height(5.dp).background(country.accent, CircleShape))
            }
            Spacer(Modifier.height(5.dp))
            Text(
                when {
                    !open -> "Önceki bölgeyi %50 tamamla"
                    !hasContent -> "Bölüm verisi bekleniyor"
                    else -> "$done/20"
                },
                color = Color.White.copy(alpha = .35f),
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
private fun V6CountrySheet(
    country: B44Country,
    completed: Set<Int>,
    unlocked: Int,
    levelCount: Int,
    close: () -> Unit,
    play: () -> Unit,
) {
    val done = (country.start - 1..country.end - 1).count { it in completed }
    val hasContent = country.start <= levelCount
    val stars = ((done * 5) / 20).coerceIn(0, 5)
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .66f)).clickable(onClick = close), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF13223B), Color(0xFF07101E))))
                .border(1.dp, country.accent.copy(alpha = .28f), RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .clickable(enabled = false) {}.padding(22.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.width(46.dp).height(4.dp).background(Color.White.copy(alpha = .12f), CircleShape))
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                    .background(Brush.radialGradient(listOf(country.accent.copy(alpha = .28f), Color.White.copy(alpha = .03f))))
                    .border(1.dp, country.accent.copy(alpha = .24f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(country.emoji, fontSize = 46.sp) }
            Spacer(Modifier.height(10.dp))
            Text(country.name, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("20 BÖLÜM", color = country.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text(country.subtitle, color = Color.White.copy(alpha = .45f), fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i -> Icon(Icons.Filled.Star, null, tint = if (i < stars) B44V2Colors.Gold else Color.White.copy(alpha = .12f), modifier = Modifier.size(16.dp)) }
            }
            Spacer(Modifier.height(8.dp))
            Text("$done/20 tamamlandı", color = Color.White.copy(alpha = .52f), fontSize = 10.sp)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (hasContent) Brush.horizontalGradient(listOf(country.accent, country.accent.copy(alpha = .72f))) else Brush.horizontalGradient(listOf(Color.White.copy(alpha = .08f), Color.White.copy(alpha = .05f))))
                    .clickable(enabled = hasContent, onClick = play),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (!hasContent) "İÇERİK BEKLENİYOR" else if ((unlocked + 1) in country.start..country.end) "DEVAM ET" else if (done > 0) "TEKRAR OYNA" else "BAŞLA",
                    color = if (hasContent) Color.White else Color.White.copy(alpha = .35f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}
